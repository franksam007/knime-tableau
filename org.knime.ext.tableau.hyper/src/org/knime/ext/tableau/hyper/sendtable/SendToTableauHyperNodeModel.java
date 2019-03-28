/*
 * ------------------------------------------------------------------------
 *
 *  Copyright by KNIME AG, Zurich, Switzerland
 *  Website: http://www.knime.com; Email: contact@knime.com
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License, Version 3, as
 *  published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, see <http://www.gnu.org/licenses>.
 *
 *  Additional permission under GNU GPL version 3 section 7:
 *
 *  KNIME interoperates with ECLIPSE solely via ECLIPSE's plug-in APIs.
 *  Hence, KNIME and ECLIPSE are both independent programs and are not
 *  derived from each other. Should, however, the interpretation of the
 *  GNU GPL Version 3 ("License") under any applicable laws result in
 *  KNIME and ECLIPSE being a combined program, KNIME AG herewith grants
 *  you the additional permission to use and propagate KNIME together with
 *  ECLIPSE with only the license terms in place for ECLIPSE applying to
 *  ECLIPSE and the GNU GPL Version 3 applying for KNIME, provided the
 *  license terms of ECLIPSE themselves allow for the respective use and
 *  propagation of ECLIPSE together with KNIME.
 *
 *  Additional permission relating to nodes for KNIME that extend the Node
 *  Extension (and in particular that are based on subclasses of NodeModel,
 *  NodeDialog, and NodeView) and that only interoperate with KNIME through
 *  standard APIs ("Nodes"):
 *  Nodes are deemed to be separate and independent programs and to not be
 *  covered works.  Notwithstanding anything to the contrary in the
 *  License, the License does not apply to Nodes, you are not required to
 *  license Nodes under the License, and you are granted a license to
 *  prepare and propagate Nodes, in each case even if such Nodes are
 *  propagated with or for interoperation with KNIME.  The owner of a Node
 *  may freely choose the license terms applicable to such Node, including
 *  when such Node is propagated with or for interoperation with KNIME.
 * ---------------------------------------------------------------------
 *
 * History
 *   Feb 5, 2016 (wiswedel): created
 */
package org.knime.ext.tableau.hyper.sendtable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.util.CheckUtils;
import org.knime.core.util.FileUtil;
import org.knime.ext.tableau.TableauExtract;
import org.knime.ext.tableau.TableauExtractAPI;
import org.knime.ext.tableau.TableauExtractOpener;
import org.knime.ext.tableau.TableauPlugin;
import org.knime.ext.tableau.TableauPlugin.TABLEAU_SDK;
import org.knime.ext.tableau.TableauTable;
import org.knime.ext.tableau.hyper.TableauHyperExtractAPI;
import org.knime.ext.tableau.hyper.TableauHyperExtractOpener;
import org.knime.ext.tableau.hyper.sendtable.SendToTableauHyperSettings.FileOverwritePolicy;
import org.knime.ext.tableau.hyper.sendtable.api.RestApiConnection;
import org.knime.ext.tableau.hyper.sendtable.api.RestApiConnection.TsResponseException;
import org.knime.ext.tableau.hyper.sendtable.api.binding.DataSourceListType;

/** @author Benjamin Wilhelm, KNIME GmbH, Konstanz, Germany */
final class SendToTableauHyperNodeModel extends NodeModel {

    private static final String EXTRACT_TABLE_NAME = "Extract";

    private static final NodeLogger LOG = NodeLogger.getLogger(SendToTableauHyperNodeModel.class);

    private SendToTableauHyperSettings m_settings;

    SendToTableauHyperNodeModel() {
        super(1, 0);
    }

    @Override
    protected DataTableSpec[] configure(final DataTableSpec[] inSpecs) throws InvalidSettingsException {
        CheckUtils.checkSettingNotNull(m_settings, "No configuration available");

        if (TableauPlugin.getSelectedSDK() != TABLEAU_SDK.HYPER) {
            throw new InvalidSettingsException("This nodes requires the '" + TABLEAU_SDK.HYPER
                + "' backend, but the active backend is: '" + TableauPlugin.getSelectedSDK().toString() + "'"
                + " The Tableau backend can be configured in the Tableau preference page.");
        }

        // TODO check overwrite/append?
        return new DataTableSpec[]{};
    }

    @Override
    protected BufferedDataTable[] execute(final BufferedDataTable[] inData, final ExecutionContext exec)
        throws Exception {
        // Write table to a temporary hyper file
        final BufferedDataTable table = inData[0];
        final ExecutionMonitor writeProgress = exec.createSubProgress(0.5);
        long rowIndex = 0L;
        final long rowCount = table.size();

        // Prepare the temporary file
        final File f = FileUtil.createTempFile("tableau-", ".hyper");
        Files.delete(f.toPath());

        // Create the API Helpers
        final TableauExtractAPI extractAPI = new TableauHyperExtractAPI();
        final TableauExtractOpener extractOpener = new TableauHyperExtractOpener();

        synchronized (TableauHyperExtractAPI.class) {
            try {
                extractAPI.initialize();
            } catch (Throwable e) {
                LOG.debug(e);
                throw new InvalidSettingsException(
                    "Unable to initialize Tableau backend '" + TABLEAU_SDK.HYPER.toString()
                        + "', please follow the installation instructions in the node description. Error:" + e);
            }
            try (final TableauExtract tableauExtract = extractOpener.openExtract(f.getAbsolutePath())) {
                final TableauTable tableWriter =
                    tableauExtract.createTable(EXTRACT_TABLE_NAME, table.getDataTableSpec());
                // Add rows to the table
                for (final DataRow r : table) {
                    tableWriter.addRow(r);
                    writeProgress.setProgress((double)++rowIndex / rowCount,
                        String.format("Row %d/%d (\"%s\")", rowIndex, rowCount, r.getKey().toString()));
                    writeProgress.checkCanceled();
                }
            } finally {
                extractAPI.cleanup();
            }
        }

        // Send the file to the tableau server
        final ExecutionMonitor sendProgress = exec.createSubProgress(0.5);

        final RestApiConnection restApi = new RestApiConnection(m_settings.getHost());

        // Sign in
        signIn(restApi);
        final boolean overwrite = m_settings.getOverwrite() == FileOverwritePolicy.OVERWRITE;
        final boolean append = m_settings.getOverwrite() == FileOverwritePolicy.APPEND;
        final boolean exists = checkOverwriteAppend(restApi, m_settings.getProjectId(), overwrite, append);
        restApi.invokePublishDataSourceChunked(m_settings.getProjectId(), m_settings.getDatasourceName(), "hyper", f,
            exists && overwrite, exists && append, sendProgress);

        // Return an empty array
        return new BufferedDataTable[]{};
    }

    private void signIn(final RestApiConnection restApi) throws TsResponseException {
        restApi.invokeSignIn(m_settings.getUsername(), m_settings.getPassword(), m_settings.getSiteContentURL());
    }

    private boolean checkOverwriteAppend(final RestApiConnection restApi, final String projectId,
        final boolean overwrite, final boolean append) throws TsResponseException, InvalidSettingsException {
        final String datasourceName = m_settings.getDatasourceName();
        final DataSourceListType datasources =
            restApi.invokeQueryDatasources(RestApiConnection.eqFilterExpression("name", datasourceName));
        final boolean exits = datasources.getDatasource().stream() //
            .filter(d -> d.getName().equals(datasourceName)) //
            .anyMatch(d -> d.getProject().getId().equals(projectId));
        if (exits && !overwrite && !append) {
            // File exists but abort is selected
            throw new InvalidSettingsException(
                "A datasource with the name " + datasourceName + " exists in the configured project.");
        }
        return exits;
    }

    @Override
    protected void reset() {
        // nothing to do
    }

    @Override
    protected void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException {
        new SendToTableauHyperSettings().loadSettingsInModel(settings);
    }

    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings) throws InvalidSettingsException {
        m_settings = new SendToTableauHyperSettings().loadSettingsInModel(settings);
    }

    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
        if (m_settings != null) {
            m_settings.saveSettings(settings);
        }
    }

    @Override
    protected void loadInternals(final File nodeInternDir, final ExecutionMonitor exec)
        throws IOException, CanceledExecutionException {
        // nothing to do
    }

    @Override
    protected void saveInternals(final File nodeInternDir, final ExecutionMonitor exec)
        throws IOException, CanceledExecutionException {
        // nothing to do
    }
}
