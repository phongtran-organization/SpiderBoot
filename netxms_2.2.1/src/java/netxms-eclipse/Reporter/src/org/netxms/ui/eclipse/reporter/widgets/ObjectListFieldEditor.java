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
package org.netxms.ui.eclipse.reporter.widgets;

import java.util.HashMap;
import java.util.Map;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ImageHyperlink;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.netxms.client.objects.AbstractObject;
import org.netxms.client.reporting.ReportParameter;
import org.netxms.ui.eclipse.console.resources.SharedIcons;
import org.netxms.ui.eclipse.objectbrowser.dialogs.ObjectSelectionDialog;
import org.netxms.ui.eclipse.reporter.Messages;
import org.netxms.ui.eclipse.tools.WidgetHelper;

/**
 * Field editor for "object list" type field
 */
public class ObjectListFieldEditor extends FieldEditor {
	private TableViewer viewer;
	private Map<Long, AbstractObject> objects = new HashMap<Long, AbstractObject>();

	/**
	 * @param parameter
	 * @param toolkit
	 * @param parent
	 */
	public ObjectListFieldEditor(ReportParameter parameter,
			FormToolkit toolkit, Composite parent) {
		super(parameter, toolkit, parent);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.netxms.ui.eclipse.reporter.widgets.FieldEditor#createContent(org.
	 * eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Control createContent(Composite parent) {
		Composite content = toolkit.createComposite(parent, SWT.NONE);

		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.verticalSpacing = WidgetHelper.INNER_SPACING;
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		content.setLayout(layout);

		viewer = new TableViewer(content, SWT.FULL_SELECTION | SWT.MULTI
				| SWT.BORDER);
		viewer.setContentProvider(new ArrayContentProvider());
		viewer.setLabelProvider(new WorkbenchLabelProvider());
		viewer.setInput(new Object[0]);
		GridData gd = new GridData();
		gd.horizontalAlignment = SWT.FILL;
		gd.verticalAlignment = SWT.FILL;
		gd.grabExcessHorizontalSpace = true;
		gd.grabExcessVerticalSpace = true;
		gd.verticalSpan = 2;
		viewer.getControl().setLayoutData(gd);

		ImageHyperlink link = toolkit.createImageHyperlink(content, SWT.NONE);
		link.setImage(SharedIcons.IMG_ADD_OBJECT);
		link.setText(Messages.get().ObjectListFieldEditor_Add);
		link.addHyperlinkListener(new HyperlinkAdapter() {
			@Override
			public void linkActivated(HyperlinkEvent e) {
				addObjects();
			}
		});
		gd = new GridData();
		gd.verticalAlignment = SWT.TOP;
		link.setLayoutData(gd);

		link = toolkit.createImageHyperlink(content, SWT.NONE);
		link.setImage(SharedIcons.IMG_DELETE_OBJECT);
		link.setText(Messages.get().ObjectListFieldEditor_Delete);
		link.addHyperlinkListener(new HyperlinkAdapter() {
			@Override
			public void linkActivated(HyperlinkEvent e) {
				deleteObjects();
			}
		});
		gd = new GridData();
		gd.verticalAlignment = SWT.TOP;
		link.setLayoutData(gd);

		return content;
	}

	/**
	 * Add objects to list
	 */
	private void addObjects() {
		ObjectSelectionDialog dlg = new ObjectSelectionDialog(getShell(), null,
				null);
		if (dlg.open() == Window.OK) {
			for (AbstractObject o : dlg.getSelectedObjects())
				objects.put(o.getObjectId(), o);
			viewer.setInput(objects.values().toArray());
		}
	}

	/**
	 * Delete selected objects from the list
	 */
	private void deleteObjects() {
		IStructuredSelection selection = (IStructuredSelection) viewer
				.getSelection();
		for (Object o : selection.toList()) {
			objects.remove(((AbstractObject) o).getObjectId());
		}
		viewer.setInput(objects.values().toArray());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.netxms.ui.eclipse.reporter.widgets.FieldEditor#getValue()
	 */
	@Override
	public String getValue() {
		if (objects.size() == 0)
			return ""; //$NON-NLS-1$

		StringBuilder sb = new StringBuilder();
		for (AbstractObject o : objects.values()) {
			sb.append(o.getObjectId());
			sb.append(',');
		}
		sb.deleteCharAt(sb.length() - 1);
		return sb.toString();
	}
}
