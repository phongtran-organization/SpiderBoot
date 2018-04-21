/**
 * NetXMS - open source network management system
 * Copyright (C) 2003-2010 Victor Kirhenshtein
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
package org.netxms.client.server;

import org.netxms.base.NXCPCodes;
import org.netxms.base.NXCPMessage;

/**
 * Represents server-side job
 */
public class ServerJob {
	// Job status codes
	public static final int PENDING = 0;
	public static final int ACTIVE = 1;
	public static final int ON_HOLD = 2;
	public static final int COMPLETED = 3;
	public static final int FAILED = 4;
	public static final int CANCELLED = 5;
	public static final int CANCEL_PENDING = 6;

	private long id;
	private long userId;
	private long nodeId;
	private String jobType;
	private String description;
	private int status;
	private int progress;
	private String failureMessage;

	/**
	 * Create job object from NXCP message
	 * 
	 * @param msg
	 *            NXCP message
	 * @param baseVarId
	 *            Base variable ID for job's data
	 */
	public ServerJob(final NXCPMessage msg, final long baseVarId) {
		long var = baseVarId;

		id = msg.getFieldAsInt64(var++);
		jobType = msg.getFieldAsString(var++);
		description = msg.getFieldAsString(var++);
		nodeId = msg.getFieldAsInt64(var++);
		status = msg.getFieldAsInt32(var++);
		progress = msg.getFieldAsInt32(var++);
		failureMessage = msg.getFieldAsString(var++);
		userId = msg.getFieldAsInt64(var++);
	}

	/**
	 * Create job object from notification message
	 * 
	 * @param msg
	 *            NXCP message
	 */
	public ServerJob(final NXCPMessage msg) {
		id = msg.getFieldAsInt64(NXCPCodes.VID_JOB_ID);
		userId = msg.getFieldAsInt64(NXCPCodes.VID_USER_ID);
		jobType = msg.getFieldAsString(NXCPCodes.VID_JOB_TYPE);
		description = msg.getFieldAsString(NXCPCodes.VID_DESCRIPTION);
		nodeId = msg.getFieldAsInt64(NXCPCodes.VID_OBJECT_ID);
		status = msg.getFieldAsInt32(NXCPCodes.VID_JOB_STATUS);
		progress = msg.getFieldAsInt32(NXCPCodes.VID_JOB_PROGRESS);
		failureMessage = msg.getFieldAsString(NXCPCodes.VID_FAILURE_MESSAGE);
	}

	/**
	 * @return the id
	 */
	public long getId() {
		return id;
	}

	/**
	 * @return the nodeId
	 */
	public long getNodeId() {
		return nodeId;
	}

	/**
	 * @return the jobType
	 */
	public String getJobType() {
		return jobType;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @return the status
	 */
	public int getStatus() {
		return status;
	}

	/**
	 * @return the progress
	 */
	public int getProgress() {
		return progress;
	}

	/**
	 * @return the failureMessage
	 */
	public String getFailureMessage() {
		return failureMessage;
	}

	/**
	 * @return the userId
	 */
	public long getUserId() {
		return userId;
	}
}
