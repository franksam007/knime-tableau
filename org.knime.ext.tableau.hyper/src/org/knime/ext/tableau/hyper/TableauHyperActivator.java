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
 *   Mar 28, 2016 (wiswedel): created
 */
package org.knime.ext.tableau.hyper;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Iterator;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.knime.core.node.NodeLogger;
import org.knime.ext.tableau.TableauPlugin;
import org.knime.ext.tableau.TableauPlugin.TABLEAU_SDK;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import com.sun.jna.NativeLibrary;

/**
 * Activator for Tableau binary Plugin
 *
 * @author Benjamin Wilhelm, KNIME GmbH, Konstanz, Germany
 */
public final class TableauHyperActivator implements BundleActivator {

    private static final NodeLogger LOGGER = NodeLogger.getLogger(TableauHyperActivator.class);

    private static final String EXTENSION_POINT_ID = "org.knime.ext.tableau.hyper";

    @Override
    public void start(final BundleContext context) throws Exception {

        if (TableauPlugin.getSelectedSDK() != TABLEAU_SDK.HYPER) {
            return; // HYPER SDK is not selected, do not load this extension
        }
        if (Platform.OS_LINUX.equals(Platform.getOS())) {
            loadLinuxLibraries();
        } else {
            TableauHyperInstallDirProvider.setInstallDir(createLibraryPath());
        }
    }

    /**
     * @return the path to the first library
     * @throws IOException
     *
     */
    private String createLibraryPath() throws IOException {
        final IExtensionPoint point = Platform.getExtensionRegistry().getExtensionPoint(EXTENSION_POINT_ID);

        final IExtension[] exts = point.getExtensions();
        if (exts.length > 0) {
            final IConfigurationElement element = exts[0].getConfigurationElements()[0]; // just get the first element
            final String pluginID = element.getContributor().getName();
            final String libName = element.getAttribute("name");
            if (StringUtils.isEmpty(libName)) {
                LOGGER.errorWithFormat("Tableau library name cannot be empty in plug-in %s.", pluginID);
            }
            final Path path = new Path(element.getAttribute("path"));
            final URL fileUrl =
                FileLocator.toFileURL(FileLocator.find(Platform.getBundle(pluginID), path, Collections.emptyMap()));
            return new File(fileUrl.getPath()).getCanonicalPath();
        } else {
            LOGGER.debug("No tableau binary fragments installed -- relying on system's library path");
        }
        return null;
    }

    private void loadLinuxLibraries() throws IOException {
        final IExtensionPoint point = Platform.getExtensionRegistry().getExtensionPoint(EXTENSION_POINT_ID);
        boolean hasAtLeastOneContribution = false;

        for (final Iterator<IConfigurationElement> it =
            Stream.of(point.getExtensions()).flatMap(ext -> Stream.of(ext.getConfigurationElements())).iterator(); it
                .hasNext();) {
            final IConfigurationElement element = it.next();
            final String pluginID = element.getContributor().getName();
            final String libName = element.getAttribute("name");
            if (StringUtils.isEmpty(libName)) {
                LOGGER.errorWithFormat("Tableau library name cannot be empty in plug-in %s.", pluginID);
                continue;
            }
            final String pathString = element.getAttribute("path");
            final Path path = new Path(pathString);
            URL url = FileLocator.find(Platform.getBundle(pluginID), path, Collections.emptyMap());
            url = url == null ? null : FileLocator.resolve(url);
            if (url == null) {
                LOGGER.errorWithFormat("Cannot resolve tableau library resource for plug-in %s, path \"%s\"", pluginID,
                    pathString);
            } else if (!"file".equals(url.getProtocol())) {
                LOGGER.errorWithFormat("Could not resolve URL \"%s\" relative to bundle \"%s\" as a local file "
                    + "(original path \"%s\")", url.toString(), pluginID, pathString);
            } else {
                try {
                    // must not use url.toURI() -- FileLocator leaves spaces in the URL (see eclipse bug 145096)
                    java.nio.file.Path folderPath = Paths.get(new URI(url.getProtocol(), url.getFile(), null));
                    folderPath = folderPath.normalize();
                    LOGGER.debugWithFormat("Added tableau library path: \"%s\"", folderPath);
                    NativeLibrary.addSearchPath(libName, folderPath.toString());
                    hasAtLeastOneContribution = true;
                } catch (final URISyntaxException use) {
                    LOGGER
                        .error(String.format("Unable to resolve file from URL \"%s\": %s", url, use.getMessage(), use));
                }
            }
        }
        if (!hasAtLeastOneContribution) {
            LOGGER.debug("No tableau binary fragments installed -- relying on system's library path");
        }
    }

    @Override
    public void stop(final BundleContext context) throws Exception {
        // nothing to do
    }
}
