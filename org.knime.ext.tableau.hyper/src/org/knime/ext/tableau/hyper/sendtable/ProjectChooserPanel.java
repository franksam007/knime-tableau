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
 *   Nov 7, 2018 (Benjamin Wilhelm): created
 */
package org.knime.ext.tableau.hyper.sendtable;

import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ExecutionException;
import java.util.function.BiConsumer;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreeSelectionModel;

import org.knime.core.node.NodeLogger;
import org.knime.ext.tableau.hyper.sendtable.api.RestApiConnection;
import org.knime.ext.tableau.hyper.sendtable.api.RestApiConnection.TsResponseException;
import org.knime.ext.tableau.hyper.sendtable.api.binding.ProjectListType;
import org.knime.ext.tableau.hyper.sendtable.api.binding.ProjectType;

/** @author Benjamin Wilhelm, KNIME GmbH, Konstanz, Germany */
final class ProjectChooserPanel {

    private static final NodeLogger LOGGER = NodeLogger.getLogger(ProjectChooserPanel.class);

    private final JTree m_tree;

    private final JButton m_accept;

    private final JButton m_cancel;

    private final JDialog m_dialog;

    private final BiConsumer<String, String> m_selector;

    private ProjectLoader m_projectLoader;

    private boolean m_projectsLoaded = false;

    /**
     * Creates a new project chooser window.
     *
     * @param parent the parent window
     * @param selector a {@link BiConsumer} that is called with the id and name of the project when a project was
     *            selected
     */
    ProjectChooserPanel(final Frame parent, final BiConsumer<String, String> selector) {
        m_selector = selector;

        m_tree = new JTree(new String[]{"Loading..."});
        m_accept = new JButton("Accept");
        m_cancel = new JButton("Cancel");
        m_dialog = new JDialog(parent);
        m_dialog.setTitle("Choose project...");
        m_dialog.setLocationRelativeTo(parent);

        // Configure tree
        m_tree.setRootVisible(false);
        m_tree.setShowsRootHandles(true);
        m_tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        m_tree.getSelectionModel().addTreeSelectionListener(
            e -> m_accept.setEnabled(m_projectsLoaded && e.getNewLeadSelectionPath() != null));

        m_accept.addActionListener(a -> clickAccept());
        m_cancel.addActionListener(a -> clickCancel());
        m_accept.setEnabled(false);

        show();
    }

    private void show() {
        m_dialog.setLayout(new GridBagLayout());
        final GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.gridx = gbc.gridy = 0;

        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1;
        gbc.weighty = 1;
        m_dialog.add(new JScrollPane(m_tree), gbc);
        gbc.gridy += 1;

        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.weighty = 0;
        m_dialog.add(m_accept, gbc);
        gbc.gridx += 1;
        m_dialog.add(m_cancel, gbc);

        m_dialog.pack();
        m_dialog.setVisible(true);
    }

    /**
     * Start loading projects from the server to be shown in the window.
     *
     * @param host
     * @param user
     * @param password
     * @param contentUrl
     */
    void invokeLoadingProjects(final String host, final String user, final String password,
        final String contentUrl) {
        m_projectLoader = new ProjectLoader(host, user, password, contentUrl);
        m_projectLoader.execute();
    }

    private void clickAccept() {
        final DefaultMutableTreeNode node = (DefaultMutableTreeNode)m_tree.getSelectionPath().getLastPathComponent();
        final ProjectDesc p = (ProjectDesc)node.getUserObject();
        m_selector.accept(p.getId(), p.getName());
        m_dialog.dispose();
    }

    private void clickCancel() {
        m_dialog.dispose();
        if (m_projectLoader != null) {
            m_projectLoader.cancel(true);
        }
    }

    private static class ProjectDesc {

        private String m_name;

        private String m_id;

        public ProjectDesc(final String name, final String id) {
            m_name = name;
            m_id = id;
        }

        public String getId() {
            return m_id;
        }

        public String getName() {
            return m_name;
        }

        @Override
        public String toString() {
            return m_name;
        }
    }

    private class ProjectLoader extends SwingWorker<TreeNode, Void> {

        private String m_host;

        private String m_user;

        private String m_password;

        private String m_contentUrl;

        ProjectLoader(final String host, final String user, final String password, final String contentUrl) {
            m_host = host;
            m_user = user;
            m_password = password;
            m_contentUrl = contentUrl;
        }

        @Override
        protected TreeNode doInBackground() throws Exception {
            final RestApiConnection apiConnection = new RestApiConnection(m_host);
            apiConnection.invokeSignIn(m_user, m_password, m_contentUrl);
            return buildProjectTree(apiConnection);
        }

        @Override
        protected void done() {
            final DefaultTreeModel model = (DefaultTreeModel)m_tree.getModel();
            try {
                model.setRoot(get());
                m_projectsLoaded = true;
            } catch (final InterruptedException e) {
                // Do nothing
            } catch (final ExecutionException e) {
                LOGGER.warn(e.getCause(), e);
                JOptionPane.showMessageDialog(SwingUtilities.windowForComponent(m_dialog),
                    e.getMessage(), "Connection Failed", JOptionPane.ERROR_MESSAGE);
                m_dialog.dispose();
            }
        }

        private TreeNode buildProjectTree(final RestApiConnection apiConnection) throws TsResponseException {
            // Get all projects
            final ProjectListType projects = apiConnection.invokeQueryProjects();
            final Queue<ProjectType> queue = new LinkedList<>(projects.getProject());
            final List<String> readIds = new LinkedList<>();

            final Map<String, DefaultMutableTreeNode> tree = new HashMap<>();
            final DefaultMutableTreeNode root = new DefaultMutableTreeNode("");

            while (!queue.isEmpty()) {
                final ProjectType p = queue.poll();
                final String parentId = p.getParentProjectId();
                if (parentId == null) {
                    // Add it to the root node
                    final DefaultMutableTreeNode node =
                        new DefaultMutableTreeNode(new ProjectDesc(p.getName(), p.getId()));
                    root.add(node);
                    tree.put(p.getId(), node);
                    readIds.clear();
                } else if (tree.containsKey(parentId)) {
                    // We know about the parent
                    final DefaultMutableTreeNode node =
                        new DefaultMutableTreeNode(new ProjectDesc(p.getName(), p.getId()));
                    final DefaultMutableTreeNode parent = tree.get(parentId);
                    parent.add(node);
                    tree.put(p.getId(), node);
                    readIds.clear();
                } else {
                    if (readIds.contains(p.getId())) {
                        // We added no node to the tree since the last time we saw this node:
                        // Something is wrong
                        throw new IllegalStateException(
                            "Tableau Server send an invalid list of projects. Can't find parent projects for some nodes.");
                    }
                    // We don't know where the parent is in the tree:
                    // Add it back to the queue
                    queue.add(p);
                    readIds.add(p.getId());
                }
            }
            return root;
        }
    }
}
