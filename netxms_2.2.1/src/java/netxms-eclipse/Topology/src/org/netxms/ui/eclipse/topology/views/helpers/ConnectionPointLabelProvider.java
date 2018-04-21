/**
 * NetXMS - open source network management system
 * Copyright (C) 2003-2015 Victor Kirhenshtein
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
package org.netxms.ui.eclipse.topology.views.helpers;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.netxms.client.NXCSession;
import org.netxms.client.constants.ConnectionPointType;
import org.netxms.client.objects.Interface;
import org.netxms.client.topology.ConnectionPoint;
import org.netxms.ui.eclipse.shared.ConsoleSharedData;
import org.netxms.ui.eclipse.tools.ColorConverter;
import org.netxms.ui.eclipse.topology.Messages;
import org.netxms.ui.eclipse.topology.views.HostSearchResults;

/**
 * Label provider for connection point objects
 * 
 */
public class ConnectionPointLabelProvider extends LabelProvider implements
		ITableLabelProvider, IColorProvider {
	private static final Color COLOR_FOUND_OBJECT_DIRECT = new Color(
			Display.getDefault(),
			ColorConverter.parseColorDefinition("DarkGreen"));
	private static final Color COLOR_FOUND_OBJECT_INDIRECT = new Color(
			Display.getDefault(),
			ColorConverter.parseColorDefinition("SeaGreen"));
	private static final Color COLOR_FOUND_OBJECT_WIRELESS = new Color(
			Display.getDefault(), ColorConverter.parseColorDefinition("Teal"));
	private static final Color COLOR_FOUND_OBJECT_UNKNOWN = new Color(
			Display.getDefault(), ColorConverter.parseColorDefinition("Peru"));
	private static final Color COLOR_FOUND_MAC_DIRECT = new Color(
			Display.getDefault(),
			ColorConverter.parseColorDefinition("DarkBlue"));
	private static final Color COLOR_FOUND_MAC_INDIRECT = new Color(
			Display.getDefault(),
			ColorConverter.parseColorDefinition("DarkSlateBlue"));
	private static final Color COLOR_FOUND_MAC_WIRELESS = new Color(
			Display.getDefault(),
			ColorConverter.parseColorDefinition("SteelBlue"));
	private static final Color COLOR_NOT_FOUND = new Color(
			Display.getDefault(),
			ColorConverter.parseColorDefinition("DarkRed"));

	private Map<Long, String> cachedObjectNames = new HashMap<Long, String>();
	private NXCSession session = (NXCSession) ConsoleSharedData.getSession();

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(java.lang
	 * .Object, int)
	 */
	@Override
	public Image getColumnImage(Object element, int columnIndex) {
		return null;
	}

	/**
	 * Get (cached) object name by object ID
	 * 
	 * @param id
	 *            object id
	 * @return object name
	 */
	private String getObjectName(long id) {
		if (id == 0)
			return ""; //$NON-NLS-1$

		String name = cachedObjectNames.get(id);
		if (name == null) {
			name = session.getObjectName(id);
			cachedObjectNames.put(id, name);
		}
		return name;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang
	 * .Object, int)
	 */
	@Override
	public String getColumnText(Object element, int columnIndex) {
		ConnectionPoint cp = (ConnectionPoint) element;
		switch (columnIndex) {
		case HostSearchResults.COLUMN_SEQUENCE:
			return Integer.toString((Integer) cp.getData() + 1);
		case HostSearchResults.COLUMN_NODE:
			return getObjectName(cp.getLocalNodeId());
		case HostSearchResults.COLUMN_INTERFACE:
			return getObjectName(cp.getLocalInterfaceId());
		case HostSearchResults.COLUMN_MAC_ADDRESS:
			if (cp.getLocalMacAddress() == null)
				return "n/a";
			else
				return cp.getLocalMacAddress().toString();
		case HostSearchResults.COLUMN_IP_ADDRESS:
			InetAddress addr = cp.getLocalIpAddress();
			if (addr != null)
				return addr.getHostAddress();
			Interface iface = (Interface) session.findObjectById(
					cp.getLocalInterfaceId(), Interface.class);
			if (iface == null)
				return ""; //$NON-NLS-1$
			InetAddress a = iface.getFirstUnicastAddress();
			return (a != null) ? a.getHostAddress() : ""; //$NON-NLS-1$
		case HostSearchResults.COLUMN_SWITCH:
			if ((cp.getNodeId() == 0))
				return "n/a";
			else
				return getObjectName(cp.getNodeId());
		case HostSearchResults.COLUMN_PORT:
			if (cp.getInterfaceId() == 0)
				return "n/a";
			else
				return getObjectName(cp.getInterfaceId());
		case HostSearchResults.COLUMN_TYPE:
			if (cp.getType() == null)
				return "n/a";
			switch (cp.getType()) {
			case DIRECT:
				return Messages.get().ConnectionPointLabelProvider_Direct;
			case INDIRECT:
				return Messages.get().ConnectionPointLabelProvider_Indirect;
			case WIRELESS:
				return Messages.get().ConnectionPointLabelProvider_Wireless;
			default:
				return Messages.get().ConnectionPointLabelProvider_Unknown;
			}
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.viewers.IColorProvider#getForeground(java.lang.Object)
	 */
	@Override
	public Color getForeground(Object element) {
		ConnectionPoint cp = (ConnectionPoint) element;
		if (!cp.hasConnection())
			return COLOR_NOT_FOUND;

		if (cp.getLocalNodeId() == 0)
			return (cp.getType() == ConnectionPointType.DIRECT) ? COLOR_FOUND_MAC_DIRECT
					: ((cp.getType() == ConnectionPointType.WIRELESS) ? COLOR_FOUND_MAC_WIRELESS
							: COLOR_FOUND_MAC_INDIRECT);

		switch (cp.getType()) {
		case DIRECT:
			return COLOR_FOUND_OBJECT_DIRECT;
		case INDIRECT:
			return COLOR_FOUND_OBJECT_INDIRECT;
		case WIRELESS:
			return COLOR_FOUND_OBJECT_WIRELESS;
		case UNKNOWN:
			return COLOR_FOUND_OBJECT_UNKNOWN;
		}

		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.viewers.IColorProvider#getBackground(java.lang.Object)
	 */
	@Override
	public Color getBackground(Object element) {
		return null;
	}
}
