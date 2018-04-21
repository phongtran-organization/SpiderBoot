/**
 * NetXMS - open source network management system
 * Copyright (C) 2003-2017 Raden Solutions
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
package org.netxms.ui.eclipse.datacollection.propertypages;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;
import org.netxms.client.NXCSession;
import org.netxms.client.constants.AgentCacheMode;
import org.netxms.client.datacollection.DataCollectionItem;
import org.netxms.client.datacollection.DataCollectionObject;
import org.netxms.client.objects.Node;
import org.netxms.client.snmp.SnmpObjectId;
import org.netxms.client.snmp.SnmpObjectIdFormatException;
import org.netxms.ui.eclipse.datacollection.Messages;
import org.netxms.ui.eclipse.datacollection.dialogs.IParameterSelectionDialog;
import org.netxms.ui.eclipse.datacollection.dialogs.SelectAgentParamDlg;
import org.netxms.ui.eclipse.datacollection.dialogs.SelectInternalParamDlg;
import org.netxms.ui.eclipse.datacollection.dialogs.SelectParameterScriptDialog;
import org.netxms.ui.eclipse.datacollection.dialogs.SelectSnmpParamDlg;
import org.netxms.ui.eclipse.datacollection.dialogs.WinPerfCounterSelectionDialog;
import org.netxms.ui.eclipse.datacollection.propertypages.helpers.DCIPropertyPageDialog;
import org.netxms.ui.eclipse.objectbrowser.widgets.ObjectSelector;
import org.netxms.ui.eclipse.shared.ConsoleSharedData;
import org.netxms.ui.eclipse.tools.WidgetHelper;
import org.netxms.ui.eclipse.widgets.LabeledSpinner;
import org.netxms.ui.eclipse.widgets.LabeledText;

/**
 * "General" property page for DCI
 */
public class General extends DCIPropertyPageDialog
{
	private static final String[] snmpRawTypes = 
	{ 
		Messages.get().General_SNMP_DT_None, 
		Messages.get().General_SNMP_DT_int32, 
		Messages.get().General_SNMP_DT_uint32,
		Messages.get().General_SNMP_DT_int64, 
		Messages.get().General_SNMP_DT_uint64,
		Messages.get().General_SNMP_DT_float, 
		Messages.get().General_SNMP_DT_ipAddr,
		Messages.get().General_SNMP_DT_macAddr
	};
	
	private DataCollectionItem dci;
	private Text description;
	private LabeledText parameter;
	private Button selectButton;
	private Combo origin;
	private Combo dataType;
	private Button checkInterpretRawSnmpValue;
	private Combo snmpRawType;
	private Button checkUseCustomSnmpPort;
	private Spinner customSnmpPort;
	private ObjectSelector sourceNode;
	private Combo agentCacheMode;
	private Combo schedulingMode;
	private Combo retentionMode;
	private LabeledSpinner pollingInterval;
	private LabeledSpinner retentionTime;
	private LabeledSpinner sampleCount;
	private Button statusActive;
	private Button statusDisabled;
	private Button statusUnsupported;
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Control createContents(Composite parent)
	{		
	   Composite dialogArea = (Composite)super.createContents(parent);
		dci = editor.getObjectAsItem();
		
		GridLayout layout = new GridLayout();
		layout.verticalSpacing = WidgetHelper.OUTER_SPACING;
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		layout.numColumns = 2;
      dialogArea.setLayout(layout);
      
      /** description area **/
      Group groupDescription = new Group(dialogArea, SWT.NONE);
      groupDescription.setText(Messages.get().General_Description);
      FillLayout descriptionLayout = new FillLayout();
      descriptionLayout.marginWidth = WidgetHelper.OUTER_SPACING;
      descriptionLayout.marginHeight = WidgetHelper.OUTER_SPACING;
      groupDescription.setLayout(descriptionLayout);
      description = new Text(groupDescription, SWT.BORDER);
      description.setTextLimit(255);
      description.setText(dci.getDescription());
      GridData gd = new GridData();
      gd.grabExcessHorizontalSpace = true;
      gd.horizontalAlignment = SWT.FILL;
      gd.horizontalSpan = 2;
      groupDescription.setLayoutData(gd);
      
      /** data area **/
      Group groupData = new Group(dialogArea, SWT.NONE);
      groupData.setText(Messages.get().General_Data);
      FormLayout dataLayout = new FormLayout();
      dataLayout.marginHeight = WidgetHelper.OUTER_SPACING;
      dataLayout.marginWidth = WidgetHelper.OUTER_SPACING;
      groupData.setLayout(dataLayout);
      gd = new GridData();
      gd.grabExcessHorizontalSpace = true;
      gd.horizontalAlignment = SWT.FILL;
      gd.horizontalSpan = 2;
      groupData.setLayoutData(gd);
      
      parameter = new LabeledText(groupData, SWT.NONE);
      parameter.setLabel(Messages.get().General_Parameter);
      parameter.getTextControl().setTextLimit(255);
      parameter.setText(dci.getName());
      
      selectButton = new Button(groupData, SWT.PUSH);
      selectButton.setText(Messages.get().General_Select);
      selectButton.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetDefaultSelected(SelectionEvent e)
			{
				widgetSelected(e);
			}

			@Override
			public void widgetSelected(SelectionEvent e)
			{
				selectParameter();
			}
      });

      FormData fd = new FormData();
      fd.left = new FormAttachment(0, 0);
      fd.top = new FormAttachment(0, 0);
      fd.right = new FormAttachment(selectButton, -WidgetHelper.INNER_SPACING, SWT.LEFT);
      parameter.setLayoutData(fd);

      fd = new FormData();
      fd.right = new FormAttachment(100, 0);
      fd.bottom = new FormAttachment(parameter, 0, SWT.BOTTOM);
      fd.width = WidgetHelper.BUTTON_WIDTH_HINT;
      selectButton.setLayoutData(fd);
      
      fd = new FormData();
      fd.left = new FormAttachment(0, 0);
      fd.top = new FormAttachment(parameter, WidgetHelper.OUTER_SPACING, SWT.BOTTOM);
      fd.right = new FormAttachment(50, -WidgetHelper.OUTER_SPACING / 2);
      origin = WidgetHelper.createLabeledCombo(groupData, SWT.READ_ONLY, Messages.get().General_Origin, fd);
      origin.add(Messages.get().General_SourceInternal);
      origin.add(Messages.get().General_SourceAgent);
      origin.add(Messages.get().General_SourceSNMP);
      origin.add(Messages.get().General_SourceCPSNMP);
      origin.add(Messages.get().General_SourcePush);
      origin.add(Messages.get().General_WinPerf);
      origin.add(Messages.get().General_SMCLP);
      origin.add(Messages.get().General_Script);
      origin.add(Messages.get().General_SourceSSH);
      origin.add(Messages.get().General_SourceMQTT);
      origin.select(dci.getOrigin());
      origin.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetDefaultSelected(SelectionEvent e)
			{
				widgetSelected(e);
			}

			@Override
			public void widgetSelected(SelectionEvent e)
			{
				onOriginChange();
			}
      });
      
      fd = new FormData();
      fd.left = new FormAttachment(50, WidgetHelper.OUTER_SPACING / 2);
      fd.top = new FormAttachment(parameter, WidgetHelper.OUTER_SPACING, SWT.BOTTOM);
      fd.right = new FormAttachment(100, 0);
      dataType = WidgetHelper.createLabeledCombo(groupData, SWT.READ_ONLY, Messages.get().General_DataType, fd);
      dataType.add(Messages.get().General_DT_int32);
      dataType.add(Messages.get().General_DT_uint32);
      dataType.add(Messages.get().General_DT_int64);
      dataType.add(Messages.get().General_DT_uint64);
      dataType.add(Messages.get().General_DT_string);
      dataType.add(Messages.get().General_DT_float);
      dataType.select(dci.getDataType());

      checkInterpretRawSnmpValue = new Button(groupData, SWT.CHECK);
      checkInterpretRawSnmpValue.setText(Messages.get().General_InterpretRawValue);
      checkInterpretRawSnmpValue.setSelection(dci.isSnmpRawValueInOctetString());
      checkInterpretRawSnmpValue.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e)
			{
		      snmpRawType.setEnabled(checkInterpretRawSnmpValue.getSelection());
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e)
			{
				widgetSelected(e);
			}
		});
      fd = new FormData();
      fd.left = new FormAttachment(0, 0);
      fd.top = new FormAttachment(origin.getParent(), WidgetHelper.OUTER_SPACING, SWT.BOTTOM);
      checkInterpretRawSnmpValue.setLayoutData(fd);
      checkInterpretRawSnmpValue.setEnabled(dci.getOrigin() == DataCollectionItem.SNMP);

      snmpRawType = new Combo(groupData, SWT.BORDER | SWT.READ_ONLY);
      for(int i = 0; i < snmpRawTypes.length; i++)
      	snmpRawType.add(snmpRawTypes[i]);
      snmpRawType.select(dci.getSnmpRawValueType());
      snmpRawType.setEnabled((dci.getOrigin() == DataCollectionItem.SNMP) && dci.isSnmpRawValueInOctetString());
      fd = new FormData();
      fd.left = new FormAttachment(0, 0);
      fd.top = new FormAttachment(checkInterpretRawSnmpValue, WidgetHelper.OUTER_SPACING, SWT.BOTTOM);
      fd.right = new FormAttachment(checkInterpretRawSnmpValue, 0, SWT.RIGHT);
      snmpRawType.setLayoutData(fd);
      
      checkUseCustomSnmpPort = new Button(groupData, SWT.CHECK);
      checkUseCustomSnmpPort.setText(Messages.get().General_UseCustomPort);
      checkUseCustomSnmpPort.setSelection(dci.getSnmpPort() != 0);
      checkUseCustomSnmpPort.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e)
			{
		      customSnmpPort.setEnabled(checkUseCustomSnmpPort.getSelection());
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e)
			{
				widgetSelected(e);
			}
		});
      fd = new FormData();
      fd.left = new FormAttachment(checkInterpretRawSnmpValue, WidgetHelper.OUTER_SPACING, SWT.RIGHT);
      fd.right = new FormAttachment(100, 0);
      fd.top = new FormAttachment(dataType.getParent(), WidgetHelper.OUTER_SPACING, SWT.BOTTOM);
      checkUseCustomSnmpPort.setLayoutData(fd);
      checkUseCustomSnmpPort.setEnabled(dci.getOrigin() == DataCollectionItem.SNMP);

      customSnmpPort = new Spinner(groupData, SWT.BORDER);
      customSnmpPort.setMinimum(1);
      customSnmpPort.setMaximum(65535);
      if ((dci.getOrigin() == DataCollectionItem.SNMP) && (dci.getSnmpPort() != 0))
      {
      	customSnmpPort.setEnabled(true);
      	customSnmpPort.setSelection(dci.getSnmpPort());
      }
      else
      {
      	customSnmpPort.setEnabled(false);
      }
      fd = new FormData();
      fd.left = new FormAttachment(checkInterpretRawSnmpValue, WidgetHelper.OUTER_SPACING, SWT.RIGHT);
      fd.right = new FormAttachment(100, 0);
      fd.top = new FormAttachment(checkUseCustomSnmpPort, WidgetHelper.OUTER_SPACING, SWT.BOTTOM);
      customSnmpPort.setLayoutData(fd);
      
      sampleCount = new LabeledSpinner(groupData, SWT.NONE);
      sampleCount.setLabel(Messages.get().General_SampleCountForAvg);
      sampleCount.setRange(0, 65535);
      sampleCount.setSelection(dci.getSampleCount());
		sampleCount.setEnabled(dci.getOrigin() == DataCollectionItem.WINPERF);
      fd = new FormData();
      fd.left = new FormAttachment(0, 0);
      fd.top = new FormAttachment(snmpRawType, WidgetHelper.OUTER_SPACING, SWT.BOTTOM);
      fd.right = new FormAttachment(100, 0);
      sampleCount.setLayoutData(fd);
      
      sourceNode = new ObjectSelector(groupData, SWT.NONE, true);
      sourceNode.setLabel(Messages.get().General_ProxyNode);
      sourceNode.setObjectClass(Node.class);
      sourceNode.setObjectId(dci.getSourceNode());
      sourceNode.setEnabled(dci.getOrigin() != DataCollectionItem.PUSH);
      
      fd = new FormData();
      fd.top = new FormAttachment(sampleCount, WidgetHelper.OUTER_SPACING, SWT.BOTTOM);
      fd.right = new FormAttachment(100, 0);
      agentCacheMode = WidgetHelper.createLabeledCombo(groupData, SWT.READ_ONLY, Messages.get().General_AgentCacheMode, fd);
      agentCacheMode.add(Messages.get().General_Default);
      agentCacheMode.add(Messages.get().General_On);
      agentCacheMode.add(Messages.get().General_Off);
      agentCacheMode.select(dci.getCacheMode().getValue());
      agentCacheMode.setEnabled((dci.getOrigin() == DataCollectionItem.AGENT) || (dci.getOrigin() == DataCollectionItem.SNMP));

      fd = new FormData();
      fd.left = new FormAttachment(0, 0);
      fd.top = new FormAttachment(sampleCount, WidgetHelper.OUTER_SPACING, SWT.BOTTOM);
      fd.right = new FormAttachment(agentCacheMode.getParent(), -WidgetHelper.OUTER_SPACING, SWT.LEFT);
      sourceNode.setLayoutData(fd);
      
      /** polling area **/
      Group groupPolling = new Group(dialogArea, SWT.NONE);
      groupPolling.setText(Messages.get().General_Polling);
      FormLayout pollingLayout = new FormLayout();
      pollingLayout.marginHeight = WidgetHelper.OUTER_SPACING;
      pollingLayout.marginWidth = WidgetHelper.OUTER_SPACING;
      groupPolling.setLayout(pollingLayout);
      gd = new GridData();
      gd.grabExcessHorizontalSpace = true;
      gd.horizontalAlignment = SWT.FILL;
      gd.verticalAlignment = SWT.FILL;
      groupPolling.setLayoutData(gd);
      
      fd = new FormData();
      fd.left = new FormAttachment(0, 0);
      fd.right = new FormAttachment(50, -WidgetHelper.OUTER_SPACING / 2);
      fd.top = new FormAttachment(0, 0);
      schedulingMode = WidgetHelper.createLabeledCombo(groupPolling, SWT.READ_ONLY, Messages.get().General_PollingMode, fd);
      schedulingMode.add(Messages.get().General_FixedIntervalsDefault);
      schedulingMode.add(Messages.get().General_FixedIntervalsCustom);
      schedulingMode.add(Messages.get().General_CustomSchedule);
      schedulingMode.select(dci.isUseAdvancedSchedule() ? 2 : ((dci.getPollingInterval() > 0) ? 1 : 0));
      schedulingMode.setEnabled(dci.getOrigin() != DataCollectionItem.PUSH);
      schedulingMode.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetDefaultSelected(SelectionEvent e)
			{
				widgetSelected(e);
			}

			@Override
			public void widgetSelected(SelectionEvent e)
			{
				pollingInterval.setEnabled(schedulingMode.getSelectionIndex() == 1);
			}
      });
      
      pollingInterval = new LabeledSpinner(groupPolling, SWT.NONE);
      pollingInterval.setLabel(Messages.get().General_PollingInterval);
      pollingInterval.setRange(1, 99999);
      pollingInterval.setSelection((dci.getPollingInterval() > 0) ? dci.getPollingInterval() : ConsoleSharedData.getSession().getDefaultDciPollingInterval());
      pollingInterval.setEnabled(!dci.isUseAdvancedSchedule() && (dci.getPollingInterval() > 0) && (dci.getOrigin() != DataCollectionItem.PUSH));
      fd = new FormData();
      fd.left = new FormAttachment(50, WidgetHelper.OUTER_SPACING / 2);
      fd.right = new FormAttachment(100, 0);
      fd.top = new FormAttachment(0, 0);
      pollingInterval.setLayoutData(fd);
      
      /** status **/
      Group groupStatus = new Group(dialogArea, SWT.NONE);
      groupStatus.setText(Messages.get().General_Status);
      gd = new GridData();
      gd.horizontalAlignment = SWT.FILL;
      gd.verticalAlignment = SWT.FILL;
      groupStatus.setLayoutData(gd);
      RowLayout statusLayout = new RowLayout();
      statusLayout.type = SWT.VERTICAL;
      groupStatus.setLayout(statusLayout);
      
      statusActive = new Button(groupStatus, SWT.RADIO);
      statusActive.setText(Messages.get().General_Active);
      statusActive.setSelection(dci.getStatus() == DataCollectionItem.ACTIVE);
      
      statusDisabled = new Button(groupStatus, SWT.RADIO);
      statusDisabled.setText(Messages.get().General_Disabled);
      statusDisabled.setSelection(dci.getStatus() == DataCollectionItem.DISABLED);
      
      statusUnsupported = new Button(groupStatus, SWT.RADIO);
      statusUnsupported.setText(Messages.get().General_NotSupported);
      statusUnsupported.setSelection(dci.getStatus() == DataCollectionItem.NOT_SUPPORTED);
      
      /** storage **/
      Group groupStorage = new Group(dialogArea, SWT.NONE);
      groupStorage.setText(Messages.get().General_Storage);
      gd = new GridData();
      gd.horizontalAlignment = SWT.FILL;
      gd.verticalAlignment = SWT.FILL;
      gd.horizontalSpan = 2;
      groupStorage.setLayoutData(gd);
      GridLayout storageLayout = new GridLayout();
      storageLayout.numColumns = 2;
      storageLayout.horizontalSpacing = WidgetHelper.OUTER_SPACING;
      groupStorage.setLayout(storageLayout);
      
      gd = new GridData();
      gd.grabExcessHorizontalSpace = true;
      gd.horizontalAlignment = SWT.FILL;
      retentionMode = WidgetHelper.createLabeledCombo(groupStorage, SWT.READ_ONLY, Messages.get().General_RetentionMode, gd);
      retentionMode.add(Messages.get().General_UseDefaultRetention);
      retentionMode.add(Messages.get().General_UseCustomRetention);
      retentionMode.add(Messages.get().General_NoStorage);
      retentionMode.select(((dci.getFlags() & DataCollectionObject.DCF_NO_STORAGE) != 0) ? 2 : ((dci.getRetentionTime() > 0) ? 1 : 0));
      retentionMode.addSelectionListener(new SelectionListener() {
         @Override
         public void widgetDefaultSelected(SelectionEvent e)
         {
            widgetSelected(e);
         }

         @Override
         public void widgetSelected(SelectionEvent e)
         {
            int mode = retentionMode.getSelectionIndex();
            retentionTime.setEnabled(mode == 1);
         }
      });
      
      retentionTime = new LabeledSpinner(groupStorage, SWT.NONE);
      retentionTime.setLabel(Messages.get().General_RetentionTime);
      retentionTime.setRange(1, 99999);
      retentionTime.setSelection((dci.getRetentionTime() > 0) ? dci.getRetentionTime() : ConsoleSharedData.getSession().getDefaultDciRetentionTime());
      retentionTime.setEnabled(((dci.getFlags() & DataCollectionObject.DCF_NO_STORAGE) == 0) && (dci.getRetentionTime() > 0));
      gd = new GridData();
      gd.horizontalAlignment = SWT.FILL;
      gd.grabExcessHorizontalSpace = true;
      retentionTime.setLayoutData(gd);
      
      int mode = 0;
      if ((dci.getFlags() & DataCollectionObject.DCF_NO_STORAGE) != 0)
         mode = 2;
      else if (dci.getRetentionTime() > 0)
         mode = 1;
      retentionMode.select(mode);
      retentionTime.setEnabled(mode == 1);
      
      onOriginChange();
      return dialogArea;
	}

	/**
	 * Handler for changing item origin
	 */
	private void onOriginChange()
	{
		int index = origin.getSelectionIndex();
		sourceNode.setEnabled(index != DataCollectionItem.PUSH);
		schedulingMode.setEnabled((index != DataCollectionItem.PUSH) && (index != DataCollectionItem.MQTT));
		pollingInterval.setEnabled((index != DataCollectionItem.PUSH) && (index != DataCollectionItem.MQTT) && (schedulingMode.getSelectionIndex() == 1));
		checkInterpretRawSnmpValue.setEnabled(index == DataCollectionItem.SNMP);
		snmpRawType.setEnabled((index == DataCollectionItem.SNMP) && checkInterpretRawSnmpValue.getSelection());
		checkUseCustomSnmpPort.setEnabled(index == DataCollectionItem.SNMP);
		customSnmpPort.setEnabled((index == DataCollectionItem.SNMP) && checkUseCustomSnmpPort.getSelection());
		sampleCount.setEnabled(index == DataCollectionItem.WINPERF);
		agentCacheMode.setEnabled((index == DataCollectionItem.AGENT) || (index == DataCollectionItem.SNMP));
		selectButton.setEnabled(
		      (index == DataCollectionItem.AGENT) || 
		      (index == DataCollectionItem.SNMP) || 
		      (index == DataCollectionItem.INTERNAL) || 
		      (index == DataCollectionItem.WINPERF) || 
		      (index == DataCollectionItem.CHECKPOINT_SNMP) || 
		      (index == DataCollectionItem.SCRIPT));
	}
	
	/**
	 * Select parameter
	 */
	private void selectParameter()
	{
		Dialog dlg;
		switch(origin.getSelectionIndex())
		{
			case DataCollectionItem.INTERNAL:
			   if (sourceNode.getObjectId() != 0)
			      dlg = new SelectInternalParamDlg(getShell(), sourceNode.getObjectId());
			   else
			      dlg = new SelectInternalParamDlg(getShell(), dci.getNodeId());
				break;
			case DataCollectionItem.AGENT:
			   if (sourceNode.getObjectId() != 0)
			      dlg = new SelectAgentParamDlg(getShell(), sourceNode.getObjectId(), false);
			   else
			      dlg = new SelectAgentParamDlg(getShell(), dci.getNodeId(), false);
				break;
			case DataCollectionItem.SNMP:
			case DataCollectionItem.CHECKPOINT_SNMP:
				SnmpObjectId oid;
				try
				{
					oid = SnmpObjectId.parseSnmpObjectId(parameter.getText());
				}
				catch(SnmpObjectIdFormatException e)
				{
					oid = null;
				}
				if (sourceNode.getObjectId() != 0)
				   dlg = new SelectSnmpParamDlg(getShell(), oid, sourceNode.getObjectId());
				else
				   dlg = new SelectSnmpParamDlg(getShell(), oid, dci.getNodeId());
				break;
			case DataCollectionItem.WINPERF:
			   if (sourceNode.getObjectId() != 0)
			      dlg = new WinPerfCounterSelectionDialog(getShell(), sourceNode.getObjectId());
			   else
			      dlg = new WinPerfCounterSelectionDialog(getShell(), dci.getNodeId());
				break;
         case DataCollectionItem.SCRIPT:
            dlg = new SelectParameterScriptDialog(getShell());
            break;
			default:
				dlg = null;
				break;
		}
		
		if ((dlg != null) && (dlg.open() == Window.OK))
		{
			IParameterSelectionDialog pd = (IParameterSelectionDialog)dlg;
			description.setText(pd.getParameterDescription());
			parameter.setText(pd.getParameterName());
			dataType.select(pd.getParameterDataType());
			editor.fireOnSelectItemListeners(origin.getSelectionIndex(), pd.getParameterName(), pd.getParameterDescription(), pd.getParameterDataType());
		}
	}
	
	/**
	 * Apply changes
	 * 
	 * @param isApply true if update operation caused by "Apply" button
	 */
	protected boolean applyChanges(final boolean isApply)
	{
		dci.setDescription(description.getText().trim());
		dci.setName(parameter.getText().trim());
		dci.setOrigin(origin.getSelectionIndex());
		dci.setDataType(dataType.getSelectionIndex());
		dci.setSampleCount(sampleCount.getSelection());
		dci.setSourceNode(sourceNode.getObjectId());
		dci.setCacheMode(AgentCacheMode.getByValue(agentCacheMode.getSelectionIndex()));
		dci.setUseAdvancedSchedule(schedulingMode.getSelectionIndex() == 2);
		dci.setPollingInterval((schedulingMode.getSelectionIndex() == 0) ? 0 : pollingInterval.getSelection());
		dci.setRetentionTime((retentionMode.getSelectionIndex() == 0) ? 0 : retentionTime.getSelection());
		dci.setSnmpRawValueInOctetString(checkInterpretRawSnmpValue.getSelection());
		dci.setSnmpRawValueType(snmpRawType.getSelectionIndex());
		if (checkUseCustomSnmpPort.getSelection())
		{
			dci.setSnmpPort(customSnmpPort.getSelection());
		}
		else
		{
			dci.setSnmpPort(0);
		}
		
		if (statusActive.getSelection())
			dci.setStatus(DataCollectionItem.ACTIVE);
		else if (statusDisabled.getSelection())
			dci.setStatus(DataCollectionItem.DISABLED);
		else if (statusUnsupported.getSelection())
			dci.setStatus(DataCollectionItem.NOT_SUPPORTED);
		
      if (retentionMode.getSelectionIndex() == 2)
         dci.setFlags(dci.getFlags() | DataCollectionObject.DCF_NO_STORAGE);
      else
         dci.setFlags(dci.getFlags() & ~DataCollectionObject.DCF_NO_STORAGE);
      
		editor.modify();
		return true;
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
	 * @see org.eclipse.jface.preference.PreferencePage#performApply()
	 */
	@Override
	protected void performApply()
	{
		applyChanges(true);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage#performDefaults()
	 */
	@Override
	protected void performDefaults()
	{
		super.performDefaults();
		
		NXCSession session = (NXCSession)ConsoleSharedData.getSession();
		
		schedulingMode.select(0);
		pollingInterval.setSelection(session.getDefaultDciPollingInterval());
		statusActive.setSelection(true);
		statusDisabled.setSelection(false);
		statusUnsupported.setSelection(false);
		retentionTime.setSelection(session.getDefaultDciRetentionTime());
		checkInterpretRawSnmpValue.setSelection(false);
		checkUseCustomSnmpPort.setSelection(false);
		customSnmpPort.setSelection(161);
	}
}
