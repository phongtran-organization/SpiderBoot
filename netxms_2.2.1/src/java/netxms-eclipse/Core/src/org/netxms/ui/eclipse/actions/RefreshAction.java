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
package org.netxms.ui.eclipse.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.commands.ActionHandler;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.handlers.IHandlerService;
import org.netxms.ui.eclipse.console.Activator;
import org.netxms.ui.eclipse.console.Messages;

/**
 * Refresh action - provides corret icon and text
 */
public class RefreshAction extends Action {
	public static final String ID = "org.netxms.ui.eclipse.library.actions.refresh"; //$NON-NLS-1$

	/**
	 * Create default refresh action
	 */
	public RefreshAction() {
		super(Messages.get().RefreshAction_Name, Activator
				.getImageDescriptor("icons/refresh.gif")); //$NON-NLS-1$
		setId(ID);
	}

	/**
	 * Create refresh action attached to handler service
	 * 
	 * @param viewPart
	 *            owning view part
	 */
	public RefreshAction(IViewPart viewPart) {
		super(Messages.get().RefreshAction_Name, Activator
				.getImageDescriptor("icons/refresh.gif")); //$NON-NLS-1$

		setId(ID);

		final IHandlerService handlerService = (IHandlerService) viewPart
				.getSite().getService(IHandlerService.class);
		setActionDefinitionId("org.netxms.ui.eclipse.library.commands.refresh"); //$NON-NLS-1$
		handlerService.activateHandler(getActionDefinitionId(),
				new ActionHandler(this));
	}
}
