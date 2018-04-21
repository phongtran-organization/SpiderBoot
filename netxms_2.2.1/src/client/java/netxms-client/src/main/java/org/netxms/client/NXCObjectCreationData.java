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
package org.netxms.client;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import org.netxms.base.InetAddressEx;
import org.netxms.base.MacAddress;
import org.netxms.client.objects.AbstractObject;
import org.netxms.client.objects.NetworkService;

/**
 * NetXMS object creation data
 */
public class NXCObjectCreationData {
	// Creation flags
	public static int CF_DISABLE_ICMP = 0x0001;
	public static int CF_DISABLE_NXCP = 0x0002;
	public static int CF_DISABLE_SNMP = 0x0004;
	public static int CF_CREATE_UNMANAGED = 0x0008;

	private int objectClass;
	private String name;
	private long parentId;
	private String comments;
	private int creationFlags;
	private String primaryName;
	private int agentPort;
	private int snmpPort;
	private InetAddressEx ipAddress;
	private long agentProxyId;
	private long snmpProxyId;
	private long icmpProxyId;
	private long sshProxyId;
	private int mapType;
	private List<Long> seedObjectIds;
	private long zoneUIN;
	private int serviceType;
	private int ipProtocol;
	private int ipPort;
	private String request;
	private String response;
	private long linkedNodeId;
	private boolean template;
	private MacAddress macAddress;
	private int ifIndex;
	private int ifType;
	private int slot;
	private int port;
	private boolean physicalPort;
	private boolean createStatusDci;
	private String deviceId;
	private int height;
	private int flags;
	private long controllerId;
	private long chassisId;
	private String sshLogin;
	private String sshPassword;

	/**
	 * Constructor.
	 * 
	 * @param objectClass
	 *            Class of new object (one of NXCObject.OBJECT_xxx constants)
	 * @see AbstractObject
	 * @param name
	 *            Name of new object
	 * @param parentId
	 *            Parent object ID
	 */
	public NXCObjectCreationData(final int objectClass, final String name,
			final long parentId) {
		this.objectClass = objectClass;
		this.name = name;
		this.parentId = parentId;

		try {
			ipAddress = new InetAddressEx(InetAddress.getByName("127.0.0.1"), 8);
		} catch (UnknownHostException e) {
		}

		primaryName = null;
		agentPort = 0;
		snmpPort = 0;
		comments = null;
		creationFlags = 0;
		agentProxyId = 0;
		snmpProxyId = 0;
		icmpProxyId = 0;
		sshProxyId = 0;
		mapType = 0;
		seedObjectIds = new ArrayList<Long>();
		zoneUIN = 0;
		serviceType = NetworkService.CUSTOM;
		ipProtocol = 6;
		ipPort = 80;
		request = "";
		response = "";
		linkedNodeId = 0;
		template = false;
		macAddress = new MacAddress();
		ifIndex = 0;
		ifType = 1;
		slot = 0;
		port = 0;
		physicalPort = false;
		createStatusDci = false;
		sshLogin = "";
		sshPassword = "";
	}

	/**
	 * @return the objectClass
	 */
	public int getObjectClass() {
		return objectClass;
	}

	/**
	 * @param objectClass
	 *            the objectClass to set
	 */
	public void setObjectClass(int objectClass) {
		this.objectClass = objectClass;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the parentId
	 */
	public long getParentId() {
		return parentId;
	}

	/**
	 * @param parentId
	 *            the parentId to set
	 */
	public void setParentId(long parentId) {
		this.parentId = parentId;
	}

	/**
	 * @return the comments
	 */
	public String getComments() {
		return comments;
	}

	/**
	 * @param comments
	 *            the comments to set
	 */
	public void setComments(String comments) {
		this.comments = comments;
	}

	/**
	 * @return the creationFlags
	 */
	public int getCreationFlags() {
		return creationFlags;
	}

	/**
	 * @param creationFlags
	 *            Node creation flags (combination of
	 *            NXCObjectCreationData.CF_xxx constants)
	 */
	public void setCreationFlags(int creationFlags) {
		this.creationFlags = creationFlags;
	}

	/**
	 * @return the ipAddress
	 */
	public InetAddressEx getIpAddress() {
		return ipAddress;
	}

	/**
	 * @param ipAddress
	 *            the ipAddress to set
	 */
	public void setIpAddress(InetAddressEx ipAddress) {
		this.ipAddress = ipAddress;
	}

	/**
	 * @return the agentProxyId
	 */
	public long getAgentProxyId() {
		return agentProxyId;
	}

	/**
	 * @param agentProxyId
	 *            the agentProxyId to set
	 */
	public void setAgentProxyId(long agentProxyId) {
		this.agentProxyId = agentProxyId;
	}

	/**
	 * @return the snmpProxyId
	 */
	public long getSnmpProxyId() {
		return snmpProxyId;
	}

	/**
	 * @param snmpProxyId
	 *            the snmpProxyId to set
	 */
	public void setSnmpProxyId(long snmpProxyId) {
		this.snmpProxyId = snmpProxyId;
	}

	/**
	 * @return the icmpProxyId
	 */
	public long getIcmpProxyId() {
		return icmpProxyId;
	}

	/**
	 * @param icmpProxyId
	 *            the icmpProxyId to set
	 */
	public void setIcmpProxyId(long icmpProxyId) {
		this.icmpProxyId = icmpProxyId;
	}

	/**
	 * @return the sshProxyId
	 */
	public long getSshProxyId() {
		return sshProxyId;
	}

	/**
	 * @param sshProxyId
	 *            the sshProxyId to set
	 */
	public void setSshProxyId(long sshProxyId) {
		this.sshProxyId = sshProxyId;
	}

	/**
	 * @return the mapType
	 */
	public int getMapType() {
		return mapType;
	}

	/**
	 * @param mapType
	 *            the mapType to set
	 */
	public void setMapType(int mapType) {
		this.mapType = mapType;
	}

	/**
	 * @return the seedObjectIds
	 */
	public Long[] getSeedObjectIds() {
		return seedObjectIds.toArray(new Long[seedObjectIds.size()]);
	}

	/**
	 * @param seedObjectId
	 *            the seedObjectId to set
	 */
	public void setSeedObjectId(long seedObjectId) {
		seedObjectIds.clear();
		seedObjectIds.add(seedObjectId);
	}

	/**
	 * @param seedObjectIds
	 *            the seed node object Ids to set
	 */
	public void setSeedObjectIds(List<Long> seedObjectIds) {
		this.seedObjectIds = seedObjectIds;
	}

	/**
	 * @return the zoneId
	 */
	public long getZoneUIN() {
		return zoneUIN;
	}

	/**
	 * @param zoneUIN
	 *            the zoneId to set
	 */
	public void setZoneUIN(long zoneUIN) {
		this.zoneUIN = zoneUIN;
	}

	/**
	 * @return the serviceType
	 */
	public int getServiceType() {
		return serviceType;
	}

	/**
	 * @param serviceType
	 *            the serviceType to set
	 */
	public void setServiceType(int serviceType) {
		this.serviceType = serviceType;
	}

	/**
	 * @return the ipProtocol
	 */
	public int getIpProtocol() {
		return ipProtocol;
	}

	/**
	 * @param ipProtocol
	 *            the ipProtocol to set
	 */
	public void setIpProtocol(int ipProtocol) {
		this.ipProtocol = ipProtocol;
	}

	/**
	 * @return the ipPort
	 */
	public int getIpPort() {
		return ipPort;
	}

	/**
	 * @param ipPort
	 *            the ipPort to set
	 */
	public void setIpPort(int ipPort) {
		this.ipPort = ipPort;
	}

	/**
	 * @return the request
	 */
	public String getRequest() {
		return request;
	}

	/**
	 * @param request
	 *            the request to set
	 */
	public void setRequest(String request) {
		this.request = request;
	}

	/**
	 * @return the response
	 */
	public String getResponse() {
		return response;
	}

	/**
	 * @param response
	 *            the response to set
	 */
	public void setResponse(String response) {
		this.response = response;
	}

	/**
	 * @return the linkedNodeId
	 */
	public long getLinkedNodeId() {
		return linkedNodeId;
	}

	/**
	 * @param linkedNodeId
	 *            the linkedNodeId to set
	 */
	public void setLinkedNodeId(long linkedNodeId) {
		this.linkedNodeId = linkedNodeId;
	}

	/**
	 * @return the primaryName
	 */
	public String getPrimaryName() {
		return primaryName;
	}

	/**
	 * @param primaryName
	 *            the primaryName to set
	 */
	public void setPrimaryName(String primaryName) {
		this.primaryName = primaryName;
	}

	/**
	 * @return the template
	 */
	public boolean isTemplate() {
		return template;
	}

	/**
	 * @param template
	 *            the template to set
	 */
	public void setTemplate(boolean template) {
		this.template = template;
	}

	/**
	 * @return the macAddress
	 */
	public MacAddress getMacAddress() {
		return macAddress;
	}

	/**
	 * @param macAddress
	 *            the macAddress to set
	 */
	public void setMacAddress(MacAddress macAddress) {
		this.macAddress = macAddress;
	}

	/**
	 * @return the ifIndex
	 */
	public int getIfIndex() {
		return ifIndex;
	}

	/**
	 * @param ifIndex
	 *            the ifIndex to set
	 */
	public void setIfIndex(int ifIndex) {
		this.ifIndex = ifIndex;
	}

	/**
	 * @return the ifType
	 */
	public int getIfType() {
		return ifType;
	}

	/**
	 * @param ifType
	 *            the ifType to set
	 */
	public void setIfType(int ifType) {
		this.ifType = ifType;
	}

	/**
	 * @return the slot
	 */
	public int getSlot() {
		return slot;
	}

	/**
	 * @param slot
	 *            the slot to set
	 */
	public void setSlot(int slot) {
		this.slot = slot;
	}

	/**
	 * @return the port
	 */
	public int getPort() {
		return port;
	}

	/**
	 * @param port
	 *            the port to set
	 */
	public void setPort(int port) {
		this.port = port;
	}

	/**
	 * @return the physicalPort
	 */
	public boolean isPhysicalPort() {
		return physicalPort;
	}

	/**
	 * @param physicalPort
	 *            the physicalPort to set
	 */
	public void setPhysicalPort(boolean physicalPort) {
		this.physicalPort = physicalPort;
	}

	/**
	 * @return the createStatusDci
	 */
	public boolean isCreateStatusDci() {
		return createStatusDci;
	}

	/**
	 * @param createStatusDci
	 *            the createStatusDci to set
	 */
	public void setCreateStatusDci(boolean createStatusDci) {
		this.createStatusDci = createStatusDci;
	}

	/**
	 * @return the agentPort
	 */
	public int getAgentPort() {
		return agentPort;
	}

	/**
	 * @param agentPort
	 *            the agentPort to set
	 */
	public void setAgentPort(int agentPort) {
		this.agentPort = agentPort;
	}

	/**
	 * @return the snmpPort
	 */
	public int getSnmpPort() {
		return snmpPort;
	}

	/**
	 * @param snmpPort
	 *            the snmpPort to set
	 */
	public void setSnmpPort(int snmpPort) {
		this.snmpPort = snmpPort;
	}

	/**
	 * @return the deviceId
	 */
	public final String getDeviceId() {
		return deviceId;
	}

	/**
	 * @param deviceId
	 *            the deviceId to set
	 */
	public final void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}

	/**
	 * @return the height
	 */
	public int getHeight() {
		return height;
	}

	/**
	 * @param height
	 *            the height to set
	 */
	public void setHeight(int height) {
		this.height = height;
	}

	/**
	 * @return the flags
	 */
	public int getFlags() {
		return flags;
	}

	/**
	 * @param flags
	 *            the flags to set
	 */
	public void setFlags(int flags) {
		this.flags = flags;
	}

	/**
	 * @return the controllerId
	 */
	public long getControllerId() {
		return controllerId;
	}

	/**
	 * @param controllerId
	 *            the controllerId to set
	 */
	public void setControllerId(long controllerId) {
		this.controllerId = controllerId;
	}

	/**
	 * @return the chassisId
	 */
	public long getChassisId() {
		return chassisId;
	}

	/**
	 * @param chassisId
	 *            the chassisId to set
	 */
	public void setChassisId(long chassisId) {
		this.chassisId = chassisId;
	}

	/**
	 * @return the sshLogin
	 */
	public String getSshLogin() {
		return sshLogin;
	}

	/**
	 * @param sshLogin
	 *            the sshLogin to set
	 */
	public void setSshLogin(String sshLogin) {
		this.sshLogin = sshLogin;
	}

	/**
	 * @return the sshPassword
	 */
	public String getSshPassword() {
		return sshPassword;
	}

	/**
	 * @param sshPassword
	 *            the sshPassword to set
	 */
	public void setSshPassword(String sshPassword) {
		this.sshPassword = sshPassword;
	}
}
