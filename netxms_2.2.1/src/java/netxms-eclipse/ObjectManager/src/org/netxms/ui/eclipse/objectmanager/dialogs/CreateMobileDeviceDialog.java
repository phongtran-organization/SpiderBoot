/**
 * NetXMS - open source network management system
 * Copyright (C) 2003-2013 Victor Kirhenshtein
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
package org.netxms.ui.eclipse.objectmanager.dialogs;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.netxms.ui.eclipse.objectmanager.Messages;
import org.netxms.ui.eclipse.tools.MessageDialogHelper;
import org.netxms.ui.eclipse.tools.WidgetHelper;
import org.netxms.ui.eclipse.widgets.LabeledText;

/**
 * Mobile device object creation dialog
 */
public class CreateMobileDeviceDialog extends Dialog {
	private LabeledText nameField;
	private LabeledText deviceIdField;

	private String name;
	private String deviceId;

	/**
	 * @param parentShell
	 */
	public CreateMobileDeviceDialog(Shell parentShell) {
		super(parentShell);
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
		newShell.setText(Messages.get().CreateMobileDeviceDialog_Title);
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
		Composite dialogArea = (Composite) super.createDialogArea(parent);

		GridLayout layout = new GridLayout();
		layout.verticalSpacing = WidgetHelper.DIALOG_SPACING;
		layout.marginHeight = WidgetHelper.DIALOG_HEIGHT_MARGIN;
		layout.marginWidth = WidgetHelper.DIALOG_WIDTH_MARGIN;
		dialogArea.setLayout(layout);

		nameField = new LabeledText(dialogArea, SWT.NONE);
		nameField.setLabel(Messages.get().CreateMobileDeviceDialog_Name);
		nameField.getTextControl().setTextLimit(255);
		GridData gd = new GridData();
		gd.horizontalAlignment = SWT.FILL;
		gd.grabExcessHorizontalSpace = true;
		gd.widthHint = 300;
		nameField.setLayoutData(gd);

		deviceIdField = new LabeledText(dialogArea, SWT.NONE);
		deviceIdField
				.setLabel(Messages.get().CreateMobileDeviceDialog_DeviceID);
		deviceIdField.getTextControl().setTextLimit(64);
		gd = new GridData();
		gd.horizontalAlignment = SWT.FILL;
		gd.grabExcessHorizontalSpace = true;
		deviceIdField.setLayoutData(gd);

		return dialogArea;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.Dialog#okPressed()
	 */
	@Override
	protected void okPressed() {
		deviceId = deviceIdField.getText().trim();
		name = nameField.getText().trim();
		if (name.isEmpty()) {
			MessageDialogHelper.openWarning(getShell(),
					Messages.get().CreateMobileDeviceDialog_Warning,
					Messages.get().CreateMobileDeviceDialog_WarningEmptyName);
			return;
		}

		super.okPressed();
	}

	/**
	 * @return object name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return device ID
	 */
	public String getDeviceId() {
		return deviceId;
	}
}
