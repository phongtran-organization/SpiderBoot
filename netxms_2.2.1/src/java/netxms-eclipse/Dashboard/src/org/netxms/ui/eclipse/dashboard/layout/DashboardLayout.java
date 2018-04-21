/**
 * NetXMS - open source network management system
 * Copyright (C) 2003-2017 Victor Kirhenshtein
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
package org.netxms.ui.eclipse.dashboard.layout;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Layout;

/**
 * Layout for dashboards
 */
public class DashboardLayout extends Layout {
	public int numColumns = 1;
	public int marginWidth = 0;
	public int marginHeight = 0;
	public int horizontalSpacing = 5;
	public int verticalSpacing = 5;

	private static final int MAX_ROWS = 64;

	private int rowCount = 0;
	private Map<Control, Point> coordinates = new HashMap<Control, Point>(0);
	private int[] rowStart = null;
	private Point cachedSize = null;

	/**
	 * Create logical grid
	 */
	private void createGrid(Composite parent) {
		Control[] children = parent.getChildren();
		coordinates = new HashMap<Control, Point>(children.length);

		boolean[][] cellUsed = new boolean[MAX_ROWS][numColumns];
		int currentColumn = 0;
		int currentRow = 0;
		rowCount = 0;
		for (Control child : children) {
			if (!child.isVisible())
				continue;
			DashboardLayoutData layoutData = getLayoutData(child);

			int columnSpan = layoutData.horizontalSpan;
			if (columnSpan > numColumns) {
				throw new IllegalArgumentException("colSpan (" + columnSpan
						+ ") > number of columns (" + numColumns + ")");
			}

			int freeSpace = 0;
			for (int j = currentColumn; j < numColumns
					&& freeSpace < columnSpan; j++) {
				if (!cellUsed[currentRow][j]) {
					freeSpace++;
				} else {
					currentColumn++;
					freeSpace = 0;
				}
			}

			if (freeSpace < columnSpan) {
				currentRow++;
				currentColumn = 0;
			}
			for (int _c = 0; _c < columnSpan; _c++) {
				for (int _r = 0; _r < layoutData.verticalSpan; _r++) {
					cellUsed[currentRow + _r][currentColumn + _c] = true;
				}
			}
			coordinates.put(child, new Point(currentColumn, currentRow));

			rowCount = Math.max(currentRow + layoutData.verticalSpan, rowCount);

			currentColumn += columnSpan;
			if (currentColumn == numColumns) {
				currentColumn = 0;
				currentRow++;
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.swt.widgets.Layout#computeSize(org.eclipse.swt.widgets.Composite
	 * , int, int, boolean)
	 */
	@Override
	protected Point computeSize(Composite composite, int wHint, int hHint,
			boolean flushCache) {
		if ((wHint != SWT.DEFAULT) && (hHint != SWT.DEFAULT))
			return new Point(wHint, hHint);

		if ((cachedSize != null) && !flushCache)
			return new Point(cachedSize.x, cachedSize.y);

		createGrid(composite);
		if (rowCount == 0)
			return new Point(0, 0);

		int widthHint = (wHint != SWT.DEFAULT) ? wHint / numColumns
				: SWT.DEFAULT;
		int[] rowSizeX = new int[rowCount];
		int[] rowSizeY = new int[rowCount];
		for (Entry<Control, Point> e : coordinates.entrySet()) {
			DashboardLayoutData layoutData = getLayoutData(e.getKey());
			Point s = e.getKey().computeSize(
					(widthHint != SWT.DEFAULT) ? widthHint
							* layoutData.horizontalSpan : SWT.DEFAULT,
					SWT.DEFAULT, flushCache);
			rowSizeX[e.getValue().y] += s.x;
			if (rowSizeY[e.getValue().y] < s.y / layoutData.verticalSpan)
				rowSizeY[e.getValue().y] = s.y / layoutData.verticalSpan;
		}

		Point size = new Point(0, 0);
		for (int i = 0; i < rowCount; i++) {
			if (size.x < rowSizeX[i])
				size.x = rowSizeX[i];
			if (size.y < rowSizeX[i])
				size.y = rowSizeX[i];
		}
		cachedSize = new Point(size.x, size.y);
		return size;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.swt.widgets.Layout#layout(org.eclipse.swt.widgets.Composite,
	 * boolean)
	 */
	@Override
	protected void layout(Composite composite, boolean flushCache) {
		createGrid(composite);
		if (rowCount == 0)
			return;

		Point size = composite.getSize();
		int height = size.y - marginHeight * 2;
		int columnWidth = (size.x - marginWidth * 2 - horizontalSpacing
				* (numColumns - 1))
				/ numColumns;

		// Calculate size for each row
		float[] rowSize = new float[rowCount];
		Arrays.fill(rowSize, 0);
		boolean[] rowFill = new boolean[rowCount];
		Arrays.fill(rowFill, false);

		for (Entry<Control, Point> e : coordinates.entrySet()) {
			DashboardLayoutData layoutData = getLayoutData(e.getKey());
			if (layoutData.fill) {
				for (int i = 0; i < layoutData.verticalSpan; i++)
					rowFill[e.getValue().y + i] = true;
			} else {
				int ch = (layoutData.heightHint > 0) ? layoutData.heightHint
						: e.getKey().computeSize(columnWidth, SWT.DEFAULT,
								flushCache).y;
				int rs = (ch - (layoutData.verticalSpan - 1) * verticalSpacing)
						/ layoutData.verticalSpan;
				for (int i = 0; i < layoutData.verticalSpan; i++) {
					if (rowSize[e.getValue().y + i] < rs)
						rowSize[e.getValue().y + i] = rs;
				}
			}
		}

		float usedSpace = 0;
		int fillRows = 0;
		for (int i = 0; i < rowCount; i++) {
			if (rowFill[i])
				fillRows++;
			usedSpace += rowSize[i];
		}

		// If children requires too much space, reduce biggest
		if (usedSpace > height - verticalSpacing * (rowCount - 1)) {
			float extraSpace = usedSpace - height;
			float maxRowSize = (float) (height - verticalSpacing
					* (rowCount - 1))
					/ (float) rowCount;
			for (int i = 0; i < rowCount; i++) {
				if (rowSize[i] > maxRowSize) {
					float delta = Math.min(rowSize[i] - maxRowSize, extraSpace);
					rowSize[i] -= delta;
					extraSpace -= delta;
					usedSpace -= delta;
				}
			}
		}

		// distribute space evenly between fill rows
		if (fillRows > 0) {
			float extraHeight = (float) (height - usedSpace - verticalSpacing
					* (rowCount - 1))
					/ (float) fillRows;
			for (int i = 0; i < rowCount; i++) {
				if (rowFill[i])
					rowSize[i] += extraHeight;
			}
		}

		// calculate row starts
		rowStart = new int[rowCount];
		rowStart[0] = marginHeight;
		for (int i = 1; i < rowCount; i++)
			rowStart[i] = Math.round(rowStart[i - 1] + rowSize[i - 1])
					+ verticalSpacing;

		for (Control c : composite.getChildren()) {
			if (!c.isVisible())
				continue;

			Point p = coordinates.get(c);
			if (p == null)
				continue;

			DashboardLayoutData layoutData = getLayoutData(c);
			int cw = layoutData.horizontalSpan * columnWidth
					+ (layoutData.horizontalSpan - 1) * horizontalSpacing;
			float ch = rowSize[p.y];
			for (int j = 1; j < layoutData.verticalSpan; j++)
				ch += rowSize[p.y + j] + verticalSpacing;
			c.setSize(cw, Math.round(ch));
			c.setLocation(
					p.x * (columnWidth + horizontalSpacing) + marginWidth,
					rowStart[p.y]);
		}
	}

	/**
	 * Get layout data for control
	 * 
	 * @param c
	 *            control
	 * @return layout data for given control
	 */
	private static DashboardLayoutData getLayoutData(Control c) {
		Object data = c.getLayoutData();
		return ((data != null) && (data instanceof DashboardLayoutData)) ? (DashboardLayoutData) data
				: DashboardLayoutData.DEFAULT;
	}
}
