/**
 * NetXMS - open source network management system
 * Copyright (C) 2003-2012 Victor Kirhenshtein
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
package org.netxms.ui.eclipse.filemanager.widgets;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.netxms.ui.eclipse.filemanager.Messages;
import org.netxms.ui.eclipse.widgets.AbstractSelector;

/**
 * Local file selector
 */
public class LocalFileSelector extends AbstractSelector {
	private List<File> fileList = new ArrayList<File>();
	private String[] filterExtensions = { "*" }; //$NON-NLS-1$
	private String[] filterNames = { Messages.get().LocalFileSelector_AllFiles };
	private int selectorType;

	/**
	 * @param parent
	 * @param style
	 * @param selectorType
	 *            Selector type: SWT.OPEN or SWT.SAVE
	 */
	public LocalFileSelector(Composite parent, int style, boolean useHyperlink,
			int selectorType) {
		super(parent, style, USE_TEXT | (useHyperlink ? USE_HYPERLINK : 0));

		this.selectorType = selectorType;

		setText(Messages.get().LocalFileSelector_None);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.netxms.ui.eclipse.widgets.AbstractSelector#selectionButtonHandler()
	 */
	@Override
	protected void selectionButtonHandler() {
		FileDialog fd = new FileDialog(getShell(), selectorType);
		fd.setText(Messages.get().LocalFileSelector_SelectFile);
		fd.setFilterExtensions(filterExtensions);
		fd.setFilterNames(filterNames);
		fd.open();
		String files[] = fd.getFileNames();
		if (files.length > 0) {
			fileList.clear();
			for (int i = 0; i < files.length; i++) {
				fileList.add(new File(fd.getFilterPath(), files[i]));
			}
			updateFileList();
		} else {
			fileList.clear();
			updateFileList();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.netxms.ui.eclipse.widgets.AbstractSelector#getSelectionButtonToolTip
	 * ()
	 */
	@Override
	protected String getSelectionButtonToolTip() {
		return Messages.get().LocalFileSelector_Tooltip;
	}

	/**
	 * @return the file
	 */
	public File getFile() {
		if (fileList.size() > 0) {
			return fileList.get(0);
		} else {
			return null;
		}
	}

	public List<File> getFileList() {
		return fileList;
	}

	/**
	 * @param file
	 *            the file to set
	 */
	public void setFile(File file) {
		fileList.clear();
		fileList.add(file);
		updateFileList();
	}

	private void updateFileList() {
		StringBuilder fileListString = new StringBuilder();
		for (int i = 0; i < fileList.size(); i++) {
			fileListString.append(fileList.get(i).getName());
			if (i != (fileList.size() - 1)) {
				fileListString.append(", "); //$NON-NLS-1$
			}
		}

		if (fileList.size() == 0)
			fileListString.append(Messages.get().LocalFileSelector_None);

		setText(fileListString.toString());
		fireModifyListeners();
	}

	/**
	 * @return the filterExtensions
	 */
	public String[] getFilterExtensions() {
		return filterExtensions;
	}

	/**
	 * @param filterExtensions
	 *            the filterExtensions to set
	 */
	public void setFilterExtensions(String[] filterExtensions) {
		this.filterExtensions = filterExtensions;
	}

	/**
	 * @return the filterNames
	 */
	public String[] getFilterNames() {
		return filterNames;
	}

	/**
	 * @param filterNames
	 *            the filterNames to set
	 */
	public void setFilterNames(String[] filterNames) {
		this.filterNames = filterNames;
	}
}
