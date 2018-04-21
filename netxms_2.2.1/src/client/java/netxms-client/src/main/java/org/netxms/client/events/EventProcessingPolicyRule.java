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
package org.netxms.client.events;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.netxms.base.NXCPCodes;
import org.netxms.base.NXCPMessage;
import org.netxms.client.constants.Severity;

/**
 * This class represents single rule of event processing policy.
 */
public class EventProcessingPolicyRule {
	// Rule flags (options)
	public static final int STOP_PROCESSING = 0x0001;
	public static final int NEGATED_SOURCE = 0x0002;
	public static final int NEGATED_EVENTS = 0x0004;
	public static final int GENERATE_ALARM = 0x0008;
	public static final int DISABLED = 0x0010;
	public static final int TERMINATE_BY_REGEXP = 0x0020;
	public static final int SEVERITY_NORMAL = 0x0100;
	public static final int SEVERITY_WARNING = 0x0200;
	public static final int SEVERITY_MINOR = 0x0400;
	public static final int SEVERITY_MAJOR = 0x0800;
	public static final int SEVERITY_CRITICAL = 0x1000;
	public static final int CREATE_TICKET = 0x2000;

	public static final int SEVERITY_ANY = SEVERITY_NORMAL | SEVERITY_WARNING
			| SEVERITY_MINOR | SEVERITY_MAJOR | SEVERITY_CRITICAL;

	private UUID guid;
	private List<Long> sources;
	private List<Long> events;
	private String script;
	private int flags;
	private String alarmKey;
	private String alarmMessage;
	private Severity alarmSeverity;
	private int alarmTimeout;
	private long alarmTimeoutEvent;
	private List<Long> alarmCategoryIds;
	private List<Long> actions;
	private Map<String, String> persistentStorageSet;
	private List<String> persistentStorageDelete;
	private String comments;
	private int ruleNumber;

	/**
	 * Create empty rule
	 */
	public EventProcessingPolicyRule() {
		guid = UUID.randomUUID();
		sources = new ArrayList<Long>(0);
		events = new ArrayList<Long>(0);
		script = "";
		flags = SEVERITY_ANY;
		alarmKey = "";
		alarmMessage = "%m";
		alarmSeverity = Severity.UNKNOWN;
		alarmTimeout = 0;
		alarmTimeoutEvent = 43;
		alarmCategoryIds = new ArrayList<Long>(0);
		actions = new ArrayList<Long>(0);
		persistentStorageSet = new HashMap<String, String>(0);
		persistentStorageDelete = new ArrayList<String>(0);
		comments = "";
		ruleNumber = 0;
	}

	/**
	 * Copy constructor
	 */
	public EventProcessingPolicyRule(EventProcessingPolicyRule src) {
		guid = UUID.randomUUID();
		sources = new ArrayList<Long>(src.sources);
		events = new ArrayList<Long>(src.events);
		script = src.script;
		flags = src.flags;
		alarmKey = src.alarmKey;
		alarmMessage = src.alarmMessage;
		alarmSeverity = src.alarmSeverity;
		alarmTimeout = src.alarmTimeout;
		alarmTimeoutEvent = src.alarmTimeoutEvent;
		alarmCategoryIds = src.alarmCategoryIds;
		actions = new ArrayList<Long>(src.actions);
		persistentStorageSet = new HashMap<String, String>(
				src.persistentStorageSet);
		persistentStorageDelete = new ArrayList<String>(
				src.persistentStorageDelete);
		comments = src.comments;
		ruleNumber = src.ruleNumber;
	}

	/**
	 * Create rule from NXCP message.
	 * 
	 * @param msg
	 *            NXCP message
	 */
	public EventProcessingPolicyRule(NXCPMessage msg, int ruleNumber) {
		guid = msg.getFieldAsUUID(NXCPCodes.VID_GUID);
		sources = Arrays.asList(msg
				.getFieldAsUInt32ArrayEx(NXCPCodes.VID_RULE_SOURCES));
		events = Arrays.asList(msg
				.getFieldAsUInt32ArrayEx(NXCPCodes.VID_RULE_EVENTS));
		script = msg.getFieldAsString(NXCPCodes.VID_SCRIPT);
		flags = msg.getFieldAsInt32(NXCPCodes.VID_FLAGS);
		alarmKey = msg.getFieldAsString(NXCPCodes.VID_ALARM_KEY);
		alarmMessage = msg.getFieldAsString(NXCPCodes.VID_ALARM_MESSAGE);
		alarmSeverity = Severity.getByValue(msg
				.getFieldAsInt32(NXCPCodes.VID_ALARM_SEVERITY));
		alarmTimeout = msg.getFieldAsInt32(NXCPCodes.VID_ALARM_TIMEOUT);
		alarmTimeoutEvent = msg
				.getFieldAsInt64(NXCPCodes.VID_ALARM_TIMEOUT_EVENT);
		alarmCategoryIds = Arrays.asList(msg
				.getFieldAsUInt32ArrayEx(NXCPCodes.VID_ALARM_CATEGORY_ID));
		actions = Arrays.asList(msg
				.getFieldAsUInt32ArrayEx(NXCPCodes.VID_RULE_ACTIONS));
		comments = msg.getFieldAsString(NXCPCodes.VID_COMMENTS);

		int numSetVar = msg.getFieldAsInt32(NXCPCodes.VID_NUM_SET_PSTORAGE);
		persistentStorageSet = new HashMap<String, String>(numSetVar);
		long varId = NXCPCodes.VID_PSTORAGE_SET_LIST_BASE;
		for (int i = 0; i < numSetVar; i++) {
			final String key = msg.getFieldAsString(varId++);
			final String value = msg.getFieldAsString(varId++);
			persistentStorageSet.put(key, value);
		}

		int numDeleteVar = msg
				.getFieldAsInt32(NXCPCodes.VID_NUM_DELETE_PSTORAGE);
		persistentStorageDelete = new ArrayList<String>(numDeleteVar);
		varId = NXCPCodes.VID_PSTORAGE_DELETE_LIST_BASE;
		for (int i = 0; i < numDeleteVar; i++) {
			final String key = msg.getFieldAsString(varId++);
			persistentStorageDelete.add(key);
		}
		this.ruleNumber = ruleNumber;
	}

	/**
	 * Fill NXCP message with rule's data
	 * 
	 * @param msg
	 *            NXCP message
	 */
	public void fillMessage(final NXCPMessage msg) {
		msg.setField(NXCPCodes.VID_GUID, guid);
		msg.setFieldInt32(NXCPCodes.VID_FLAGS, flags);
		msg.setField(NXCPCodes.VID_COMMENTS, comments);
		msg.setField(NXCPCodes.VID_SCRIPT, script);

		msg.setFieldInt32(NXCPCodes.VID_NUM_ACTIONS, actions.size());
		msg.setField(NXCPCodes.VID_RULE_ACTIONS,
				actions.toArray(new Long[actions.size()]));

		msg.setFieldInt32(NXCPCodes.VID_NUM_EVENTS, events.size());
		msg.setField(NXCPCodes.VID_RULE_EVENTS,
				events.toArray(new Long[events.size()]));

		msg.setFieldInt32(NXCPCodes.VID_NUM_SOURCES, sources.size());
		msg.setField(NXCPCodes.VID_RULE_SOURCES,
				sources.toArray(new Long[sources.size()]));

		msg.setField(NXCPCodes.VID_ALARM_KEY, alarmKey);
		msg.setField(NXCPCodes.VID_ALARM_MESSAGE, alarmMessage);
		msg.setFieldInt16(NXCPCodes.VID_ALARM_SEVERITY,
				alarmSeverity.getValue());
		msg.setFieldInt32(NXCPCodes.VID_ALARM_TIMEOUT, alarmTimeout);
		msg.setFieldInt32(NXCPCodes.VID_ALARM_TIMEOUT_EVENT,
				(int) alarmTimeoutEvent);

		msg.setField(NXCPCodes.VID_ALARM_CATEGORY_ID,
				alarmCategoryIds.toArray(new Long[alarmCategoryIds.size()]));

		msg.setFieldInt32(NXCPCodes.VID_NUM_SET_PSTORAGE,
				persistentStorageSet.size());
		long varId = NXCPCodes.VID_PSTORAGE_SET_LIST_BASE;
		for (Entry<String, String> e : persistentStorageSet.entrySet()) {
			msg.setField(varId++, e.getKey());
			msg.setField(varId++, e.getValue());
		}

		msg.setFieldInt32(NXCPCodes.VID_NUM_DELETE_PSTORAGE,
				persistentStorageDelete.size());
		varId = NXCPCodes.VID_PSTORAGE_DELETE_LIST_BASE;
		for (int i = 0; i < persistentStorageDelete.size(); i++) {
			msg.setField(varId++, persistentStorageDelete.get(i));
		}
	}

	/**
	 * Get rule's comments.
	 * 
	 * @return Rule's comments
	 */
	public String getComments() {
		return comments;
	}

	/**
	 * Set rule's comments.
	 * 
	 * @param comments
	 *            New comments
	 */
	public void setComments(String comments) {
		this.comments = comments;
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
	 * @return the alarmKey
	 */
	public String getAlarmKey() {
		return alarmKey;
	}

	/**
	 * @param alarmKey
	 *            the alarmKey to set
	 */
	public void setAlarmKey(String alarmKey) {
		this.alarmKey = alarmKey;
	}

	/**
	 * @return the alarmMessage
	 */
	public String getAlarmMessage() {
		return alarmMessage;
	}

	/**
	 * @param alarmMessage
	 *            the alarmMessage to set
	 */
	public void setAlarmMessage(String alarmMessage) {
		this.alarmMessage = alarmMessage;
	}

	/**
	 * @return the alarmSeverity
	 */
	public Severity getAlarmSeverity() {
		return alarmSeverity;
	}

	/**
	 * @param alarmSeverity
	 *            the alarmSeverity to set
	 */
	public void setAlarmSeverity(Severity alarmSeverity) {
		this.alarmSeverity = alarmSeverity;
	}

	/**
	 * @return the alarmTimeout
	 */
	public int getAlarmTimeout() {
		return alarmTimeout;
	}

	/**
	 * @param alarmTimeout
	 *            the alarmTimeout to set
	 */
	public void setAlarmTimeout(int alarmTimeout) {
		this.alarmTimeout = alarmTimeout;
	}

	/**
	 * @return the alarmTimeoutEvent
	 */
	public long getAlarmTimeoutEvent() {
		return alarmTimeoutEvent;
	}

	/**
	 * @param alarmTimeoutEvent
	 *            the alarmTimeoutEvent to set
	 */
	public void setAlarmTimeoutEvent(long alarmTimeoutEvent) {
		this.alarmTimeoutEvent = alarmTimeoutEvent;
	}

	/**
	 * @return alarmCategoryIds the alarm category ids
	 */
	public List<Long> getAlarmCategories() {
		return alarmCategoryIds;
	}

	/**
	 * @param alarmCategoryIds
	 *            the alarm category ids to set
	 */
	public void setAlarmCategories(List<Long> alarmCategoryIds) {
		this.alarmCategoryIds = alarmCategoryIds;
	}

	public void removeAlarmCategory(Long categoryId) {
		for (int i = 0; i < alarmCategoryIds.size(); i++) {
			if (alarmCategoryIds.get(i) == categoryId) {
				alarmCategoryIds.remove(i);
				break;
			}
		}

	}

	/**
	 * @return the sources
	 */
	public List<Long> getSources() {
		return sources;
	}

	/**
	 * @return the events
	 */
	public List<Long> getEvents() {
		return events;
	}

	/**
	 * @return the actions
	 */
	public List<Long> getActions() {
		return actions;
	}

	/**
	 * @return the persistentStorageSet
	 */
	public Map<String, String> getPStorageSet() {
		return persistentStorageSet;
	}

	/**
	 * @return the persistentStorageDelete
	 */
	public List<String> getPStorageDelete() {
		return persistentStorageDelete;
	}

	/**
	 * @param sources
	 *            the sources to set
	 */
	public void setSources(List<Long> sources) {
		this.sources = sources;
	}

	/**
	 * @param events
	 *            the events to set
	 */
	public void setEvents(List<Long> events) {
		this.events = events;
	}

	/**
	 * @param actions
	 *            the actions to set
	 */
	public void setActions(List<Long> actions) {
		this.actions = actions;
	}

	/**
	 * @param persistentStorageSet
	 *            the persistentStorageSet to set
	 */
	public void setPStorageSet(Map<String, String> persistentStorageSet) {
		this.persistentStorageSet = persistentStorageSet;
	}

	/**
	 * @param persistentStorageDelete
	 *            the persistentStorageDelete to set
	 */
	public void setPStorageDelete(List<String> persistentStorageDelete) {
		this.persistentStorageDelete = persistentStorageDelete;
	}

	/**
	 * Check rule's DISABLED flag
	 * 
	 * @return true if DISABLED flag set
	 */
	public boolean isDisabled() {
		return (flags & DISABLED) != 0;
	}

	/**
	 * Check rule's NEGATED_SOURCE flag
	 * 
	 * @return true if NEGATED_SOURCE flag set
	 */
	public boolean isSourceInverted() {
		return (flags & NEGATED_SOURCE) != 0;
	}

	/**
	 * Check rule's NEGATED_EVENTS flag
	 * 
	 * @return true if NEGATED_EVENTS flag set
	 */
	public boolean isEventsInverted() {
		return (flags & NEGATED_EVENTS) != 0;
	}

	/**
	 * @return the guid
	 */
	public UUID getGuid() {
		return guid;
	}

	/**
	 * Get rule number
	 * 
	 * @return rule number
	 */
	public int getRuleNumber() {
		return ruleNumber;
	}

	/**
	 * Set rule number
	 * 
	 * @param ruleNumber
	 *            to set
	 */
	public void setRuleNumber(int ruleNumber) {
		this.ruleNumber = ruleNumber;
	}
}
