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
 */
package org.knime.ext.tableau.preferences;

import java.io.IOException;
import java.net.URL;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWebBrowser;
import org.knime.core.util.SWTUtilities;
import org.knime.ext.tableau.TableauPlugin;
import org.knime.ext.tableau.TableauPlugin.TABLEAU_SDK;

/**
 * Preference Page for the Tableau integration
 *
 * @author Gabriel Einsdorf, KNIME GmbH, Konstanz, Germany
 */
public class TableauPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

    private boolean m_apply = false;

    private String m_tmpSDK;

    private Button m_tdeButton;

    private Button m_hyperButton;

    /**
     *
     */
    public TableauPreferencePage() {
        super("Tableau Preferences");
        final IPreferenceStore store = TableauPlugin.getDefault().getPreferenceStore();
        m_tmpSDK = store.getString(TableauPlugin.TABLEAU_SDK_KEY);
        setDescription("Select the backend to use for data transfer to Tableau.");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Control createContents(final Composite parent) {

        final Composite composite = createComposite(parent, 1, "");

        final Label label = new Label(composite, SWT.NONE);
        label.setText("1): Select the backend matching your Tableau server:");

        // sdk selection radio buttons
        final Composite sdkSeletionComposite = createComposite(composite, 1, "Tableau Backend");
        if (TableauPlugin.isTDEInstalled()) {
            m_tdeButton = new Button(sdkSeletionComposite, SWT.RADIO);
            m_tdeButton.setText(TABLEAU_SDK.TDE.toString() + " (for Tableau 10.4 and earlier)");
        }
        m_hyperButton = new Button(sdkSeletionComposite, SWT.RADIO);
        m_hyperButton.setText(TABLEAU_SDK.HYPER.toString() + " (for Tableau 10.5 and later)");

        m_hyperButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent e) {
                if (m_hyperButton.getSelection()) {
                    if (TableauPlugin.getSelectedSDK() != TABLEAU_SDK.HYPER) {
                        setMessage("You need to restart KNIME Analytics Platform to apply this setting!",
                            IMessageProvider.WARNING);
                    } else {
                        setMessage("", IMessageProvider.NONE);
                    }
                }
            }
        });

        m_tdeButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent e) {
                if (m_tdeButton.getSelection()) {
                    if (TableauPlugin.getSelectedSDK() != TABLEAU_SDK.TDE) {
                        setMessage("You need to restart KNIME Analytics Platform to apply this setting!",
                            IMessageProvider.WARNING);
                    } else {
                        setMessage("", IMessageProvider.NONE);
                    }
                }
            }
        });

        // Add additional settings for windows and mac

        final String os = Platform.getOS();
        // Windows
        if (os.equals(Platform.WS_WIN32)) {
            addWindowsFields(composite);
        } else if (os.equals(Platform.OS_MACOSX) && (TableauPlugin.getSelectedSDK() == TABLEAU_SDK.HYPER)) {
            addMacHyperFields(composite);
        }

        initializeValues();

        return composite;
    }

    /**
     *
     */
    private void initializeValues() {
        final TABLEAU_SDK sdk = TableauPlugin.getSelectedSDK();
        if (sdk == TABLEAU_SDK.HYPER) {
            m_hyperButton.setSelection(true);
        } else {
            m_hyperButton.setSelection(false);
            m_tdeButton.setSelection(true);
        }
    }

    /**
     *
     */
    private static void addMacHyperFields(final Composite parent) {
        final Label instruction = new Label(parent, SWT.NONE);
        instruction.setText("2): Copy the files from source folder to the target folder.");

        final Composite buttonComposite = createComposite(parent, 2, "");
        final Button sourceFolderButton = new Button(buttonComposite, SWT.PUSH);
        sourceFolderButton.setText("Open source folder");
        final Button targetFolderButton = new Button(buttonComposite, SWT.PUSH);
        targetFolderButton.setText("Open target folder");

        final String path = TableauPlugin.getTableauInstallPath();
        sourceFolderButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent e) {
                try {
                    Runtime.getRuntime().exec("open -R file://" + path.replaceAll(" ", "%20"));
                } catch (final IOException e1) {
                    // nothing to do
                }
            }
        });

        targetFolderButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent e) {
                try {
                    Runtime.getRuntime().exec("open /Library/Frameworks");
                } catch (final IOException e1) {
                    // nothing to do
                }
            }
        });
    }

    /**
     *
     */
    private void addWindowsFields(final Composite parent) {

        final Link instruction = new Link(parent, SWT.NONE);
        instruction.setText("2): Add the following String to your PATH environment variable: "
            + "<a href=\"https://www.computerhope.com/issues/ch000549.htm\"> Instructions</a>");
        instruction.addSelectionListener(new UriOpenListener());

        final String path = TableauPlugin.getTableauInstallPath();
        final String DIR_KEY = "org.knime.ext.tableau.dir";
        getPreferenceStore().setValue(DIR_KEY, path);

        final StyledText pathField = new StyledText(parent, SWT.READ_ONLY | SWT.H_SCROLL);
        pathField.setText(path);

        GridData fd = new GridData();
        fd.widthHint = 500;
        fd.heightHint = 25;
        pathField.setLayoutData(fd);

        final Button copyButton = new Button(parent, SWT.PUSH);
        copyButton.setText("Copy to Clipboard");
        copyButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent e) {
                Point p = pathField.getSelection();
                pathField.selectAll();
                pathField.copy();
                pathField.setSelection(p);
            }
        });
        final Link visCpp = new Link(parent, SWT.NONE);
        visCpp.setText(
            "3): Download and install <a href=\"https://www.microsoft.com/en-US/download/details.aspx?id=40784\">"
                + "Redistributable Packages for Visual C++ 2013</a>");
        visCpp.addSelectionListener(new UriOpenListener());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void init(final IWorkbench workbench) {
        setPreferenceStore(TableauPlugin.getDefault().getPreferenceStore());
    }

    /**
     * Overriden to display a message box in case the SDK was changed.
     *
     * {@inheritDoc}
     */
    @Override
    public boolean performOk() {
        saveSDKPreference();
        checkChanges();
        return true;
    }

    /**
     *
     * {@inheritDoc}
     */
    @Override
    protected void performApply() {
        m_apply = true;
        saveSDKPreference();
        super.performApply();
    }

    private void saveSDKPreference() {
        final IPreferenceStore store = getPreferenceStore();
        if (m_hyperButton.getSelection()) {
            store.setValue(TableauPlugin.TABLEAU_SDK_KEY, TABLEAU_SDK.HYPER.name());
        } else {
            store.setValue(TableauPlugin.TABLEAU_SDK_KEY, TABLEAU_SDK.TDE.name());
        }
    }

    /**
     * Overriden to react when the users applies but then presses cancel.
     *
     * {@inheritDoc}
     */
    @Override
    public boolean performCancel() {
        final boolean result = super.performCancel();
        checkChanges();
        return result;
    }

    private void checkChanges() {
        final boolean apply = m_apply;
        m_apply = false;

        if (apply) {
            return;
        }

        // get the preference store for the UI plugin
        final IPreferenceStore store = TableauPlugin.getDefault().getPreferenceStore();
        final String currentSDK = store.getString(TableauPlugin.TABLEAU_SDK_KEY);
        if (!m_tmpSDK.equals(currentSDK)) {
            // reset the directory
            m_tmpSDK = currentSDK;
            final String message = "Changes of the Tableau backend become " //
                + "available after restarting the workbench.\n" //
                + "Do you want to restart the workbench now?";

            Display.getDefault().asyncExec(() -> promptRestartWithMessage(message));
        }
    }

    private static void promptRestartWithMessage(final String message) {
        final MessageBox mb = new MessageBox(SWTUtilities.getActiveShell(), SWT.ICON_QUESTION | SWT.YES | SWT.NO);
        mb.setText("Restart workbench...");
        mb.setMessage(message);
        if (mb.open() != SWT.YES) {
            return;
        }
        PlatformUI.getWorkbench().restart();
    }

    private static Composite createComposite(final Composite parent, final int numColumns, final String title) {
        Composite composite;
        final GridLayout layout = new GridLayout();

        if (!StringUtils.isEmpty(title)) {
            final Group group = new Group(parent, SWT.NULL);
            group.setText(title);
            composite = group;
            layout.marginWidth = 10;
            layout.marginHeight = 10;
        } else {
            composite = new Composite(parent, SWT.NULL);
        }
        layout.numColumns = numColumns;
        composite.setLayout(layout);
        composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
        return composite;
    }

    private class UriOpenListener extends SelectionAdapter {
        @Override
        public void widgetSelected(final SelectionEvent e) {
            try {
                //Open external browser
                final IWebBrowser browser = PlatformUI.getWorkbench().getBrowserSupport().getExternalBrowser();
                browser.openURL(new URL(e.text));
            } catch (final Exception ex) {
                /* do nothing */
            }
        }
    }
}
