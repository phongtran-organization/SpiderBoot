/**
 * NetXMS - open source network management system
 * Copyright (C) 2003-2011 Victor Kirhenshtein
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

import java.util.Date;
import org.netxms.base.NXCPMessage;
import org.netxms.client.constants.Severity;

/**
 * Represents data collection item's threshold
 */
public class Threshold {
	public static final int F_LAST = 0;
	public static final int F_AVERAGE = 1;
	public static final int F_DEVIATION = 2;
	public static final int F_DIFF = 3;
	public static final int F_ERROR = 4;
	public static final int F_SUM = 5;
	public static final int F_SCRIPT = 6;

	public static final int OP_LE = 0;
	public static final int OP_LE_EQ = 1;
	public static final int OP_EQ = 2;
	public static final int OP_GT_EQ = 3;
	public static final int OP_GT = 4;
	public static final int OP_NE = 5;
	public static final int OP_LIKE = 6;
	public static final int OP_NOTLIKE = 7;

	private long id;
	private int fireEvent;
	private int rearmEvent;
	private int sampleCount;
	private int function;
	private int operation;
	private String script;
	private int repeatInterval;
	private String value;
	private boolean active;
	private Severity currentSeverity;
	private Date lastEventTimestamp;

	/**
	 * Create DCI threshold object from NXCP message
	 * 
	 * @param msg
	 *            NXCP message
	 * @param baseId
	 *            Base variable ID for this threshold in message
	 */
	public Threshold(final NXCPMessage msg, final long baseId) {
		long varId = baseId;

		id = msg.getFieldAsInt64(varId++);
		fireEvent = msg.getFieldAsInt32(varId++);
		rearmEvent = msg.getFieldAsInt32(varId++);
		function = msg.getFieldAsInt32(varId++);
		operation = msg.getFieldAsInt32(varId++);
		sampleCount = msg.getFieldAsInt32(varId++);
		script = msg.getFieldAsString(varId++);
		repeatInterval = msg.getFieldAsInt32(varId++);
		value = msg.getFieldAsString(varId++);
		active = msg.getFieldAsBoolean(varId++);
		currentSeverity = Severity.getByValue(msg.getFieldAsInt32(varId++));
		lastEventTimestamp = msg.getFieldAsDate(varId);
	}

	/**
	 * Create new threshold object
	 */
	public Threshold() {
		id = 0;
		fireEvent = 17;
		rearmEvent = 18;
		sampleCount = 1;
		script = null;
		function = F_LAST;
		operation = OP_LE;
		repeatInterval = -1;
		value = "0";
		active = false;
		currentSeverity = Severity.NORMAL;
		lastEventTimestamp = new Date(0);
	}

	/**
	 * Copy constructor
	 * 
	 * @param src
	 *            source object
	 */
	public Threshold(Threshold src) {
		id = src.id;
		fireEvent = src.fireEvent;
		rearmEvent = src.rearmEvent;
		sampleCount = src.sampleCount;
		script = src.script;
		function = src.function;
		operation = src.operation;
		repeatInterval = src.repeatInterval;
		value = src.value;
		active = src.active;
		currentSeverity = src.currentSeverity;
		lastEventTimestamp = src.lastEventTimestamp;
	}

	/**
	 * Fill NXCP message with threshold's data. Operational data maintained by
	 * server will not be put into message.
	 * 
	 * @param msg
	 *            NXCP message
	 * @param baseId
	 *            Base variable identifier
	 */
	protected void fillMessage(final NXCPMessage msg, final long baseId) {
		long varId = baseId;
		msg.setFieldInt32(varId++, (int) id);
		msg.setFieldInt32(varId++, fireEvent);
		msg.setFieldInt32(varId++, rearmEvent);
		msg.setFieldInt16(varId++, function);
		msg.setFieldInt16(varId++, operation);
		msg.setFieldInt32(varId++, sampleCount);
		msg.setField(varId++, script);
		msg.setFieldInt32(varId++, repeatInterval);
		msg.setField(varId++, value);
	}

	/**
	 * @return the fireEvent
	 */
	public int getFireEvent() {
		return fireEvent;
	}

	/**
	 * @param fireEvent
	 *            the fireEvent to set
	 */
	public void setFireEvent(int fireEvent) {
		this.fireEvent = fireEvent;
	}

	/**
	 * @return the rearmEvent
	 */
	public int getRearmEvent() {
		return rearmEvent;
	}

	/**
	 * @param rearmEvent
	 *            the rearmEvent to set
	 */
	public void setRearmEvent(int rearmEvent) {
		this.rearmEvent = rearmEvent;
	}

	/**
	 * @return sample count
	 */
	public int getSampleCount() {
		return sampleCount;
	}

	/**
	 * Set sample count for threshold
	 * 
	 * @param sampleCount
	 *            sample count
	 */
	public void setSampleCount(int sampleCount) {
		this.sampleCount = sampleCount;
	}

	/**
	 * @return the function
	 */
	public int getFunction() {
		return function;
	}

	/**
	 * @param function
	 *            the function to set
	 */
	public void setFunction(int function) {
		this.function = function;
	}

	/**
	 * @return the operation
	 */
	public int getOperation() {
		return operation;
	}

	/**
	 * @param operation
	 *            the operation to set
	 */
	public void setOperation(int operation) {
		this.operation = operation;
	}

	/**
	 * @return the repeatInterval
	 */
	public int getRepeatInterval() {
		return repeatInterval;
	}

	/**
	 * @param repeatInterval
	 *            the repeatInterval to set
	 */
	public void setRepeatInterval(int repeatInterval) {
		this.repeatInterval = repeatInterval;
	}

	/**
	 * @return the value
	 */
	public String getValue() {
		return value;
	}

	/**
	 * @param value
	 *            the value to set
	 */
	public void setValue(String value) {
		this.value = value;
	}

	/**
	 * @return the id
	 */
	public long getId() {
		return id;
	}

	/**
	 * Returns true if threshold is currently active (it's condition was
	 * evaluated to true at last poll). This field cannot be set and is always
	 * false when threshold received as part of DCI configuration.
	 * 
	 * @return the active status
	 */
	public boolean isActive() {
		return active;
	}

	/**
	 * @return the currentSeverity
	 */
	public Severity getCurrentSeverity() {
		return currentSeverity;
	}

	/**
	 * @return the lastEventTimestamp
	 */
	public Date getLastEventTimestamp() {
		return lastEventTimestamp;
	}

	/**
	 * @return the script
	 */
	public String getScript() {
		return script;
	}

	/**
	 * @param script
	 *            the script to set
	 */
	public void setScript(String script) {
		this.script = script;
	}
}
