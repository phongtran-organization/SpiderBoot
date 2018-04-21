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
package org.netxms.ui.eclipse.networkmaps.views;

import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.commands.ActionHandler;
import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.ViewerDropAdapter;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.internal.dialogs.PropertyDialog;
import org.netxms.base.NXCommon;
import org.netxms.client.NXCObjectModificationData;
import org.netxms.client.constants.UserAccessRights;
import org.netxms.client.maps.MapLayoutAlgorithm;
import org.netxms.client.maps.NetworkMapLink;
import org.netxms.client.maps.elements.NetworkMapDCIContainer;
import org.netxms.client.maps.elements.NetworkMapDCIImage;
import org.netxms.client.maps.elements.NetworkMapDecoration;
import org.netxms.client.maps.elements.NetworkMapElement;
import org.netxms.client.maps.elements.NetworkMapObject;
import org.netxms.client.maps.elements.NetworkMapTextBox;
import org.netxms.client.objects.AbstractObject;
import org.netxms.client.objects.NetworkMap;
import org.netxms.ui.eclipse.console.resources.SharedIcons;
import org.netxms.ui.eclipse.imagelibrary.dialogs.ImageSelectionDialog;
import org.netxms.ui.eclipse.imagelibrary.shared.ImageProvider;
import org.netxms.ui.eclipse.imagelibrary.shared.ImageUpdateListener;
import org.netxms.ui.eclipse.jobs.ConsoleJob;
import org.netxms.ui.eclipse.networkmaps.Activator;
import org.netxms.ui.eclipse.networkmaps.Messages;
import org.netxms.ui.eclipse.networkmaps.dialogs.AddGroupBoxDialog;
import org.netxms.ui.eclipse.networkmaps.views.helpers.LinkEditor;
import org.netxms.ui.eclipse.objectbrowser.dialogs.ObjectSelectionDialog;
import org.netxms.ui.eclipse.tools.ColorConverter;
import org.netxms.ui.eclipse.tools.MessageDialogHelper;

/**
 * View for predefined map
 */
@SuppressWarnings("restriction")
public class PredefinedMap extends AbstractNetworkMapView implements
		ImageUpdateListener {
	public static final String ID = "org.netxms.ui.eclipse.networkmaps.views.PredefinedMap"; //$NON-NLS-1$

	private NetworkMap mapObject;
	private Action actionAddObject;
	private Action actionAddDCIContainer;
	private Action actionLinkObjects;
	private Action actionAddGroupBox;
	private Action actionAddImage;
	private Action actionRemove;
	private Action actionDCIContainerProperties;
	private Action actionDCIImageProperties;
	private Action actionMapProperties;
	private Action actionLinkProperties;
	private Action actionAddDCIImage;
	private Action actionAddTextBox;
	private Action actionTextBoxProperties;
	private Color defaultLinkColor = null;
	private boolean readOnly;

	/**
	 * Create predefined map view
	 */
	public PredefinedMap() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.netxms.ui.eclipse.networkmaps.views.NetworkMap#init(org.eclipse.ui
	 * .IViewSite)
	 */
	@Override
	public void init(IViewSite site) throws PartInitException {
		super.init(site);
		mapObject = (NetworkMap) rootObject;
		setPartName(rootObject.getObjectName());

		ConsoleJob job = new ConsoleJob("Get map effective rights", this,
				Activator.PLUGIN_ID, null) {
			@Override
			protected void runInternal(IProgressMonitor monitor)
					throws Exception {
				readOnly = ((mapObject.getEffectiveRights() & UserAccessRights.OBJECT_ACCESS_MODIFY) == 0);
			}

			@Override
			protected String getErrorMessage() {
				return "Cannot get effective rights for map";
			}
		};
		job.start();

		// FIXME: rewrite waiting
		try {
			job.join();
		} catch (InterruptedException e) {
		}

		allowManualLayout = !readOnly;
		if (mapObject.getLayout() == MapLayoutAlgorithm.MANUAL) {
			automaticLayoutEnabled = false;
		} else {
			automaticLayoutEnabled = true;
			layoutAlgorithm = mapObject.getLayout();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.netxms.ui.eclipse.networkmaps.views.AbstractNetworkMapView#
	 * setupMapControl()
	 */
	@Override
	public void setupMapControl() {
		if (readOnly) {
			viewer.setDraggingEnabled(false);
		} else {
			viewer.addSelectionChangedListener(new ISelectionChangedListener() {
				@Override
				public void selectionChanged(SelectionChangedEvent event) {
					actionLinkObjects.setEnabled(((IStructuredSelection) event
							.getSelection()).size() == 2);
				}
			});

			addDropSupport();
		}

		ImageProvider.getInstance().addUpdateListener(this);

		if ((mapObject.getBackground() != null)
				&& (mapObject.getBackground().compareTo(NXCommon.EMPTY_GUID) != 0)) {
			if (mapObject.getBackground().equals(
					org.netxms.client.objects.NetworkMap.GEOMAP_BACKGROUND)) {
				if (!disableGeolocationBackground)
					viewer.setBackgroundImage(
							mapObject.getBackgroundLocation(),
							mapObject.getBackgroundZoom());
			} else {
				viewer.setBackgroundImage(ImageProvider.getInstance().getImage(
						mapObject.getBackground()));
			}
		}

		setConnectionRouter(mapObject.getDefaultLinkRouting(), false);
		viewer.setBackgroundColor(ColorConverter.rgbFromInt(mapObject
				.getBackgroundColor()));

		if (mapObject.getDefaultLinkColor() >= 0) {
			defaultLinkColor = new Color(viewer.getControl().getDisplay(),
					ColorConverter.rgbFromInt(mapObject.getDefaultLinkColor()));
			labelProvider.setDefaultLinkColor(defaultLinkColor);
		}

		setObjectDisplayMode(mapObject.getObjectDisplayMode(), false);
		labelProvider
				.setShowStatusBackground((mapObject.getFlags() & org.netxms.client.objects.NetworkMap.MF_SHOW_STATUS_BKGND) > 0);
		labelProvider
				.setShowStatusFrame((mapObject.getFlags() & org.netxms.client.objects.NetworkMap.MF_SHOW_STATUS_FRAME) > 0);
		labelProvider
				.setShowStatusIcons((mapObject.getFlags() & org.netxms.client.objects.NetworkMap.MF_SHOW_STATUS_ICON) > 0);

		actionShowStatusBackground.setChecked(labelProvider
				.isShowStatusBackground());
		actionShowStatusFrame.setChecked(labelProvider.isShowStatusFrame());
		actionShowStatusIcon.setChecked(labelProvider.isShowStatusIcons());
	}

	/**
	 * Add drop support
	 */
	private void addDropSupport() {
		final Transfer[] transfers = new Transfer[] { LocalSelectionTransfer
				.getTransfer() };
		viewer.addDropSupport(DND.DROP_COPY | DND.DROP_MOVE, transfers,
				new ViewerDropAdapter(viewer) {
					private int x;
					private int y;

					@SuppressWarnings("rawtypes")
					@Override
					public boolean validateDrop(Object target, int operation,
							TransferData transferType) {
						if (!LocalSelectionTransfer.getTransfer()
								.isSupportedType(transferType))
							return false;

						IStructuredSelection selection = (IStructuredSelection) LocalSelectionTransfer
								.getTransfer().getSelection();
						Iterator it = selection.iterator();
						while (it.hasNext()) {
							Object object = it.next();
							if (!((object instanceof AbstractObject) && ((AbstractObject) object)
									.isAllowedOnMap()))
								return false;
						}

						return true;
					}

					@SuppressWarnings("unchecked")
					@Override
					public boolean performDrop(Object data) {
						IStructuredSelection selection = (IStructuredSelection) LocalSelectionTransfer
								.getTransfer().getSelection();
						addObjectsFromList(selection.toList(), viewer
								.getControl().toControl(x, y));
						return true;
					}

					/*
					 * (non-Javadoc)
					 * 
					 * @see
					 * org.eclipse.jface.viewers.ViewerDropAdapter#dropAccept
					 * (org.eclipse .swt.dnd.DropTargetEvent)
					 */
					@Override
					public void dropAccept(DropTargetEvent event) {
						x = event.x;
						y = event.y;
						super.dropAccept(event);
					}
				});
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.netxms.ui.eclipse.networkmaps.views.NetworkMap#buildMapPage()
	 */
	@Override
	protected void buildMapPage() {
		mapPage = mapObject.createMapPage();
		addDciToRequestList();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.netxms.ui.eclipse.networkmaps.views.NetworkMap#createActions()
	 */
	@Override
	protected void createActions() {
		super.createActions();
		final IHandlerService handlerService = (IHandlerService) getSite()
				.getService(IHandlerService.class);

		actionAddObject = new Action(Messages.get().PredefinedMap_AddObject) {
			@Override
			public void run() {
				addObjectToMap();
			}
		};
		actionAddObject
				.setId("org.netxms.ui.eclipse.networkmaps.localActions.PredefinedMap.AddObject"); //$NON-NLS-1$
		actionAddObject
				.setActionDefinitionId("org.netxms.ui.eclipse.networkmaps.localCommands.PredefinedMap.AddObject"); //$NON-NLS-1$
		handlerService.activateHandler(actionAddObject.getActionDefinitionId(),
				new ActionHandler(actionAddObject));

		actionAddDCIContainer = new Action(
				Messages.get().PredefinedMap_AddDciContainer) {
			@Override
			public void run() {
				addDCIContainerToMap();
			}
		};
		actionAddDCIContainer
				.setId("org.netxms.ui.eclipse.networkmaps.localActions.PredefinedMap.AddDCIContainer"); //$NON-NLS-1$
		actionAddDCIContainer
				.setActionDefinitionId("org.netxms.ui.eclipse.networkmaps.localCommands.PredefinedMap.AddDCIContainer"); //$NON-NLS-1$
		handlerService.activateHandler(
				actionAddDCIContainer.getActionDefinitionId(),
				new ActionHandler(actionAddDCIContainer));

		actionAddDCIImage = new Action(Messages.get().PredefinedMap_AddDciImage) {
			@Override
			public void run() {
				addDCIImageToMap();
			}
		};
		actionAddDCIImage
				.setId("org.netxms.ui.eclipse.networkmaps.localActions.PredefinedMap.AddDCIImage"); //$NON-NLS-1$
		actionAddDCIImage
				.setActionDefinitionId("org.netxms.ui.eclipse.networkmaps.localCommands.PredefinedMap.AddDCIImage"); //$NON-NLS-1$
		handlerService.activateHandler(actionAddDCIImage
				.getActionDefinitionId(), new ActionHandler(actionAddDCIImage));

		actionAddGroupBox = new Action(Messages.get().PredefinedMap_GroupBox) {
			@Override
			public void run() {
				addGroupBoxDecoration();
			}
		};

		actionAddImage = new Action(Messages.get().PredefinedMap_Image) {
			@Override
			public void run() {
				addImageDecoration();
			}
		};

		actionLinkObjects = new Action(
				Messages.get().PredefinedMap_LinkObjects,
				Activator.getImageDescriptor("icons/link_add.png")) { //$NON-NLS-1$
			@Override
			public void run() {
				linkSelectedObjects();
			}
		};
		actionLinkObjects
				.setId("org.netxms.ui.eclipse.networkmaps.localActions.PredefinedMap.LinkObjects"); //$NON-NLS-1$
		actionLinkObjects
				.setActionDefinitionId("org.netxms.ui.eclipse.networkmaps.localCommands.PredefinedMap.LinkObjects"); //$NON-NLS-1$
		handlerService.activateHandler(actionLinkObjects
				.getActionDefinitionId(), new ActionHandler(actionLinkObjects));

		actionRemove = new Action(Messages.get().PredefinedMap_RemoveFromMap,
				SharedIcons.DELETE_OBJECT) {
			@Override
			public void run() {
				removeSelectedElements();
			}
		};
		actionRemove
				.setId("org.netxms.ui.eclipse.networkmaps.localActions.PredefinedMap.Remove"); //$NON-NLS-1$
		actionRemove
				.setActionDefinitionId("org.netxms.ui.eclipse.networkmaps.localCommands.PredefinedMap.Remove"); //$NON-NLS-1$
		handlerService.activateHandler(actionRemove.getActionDefinitionId(),
				new ActionHandler(actionRemove));

		actionDCIContainerProperties = new Action(
				Messages.get().PredefinedMap_Properties) {
			@Override
			public void run() {
				showDCIContainerProperties();
			}
		};

		actionDCIImageProperties = new Action(
				Messages.get().PredefinedMap_Properties) {
			@Override
			public void run() {
				showDCIImageProperties();
			}
		};

		actionMapProperties = new Action(
				Messages.get().PredefinedMap_MapProperties) {
			@Override
			public void run() {
				showMapProperties();
			}
		};

		actionLinkProperties = new Action(
				Messages.get().PredefinedMap_Properties) {
			@Override
			public void run() {
				showLinkProperties();
			}
		};

		actionAddTextBox = new Action("Text box") {
			@Override
			public void run() {
				addTextBoxToMap();
			}
		};

		actionTextBoxProperties = new Action("Text box properties") {
			@Override
			public void run() {
				showTextBoxProperties();
			}
		};
	}

	/**
	 * Create "Add decoration" submenu
	 * 
	 * @return menu manager for decoration submenu
	 */
	private IMenuManager createDecorationAdditionSubmenu() {
		MenuManager menu = new MenuManager(
				Messages.get().PredefinedMap_AddDecoration);
		menu.add(actionAddGroupBox);
		menu.add(actionAddImage);
		menu.add(actionAddTextBox);
		return menu;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.netxms.ui.eclipse.networkmaps.views.NetworkMap#fillMapContextMenu(
	 * org.eclipse.jface.action.IMenuManager)
	 */
	@Override
	protected void fillMapContextMenu(IMenuManager manager) {
		if (!readOnly) {
			manager.add(actionAddObject);
			manager.add(actionAddDCIContainer);
			manager.add(actionAddDCIImage);
			manager.add(createDecorationAdditionSubmenu());
			manager.add(new Separator());
		}
		super.fillMapContextMenu(manager);
		manager.add(new Separator());
		manager.add(actionMapProperties);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.netxms.ui.eclipse.networkmaps.views.NetworkMap#fillObjectContextMenu
	 * (org.eclipse.jface.action.IMenuManager)
	 */
	@Override
	protected void fillObjectContextMenu(IMenuManager manager) {
		if (!readOnly) {
			int size = ((IStructuredSelection) viewer.getSelection()).size();
			if (size == 2)
				manager.add(actionLinkObjects);
			manager.add(actionRemove);
			manager.add(new Separator());
		}
		super.fillObjectContextMenu(manager);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.netxms.ui.eclipse.networkmaps.views.NetworkMap#fillLinkContextMenu
	 * (org.eclipse.jface.action.IMenuManager)
	 */
	@Override
	protected void fillLinkContextMenu(IMenuManager manager) {
		if (readOnly) {
			super.fillLinkContextMenu(manager);
			return;
		}

		int size = ((IStructuredSelection) viewer.getSelection()).size();
		manager.add(actionRemove);
		manager.add(new Separator());
		super.fillLinkContextMenu(manager);
		if (size == 1) {
			manager.add(new Separator());
			manager.add(actionLinkProperties);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.netxms.ui.eclipse.networkmaps.views.NetworkMap#fillElementContextMenu
	 * (org.eclipse.jface.action.IMenuManager)
	 */
	protected void fillElementContextMenu(IMenuManager manager) {
		if (!readOnly) {
			manager.add(actionRemove);
			Object o = ((IStructuredSelection) viewer.getSelection())
					.getFirstElement();
			if (o instanceof NetworkMapDCIContainer)
				manager.add(actionDCIContainerProperties);
			if (o instanceof NetworkMapDCIImage)
				manager.add(actionDCIImageProperties);
			if (o instanceof NetworkMapTextBox)
				manager.add(actionTextBoxProperties);
			manager.add(new Separator());
		}
		super.fillElementContextMenu(manager);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.netxms.ui.eclipse.networkmaps.views.NetworkMap#fillLocalPullDown(org
	 * .eclipse.jface.action.IMenuManager)
	 */
	@Override
	protected void fillLocalPullDown(IMenuManager manager) {
		if (!readOnly) {
			manager.add(actionAddObject);
			manager.add(actionLinkObjects);
			manager.add(createDecorationAdditionSubmenu());
			manager.add(new Separator());
		}
		super.fillLocalPullDown(manager);
		manager.add(new Separator());
		manager.add(actionMapProperties);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.netxms.ui.eclipse.networkmaps.views.NetworkMap#fillLocalToolBar(org
	 * .eclipse.jface.action.IToolBarManager)
	 */
	@Override
	protected void fillLocalToolBar(IToolBarManager manager) {
		if (!readOnly) {
			manager.add(actionLinkObjects);
			manager.add(new Separator());
		}
		super.fillLocalToolBar(manager);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.netxms.ui.eclipse.networkmaps.views.NetworkMap#saveLayout()
	 */
	@Override
	protected void saveLayout() {
		saveMap();
	}

	/**
	 * Add object to map
	 */
	private void addObjectToMap() {
		ObjectSelectionDialog dlg = new ObjectSelectionDialog(getSite()
				.getShell(), null, null);
		if (dlg.open() != Window.OK)
			return;

		addObjectsFromList(dlg.getSelectedObjects(), null);
	}

	/**
	 * Add DCI container to map
	 */
	private void addDCIContainerToMap() {
		NetworkMapDCIContainer dciContainer = new NetworkMapDCIContainer(
				mapPage.createElementId());
		// runn property page
		PropertyDialog dlg = PropertyDialog.createDialogOn(
				getSite().getShell(), null, dciContainer);
		if (dlg != null) {
			if (dlg.open() == Window.OK) {
				mapPage.addElement(dciContainer);
				saveMap();
				addDciToRequestList();
			}
		}
	}

	/**
	 * Add DCI image to map
	 */
	private void addDCIImageToMap() {
		NetworkMapDCIImage dciImage = new NetworkMapDCIImage(
				mapPage.createElementId());
		// runn property page
		PropertyDialog dlg = PropertyDialog.createDialogOn(
				getSite().getShell(), null, dciImage);
		if (dlg != null) {
			if (dlg.open() == Window.OK) {
				mapPage.addElement(dciImage);
				saveMap();
				addDciToRequestList();
			}
		}
	}

	/**
	 * Add objects from list to map
	 * 
	 * @param list
	 *            object list
	 */
	private void addObjectsFromList(List<AbstractObject> list, Point location) {
		int added = 0;
		for (AbstractObject object : list) {
			if (mapPage.findObjectElement(object.getObjectId()) == null) {
				final NetworkMapObject mapObject = new NetworkMapObject(
						mapPage.createElementId(), object.getObjectId());
				if (location != null)
					mapObject.setLocation(location.x, location.y);
				else
					mapObject.setLocation(40, 40);
				mapPage.addElement(mapObject);
				added++;
			}
		}

		if (added > 0) {
			saveMap();
		}
	}

	/**
	 * Link currently selected objects
	 */
	private void linkSelectedObjects() {
		IStructuredSelection selection = (IStructuredSelection) viewer
				.getSelection();
		if (selection.size() != 2)
			return;

		Object[] objects = selection.toArray();
		long id1 = ((NetworkMapObject) objects[0]).getId();
		long id2 = ((NetworkMapObject) objects[1]).getId();
		mapPage.addLink(new NetworkMapLink(NetworkMapLink.NORMAL, id1, id2));
		saveMap();
	}

	/**
	 * Remove currently selected map elements
	 */
	private void removeSelectedElements() {
		IStructuredSelection selection = (IStructuredSelection) viewer
				.getSelection();

		if (!MessageDialogHelper
				.openQuestion(
						getSite().getShell(),
						Messages.get().PredefinedMap_ConfirmRemoval,
						(selection.size() == 1) ? Messages.get().PredefinedMap_RemovalConfirmationSingular
								: Messages.get().PredefinedMap_RemovalConfirmationPlural))
			return;

		Object[] objects = selection.toArray();
		for (Object element : objects) {
			if (element instanceof AbstractObject) {
				mapPage.removeObjectElement(((AbstractObject) element)
						.getObjectId());
			} else if (element instanceof NetworkMapElement) {
				mapPage.removeElement(((NetworkMapElement) element).getId());
			} else if (element instanceof NetworkMapLink) {
				mapPage.removeLink((NetworkMapLink) element);
			}
		}
		saveMap();

		// for some reason graph viewer does not clear selection
		// after all selected elements was removed, so we have to do it manually
		viewer.setSelection(StructuredSelection.EMPTY);
	}

	/**
	 * Add group box decoration
	 */
	private void addGroupBoxDecoration() {
		AddGroupBoxDialog dlg = new AddGroupBoxDialog(getSite().getShell());
		if (dlg.open() != Window.OK)
			return;

		NetworkMapDecoration element = new NetworkMapDecoration(
				mapPage.createElementId(), NetworkMapDecoration.GROUP_BOX);
		element.setSize(dlg.getWidth(), dlg.getHeight());
		element.setTitle(dlg.getTitle());
		element.setColor(ColorConverter.rgbToInt(dlg.getColor()));
		mapPage.addElement(element);

		saveMap();
	}

	/**
	 * Add image decoration
	 */
	private void addImageDecoration() {
		ImageSelectionDialog dlg = new ImageSelectionDialog(getSite()
				.getShell());
		if (dlg.open() != Window.OK)
			return;

		UUID imageGuid = dlg.getLibraryImage().getGuid();
		Rectangle imageBounds = dlg.getImage().getBounds();

		NetworkMapDecoration element = new NetworkMapDecoration(
				mapPage.createElementId(), NetworkMapDecoration.IMAGE);
		element.setSize(imageBounds.width, imageBounds.height);
		element.setTitle(imageGuid.toString());
		mapPage.addElement(element);

		saveMap();
	}

	/**
	 * Add text box element
	 */
	private void addTextBoxToMap() {
		NetworkMapTextBox textBox = new NetworkMapTextBox(
				mapPage.createElementId());
		PropertyDialog dlg = PropertyDialog.createDialogOn(
				getSite().getShell(), null, textBox);
		if (dlg.open() != Window.OK)
			return;

		mapPage.addElement(textBox);
		saveMap();
	}

	/**
	 * Save map on server
	 */
	private void saveMap() {
		updateObjectPositions();

		final NXCObjectModificationData md = new NXCObjectModificationData(
				rootObject.getObjectId());
		md.setMapContent(mapPage.getElements(), mapPage.getLinks());
		md.setMapLayout(automaticLayoutEnabled ? layoutAlgorithm
				: MapLayoutAlgorithm.MANUAL);
		md.setConnectionRouting(routingAlgorithm);

		int flags = mapObject.getFlags();
		if (labelProvider.isShowStatusIcons())
			flags |= NetworkMap.MF_SHOW_STATUS_ICON;
		else
			flags &= ~NetworkMap.MF_SHOW_STATUS_ICON;
		if (labelProvider.isShowStatusFrame())
			flags |= NetworkMap.MF_SHOW_STATUS_FRAME;
		else
			flags &= ~NetworkMap.MF_SHOW_STATUS_FRAME;
		if (labelProvider.isShowStatusBackground())
			flags |= NetworkMap.MF_SHOW_STATUS_BKGND;
		else
			flags &= ~NetworkMap.MF_SHOW_STATUS_BKGND;
		md.setObjectFlags(flags);

		new ConsoleJob(String.format(Messages.get().PredefinedMap_SaveJobTitle,
				rootObject.getObjectName()), this, Activator.PLUGIN_ID,
				Activator.PLUGIN_ID) {
			@Override
			protected void runInternal(IProgressMonitor monitor)
					throws Exception {
				session.modifyObject(md);
				runInUIThread(new Runnable() {
					@Override
					public void run() {
						viewer.setInput(mapPage);
					}
				});
			}

			@Override
			protected String getErrorMessage() {
				return Messages.get().PredefinedMap_SaveJobError;
			}
		}.start();
		addDciToRequestList();
	}

	/**
	 * Show properties dialog for map object
	 */
	private void showMapProperties() {
		updateObjectPositions();
		PropertyDialog dlg = PropertyDialog.createDialogOn(
				getSite().getShell(), null, mapObject);
		dlg.open();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.netxms.ui.eclipse.networkmaps.views.NetworkMap#isSelectableElement
	 * (java.lang.Object)
	 */
	@Override
	protected boolean isSelectableElement(Object element) {
		return (element instanceof NetworkMapDecoration)
				|| (element instanceof NetworkMapLink)
				|| (element instanceof NetworkMapDCIContainer)
				|| (element instanceof NetworkMapDCIImage)
				|| (element instanceof NetworkMapTextBox);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.netxms.ui.eclipse.imagelibrary.shared.ImageUpdateListener#imageUpdated
	 * (java.util.UUID)
	 */
	@Override
	public void imageUpdated(final UUID guid) {
		getSite().getShell().getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				if (guid.equals(mapObject.getBackground()))
					viewer.setBackgroundImage(ImageProvider.getInstance()
							.getImage(guid));

				final String guidText = guid.toString();
				for (NetworkMapElement e : mapPage.getElements()) {
					if ((e instanceof NetworkMapDecoration)
							&& (((NetworkMapDecoration) e).getDecorationType() == NetworkMapDecoration.IMAGE)
							&& ((NetworkMapDecoration) e).getTitle().equals(
									guidText)) {
						viewer.updateDecorationFigure((NetworkMapDecoration) e);
						break;
					}
				}
			}
		});
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.netxms.ui.eclipse.networkmaps.views.NetworkMap#dispose()
	 */
	@Override
	public void dispose() {
		ImageProvider.getInstance().removeUpdateListener(this);
		if (defaultLinkColor != null)
			defaultLinkColor.dispose();
		super.dispose();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.netxms.ui.eclipse.networkmaps.views.NetworkMap#onObjectChange(org
	 * .netxms.client.objects.AbstractObject)
	 */
	@Override
	protected void onObjectChange(final AbstractObject object) {
		super.onObjectChange(object);

		if (object.getObjectId() != mapObject.getObjectId())
			return;

		UUID oldBackground = mapObject.getBackground();
		mapObject = (org.netxms.client.objects.NetworkMap) object;
		if (!oldBackground.equals(mapObject.getBackground())
				|| mapObject.getBackground().equals(
						org.netxms.client.objects.NetworkMap.GEOMAP_BACKGROUND)) {
			if (mapObject.getBackground().equals(NXCommon.EMPTY_GUID)) {
				viewer.setBackgroundImage(null);
			} else if (mapObject.getBackground().equals(
					org.netxms.client.objects.NetworkMap.GEOMAP_BACKGROUND)) {
				if (!disableGeolocationBackground)
					viewer.setBackgroundImage(
							mapObject.getBackgroundLocation(),
							mapObject.getBackgroundZoom());
			} else {
				viewer.setBackgroundImage(ImageProvider.getInstance().getImage(
						mapObject.getBackground()));
			}
		}

		viewer.setBackgroundColor(ColorConverter.rgbFromInt(mapObject
				.getBackgroundColor()));

		setConnectionRouter(mapObject.getDefaultLinkRouting(), false);

		if (defaultLinkColor != null)
			defaultLinkColor.dispose();
		if (mapObject.getDefaultLinkColor() >= 0) {
			defaultLinkColor = new Color(viewer.getControl().getDisplay(),
					ColorConverter.rgbFromInt(mapObject.getDefaultLinkColor()));
		} else {
			defaultLinkColor = null;
		}
		labelProvider.setDefaultLinkColor(defaultLinkColor);

		if ((mapObject.getBackground() != null)
				&& (mapObject.getBackground().compareTo(NXCommon.EMPTY_GUID) != 0)) {
			if (mapObject.getBackground().equals(
					org.netxms.client.objects.NetworkMap.GEOMAP_BACKGROUND)) {
				if (!disableGeolocationBackground)
					viewer.setBackgroundImage(
							mapObject.getBackgroundLocation(),
							mapObject.getBackgroundZoom());
			} else {
				viewer.setBackgroundImage(ImageProvider.getInstance().getImage(
						mapObject.getBackground()));
			}
		}

		setLayoutAlgorithm(mapObject.getLayout(), false);

		labelProvider
				.setShowStatusBackground((mapObject.getFlags() & org.netxms.client.objects.NetworkMap.MF_SHOW_STATUS_BKGND) > 0);
		labelProvider
				.setShowStatusFrame((mapObject.getFlags() & org.netxms.client.objects.NetworkMap.MF_SHOW_STATUS_FRAME) > 0);
		labelProvider
				.setShowStatusIcons((mapObject.getFlags() & org.netxms.client.objects.NetworkMap.MF_SHOW_STATUS_ICON) > 0);

		refreshMap();
	}

	/**
	 * Show DCI Container properties
	 */
	private void showDCIContainerProperties() {
		updateObjectPositions();
		IStructuredSelection selection = (IStructuredSelection) viewer
				.getSelection();
		if ((selection.size() != 1)
				|| !(selection.getFirstElement() instanceof NetworkMapDCIContainer))
			return;
		NetworkMapDCIContainer container = (NetworkMapDCIContainer) selection
				.getFirstElement();
		PropertyDialog dlg = PropertyDialog.createDialogOn(
				getSite().getShell(), null, container);
		if (dlg != null) {
			if (dlg.open() == PropertyDialog.OK)
				saveMap();
		}
	}

	/**
	 * Show DCI Image properties
	 */
	private void showDCIImageProperties() {
		updateObjectPositions();
		IStructuredSelection selection = (IStructuredSelection) viewer
				.getSelection();
		if ((selection.size() != 1)
				|| !(selection.getFirstElement() instanceof NetworkMapDCIImage))
			return;
		NetworkMapDCIImage container = (NetworkMapDCIImage) selection
				.getFirstElement();
		PropertyDialog dlg = PropertyDialog.createDialogOn(
				getSite().getShell(), null, container);
		if (dlg != null) {
			if (dlg.open() == PropertyDialog.OK)
				saveMap();
		}
	}

	/**
	 * Show properties for currently selected link
	 */
	private void showLinkProperties() {
		updateObjectPositions();
		IStructuredSelection selection = (IStructuredSelection) viewer
				.getSelection();
		if ((selection.size() != 1)
				|| !(selection.getFirstElement() instanceof NetworkMapLink))
			return;
		LinkEditor link = new LinkEditor(
				(NetworkMapLink) selection.getFirstElement(), mapPage);
		PropertyDialog dlg = PropertyDialog.createDialogOn(
				getSite().getShell(), null, link);
		if (dlg != null) {
			dlg.open();
			if (link.isModified())
				saveMap();
		}
	}

	/**
	 * Show text box properties
	 */
	private void showTextBoxProperties() {
		updateObjectPositions();
		IStructuredSelection selection = (IStructuredSelection) viewer
				.getSelection();
		if ((selection.size() != 1)
				|| !(selection.getFirstElement() instanceof NetworkMapTextBox))
			return;

		PropertyDialog dlg = PropertyDialog.createDialogOn(
				getSite().getShell(), null,
				(NetworkMapTextBox) selection.getFirstElement());
		if (dlg.open() != Window.OK)
			return;

		saveMap();
	}
}
