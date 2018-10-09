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

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

/**
 * TDE Writer Settings Proxy.
 *
 * @author Bernd Wiswedel, KNIME AG, Zurich, Switzerland
 * @author Benjamin Wilhelm, KNIME GmbH, Konstanz, Germany
 */
final class TableauExtractSettings {

    static final String CFG_OUTPUT_LOCATION = "outputLocation";

    static final String CFG_OVERWRITE_OK = "overwriteOK";

    static final String CFG_OVERWRITE_POLICY = "overwritePolicy";

    private String m_outputLocation;

    private FileOverwritePolicy m_fileOverwritePolicy;

    /**
     * Policy how to proceed when output file exists (overwrite, abort, append).
     */
    enum FileOverwritePolicy {
            /** Fail during configure/execute. */
            Abort,
            /** Overwrite existing file. */
            Overwrite,
            /** Append to existing file. */
            Append
    }

    String getOutputLocation() {
        return m_outputLocation;
    }

    void setOutputLocation(final String outputLocation) {
        m_outputLocation = outputLocation;
    }

    FileOverwritePolicy getFileOverwritePolicy() {
        return m_fileOverwritePolicy;
    }

    void setFileOverwritePolicy(final FileOverwritePolicy fileOverwritePolicy) {
        if (fileOverwritePolicy == null) {
            m_fileOverwritePolicy = FileOverwritePolicy.Abort;
        } else {
            m_fileOverwritePolicy = fileOverwritePolicy;
        }
    }

    void saveSettings(final NodeSettingsWO settings) {
        settings.addString(CFG_OUTPUT_LOCATION, m_outputLocation);
        settings.addString(CFG_OVERWRITE_POLICY, m_fileOverwritePolicy.toString());
    }

    TableauExtractSettings loadSettings(final NodeSettingsRO settings) throws InvalidSettingsException {
        m_outputLocation = settings.getString(CFG_OUTPUT_LOCATION, "");
        if (settings.containsKey(CFG_OVERWRITE_POLICY)) { // since v3.7
            final String val = settings.getString(CFG_OVERWRITE_POLICY, FileOverwritePolicy.Abort.toString());
            try {
                m_fileOverwritePolicy = FileOverwritePolicy.valueOf(val);
            } catch (final Exception e) {
                throw new InvalidSettingsException("Unable to parse 'file " + "overwrite policy' field: " + val, e);
            }
        } else if (settings.containsKey(CFG_OVERWRITE_OK)) { // before v3.7
            if (settings.getBoolean(CFG_OVERWRITE_OK, false)) {
                m_fileOverwritePolicy = FileOverwritePolicy.Overwrite;
            } else {
                m_fileOverwritePolicy = FileOverwritePolicy.Abort;
            }
        } else {
            // Default value
            m_fileOverwritePolicy = FileOverwritePolicy.Abort;
        }
        return this;
    }
}
