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
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;
import org.netxms.client.datacollection.ConditionDciInfo;
import org.netxms.client.datacollection.DataCollectionObject;
import org.netxms.client.datacollection.Threshold;
import org.netxms.ui.eclipse.objectmanager.Messages;
import org.netxms.ui.eclipse.tools.WidgetFactory;
import org.netxms.ui.eclipse.tools.WidgetHelper;
import org.netxms.ui.eclipse.widgets.LabeledText;

/**
 * Dialog for editing condition DCI mapping
 */
public class ConditionDciEditDialog extends Dialog
{
	private ConditionDciInfo dci;
	private String nodeName;
	private String dciName;
	private Combo function;
	private Spinner polls;
	
	/**
	 * @param parentShell
	 * @param dci
	 * @param nodeName
	 * @param dciName
	 */
	public ConditionDciEditDialog(Shell parentShell, ConditionDciInfo dci, String nodeName, String dciName)
	{
		super(parentShell);
		this.dci = dci;
		this.nodeName = nodeName;
		this.dciName = dciName;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
	 */
	@Override
	protected void configureShell(Shell newShell)
	{
		super.configureShell(newShell);
		newShell.setText(Messages.get().ConditionDciEditDialog_Title);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Control createDialogArea(Composite parent)
	{
		Composite dialogArea = (Composite)super.createDialogArea(parent);
		
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		dialogArea.setLayout(layout);
		
		LabeledText node = new LabeledText(dialogArea, SWT.NONE, SWT.BORDER | SWT.READ_ONLY);
		node.setLabel(Messages.get().ConditionDciEditDialog_Node);
		node.setText(nodeName);
		GridData gd = new GridData();
		gd.horizontalSpan = 2;
		gd.horizontalAlignment = SWT.FILL;
		gd.grabExcessHorizontalSpace = true;
		gd.widthHint = 400;
		node.setLayoutData(gd);
		
		LabeledText parameter = new LabeledText(dialogArea, SWT.NONE, SWT.BORDER | SWT.READ_ONLY);
		parameter.setLabel(Messages.get().ConditionDciEditDialog_Parameter);
		parameter.setText(dciName);
		gd = new GridData();
		gd.horizontalSpan = 2;
		gd.horizontalAlignment = SWT.FILL;
		gd.grabExcessHorizontalSpace = true;
		parameter.setLayoutData(gd);
		
		if (dci.getType() != DataCollectionObject.DCO_TYPE_TABLE)
		{
   		gd = new GridData();
   		gd.horizontalAlignment = SWT.FILL;
   		gd.grabExcessHorizontalSpace = true;
   		function = WidgetHelper.createLabeledCombo(dialogArea, SWT.NONE, Messages.get().ConditionDciEditDialog_Function, gd);
   		function.add(Messages.get().ConditionDciEditDialog_FuncLast);
   		function.add(Messages.get().ConditionDciEditDialog_FuncAvg);
   		function.add(Messages.get().ConditionDciEditDialog_FuncDeviation);
   		function.add(Messages.get().ConditionDciEditDialog_FuncDiff);
   		function.add(Messages.get().ConditionDciEditDialog_FuncError);
   		function.add(Messages.get().ConditionDciEditDialog_FuncSum);
   		function.select(dci.getFunction());
   		function.addSelectionListener(new SelectionListener() {
   			@Override
   			public void widgetSelected(SelectionEvent e)
   			{
   				polls.setEnabled(isFunctionWithArgs());
   			}
   			
   			@Override
   			public void widgetDefaultSelected(SelectionEvent e)
   			{
   				widgetSelected(e);
   			}
   		});
   		
   		final WidgetFactory factory = new WidgetFactory() {
   			@Override
   			public Control createControl(Composite parent, int style)
   			{
   				Spinner spinner = new Spinner(parent, style);
   				spinner.setMinimum(1);
   				spinner.setMaximum(255);
   				return spinner;
   			}
   		};
   		polls = (Spinner)WidgetHelper.createLabeledControl(dialogArea, SWT.BORDER, factory, Messages.get().ConditionDciEditDialog_Polls, gd);
   		polls.setSelection(dci.getPolls());
   		polls.setEnabled(isFunctionWithArgs());
		}		
		return dialogArea;
	}

	/**
	 * Returns true if currently selected function requires arguments
	 * 
	 * @return
	 */
	private boolean isFunctionWithArgs()
	{
		int f = function.getSelectionIndex();
		return (f != Threshold.F_DIFF) && (f != Threshold.F_LAST);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#okPressed()
	 */
	@Override
	protected void okPressed()
	{
      if (dci.getType() != DataCollectionObject.DCO_TYPE_TABLE)
      {
   		dci.setFunction(function.getSelectionIndex());
   		dci.setPolls(polls.getSelection());
      }
		super.okPressed();
	}
}
