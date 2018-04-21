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

/**
 * Listener for text output (provided by actions, scripts, etc.)
 */
public interface TextOutputListener {
	/**
	 * Called when new message is received.
	 * 
	 * @param text
	 *            received message
	 */
	public void messageReceived(String text);

	/**
	 * Called when output stream ID is known
	 * 
	 * @param streamId
	 *            ID of output stream (actual meaning depends on API call)
	 */
	public void setStreamId(long streamId);
}
