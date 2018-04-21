/**
 * NetXMS - open source network management system
 * Copyright (C) 2003-2017 Victor Kirhenshtein
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
package org.netxms.ui.eclipse.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PartInitException;
import org.netxms.ui.eclipse.console.Messages;
import org.netxms.ui.eclipse.tools.MessageDialogHelper;

/**
 * Generic action for opening view
 */
public abstract class OpenView implements IWorkbenchWindowActionDelegate
{
   protected IWorkbenchWindow window;
   
   /**
    * Get ID of view to open
    * 
    * @return ID of view to open
    */
   protected abstract String getViewId();
   
   /* (non-Javadoc)
    * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
    */
   @Override
   public void run(IAction action)
   {
      if (window != null)
      {  
         try 
         {
            window.getActivePage().showView(getViewId());
         } 
         catch (PartInitException e) 
         {
            MessageDialogHelper.openError(window.getShell(), Messages.get().OpenView_Error, 
                  String.format(Messages.get().OpenView_ErrorText, e.getLocalizedMessage()));
         }
      }
   }

   /* (non-Javadoc)
    * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
    */
   @Override
   public void selectionChanged(IAction action, ISelection selection)
   {
   }

   /* (non-Javadoc)
    * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#dispose()
    */
   @Override
   public void dispose()
   {
   }

   /* (non-Javadoc)
    * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#init(org.eclipse.ui.IWorkbenchWindow)
    */
   @Override
   public void init(IWorkbenchWindow window)
   {
      this.window = window;
   }
}
