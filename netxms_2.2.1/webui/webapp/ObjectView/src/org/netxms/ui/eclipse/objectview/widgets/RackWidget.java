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
package org.netxms.ui.eclipse.objectview.widgets;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.netxms.base.NXCommon;
import org.netxms.client.constants.RackOrientation;
import org.netxms.client.datacollection.DciValue;
import org.netxms.client.objects.AbstractNode;
import org.netxms.client.objects.AbstractObject;
import org.netxms.client.objects.DataCollectionTarget;
import org.netxms.client.objects.Rack;
import org.netxms.client.objects.RackElement;
import org.netxms.ui.eclipse.console.resources.SharedColors;
import org.netxms.ui.eclipse.console.resources.StatusDisplayInfo;
import org.netxms.ui.eclipse.imagelibrary.shared.ImageProvider;
import org.netxms.ui.eclipse.imagelibrary.shared.ImageUpdateListener;
import org.netxms.ui.eclipse.objectview.Activator;
import org.netxms.ui.eclipse.objectview.widgets.helpers.RackSelectionListener;
import org.netxms.ui.eclipse.tools.ColorCache;
import org.netxms.ui.eclipse.tools.FontTools;
import org.netxms.ui.eclipse.tools.WidgetHelper;

/**
 * Rack display widget
 */
public class RackWidget extends Canvas implements PaintListener, DisposeListener, ImageUpdateListener, MouseListener, MouseTrackListener, MouseMoveListener
{
   private static final double UNIT_WH_RATIO = 10.85;
   private static final int BORDER_WIDTH_RATIO = 15;
   private static final int FULL_UNIT_WIDTH = 482;
   private static final int FULL_UNIT_HEIGHT = 45;
   private static final int MARGIN_HEIGHT = 10;
   private static final int MARGIN_WIDTH = 10;
   private static final int UNIT_NUMBER_WIDTH = 30;
   private static final int TITLE_HEIGHT = 20;
   private static final int OBJECT_TOOLTIP_X_MARGIN = 6;
   private static final int OBJECT_TOOLTIP_Y_MARGIN = 6;
   private static final int OBJECT_TOOLTIP_SPACING = 6;
   private static final String[] FONT_NAMES = { "Segoe UI", "Liberation Sans", "DejaVu Sans", "Verdana", "Arial" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
   private static final String[] VIEW_LABELS = { "Front", "Back" };
   
   private Rack rack;
   private Font[] labelFonts;
   private Font[] titleFonts;
   private Image imageDefaultTop;
   private Image imageDefaultMiddle;
   private Image imageDefaultBottom;
   private Image imageDefaultRear;
   private List<ObjectImage> objects = new ArrayList<ObjectImage>();
   private AbstractObject selectedObject = null;
   private Set<RackSelectionListener> selectionListeners = new HashSet<RackSelectionListener>(0);
   private Point objectToolTipLocation = null;
   private Rectangle objectTooltipRectangle = null;
   private Font objectToolTipHeaderFont;
   private AbstractObject tooltipObject = null;
   private ColorCache colorCache;
   private RackOrientation view;
   
   /**
    * @param parent
    * @param style
    */
   public RackWidget(Composite parent, int style, Rack rack, RackOrientation view)
   {
      super(parent, style | SWT.DOUBLE_BUFFERED);
      this.rack = rack;
      this.view = (view == RackOrientation.FILL) ? RackOrientation.FRONT : view;
      
      colorCache = new ColorCache(this);
      
      objectToolTipHeaderFont = FontTools.createFont(FONT_NAMES, 1, SWT.BOLD);
      
      setBackground(SharedColors.getColor(SharedColors.RACK_BACKGROUND, getDisplay()));
      
      final String fontName = FontTools.findFirstAvailableFont(FONT_NAMES);
      labelFonts = new Font[16];
      titleFonts = new Font[16];
      for(int i = 0; i < labelFonts.length; i++)
      {
         labelFonts[i] = new Font(getDisplay(), fontName, i + 6, SWT.NORMAL);
         titleFonts[i] = new Font(getDisplay(), fontName, i + 6, SWT.BOLD);
      }
      
      imageDefaultTop = Activator.getImageDescriptor("icons/rack-default-top.png").createImage(); //$NON-NLS-1$
      imageDefaultMiddle = Activator.getImageDescriptor("icons/rack-default-middle.png").createImage(); //$NON-NLS-1$
      imageDefaultBottom = Activator.getImageDescriptor("icons/rack-default-bottom.png").createImage(); //$NON-NLS-1$
      imageDefaultRear = Activator.getImageDescriptor("icons/rack-default-rear.png").createImage(); //$NON-NLS-1$
      
      addPaintListener(this);
      addMouseListener(this);
      //addMouseTrackListener(this);
      //addMouseMoveListener(this);
      addDisposeListener(this);
      ImageProvider.getInstance().addUpdateListener(this);
   }

   /**
    * @return the currentObject
    */
   public AbstractObject getCurrentObject()
   {
      return selectedObject;
   }

   /* (non-Javadoc)
    * @see org.eclipse.swt.events.PaintListener#paintControl(org.eclipse.swt.events.PaintEvent)
    */
   @Override
   public void paintControl(PaintEvent e)
   {
      final GC gc = e.gc;
      
      gc.setAntialias(SWT.ON);
      //gc.setInterpolation(SWT.HIGH);
      
      // Calculate bounding box for rack picture
      Rectangle rect = getClientArea();
      rect.x += MARGIN_WIDTH + UNIT_NUMBER_WIDTH;
      rect.y += MARGIN_HEIGHT + MARGIN_HEIGHT / 2 + TITLE_HEIGHT;
      rect.height -= MARGIN_HEIGHT * 2 + MARGIN_HEIGHT / 2 + TITLE_HEIGHT;
      
      // Estimated unit width/height and calculate border width
      double unitHeight = (double)rect.height / (double)rack.getHeight();
      int unitWidth = (int)(unitHeight * UNIT_WH_RATIO);
      int borderWidth = unitWidth / BORDER_WIDTH_RATIO;
      if (borderWidth < 3)
         borderWidth = 3;
      rect.height -= borderWidth;

      // precise unit width and height taking borders into consideration
      unitHeight = (double)(rect.height - ((borderWidth + 1) / 2) * 2) / (double)rack.getHeight();
      unitWidth = (int)(unitHeight * UNIT_WH_RATIO);
      rect.width = unitWidth + borderWidth * 2;
      
      // Title
      gc.setFont(WidgetHelper.getBestFittingFont(gc, titleFonts, VIEW_LABELS[0], rect.width, TITLE_HEIGHT)); //$NON-NLS-1$
      Point titleSize = gc.textExtent(VIEW_LABELS[view.getValue() - 1]);
      gc.drawText(VIEW_LABELS[view.getValue() - 1], (rect.width / 2 - titleSize.x / 2) + UNIT_NUMBER_WIDTH + MARGIN_WIDTH, rect.y - TITLE_HEIGHT - MARGIN_HEIGHT / 2);

      // Rack itself
      gc.setBackground(SharedColors.getColor(SharedColors.RACK_EMPTY_SPACE, getDisplay()));
      gc.fillRoundRectangle(rect.x, rect.y, rect.width, rect.height, 3, 3);
      gc.setLineWidth(borderWidth);
      gc.setForeground(SharedColors.getColor(SharedColors.RACK_BORDER, getDisplay()));
      gc.drawRoundRectangle(rect.x, rect.y, rect.width, rect.height, 3, 3);
      
      // Rack bottom
      gc.setBackground(SharedColors.getColor(SharedColors.RACK_BORDER, getDisplay()));
      gc.fillRectangle(rect.x + borderWidth * 2 - (borderWidth + 1) / 2, rect.y + rect.height, borderWidth * 2, (int)(borderWidth * 1.5));
      gc.fillRectangle(rect.x + rect.width - borderWidth * 3 - (borderWidth + 1) / 2, rect.y + rect.height, borderWidth * 2, (int)(borderWidth * 1.5));

      // Draw unit numbers
      int[] unitBaselines = new int[rack.getHeight() + 1];
      gc.setFont(WidgetHelper.getBestFittingFont(gc, labelFonts, "00", UNIT_NUMBER_WIDTH, (int)unitHeight - 2)); //$NON-NLS-1$
      gc.setForeground(SharedColors.getColor(SharedColors.RACK_TEXT, getDisplay()));
      gc.setBackground(SharedColors.getColor(SharedColors.RACK_BACKGROUND, getDisplay()));
      gc.setLineWidth(1);
      double dy = rack.isTopBottomNumbering() ? rect.y + unitHeight + (borderWidth + 1) / 2 : rect.y + rect.height - (borderWidth + 1) / 2;
      if (rack.isTopBottomNumbering())
         unitBaselines[0] = (int)(dy - unitHeight);
      for(int u = 1; u <= rack.getHeight(); u++)
      {
         int y = (int)dy;
         gc.drawLine(MARGIN_WIDTH, y, UNIT_NUMBER_WIDTH, y);
         String label = Integer.toString(u);
         Point textExtent = gc.textExtent(label);
         gc.drawText(label, UNIT_NUMBER_WIDTH - textExtent.x, y - (int)unitHeight / 2 - textExtent.y / 2);
         if (rack.isTopBottomNumbering())
         {
            unitBaselines[u] = y;
            dy += unitHeight;
         }
         else
         {
            unitBaselines[u - 1] = y;
            dy -= unitHeight;
         }
      }
      if (!rack.isTopBottomNumbering())
         unitBaselines[rack.getHeight()] = (int)dy;
      
      // Draw units
      objects.clear();
      List<RackElement> units = rack.getUnits();
      for(RackElement n : units)
      {
         if ((n.getRackPosition() < 1) || (n.getRackPosition() > rack.getHeight()) || 
             (rack.isTopBottomNumbering() && (n.getRackPosition() + n.getRackHeight() > rack.getHeight() + 1)) ||
             (!rack.isTopBottomNumbering() && (n.getRackPosition() - n.getRackHeight() < 0)) || 
             ((n.getRackOrientation() != view) && (n.getRackOrientation() != RackOrientation.FILL)))
            continue;
         
         int topLine, bottomLine;
         if (rack.isTopBottomNumbering())
         {
            bottomLine = unitBaselines[n.getRackPosition() + n.getRackHeight() - 1]; // lower border
            topLine = unitBaselines[n.getRackPosition() - 1];   // upper border
         }
         else
         {
            bottomLine = unitBaselines[n.getRackPosition() - n.getRackHeight()]; // lower border
            topLine = unitBaselines[n.getRackPosition()];   // upper border
         }
         final Rectangle unitRect = new Rectangle(rect.x + (borderWidth + 1) / 2, topLine + 1, rect.width - borderWidth, bottomLine - topLine);

         if ((unitRect.width <= 0) || (unitRect.height <= 0))
            break;
         
         objects.add(new ObjectImage(n, unitRect));

         // draw status indicator
         gc.setBackground(StatusDisplayInfo.getStatusColor(n.getStatus()));
         gc.fillRectangle(unitRect.x - borderWidth + borderWidth / 4 + 1, unitRect.y + 1, borderWidth / 2 - 1, Math.min(borderWidth, (int)unitHeight - 2));
         
         if ((n.getRackImage() != null) && !n.getRackImage().equals(NXCommon.EMPTY_GUID))
         {
            Image image = ImageProvider.getInstance().getImage(n.getRackImage());
            Rectangle r = image.getBounds();
            gc.drawImage(image, 0, 0, r.width, r.height, unitRect.x, unitRect.y, unitRect.width, unitRect.height);
         }
         else // Draw default representation
         {
            Image imageTop = (view == RackOrientation.REAR && n.getRackOrientation() == RackOrientation.FILL) ? imageDefaultRear : imageDefaultTop;
            
            Rectangle r = imageTop.getBounds();
            if (n.getRackHeight() == 1)
            {
                  gc.drawImage(imageTop, 0, 0, r.width, r.height, unitRect.x, unitRect.y, unitRect.width, unitRect.height);
            }
            else
            {
               Image imageMiddle = (view == RackOrientation.REAR && n.getRackOrientation() == RackOrientation.FILL) ? imageDefaultRear : imageDefaultMiddle;
               Image imageBottom = (view == RackOrientation.REAR && n.getRackOrientation() == RackOrientation.FILL) ? imageDefaultRear : imageDefaultBottom;
               if (rack.isTopBottomNumbering())
               {
                  unitRect.height = unitBaselines[n.getRackPosition()] - topLine;
                  gc.drawImage(imageTop, 0, 0, r.width, r.height, unitRect.x, unitRect.y, unitRect.width, unitRect.height);
                  
                  r = imageMiddle.getBounds();
                  int u = n.getRackPosition() + 1;
                  for(int i = 1; i < n.getRackHeight() - 1; i++, u++)
                  {
                     unitRect.y = unitBaselines[u - 1];
                     unitRect.height = unitBaselines[u] - unitRect.y;
                     gc.drawImage(imageMiddle, 0, 0, r.width, r.height, unitRect.x, unitRect.y, unitRect.width, unitRect.height);
                  }
                  
                  r = imageBottom.getBounds();
                  unitRect.y = unitBaselines[u - 1];
                  unitRect.height = unitBaselines[u] - unitRect.y;
                  gc.drawImage(imageBottom, 0, 0, r.width, r.height, unitRect.x, unitRect.y, unitRect.width, unitRect.height);
               }
               else
               {
                  unitRect.height = unitBaselines[n.getRackPosition() - 1] - topLine;
                  gc.drawImage(imageTop, 0, 0, r.width, r.height, unitRect.x, unitRect.y, unitRect.width, unitRect.height);
   
                  r = imageMiddle.getBounds();
                  int u = n.getRackPosition() - 1;
                  for(int i = 1; i < n.getRackHeight() - 1; i++, u--)
                  {
                     unitRect.y = unitBaselines[u];
                     unitRect.height = unitBaselines[u - 1] - unitRect.y;
                     gc.drawImage(imageMiddle, 0, 0, r.width, r.height, unitRect.x, unitRect.y, unitRect.width, unitRect.height);
                  }
                  
                  r = imageBottom.getBounds();
                  unitRect.y = unitBaselines[u];
                  unitRect.height = unitBaselines[u - 1] - unitRect.y;
                  gc.drawImage(imageBottom, 0, 0, r.width, r.height, unitRect.x, unitRect.y, unitRect.width, unitRect.height);
               }
            }
         }
      }

      if (objectToolTipLocation != null)
         drawObjectToolTip(gc);
   }
   
   /**
    * Draw tooltip for current object
    * 
    * @param gc
    */
   private void drawObjectToolTip(GC gc)
   {
      gc.setFont(objectToolTipHeaderFont);
      Point titleSize = gc.textExtent(tooltipObject.getObjectName());
      gc.setFont(JFaceResources.getDefaultFont());
      
      // Calculate width and height
      int width = Math.max(titleSize.x + 12, 128);
      int height = OBJECT_TOOLTIP_Y_MARGIN * 2 + titleSize.y + 2 + OBJECT_TOOLTIP_SPACING;
      
      List<String> texts = new ArrayList<String>();
      if (tooltipObject instanceof AbstractNode)
      {
         texts.add(((AbstractNode)tooltipObject).getPrimaryIP().getHostAddress());
         texts.add(((AbstractNode)tooltipObject).getPlatformName());
         String sd = ((AbstractNode)tooltipObject).getSystemDescription();
         if (sd.length() > 127)
            sd = sd.substring(0, 127) + "..."; //$NON-NLS-1$
         texts.add(sd);
         texts.add(((AbstractNode)tooltipObject).getSnmpSysName());
         texts.add(((AbstractNode)tooltipObject).getSnmpSysContact());
      }
      
      for(String s : texts)
      {
         if ((s == null) || s.isEmpty())
            continue;

         Point pt = gc.textExtent(s);
         if (width < pt.x)
            width = pt.x;
         height += pt.y;
      }
      
      List<DciValue> values = ((DataCollectionTarget)tooltipObject).getTooltipDciData();
      if (!values.isEmpty())
      {
         for(DciValue v : values)
         {
            Point pt = gc.textExtent(v.getName() + "  " + v.getValue()); //$NON-NLS-1$
            if (width < pt.x)
               width = pt.x;
            height += pt.y;
         }
         height += OBJECT_TOOLTIP_SPACING * 2 + 1;
      }
      
      if ((tooltipObject.getComments() != null) && !tooltipObject.getComments().isEmpty())
      {
         Point pt = gc.textExtent(tooltipObject.getComments());
         if (width < pt.x)
            width = pt.x;
         height += pt.y + OBJECT_TOOLTIP_SPACING * 2 + 1;
      }
      
      width += OBJECT_TOOLTIP_X_MARGIN * 2;
      
      Rectangle ca = getClientArea();
      Rectangle rect = new Rectangle(objectToolTipLocation.x - width / 2, objectToolTipLocation.y - height / 2, width, height);
      if (rect.x < 0)
         rect.x = 0;
      else if (rect.x + rect.width >= ca.width)
         rect.x = ca.width - rect.width - 1;
      if (rect.y < 0)
         rect.y = 0;
      else if (rect.y + rect.height  >= ca.height)
         rect.y = ca.height - rect.height - 1;
      
      gc.setBackground(colorCache.create(239, 225, 160));
      gc.setAlpha(240);
      gc.fillRoundRectangle(rect.x, rect.y, rect.width, rect.height, 3, 3);
      
      gc.setForeground(colorCache.create(92, 92, 92));
      gc.setAlpha(255);
      gc.setLineWidth(3);
      gc.drawRoundRectangle(rect.x, rect.y, rect.width, rect.height, 3, 3);
      gc.setLineWidth(1);
      int y = rect.y + OBJECT_TOOLTIP_Y_MARGIN + titleSize.y + 2;
      gc.drawLine(rect.x + 1, y, rect.x + rect.width - 1, y);
      
      gc.setBackground(StatusDisplayInfo.getStatusColor(tooltipObject.getStatus()));
      gc.fillOval(rect.x + OBJECT_TOOLTIP_X_MARGIN, rect.y + OBJECT_TOOLTIP_Y_MARGIN + titleSize.y / 2 - 4, 8, 8);
      
      gc.setForeground(colorCache.create(0, 0, 0));
      gc.setFont(objectToolTipHeaderFont);
      gc.drawText(tooltipObject.getObjectName(), rect.x + OBJECT_TOOLTIP_X_MARGIN + 12, rect.y + OBJECT_TOOLTIP_Y_MARGIN, true);
      
      gc.setFont(JFaceResources.getDefaultFont());
      int textLineHeight = gc.textExtent("M").y; //$NON-NLS-1$
      y = rect.y + OBJECT_TOOLTIP_Y_MARGIN + titleSize.y + OBJECT_TOOLTIP_SPACING + 2 - textLineHeight;
      for(String s : texts)
      {
         if ((s == null) || s.isEmpty())
            continue;
         
         y += textLineHeight;
         gc.drawText(s, rect.x + OBJECT_TOOLTIP_X_MARGIN, y, true);
      }

      if (!values.isEmpty())
      {
         y += textLineHeight + OBJECT_TOOLTIP_SPACING;
         gc.setForeground(colorCache.create(92, 92, 92));
         gc.drawLine(rect.x + 1, y, rect.x + rect.width - 1, y);
         y += OBJECT_TOOLTIP_SPACING;
         gc.setForeground(colorCache.create(0, 0, 0));

         for(DciValue v : values)
         {
            gc.drawText(v.getName(), rect.x + OBJECT_TOOLTIP_X_MARGIN, y, true);
            Point pt = gc.textExtent(v.getValue());
            gc.drawText(v.getValue(), rect.x + rect.width - OBJECT_TOOLTIP_X_MARGIN - pt.x, y, true);
            y += textLineHeight;
         }
         y -= textLineHeight;
      }
      
      if ((tooltipObject.getComments() != null) && !tooltipObject.getComments().isEmpty())
      {
         y += textLineHeight + OBJECT_TOOLTIP_SPACING;
         gc.setForeground(colorCache.create(92, 92, 92));
         gc.drawLine(rect.x + 1, y, rect.x + rect.width - 1, y);
         y += OBJECT_TOOLTIP_SPACING;
         gc.setForeground(colorCache.create(0, 0, 0));
         gc.drawText(tooltipObject.getComments(), rect.x + OBJECT_TOOLTIP_X_MARGIN, y, true);
      }
      
      objectTooltipRectangle = rect;
   }

   /* (non-Javadoc)
    * @see org.eclipse.swt.widgets.Composite#computeSize(int, int, boolean)
    */
   @Override
   public Point computeSize(int wHint, int hHint, boolean changed)
   {
      if (hHint == SWT.DEFAULT && wHint == SWT.DEFAULT)
         return new Point(10, 10);

      if (hHint == SWT.DEFAULT)
      {
         int borderWidth = FULL_UNIT_WIDTH / BORDER_WIDTH_RATIO;
         return new Point(FULL_UNIT_WIDTH + MARGIN_WIDTH * 2 + UNIT_NUMBER_WIDTH + borderWidth * 2, rack.getHeight() * FULL_UNIT_HEIGHT + MARGIN_HEIGHT * 2 + borderWidth * 2);
      }
      
      double unitHeight = (double)hHint / (double)rack.getHeight();
      int unitWidth = (int)(unitHeight * UNIT_WH_RATIO);
      int borderWidth = unitWidth / BORDER_WIDTH_RATIO;
      if (borderWidth < 3)
         borderWidth = 3;
      
      unitWidth = (int)((double)(hHint - ((borderWidth + 1) / 2) * 2 - MARGIN_HEIGHT * 2) / (double)rack.getHeight() * UNIT_WH_RATIO);
      return new Point(unitWidth + MARGIN_WIDTH * 2 + UNIT_NUMBER_WIDTH + borderWidth * 2, hHint);
   }

   /* (non-Javadoc)
    * @see org.eclipse.swt.events.DisposeListener#widgetDisposed(org.eclipse.swt.events.DisposeEvent)
    */
   @Override
   public void widgetDisposed(DisposeEvent e)
   {
      for(int i = 0; i < labelFonts.length; i++)
      {
         labelFonts[i].dispose();
         titleFonts[i].dispose();
      }
      
      objectToolTipHeaderFont.dispose();
      
      imageDefaultTop.dispose();
      imageDefaultMiddle.dispose();
      imageDefaultBottom.dispose();
      imageDefaultRear.dispose();
      
      ImageProvider.getInstance().removeUpdateListener(this);
   }

   /* (non-Javadoc)
    * @see org.netxms.ui.eclipse.imagelibrary.shared.ImageUpdateListener#imageUpdated(java.util.UUID)
    */
   @Override
   public void imageUpdated(UUID guid)
   {
      boolean found = false;
      List<RackElement> units = rack.getUnits();
      for(RackElement e : units)
      {
         if (guid.equals(e.getRackImage()))
         {
            found = true;
            break;
         }
      }
      if (found)
      {
         getDisplay().asyncExec(new Runnable() {
            @Override
            public void run()
            {
               redraw();
            }
         });
      }
   }
   
   /**
    * Get object at given point
    * 
    * @param p
    * @return
    */
   private AbstractObject getObjectAtPoint(Point p)
   {
      for(ObjectImage i : objects)
         if (i.contains(p))
         {
            return (AbstractObject)i.getObject();
         }
      return null;
   }

   /* (non-Javadoc)
    * @see org.eclipse.swt.events.MouseListener#mouseDoubleClick(org.eclipse.swt.events.MouseEvent)
    */
   @Override
   public void mouseDoubleClick(MouseEvent e)
   {
   }

   /* (non-Javadoc)
    * @see org.eclipse.swt.events.MouseListener#mouseDown(org.eclipse.swt.events.MouseEvent)
    */
   @Override
   public void mouseDown(MouseEvent e)
   {  
      AbstractObject object = getObjectAtPoint(new Point(e.x, e.y));
      if (objectTooltipRectangle != null)
      {
         objectToolTipLocation = null;
         objectTooltipRectangle = null;
         tooltipObject = null;
         redraw();
      }
      if ((e.button == 1) && (object != null))
      {
         mouseHover(e);
      }
      setCurrentObject(object);
   }

   /* (non-Javadoc)
    * @see org.eclipse.swt.events.MouseListener#mouseUp(org.eclipse.swt.events.MouseEvent)
    */
   @Override
   public void mouseUp(MouseEvent e)
   {
   }
   
   /* (non-Javadoc)
    * @see org.eclipse.swt.events.MouseTrackListener#mouseEnter(org.eclipse.swt.events.MouseEvent)
    */
   @Override
   public void mouseEnter(MouseEvent e)
   {
   }

   /* (non-Javadoc)
    * @see org.eclipse.swt.events.MouseTrackListener#mouseExit(org.eclipse.swt.events.MouseEvent)
    */
   @Override
   public void mouseExit(MouseEvent e)
   {
      objectToolTipLocation = null;
      objectTooltipRectangle = null;
      tooltipObject = null;
      redraw();
   }

   /* (non-Javadoc)
    * @see org.eclipse.swt.events.MouseTrackListener#mouseHover(org.eclipse.swt.events.MouseEvent)
    */
   @Override
   public void mouseHover(MouseEvent e)
   {
      if (objectTooltipRectangle != null) // ignore hover if tooltip already open
         return;
      
      AbstractObject object = getObjectAtPoint(new Point(e.x, e.y));
      if (object != selectedObject)
      {
         objectToolTipLocation = (object != null) ? new Point(e.x, e.y) : null;
         tooltipObject = object;
         redraw();
      }
   }

   /* (non-Javadoc)
    * @see org.eclipse.swt.events.MouseMoveListener#mouseMove(org.eclipse.swt.events.MouseEvent)
    */
   @Override
   public void mouseMove(MouseEvent e)
   {
      if ((objectTooltipRectangle != null) && !objectTooltipRectangle.contains(e.x, e.y))
      {
         objectTooltipRectangle = null;
         objectToolTipLocation = null;
         tooltipObject = null;
         redraw();
      }
   }

   /**
    * Add selection listener
    * 
    * @param listener
    */
   public void addSelectionListener(RackSelectionListener listener)
   {
      selectionListeners.add(listener);
   }
   
   /**
    * Remove selection listener
    * 
    * @param listener
    */
   public void removeSelectionListener(RackSelectionListener listener)
   {
      selectionListeners.remove(listener);
   }
   
   /**
    * Set current selection
    * 
    * @param o
    */
   private void setCurrentObject(AbstractObject o)
   {
      selectedObject = o;
      for(RackSelectionListener l : selectionListeners)
         l.objectSelected(selectedObject);
   }
   
   /**
    * Object image information
    */
   private class ObjectImage
   {
      private RackElement object;
      private Rectangle rect;

      public ObjectImage(RackElement object, Rectangle rect)
      {
         this.object = object;
         this.rect = new Rectangle(rect.x, rect.y, rect.width, rect.height);
      }
      
      public boolean contains(Point p)
      {
         return rect.contains(p);
      }
      
      public RackElement getObject()
      {
         return object;
      }
   }
}
