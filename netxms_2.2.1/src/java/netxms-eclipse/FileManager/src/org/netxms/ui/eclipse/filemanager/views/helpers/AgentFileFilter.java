/**
 * NetXMS - open source network management system
 * Copyright (C) 2003-2011 Victor Kirhenshtein
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
package org.netxms.ui.eclipse.filemanager.views.helpers;

import java.util.List;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.netxms.client.server.AgentFile;

/**
 * Filter for server file editor
 */
public class AgentFileFilter extends ViewerFilter {
	private String filterString = null;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.viewers.ViewerFilter#select(org.eclipse.jface.viewers
	 * .Viewer, java.lang.Object, java.lang.Object)
	 */
	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		if ((filterString == null) || (filterString.isEmpty()))
			return true;

		final AgentFile filename = (AgentFile) element;

		boolean pass = filename.getName().toLowerCase()
				.contains(filterString.toLowerCase());
		List<AgentFile> children = filename.getChildren();
		if (!pass && children != null) {
			pass = containString(children);
		}
		return pass;
	}

	/**
	 * @param children
	 * @return
	 */
	private boolean containString(List<AgentFile> children) {
		if (children != null) {
			for (AgentFile f : children) {
				if (f.getName().toLowerCase()
						.contains(filterString.toLowerCase()))
					return true;
			}

			for (AgentFile f : children) {
				if (containString(f.getChildren()))
					return true;
			}
		}
		return false;
	}

	/**
	 * @return the filterString
	 */
	public String getFilterString() {
		return filterString;
	}

	/**
	 * @param filterString
	 *            the filterString to set
	 */
	public void setFilterString(String filterString) {
		this.filterString = filterString.toLowerCase();
	}
}
