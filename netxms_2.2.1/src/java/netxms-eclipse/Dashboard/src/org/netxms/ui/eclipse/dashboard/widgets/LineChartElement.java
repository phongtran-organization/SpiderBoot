/**
 * NetXMS - open source network management system
 * Copyright (C) 2003-2016 Victor Kirhenshtein
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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.IViewPart;
import org.netxms.client.NXCSession;
import org.netxms.client.dashboards.DashboardElement;
import org.netxms.client.datacollection.ChartDciConfig;
import org.netxms.client.datacollection.DciData;
import org.netxms.client.datacollection.GraphItem;
import org.netxms.client.datacollection.GraphItemStyle;
import org.netxms.ui.eclipse.actions.RefreshAction;
import org.netxms.ui.eclipse.charts.api.ChartColor;
import org.netxms.ui.eclipse.charts.api.ChartFactory;
import org.netxms.ui.eclipse.charts.api.HistoricalDataChart;
import org.netxms.ui.eclipse.dashboard.Activator;
import org.netxms.ui.eclipse.dashboard.Messages;
import org.netxms.ui.eclipse.dashboard.widgets.internal.LineChartConfig;
import org.netxms.ui.eclipse.jobs.ConsoleJob;
import org.netxms.ui.eclipse.perfview.views.HistoricalGraphView;
import org.netxms.ui.eclipse.perfview.views.HistoricalGraphView.ActionType;
import org.netxms.ui.eclipse.shared.ConsoleSharedData;
import org.netxms.ui.eclipse.tools.ViewRefreshController;

/**
 * Line chart element
 */
public class LineChartElement extends ElementWidget {
	private HistoricalDataChart chart;
	private LineChartConfig config;
	private ViewRefreshController refreshController;
	private boolean updateInProgress = false;
	private NXCSession session;
	private List<DataCacheElement> dataCache = new ArrayList<DataCacheElement>(
			16);
	private Action actionRefresh;
	private Action actionAdjustX;
	private Action actionAdjustY;
	private Action actionAdjustBoth;
	private Action[] presetActions;

	/**
	 * @param parent
	 * @param data
	 */
	public LineChartElement(DashboardControl parent, DashboardElement element,
			IViewPart viewPart) {
		super(parent, element, viewPart);
		session = (NXCSession) ConsoleSharedData.getSession();

		try {
			config = LineChartConfig.createFromXml(element.getData());
		} catch (Exception e) {
			e.printStackTrace();
			config = new LineChartConfig();
		}

		setLayout(new FillLayout());

		chart = ChartFactory.createLineChart(this, SWT.NONE);
		chart.setZoomEnabled(config.isInteractive());
		chart.setTitleVisible(config.isShowTitle());
		chart.setChartTitle(config.getTitle());
		chart.setLegendVisible(config.isShowLegend());
		chart.setLegendPosition(config.getLegendPosition());
		chart.setExtendedLegend(config.isExtendedLegend());
		chart.setGridVisible(config.isShowGrid());
		chart.setLogScaleEnabled(config.isLogScaleEnabled());
		chart.setStacked(config.isStacked());
		chart.setLineWidth(config.getLineWidth());
		chart.setTranslucent(config.isTranslucent());
		if (!config.isAutoScale())
			chart.setYAxisRange(config.getMinYScaleValue(),
					config.getMaxYScaleValue());
		else
			chart.modifyYBase(config.modifyYBase());

		final List<GraphItemStyle> styles = new ArrayList<GraphItemStyle>(
				config.getDciList().length);
		int index = 0;
		for (ChartDciConfig dci : config.getDciList()) {
			chart.addParameter(new GraphItem(dci.nodeId, dci.dciId, 0, 0, Long
					.toString(dci.dciId), dci.getName(), dci.getDisplayFormat()));
			int color = dci.getColorAsInt();
			if (color == -1)
				color = ChartColor.getDefaultColor(index).getRGB();
			styles.add(new GraphItemStyle(getDisplayType(dci), color, 2,
					dci.invertValues ? GraphItemStyle.INVERTED : 0));
			index++;
		}
		chart.setItemStyles(styles);

		refreshController = new ViewRefreshController(viewPart,
				config.getRefreshRate(), new Runnable() {
					@Override
					public void run() {
						if (LineChartElement.this.isDisposed())
							return;

						refreshData();
					}
				});
		refreshData();

		addDisposeListener(new DisposeListener() {
			@Override
			public void widgetDisposed(DisposeEvent e) {
				refreshController.dispose();
			}
		});

		if (config.isInteractive()) {
			createActions();
			createChartContextMenu();
		}
	}

	/**
	 * Create actions
	 */
	private void createActions() {
		actionRefresh = new RefreshAction() {
			@Override
			public void run() {
				refreshData();
			}
		};

		actionAdjustX = HistoricalGraphView.createAction(ActionType.ADJUST_X,
				chart);
		actionAdjustY = HistoricalGraphView.createAction(ActionType.ADJUST_Y,
				chart);
		actionAdjustBoth = HistoricalGraphView.createAction(
				ActionType.ADJUST_BOTH, chart);

		presetActions = HistoricalGraphView
				.createPresetActions(new HistoricalGraphView.PresetHandler() {
					@Override
					public void onPresetSelected(int units, int range) {
						config.setTimeUnits(units);
						config.setTimeRange(range);
						refreshData();
					}
				});
	}

	/**
	 * Create chart's context menu
	 */
	private void createChartContextMenu() {
		final MenuManager manager = new MenuManager();
		manager.setRemoveAllWhenShown(true);
		manager.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager mgr) {
				fillContextMenu(manager);
			}
		});

		Menu menu = manager.createContextMenu((Control) chart);
		((Control) chart).setMenu(menu);
		for (Control ch : ((Composite) chart).getChildren()) {
			ch.setMenu(menu);
		}
	}

	/**
	 * Fill context menu
	 * 
	 * @param manager
	 */
	private void fillContextMenu(IMenuManager manager) {
		MenuManager presets = new MenuManager("&Presets");
		for (int i = 0; i < presetActions.length; i++)
			presets.add(presetActions[i]);

		manager.add(presets);
		manager.add(new Separator());
		manager.add(actionAdjustBoth);
		manager.add(actionAdjustX);
		manager.add(actionAdjustY);
		manager.add(new Separator());
		manager.add(actionRefresh);
	}

	/**
	 * @param dci
	 * @return
	 */
	private int getDisplayType(ChartDciConfig dci) {
		int type = dci.getDisplayType();
		switch (type) {
		case ChartDciConfig.AREA:
			return GraphItemStyle.AREA;
		case ChartDciConfig.LINE:
			return GraphItemStyle.LINE;
		default:
			return config.isArea() ? GraphItemStyle.AREA : GraphItemStyle.LINE;
		}
	}

	/**
	 * Refresh graph's data
	 */
	private void refreshData() {
		if (updateInProgress)
			return;

		updateInProgress = true;

		ConsoleJob job = new ConsoleJob(
				Messages.get().LineChartElement_JobTitle, viewPart,
				Activator.PLUGIN_ID, Activator.PLUGIN_ID) {
			private ChartDciConfig currentDci;

			@Override
			protected void runInternal(IProgressMonitor monitor)
					throws Exception {
				final Date from = new Date(System.currentTimeMillis()
						- config.getTimeRangeMillis());
				final Date to = new Date(System.currentTimeMillis());
				final ChartDciConfig[] dciList = config.getDciList();
				final DciData[] data = new DciData[dciList.length];
				for (int i = 0; i < dciList.length; i++) {
					currentDci = dciList[i];
					if (currentDci.type == ChartDciConfig.ITEM)
						data[i] = session.getCollectedData(currentDci.nodeId,
								currentDci.dciId, from, to, 0);
					else
						data[i] = session.getCollectedTableData(
								currentDci.nodeId, currentDci.dciId,
								currentDci.instance, currentDci.column, from,
								to, 0);
				}
				runInUIThread(new Runnable() {
					@Override
					public void run() {
						if (!((Widget) chart).isDisposed()) {
							dataCache.clear();
							chart.setTimeRange(from, to);
							for (int i = 0; i < data.length; i++) {
								chart.updateParameter(i, data[i], false);
								dataCache.add(new DataCacheElement(dciList[i],
										data[i]));
							}
							chart.refresh();
							chart.clearErrors();
						}
						updateInProgress = false;
					}
				});
			}

			@Override
			protected String getErrorMessage() {
				return String.format(Messages.get().LineChartElement_JobError,
						session.getObjectName(currentDci.nodeId),
						currentDci.name);
			}

			@Override
			protected void jobFailureHandler() {
				updateInProgress = false;
				super.jobFailureHandler();
			}

			@Override
			protected IStatus createFailureStatus(final Exception e) {
				runInUIThread(new Runnable() {
					@Override
					public void run() {
						chart.addError(getErrorMessage()
								+ " (" + e.getLocalizedMessage() + ")"); //$NON-NLS-1$ //$NON-NLS-2$
					}
				});
				return Status.OK_STATUS;
			}
		};
		job.setUser(false);
		job.start();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.swt.widgets.Composite#computeSize(int, int, boolean)
	 */
	@Override
	public Point computeSize(int wHint, int hHint, boolean changed) {
		Point size = super.computeSize(wHint, hHint, changed);
		if ((hHint == SWT.DEFAULT) && (size.y < 210))
			size.y = 210;
		return size;
	}

	/**
	 * Get data cache for this chart. Must be called on UI thread.
	 * 
	 * @return
	 */
	public List<DataCacheElement> getDataCache() {
		return new ArrayList<DataCacheElement>(dataCache);
	}

	/**
	 * Data cache element
	 */
	public class DataCacheElement {
		public DciData data;
		public String name;

		public DataCacheElement(ChartDciConfig config, DciData data) {
			this.name = config.getName();
			this.data = data;
		}
	}
}
