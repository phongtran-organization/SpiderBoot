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
package org.netxms.ui.eclipse.console;

import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tray;
import org.eclipse.swt.widgets.TrayItem;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.netxms.base.Logger;
import org.netxms.base.LoggingFacility;
import org.netxms.ui.eclipse.console.resources.SharedIcons;
import org.netxms.ui.eclipse.shared.ConsoleSharedData;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {
	// The plug-in ID
	public static final String PLUGIN_ID = "org.netxms.ui.eclipse.console"; //$NON-NLS-1$

	// The shared instance
	private static Activator plugin;

	// Status line
	private IStatusLineManager statusLine;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext
	 * )
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		SharedIcons.init();

		Logger.setLoggingFacility(new LoggingFacility() {
			@Override
			public void writeLog(int level, String tag, String message,
					Throwable t) {
				int s;
				switch (level) {
				case LoggingFacility.ERROR:
					s = Status.ERROR;
					break;
				case LoggingFacility.WARNING:
					s = Status.WARNING;
					break;
				case LoggingFacility.INFO:
					s = Status.INFO;
					break;
				default:
					s = Status.OK;
					break;
				}
				log(s, tag + ": " + message, t); //$NON-NLS-1$
			}
		});
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext
	 * )
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 * 
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}

	/**
	 * Returns an image descriptor for the image file at the given plug-in
	 * relative path
	 * 
	 * @param path
	 *            the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}

	/**
	 * @return the statusLine
	 */
	public IStatusLineManager getStatusLine() {
		return statusLine;
	}

	/**
	 * @param statusLine
	 *            the statusLine to set
	 */
	public void setStatusLine(IStatusLineManager statusLine) {
		this.statusLine = statusLine;
	}

	/**
	 * Show tray icon
	 */
	public static void showTrayIcon() {
		if (ConsoleSharedData.getTrayIcon() != null)
			return; // Tray icon already exist

		Tray tray = Display.getDefault().getSystemTray();
		if (tray != null) {
			TrayItem item = new TrayItem(tray, SWT.NONE);
			item.setToolTipText(Messages.get().Activator_TrayTooltip);
			item.setImage(getImageDescriptor("icons/launcher/16x16.png").createImage()); //$NON-NLS-1$
			item.addSelectionListener(new SelectionListener() {
				@Override
				public void widgetSelected(SelectionEvent e) {
				}

				@Override
				public void widgetDefaultSelected(SelectionEvent e) {
					final Shell shell = PlatformUI.getWorkbench()
							.getActiveWorkbenchWindow().getShell();
					shell.setVisible(true);
					shell.setMinimized(false);
				}
			});
			ConsoleSharedData.setTrayIcon(item);
		}
	}

	/**
	 * Hide tray icon
	 */
	public static void hideTrayIcon() {
		TrayItem item = ConsoleSharedData.getTrayIcon();
		if (item == null)
			return; // No tray icon

		ConsoleSharedData.setTrayIcon(null);
		item.dispose();
	}

	/**
	 * Log via platform logging facilities
	 * 
	 * @param msg
	 */
	public static void logInfo(String msg) {
		log(Status.INFO, msg, null);
	}

	/**
	 * Log via platform logging facilities
	 * 
	 * @param msg
	 */
	public static void logError(String msg, Throwable t) {
		log(Status.ERROR, msg, t);
	}

	/**
	 * Log via platform logging facilities
	 * 
	 * @param msg
	 * @param t
	 */
	public static void log(int status, String msg, Throwable t) {
		getDefault().getLog().log(
				new Status(status, PLUGIN_ID, Status.OK, msg, t));
	}
}
