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
package org.netxms.client.services;

import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

/**
 * Manager for client services
 */
public final class ServiceManager {
	private static Map<ClassLoader, ServiceLoader<ServiceHandler>> serviceLoaders = new HashMap<ClassLoader, ServiceLoader<ServiceHandler>>();

	/**
	 * Reload service providers
	 */
	public static synchronized void registerClassLoader(ClassLoader classLoader) {
		ServiceLoader<ServiceHandler> serviceLoader = serviceLoaders
				.get(classLoader);
		if (serviceLoader != null) {
			serviceLoader.reload();
		} else {
			serviceLoaders.put(classLoader,
					ServiceLoader.load(ServiceHandler.class, classLoader));
		}
	}

	/**
	 * Get service handler by name and check if handler class is correct.
	 * 
	 * @param name
	 *            service name
	 * @param serviceClass
	 *            service handler class
	 * @return service handler for given service
	 */
	public static synchronized ServiceHandler getServiceHandler(String name,
			Class<? extends ServiceHandler> serviceClass) {
		for (ServiceLoader<ServiceHandler> loader : serviceLoaders.values()) {
			for (ServiceHandler s : loader)
				if (s.getServiceName().equals(name)
						&& serviceClass.isInstance(s))
					return s;
		}
		return null;
	}

	/**
	 * Get service handler by name
	 * 
	 * @param name
	 *            service name
	 * @return service handler for given service
	 */
	public static synchronized ServiceHandler getServiceHandler(String name) {
		for (ServiceLoader<ServiceHandler> loader : serviceLoaders.values()) {
			for (ServiceHandler s : loader)
				if (s.getServiceName().equals(name))
					return s;
		}
		return null;
	}

	/**
	 * Get service handler by class
	 * 
	 * @param serviceClass
	 *            service handler class
	 * @return service handler for given service
	 */
	public static synchronized ServiceHandler getServiceHandler(
			Class<? extends ServiceHandler> serviceClass) {
		for (ServiceLoader<ServiceHandler> loader : serviceLoaders.values()) {
			for (ServiceHandler s : loader)
				if (serviceClass.isInstance(s))
					return s;
		}
		return null;
	}

	/**
	 * Debug method to dump all registered services
	 */
	public static synchronized void dump() {
		for (ServiceLoader<ServiceHandler> loader : serviceLoaders.values()) {
			for (ServiceHandler s : loader)
				System.out.println(s.getServiceName());
		}
	}
}
