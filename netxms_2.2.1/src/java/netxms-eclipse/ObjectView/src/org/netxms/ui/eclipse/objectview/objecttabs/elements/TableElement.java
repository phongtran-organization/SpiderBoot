package org.netxms.ui.eclipse.objectview.objecttabs.elements;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.netxms.ui.eclipse.console.resources.SharedColors;
import org.netxms.ui.eclipse.objectview.Messages;
import org.netxms.ui.eclipse.objectview.objecttabs.ObjectTab;
import org.netxms.ui.eclipse.tools.WidgetHelper;

/**
 * Generic table element
 */
public abstract class TableElement extends OverviewPageElement {
	private Table table;
	private Action actionCopy;
	private Action actionCopyName;
	private Action actionCopyValue;

	/**
	 * @param parent
	 * @param anchor
	 * @param objectTab
	 */
	public TableElement(Composite parent, OverviewPageElement anchor,
			ObjectTab objectTab) {
		super(parent, anchor, objectTab);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.netxms.ui.eclipse.widgets.DashboardElement#createClientArea(org.eclipse
	 * .swt.widgets.Composite)
	 */
	@Override
	protected Control createClientArea(Composite parent) {
		table = new Table(parent, SWT.FULL_SELECTION | SWT.HIDE_SELECTION
				| SWT.SINGLE | SWT.H_SCROLL);
		setupTable();
		createActions();
		createPopupMenu();
		fillTableInternal();
		return table;
	}

	/**
	 * Setup table
	 */
	private void setupTable() {
		TableColumn tc = new TableColumn(table, SWT.LEFT);
		tc.setText(Messages.get().TableElement_Name);
		tc = new TableColumn(table, SWT.LEFT);
		tc.setText(Messages.get().TableElement_Value);

		table.setHeaderVisible(false);
		table.setLinesVisible(false);
		table.setBackground(SharedColors.getColor(
				SharedColors.OBJECT_TAB_BACKGROUND, getDisplay()));

		// On Windows Vista and Windows 7, Table widget draws
		// vertical lines even when setLinesVisible set to false.
		// To avoid it, we use custom drawn background. As side effect,
		// selection highlight also removed.
		table.addListener(SWT.EraseItem, new Listener() {
			@Override
			public void handleEvent(Event event) {
				GC gc = event.gc;
				gc.setBackground(SharedColors.getColor(
						SharedColors.OBJECT_TAB_BACKGROUND, getDisplay()));
				gc.fillRectangle(event.x, event.y, event.width, event.height);
			}
		});
	}

	/**
	 * Create actions required for popup menu
	 */
	protected void createActions() {
		actionCopy = new Action(Messages.get().TableElement_ActionCopy) {
			@Override
			public void run() {
				int index = table.getSelectionIndex();
				if (index >= 0) {
					final TableItem item = table.getItem(index);
					WidgetHelper.copyToClipboard(item.getText(0)
							+ "=" + item.getText(1)); //$NON-NLS-1$
				}
			}
		};

		actionCopyName = new Action(Messages.get().TableElement_ActionCopyName) {
			@Override
			public void run() {
				int index = table.getSelectionIndex();
				if (index >= 0) {
					final TableItem item = table.getItem(index);
					WidgetHelper.copyToClipboard(item.getText(0));
				}
			}
		};

		actionCopyValue = new Action(
				Messages.get().TableElement_ActionCopyValue) {
			@Override
			public void run() {
				int index = table.getSelectionIndex();
				if (index >= 0) {
					final TableItem item = table.getItem(index);
					WidgetHelper.copyToClipboard(item.getText(1));
				}
			}
		};
	}

	/**
	 * Create popup menu
	 */
	private void createPopupMenu() {
		MenuManager menuMgr = new MenuManager();
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager mgr) {
				fillContextMenu(mgr);
			}
		});

		Menu menu = menuMgr.createContextMenu(table);
		table.setMenu(menu);
	}

	/**
	 * Fill item's context menu
	 * 
	 * @param manager
	 *            menu manager
	 */
	protected void fillContextMenu(IMenuManager manager) {
		manager.add(actionCopy);
		manager.add(actionCopyName);
		manager.add(actionCopyValue);
	}

	/**
	 * Fill table
	 */
	abstract protected void fillTable();

	/**
	 * Fill table - internal wrapper
	 */
	private void fillTableInternal() {
		if (getObject() == null)
			return;

		fillTable();
		table.getColumn(0).pack();
		table.getColumn(1).pack();
	}

	/**
	 * Add attribute/value pair to list
	 * 
	 * @param attr
	 * @param value
	 * @param allowEmpty
	 *            if set to false, empty values will not be added
	 */
	protected void addPair(String attr, String value, boolean allowEmpty) {
		if (!allowEmpty && ((value == null) || value.isEmpty()))
			return;
		TableItem item = new TableItem(table, SWT.NONE);
		item.setText(0, attr);
		item.setText(1, (value != null) ? value : ""); //$NON-NLS-1$
	}

	/**
	 * Add attribute/value pair to list
	 * 
	 * @param attr
	 * @param value
	 */
	protected void addPair(String attr, String value) {
		addPair(attr, value, true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.netxms.ui.eclipse.objectview.objecttabs.elements.OverviewPageElement
	 * #onObjectChange()
	 */
	@Override
	protected void onObjectChange() {
		table.removeAll();
		fillTableInternal();
	}
}
