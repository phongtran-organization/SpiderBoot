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
package org.netxms.ui.eclipse.networkmaps.views.helpers;

import org.eclipse.gef4.zest.core.widgets.GraphItem;
import org.eclipse.gef4.zest.core.widgets.GraphNode;
import org.eclipse.gef4.zest.core.widgets.LayoutFilter;
import org.netxms.client.maps.elements.NetworkMapDCIContainer;
import org.netxms.client.maps.elements.NetworkMapDCIImage;
import org.netxms.client.maps.elements.NetworkMapDecoration;
import org.netxms.client.maps.elements.NetworkMapTextBox;

/**
 * Custom graph layout filter which prevents decoration
 * elements from being included into layout
 *
 */
public class GraphLayoutFilter implements LayoutFilter
{
	private boolean filterDecoration;
	
	/**
	 * Create new layout filter. If filterDecoration set to true, decoration
	 * elements will be filtered out, otherwise all elements except decoration
	 * will be filtered out.
	 * 
	 * @param filterDecoration
	 */
	public GraphLayoutFilter(boolean filterDecoration)
	{
		this.filterDecoration = filterDecoration;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.gef4.zest.core.widgets.LayoutFilter#isObjectFiltered(org.eclipse.gef4.zest.core.widgets.GraphItem)
	 */
	@Override
	public boolean isObjectFiltered(GraphItem item)
	{
		if (item instanceof GraphNode)
		{
			if (filterDecoration)
				return (item.getData() instanceof NetworkMapDecoration || item.getData() instanceof NetworkMapDCIContainer || item.getData() instanceof NetworkMapDCIImage || item.getData() instanceof NetworkMapTextBox);
			else
				return !(item.getData() instanceof NetworkMapDecoration || item.getData() instanceof NetworkMapDCIContainer || item.getData() instanceof NetworkMapDCIImage || item.getData() instanceof NetworkMapTextBox);
		}
		return false;
	}
}
