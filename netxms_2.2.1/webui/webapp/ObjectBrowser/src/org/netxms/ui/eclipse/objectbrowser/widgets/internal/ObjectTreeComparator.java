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
package org.netxms.ui.eclipse.objectbrowser.widgets.internal;

import java.util.Comparator;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.netxms.client.objects.AbstractObject;

/**
 * Comparator for object tree
 *
 */
public class ObjectTreeComparator extends ViewerComparator
{
	// Categories
	private static final int CATEGORY_CONTAINER = 10;
	private static final int CATEGORY_NODELINK = 100;
	private static final int CATEGORY_OTHER = 255;

	/**
	 * Default constructor
	 */
	public ObjectTreeComparator()
	{
		super(new Comparator<String>() {
			@Override
			public int compare(String o1, String o2)
			{
				return o1.compareToIgnoreCase(o2);
			}
		});
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ViewerComparator#category(java.lang.Object)
	 */
	@Override
	public int category(Object element)
	{
		if (((AbstractObject)element).getObjectId() < 10)
			return (int)((AbstractObject)element).getObjectId();
		if ((((AbstractObject)element).getObjectClass() == AbstractObject.OBJECT_CONTAINER) ||
		    (((AbstractObject)element).getObjectClass() == AbstractObject.OBJECT_TEMPLATEGROUP) ||
		    (((AbstractObject)element).getObjectClass() == AbstractObject.OBJECT_POLICYGROUP) ||
		    (((AbstractObject)element).getObjectClass() == AbstractObject.OBJECT_BUSINESSSERVICE))
			return CATEGORY_CONTAINER;
		if (((AbstractObject)element).getObjectClass() == AbstractObject.OBJECT_NODELINK)
				return CATEGORY_NODELINK;
		return CATEGORY_OTHER;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ViewerComparator#compare(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
	 */
	@Override
	public int compare(Viewer viewer, Object e1, Object e2)
	{
		return super.compare(viewer, e1, e2);
	}
}
