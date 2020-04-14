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

package com.googlecode.alv.chart;

import java.awt.Color;
import java.text.NumberFormat;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.labels.StandardCategoryToolTipGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.StackedBarRenderer;
import org.jfree.data.category.CategoryDataset;

import com.googlecode.alv.logData.LogDataHolder;

public abstract class HorizontalStackedBarChartBuilder extends AbstractChart {
    private final String xLable;

    private final String yLable;

    protected HorizontalStackedBarChartBuilder(
                                               final LogDataHolder logData, final String title,
                                               final String xLable, final String yLable,
                                               final boolean includeLegend) {
        super(title, logData, includeLegend);
        this.xLable = xLable;
        this.yLable = yLable;
        addChart();
    }

    protected abstract CategoryDataset createDataset();

    private JFreeChart createChart(
                                   final CategoryDataset dataset) {
        final JFreeChart chart = ChartFactory.createStackedBarChart(getTitle(),
                                                                    xLable,
                                                                    yLable,
                                                                    dataset,
                                                                    PlotOrientation.HORIZONTAL,
                                                                    isIncludeLegend(),
                                                                    true,
                                                                    false);
        final CategoryPlot plot = (CategoryPlot) chart.getPlot();
        final StackedBarRenderer renderer = (StackedBarRenderer) plot.getRenderer();
        final CategoryAxis categoryAxis = plot.getDomainAxis();
        final NumberAxis numberAxis = (NumberAxis) plot.getRangeAxis();

        plot.setNoDataMessage("No data available");
        plot.setBackgroundPaint(Color.white);
        plot.setRangeGridlinePaint(Color.black);
        plot.setRangeAxisLocation(AxisLocation.BOTTOM_OR_LEFT);
        setBarShadowVisible(chart, false);

        renderer.setDrawBarOutline(false);
        renderer.setBaseItemLabelsVisible(true);
        renderer.setBaseItemLabelGenerator(new StandardCategoryItemLabelGenerator());
        renderer.setBaseToolTipGenerator(new StandardCategoryToolTipGenerator("{1}, {2}",
                                                                              NumberFormat.getInstance()));

        categoryAxis.setCategoryMargin(0.02);
        categoryAxis.setUpperMargin(0.02);
        categoryAxis.setLowerMargin(0.02);
        numberAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        numberAxis.setUpperMargin(0.1);

        return chart;
    }

    @Override
    protected ChartPanel createChartPanel() {
        return new ChartPanel(createChart(createDataset()), false);
    }
}
