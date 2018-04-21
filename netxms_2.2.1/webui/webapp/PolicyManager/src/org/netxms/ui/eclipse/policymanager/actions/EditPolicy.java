/**
 * NetXMS - open source network management system
 * Copyright (C) 2003-2010 Victor Kirhenshtein
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
package org.netxms.ui.eclipse.policymanager.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.netxms.client.objects.AbstractObject;
import org.netxms.client.objects.AgentPolicyConfig;
import org.netxms.client.objects.AgentPolicyLogParser;
import org.netxms.ui.eclipse.policymanager.views.ConfigPolicyEditor;
import org.netxms.ui.eclipse.policymanager.views.LogParserPolicyEditor;
import org.netxms.ui.eclipse.tools.MessageDialogHelper;

/**
 * Create policy group
 */
public class EditPolicy implements IObjectActionDelegate
{
   private IWorkbenchWindow window;
   private AbstractObject currentSelection = null;
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IObjectActionDelegate#setActivePart(org.eclipse.jface.action.IAction, org.eclipse.ui.IWorkbenchPart)
	 */
	@Override
	public void setActivePart(IAction action, IWorkbenchPart targetPart)
	{
	   window = targetPart.getSite().getWorkbenchWindow();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	@Override
	public void run(IAction action)
	{
	   if(currentSelection != null)
	   {
   	   if(currentSelection instanceof AgentPolicyConfig)
   	   {
   	      try
   	      {
   	         ConfigPolicyEditor view = (ConfigPolicyEditor)window.getActivePage().showView(ConfigPolicyEditor.ID);
   	         view.setPolicy(currentSelection);
   	      }
   	      catch(PartInitException e)
   	      {
   	         MessageDialogHelper.openError(window.getShell(), "", "");
   	      }   	         	      
   	   }
   	   else if(currentSelection instanceof AgentPolicyLogParser)
   	   {
            try
            {
               LogParserPolicyEditor view = (LogParserPolicyEditor)window.getActivePage().showView(LogParserPolicyEditor.ID);
               view.setPolicy(currentSelection);
            }
            catch(PartInitException e)
            {
               MessageDialogHelper.openError(window.getShell(), "", "");
            }      	      
   	   }
	   }
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
	 */
	@Override
	public void selectionChanged(IAction action, ISelection selection)
	{
	   currentSelection = null;
		if (selection instanceof TreeSelection)
		{
		   currentSelection = (AbstractObject)((TreeSelection)selection).getFirstElement();
			action.setEnabled((currentSelection instanceof AgentPolicyConfig) || (currentSelection instanceof AgentPolicyLogParser));
		}
	}
}
