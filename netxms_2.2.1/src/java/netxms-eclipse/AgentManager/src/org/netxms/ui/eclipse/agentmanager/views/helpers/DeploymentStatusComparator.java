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
package org.netxms.ui.eclipse.agentmanager.views.helpers;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.netxms.ui.eclipse.agentmanager.views.PackageDeploymentMonitor;
import org.netxms.ui.eclipse.widgets.SortableTableViewer;

/**
 * Deployment status objects comparator
 */
public class DeploymentStatusComparator extends ViewerComparator {
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.viewers.ViewerComparator#compare(org.eclipse.jface.
	 * viewers.Viewer, java.lang.Object, java.lang.Object)
	 */
	@Override
	public int compare(Viewer viewer, Object e1, Object e2) {
		DeploymentStatus s1 = (DeploymentStatus) e1;
		DeploymentStatus s2 = (DeploymentStatus) e2;
		int result;
		switch ((Integer) ((SortableTableViewer) viewer).getTable()
				.getSortColumn().getData("ID")) //$NON-NLS-1$
		{
		case PackageDeploymentMonitor.COLUMN_NODE:
			result = s1.getNodeName().compareToIgnoreCase(s2.getNodeName());
			break;
		case PackageDeploymentMonitor.COLUMN_STATUS:
			result = s1.getStatus() - s2.getStatus();
			break;
		case PackageDeploymentMonitor.COLUMN_ERROR:
			result = s1.getMessage().compareToIgnoreCase(s2.getMessage());
			break;
		default:
			result = 0;
		}
		return (((SortableTableViewer) viewer).getTable().getSortDirection() == SWT.UP) ? result
				: -result;
	}
}
