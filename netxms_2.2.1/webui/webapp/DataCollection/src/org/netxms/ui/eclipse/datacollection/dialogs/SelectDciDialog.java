/**
 * NetXMS - open source network management system
 * Copyright (C) 2003-2013 Victor Kirhenshtein
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
package org.netxms.ui.eclipse.datacollection.dialogs;

import java.util.List;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.netxms.client.NXCSession;
import org.netxms.client.datacollection.DciValue;
import org.netxms.client.objects.AbstractNode;
import org.netxms.client.objects.AbstractObject;
import org.netxms.client.objects.Cluster;
import org.netxms.client.objects.MobileDevice;
import org.netxms.client.objects.Template;
import org.netxms.ui.eclipse.datacollection.Activator;
import org.netxms.ui.eclipse.datacollection.Messages;
import org.netxms.ui.eclipse.datacollection.widgets.DciList;
import org.netxms.ui.eclipse.objectbrowser.dialogs.ObjectSelectionDialog;
import org.netxms.ui.eclipse.objectbrowser.widgets.ObjectTree;
import org.netxms.ui.eclipse.shared.ConsoleSharedData;
import org.netxms.ui.eclipse.tools.MessageDialogHelper;

/**
 * Dialog for DCI selection
 */
public class SelectDciDialog extends Dialog
{
	private SashForm splitter;
	private ObjectTree objectTree;
	private DciList dciList;
	private List<DciValue> selection;
	private int dcObjectType = -1;
	private long fixedNode;
	private boolean enableEmptySelection = false;
	private boolean allowTemplateItems = false;
	private boolean allowSingleSelection = false;
	private boolean allowNoValueObjects = false;
	
	/**
	 * @param parentShell
	 */
	public SelectDciDialog(Shell parentShell, long fixedNode)
	{
		super(parentShell);
		setShellStyle(getShellStyle() | SWT.RESIZE);
		this.fixedNode = fixedNode;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
	 */
	@Override
	protected void configureShell(Shell newShell)
	{
		super.configureShell(newShell);
		newShell.setText(Messages.get().SelectDciDialog_Title);
		IDialogSettings settings = Activator.getDefault().getDialogSettings();
		try
		{
			newShell.setSize(settings.getInt("SelectDciDialog.width"), settings.getInt("SelectDciDialog.hight")); //$NON-NLS-1$ //$NON-NLS-2$
			newShell.setLocation(settings.getInt("SelectDciDialog.cx"), settings.getInt("SelectDciDialog.cy")); //$NON-NLS-1$ //$NON-NLS-2$
		}
		catch(NumberFormatException e)
		{
			newShell.setSize(600, 350);
         newShell.setLocation(100, 100);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#createButtonsForButtonBar(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent)
	{
		if (enableEmptySelection)
		{
			Button button = createButton(parent, 1000, Messages.get().SelectDciDialog_None, false);
			button.addSelectionListener(new SelectionListener() {
				@Override
				public void widgetSelected(SelectionEvent e)
				{
					selection = null;
					saveSettings();
					SelectDciDialog.super.okPressed();
				}
				
				@Override
				public void widgetDefaultSelected(SelectionEvent e)
				{
					widgetSelected(e);
				}
			});
		}		
		super.createButtonsForButtonBar(parent);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Control createDialogArea(Composite parent)
	{
		IDialogSettings settings = Activator.getDefault().getDialogSettings();
		
		Composite dialogArea = (Composite)super.createDialogArea(parent);
		dialogArea.setLayout(new FillLayout());
		
		if (fixedNode == 0)
		{
			splitter = new SashForm(dialogArea, SWT.HORIZONTAL);
			
			objectTree = new ObjectTree(splitter, SWT.BORDER, ObjectTree.NONE, null, ObjectSelectionDialog.createNodeSelectionFilter(true), true, false);
			String text = settings.get("SelectDciDialog.Filter"); //$NON-NLS-1$
			if (text != null)
				objectTree.setFilter(text);
		}

      dciList = new DciList(null, (fixedNode == 0) ? splitter : dialogArea, SWT.BORDER, null,
            "SelectDciDialog.dciList", dcObjectType, allowSingleSelection ? SWT.NONE : SWT.MULTI, allowNoValueObjects); //$NON-NLS-1$
		dciList.setDcObjectType(dcObjectType);
		dciList.addDoubleClickListener(new IDoubleClickListener() {
			@Override
			public void doubleClick(DoubleClickEvent event)
			{
				okPressed();
			}
		});

		if (fixedNode == 0)
		{
			try
			{
				int[] weights = new int[2];
				weights[0] = settings.getInt("SelectDciDialog.weight1"); //$NON-NLS-1$
				weights[1] = settings.getInt("SelectDciDialog.weight2"); //$NON-NLS-1$
				splitter.setWeights(weights);
			}
			catch(NumberFormatException e)
			{
				splitter.setWeights(new int[] { 30, 70 });
			}
			
			objectTree.getTreeViewer().addSelectionChangedListener(new ISelectionChangedListener()	{
				@Override
				public void selectionChanged(SelectionChangedEvent event)
				{
					AbstractObject object = objectTree.getFirstSelectedObject2();
					if ((object != null) && 
					    ((object instanceof AbstractNode) || (object instanceof MobileDevice) || (object instanceof Cluster) ||
					     (allowTemplateItems && (object instanceof Template))))
					{
						dciList.setNode(object);
					}
					else
					{
						dciList.setNode(null);
					}
				}
			});
			
			objectTree.setFocus();
		}
		else
		{
			dciList.setNode(((NXCSession)ConsoleSharedData.getSession()).findObjectById(fixedNode));
		}
		
		return dialogArea;
	}

	/**
	 * Save dialog settings
	 */
	private void saveSettings()
	{
		Point size = getShell().getSize();
		Point pleace = getShell().getLocation();
		IDialogSettings settings = Activator.getDefault().getDialogSettings();

		settings.put("SelectDciDialog.cx", pleace.x); //$NON-NLS-1$
		settings.put("SelectDciDialog.cy", pleace.y); //$NON-NLS-1$
		settings.put("SelectDciDialog.width", size.x); //$NON-NLS-1$
      settings.put("SelectDciDialog.hight", size.y); //$NON-NLS-1$
		if (fixedNode == 0)
		{
			settings.put("SelectDciDialog.Filter", objectTree.getFilter()); //$NON-NLS-1$
			
			int[] weights = splitter.getWeights();
			settings.put("SelectDciDialog.weight1", weights[0]); //$NON-NLS-1$
			settings.put("SelectDciDialog.weight2", weights[1]); //$NON-NLS-1$
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#cancelPressed()
	 */
	@Override
	protected void cancelPressed()
	{
		saveSettings();
		super.cancelPressed();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#okPressed()
	 */
	@Override
	protected void okPressed()
	{
		selection = dciList.getSelection();
		if (selection == null || selection.size() == 0)
		{
			MessageDialogHelper.openWarning(getShell(), Messages.get().SelectDciDialog_Warning, Messages.get().SelectDciDialog_WarningMessage);
			return;
		}
		saveSettings();
		super.okPressed();
	}

	/**
	 * @return the selection
	 */
	public List<DciValue> getSelection()
	{
		return selection;
	}

	/**
	 * @return the dcObjectType
	 */
	public int getDcObjectType()
	{
		return dcObjectType;
	}

	/**
	 * @param dcObjectType the dcObjectType to set
	 */
	public void setDcObjectType(int dcObjectType)
	{
		this.dcObjectType = dcObjectType;
		if (dciList != null)
			dciList.setDcObjectType(dcObjectType);
	}

	/**
	 * @return the enableEmptySelection
	 */
	public final boolean isEnableEmptySelection()
	{
		return enableEmptySelection;
	}

	/**
	 * @param enableEmptySelection the enableEmptySelection to set
	 */
	public final void setEnableEmptySelection(boolean enableEmptySelection)
	{
		this.enableEmptySelection = enableEmptySelection;
	}

	/**
	 * @return the allowTemplateItems
	 */
	public final boolean isAllowTemplateItems()
	{
		return allowTemplateItems;
	}

	/**
	 * @param allowTemplateItems the allowTemplateItems to set
	 */
	public final void setAllowTemplateItems(boolean allowTemplateItems)
	{
		this.allowTemplateItems = allowTemplateItems;
	}

	/**
    * @param allowSingleSelection false to have multiple selection, true to have single selection
    * in DCI list. 
    * By default multiple selection is set. 
    */
   public void setSingleSelection(boolean allowSingleSelection)
   {
      this.allowSingleSelection = allowSingleSelection;
   }

   /**
    * @param allowNoValueObjects the allowNoValueObjects to set
    */
   public void setAllowNoValueObjects(boolean allowNoValueObjects)
   {
      this.allowNoValueObjects = allowNoValueObjects;
   }
}
