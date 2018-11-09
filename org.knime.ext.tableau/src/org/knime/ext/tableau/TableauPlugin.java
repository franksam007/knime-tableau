package org.knime.ext.tableau;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.knime.core.node.NodeLogger;
import org.knime.ext.tableau.preferences.TableauInstallDirProvider;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

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

/**
 *
 * @author Gabriel Einsdorf
 */
public class TableauPlugin extends AbstractUIPlugin {

    private static final NodeLogger LOGGER = NodeLogger.getLogger(TableauPlugin.class);

    private static TableauPlugin plugin;

    public static final String TABLEAU_SDK_KEY = "org.knime.ext.tableau.sdk";

    public static final String TABLEAU_INSTALLDIR_KEY = "org.knime.ext.tableau.installdir";

    private static String sdkName;

    private static Boolean m_tdeInstalled;

    private static String m_tableauInstallPath;

    /**
     * Creates the {@link TableauPlugin}
     */
    public TableauPlugin() {
        plugin = this;
    }

    /**
     * @return default instance
     */
    public static TableauPlugin getDefault() {
        return plugin;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void start(final BundleContext context) throws Exception {
        super.start(context);
    }

    /**
     * The Tableau SDK variant to use
     */
    public enum TABLEAU_SDK {
            HYPER("Tableau Hyper"), TDE("Tableau TDE");

        private String m_name;

        TABLEAU_SDK(final String name) {
            m_name = name;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            return m_name;
        }
    }

    /**
     * @return the Tableau SDK selected in the preferences
     */
    public static TABLEAU_SDK getSelectedSDK() {
        if (sdkName == null) { // only switch SDK after restart
            sdkName = getDefault().getPreferenceStore().getString(TableauPlugin.TABLEAU_SDK_KEY);
        }
        if (sdkName.equals(TABLEAU_SDK.HYPER.name())) {
            return TABLEAU_SDK.HYPER;
        }
        if (sdkName.equals(TABLEAU_SDK.TDE.name())) {
            return TABLEAU_SDK.TDE;
        }
        throw new IllegalArgumentException("Unknown SDK:" + sdkName);
    }

    public static boolean isTDEInstalled() {
        if (m_tdeInstalled == null) {
            final Bundle[] bundles = getDefault().getBundle().getBundleContext().getBundles();
            boolean tdeInstalled = false;
            for (final Bundle bundle : bundles) {
                if (bundle.getSymbolicName().equals("org.knime.ext.tableau.tde")) {
                    tdeInstalled = true;
                    break;
                }
            }
            m_tdeInstalled = tdeInstalled;
        }
        return m_tdeInstalled;
    }

    /**
     *
     */
    public static String getTableauInstallPath() {
        if (m_tableauInstallPath == null || m_tableauInstallPath == "") {
            final IConfigurationElement[] configs =
                Platform.getExtensionRegistry().getConfigurationElementsFor("org.knime.ext.tableau.installdir");
            for (IConfigurationElement element : configs) {
                try {
                    element.getAttributeNames();
                    Object o = element.createExecutableExtension("InstallDirProvider");
                    if (o instanceof TableauInstallDirProvider) {
                        TableauInstallDirProvider tableauInstallDirProvider = (TableauInstallDirProvider)o;
                        if (tableauInstallDirProvider.backend() == getSelectedSDK()) {
                            m_tableauInstallPath = tableauInstallDirProvider.installationDir();
                            break;
                        }
                    }
                } catch (CoreException e) {
                    LOGGER.error("Unknonw plugin extension! " + element.getName());
                }
            }
        }
        return m_tableauInstallPath;
    }
}
