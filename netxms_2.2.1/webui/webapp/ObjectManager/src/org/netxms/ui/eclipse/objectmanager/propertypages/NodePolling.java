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
package org.netxms.ui.eclipse.objectmanager.propertypages;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.dialogs.PropertyPage;
import org.netxms.client.NXCObjectModificationData;
import org.netxms.client.NXCSession;
import org.netxms.client.constants.AgentCacheMode;
import org.netxms.client.objects.AbstractNode;
import org.netxms.ui.eclipse.jobs.ConsoleJob;
import org.netxms.ui.eclipse.objectbrowser.widgets.ObjectSelector;
import org.netxms.ui.eclipse.objectmanager.Activator;
import org.netxms.ui.eclipse.objectmanager.Messages;
import org.netxms.ui.eclipse.shared.ConsoleSharedData;
import org.netxms.ui.eclipse.tools.WidgetHelper;
import org.netxms.ui.eclipse.widgets.LabeledSpinner;

/**
 * "Polling" property page for nodes
 */
public class NodePolling extends PropertyPage
{
	private AbstractNode object;
	private ObjectSelector pollerNode;
	private Button radioIfXTableDefault;
	private Button radioIfXTableEnable;
	private Button radioIfXTableDisable;
	private Button radioAgentCacheDefault;
   private Button radioAgentCacheOn;
   private Button radioAgentCacheOff;
   private LabeledSpinner pollCount;
	private List<Button> flagButtons = new ArrayList<Button>();
	private List<Integer> flagValues = new ArrayList<Integer>();
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Control createContents(Composite parent)
	{
		Composite dialogArea = new Composite(parent, SWT.NONE);
		
		object = (AbstractNode)getElement().getAdapter(AbstractNode.class);
		
		GridLayout layout = new GridLayout();
		layout.verticalSpacing = WidgetHelper.DIALOG_SPACING;
		layout.marginWidth = 0;
		layout.marginHeight = 0;
      dialogArea.setLayout(layout);
      
      /* poller node */
      Group servicePollGroup = new Group(dialogArea, SWT.NONE);
      servicePollGroup.setText(Messages.get().NodePolling_GroupNetSrv);
		layout = new GridLayout();
		layout.horizontalSpacing = WidgetHelper.DIALOG_SPACING;
		layout.numColumns = 2;
		servicePollGroup.setLayout(layout);
		GridData gd = new GridData();
		gd.horizontalAlignment = SWT.FILL;
		gd.grabExcessHorizontalSpace = true;
		servicePollGroup.setLayoutData(gd);
		
		pollerNode = new ObjectSelector(servicePollGroup, SWT.NONE, true);
		pollerNode.setLabel(Messages.get().NodePolling_PollerNode);
		pollerNode.setObjectClass(AbstractNode.class);
		pollerNode.setEmptySelectionName(Messages.get().NodePolling_EmptySelectionServer);
		pollerNode.setObjectId(object.getPollerNodeId());
		gd = new GridData();
		gd.horizontalAlignment = SWT.FILL;
		gd.grabExcessHorizontalSpace = true;
		pollerNode.setLayoutData(gd);
		
		Label label = new Label(servicePollGroup, SWT.WRAP);
		label.setText(Messages.get().NodePolling_PollerNodeDescription);
		gd = new GridData();
		gd.widthHint = 250;
		label.setLayoutData(gd);

      /* poll count */
      if (object instanceof AbstractNode)
      {
         pollCount = new LabeledSpinner(dialogArea, SWT.NONE);
         pollCount.setLabel("Required poll count for status change");
         pollCount.setSelection(((AbstractNode)object).getRequredPollCount());
      }

		/* options */
		Group optionsGroup = new Group(dialogArea, SWT.NONE);
		optionsGroup.setText(Messages.get().NodePolling_GroupOptions);
		layout = new GridLayout();
		layout.verticalSpacing = WidgetHelper.DIALOG_SPACING;
		optionsGroup.setLayout(layout);
		gd = new GridData();
		gd.horizontalAlignment = SWT.FILL;
		gd.grabExcessHorizontalSpace = true;
		optionsGroup.setLayoutData(gd);
		
		addFlag(optionsGroup, AbstractNode.NF_DISABLE_NXCP, Messages.get().NodePolling_OptDisableAgent);
		addFlag(optionsGroup, AbstractNode.NF_DISABLE_SNMP, Messages.get().NodePolling_OptDisableSNMP);
		addFlag(optionsGroup, AbstractNode.NF_DISABLE_ICMP, Messages.get().NodePolling_OptDisableICMP);
		addFlag(optionsGroup, AbstractNode.NF_DISABLE_STATUS_POLL, Messages.get().NodePolling_OptDisableStatusPoll);
		addFlag(optionsGroup, AbstractNode.NF_DISABLE_CONF_POLL, Messages.get().NodePolling_OptDisableConfigPoll);
		addFlag(optionsGroup, AbstractNode.NF_DISABLE_ROUTE_POLL, Messages.get().NodePolling_OptDisableRTPoll);
		addFlag(optionsGroup, AbstractNode.NF_DISABLE_TOPOLOGY_POLL, Messages.get().NodePolling_OptDisableTopoPoll);
		addFlag(optionsGroup, AbstractNode.NF_DISABLE_DISCOVERY_POLL, Messages.get().NodePolling_OptDisableDiscoveryPoll);
		addFlag(optionsGroup, AbstractNode.NF_DISABLE_DATA_COLLECT, Messages.get().NodePolling_OptDisableDataCollection);
		
		/* use ifXTable */
		Group ifXTableGroup = new Group(dialogArea, SWT.NONE);
		ifXTableGroup.setText(Messages.get().NodePolling_GroupIfXTable);
		layout = new GridLayout();
		layout.horizontalSpacing = WidgetHelper.DIALOG_SPACING;
		layout.numColumns = 3;
		layout.makeColumnsEqualWidth = true;
		ifXTableGroup.setLayout(layout);
		gd = new GridData();
		gd.horizontalAlignment = SWT.FILL;
		gd.grabExcessHorizontalSpace = true;
		ifXTableGroup.setLayoutData(gd);
		
		radioIfXTableDefault = new Button(ifXTableGroup, SWT.RADIO);
		radioIfXTableDefault.setText(Messages.get().NodePolling_Default);
		radioIfXTableDefault.setSelection(object.getIfXTablePolicy() == AbstractNode.IFXTABLE_DEFAULT);

		radioIfXTableEnable = new Button(ifXTableGroup, SWT.RADIO);
		radioIfXTableEnable.setText(Messages.get().NodePolling_Enable);
		radioIfXTableEnable.setSelection(object.getIfXTablePolicy() == AbstractNode.IFXTABLE_ENABLED);

		radioIfXTableDisable = new Button(ifXTableGroup, SWT.RADIO);
		radioIfXTableDisable.setText(Messages.get().NodePolling_Disable);
		radioIfXTableDisable.setSelection(object.getIfXTablePolicy() == AbstractNode.IFXTABLE_DISABLED);

      /* agent cache */
      Group agentCacheGroup = new Group(dialogArea, SWT.NONE);
      agentCacheGroup.setText(Messages.get().NodePolling_AgentCacheMode);
      layout = new GridLayout();
      layout.horizontalSpacing = WidgetHelper.DIALOG_SPACING;
      layout.numColumns = 3;
      layout.makeColumnsEqualWidth = true;
      agentCacheGroup.setLayout(layout);
      gd = new GridData();
      gd.horizontalAlignment = SWT.FILL;
      gd.grabExcessHorizontalSpace = true;
      agentCacheGroup.setLayoutData(gd);
      
      radioAgentCacheDefault = new Button(agentCacheGroup, SWT.RADIO);
      radioAgentCacheDefault.setText(Messages.get().NodePolling_Default);
      radioAgentCacheDefault.setSelection(object.getAgentCacheMode() == AgentCacheMode.DEFAULT);

      radioAgentCacheOn = new Button(agentCacheGroup, SWT.RADIO);
      radioAgentCacheOn.setText(Messages.get().NodePolling_On);
      radioAgentCacheOn.setSelection(object.getAgentCacheMode() == AgentCacheMode.ON);

      radioAgentCacheOff = new Button(agentCacheGroup, SWT.RADIO);
      radioAgentCacheOff.setText(Messages.get().NodePolling_Off);
      radioAgentCacheOff.setSelection(object.getAgentCacheMode() == AgentCacheMode.OFF);
      
      return dialogArea;
   }

	/**
	 * Add checkbox for flag
	 * 
	 * @param value
	 * @param name
	 */
	private void addFlag(Composite parent, int value, String name)
	{
		final Button button = new Button(parent, SWT.CHECK);
		button.setText(name);
		button.setSelection((object.getFlags() & value) != 0);
		flagButtons.add(button);
		flagValues.add(value);
	}
	
	/**
	 * Collect new node flags from checkboxes
	 *  
	 * @return new node flags
	 */
	private int collectNodeFlags()
	{
		int flags = object.getFlags();
		for(int i = 0; i < flagButtons.size(); i++)
		{
			if (flagButtons.get(i).getSelection())
			{
				flags |= flagValues.get(i);
			}
			else
			{
				flags &= ~flagValues.get(i);
			}
		}
		return flags;
	}

   /**
    * Collect mask for flags being modified
    * 
    * @return
    */
   private int collectNodeFlagsMask()
   {
      int mask = 0;
      for(int i = 0; i < flagButtons.size(); i++)
      {
         mask |= flagValues.get(i);
      }
      return mask;
   }
	
	/**
	 * Collect ifXTabe usage policy from radio buttons
	 * 
	 * @return
	 */
	private int collectIfXTablePolicy()
	{
		if (radioIfXTableEnable.getSelection())
			return AbstractNode.IFXTABLE_ENABLED;
		if (radioIfXTableDisable.getSelection())
			return AbstractNode.IFXTABLE_DISABLED;
		return AbstractNode.IFXTABLE_DEFAULT;
	}

   /**
    * Collect agent cache mode from radio buttons
    * 
    * @return
    */
   private AgentCacheMode collectAgentCacheMode()
   {
      if (radioAgentCacheOn.getSelection())
         return AgentCacheMode.ON;
      if (radioAgentCacheOff.getSelection())
         return AgentCacheMode.OFF;
      return AgentCacheMode.DEFAULT;
   }

	/**
	 * Apply changes
	 * 
	 * @param isApply true if update operation caused by "Apply" button
	 */
	protected boolean applyChanges(final boolean isApply)
	{
		final NXCObjectModificationData md = new NXCObjectModificationData(object.getObjectId());
		
		md.setPollerNode(pollerNode.getObjectId());
		md.setObjectFlags(collectNodeFlags(), collectNodeFlagsMask());
		md.setIfXTablePolicy(collectIfXTablePolicy());
		md.setAgentCacheMode(collectAgentCacheMode());
      if (object instanceof AbstractNode)
         md.setRequiredPolls(pollCount.getSelection());
		
		if (isApply)
			setValid(false);
		
		final NXCSession session = (NXCSession)ConsoleSharedData.getSession();
		new ConsoleJob(Messages.get().NodePolling_JobName, null, Activator.PLUGIN_ID, null) {
			@Override
			protected void runInternal(IProgressMonitor monitor) throws Exception
			{
				session.modifyObject(md);
			}

			@Override
			protected String getErrorMessage()
			{
				return Messages.get().NodePolling_JobError;
			}

			@Override
			protected void jobFinalize()
			{
				if (isApply)
				{
					runInUIThread(new Runnable() {
						@Override
						public void run()
						{
							NodePolling.this.setValid(true);
						}
					});
				}
			}
		}.start();
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

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage#performOk()
	 */
	@Override
	public boolean performOk()
	{
		return applyChanges(false);
	}

   /* (non-Javadoc)
    * @see org.eclipse.jface.preference.PreferencePage#performDefaults()
    */
   @Override
   protected void performDefaults()
   {
      super.performDefaults();
      pollerNode.setObjectId(0);
      radioIfXTableDefault.setSelection(true);
      radioIfXTableDisable.setSelection(false);
      radioIfXTableEnable.setSelection(false);
      for(Button b : flagButtons)
         b.setSelection(false);
   }
}
