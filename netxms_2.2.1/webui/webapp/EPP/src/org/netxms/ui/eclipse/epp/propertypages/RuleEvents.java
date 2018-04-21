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
package org.netxms.ui.eclipse.epp.propertypages;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.dialogs.PropertyPage;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.netxms.client.NXCSession;
import org.netxms.client.events.EventProcessingPolicyRule;
import org.netxms.client.events.EventTemplate;
import org.netxms.ui.eclipse.epp.Messages;
import org.netxms.ui.eclipse.epp.widgets.RuleEditor;
import org.netxms.ui.eclipse.eventmanager.dialogs.EventSelectionDialog;
import org.netxms.ui.eclipse.shared.ConsoleSharedData;
import org.netxms.ui.eclipse.tools.ObjectLabelComparator;
import org.netxms.ui.eclipse.tools.WidgetHelper;
import org.netxms.ui.eclipse.widgets.SortableTableViewer;

/**
 * "Events" property page for EPP rule
 */
public class RuleEvents extends PropertyPage
{
	private NXCSession session;
	private RuleEditor editor;
	private EventProcessingPolicyRule rule;
	private SortableTableViewer viewer;
	private Map<Long, EventTemplate> events = new HashMap<Long, EventTemplate>();
	private Button addButton;
	private Button deleteButton;
	private Button checkInverted;
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Control createContents(Composite parent)
	{
		session = (NXCSession)ConsoleSharedData.getSession();
		editor = (RuleEditor)getElement().getAdapter(RuleEditor.class);
		rule = editor.getRule();
		
		Composite dialogArea = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.verticalSpacing = WidgetHelper.OUTER_SPACING;
		layout.marginWidth = 0;
		layout.marginHeight = 0;
      dialogArea.setLayout(layout);
      
      checkInverted = new Button(dialogArea, SWT.CHECK);
      checkInverted.setText(Messages.get().RuleEvents_InvertedRule);
      checkInverted.setSelection(rule.isEventsInverted());
      
      final String[] columnNames = { Messages.get().RuleEvents_Event };
      final int[] columnWidths = { 300 };
      viewer = new SortableTableViewer(dialogArea, columnNames, columnWidths, 0, SWT.UP, SWT.BORDER | SWT.MULTI | SWT.FULL_SELECTION);
      viewer.setContentProvider(new ArrayContentProvider());
      viewer.setLabelProvider(new WorkbenchLabelProvider());
      viewer.setComparator(new ObjectLabelComparator((ILabelProvider)viewer.getLabelProvider()));
      viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event)
			{
				int size = ((IStructuredSelection)viewer.getSelection()).size();
				deleteButton.setEnabled(size > 0);
			}
      });

      for(EventTemplate e : session.findMultipleEventTemplates(rule.getEvents().toArray(new Long[0])))
      	events.put(e.getCode(), e);
      viewer.setInput(events.values().toArray());
      
      GridData gridData = new GridData();
      gridData.verticalAlignment = GridData.FILL;
      gridData.grabExcessVerticalSpace = true;
      gridData.horizontalAlignment = GridData.FILL;
      gridData.grabExcessHorizontalSpace = true;
      gridData.heightHint = 0;
      viewer.getControl().setLayoutData(gridData);
      
      Composite buttons = new Composite(dialogArea, SWT.NONE);
      RowLayout buttonLayout = new RowLayout();
      buttonLayout.type = SWT.HORIZONTAL;
      buttonLayout.pack = false;
      buttonLayout.marginLeft = 0;
      buttonLayout.marginRight = 0;
      buttons.setLayout(buttonLayout);
      gridData = new GridData();
      gridData.horizontalAlignment = SWT.RIGHT;
      buttons.setLayoutData(gridData);

      addButton = new Button(buttons, SWT.PUSH);
      addButton.setText(Messages.get().RuleEvents_Add);
      addButton.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetDefaultSelected(SelectionEvent e)
			{
				widgetSelected(e);
			}

			@Override
			public void widgetSelected(SelectionEvent e)
			{
				addEvent();
			}
      });
      RowData rd = new RowData();
      rd.width = WidgetHelper.BUTTON_WIDTH_HINT;
      addButton.setLayoutData(rd);
		
      deleteButton = new Button(buttons, SWT.PUSH);
      deleteButton.setText(Messages.get().RuleEvents_Delete);
      deleteButton.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetDefaultSelected(SelectionEvent e)
			{
				widgetSelected(e);
			}

			@Override
			public void widgetSelected(SelectionEvent e)
			{
				deleteEvent();
			}
      });
      rd = new RowData();
      rd.width = WidgetHelper.BUTTON_WIDTH_HINT;
      deleteButton.setLayoutData(rd);
      deleteButton.setEnabled(false);
		
		return dialogArea;
	}

	/**
	 * Add new event
	 */
	private void addEvent()
	{
		EventSelectionDialog dlg = new EventSelectionDialog(getShell());
		dlg.enableMultiSelection(true);
		if (dlg.open() == Window.OK)
		{
			for(EventTemplate e : dlg.getSelectedEvents())
				events.put(e.getCode(), e);
		}
      viewer.setInput(events.values().toArray());
	}
	
	/**
	 * Delete event from list
	 */
	private void deleteEvent()
	{
		IStructuredSelection selection = (IStructuredSelection)viewer.getSelection();
		Iterator<?> it = selection.iterator();
		if (it.hasNext())
		{
			while(it.hasNext())
			{
				EventTemplate e = (EventTemplate)it.next();
				events.remove(e.getCode());
			}
	      viewer.setInput(events.values().toArray());
		}
	}
	
	/**
	 * Update rule object
	 */
	private void doApply()
	{
      int flags = rule.getFlags();
      if (checkInverted.getSelection() && !events.isEmpty()) // ignore "negate" flag if event set is empty
         flags |= EventProcessingPolicyRule.NEGATED_EVENTS;
      else
         flags &= ~EventProcessingPolicyRule.NEGATED_EVENTS;
      rule.setFlags(flags);
      rule.setEvents(new ArrayList<Long>(events.keySet()));
		editor.setModified(true);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage#performApply()
	 */
	@Override
	protected void performApply()
	{
		doApply();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage#performOk()
	 */
	@Override
	public boolean performOk()
	{
		doApply();
		return super.performOk();
	}
}
