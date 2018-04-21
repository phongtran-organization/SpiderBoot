package org.netxms.ui.eclipse.serverconfig.views.helpers;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.netxms.client.server.ServerVariable;
import org.netxms.ui.eclipse.serverconfig.views.ServerConfigurationEditor;
import org.netxms.ui.eclipse.widgets.SortableTableViewer;

/**
 * Comparator for server configuration variables
 */
public class ServerVariableComparator extends ViewerComparator
{
	/**
	 * Compare two booleans and return -1, 0, or 1
	 */
	private int compareBooleans(boolean b1, boolean b2)
	{
		return (!b1 && b2) ? -1 : ((b1 && !b2) ? 1 : 0);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ViewerComparator#compare(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
	 */
	@Override
	public int compare(Viewer viewer, Object e1, Object e2)
	{
		int result;
		
		switch((Integer)((SortableTableViewer)viewer).getTable().getSortColumn().getData("ID")) //$NON-NLS-1$
		{
			case ServerConfigurationEditor.COLUMN_NAME:
				result = ((ServerVariable)e1).getName().compareToIgnoreCase(((ServerVariable)e2).getName());
				break;
			case ServerConfigurationEditor.COLUMN_VALUE:
			   if (((ServerVariable)e1).getDataType().equals("C") && ((ServerVariable)e2).getDataType().equals("C"))
			      result = ((ServerVariable)e1).getValueDescription().compareToIgnoreCase(((ServerVariable)e2).getValueDescription());
			   else
			      result = ((ServerVariable)e1).getValue().compareToIgnoreCase(((ServerVariable)e2).getValue());
				break;
			case ServerConfigurationEditor.COLUMN_DEFAULT_VALUE:
			   result = ((ServerVariable)e1).getDefaultValue().compareToIgnoreCase(((ServerVariable)e2).getDefaultValue());
			   break;
			case ServerConfigurationEditor.COLUMN_NEED_RESTART:
				result = compareBooleans(((ServerVariable)e1).isServerRestartNeeded(), ((ServerVariable)e2).isServerRestartNeeded());
				break;
        case ServerConfigurationEditor.COLUMN_DESCRIPTION:
            result = ((ServerVariable)e1).getDescription().compareToIgnoreCase(((ServerVariable)e2).getDescription());
            break;
			default:
				result = 0;
				break;
		}
		return (((SortableTableViewer)viewer).getTable().getSortDirection() == SWT.UP) ? result : -result;
	}
}
