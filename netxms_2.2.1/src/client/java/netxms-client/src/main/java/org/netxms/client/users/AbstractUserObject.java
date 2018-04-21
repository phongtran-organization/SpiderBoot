/**
 * NetXMS - open source network management system
 * Copyright (C) 2003-2009 Victor Kirhenshtein
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
package org.netxms.client.users;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;

import org.netxms.base.NXCPCodes;
import org.netxms.base.NXCPMessage;

/**
 * Abstract NetXMS user database object.
 * 
 */
public abstract class AbstractUserObject {
	// Object flags
	public static final int MODIFIED = 0x0001;
	public static final int DELETED = 0x0002;
	public static final int DISABLED = 0x0004;
	public static final int CHANGE_PASSWORD = 0x0008;
	public static final int CANNOT_CHANGE_PASSWORD = 0x0010;
	public static final int INTRUDER_LOCKOUT = 0x0020;
	public static final int PASSWORD_NEVER_EXPIRES = 0x0040;
	public static final int LDAP_USER = 0x0080;
	public static final int SYNC_EXCEPTION = 0x0100;
	public static final int CLOSE_OTHER_SESSIONS = 0x0200;

	// User object fields
	public static final int MODIFY_LOGIN_NAME = 0x00000001;
	public static final int MODIFY_DESCRIPTION = 0x00000002;
	public static final int MODIFY_FULL_NAME = 0x00000004;
	public static final int MODIFY_FLAGS = 0x00000008;
	public static final int MODIFY_ACCESS_RIGHTS = 0x00000010;
	public static final int MODIFY_MEMBERS = 0x00000020;
	public static final int MODIFY_CERT_MAPPING = 0x00000040;
	public static final int MODIFY_AUTH_METHOD = 0x00000080;
	public static final int MODIFY_PASSWD_LENGTH = 0x00000100;
	public static final int MODIFY_TEMP_DISABLE = 0x00000200;
	public static final int MODIFY_CUSTOM_ATTRIBUTES = 0x00000400;
	public static final int MODIFY_XMPP_ID = 0x00000800;
	public static final int MODIFY_GROUP_MEMBERSHIP = 0x00001000;

	protected long id;
	protected String name;
	protected UUID guid;
	protected long systemRights;
	protected int flags;
	protected String description;
	protected String ldapDn;
	protected String ldapId;
	protected Map<String, String> customAttributes = new HashMap<String, String>(
			0);

	/**
	 * Default constructor
	 * 
	 * @param name
	 *            object name
	 */
	public AbstractUserObject(final String name) {
		this.name = name;
		description = "";
		guid = UUID.randomUUID();
	}

	/**
	 * Copy constructor
	 * 
	 * @param src
	 *            source object
	 */
	public AbstractUserObject(final AbstractUserObject src) {
		this.id = src.id;
		this.name = new String(src.name);
		this.guid = UUID.fromString(src.guid.toString());
		this.systemRights = src.systemRights;
		this.flags = src.flags;
		this.description = src.description;
		this.ldapDn = src.ldapDn;
		this.ldapId = src.ldapId;
		this.customAttributes = new HashMap<String, String>(0);
		Iterator<Entry<String, String>> it = src.customAttributes.entrySet()
				.iterator();
		while (it.hasNext()) {
			Entry<String, String> e = it.next();
			this.customAttributes.put(new String(e.getKey()),
					new String(e.getValue()));
		}
	}

	/**
	 * Create object from NXCP message
	 * 
	 * @param msg
	 *            Message containing object's data
	 */
	public AbstractUserObject(final NXCPMessage msg) {
		id = msg.getFieldAsInt64(NXCPCodes.VID_USER_ID);
		name = msg.getFieldAsString(NXCPCodes.VID_USER_NAME);
		flags = msg.getFieldAsInt32(NXCPCodes.VID_USER_FLAGS);
		systemRights = msg.getFieldAsInt64(NXCPCodes.VID_USER_SYS_RIGHTS);
		description = msg.getFieldAsString(NXCPCodes.VID_USER_DESCRIPTION);
		guid = msg.getFieldAsUUID(NXCPCodes.VID_GUID);
		ldapDn = msg.getFieldAsString(NXCPCodes.VID_LDAP_DN);
		ldapId = msg.getFieldAsString(NXCPCodes.VID_LDAP_ID);

		int count = msg.getFieldAsInt32(NXCPCodes.VID_NUM_CUSTOM_ATTRIBUTES);
		long varId = NXCPCodes.VID_CUSTOM_ATTRIBUTES_BASE;
		for (int i = 0; i < count; i++) {
			String name = msg.getFieldAsString(varId++);
			String value = msg.getFieldAsString(varId++);
			customAttributes.put(name, value);
		}
	}

	/**
	 * Fill NXCP message with object data
	 * 
	 * @param msg
	 *            destination message
	 */
	public void fillMessage(final NXCPMessage msg) {
		msg.setFieldInt32(NXCPCodes.VID_USER_ID, (int) id);
		msg.setField(NXCPCodes.VID_USER_NAME, name);
		msg.setFieldInt16(NXCPCodes.VID_USER_FLAGS, flags);
		msg.setFieldInt64(NXCPCodes.VID_USER_SYS_RIGHTS, systemRights);
		msg.setField(NXCPCodes.VID_USER_DESCRIPTION, description);

		msg.setFieldInt32(NXCPCodes.VID_NUM_CUSTOM_ATTRIBUTES,
				customAttributes.size());
		long varId = NXCPCodes.VID_CUSTOM_ATTRIBUTES_BASE;
		Iterator<Entry<String, String>> it = customAttributes.entrySet()
				.iterator();
		while (it.hasNext()) {
			Entry<String, String> e = it.next();
			msg.setField(varId++, e.getKey());
			msg.setField(varId++, e.getValue());
		}
	}

	/**
	 * @return true if user is marked as deleted
	 */
	public boolean isDeleted() {
		return (flags & DELETED) == DELETED;
	}

	/**
	 * @return the id
	 */
	public long getId() {
		return id;
	}

	/**
	 * @param id
	 *            the id to set
	 */
	public void setId(long id) {
		this.id = id;
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
	 * @return the guid
	 */
	public UUID getGuid() {
		return guid;
	}

	/**
	 * @return the ldapDn
	 */
	public String getLdapDn() {
		return ldapDn;
	}

	/**
	 * @return the ldapId
	 */
	public String getLdapId() {
		return ldapId;
	}

	/**
	 * @return the systemRights
	 */
	public long getSystemRights() {
		return systemRights;
	}

	/**
	 * @param systemRights
	 *            the systemRights to set
	 */
	public void setSystemRights(long systemRights) {
		this.systemRights = systemRights;
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
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param description
	 *            the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * Get custom attribute
	 * 
	 * @param name
	 *            Name of the attribute
	 * @return Custom attribute value
	 */
	public String getCustomAttribute(final String name) {
		return customAttributes.get(name);
	}

	/**
	 * Set custom attribute's value
	 * 
	 * @param name
	 *            Name of the attribute
	 * @param value
	 *            New value for attribute
	 */
	public void setCustomAttribute(final String name, final String value) {
		customAttributes.put(name, value);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}

	/**
	 * Check if object is disabled
	 * 
	 * @return true if object is disabled
	 */
	public boolean isDisabled() {
		return ((flags & DISABLED) == DISABLED);
	}

	/**
	 * Check if password should be changed at next logon
	 * 
	 * @return true if password should be changed at next logon
	 */
	public boolean isPasswordChangeNeeded() {
		return ((flags & CHANGE_PASSWORD) == CHANGE_PASSWORD);
	}

	/**
	 * Check if password change is forbidden
	 * 
	 * @return true if password change is forbidden
	 */
	public boolean isPasswordChangeForbidden() {
		return ((flags & CANNOT_CHANGE_PASSWORD) == CANNOT_CHANGE_PASSWORD);
	}
}
