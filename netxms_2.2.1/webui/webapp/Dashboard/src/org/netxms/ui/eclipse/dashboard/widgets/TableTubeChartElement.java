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
package org.netxms.ui.eclipse.dashboard.widgets;

import org.eclipse.swt.SWT;
import org.eclipse.ui.IViewPart;
import org.netxms.client.dashboards.DashboardElement;
import org.netxms.ui.eclipse.charts.api.ChartFactory;
import org.netxms.ui.eclipse.dashboard.widgets.internal.TableTubeChartConfig;

/**
 * Tube chart element for table DCI
 */
public class TableTubeChartElement extends TableComparisonChartElement
{
	/**
	 * @param parent
	 * @param data
	 */
	public TableTubeChartElement(DashboardControl parent, DashboardElement element, IViewPart viewPart)
	{
		super(parent, element, viewPart);
		
		try
		{
			config = TableTubeChartConfig.createFromXml(element.getData());
		}
		catch(Exception e)
		{
			e.printStackTrace();
			config = new TableTubeChartConfig();
		}

		chart = ChartFactory.createTubeChart(this, SWT.NONE);
		chart.setTitleVisible(config.isShowTitle());
		chart.setChartTitle(config.getTitle());
		chart.setLegendPosition(config.getLegendPosition());
		chart.setLegendVisible(config.isShowLegend());
		chart.set3DModeEnabled(config.isShowIn3D());
		chart.setTransposed(((TableTubeChartConfig)config).isTransposed());
		chart.setTranslucent(config.isTranslucent());
		if (!config.isAutoScale())
         chart.setYAxisRange(config.getMinYScaleValue(), config.getMaxYScaleValue());
		
		startRefreshTimer();
	}
}
