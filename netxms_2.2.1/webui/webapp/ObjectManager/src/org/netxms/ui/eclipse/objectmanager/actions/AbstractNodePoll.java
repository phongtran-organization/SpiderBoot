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
package org.netxms.ui.eclipse.objectmanager.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.netxms.client.constants.NodePollType;
import org.netxms.client.objects.AbstractNode;
import org.netxms.ui.eclipse.objectmanager.Messages;
import org.netxms.ui.eclipse.objectmanager.views.NodePollerView;
import org.netxms.ui.eclipse.tools.MessageDialogHelper;

/**
 * Generic poll starter
 */
public abstract class AbstractNodePoll implements IObjectActionDelegate
{
	private IWorkbenchWindow window;
	private AbstractNode node = null;
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	@Override
	public void run(IAction action)
	{
		if (node == null)
		   return;

		String msg = getConfirmation();
		if (msg != null)
		{
		   if (!MessageDialogHelper.openQuestion(window.getShell(), Messages.get().AbstractNodePoll_Warning, msg))
		      return;
		}
		
		try
		{
			NodePollerView view = (NodePollerView)window.getActivePage().showView(NodePollerView.ID, Long.toString(node.getObjectId()) + "&" + getPollType(), IWorkbenchPage.VIEW_ACTIVATE); //$NON-NLS-1$
			view.startPoll();
		}
		catch(PartInitException e)
		{
			MessageDialogHelper.openError(window.getShell(), Messages.get().AbstractNodePoll_Error, String.format(Messages.get().AbstractNodePoll_ErrorText, e.getLocalizedMessage()));
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
	 */
	@Override
	public void selectionChanged(IAction action, ISelection selection)
	{
		Object obj;
		if ((selection instanceof IStructuredSelection) &&
		    (((IStructuredSelection)selection).size() == 1) &&
			 ((obj = ((IStructuredSelection)selection).getFirstElement()) instanceof AbstractNode))
		{
			node = (AbstractNode)obj;
		}
		else
		{
			node = null;
		}
		action.setEnabled(node != null);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IObjectActionDelegate#setActivePart(org.eclipse.jface.action.IAction, org.eclipse.ui.IWorkbenchPart)
	 */
	@Override
	public void setActivePart(IAction action, IWorkbenchPart targetPart)
	{
		window = targetPart.getSite().getWorkbenchWindow();
	}
	
	/**
	 * Get poll type
	 * 
	 * @return
	 */
	abstract protected NodePollType getPollType();
	
	/**
	 * @return
	 */
	protected String getConfirmation()
	{
	   return null;
	}
}
