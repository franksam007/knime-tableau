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
package org.knime.ext.tableau.sendtotableau;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
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
import org.knime.ext.tableau.TableauTableWriter;

import com.tableausoftware.extract.ExtractAPI;
import com.tableausoftware.server.ServerAPI;
import com.tableausoftware.server.ServerConnection;

/**
 *
 * @author wiswedel
 */
final class SendToTableauNodeModel extends NodeModel {

    private SendToTableauSettings m_settings;

    SendToTableauNodeModel() {
        super(1, 0);
    }

    /** {@inheritDoc} */
    @Override
    protected DataTableSpec[] configure(final DataTableSpec[] inSpecs) throws InvalidSettingsException {
        CheckUtils.checkSettingNotNull(m_settings, "No configuration available");
        return new DataTableSpec[]{};
    }

    /** {@inheritDoc} */
    @Override
    protected BufferedDataTable[] execute(final BufferedDataTable[] inData,
        final ExecutionContext exec) throws Exception {
        SendToTableauSettings s = m_settings;
        BufferedDataTable table = inData[0];
        long rowIndex = 0L;
        final long rowCount = table.size();
        File t = FileUtil.createTempFile("tableau-", ".tde");
        t.delete(); // just need a unique file name -- must not exist
        try {
            getLogger().debugWithFormat("Will write temporary tableau file to \"%s\"", t.getAbsolutePath());
            ExtractAPI.initialize();
            try (TableauTableWriter tableWriter = TableauTableWriter.create(t.getAbsolutePath(),
                table.getDataTableSpec())) {
                // This part works ok
                for (DataRow r : table) {
                    tableWriter.addRow(r);
                    exec.setProgress((double)++rowIndex / rowCount, String.format("Row %d/%d (\"%s\")",
                        rowIndex, rowCount, r.getKey().toString()));
                }
                getLogger().debugWithFormat("Successfully written temporary tableau file (\"%s\" - %s)",
                    t.getAbsolutePath(), FileUtils.byteCountToDisplaySize(FileUtils.sizeOf(t)));
            } finally {
                ExtractAPI.cleanup();
            }
            ServerConnection serverConnection = null;
            ServerAPI.initialize();
            try {
                serverConnection = new ServerConnection();
                serverConnection.connect(s.getHost(), s.getUsername(), s.getPassword(), s.getSiteID());
                if (StringUtils.isNotEmpty(s.getProxyUsername())) {
                    serverConnection.setProxyCredentials(s.getProxyUsername(), s.getProxyPassword());
                }
                serverConnection.publishExtract(t.getAbsolutePath(), s.getProjectName(),
                    s.getDatasourceName(), s.isOverwrite());
            } finally {
                if (serverConnection != null) {
                    serverConnection.disconnect();
                }
            }
        } catch (UnsatisfiedLinkError e) {
            throw new Exception(e.getMessage() + " (follow \"Installation\" steps described in node description)", e);
        } finally {
            t.delete();
        }
        return new BufferedDataTable[] {};
    }

    /** {@inheritDoc} */
    @Override
    protected void reset() {
    }

    /** {@inheritDoc} */
    @Override
    protected void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException {
        new SendToTableauSettings().loadSettingsInModel(settings);
    }

    /** {@inheritDoc} */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings) throws InvalidSettingsException {
        m_settings = new SendToTableauSettings().loadSettingsInModel(settings);
    }

    /** {@inheritDoc} */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
        if (m_settings != null) {
            m_settings.saveSettings(settings);
        }
    }

    /** {@inheritDoc} */
    @Override
    protected void loadInternals(final File nodeInternDir, final ExecutionMonitor exec)
        throws IOException, CanceledExecutionException {
    }

    /** {@inheritDoc} */
    @Override
    protected void saveInternals(final File nodeInternDir, final ExecutionMonitor exec)
        throws IOException, CanceledExecutionException {
    }

}
