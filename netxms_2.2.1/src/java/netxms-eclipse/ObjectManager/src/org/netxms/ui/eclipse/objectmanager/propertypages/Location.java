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
package org.netxms.ui.eclipse.objectmanager.propertypages;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.dialogs.PropertyPage;
import org.netxms.base.GeoLocation;
import org.netxms.base.GeoLocationFormatException;
import org.netxms.base.PostalAddress;
import org.netxms.client.NXCObjectModificationData;
import org.netxms.client.NXCSession;
import org.netxms.client.objects.AbstractObject;
import org.netxms.ui.eclipse.jobs.ConsoleJob;
import org.netxms.ui.eclipse.objectmanager.Activator;
import org.netxms.ui.eclipse.objectmanager.Messages;
import org.netxms.ui.eclipse.shared.ConsoleSharedData;
import org.netxms.ui.eclipse.tools.MessageDialogHelper;
import org.netxms.ui.eclipse.tools.WidgetHelper;
import org.netxms.ui.eclipse.widgets.LabeledText;

/**
 * "Geolocation" property page
 */
public class Location extends PropertyPage {
	private AbstractObject object;
	private LabeledText latitude;
	private LabeledText longitude;
	private Button radioTypeUndefined;
	private Button radioTypeManual;
	private Button radioTypeAuto;
	private LabeledText country;
	private LabeledText city;
	private LabeledText streetAddress;
	private LabeledText postcode;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse
	 * .swt.widgets.Composite)
	 */
	@Override
	protected Control createContents(Composite parent) {
		Composite dialogArea = new Composite(parent, SWT.NONE);

		object = (AbstractObject) getElement().getAdapter(AbstractObject.class);
		GeoLocation gl = object.getGeolocation();

		GridLayout layout = new GridLayout();
		layout.verticalSpacing = WidgetHelper.OUTER_SPACING;
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		layout.numColumns = 2;
		dialogArea.setLayout(layout);

		Group typeGroup = new Group(dialogArea, SWT.NONE);
		typeGroup.setText(Messages.get().Location_LocationType);
		GridData gd = new GridData();
		gd.verticalSpan = 2;
		typeGroup.setLayoutData(gd);
		layout = new GridLayout();
		typeGroup.setLayout(layout);

		radioTypeUndefined = new Button(typeGroup, SWT.RADIO);
		radioTypeUndefined.setText(Messages.get().Location_Undefined);
		radioTypeUndefined.setSelection(gl.getType() == GeoLocation.UNSET);

		radioTypeManual = new Button(typeGroup, SWT.RADIO);
		radioTypeManual.setText(Messages.get().Location_Manual);
		radioTypeManual.setSelection(gl.getType() == GeoLocation.MANUAL);

		radioTypeAuto = new Button(typeGroup, SWT.RADIO);
		radioTypeAuto.setText(Messages.get().Location_Automatic);
		radioTypeAuto.setSelection(gl.getType() == GeoLocation.GPS);

		latitude = new LabeledText(dialogArea, SWT.NONE);
		latitude.setLabel(Messages.get().Location_Latitude);
		if (gl.getType() != GeoLocation.UNSET)
			latitude.setText(gl.getLatitudeAsString());
		gd = new GridData();
		gd.grabExcessHorizontalSpace = true;
		gd.horizontalAlignment = SWT.FILL;
		latitude.setLayoutData(gd);
		latitude.setEnabled(gl.getType() == GeoLocation.MANUAL);

		longitude = new LabeledText(dialogArea, SWT.NONE);
		longitude.setLabel(Messages.get().Location_Longitude);
		if (gl.getType() != GeoLocation.UNSET)
			longitude.setText(gl.getLongitudeAsString());
		gd = new GridData();
		gd.grabExcessHorizontalSpace = true;
		gd.horizontalAlignment = SWT.FILL;
		longitude.setLayoutData(gd);
		longitude.setEnabled(gl.getType() == GeoLocation.MANUAL);

		final SelectionListener listener = new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				latitude.setEnabled(radioTypeManual.getSelection());
				longitude.setEnabled(radioTypeManual.getSelection());
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}
		};
		radioTypeUndefined.addSelectionListener(listener);
		radioTypeManual.addSelectionListener(listener);
		radioTypeAuto.addSelectionListener(listener);

		country = new LabeledText(dialogArea, SWT.NONE);
		country.setLabel(Messages.get().Location_Country);
		country.setText(object.getPostalAddress().country);
		country.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false,
				2, 1));

		city = new LabeledText(dialogArea, SWT.NONE);
		city.setLabel(Messages.get().Location_City);
		city.setText(object.getPostalAddress().city);
		city.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));

		streetAddress = new LabeledText(dialogArea, SWT.NONE);
		streetAddress.setLabel(Messages.get().Location_StreetAddress);
		streetAddress.setText(object.getPostalAddress().streetAddress);
		streetAddress.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
				false, 2, 1));

		postcode = new LabeledText(dialogArea, SWT.NONE);
		postcode.setLabel(Messages.get().Location_Postcode);
		postcode.setText(object.getPostalAddress().postcode);
		postcode.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false,
				2, 1));

		return dialogArea;
	}

	/**
	 * Apply changes
	 * 
	 * @param isApply
	 *            true if update operation caused by "Apply" button
	 */
	protected boolean applyChanges(final boolean isApply) {
		int type = GeoLocation.UNSET;
		if (radioTypeManual.getSelection())
			type = GeoLocation.MANUAL;
		else if (radioTypeAuto.getSelection())
			type = GeoLocation.GPS;

		GeoLocation location;
		if (type == GeoLocation.MANUAL) {
			try {
				location = GeoLocation.parseGeoLocation(latitude.getText(),
						longitude.getText());
			} catch (GeoLocationFormatException e) {
				MessageDialogHelper.openError(getShell(),
						Messages.get().Location_Error,
						Messages.get().Location_FormatError);
				return false;
			}
		} else {
			location = new GeoLocation(type == GeoLocation.GPS);
		}

		final NXCObjectModificationData md = new NXCObjectModificationData(
				object.getObjectId());
		md.setGeolocation(location);
		md.setPostalAddress(new PostalAddress(country.getText().trim(), city
				.getText().trim(), streetAddress.getText().trim(), postcode
				.getText().trim()));

		if (isApply)
			setValid(false);

		final NXCSession session = (NXCSession) ConsoleSharedData.getSession();
		new ConsoleJob(String.format(Messages.get().Location_JobName,
				object.getObjectName()), null, Activator.PLUGIN_ID, null) {
			@Override
			protected void runInternal(IProgressMonitor monitor)
					throws Exception {
				session.modifyObject(md);
			}

			@Override
			protected String getErrorMessage() {
				return Messages.get().Location_JobError;
			}

			@Override
			protected void jobFinalize() {
				if (isApply) {
					runInUIThread(new Runnable() {
						@Override
						public void run() {
							Location.this.setValid(true);
						}
					});
				}
			}
		}.start();

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
		radioTypeUndefined.setSelection(true);
		radioTypeManual.setSelection(false);
		radioTypeAuto.setSelection(false);
		latitude.setText(""); //$NON-NLS-1$
		latitude.setEnabled(false);
		longitude.setText(""); //$NON-NLS-1$
		longitude.setEnabled(false);
	}
}
