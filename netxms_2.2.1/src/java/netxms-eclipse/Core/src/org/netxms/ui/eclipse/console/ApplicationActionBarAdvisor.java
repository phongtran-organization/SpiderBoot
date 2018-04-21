/**
 * NetXMS - open source network management system
 * Copyright (C) 2003-2014 Victor Kirhenshtein
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
package org.netxms.ui.eclipse.console;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.ICoolBarManager;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.ToolBarContributionItem;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.service.datalocation.Location;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchCommandConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.XMLMemento;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.eclipse.ui.actions.ContributionItemFactory;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;
import org.eclipse.ui.internal.Perspective;
import org.eclipse.ui.internal.WorkbenchPage;
import org.eclipse.ui.internal.actions.CommandAction;
import org.eclipse.ui.internal.dialogs.SelectPerspectiveDialog;
import org.eclipse.ui.internal.registry.PerspectiveDescriptor;
import org.eclipse.ui.internal.registry.PerspectiveRegistry;
import org.eclipse.ui.internal.tweaklets.Tweaklets;
import org.eclipse.ui.internal.tweaklets.WorkbenchImplementation;
import org.netxms.base.BuildNumber;
import org.netxms.base.NXCommon;
import org.netxms.ui.eclipse.console.resources.GroupMarkers;
import org.netxms.ui.eclipse.jobs.ConsoleJob;
import org.netxms.ui.eclipse.shared.ConsoleSharedData;
import org.netxms.ui.eclipse.tools.MessageDialogHelper;

/**
 * Action bar advisor for management console
 */
@SuppressWarnings("restriction")
public class ApplicationActionBarAdvisor extends ActionBarAdvisor {
	private IWorkbenchAction actionExit;
	private IWorkbenchAction actionAbout;
	private Action actionAboutCustom;
	private IWorkbenchAction actionShowPreferences;
	private IWorkbenchAction actionCustomizePerspective;
	private IWorkbenchAction actionSavePerspective;
	private IWorkbenchAction actionResetPerspective;
	private IWorkbenchAction actionClosePerspective;
	private IWorkbenchAction actionCloseAllPerspectives;
	private Action actionExportPerspective;
	private Action actionImportPerspective;
	private IWorkbenchAction actionMinimize;
	private IWorkbenchAction actionMaximize;
	private Action actionClose;
	private IWorkbenchAction actionPrevView;
	private IWorkbenchAction actionNextView;
	private IWorkbenchAction actionQuickAccess;
	private IWorkbenchAction actionShowViewMenu;
	private Action actionOpenProgressView;
	private Action actionFullScreen;
	private Action actionLangArabic;
	private Action actionLangChinese;
	private Action actionLangCzech;
	private Action actionLangEnglish;
	private Action actionLangFrench;
	private Action actionLangGerman;
	private Action actionLangPortuguese;
	private Action actionLangRussian;
	private Action actionLangSpanish;
	private IContributionItem contribItemShowView;
	private IContributionItem contribItemOpenPerspective;

	/**
	 * @param configurer
	 */
	public ApplicationActionBarAdvisor(IActionBarConfigurer configurer) {
		super(configurer);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.application.ActionBarAdvisor#makeActions(org.eclipse.ui
	 * .IWorkbenchWindow)
	 */
	@Override
	protected void makeActions(final IWorkbenchWindow window) {
		contribItemShowView = ContributionItemFactory.VIEWS_SHORTLIST
				.create(window);
		contribItemOpenPerspective = ContributionItemFactory.PERSPECTIVES_SHORTLIST
				.create(window);

		actionExit = ActionFactory.QUIT.create(window);
		register(actionExit);

		actionAbout = ActionFactory.ABOUT.create(window);
		register(actionAbout);

		actionAboutCustom = new Action(
				Messages.get().ApplicationActionBarAdvisor_About) {
			@Override
			public void run() {
				Dialog dlg = BrandingManager.getInstance().getAboutDialog(
						window.getShell());
				if (dlg != null) {
					dlg.open();
				} else {
					MessageDialogHelper
							.openInformation(
									window.getShell(),
									Messages.get().ApplicationActionBarAdvisor_AboutTitle,
									String.format(
											Messages.get().ApplicationActionBarAdvisor_AboutText,
											NXCommon.VERSION
													+ " (" + BuildNumber.TEXT + ")")); //$NON-NLS-1$ //$NON-NLS-2$
				}
			}
		};

		actionShowPreferences = ActionFactory.PREFERENCES.create(window);
		register(actionShowPreferences);

		actionCustomizePerspective = ActionFactory.EDIT_ACTION_SETS
				.create(window);
		register(actionCustomizePerspective);

		actionSavePerspective = ActionFactory.SAVE_PERSPECTIVE.create(window);
		register(actionSavePerspective);

		actionResetPerspective = ActionFactory.RESET_PERSPECTIVE.create(window);
		register(actionResetPerspective);

		actionClosePerspective = ActionFactory.CLOSE_PERSPECTIVE.create(window);
		register(actionClosePerspective);

		actionCloseAllPerspectives = ActionFactory.CLOSE_ALL_PERSPECTIVES
				.create(window);
		register(actionCloseAllPerspectives);

		actionMinimize = ActionFactory.MINIMIZE.create(window);
		register(actionMinimize);

		actionMaximize = ActionFactory.MAXIMIZE.create(window);
		register(actionMaximize);

		actionClose = new CommandAction(window,
				IWorkbenchCommandConstants.WINDOW_CLOSE_PART);
		register(actionClose);

		actionPrevView = ActionFactory.PREVIOUS_PART.create(window);
		register(actionPrevView);

		actionNextView = ActionFactory.NEXT_PART.create(window);
		register(actionNextView);

		actionQuickAccess = ActionFactory.SHOW_QUICK_ACCESS.create(window);
		register(actionQuickAccess);

		actionShowViewMenu = ActionFactory.SHOW_VIEW_MENU.create(window);
		register(actionShowViewMenu);

		actionOpenProgressView = new Action() {
			@Override
			public void run() {
				IWorkbench wb = PlatformUI.getWorkbench();
				if (wb != null) {
					IWorkbenchWindow win = wb.getActiveWorkbenchWindow();
					if (win != null) {
						IWorkbenchPage page = win.getActivePage();
						if (page != null) {
							try {
								page.showView("org.eclipse.ui.views.ProgressView"); //$NON-NLS-1$
							} catch (PartInitException e) {
								e.printStackTrace();
							}
						}
					}
				}
			}
		};
		actionOpenProgressView
				.setText(Messages.get().ApplicationActionBarAdvisor_Progress);
		actionOpenProgressView.setImageDescriptor(Activator
				.getImageDescriptor("icons/pview.gif")); //$NON-NLS-1$

		actionFullScreen = new Action(
				Messages.get().ApplicationActionBarAdvisor_FullScreen,
				Action.AS_CHECK_BOX) { //$NON-NLS-1$
			@Override
			public void run() {
				boolean fullScreen = !PlatformUI.getWorkbench()
						.getActiveWorkbenchWindow().getShell().getFullScreen();
				PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell()
						.setFullScreen(fullScreen);
				actionFullScreen.setChecked(fullScreen);
			}
		};
		actionFullScreen.setChecked(false);
		actionFullScreen
				.setId("org.netxms.ui.eclipse.console.actions.full_screen"); //$NON-NLS-1$
		actionFullScreen
				.setActionDefinitionId("org.netxms.ui.eclipse.console.commands.full_screen"); //$NON-NLS-1$
		getActionBarConfigurer().registerGlobalAction(actionFullScreen);
		ConsoleSharedData.setProperty("FullScreenAction", actionFullScreen); //$NON-NLS-1$

		actionExportPerspective = new Action(
				Messages.get().ApplicationActionBarAdvisor_ActionExportPerspective) {
			@Override
			public void run() {
				exportPerspective(window);
			}
		};

		actionImportPerspective = new Action(
				Messages.get().ApplicationActionBarAdvisor_ActionImportPerspective) {
			@Override
			public void run() {
				importPerspective(window);
			}
		};

		actionLangArabic = new Action(
				"&Arabic", Activator.getImageDescriptor("icons/lang/ar.png")) { //$NON-NLS-1$ //$NON-NLS-2$
			public void run() {
				setLanguage("ar"); //$NON-NLS-1$
			}
		};
		actionLangChinese = new Action(
				"C&hinese", Activator.getImageDescriptor("icons/lang/zh.png")) { //$NON-NLS-1$ //$NON-NLS-2$
			public void run() {
				setLanguage("zh"); //$NON-NLS-1$
			}
		};
		actionLangCzech = new Action(
				"&Czech", Activator.getImageDescriptor("icons/lang/cs.png")) { //$NON-NLS-1$ //$NON-NLS-2$
			public void run() {
				setLanguage("cs"); //$NON-NLS-1$
			}
		};
		actionLangEnglish = new Action(
				"&English", Activator.getImageDescriptor("icons/lang/gb.png")) { //$NON-NLS-1$ //$NON-NLS-2$
			public void run() {
				setLanguage("en"); //$NON-NLS-1$
			}
		};
		actionLangFrench = new Action(
				"&French", Activator.getImageDescriptor("icons/lang/fr.png")) { //$NON-NLS-1$ //$NON-NLS-2$
			public void run() {
				setLanguage("fr"); //$NON-NLS-1$
			}
		};
		actionLangGerman = new Action(
				"&German", Activator.getImageDescriptor("icons/lang/de.png")) { //$NON-NLS-1$ //$NON-NLS-2$
			public void run() {
				setLanguage("de"); //$NON-NLS-1$
			}
		};
		actionLangPortuguese = new Action(
				"&Portuguese", Activator.getImageDescriptor("icons/lang/pt.png")) { //$NON-NLS-1$ //$NON-NLS-2$
			public void run() {
				setLanguage("pt"); //$NON-NLS-1$
			}
		};
		actionLangRussian = new Action(
				"&Russian", Activator.getImageDescriptor("icons/lang/ru.png")) { //$NON-NLS-1$ //$NON-NLS-2$
			public void run() {
				setLanguage("ru"); //$NON-NLS-1$
			}
		};
		actionLangSpanish = new Action(
				"&Spanish", Activator.getImageDescriptor("icons/lang/es.png")) { //$NON-NLS-1$ //$NON-NLS-2$
			public void run() {
				setLanguage("es"); //$NON-NLS-1$
			}
		};
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.application.ActionBarAdvisor#fillMenuBar(org.eclipse.jface
	 * .action.IMenuManager)
	 */
	@Override
	protected void fillMenuBar(IMenuManager menuBar) {
		MenuManager fileMenu = new MenuManager(
				Messages.get().ApplicationActionBarAdvisor_File,
				IWorkbenchActionConstants.M_FILE);
		MenuManager viewMenu = new MenuManager(
				Messages.get().ApplicationActionBarAdvisor_View,
				GroupMarkers.M_VIEW);
		MenuManager monitorMenu = new MenuManager(
				Messages.get().ApplicationActionBarAdvisor_Monitor,
				GroupMarkers.M_MONITOR);
		MenuManager configMenu = new MenuManager(
				Messages.get().ApplicationActionBarAdvisor_Configuration,
				GroupMarkers.M_CONFIG);
		MenuManager toolsMenu = new MenuManager(
				Messages.get().ApplicationActionBarAdvisor_Tools,
				GroupMarkers.M_TOOLS);
		MenuManager windowMenu = new MenuManager(
				Messages.get().ApplicationActionBarAdvisor_Window,
				IWorkbenchActionConstants.M_WINDOW);
		MenuManager helpMenu = new MenuManager(
				Messages.get().ApplicationActionBarAdvisor_Help,
				IWorkbenchActionConstants.M_HELP);

		menuBar.add(fileMenu);
		menuBar.add(viewMenu);
		menuBar.add(monitorMenu);
		menuBar.add(configMenu);
		menuBar.add(toolsMenu);

		// Add a group marker indicating where action set menus will appear.
		menuBar.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
		if (!Activator.getDefault().getPreferenceStore()
				.getBoolean("HIDE_WINDOW_MENU")) //$NON-NLS-1$
			menuBar.add(windowMenu);
		menuBar.add(helpMenu);

		// Language selection (intentionally left in English only)
		final MenuManager langMenu = new MenuManager("&Language"); //$NON-NLS-1$
		langMenu.add(actionLangArabic);
		langMenu.add(actionLangChinese);
		langMenu.add(actionLangCzech);
		langMenu.add(actionLangEnglish);
		langMenu.add(actionLangFrench);
		langMenu.add(actionLangGerman);
		langMenu.add(actionLangPortuguese);
		langMenu.add(actionLangRussian);
		langMenu.add(actionLangSpanish);

		// File
		fileMenu.add(actionShowPreferences);
		fileMenu.add(langMenu);
		fileMenu.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
		fileMenu.add(new Separator());
		fileMenu.add(actionExit);

		// View
		viewMenu.add(new GroupMarker(GroupMarkers.M_PRODUCT_VIEW));
		viewMenu.add(new Separator());
		viewMenu.add(new GroupMarker(GroupMarkers.M_PRIMARY_VIEW));
		viewMenu.add(new Separator());
		viewMenu.add(new GroupMarker(GroupMarkers.M_LOGS_VIEW));
		viewMenu.add(new Separator());
		viewMenu.add(actionOpenProgressView);
		viewMenu.add(new GroupMarker(GroupMarkers.M_TOOL_VIEW));

		// Monitor
		monitorMenu
				.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));

		// Tools
		toolsMenu.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));

		// Window
		MenuManager openPerspectiveMenuMgr = new MenuManager(
				Messages.get().ApplicationActionBarAdvisor_OpenPerspective,
				"openPerspective"); //$NON-NLS-1$
		openPerspectiveMenuMgr.add(contribItemOpenPerspective);
		windowMenu.add(openPerspectiveMenuMgr);

		final MenuManager showViewMenuMgr = new MenuManager(
				Messages.get().ApplicationActionBarAdvisor_ShowView, "showView"); //$NON-NLS-1$
		showViewMenuMgr.add(contribItemShowView);
		windowMenu.add(showViewMenuMgr);

		windowMenu.add(new Separator());
		windowMenu.add(actionCustomizePerspective);
		windowMenu.add(actionSavePerspective);
		windowMenu.add(actionResetPerspective);
		windowMenu.add(actionClosePerspective);
		windowMenu.add(actionCloseAllPerspectives);
		windowMenu.add(new Separator());
		windowMenu.add(actionExportPerspective);
		windowMenu.add(actionImportPerspective);
		windowMenu.add(new Separator());

		final MenuManager navMenu = new MenuManager(
				Messages.get().ApplicationActionBarAdvisor_Navigation,
				IWorkbenchActionConstants.M_NAVIGATE);
		windowMenu.add(navMenu);
		navMenu.add(actionQuickAccess);
		navMenu.add(actionShowViewMenu);
		navMenu.add(new Separator());
		navMenu.add(actionMaximize);
		navMenu.add(actionMinimize);
		navMenu.add(actionClose);
		navMenu.add(new Separator());
		navMenu.add(actionNextView);
		navMenu.add(actionPrevView);

		// Help
		helpMenu.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
		helpMenu.add((BrandingManager.getInstance().getAboutDialog(null) != null) ? actionAboutCustom
				: actionAbout);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.application.ActionBarAdvisor#fillCoolBar(org.eclipse.jface
	 * .action.ICoolBarManager)
	 */
	@Override
	protected void fillCoolBar(ICoolBarManager coolBar) {
		IToolBarManager toolbar = new ToolBarManager(SWT.FLAT | SWT.TRAIL);
		toolbar.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
		coolBar.add(new ToolBarContributionItem(toolbar, "product")); //$NON-NLS-1$

		toolbar = new ToolBarManager(SWT.FLAT | SWT.TRAIL);
		toolbar.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
		coolBar.add(new ToolBarContributionItem(toolbar, "view")); //$NON-NLS-1$

		toolbar = new ToolBarManager(SWT.FLAT | SWT.TRAIL);
		toolbar.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
		coolBar.add(new ToolBarContributionItem(toolbar, "logs")); //$NON-NLS-1$

		toolbar = new ToolBarManager(SWT.FLAT | SWT.TRAIL);
		toolbar.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
		coolBar.add(new ToolBarContributionItem(toolbar, "tools")); //$NON-NLS-1$

		toolbar = new ToolBarManager(SWT.FLAT | SWT.TRAIL);
		toolbar.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
		coolBar.add(new ToolBarContributionItem(toolbar, "config")); //$NON-NLS-1$

		if (Activator.getDefault().getPreferenceStore()
				.getBoolean("SHOW_SERVER_CLOCK")) //$NON-NLS-1$
		{
			coolBar.add(new ServerClockContributionItem());
		}
		ConsoleSharedData.setProperty("CoolBarManager", coolBar); //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.application.ActionBarAdvisor#fillStatusLine(org.eclipse
	 * .jface.action.IStatusLineManager)
	 */
	@Override
	protected void fillStatusLine(IStatusLineManager statusLine) {
		Activator.getDefault().setStatusLine(statusLine);

		StatusLineContributionItem statusItem = new StatusLineContributionItem(
				"ConnectionStatus", StatusLineContributionItem.CALC_TRUE_WIDTH); //$NON-NLS-1$
		statusItem.setText(""); //$NON-NLS-1$
		statusLine.add(statusItem);

		statusLine.add(new ServerNameStatusLineItem("ServerName")); //$NON-NLS-1$
	}

	/**
	 * Set program language
	 * 
	 * @param locale
	 */
	private void setLanguage(String locale) {
		if (!MessageDialogHelper
				.openConfirm(
						null,
						Messages.get().ApplicationActionBarAdvisor_ConfirmRestart,
						Messages.get().ApplicationActionBarAdvisor_RestartConsoleMessage))
			return;

		Activator.getDefault().getPreferenceStore().setValue("NL", locale); //$NON-NLS-1$

		// Patch product's .ini file
		final Location configArea = Platform.getInstallLocation();
		if (configArea != null) {
			BufferedReader in = null;
			BufferedWriter out = null;

			try {
				final URL iniFileUrl = new URL(configArea.getURL()
						.toExternalForm() + "nxmc.ini"); //$NON-NLS-1$
				final String iniFileName = iniFileUrl.getFile();
				final File iniFile = new File(iniFileName);

				final File iniFileBackup = new File(iniFileName + ".bak"); //$NON-NLS-1$
				iniFileBackup.delete();
				iniFile.renameTo(iniFileBackup); //$NON-NLS-1$

				in = new BufferedReader(new FileReader(iniFileName + ".bak")); //$NON-NLS-1$
				out = new BufferedWriter(new FileWriter(iniFileName));

				int state = 0;
				while (true) {
					String line = in.readLine();
					if (line == null)
						break;

					switch (state) {
					case 0:
						if (line.equals("-nl")) //$NON-NLS-1$
							state = 1;
						break;
					case 1: // -nl argument
						line = locale;
						state = 0;
						break;
					}
					out.write(line);
					out.newLine();
				}
			} catch (Exception e) {
				Activator
						.getDefault()
						.getLog()
						.log(new Status(Status.ERROR, Activator.PLUGIN_ID,
								Status.OK, "Exception in setLanguage()", e)); //$NON-NLS-1$
			} finally {
				try {
					if (in != null)
						in.close();
					if (out != null)
						out.close();
				} catch (IOException e) {
				}
			}
		}
		System.getProperties().setProperty("eclipse.exitdata", "-nl " + locale); //$NON-NLS-1$ //$NON-NLS-2$
		PlatformUI.getWorkbench().restart();
	}

	/**
	 * Export perspective
	 * 
	 * @param window
	 */
	private void exportPerspective(IWorkbenchWindow window) {
		try {
			SelectPerspectiveDialog dlg = new SelectPerspectiveDialog(
					window.getShell(), window.getWorkbench()
							.getPerspectiveRegistry());
			if (dlg.open() == Window.OK) {
				WorkbenchPage page = (WorkbenchPage) window.getActivePage();
				Perspective p = page.findPerspective(dlg.getSelection());
				final XMLMemento memento = XMLMemento
						.createWriteRoot("perspective"); //$NON-NLS-1$
				p.saveState(memento);

				FileDialog fd = new FileDialog(window.getShell());
				fd.setFilterExtensions(new String[] { "*.xml", "*.*" }); //$NON-NLS-1$ //$NON-NLS-2$
				fd.setFilterNames(new String[] {
						Messages.get().ApplicationActionBarAdvisor_XMLFiles,
						Messages.get().ApplicationActionBarAdvisor_AllFiles });
				fd.setOverwrite(true);
				fd.setText(Messages.get().ApplicationActionBarAdvisor_ExportPerspective);
				final String fileName = fd.open();
				if (fileName != null) {
					new ConsoleJob(
							Messages.get().ApplicationActionBarAdvisor_ExportPerspective,
							null, Activator.PLUGIN_ID, null) {
						@Override
						protected void runInternal(IProgressMonitor monitor)
								throws Exception {
							FileWriter writer = null;
							try {
								writer = new FileWriter(fileName);
								memento.save(writer);
							} finally {
								if (writer != null)
									writer.close();
							}
						}

						@Override
						protected String getErrorMessage() {
							return Messages.get().ApplicationActionBarAdvisor_PerspectiveExportFailed;
						}
					}.start();
				}
			}
		} catch (Exception e) {
			Activator.logError("Exception in exportPerspective", e); //$NON-NLS-1$
			MessageDialogHelper
					.openError(
							window.getShell(),
							Messages.get().ApplicationActionBarAdvisor_Error,
							Messages.get().ApplicationActionBarAdvisor_PerspectiveExportFailed);
		}
	}

	/**
	 * Import perspective
	 * 
	 * @param window
	 */
	private void importPerspective(final IWorkbenchWindow window) {
		FileDialog fd = new FileDialog(window.getShell());
		fd.setFilterExtensions(new String[] { "*.xml", "*.*" }); //$NON-NLS-1$ //$NON-NLS-2$
		fd.setFilterNames(new String[] {
				Messages.get().ApplicationActionBarAdvisor_XMLFiles,
				Messages.get().ApplicationActionBarAdvisor_AllFiles });
		fd.setText(Messages.get().ApplicationActionBarAdvisor_ImportPerspective);
		final String fileName = fd.open();
		if (fileName == null)
			return;

		if (!MessageDialogHelper
				.openConfirm(
						window.getShell(),
						Messages.get().ApplicationActionBarAdvisor_ConfirmRestart,
						Messages.get().ApplicationActionBarAdvisor_RestartConfirmationMessage))
			return;

		new ConsoleJob(
				Messages.get().ApplicationActionBarAdvisor_ImportPerspective,
				null, Activator.PLUGIN_ID, null) {
			@Override
			protected void runInternal(IProgressMonitor monitor)
					throws Exception {
				FileReader reader = null;
				try {
					reader = new FileReader(fileName);
					final XMLMemento memento = XMLMemento
							.createReadRoot(reader);
					runInUIThread(new Runnable() {
						@Override
						public void run() {
							try {
								Perspective p = ((WorkbenchImplementation) Tweaklets
										.get(WorkbenchImplementation.KEY))
										.createPerspective(null,
												(WorkbenchPage) window
														.getActivePage());
								p.restoreState(memento);

								PerspectiveRegistry reg = (PerspectiveRegistry) window
										.getWorkbench()
										.getPerspectiveRegistry();
								PerspectiveDescriptor pd = reg
										.createPerspective(p.getDesc()
												.getLabel(),
												(PerspectiveDescriptor) p
														.getDesc());

								WorkbenchPage page = (WorkbenchPage) window
										.getActivePage();
								page.savePerspectiveAs(pd);
								p = page.findPerspective(pd);
								p.restoreState(memento);
								p.restoreState();
								window.getWorkbench().showPerspective(
										pd.getId(), window);
								page.savePerspective();

								PlatformUI.getWorkbench().restart();
							} catch (Exception e) {
								Activator.logError(
										"Exception in importPerspective", e); //$NON-NLS-1$
								MessageDialogHelper
										.openError(
												window.getShell(),
												Messages.get().ApplicationActionBarAdvisor_Error,
												Messages.get().ApplicationActionBarAdvisor_PerspectiveImportFailed);
							}
						}
					});
				} finally {
					if (reader != null)
						reader.close();
				}
			}

			@Override
			protected String getErrorMessage() {
				return Messages.get().ApplicationActionBarAdvisor_PerspectiveImportFailed;
			}
		}.start();
	}
}
