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
 */
package org.knime.ext.tableau.hyper.sendtable;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.JSeparator;

import org.knime.core.data.DataTableSpec;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.util.StringHistoryPanel;
import org.knime.ext.tableau.hyper.sendtable.SendToTableauHyperSettings.FileOverwritePolicy;

/**
 *
 * @author Benjamin Wilhelm, KNIME GmbH, Konstanz, Germany
 */
final class SendToTableauHyperNodeDialogPane extends NodeDialogPane {

    private final StringHistoryPanel m_hostPanel;

    private final StringHistoryPanel m_usernamePanel;

    private final JPasswordField m_passwordField;

    private final StringHistoryPanel m_siteContentURLPanel;

    private final StringHistoryPanel m_projectNamePanel;

    private final StringHistoryPanel m_datasourceNamePanel;

    private final JRadioButton m_overwritePolicyAbortButton;

    private final JRadioButton m_overwritePolicyAppendButton;

    private final JRadioButton m_overwritePolicyOverwriteButton;

    SendToTableauHyperNodeDialogPane() {
        m_hostPanel = new StringHistoryPanel("send-to-tableau-host");
        m_usernamePanel = new StringHistoryPanel("send-to-tableau-username");
        m_passwordField = new JPasswordField();
        m_siteContentURLPanel = new StringHistoryPanel("send-to-tableau-siteContentURL");
        m_projectNamePanel = new StringHistoryPanel("send-to-tableau-projectName");
        m_datasourceNamePanel = new StringHistoryPanel("send-to-tableau-datasourceName");

        // Overwrite policy buttons
        m_overwritePolicyAppendButton = new JRadioButton("Append");
        m_overwritePolicyOverwriteButton = new JRadioButton("Overwrite");
        m_overwritePolicyAbortButton = new JRadioButton("Abort");
        final ButtonGroup bg = new ButtonGroup();
        bg.add(m_overwritePolicyAppendButton);
        bg.add(m_overwritePolicyOverwriteButton);
        bg.add(m_overwritePolicyAbortButton);

        addTab("Tableau Server Settings", initPanel());
    }

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
        p.add(new JLabel("Site Content URL "), gbcLabel(gbc));
        gbc.gridx += 1;
        p.add(m_siteContentURLPanel, gbcComponent(gbc));
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

        gbc.gridx = 0;
        p.add(new JLabel(" If file exists... "), gbcLabel(gbc));
        gbc.gridx += 1;

        // Overwrite policy settings (copied from csv writer)
        final JPanel overwriteFilePane = new JPanel();
        overwriteFilePane.setLayout(new BoxLayout(overwriteFilePane, BoxLayout.X_AXIS));
        m_overwritePolicyOverwriteButton.setAlignmentY(Component.TOP_ALIGNMENT);
        overwriteFilePane.add(m_overwritePolicyOverwriteButton);
        overwriteFilePane.add(Box.createHorizontalStrut(20));
        m_overwritePolicyAppendButton.setAlignmentY(Component.TOP_ALIGNMENT);
        overwriteFilePane.add(m_overwritePolicyAppendButton);
        overwriteFilePane.add(Box.createHorizontalStrut(20));
        m_overwritePolicyAbortButton.setAlignmentY(Component.TOP_ALIGNMENT);
        overwriteFilePane.add(m_overwritePolicyAbortButton);
        overwriteFilePane.add(Box.createHorizontalGlue());

        p.add(overwriteFilePane, gbc);

        m_overwritePolicyAbortButton.doClick();
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

    @Override
    protected void loadSettingsFrom(final NodeSettingsRO settings, final DataTableSpec[] specs) {
        final SendToTableauHyperSettings s = new SendToTableauHyperSettings().loadSettingsInDialog(settings);
        m_hostPanel.setSelectedString(s.getHost());
        m_usernamePanel.setSelectedString(s.getUsername());
        m_passwordField.setText(s.getPassword());
        m_siteContentURLPanel.setSelectedString(s.getSiteContentURL());

        m_projectNamePanel.setSelectedString(s.getProjectName());
        m_datasourceNamePanel.setSelectedString(s.getDatasourceName());
        switch (s.getOverwrite()) {
            case APPEND:
                m_overwritePolicyAppendButton.doClick();
                break;
            case OVERWRITE:
                m_overwritePolicyOverwriteButton.doClick();
                break;
            case ABORT:
                m_overwritePolicyAbortButton.doClick();
                break;
        }
    }

    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) throws InvalidSettingsException {
        final SendToTableauHyperSettings s = new SendToTableauHyperSettings();
        s.setHost(m_hostPanel.getSelectedString());
        s.setUsername(m_usernamePanel.getSelectedString());
        s.setPassword(new String(m_passwordField.getPassword()));
        s.setSiteContentURL(m_siteContentURLPanel.getSelectedString());

        s.setProjectName(m_projectNamePanel.getSelectedString());
        s.setDatasourceName(m_datasourceNamePanel.getSelectedString());
        // Save the overwrite policy
        if (m_overwritePolicyAppendButton.isSelected()) {
            s.setOverwrite(FileOverwritePolicy.APPEND);
        } else if (m_overwritePolicyOverwriteButton.isSelected()) {
            s.setOverwrite(FileOverwritePolicy.OVERWRITE);
        } else {
            s.setOverwrite(FileOverwritePolicy.ABORT);
        }
        s.saveSettings(settings);
    }
}
