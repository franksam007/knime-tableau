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

import org.apache.commons.lang3.StringUtils;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.util.CheckUtils;

/**
 *
 * @author wiswedel
 * @author Benjamin Wilhelm, KNIME GmbH, Konstanz, Germany
 */
final class SendToTableauHyperSettings {

    private static final String PASSWORD_ENC = "tableau-knime";

    private static final String CFG_OVERWRITE = "overwrite";

    private static final String CFG_DATASOURCE_NAME = "datasourceName";

    private static final String CFG_PROJECT_NAME = "projectName";

    private static final String CFG_SITE_CONTENT_URL = "siteContentURL";

    private static final String CFG_PASSWORD = "password-enc";

    private static final String CFG_HOST = "host";

    private static final String CFG_USERNAME = "username";

    private String m_host;

    private String m_username;

    private String m_password;

    private String m_siteContentURL;

    private String m_projectName;

    private String m_datasourceName;

    private FileOverwritePolicy m_overwrite;

    /**
     * Policy how to proceed when output file exists (overwrite, abort, append).
     */
    enum FileOverwritePolicy {
            /** Fail during configure/execute. */
            ABORT,
            /** Overwrite existing file. */
            OVERWRITE,
            /** Append to existing file. */
            APPEND
    }

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

    String getSiteContentURL() {
        return m_siteContentURL;
    }

    void setSiteContentURL(final String siteID) {
        m_siteContentURL = siteID;
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

    FileOverwritePolicy getOverwrite() {
        return m_overwrite;
    }

    void setOverwrite(final FileOverwritePolicy overwrite) {
        m_overwrite = overwrite;
    }

    void saveSettings(final NodeSettingsWO settings) {
        settings.addString(CFG_HOST, m_host);
        settings.addString(CFG_USERNAME, m_username);
        settings.addPassword(CFG_PASSWORD, PASSWORD_ENC, m_password);
        settings.addString(CFG_SITE_CONTENT_URL, m_siteContentURL);

        settings.addString(CFG_PROJECT_NAME, m_projectName);
        settings.addString(CFG_DATASOURCE_NAME, m_datasourceName);
        settings.addString(CFG_OVERWRITE, m_overwrite.toString());
    }

    SendToTableauHyperSettings loadSettingsInDialog(final NodeSettingsRO settings) {
        m_host = settings.getString(CFG_HOST, "");
        m_username = settings.getString(CFG_USERNAME, "");
        m_password = settings.getPassword(CFG_PASSWORD, PASSWORD_ENC, "");
        m_siteContentURL = settings.getString(CFG_SITE_CONTENT_URL, "");

        m_projectName = settings.getString(CFG_PROJECT_NAME, "");
        m_datasourceName = settings.getString(CFG_DATASOURCE_NAME, "");
        m_overwrite =
            FileOverwritePolicy.valueOf(settings.getString(CFG_OVERWRITE, FileOverwritePolicy.ABORT.toString()));
        return this;
    }

    SendToTableauHyperSettings loadSettingsInModel(final NodeSettingsRO settings) throws InvalidSettingsException {
        m_host = settings.getString(CFG_HOST);
        CheckUtils.checkSetting(StringUtils.isNotEmpty(m_host), "Host must not be empty");

        m_username = settings.getString(CFG_USERNAME);
        CheckUtils.checkSetting(StringUtils.isNotEmpty(m_username), "User must not be empty");

        m_password = settings.getPassword(CFG_PASSWORD, PASSWORD_ENC);

        m_siteContentURL = settings.getString(CFG_SITE_CONTENT_URL);
        // Site content URL can be empty: For the default site

        m_projectName = settings.getString(CFG_PROJECT_NAME);
        CheckUtils.checkSetting(StringUtils.isNotEmpty(m_projectName), "Project name must not be empty");

        m_datasourceName = settings.getString(CFG_DATASOURCE_NAME);
        CheckUtils.checkSetting(StringUtils.isNotEmpty(m_datasourceName), "Data source name must not be empty");

        m_overwrite = FileOverwritePolicy.valueOf(settings.getString(CFG_OVERWRITE));
        return this;
    }
}
