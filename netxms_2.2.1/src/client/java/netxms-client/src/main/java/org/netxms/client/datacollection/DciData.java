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
package org.netxms.client.datacollection;

import java.util.ArrayList;

/**
 * Class to hold series of collected DCI data
 */
public class DciData {
	private long nodeId;
	private long dciId;
	private int dataType;
	private ArrayList<DciDataRow> values = new ArrayList<DciDataRow>();

	/**
	 * @param nodeId
	 *            The node ID
	 * @param dciId
	 *            The dci ID
	 */
	public DciData(long nodeId, long dciId) {
		this.nodeId = nodeId;
		this.dciId = dciId;
	}

	/**
	 * @return the nodeId
	 */
	public long getNodeId() {
		return nodeId;
	}

	/**
	 * @return the dciId
	 */
	public long getDciId() {
		return dciId;
	}

	/**
	 * @return the values
	 */
	public DciDataRow[] getValues() {
		return values.toArray(new DciDataRow[values.size()]);
	}

	/**
	 * Get last added value
	 * 
	 * @return last added value
	 */
	public DciDataRow getLastValue() {
		return (values.size() > 0) ? values.get(values.size() - 1) : null;
	}

	/**
	 * Add new value
	 * 
	 * @param row
	 *            DciDataRow
	 */
	public void addDataRow(DciDataRow row) {
		values.add(row);
	}

	/**
	 * @return the dataType
	 */
	public int getDataType() {
		return dataType;
	}

	/**
	 * @param dataType
	 *            the dataType to set
	 */
	public void setDataType(int dataType) {
		this.dataType = dataType;
	}

	/**
	 * Invert values
	 */
	public void invert() {
		for (DciDataRow r : values)
			r.invert();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "DciData [nodeId=" + nodeId + ", dciId=" + dciId + ", dataType="
				+ dataType + ", valuesSize=" + values.size() + "]";
	}
}
