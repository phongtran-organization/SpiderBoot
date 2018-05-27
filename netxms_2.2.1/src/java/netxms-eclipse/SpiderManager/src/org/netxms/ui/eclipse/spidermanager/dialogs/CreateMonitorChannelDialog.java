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
package org.netxms.ui.eclipse.spidermanager.dialogs;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;

/**
 * User database object creation dialog
 * 
 */
public class CreateMonitorChannelDialog extends Dialog {
	private Text txtChannelId;
	private Text txtChannelName;
	
	private String channelId;
	private String channelName;

	public CreateMonitorChannelDialog(Shell parentShell) 
	{
		super(parentShell);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite dialogArea = (Composite) super.createDialogArea(parent);
				
		GridData gridData;
		dialogArea.setLayout(null);
		gridData = new GridData(GridData.VERTICAL_ALIGN_END);
		gridData.horizontalAlignment = GridData.FILL;
	    gridData.horizontalSpan = 4;
		gridData = new GridData(GridData.VERTICAL_ALIGN_END);
		gridData.horizontalAlignment = GridData.FILL;
	    gridData.horizontalSpan = 2;
		
		Group grpCreateNewAccount = new Group(dialogArea, SWT.NONE);
		grpCreateNewAccount.setText("Create new Monitor channel");
		grpCreateNewAccount.setBounds(5, 10, 518, 97);
		
		Label lblChannelId = new Label(grpCreateNewAccount, SWT.NONE);
		lblChannelId.setAlignment(SWT.RIGHT);
		lblChannelId.setText("Channel ID");
		lblChannelId.setBounds(10, 31, 109, 17);
		
		txtChannelId = new Text(grpCreateNewAccount, SWT.BORDER);
		txtChannelId.setTextLimit(150);
		txtChannelId.setBounds(131, 26, 290, 27);
		
		Label lblChannelName = new Label(grpCreateNewAccount, SWT.NONE);
		lblChannelName.setAlignment(SWT.RIGHT);
		lblChannelName.setText("Channel name");
		lblChannelName.setBounds(10, 64, 109, 17);
		
		txtChannelName = new Text(grpCreateNewAccount, SWT.BORDER);
		txtChannelName.setTextLimit(150);
		txtChannelName.setBounds(131, 59, 290, 27);
		
		Button button = new Button(grpCreateNewAccount, SWT.NONE);
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Program.launch("https://www.youtube.com/channel/" + txtChannelId.getText());
			}
		});
		button.setText("View");
		button.setBounds(427, 26, 79, 29);
		
		return dialogArea;
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Create new monitor channel");
	}

	@Override
	protected void okPressed() {
		channelId = txtChannelId.getText();
		channelName = txtChannelName.getText();
		// validate data
		if(channelId == null || channelId.isEmpty())
		{
			MessageBox dialog =
					new MessageBox(getShell(), SWT.ERROR | SWT.OK);
			dialog.setText("Error");
			dialog.setMessage("Channel ID must not empty!");
			dialog.open();
			return;
		}
		super.okPressed();
	}

	public String getChannelId() {
		return channelId;
	}

	public String getChannelName() {
		return channelName;
	}
}
