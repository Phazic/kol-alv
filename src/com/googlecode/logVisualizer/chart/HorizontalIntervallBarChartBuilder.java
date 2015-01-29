/* Copyright (c) 2008-2011, developers of the Ascension Log Visualizer
 *
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or
 * sell copies of the Software, and to permit persons to whom
 * the Software is furnished to do so, subject to the following
 * conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 */

package com.googlecode.logVisualizer.chart;

import java.awt.Color;
import java.text.NumberFormat;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.ItemLabelAnchor;
import org.jfree.chart.labels.ItemLabelPosition;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.labels.StandardCategoryToolTipGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.LayeredBarRenderer;
import org.jfree.data.category.CategoryDataset;
import org.jfree.ui.TextAnchor;
import org.jfree.util.SortOrder;

import com.googlecode.logVisualizer.logData.LogDataHolder;

//TODO Class not used. Should it be deleted?
public abstract class HorizontalIntervallBarChartBuilder extends AbstractChart {
    private final String xLable;

    private final String yLable;

    protected HorizontalIntervallBarChartBuilder(
                                                 final LogDataHolder logData, final String title,
                                                 final String xLable, final String yLable,
                                                 final boolean isIncludeLegend) {
        super(title, logData, isIncludeLegend);
        this.xLable = xLable;
        this.yLable = yLable;
        addChart();
    }

    protected abstract CategoryDataset createDataset();

    private JFreeChart createChart(
                                   final CategoryDataset dataset) {
        final JFreeChart chart = ChartFactory.createBarChart(getTitle(),
                                                             xLable,
                                                             yLable,
                                                             dataset,
                                                             PlotOrientation.HORIZONTAL,
                                                             isIncludeLegend(),
                                                             true,
                                                             false);
        final CategoryPlot plot = (CategoryPlot) chart.getPlot();
        final CategoryAxis categoryAxis = plot.getDomainAxis();
        final LayeredBarRenderer renderer = new LayeredBarRenderer();

        plot.setBackgroundPaint(Color.white);
        plot.setDomainGridlinesVisible(false);
        plot.setRangeGridlinePaint(Color.black);
        setBarShadowVisible(chart, false);

        plot.getRangeAxis().setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        plot.getRangeAxis().setLowerBound(-35);
        plot.getRangeAxis().setUpperBound(35);

        renderer.setDrawBarOutline(false);
        renderer.setSeriesPaint(0, Color.blue);
        renderer.setSeriesPaint(1, Color.green);
        renderer.setBaseItemLabelsVisible(true);
        renderer.setBaseItemLabelGenerator(new StandardCategoryItemLabelGenerator());
        renderer.setSeriesPositiveItemLabelPosition(0,
                                                    new ItemLabelPosition(ItemLabelAnchor.OUTSIDE3,
                                                                          TextAnchor.CENTER));
        renderer.setSeriesPositiveItemLabelPosition(1,
                                                    new ItemLabelPosition(ItemLabelAnchor.OUTSIDE2,
                                                                          TextAnchor.CENTER));
        renderer.setSeriesNegativeItemLabelPosition(0,
                                                    new ItemLabelPosition(ItemLabelAnchor.OUTSIDE9,
                                                                          TextAnchor.CENTER));
        renderer.setSeriesNegativeItemLabelPosition(1,
                                                    new ItemLabelPosition(ItemLabelAnchor.OUTSIDE10,
                                                                          TextAnchor.CENTER));
        renderer.setItemLabelAnchorOffset(9.0);
        renderer.setBaseToolTipGenerator(new StandardCategoryToolTipGenerator("{1}, {2}",
                                                                              NumberFormat.getInstance()));
        plot.setRenderer(renderer);
        plot.setRowRenderingOrder(SortOrder.DESCENDING);

        categoryAxis.setCategoryMargin(0.15);
        categoryAxis.setUpperMargin(0.0175);
        categoryAxis.setLowerMargin(0.0175);

        return chart;
    }

    @Override
    protected ChartPanel createChartPanel() {
        return new ChartPanel(createChart(createDataset()), false);
    }
}