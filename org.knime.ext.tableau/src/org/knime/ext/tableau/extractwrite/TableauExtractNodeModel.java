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
package org.knime.ext.tableau.extractwrite;

import java.io.File;
import java.io.IOException;

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
import org.knime.ext.tableau.TableauTable;
import org.knime.ext.tableau.extractwrite.TableauExtractSettings.FileOverwritePolicy;

/**
 * Model for Tableau Extract writer nodes.
 *
 * @author Bernd Wiswedel, KNIME AG, Zurich, Switzerland
 * @author Benjamin Wilhelm, KNIME GmbH, Konstanz, Germany
 */
public final class TableauExtractNodeModel extends NodeModel {

    private static final String EXTRACT_TABLE_NAME = "Extract";

    private static final Object libararyLock = new Object();

    private static final NodeLogger LOG = NodeLogger.getLogger(TableauExtractNodeModel.class);

    private final TableauExtractAPI m_extractAPI;

    private final TableauExtractOpener m_extractCreator;

    private TableauExtractSettings m_settings;

    /**
     * Creates a new node model for writing tableau extracts.
     *
     * @param extractAPI the wrapper to the ExtractAPI to use
     * @param extractOpener the tableau extract creator to use
     */
    public TableauExtractNodeModel(final TableauExtractAPI extractAPI, final TableauExtractOpener extractOpener) {
        super(1, 0);
        m_extractAPI = extractAPI;
        m_extractCreator = extractOpener;
    }

    @Override
    protected DataTableSpec[] configure(final DataTableSpec[] inSpecs) throws InvalidSettingsException {

        if (TableauPlugin.getSelectedSDK() != m_extractAPI.getSDKType()) {
            throw new InvalidSettingsException("This nodes requires the '" + m_extractAPI.getSDKType().toString()
                + "' backend, but the active backend is: '" + TableauPlugin.getSelectedSDK().toString() + "'"
                + " The Tableau backend can be configured in the Tableau preference page.");
        }
        CheckUtils.checkSettingNotNull(m_settings, "No configuration available");
        final boolean overwrite = m_settings.getFileOverwritePolicy() == FileOverwritePolicy.Overwrite;
        final boolean append = m_settings.getFileOverwritePolicy() == FileOverwritePolicy.Append;
        setWarningMessage(CheckUtils.checkDestinationFile(m_settings.getOutputLocation(), overwrite, append));
        return new DataTableSpec[]{};
    }

    @Override
    protected BufferedDataTable[] execute(final BufferedDataTable[] inData, final ExecutionContext exec)
        throws Exception {
        final BufferedDataTable table = inData[0];
        long rowIndex = 0L;
        final long rowCount = table.size();
        final File f = FileUtil.getFileFromURL(FileUtil.toURL(m_settings.getOutputLocation()));
        if (f.exists()) {
            if (m_settings.getFileOverwritePolicy() == FileOverwritePolicy.Overwrite) {
                f.delete();
            } else if (m_settings.getFileOverwritePolicy() == FileOverwritePolicy.Abort) {
                throw new InvalidSettingsException(String.format(
                    "Output file \"%s\" already exists - must not overwrite as per user setting", f.getAbsolutePath()));
            }
        }
        synchronized (libararyLock) {
            try {
                m_extractAPI.initialize();
            } catch (Throwable e) {
                LOG.debug(e);
                throw new InvalidSettingsException(
                    "Unable to initialize Tableau backend '" + m_extractAPI.getSDKType().toString()
                        + "', please follow the installation instructions in the node description.");
            }
            try (final TableauExtract tableauExtract = m_extractCreator.openExtract(f.getAbsolutePath())) {
                TableauTable tableWriter = null;
                if (m_settings.getFileOverwritePolicy() == FileOverwritePolicy.Append) {
                    // If the extract contains this table: Open it
                    if (tableauExtract.hasTable(EXTRACT_TABLE_NAME)) {
                        tableWriter = tableauExtract.openTable(EXTRACT_TABLE_NAME, table.getDataTableSpec());
                    }
                }
                if (tableWriter == null) {
                    // Create the new table
                    tableWriter = tableauExtract.createTable(EXTRACT_TABLE_NAME, table.getDataTableSpec());
                }
                // Add rows to the table
                for (final DataRow r : table) {
                    tableWriter.addRow(r);
                    exec.setProgress((double)++rowIndex / rowCount,
                        String.format("Row %d/%d (\"%s\")", rowIndex, rowCount, r.getKey().toString()));
                }
            } finally {
                m_extractAPI.cleanup();
            }
        }
        return new BufferedDataTable[]{};
    }

    @Override
    protected void reset() {
        // nothing to do here
    }

    @Override
    protected void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException {
        new TableauExtractSettings().loadSettings(settings);
    }

    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings) throws InvalidSettingsException {
        m_settings = new TableauExtractSettings().loadSettings(settings);
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
    }

    @Override
    protected void saveInternals(final File nodeInternDir, final ExecutionMonitor exec)
        throws IOException, CanceledExecutionException {
    }
}
