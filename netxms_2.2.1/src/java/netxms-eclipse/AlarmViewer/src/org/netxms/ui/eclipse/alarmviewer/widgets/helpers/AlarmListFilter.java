/**
 * NetXMS - open source network management system
 * Copyright (C) 2003-2014 Victor Kirhenshtein
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
package org.netxms.ui.eclipse.alarmviewer.widgets.helpers;

import java.util.ArrayList;
import java.util.List;
import org.netxms.client.NXCSession;
import org.netxms.client.events.Alarm;
import org.netxms.client.objects.AbstractObject;
import org.netxms.ui.eclipse.shared.ConsoleSharedData;

/**
 * Filter for alarm list
 */
public class AlarmListFilter {
	private List<Long> rootObjects = new ArrayList<Long>();
	private int stateFilter = -1;
	private int severityFilter = 0xFF;
	private NXCSession session;

	/**
	 * 
	 */
	public AlarmListFilter() {
		session = (NXCSession) ConsoleSharedData.getSession();
	}

	/**
	 * @return true if alarm should be displayed
	 */
	public boolean select(Alarm alarm) {
		if ((stateFilter != -1) && (alarm.getState() != stateFilter))
			return false;

		if (((1 << alarm.getCurrentSeverity().getValue()) & severityFilter) == 0)
			return false;

		if ((rootObjects.size() == 0)
				|| (rootObjects.contains(((Alarm) alarm).getSourceObjectId())))
			return true; // No filtering by object ID or root object is a source

		AbstractObject object = session.findObjectById(alarm
				.getSourceObjectId());
		if (object != null) {
			// convert List of Longs to array of longs
			long[] rootObjectsArray = new long[rootObjects.size()];
			int i = 0;
			for (long objectId : rootObjects) {
				rootObjectsArray[i++] = objectId;
			}
			return object.isChildOf(rootObjectsArray);
		}
		return false;
	}

	/**
	 * @param rootObject
	 *            the rootObject to set
	 */
	public final void setRootObject(long rootObject) {
		this.rootObjects.clear();
		this.rootObjects.add(rootObject);
	}

	/**
	 * @param selectedObjects
	 */
	public void setRootObjects(List<Long> selectedObjects) {
		this.rootObjects.clear();
		this.rootObjects.addAll(selectedObjects);
	}

	/**
	 * @param stateFilter
	 *            the stateFilter to set
	 */
	public void setStateFilter(int stateFilter) {
		this.stateFilter = stateFilter;
	}

	/**
	 * @param severityFilter
	 *            the severityFilter to set
	 */
	public void setSeverityFilter(int severityFilter) {
		this.severityFilter = severityFilter;
	}
}
