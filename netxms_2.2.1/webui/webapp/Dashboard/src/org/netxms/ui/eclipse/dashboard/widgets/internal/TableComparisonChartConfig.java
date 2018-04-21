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
package org.netxms.ui.eclipse.dashboard.widgets.internal;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.netxms.client.datacollection.GraphSettings;
import org.netxms.ui.eclipse.dashboard.dialogs.helpers.DciIdMatchingData;
import org.netxms.ui.eclipse.dashboard.dialogs.helpers.ObjectIdMatchingData;
import org.simpleframework.xml.Element;

/**
 * Common base class for table comparison chart configs
 */
public abstract class TableComparisonChartConfig extends DashboardElementConfig
{
	@Element
	private long nodeId = 0;
	
	@Element
	private long dciId = 0;
	
	@Element(required = false)
	private String instanceColumn = null;

	@Element(required = false)
	private String dataColumn = null;

	@Element(required = false)
	private String title = ""; //$NON-NLS-1$
	
	@Element(required = false)
	private int legendPosition = GraphSettings.POSITION_RIGHT;
	
	@Element(required = false)
	private boolean showLegend = true;
	
	@Element(required = false)
	private boolean showTitle = true;

	@Element(required = false)
	private int refreshRate = 30;

	@Element(required = false)
	private boolean showIn3D = false;
	
	@Element(required = false)
	private boolean translucent = false;
		
	@Element(required = false)
	private boolean ignoreZeroValues = false;
	
   @Element(required = false)
	private boolean sortOnDataColumn = false;
	
   @Element(required = false)
	private boolean sortDescending = false;

   @Element(required = false)
   private boolean autoScale = true;
   
   @Element(required = false)
   private int minYScaleValue = 0;

   @Element(required = false)
   private int maxYScaleValue = 100;

   @Element(required = false)
   private boolean modifyYBase = false;
	
	/**
	 * @return the showIn3D
	 */
	public boolean isShowIn3D()
	{
		return showIn3D;
	}

	/**
	 * @param showIn3D the showIn3D to set
	 */
	public void setShowIn3D(boolean showIn3D)
	{
		this.showIn3D = showIn3D;
	}

	/**
	 * @return the translucent
	 */
	public boolean isTranslucent()
	{
		return translucent;
	}

	/**
	 * @param translucent the translucent to set
	 */
	public void setTranslucent(boolean translucent)
	{
		this.translucent = translucent;
	}

	/**
	 * @return the nodeId
	 */
	public long getNodeId()
	{
		return nodeId;
	}

	/**
	 * @param nodeId the nodeId to set
	 */
	public void setNodeId(long nodeId)
	{
		this.nodeId = nodeId;
	}

	/**
	 * @return the dciId
	 */
	public long getDciId()
	{
		return dciId;
	}

	/**
	 * @param dciId the dciId to set
	 */
	public void setDciId(long dciId)
	{
		this.dciId = dciId;
	}

	/**
	 * @return the instanceColumn
	 */
	public String getInstanceColumn()
	{
		return instanceColumn;
	}

	/**
	 * @param instanceColumn the instanceColumn to set
	 */
	public void setInstanceColumn(String instanceColumn)
	{
		this.instanceColumn = instanceColumn;
	}

	/**
	 * @return the dataColumn
	 */
	public String getDataColumn()
	{
		return dataColumn;
	}

	/**
	 * @param dataColumn the dataColumn to set
	 */
	public void setDataColumn(String dataColumn)
	{
		this.dataColumn = dataColumn;
	}

	/**
	 * @return the title
	 */
	public String getTitle()
	{
		return title;
	}

	/**
	 * @param title the title to set
	 */
	public void setTitle(String title)
	{
		this.title = title;
	}

	/**
	 * @return the legendPosition
	 */
	public int getLegendPosition()
	{
		return legendPosition;
	}

	/**
	 * @param legendPosition the legendPosition to set
	 */
	public void setLegendPosition(int legendPosition)
	{
		this.legendPosition = legendPosition;
	}

	/**
	 * @return the showLegend
	 */
	public boolean isShowLegend()
	{
		return showLegend;
	}

	/**
	 * @param showLegend the showLegend to set
	 */
	public void setShowLegend(boolean showLegend)
	{
		this.showLegend = showLegend;
	}

	/**
	 * @return the showTitle
	 */
	public boolean isShowTitle()
	{
		return showTitle;
	}

	/**
	 * @param showTitle the showTitle to set
	 */
	public void setShowTitle(boolean showTitle)
	{
		this.showTitle = showTitle;
	}

	/**
	 * @return the refreshRate
	 */
	public int getRefreshRate()
	{
		return refreshRate;
	}

	/**
	 * @param refreshRate the refreshRate to set
	 */
	public void setRefreshRate(int refreshRate)
	{
		this.refreshRate = refreshRate;
	}

	/**
	 * @return the ignoreZeroValues
	 */
	public boolean isIgnoreZeroValues()
	{
		return ignoreZeroValues;
	}

	/**
	 * @param ignoreZeroValues the ignoreZeroValues to set
	 */
	public void setIgnoreZeroValues(boolean ignoreZeroValues)
	{
		this.ignoreZeroValues = ignoreZeroValues;
	}	
	
	/**
    * @return the minYScaleValue
    */
   public int getMinYScaleValue()
   {
      return minYScaleValue;
   }

   /**
    * @param minYScaleValue the minYScaleValue to set
    */
   public void setMinYScaleValue(int minYScaleValue)
   {
      this.minYScaleValue = minYScaleValue;
   }

   /**
    * @return the maxYScaleValue
    */
   public int getMaxYScaleValue()
   {
      return maxYScaleValue;
   }

   /**
    * @param maxYScaleValue the maxYScaleValue to set
    */
   public void setMaxYScaleValue(int maxYScaleValue)
   {
      this.maxYScaleValue = maxYScaleValue;
   }
   
   public boolean isAutoScale()
   {
      return autoScale;
   }

   public void setAutoScale(boolean autoScale)
   {
      this.autoScale = autoScale;
   }

   /**
    * @return the sortOnDataColumn
    */
   public boolean isSortOnDataColumn()
   {
      return sortOnDataColumn;
   }

   /**
    * @param sortOnDataColumn the sortOnDataColumn to set
    */
   public void setSortOnDataColumn(boolean sortOnDataColumn)
   {
      this.sortOnDataColumn = sortOnDataColumn;
   }

   /**
    * @return the sortDescending
    */
   public boolean isSortDescending()
   {
      return sortDescending;
   }

   /**
    * @param sortDescending the sortDescending to set
    */
   public void setSortDescending(boolean sortDescending)
   {
      this.sortDescending = sortDescending;
   }

   /* (non-Javadoc)
    * @see org.netxms.ui.eclipse.dashboard.widgets.internal.DashboardElementConfig#getObjects()
    */
   @Override
   public Set<Long> getObjects()
   {
      Set<Long> objects = super.getObjects();
      objects.add(nodeId);
      return objects;
   }

   /* (non-Javadoc)
    * @see org.netxms.ui.eclipse.dashboard.widgets.internal.DashboardElementConfig#remapObjects(java.util.Map)
    */
   @Override
   public void remapObjects(Map<Long, ObjectIdMatchingData> remapData)
   {
      super.remapObjects(remapData);
      ObjectIdMatchingData md = remapData.get(nodeId);
      if (md != null)
         nodeId = md.dstId;
   }

   /* (non-Javadoc)
    * @see org.netxms.ui.eclipse.dashboard.widgets.internal.DashboardElementConfig#getDataCollectionItems()
    */
   @Override
   public Map<Long, Long> getDataCollectionItems()
   {
      Map<Long, Long> dcis = new HashMap<Long, Long>();
      dcis.put(dciId,  nodeId);
      return dcis;
   }

   /* (non-Javadoc)
    * @see org.netxms.ui.eclipse.dashboard.widgets.internal.DashboardElementConfig#remapDataCollectionItems(java.util.Map)
    */
   @Override
   public void remapDataCollectionItems(Map<Long, DciIdMatchingData> remapData)
   {
      super.remapDataCollectionItems(remapData);
      DciIdMatchingData md = remapData.get(dciId);
      if (md != null)
      {
         nodeId = md.dstNodeId;
         dciId = md.dstDciId;
      }
   }
   
   /**
    * Set modify Y base
    * 
    * @param modifyYBase if true, use min DCI value as Y base
    */
   public void setModifyYBase(boolean modifyYBase)
   {
      this.modifyYBase = modifyYBase;
   }
   
   /**
    * Modify Y base
    * 
    * @return true if use min DCI value as Y base
    */
   public boolean modifyYBase()
   {
      return modifyYBase;
   }
}
