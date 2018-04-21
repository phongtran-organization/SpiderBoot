/**
 * NetXMS - open source network management system
 * Copyright (C) 2003 - 2016 Raden Solutions
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
package org.netxms.ui.eclipse.snmp.views.helpers;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.netxms.client.snmp.MibObject;
import org.netxms.client.snmp.SnmpValue;
import org.netxms.ui.eclipse.snmp.shared.MibCache;

/**
 * Filter for MIB Explorer
 */
public class SnmpWalkFilter extends ViewerFilter {
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

		final SnmpValue vl = (SnmpValue) element;
		if (containsValue(vl))
			return true;
		else if (containsOid(vl))
			return true;
		else if (containsOidText(vl))
			return true;
		return false;
	}

	/**
	 * Checks if contains SNMP walk value
	 */
	public boolean containsValue(SnmpValue vl) {
		if (vl.getValue().toLowerCase().contains(filterString.toLowerCase()))
			return true;
		return false;
	}

	/**
	 * Checks if contains SNMP walk OID
	 */
	public boolean containsOid(SnmpValue vl) {
		if (vl.getObjectId().toString().toLowerCase()
				.contains(filterString.toLowerCase()))
			return true;
		return false;
	}

	/**
	 * Checks if contains SNMP walk OID as text
	 */
	public boolean containsOidText(SnmpValue vl) {
		MibObject object = MibCache.findObject(vl.getName(), false);
		if (object == null)
			return false;
		else if (object.getFullName().toLowerCase()
				.contains(filterString.toLowerCase()))
			return true;

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
