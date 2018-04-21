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
package org.netxms.ui.eclipse.serverconfig.views.helpers;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ITableFontProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.netxms.client.server.ServerVariable;
import org.netxms.ui.eclipse.serverconfig.Messages;
import org.netxms.ui.eclipse.serverconfig.views.ServerConfigurationEditor;

/**
 * Label provider for server configuration variables
 */
public class ServerVariablesLabelProvider extends LabelProvider implements
		ITableLabelProvider, ITableFontProvider {
	private Font textFont;

	/**
	 * The constructor
	 */
	public ServerVariablesLabelProvider() {
		FontData fd = JFaceResources.getDefaultFont().getFontData()[0];
		fd.setStyle(SWT.BOLD);
		textFont = new Font(Display.getDefault(), fd);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang
	 * .Object, int)
	 */
	@Override
	public String getColumnText(Object obj, int index) {
		switch (index) {
		case ServerConfigurationEditor.COLUMN_NAME:
			return ((ServerVariable) obj).getName();
		case ServerConfigurationEditor.COLUMN_VALUE:
			if (((ServerVariable) obj).getDataType().equals("C"))
				return ((ServerVariable) obj).getValueDescription();
			else
				return ((ServerVariable) obj).getValue();
		case ServerConfigurationEditor.COLUMN_DEFAULT_VALUE:
			return ((ServerVariable) obj).getDefaultValue();
		case ServerConfigurationEditor.COLUMN_NEED_RESTART:
			return ((ServerVariable) obj).isServerRestartNeeded() ? Messages
					.get().ServerVariablesLabelProvider_Yes
					: Messages.get().ServerVariablesLabelProvider_No;
		case ServerConfigurationEditor.COLUMN_DESCRIPTION:
			return ((ServerVariable) obj).getDescription();
		}
		return ""; //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.LabelProvider#getImage(java.lang.Object)
	 */
	@Override
	public Image getImage(Object obj) {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(java.lang
	 * .Object, int)
	 */
	@Override
	public Image getColumnImage(Object element, int columnIndex) {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.viewers.ITableFontProvider#getFont(java.lang.Object,
	 * int)
	 */
	@Override
	public Font getFont(Object element, int columnIndex) {
		if (((ServerVariable) element).isDefault()) {
			return null;
		}
		return textFont;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.BaseLabelProvider#dispose()
	 */
	@Override
	public void dispose() {
		textFont.dispose();
		super.dispose();
	}
}