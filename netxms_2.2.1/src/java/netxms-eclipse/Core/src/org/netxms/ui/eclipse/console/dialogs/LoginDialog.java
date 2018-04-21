/**
 * NetXMS - open source network management system
 * Copyright (C) 2003-2009 Victor Kirhenshtein
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */
package org.netxms.ui.eclipse.console.dialogs;

import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Monitor;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.netxms.certificate.loader.exception.KeyStoreLoaderException;
import org.netxms.certificate.manager.CertificateManager;
import org.netxms.certificate.subject.Subject;
import org.netxms.certificate.subject.SubjectParser;
import org.netxms.client.constants.AuthenticationType;
import org.netxms.ui.eclipse.console.Activator;
import org.netxms.ui.eclipse.console.BrandingManager;
import org.netxms.ui.eclipse.console.Messages;
import org.netxms.ui.eclipse.tools.MessageDialogHelper;
import org.netxms.ui.eclipse.tools.WidgetHelper;
import org.netxms.ui.eclipse.widgets.LabeledText;

/**
 * Login dialog
 */
public class LoginDialog extends Dialog {
	private FormToolkit toolkit;
	private ImageDescriptor loginImage;
	private Combo comboServer;
	private LabeledText textLogin;
	private LabeledText textPassword;
	private Composite authEntryFields;
	private Combo comboAuth;
	private Combo comboCert;
	private String password;
	private Button checkSlowLink;
	private Certificate certificate;
	private Color labelColor;
	private final CertificateManager certMgr;
	private AuthenticationType authMethod = AuthenticationType.PASSWORD;

	/**
	 * @param parentShell
	 */
	public LoginDialog(Shell parentShell, CertificateManager certMgr) {
		super(parentShell);

		loginImage = BrandingManager.getInstance().getLoginTitleImage();
		if (loginImage == null)
			loginImage = Activator.getImageDescriptor("icons/login.png"); //$NON-NLS-1$

		this.certMgr = certMgr;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets
	 * .Shell)
	 */
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		String customTitle = BrandingManager.getInstance().getLoginTitle();
		newShell.setText((customTitle != null) ? customTitle
				: Messages.get().LoginDialog_title); //$NON-NLS-1$

		// Center dialog on screen
		// We don't have main window at this moment, so use
		// monitor data to determine right position
		Monitor[] ma = newShell.getDisplay().getMonitors();
		if (ma != null) {
			newShell.setLocation(
					(ma[0].getClientArea().width - newShell.getSize().x) / 2,
					(ma[0].getClientArea().height - newShell.getSize().y) / 2);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets
	 * .Composite)
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		IDialogSettings settings = Activator.getDefault().getDialogSettings();

		toolkit = new FormToolkit(parent.getDisplay());
		Form dialogArea = toolkit.createForm(parent);
		dialogArea.setLayoutData(new GridData(GridData.FILL_BOTH));
		applyDialogFont(dialogArea);

		GridLayout dialogLayout = new GridLayout();
		dialogLayout.numColumns = 2;
		dialogLayout.marginWidth = WidgetHelper.DIALOG_WIDTH_MARGIN;
		dialogLayout.marginHeight = WidgetHelper.DIALOG_HEIGHT_MARGIN;
		dialogLayout.horizontalSpacing = WidgetHelper.DIALOG_SPACING * 2;
		dialogArea.getBody().setLayout(dialogLayout);

		RGB customColor = BrandingManager.getInstance().getLoginTitleColor();
		labelColor = (customColor != null) ? new Color(dialogArea.getDisplay(),
				customColor) : new Color(dialogArea.getDisplay(), dialogArea
				.getBody().getBackground().getRGB());
		dialogArea.addDisposeListener(new DisposeListener() {
			@Override
			public void widgetDisposed(DisposeEvent e) {
				labelColor.dispose();
			}
		});

		// Login image
		Label label = new Label(dialogArea.getBody(), SWT.NONE);
		label.setBackground(labelColor);
		label.setImage(loginImage.createImage());
		label.addDisposeListener(new DisposeListener() {
			@Override
			public void widgetDisposed(DisposeEvent event) {
				((Label) event.widget).getImage().dispose();
			}
		});
		GridData gd = new GridData();
		gd.horizontalAlignment = SWT.CENTER;
		gd.verticalAlignment = SWT.TOP;
		gd.grabExcessVerticalSpace = true;
		label.setLayoutData(gd);

		final Composite fields = toolkit.createComposite(dialogArea.getBody());
		fields.setBackgroundMode(SWT.INHERIT_DEFAULT);
		GridLayout fieldsLayout = new GridLayout();
		fieldsLayout.verticalSpacing = WidgetHelper.DIALOG_SPACING;
		fieldsLayout.marginHeight = 0;
		fieldsLayout.marginWidth = 0;
		fields.setLayout(fieldsLayout);
		gd = new GridData();
		gd.horizontalAlignment = SWT.FILL;
		gd.grabExcessHorizontalSpace = true;
		gd.verticalAlignment = SWT.TOP;
		gd.grabExcessVerticalSpace = true;
		fields.setLayoutData(gd);

		gd = new GridData();
		gd.horizontalAlignment = SWT.FILL;
		gd.grabExcessHorizontalSpace = true;
		comboServer = WidgetHelper.createLabeledCombo(fields, SWT.DROP_DOWN,
				Messages.get().LoginDialog_server, gd, toolkit);

		checkSlowLink = new Button(fields, SWT.CHECK);
		checkSlowLink.setText(Messages.get().LoginDialog_SlowLinkConnection);
		gd = new GridData();
		gd.horizontalIndent = 8;
		checkSlowLink.setLayoutData(gd);

		textLogin = new LabeledText(fields, SWT.NONE, SWT.SINGLE | SWT.BORDER,
				toolkit);
		textLogin.setLabel(Messages.get().LoginDialog_login);
		gd = new GridData();
		gd.horizontalAlignment = SWT.FILL;
		gd.grabExcessHorizontalSpace = true;
		gd.widthHint = WidgetHelper.getTextWidth(textLogin, "M") * 24; //$NON-NLS-1$
		textLogin.setLayoutData(gd);

		gd = new GridData();
		gd.horizontalAlignment = SWT.FILL;
		gd.grabExcessHorizontalSpace = true;
		comboAuth = WidgetHelper.createLabeledCombo(fields, SWT.DROP_DOWN
				| SWT.READ_ONLY, Messages.get().LoginDialog_Auth, gd, toolkit);
		comboAuth.add(Messages.get().LoginDialog_Passwd);
		comboAuth.add(Messages.get().LoginDialog_Cert);
		comboAuth.select(authMethod.getValue());
		comboAuth.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				selectAuthenticationField(true);
			}
		});

		authEntryFields = toolkit.createComposite(fields);
		authEntryFields.setLayout(new StackLayout());
		authEntryFields.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
				false));

		textPassword = new LabeledText(authEntryFields, SWT.NONE, SWT.SINGLE
				| SWT.BORDER | SWT.PASSWORD, toolkit);
		textPassword.setLabel(Messages.get().LoginDialog_Passwd);

		comboCert = WidgetHelper.createLabeledCombo(authEntryFields,
				SWT.DROP_DOWN | SWT.READ_ONLY, Messages.get().LoginDialog_Cert,
				null, toolkit);
		comboCert.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				selectCertificate();
			}
		});

		// Read field data
		String[] items = settings.getArray("Connect.ServerHistory"); //$NON-NLS-1$
		if (items != null)
			comboServer.setItems(items);
		String text = settings.get("Connect.Server"); //$NON-NLS-1$
		if (text != null)
			comboServer.setText(text);

		text = settings.get("Connect.Login"); //$NON-NLS-1$
		if (text != null)
			textLogin.setText(text);

		checkSlowLink.setSelection(settings.getBoolean("Connect.SlowLink")); //$NON-NLS-1$

		try {
			authMethod = AuthenticationType.getByValue(settings
					.getInt("Connect.AuthMethod")); //$NON-NLS-1$
		} catch (NumberFormatException e) {
			authMethod = AuthenticationType.PASSWORD;
		}
		comboAuth.select(authMethod.getValue());
		selectAuthenticationField(false);

		// Set initial focus
		if (comboServer.getText().isEmpty()) {
			comboServer.setFocus();
		} else if (textLogin.getText().isEmpty()) {
			textLogin.setFocus();
		} else {
			if (authMethod == AuthenticationType.PASSWORD)
				textPassword.setFocus();
			else if (authMethod == AuthenticationType.CERTIFICATE)
				comboCert.setFocus();
		}

		return dialogArea;
	}

	/**
	 * Select authentication information entry filed depending on selected
	 * authentication method.
	 * 
	 * @param doLayout
	 */
	private void selectAuthenticationField(boolean doLayout) {
		authMethod = AuthenticationType.getByValue(comboAuth
				.getSelectionIndex());
		switch (authMethod) {
		case PASSWORD:
			((StackLayout) authEntryFields.getLayout()).topControl = textPassword;
			break;
		case CERTIFICATE:
			fillCertCombo();
			((StackLayout) authEntryFields.getLayout()).topControl = comboCert
					.getParent();
			break;
		default:
			((StackLayout) authEntryFields.getLayout()).topControl = null; // hide
																			// entry
																			// control
			break;
		}
		if (doLayout) {
			authEntryFields.layout();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.Dialog#okPressed()
	 */
	@Override
	protected void okPressed() {
		if ((authMethod == AuthenticationType.CERTIFICATE)
				&& (comboCert.getSelectionIndex() == -1)) {
			MessageDialogHelper.openWarning(getShell(),
					Messages.get().LoginDialog_Warning,
					Messages.get().LoginDialog_NoCertSelected);
			return;
		}

		IDialogSettings settings = Activator.getDefault().getDialogSettings();

		HashSet<String> items = new HashSet<String>();
		items.addAll(Arrays.asList(comboServer.getItems()));
		items.add(comboServer.getText());

		settings.put("Connect.Server", comboServer.getText()); //$NON-NLS-1$
		settings.put(
				"Connect.ServerHistory", items.toArray(new String[items.size()])); //$NON-NLS-1$
		settings.put("Connect.Login", textLogin.getText()); //$NON-NLS-1$
		settings.put("Connect.AuthMethod", authMethod.getValue()); //$NON-NLS-1$
		settings.put("Connect.SlowLink", checkSlowLink.getSelection()); //$NON-NLS-1$
		if (certificate != null)
			settings.put(
					"Connect.Certificate", ((X509Certificate) certificate).getSubjectDN().toString()); //$NON-NLS-1$

		password = textPassword.getText();
		super.okPressed();
	}

	/**
	 * @return the password
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * Select certificate
	 */
	private void selectCertificate() {
		int index = comboCert.getSelectionIndex();
		if (index >= 0) {
			certificate = certMgr.getCerts()[index];
		}
	}

	/**
	 * @param c
	 * @return
	 */
	private static String getCertificateDisplayName(Certificate c) {
		String subjString = ((X509Certificate) c).getSubjectDN().toString();
		Subject subj = SubjectParser.parseSubject(subjString);
		return String
				.format("%s (%s, %s, %s)", subj.getCommonName(), subj.getOrganization(), subj.getState(), subj.getCountry()); //$NON-NLS-1$
	}

	/**
	 * Fill certificate list
	 * 
	 * @return
	 */
	private boolean fillCertCombo() {
		if (comboCert.getItemCount() != 0)
			return true;

		try {
			if (certMgr.hasNoCertificates()) {
				certMgr.load();
			}
		} catch (KeyStoreLoaderException ksle) {
			Shell shell = Display.getCurrent().getActiveShell();
			MessageDialog.openError(shell, Messages.get().LoginDialog_Error,
					Messages.get().LoginDialog_WrongKeyStorePasswd);
			return false;
		}

		Certificate[] certs = certMgr.getCerts();
		Arrays.sort(certs, new Comparator<Certificate>() {
			@Override
			public int compare(Certificate o1, Certificate o2) {
				return getCertificateDisplayName(o1).compareToIgnoreCase(
						getCertificateDisplayName(o2));
			}
		});

		String[] subjectStrings = new String[certs.length];

		IDialogSettings settings = Activator.getDefault().getDialogSettings();
		String lastSelected = settings.get("Connect.Certificate"); //$NON-NLS-1$
		int selectionIndex = 0;

		for (int i = 0; i < certs.length; i++) {
			String subject = ((X509Certificate) certs[i]).getSubjectDN()
					.toString();
			if (subject.equals(lastSelected))
				selectionIndex = i;
			subjectStrings[i] = getCertificateDisplayName(certs[i]);
		}

		if (subjectStrings.length != 0) {
			comboCert.setItems(subjectStrings);
			comboCert.select(selectionIndex);
			selectCertificate();
			return true;
		}

		return false;
	}

	/**
	 * Get selected certificate
	 * 
	 * @return
	 */
	public Certificate getCertificate() {
		return certificate;
	}
}
