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
package org.netxms.ui.eclipse.widgets;

import java.util.LinkedList;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.commands.ActionHandler;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.IHandlerService;
import org.netxms.ui.eclipse.console.Activator;
import org.netxms.ui.eclipse.console.Messages;
import org.netxms.ui.eclipse.console.resources.SharedIcons;
import org.netxms.ui.eclipse.tools.WidgetHelper;
import org.netxms.ui.eclipse.views.helpers.AbstractTraceViewFilter;

/**
 * Abstract widget for trace views
 */
public abstract class AbstractTraceWidget extends Composite {
	private static final int MAX_ELEMENTS = 500;

	private IViewPart viewPart;
	private FilterText filterText;
	private TableViewer viewer;
	private LinkedList<Object> data = new LinkedList<Object>();
	private AbstractTraceViewFilter filter = null;
	private boolean paused = false;
	private boolean filterEnabled = true;
	private long lastUpdated = 0;
	private boolean updateScheduled = false;
	private Action actionPause;
	private Action actionCopy;

	/**
	 * @param parent
	 * @param style
	 * @param viewPart
	 */
	public AbstractTraceWidget(Composite parent, int style, IViewPart viewPart) {
		super(parent, style);

		this.viewPart = viewPart;

		setLayout(new FormLayout());

		filterText = new FilterText(this, SWT.NONE);
		filterText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				onFilterModify();
			}
		});

		viewer = new TableViewer(this, SWT.FULL_SELECTION | SWT.MULTI);
		viewer.getTable().setHeaderVisible(true);
		viewer.setContentProvider(new ArrayContentProvider());
		setupViewer(viewer);
		WidgetHelper.restoreColumnSettings(viewer.getTable(),
				getDialogSettings(), getConfigPrefix());
		viewer.getTable().addDisposeListener(new DisposeListener() {
			@Override
			public void widgetDisposed(DisposeEvent e) {
				WidgetHelper.saveColumnSettings(viewer.getTable(),
						getDialogSettings(), getConfigPrefix());
			}
		});

		// Setup layout
		FormData fd = new FormData();
		fd.left = new FormAttachment(0, 0);
		fd.top = new FormAttachment(filterText);
		fd.right = new FormAttachment(100, 0);
		fd.bottom = new FormAttachment(100, 0);
		viewer.getTable().setLayoutData(fd);

		fd = new FormData();
		fd.left = new FormAttachment(0, 0);
		fd.top = new FormAttachment(0, 0);
		fd.right = new FormAttachment(100, 0);
		filterText.setLayoutData(fd);

		createActions();
		createPopupMenu();

		enableFilter(filterEnabled);

		addDisposeListener(new DisposeListener() {
			@Override
			public void widgetDisposed(DisposeEvent e) {
				saveConfig();
			}
		});
	}

	/**
	 * Create actions
	 */
	protected void createActions() {
		final IHandlerService handlerService = (IHandlerService) PlatformUI
				.getWorkbench().getService(IHandlerService.class);

		actionPause = new Action(Messages.get().AbstractTraceView_Pause,
				Action.AS_CHECK_BOX) {
			@Override
			public void run() {
				setPaused(actionPause.isChecked());
			}
		};
		actionPause.setImageDescriptor(Activator
				.getImageDescriptor("icons/pause.png")); //$NON-NLS-1$
		actionPause
				.setActionDefinitionId("org.netxms.ui.eclipse.library.commands.pause_trace"); //$NON-NLS-1$
		final ActionHandler pauseHandler = new ActionHandler(actionPause);
		handlerService.activateHandler(actionPause.getActionDefinitionId(),
				pauseHandler);

		actionCopy = new Action(
				Messages.get().AbstractTraceView_CopyToClipboard,
				SharedIcons.COPY) {
			@Override
			public void run() {
				copySelectionToClipboard();
			}
		};
		actionCopy
				.setActionDefinitionId("org.netxms.ui.eclipse.library.commands.copy"); //$NON-NLS-1$
		handlerService.activateHandler(actionCopy.getActionDefinitionId(),
				new ActionHandler(actionCopy));
	}

	/**
	 * Create viewer's popup menu
	 */
	private void createPopupMenu() {
		// Create menu manager
		MenuManager menuMgr = new MenuManager();
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager mgr) {
				fillContextMenu(mgr);
			}
		});

		// Create menu
		Menu menu = menuMgr.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);

		// Register menu for extension.
		if (viewPart != null) {
			viewPart.getSite().registerContextMenu(menuMgr, viewer);
		}
	}

	/**
	 * Fill context menu
	 * 
	 * @param manager
	 *            Menu manager
	 */
	protected void fillContextMenu(final IMenuManager manager) {
		manager.add(actionCopy);
		manager.add(new Separator());
		manager.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.swt.widgets.Composite#setFocus()
	 */
	@Override
	public boolean setFocus() {
		return viewer.getControl().setFocus();
	}

	/**
	 * Setup table vewer
	 * 
	 * @param viewer
	 */
	protected abstract void setupViewer(TableViewer viewer);

	/**
	 * Add column to viewer
	 * 
	 * @param name
	 * @param width
	 */
	protected void addColumn(String name, int width) {
		final TableColumn tc = new TableColumn(viewer.getTable(), SWT.LEFT);
		tc.setText(name);
		tc.setWidth(width);
	}

	/**
	 * Add new element to the viewer
	 * 
	 * @param element
	 */
	protected void addElement(Object element) {
		if (data.size() == MAX_ELEMENTS)
			data.removeLast();

		data.addFirst(element);
		if (!paused) {
			long now = System.currentTimeMillis();
			if (now - lastUpdated > 200) {
				viewer.setInput(data.toArray());
				lastUpdated = now;
			} else if (!updateScheduled) {
				updateScheduled = true;
				getDisplay().timerExec(200, new Runnable() {
					@Override
					public void run() {
						viewer.setInput(data.toArray());
						lastUpdated = System.currentTimeMillis();
						updateScheduled = false;
					}
				});
			}
		}
	}

	/**
	 * Clear viewer
	 */
	public void clear() {
		data.clear();
		viewer.setInput(new Object[0]);
	}

	/**
	 * Refresh viewer
	 */
	protected void refresh() {
		viewer.refresh();
	}

	/**
	 * @return the paused
	 */
	protected boolean isPaused() {
		return paused;
	}

	/**
	 * @param paused
	 *            the paused to set
	 */
	protected void setPaused(boolean paused) {
		this.paused = paused;
		if (!paused)
			viewer.setInput(data.toArray());
	}

	/**
	 * Get dialog settings for saving viewer config
	 * 
	 * @return
	 */
	protected abstract IDialogSettings getDialogSettings();

	/**
	 * Get configuration prefix for saving viewer config
	 * 
	 * @return
	 */
	protected abstract String getConfigPrefix();

	/**
	 * Save config
	 */
	protected void saveConfig() {
	}

	/**
	 * Enable or disable filter
	 * 
	 * @param enable
	 *            New filter state
	 */
	public void enableFilter(boolean enable) {
		filterEnabled = enable;
		filterText.setVisible(filterEnabled);
		FormData fd = (FormData) viewer.getTable().getLayoutData();
		fd.top = enable ? new FormAttachment(filterText) : new FormAttachment(
				0, 0);
		layout(true, true);
		if (enable)
			filterText.setFocus();
		else
			setFilter(""); //$NON-NLS-1$
	}

	/**
	 * @return the filterEnabled
	 */
	public boolean isFilterEnabled() {
		return filterEnabled;
	}

	/**
	 * Set filter text
	 * 
	 * @param text
	 *            New filter text
	 */
	protected void setFilter(final String text) {
		filterText.setText(text);
		onFilterModify();
	}

	/**
	 * Handler for filter modification
	 */
	private void onFilterModify() {
		final String text = filterText.getText();
		if (filter != null) {
			filter.setFilterString(text);
			viewer.refresh(false);
		}
	}

	/**
	 * @param filter
	 *            the filter to set
	 */
	protected void setFilter(AbstractTraceViewFilter filter) {
		if (this.filter != null)
			viewer.removeFilter(this.filter);

		this.filter = filter;
		viewer.addFilter(filter);
	}

	/**
	 * Set action to be executed when user press "Close" button in filter.
	 * Default implementation will hide filter area without notifying parent.
	 * 
	 * @param action
	 */
	public void setFilterCloseAction(Action action) {
		filterText.setCloseAction(action);
	}

	/**
	 * @param runnable
	 */
	protected void runInUIThread(final Runnable runnable) {
		getDisplay().asyncExec(runnable);
	}

	/**
	 * Copy selection in the list to clipboard
	 */
	private void copySelectionToClipboard() {
		TableItem[] selection = viewer.getTable().getSelection();
		if (selection.length > 0) {
			StringBuilder sb = new StringBuilder();
			final String newLine = Platform.getOS().equals(Platform.OS_WIN32) ? "\r\n" : "\n"; //$NON-NLS-1$ //$NON-NLS-2$
			for (int i = 0; i < selection.length; i++) {
				if (i > 0)
					sb.append(newLine);
				sb.append(selection[i].getText(0));
				for (int j = 1; j < viewer.getTable().getColumnCount(); j++) {
					sb.append('\t');
					sb.append(selection[i].getText(j));
				}
			}
			WidgetHelper.copyToClipboard(sb.toString());
		}
	}

	/**
	 * @return the actionPause
	 */
	public Action getActionPause() {
		return actionPause;
	}

	/**
	 * @return the actionCopy
	 */
	public Action getActionCopy() {
		return actionCopy;
	}

	/**
	 * Get associated view part
	 * 
	 * @return
	 */
	protected IViewPart getViewPart() {
		return viewPart;
	}

	/**
	 * @return the viewer
	 */
	public TableViewer getViewer() {
		return viewer;
	}

	/**
	 * Get selection provider
	 * 
	 * @return
	 */
	public ISelectionProvider getSelectionProvider() {
		return viewer;
	}
}
