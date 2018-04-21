/**
 * NetXMS - open source network management system
 * Copyright (C) 2003-2011 Alex Kirhenshtein
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
package org.netxms.client;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.UUID;
import org.netxms.base.NXCPCodes;
import org.netxms.base.NXCPMessage;

/**
 * This class represents image in image library
 * 
 */
public class LibraryImage implements Comparable<LibraryImage> {
	private UUID guid;
	private String name;
	private String category;
	private String mimeType;
	private byte[] binaryData;
	private boolean imageProtected;
	private boolean complete = false;

	/**
	 * 
	 * @param guid
	 *            GUID
	 */
	public LibraryImage(final UUID guid) {
		this.guid = guid;
	}

	/**
	 * 
	 * @param guid
	 *            GUID
	 * @param name
	 *            name
	 * @param category
	 *            category
	 * @param mimeType
	 *            mime type
	 * @param imageProtected
	 *            defines if image is protected
	 */
	public LibraryImage(final UUID guid, final String name,
			final String category, final String mimeType,
			final boolean imageProtected) {
		this.guid = guid;
		this.name = name;
		this.category = category;
		this.mimeType = mimeType;
		this.imageProtected = imageProtected;
	}

	/**
	 * Create object from NXCP message
	 * 
	 * @param msg
	 *            Message containing object's data
	 * @param imageFile
	 *            image file
	 */
	public LibraryImage(final NXCPMessage msg, File imageFile) {
		guid = msg.getFieldAsUUID(NXCPCodes.VID_GUID);
		name = msg.getFieldAsString(NXCPCodes.VID_NAME);
		category = msg.getFieldAsString(NXCPCodes.VID_CATEGORY);
		mimeType = msg.getFieldAsString(NXCPCodes.VID_IMAGE_MIMETYPE);
		imageProtected = msg.getFieldAsBoolean(NXCPCodes.VID_IMAGE_PROTECTED);

		binaryData = new byte[(int) imageFile.length()];
		InputStream in = null;
		try {
			in = new FileInputStream(imageFile);
			in.read(binaryData);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (in != null) {
					in.close();
				}
				imageFile.delete();
			} catch (Exception e) {
			}
		}

		this.complete = true;
	}

	/**
	 * Default constructor
	 */
	public LibraryImage() {
	}

	/**
	 * Fill NXCP message with object data
	 * 
	 * @param msg
	 *            NXCP message
	 */
	public void fillMessage(final NXCPMessage msg) {
		if (guid != null) {
			msg.setField(NXCPCodes.VID_GUID, guid);
		}
		msg.setField(NXCPCodes.VID_NAME, name);
		if (category != null) {
			msg.setField(NXCPCodes.VID_CATEGORY, category);
		}
	}

	/**
	 * @return the guid
	 */
	public UUID getGuid() {
		return guid;
	}

	/**
	 * @param guid
	 *            the guid to set
	 */
	public void setGuid(UUID guid) {
		this.guid = guid;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the category
	 */
	public String getCategory() {
		return category;
	}

	/**
	 * @param category
	 *            the category to set
	 */
	public void setCategory(String category) {
		this.category = category;
	}

	/**
	 * @return the binaryData
	 */
	public byte[] getBinaryData() {
		return binaryData;
	}

	/**
	 * @param binaryData
	 *            the binaryData to set
	 */
	public void setBinaryData(byte[] binaryData) {
		this.binaryData = binaryData;
	}

	/**
	 * @return is image protected?
	 */
	public boolean isProtected() {
		return imageProtected;
	}

	/**
	 * @return is complete (contains binary data)?
	 */
	public boolean isComplete() {
		return complete;
	}

	/**
	 * @return the mimeType
	 */
	public String getMimeType() {
		return mimeType;
	}

	/**
	 * @param mimeType
	 *            the mimeType to set
	 */
	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "LibraryImage [guid=" + guid + ", name=" + name + ", category="
				+ category + ", mimeType=" + mimeType + ", binaryData="
				+ (binaryData == null ? "[null]" : "[not null]")
				+ ", imageProtected=" + imageProtected + ", complete="
				+ complete + "]";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(LibraryImage o) {
		return name.compareToIgnoreCase(o.getName());
	}
}
