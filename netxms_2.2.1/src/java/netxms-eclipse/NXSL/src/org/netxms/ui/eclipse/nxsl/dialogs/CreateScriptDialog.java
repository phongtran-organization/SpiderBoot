/**
 * NetXMS - open source network management system
 * Copyright (C) 2003-2010 Victor Kirhenshtein
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
package org.netxms.ui.eclipse.nxsl.dialogs;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.netxms.ui.eclipse.nxsl.Messages;
import org.netxms.ui.eclipse.tools.MessageDialogHelper;
import org.netxms.ui.eclipse.tools.WidgetHelper;

/**
 * Dialog for creating new script
 * 
 */
public class CreateScriptDialog extends Dialog {
	private boolean rename;
	private Text nameInputField;
	private String name;

	/**
	 * Constructor
	 * 
	 * @param parent
	 *            Parent shell
	 */
	public CreateScriptDialog(Shell parent, String currentName) {
		super(parent);
		rename = (currentName != null);
		name = currentName;
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
		layout.marginWidth = WidgetHelper.DIALOG_WIDTH_MARGIN;
		layout.marginHeight = WidgetHelper.DIALOG_HEIGHT_MARGIN;
		dialogArea.setLayout(layout);

		nameInputField = WidgetHelper.createLabeledText(dialogArea, SWT.SINGLE
				| SWT.BORDER, SWT.DEFAULT,
				Messages.get().CreateScriptDialog_ScriptName, name,
				WidgetHelper.DEFAULT_LAYOUT_DATA);
		nameInputField.getShell().setMinimumSize(300, 0);

		return dialogArea;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.Dialog#okPressed()
	 */
	@Override
	protected void okPressed() {
		name = nameInputField.getText();
		name = name.trim();
		if (name.isEmpty())
			MessageDialogHelper.openWarning(getShell(),
					Messages.get().CreateScriptDialog_Warning,
					Messages.get().CreateScriptDialog_WarningEmptyName);
		else
			super.okPressed();
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
		newShell.setText(rename ? Messages.get().CreateScriptDialog_Rename
				: Messages.get().CreateScriptDialog_CreateNew);
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
}
