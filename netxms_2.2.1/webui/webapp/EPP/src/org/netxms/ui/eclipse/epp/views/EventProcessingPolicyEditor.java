/**
 * NetXMS - open source network management system
 * Copyright (C) 2003-2015 Victor Kirhenshtein
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
package org.netxms.ui.eclipse.epp.views;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.commands.ActionHandler;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.ISaveablePart;
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.part.ViewPart;
import org.netxms.client.NXCSession;
import org.netxms.client.ServerAction;
import org.netxms.client.SessionListener;
import org.netxms.client.SessionNotification;
import org.netxms.client.events.AlarmCategory;
import org.netxms.client.events.EventProcessingPolicy;
import org.netxms.client.events.EventProcessingPolicyRule;
import org.netxms.client.events.EventTemplate;
import org.netxms.ui.eclipse.console.resources.SharedIcons;
import org.netxms.ui.eclipse.epp.Activator;
import org.netxms.ui.eclipse.epp.Messages;
import org.netxms.ui.eclipse.epp.views.helpers.RuleClipboard;
import org.netxms.ui.eclipse.epp.widgets.RuleEditor;
import org.netxms.ui.eclipse.epp.widgets.helpers.ImageFactory;
import org.netxms.ui.eclipse.jobs.ConsoleJob;
import org.netxms.ui.eclipse.shared.ConsoleSharedData;
import org.netxms.ui.eclipse.tools.MessageDialogHelper;
import org.netxms.ui.eclipse.widgets.FilterText;

/**
 * Event processing policy editor
 * 
 */
public class EventProcessingPolicyEditor extends ViewPart implements ISaveablePart
{
   public static final String ID = "org.netxms.ui.eclipse.epp.view.policy_editor"; //$NON-NLS-1$
   public static final String JOB_FAMILY = "PolicyEditorJob"; //$NON-NLS-1$

   private static final Color BACKGROUND_COLOR = new Color(Display.getCurrent(), 255, 255, 255);

   private NXCSession session;
   private boolean policyLocked = false;
   private EventProcessingPolicy policy;
   private SessionListener sessionListener;
   private Map<Long, ServerAction> actions = new HashMap<Long, ServerAction>();
   private FilterText filterControl;
   private String filterText = null;
   private ScrolledComposite scroller;
   private Composite dataArea;
   private List<RuleEditor> ruleEditors = new ArrayList<RuleEditor>();
   private boolean verticalLayout = false;
   private boolean filterEnabled = true;
   private boolean modified = false;
   private Set<RuleEditor> selection;
   private int lastSelectedRule = -1;
   private RuleClipboard clipboard = new RuleClipboard();

   private Font normalFont;
   private Font boldFont;

   private Image imageAlarm;
   private Image imageSituation;
   private Image imageExecute;
   private Image imageTerminate;
   private Image imageStop;
   private Image imageCollapse;
   private Image imageExpand;
   private Image imageEdit;

   private Action actionHorizontal;
   private Action actionVertical;
   private Action actionSave;
   private Action actionCollapseAll;
   private Action actionExpandAll;
   private Action actionInsertBefore;
   private Action actionInsertAfter;
   private Action actionCut;
   private Action actionCopy;
   private Action actionPaste;
   private Action actionDelete;
   private Action actionEnableRule;
   private Action actionDisableRule;
   private Action actionShowFilter;
   private Action actionAddRule;

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
    */
   @Override
   public void createPartControl(Composite parent)
   {
      session = (NXCSession)ConsoleSharedData.getSession();

      IDialogSettings settings = Activator.getDefault().getDialogSettings();
      filterEnabled = settings.getBoolean("EventProcessingPolicyEditor.filterEnabled"); //$NON-NLS-1$

      // Initiate loading of required plugins if they was not loaded yet
      try
      {
         Platform.getAdapterManager().loadAdapter(new EventTemplate(0), "org.eclipse.ui.model.IWorkbenchAdapter"); //$NON-NLS-1$
         Platform.getAdapterManager().loadAdapter(new ServerAction(0), "org.eclipse.ui.model.IWorkbenchAdapter"); //$NON-NLS-1$
         Platform.getAdapterManager().loadAdapter(session.getTopLevelObjects()[0], "org.eclipse.ui.model.IWorkbenchAdapter"); //$NON-NLS-1$
      }
      catch(Exception e)
      {
      }

      imageStop = Activator.getImageDescriptor("icons/stop.png").createImage(); //$NON-NLS-1$
      imageAlarm = Activator.getImageDescriptor("icons/alarm.png").createImage(); //$NON-NLS-1$
      imageSituation = Activator.getImageDescriptor("icons/situation.gif").createImage(); //$NON-NLS-1$
      imageExecute = Activator.getImageDescriptor("icons/execute.png").createImage(); //$NON-NLS-1$
      imageTerminate = Activator.getImageDescriptor("icons/terminate.png").createImage(); //$NON-NLS-1$
      imageCollapse = SharedIcons.COLLAPSE.createImage();
      imageExpand = SharedIcons.EXPAND.createImage();
      imageEdit = SharedIcons.EDIT.createImage();

      parent.setLayout(new FormLayout());

      // Create filter area
      filterControl = new FilterText(parent, SWT.NONE);
      filterControl.addModifyListener(new ModifyListener() {
         @Override
         public void modifyText(ModifyEvent e)
         {
            onFilterModify();
         }
      });
      filterControl.setCloseAction(new Action() {
         @Override
         public void run()
         {
            enableFilter(false);
         }
      });

		scroller = new ScrolledComposite(parent, SWT.V_SCROLL);

		dataArea = new Composite(scroller, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.verticalSpacing = 0;
		dataArea.setLayout(layout);
		dataArea.setBackground(BACKGROUND_COLOR);

		scroller.setContent(dataArea);
		scroller.setExpandVertical(true);
		scroller.setExpandHorizontal(true);
		scroller.addControlListener(new ControlAdapter() {
			public void controlResized(ControlEvent e)
			{
				Rectangle r = scroller.getClientArea();
				scroller.setMinSize(dataArea.computeSize(r.width, SWT.DEFAULT));
			}
		});
		
      // Setup layout
      FormData fd = new FormData();
      fd.left = new FormAttachment(0, 0);
      fd.top = new FormAttachment(filterControl);
      fd.right = new FormAttachment(100, 0);
      fd.bottom = new FormAttachment(100, 0);
      scroller.setLayoutData(fd);
      
      fd = new FormData();
      fd.left = new FormAttachment(0, 0);
      fd.top = new FormAttachment(0, 0);
      fd.right = new FormAttachment(100, 0);
      filterControl.setLayoutData(fd);

      normalFont = JFaceResources.getDefaultFont();
      boldFont = JFaceResources.getFontRegistry().getBold(JFaceResources.DEFAULT_FONT);
		
		sessionListener = new SessionListener() {
			@Override
			public void notificationHandler(SessionNotification n)
			{
				processSessionNotification(n);
			}
		};
		session.addListener(sessionListener);

      selection = new TreeSet<RuleEditor>(new Comparator<RuleEditor>() {
         @Override
         public int compare(RuleEditor arg0, RuleEditor arg1)
         {
            return arg0.getRuleNumber() - arg1.getRuleNumber();
         }
      });

		createActions();
		contributeToActionBars();
      
      openEventProcessingPolicy();      
      activateContext();

      // Set initial focus to filter input line
      if (filterEnabled)
         filterControl.setFocus();
      else
         enableFilter(false); // Will hide filter area correctly
	}
   
	/**
    * Activate context
    */
   private void activateContext()
   {
      IContextService contextService = (IContextService)getSite().getService(IContextService.class);
      if (contextService != null)
      {
         contextService.activateContext("org.netxms.ui.eclipse.epp.context.PolicyEditor"); //$NON-NLS-1$
      }
   }
	
	/**
	 * Create actions
	 */
	private void createActions()
	{
      final IHandlerService handlerService = (IHandlerService)getSite().getService(IHandlerService.class);
      
		actionHorizontal = new Action(Messages.get().EventProcessingPolicyEditor_LayoutH, Action.AS_RADIO_BUTTON) {
			@Override
			public void run()
			{
				verticalLayout = false;
				updateLayout();
			}
		};
		actionHorizontal.setChecked(!verticalLayout);
		actionHorizontal.setImageDescriptor(Activator.getImageDescriptor("icons/h_layout.gif")); //$NON-NLS-1$

		actionVertical = new Action(Messages.get().EventProcessingPolicyEditor_LayoutV, Action.AS_RADIO_BUTTON) {
			@Override
			public void run()
			{
				verticalLayout = true;
				updateLayout();
			}
		};
		actionVertical.setChecked(verticalLayout);
		actionVertical.setImageDescriptor(Activator.getImageDescriptor("icons/v_layout.gif")); //$NON-NLS-1$

		actionSave = new Action(Messages.get().EventProcessingPolicyEditor_Save) {
			@Override
			public void run()
			{
				savePolicy();
			}
		};
		actionSave.setImageDescriptor(SharedIcons.SAVE);
		actionSave.setEnabled(false);

		actionCollapseAll = new Action(Messages.get().EventProcessingPolicyEditor_CollapseAll) {
			@Override
			public void run()
			{
				setAllRulesCollapsed(true);
			}
		};
		actionCollapseAll.setImageDescriptor(SharedIcons.COLLAPSE_ALL);

		actionExpandAll = new Action(Messages.get().EventProcessingPolicyEditor_ExpandAll) {
			@Override
			public void run()
			{
				setAllRulesCollapsed(false);
			}
		};
		actionExpandAll.setImageDescriptor(SharedIcons.EXPAND_ALL);

		actionDelete = new Action(Messages.get().EventProcessingPolicyEditor_Delete) {
			@Override
			public void run()
			{
				deleteSelectedRules();
			}
		};
		actionDelete.setImageDescriptor(SharedIcons.DELETE_OBJECT);
		actionDelete.setEnabled(false);

		actionInsertBefore = new Action(Messages.get().EventProcessingPolicyEditor_InsertBefore) {
			@Override
			public void run()
			{
				insertRule(lastSelectedRule - 1);
			}
		};

		actionInsertAfter = new Action(Messages.get().EventProcessingPolicyEditor_InsertAfter) {
			@Override
			public void run()
			{
				insertRule(lastSelectedRule);
			}
		};

		actionCut = new Action(Messages.get().EventProcessingPolicyEditor_Cut) {
			@Override
			public void run()
			{
				cutRules();
			}
		};
		actionCut.setImageDescriptor(SharedIcons.CUT);
		actionCut.setEnabled(false);

		actionCopy = new Action(Messages.get().EventProcessingPolicyEditor_Copy) {
			@Override
			public void run()
			{
				copyRules();
			}
		};
		actionCopy.setImageDescriptor(SharedIcons.COPY);
		actionCopy.setEnabled(false);

		actionPaste = new Action(Messages.get().EventProcessingPolicyEditor_Paste) {
			@Override
			public void run()
			{
				pasteRules();
			}
		};
		actionPaste.setImageDescriptor(SharedIcons.PASTE);
		actionPaste.setEnabled(false);

		actionEnableRule = new Action(Messages.get().EventProcessingPolicyEditor_Enable) {
			@Override
			public void run()
			{
				enableRules(true);
			}
		};

		actionDisableRule = new Action(Messages.get().EventProcessingPolicyEditor_Disable) {
			@Override
			public void run()
			{
				enableRules(false);
			}
		};

      actionShowFilter = new Action(Messages.get().EventProcessingPolicyEditor_ShowFilter, Action.AS_CHECK_BOX) {
         @Override
         public void run()
         {
            enableFilter(actionShowFilter.isChecked());
         }
      };
      
      actionAddRule = new Action("&Add new rule") {
         @Override
         public void run()
         {
            insertRule(ruleEditors.size());
         }
      };
      actionAddRule.setImageDescriptor(SharedIcons.ADD_OBJECT);
      
      actionShowFilter.setChecked(filterEnabled);
      actionShowFilter.setActionDefinitionId("org.netxms.ui.eclipse.epp.commands.show_rule_filter"); //$NON-NLS-1$
      final ActionHandler showFilterHandler = new ActionHandler(actionShowFilter);
      handlerService.activateHandler(actionShowFilter.getActionDefinitionId(), showFilterHandler);
	}

	/**
	 * Contribute actions to action bar
	 */
	private void contributeToActionBars()
	{
		IActionBars bars = getViewSite().getActionBars();
		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
	}

	/**
	 * Fill local pull-down menu
	 * 
	 * @param manager Menu manager for pull-down menu
	 */
	private void fillLocalPullDown(IMenuManager manager)
	{
		manager.add(actionSave);
		manager.add(new Separator());
		manager.add(actionExpandAll);
		manager.add(actionCollapseAll);
		manager.add(new Separator());
		manager.add(actionHorizontal);
		manager.add(actionVertical);
      manager.add(new Separator());
      manager.add(actionShowFilter);
	}

	/**
	 * Fill local tool bar
	 * 
	 * @param manager Menu manager for local toolbar
	 */
	private void fillLocalToolBar(IToolBarManager manager)
	{
		manager.add(actionAddRule);
		manager.add(actionSave);
		manager.add(new Separator());
		manager.add(actionExpandAll);
		manager.add(actionCollapseAll);
		manager.add(new Separator());
		manager.add(actionHorizontal);
		manager.add(actionVertical);
		manager.add(new Separator());
		manager.add(actionCut);
		manager.add(actionCopy);
		manager.add(actionPaste);
		manager.add(actionDelete);
	}

	/**
	 * Open event processing policy
	 */
	private void openEventProcessingPolicy()
	{
		ConsoleJob job = new ConsoleJob(Messages.get().EventProcessingPolicyEditor_OpenJob_Title, this, Activator.PLUGIN_ID, JOB_FAMILY) {
			@Override
			protected String getErrorMessage()
			{
				return Messages.get().EventProcessingPolicyEditor_OpenJob_Error;
			}

			@Override
			protected void runInternal(IProgressMonitor monitor) throws Exception
			{
				List<ServerAction> actions = session.getActions();
				synchronized(EventProcessingPolicyEditor.this.actions)
				{
					for(ServerAction a : actions)
					{
						EventProcessingPolicyEditor.this.actions.put(a.getId(), a);
					}
				}

				policy = session.openEventProcessingPolicy();
				policyLocked = true;
				runInUIThread(new Runnable() {
					@Override
					public void run()
					{
						initPolicyEditor();
						if (filterEnabled)
						   filterControl.setFocus();
					}
				});
			}

			@Override
			protected void jobFailureHandler()
			{
				runInUIThread(new Runnable() {
					@Override
					public void run()
					{
						EventProcessingPolicyEditor.this.getViewSite().getPage().hideView(EventProcessingPolicyEditor.this);
					}
				});
			}
		};
		job.start();
	}

   /**
    * Init policy editor
    */
   private void initPolicyEditor()
   {
      for(EventProcessingPolicyRule rule : policy.getRules())
      {
         if (isRuleVisible(rule))
         {
            RuleEditor editor = new RuleEditor(dataArea, rule, this);
            ruleEditors.add(editor);
            GridData gd = new GridData();
            gd.horizontalAlignment = SWT.FILL;
            gd.grabExcessHorizontalSpace = true;
            editor.setLayoutData(gd);
         }
      }
      dataArea.layout();

      Rectangle r = scroller.getClientArea();
      scroller.setMinSize(dataArea.computeSize(r.width, SWT.DEFAULT));
   }

	/**
	 * Update editor's layout
	 */
	private void updateLayout()
	{
		for(RuleEditor editor : ruleEditors)
		{
		   if (!editor.isDisposed())
            editor.setVerticalLayout(verticalLayout, false);
		}
		updateEditorAreaLayout();
	}

	/**
	 * Process session notifications
	 * 
	 * @param n notification
	 */
	private void processSessionNotification(SessionNotification n)
	{
		switch(n.getCode())
		{
			case SessionNotification.ACTION_CREATED:
				synchronized(actions)
				{
					actions.put(n.getSubCode(), (ServerAction)n.getObject());
				}
				break;
			case SessionNotification.ACTION_MODIFIED:
				synchronized(actions)
				{
					actions.put(n.getSubCode(), (ServerAction)n.getObject());
				}
				break;
			case SessionNotification.ACTION_DELETED:
				synchronized(actions)
				{
					actions.remove(n.getSubCode());
				}
				break;
		}
	}

	/**
	 * Set all rules to collapsed or expanded state
	 * 
	 * @param collapsed true to collapse all, false to expand
	 */
	private void setAllRulesCollapsed(boolean collapsed)
	{
		for(RuleEditor editor : ruleEditors)
		{
		   if (!editor.isDisposed())
            editor.setCollapsed(collapsed, false);
		}
		updateEditorAreaLayout();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
	 */
	@Override
	public void setFocus()
	{
		dataArea.setFocus();
	}

	/**
	 * Save policy to server
	 */
	private void savePolicy()
	{
		actionSave.setEnabled(false);
		new ConsoleJob(Messages.get().EventProcessingPolicyEditor_SaveJob_Title, this, Activator.PLUGIN_ID, JOB_FAMILY) {
			@Override
			protected void runInternal(IProgressMonitor monitor) throws Exception
			{
            EventProcessingPolicy newPolicy = new EventProcessingPolicy(ruleEditors.size());
            for(RuleEditor editor: ruleEditors)
               newPolicy.addRule(editor.getRule());
				session.saveEventProcessingPolicy(policy);
				runInUIThread(new Runnable() {
					@Override
					public void run()
					{
						modified = false;
						firePropertyChange(PROP_DIRTY);
					}
				});
			}

			@Override
			protected void jobFinalize()
			{
				runInUIThread(new Runnable() {
					@Override
					public void run()
					{
						actionSave.setEnabled(modified);
					}
				});
			}

			@Override
			protected String getErrorMessage()
			{
				return Messages.get().EventProcessingPolicyEditor_SaveJob_Error;
			}
		}.start();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#dispose()
	 */
	@Override
	public void dispose()
	{
	   IDialogSettings settings = Activator.getDefault().getDialogSettings();
      settings.put("EventProcessingPolicyEditor.filterEnabled", filterEnabled); //$NON-NLS-1$
	   
		if (sessionListener != null)
			session.removeListener(sessionListener);

		if (policyLocked)
		{
			new ConsoleJob(Messages.get().EventProcessingPolicyEditor_CloseJob_Title, null, Activator.PLUGIN_ID, JOB_FAMILY) {
				@Override
				protected void runInternal(IProgressMonitor monitor) throws Exception
				{
					session.closeEventProcessingPolicy();
				}

				@Override
				protected String getErrorMessage()
				{
					return Messages.get().EventProcessingPolicyEditor_CloseJob_Error;
				}
			}.start();
		}
		
		imageStop.dispose();
		imageAlarm.dispose();
		imageExecute.dispose();
		imageTerminate.dispose();
		imageCollapse.dispose();
		imageExpand.dispose();
		imageEdit.dispose();

		super.dispose();
		ImageFactory.clearCache();
	}

	/**
	 * Update entire editor area layout after change in rule editor windget's size
	 */
	public void updateEditorAreaLayout()
	{
		dataArea.layout();
		Rectangle r = scroller.getClientArea();
		scroller.setMinSize(dataArea.computeSize(r.width, SWT.DEFAULT));
	}

	/**
	 * Find server action by ID
	 * 
	 * @param id action id
	 * @return server action object or null
	 */
	public ServerAction findActionById(Long id)
	{
		return actions.get(id);
	}

	/**
	 * Find server actions for list of Ids
	 * 
	 * @param idList list of action identifiers
	 * @return list of server actions
	 */
	public Map<Long, ServerAction> findServerActions(List<Long> idList)
	{
		Map<Long, ServerAction> resultSet = new HashMap<Long, ServerAction>();
		for(Long id : idList)
		{
			ServerAction action = actions.get(id);
			if (action != null)
				resultSet.put(id, action);
		}
		return resultSet;
	}

	/**
	 * Return complete actions list
	 * 
	 * @return actions list
	 */
	public Collection<ServerAction> getActions()
	{
		return actions.values();
	}
	
   /**
    * @return AlarmCategory
    */
   public AlarmCategory findAlarmCategoryById(Long id)
   {
      NXCSession session = (NXCSession)ConsoleSharedData.getSession();
      
      return session.findAlarmCategoryById(id);
   }	
	
	/**
	 * @return the normalFont
	 */
	public Font getNormalFont()
	{
		return normalFont;
	}

	/**
	 * @return the boldFont
	 */
	public Font getBoldFont()
	{
		return boldFont;
	}

	/**
	 * @return the imageAlarm
	 */
	public Image getImageAlarm()
	{
		return imageAlarm;
	}

	/**
	 * @return the imageExecute
	 */
	public Image getImageExecute()
	{
		return imageExecute;
	}

	/**
	 * @return the imageTerminate
	 */
	public Image getImageTerminate()
	{
		return imageTerminate;
	}

	/**
	 * @return the imageStop
	 */
	public Image getImageStop()
	{
		return imageStop;
	}

	/**
	 * @return the imageCollapse
	 */
	public Image getImageCollapse()
	{
		return imageCollapse;
	}

	/**
	 * @return the imageExpand
	 */
	public Image getImageExpand()
	{
		return imageExpand;
	}

	/**
	 * @return the imageEdit
	 */
	public Image getImageEdit()
	{
		return imageEdit;
	}

	/**
	 * @return the modified
	 */
	public boolean isModified()
	{
		return modified;
	}

	/**
	 * @param modified the modified to set
	 */
	public void setModified(boolean modified)
	{
		this.modified = modified;
		actionSave.setEnabled(modified);
		firePropertyChange(PROP_DIRTY);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.ISaveablePart#doSave(org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public void doSave(IProgressMonitor monitor)
	{
		try
		{
			session.saveEventProcessingPolicy(policy);
		}
		catch(Exception e)
		{
			MessageDialogHelper.openError(getViewSite().getShell(), Messages.get().EventProcessingPolicyEditor_Error, Messages.get().EventProcessingPolicyEditor_SaveError + e.getMessage());
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.ISaveablePart#doSaveAs()
	 */
	@Override
	public void doSaveAs()
	{
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.ISaveablePart#isDirty()
	 */
	@Override
	public boolean isDirty()
	{
		return modified;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.ISaveablePart#isSaveAsAllowed()
	 */
	@Override
	public boolean isSaveAsAllowed()
	{
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.ISaveablePart#isSaveOnCloseNeeded()
	 */
	@Override
	public boolean isSaveOnCloseNeeded()
	{
		return modified;
	}

	/**
	 * Clear selection
	 */
	private void clearSelection()
	{
		for(RuleEditor e : selection)
			e.setSelected(false);
		selection.clear();
		lastSelectedRule = -1;
	}

	/**
	 * Set selection to given rule
	 * 
	 * @param e rule editor
	 */
	public void setSelection(RuleEditor e)
	{
	   if (!e.isDragged())
         clearSelection();      
      
      addToSelection(e, false);
	}

	/**
	 * Add rule to selection
	 * 
	 * @param e rule editor
	 */
	public void addToSelection(RuleEditor e, boolean allFromPrevSelection)
	{
		if (allFromPrevSelection && (lastSelectedRule != -1))
		{
			int direction = Integer.signum(e.getRuleNumber() - lastSelectedRule);
			for(int i = lastSelectedRule + direction; i != e.getRuleNumber(); i += direction)
			{
				RuleEditor r = ruleEditors.get(i - 1);
				selection.add(r);
				r.setSelected(true);
			}
		}
		selection.add(e);
		e.setSelected(true);
		lastSelectedRule = e.getRuleNumber();
		onSelectionChange();
	}

	/**
	 * Internal handler for selection change
	 */
	private void onSelectionChange()
	{
		actionDelete.setEnabled(selection.size() > 0);
		actionInsertBefore.setEnabled(selection.size() == 1);
		actionInsertAfter.setEnabled(selection.size() == 1);
		actionCut.setEnabled(selection.size() > 0);
		actionCopy.setEnabled(selection.size() > 0);
		actionPaste.setEnabled((selection.size() == 1) && !clipboard.isEmpty());
	}

	/**
	 * Delete selected rules
	 */
	private void deleteSelectedRules()
	{
		for(RuleEditor e : selection)
		{
			policy.deleteRule(e.getRule());
			ruleEditors.remove(e);
			e.dispose();
		}

		// Renumber rules
		for(int i = 0; i < ruleEditors.size(); i++)
			ruleEditors.get(i).setRuleNumber(i + 1);

		selection.clear();
		lastSelectedRule = -1;
		onSelectionChange();

		updateEditorAreaLayout();
		setModified(true);
	}

   /**
    * Insert new rule at given position
    * 
    * @param position
    */
   private void insertRule(int position)
   {
      EventProcessingPolicyRule rule = new EventProcessingPolicyRule();
      rule.setRuleNumber(position + 1);
      policy.insertRule(rule, position);

      RuleEditor editor = new RuleEditor(dataArea, rule, this);
      ruleEditors.add(position, editor);
      GridData gd = new GridData();
      gd.horizontalAlignment = SWT.FILL;
      gd.grabExcessHorizontalSpace = true;
      editor.setLayoutData(gd);

      for(int i = position + 1; i < ruleEditors.size(); i++)
         ruleEditors.get(i).setRuleNumber(i + 1);

      if (position < ruleEditors.size() - 1)
      {
         RuleEditor anchor = null;
         for(int i = position + 1; i < ruleEditors.size(); i++)
            if (!ruleEditors.get(i).isDisposed())
            {
               anchor = ruleEditors.get(i);
               break;
            }
         if (anchor != null)
            editor.moveAbove(anchor);
      }
      updateEditorAreaLayout();

      setModified(true);
   }

	/**
	 * Cut selected rules to internal clipboard
	 */
	private void cutRules()
	{
		clipboard.clear();
		actionPaste.setEnabled(true);

		for(RuleEditor e : selection)
		{
			clipboard.add(e.getRule());
			policy.deleteRule(e.getRule());
			ruleEditors.remove(e);
			e.dispose();
		}

		// Renumber rules
		for(int i = 0; i < ruleEditors.size(); i++)
			ruleEditors.get(i).setRuleNumber(i + 1);

		selection.clear();
		lastSelectedRule = -1;
		onSelectionChange();

		updateEditorAreaLayout();
		setModified(true);
	}

	/**
	 * Copy selected rules to internal clipboard
	 */
	private void copyRules()
	{
		clipboard.clear();
		actionPaste.setEnabled(true);

		for(RuleEditor e : selection)
			clipboard.add(new EventProcessingPolicyRule(e.getRule()));
	}

   /**
    * Paste rules from internal clipboard
    */
   private void pasteRules()
   {
      int position = lastSelectedRule;

      RuleEditor anchor = null;
      if (position < ruleEditors.size() - 1)
      {
         for(int i = position; i < ruleEditors.size(); i++)
            if (!ruleEditors.get(i).isDisposed())
            {
               anchor = ruleEditors.get(i);
               break;
            }
      }

      for(EventProcessingPolicyRule rule : clipboard.paste())
		{
         rule.setRuleNumber(position + 1);
			policy.insertRule(rule, position);

			RuleEditor editor = new RuleEditor(dataArea, rule, this);
			ruleEditors.add(position, editor);
			GridData gd = new GridData();
			gd.horizontalAlignment = SWT.FILL;
			gd.grabExcessHorizontalSpace = true;
			editor.setLayoutData(gd);

			if (anchor != null)
            editor.moveAbove(anchor);

			position++;
		}

		for(int i = position; i < ruleEditors.size(); i++)
			ruleEditors.get(i).setRuleNumber(i + 1);

		updateEditorAreaLayout();
		setModified(true);
	}

	/**
    * Moves rule selection
    * 
    * @param anchor - where the selection is being moved
    */
   public void moveSelection(RuleEditor anchor)
   {
      if (selection.contains(anchor))
      {
         for(RuleEditor s : selection)
            s.setDragged(false);
         return;
      }
      
      List<RuleEditor> movedRuleEditors = new ArrayList<RuleEditor>();
      for(RuleEditor e : ruleEditors)
      {
         if (!selection.contains(e))
         {
            movedRuleEditors.add(e);
            if (e.equals(anchor))
            {
               RuleEditor curr = anchor;
               for(RuleEditor s : selection)
               {
                  movedRuleEditors.add(s);
                  s.moveBelow(curr);
                  curr = s;
                  s.setDragged(false);
               }
            }
         }
      }

      policy = new EventProcessingPolicy(movedRuleEditors.size());
      int i = 0;
      for(RuleEditor e : movedRuleEditors)
      {
         policy.addRule(e.getRule());
         e.setRuleNumber(++i);
      }

      ruleEditors = movedRuleEditors;
      anchor.setDragged(false);

      updateEditorAreaLayout();
      setModified(true);
   }
	
	/**
	 * Enable selected rules
	 */
	private void enableRules(boolean enabled)
	{
		for(RuleEditor e : selection)
			e.enableRule(enabled);
	}

	/**
	 * Fill context menu for rule
	 * 
	 * @param manager menu manager
	 */
	public void fillRuleContextMenu(IMenuManager manager)
	{
		manager.add(actionEnableRule);
		manager.add(actionDisableRule);
		manager.add(new Separator());
		manager.add(actionInsertBefore);
		manager.add(actionInsertAfter);
		manager.add(new Separator());
		manager.add(actionCut);
		manager.add(actionCopy);
		manager.add(actionPaste);
		manager.add(new Separator());
		manager.add(actionDelete);
	}

	/**
	 * @return the imageSituation
	 */
	public Image getImageSituation()
	{
		return imageSituation;
	}

   /**
    * Enable or disable filter
    * 
    * @param enable New filter state
    */
   private void enableFilter(boolean enable)
   {
      filterEnabled = enable;
      filterControl.setVisible(filterEnabled);
      FormData fd = (FormData)scroller.getLayoutData();
      fd.top = enable ? new FormAttachment(filterControl) : new FormAttachment(0, 0);
      scroller.getParent().layout();
      if (enable)
      {
         filterControl.setFocus();
      }
      else
      {
         filterControl.setText(""); //$NON-NLS-1$
         onFilterModify();
      }
      actionShowFilter.setChecked(enable);
   }

   /**
    * Handler for filter modification
    */
   private void onFilterModify()
   {
      filterText = filterControl.getText().trim().toLowerCase();

      // change editors visibility
      RuleEditor prev = null;
      for(int i = 0; i < ruleEditors.size(); i++)
      {
         RuleEditor e = ruleEditors.get(i);
         boolean visible = isRuleVisible(e.getRule());
         if (!e.isDisposed() && !visible)
         {
            e.dispose();
            selection.remove(e);
         }
         else if (e.isDisposed() && visible)
         {
            e = new RuleEditor(dataArea, e.getRule(), this);
            GridData gd = new GridData();
            gd.horizontalAlignment = SWT.FILL;
            gd.grabExcessHorizontalSpace = true;
            e.setLayoutData(gd);
            if (prev != null)
               e.moveBelow(prev);
            else
               e.moveAbove(null);
            ruleEditors.set(i, e);
         }
         if (!e.isDisposed())
            prev = e;
      }

      updateEditorAreaLayout();
   }

   /**
    * Check if given rule should be visible
    * 
    * @param rule
    * @return
    */
   private boolean isRuleVisible(EventProcessingPolicyRule rule)
   {
      if ((filterText == null) || filterText.isEmpty())
         return true;
      
      if (rule.getComments().toLowerCase().contains(filterText))
         return true;

      // check event names
      for(Long code : rule.getEvents())
      {
         EventTemplate evt = session.findEventTemplateByCode(code);
         if ((evt != null) && evt.getName().toLowerCase().contains(filterText))
            return true;
      }
      
      // check object names
      for(Long id : rule.getSources())
      {
         String name = session.getObjectName(id);
         if ((name != null) && name.toLowerCase().contains(filterText))
            return true;
      }
      
      return false;
   }
}
