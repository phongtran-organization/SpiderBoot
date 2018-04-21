package org.netxms.ui.eclipse.alarmviewer;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.netxms.ui.eclipse.alarmviewer.messages"; //$NON-NLS-1$

	public static String AcknowledgeAlarm_ErrorMessage;
	public static String AcknowledgeAlarm_JobName;
	public static String AcknowledgeAlarm_TaskName;
	public static String AcknowledgeCustomTimeDialog_ConfigurationInfoLabel;
	public static String AcknowledgeCustomTimeDialog_CustomTimeDialogTitle;
	public static String AcknowledgeCustomTimeDialog_Days;
	public static String AcknowledgeCustomTimeDialog_Hours;
	public static String AcknowledgeCustomTimeDialog_Minutes;
	public static String AcknowledgeCustomTimeDialog_Warning;
	public static String AcknowledgeCustomTimeDialog_WarningZeroTime;
	public static String AlarmAcknowledgeTimeFunctions_day;
	public static String AlarmAcknowledgeTimeFunctions_days;
	public static String AlarmAcknowledgeTimeFunctions_hour;
	public static String AlarmAcknowledgeTimeFunctions_hours;
	public static String AlarmAcknowledgeTimeFunctions_minute;
	public static String AlarmAcknowledgeTimeFunctions_minutes;
	public static String AlarmAcknowledgeTimeFunctions_ZeroMinutesEntry;
	public static String AlarmComments_AckToDeleteComment;
	public static String AlarmComments_AddCommentJob;
	public static String AlarmComments_AddCommentLink;
	public static String AlarmComments_AddError;
	public static String AlarmComments_Comments;
	public static String AlarmComments_Confirmation;
	public static String AlarmComments_DeleteCommentJob;
	public static String AlarmComments_Details;
	public static String AlarmComments_ErrorDeleteAlarmComment;
	public static String AlarmComments_GetComments;
	public static String AlarmComments_GetError;
	public static String AlarmComments_InternalError;
	public static String AlarmCommentsEditor_DeleteLabel;
	public static String AlarmCommentsEditor_Edit;
	public static String AlarmCommentsEditor_Unknown;
	public static String AlarmComparator_Unknown;
	public static String AlarmDetails_Column_Message;
	public static String AlarmDetails_Column_Name;
	public static String AlarmDetails_Column_Severity;
	public static String AlarmDetails_Column_Source;
	public static String AlarmDetails_Column_Timestamp;
	public static String AlarmDetails_LastValues;
	public static String AlarmDetails_Overview;
	public static String AlarmDetails_RefreshJobError;
	public static String AlarmDetails_RefreshJobTitle;
	public static String AlarmDetails_RelatedEvents;
	public static String AlarmDetailsProvider_Error;
	public static String AlarmDetailsProvider_ErrorOpeningView;
	public static String AlarmList_AckBy;
	public static String AlarmList_Acknowledge;
	public static String AlarmList_ActionAlarmDetails;
	public static String AlarmList_ActionObjectDetails;
	public static String AlarmList_CannotResoveAlarm;
	public static String AlarmList_ColumnCount;
	public static String AlarmList_ColumnCreated;
	public static String AlarmList_ColumnLastChange;
	public static String AlarmList_ColumnMessage;
	public static String AlarmList_ColumnSeverity;
	public static String AlarmList_ColumnSource;
	public static String AlarmList_ColumnState;
	public static String AlarmList_Comments;
	public static String AlarmList_CopyMsgToClipboard;
	public static String AlarmList_CopyToClipboard;
	public static String AlarmList_Error;
	public static String AlarmList_ErrorText;
	public static String AlarmList_OpenDetailsError;
	public static String AlarmList_Resolve;
	public static String AlarmList_ResolveAlarm;
	public static String AlarmList_Resolving;
	public static String AlarmList_ShowStatusColors;
	public static String AlarmList_StickyAck;
	public static String AlarmList_StickyAckMenutTitle;
	public static String AlarmList_SyncJobError;
	public static String AlarmList_SyncJobName;
	public static String AlarmList_Terminate;
	public static String AlarmListLabelProvider_AlarmState_Acknowledged;
	public static String AlarmListLabelProvider_AlarmState_Outstanding;
	public static String AlarmListLabelProvider_AlarmState_Resolved;
	public static String AlarmListLabelProvider_AlarmState_Terminated;
	public static String AlarmMelody_ErrorGettingMelodyList;
	public static String AlarmMelody_ErrorGettingMelodyListDescription;
	public static String AlarmMelody_ErrorMelodyNotExists;
	public static String AlarmMelody_ErrorMelodyNotExistsDescription;
	public static String AlarmMelody_JobGetMelodyList;
	public static String AlarmMelody_SaveClientSelection;
	public static String AlarmNotifier_ErrorMelodynotExists;
	public static String AlarmNotifier_ErrorMelodyNotExistsDescription;
	public static String AlarmNotifier_ErrorPlayingSound;
	public static String AlarmNotifier_ErrorPlayingSoundDescription;
	public static String AlarmNotifier_ToolTip_Header;
	public static String AlarmReminderDialog_Dismiss;
	public static String AlarmReminderDialog_OutstandingAlarms;
	public static String Alarms_AcknowledgeTimeEditor;
	public static String Alarms_Blinking;
	public static String Alarms_ShowDetailedTooltips;
	public static String Alarms_ShowPopup;
	public static String Alarms_ShowReminder;
	public static String EditCommentDialog_Comment;
	public static String EditCommentDialog_EditComment;
	public static String ObjectAlarmBrowser_SelectedObjects;
	public static String ObjectAlarmBrowser_Title;
	public static String ObjectAlarmBrowser_TitleMultipleObjects;
	public static String OpenAlarmBrowser_Error;
	public static String OpenAlarmBrowser_ErrorOpeningView;
	public static String ShowObjectAlarms_Error;
	public static String ShowObjectAlarms_ErrorOpeningView;
	public static String Startup_JobName;
	public static String TerminateAlarm_ErrorMessage;
	public static String TerminateAlarm_JobTitle;
	public static String TerminateAlarm_TaskName;
	public static String AlarmDetails_RelatedEvents_AccessDenied;
	public static String AlarmList_CountLimitWarning;
	public static String AlarmList_CreateTicket;
	public static String AlarmList_HelpdeskId;
	public static String AlarmList_InternalError;
	public static String AlarmList_JobError_CreateTicket;
	public static String AlarmList_JobError_ShowTicket;
	public static String AlarmList_JobError_UnlinkTicket;
	public static String AlarmList_JobTitle_CreateTicket;
	public static String AlarmList_JobTitle_ShowTicket;
	public static String AlarmList_JobTitle_UnlinkTicket;
	public static String AlarmList_ShowTicketInBrowser;
	public static String AlarmList_UnlinkTicket;
	public static String AlarmNotifier_Error;
	public static String AlarmNotifier_SoundPlayError;
	public static String AlarmListLabelProvider_Closed;

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
