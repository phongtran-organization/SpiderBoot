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
package org.netxms.ui.eclipse.console;

import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.jface.bindings.BindingManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.client.service.JavaScriptExecutor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CBanner;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;
import org.eclipse.ui.internal.keys.BindingService;
import org.eclipse.ui.keys.IBindingService;
import org.netxms.client.NXCSession;
import org.netxms.client.objects.AbstractObject;
import org.netxms.client.objects.Dashboard;
import org.netxms.ui.eclipse.console.resources.RegionalSettings;
import org.netxms.ui.eclipse.shared.ConsoleSharedData;
import org.netxms.ui.eclipse.tools.MessageDialogHelper;

/**
 * Workbench window advisor
 */
@SuppressWarnings("restriction")
public class ApplicationWorkbenchWindowAdvisor extends WorkbenchWindowAdvisor
{
	/**
	 * @param configurer
	 */
	public ApplicationWorkbenchWindowAdvisor(IWorkbenchWindowConfigurer configurer)
	{
		super(configurer);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.application.WorkbenchWindowAdvisor#createActionBarAdvisor(org.eclipse.ui.application.IActionBarConfigurer)
	 */
	public ActionBarAdvisor createActionBarAdvisor(IActionBarConfigurer configurer)
	{
		return new ApplicationActionBarAdvisor(configurer);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.application.WorkbenchWindowAdvisor#preWindowOpen()
	 */
	@Override
	public void preWindowOpen()
	{
      RegionalSettings.updateFromPreferences();
      
		IWorkbenchWindowConfigurer configurer = getWindowConfigurer();
		configurer.setShowPerspectiveBar(true);
		configurer.setShowStatusLine(false);
		configurer.setTitle(Messages.get().ApplicationWorkbenchWindowAdvisor_AppTitle);
		configurer.setShellStyle(SWT.NO_TRIM);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.application.WorkbenchWindowAdvisor#postWindowCreate()
	 */
	@Override
	public void postWindowCreate()
	{
		super.postWindowCreate();
		
		NXCSession session = (NXCSession)ConsoleSharedData.getSession();
		
		// Changes the page title at runtime
      JavaScriptExecutor executor = RWT.getClient().getService(JavaScriptExecutor.class);
      if (executor != null)
      {
         StringBuilder js = new StringBuilder();
         js.append("document.title = "); //$NON-NLS-1$
         js.append("\"");
         js.append(BrandingManager.getInstance().getProductName());
         js.append(" - [");
         js.append(session.getUserName());
         js.append("@");
         js.append(session.getServerAddress());
         js.append("]");
         js.append("\"");
         executor.execute(js.toString());
      }

		BindingService service = (BindingService)getWindowConfigurer().getWindow().getWorkbench().getService(IBindingService.class);
		BindingManager bindingManager = service.getBindingManager();
		try
		{
			bindingManager.setActiveScheme(service.getScheme("org.netxms.ui.eclipse.defaultKeyBinding")); //$NON-NLS-1$
		}
		catch(NotDefinedException e)
		{
			e.printStackTrace();
		}
		
		final Shell shell = getWindowConfigurer().getWindow().getShell(); 
		shell.setMaximized(true);
		
		for(Control ctrl : shell.getChildren())
		{
			ctrl.setData(RWT.CUSTOM_VARIANT, "gray"); //$NON-NLS-1$
			if (ctrl instanceof CBanner)
			{
				for(Control cc : ((CBanner)ctrl).getChildren())
					cc.setData(RWT.CUSTOM_VARIANT, "gray"); //$NON-NLS-1$
			}
			else if (ctrl.getClass().getName().equals("org.eclipse.swt.widgets.Composite")) //$NON-NLS-1$
			{
				for(Control cc : ((Composite)ctrl).getChildren())
					cc.setData(RWT.CUSTOM_VARIANT, "gray"); //$NON-NLS-1$
			}
		}
		
		Menu menuBar = shell.getMenuBar();
		if (menuBar != null)
		   menuBar.setData(RWT.CUSTOM_VARIANT, "menuBar"); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.application.WorkbenchWindowAdvisor#postWindowOpen()
	 */
	@Override
   public void postWindowOpen()
   {
      String dashboardId = Application.getParameter("dashboard"); //$NON-NLS-1$
      if (dashboardId != null)
         showDashboard(dashboardId);
      showMessageOfTheDay();
   }
    
	/**
	 * Show dashboard
	 * 
	 * @param dashboardId
	 */
	private void showDashboard(String dashboardId)
	{
      NXCSession session = (NXCSession)ConsoleSharedData.getSession();
      
      long objectId;
      try
      {
         objectId = Long.parseLong(dashboardId);
      }
      catch(NumberFormatException e)
      {
         AbstractObject object = session.findObjectByName(dashboardId);
         if ((object == null) || !(object instanceof Dashboard))
         {
            MessageDialogHelper.openError(null, Messages.get().ApplicationWorkbenchWindowAdvisor_Error, String.format(Messages.get().ApplicationWorkbenchWindowAdvisor_CannotOpenDashboard, dashboardId));
            return;
         }
         objectId = object.getObjectId();
      }
      
      Dashboard dashboard = (Dashboard)session.findObjectById(objectId, Dashboard.class);
      if (dashboard == null)
      {
         MessageDialogHelper.openError(null, Messages.get().ApplicationWorkbenchWindowAdvisor_Error, String.format(Messages.get().ApplicationWorkbenchWindowAdvisor_CannotOpenDashboard, dashboardId));
         return;
      }
      
      IWorkbenchPage page = getWindowConfigurer().getWindow().getActivePage();
      try
      {
         IViewPart view = page.showView("org.netxms.ui.eclipse.dashboard.views.DashboardView", Long.toString(objectId), IWorkbenchPage.VIEW_ACTIVATE); //$NON-NLS-1$
         page.setPartState(page.getReference(view), IWorkbenchPage.STATE_MAXIMIZED);
      }
      catch(PartInitException e)
      {
         MessageDialogHelper.openError(null, Messages.get().ApplicationWorkbenchWindowAdvisor_Error, String.format(Messages.get().ApplicationWorkbenchWindowAdvisor_CannotOpenDashboardType2, dashboardId, e.getLocalizedMessage()));
      }
   }

   /* (non-Javadoc)
	 * @see org.eclipse.ui.application.WorkbenchWindowAdvisor#postWindowClose()
	 */
	@Override
	public void postWindowClose()
	{
		super.postWindowClose();
      if (RWT.getUISession().getAttribute("NoPageReload") == null)
      {
         JavaScriptExecutor executor = RWT.getClient().getService(JavaScriptExecutor.class);
         if (executor != null)
            executor.execute("location.reload(true);");
      }
	}
	
	/**
    * Show the message of the day messagebox
    */
   private void showMessageOfTheDay()
   {   
      NXCSession session = (NXCSession)ConsoleSharedData.getSession();
      String message = session.getMessageOfTheDay();
      
      if (!message.isEmpty())
      {
         MessageDialog.openInformation(Display.getCurrent().getActiveShell(), "Announcement", message);
      }
   }
}
