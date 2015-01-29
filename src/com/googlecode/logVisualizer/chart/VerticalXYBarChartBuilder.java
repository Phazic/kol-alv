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

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.IntervalXYDataset;

import com.googlecode.logVisualizer.logData.LogDataHolder;

public abstract class VerticalXYBarChartBuilder extends AbstractChart {
    private final String xLable;

    private final String yLable;

    protected VerticalXYBarChartBuilder(
                                        final LogDataHolder logData, final String title,
                                        final String xLable, final String yLable,
                                        final boolean isIncludeLegend) {
        super(title, logData, isIncludeLegend);
        this.xLable = xLable;
        this.yLable = yLable;
        addChart();
    }

    protected abstract IntervalXYDataset createDataset();

    private JFreeChart createChart(
                                   final IntervalXYDataset dataset) {
        final JFreeChart chart = ChartFactory.createXYBarChart(getTitle(),
                                                               xLable,
                                                               false,
                                                               yLable,
                                                               dataset,
                                                               PlotOrientation.VERTICAL,
                                                               isIncludeLegend(),
                                                               true,
                                                               false);
        final XYPlot plot = (XYPlot) chart.getPlot();
        final NumberAxis yAxis = (NumberAxis) plot.getRangeAxis();

        double lastXValue = 0;
        if (dataset.getSeriesCount() > 0)
            lastXValue = dataset.getXValue(0, dataset.getItemCount(0) - 1);

        plot.setDomainAxis(new FixedZoomNumberAxis(lastXValue));
        plot.setNoDataMessage("No data available");
        plot.setBackgroundPaint(Color.white);
        plot.setRangeGridlinePaint(Color.black);
        plot.setRangeAxisLocation(AxisLocation.BOTTOM_OR_LEFT);
        setBarShadowVisible(chart, false);

        plot.getDomainAxis().setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        if (dataset.getSeriesCount() > 0)
            plot.getDomainAxis().setUpperBound(lastXValue);
        plot.getDomainAxis().setLowerBound(0);
        yAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        yAxis.setUpperMargin(0.1);

        return chart;
    }

    @Override
    protected ChartPanel createChartPanel() {
        return new ChartPanel(createChart(createDataset()), false);
    }
}
