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
package org.netxms.ui.eclipse.objectview.views;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;
import org.netxms.client.NXCSession;
import org.netxms.client.objects.AbstractObject;
import org.netxms.ui.eclipse.actions.RefreshAction;
import org.netxms.ui.eclipse.objectview.Activator;
import org.netxms.ui.eclipse.objectview.Messages;
import org.netxms.ui.eclipse.objectview.widgets.ObjectStatusMap;
import org.netxms.ui.eclipse.shared.ConsoleSharedData;

/**
 * Show object status map for given object
 */
public class ObjectStatusMapView extends ViewPart
{
	public static final String ID = "org.netxms.ui.eclipse.objectview.views.ObjectStatusMapView"; //$NON-NLS-1$

	private long rootObjectId;
	private ObjectStatusMap map;
	private boolean initialGroupFlag = true;
   private boolean initialShowFilter = true;
	private Action actionRefresh;
	private Action actionGroupNodes;
	private Action actionShowFilter;
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.ViewPart#init(org.eclipse.ui.IViewSite)
	 */
	@Override
	public void init(IViewSite site) throws PartInitException
	{
		super.init(site);
		
		final NXCSession session = (NXCSession)ConsoleSharedData.getSession();
		rootObjectId = Long.parseLong(site.getSecondaryId());
		final AbstractObject object = session.findObjectById(rootObjectId);
		setPartName(String.format(Messages.get().ObjectStatusMapView_PartName, (object != null) ? object.getObjectName() : ("[" + rootObjectId + "]"))); //$NON-NLS-1$ //$NON-NLS-2$		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createPartControl(Composite parent)
	{
      final IDialogSettings settings = Activator.getDefault().getDialogSettings();
      initialGroupFlag = (settings.get("GroupObjects") != null) ? settings.getBoolean("GroupObjects") : true;
      initialShowFilter = (settings.get("ShowFilter") != null) ? settings.getBoolean("ShowFilter") : true;
	   
	   map = new ObjectStatusMap(this, parent, SWT.NONE, true);
		map.setGroupObjects(initialGroupFlag);
		map.setRootObject(rootObjectId);
		map.enableFilter(initialShowFilter);
		
		map.setFilterCloseAction(new Action() {
         @Override
         public void run()
         {
            actionShowFilter.setChecked(false);
            map.enableFilter(false);
         }
      });
		
		map.addDisposeListener(new DisposeListener() {
         @Override
         public void widgetDisposed(DisposeEvent e)
         {
            settings.put("GroupObjects", map.isGroupObjects());
            settings.put("ShowFilter", map.isFilterEnabled());
         }
      });

		createActions();
		contributeToActionBars();
		
		getSite().setSelectionProvider(map);
	}

	/**
	 * Create actions
	 */
	private void createActions()
	{
		actionRefresh = new RefreshAction(this) {
			@Override
			public void run()
			{
				map.refresh();
			}
		};
		
		actionGroupNodes = new Action(Messages.get().ObjectStatusMapView_ActionGroupNodes, Action.AS_CHECK_BOX) {
			@Override
			public void run()
			{
				map.setGroupObjects(actionGroupNodes.isChecked());
				map.refresh();
			}
		};
		actionGroupNodes.setChecked(initialGroupFlag);
      
      actionShowFilter = new Action(Messages.get().ObjectStatusMapView_ActionShowFilter, Action.AS_CHECK_BOX) {
         @Override
         public void run()
         {
            map.enableFilter(actionShowFilter.isChecked());
         }
      };
      actionShowFilter.setChecked(initialShowFilter);
	}
	
	/**
	 * Contribute actions to action bar
	 */
	private void contributeToActionBars()
	{
		IActionBars bars = getViewSite().getActionBars();
		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
	}

	/**
	 * Fill local pull-down menu
	 * 
	 * @param manager
	 *           Menu manager for pull-down menu
	 */
	private void fillLocalPullDown(IMenuManager manager)
	{
		manager.add(actionGroupNodes);
      manager.add(actionShowFilter);
		manager.add(new Separator());
		manager.add(actionRefresh);
	}

	/**
	 * Fill local tool bar
	 * 
	 * @param manager
	 *           Menu manager for local toolbar
	 */
	private void fillLocalToolBar(IToolBarManager manager)
	{
		manager.add(actionRefresh);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
	 */
	@Override
	public void setFocus()
	{
		map.setFocus();
	}
}
