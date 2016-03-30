/*
 * ------------------------------------------------------------------------
 *
 *  Copyright by KNIME GmbH, Konstanz, Germany
 *  Website: http://www.knime.org; Email: contact@knime.org
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
 *  KNIME and ECLIPSE being a combined program, KNIME GMBH herewith grants
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
package org.knime.ext.tableau.tdewrite;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

/**
 * TDE Writer Settings Proxy.
 * @author Bernd Wiswedel, KNIME.com, Zurich, Switzerland
 */
final class TableauDESettings {

    static final String CFG_OUTPUT_LOCATION = "outputLocation";

    private String m_outputLocation;
    private boolean m_overwriteOK;

    String getOutputLocation() {
        return m_outputLocation;
    }

    void setOutputLocation(final String outputLocation) {
        m_outputLocation = outputLocation;
    }

    boolean isOverwriteOK() {
        return m_overwriteOK;
    }

    void setOverwriteOK(final boolean overwriteOK) {
        m_overwriteOK = overwriteOK;
    }

    void saveSettings(final NodeSettingsWO settings) {
        settings.addString(TableauDESettings.CFG_OUTPUT_LOCATION, m_outputLocation);
        settings.addBoolean("overwriteOK", m_overwriteOK);
    }

    TableauDESettings loadSettingsInDialog(final NodeSettingsRO settings) {
        m_outputLocation = settings.getString(TableauDESettings.CFG_OUTPUT_LOCATION, "");
        m_overwriteOK = settings.getBoolean("overwriteOK", false);
        return this;
    }

    TableauDESettings loadSettingsInModel(final NodeSettingsRO settings) throws InvalidSettingsException {
        m_overwriteOK = settings.getBoolean("overwriteOK");
        m_outputLocation = settings.getString(TableauDESettings.CFG_OUTPUT_LOCATION);
        return this;
    }

}
