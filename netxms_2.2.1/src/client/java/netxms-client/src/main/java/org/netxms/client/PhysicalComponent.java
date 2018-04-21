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
package org.netxms.client;

import java.util.ArrayList;
import java.util.List;
import org.netxms.base.NXCPMessage;

/**
 * Represents node's physical component
 */
public class PhysicalComponent {
	public static final int OTHER = 1;
	public static final int UNKNOWN = 2;
	public static final int CHASSIS = 3;
	public static final int BACKPLANE = 4;
	public static final int CONTAINER = 5;
	public static final int PSU = 6;
	public static final int FAN = 7;
	public static final int SENSOR = 8;
	public static final int MODULE = 9;
	public static final int PORT = 10;
	public static final int STACK = 11;

	private int index;
	private int parentIndex;
	private int ifIndex;
	private int phyClass;
	private String name;
	private String description;
	private String model;
	private String serialNumber;
	private String firmware;
	private String vendor;
	private PhysicalComponent parent;
	private List<PhysicalComponent> subcomponents;
	private long nextVarId;

	/**
	 * Create object from NXCP message
	 * 
	 * @param msg
	 *            The NXCPMessage
	 * @param baseId
	 *            The base ID
	 * @param parent
	 *            The PhysycalComponent parent
	 */
	protected PhysicalComponent(NXCPMessage msg, long baseId,
			PhysicalComponent parent) {
		this.parent = parent;

		index = msg.getFieldAsInt32(baseId);
		parentIndex = msg.getFieldAsInt32(baseId + 1);
		phyClass = msg.getFieldAsInt32(baseId + 2);
		ifIndex = msg.getFieldAsInt32(baseId + 3);
		name = msg.getFieldAsString(baseId + 4);
		description = msg.getFieldAsString(baseId + 5);
		model = msg.getFieldAsString(baseId + 6);
		serialNumber = msg.getFieldAsString(baseId + 7);
		vendor = msg.getFieldAsString(baseId + 8);
		firmware = msg.getFieldAsString(baseId + 9);

		int count = msg.getFieldAsInt32(baseId + 10);
		subcomponents = new ArrayList<PhysicalComponent>(count);
		long varId = baseId + 11;
		for (int i = 0; i < count; i++) {
			final PhysicalComponent c = new PhysicalComponent(msg, varId, this);
			varId = c.nextVarId;
			subcomponents.add(c);
		}
		nextVarId = varId;
	}

	/**
	 * @return the index
	 */
	public int getIndex() {
		return index;
	}

	/**
	 * @return the parentIndex
	 */
	public int getParentIndex() {
		return parentIndex;
	}

	/**
	 * @return the ifIndex
	 */
	public int getIfIndex() {
		return ifIndex;
	}

	/**
	 * @return the phyClass
	 */
	public int getPhyClass() {
		return phyClass;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @return the model
	 */
	public String getModel() {
		return model;
	}

	/**
	 * @return the serialNumber
	 */
	public String getSerialNumber() {
		return serialNumber;
	}

	/**
	 * @return the firmware
	 */
	public String getFirmware() {
		return firmware;
	}

	/**
	 * @return the vendor
	 */
	public String getVendor() {
		return vendor;
	}

	/**
	 * @return the subcomponents
	 */
	public List<PhysicalComponent> getSubcomponents() {
		return subcomponents;
	}

	/**
	 * @return the parent
	 */
	public PhysicalComponent getParent() {
		return parent;
	}
}
