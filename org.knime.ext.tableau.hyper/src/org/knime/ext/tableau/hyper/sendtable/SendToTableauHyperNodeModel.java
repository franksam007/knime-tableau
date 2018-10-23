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
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.util.CheckUtils;
import org.knime.core.util.FileUtil;
import org.knime.ext.tableau.TableauExtract;
import org.knime.ext.tableau.TableauExtractAPI;
import org.knime.ext.tableau.TableauExtractOpener;
import org.knime.ext.tableau.TableauTable;
import org.knime.ext.tableau.hyper.TableauHyperExtractAPI;
import org.knime.ext.tableau.hyper.TableauHyperExtractOpener;
import org.knime.ext.tableau.hyper.sendtable.SendToTableauHyperSettings.FileOverwritePolicy;
import org.knime.ext.tableau.hyper.sendtable.api.RestApiConnection;
import org.knime.ext.tableau.hyper.sendtable.api.binding.ProjectListType;
import org.knime.ext.tableau.hyper.sendtable.api.binding.ProjectType;

/**
 * @author Benjamin Wilhelm, KNIME GmbH, Konstanz, Germany
 */
final class SendToTableauHyperNodeModel extends NodeModel {

    // TODO change name?
    // TODO make configurable?
    private static final String EXTRACT_TABLE_NAME = "Extract";

    private SendToTableauHyperSettings m_settings;

    SendToTableauHyperNodeModel() {
        super(1, 0);
    }

    @Override
    protected DataTableSpec[] configure(final DataTableSpec[] inSpecs) throws InvalidSettingsException {
        CheckUtils.checkSettingNotNull(m_settings, "No configuration available");
        // TODO check overwrite/append?
        return new DataTableSpec[]{};
    }

    @Override
    protected BufferedDataTable[] execute(final BufferedDataTable[] inData, final ExecutionContext exec)
        throws Exception {
        // Write table to a temporary hyper file
        final BufferedDataTable table = inData[0];
        long rowIndex = 0L;
        final long rowCount = table.size();

        // Prepare the temporary file
        final File f = FileUtil.createTempFile("tableau-", ".hyper");
        Files.delete(f.toPath());

        // Create the API Helpers
        final TableauExtractAPI extractAPI = new TableauHyperExtractAPI();
        final TableauExtractOpener extractOpener = new TableauHyperExtractOpener();

        synchronized (TableauHyperExtractAPI.class) {
            extractAPI.initialize();
            try (final TableauExtract tableauExtract = extractOpener.openExtract(f.getAbsolutePath())) {
                final TableauTable tableWriter =
                    tableauExtract.createTable(EXTRACT_TABLE_NAME, table.getDataTableSpec());
                // Add rows to the table
                for (final DataRow r : table) {
                    tableWriter.addRow(r);
                    exec.setProgress((double)++rowIndex / rowCount,
                        String.format("Row %d/%d (\"%s\")", rowIndex, rowCount, r.getKey().toString()));
                }
            } catch (final UnsatisfiedLinkError e) {
                // TODO move somewhere else?
                throw new IllegalStateException(
                    e.getMessage() + " (follow \"Installation\" steps described in node description)", e);
            } finally {
                extractAPI.cleanup();
            }
        }

        // Send the file to the tableau server
        final RestApiConnection restApi = new RestApiConnection(m_settings.getHost());

        // Sign in
        restApi.invokeSignIn(m_settings.getUsername(), m_settings.getPassword(), m_settings.getSiteContentURL());
        final ProjectListType projects = restApi.invokeQueryProjects();
        // TODO support projects with same name in different parent projects
        final String projectId = getProjectId(projects, m_settings.getProjectName());
        final boolean overwrite = m_settings.getOverwrite() == FileOverwritePolicy.OVERWRITE;
        final boolean append = m_settings.getOverwrite() == FileOverwritePolicy.APPEND;
        restApi.invokePublishDataSourceChunked(projectId, m_settings.getDatasourceName(), "hyper", f, overwrite,
            append);

        // Return an empty array
        return new BufferedDataTable[]{};
    }

    private static String getProjectId(final ProjectListType projectsList, final String name) {
        for (final ProjectType p : projectsList.getProject()) {
            if (p.getName().equals(name)) {
                return p.getId();
            }
        }
        throw new IllegalArgumentException("There is no project with the name '" + name + "'.");
    }

    @Override
    protected void reset() {
        // nothing to do
    }

    @Override
    protected void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException {
        new SendToTableauHyperSettings().loadSettingsInDialog(settings);
    }

    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings) throws InvalidSettingsException {
        m_settings = new SendToTableauHyperSettings().loadSettingsInDialog(settings);
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
