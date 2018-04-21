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
package org.netxms.ui.eclipse.networkmaps.actions;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.netxms.client.NXCObjectCreationData;
import org.netxms.client.NXCObjectModificationData;
import org.netxms.client.NXCSession;
import org.netxms.client.objects.AbstractObject;
import org.netxms.client.objects.NetworkMap;
import org.netxms.ui.eclipse.jobs.ConsoleJob;
import org.netxms.ui.eclipse.networkmaps.Activator;
import org.netxms.ui.eclipse.networkmaps.Messages;
import org.netxms.ui.eclipse.objectbrowser.dialogs.CreateObjectDialog;
import org.netxms.ui.eclipse.shared.ConsoleSharedData;

/**
 * Create container object
 * 
 */
public class CloneNetworkMap implements IObjectActionDelegate {
	private IWorkbenchWindow window;
	private IWorkbenchPart part;
	private NetworkMap source = null;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.IObjectActionDelegate#setActivePart(org.eclipse.jface.
	 * action.IAction, org.eclipse.ui.IWorkbenchPart)
	 */
	@Override
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		part = targetPart;
		window = targetPart.getSite().getWorkbenchWindow();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	@Override
	public void run(IAction action) {
		if (source == null)
			return;

		final CreateObjectDialog dlg = new CreateObjectDialog(
				window.getShell(), Messages.get().CloneNetworkMap_NetworkMap);
		if (dlg.open() != Window.OK)
			return;

		final NXCSession session = (NXCSession) ConsoleSharedData.getSession();
		new ConsoleJob(Messages.get().CreateNetworkMap_JobName, part,
				Activator.PLUGIN_ID, null) {
			@Override
			protected void runInternal(IProgressMonitor monitor)
					throws Exception {
				NXCObjectCreationData cd = new NXCObjectCreationData(
						AbstractObject.OBJECT_NETWORKMAP, dlg.getObjectName(),
						source.getParentIdList()[0]);
				NXCObjectModificationData md = new NXCObjectModificationData(0);
				source.prepareCopy(cd, md);
				long id = session.createObject(cd);
				md.setObjectId(id);
				session.modifyObject(md);
			}

			@Override
			protected String getErrorMessage() {
				return String.format(Messages.get().CreateNetworkMap_JobError,
						dlg.getObjectName());
			}
		}.start();
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
			final Object object = ((IStructuredSelection) selection)
					.getFirstElement();
			if (object instanceof NetworkMap) {
				source = (NetworkMap) object;
			} else {
				source = null;
			}
		} else {
			source = null;
		}

		action.setEnabled(source != null);
	}
}
