/**
 * NetXMS - open source network management system
 * Copyright (C) 2003-2011 Victor Kirhenshtein
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
package org.netxms.ui.eclipse.console.preferencepages;

import org.eclipse.jface.action.ICoolBarManager;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;
import org.netxms.ui.eclipse.console.Activator;
import org.netxms.ui.eclipse.console.Messages;
import org.netxms.ui.eclipse.console.ServerClockContributionItem;
import org.netxms.ui.eclipse.shared.ConsoleSharedData;
import org.netxms.ui.eclipse.tools.WidgetHelper;

/**
 * "Workbench" preference page 
 */
public class WorkbenchGeneralPrefs extends PreferencePage implements	IWorkbenchPreferencePage
{
	private Button cbShowHiddenAttributes;
   private Button cbShowServerClock;
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Control createContents(Composite parent)
	{
		Composite dialogArea = new Composite(parent, SWT.NONE);
		
		GridLayout layout = new GridLayout();
		layout.verticalSpacing = WidgetHelper.OUTER_SPACING;
		dialogArea.setLayout(layout);
		
		cbShowHiddenAttributes = new Button(dialogArea, SWT.CHECK);
		cbShowHiddenAttributes.setText(Messages.get().WorkbenchGeneralPrefs_ShowHiddenAttrs);
		cbShowHiddenAttributes.setSelection(Activator.getDefault().getPreferenceStore().getBoolean("SHOW_HIDDEN_ATTRIBUTES")); //$NON-NLS-1$
		
      cbShowServerClock = new Button(dialogArea, SWT.CHECK);
      cbShowServerClock.setText(Messages.get().WorkbenchGeneralPrefs_ShowServerClock);
      cbShowServerClock.setSelection(Activator.getDefault().getPreferenceStore().getBoolean("SHOW_SERVER_CLOCK")); //$NON-NLS-1$
      
		return dialogArea;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	@Override
	public void init(IWorkbench workbench)
	{
		setPreferenceStore(PlatformUI.getPreferenceStore());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage#performDefaults()
	 */
	@Override
	protected void performDefaults()
	{
		super.performDefaults();
		cbShowHiddenAttributes.setSelection(false);
		cbShowServerClock.setSelection(false);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage#performOk()
	 */
	@Override
	public boolean performOk()
	{
		Activator.getDefault().getPreferenceStore().setValue("SHOW_HIDDEN_ATTRIBUTES", cbShowHiddenAttributes.getSelection()); //$NON-NLS-1$
      Activator.getDefault().getPreferenceStore().setValue("SHOW_SERVER_CLOCK", cbShowServerClock.getSelection()); //$NON-NLS-1$
		
      ICoolBarManager coolBar = (ICoolBarManager)ConsoleSharedData.getProperty("CoolBarManager"); //$NON-NLS-1$
      coolBar.remove(ServerClockContributionItem.ID);
		if (cbShowServerClock.getSelection())
		{
		   coolBar.add(new ServerClockContributionItem());
	      coolBar.update(true);
	      PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell().layout(true, true);
		}
		else
		{
         coolBar.update(true);
		}
		
		return super.performOk();
	}
}
