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
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.ui.IViewPart;
import org.netxms.client.dashboards.DashboardElement;
import org.netxms.client.datacollection.ChartDciConfig;
import org.netxms.client.datacollection.GraphItem;
import org.netxms.ui.eclipse.charts.api.ChartColor;
import org.netxms.ui.eclipse.charts.api.ChartFactory;
import org.netxms.ui.eclipse.dashboard.widgets.internal.PieChartConfig;

/**
 * Pie chart element
 */
public class PieChartElement extends ComparisonChartElement {
	private PieChartConfig config;

	/**
	 * @param parent
	 * @param data
	 */
	public PieChartElement(DashboardControl parent, DashboardElement element,
			IViewPart viewPart) {
		super(parent, element, viewPart);

		try {
			config = PieChartConfig.createFromXml(element.getData());
		} catch (Exception e) {
			e.printStackTrace();
			config = new PieChartConfig();
		}

		refreshInterval = config.getRefreshRate();

		setLayout(new FillLayout());

		chart = ChartFactory.createPieChart(this, SWT.NONE);
		chart.setTitleVisible(config.isShowTitle());
		chart.setChartTitle(config.getTitle());
		chart.setLegendPosition(config.getLegendPosition());
		chart.setLegendVisible(config.isShowLegend());
		chart.set3DModeEnabled(config.isShowIn3D());
		chart.setTranslucent(config.isTranslucent());

		int index = 0;
		for (ChartDciConfig dci : config.getDciList()) {
			chart.addParameter(
					new GraphItem(dci.nodeId, dci.dciId, 0, 0, Long
							.toString(dci.dciId), dci.getName(), dci
							.getDisplayFormat()), 0.0);
			int color = dci.getColorAsInt();
			if (color != -1)
				chart.setPaletteEntry(index, new ChartColor(color));
			index++;
		}
		chart.initializationComplete();

		startRefreshTimer();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.netxms.ui.eclipse.dashboard.widgets.ComparisonChartElement#getDciList
	 * ()
	 */
	@Override
	protected ChartDciConfig[] getDciList() {
		return config.getDciList();
	}
}
