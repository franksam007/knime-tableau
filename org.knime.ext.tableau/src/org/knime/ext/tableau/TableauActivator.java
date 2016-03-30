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
 *   Mar 28, 2016 (wiswedel): created
 */
package org.knime.ext.tableau;

import java.net.URL;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Iterator;
import java.util.stream.Stream;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.knime.core.node.NodeLogger;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import com.sun.jna.NativeLibrary;

public final class TableauActivator implements BundleActivator {

    private static final NodeLogger LOGGER = NodeLogger.getLogger(TableauActivator.class);

    @Override
    public void start(final BundleContext context) throws Exception {
        IExtensionRegistry registry = Platform.getExtensionRegistry();
        IExtensionPoint point = registry.getExtensionPoint("org.knime.ext.tableau");
        boolean hasAtLeastOneContribution = false;

        for (Iterator<IConfigurationElement> it = Stream.of(point.getExtensions()).flatMap(
            ext -> Stream.of(ext.getConfigurationElements())).iterator(); it.hasNext(); ) {
            IConfigurationElement element = it.next();
            String pluginID = element.getContributor().getName();
            String pathString = element.getAttribute("path");
            Path path = new Path(pathString);
            URL url = FileLocator.find(Platform.getBundle(pluginID), path, Collections.emptyMap());
            url = url == null ? null : FileLocator.resolve(url);
            if (url == null) {
                LOGGER.errorWithFormat("Cannot resolve tableau library resource for plug-in %s, path \"%s\"",
                    pluginID, pathString);
            } else if (!"file".equals(url.getProtocol())) {
                LOGGER.errorWithFormat("Could not resolve URL \"%s\" relative to bundle \"%s\" as a local file "
                        + "(original path \"%s\")", url.toString(), pluginID, pathString);
            } else {
                String folderPath = Paths.get(url.getFile()).normalize().toString();
                LOGGER.debugWithFormat("Added tableau library path: \"%s\"", folderPath);
                NativeLibrary.addSearchPath("TableauExtract", folderPath);
                hasAtLeastOneContribution = true;
            }
        }
        if (!hasAtLeastOneContribution) {
            LOGGER.debug("No tableau binary fragments installed -- relying on system's library path");
        }
    }

    @Override
    public void stop(final BundleContext context) throws Exception {
    }

}
