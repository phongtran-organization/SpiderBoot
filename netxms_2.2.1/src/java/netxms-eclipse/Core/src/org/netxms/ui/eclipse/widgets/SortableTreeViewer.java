/**
 * NetXMS - open source network management system
 * Copyright (C) 2003-2012 Victor Kirhenshtein
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
package org.netxms.ui.eclipse.widgets;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;
import org.netxms.ui.eclipse.widgets.helpers.TreeSortingListener;

/**
 * Implementation of TreeViewer with column sorting support
 */
public class SortableTreeViewer extends TreeViewer {
	public static final int DEFAULT_STYLE = -1;

	private boolean initialized = false;
	private TreeColumn[] columns;
	private TreeSortingListener sortingListener;

	/**
	 * Constructor
	 * 
	 * @param parent
	 *            Parent composite for table control
	 * @param names
	 *            Column names
	 * @param widths
	 *            Column widths (may be null)
	 * @param defaultSortingColumn
	 *            Index of default sorting column
	 */
	public SortableTreeViewer(Composite parent, String[] names, int[] widths,
			int defaultSortingColumn, int defaultSortDir, int style) {
		super(new Tree(parent,
				(style == DEFAULT_STYLE) ? (SWT.MULTI | SWT.FULL_SELECTION)
						: style));
		getTree().setLinesVisible(true);
		getTree().setHeaderVisible(true);
		createColumns(names, widths, defaultSortingColumn, defaultSortDir);
	}

	/**
	 * Constructor for delayed initialization
	 * 
	 * @param parent
	 * @param style
	 */
	public SortableTreeViewer(Composite parent, int style) {
		super(new Tree(parent,
				(style == DEFAULT_STYLE) ? (SWT.MULTI | SWT.FULL_SELECTION)
						: style));
		getTree().setLinesVisible(true);
		getTree().setHeaderVisible(true);
	}

	/**
	 * Create columns
	 * 
	 * @param names
	 * @param widths
	 * @param defaultSortingColumn
	 * @param defaultSortDir
	 */
	public void createColumns(String[] names, int[] widths,
			int defaultSortingColumn, int defaultSortDir) {
		if (initialized)
			return;
		initialized = true;

		sortingListener = new TreeSortingListener(this);

		columns = new TreeColumn[names.length];
		for (int i = 0; i < names.length; i++) {
			columns[i] = new TreeColumn(getTree(), SWT.LEFT);
			columns[i].setText(names[i]);
			if (widths != null)
				columns[i].setWidth(widths[i]);
			columns[i].setData("ID", new Integer(i)); //$NON-NLS-1$
			columns[i].addSelectionListener(sortingListener);
		}

		if ((defaultSortingColumn >= 0)
				&& (defaultSortingColumn < names.length))
			getTree().setSortColumn(columns[defaultSortingColumn]);
		getTree().setSortDirection(defaultSortDir);
	}

	/**
	 * Get column object by id (named data with key ID)
	 * 
	 * @param id
	 *            Column ID
	 * @return Column object or null if object with given ID not found
	 */
	public TreeColumn getColumnById(int id) {
		for (int i = 0; i < columns.length; i++) {
			if ((Integer) columns[i].getData("ID") == id) //$NON-NLS-1$
			{
				return columns[i];
			}
		}
		return null;
	}

	/**
	 * Get column index at given point
	 * 
	 * @param p
	 * @return
	 */
	public TreeColumn getColumnAtPoint(Point p) {
		TreeItem item = getTree().getItem(p);
		if (item == null)
			return null;
		int columnCount = getTree().getColumnCount();
		for (int i = 0; i < columnCount; i++) {
			Rectangle rect = item.getBounds(i);
			if (rect.contains(p)) {
				return getTree().getColumn(i);
			}
		}
		return null;
	}

	/**
	 * @return the initialized
	 */
	public boolean isInitialized() {
		return initialized;
	}

	/**
	 * Disable sorting
	 */
	public void disableSorting() {
		for (int i = 0; i < columns.length; i++)
			columns[i].removeSelectionListener(sortingListener);
		getTree().setSortColumn(null);
	}
}
