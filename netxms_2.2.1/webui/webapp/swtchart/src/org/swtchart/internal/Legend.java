/*******************************************************************************
 * Copyright (c) 2008-2011 SWTChart project. All rights reserved. 
 * 
 * This code is distributed under the terms of the Eclipse Public License v1.0
 * which is available at http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.swtchart.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.netxms.client.datacollection.DataFormatter;
import org.swtchart.Chart;
import org.swtchart.IBarSeries;
import org.swtchart.ILegend;
import org.swtchart.ILineSeries;
import org.swtchart.ISeries;
import org.swtchart.internal.series.Series;

/**
 * A legend for chart.
 */
public class Legend extends Canvas implements ILegend, PaintListener
{
   private static final String HEADER_ID = "$$header$$";
   private static final String VALUE_PLACEHOLDER = "000.0000";
   
   /** the plot chart */
   private Chart chart;

   /** the state indicating the legend visibility */
   private boolean visible;

   /** the position of legend */
   private int position;

   /** the margin */
   private static final int MARGIN = 4;

   /** the width of area to draw symbol */
   private static final int SYMBOL_WIDTH = 20;

   /** the default foreground */
   private static final Color DEFAULT_FOREGROUND = Display.getDefault().getSystemColor(SWT.COLOR_BLACK);

   /** the default position */
   private static final int DEFAULT_POSITION = SWT.BOTTOM;

   /** the map between series id and cell bounds */
   private Map<String, Rectangle> cellBounds;
   
   /** offset for drawing extended info */
   private int extendedInfoOffset = 0;
   
   /** extended legend flag */
   private boolean extended = true;
   
   /** font used for column headers */
   private Font headerFont = null;

   /**
    * Constructor.
    * 
    * @param chart the chart
    * @param style the style
    */
   public Legend(Chart chart, int style)
   {
      super(chart, style);
      this.chart = chart;

      updateHeaderFont();
      visible = true;
      position = DEFAULT_POSITION;
      cellBounds = new HashMap<String, Rectangle>();
      setForeground(DEFAULT_FOREGROUND);
      setBackground(chart.getBackground());
      addPaintListener(this);
      addDisposeListener(new DisposeListener() {
         @Override
         public void widgetDisposed(DisposeEvent e)
         {
            if (headerFont != null)
               headerFont.dispose();
         }
      });
   }

   /*
    * @see Control#setVisible(boolean)
    */
   @Override
   public void setVisible(boolean visible)
   {
      if (this.visible == visible)
      {
         return;
      }

      this.visible = visible;
      chart.updateLayout();
   }

   /*
    * @see Control#isVisible()
    */
   @Override
   public boolean isVisible()
   {
      return visible;
   }

   /* (non-Javadoc)
    * @see org.swtchart.ILegend#isExtended()
    */
   @Override
   public boolean isExtended()
   {
      return extended;
   }

   /* (non-Javadoc)
    * @see org.swtchart.ILegend#setExtended(boolean)
    */
   @Override
   public void setExtended(boolean extended)
   {
      this.extended = extended;
   }
   
   /**
    * Update header font
    */
   private void updateHeaderFont()
   {
      if (headerFont != null)
         headerFont.dispose();
      
      FontData fd = getFont().getFontData()[0];
      fd.setStyle(SWT.BOLD);
      headerFont = new Font(getDisplay(), fd);
   }

   /*
    * @see Canvas#setFont(Font)
    */
   @Override
   public void setFont(Font font)
   {
      super.setFont(font);
      updateHeaderFont();
      chart.updateLayout();
   }

   /*
    * @see Control#setForeground(Color)
    */
   @Override
   public void setForeground(Color color)
   {
      if (color == null)
      {
         super.setForeground(DEFAULT_FOREGROUND);
      }
      else
      {
         super.setForeground(color);
      }
   }

   /*
    * @see Control#setBackground(Color)
    */
   @Override
   public void setBackground(Color color)
   {
      if (color == null)
      {
         super.setBackground(chart.getBackground());
      }
      else
      {
         super.setBackground(color);
      }
   }

   /*
    * @see ILegend#getPosition()
    */
   public int getPosition()
   {
      return position;
   }

   /*
    * @see ILegend#setPosition(int)
    */
   public void setPosition(int value)
   {
      if (value == SWT.LEFT || value == SWT.RIGHT || value == SWT.TOP || value == SWT.BOTTOM)
      {
         position = value;
      }
      else
      {
         position = DEFAULT_POSITION;
      }
      chart.updateLayout();
   }

   /*
    * @see ILegend#getBounds(String)
    */
   public Rectangle getBounds(String seriesId)
   {
      return cellBounds.get(seriesId);
   }

   /**
    * Sorts the given series array. For instance, if there are two stack series in horizontal orientation, the top of stack series
    * should appear at top of legend.
    * <p>
    * If there are multiple x axes, the given series array will be sorted with x axis first. And then, the series in each x axis
    * will be sorted with {@link Legend#sort(List, boolean, boolean)}.
    * 
    * @param seriesArray the series array
    * @return the sorted series array
    */
   private ISeries[] sort(ISeries[] seriesArray)
   {
      // create a map between axis id and series list
      Map<Integer, List<ISeries>> map = new HashMap<Integer, List<ISeries>>();
      for(ISeries series : seriesArray)
      {
         int axisId = series.getXAxisId();
         List<ISeries> list = map.get(axisId);
         if (list == null)
         {
            list = new ArrayList<ISeries>();
         }
         list.add(series);
         map.put(axisId, list);
      }

      // sort an each series list
      List<ISeries> sortedArray = new ArrayList<ISeries>();
      boolean isVertical = chart.getOrientation() == SWT.VERTICAL;
      for(Entry<Integer, List<ISeries>> entry : map.entrySet())
      {
         boolean isCategoryEnabled = chart.getAxisSet().getXAxis(entry.getKey()).isCategoryEnabled();
         sortedArray.addAll(sort(entry.getValue(), isCategoryEnabled, isVertical));
      }

      return sortedArray.toArray(new ISeries[0]);
   }

   /**
    * Sorts the given series list which belongs to a certain x axis.
    * <ul>
    * <li>The stacked series will be gathered, and the order of stack series will be reversed.</li>
    * <li>In the case of vertical orientation, the order of whole series will be reversed.</li>
    * </ul>
    * 
    * @param seriesList the series list which belongs to a certain x axis
    * @param isCategoryEnabled true if category is enabled
    * @param isVertical true in the case of vertical orientation
    * @return the sorted series array
    */
   private List<ISeries> sort(List<ISeries> seriesList, boolean isCategoryEnabled, boolean isVertical)
   {
      List<ISeries> sortedArray = new ArrayList<ISeries>();

      // gather the stacked series reversing the order of stack series
      int insertIndex = -1;
      for(int i = 0; i < seriesList.size(); i++)
      {
         if (isCategoryEnabled && ((Series)seriesList.get(i)).isValidStackSeries())
         {
            if (insertIndex == -1)
            {
               insertIndex = i;
            }
            else
            {
               sortedArray.add(insertIndex, seriesList.get(i));
               continue;
            }
         }
         sortedArray.add(seriesList.get(i));
      }

      // reverse the order of whole series in the case of vertical orientation
      if (isVertical)
      {
         Collections.reverse(sortedArray);
      }

      return sortedArray;
   }

   /**
    * Update the layout data.
    */
   public void updateLayoutData()
   {
      if (!visible)
      {
         setLayoutData(new ChartLayoutData(0, 0));
         return;
      }
      
      extendedInfoOffset = 0;

      int width = 0;
      int height = 0;

      ISeries[] seriesArray = sort(chart.getSeriesSet().getSeries());

      Rectangle r = chart.getClientArea();
      final int titleHeight = ((Composite)chart.getTitle()).getSize().y;
      final int cellHeight = Util.getExtentInGC(getFont(), "fgdl0").y;
      final int cellExtraWidth = extended ? (Util.getExtentInGC(getFont(), VALUE_PLACEHOLDER).x + MARGIN) * 4 : 0;
      
      if (position == SWT.RIGHT || position == SWT.LEFT)
      {
         int columns = 1;
         int yPosition = extended ? (cellHeight + MARGIN * 2) : MARGIN;
         int maxCellWidth = 0;

         for(ISeries series : seriesArray)
         {
            int textWidth = Util.getExtentInGC(getFont(), series.getName()).x;
            int cellWidth = textWidth + SYMBOL_WIDTH + MARGIN * 3;
            maxCellWidth = Math.max(maxCellWidth, cellWidth);
            if (extendedInfoOffset < cellWidth)
               extendedInfoOffset = cellWidth;
            if (yPosition + cellHeight < r.height - titleHeight || yPosition == MARGIN)
            {
               yPosition += cellHeight + MARGIN;
            }
            else
            {
               columns++;
               yPosition = cellHeight + MARGIN * 2;
            }
            cellBounds.put(series.getId(), new Rectangle(maxCellWidth * (columns - 1), 
                  yPosition - cellHeight - MARGIN, cellWidth + cellExtraWidth, cellHeight));
            height = Math.max(yPosition, height);
         }
         width = maxCellWidth * columns;
      }
      else if (position == SWT.TOP || position == SWT.BOTTOM)
      {
         int rows = 1;
         int xPosition = 0;
         final int topOffset = extended ? (cellHeight + MARGIN * 2) : MARGIN;

         for(ISeries series : seriesArray)
         {
            int textWidth = Util.getExtentInGC(getFont(), series.getName()).x;
            int cellWidth = textWidth + SYMBOL_WIDTH + MARGIN * 3;
            if ((!extended && (xPosition + cellWidth < r.width)) || (xPosition == 0))
            {
               xPosition += cellWidth;
            }
            else
            {
               rows++;
               xPosition = cellWidth;
            }
            cellBounds.put(series.getId(), new Rectangle(xPosition - cellWidth, (cellHeight + MARGIN) * (rows - 1) + topOffset, 
                  cellWidth + cellExtraWidth, cellHeight));
            width = Math.max(xPosition, width);
            if (extendedInfoOffset < cellWidth)
               extendedInfoOffset = cellWidth;
         }
         height = (cellHeight + MARGIN) * rows + MARGIN;
      }

      if (extended)
      {
         width += cellExtraWidth;
         height += cellHeight + MARGIN; 
         cellBounds.put(HEADER_ID, new Rectangle(0, MARGIN, cellExtraWidth, cellHeight));
      }
      
      setLayoutData(new ChartLayoutData(width, height));
   }

   /**
    * Draws the symbol of series.
    * 
    * @param gc the graphics context
    * @param series the series
    * @param r the rectangle to draw the symbol of series
    */
   protected void drawSymbol(GC gc, Series series, Rectangle r)
   {
      if (!visible)
      {
         return;
      }

      if (series instanceof ILineSeries)
      {
         gc.setBackground(((ILineSeries)series).getLineColor());
      }
      else if (series instanceof IBarSeries)
      {
         gc.setBackground(((IBarSeries)series).getBarColor());
      }
      int size = SYMBOL_WIDTH / 2;
      int x = r.x + size / 2;
      int y = (int)(r.y - size / 2d + r.height / 2d);
      gc.fillRectangle(x, y, size, size);
      
      gc.setForeground(DEFAULT_FOREGROUND);
      gc.drawRectangle(x, y, size, size);
   }
   
   /**
    * Draw extended info (min, max, average value)
    * 
    * @param gc
    * @param series
    * @param r
    */
   private void drawExtendedInfo(GC gc, Series series, Rectangle r)
   {
      int shift = Util.getExtentInGC(getFont(), VALUE_PLACEHOLDER).x;
      int x = r.x + extendedInfoOffset + MARGIN * 2;
      
      gc.drawText(DataFormatter.roundDecimalValue(series.getCurY(), 0.005, 3), x, r.y, true);
      x += shift;

      gc.drawText(DataFormatter.roundDecimalValue(series.getMinY(), 0.005, 3), x, r.y, true);
      x += shift;

      gc.drawText(DataFormatter.roundDecimalValue(series.getAvgY(), 0.005, 3), x, r.y, true);
      x += shift;

      gc.drawText(DataFormatter.roundDecimalValue(series.getMaxY(), 0.005, 3), x, r.y, true);
   }

   /*
    * @see PaintListener#paintControl(PaintEvent)
    */
   public void paintControl(PaintEvent e)
   {
      if (!visible)
      {
         return;
      }

      GC gc = e.gc;
      gc.setFont(getFont());
      ISeries[] seriesArray = chart.getSeriesSet().getSeries();
      if (seriesArray.length == 0)
      {
         return;
      }
      
      // Draw column headers
      if (extended)
      {
         gc.setBackground(getBackground());
         gc.setForeground(getForeground());
         gc.setFont(headerFont);
         
         final int shift = Util.getExtentInGC(getFont(), VALUE_PLACEHOLDER).x;
         
         Rectangle r = cellBounds.get(HEADER_ID);
         int x = r.x + extendedInfoOffset + MARGIN * 2;

         gc.drawText("Curr", x, r.y, true);
         x += shift;

         gc.drawText("Min", x, r.y, true);
         x += shift;

         gc.drawText("Avg", x, r.y, true);
         x += shift;

         gc.drawText("Max", x, r.y, true);
         
         gc.setFont(getFont());
      }

      // draw content
      for(int i = 0; i < seriesArray.length; i++)
      {
         // draw plot line, symbol etc
         Rectangle r = cellBounds.get(seriesArray[i].getId());
         drawSymbol(gc, (Series)seriesArray[i], new Rectangle(r.x + MARGIN, r.y + MARGIN, SYMBOL_WIDTH, r.height - MARGIN * 2));

         // draw plot id
         gc.setBackground(getBackground());
         gc.setForeground(getForeground());
         gc.drawText(seriesArray[i].getName(), r.x + SYMBOL_WIDTH + MARGIN * 2, r.y, true);
         
         if (extended)
         {
            drawExtendedInfo(gc, (Series)seriesArray[i], r);
         }
      }
   }
}
