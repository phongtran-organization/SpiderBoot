/**
 * NetXMS - open source network management system
 * Copyright (C) 2003-2012 Victor Kirhenshtein
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
package org.netxms.ui.eclipse.osm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;
import org.netxms.base.GeoLocation;
import org.netxms.client.NXCSession;
import org.netxms.client.SessionListener;
import org.netxms.client.SessionNotification;
import org.netxms.client.objects.AbstractObject;
import org.netxms.ui.eclipse.osm.tools.Area;
import org.netxms.ui.eclipse.osm.tools.QuadTree;
import org.netxms.ui.eclipse.shared.ConsoleSharedData;

/**
 * Cache for objects' geolocation information
 */
public class GeoLocationCache implements SessionListener
{
	public static final int CENTER = 0;
	public static final int TOP_LEFT = 1;
	public static final int BOTTOM_RIGHT = 2;
	
	private static final int TILE_SIZE = 256;
	
	private Map<Long, AbstractObject> objects = new HashMap<Long, AbstractObject>();
	private QuadTree<Long> locationTree = new QuadTree<Long>();
	private NXCSession session;
	private Set<GeoLocationCacheListener> listeners = new HashSet<GeoLocationCacheListener>();
	
	/* (non-Javadoc)
	 * @see org.netxms.api.client.SessionListener#notificationHandler(org.netxms.api.client.SessionNotification)
	 */
	@Override
	public void notificationHandler(SessionNotification n)
	{
		if (n.getCode() == SessionNotification.OBJECT_CHANGED)
			onObjectChange((AbstractObject)n.getObject());
		else if (n.getCode() == SessionNotification.OBJECT_SYNC_COMPLETED)
			internalInitialize();
	}
	
	/**
	 * Initialize location cache
	 */
	void initialize(NXCSession session)
	{
		this.session = session;
		if (session.isObjectsSynchronized())
			internalInitialize();
		session.addListener(this);
	}
	
	/**
	 * (Re)initialize location cache - actual initialization
	 */
	private void internalInitialize()
	{
		synchronized(locationTree)
		{
			locationTree.removeAll();
			objects.clear();
		
			for(AbstractObject object : session.getAllObjects())
			{
				if ((object.getObjectClass() == AbstractObject.OBJECT_NODE) ||
					 (object.getObjectClass() == AbstractObject.OBJECT_MOBILEDEVICE) ||
					 (object.getObjectClass() == AbstractObject.OBJECT_CLUSTER) ||
					 (object.getObjectClass() == AbstractObject.OBJECT_CONTAINER) ||
					 (object.getObjectClass() == AbstractObject.OBJECT_RACK))
				{
					GeoLocation gl = object.getGeolocation();
					if (gl.getType() != GeoLocation.UNSET)
					{
						objects.put(object.getObjectId(), object);
						locationTree.insert(gl.getLatitude(), gl.getLongitude(), object.getObjectId());
					}
				}
			}
		}		
	}
	
	/**
	 * Handle object change
	 * 
	 * @param object
	 */
	private void onObjectChange(AbstractObject object)
	{
		if ((object.getObjectClass() != AbstractObject.OBJECT_NODE) &&
			 (object.getObjectClass() != AbstractObject.OBJECT_MOBILEDEVICE) &&
		    (object.getObjectClass() != AbstractObject.OBJECT_CLUSTER) &&
		    (object.getObjectClass() != AbstractObject.OBJECT_CONTAINER) &&
          (object.getObjectClass() != AbstractObject.OBJECT_RACK))
			return;
		
		GeoLocation prevLocation = null;
		boolean cacheChanged = false;
		synchronized(locationTree)
		{
			GeoLocation gl = object.getGeolocation();
			if (gl.getType() == GeoLocation.UNSET)
			{
				AbstractObject prevObject = objects.remove(object.getObjectId());
				if (prevObject != null)
					prevLocation = prevObject.getGeolocation();
				cacheChanged = locationTree.remove(object.getObjectId());
			}
			else
			{
				if (!objects.containsKey(object.getObjectId()))
				{
					locationTree.insert(gl.getLatitude(), gl.getLongitude(), object.getObjectId());
					cacheChanged = true;
				}
				else
				{
					prevLocation = objects.get(object.getObjectId()).getGeolocation();
					if (!gl.equals(prevLocation))
					{
						locationTree.remove(object.getObjectId());
						locationTree.insert(gl.getLatitude(), gl.getLongitude(), object.getObjectId());
						cacheChanged = true;
					}
				}
				objects.put(object.getObjectId(), object);
			}
		}
		
		// Notify listeners about cache change
		if (cacheChanged)
			synchronized(listeners)
			{
				for(GeoLocationCacheListener l : listeners)
					l.geoLocationCacheChanged(object, prevLocation);
			}
	}
	
	/**
	 * Get all objects in given area. If parent ID is set, only objects under given parent will be included.
	 * 
	 * @param area geographical area
	 * @param parentId parent object ID or 0
	 * @param filterString object name filter
	 * @return
	 */
	public List<AbstractObject> getObjectsInArea(Area area, long parentId, String filterString)
	{
		List<AbstractObject> list = null;
		synchronized(locationTree)
		{
			List<Long> idList = locationTree.query(area);
			list = new ArrayList<AbstractObject>(idList.size());
			for(Long id : idList)
			{
				AbstractObject o = objects.get(id);
				if ((o != null) && ((parentId == 0) || (o.getObjectId() == parentId) || o.isChildOf(parentId)) &&
					 (filterString != null ? o.getObjectName().toLowerCase().contains(filterString) : true))
					list.add(o);
			}
		}
		return list;
	}
	
	/**
	 * Add cache listener
	 * 
	 * @param listener
	 */
	public void addListener(GeoLocationCacheListener listener)
	{
		synchronized(listeners)
		{
			listeners.add(listener);
		}
	}
	
	/**
	 * Remove cache listener
	 * 
	 * @param listener
	 */
	public void removeListener(GeoLocationCacheListener listener)
	{
		synchronized(listeners)
		{
			listeners.remove(listener);
		}
	}
	
	/**
	 * Convert location to abstract display coordinates (coordinates on virtual map covering entire world)
	 * 
	 * @param location geolocation
	 * @param zoom zoom level
	 * @return abstract display coordinates
	 */
	public static Point coordinateToDisplay(GeoLocation location, int zoom)
	{
		final double numberOfTiles = Math.pow(2, zoom);
		
		// LonToX
		double x = (location.getLongitude() + 180) * (numberOfTiles * TILE_SIZE) / 360.0;
	    
		// LatToY
		double y = 1 - (Math.log(Math.tan(Math.PI / 4 + Math.toRadians(location.getLatitude()) / 2)) / Math.PI);
		y = y / 2 * (numberOfTiles * TILE_SIZE);
		return new Point((int)x, (int)y);
	}
	
	/**
	 * Convert abstract display coordinates (coordinates on virtual map covering entire world) to location
	 * 
	 * @param point display coordinates
	 * @param zoom zoom level
	 * @return location for given point
	 */
	public static GeoLocation displayToCoordinates(Point point, int zoom)
	{
	   double longitude = (point.x * (360 / (Math.pow(2, zoom) * 256))) - 180;
		double latitude = (1 - (point.y * (2 / (Math.pow(2, zoom) * 256)))) * Math.PI;
		latitude = Math.toDegrees(Math.atan(Math.sinh(latitude)));
		if (latitude <= (-89.998))
		   latitude = -89.997; // Needed because tan() is undefined for angles of 90 degrees
		
		return new GeoLocation(latitude, longitude);
	}
	
	/**
	 * Calculate map coverage.
	 * 
	 * @param mapSize map size in pixels
	 * @param point coordinates of map's base point
	 * @param pointLocation location of base point: TOP-LEFT, BOTTOM-RIGHT, CENTER
	 * @param zoom zoom level
	 * @return area covered by map
	 */
	public static Area calculateCoverage(Point mapSize, GeoLocation basePoint, int pointLocation, int zoom)
	{
		Point bp = coordinateToDisplay(basePoint, zoom);
		GeoLocation topLeft = null;
		GeoLocation bottomRight = null;
		switch(pointLocation)
		{
			case CENTER:
				topLeft = displayToCoordinates(new Point(bp.x - mapSize.x / 2, bp.y - mapSize.y / 2), zoom);
				bottomRight = displayToCoordinates(new Point(bp.x + mapSize.x / 2, bp.y + mapSize.y / 2), zoom);
				break;
			case TOP_LEFT:
				topLeft = displayToCoordinates(new Point(bp.x, bp.y), zoom);
				bottomRight = displayToCoordinates(new Point(bp.x + mapSize.x, bp.y + mapSize.y), zoom);
				break;
			case BOTTOM_RIGHT:
				topLeft = displayToCoordinates(new Point(bp.x - mapSize.x, bp.y - mapSize.y), zoom);
				bottomRight = displayToCoordinates(new Point(bp.x, bp.y), zoom);
				break;
			default:
				throw new IllegalArgumentException("pointLocation=" + pointLocation); //$NON-NLS-1$
		}	
		return new Area(topLeft.getLatitude(), topLeft.getLongitude(), bottomRight.getLatitude(), bottomRight.getLongitude());
	}

	/**
	 * @return the instance
	 */
	public static GeoLocationCache getInstance()
	{
	   return getInstance(Display.getCurrent());
	}

   /**
    * @param display
    * @return
    */
   public static GeoLocationCache getInstance(Display display)
   {
      GeoLocationCache instance = (GeoLocationCache)ConsoleSharedData.getProperty(display, "GeoLocationCache");
      if (instance == null)
         instance = new GeoLocationCache();
      return instance;
   }
}
