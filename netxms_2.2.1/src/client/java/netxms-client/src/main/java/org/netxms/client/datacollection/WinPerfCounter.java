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

/**
 * Windows performance counter
 */
public class WinPerfCounter {
	private WinPerfObject object;
	private String name;

	/**
	 * @param object
	 * @param name
	 */
	protected WinPerfCounter(WinPerfObject object, String name) {
		super();
		this.object = object;
		this.name = name;
	}

	/**
	 * @return the object
	 */
	public WinPerfObject getObject() {
		return object;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
}
