/**
 * NetXMS - open source network management system
 * Copyright (C) 2016 RadenSolutions
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
package org.netxms.ui.eclipse.objecttools.views.helpers;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.netxms.client.objecttools.ObjectTool;
import org.netxms.ui.eclipse.objecttools.Messages;

public class ObjectToolsFilter extends ViewerFilter {
	private String filterString = null;
	private static final String[] toolTypes = {
			Messages.get().ObjectToolsLabelProvider_TypeInternal,
			Messages.get().ObjectToolsLabelProvider_TypeAgentCmd,
			Messages.get().ObjectToolsLabelProvider_TypeSNMPList,
			Messages.get().ObjectToolsLabelProvider_TypeAgentTable,
			Messages.get().ObjectToolsLabelProvider_TypeURL,
			Messages.get().ObjectToolsLabelProvider_TypeLocalCmd,
			Messages.get().ObjectToolsLabelProvider_TypeServerCmd,
			Messages.get().ObjectToolsLabelProvider_TypeDownloadFile };

	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		if ((filterString == null) || (filterString.isEmpty()))
			return true;
		else if (containsId(element))
			return true;
		else if (containsName(element))
			return true;
		else if (containsType(element))
			return true;
		else if (containsDescription(element))
			return true;
		return false;
	}

	public boolean containsId(Object element) {
		if (Long.toString(((ObjectTool) element).getId()).toLowerCase()
				.contains(filterString))
			return true;
		return false;
	}

	public boolean containsName(Object element) {
		if (((ObjectTool) element).getName().toLowerCase()
				.contains(filterString))
			return true;
		return false;
	}

	public boolean containsType(Object element) {
		if (toolTypes[((ObjectTool) element).getToolType()].toLowerCase()
				.contains(filterString))
			return true;
		return false;
	}

	public boolean containsDescription(Object element) {
		if (((ObjectTool) element).getDescription().toLowerCase()
				.contains(filterString))
			return true;
		return false;
	}

	/**
	 * @param filterString
	 *            the filterString to set
	 */
	public void setFilterString(String filterString) {
		this.filterString = filterString.toLowerCase();
	}
}
