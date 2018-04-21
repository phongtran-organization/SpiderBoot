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
package org.netxms.ui.eclipse.objectbrowser.dialogs;

import java.net.InetAddress;
import java.util.HashSet;
import java.util.Set;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.netxms.base.InetAddressEx;
import org.netxms.client.objects.AbstractNode;
import org.netxms.client.objects.AbstractObject;
import org.netxms.client.objects.Interface;
import org.netxms.ui.eclipse.objectbrowser.Messages;
import org.netxms.ui.eclipse.objectbrowser.dialogs.helpers.AddressListElement;
import org.netxms.ui.eclipse.objectbrowser.dialogs.helpers.AddressListLabelProvider;
import org.netxms.ui.eclipse.tools.ComparatorHelper;
import org.netxms.ui.eclipse.tools.MessageDialogHelper;
import org.netxms.ui.eclipse.tools.WidgetHelper;

/**
 * IP address selection dialog.
 * 
 */
public class IPAddressSelectionDialog extends Dialog {
	private AbstractNode node;
	private TableViewer viewer;
	private InetAddress address;

	/**
	 * Create IP address selection dialog.
	 * 
	 * @param parentShell
	 *            parent shell
	 * @param node
	 *            node object which IP address should be selected
	 */
	public IPAddressSelectionDialog(Shell parentShell, AbstractNode node) {
		super(parentShell);
		this.node = node;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets
	 * .Shell)
	 */
	@Override
	protected void configureShell(Shell newShell) {
		newShell.setText(Messages.get().IPAddressSelectionDialog_Title);
		super.configureShell(newShell);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets
	 * .Composite)
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite dialogArea = (Composite) super.createDialogArea(parent);
		GridLayout layout = new GridLayout();
		layout.marginHeight = WidgetHelper.DIALOG_HEIGHT_MARGIN;
		layout.marginWidth = WidgetHelper.DIALOG_WIDTH_MARGIN;
		layout.verticalSpacing = WidgetHelper.DIALOG_SPACING;
		dialogArea.setLayout(layout);

		// Label label = new Label(dialogArea, SWT.NONE);

		viewer = new TableViewer(dialogArea, SWT.BORDER | SWT.FULL_SELECTION);
		GridData gd = new GridData();
		gd.horizontalAlignment = SWT.FILL;
		gd.verticalAlignment = SWT.FILL;
		gd.grabExcessHorizontalSpace = true;
		gd.grabExcessVerticalSpace = true;
		gd.heightHint = 300;
		gd.widthHint = 250;
		viewer.getControl().setLayoutData(gd);
		viewer.setContentProvider(new ArrayContentProvider());
		viewer.setComparator(new ViewerComparator() {
			@Override
			public int compare(Viewer viewer, Object e1, Object e2) {
				return ComparatorHelper.compareInetAddresses(
						((AddressListElement) e1).address,
						((AddressListElement) e2).address);
			}
		});
		viewer.setLabelProvider(new AddressListLabelProvider());

		final Table table = viewer.getTable();
		table.setHeaderVisible(true);

		TableColumn tc = new TableColumn(table, SWT.LEFT);
		tc.setText(Messages.get().IPAddressSelectionDialog_IPAddress);
		tc.setWidth(90);

		tc = new TableColumn(table, SWT.LEFT);
		tc.setText(Messages.get().IPAddressSelectionDialog_Interface);
		tc.setWidth(150);

		Set<AddressListElement> addrList = new HashSet<AddressListElement>();
		for (AbstractObject o : node.getChildsAsArray()) {
			if (o instanceof Interface) {
				for (InetAddressEx a : ((Interface) o).getIpAddressList()) {
					if (a.isValidUnicastAddress()) {
						addrList.add(new AddressListElement((Interface) o, a
								.getAddress()));
					}
				}
			}
		}
		viewer.setInput(addrList.toArray());

		return dialogArea;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.Dialog#okPressed()
	 */
	@Override
	protected void okPressed() {
		IStructuredSelection selection = (IStructuredSelection) viewer
				.getSelection();
		if (selection.isEmpty()) {
			MessageDialogHelper.openWarning(getShell(),
					Messages.get().IPAddressSelectionDialog_Warning,
					Messages.get().IPAddressSelectionDialog_WarningText);
			return;
		}
		address = ((AddressListElement) selection.getFirstElement()).address;
		super.okPressed();
	}

	/**
	 * Get selected address
	 * 
	 * @return the address
	 */
	public InetAddress getAddress() {
		return address;
	}
}
