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
package org.netxms.client.objects;

import java.util.Set;
import org.netxms.base.InetAddressEx;
import org.netxms.base.MacAddress;
import org.netxms.base.NXCPCodes;
import org.netxms.base.NXCPMessage;
import org.netxms.client.NXCSession;
import org.netxms.client.constants.AccessPointState;
import org.netxms.client.topology.RadioInterface;

/**
 * Access point class
 */
public class AccessPoint extends DataCollectionTarget {
	private long nodeId;
	private int index;
	private MacAddress macAddress;
	private InetAddressEx ipAddress;
	private AccessPointState state;
	private String vendor;
	private String model;
	private String serialNumber;
	private RadioInterface[] radios;

	/**
	 * @param msg
	 * @param session
	 */
	public AccessPoint(NXCPMessage msg, NXCSession session) {
		super(msg, session);
		nodeId = msg.getFieldAsInt64(NXCPCodes.VID_NODE_ID);
		index = msg.getFieldAsInt32(NXCPCodes.VID_AP_INDEX);
		macAddress = new MacAddress(
				msg.getFieldAsBinary(NXCPCodes.VID_MAC_ADDR));
		ipAddress = msg.getFieldAsInetAddressEx(NXCPCodes.VID_IP_ADDRESS);
		state = AccessPointState.getByValue(msg
				.getFieldAsInt32(NXCPCodes.VID_STATE));
		vendor = msg.getFieldAsString(NXCPCodes.VID_VENDOR);
		model = msg.getFieldAsString(NXCPCodes.VID_MODEL);
		serialNumber = msg.getFieldAsString(NXCPCodes.VID_SERIAL_NUMBER);

		int count = msg.getFieldAsInt32(NXCPCodes.VID_RADIO_COUNT);
		radios = new RadioInterface[count];
		long fieldId = NXCPCodes.VID_RADIO_LIST_BASE;
		for (int i = 0; i < count; i++) {
			radios[i] = new RadioInterface(this, msg, fieldId);
			fieldId += 10;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.netxms.client.objects.AbstractObject#getObjectClassName()
	 */
	@Override
	public String getObjectClassName() {
		return "AccessPoint";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.netxms.client.objects.AbstractObject#isAllowedOnMap()
	 */
	@Override
	public boolean isAllowedOnMap() {
		return true;
	}

	/**
	 * @return the nodeId
	 */
	public long getNodeId() {
		return nodeId;
	}

	/**
	 * @return the index
	 */
	public int getIndex() {
		return index;
	}

	/**
	 * @return the macAddress
	 */
	public MacAddress getMacAddress() {
		return macAddress;
	}

	/**
	 * @return the ipAddress
	 */
	public InetAddressEx getIpAddress() {
		return ipAddress;
	}

	/**
	 * @return the vendor
	 */
	public String getVendor() {
		return vendor;
	}

	/**
	 * @return the serialNumber
	 */
	public String getSerialNumber() {
		return serialNumber;
	}

	/**
	 * @return the model
	 */
	public String getModel() {
		return model;
	}

	/**
	 * @return the radios
	 */
	public RadioInterface[] getRadios() {
		return radios;
	}

	/**
	 * @return the state
	 */
	public AccessPointState getState() {
		return state;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.netxms.client.objects.AbstractObject#getStrings()
	 */
	@Override
	public Set<String> getStrings() {
		Set<String> strings = super.getStrings();
		addString(strings, model);
		addString(strings, serialNumber);
		addString(strings, vendor);
		return strings;
	}
}
