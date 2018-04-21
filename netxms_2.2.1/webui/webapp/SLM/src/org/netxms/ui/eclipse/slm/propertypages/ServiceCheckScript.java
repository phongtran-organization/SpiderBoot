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
package org.netxms.ui.eclipse.slm.propertypages;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.dialogs.PropertyPage;
import org.netxms.client.NXCObjectModificationData;
import org.netxms.client.NXCSession;
import org.netxms.client.objects.ServiceCheck;
import org.netxms.ui.eclipse.jobs.ConsoleJob;
import org.netxms.ui.eclipse.nxsl.widgets.ScriptEditor;
import org.netxms.ui.eclipse.shared.ConsoleSharedData;
import org.netxms.ui.eclipse.slm.Activator;
import org.netxms.ui.eclipse.slm.Messages;
import org.netxms.ui.eclipse.tools.WidgetHelper;

/**
 * "Script" property page for condition object
 *
 */
public class ServiceCheckScript extends PropertyPage
{
	private ServiceCheck object;
	private ScriptEditor filterSource;
	private String initialScript;

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Control createContents(Composite parent)
	{
		Composite dialogArea = new Composite(parent, SWT.NONE);
		
		object = (ServiceCheck)getElement().getAdapter(ServiceCheck.class);
		if (object == null)	// Paranoid check
			return dialogArea;
		
		initialScript = object.getScript();
		
		GridLayout layout = new GridLayout();
		layout.verticalSpacing = WidgetHelper.INNER_SPACING;
		layout.marginWidth = 0;
		layout.marginHeight = 0;
      dialogArea.setLayout(layout);
      
      // Script
      Label label = new Label(dialogArea, SWT.NONE);
      label.setText(Messages.get().ServiceCheckScript_CheckScript);

      filterSource = new ScriptEditor(dialogArea, SWT.BORDER, SWT.H_SCROLL | SWT.V_SCROLL, false, "Variables:\r\n\t$node\tnode object for node links, null for other checks\r\n\r\nReturn value: OK/FAIL to indicate check result.");
		filterSource.setText(object.getScript());
		
		GridData gd = new GridData();
		gd.grabExcessHorizontalSpace = true;
		gd.grabExcessVerticalSpace = true;
		gd.horizontalAlignment = SWT.FILL;
		gd.verticalAlignment = SWT.FILL;
		gd.widthHint = 0;
      gd.heightHint = 0;
		filterSource.setLayoutData(gd);
		
		return dialogArea;
	}
	
	/**
	 * Apply changes
	 * 
	 * @param isApply true if update operation caused by "Apply" button
	 */
	protected void applyChanges(final boolean isApply)
	{
		if (initialScript.equals(filterSource.getText()))
			return;		// Nothing to apply
		
		if (isApply)
			setValid(false);
		
		final String newScript = filterSource.getText();
		final NXCSession session = (NXCSession)ConsoleSharedData.getSession();
		new ConsoleJob(Messages.get().ServiceCheckScript_JobTitle, null, Activator.PLUGIN_ID, null) {
			@Override
			protected void runInternal(IProgressMonitor monitor) throws Exception
			{
				NXCObjectModificationData md = new NXCObjectModificationData(object.getObjectId());
				md.setScript(newScript);
				session.modifyObject(md);
				initialScript = newScript;
			}

			@Override
			protected String getErrorMessage()
			{
				return Messages.get().ServiceCheckScript_JobError;
			}

			@Override
			protected void jobFinalize()
			{
				if (isApply)
				{
					getDisplay().asyncExec(new Runnable() {
						@Override
						public void run()
						{
							ServiceCheckScript.this.setValid(true);
						}
					});
				}
			}
		}.start();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage#performOk()
	 */
	@Override
	public boolean performOk()
	{
		applyChanges(false);
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage#performApply()
	 */
	@Override
	protected void performApply()
	{
		applyChanges(true);
	}
}
