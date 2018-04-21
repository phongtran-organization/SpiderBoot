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
package org.netxms.webui.mobile;

import java.util.Locale;
import java.util.Properties;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.application.WorkbenchAdvisor;
import org.netxms.api.client.Session;
import org.netxms.ui.eclipse.console.AppPropertiesLoader;
import org.netxms.ui.eclipse.console.resources.SharedIcons;
import org.netxms.ui.eclipse.shared.ConsoleSharedData;

/**
 * This class controls all aspects of the application's execution and is
 * contributed through the plugin.xml.
 */
public class MobileApplication implements IApplication
{
   /**
    * Get page manager instance for current display
    * 
    * @return
    */
   public static PageManager getPageManager()
   {
      return (PageManager)ConsoleSharedData.getProperty("MobileUI.PageManagerInstance");
   }
   
	/* (non-Javadoc)
	 * @see org.eclipse.equinox.app.IApplication#start(org.eclipse.equinox.app.IApplicationContext)
	 */
	public Object start(IApplicationContext context) throws Exception
	{
		String lang = RWT.getRequest().getParameter("lang"); //$NON-NLS-1$
		if (lang != null)
			RWT.setLocale(new Locale(lang));
		
		final Properties properties = new AppPropertiesLoader().load();
		int timeout;
		try
		{
			timeout = Integer.parseInt(properties.getProperty("sessionTimeout", "120"));			
		}
		catch(NumberFormatException e)
		{
			timeout = 120;
		}
		
		final Display display = PlatformUI.createDisplay();
		RWT.getUISession().getHttpSession().setMaxInactiveInterval(timeout);
		display.disposeExec(new Runnable() {
			public void run()
			{
				Session session = ConsoleSharedData.getSession();
				if (session != null)
					session.disconnect();
			}
		});
		
		SharedIcons.init(display);
		WorkbenchAdvisor advisor = new MobileApplicationWorkbenchAdvisor();
		return PlatformUI.createAndRunWorkbench(display, advisor);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.equinox.app.IApplication#stop()
	 */
	public void stop()
	{
		// Do nothing
	}
}
