/**
 * NetXMS - open source network management system
 * Copyright (C) 2003-2014 Victor Kirhenshtein
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
package org.netxms.ui.eclipse.objectbrowser.widgets.internal;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.netxms.base.Glob;
import org.netxms.base.InetAddressEx;
import org.netxms.client.constants.ObjectStatus;
import org.netxms.client.objects.AbstractNode;
import org.netxms.client.objects.AbstractObject;
import org.netxms.client.objects.AccessPoint;
import org.netxms.client.objects.Interface;
import org.netxms.client.objects.ServiceCheck;
import org.netxms.client.objects.Subnet;
import org.netxms.ui.eclipse.shared.ConsoleSharedData;

/**
 * Filter for object tree
 */
public class ObjectFilter extends ViewerFilter
{
	private static final int NONE = 0;
	private static final int NAME = 1;
	private static final int COMMENTS = 2;
	private static final int IP_ADDRESS = 3;
	private static final int OBJECT_ID = 4;
	
	private String filterString = null;
	private boolean hideUnmanaged = false;
	private boolean hideTemplateChecks = false;
	private boolean hideSubInterfaces = false;
	private Map<Long, AbstractObject> objectList = null;
	private AbstractObject lastMatch = null;
	private List<AbstractObject> sourceObjects = null;
	private long[] rootObjects = null;
	private Set<Integer> classFilter = null;
	private boolean usePatternMatching = false;
	private int mode = NAME;
	
	/**
	 * Constructor
	 */
	public ObjectFilter(long[] rootObjects, AbstractObject[] sourceObjects, Set<Integer> classFilter)
	{
		if (rootObjects != null)
		{
			this.rootObjects = new long[rootObjects.length];
			System.arraycopy(rootObjects, 0, this.rootObjects, 0, rootObjects.length);
		}
		this.sourceObjects = (sourceObjects != null) ? Arrays.asList(sourceObjects) : null;
		this.classFilter = classFilter;
	}
	
	/**
	 * Match given value to current filter string
	 * 
	 * @param object object to match
	 * @return true if object matched to current filter string
	 */
	private boolean matchFilterString(AbstractObject object)
	{
		if ((mode == NONE) || (filterString == null))
			return true;
		
		switch(mode)
		{
         case COMMENTS:
            return object.getComments().toLowerCase().contains(filterString);
			case IP_ADDRESS:
			   if (object instanceof AbstractNode)
			   {
			      if (!((AbstractNode)object).getPrimaryIP().isValidAddress())
			         return false;
			      return ((AbstractNode)object).getPrimaryIP().getHostAddress().startsWith(filterString);
			   }
			   else if (object instanceof Subnet)
            {
               return ((Subnet)object).getSubnetAddress().getHostAddress().startsWith(filterString);
            }
            else if (object instanceof Interface)
            {
               for(InetAddressEx a : ((Interface)object).getIpAddressList())
               {
                  if (a.getHostAddress().startsWith(filterString))
                     return true;
               }
               return false;
            }
            else if (object instanceof AccessPoint)
            {
               if (!((AccessPoint)object).getIpAddress().isValidAddress())
                  return false;
               return ((AccessPoint)object).getIpAddress().getHostAddress().startsWith(filterString);
            }
			   return false;
         case NAME:
            return usePatternMatching ? Glob.matchIgnoreCase(filterString, object.getObjectName()) : object.getObjectName().toLowerCase().contains(filterString);
			case OBJECT_ID:
			   if (object instanceof AbstractObject)
			   {
			      long objectID = ((AbstractObject)object).getObjectId();
			      return String.valueOf(objectID).startsWith(filterString);
			   }
            return false;
		}
		
		return false;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ViewerFilter#select(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
	 */
	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element)
	{
		if (classFilter != null)
		{
			if (!classFilter.contains(((AbstractObject)element).getObjectClass()))
				return false;
		}
		
		if (hideUnmanaged && (((AbstractObject)element).getStatus() == ObjectStatus.UNMANAGED))
			return false;
		
		if (hideTemplateChecks && (element instanceof ServiceCheck) && ((ServiceCheck)element).isTemplate())
			return false;
		
      if (hideSubInterfaces && (element instanceof Interface) && (((Interface)element).getParentInterfaceId() != 0))
         return false;
      
		if (objectList == null)
			return true;

		boolean pass = objectList.containsKey(((AbstractObject)element).getObjectId());
		if (!pass && ((AbstractObject)element).hasChildren())
		{
			Iterator<AbstractObject> it = objectList.values().iterator();
			while(it.hasNext())
			{
				AbstractObject obj = it.next();
				if (obj.isChildOf(((AbstractObject)element).getObjectId()))
				{
					pass = true;
					break;
				}
			}
		}
		return pass;
	}
	
	/**
	 * Set filter string
	 * 
	 * @param filterString new filter string
	 */
	public void setFilterString(final String filterString)
	{
		boolean fullSearch = true;
		if ((filterString != null) && !filterString.isEmpty())
		{
			if (filterString.charAt(0) == '/')
			{
				mode = COMMENTS;
            String newFilterString = filterString.substring(1).toLowerCase();
            if ((this.filterString != null) && newFilterString.startsWith(this.filterString))
               fullSearch = false;
            this.filterString = newFilterString;
			}
			else if (filterString.charAt(0) == '>')
			{
				mode = IP_ADDRESS;
				this.filterString = filterString.substring(1);
			}
			else if (filterString.charAt(0) == '#')
			{
			   mode = OBJECT_ID;
			   this.filterString = filterString.substring(1);
			}
			else
			{
				mode = NAME;
				usePatternMatching = filterString.contains("*") || filterString.contains("?"); //$NON-NLS-1$ //$NON-NLS-2$
				if (usePatternMatching)
				{
					this.filterString = filterString.toLowerCase() + "*"; //$NON-NLS-1$
				}
				else
				{
					String newFilterString = filterString.toLowerCase();
					if ((this.filterString != null) && newFilterString.startsWith(this.filterString))
					   fullSearch = false;
					this.filterString = newFilterString;
				}
			}
		}
		else
		{
			this.filterString = null;
			usePatternMatching = false;
			mode = NONE;
		}
		updateObjectList(fullSearch);
	}
	
	/**
	 * Update list of matching objects
	 */
	private void updateObjectList(boolean doFullSearch)
	{
		if (filterString != null)
		{
			if (doFullSearch)
			{
				List<AbstractObject> fullList = (sourceObjects != null) ? sourceObjects : ConsoleSharedData.getSession().getAllObjects();
				objectList = new HashMap<Long, AbstractObject>();
				for(AbstractObject o : fullList)
					if (matchFilterString(o) &&
					    ((rootObjects == null) || o.isChildOf(rootObjects)))
					{
						objectList.put(o.getObjectId(), o);
						lastMatch = o;
					}
			}
			else
			{
				lastMatch = null;
				Iterator<AbstractObject> it = objectList.values().iterator();
				while(it.hasNext())
				{
					AbstractObject obj = it.next();
					if (!matchFilterString(obj))
						it.remove();
					else
						lastMatch = obj;
				}
			}
		}
		else
		{
			objectList = null;
			lastMatch = null;
		}
	}

	/**
	 * Get last matched object
	 * @return Last matched object
	 */
	public final AbstractObject getLastMatch()
	{
		return lastMatch;
	}
	
	/**
	 * Get parent for given object
	 */
	public AbstractObject getParent(final AbstractObject childObject)
	{
		AbstractObject[] parents = childObject.getParentsAsArray();
		for(AbstractObject object : parents)
		{
			if (object != null)
			{
				if ((rootObjects == null) || object.isChildOf(rootObjects))
					return object;
			}
		}
		return null;
	}

	/**
	 * @return the hideUnmanaged
	 */
	public boolean isHideUnmanaged()
	{
		return hideUnmanaged;
	}

	/**
	 * @param hideUnmanaged the hideUnmanaged to set
	 */
	public void setHideUnmanaged(boolean hideUnmanaged)
	{
		this.hideUnmanaged = hideUnmanaged;
	}

	/**
	 * @return the hideTemplateChecks
	 */
	public boolean isHideTemplateChecks()
	{
		return hideTemplateChecks;
	}

	/**
	 * @param hideTemplateChecks the hideTemplateChecks to set
	 */
	public void setHideTemplateChecks(boolean hideTemplateChecks)
	{
		this.hideTemplateChecks = hideTemplateChecks;
	}

	/**
    * @return the hideSubInterfaces
    */
   public boolean isHideSubInterfaces()
   {
      return hideSubInterfaces;
   }

   /**
    * @param hideSubInterfaces the hideSubInterfaces to set
    */
   public void setHideSubInterfaces(boolean hideSubInterfaces)
   {
      this.hideSubInterfaces = hideSubInterfaces;
   }

   /**
	 * @param rootObjects the rootObjects to set
	 */
	public void setRootObjects(long[] rootObjects)
	{
		if (rootObjects != null)
		{
			this.rootObjects = new long[rootObjects.length];
			System.arraycopy(rootObjects, 0, this.rootObjects, 0, rootObjects.length);
		}
		else
		{
			this.rootObjects = null;
		}
	}
}
