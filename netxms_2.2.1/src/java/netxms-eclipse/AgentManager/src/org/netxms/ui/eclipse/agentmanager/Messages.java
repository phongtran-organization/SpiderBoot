package org.netxms.ui.eclipse.agentmanager;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.netxms.ui.eclipse.agentmanager.messages"; //$NON-NLS-1$
	public static String AgentConfigEditorView_Error;
	public static String AgentConfigEditorView_PartName;
	public static String AgentConfigEditorView_Save;
	public static String AgentConfigEditorView_SaveError;
	public static String DeploymentStatusLabelProvider_Completed;
	public static String DeploymentStatusLabelProvider_Failed;
	public static String DeploymentStatusLabelProvider_Init;
	public static String DeploymentStatusLabelProvider_Installing;
	public static String DeploymentStatusLabelProvider_Pending;
	public static String DeploymentStatusLabelProvider_Unknown;
	public static String DeploymentStatusLabelProvider_Uploading;
	public static String OpenAgentConfig_Error;
	public static String OpenAgentConfig_GetConfig;
	public static String OpenAgentConfig_OpenError;
	public static String OpenAgentConfig_OpenErrorPrefix;
	public static String OpenPackageManager_Error;
	public static String OpenPackageManager_ErrorOpenView;
	public static String PackageDeploymentMonitor_ColumnMessage;
	public static String PackageDeploymentMonitor_ColumnNode;
	public static String PackageDeploymentMonitor_ColumnStatus;
	public static String PackageManager_ColumnDescription;
	public static String PackageManager_ColumnFile;
	public static String PackageManager_ColumnID;
	public static String PackageManager_ColumnName;
	public static String PackageManager_ColumnPlatform;
	public static String PackageManager_ColumnVersion;
	public static String PackageManager_ConfirmDeleteText;
	public static String PackageManager_ConfirmDeleteTitle;
	public static String PackageManager_DBUnlockError;
	public static String PackageManager_DeletePackages;
	public static String PackageManager_DeployAction;
	public static String PackageManager_DeployAgentPackage;
	public static String PackageManager_DepStartError;
	public static String PackageManager_Error;
	public static String PackageManager_ErrorOpenView;
	public static String PackageManager_FileTypeAll;
	public static String PackageManager_FileTypePackage;
	public static String PackageManager_Information;
	public static String PackageManager_InstallAction;
	public static String PackageManager_InstallError;
	public static String PackageManager_InstallPackage;
	public static String PackageManager_LoadPkgList;
	public static String PackageManager_OpenDatabase;
	public static String PackageManager_OpenError;
	public static String PackageManager_PkgDeleteError;
	public static String PackageManager_PkgDepCompleted;
	public static String PackageManager_PkgFileOpenError;
	public static String PackageManager_PkgListLoadError;
	public static String PackageManager_RemoveAction;
	public static String PackageManager_SelectFile;
	public static String PackageManager_UnlockDatabase;
	public static String PackageManager_UploadPackage;
	public static String SaveConfigDialog_Cancel;
	public static String SaveConfigDialog_Discard;
	public static String SaveConfigDialog_ModifiedMessage;
	public static String SaveConfigDialog_Save;
	public static String SaveConfigDialog_SaveApply;
	public static String SaveConfigDialog_UnsavedChanges;
	public static String OpenAgentConfigManager_Error;
	public static String OpenAgentConfigManager_ErrorMessage;
	public static String PackageDeploymentMonitor_RestartFailedInstallation;
	public static String SaveStoredConfigDialog_SaveWarning;
	public static String ScreenshotView_AllFiles;
	public static String ScreenshotView_CannotCreateFile;
	public static String ScreenshotView_CannotSaveImage;
	public static String ScreenshotView_CopyToClipboard;
	public static String ScreenshotView_Error;
	public static String ScreenshotView_ErrorWithMsg;
	public static String ScreenshotView_ErrorWithoutMsg;
	public static String ScreenshotView_ErrorNoActiveSessions;
	public static String ScreenshotView_JobError;
	public static String ScreenshotView_JobTitle;
	public static String ScreenshotView_PartTitle;
	public static String ScreenshotView_PngFiles;
	public static String ScreenshotView_Save;
	public static String ScreenshotView_SaveScreenshot;
	public static String ServerStoredAgentConfigEditorView_ConfigFile;
	public static String ServerStoredAgentConfigEditorView_Delete;
	public static String ServerStoredAgentConfigEditorView_Filter;
	public static String ServerStoredAgentConfigEditorView_JobDelete;
	public static String ServerStoredAgentConfigEditorView_JobError_Delete;
	public static String ServerStoredAgentConfigEditorView_JobError_GetContent;
	public static String ServerStoredAgentConfigEditorView_JobError_GetList;
	public static String ServerStoredAgentConfigEditorView_JobError_MoveDown;
	public static String ServerStoredAgentConfigEditorView_JobError_MoveUp;
	public static String ServerStoredAgentConfigEditorView_JobError_Save;
	public static String ServerStoredAgentConfigEditorView_JobMoveDown;
	public static String ServerStoredAgentConfigEditorView_JobMoveUp;
	public static String ServerStoredAgentConfigEditorView_JobSave;
	public static String ServerStoredAgentConfigEditorView_JobTitle_CreateNew;
	public static String ServerStoredAgentConfigEditorView_JobTitle_GetContent;
	public static String ServerStoredAgentConfigEditorView_MoveDown;
	public static String ServerStoredAgentConfigEditorView_MoveUp;
	public static String ServerStoredAgentConfigEditorView_Name;
	public static String ServerStoredAgentConfigEditorView_Noname;
	public static String TakeScreenshot_Error;
	public static String TakeScreenshot_ErrorOpeningView;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}

	private static Messages instance = new Messages();

	public static Messages get() {
		return instance;
	}

}
