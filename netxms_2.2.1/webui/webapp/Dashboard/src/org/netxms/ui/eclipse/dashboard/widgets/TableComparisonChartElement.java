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

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.IViewPart;
import org.netxms.client.NXCSession;
import org.netxms.client.Table;
import org.netxms.client.TableCell;
import org.netxms.client.TableRow;
import org.netxms.client.dashboards.DashboardElement;
import org.netxms.client.datacollection.GraphItem;
import org.netxms.ui.eclipse.charts.api.DataChart;
import org.netxms.ui.eclipse.charts.api.DataComparisonChart;
import org.netxms.ui.eclipse.dashboard.Activator;
import org.netxms.ui.eclipse.dashboard.Messages;
import org.netxms.ui.eclipse.dashboard.widgets.internal.TableComparisonChartConfig;
import org.netxms.ui.eclipse.jobs.ConsoleJob;
import org.netxms.ui.eclipse.shared.ConsoleSharedData;
import org.netxms.ui.eclipse.tools.ViewRefreshController;

/**
 * Base class for data comparison charts based on table DCI - like bar chart, pie chart, etc.
 */
public abstract class TableComparisonChartElement extends ElementWidget
{
	protected DataComparisonChart chart;
	protected NXCSession session;
	protected TableComparisonChartConfig config;
	
	private ViewRefreshController refreshController;
	private boolean updateInProgress = false;
	private Map<String, Integer> instanceMap = new HashMap<String, Integer>(DataChart.MAX_CHART_ITEMS);
	private boolean chartInitialized = false;

	/**
	 * @param parent
	 * @param data
	 */
	public TableComparisonChartElement(DashboardControl parent, DashboardElement element, IViewPart viewPart)
	{
		super(parent, element, viewPart);
		session = (NXCSession)ConsoleSharedData.getSession();

		setLayout(new FillLayout());
		
		addDisposeListener(new DisposeListener() {
         @Override
         public void widgetDisposed(DisposeEvent e)
         {
            if (refreshController != null)
               refreshController.dispose();
         }
      });
	}
	
	/**
	 * Start refresh timer
	 */
	protected void startRefreshTimer()
	{
		if ((config == null) || (config.getDataColumn() == null))
			return;	// Invalid configuration
		
		refreshController = new ViewRefreshController(viewPart, config.getRefreshRate(), new Runnable() {
			@Override
			public void run()
			{
				if (TableComparisonChartElement.this.isDisposed())
					return;
				
				refreshData();
			}
		});
		refreshData();
	}

	/**
	 * Refresh graph's data
	 */
	protected void refreshData()
	{
		if (updateInProgress)
			return;
		
		updateInProgress = true;
		
		ConsoleJob job = new ConsoleJob(Messages.get().TableComparisonChartElement_JobTitle, viewPart, Activator.PLUGIN_ID, Activator.PLUGIN_ID) {
			@Override
			protected void runInternal(IProgressMonitor monitor) throws Exception
			{
				final Table data = session.getTableLastValues(config.getNodeId(), config.getDciId());
				runInUIThread(new Runnable() {
					@Override
					public void run()
					{
						if (!((Widget)chart).isDisposed())
							updateChart(data);
						updateInProgress = false;
					}
				});
			}
	
			@Override
			protected String getErrorMessage()
			{
				return Messages.get().TableComparisonChartElement_JobError;
			}
	
			@Override
			protected void jobFailureHandler()
			{
				updateInProgress = false;
				super.jobFailureHandler();
			}
		};
		job.setUser(false);
		job.start();
	}
	
	/**
	 * Update chart with new data
	 * 
	 * @param data
	 */
	private void updateChart(final Table data)
	{
		String instanceColumn = (config.getInstanceColumn() != null) ? config.getInstanceColumn() : ""; // FIXME //$NON-NLS-1$
		if (instanceColumn == null)
			return;
		
		final int icIndex = data.getColumnIndex(instanceColumn);
		final int dcIndex = data.getColumnIndex(config.getDataColumn());
		if ((icIndex == -1) || (dcIndex == -1))
			return;	// at least one column is missing
		
		if (config.isSortOnDataColumn())
		{
		   data.sort(new Comparator<TableRow>() {
            @Override
            public int compare(TableRow row1, TableRow row2)
            {
               TableCell c1 = row1.get(dcIndex);
               TableCell c2 = row2.get(dcIndex);

               String s1 = (c1 != null) ? c1.getValue() : "";
               String s2 = (c2 != null) ? c2.getValue() : "";
               
               int result = 0;
               try
               {
                  double value1 = Double.parseDouble(s1);
                  double value2 = Double.parseDouble(s2);
                  result = Double.compare(value1, value2);
               }
               catch(NumberFormatException e)
               {
                  result = s1.compareToIgnoreCase(s2);
               }
               return config.isSortDescending() ? -result : result;
            }
         });
		   
		   // Sorting may reorder instances, so clear everything
		   instanceMap.clear();
		   chart.removeAllParameters();
		}

		boolean rebuild = false;
		for(int i = 0; i < data.getRowCount(); i++)
		{
			String instance = data.getCellValue(i, icIndex);
			if (instance == null)
				continue;

			double value;
			try
			{
				value = Double.parseDouble(data.getCellValue(i, dcIndex));
			}
			catch(NumberFormatException e)
			{
				value = 0.0;
			}
			
			Integer index = instanceMap.get(instance);
			if (index == null)
			{
				if ((instanceMap.size() >= DataChart.MAX_CHART_ITEMS) ||
				    ((value == 0) && config.isIgnoreZeroValues()))
					continue;
				index = chart.addParameter(new GraphItem(config.getNodeId(), config.getDciId(), 0, 0, Long.toString(config.getDciId()), instance, "%s"), 0.0); //$NON-NLS-1$
				instanceMap.put(instance, index);
				rebuild = true;
			}

			chart.updateParameter(index, value, false);
		}

		if (!chartInitialized)
		{
			chart.initializationComplete();
			chartInitialized = true;
		}
		else
		{
			if (rebuild)
				chart.rebuild();
			else
				chart.refresh();
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.swt.widgets.Composite#computeSize(int, int, boolean)
	 */
	@Override
	public Point computeSize(int wHint, int hHint, boolean changed)
	{
		Point size = super.computeSize(wHint, hHint, changed);
		if ((hHint == SWT.DEFAULT) && (size.y < 250))
			size.y = 250;
		return size;
	}
}
