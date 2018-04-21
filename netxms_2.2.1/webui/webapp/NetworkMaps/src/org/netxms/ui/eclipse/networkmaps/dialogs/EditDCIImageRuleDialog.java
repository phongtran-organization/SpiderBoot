/**
 * NetXMS - open source network management system
 * Copyright (C) 2003-2014 Victor Kirhenshtein
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
package org.netxms.ui.eclipse.networkmaps.dialogs;

import java.util.UUID;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.netxms.base.NXCommon;
import org.netxms.client.maps.configs.DCIImageRule;
import org.netxms.ui.eclipse.imagelibrary.widgets.ImageSelector;
import org.netxms.ui.eclipse.networkmaps.Messages;
import org.netxms.ui.eclipse.tools.MessageDialogHelper;
import org.netxms.ui.eclipse.tools.WidgetHelper;

/**
 * Threshold configuration dialog
 *
 */
public class EditDCIImageRuleDialog extends Dialog
{
	private DCIImageRule rule;
	private Combo operation;
	private Text value;
   private Text comment;
   private UUID selectedImage;
	private Group conditionGroup;
   private ImageSelector image;
	
	/**
	 * @param parentShell
	 * @param rule
	 */
	public EditDCIImageRuleDialog(Shell parentShell, DCIImageRule rule)
	{
		super(parentShell);
		this.rule = rule;
		setShellStyle(getShellStyle() | SWT.RESIZE);
	}

   /* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Control createDialogArea(final Composite parent)
	{
	   Composite dialogArea = new Composite(parent, SWT.NONE);
      
      GridLayout layout = new GridLayout();
      layout.numColumns = 1;
      layout.makeColumnsEqualWidth = true;
      dialogArea.setLayout(layout);
      
   // Condition area
      conditionGroup = new Group(dialogArea, SWT.NONE);
      conditionGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
      conditionGroup.setText(Messages.get().EditDCIImageRuleDialog_Condition);
      
      GridLayout condLayout = new GridLayout();
      condLayout.numColumns = 2;
      conditionGroup.setLayout(condLayout);
		
	   operation = WidgetHelper.createLabeledCombo(conditionGroup, SWT.BORDER | SWT.DROP_DOWN | SWT.READ_ONLY, Messages.get().EditDCIImageRuleDialog_Operation, WidgetHelper.DEFAULT_LAYOUT_DATA);
      operation.add(Messages.get().EditDCIImageRuleDialog_OpLess);
      operation.add(Messages.get().EditDCIImageRuleDialog_OpLessEq);
      operation.add(Messages.get().EditDCIImageRuleDialog_OpEq);
      operation.add(Messages.get().EditDCIImageRuleDialog_OpGtEq);
      operation.add(Messages.get().EditDCIImageRuleDialog_OpGt);
      operation.add(Messages.get().EditDCIImageRuleDialog_OpNe);
      operation.add(Messages.get().EditDCIImageRuleDialog_OpLike);
      operation.add(Messages.get().EditDCIImageRuleDialog_OpNotLike);
      operation.select((rule.getComparisonType() != -1) ? rule.getComparisonType() : 0);
      
      value = WidgetHelper.createLabeledText(conditionGroup, SWT.BORDER, 120, Messages.get().EditDCIImageRuleDialog_Value, 
            rule.getCompareValue(), WidgetHelper.DEFAULT_LAYOUT_DATA);
      
      comment = WidgetHelper.createLabeledText(dialogArea, SWT.BORDER, 120, Messages.get().EditDCIImageRuleDialog_Comment, 
            rule.getComment(), WidgetHelper.DEFAULT_LAYOUT_DATA);
      
      image = new ImageSelector(dialogArea, SWT.NONE);
      image.setLabel(Messages.get().EditDCIImageRuleDialog_Image);
      GridData gd = new GridData();
      gd.horizontalAlignment = SWT.FILL;
      gd.grabExcessHorizontalSpace = true;
      image.setLayoutData(gd);
      try
      {
         selectedImage = rule.getImage();
         image.setImageGuid(selectedImage, true);
      }
      catch (Exception e) 
      {
      }
      
      image.addModifyListener(new ModifyListener() {
         
         @Override
         public void modifyText(ModifyEvent e)
         {
            getShell().setSize(getShell().computeSize(SWT.DEFAULT, SWT.DEFAULT, true));
            getShell().layout(true, true);
         }
      });
      
		return dialogArea;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
	 */
	@Override
	protected void configureShell(Shell newShell)
	{
		super.configureShell(newShell);
		newShell.setText(Messages.get().EditDCIImageRuleDialog_EditImageRule);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#okPressed()
	 */
	@Override
	protected void okPressed()
	{
	   selectedImage = image.getImageGuid();
	   if(selectedImage == null || selectedImage == NXCommon.EMPTY_GUID)
      {
         MessageDialogHelper.openError(getShell(), Messages.get().EditDCIImageRuleDialog_Error, Messages.get().EditDCIImageRuleDialog_ImageShouldBeSelected);
         return;
      }
	   
	   String compareValue = value.getText();
	   if(compareValue == null || compareValue.isEmpty())
      {
         MessageDialogHelper.openError(getShell(), Messages.get().EditDCIImageRuleDialog_Error, Messages.get().EditDCIImageRuleDialog_ValueShouldNotBeEmpty);
         return;
      }
	   
	   rule.setImage(selectedImage);
      rule.setComparisonType(operation.getSelectionIndex());
	   rule.setCompareValue(compareValue);
	   rule.setComment(comment.getText());
				
		super.okPressed();
	}
}
