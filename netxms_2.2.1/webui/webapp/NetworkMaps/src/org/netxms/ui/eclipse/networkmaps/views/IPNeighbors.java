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
package org.netxms.ui.eclipse.networkmaps.views;

import java.util.Iterator;

import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.netxms.client.objects.AbstractObject;
import org.netxms.client.objects.Node;
import org.netxms.client.objects.Subnet;
import org.netxms.client.objects.VPNConnector;
import org.netxms.client.maps.NetworkMapLink;
import org.netxms.client.maps.NetworkMapPage;
import org.netxms.client.maps.elements.NetworkMapObject;
import org.netxms.ui.eclipse.networkmaps.Messages;

/**
 * IP neighbors for given node
 */
public class IPNeighbors extends AbstractNetworkMapView
{
	public static final String ID = "org.netxms.ui.eclipse.networkmaps.view.ip_neighbors"; //$NON-NLS-1$
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.ViewPart#init(org.eclipse.ui.IViewSite)
	 */
	@Override
	public void init(IViewSite site) throws PartInitException
	{
		super.init(site);
		setPartName(Messages.get().IPNeighbors_PartName + rootObject.getObjectName());
	}

	/**
	 * Build map page
	 */
	protected void buildMapPage()
	{
		mapPage = new NetworkMapPage(ID + "." + this.toString()); //$NON-NLS-1$

		long rootElementId = mapPage.createElementId();
		mapPage.addElement(new NetworkMapObject(rootElementId, rootObject.getObjectId()));
		
		addSubnets(rootObject, rootElementId);

      for(long objectId : rootObject.getChildIdList())
      {
         AbstractObject object = session.findObjectById(objectId);
         if ((object != null) && (object instanceof VPNConnector))
         {
            AbstractObject peer = session.findObjectById(((VPNConnector)object).getPeerGatewayId());
            if (peer != null)
            {
               long elementId = mapPage.createElementId();
               mapPage.addElement(new NetworkMapObject(elementId, peer.getObjectId()));
               NetworkMapLink link = new NetworkMapLink(NetworkMapLink.VPN, rootElementId, elementId);
               link.setName(object.getObjectName());
               mapPage.addLink(link);
               addSubnets(peer, elementId);
            }
         }
      }

		addDciToRequestList();
	}
	
	/**
	 * Add subnets connected by given node
	 * 
	 * @param root
	 * @param rootElementId
	 */
	private void addSubnets(AbstractObject root, long rootElementId)
	{
      for(long objectId : root.getParentIdList())
      {
         AbstractObject object = session.findObjectById(objectId);
         if ((object != null) && (object instanceof Subnet))
         {
            long elementId = mapPage.createElementId();
            mapPage.addElement(new NetworkMapObject(elementId, objectId));
            mapPage.addLink(new NetworkMapLink(NetworkMapLink.NORMAL, rootElementId, elementId));
            addNodesFromSubnet((Subnet)object, elementId, root.getObjectId());
         }
      }  
	}

	/**
	 * Add nodes connected to given subnet to map
	 * @param subnet Subnet object
	 * @param rootNodeId ID of map's root node (used to prevent recursion)
	 */
	private void addNodesFromSubnet(Subnet subnet, long subnetElementId, long rootNodeId)
	{
		Iterator<Long> it = subnet.getChildren();
		while(it.hasNext())
		{
			long objectId = it.next();
			if (objectId != rootNodeId)
			{
				AbstractObject object = session.findObjectById(objectId);
				if ((object != null) && (object instanceof Node))
				{
					long elementId = mapPage.createElementId();
					mapPage.addElement(new NetworkMapObject(elementId, objectId));
					mapPage.addLink(new NetworkMapLink(NetworkMapLink.NORMAL, subnetElementId, elementId));
				}
			}
		}
      addDciToRequestList();
	}
}
