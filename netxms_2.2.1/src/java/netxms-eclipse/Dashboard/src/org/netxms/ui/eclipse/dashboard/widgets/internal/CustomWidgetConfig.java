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
package org.netxms.ui.eclipse.dashboard.widgets.internal;

import java.io.StringWriter;
import java.io.Writer;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

/**
 * Configuration for custom widget
 */
public class CustomWidgetConfig extends DashboardElementConfig {
	@Element(required = false)
	private String className = ""; //$NON-NLS-1$

	/**
	 * Create line chart settings object from XML document
	 * 
	 * @param xml
	 *            XML document
	 * @return deserialized object
	 * @throws Exception
	 *             if the object cannot be fully deserialized
	 */
	public static CustomWidgetConfig createFromXml(final String xml)
			throws Exception {
		Serializer serializer = new Persister();
		return serializer.read(CustomWidgetConfig.class, xml);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.netxms.ui.eclipse.dashboard.widgets.internal.DashboardElementConfig
	 * #createXml()
	 */
	@Override
	public String createXml() throws Exception {
		Serializer serializer = new Persister();
		Writer writer = new StringWriter();
		serializer.write(this, writer);
		return writer.toString();
	}

	/**
	 * @return the className
	 */
	public String getClassName() {
		return className;
	}

	/**
	 * @param className
	 *            the className to set
	 */
	public void setClassName(String className) {
		this.className = className;
	}
}
