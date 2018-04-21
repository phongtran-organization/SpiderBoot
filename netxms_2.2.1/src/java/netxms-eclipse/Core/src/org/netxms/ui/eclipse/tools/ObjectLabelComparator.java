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
package org.netxms.ui.eclipse.tools;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.TableColumn;

/**
 * Generic object comparator for SortableTableViewer - sort objects by labels
 * 
 */
public class ObjectLabelComparator extends ViewerComparator {
	private ILabelProvider labelProvider;

	/**
	 * The constructor
	 * 
	 * @param labelProvider
	 *            label provider
	 */
	public ObjectLabelComparator(ILabelProvider labelProvider) {
		this.labelProvider = labelProvider;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.viewers.ViewerComparator#compare(org.eclipse.jface.
	 * viewers.Viewer, java.lang.Object, java.lang.Object)
	 */
	@Override
	public int compare(Viewer viewer, Object e1, Object e2) {
		int rc, column;

		if (((TableViewer) viewer).getTable().getColumnCount() > 0) {
			TableColumn sortColumn = ((TableViewer) viewer).getTable()
					.getSortColumn();
			if (sortColumn == null)
				return 0;

			column = (Integer) sortColumn.getData("ID"); //$NON-NLS-1$
		} else {
			// Table control in simple mode, assume column #0
			column = 0;
		}

		if (labelProvider instanceof ITableLabelProvider) {
			rc = ((ITableLabelProvider) labelProvider)
					.getColumnText(e1, column).compareToIgnoreCase(
							((ITableLabelProvider) labelProvider)
									.getColumnText(e2, column));
		} else {
			if (column == 0) {

				rc = labelProvider.getText(e1).compareToIgnoreCase(
						labelProvider.getText(e2));
			} else {
				rc = 0;
			}
		}
		int dir = ((TableViewer) viewer).getTable().getSortDirection();
		return (dir == SWT.UP) ? rc : -rc;
	}
}
