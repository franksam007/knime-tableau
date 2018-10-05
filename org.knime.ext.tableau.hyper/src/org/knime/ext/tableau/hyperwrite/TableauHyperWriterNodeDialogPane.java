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
package org.knime.ext.tableau.hyperwrite;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.knime.core.data.DataTableSpec;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.util.FilesHistoryPanel;
import org.knime.core.node.util.FilesHistoryPanel.LocationValidation;
import org.knime.core.node.workflow.FlowVariable.Type;

/**
 * Dialog for Tableau Hyper Writer.
 *
 * @author Bernd Wiswedel, KNIME AG, Zurich, Switzerland
 * @author Benjamin Wilhelm, KNIME GmbH, Konstanz, Germany
 */
final class TableauHyperWriterNodeDialogPane extends NodeDialogPane {

    private final FilesHistoryPanel m_filePanel;
    private final JCheckBox m_overwriteChecker;

    TableauHyperWriterNodeDialogPane() {
        m_filePanel = new FilesHistoryPanel(createFlowVariableModel(TableauHyperWriterSettings.CFG_OUTPUT_LOCATION, Type.STRING),
                    "org.knime.ext.tableau.tdewrite", LocationValidation.FileOutput, ".tde", ".TDE");
        m_filePanel.setDialogTypeSaveWithExtension(".tde");
        m_overwriteChecker = new JCheckBox("Overwrite OK");
        addTab("TDE Settings", initPanel());
    }

    private JPanel initPanel() {
        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.gridx = gbc.gridy = 0;

        gbc.anchor = GridBagConstraints.EAST;

        p.add(new JLabel("Output Location "), gbcLabel(gbc));
        gbc.gridx += 1;
        p.add(m_filePanel, gbcComponent(gbc));
        gbc.gridy += 1;

        gbc.gridx = 1;
        p.add(m_overwriteChecker, gbcComponent(gbc));
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
        TableauHyperWriterSettings s = new TableauHyperWriterSettings().loadSettingsInDialog(settings);
        m_filePanel.updateHistory();
        m_filePanel.setSelectedFile(s.getOutputLocation());
        m_overwriteChecker.setSelected(s.isOverwriteOK());
    }

    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) throws InvalidSettingsException {
        TableauHyperWriterSettings s = new TableauHyperWriterSettings();
        s.setOutputLocation(m_filePanel.getSelectedFile());
        s.setOverwriteOK(m_overwriteChecker.isSelected());

        s.saveSettings(settings);
    }

}
