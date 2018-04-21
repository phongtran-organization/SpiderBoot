package org.netxms.ui.eclipse.filemanager;

import org.eclipse.osgi.util.NLS;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.swt.widgets.Display;


public class Messages extends NLS
{
	private static final String BUNDLE_NAME = "org.netxms.ui.eclipse.filemanager.messages"; //$NON-NLS-1$
	public String AgentFileManager_AllFiles;
   public String AgentFileManager_ColAccessRights;
   public String AgentFileManager_ColDate;
   public String AgentFileManager_ColGroup;
   public String AgentFileManager_ColName;
   public String AgentFileManager_ColOwner;
   public String AgentFileManager_ColSize;
   public String AgentFileManager_ColType;
   public String AgentFileManager_CreateFolder;
   public String AgentFileManager_CreatingFolder;
   public String AgentFileManager_Delete;
   public String AgentFileManager_DirectoryReadError;
   public String AgentFileManager_Download;
   public String AgentFileManager_DownloadFileFromAgent;
   public String AgentFileManager_DownloadJobError;
   public String AgentFileManager_DownloadJobTitle;
   public String AgentFileManager_Error;
   public String AgentFileManager_FileDownloadError;
   public String AgentFileManager_FolderCreationError;
   public String AgentFileManager_FollowChanges;
   public String AgentFileManager_MoveError;
   public String AgentFileManager_MoveFile;
   public String AgentFileManager_OpenViewError;
   public String AgentFileManager_PartTitle;
   public String AgentFileManager_RefreshFolder;
   public String AgentFileManager_Rename;
   public String AgentFileManager_RenameError;
   public String AgentFileManager_Show;
   public String AgentFileManager_StartDownloadDialogTitle;
   public String AgentFileManager_UploadFile;
   public String AgentFileManager_UploadFileJobTitle;
   public String AgentFileManager_UploadFolder;
   public String AgentFileManager_UploadFolderJobTitle;
   public String AgentFileViewer_Copy;
   public String AgentFileViewer_FileIsTooLarge;
   public String AgentFileViewer_FileIsTooLargeMessageText;
   public String AgentFileViewer_Find;
   public String AgentFileViewer_SelectAll;
   public String BaseFileViewer_Close;
   public String BaseFileViewer_Find;
   public String BaseFileViewer_FindInFile;
   public String BaseFileViewer_HideMessage;
   public String BaseFileViewer_LoadJobError;
   public String BaseFileViewer_LoadJobName;
   public String CreateFolderDialog_Label;
   public String CreateFolderDialog_Title;
   public String DynamicFileViewer_CannotRestartFileTracking;
   public String DynamicFileViewer_FileTrackingFailed;
   public String DynamicFileViewer_RestartFileTracking;
   public String DynamicFileViewer_TrackFileChanges;
   public String FileViewer_Cannot_Stop_File_Monitoring;
   public String FileViewer_ClearOutput;
   public String FileViewer_Download_File_Updates;
   public String FileViewer_InvalidObjectID;
   public String FileViewer_NotifyFollowConnectionEnabed;
   public String FileViewer_NotifyFollowConnectionLost;
   public String FileViewer_ScrollLock;
   public String FileViewer_Stop_File_Monitoring;
   public String GetServerFileList_ErrorMessageFileView;
   public String GetServerFileList_ErrorMessageFileViewTitle;
   public String LocalFileSelector_AllFiles;
	public String LocalFileSelector_None;
	public String LocalFileSelector_SelectFile;
	public String LocalFileSelector_Tooltip;
   public String OpenFileManager_Error;
   public String OpenFileManager_ErrorText;
	public String RenameFileDialog_NewName;
   public String RenameFileDialog_OldName;
   public String RenameFileDialog_Title;
   public String RenameFileDialog_Warning;
   public String RenameFileDialog_WarningMessage;
   public String SelectServerFileDialog_ColModTime;
	public String SelectServerFileDialog_ColName;
	public String SelectServerFileDialog_ColSize;
	public String SelectServerFileDialog_JobError;
	public String SelectServerFileDialog_JobTitle;
	public String SelectServerFileDialog_Title;
	public String SelectServerFileDialog_Warning;
	public String SelectServerFileDialog_WarningText;
	public String ServerFileSelector_None;
	public String ServerFileSelector_Tooltip;
	public String StartClientToAgentFolderUploadDialog_Title;
   public String StartClientToServerFileUploadDialog_LocalFile;
	public String StartClientToServerFileUploadDialog_RemoteFileName;
	public String StartClientToServerFileUploadDialog_Title;
	public String StartClientToServerFileUploadDialog_Warning;
	public String StartClientToServerFileUploadDialog_WarningText;
	public String StartServerToAgentFileUploadDialog_CreateJobOnHold;
	public String StartServerToAgentFileUploadDialog_RemoteFileName;
	public String StartServerToAgentFileUploadDialog_ScheduleTask;
   public String StartServerToAgentFileUploadDialog_ServerFile;
	public String StartServerToAgentFileUploadDialog_Title;
	public String StartServerToAgentFileUploadDialog_Warning;
	public String StartServerToAgentFileUploadDialog_WarningText;
	public String UploadFileToAgent_JobError;
	public String UploadFileToAgent_JobTitle;
	public String UploadFileToServer_JobError;
	public String UploadFileToServer_JobTitle;
	public String UploadFileToServer_TaskNamePrefix;
	public String ViewAgentFilesProvider_JobError;
   public String ViewAgentFilesProvider_JobTitle;
   public String ViewAgentFilesProvider_Loading;
   public String ViewServerFile_DeletAck;
   public String ViewServerFile_DeleteConfirmation;
   public String ViewServerFile_DeleteFileOnServerAction;
   public String ViewServerFile_DeletFileFromServerJob;
   public String ViewServerFile_ErrorDeleteFileJob;
   public String ViewServerFile_FileName;
   public String ViewServerFile_FileSize;
   public String ViewServerFile_FileType;
   public String ViewServerFile_ModificationDate;
   public String ViewServerFile_ShowFilterAction;
   public String ViewServerFile_UploadFileOnServerAction;
   static
	{
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	

private Messages()
	{
	}


	/**
	 * Get message class for current locale
	 *
	 * @return
	 */
	public static Messages get()
	{
		return RWT.NLS.getISO8859_1Encoded(BUNDLE_NAME, Messages.class);
	}

	/**
	 * Get message class for current locale
	 *
	 * @return
	 */
	public static Messages get(Display display)
	{
		CallHelper r = new CallHelper();
		display.syncExec(r);
		return r.messages;
	}

	/**
	 * Helper class to call RWT.NLS.getISO8859_1Encoded from non-UI thread
	 */
	private static class CallHelper implements Runnable
	{
		Messages messages;

		@Override
		public void run()
		{
			messages = RWT.NLS.getISO8859_1Encoded(BUNDLE_NAME, Messages.class);
		}
	}

}
