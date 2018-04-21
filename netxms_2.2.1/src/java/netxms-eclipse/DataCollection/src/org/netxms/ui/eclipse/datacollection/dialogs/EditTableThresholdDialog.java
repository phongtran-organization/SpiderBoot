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
package org.netxms.ui.eclipse.datacollection.dialogs;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.netxms.client.datacollection.TableThreshold;
import org.netxms.ui.eclipse.datacollection.Messages;
import org.netxms.ui.eclipse.datacollection.propertypages.TableColumns.TableColumnDataProvider;
import org.netxms.ui.eclipse.datacollection.widgets.TableConditionsEditor;
import org.netxms.ui.eclipse.eventmanager.widgets.EventSelector;
import org.netxms.ui.eclipse.tools.WidgetHelper;
import org.netxms.ui.eclipse.widgets.LabeledSpinner;

/**
 * Table threshold editing dialog
 */
public class EditTableThresholdDialog extends Dialog {
	private TableThreshold threshold;
	private EventSelector activationEvent;
	private EventSelector deactivationEvent;
	private LabeledSpinner sampleCount;
	private TableConditionsEditor conditionsEditor;
	private TableColumnDataProvider columnCallback;

	/**
	 * @param parentShell
	 * @param threshold
	 */
	public EditTableThresholdDialog(Shell parentShell,
			TableThreshold threshold, TableColumnDataProvider columnCallback) {
		super(parentShell);
		this.threshold = threshold;
		this.columnCallback = columnCallback;
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
		newShell.setText(Messages.get().EditTableThresholdDialog_Title);
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
		dialogArea.setLayout(layout);

		activationEvent = new EventSelector(dialogArea, SWT.NONE);
		activationEvent
				.setLabel(Messages.get().EditTableThresholdDialog_ActivationEvent);
		activationEvent.setEventCode(threshold.getActivationEvent());
		activationEvent.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
				false));

		deactivationEvent = new EventSelector(dialogArea, SWT.NONE);
		deactivationEvent
				.setLabel(Messages.get().EditTableThresholdDialog_DeactivationEvent);
		deactivationEvent.setEventCode(threshold.getDeactivationEvent());
		deactivationEvent.setLayoutData(new GridData(SWT.FILL, SWT.CENTER,
				true, false));

		sampleCount = new LabeledSpinner(dialogArea, SWT.NONE);
		sampleCount.setLabel("Sample count");
		sampleCount.setRange(1, 100000);
		sampleCount.setSelection(threshold.getSampleCount());

		new Label(dialogArea, SWT.NONE)
				.setText(Messages.get().EditTableThresholdDialog_Conditions);

		conditionsEditor = new TableConditionsEditor(dialogArea, SWT.BORDER,
				columnCallback);
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		gd.widthHint = 700;
		gd.heightHint = 400;
		conditionsEditor.setLayoutData(gd);
		conditionsEditor.setConditions(threshold.getConditions());

		return dialogArea;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.Dialog#okPressed()
	 */
	@Override
	protected void okPressed() {
		threshold.setActivationEvent((int) activationEvent.getEventCode());
		threshold.setDeactivationEvent((int) deactivationEvent.getEventCode());
		threshold.setSampleCount(sampleCount.getSelection());
		threshold.setConditions(conditionsEditor.getConditions());
		super.okPressed();
	}
}
