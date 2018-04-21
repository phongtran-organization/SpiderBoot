/**
 * NetXMS - open source network management system
 * Copyright (C) 2003-2016 Raden Solutions
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
package org.netxms.ui.eclipse.filemanager.views;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.commands.ActionHandler;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnViewerEditor;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationEvent;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationStrategy;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.TreeViewerEditor;
import org.eclipse.jface.viewers.ViewerDropAdapter;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSourceAdapter;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.part.ViewPart;
import org.netxms.client.AgentFileData;
import org.netxms.client.NXCException;
import org.netxms.client.NXCSession;
import org.netxms.client.ProgressListener;
import org.netxms.client.constants.RCC;
import org.netxms.client.objects.Node;
import org.netxms.client.server.AgentFile;
import org.netxms.ui.eclipse.actions.RefreshAction;
import org.netxms.ui.eclipse.console.resources.SharedIcons;
import org.netxms.ui.eclipse.filemanager.Activator;
import org.netxms.ui.eclipse.filemanager.Messages;
import org.netxms.ui.eclipse.filemanager.dialogs.CreateFolderDialog;
import org.netxms.ui.eclipse.filemanager.dialogs.StartClientToAgentFolderUploadDialog;
import org.netxms.ui.eclipse.filemanager.dialogs.StartClientToServerFileUploadDialog;
import org.netxms.ui.eclipse.filemanager.views.helpers.AgentFileComparator;
import org.netxms.ui.eclipse.filemanager.views.helpers.AgentFileFilter;
import org.netxms.ui.eclipse.filemanager.views.helpers.AgentFileLabelProvider;
import org.netxms.ui.eclipse.filemanager.views.helpers.ViewAgentFilesProvider;
import org.netxms.ui.eclipse.jobs.ConsoleJob;
import org.netxms.ui.eclipse.jobs.ConsoleJobCallingServerJob;
import org.netxms.ui.eclipse.shared.ConsoleSharedData;
import org.netxms.ui.eclipse.tools.DialogData;
import org.netxms.ui.eclipse.tools.MessageDialogHelper;
import org.netxms.ui.eclipse.tools.WidgetHelper;
import org.netxms.ui.eclipse.widgets.FilterText;
import org.netxms.ui.eclipse.widgets.SortableTableViewer;
import org.netxms.ui.eclipse.widgets.SortableTreeViewer;

/**
 * File manager for agent files
 */
public class AgentFileManager extends ViewPart {
	public static final String ID = "org.netxms.ui.eclipse.filemanager.views.AgentFileManager"; //$NON-NLS-1$

	private static final String TABLE_CONFIG_PREFIX = "AgentFileManager"; //$NON-NLS-1$

	// Columns
	public static final int COLUMN_NAME = 0;
	public static final int COLUMN_TYPE = 1;
	public static final int COLUMN_SIZE = 2;
	public static final int COLUMN_MODIFYED = 3;
	public static final int COLUMN_OWNER = 4;
	public static final int COLUMN_GROUP = 5;
	public static final int COLUMN_ACCESS_RIGHTS = 6;

	private boolean initShowFilter = true;
	private Composite content;
	private AgentFileFilter filter;
	private FilterText filterText;
	private SortableTreeViewer viewer;
	private NXCSession session;
	private Action actionRefreshAll;
	private Action actionUploadFile;
	private Action actionUploadFolder;
	private Action actionDelete;
	private Action actionRename;
	private Action actionRefreshDirectory;
	private Action actionShowFilter;
	private Action actionDownloadFile;
	private Action actionTailFile;
	private Action actionShowFile;
	private Action actionCreateDirectory;
	private Action actionCalculateFolderSize;
	private Action actionCopyFilePath;
	private Action actionCopyFileName;
	private long objectId = 0;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.ViewPart#init(org.eclipse.ui.IViewSite)
	 */
	@Override
	public void init(IViewSite site) throws PartInitException {
		super.init(site);

		session = (NXCSession) ConsoleSharedData.getSession();
		IDialogSettings settings = Activator.getDefault().getDialogSettings();
		initShowFilter = safeCast(settings.get("AgentFileManager.showFilter"),
				settings.getBoolean("AgentFileManager.showFilter"),
				initShowFilter);
		objectId = Long.parseLong(site.getSecondaryId());
		setPartName(String.format(Messages.get().AgentFileManager_PartTitle,
				session.getObjectName(objectId)));
	}

	/**
	 * @param b
	 * @param defval
	 * @return
	 */
	private static boolean safeCast(String s, boolean b, boolean defval) {
		return (s != null) ? b : defval;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets
	 * .Composite)
	 */
	@Override
	public void createPartControl(Composite parent) {
		content = new Composite(parent, SWT.NONE);
		content.setLayout(new FormLayout());

		// Create filter area
		filterText = new FilterText(content, SWT.NONE);
		filterText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				onFilterModify();
			}
		});

		String os = ((Node) session.findObjectById(objectId))
				.getSystemDescription(); //$NON-NLS-1$

		if (os.contains("Windows")) //if OS is windows don't show group and access rights columns //$NON-NLS-1$
		{
			final String[] columnNames = {
					Messages.get().AgentFileManager_ColName,
					Messages.get().AgentFileManager_ColType,
					Messages.get().AgentFileManager_ColSize,
					Messages.get().AgentFileManager_ColDate,
					Messages.get().AgentFileManager_ColOwner };
			final int[] columnWidths = { 300, 120, 150, 150, 150 };
			viewer = new SortableTreeViewer(content, columnNames, columnWidths,
					0, SWT.UP, SortableTableViewer.DEFAULT_STYLE);
		} else {
			final String[] columnNames = {
					Messages.get().AgentFileManager_ColName,
					Messages.get().AgentFileManager_ColType,
					Messages.get().AgentFileManager_ColSize,
					Messages.get().AgentFileManager_ColDate,
					Messages.get().AgentFileManager_ColOwner,
					Messages.get().AgentFileManager_ColGroup,
					Messages.get().AgentFileManager_ColAccessRights };
			final int[] columnWidths = { 300, 120, 150, 150, 150, 150, 200 };
			viewer = new SortableTreeViewer(content, columnNames, columnWidths,
					0, SWT.UP, SortableTableViewer.DEFAULT_STYLE);
		}

		WidgetHelper.restoreTreeViewerSettings(viewer, Activator.getDefault()
				.getDialogSettings(), TABLE_CONFIG_PREFIX);
		viewer.setContentProvider(new ViewAgentFilesProvider());
		viewer.setLabelProvider(new AgentFileLabelProvider());
		viewer.setComparator(new AgentFileComparator());
		filter = new AgentFileFilter();
		viewer.addFilter(filter);
		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection selection = (IStructuredSelection) event
						.getSelection();
				if (selection != null) {
					actionDelete.setEnabled(selection.size() > 0);
					actionCalculateFolderSize.setEnabled(selection.size() > 0);
				}
			}
		});
		viewer.getTree().addDisposeListener(new DisposeListener() {
			@Override
			public void widgetDisposed(DisposeEvent e) {
				WidgetHelper.saveTreeViewerSettings(viewer, Activator
						.getDefault().getDialogSettings(), TABLE_CONFIG_PREFIX);
			}
		});
		enableDragSupport();
		enableDropSupport();
		enableInPlaceRename();

		// Setup layout
		FormData fd = new FormData();
		fd.left = new FormAttachment(0, 0);
		fd.top = new FormAttachment(filterText);
		fd.right = new FormAttachment(100, 0);
		fd.bottom = new FormAttachment(100, 0);
		viewer.getTree().setLayoutData(fd);

		fd = new FormData();
		fd.left = new FormAttachment(0, 0);
		fd.top = new FormAttachment(0, 0);
		fd.right = new FormAttachment(100, 0);
		filterText.setLayoutData(fd);

		createActions();
		contributeToActionBars();
		createPopupMenu();
		activateContext();

		filterText.setCloseAction(actionShowFilter);

		// Set initial focus to filter input line
		if (initShowFilter)
			filterText.setFocus();
		else
			enableFilter(false); // Will hide filter area correctly

		refreshFileList();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.WorkbenchPart#dispose()
	 */
	@Override
	public void dispose() {
		IDialogSettings settings = Activator.getDefault().getDialogSettings();
		settings.put("AgentFileManager.showFilter", initShowFilter);
		super.dispose();
	}

	/**
	 * Activate context
	 */
	private void activateContext() {
		IContextService contextService = (IContextService) getSite()
				.getService(IContextService.class);
		if (contextService != null) {
			contextService
					.activateContext("org.netxms.ui.eclipse.filemanager.context.FileManager"); //$NON-NLS-1$
		}
	}

	/**
	 * Enable drag support in object tree
	 */
	public void enableDragSupport() {
		Transfer[] transfers = new Transfer[] { LocalSelectionTransfer
				.getTransfer() };
		viewer.addDragSupport(DND.DROP_COPY | DND.DROP_MOVE, transfers,
				new DragSourceAdapter() {
					@Override
					public void dragStart(DragSourceEvent event) {
						LocalSelectionTransfer.getTransfer().setSelection(
								viewer.getSelection());
						event.doit = true;
					}

					@Override
					public void dragSetData(DragSourceEvent event) {
						event.data = LocalSelectionTransfer.getTransfer()
								.getSelection();
					}
				});
	}

	/**
	 * Enable drop support in object tree
	 */
	public void enableDropSupport()// SubtreeType infrastructure
	{
		final Transfer[] transfers = new Transfer[] { LocalSelectionTransfer
				.getTransfer() };
		viewer.addDropSupport(DND.DROP_COPY | DND.DROP_MOVE, transfers,
				new ViewerDropAdapter(viewer) {

					@Override
					public boolean performDrop(Object data) {
						IStructuredSelection selection = (IStructuredSelection) data;
						List<?> movableSelection = selection.toList();
						for (int i = 0; i < movableSelection.size(); i++) {
							AgentFile movableObject = (AgentFile) movableSelection
									.get(i);

							moveFile((AgentFile) getCurrentTarget(),
									movableObject);

						}
						return true;
					}

					@Override
					public boolean validateDrop(Object target, int operation,
							TransferData transferType) {
						if ((target == null)
								|| !LocalSelectionTransfer.getTransfer()
										.isSupportedType(transferType))
							return false;

						IStructuredSelection selection = (IStructuredSelection) LocalSelectionTransfer
								.getTransfer().getSelection();
						if (selection.isEmpty())
							return false;

						for (final Object object : selection.toList()) {
							if (!(object instanceof AgentFile))
								return false;
						}
						if (!(target instanceof AgentFile)) {
							return false;
						} else {
							if (!((AgentFile) target).isDirectory()) {
								return false;
							}
						}
						return true;
					}
				});
	}

	/**
	 * Enable in-place renames
	 */
	private void enableInPlaceRename() {
		TreeViewerEditor.create(viewer,
				new ColumnViewerEditorActivationStrategy(viewer) {
					@Override
					protected boolean isEditorActivationEvent(
							ColumnViewerEditorActivationEvent event) {
						return event.eventType == ColumnViewerEditorActivationEvent.PROGRAMMATIC;
					}
				}, ColumnViewerEditor.DEFAULT);
		viewer.setCellEditors(new CellEditor[] { new TextCellEditor(viewer
				.getTree()) });
		viewer.setColumnProperties(new String[] { "name" }); //$NON-NLS-1$
		viewer.setCellModifier(new ICellModifier() {
			@Override
			public void modify(Object element, String property, Object value) {
				if (element instanceof Item)
					element = ((Item) element).getData();

				if (property.equals("name")) //$NON-NLS-1$
				{
					if (element instanceof AgentFile) {
						doRename((AgentFile) element, value.toString());
					}
				}
			}

			@Override
			public Object getValue(Object element, String property) {
				if (property.equals("name")) //$NON-NLS-1$
				{
					if (element instanceof AgentFile) {
						return ((AgentFile) element).getName();
					}
				}
				return null;
			}

			@Override
			public boolean canModify(Object element, String property) {
				return property.equals("name"); //$NON-NLS-1$
			}
		});
	}

	/**
	 * Do actual rename
	 * 
	 * @param AgentFile
	 * @param newName
	 */
	private void doRename(final AgentFile agentFile, final String newName) {
		new ConsoleJob("Rename file", this, Activator.PLUGIN_ID,
				Activator.PLUGIN_ID) {
			@Override
			protected String getErrorMessage() {
				return Messages.get().AgentFileManager_RenameError;
			}

			@Override
			protected void runInternal(IProgressMonitor monitor)
					throws Exception {
				final NestedVerifyOverwrite verify = new NestedVerifyOverwrite(
						agentFile.getType(), newName, true, true, false) {

					@Override
					public void executeAction() throws NXCException,
							IOException {
						session.renameAgentFile(objectId,
								agentFile.getFullName(), agentFile.getParent()
										.getFullName() + "/" + newName, false); //$NON-NLS-1$
					}

					@Override
					public void executeSameFunctionWithOverwrite()
							throws IOException, NXCException {
						session.renameAgentFile(objectId,
								agentFile.getFullName(), agentFile.getParent()
										.getFullName() + "/" + newName, true); //$NON-NLS-1$
					}
				};
				verify.run(viewer.getControl().getDisplay());

				if (verify.isOkPressed()) {
					runInUIThread(new Runnable() {
						@Override
						public void run() {
							if (verify.isOkPressed())
								refreshFileOrDirectory();
							agentFile.setName(newName);
							viewer.refresh(agentFile, true);
						}
					});
				}
			}
		}.start();
	}

	/**
	 * Create actions
	 */
	private void createActions() {
		final IHandlerService handlerService = (IHandlerService) getSite()
				.getService(IHandlerService.class);

		actionRefreshDirectory = new Action(
				Messages.get().AgentFileManager_RefreshFolder,
				SharedIcons.REFRESH) {
			@Override
			public void run() {
				refreshFileOrDirectory();
			}
		};
		actionRefreshDirectory
				.setActionDefinitionId("org.netxms.ui.eclipse.filemanager.commands.refreshFolder"); //$NON-NLS-1$
		handlerService.activateHandler(
				actionRefreshDirectory.getActionDefinitionId(),
				new ActionHandler(actionRefreshDirectory));

		actionRefreshAll = new RefreshAction(this) {
			@Override
			public void run() {
				refreshFileList();
			}
		};

		actionUploadFile = new Action(
				Messages.get().AgentFileManager_UploadFile) {
			@Override
			public void run() {
				uploadFile(false);
			}
		};
		actionUploadFile
				.setActionDefinitionId("org.netxms.ui.eclipse.filemanager.commands.uploadFile"); //$NON-NLS-1$
		handlerService.activateHandler(
				actionUploadFile.getActionDefinitionId(), new ActionHandler(
						actionUploadFile));

		actionUploadFolder = new Action(
				Messages.get().AgentFileManager_UploadFolder) {
			@Override
			public void run() {
				uploadFolder();
			}
		};

		actionDelete = new Action(Messages.get().AgentFileManager_Delete,
				SharedIcons.DELETE_OBJECT) {
			@Override
			public void run() {
				deleteFile();
			}
		};
		actionDelete
				.setActionDefinitionId("org.netxms.ui.eclipse.filemanager.commands.delete"); //$NON-NLS-1$
		handlerService.activateHandler(actionDelete.getActionDefinitionId(),
				new ActionHandler(actionDelete));

		actionRename = new Action(Messages.get().AgentFileManager_Rename) {
			@Override
			public void run() {
				renameFile();
			}
		};
		actionRename
				.setActionDefinitionId("org.netxms.ui.eclipse.filemanager.commands.rename"); //$NON-NLS-1$
		handlerService.activateHandler(actionRename.getActionDefinitionId(),
				new ActionHandler(actionRename));

		actionShowFilter = new Action(
				Messages.get().ViewServerFile_ShowFilterAction,
				Action.AS_CHECK_BOX) {
			@Override
			public void run() {
				enableFilter(!initShowFilter);
				actionShowFilter.setChecked(initShowFilter);
			}
		};
		actionShowFilter.setChecked(initShowFilter);
		actionShowFilter
				.setActionDefinitionId("org.netxms.ui.eclipse.filemanager.commands.showFilter"); //$NON-NLS-1$
		handlerService.activateHandler(
				actionShowFilter.getActionDefinitionId(), new ActionHandler(
						actionShowFilter));

		actionDownloadFile = new Action(
				Messages.get().AgentFileManager_Download) {
			@Override
			public void run() {
				startDownload();
			}
		};
		actionDownloadFile
				.setActionDefinitionId("org.netxms.ui.eclipse.filemanager.commands.download"); //$NON-NLS-1$
		handlerService
				.activateHandler(actionDownloadFile.getActionDefinitionId(),
						new ActionHandler(actionDownloadFile));

		actionTailFile = new Action(
				Messages.get().AgentFileManager_FollowChanges) {
			@Override
			public void run() {
				tailFile(true, 1024);
			}
		};

		actionShowFile = new Action(Messages.get().AgentFileManager_Show) {
			@Override
			public void run() {
				tailFile(false, 0);
			}
		};

		actionCreateDirectory = new Action(
				Messages.get().AgentFileManager_CreateFolder) {
			@Override
			public void run() {
				createFolder();
			}
		};
		actionCreateDirectory
				.setActionDefinitionId("org.netxms.ui.eclipse.filemanager.commands.newFolder"); //$NON-NLS-1$
		handlerService.activateHandler(
				actionCreateDirectory.getActionDefinitionId(),
				new ActionHandler(actionCreateDirectory));

		actionCalculateFolderSize = new Action("Calculate folder &size") {
			@Override
			public void run() {
				calculateFolderSize();
			}
		};
		actionCalculateFolderSize
				.setActionDefinitionId("org.netxms.ui.eclipse.filemanager.commands.calculateFolderSize"); //$NON-NLS-1$
		handlerService.activateHandler(
				actionCalculateFolderSize.getActionDefinitionId(),
				new ActionHandler(actionCalculateFolderSize));

		actionCopyFileName = new Action("Copy file &name") {
			@Override
			public void run() {
				copyFileName();
			}
		};
		actionCopyFileName
				.setActionDefinitionId("org.netxms.ui.eclipse.filemanager.commands.copyFileName"); //$NON-NLS-1$
		handlerService
				.activateHandler(actionCopyFileName.getActionDefinitionId(),
						new ActionHandler(actionCopyFileName));

		actionCopyFilePath = new Action("Copy file &path") {
			@Override
			public void run() {
				copyFilePath();
			}
		};
		actionCopyFilePath
				.setActionDefinitionId("org.netxms.ui.eclipse.filemanager.commands.copyFilePath"); //$NON-NLS-1$
		handlerService
				.activateHandler(actionCopyFilePath.getActionDefinitionId(),
						new ActionHandler(actionCopyFilePath));
	}

	/**
	 * Contribute actions to action bar
	 */
	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
	}

	/**
	 * Fill local pull-down menu
	 * 
	 * @param manager
	 *            Menu manager for pull-down menu
	 */
	private void fillLocalPullDown(IMenuManager manager) {
		manager.add(actionShowFilter);
		manager.add(new Separator());
		manager.add(actionRefreshAll);
	}

	/**
	 * Fill local tool bar
	 * 
	 * @param manager
	 *            Menu manager for local tool bar
	 */
	private void fillLocalToolBar(IToolBarManager manager) {
		manager.add(actionRefreshAll);
	}

	/**
	 * Create pop-up menu for user list
	 */
	private void createPopupMenu() {
		// Create menu manager
		MenuManager menuMgr = new MenuManager();
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager mgr) {
				fillContextMenu(mgr);
			}
		});

		// Create menu
		Menu menu = menuMgr.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);

		// Register menu for extension.
		getSite().registerContextMenu(menuMgr, viewer);
	}

	/**
	 * Fill context menu
	 * 
	 * @param mgr
	 *            Menu manager
	 */
	protected void fillContextMenu(final IMenuManager mgr) {
		IStructuredSelection selection = (IStructuredSelection) viewer
				.getSelection();
		if (selection.isEmpty())
			return;

		if (selection.size() == 1) {
			if (((AgentFile) selection.getFirstElement()).isDirectory()) {
				mgr.add(actionUploadFile);
				mgr.add(actionUploadFolder);
			} else {
				mgr.add(actionTailFile);
				mgr.add(actionShowFile);
			}
		}

		mgr.add(actionDownloadFile);

		if (isFolderOnlySelection(selection))
			mgr.add(actionCalculateFolderSize);
		mgr.add(new Separator());

		if (selection.size() == 1) {
			if (((AgentFile) selection.getFirstElement()).isDirectory()) {
				mgr.add(actionCreateDirectory);
			}
			mgr.add(actionRename);
		}
		mgr.add(actionDelete);
		mgr.add(new Separator());
		if (selection.size() == 1) {
			mgr.add(actionCopyFileName);
			mgr.add(actionCopyFilePath);
		}
		mgr.add(new Separator());
		mgr.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
		if ((selection.size() == 1)
				&& ((AgentFile) selection.getFirstElement()).isDirectory()) {
			mgr.add(new Separator());
			mgr.add(actionRefreshDirectory);
		}
	}

	/**
	 * Check if given selection contains only folders
	 * 
	 * @param selection
	 * @return
	 */
	private boolean isFolderOnlySelection(IStructuredSelection selection) {
		for (Object o : selection.toList())
			if (!((AgentFile) o).isDirectory())
				return false;
		return true;
	}

	/**
	 * Refresh file list
	 */
	private void refreshFileList() {
		new ConsoleJob(Messages.get().SelectServerFileDialog_JobTitle, null,
				Activator.PLUGIN_ID, null) {
			@Override
			protected void runInternal(IProgressMonitor monitor)
					throws Exception {
				final List<AgentFile> files = session.listAgentFiles(null,
						"/", objectId); //$NON-NLS-1$
				runInUIThread(new Runnable() {
					@Override
					public void run() {
						viewer.setInput(files);
					}
				});
			}

			@Override
			protected String getErrorMessage() {
				return Messages.get().SelectServerFileDialog_JobError;
			}
		}.start();
	}

	/**
	 * Refresh file list
	 */
	private void refreshFileOrDirectory() {
		IStructuredSelection selection = (IStructuredSelection) viewer
				.getSelection();
		if (selection.isEmpty())
			return;

		final Object[] objects = selection.toArray();

		new ConsoleJob("Reading remote directory", null, Activator.PLUGIN_ID,
				null) {
			@Override
			protected void runInternal(IProgressMonitor monitor)
					throws Exception {
				for (int i = 0; i < objects.length; i++) {
					if (!((AgentFile) objects[i]).isDirectory())
						objects[i] = ((AgentFile) objects[i]).getParent();

					final AgentFile sf = ((AgentFile) objects[i]);
					sf.setChildren(session.listAgentFiles(sf, sf.getFullName(),
							objectId));

					runInUIThread(new Runnable() {
						@Override
						public void run() {
							viewer.refresh(sf);
						}
					});
				}
			}

			@Override
			protected String getErrorMessage() {
				return "Cannot read remote directory";
			}
		}.start();
	}

	/**
	 * Upload local file to agent
	 */
	private void uploadFile(final boolean overvrite) {
		IStructuredSelection selection = (IStructuredSelection) viewer
				.getSelection();
		if (selection.isEmpty())
			return;

		final Object[] objects = selection.toArray();
		final AgentFile upladFolder = ((AgentFile) objects[0]).isDirectory() ? ((AgentFile) objects[0])
				: ((AgentFile) objects[0]).getParent();

		final StartClientToServerFileUploadDialog dlg = new StartClientToServerFileUploadDialog(
				getSite().getShell());
		if (dlg.open() == Window.OK) {
			final NXCSession session = (NXCSession) ConsoleSharedData
					.getSession();
			new ConsoleJob(Messages.get().AgentFileManager_UploadFileJobTitle,
					null, Activator.PLUGIN_ID, null) {
				@Override
				protected void runInternal(final IProgressMonitor monitor)
						throws Exception {
					List<File> fileList = dlg.getLocalFiles();
					for (int i = 0; i < fileList.size(); i++) {
						final File localFile = fileList.get(i);
						String remoteFile = fileList.get(i).getName();
						if (fileList.size() == 1)
							remoteFile = dlg.getRemoteFileName();
						final String rFileName = remoteFile;

						new NestedVerifyOverwrite(
								localFile.isDirectory() ? AgentFile.DIRECTORY
										: AgentFile.FILE, localFile.getName(),
								true, true, false) {

							@Override
							public void executeAction() throws NXCException,
									IOException {
								session.uploadLocalFileToAgent(
										objectId,
										localFile,
										upladFolder.getFullName()
												+ "/" + rFileName, overvrite, new ProgressListener() { //$NON-NLS-1$
											private long prevWorkDone = 0;

											@Override
											public void setTotalWorkAmount(
													long workTotal) {
												monitor.beginTask(
														Messages.get().UploadFileToServer_TaskNamePrefix
																+ localFile
																		.getAbsolutePath(),
														(int) workTotal);
											}

											@Override
											public void markProgress(
													long workDone) {
												monitor.worked((int) (workDone - prevWorkDone));
												prevWorkDone = workDone;
											}
										});
								monitor.done();
							}

							@Override
							public void executeSameFunctionWithOverwrite()
									throws IOException, NXCException {
								session.uploadLocalFileToAgent(
										objectId,
										localFile,
										upladFolder.getFullName()
												+ "/" + rFileName, true, new ProgressListener() { //$NON-NLS-1$
											private long prevWorkDone = 0;

											@Override
											public void setTotalWorkAmount(
													long workTotal) {
												monitor.beginTask(
														Messages.get().UploadFileToServer_TaskNamePrefix
																+ localFile
																		.getAbsolutePath(),
														(int) workTotal);
											}

											@Override
											public void markProgress(
													long workDone) {
												monitor.worked((int) (workDone - prevWorkDone));
												prevWorkDone = workDone;
											}
										});
								monitor.done();
							}
						}.run(viewer.getControl().getDisplay());
					}

					upladFolder.setChildren(session.listAgentFiles(upladFolder,
							upladFolder.getFullName(), objectId));
					runInUIThread(new Runnable() {
						@Override
						public void run() {

							viewer.refresh(upladFolder, true);
						}
					});
				}

				@Override
				protected String getErrorMessage() {
					return "Cannot upload file to remote agent";
				}
			}.start();
		}
	}

	class UploadConsoleJob extends ConsoleJob {
		private boolean askFolderOverwrite;
		private boolean askFileOverwrite;
		private boolean overwrite;
		private File folder;
		private AgentFile uploadFolder;
		private String remoteFileName;

		public UploadConsoleJob(String name, IWorkbenchPart wbPart,
				String pluginId, Object jobFamily, File folder,
				final AgentFile uploadFolder, final String remoteFileName) {
			super(name, wbPart, pluginId, jobFamily);
			askFolderOverwrite = true;
			askFileOverwrite = true;
			overwrite = false;
			this.folder = folder;
			this.uploadFolder = uploadFolder;
			this.remoteFileName = remoteFileName;
		}

		@Override
		protected void runInternal(IProgressMonitor monitor) throws Exception {
			NestedVerifyOverwrite verify = new NestedVerifyOverwrite(
					AgentFile.DIRECTORY, folder.getName(), askFolderOverwrite,
					askFileOverwrite, overwrite) {

				@Override
				public void executeAction() throws NXCException, IOException {
					session.createFolderOnAgent(objectId,
							uploadFolder.getFullName() + "/" + remoteFileName); //$NON-NLS-1$
				}

				@Override
				public void executeSameFunctionWithOverwrite()
						throws NXCException, IOException {
					// do nothing
				}
			};
			verify.run(viewer.getControl().getDisplay());
			askFolderOverwrite = verify.askFolderOverwrite();

			uploadFilesInFolder(folder, uploadFolder.getFullName()
					+ "/" + remoteFileName, monitor); //$NON-NLS-1$

			uploadFolder.setChildren(session.listAgentFiles(uploadFolder,
					uploadFolder.getFullName(), objectId));
			runInUIThread(new Runnable() {
				@Override
				public void run() {
					viewer.refresh(uploadFolder, true);
				}
			});
		}

		@Override
		protected String getErrorMessage() {
			return Messages.get().UploadFileToServer_JobError;
		}

		/**
		 * Recursively uploads files to agent and creates correct folders
		 * 
		 * @param folder
		 * @param upladFolder
		 * @param monitor
		 * @throws NXCException
		 * @throws IOException
		 */
		public void uploadFilesInFolder(final File folder,
				final String uploadFolder, final IProgressMonitor monitor)
				throws NXCException, IOException {
			for (final File fileEntry : folder.listFiles()) {
				if (fileEntry.isDirectory()) {
					NestedVerifyOverwrite verify = new NestedVerifyOverwrite(
							AgentFile.DIRECTORY, fileEntry.getName(),
							askFolderOverwrite, askFileOverwrite, overwrite) {

						@Override
						public void executeAction() throws NXCException,
								IOException {
							session.createFolderOnAgent(objectId, uploadFolder
									+ "/" + fileEntry.getName()); //$NON-NLS-1$
						}

						@Override
						public void executeSameFunctionWithOverwrite()
								throws NXCException, IOException {
							// do nothing
						}
					};
					verify.run(viewer.getControl().getDisplay());
					askFolderOverwrite = verify.askFolderOverwrite();

					uploadFilesInFolder(fileEntry, uploadFolder
							+ "/" + fileEntry.getName(), monitor); //$NON-NLS-1$
				} else {
					NestedVerifyOverwrite verify = new NestedVerifyOverwrite(
							AgentFile.FILE, fileEntry.getName(),
							askFolderOverwrite, askFileOverwrite, overwrite) {

						@Override
						public void executeAction() throws NXCException,
								IOException {
							session.uploadLocalFileToAgent(
									objectId,
									fileEntry,
									uploadFolder + "/" + fileEntry.getName(), overwrite, new ProgressListener() { //$NON-NLS-1$
										private long prevWorkDone = 0;

										@Override
										public void setTotalWorkAmount(
												long workTotal) {
											monitor.beginTask(
													Messages.get().UploadFileToServer_TaskNamePrefix
															+ fileEntry
																	.getAbsolutePath(),
													(int) workTotal);
										}

										@Override
										public void markProgress(long workDone) {
											monitor.worked((int) (workDone - prevWorkDone));
											prevWorkDone = workDone;
										}
									});
							monitor.done();
						}

						@Override
						public void executeSameFunctionWithOverwrite()
								throws NXCException, IOException {
							session.uploadLocalFileToAgent(
									objectId,
									fileEntry,
									uploadFolder + "/" + fileEntry.getName(), true, new ProgressListener() { //$NON-NLS-1$
										private long prevWorkDone = 0;

										@Override
										public void setTotalWorkAmount(
												long workTotal) {
											monitor.beginTask(
													Messages.get().UploadFileToServer_TaskNamePrefix
															+ fileEntry
																	.getAbsolutePath(),
													(int) workTotal);
										}

										@Override
										public void markProgress(long workDone) {
											monitor.worked((int) (workDone - prevWorkDone));
											prevWorkDone = workDone;
										}
									});
							monitor.done();
						}
					};
					verify.run(viewer.getControl().getDisplay());
					askFileOverwrite = verify.askFileOverwrite();
					if (!askFileOverwrite)
						overwrite = verify.isOkPressed();
				}
			}
		}
	}

	/**
	 * Upload local folder to agent
	 */
	private void uploadFolder() {
		IStructuredSelection selection = (IStructuredSelection) viewer
				.getSelection();
		if (selection.isEmpty())
			return;

		final Object[] objects = selection.toArray();
		final AgentFile upladFolder = ((AgentFile) objects[0]).isDirectory() ? ((AgentFile) objects[0])
				: ((AgentFile) objects[0]).getParent();

		final StartClientToAgentFolderUploadDialog dlg = new StartClientToAgentFolderUploadDialog(
				getSite().getShell());
		if (dlg.open() == Window.OK) {
			ConsoleJob job = new UploadConsoleJob(
					Messages.get().AgentFileManager_UploadFolderJobTitle, null,
					Activator.PLUGIN_ID, null, dlg.getLocalFile(), upladFolder,
					dlg.getRemoteFileName());
			job.start();
		}
	}

	/**
	 * Delete selected file
	 */
	private void deleteFile() {
		IStructuredSelection selection = (IStructuredSelection) viewer
				.getSelection();
		if (selection.isEmpty())
			return;

		if (!MessageDialogHelper.openConfirm(getSite().getShell(),
				Messages.get().ViewServerFile_DeleteConfirmation,
				Messages.get().ViewServerFile_DeletAck))
			return;

		final Object[] objects = selection.toArray();
		new ConsoleJob(Messages.get().ViewServerFile_DeletFileFromServerJob,
				this, Activator.PLUGIN_ID, Activator.PLUGIN_ID) {
			@Override
			protected String getErrorMessage() {
				return Messages.get().ViewServerFile_ErrorDeleteFileJob;
			}

			@Override
			protected void runInternal(IProgressMonitor monitor)
					throws Exception {
				for (int i = 0; i < objects.length; i++) {
					final AgentFile sf = (AgentFile) objects[i];
					session.deleteAgentFile(objectId, sf.getFullName());

					runInUIThread(new Runnable() {
						@Override
						public void run() {
							sf.getParent().removeChield(sf);
							viewer.refresh(sf.getParent());
						}
					});
				}
			}
		}.start();
	}

	/**
	 * Starts file tail
	 */
	private void tailFile(final boolean followChanges, final int offset) {
		IStructuredSelection selection = (IStructuredSelection) viewer
				.getSelection();
		if (selection.isEmpty())
			return;

		final Object[] objects = selection.toArray();

		if (((AgentFile) objects[0]).isDirectory())
			return;

		final AgentFile sf = ((AgentFile) objects[0]);

		ConsoleJobCallingServerJob job = new ConsoleJobCallingServerJob(
				Messages.get().AgentFileManager_DownloadJobTitle, null,
				Activator.PLUGIN_ID, null) {
			@Override
			protected String getErrorMessage() {
				return String.format(
						Messages.get().AgentFileManager_DownloadJobError,
						sf.getFullName(), objectId);
			}

			@Override
			protected void runInternal(final IProgressMonitor monitor)
					throws Exception {
				final AgentFileData file = session.downloadFileFromAgent(
						objectId, sf.getFullName(), offset, followChanges,
						null, this);
				runInUIThread(new Runnable() {
					@Override
					public void run() {
						try {
							String secondaryId = Long.toString(objectId)
									+ "&" + URLEncoder.encode(sf.getName(), "UTF-8"); //$NON-NLS-1$ //$NON-NLS-2$
							AgentFileViewer.createView(secondaryId, objectId,
									file, followChanges);
						} catch (Exception e) {
							final IWorkbenchWindow window = PlatformUI
									.getWorkbench().getActiveWorkbenchWindow();
							MessageDialogHelper
									.openError(
											window.getShell(),
											Messages.get().AgentFileManager_Error,
											String.format(
													Messages.get().AgentFileManager_OpenViewError,
													e.getLocalizedMessage()));
							Activator
									.logError(
											"Exception in AgentFileManager.tailFile",
											e);
						}
					}
				});
			}
		};
		job.start();
	}

	/**
	 * Download file from agent
	 */
	private void startDownload() {
		IStructuredSelection selection = (IStructuredSelection) viewer
				.getSelection();
		if (selection.isEmpty())
			return;

		AgentFile sf = (AgentFile) selection.getFirstElement();

		final String target;
		if (!sf.isDirectory() && (selection.size() == 1)) {
			FileDialog dlg = new FileDialog(getSite().getShell(), SWT.SAVE);
			dlg.setText(Messages.get().AgentFileManager_StartDownloadDialogTitle);
			String[] filterExtensions = { "*.*" }; //$NON-NLS-1$
			dlg.setFilterExtensions(filterExtensions);
			String[] filterNames = { Messages.get().AgentFileManager_AllFiles };
			dlg.setFilterNames(filterNames);
			dlg.setFileName(sf.getName());
			dlg.setOverwrite(true);
			target = dlg.open();
		} else {
			DirectoryDialog dlg = new DirectoryDialog(getSite().getShell());
			target = dlg.open();
		}

		if (target == null)
			return;

		final List<AgentFile> files = new ArrayList<AgentFile>(selection.size());
		for (Object o : selection.toList())
			files.add((AgentFile) o);

		ConsoleJobCallingServerJob job = new ConsoleJobCallingServerJob(
				"Download from agent", null, Activator.PLUGIN_ID, null) {
			@Override
			protected void runInternal(IProgressMonitor monitor)
					throws Exception {
				if (files.size() == 1) {
					AgentFile f = files.get(0);
					if (f.isDirectory()) {
						long dirSize = -1;
						try {
							dirSize = session.getAgentFileInfo(f).getSize();
						} catch (Exception e) {
						}
						monitor.beginTask(String.format(
								"Downloading directory %s", f.getName()),
								(dirSize >= 0) ? (int) dirSize
										: IProgressMonitor.UNKNOWN);
						downloadDir(f, target + "/" + f.getName(), monitor,
								this);
						monitor.done();
					} else {
						downloadFile(f, target, monitor, false, this);
					}
				} else {
					long total = 0;
					for (AgentFile f : files) {
						if (f.isDirectory() && (f.getSize() < 0)) {
							try {
								total += session.getAgentFileInfo(f).getSize();
							} catch (Exception e) {
							}
						} else {
							total += f.getSize();
						}
					}

					monitor.beginTask("Downloading files", (int) total);
					for (AgentFile f : files) {
						if (f.isDirectory()) {
							downloadDir(f, target + "/" + f.getName(), monitor,
									this);
						} else {
							downloadFile(f, target + "/" + f.getName(),
									monitor, true, this);
						}
					}
					monitor.done();
				}
			}

			@Override
			protected String getErrorMessage() {
				return Messages.get().AgentFileManager_DirectoryReadError;
			}
		};
		job.start();
	}

	/**
	 * Recursively download directory from agent to local pc
	 * 
	 * @param sf
	 * @param localFileName
	 * @param job
	 * @throws IOException
	 * @throws NXCException
	 */
	private void downloadDir(final AgentFile sf, String localFileName,
			final IProgressMonitor monitor, ConsoleJobCallingServerJob job)
			throws NXCException, IOException {
		File dir = new File(localFileName);
		dir.mkdir();
		List<AgentFile> files = sf.getChildren();
		if (files == null) {
			files = session
					.listAgentFiles(sf, sf.getFullName(), sf.getNodeId());
			sf.setChildren(files);
		}
		for (AgentFile f : files) {
			if (job.isCanceled())
				break;
			if (f.isDirectory()) {
				downloadDir(f, localFileName + "/" + f.getName(), monitor, job); //$NON-NLS-1$
			} else {
				downloadFile(f,
						localFileName + "/" + f.getName(), monitor, true, job); //$NON-NLS-1$
			}
		}
		dir.setLastModified(sf.getModifyicationTime().getTime());
	}

	/**
	 * Downloads file
	 * 
	 * @throws NXCException
	 * @throws IOException
	 */
	private void downloadFile(final AgentFile sf, final String localName,
			final IProgressMonitor monitor, final boolean subTask,
			ConsoleJobCallingServerJob job) throws IOException, NXCException {
		if (subTask)
			monitor.subTask(String.format("Downloading file %s",
					sf.getFullName()));
		final AgentFileData file = session.downloadFileFromAgent(objectId,
				sf.getFullName(), 0, false, new ProgressListener() {
					@Override
					public void setTotalWorkAmount(long workTotal) {
						if (!subTask)
							monitor.beginTask(
									String.format("Downloading file %s",
											sf.getFullName()), (int) workTotal);
					}

					@Override
					public void markProgress(long workDone) {
						monitor.worked((int) workDone);
					}
				}, job);
		if (file.getFile() != null) {
			File outputFile = new File(localName);
			outputFile.createNewFile();
			InputStream in = new FileInputStream(file.getFile());
			OutputStream out = new FileOutputStream(outputFile);
			byte[] buf = new byte[1024];
			int len;
			while ((len = in.read(buf)) > 0) {
				out.write(buf, 0, len);
			}
			in.close();
			out.close();
			outputFile.setLastModified(sf.getModifyicationTime().getTime());
		}
	}

	/**
	 * Rename selected file
	 */
	private void renameFile() {
		IStructuredSelection selection = (IStructuredSelection) viewer
				.getSelection();
		if (selection.size() != 1)
			return;

		viewer.editElement(selection.getFirstElement(), 0);
	}

	/**
	 * Move selected file
	 */
	private void moveFile(final AgentFile target, final AgentFile object) {
		new ConsoleJob(Messages.get().AgentFileManager_MoveFile, this,
				Activator.PLUGIN_ID, Activator.PLUGIN_ID) {
			@Override
			protected String getErrorMessage() {
				return Messages.get().AgentFileManager_MoveError;
			}

			@Override
			protected void runInternal(IProgressMonitor monitor)
					throws Exception {
				NestedVerifyOverwrite verify = new NestedVerifyOverwrite(
						object.getType(), object.getName(), true, true, false) {

					@Override
					public void executeAction() throws NXCException,
							IOException {
						session.renameAgentFile(
								objectId,
								object.getFullName(),
								target.getFullName() + "/" + object.getName(), false); //$NON-NLS-1$
					}

					@Override
					public void executeSameFunctionWithOverwrite()
							throws IOException, NXCException {
						session.renameAgentFile(
								objectId,
								object.getFullName(),
								target.getFullName() + "/" + object.getName(), true); //$NON-NLS-1$                  
					}
				};
				verify.run(viewer.getControl().getDisplay());

				if (verify.isOkPressed()) {
					runInUIThread(new Runnable() {
						@Override
						public void run() {
							object.getParent().removeChield(object);
							viewer.refresh(object.getParent(), true);
							object.setParent(target);
							target.addChield(object);
							viewer.refresh(object.getParent(), true);
						}
					});
				}
			}
		}.start();
	}

	/**
	 * Create new folder
	 */
	private void createFolder() {
		IStructuredSelection selection = (IStructuredSelection) viewer
				.getSelection();
		if (selection.isEmpty())
			return;

		final Object[] objects = selection.toArray();
		final AgentFile parentFolder = ((AgentFile) objects[0]).isDirectory() ? ((AgentFile) objects[0])
				: ((AgentFile) objects[0]).getParent();

		final CreateFolderDialog dlg = new CreateFolderDialog(getSite()
				.getShell());
		if (dlg.open() != Window.OK)
			return;

		final String newFolder = dlg.getNewName();

		new ConsoleJob(Messages.get().AgentFileManager_CreatingFolder, this,
				Activator.PLUGIN_ID, Activator.PLUGIN_ID) {
			@Override
			protected String getErrorMessage() {
				return Messages.get().AgentFileManager_FolderCreationError;
			}

			@Override
			protected void runInternal(IProgressMonitor monitor)
					throws Exception {
				NestedVerifyOverwrite verify = new NestedVerifyOverwrite(
						AgentFile.DIRECTORY, newFolder, true, true, false) {

					@Override
					public void executeAction() throws NXCException,
							IOException {
						session.createFolderOnAgent(objectId,
								parentFolder.getFullName() + "/" + newFolder); //$NON-NLS-1$
					}

					@Override
					public void executeSameFunctionWithOverwrite()
							throws IOException, NXCException {
						// do nothing
					}
				};
				verify.run(viewer.getControl().getDisplay());
				parentFolder.setChildren(session.listAgentFiles(parentFolder,
						parentFolder.getFullName(), objectId));

				runInUIThread(new Runnable() {
					@Override
					public void run() {
						viewer.refresh(parentFolder, true);
					}
				});
			}
		}.start();
	}

	/**
	 * Show file size
	 */
	private void calculateFolderSize() {
		IStructuredSelection selection = (IStructuredSelection) viewer
				.getSelection();
		if (selection.isEmpty())
			return;

		final List<AgentFile> files = new ArrayList<AgentFile>(selection.size());
		for (Object o : selection.toList())
			files.add((AgentFile) o);

		new ConsoleJob("Calculate folder size", this, Activator.PLUGIN_ID, null) {
			@Override
			protected void runInternal(IProgressMonitor monitor)
					throws Exception {
				for (AgentFile f : files) {
					f.setFileInfo(session.getAgentFileInfo(f));
				}
				runInUIThread(new Runnable() {
					@Override
					public void run() {
						viewer.update(files.toArray(), null);
					}
				});
			}

			@Override
			protected String getErrorMessage() {
				return "Cannot calculate folder size";
			}
		}.start();
	}

	/**
	 * Copy name of file to clipboard
	 */
	private void copyFileName() {
		IStructuredSelection selection = (IStructuredSelection) viewer
				.getSelection();
		if (selection.size() != 1)
			return;

		WidgetHelper.copyToClipboard(((AgentFile) selection.getFirstElement())
				.getName());
	}

	/**
	 * Copy full path to file to clipboard
	 */
	private void copyFilePath() {
		IStructuredSelection selection = (IStructuredSelection) viewer
				.getSelection();
		if (selection.size() != 1)
			return;

		WidgetHelper.copyToClipboard(((AgentFile) selection.getFirstElement())
				.getFilePath());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
	 */
	@Override
	public void setFocus() {
		viewer.getControl().setFocus();
	}

	/**
	 * Enable or disable filter
	 * 
	 * @param enable
	 *            New filter state
	 */
	private void enableFilter(boolean enable) {
		initShowFilter = enable;
		filterText.setVisible(initShowFilter);
		FormData fd = (FormData) viewer.getTree().getLayoutData();
		fd.top = enable ? new FormAttachment(filterText) : new FormAttachment(
				0, 0);
		content.layout();
		if (enable) {
			filterText.setFocus();
		} else {
			filterText.setText(""); //$NON-NLS-1$
			onFilterModify();
		}
	}

	/**
	 * Handler for filter modification
	 */
	private void onFilterModify() {
		final String text = filterText.getText();
		filter.setFilterString(text);
		viewer.refresh(false);
	}

	/**
	 * Nested class that check if file already exist and it should be
	 * overwritten
	 * 
	 */
	abstract class NestedVerifyOverwrite {
		private boolean okPresseed;
		private int type;
		private String name;
		private boolean askFolderOverwrite;
		private boolean askFileOverwrite;
		private boolean overwrite;

		public NestedVerifyOverwrite(int fileType, String fileName,
				boolean askFolderOverwrite, boolean askFileOverwrite,
				boolean overwrite) {
			type = fileType;
			name = fileName;
			this.askFolderOverwrite = askFolderOverwrite;
			this.askFileOverwrite = askFileOverwrite;
			this.overwrite = overwrite;
			okPresseed = true;
		}

		public boolean askFolderOverwrite() {
			return askFolderOverwrite;
		}

		public boolean askFileOverwrite() {
			return askFileOverwrite;
		}

		public void run(Display disp) throws IOException, NXCException {
			try {
				executeAction();
			} catch (final NXCException e) {
				if (e.getErrorCode() == RCC.FOLDER_ALREADY_EXIST
						|| type == AgentFile.DIRECTORY) {
					if (askFolderOverwrite) {
						disp.syncExec(new Runnable() {
							@Override
							public void run() {
								DialogData data = MessageDialogHelper.openOneButtonWarningWithCheckbox(
										getSite().getShell(),
										String.format(
												"%s already exist",
												e.getErrorCode() == RCC.FOLDER_ALREADY_EXIST ? "Folder"
														: "File"),
										"Do not show again for this upload",
										String.format(
												"%s %s already exist",
												e.getErrorCode() == RCC.FOLDER_ALREADY_EXIST ? "Folder"
														: "File", name));
								askFolderOverwrite = !data.getSaveSelection();
								okPresseed = false;
							}
						});
					}
				} else if (e.getErrorCode() == RCC.FILE_ALREADY_EXISTS
						|| e.getErrorCode() == RCC.FOLDER_ALREADY_EXIST) {
					if (askFileOverwrite) {
						disp.syncExec(new Runnable() {
							@Override
							public void run() {

								DialogData data = MessageDialogHelper.openWarningWithCheckbox(
										getSite().getShell(),
										String.format(
												"%s overwrite confirmation",
												type == AgentFile.DIRECTORY ? "Folder"
														: "File"),
										"Save chose for current upload files",
										String.format(
												"%s with %s name already exists. Are you sure you want to overwrite it?",
												e.getErrorCode() == RCC.FOLDER_ALREADY_EXIST ? "Folder"
														: "File", name));
								askFileOverwrite = !data.getSaveSelection();
								okPresseed = data.isOkPressed();
							}
						});
						if (okPresseed)
							executeSameFunctionWithOverwrite();
					} else {
						if (overwrite) {
							executeSameFunctionWithOverwrite();
						}
					}
				} else
					throw e;
			}
		}

		public boolean isOkPressed() {
			return okPresseed;
		}

		public abstract void executeAction() throws NXCException, IOException;

		public abstract void executeSameFunctionWithOverwrite()
				throws NXCException, IOException;
	}
}
