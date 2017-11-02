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
package org.knime.ext.tableau.sendtotableau;

import org.apache.commons.lang3.StringUtils;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.util.CheckUtils;

/**
 *
 * @author wiswedel
 */
final class SendToTableauSettings {

    private String m_host;
    private String m_username;
    private String m_password;
    private String m_siteID;

    private String m_projectName;
    private String m_datasourceName;
    private boolean m_overwrite;

    private String m_proxyUsername;
    private String m_proxyPassword;

    String getHost() {
        return m_host;
    }
    void setHost(final String host) {
        m_host = host;
    }
    String getUsername() {
        return m_username;
    }
    void setUsername(final String username) {
        m_username = username;
    }
    String getPassword() {
        return m_password;
    }
    void setPassword(final String password) {
        m_password = password;
    }
    String getSiteID() {
        return m_siteID;
    }
    void setSiteID(final String siteID) {
        m_siteID = siteID;
    }
    String getProjectName() {
        return m_projectName;
    }
    void setProjectName(final String projectName) {
        m_projectName = projectName;
    }
    String getDatasourceName() {
        return m_datasourceName;
    }
    void setDatasourceName(final String datasourceName) {
        m_datasourceName = datasourceName;
    }
    boolean isOverwrite() {
        return m_overwrite;
    }
    void setOverwrite(final boolean overwrite) {
        m_overwrite = overwrite;
    }
    String getProxyUsername() {
        return m_proxyUsername;
    }
    void setProxyUsername(final String proxyUsername) {
        m_proxyUsername = proxyUsername;
    }
    String getProxyPassword() {
        return m_proxyPassword;
    }
    void setProxyPassword(final String proxyPassword) {
        m_proxyPassword = proxyPassword;
    }

    void saveSettings(final NodeSettingsWO settings) {
        settings.addString("host", m_host);
        settings.addString("username", m_username);
        settings.addPassword("password-enc", "tableau-knime", m_password);
        settings.addString("siteID", m_siteID);

        settings.addString("projectName", m_projectName);
        settings.addString("datasourceName", m_datasourceName);
        settings.addBoolean("overwrite", m_overwrite);

        settings.addString("proxyUsername", m_proxyUsername);
        settings.addPassword("proxyPassword",  "proxy-tableau-knime", m_proxyPassword);
    }

    SendToTableauSettings loadSettingsInDialog(final NodeSettingsRO settings) {
        m_host = settings.getString("host", "");
        m_username = settings.getString("username", "");
        m_password = settings.getPassword("password-enc", "tableau-knime", "");
        m_siteID = settings.getString("siteID", "");

        m_projectName = settings.getString("projectName", "");
        m_datasourceName = settings.getString("datasourceName", "");
        m_overwrite = settings.getBoolean("overwrite", false);

        m_proxyUsername = settings.getString("proxyUsername", "");
        m_proxyPassword = settings.getPassword("proxyPassword",  "proxy-tableau-knime", "");
        return this;
    }

    SendToTableauSettings loadSettingsInModel(final NodeSettingsRO settings) throws InvalidSettingsException {
        m_host = settings.getString("host");
        CheckUtils.checkSetting(StringUtils.isNotEmpty(m_host), "Host must not be empty");

        m_username = settings.getString("username");
        CheckUtils.checkSetting(StringUtils.isNotEmpty(m_username), "User must not be empty");

        m_password = settings.getPassword("password-enc", "tableau-knime");

        m_siteID = settings.getString("siteID");
        CheckUtils.checkSetting(m_siteID != null, "SiteID must not be null");


        m_projectName = settings.getString("projectName");
        CheckUtils.checkSetting(StringUtils.isNotEmpty(m_projectName), "Project name must not be empty");

        m_datasourceName = settings.getString("datasourceName");
        CheckUtils.checkSetting(StringUtils.isNotEmpty(m_datasourceName), "Data source name must not be empty");

        m_overwrite = settings.getBoolean("overwrite");

        m_proxyUsername = settings.getString("proxyUsername");
        m_proxyPassword = settings.getPassword("proxyPassword",  "proxy-tableau-knime");
        return this;
    }

}
