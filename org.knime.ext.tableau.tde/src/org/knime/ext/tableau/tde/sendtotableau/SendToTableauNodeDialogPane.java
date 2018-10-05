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
package org.knime.ext.tableau.tde.sendtotableau;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JSeparator;

import org.knime.core.data.DataTableSpec;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.util.StringHistoryPanel;

/**
 *
 * @author wiswedel
 */
final class SendToTableauNodeDialogPane extends NodeDialogPane {

    private final StringHistoryPanel m_hostPanel;
    private final StringHistoryPanel m_usernamePanel;
    private final JPasswordField m_passwordField;
    private final StringHistoryPanel m_siteIDPanel;

    private final StringHistoryPanel m_projectNamePanel;
    private final StringHistoryPanel m_datasourceNamePanel;
    private final JCheckBox m_overwriteChecker;

    private final StringHistoryPanel m_proxyUsernamePanel;
    private final JPasswordField m_proxyPassword;

    SendToTableauNodeDialogPane() {
        m_hostPanel = new StringHistoryPanel("send-to-tableau-host");
        m_usernamePanel = new StringHistoryPanel("send-to-tableau-username");
        m_passwordField = new JPasswordField();
        m_siteIDPanel = new StringHistoryPanel("send-to-tableau-m_siteID");
        m_projectNamePanel = new StringHistoryPanel("send-to-tableau-projectName");
        m_datasourceNamePanel = new StringHistoryPanel("send-to-tableau-datasourceName");
        m_overwriteChecker = new JCheckBox("Overwrite");
        m_proxyUsernamePanel = new StringHistoryPanel("send-to-tableau-proxyUsername");
        m_proxyPassword = new JPasswordField();
        addTab("Tableau Server Settings", initPanel());
    }

    /**
     * @return
     */
    private JPanel initPanel() {
        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.gridx = gbc.gridy = 0;

        gbc.anchor = GridBagConstraints.EAST;

        p.add(new JLabel("Host "), gbcLabel(gbc));
        gbc.gridx += 1;
        p.add(m_hostPanel, gbcComponent(gbc));
        gbc.gridy += 1;

        gbc.gridx = 0;
        p.add(new JLabel("Username "), gbcLabel(gbc));
        gbc.gridx += 1;
        p.add(m_usernamePanel, gbcComponent(gbc));
        gbc.gridy += 1;

        gbc.gridx = 0;
        p.add(new JLabel("Password "), gbcLabel(gbc));
        gbc.gridx += 1;
        p.add(m_passwordField, gbcComponent(gbc));
        gbc.gridy += 1;

        gbc.gridx = 0;
        p.add(new JLabel("SiteID "), gbcLabel(gbc));
        gbc.gridx += 1;
        p.add(m_siteIDPanel, gbcComponent(gbc));
        gbc.gridy += 1;

        gbc.gridx = 0;
        gbc.gridwidth = 2;
        p.add(new JSeparator(), gbc);
        gbc.gridy += 1;


        gbc.gridwidth = 1;
        gbc.gridx = 0;
        p.add(new JLabel("Project Name "), gbcLabel(gbc));
        gbc.gridx += 1;
        p.add(m_projectNamePanel, gbcComponent(gbc));
        gbc.gridy += 1;

        gbc.gridx = 0;
        p.add(new JLabel("Data Source"), gbcLabel(gbc));
        gbc.gridx += 1;
        p.add(m_datasourceNamePanel, gbcComponent(gbc));
        gbc.gridy += 1;

        p.add(m_overwriteChecker, gbc);

        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.gridy += 1;
        p.add(new JSeparator(), gbc);
        gbc.gridy += 1;


        gbc.gridwidth = 1;
        gbc.gridx = 0;
        p.add(new JLabel("Proxy Username (or empty)"), gbcLabel(gbc));
        gbc.gridx += 1;
        p.add(m_proxyUsernamePanel, gbcComponent(gbc));
        gbc.gridy += 1;

        gbc.gridx = 0;
        p.add(new JLabel("Proxy Password"), gbcLabel(gbc));
        gbc.gridx += 1;
        p.add(m_proxyPassword, gbcComponent(gbc));
        return p;
    }

    private static GridBagConstraints gbcLabel(final GridBagConstraints gbc) {
        gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0.0;
        return gbc;
    }

    private static GridBagConstraints gbcComponent(final GridBagConstraints gbc) {
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        return gbc;
    }

    /** {@inheritDoc} */
    @Override
    protected void loadSettingsFrom(final NodeSettingsRO settings, final DataTableSpec[] specs) {
        SendToTableauSettings s = new SendToTableauSettings().loadSettingsInDialog(settings);
        m_hostPanel.setSelectedString(s.getHost());
        m_usernamePanel.setSelectedString(s.getUsername());
        m_passwordField.setText(s.getPassword());
        m_siteIDPanel.setSelectedString(s.getSiteID());

        m_projectNamePanel.setSelectedString(s.getProjectName());
        m_datasourceNamePanel.setSelectedString(s.getDatasourceName());
        m_overwriteChecker.setSelected(s.isOverwrite());

        m_proxyUsernamePanel.setSelectedString(s.getProxyUsername());
        m_proxyPassword.setText(s.getProxyPassword());
    }

    /** {@inheritDoc} */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) throws InvalidSettingsException {
        SendToTableauSettings s = new SendToTableauSettings();
        s.setHost(m_hostPanel.getSelectedString());
        s.setUsername(m_usernamePanel.getSelectedString());
        s.setPassword(new String(m_passwordField.getPassword()));
        s.setSiteID(m_siteIDPanel.getSelectedString());

        s.setProjectName(m_projectNamePanel.getSelectedString());
        s.setDatasourceName(m_datasourceNamePanel.getSelectedString());
        s.setOverwrite(m_overwriteChecker.isSelected());

        s.setProxyUsername(m_proxyUsernamePanel.getSelectedString());
        s.setProxyPassword(new String(m_proxyPassword.getPassword()));

        s.saveSettings(settings);
    }

}
