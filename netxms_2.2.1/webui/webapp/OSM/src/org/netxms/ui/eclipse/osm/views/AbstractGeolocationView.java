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
package org.netxms.ui.eclipse.osm.views;

import java.util.HashSet;
import java.util.Set;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.part.ViewPart;
import org.netxms.base.GeoLocation;
import org.netxms.client.NXCSession;
import org.netxms.client.objects.AbstractObject;
import org.netxms.ui.eclipse.console.resources.SharedIcons;
import org.netxms.ui.eclipse.objectbrowser.api.ObjectContextMenu;
import org.netxms.ui.eclipse.osm.Messages;
import org.netxms.ui.eclipse.osm.tools.MapAccessor;
import org.netxms.ui.eclipse.osm.widgets.AbstractGeoMapViewer;
import org.netxms.ui.eclipse.osm.widgets.helpers.GeoMapListener;
import org.netxms.ui.eclipse.shared.ConsoleSharedData;

/**
 * Base class for all geographical views
 */
public abstract class AbstractGeolocationView extends ViewPart implements ISelectionProvider
{
	public static final String JOB_FAMILY = "MapViewJob"; //$NON-NLS-1$
	
	protected AbstractGeoMapViewer map;
	
	private MapAccessor mapAccessor;
	private int zoomLevel = 15;
	private Action actionZoomIn;
	private Action actionZoomOut;
	private ISelection selection;
	private Set<ISelectionChangedListener> selectionChangeListeners = new HashSet<ISelectionChangedListener>();
	
	/**
	 * Get initial center point for displayed map
	 * 
	 * @return
	 */
	protected abstract GeoLocation getInitialCenterPoint();
	
	/**
	 * Get initial zoom level
	 * 
	 * @return
	 */
	protected abstract int getInitialZoomLevel();

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.ViewPart#init(org.eclipse.ui.IViewSite)
	 */
	@Override
	public void init(IViewSite site) throws PartInitException
	{
		super.init(site);
		
		// Initiate loading of required plugins if they was not loaded yet
		try
		{
			Platform.getAdapterManager().loadAdapter(((NXCSession)ConsoleSharedData.getSession()).getTopLevelObjects()[0], "org.eclipse.ui.model.IWorkbenchAdapter"); //$NON-NLS-1$
		}
		catch(Exception e)
		{
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createPartControl(Composite parent)
	{      
		// Map control
		map = createMapViewer(parent, SWT.BORDER);
		map.setViewPart(this);
		
		createActions();
		contributeToActionBars();
		createPopupMenu();
		activateContext();
		
		// Initial map view
		mapAccessor = new MapAccessor(getInitialCenterPoint());
		zoomLevel = getInitialZoomLevel();
		mapAccessor.setZoom(zoomLevel);
		map.showMap(mapAccessor);
		
		map.addMapListener(new GeoMapListener() {
			@Override
			public void onZoom(int zoomLevel)
			{
				AbstractGeolocationView.this.zoomLevel = zoomLevel;
				mapAccessor.setZoom(zoomLevel);
				actionZoomIn.setEnabled(zoomLevel < MapAccessor.MAX_MAP_ZOOM);
				actionZoomOut.setEnabled(zoomLevel > MapAccessor.MIN_MAP_ZOOM);
			}

			@Override
			public void onPan(GeoLocation centerPoint)
			{
				mapAccessor.setLatitude(centerPoint.getLatitude());
				mapAccessor.setLongitude(centerPoint.getLongitude());
			}
		});
		
		getSite().setSelectionProvider(this);
	}
	
   /**
    * Activate context
    */
   private void activateContext()
   {
      IContextService contextService = (IContextService)getSite().getService(IContextService.class);
      if (contextService != null)
      {
         contextService.activateContext("org.netxms.ui.eclipse.osm.context.Geolocation"); //$NON-NLS-1$
      }
   }

	/**
	 * Create actual map viewer control
	 * 
	 * @param parent
	 * @param style
	 * @return
	 */
	protected abstract AbstractGeoMapViewer createMapViewer(Composite parent, int style);
	
	/**
	 * Create actions
	 */
	protected void createActions()
	{
		actionZoomIn = new Action(Messages.get().AbstractGeolocationView_ZoomIn) {
			@Override
			public void run()
			{
				setZoomLevel(zoomLevel + 1);
			}
		};
		actionZoomIn.setImageDescriptor(SharedIcons.ZOOM_IN);
	
		actionZoomOut = new Action(Messages.get().AbstractGeolocationView_ZoomOut) {
			@Override
			public void run()
			{
				setZoomLevel(zoomLevel - 1);
			}
		};
		actionZoomOut.setImageDescriptor(SharedIcons.ZOOM_OUT);
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
	 * @param manager Menu manager for pull-down menu
	 */
	protected void fillLocalPullDown(IMenuManager manager)
	{
		manager.add(actionZoomIn);
		manager.add(actionZoomOut);
	}

	/**
	 * Fill local tool bar
	 * 
	 * @param manager Menu manager for local toolbar
	 */
	protected void fillLocalToolBar(IToolBarManager manager)
	{
		manager.add(actionZoomIn);
		manager.add(actionZoomOut);
	}

	/**
	 * Create pop-up menu for variable list
	 */
	private void createPopupMenu()
	{
		// Create menu manager.
		MenuManager menuMgr = new MenuManager();
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager mgr)
			{
				fillContextMenu(mgr);
			}
		});
	
		// Create menu.
		Menu menu = menuMgr.createContextMenu(map);
		map.setMenu(menu);

      // Register menu for extension.
      getSite().registerContextMenu(menuMgr, this);
	}

	/**
	 * Fill context menu
	 * 
	 * @param manager
	 *           Menu manager
	 */
	protected void fillContextMenu(final IMenuManager manager)
	{
		AbstractObject object = map.getObjectAtPoint(map.getCurrentPoint());
		selection = (object != null) ? new StructuredSelection(object) : new StructuredSelection();
		if (!selection.isEmpty())
		{
		   ObjectContextMenu.fill(manager, getSite(), this);
		}
		else
		{
	      manager.add(actionZoomIn);
	      manager.add(actionZoomOut);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
	 */
	@Override
	public void setFocus()
	{
		map.setFocus();
	}

	/**
	 * Set new zoom level for map
	 * 
	 * @param newLevel new zoom level
	 */
	private void setZoomLevel(int newLevel)
	{
		if ((newLevel < MapAccessor.MIN_MAP_ZOOM) || (newLevel > MapAccessor.MAX_MAP_ZOOM))
			return;
		
		zoomLevel = newLevel;
		mapAccessor.setZoom(zoomLevel);
		map.showMap(mapAccessor);
		
		actionZoomIn.setEnabled(zoomLevel < MapAccessor.MAX_MAP_ZOOM);
		actionZoomOut.setEnabled(zoomLevel > MapAccessor.MIN_MAP_ZOOM);
	}

	/**
	 * @return the mapAccessor
	 */
	protected MapAccessor getMapAccessor()
	{
		return mapAccessor;
	}

   /* (non-Javadoc)
    * @see org.eclipse.jface.viewers.ISelectionProvider#addSelectionChangedListener(org.eclipse.jface.viewers.ISelectionChangedListener)
    */
   @Override
   public void addSelectionChangedListener(ISelectionChangedListener listener)
   {
      selectionChangeListeners.add(listener);
   }

   /* (non-Javadoc)
    * @see org.eclipse.jface.viewers.ISelectionProvider#removeSelectionChangedListener(org.eclipse.jface.viewers.ISelectionChangedListener)
    */
   @Override
   public void removeSelectionChangedListener(ISelectionChangedListener listener)
   {
      selectionChangeListeners.remove(listener);
   }

   /* (non-Javadoc)
    * @see org.eclipse.jface.viewers.ISelectionProvider#getSelection()
    */
   @Override
   public ISelection getSelection()
   {
      return selection;
   }

   /* (non-Javadoc)
    * @see org.eclipse.jface.viewers.ISelectionProvider#setSelection(org.eclipse.jface.viewers.ISelection)
    */
   @Override
   public void setSelection(ISelection selection)
   {
      this.selection = selection;
   }
}
