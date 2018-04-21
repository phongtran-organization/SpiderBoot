/**
 * 
 */
package org.netxms.client.server;

import java.util.Date;
import java.util.List;
import org.netxms.base.NXCPMessage;
import org.netxms.client.AgentFileInfo;

/**
 * Represents information about file in server's file store
 * 
 */
public class AgentFile {
	public static final int FILE = 1;
	public static final int DIRECTORY = 2;
	public static final int SYMBOLYC_LINK = 4;
	public static final int PLACEHOLDER = 65536;

	private String name;
	private long size;
	private Date modificationTime;
	private String extension;
	private int type = 0;
	private String owner;
	private String group;
	private String accessRights;
	private List<AgentFile> children;
	private AgentFile parent;
	private long nodeId;
	private AgentFileInfo info;

	/**
	 * Create server file object from NXCP message.
	 * 
	 * @param msg
	 *            NXCP message
	 * @param baseId
	 *            base variable ID
	 * @param parent
	 *            parent file for this file
	 * @param nodeId
	 *            source node of file
	 */
	public AgentFile(NXCPMessage msg, long baseId, AgentFile parent, long nodeId) {
		name = msg.getFieldAsString(baseId);
		size = msg.getFieldAsInt64(baseId + 1);
		modificationTime = msg.getFieldAsDate(baseId + 2);
		type = (int) msg.getFieldAsInt64(baseId + 3);
		// +4 full name
		owner = msg.getFieldAsString(baseId + 5);
		group = msg.getFieldAsString(baseId + 6);
		accessRights = msg.getFieldAsString(baseId + 7);
		this.parent = parent;
		this.nodeId = nodeId;
		setExtension();
	}

	/**
	 * Constructor for AgentFile
	 * 
	 * @param name
	 *            file name
	 * @param fileType
	 *            file type
	 * @param parent
	 *            parent file for this file
	 * @param nodeId
	 *            source node of file
	 */
	public AgentFile(String name, int fileType, AgentFile parent, long nodeId) {
		this.name = name;
		this.type = fileType;
		this.parent = parent;
		this.nodeId = nodeId;
		this.modificationTime = new Date();
		setExtension();
	}

	/**
	 * Set file extension
	 */
	private void setExtension() {
		if (isDirectory() || name.startsWith(".")) {
			extension = " ";
			return;
		}

		String[] parts = name.split("\\."); //$NON-NLS-1$
		if (parts.length > 1) {
			extension = parts[parts.length - 1];
		} else {
			extension = " ";
		}
	}

	public boolean isDirectory() {
		return (type & DIRECTORY) > 0 ? true : false;
	}

	public boolean isPlaceholder() {
		return (type & PLACEHOLDER) > 0 ? true : false;
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
		setExtension();
	}

	/**
	 * @return the size
	 */
	public long getSize() {
		return size;
	}

	/**
	 * @return the modifyicationTime
	 */
	public Date getModifyicationTime() {
		return modificationTime;
	}

	/**
	 * @return the type
	 */
	public String getExtension() {
		return extension;
	}

	/**
	 * @return the children
	 */
	public List<AgentFile> getChildren() {
		return children;
	}

	/**
	 * @param children
	 *            the children to set
	 */
	public void setChildren(List<AgentFile> children) {
		this.children = children;
	}

	/**
	 * @param chield
	 *            to be removed
	 */
	public void removeChield(AgentFile chield) {
		for (int i = 0; i < children.size(); i++) {
			if (children.get(i).getName().equalsIgnoreCase(chield.getName())) {
				children.remove(i);
			}
		}
	}

	/**
	 * @param chield
	 *            to be added
	 */
	public void addChield(AgentFile chield) {
		if (children == null)
			return;
		boolean childReplaced = false;
		for (int i = 0; i < children.size(); i++) {
			if (children.get(i).getName().equalsIgnoreCase(chield.getName())) {
				if (chield.getType() != DIRECTORY)
					children.set(i, chield);
				childReplaced = true;
				break;
			}
		}
		if (!childReplaced) {
			children.add(chield);
		}
	}

	/**
	 * @return the fullName
	 */
	public String getFullName() {
		if (parent == null) {
			return name;
		}
		return parent.getFullName() + "/" + name;
	}

	/**
	 * @return the fullName
	 */
	public String getFilePath() {
		if (parent == null) {
			return name;
		}
		return parent.getFilePath()
				+ ((parent.getFilePath().endsWith("/") || parent.getFilePath()
						.endsWith("\\")) ? ""
						: ((parent.getFilePath().contains("\\") || parent
								.getFilePath().contains(":")) ? "\\" : "/"))
				+ name;
	}

	/**
	 * @return the parent
	 */
	public AgentFile getParent() {
		return parent;
	}

	/**
	 * @param parent
	 *            the parent to set
	 */
	public void setParent(AgentFile parent) {
		this.parent = parent;
	}

	/**
	 * @return the nodeId
	 */
	public long getNodeId() {
		return nodeId;
	}

	/**
	 * @return the type
	 */
	public int getType() {
		return type;
	}

	/**
	 * @return the owner
	 */
	public String getOwner() {
		return owner;
	}

	/**
	 * @param owner
	 *            the owner to set
	 */
	public void setOwner(String owner) {
		this.owner = owner;
	}

	/**
	 * @return the group
	 */
	public String getGroup() {
		return group;
	}

	/**
	 * @param group
	 *            the group to set
	 */
	public void setGroup(String group) {
		this.group = group;
	}

	/**
	 * @return the accessRights
	 */
	public String getAccessRights() {
		return accessRights;
	}

	/**
	 * @param accessRights
	 *            the accessRights to set
	 */
	public void setAccessRights(String accessRights) {
		this.accessRights = accessRights;
	}

	/**
	 * @param info
	 *            Set agent file info
	 */
	public void setFileInfo(AgentFileInfo info) {
		this.info = info;
	}

	/**
	 * @return Agent File Info
	 */
	public AgentFileInfo getFileInfo() {
		return info;
	}
}
