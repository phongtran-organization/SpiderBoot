package org.netxms.ui.eclipse.alarmviewer;

import org.eclipse.osgi.util.NLS;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.swt.widgets.Display;


public class Messages extends NLS
{
   private static final String BUNDLE_NAME = "org.netxms.ui.eclipse.alarmviewer.messages"; //$NON-NLS-1$

   public String AcknowledgeAlarm_ErrorMessage;
   public String AcknowledgeAlarm_JobName;
   public String AcknowledgeAlarm_TaskName;
   public String AcknowledgeCustomTimeDialog_ConfigurationInfoLabel;
   public String AcknowledgeCustomTimeDialog_CustomTimeDialogTitle;
   public String AcknowledgeCustomTimeDialog_Days;
   public String AcknowledgeCustomTimeDialog_Hours;
   public String AcknowledgeCustomTimeDialog_Minutes;
   public String AcknowledgeCustomTimeDialog_Warning;
   public String AcknowledgeCustomTimeDialog_WarningZeroTime;
   public String AlarmAcknowledgeTimeFunctions_day;
   public String AlarmAcknowledgeTimeFunctions_days;
   public String AlarmAcknowledgeTimeFunctions_hour;
   public String AlarmAcknowledgeTimeFunctions_hours;
   public String AlarmAcknowledgeTimeFunctions_minute;
   public String AlarmAcknowledgeTimeFunctions_minutes;
   public String AlarmAcknowledgeTimeFunctions_ZeroMinutesEntry;
   public String AlarmComments_AckToDeleteComment;
   public String AlarmComments_AddCommentJob;
   public String AlarmComments_AddCommentLink;
   public String AlarmComments_AddError;
   public String AlarmComments_Comments;
   public String AlarmComments_Confirmation;
   public String AlarmComments_DeleteCommentJob;
   public String AlarmComments_Details;
   public String AlarmComments_ErrorDeleteAlarmComment;
   public String AlarmComments_GetComments;
   public String AlarmComments_GetError;
   public String AlarmComments_InternalError;
   public String AlarmCommentsEditor_DeleteLabel;
   public String AlarmCommentsEditor_Edit;
   public String AlarmCommentsEditor_Unknown;
   public String AlarmComparator_Unknown;
   public String AlarmDetails_Column_Message;
   public String AlarmDetails_Column_Name;
   public String AlarmDetails_Column_Severity;
   public String AlarmDetails_Column_Source;
   public String AlarmDetails_Column_Timestamp;
   public String AlarmDetails_LastValues;
   public String AlarmDetails_Overview;
   public String AlarmDetails_RefreshJobError;
   public String AlarmDetails_RefreshJobTitle;
   public String AlarmDetails_RelatedEvents;
   public String AlarmDetailsProvider_Error;
   public String AlarmDetailsProvider_ErrorOpeningView;
   public String AlarmList_AckBy;
   public String AlarmList_Acknowledge;
   public String AlarmList_ActionAlarmDetails;
   public String AlarmList_ActionObjectDetails;
   public String AlarmList_CannotResoveAlarm;
   public String AlarmList_ColumnCount;
   public String AlarmList_ColumnCreated;
   public String AlarmList_ColumnLastChange;
   public String AlarmList_ColumnMessage;
   public String AlarmList_ColumnSeverity;
   public String AlarmList_ColumnSource;
   public String AlarmList_ColumnState;
   public String AlarmList_Comments;
   public String AlarmList_CopyMsgToClipboard;
   public String AlarmList_CopyToClipboard;
   public String AlarmList_Error;
   public String AlarmList_ErrorText;
   public String AlarmList_OpenDetailsError;
   public String AlarmList_Resolve;
   public String AlarmList_ResolveAlarm;
   public String AlarmList_Resolving;
   public String AlarmList_ShowStatusColors;
   public String AlarmList_StickyAck;
   public String AlarmList_StickyAckMenutTitle;
   public String AlarmList_SyncJobError;
   public String AlarmList_SyncJobName;
   public String AlarmList_Terminate;
   public String AlarmListLabelProvider_AlarmState_Acknowledged;
   public String AlarmListLabelProvider_AlarmState_Outstanding;
   public String AlarmListLabelProvider_AlarmState_Resolved;
   public String AlarmListLabelProvider_AlarmState_Terminated;
   public String AlarmMelody_ErrorGettingMelodyList;
   public String AlarmMelody_ErrorGettingMelodyListDescription;
   public String AlarmMelody_ErrorMelodyNotExists;
   public String AlarmMelody_ErrorMelodyNotExistsDescription;
   public String AlarmMelody_JobGetMelodyList;
   public String AlarmMelody_SaveClientSelection;
   public String AlarmNotifier_ErrorMelodynotExists;
   public String AlarmNotifier_ErrorMelodyNotExistsDescription;
   public String AlarmNotifier_ErrorPlayingSound;
   public String AlarmNotifier_ErrorPlayingSoundDescription;
   public String AlarmNotifier_ToolTip_Header;
   public String AlarmReminderDialog_Dismiss;
   public String AlarmReminderDialog_OutstandingAlarms;
   public String Alarms_AcknowledgeTimeEditor;
   public String Alarms_Blinking;
   public String Alarms_ShowDetailedTooltips;
   public String Alarms_ShowPopup;
   public String Alarms_ShowReminder;
   public String EditCommentDialog_Comment;
   public String EditCommentDialog_EditComment;
   public String ObjectAlarmBrowser_SelectedObjects;
   public String ObjectAlarmBrowser_Title;
   public String ObjectAlarmBrowser_TitleMultipleObjects;
   public String OpenAlarmBrowser_Error;
   public String OpenAlarmBrowser_ErrorOpeningView;
   public String ShowObjectAlarms_Error;
   public String ShowObjectAlarms_ErrorOpeningView;
   public String Startup_JobName;
   public String TerminateAlarm_ErrorMessage;
   public String TerminateAlarm_JobTitle;
   public String TerminateAlarm_TaskName;
   public String AlarmDetails_RelatedEvents_AccessDenied;
   public String AlarmList_CountLimitWarning;
   public String AlarmList_CreateTicket;
   public String AlarmList_HelpdeskId;
   public String AlarmList_InternalError;
   public String AlarmList_JobError_CreateTicket;
   public String AlarmList_JobError_ShowTicket;
   public String AlarmList_JobError_UnlinkTicket;
   public String AlarmList_JobTitle_CreateTicket;
   public String AlarmList_JobTitle_ShowTicket;
   public String AlarmList_JobTitle_UnlinkTicket;
   public String AlarmList_ShowTicketInBrowser;
   public String AlarmList_UnlinkTicket;
   public String AlarmNotifier_Error;
   public String AlarmNotifier_SoundPlayError;
   public String AlarmListLabelProvider_Closed;

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
