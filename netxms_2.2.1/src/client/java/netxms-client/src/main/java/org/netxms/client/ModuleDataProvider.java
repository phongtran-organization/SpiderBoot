/**
 * NetXMS - open source network management system
 * Copyright (C) 2003-2014 Victor Kirhenshtein
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
/**
 * 
 */
package org.netxms.client;

import org.netxms.base.NXCPMessage;
import org.netxms.client.services.ServiceHandler;

/**
 * Module data provider
 */
public interface ModuleDataProvider extends ServiceHandler {
	/**
	 * Create module data from NXCP message
	 * 
	 * @param msg
	 *            NXCPMessage
	 * @param baseId
	 *            base ID
	 * @return ModularDataProvider object
	 */
	public Object createModuleData(NXCPMessage msg, long baseId);
}
