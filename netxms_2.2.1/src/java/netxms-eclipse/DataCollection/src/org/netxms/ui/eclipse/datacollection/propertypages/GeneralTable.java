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

import java.util.HashMap;
import java.util.Map;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
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
import org.netxms.client.datacollection.DataCollectionTable;
import org.netxms.client.objects.AbstractNode;
import org.netxms.client.objects.AbstractObject;
import org.netxms.client.objects.Cluster;
import org.netxms.client.objects.ClusterResource;
import org.netxms.client.objects.Node;
import org.netxms.client.snmp.SnmpObjectId;
import org.netxms.client.snmp.SnmpObjectIdFormatException;
import org.netxms.ui.eclipse.datacollection.Messages;
import org.netxms.ui.eclipse.datacollection.dialogs.IParameterSelectionDialog;
import org.netxms.ui.eclipse.datacollection.dialogs.SelectAgentParamDlg;
import org.netxms.ui.eclipse.datacollection.dialogs.SelectParameterScriptDialog;
import org.netxms.ui.eclipse.datacollection.dialogs.SelectSnmpParamDlg;
import org.netxms.ui.eclipse.datacollection.propertypages.helpers.DCIPropertyPageDialog;
import org.netxms.ui.eclipse.objectbrowser.widgets.ObjectSelector;
import org.netxms.ui.eclipse.shared.ConsoleSharedData;
import org.netxms.ui.eclipse.tools.WidgetHelper;
import org.netxms.ui.eclipse.widgets.LabeledSpinner;
import org.netxms.ui.eclipse.widgets.LabeledText;

/**
 * "General" property page for table DCI
 */
public class GeneralTable extends DCIPropertyPageDialog {
	private DataCollectionTable dci;
	private AbstractObject owner;
	private Cluster cluster = null;
	private Map<Integer, Long> clusterResourceMap;
	private Text description;
	private LabeledText parameter;
	private Button selectButton;
	private Combo origin;
	private Button checkUseCustomSnmpPort;
	private Spinner customSnmpPort;
	private ObjectSelector sourceNode;
	private Combo agentCacheMode;
	private Combo schedulingMode;
	private Combo retentionMode;
	private LabeledSpinner pollingInterval;
	private LabeledSpinner retentionTime;
	private Combo clusterResource;
	private Button statusActive;
	private Button statusDisabled;
	private Button statusUnsupported;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse
	 * .swt.widgets.Composite)
	 */
	@Override
	protected Control createContents(Composite parent) {
		Composite dialogArea = (Composite) super.createContents(parent);

		dci = editor.getObjectAsTable();

		final NXCSession session = (NXCSession) ConsoleSharedData.getSession();
		owner = session.findObjectById(dci.getNodeId());

		if (owner instanceof Cluster) {
			cluster = (Cluster) owner;
		} else if (owner instanceof AbstractNode) {
			for (AbstractObject o : owner.getParentsAsArray()) {
				if (o instanceof Cluster) {
					cluster = (Cluster) o;
					break;
				}
			}
		}

		GridLayout layout = new GridLayout();
		layout.verticalSpacing = WidgetHelper.OUTER_SPACING;
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		layout.numColumns = 2;
		dialogArea.setLayout(layout);

		/** description area **/
		Group groupDescription = new Group(dialogArea, SWT.NONE);
		groupDescription.setText(Messages.get().GeneralTable_Description);
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
		groupData.setText(Messages.get().GeneralTable_Data);
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
		parameter.setLabel(Messages.get().GeneralTable_Parameter);
		parameter.getTextControl().setTextLimit(255);
		parameter.setText(dci.getName());

		selectButton = new Button(groupData, SWT.PUSH);
		selectButton.setText(Messages.get().GeneralTable_Select);
		selectButton.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}

			@Override
			public void widgetSelected(SelectionEvent e) {
				selectParameter();
			}
		});

		FormData fd = new FormData();
		fd.left = new FormAttachment(0, 0);
		fd.top = new FormAttachment(0, 0);
		fd.right = new FormAttachment(selectButton,
				-WidgetHelper.INNER_SPACING, SWT.LEFT);
		parameter.setLayoutData(fd);

		fd = new FormData();
		fd.right = new FormAttachment(100, 0);
		fd.bottom = new FormAttachment(parameter, 0, SWT.BOTTOM);
		fd.width = WidgetHelper.BUTTON_WIDTH_HINT;
		selectButton.setLayoutData(fd);

		fd = new FormData();
		fd.left = new FormAttachment(0, 0);
		fd.top = new FormAttachment(parameter, WidgetHelper.OUTER_SPACING,
				SWT.BOTTOM);
		fd.right = new FormAttachment(50, -WidgetHelper.OUTER_SPACING / 2);
		origin = WidgetHelper.createLabeledCombo(groupData, SWT.READ_ONLY,
				Messages.get().GeneralTable_Origin, fd);
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
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}

			@Override
			public void widgetSelected(SelectionEvent e) {
				onOriginChange();
			}
		});

		checkUseCustomSnmpPort = new Button(groupData, SWT.CHECK);
		checkUseCustomSnmpPort
				.setText(Messages.get().GeneralTable_UseCustomSNMPPort);
		checkUseCustomSnmpPort.setSelection(dci.getSnmpPort() != 0);
		checkUseCustomSnmpPort.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				customSnmpPort.setEnabled(checkUseCustomSnmpPort.getSelection());
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}
		});
		fd = new FormData();
		fd.left = new FormAttachment(origin.getParent(),
				WidgetHelper.OUTER_SPACING, SWT.RIGHT);
		fd.right = new FormAttachment(100, 0);
		fd.top = new FormAttachment(parameter, WidgetHelper.OUTER_SPACING,
				SWT.BOTTOM);
		checkUseCustomSnmpPort.setLayoutData(fd);
		checkUseCustomSnmpPort
				.setEnabled(dci.getOrigin() == DataCollectionObject.SNMP);

		customSnmpPort = new Spinner(groupData, SWT.BORDER);
		customSnmpPort.setMinimum(1);
		customSnmpPort.setMaximum(65535);
		if ((dci.getOrigin() == DataCollectionItem.SNMP)
				&& (dci.getSnmpPort() != 0)) {
			customSnmpPort.setEnabled(true);
			customSnmpPort.setSelection(dci.getSnmpPort());
		} else {
			customSnmpPort.setEnabled(false);
		}
		fd = new FormData();
		fd.left = new FormAttachment(origin.getParent(),
				WidgetHelper.OUTER_SPACING, SWT.RIGHT);
		fd.right = new FormAttachment(100, 0);
		fd.top = new FormAttachment(checkUseCustomSnmpPort,
				WidgetHelper.OUTER_SPACING, SWT.BOTTOM);
		customSnmpPort.setLayoutData(fd);

		sourceNode = new ObjectSelector(groupData, SWT.NONE, true);
		sourceNode.setLabel(Messages.get().GeneralTable_ProxyNode);
		sourceNode.setObjectClass(Node.class);
		sourceNode.setObjectId(dci.getSourceNode());
		sourceNode.setEnabled(dci.getOrigin() != DataCollectionObject.PUSH);
		sourceNode.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				editor.setSourceNode(sourceNode.getObjectId());
			}
		});

		fd = new FormData();
		fd.top = new FormAttachment(origin.getParent(),
				WidgetHelper.OUTER_SPACING, SWT.BOTTOM);
		fd.right = new FormAttachment(100, 0);
		agentCacheMode = WidgetHelper.createLabeledCombo(groupData,
				SWT.READ_ONLY, Messages.get().GeneralTable_AgentCacheMode, fd);
		agentCacheMode.add(Messages.get().GeneralTable_Default);
		agentCacheMode.add(Messages.get().GeneralTable_On);
		agentCacheMode.add(Messages.get().GeneralTable_Off);
		agentCacheMode.select(dci.getCacheMode().getValue());
		agentCacheMode.setEnabled((dci.getOrigin() == DataCollectionItem.AGENT)
				|| (dci.getOrigin() == DataCollectionItem.SNMP));

		fd = new FormData();
		fd.left = new FormAttachment(0, 0);
		fd.top = new FormAttachment(origin.getParent(),
				WidgetHelper.OUTER_SPACING, SWT.BOTTOM);
		fd.right = new FormAttachment(agentCacheMode.getParent(),
				-WidgetHelper.OUTER_SPACING, SWT.LEFT);
		sourceNode.setLayoutData(fd);

		/** polling area **/
		Group groupPolling = new Group(dialogArea, SWT.NONE);
		groupPolling.setText(Messages.get().GeneralTable_Polling);
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
		schedulingMode = WidgetHelper.createLabeledCombo(groupPolling,
				SWT.READ_ONLY, Messages.get().GeneralTable_PollingMode, fd);
		schedulingMode.add(Messages.get().General_FixedIntervalsDefault);
		schedulingMode.add(Messages.get().General_FixedIntervalsCustom);
		schedulingMode.add(Messages.get().General_CustomSchedule);
		schedulingMode.select(dci.isUseAdvancedSchedule() ? 2 : ((dci
				.getPollingInterval() > 0) ? 1 : 0));
		schedulingMode.setEnabled(dci.getOrigin() != DataCollectionObject.PUSH);
		schedulingMode.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}

			@Override
			public void widgetSelected(SelectionEvent e) {
				pollingInterval.setEnabled(schedulingMode.getSelectionIndex() == 1);
			}
		});

		pollingInterval = new LabeledSpinner(groupPolling, SWT.NONE);
		pollingInterval.setLabel(Messages.get().General_PollingInterval);
		pollingInterval.setRange(1, 99999);
		pollingInterval.setSelection((dci.getPollingInterval() > 0) ? dci
				.getPollingInterval() : ConsoleSharedData.getSession()
				.getDefaultDciPollingInterval());
		pollingInterval.setEnabled(!dci.isUseAdvancedSchedule()
				&& (dci.getPollingInterval() > 0)
				&& (dci.getOrigin() != DataCollectionItem.PUSH));
		fd = new FormData();
		fd.left = new FormAttachment(50, WidgetHelper.OUTER_SPACING / 2);
		fd.right = new FormAttachment(100, 0);
		fd.top = new FormAttachment(0, 0);
		pollingInterval.setLayoutData(fd);

		fd = new FormData();
		fd.left = new FormAttachment(0, 0);
		fd.right = new FormAttachment(100, 0);
		fd.top = new FormAttachment(schedulingMode.getParent(),
				WidgetHelper.OUTER_SPACING, SWT.BOTTOM);
		clusterResource = WidgetHelper.createLabeledCombo(groupPolling,
				SWT.READ_ONLY, Messages.get().GeneralTable_ClRes, fd);
		if (cluster != null) {
			clusterResourceMap = new HashMap<Integer, Long>();
			clusterResourceMap.put(0, 0L);

			clusterResource.add(Messages.get().GeneralTable_None);
			if (dci.getResourceId() == 0)
				clusterResource.select(0);

			int index = 1;
			for (ClusterResource r : cluster.getResources()) {
				clusterResource.add(r.getName());
				clusterResourceMap.put(index, r.getId());
				if (dci.getResourceId() == r.getId())
					clusterResource.select(index);
				index++;
			}
		} else {
			clusterResource.add(Messages.get().GeneralTable_None);
			clusterResource.select(0);
			clusterResource.setEnabled(false);
		}

		/** status **/
		Group groupStatus = new Group(dialogArea, SWT.NONE);
		groupStatus.setText(Messages.get().GeneralTable_Status);
		gd = new GridData();
		gd.horizontalAlignment = SWT.FILL;
		gd.verticalAlignment = SWT.FILL;
		groupStatus.setLayoutData(gd);
		RowLayout statusLayout = new RowLayout();
		statusLayout.type = SWT.VERTICAL;
		groupStatus.setLayout(statusLayout);

		statusActive = new Button(groupStatus, SWT.RADIO);
		statusActive.setText(Messages.get().GeneralTable_Active);
		statusActive
				.setSelection(dci.getStatus() == DataCollectionObject.ACTIVE);

		statusDisabled = new Button(groupStatus, SWT.RADIO);
		statusDisabled.setText(Messages.get().GeneralTable_Disabled);
		statusDisabled
				.setSelection(dci.getStatus() == DataCollectionObject.DISABLED);

		statusUnsupported = new Button(groupStatus, SWT.RADIO);
		statusUnsupported.setText(Messages.get().GeneralTable_NotSupported);
		statusUnsupported
				.setSelection(dci.getStatus() == DataCollectionObject.NOT_SUPPORTED);

		/** storage **/
		Group groupStorage = new Group(dialogArea, SWT.NONE);
		groupStorage.setText(Messages.get().GeneralTable_Storage);
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
		retentionMode = WidgetHelper.createLabeledCombo(groupStorage,
				SWT.READ_ONLY, Messages.get().GeneralTable_RetentionMode, gd);
		retentionMode.add(Messages.get().GeneralTable_UseDefaultRetention);
		retentionMode.add(Messages.get().GeneralTable_UseCustomRetention);
		retentionMode.add(Messages.get().GeneralTable_NoStorage);
		retentionMode
				.select(((dci.getFlags() & DataCollectionObject.DCF_NO_STORAGE) != 0) ? 2
						: ((dci.getRetentionTime() > 0) ? 1 : 0));
		retentionMode.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}

			@Override
			public void widgetSelected(SelectionEvent e) {
				int mode = retentionMode.getSelectionIndex();
				retentionTime.setEnabled(mode == 1);
			}
		});

		retentionTime = new LabeledSpinner(groupStorage, SWT.NONE);
		retentionTime.setLabel(Messages.get().GeneralTable_RetentionTime);
		retentionTime.setRange(1, 99999);
		retentionTime.setSelection((dci.getRetentionTime() > 0) ? dci
				.getRetentionTime() : ConsoleSharedData.getSession()
				.getDefaultDciRetentionTime());
		retentionTime
				.setEnabled(((dci.getFlags() & DataCollectionObject.DCF_NO_STORAGE) == 0)
						&& (dci.getRetentionTime() > 0));
		gd = new GridData();
		gd.horizontalAlignment = SWT.FILL;
		gd.grabExcessHorizontalSpace = true;
		retentionTime.setLayoutData(gd);

		return dialogArea;
	}

	/**
	 * Handler for changing item origin
	 */
	private void onOriginChange() {
		int index = origin.getSelectionIndex();
		sourceNode.setEnabled(index != DataCollectionObject.PUSH);
		schedulingMode.setEnabled((index != DataCollectionItem.PUSH)
				&& (index != DataCollectionItem.MQTT));
		pollingInterval.setEnabled((index != DataCollectionItem.PUSH)
				&& (index != DataCollectionItem.MQTT)
				&& (schedulingMode.getSelectionIndex() == 1));
		checkUseCustomSnmpPort.setEnabled(index == DataCollectionObject.SNMP);
		customSnmpPort.setEnabled((index == DataCollectionObject.SNMP)
				&& checkUseCustomSnmpPort.getSelection());
		agentCacheMode.setEnabled((index == DataCollectionItem.AGENT)
				|| (index == DataCollectionItem.SNMP));
		selectButton.setEnabled((index == DataCollectionItem.AGENT)
				|| (index == DataCollectionItem.SNMP)
				|| (index == DataCollectionItem.INTERNAL)
				|| (index == DataCollectionItem.WINPERF)
				|| (index == DataCollectionItem.CHECKPOINT_SNMP)
				|| (index == DataCollectionItem.SCRIPT));
	}

	/**
	 * Select parameter
	 */
	private void selectParameter() {
		Dialog dlg;
		editor.setSourceNode(sourceNode.getObjectId());
		switch (origin.getSelectionIndex()) {
		case DataCollectionObject.AGENT:
			if (sourceNode.getObjectId() != 0)
				dlg = new SelectAgentParamDlg(getShell(),
						sourceNode.getObjectId(), true);
			else
				dlg = new SelectAgentParamDlg(getShell(), dci.getNodeId(), true);
			break;
		case DataCollectionObject.SNMP:
		case DataCollectionObject.CHECKPOINT_SNMP:
			SnmpObjectId oid;
			try {
				oid = SnmpObjectId.parseSnmpObjectId(parameter.getText());
			} catch (SnmpObjectIdFormatException e) {
				oid = null;
			}
			if (sourceNode.getObjectId() != 0)
				dlg = new SelectSnmpParamDlg(getShell(), oid,
						sourceNode.getObjectId());
			else
				dlg = new SelectSnmpParamDlg(getShell(), oid, dci.getNodeId());
			break;
		case DataCollectionItem.SCRIPT:
			dlg = new SelectParameterScriptDialog(getShell());
			break;
		default:
			dlg = null;
			break;
		}

		if ((dlg != null) && (dlg.open() == Window.OK)) {
			IParameterSelectionDialog pd = (IParameterSelectionDialog) dlg;
			description.setText(pd.getParameterDescription());
			parameter.setText(pd.getParameterName());
			editor.fireOnSelectTableListeners(origin.getSelectionIndex(),
					pd.getParameterName(), pd.getParameterDescription());
		}
	}

	/**
	 * Apply changes
	 * 
	 * @param isApply
	 *            true if update operation caused by "Apply" button
	 */
	protected boolean applyChanges(final boolean isApply) {
		dci.setDescription(description.getText().trim());
		dci.setName(parameter.getText().trim());
		dci.setOrigin(origin.getSelectionIndex());
		dci.setSourceNode(sourceNode.getObjectId());
		dci.setCacheMode(AgentCacheMode.getByValue(agentCacheMode
				.getSelectionIndex()));
		dci.setUseAdvancedSchedule(schedulingMode.getSelectionIndex() == 2);
		dci.setPollingInterval((schedulingMode.getSelectionIndex() == 0) ? 0
				: pollingInterval.getSelection());
		dci.setRetentionTime((retentionMode.getSelectionIndex() == 0) ? 0
				: retentionTime.getSelection());
		if (checkUseCustomSnmpPort.getSelection()) {
			dci.setSnmpPort(Integer.parseInt(customSnmpPort.getText()));
		} else {
			dci.setSnmpPort(0);
		}

		if (statusActive.getSelection())
			dci.setStatus(DataCollectionObject.ACTIVE);
		else if (statusDisabled.getSelection())
			dci.setStatus(DataCollectionObject.DISABLED);
		else if (statusUnsupported.getSelection())
			dci.setStatus(DataCollectionObject.NOT_SUPPORTED);

		if (retentionMode.getSelectionIndex() == 2)
			dci.setFlags(dci.getFlags() | DataCollectionObject.DCF_NO_STORAGE);
		else
			dci.setFlags(dci.getFlags() & ~DataCollectionObject.DCF_NO_STORAGE);

		if (cluster != null) {
			dci.setResourceId(clusterResourceMap.get(clusterResource
					.getSelectionIndex()));
		}

		editor.modify();
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.PreferencePage#performOk()
	 */
	@Override
	public boolean performOk() {
		return applyChanges(false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.PreferencePage#performApply()
	 */
	@Override
	protected void performApply() {
		applyChanges(true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.PreferencePage#performDefaults()
	 */
	@Override
	protected void performDefaults() {
		super.performDefaults();
		schedulingMode.select(0);
		pollingInterval.setSelection(60);
		statusActive.setSelection(true);
		statusDisabled.setSelection(false);
		statusUnsupported.setSelection(false);
		retentionTime.setSelection(30);
		checkUseCustomSnmpPort.setSelection(false);
	}
}
