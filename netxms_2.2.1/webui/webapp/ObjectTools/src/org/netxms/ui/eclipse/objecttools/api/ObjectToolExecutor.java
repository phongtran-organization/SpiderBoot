/**
 * NetXMS - open source network management system
 * Copyright (C) 2003-2016 Victor Kirhenshtein
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
package org.netxms.ui.eclipse.objecttools.api;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.window.Window;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.client.service.UrlLauncher;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.netxms.client.AgentFileData;
import org.netxms.client.NXCSession;
import org.netxms.client.ProgressListener;
import org.netxms.client.objecttools.InputField;
import org.netxms.client.objecttools.InputFieldType;
import org.netxms.client.objecttools.ObjectTool;
import org.netxms.ui.eclipse.filemanager.views.AgentFileViewer;
import org.netxms.ui.eclipse.jobs.ConsoleJob;
import org.netxms.ui.eclipse.jobs.ConsoleJobCallingServerJob;
import org.netxms.ui.eclipse.objects.ObjectContext;
import org.netxms.ui.eclipse.objecttools.Activator;
import org.netxms.ui.eclipse.objecttools.Messages;
import org.netxms.ui.eclipse.objecttools.dialogs.ObjectToolInputDialog;
import org.netxms.ui.eclipse.objecttools.views.AgentActionResults;
import org.netxms.ui.eclipse.objecttools.views.ServerCommandResults;
import org.netxms.ui.eclipse.objecttools.views.ServerScriptResults;
import org.netxms.ui.eclipse.objecttools.views.TableToolResults;
import org.netxms.ui.eclipse.shared.ConsoleSharedData;
import org.netxms.ui.eclipse.tools.MessageDialogHelper;

/**
 * Executor for object tool
 */
public final class ObjectToolExecutor
{
   /**
    * Private constructor to forbid instantiation 
    */
   private ObjectToolExecutor()
   {
   }
   
   /**
    * Check if tool is allowed for execution on each node from set
    * 
    * @param tool
    * @param nodes
    * @return
    */
   public static boolean isToolAllowed(ObjectTool tool, Set<ObjectContext> nodes)
   {
      if (tool.getToolType() != ObjectTool.TYPE_INTERNAL)
         return true;
      
      ObjectToolHandler handler = ObjectToolsCache.findHandler(tool.getData());
      if (handler != null)
      {
         for(ObjectContext n : nodes)
            if (!handler.canExecuteOnNode(n.object, tool))
               return false;
         return true;
      }
      else
      {
         return false;
      }
   }
   
   /**
    * Check if given tool is applicable for all nodes in set
    * 
    * @param tool
    * @param nodes
    * @return
    */
   public static boolean isToolApplicable(ObjectTool tool, Set<ObjectContext> nodes)
   {
      for(ObjectContext n : nodes)
         if (!tool.isApplicableForNode(n.object))
            return false;
      return true;
   }
   
   /**
    * Execute object tool on node set
    * 
    * @param tool Object tool
    */
   public static void execute(final Set<ObjectContext> nodes, final ObjectTool tool)
   {
      final Map<String, String> inputValues;
      final InputField[] fields = tool.getInputFields();
      if (fields.length > 0)
      {
         Arrays.sort(fields, new Comparator<InputField>() {
            @Override
            public int compare(InputField f1, InputField f2)
            {
               return f1.getSequence() - f2.getSequence();
            }
         });
         inputValues = readInputFields(fields);
         if (inputValues == null)
            return;  // cancelled
      }
      else
      {
         inputValues = new HashMap<String, String>(0);
      }
      
      if ((tool.getFlags() & ObjectTool.ASK_CONFIRMATION) != 0)
      {
         String message = tool.getConfirmationText();
         if (nodes.size() == 1)
         {
            ObjectContext node = nodes.iterator().next();
            message = node.substituteMacros(message, new HashMap<String, String>(0));
         }
         else
         {
            message = new ObjectContext(null, null).substituteMacros(message, new HashMap<String, String>(0));
         }
         if (!MessageDialogHelper.openQuestion(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), 
               Messages.get().ObjectToolsDynamicMenu_ConfirmExec, message))
            return;
      }
      
      // Check if password validation needed
      boolean validationNeeded = false;
      for(int i = 0; i < fields.length; i++)
         if (fields[i].getOptions().validatePassword)
         {
            validationNeeded = true;
            break;
         }
      
      if (validationNeeded)
      {
         final NXCSession session = ConsoleSharedData.getSession();
         new ConsoleJob(Messages.get().ObjectToolExecutor_JobName, null, Activator.PLUGIN_ID, null) {
            @Override
            protected void runInternal(IProgressMonitor monitor) throws Exception
            {
               for(int i = 0; i < fields.length; i++)
               {
                  if ((fields[i].getType() == InputFieldType.PASSWORD) && (fields[i].getOptions().validatePassword))
                  {
                     boolean valid = session.validateUserPassword(inputValues.get(fields[i].getName()));
                     if (!valid)
                     {
                        final String fieldName = fields[i].getDisplayName();
                        getDisplay().syncExec(new Runnable() {
                           @Override
                           public void run()
                           {
                              MessageDialogHelper.openError(null, Messages.get().ObjectToolExecutor_ErrorTitle, 
                                    String.format(Messages.get().ObjectToolExecutor_ErrorText, fieldName));
                           }
                        });
                        return;
                     }
                  }
               }
               
               runInUIThread(new Runnable() {
                  @Override
                  public void run()
                  {
                     for(ObjectContext n : nodes)
                        executeOnNode(n, tool, inputValues);
                  }
               });
            }
            
            @Override
            protected String getErrorMessage()
            {
               return Messages.get().ObjectToolExecutor_PasswordValidationFailed;
            }
         }.start();
      }
      else
      {
         for(ObjectContext n : nodes)
            executeOnNode(n, tool, inputValues);
      }
   }
   
   /**
    * Read input fields
    * 
    * @param fields
    * @return
    */
   private static Map<String, String> readInputFields(InputField[] fields)
   {
      ObjectToolInputDialog dlg = new ObjectToolInputDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), fields);
      if (dlg.open() != Window.OK)
         return null;
      return dlg.getValues();
   }

   /**
    * Execute object tool on single node
    * 
    * @param node
    * @param tool
    * @param inputValues 
    */
   private static void executeOnNode(final ObjectContext node, final ObjectTool tool, Map<String, String> inputValues)
   {
      switch(tool.getToolType())
      {
         case ObjectTool.TYPE_ACTION:
            executeAgentAction(node, tool, inputValues);
            break;
         case ObjectTool.TYPE_FILE_DOWNLOAD:
            executeFileDownload(node, tool, inputValues);
            break;
         case ObjectTool.TYPE_INTERNAL:
            executeInternalTool(node, tool);
            break;
         case ObjectTool.TYPE_SERVER_COMMAND:
            executeServerCommand(node, tool, inputValues);
            break;
         case ObjectTool.TYPE_SERVER_SCRIPT:
            executeServerScript(node, tool, inputValues);
            break;
         case ObjectTool.TYPE_TABLE_AGENT:
         case ObjectTool.TYPE_TABLE_SNMP:
            executeTableTool(node, tool);
            break;
         case ObjectTool.TYPE_URL:
            openURL(node, tool, inputValues);
            break;
      }
   }
   
   /**
    * Execute table tool
    * 
    * @param node
    * @param tool
    */
   private static void executeTableTool(final ObjectContext node, final ObjectTool tool)
   {
      final IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
      try
      {
         final IWorkbenchPage page = window.getActivePage();
         final TableToolResults view = (TableToolResults)page.showView(TableToolResults.ID,
               Long.toString(tool.getId()) + "&" + Long.toString(node.object.getObjectId()), IWorkbenchPage.VIEW_ACTIVATE); //$NON-NLS-1$
         view.refreshTable();
      }
      catch(PartInitException e)
      {
         MessageDialogHelper.openError(window.getShell(), Messages.get().ObjectToolsDynamicMenu_Error, String.format(Messages.get().ObjectToolsDynamicMenu_ErrorOpeningView, e.getLocalizedMessage()));
      }
   }
   
   /**
    * Split command line into tokens
    *  
    * @param input
    * @return
    */
   private static String[] splitCommandLine(String input)
   {
      char[] in = input.toCharArray();
      List<String> args = new ArrayList<String>();
      
      StringBuilder sb = new StringBuilder();
      int state = 0;
      for(char c : in)
      {
         switch(state)
         {
            case 0: // normal
               if (Character.isSpaceChar(c))
               {
                  args.add(sb.toString());
                  sb = new StringBuilder();
                  state = 3;
               }
               else if (c == '"')
               {
                  state = 1;
               }
               else if (c == '\'')
               {
                  state = 2;
               }
               else
               {
                  sb.append(c);
               }
               break;
            case 1: // double quoted string
               if (c == '"')
               {
                  state = 0;
               }
               else
               {
                  sb.append(c);
               }
               break;
            case 2: // single quoted string
               if (c == '\'')
               {
                  state = 0;
               }
               else
               {
                  sb.append(c);
               }
               break;
            case 3: // skip
               if (!Character.isSpaceChar(c))
               {
                  if (c == '"')
                  {
                     state = 1;
                  }
                  else if (c == '\'')
                  {
                     state = 2;
                  }
                  else
                  {
                     sb.append(c);
                     state = 0;
                  }
               }
               break;
         }
      }
      if (state != 3)
         args.add(sb.toString());
      
      return args.toArray(new String[args.size()]);
   }

   /**
    * @param node
    * @param tool
    * @param inputValues 
    */
   private static void executeAgentAction(final ObjectContext node, final ObjectTool tool, Map<String, String> inputValues)
   {
      final NXCSession session = (NXCSession)ConsoleSharedData.getSession();
      String[] parts = splitCommandLine(node.substituteMacros(tool.getData(), inputValues));
      final String action = parts[0];
      final String[] args = Arrays.copyOfRange(parts, 1, parts.length);
      
      if ((tool.getFlags() & ObjectTool.GENERATES_OUTPUT) == 0)
      {      
         new ConsoleJob(String.format(Messages.get().ObjectToolsDynamicMenu_ExecuteOnNode, node.object.getObjectName()), null, Activator.PLUGIN_ID, null) {
            @Override
            protected String getErrorMessage()
            {
               return String.format(Messages.get().ObjectToolsDynamicMenu_CannotExecuteOnNode, node.object.getObjectName());
            }
   
            @Override
            protected void runInternal(IProgressMonitor monitor) throws Exception
            {
               session.executeAction(node.object.getObjectId(), action, args);
               runInUIThread(new Runnable() {
                  @Override
                  public void run()
                  {
                     MessageDialogHelper.openInformation(null, Messages.get().ObjectToolsDynamicMenu_ToolExecution, String.format(Messages.get().ObjectToolsDynamicMenu_ExecSuccess, action, node.object.getObjectName()));
                  }
               });
            }
         }.start();
      }
      else
      {
         final String secondaryId = Long.toString(node.object.getObjectId()) + "&" + Long.toString(tool.getId()); //$NON-NLS-1$
         final IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
         try
         {
            AgentActionResults view = (AgentActionResults)window.getActivePage().showView(AgentActionResults.ID, secondaryId, IWorkbenchPage.VIEW_ACTIVATE);
            view.executeAction(action, args);
         }
         catch(Exception e)
         {
            MessageDialogHelper.openError(window.getShell(), Messages.get().ObjectToolsDynamicMenu_Error, String.format(Messages.get().ObjectToolsDynamicMenu_ErrorOpeningView, e.getLocalizedMessage()));
         }
      }
   }

   /**
    * Execute server command
    * 
    * @param node
    * @param tool
    * @param inputValues 
    */
   private static void executeServerCommand(final ObjectContext node, final ObjectTool tool, final Map<String, String> inputValues)
   {
      final NXCSession session = (NXCSession)ConsoleSharedData.getSession();
      if ((tool.getFlags() & ObjectTool.GENERATES_OUTPUT) == 0)
      {      
         new ConsoleJob(Messages.get().ObjectToolsDynamicMenu_ExecuteServerCmd, null, Activator.PLUGIN_ID, null) {
            @Override
            protected void runInternal(IProgressMonitor monitor) throws Exception
            {
               session.executeServerCommand(node.object.getObjectId(), tool.getData(), inputValues);
               runInUIThread(new Runnable() {
                  @Override
                  public void run()
                  {
                     MessageDialogHelper.openInformation(null, Messages.get().ObjectToolsDynamicMenu_Information, Messages.get().ObjectToolsDynamicMenu_ServerCommandExecuted);
                  }
               });
            }
            
            @Override
            protected String getErrorMessage()
            {
               return Messages.get().ObjectToolsDynamicMenu_ServerCmdExecError;
            }
         }.start();
      }
      else
      {
         final String secondaryId = Long.toString(node.object.getObjectId()) + "&" + Long.toString(tool.getId()); //$NON-NLS-1$
         final IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
         try
         {
            ServerCommandResults view = (ServerCommandResults)window.getActivePage().showView(ServerCommandResults.ID, secondaryId, IWorkbenchPage.VIEW_ACTIVATE);
            view.executeCommand(tool.getData(), inputValues);
         }
         catch(Exception e)
         {
            MessageDialogHelper.openError(window.getShell(), Messages.get().ObjectToolsDynamicMenu_Error, String.format(Messages.get().ObjectToolsDynamicMenu_ErrorOpeningView, e.getLocalizedMessage()));
         }
      }
   }
   
   /**
    * Execute server script
    * 
    * @param node
    * @param tool
    * @param inputValues 
    */
   private static void executeServerScript(final ObjectContext node, final ObjectTool tool, final Map<String, String> inputValues)
   {
      final NXCSession session = (NXCSession)ConsoleSharedData.getSession();
      if ((tool.getFlags() & ObjectTool.GENERATES_OUTPUT) == 0)
      {      
         new ConsoleJob("Execute server script", null, Activator.PLUGIN_ID, null) {
            @Override
            protected void runInternal(IProgressMonitor monitor) throws Exception
            {
               session.executeLibraryScript(node.object.getObjectId(), tool.getData(), inputValues, null);
               runInUIThread(new Runnable() {
                  @Override
                  public void run()
                  {
                     MessageDialogHelper.openInformation(null, Messages.get().ObjectToolsDynamicMenu_Information, Messages.get().ObjectToolsDynamicMenu_ServerScriptExecuted);
                  }
               });
            }
            
            @Override
            protected String getErrorMessage()
            {
               return Messages.get().ObjectToolsDynamicMenu_ServerScriptExecError;
            }
         }.start();
      }
      else
      {
         final String secondaryId = Long.toString(node.object.getObjectId()) + "&" + Long.toString(tool.getId()); //$NON-NLS-1$
         final IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
         try
         {
            ServerScriptResults view = (ServerScriptResults)window.getActivePage().showView(ServerScriptResults.ID, secondaryId, IWorkbenchPage.VIEW_ACTIVATE);
            view.executeScript(tool.getData(), inputValues);
         }
         catch(Exception e)
         {
            MessageDialogHelper.openError(window.getShell(), Messages.get().ObjectToolsDynamicMenu_Error, String.format(Messages.get().ObjectToolsDynamicMenu_ErrorOpeningView, e.getLocalizedMessage()));
         }
      }
   }
   
   /**
    * @param node
    * @param tool
    * @param inputValues 
    * @param inputValues 
    */
   private static void executeFileDownload(final ObjectContext node, final ObjectTool tool, Map<String, String> inputValues)
   {
      final NXCSession session = (NXCSession)ConsoleSharedData.getSession();
      String[] parameters = tool.getData().split("\u007F"); //$NON-NLS-1$
      
      final String fileName = node.substituteMacros(parameters[0], inputValues);
      final int maxFileSize = Integer.parseInt(parameters[1]);
      final boolean follow = parameters[2].equals("true") ? true : false; //$NON-NLS-1$
      
      ConsoleJobCallingServerJob job = new ConsoleJobCallingServerJob(Messages.get().ObjectToolsDynamicMenu_DownloadFromAgent, null, Activator.PLUGIN_ID, null) {
         @Override
         protected String getErrorMessage()
         {
            return String.format(Messages.get().ObjectToolsDynamicMenu_DownloadError, fileName, node.object.getObjectName());
         }

         @Override
         protected void runInternal(final IProgressMonitor monitor) throws Exception
         {
            final AgentFileData file = session.downloadFileFromAgent(node.object.getObjectId(), fileName, maxFileSize, follow, new ProgressListener() {
               @Override
               public void setTotalWorkAmount(long workTotal)
               {
                  monitor.beginTask("Download file " + fileName, (int)workTotal);
               }

               @Override
               public void markProgress(long workDone)
               {
                  monitor.worked((int)workDone);
               }
            }, this);
            runInUIThread(new Runnable() {
               @Override
               public void run()
               {
                  try
                  {
                     String secondaryId = Long.toString(node.object.getObjectId()) + "&" + URLEncoder.encode(fileName, "UTF-8"); //$NON-NLS-1$ //$NON-NLS-2$
                     AgentFileViewer.createView(secondaryId, node.object.getObjectId(), file, follow);
                  }
                  catch(Exception e)
                  {
                     final IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
                     MessageDialogHelper.openError(window.getShell(), Messages.get().ObjectToolsDynamicMenu_Error, String.format(Messages.get().ObjectToolsDynamicMenu_ErrorOpeningView, e.getLocalizedMessage()));
                  }
               }
            });
         }
      };
      job.start();
   }

   /**
    * @param node
    * @param tool
    */
   private static void executeInternalTool(final ObjectContext node, final ObjectTool tool)
   {
      ObjectToolHandler handler = ObjectToolsCache.findHandler(tool.getData());
      if (handler != null)
      {
         handler.execute(node.object, tool);
      }
      else
      {
         MessageDialogHelper.openError(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), Messages.get().ObjectToolsDynamicMenu_Error, Messages.get().ObjectToolsDynamicMenu_HandlerNotDefined);
      }
   }

   /**
    * @param node
    * @param tool
    * @param inputValues 
    */
   private static void openURL(final ObjectContext node, final ObjectTool tool, Map<String, String> inputValues)
   {
      final String url = node.substituteMacros(tool.getData(), inputValues);
      final UrlLauncher launcher = RWT.getClient().getService(UrlLauncher.class);
      launcher.openURL(url);
   }
}
