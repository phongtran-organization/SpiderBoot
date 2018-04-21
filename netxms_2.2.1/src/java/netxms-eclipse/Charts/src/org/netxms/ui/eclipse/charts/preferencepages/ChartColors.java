/**
 * NetXMS - open source network management system
 * Copyright (C) 2003-2015 Victor Kirhenshtein
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
package org.netxms.ui.eclipse.charts.preferencepages;

import org.eclipse.jface.preference.ColorFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.netxms.ui.eclipse.charts.Activator;
import org.netxms.ui.eclipse.charts.Messages;

/**
 * Chart colors preference page
 * 
 */
public class ChartColors extends FieldEditorPreferencePage implements
		IWorkbenchPreferencePage {
	private Group lineColors;

	public ChartColors() {
		super(FieldEditorPreferencePage.GRID);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	@Override
	public void init(IWorkbench workbench) {
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.preference.FieldEditorPreferencePage#createFieldEditors
	 * ()
	 */
	@Override
	protected void createFieldEditors() {
		addField(new ColorFieldEditor(
				"Chart.Colors.Background", Messages.get().ChartColors_Background, getFieldEditorParent())); //$NON-NLS-1$
		addField(new ColorFieldEditor(
				"Chart.Colors.PlotArea", Messages.get().ChartColors_PlotArea, getFieldEditorParent())); //$NON-NLS-1$
		addField(new ColorFieldEditor(
				"Chart.Colors.Title", Messages.get().ChartColors_Title, getFieldEditorParent())); //$NON-NLS-1$
		addField(new ColorFieldEditor(
				"Chart.Colors.Legend", Messages.get().ChartColors_LegendTextColor, getFieldEditorParent())); //$NON-NLS-1$

		addField(new ColorFieldEditor(
				"Chart.Axis.X.Color", Messages.get().ChartColors_TickX, getFieldEditorParent())); //$NON-NLS-1$
		addField(new ColorFieldEditor(
				"Chart.Axis.Y.Color", Messages.get().ChartColors_TickY, getFieldEditorParent())); //$NON-NLS-1$
		addField(new ColorFieldEditor(
				"Chart.Grid.X.Color", Messages.get().ChartColors_GridX, getFieldEditorParent())); //$NON-NLS-1$
		addField(new ColorFieldEditor(
				"Chart.Grid.Y.Color", Messages.get().ChartColors_GridY, getFieldEditorParent())); //$NON-NLS-1$

		addField(new ColorFieldEditor(
				"Chart.Colors.DialNeedle", Messages.get().ChartColors_DialNeedleColor, getFieldEditorParent())); //$NON-NLS-1$
		addField(new ColorFieldEditor(
				"Chart.Colors.DialNeedlePin", Messages.get().ChartColors_DialNeedlePinColor, getFieldEditorParent())); //$NON-NLS-1$
		addField(new ColorFieldEditor(
				"Chart.Colors.DialScale", Messages.get().ChartColors_DialScaleColor, getFieldEditorParent())); //$NON-NLS-1$
		addField(new ColorFieldEditor(
				"Chart.Colors.DialScaleText", Messages.get().ChartColors_DialScaleTextColor, getFieldEditorParent())); //$NON-NLS-1$
		addField(new ColorFieldEditor(
				"Chart.Colors.DialValueBackground", Messages.get().ChartColors_DialCurrValueBkgndColor, getFieldEditorParent())); //$NON-NLS-1$
		addField(new ColorFieldEditor(
				"Chart.Colors.DialValueText", Messages.get().ChartColors_DialCurrValueTextColor, getFieldEditorParent())); //$NON-NLS-1$

		lineColors = new Group(getFieldEditorParent(), SWT.NONE);
		lineColors.setText(Messages.get().ChartColors_LineColors);

		addField(new ColorFieldEditor(
				"Chart.Colors.Data.0", Messages.get().ChartColors_1st, lineColors)); //$NON-NLS-1$
		addField(new ColorFieldEditor(
				"Chart.Colors.Data.1", Messages.get().ChartColors_2nd, lineColors)); //$NON-NLS-1$
		addField(new ColorFieldEditor(
				"Chart.Colors.Data.2", Messages.get().ChartColors_3rd, lineColors)); //$NON-NLS-1$
		addField(new ColorFieldEditor(
				"Chart.Colors.Data.3", Messages.get().ChartColors_4th, lineColors)); //$NON-NLS-1$
		addField(new ColorFieldEditor(
				"Chart.Colors.Data.4", Messages.get().ChartColors_5th, lineColors)); //$NON-NLS-1$
		addField(new ColorFieldEditor(
				"Chart.Colors.Data.5", Messages.get().ChartColors_6th, lineColors)); //$NON-NLS-1$
		addField(new ColorFieldEditor(
				"Chart.Colors.Data.6", Messages.get().ChartColors_7th, lineColors)); //$NON-NLS-1$
		addField(new ColorFieldEditor(
				"Chart.Colors.Data.7", Messages.get().ChartColors_8th, lineColors)); //$NON-NLS-1$
		addField(new ColorFieldEditor(
				"Chart.Colors.Data.8", Messages.get().ChartColors_9th, lineColors)); //$NON-NLS-1$
		addField(new ColorFieldEditor(
				"Chart.Colors.Data.9", Messages.get().ChartColors_10th, lineColors)); //$NON-NLS-1$
		addField(new ColorFieldEditor(
				"Chart.Colors.Data.10", Messages.get().ChartColors_11th, lineColors)); //$NON-NLS-1$
		addField(new ColorFieldEditor(
				"Chart.Colors.Data.11", Messages.get().ChartColors_12th, lineColors)); //$NON-NLS-1$
		addField(new ColorFieldEditor(
				"Chart.Colors.Data.12", Messages.get().ChartColors_13th, lineColors)); //$NON-NLS-1$
		addField(new ColorFieldEditor(
				"Chart.Colors.Data.13", Messages.get().ChartColors_14th, lineColors)); //$NON-NLS-1$
		addField(new ColorFieldEditor(
				"Chart.Colors.Data.14", Messages.get().ChartColors_15th, lineColors)); //$NON-NLS-1$
		addField(new ColorFieldEditor(
				"Chart.Colors.Data.15", Messages.get().ChartColors_16th, lineColors)); //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.preference.FieldEditorPreferencePage#adjustGridLayout()
	 */
	@Override
	protected void adjustGridLayout() {
		((GridLayout) getFieldEditorParent().getLayout()).numColumns = 4;

		GridLayout layout = new GridLayout();
		layout.numColumns = 8;
		layout.makeColumnsEqualWidth = false;
		lineColors.setLayout(layout);
		GridData gd = new GridData();
		gd.horizontalAlignment = SWT.FILL;
		gd.grabExcessHorizontalSpace = true;
		gd.horizontalSpan = 4;
		lineColors.setLayoutData(gd);
	}
}
