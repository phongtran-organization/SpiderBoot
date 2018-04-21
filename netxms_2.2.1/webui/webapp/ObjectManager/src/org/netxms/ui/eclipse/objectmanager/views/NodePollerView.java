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
package org.netxms.ui.eclipse.objectmanager.views;

import java.util.Date;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.commands.ActionHandler;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.part.ViewPart;
import org.netxms.client.NXCSession;
import org.netxms.client.TextOutputListener;
import org.netxms.client.constants.NodePollType;
import org.netxms.client.objects.AbstractNode;
import org.netxms.client.objects.AbstractObject;
import org.netxms.ui.eclipse.console.resources.RegionalSettings;
import org.netxms.ui.eclipse.console.resources.SharedIcons;
import org.netxms.ui.eclipse.objectmanager.Messages;
import org.netxms.ui.eclipse.shared.ConsoleSharedData;
import org.netxms.ui.eclipse.widgets.StyledText;
import org.netxms.ui.eclipse.widgets.helpers.StyleRange;

/**
 * Forced node poll view
 */
public class NodePollerView extends ViewPart
{
   public static final String ID = "org.netxms.ui.eclipse.objectmanager.views.NodePollerView"; //$NON-NLS-1$

   private final String[] POLL_NAME = { 
         "", //$NON-NLS-1$
         Messages.get().NodePollerView_StatusPoll, 
         Messages.get().NodePollerView_FullConfigPoll,
         Messages.get().NodePollerView_InterfacePoll, 
         Messages.get().NodePollerView_TopologyPoll,
         Messages.get().NodePollerView_ConfigPoll, 
         Messages.get().NodePollerView_InstanceDiscovery 
      };
   private static final Color COLOR_ERROR = new Color(Display.getCurrent(), 192, 0, 0);
   private static final Color COLOR_WARNING = new Color(Display.getCurrent(), 255, 128, 0);
   private static final Color COLOR_INFO = new Color(Display.getCurrent(), 0, 128, 0);
   private static final Color COLOR_LOCAL = new Color(Display.getCurrent(), 0, 0, 192);

   private NXCSession session;
   private AbstractNode node;
   private NodePollType pollType;
   private Display display;
   private StyledText textArea;
   private boolean pollActive = false;
   private Action actionRestart;
   private Action actionClearOutput;

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.ui.part.ViewPart#init(org.eclipse.ui.IViewSite)
    */
   @Override
   public void init(IViewSite site) throws PartInitException
   {
      super.init(site);

      session = (NXCSession)ConsoleSharedData.getSession();
      display = Display.getCurrent();

      // Secondary ID must by in form nodeId&pollType
      String[] parts = site.getSecondaryId().split("&"); //$NON-NLS-1$
      if (parts.length != 2)
         throw new PartInitException("Internal error"); //$NON-NLS-1$

      AbstractObject obj = session.findObjectById(Long.parseLong(parts[0]));
      node = ((obj != null) && (obj instanceof AbstractNode)) ? (AbstractNode)obj : null;
      if (node == null)
         throw new PartInitException(Messages.get().NodePollerView_InvalidObjectID);
      pollType = NodePollType.valueOf(parts[1]);

      setPartName(POLL_NAME[pollType.getValue()] + " - " + node.getObjectName()); //$NON-NLS-1$
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
    */
   @Override
   public void createPartControl(Composite parent)
   {
      textArea = new StyledText(parent, SWT.MULTI | SWT.V_SCROLL);
      textArea.setBackground(new Color(Display.getCurrent(), 255, 255, 255));
      textArea.setScrollOnAppend(true);

      activateContext();
      createActions();
      contributeToActionBars();
   }

   /**
    * Activate context
    */
   private void activateContext()
   {
      IContextService contextService = (IContextService)getSite().getService(IContextService.class);
      if (contextService != null)
      {
         contextService.activateContext("org.netxms.ui.eclipse.objectmanager.contexts.NodePollerView"); //$NON-NLS-1$
      }
   }

   /**
    * Create actions
    */
   private void createActions()
   {
      final IHandlerService handlerService = (IHandlerService)getSite().getService(IHandlerService.class);

      actionRestart = new Action(Messages.get().NodePollerView_ActionRestart, SharedIcons.RESTART) {
         @Override
         public void run()
         {
            startPoll();
         }
      };
      actionRestart.setActionDefinitionId("org.netxms.ui.eclipse.objectmanager.commands.restart_poller"); //$NON-NLS-1$
      handlerService.activateHandler(actionRestart.getActionDefinitionId(), new ActionHandler(actionRestart));

      actionClearOutput = new Action(Messages.get().NodePollerView_ActionClear, SharedIcons.CLEAR_LOG) {
         @Override
         public void run()
         {
            textArea.setText(""); //$NON-NLS-1$
         }
      };
      actionClearOutput.setActionDefinitionId("org.netxms.ui.eclipse.objectmanager.commands.clear_output"); //$NON-NLS-1$
      handlerService.activateHandler(actionClearOutput.getActionDefinitionId(), new ActionHandler(actionClearOutput));
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
   private void fillLocalPullDown(IMenuManager manager)
   {
      manager.add(actionRestart);
      manager.add(actionClearOutput);
   }

   /**
    * Fill local tool bar
    * 
    * @param manager Menu manager for local toolbar
    */
   private void fillLocalToolBar(IToolBarManager manager)
   {
      manager.add(actionRestart);
      manager.add(actionClearOutput);
   }

   /**
    * Fill context menu
    * 
    * @param mgr Menu manager
    */
   protected void fillContextMenu(final IMenuManager manager)
   {
      manager.add(actionClearOutput);
   }

   /**
    * Add poller message to text area
    * 
    * @param message poller message
    */
   private void addPollerMessage(String message)
   {
      Date now = new Date();
      textArea.append("[" + RegionalSettings.getDateTimeFormat().format(now) + "] "); //$NON-NLS-1$ //$NON-NLS-2$

      int index = message.indexOf(0x7F);
      if (index != -1)
      {
         textArea.append(message.substring(0, index));
         char code = message.charAt(index + 1);
         int lastPos = textArea.getCharCount();
         final String msgPart = message.substring(index + 2);
         textArea.append(msgPart);

         StyleRange style = new StyleRange();
         style.start = lastPos;
         style.length = msgPart.length();
         style.foreground = getTextColor(code);
         textArea.setStyleRange(style);
      }
      else
      {
         textArea.append(message);
      }
   }

   /**
    * Get color from color code
    * 
    * @param code
    * @return
    */
   private Color getTextColor(char code)
   {
      switch(code)
      {
         case 'e':
            return COLOR_ERROR;
         case 'w':
            return COLOR_WARNING;
         case 'i':
            return COLOR_INFO;
         case 'l':
            return COLOR_LOCAL;
      }
      return null;
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
    */
   @Override
   public void setFocus()
   {
      textArea.setFocus();
   }

   /**
    * Start poll
    */
   public void startPoll()
   {
      if (pollActive)
         return;
      pollActive = true;
      actionRestart.setEnabled(false);

      addPollerMessage("\u007Fl**** Poll request sent to server ****\r\n"); //$NON-NLS-1$

      final TextOutputListener listener = new TextOutputListener() {
         @Override
         public void messageReceived(final String message)
         {
            display.asyncExec(new Runnable() {
               @Override
               public void run()
               {
                  if (!textArea.isDisposed())
                     addPollerMessage(message);
               }
            });
         }

         @Override
         public void setStreamId(long streamId)
         {
         }
      };

      Job job = new Job(String.format(Messages.get().NodePollerView_JobName, node.getObjectName(), node.getObjectId())) {
         @Override
         protected IStatus run(IProgressMonitor monitor)
         {
            try
            {
               session.pollNode(node.getObjectId(), pollType, listener);
               onPollComplete(true, null);
            }
            catch(Exception e)
            {
               onPollComplete(false, e.getMessage());
            }
            return Status.OK_STATUS;
         }
      };
      job.setSystem(true);
      job.schedule();
   }

   /**
    * Poll completion handler
    * 
    * @param success
    * @param errorMessage
    */
   private void onPollComplete(final boolean success, final String errorMessage)
   {
      display.asyncExec(new Runnable() {
         @Override
         public void run()
         {
            if (textArea.isDisposed())
               return;

            if (success)
            {
               addPollerMessage("\u007Fl**** Poll completed successfully ****\r\n\r\n"); //$NON-NLS-1$
            }
            else
            {
               addPollerMessage(String.format("\u007FePOLL ERROR: %s", errorMessage)); //$NON-NLS-1$
               addPollerMessage("\u007Fl**** Poll failed ****\r\n\r\n"); //$NON-NLS-1$
            }
            pollActive = false;
            actionRestart.setEnabled(true);
         }
      });
   }
}
