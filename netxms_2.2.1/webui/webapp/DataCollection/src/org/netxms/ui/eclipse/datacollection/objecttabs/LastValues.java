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
package org.netxms.ui.eclipse.datacollection.objecttabs;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.State;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.netxms.client.objects.AbstractObject;
import org.netxms.client.objects.DataCollectionTarget;
import org.netxms.ui.eclipse.datacollection.widgets.LastValuesWidget;
import org.netxms.ui.eclipse.datacollection.Activator;
import org.netxms.ui.eclipse.objectview.objecttabs.ObjectTab;
import org.netxms.ui.eclipse.tools.VisibilityValidator;

/**
 * Last values tab
 */
public class LastValues extends ObjectTab
{
	private LastValuesWidget dataView;
	private boolean initShowFilter = true;
	
	/* (non-Javadoc)
	 * @see org.netxms.ui.eclipse.objectview.objecttabs.ObjectTab#createTabContent(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected void createTabContent(Composite parent)
	{
	   final IDialogSettings settings = Activator.getDefault().getDialogSettings();
      initShowFilter = safeCast(settings.get("LastValuesTab.showFilter"), settings.getBoolean("LastValuesTab.showFilter"), initShowFilter);
	   
		dataView = new LastValuesWidget(getViewPart(), parent, SWT.NONE, getObject(), "LastValuesTab", new VisibilityValidator() {  //$NON-NLS-1$
         @Override
         public boolean isVisible()
         {
            return isActive();
         }
      });
		
		dataView.addDisposeListener(new DisposeListener() {

         @Override
         public void widgetDisposed(DisposeEvent e)
         {
            settings.put("LastValuesTab.showFilter", dataView.isFilterEnabled());
         }
		   
		});
		
		dataView.setAutoRefreshEnabled(true);
		dataView.setFilterCloseAction(new Action() {
			@Override
			public void run()
			{
				dataView.enableFilter(false);
				ICommandService service = (ICommandService)PlatformUI.getWorkbench().getService(ICommandService.class);
				Command command = service.getCommand("org.netxms.ui.eclipse.datacollection.commands.show_dci_filter"); //$NON-NLS-1$
				State state = command.getState("org.netxms.ui.eclipse.datacollection.commands.show_dci_filter.state"); //$NON-NLS-1$
				state.setValue(false);
				service.refreshElements(command.getId(), null);
			}
		});
		
		dataView.enableFilter(initShowFilter);
	}
	
	/**
    * @param b
    * @param defval
    * @return
    */
   private static boolean safeCast(String s, boolean b, boolean defval)
   {
      return (s != null) ? b : defval;
   }

	/* (non-Javadoc)
	 * @see org.netxms.ui.eclipse.objectview.objecttabs.ObjectTab#objectChanged(org.netxms.client.objects.AbstractObject)
	 */
	@Override
	public void objectChanged(AbstractObject object)
	{
		dataView.setDataCollectionTarget(object);
		if (getViewPart().getSite().getPage().isPartVisible(getViewPart()) && isActive())
		   dataView.refresh();
	}

	/* (non-Javadoc)
	 * @see org.netxms.ui.eclipse.objectview.objecttabs.ObjectTab#showForObject(org.netxms.client.objects.AbstractObject)
	 */
	@Override
	public boolean showForObject(AbstractObject object)
	{
		return object instanceof DataCollectionTarget;
	}

	/* (non-Javadoc)
	 * @see org.netxms.ui.eclipse.objectview.objecttabs.ObjectTab#refresh()
	 */
	@Override
	public void refresh()
	{
		dataView.refresh();
	}

	/**
	 * @param enabled
	 */
	public void setFilterEnabled(boolean enabled)
	{
		dataView.enableFilter(enabled);
	}

	/* (non-Javadoc)
	 * @see org.netxms.ui.eclipse.objectview.objecttabs.ObjectTab#selected()
	 */
	@Override
	public void selected()
	{
		super.selected();
		refresh();
	}
}
