/**
 * NetXMS - open source network management system
 * Copyright (C) 2003-2014 Victor Kirhenshtein
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
package org.netxms.ui.eclipse.reporter;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.netxms.client.reporting.ReportParameter;
import org.netxms.ui.eclipse.reporter.api.CustomControlFactory;
import org.netxms.ui.eclipse.reporter.widgets.AlarmStateFieldEditor;
import org.netxms.ui.eclipse.reporter.widgets.BooleanFieldEditor;
import org.netxms.ui.eclipse.reporter.widgets.DateFieldEditor;
import org.netxms.ui.eclipse.reporter.widgets.EventFieldEditor;
import org.netxms.ui.eclipse.reporter.widgets.FieldEditor;
import org.netxms.ui.eclipse.reporter.widgets.NumberFieldEditor;
import org.netxms.ui.eclipse.reporter.widgets.NumericConditionFieldEditor;
import org.netxms.ui.eclipse.reporter.widgets.ObjectFieldEditor;
import org.netxms.ui.eclipse.reporter.widgets.ObjectListFieldEditor;
import org.netxms.ui.eclipse.reporter.widgets.SeverityFieldEditor;
import org.netxms.ui.eclipse.reporter.widgets.SeverityListFieldEditor;
import org.netxms.ui.eclipse.reporter.widgets.StringFieldEditor;
import org.netxms.ui.eclipse.reporter.widgets.TimestampFieldEditor;
import org.netxms.ui.eclipse.reporter.widgets.UserFieldEditor;

/**
 * Control factory for standard control types
 */
public class StandardTypesControlFactory implements CustomControlFactory {
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.netxms.ui.eclipse.reporter.api.CustomControlFactory#editorForType
	 * (org.eclipse.swt.widgets.Composite,
	 * org.netxms.api.client.reporting.ReportParameter,
	 * org.eclipse.ui.forms.widgets.FormToolkit)
	 */
	@Override
	public FieldEditor editorForType(Composite parent,
			ReportParameter parameter, FormToolkit toolkit) {
		FieldEditor fieldEditor = null;
		final String type = parameter.getType();
		if (type.equals("START_DATE") || type.equals("END_DATE")) //$NON-NLS-1$ //$NON-NLS-2$
		{
			fieldEditor = new DateFieldEditor(parameter, toolkit, parent);
		} else if (type.equals("ALARM_STATE")) //$NON-NLS-1$
		{
			fieldEditor = new AlarmStateFieldEditor(parameter, toolkit, parent);
		} else if (type.equals("BOOLEAN")) //$NON-NLS-1$
		{
			fieldEditor = new BooleanFieldEditor(parameter, toolkit, parent);
		} else if (type.equals("EVENT_CODE")) //$NON-NLS-1$
		{
			fieldEditor = new EventFieldEditor(parameter, toolkit, parent);
		} else if (type.equals("NUMBER")) //$NON-NLS-1$
		{
			fieldEditor = new NumberFieldEditor(parameter, toolkit, parent);
		} else if (type.equals("NUMERIC_CONDITION")) //$NON-NLS-1$
		{
			fieldEditor = new NumericConditionFieldEditor(parameter, toolkit,
					parent);
		} else if (type.equals("OBJECT_ID")) //$NON-NLS-1$
		{
			fieldEditor = new ObjectFieldEditor(parameter, toolkit, parent);
		} else if (type.equals("OBJECT_ID_LIST")) //$NON-NLS-1$
		{
			fieldEditor = new ObjectListFieldEditor(parameter, toolkit, parent);
		} else if (type.equals("SEVERITY")) //$NON-NLS-1$
		{
			fieldEditor = new SeverityFieldEditor(parameter, toolkit, parent);
		} else if (type.equals("SEVERITY_LIST")) //$NON-NLS-1$
		{
			fieldEditor = new SeverityListFieldEditor(parameter, toolkit,
					parent);
		} else if (type.equals("TIMESTAMP")) //$NON-NLS-1$
		{
			fieldEditor = new TimestampFieldEditor(parameter, toolkit, parent);
		} else if (type.equals("USER_ID")) //$NON-NLS-1$
		{
			fieldEditor = new UserFieldEditor(parameter, toolkit, parent, false);
		} else if (type.equals("USER_NAME")) //$NON-NLS-1$
		{
			fieldEditor = new UserFieldEditor(parameter, toolkit, parent, true);
		} else {
			fieldEditor = new StringFieldEditor(parameter, toolkit, parent);
		}
		return fieldEditor;
	}
}
