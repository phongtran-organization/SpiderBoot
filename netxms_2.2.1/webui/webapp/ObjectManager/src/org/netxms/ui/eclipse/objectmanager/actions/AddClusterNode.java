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
package org.netxms.ui.eclipse.objectmanager.actions;

import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.netxms.client.NXCSession;
import org.netxms.client.objects.Cluster;
import org.netxms.client.objects.AbstractObject;
import org.netxms.ui.eclipse.jobs.ConsoleJob;
import org.netxms.ui.eclipse.objectbrowser.dialogs.ObjectSelectionDialog;
import org.netxms.ui.eclipse.objectmanager.Activator;
import org.netxms.ui.eclipse.objectmanager.Messages;
import org.netxms.ui.eclipse.shared.ConsoleSharedData;

public class AddClusterNode implements IObjectActionDelegate
{
	private Shell shell;
	private IWorkbenchPart wbPart;
	private long clusterId;

	/**
	 * @see IObjectActionDelegate#setActivePart(IAction, IWorkbenchPart)
	 */
	public void setActivePart(IAction action, IWorkbenchPart targetPart)
	{
		shell = targetPart.getSite().getShell();
		wbPart = targetPart;
	}

	/**
	 * @see IActionDelegate#run(IAction)
	 */
	public void run(IAction action)
	{
		final ObjectSelectionDialog dlg = new ObjectSelectionDialog(shell, null, ObjectSelectionDialog.createNodeSelectionFilter(false));
		dlg.open();
		if (dlg.getReturnCode() == Window.OK)
		{
			final NXCSession session = (NXCSession)ConsoleSharedData.getSession();
			new ConsoleJob(Messages.get().AddClusterNode_JobTitle, wbPart, Activator.PLUGIN_ID, null) {
				@Override
				protected String getErrorMessage()
				{
					return Messages.get().AddClusterNode_JobError;
				}

				@Override
				protected void runInternal(IProgressMonitor monitor) throws Exception
				{
					List<AbstractObject> objects = dlg.getSelectedObjects();
					for(AbstractObject o : objects)
						session.addClusterNode(clusterId, o.getObjectId());
				}
			}.start();
		}
	}

	/**
	 * @see IActionDelegate#selectionChanged(IAction, ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection)
	{
		if (selection instanceof IStructuredSelection)
		{
			Object obj = ((IStructuredSelection)selection).getFirstElement();
			if (obj instanceof Cluster)
			{
				action.setEnabled(true);
				clusterId = ((AbstractObject)obj).getObjectId();
			}
			else
			{
				action.setEnabled(false);
				clusterId = 0;
			}
		}
		else
		{
			action.setEnabled(false);
			clusterId = 0;
		}
	}
}
