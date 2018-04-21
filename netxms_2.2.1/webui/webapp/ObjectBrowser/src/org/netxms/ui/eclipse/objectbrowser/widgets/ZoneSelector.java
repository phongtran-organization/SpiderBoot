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
package org.netxms.ui.eclipse.objectbrowser.widgets;

import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Composite;
import org.netxms.client.objects.Zone;
import org.netxms.ui.eclipse.objectbrowser.dialogs.ZoneSelectionDialog;
import org.netxms.ui.eclipse.shared.ConsoleSharedData;
import org.netxms.ui.eclipse.widgets.AbstractSelector;

/**
 * Zone selector
 */
public class ZoneSelector extends AbstractSelector
{
   private long zoneUIN = 0;
   private String emptySelectionName = "<none>";
   
   /**
    * @param parent
    * @param style
    * @param showClearButton
    */
   public ZoneSelector(Composite parent, int style, boolean showClearButton)
   {
      super(parent, style, showClearButton ? SHOW_CLEAR_BUTTON : 0);
      setText(emptySelectionName);
   }

   /* (non-Javadoc)
    * @see org.netxms.ui.eclipse.widgets.AbstractSelector#selectionButtonHandler()
    */
   @Override
   protected void selectionButtonHandler()
   {     
      ZoneSelectionDialog dlg = new ZoneSelectionDialog(getShell());
      if (dlg.open() == Window.OK)
      {
         zoneUIN = dlg.getZoneUIN();
         setText(dlg.getZoneName());
         fireModifyListeners();
      }
   }

   /* (non-Javadoc)
    * @see org.netxms.ui.eclipse.widgets.AbstractSelector#clearButtonHandler()
    */
   @Override
   protected void clearButtonHandler()
   {
      zoneUIN = -1;
      setText(emptySelectionName);
      fireModifyListeners();
   }

   /**
    * Get UIN of selected zone
    * 
    * @return selected zone UIN
    */
   public long getZoneUIN()
   {
      return zoneUIN;
   }

   /**
    * Get name of selected zone
    * 
    * @return zone name
    */
   public String getZoneName()
   {
      return getText();
   }

   /**
    * Set zone UIN
    * 
    * @param zoneUIN new zone UIN
    */
   public void setZoneUIN(long zoneUIN)
   {
      this.zoneUIN = zoneUIN;
      if (zoneUIN == -1)
      {
         setText(emptySelectionName); //$NON-NLS-1$
      }
      else
      {
         final Zone zone = ConsoleSharedData.getSession().findZone(zoneUIN);
         setText((zone != null) ? zone.getObjectName() : ("<" + Long.toString(zoneUIN) + ">")); //$NON-NLS-1$ //$NON-NLS-2$
      }
   }
}
