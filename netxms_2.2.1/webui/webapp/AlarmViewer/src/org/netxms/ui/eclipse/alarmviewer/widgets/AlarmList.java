/**
 * NetXMS - open source network management system
 * Copyright (C) 2003-2016 Victor Kirhenshtein
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
package org.netxms.ui.eclipse.alarmviewer.widgets;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.client.service.UrlLauncher;
import org.eclipse.rap.rwt.service.ServerPushSession;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.netxms.client.NXCSession;
import org.netxms.client.SessionListener;
import org.netxms.client.SessionNotification;
import org.netxms.client.constants.UserAccessRights;
import org.netxms.client.events.Alarm;
import org.netxms.client.events.BulkAlarmStateChangeData;
import org.netxms.client.objects.AbstractObject;
import org.netxms.ui.eclipse.actions.ExportToCsvAction;
import org.netxms.ui.eclipse.alarmviewer.Activator;
import org.netxms.ui.eclipse.alarmviewer.Messages;
import org.netxms.ui.eclipse.alarmviewer.dialogs.AcknowledgeCustomTimeDialog;
import org.netxms.ui.eclipse.alarmviewer.dialogs.AlarmStateChangeFailureDialog;
import org.netxms.ui.eclipse.alarmviewer.views.AlarmComments;
import org.netxms.ui.eclipse.alarmviewer.views.AlarmDetails;
import org.netxms.ui.eclipse.alarmviewer.widgets.helpers.AlarmAcknowledgeTimeFunctions;
import org.netxms.ui.eclipse.alarmviewer.widgets.helpers.AlarmComparator;
import org.netxms.ui.eclipse.alarmviewer.widgets.helpers.AlarmListFilter;
import org.netxms.ui.eclipse.alarmviewer.widgets.helpers.AlarmListLabelProvider;
import org.netxms.ui.eclipse.console.resources.GroupMarkers;
import org.netxms.ui.eclipse.console.resources.SharedIcons;
import org.netxms.ui.eclipse.jobs.ConsoleJob;
import org.netxms.ui.eclipse.objectview.views.TabbedObjectView;
import org.netxms.ui.eclipse.shared.ConsoleSharedData;
import org.netxms.ui.eclipse.tools.FilteringMenuManager;
import org.netxms.ui.eclipse.tools.MessageDialogHelper;
import org.netxms.ui.eclipse.tools.RefreshTimer;
import org.netxms.ui.eclipse.tools.VisibilityValidator;
import org.netxms.ui.eclipse.tools.WidgetHelper;
import org.netxms.ui.eclipse.widgets.CompositeWithMessageBar;
import org.netxms.ui.eclipse.widgets.SortableTableViewer;

/**
 * Alarm list widget
 */
public class AlarmList extends CompositeWithMessageBar
{
   public static final String JOB_FAMILY = "AlarmViewJob"; //$NON-NLS-1$

   // Columns
   public static final int COLUMN_SEVERITY = 0;
   public static final int COLUMN_STATE = 1;
   public static final int COLUMN_SOURCE = 2;
   public static final int COLUMN_ZONE = 3;
   public static final int COLUMN_MESSAGE = 4;
   public static final int COLUMN_COUNT = 5;
   public static final int COLUMN_COMMENTS = 6;
   public static final int COLUMN_HELPDESK_REF = 7;
	public static final int COLUMN_ACK_BY = 8;
	public static final int COLUMN_CREATED = 9;
	public static final int COLUMN_LASTCHANGE = 10;
	
	private final IViewPart viewPart;
	private NXCSession session = null;
	private SessionListener clientListener = null;
	private RefreshTimer refreshTimer;
	private SortableTableViewer alarmViewer;
   private AlarmListLabelProvider labelProvider;
	private AlarmListFilter alarmFilter;
	private Map<Long, Alarm> alarmList = new HashMap<Long, Alarm>();
   private List<Alarm> filteredAlarmList = new ArrayList<Alarm>();
   private VisibilityValidator visibilityValidator;
   private boolean needInitialRefresh = false;
   private Action actionComments;
   private Action actionAcknowledge;
   private Action actionResolve;
   private Action actionStickyAcknowledge;
   private Action actionTerminate;
   private Action actionShowAlarmDetails;
   private Action actionShowObjectDetails;
   private Action actionCreateIssue;
   private Action actionShowIssue;
   private Action actionUnlinkIssue;
   private Action actionExportToCsv;
   private MenuManager timeAcknowledgeMenu;
   private List<Action> timeAcknowledge;
   private Action timeAcknowledgeOther;
   private Action actionShowColor;

   /**
    * Create alarm list widget
    * 
    * @param viewPart owning view part
    * @param parent parent composite
    * @param style widget style
    * @param configPrefix prefix for saving/loading widget configuration
    */
   public AlarmList(IViewPart viewPart, Composite parent, int style, final String configPrefix, VisibilityValidator visibilityValidator)
	{
		super(parent, style);
		session = ConsoleSharedData.getSession();
		this.viewPart = viewPart;
		this.visibilityValidator = visibilityValidator;
		
		// Setup table columns
		final String[] names = { 
		      Messages.get().AlarmList_ColumnSeverity, 
		      Messages.get().AlarmList_ColumnState, 
		      Messages.get().AlarmList_ColumnSource,
		      "Zone",
		      Messages.get().AlarmList_ColumnMessage, 
		      Messages.get().AlarmList_ColumnCount, 
		      Messages.get().AlarmList_Comments, 
            Messages.get().AlarmList_HelpdeskId, 
		      Messages.get().AlarmList_AckBy, 
		      Messages.get().AlarmList_ColumnCreated, 
		      Messages.get().AlarmList_ColumnLastChange
		   };
		final int[] widths = { 100, 100, 150, 130, 300, 70, 70, 120, 100, 100, 100 };
		alarmViewer = new SortableTableViewer(getContent(), names, widths, 0, SWT.DOWN, SortableTableViewer.DEFAULT_STYLE);
      if (!session.isZoningEnabled())
         alarmViewer.removeColumnById(COLUMN_ZONE);
      WidgetHelper.restoreTableViewerSettings(alarmViewer, Activator.getDefault().getDialogSettings(), configPrefix);

      labelProvider = new AlarmListLabelProvider(alarmViewer);
      labelProvider.setShowColor(Activator.getDefault().getPreferenceStore().getBoolean("SHOW_ALARM_STATUS_COLORS"));
      alarmViewer.setLabelProvider(labelProvider);
      alarmViewer.setContentProvider(new ArrayContentProvider());
      alarmViewer.setComparator(new AlarmComparator());
      alarmFilter = new AlarmListFilter();
      alarmViewer.getTable().addDisposeListener(new DisposeListener() {
         @Override
         public void widgetDisposed(DisposeEvent e)
         {
            WidgetHelper.saveTableViewerSettings(alarmViewer, Activator.getDefault().getDialogSettings(), configPrefix);
         }
      });
      alarmViewer.addDoubleClickListener(new IDoubleClickListener() {
         @Override
         public void doubleClick(DoubleClickEvent event)
         {
            actionShowAlarmDetails.run();
         }
      });

      createActions();
      createPopupMenu();

      addListener(SWT.Resize, new Listener() {
         public void handleEvent(Event e)
         {
            alarmViewer.getControl().setBounds(AlarmList.this.getClientArea());
         }
      });

		if ((visibilityValidator == null) || visibilityValidator.isVisible())
		   refresh();
		else
		   needInitialRefresh = true;
		
		refreshTimer = new RefreshTimer(session.getMinViewRefreshInterval(), alarmViewer.getControl(), new Runnable() {
         @Override
         public void run()
         {
            synchronized(alarmList)
            {
               alarmViewer.refresh();
            }
         }
      });

		// Add client library listener
		clientListener = new SessionListener() {
         @Override
			public void notificationHandler(SessionNotification n)
			{
				switch(n.getCode())
				{
					case SessionNotification.NEW_ALARM:
					case SessionNotification.ALARM_CHANGED:
						synchronized(alarmList)
						{
							alarmList.put(((Alarm)n.getObject()).getId(), (Alarm)n.getObject());
                     filterAndLimit();
						}
						refreshTimer.execute();
						break;
					case SessionNotification.ALARM_TERMINATED:
					case SessionNotification.ALARM_DELETED:
                  synchronized(alarmList)
                  {
                     alarmList.remove(((Alarm)n.getObject()).getId());
                     filterAndLimit();
                  }
                  refreshTimer.execute();
                  break;
               case SessionNotification.MULTIPLE_ALARMS_RESOLVED:
                  synchronized(alarmList)
                  {
                     BulkAlarmStateChangeData d = (BulkAlarmStateChangeData)n.getObject();
                     for(Long id : d.getAlarms())
                     {
                        Alarm a = alarmList.get(id);
                        if (a != null)
                        {
                           a.setResolved(d.getUserId(), d.getChangeTime());
                        }
                     }
                     filterAndLimit();
                  }
                  refreshTimer.execute();
                  break;
               case SessionNotification.MULTIPLE_ALARMS_TERMINATED:
						synchronized(alarmList)
						{
						   for(Long id : ((BulkAlarmStateChangeData)n.getObject()).getAlarms())
						   {
						      alarmList.remove(id);
						   }
						   filterAndLimit();
						}
                  refreshTimer.execute();
                  break;
               default:
                  break;
            }
         }
      };
      session.addListener(clientListener);

      final ServerPushSession pushSession = new ServerPushSession();
      pushSession.start();
      
      final IPreferenceStore ps = Activator.getDefault().getPreferenceStore();
      final IPropertyChangeListener propertyChangeListener = new IPropertyChangeListener() {
         @Override
         public void propertyChange(PropertyChangeEvent event)
         {
            if (event.getProperty().equals("SHOW_ALARM_STATUS_COLORS")) //$NON-NLS-1$
            {
               boolean showColors = ps.getBoolean("SHOW_ALARM_STATUS_COLORS");
               if (labelProvider.isShowColor() != showColors)
               {
                  labelProvider.setShowColor(showColors);
                  actionShowColor.setChecked(showColors);
                  alarmViewer.refresh();
               }
            }
         }
      };
      ps.addPropertyChangeListener(propertyChangeListener);

      addDisposeListener(new DisposeListener() {
         @Override
         public void widgetDisposed(DisposeEvent e)
         {
            ps.removePropertyChangeListener(propertyChangeListener);
            if ((session != null) && (clientListener != null))
               session.removeListener(clientListener);
            pushSession.stop();
         }
      });
	}

   /**
	 * Get selection provider of alarm list
	 * 
	 * @return
	 */
	public ISelectionProvider getSelectionProvider()
	{
		return alarmViewer;
	}

   /**
    * Create actions
    */
   private void createActions()
   {
      actionComments = new Action(Messages.get().AlarmList_Comments, Activator.getImageDescriptor("icons/comments.png")) { //$NON-NLS-1$
         @Override
         public void run()
         {
            openAlarmDetailsView(AlarmComments.ID);
         }
      };
      actionComments.setId("org.netxms.ui.eclipse.alarmviewer.popupActions.Comments"); //$NON-NLS-1$

		actionShowAlarmDetails = new Action(Messages.get().AlarmList_ActionAlarmDetails) {
			@Override
			public void run()
			{
				openAlarmDetailsView(AlarmDetails.ID);
			}
		};
		actionShowAlarmDetails.setId("org.netxms.ui.eclipse.alarmviewer.popupActions.AlarmDetails"); //$NON-NLS-1$

		actionAcknowledge = new Action(Messages.get().AlarmList_Acknowledge, Activator.getImageDescriptor("icons/acknowledged.png")) { //$NON-NLS-1$
			@Override
			public void run()
			{
				acknowledgeAlarms(false, 0);
			}
		};
		actionAcknowledge.setId("org.netxms.ui.eclipse.alarmviewer.popupActions.Acknowledge"); //$NON-NLS-1$

		actionStickyAcknowledge = new Action(Messages.get().AlarmList_StickyAck, Activator.getImageDescriptor("icons/acknowledged_sticky.png")) { //$NON-NLS-1$
			@Override
			public void run()
			{
				acknowledgeAlarms(true, 0);
			}
		};
		actionStickyAcknowledge.setId("org.netxms.ui.eclipse.alarmviewer.popupActions.StickyAcknowledge"); //$NON-NLS-1$

		actionResolve = new Action(Messages.get().AlarmList_Resolve, Activator.getImageDescriptor("icons/resolved.png")) { //$NON-NLS-1$
			@Override
			public void run()
			{
				resolveAlarms();
			}
		};
		actionResolve.setId("org.netxms.ui.eclipse.alarmviewer.popupActions.Resolve"); //$NON-NLS-1$

		actionTerminate = new Action(Messages.get().AlarmList_Terminate, Activator.getImageDescriptor("icons/terminated.png")) { //$NON-NLS-1$
			@Override
			public void run()
			{
				terminateAlarms();
			}
		};
		actionTerminate.setId("org.netxms.ui.eclipse.alarmviewer.popupActions.Terminate"); //$NON-NLS-1$
		
      actionCreateIssue = new Action(Messages.get().AlarmList_CreateTicket, Activator.getImageDescriptor("icons/helpdesk_ticket.png")) { //$NON-NLS-1$
         @Override
         public void run()
         {
            createIssue();
         }
      };
      
      actionShowIssue = new Action(Messages.get().AlarmList_ShowTicketInBrowser, SharedIcons.BROWSER) {
         @Override
         public void run()
         {
            showIssue();
         }
      };
      
      actionUnlinkIssue = new Action(Messages.get().AlarmList_UnlinkTicket) {
         @Override
         public void run()
         {
            unlinkIssue();
         }
      };
      
		actionShowObjectDetails = new Action(Messages.get().AlarmList_ActionObjectDetails) {
			@Override
			public void run()
			{
				showObjectDetails();
			}
		};
		actionShowObjectDetails.setId("org.netxms.ui.eclipse.alarmviewer.popupActions.ShowObjectDetails"); //$NON-NLS-1$
		
		actionExportToCsv = new ExportToCsvAction(viewPart, alarmViewer, true);
		
		//time based sticky acknowledgement	
		timeAcknowledgeOther = new Action("Other...", Activator.getImageDescriptor("icons/acknowledged.png")) { //$NON-NLS-1$ //$NON-NLS-2$
         @Override
         public void run()
         {
            AcknowledgeCustomTimeDialog dlg = new AcknowledgeCustomTimeDialog(viewPart.getSite().getShell());
            if (dlg.open() == Window.OK)
            {
               int time = dlg.getTime();
               if (time > 0)
                  acknowledgeAlarms(true, time);
            }
         }
      };
      timeAcknowledgeOther.setId("org.netxms.ui.eclipse.alarmviewer.popupActions.TimeAcknowledgeOther");  //$NON-NLS-1$

      actionShowColor = new Action(Messages.get().AlarmList_ShowStatusColors, Action.AS_CHECK_BOX) {
         @Override
         public void run()
         {
            labelProvider.setShowColor(actionShowColor.isChecked());
            alarmViewer.refresh();
            Activator.getDefault().getPreferenceStore().setValue("SHOW_ALARM_STATUS_COLORS", actionShowColor.isChecked());
         }
      };
      actionShowColor.setChecked(labelProvider.isShowColor());
	}

	/**
	 * 
	 */
	private void initializeTimeAcknowledge()
   {
      IDialogSettings settings = Activator.getDefault().getDialogSettings();
      int menuSize;
      try
      {
         menuSize = settings.getInt("AlarmList.ackMenuSize");//$NON-NLS-1$
      }
      catch(NumberFormatException e)
      {
         settings.put("AlarmList.ackMenuSize", 4); //$NON-NLS-1$
         timeAcknowledge = new ArrayList<Action>(4);
         createDefaultIntervals();
         settings.put("AlarmList.ackMenuEntry0", 1 * 60 * 60); //$NON-NLS-1$
         settings.put("AlarmList.ackMenuEntry1", 4 * 60 * 60); //$NON-NLS-1$
         settings.put("AlarmList.ackMenuEntry2", 24 * 60 * 60); //$NON-NLS-1$
         settings.put("AlarmList.ackMenuEntry3", 2 * 24 * 60 * 60); //$NON-NLS-1$
         return;
	   }
	   timeAcknowledge = new ArrayList<Action>(menuSize);
      for(int i = 0; i < menuSize; i++)
      {
         final int time = settings.getInt("AlarmList.ackMenuEntry" + Integer.toString(i)); //$NON-NLS-1$
	      if (time == 0)
	         continue;
	      String title = AlarmAcknowledgeTimeFunctions.timeToString(time);
	      Action action = new Action(title, Activator.getImageDescriptor("icons/acknowledged.png")) { //$NON-NLS-1$
	         @Override
	         public void run()
	         {
	            acknowledgeAlarms(true, time);
	         }
	      };
         action.setId("org.netxms.ui.eclipse.alarmviewer.popupActions.TimeAcknowledge" + Integer.toString(i) + "ID"); //$NON-NLS-1$ //$NON-NLS-2$
	      timeAcknowledge.add(action);
	   }
   }

   /**
    * 
    */
   private void createDefaultIntervals()
   {
      Action act;
      act = new Action("1 hour(s)", Activator.getImageDescriptor("icons/acknowledged.png")) { //$NON-NLS-1$ //$NON-NLS-2$
         @Override
         public void run()
         {
            int time = 1 * 60 * 60; // hour to minutes, seconds
            acknowledgeAlarms(true, time);
         }
      };
      act.setId("org.netxms.ui.eclipse.alarmviewer.popupActions.TimeAcknowledge0ID"); //$NON-NLS-1$
      timeAcknowledge.add(act);
      
      act = new Action("4 hour(s)", Activator.getImageDescriptor("icons/acknowledged.png")) { //$NON-NLS-1$ //$NON-NLS-2$
         @Override
         public void run()
         {
            int time = 4 * 60 * 60; // hour to minutes, seconds
            acknowledgeAlarms(true, time); 
         }
      };
      act.setId("org.netxms.ui.eclipse.alarmviewer.popupActions.TimeAcknowledge1ID"); //$NON-NLS-1$
      timeAcknowledge.add(act);
      
      act = new Action("1 day(s)", Activator.getImageDescriptor("icons/acknowledged.png")) { //$NON-NLS-1$ //$NON-NLS-2$
         @Override
         public void run()
         {
            int time = 24 * 60 * 60; // day to hours, minutes, seconds
            acknowledgeAlarms(true, time);
         }
      };
      act.setId("org.netxms.ui.eclipse.alarmviewer.popupActions.TimeAcknowledge2ID"); //$NON-NLS-1$
      timeAcknowledge.add(act);
      
      act = new Action("2 days(s)", Activator.getImageDescriptor("icons/acknowledged.png")) { //$NON-NLS-1$ //$NON-NLS-2$
         @Override
         public void run()
         {
            int time = 2 * 24 * 60 * 60; // day to hours, minutes, seconds
            acknowledgeAlarms(true, time);
         }
      };
      act.setId("org.netxms.ui.eclipse.alarmviewer.popupActions.TimeAcknowledge3ID"); //$NON-NLS-1$
      timeAcknowledge.add(act);
   }

   /**
    * Create pop-up menu for alarm list
    */
   private void createPopupMenu()
   {
      // Create menu manager.
      MenuManager menuMgr = new FilteringMenuManager(Activator.PLUGIN_ID);
      menuMgr.setRemoveAllWhenShown(true);
      menuMgr.addMenuListener(new IMenuListener() {
         public void menuAboutToShow(IMenuManager mgr)
         {
            fillContextMenu(mgr);
         }
      });

		// Create menu.
		Menu menu = menuMgr.createContextMenu(alarmViewer.getControl());
		alarmViewer.getControl().setMenu(menu);

		// Register menu for extension.
		if (viewPart != null)
			viewPart.getSite().registerContextMenu(menuMgr, alarmViewer);
	}
	
	/**
	 * Fill context menu
    * 
	 * @param mgr Menu manager
	 */
	protected void fillContextMenu(IMenuManager manager)
	{
		IStructuredSelection selection = (IStructuredSelection)alarmViewer.getSelection();
		if (selection.size() == 0)
			return;
		
		int states = getSelectionType(selection.toArray());
		
		if (states == 2)
		{
   		manager.add(actionAcknowledge);
		   manager.add(actionStickyAcknowledge);

		   if (session.isTimedAlarmAckEnabled())
		   {
      		initializeTimeAcknowledge();
            timeAcknowledgeMenu = new MenuManager(Messages.get().AlarmList_StickyAckMenutTitle, "timeAcknowledge"); //$NON-NLS-1$
            for(Action act : timeAcknowledge)
            {
               timeAcknowledgeMenu.add(act);
            }
            timeAcknowledgeMenu.add(new Separator());   
            timeAcknowledgeMenu.add(timeAcknowledgeOther);
      		manager.add(timeAcknowledgeMenu);
		   }
		}
		
		if (states < 4)
		   manager.add(actionResolve);
		if (states == 4 || !session.isStrictAlarmStatusFlow())
		   manager.add(actionTerminate);
		
		manager.add(new Separator());
		manager.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
		manager.add(new Separator());

		if (selection.size() == 1)
		{
			manager.add(new GroupMarker(GroupMarkers.MB_OBJECT_TOOLS));
			manager.add(new Separator());
			manager.add(actionShowObjectDetails);
			manager.add(new Separator());
		}
		
		manager.add(actionExportToCsv);

      if (selection.size() == 1)
      {
         manager.add(new Separator());
         manager.add(actionShowAlarmDetails);
         manager.add(actionComments);
         if (session.isHelpdeskLinkActive())
         {
            manager.add(new Separator());
            if (((Alarm)selection.getFirstElement()).getHelpdeskState() == Alarm.HELPDESK_STATE_IGNORED)
            {
               manager.add(actionCreateIssue);
            }
            else
            {
               manager.add(actionShowIssue);
               if ((session.getUserSystemRights() & UserAccessRights.SYSTEM_ACCESS_UNLINK_ISSUES) != 0)
                  manager.add(actionUnlinkIssue);
            }
         }
      }
   }

   /**
    * We add 2 to status to give to outstanding status not zero meaning: 
    * STATE_OUTSTANDING + 2 = 2 
    * STATE_ACKNOWLEDGED + 2 = 3
    * STATE_RESOLVED + 2 = 4 
    * It is needed as we can't move STATE_OUTSTANDING to STATE_TERMINATED in strict flow mode. Number of status should be meaningful.
    * 
    * Then we sum all statuses with or command.
    * To STATE_ACKNOWLEDGED only from STATE_OUTSTANDING = 2, STATE_ACKNOWLEDGED = 2
    * To STATE_RESOLVED from STATE_OUTSTANDING and STATE_ACKNOWLEDGED = 2 | 3 = 3, STATE_RESOLVED <=3
    * To STATE_TERMINATED(not strict mode) from any mode(always active)
    * To STATE_TERMINATED(strict mode) only from STATE_RESOLVED = 4, STATE_TERMINATED = 4
    * More results after logical or operation
    * STATE_OUTSTANDING | STATE_RESOLVED = 6
    * STATE_ACKNOWLEDGED | STATE_RESOLVED = 7
    * STATE_OUTSTANDING | STATE_ACKNOWLEDGED | STATE_RESOLVED = 7
    * 
    * @param array selected objects array
    */
   private int getSelectionType(Object[] array)
   {
      int type = 0;
      for(int i = 0; i < array.length; i++)
      {
         type |= ((Alarm)array[i]).getState() + 2;
      }
      return type;
   }

   /**
    * Change root object for alarm list
    * 
    * @param objectId ID of new root object
    */
   public void setRootObject(long objectId)
   {
      alarmFilter.setRootObject(objectId);
      if (needInitialRefresh)
      {
         needInitialRefresh = false;
         refresh();
      }
      else
      {
         synchronized(alarmList)
         {
   	      filterAndLimit();
         }
      }
   }

   /**
    * Change root objects for alarm list. List is refreshed after change.
    * 
    * @param List of objectId
    */
   public void setRootObjects(List<Long> selectedObjects) 
   {
      alarmFilter.setRootObjects(selectedObjects);
      if ((visibilityValidator == null) || visibilityValidator.isVisible())
      {
         synchronized(alarmList)
         {
            filterAndLimit();
         }
      }
   }

   /**
    * Filter all alarms (e.g. by chosen object), sort them by last change and reduce the size to maximum as it is set in
    * configuration parameter <code>AlarmListDisplayLimit</code>.
    */
   private void filterAndLimit()
   {
      // filter
      filteredAlarmList.clear();
      for(Alarm alarm : alarmList.values())
      {
         if (alarmFilter.select(alarm))
         {
            filteredAlarmList.add(alarm);
         }
      }
      
      // limit number of alarms to display
      if ((session.getAlarmListDisplayLimit() > 0) && (filteredAlarmList.size() > session.getAlarmListDisplayLimit()))
      {
         // sort by last change - newest first
         Collections.sort(filteredAlarmList, new Comparator<Alarm>() {
            @Override
            public int compare(Alarm alarm1, Alarm alarm2)
            {
               return -(alarm1.getLastChangeTime().compareTo(alarm2.getLastChangeTime()));
            }
         });
         
         filteredAlarmList = filteredAlarmList.subList(0, session.getAlarmListDisplayLimit());
      }

      alarmViewer.getControl().getDisplay().asyncExec(new Runnable() {
         @Override
         public void run()
         {
            if (!alarmViewer.getControl().isDisposed())
            {
               synchronized(alarmList)
               {
                  alarmViewer.setInput(filteredAlarmList);
                  if ((session.getAlarmListDisplayLimit() > 0) && (filteredAlarmList.size() >= session.getAlarmListDisplayLimit()))
                  {
                     showMessage(INFORMATION, String.format(Messages.get().AlarmList_CountLimitWarning, filteredAlarmList.size()));
                  }
                  else
                  {
                     hideMessage();
                  }
               }
            }
         }
      });
   }

	/**
    * Refresh alarm list
    */
   public void refresh()
   {
      if ((visibilityValidator != null) && !visibilityValidator.isVisible())
	      return;
      new ConsoleJob(Messages.get().AlarmList_SyncJobName, viewPart, Activator.PLUGIN_ID, JOB_FAMILY) {
         @Override
         protected void runInternal(IProgressMonitor monitor) throws Exception
         {
			   HashMap<Long, Alarm> alarms = session.getAlarms();
            synchronized(alarmList)
            {
      		   alarmList.clear();
      		   alarmList.putAll(alarms);
               filterAndLimit();
            }
         }

         @Override
         protected String getErrorMessage()
         {
            return Messages.get().AlarmList_SyncJobError;
         }
      }.start();
   }

   /**
    * Open comments for selected alarm
    */
   private void openAlarmDetailsView(String viewId)
   {
      IStructuredSelection selection = (IStructuredSelection)alarmViewer.getSelection();
      if (selection.size() != 1)
         return;

      final String secondaryId = Long.toString(((Alarm)selection.getFirstElement()).getId());
      IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
      try
      {
         page.showView(viewId, secondaryId, IWorkbenchPage.VIEW_ACTIVATE);
      }
      catch(PartInitException e)
      {
         MessageDialogHelper.openError(getShell(), Messages.get().AlarmList_Error,
               Messages.get().AlarmList_ErrorText + e.getLocalizedMessage());
		}
	}
	
	/**
	 * @param filter
	 */
	public void setStateFilter(int filter)
	{
		alarmFilter.setStateFilter(filter);
	}
	
	/**
	 * @param filter
	 */
	public void setSeverityFilter(int filter)
	{
		alarmFilter.setSeverityFilter(filter);
	}
	
	/**
	 * Acknowledge selected alarms
	 * 
	 * @param sticky
	 */
	private void acknowledgeAlarms(final boolean sticky, final int time)
	{
		IStructuredSelection selection = (IStructuredSelection)alarmViewer.getSelection();
		if (selection.size() == 0)
			return;
		
		final Object[] alarms = selection.toArray();
		new ConsoleJob(Messages.get().AcknowledgeAlarm_JobName, viewPart, Activator.PLUGIN_ID, AlarmList.JOB_FAMILY) {
			@Override
			protected void runInternal(IProgressMonitor monitor) throws Exception
			{
				monitor.beginTask(Messages.get(getDisplay()).AcknowledgeAlarm_TaskName, alarms.length);
				for(Object o : alarms)
				{
					if (monitor.isCanceled())
						break;
					if (o instanceof Alarm)
						session.acknowledgeAlarm(((Alarm)o).getId(), sticky, time);
					monitor.worked(1);
				}
				monitor.done();
			}
			
			@Override
			protected String getErrorMessage()
			{
				return Messages.get().AcknowledgeAlarm_ErrorMessage;
			}
		}.start();
	}
		
	/**
	 * Resolve selected alarms
	 */
	private void resolveAlarms()
	{
		IStructuredSelection selection = (IStructuredSelection)alarmViewer.getSelection();
      if (selection.isEmpty())
			return;
		
      final List<Long> alarmIds = new ArrayList<Long>(selection.size());
      for(Object o : selection.toList())
         alarmIds.add(((Alarm)o).getId());
      new ConsoleJob(Messages.get().AlarmList_Resolving, viewPart, Activator.PLUGIN_ID, AlarmList.JOB_FAMILY) {
         @Override
         protected void runInternal(IProgressMonitor monitor) throws Exception
         {
            final Map<Long, Integer> resolveFails = session.bulkResolveAlarms(alarmIds);
            if (!resolveFails.isEmpty())
            {
               runInUIThread(new Runnable() {
                  @Override
                  public void run()
                  {
                     AlarmStateChangeFailureDialog dlg = new AlarmStateChangeFailureDialog(viewPart.getSite().getShell(), resolveFails);
                     if (dlg.open() == Window.OK)
                     {
                        return;
                     }
                  }
               });
            }
         }
         
         @Override
         protected String getErrorMessage()
         {
            return Messages.get().AlarmList_CannotResoveAlarm;
         }
      }.start();
   }

	/**
    * Terminate selected alarms
    */
   private void terminateAlarms()
   {
      IStructuredSelection selection = (IStructuredSelection)alarmViewer.getSelection();
      if (selection.isEmpty())
         return;     

      final List<Long> alarmIds = new ArrayList<Long>(selection.size());
      for(Object o : selection.toList())
         alarmIds.add(((Alarm)o).getId());
      new ConsoleJob(Messages.get().TerminateAlarm_JobTitle, viewPart, Activator.PLUGIN_ID, AlarmList.JOB_FAMILY) {
         @Override
         protected void runInternal(IProgressMonitor monitor) throws Exception
         {           
            final Map<Long, Integer> terminationFails = session.bulkTerminateAlarms(alarmIds);
            if (!terminationFails.isEmpty())
            {
               runInUIThread(new Runnable() {
                  @Override
                  public void run()
                  {
                     AlarmStateChangeFailureDialog dlg = new AlarmStateChangeFailureDialog(viewPart.getSite().getShell(), terminationFails);
                     if (dlg.open() == Window.OK)
                     {
                        return;
                     }
                  }
               });
            }
         }
         
         @Override
         protected String getErrorMessage()
         {
            return Messages.get().TerminateAlarm_ErrorMessage;
         }
      }.start();
   }

   /**
    * Create helpdesk ticket (issue) from selected alarms
    */
   private void createIssue()
   {
      IStructuredSelection selection = (IStructuredSelection)alarmViewer.getSelection();
      if (selection.size() != 1)
         return;
      
      final long id = ((Alarm)selection.getFirstElement()).getId();
      new ConsoleJob(Messages.get().AlarmList_JobTitle_CreateTicket, viewPart, Activator.PLUGIN_ID, AlarmList.JOB_FAMILY) {
         @Override
         protected void runInternal(IProgressMonitor monitor) throws Exception
         {
            session.openHelpdeskIssue(id);
         }
         
         @Override
         protected String getErrorMessage()
         {
            return Messages.get().AlarmList_JobError_CreateTicket;
         }
      }.start();
   }

   /**
    * Show in web browser helpdesk ticket (issue) linked to selected alarm
    */
   private void showIssue()
   {
      IStructuredSelection selection = (IStructuredSelection)alarmViewer.getSelection();
      if (selection.size() != 1)
         return;
      
      final long id = ((Alarm)selection.getFirstElement()).getId();
      new ConsoleJob(Messages.get().AlarmList_JobTitle_ShowTicket, viewPart, Activator.PLUGIN_ID, AlarmList.JOB_FAMILY) {
         @Override
         protected void runInternal(IProgressMonitor monitor) throws Exception
         {
            final String url = session.getHelpdeskIssueUrl(id);
            runInUIThread(new Runnable() { 
               @Override
               public void run()
               {
                  UrlLauncher launcher = RWT.getClient().getService(UrlLauncher.class);
                  launcher.openURL(url);
               }
            });
         }

         @Override
         protected String getErrorMessage()
         {
            return Messages.get().AlarmList_JobError_ShowTicket;
         }
      }.start();
   }

   /**
    * Unlink helpdesk ticket (issue) from selected alarm
    */
   private void unlinkIssue()
   {
      IStructuredSelection selection = (IStructuredSelection)alarmViewer.getSelection();
      if (selection.size() != 1)
         return;

      final long id = ((Alarm)selection.getFirstElement()).getId();
      new ConsoleJob(Messages.get().AlarmList_JobTitle_UnlinkTicket, viewPart, Activator.PLUGIN_ID, AlarmList.JOB_FAMILY) {
         @Override
         protected void runInternal(IProgressMonitor monitor) throws Exception
         {
            session.unlinkHelpdeskIssue(id);
         }

         @Override
         protected String getErrorMessage()
         {
            return Messages.get().AlarmList_JobError_UnlinkTicket;
         }
      }.start();
   }

   /**
    * Show details for selected object
    */
   private void showObjectDetails()
   {
      IStructuredSelection selection = (IStructuredSelection)alarmViewer.getSelection();
      if (selection.size() != 1)
         return;

      AbstractObject object = session.findObjectById(((Alarm)selection.getFirstElement()).getSourceObjectId());
      if (object != null)
      {
         try
         {
				TabbedObjectView view = (TabbedObjectView)PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(TabbedObjectView.ID);
            view.setObject(object);
         }
         catch(PartInitException e)
         {
            MessageDialogHelper.openError(getShell(), Messages.get().AlarmList_Error,
                  Messages.get().AlarmList_OpenDetailsError + e.getLocalizedMessage());
         }
      }
   }

   /**
    * Get underlying table viewer.
    * 
    * @return
    */
   public TableViewer getViewer()
   {
      return alarmViewer;
   }

   /**
    * Get action to toggle status color display
    * 
    * @return
    */
   public IAction getActionShowColors()
   {
      return actionShowColor;
   }

   /**
    * Enable/disable status color background
    * 
    * @param show
    */
   public void setShowColors(boolean show)
   {
      labelProvider.setShowColor(show);
      actionShowColor.setChecked(show);
      alarmViewer.refresh();
      Activator.getDefault().getPreferenceStore().setValue("SHOW_ALARM_STATUS_COLORS", show);
   }
}
