/**
 * NetXMS - open source network management system
 * Copyright (C) 2003-2017 Raden Solutions
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
package org.netxms.ui.eclipse.datacollection.propertypages;

import java.util.Arrays;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.netxms.client.datacollection.DataCollectionItem;
import org.netxms.ui.eclipse.datacollection.Messages;
import org.netxms.ui.eclipse.datacollection.dialogs.TestTransformationDlg;
import org.netxms.ui.eclipse.datacollection.propertypages.helpers.DCIPropertyPageDialog;
import org.netxms.ui.eclipse.nxsl.widgets.ScriptEditor;
import org.netxms.ui.eclipse.tools.WidgetFactory;
import org.netxms.ui.eclipse.tools.WidgetHelper;

/**
 * "Transformation" property page for DCI
 */
public class Transformation extends DCIPropertyPageDialog {
	private static final String[] DCI_FUNCTIONS = {
			"FindDCIByName", "FindDCIByDescription", "GetDCIObject", "GetDCIValue", "GetDCIValueByDescription", "GetDCIValueByName" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
	private static final String[] DCI_VARIABLES = { "$dci", "$node" }; //$NON-NLS-1$ //$NON-NLS-2$

	private Combo deltaCalculation;
	private ScriptEditor transformationScript;
	private Button testScriptButton;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse
	 * .swt.widgets.Composite)
	 */
	@Override
	protected Control createContents(Composite parent) {
		Composite dialogArea = (Composite) super.createContents(parent);

		GridLayout layout = new GridLayout();
		layout.verticalSpacing = WidgetHelper.OUTER_SPACING;
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		dialogArea.setLayout(layout);

		if (editor.getObject() instanceof DataCollectionItem) {
			deltaCalculation = WidgetHelper.createLabeledCombo(dialogArea,
					SWT.BORDER | SWT.READ_ONLY,
					Messages.get().Transformation_Step1,
					WidgetHelper.DEFAULT_LAYOUT_DATA);
			deltaCalculation.add(Messages.get().Transformation_DeltaNone);
			deltaCalculation.add(Messages.get().Transformation_DeltaSimple);
			deltaCalculation.add(Messages.get().Transformation_DeltaAvgPerSec);
			deltaCalculation.add(Messages.get().Transformation_DeltaAvgPerMin);
			deltaCalculation.select(editor.getObjectAsItem()
					.getDeltaCalculation());
		}

		GridData gd = new GridData();
		gd.horizontalAlignment = SWT.FILL;
		gd.verticalAlignment = SWT.FILL;
		gd.grabExcessHorizontalSpace = true;
		gd.grabExcessVerticalSpace = true;
		gd.widthHint = 0;
		gd.heightHint = 0;
		final WidgetFactory factory = new WidgetFactory() {
			@Override
			public Control createControl(Composite parent, int style) {
				return new ScriptEditor(
						parent,
						style,
						SWT.H_SCROLL | SWT.V_SCROLL,
						true,
						"Variables:\r\n\t$1\t\t\tvalue to transform (after delta calculation);\r\n\t$dci\t\t\tthis DCI object;\r\n\t$isCluster\ttrue if DCI is on cluster;\r\n\t$node\t\tcurrent node object (null if DCI is not on the node);\r\n\t$object\t\tcurrent object.\r\n\r\nReturn value: transformed DCI value or null to keep original value.");
			}
		};
		transformationScript = (ScriptEditor) WidgetHelper
				.createLabeledControl(
						dialogArea,
						SWT.BORDER,
						factory,
						(editor.getObject() instanceof DataCollectionItem) ? Messages
								.get().Transformation_Step2
								: Messages.get().Transformation_Script, gd);
		transformationScript.addFunctions(Arrays.asList(DCI_FUNCTIONS));
		transformationScript.addVariables(Arrays.asList(DCI_VARIABLES));
		gd = new GridData();
		gd.horizontalAlignment = SWT.FILL;
		gd.verticalAlignment = SWT.FILL;
		gd.grabExcessHorizontalSpace = true;
		gd.grabExcessVerticalSpace = true;
		transformationScript.setLayoutData(gd);
		transformationScript.setText(editor.getObject()
				.getTransformationScript());

		if (editor.getObject() instanceof DataCollectionItem) {
			testScriptButton = new Button(transformationScript.getParent(),
					SWT.PUSH);
			testScriptButton.setText(Messages.get().Transformation_Test);
			gd = new GridData();
			gd.horizontalAlignment = SWT.RIGHT;
			gd.widthHint = WidgetHelper.BUTTON_WIDTH_HINT;
			testScriptButton.setLayoutData(gd);
			testScriptButton.addSelectionListener(new SelectionListener() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					TestTransformationDlg dlg = new TestTransformationDlg(
							getShell(), editor.getObject().getNodeId(),
							transformationScript.getText());
					dlg.open();
				}

				@Override
				public void widgetDefaultSelected(SelectionEvent e) {
					widgetSelected(e);
				}
			});
		}

		return dialogArea;
	}

	/**
	 * Apply changes
	 * 
	 * @param isApply
	 *            true if update operation caused by "Apply" button
	 */
	protected void applyChanges(final boolean isApply) {
		if (editor.getObject() instanceof DataCollectionItem)
			editor.getObjectAsItem().setDeltaCalculation(
					deltaCalculation.getSelectionIndex());
		editor.getObject().setTransformationScript(
				transformationScript.getText());
		editor.modify();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.PreferencePage#performOk()
	 */
	@Override
	public boolean performOk() {
		applyChanges(false);
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.PreferencePage#performApply()
	 */
	@Override
	protected void performApply() {
		applyChanges(true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.PreferencePage#performDefaults()
	 */
	@Override
	protected void performDefaults() {
		super.performDefaults();
		if (deltaCalculation != null)
			deltaCalculation.select(DataCollectionItem.DELTA_NONE);
		transformationScript.setText(""); //$NON-NLS-1$
	}
}
