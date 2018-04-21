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
import org.eclipse.ui.part.ViewPart;
import org.netxms.client.NXCSession;
import org.netxms.client.objects.Container;
import org.netxms.client.objects.AbstractObject;
import org.netxms.client.objects.ServiceRoot;
import org.netxms.ui.eclipse.jobs.ConsoleJob;
import org.netxms.ui.eclipse.objectbrowser.dialogs.ObjectSelectionDialog;
import org.netxms.ui.eclipse.objectmanager.Activator;
import org.netxms.ui.eclipse.objectmanager.Messages;
import org.netxms.ui.eclipse.shared.ConsoleSharedData;

/**
 * "Bind" action
 */
public class BindObject implements IObjectActionDelegate {
	private Shell shell;
	private ViewPart viewPart;
	private long parentId;

	/**
	 * @see IObjectActionDelegate#setActivePart(IAction, IWorkbenchPart)
	 */
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		shell = targetPart.getSite().getShell();
		viewPart = (targetPart instanceof ViewPart) ? (ViewPart) targetPart
				: null;
	}

	/**
	 * @see IActionDelegate#run(IAction)
	 */
	public void run(IAction action) {
		final ObjectSelectionDialog dlg = new ObjectSelectionDialog(shell,
				null, ObjectSelectionDialog.createNodeSelectionFilter(true));
		if (dlg.open() == Window.OK) {
			final NXCSession session = (NXCSession) ConsoleSharedData
					.getSession();
			new ConsoleJob(Messages.get().BindObject_JobTitle, viewPart,
					Activator.PLUGIN_ID, null) {
				@Override
				protected String getErrorMessage() {
					return Messages.get().BindObject_JobError;
				}

				@Override
				protected void runInternal(IProgressMonitor monitor)
						throws Exception {
					List<AbstractObject> objects = dlg.getSelectedObjects();
					for (AbstractObject o : objects)
						session.bindObject(parentId, o.getObjectId());
				}
			}.start();
		}
	}

	/**
	 * @see IActionDelegate#selectionChanged(IAction, ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		if ((selection instanceof IStructuredSelection)
				&& (((IStructuredSelection) selection).size() == 1)) {
			Object obj = ((IStructuredSelection) selection).getFirstElement();
			if ((obj instanceof ServiceRoot) || (obj instanceof Container)) {
				action.setEnabled(true);
				parentId = ((AbstractObject) obj).getObjectId();
			} else {
				action.setEnabled(false);
				parentId = 0;
			}
		} else {
			action.setEnabled(false);
			parentId = 0;
		}
	}
}
