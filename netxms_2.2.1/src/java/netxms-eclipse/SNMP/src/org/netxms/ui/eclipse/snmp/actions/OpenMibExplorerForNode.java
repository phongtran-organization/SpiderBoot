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
package org.netxms.ui.eclipse.snmp.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.netxms.client.objects.AbstractNode;
import org.netxms.ui.eclipse.snmp.Messages;
import org.netxms.ui.eclipse.snmp.views.MibExplorer;
import org.netxms.ui.eclipse.tools.MessageDialogHelper;

/**
 * Change IP address for node.
 */
public class OpenMibExplorerForNode implements IObjectActionDelegate {
	private IWorkbenchWindow window;
	private AbstractNode node;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.IObjectActionDelegate#setActivePart(org.eclipse.jface.
	 * action.IAction, org.eclipse.ui.IWorkbenchPart)
	 */
	@Override
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		window = targetPart.getSite().getWorkbenchWindow();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	@Override
	public void run(IAction action) {
		if (window != null) {
			try {
				MibExplorer view = (MibExplorer) window.getActivePage()
						.showView(MibExplorer.ID);
				view.setNode(node);
			} catch (PartInitException e) {
				MessageDialogHelper.openError(
						window.getShell(),
						Messages.get().OpenMibExplorerForNode_Error,
						Messages.get().OpenMibExplorerForNode_ErrorText
								+ e.getMessage());
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action
	 * .IAction, org.eclipse.jface.viewers.ISelection)
	 */
	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		if ((selection instanceof IStructuredSelection)
				&& (((IStructuredSelection) selection).size() == 1)) {
			final Object obj = ((IStructuredSelection) selection)
					.getFirstElement();
			if (obj instanceof AbstractNode) {
				node = (AbstractNode) obj;
			} else {
				node = null;
			}
		} else {
			node = null;
		}

		action.setEnabled(node != null);
	}
}
