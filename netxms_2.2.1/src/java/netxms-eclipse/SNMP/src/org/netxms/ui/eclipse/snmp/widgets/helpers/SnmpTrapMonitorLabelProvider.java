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
package org.netxms.ui.eclipse.snmp.widgets.helpers;

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.netxms.client.NXCSession;
import org.netxms.client.objects.AbstractObject;
import org.netxms.client.snmp.SnmpTrapLogRecord;
import org.netxms.ui.eclipse.console.resources.RegionalSettings;
import org.netxms.ui.eclipse.shared.ConsoleSharedData;
import org.netxms.ui.eclipse.snmp.Messages;
import org.netxms.ui.eclipse.snmp.widgets.SnmpTrapTraceWidget;

/**
 * Label provider for SNMP trap monitor
 */
public class SnmpTrapMonitorLabelProvider extends LabelProvider implements
		ITableLabelProvider {
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang
	 * .Object, int)
	 */
	@Override
	public String getColumnText(Object element, int columnIndex) {
		SnmpTrapLogRecord record = (SnmpTrapLogRecord) element;
		switch (columnIndex) {
		case SnmpTrapTraceWidget.COLUMN_TIMESTAMP:
			return RegionalSettings.getDateTimeFormat().format(
					record.getTimestamp());
		case SnmpTrapTraceWidget.COLUMN_SOURCE_IP:
			return record.getSourceAddress().getHostAddress();
		case SnmpTrapTraceWidget.COLUMN_SOURCE_NODE:
			final AbstractObject object = session.findObjectById(record
					.getSourceNode());
			return (object != null) ? object.getObjectName()
					: Messages.get().SnmpTrapMonitorLabelProvider_Unknown;
		case SnmpTrapTraceWidget.COLUMN_OID:
			return record.getTrapObjectId();
		case SnmpTrapTraceWidget.COLUMN_VARBINDS:
			return record.getVarbinds();
		}
		return null;
	}
}
