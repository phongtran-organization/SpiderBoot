/**
 * NetXMS - open source network management system
 * Copyright (C) 2003-2014 Raden Solutions
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

import java.io.File;

/**
 * Information about file received from agent
 */
public class AgentFileData {
	private String id;
	private File file;
	private String remoteName;

	/**
	 * Create new agent file object
	 * 
	 * @param id
	 *            file id
	 * @param remoteName
	 *            name of remote file
	 * @param file
	 *            local file
	 */
	public AgentFileData(String id, String remoteName, File file) {
		this.id = id;
		this.remoteName = remoteName;
		this.file = file;
	}

	/**
	 * @return file id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @return remote file name
	 */
	public String getRemoteName() {
		return remoteName;
	}

	/**
	 * Get local file
	 * 
	 * @return local file
	 */
	public File getFile() {
		return file;
	}
}
