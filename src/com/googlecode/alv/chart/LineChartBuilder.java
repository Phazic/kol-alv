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

import java.awt.BasicStroke;
import java.awt.Color;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.RectangleInsets;

import com.googlecode.alv.logData.LogDataHolder;

public abstract class LineChartBuilder extends AbstractChart {
    private final String xLable;

    private final String yLable;

    protected LineChartBuilder(
                               final LogDataHolder logData, final String title,
                               final String xLable, final String yLable,
                               final boolean isIncludeLegend) {
        super(title, logData, isIncludeLegend);
        this.xLable = xLable;
        this.yLable = yLable;
        addChart();
    }

    protected abstract XYDataset createDataset();

    private JFreeChart createChart(
                                   final XYDataset dataset) {
        final JFreeChart chart = ChartFactory.createXYLineChart(getTitle(),
                                                                xLable,
                                                                yLable,
                                                                dataset,
                                                                PlotOrientation.VERTICAL,
                                                                isIncludeLegend(),
                                                                true,
                                                                false);
        final XYPlot plot = (XYPlot) chart.getPlot();

        double lastXValue = 0;
        if (dataset.getSeriesCount() > 0)
            lastXValue = dataset.getXValue(0, dataset.getItemCount(0) - 1);

        plot.setDomainAxis(new FixedZoomNumberAxis(lastXValue));
        plot.getDomainAxis().setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        plot.setAxisOffset(new RectangleInsets(5.0, 5.0, 5.0, 5.0));
        plot.setBackgroundPaint(Color.white);
        plot.setDomainGridlinePaint(Color.black);
        plot.setRangeGridlinePaint(Color.black);
        plot.setNoDataMessage("No data available");
        if (dataset.getSeriesCount() > 0)
            ((NumberAxis) plot.getDomainAxis()).setUpperBound(lastXValue);
        ((NumberAxis) plot.getRangeAxis()).setStandardTickUnits(NumberAxis.createIntegerTickUnits());

        final XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        for (int i = 0; i < dataset.getSeriesCount(); i++) {
            renderer.setSeriesShapesVisible(i, false);
            renderer.setSeriesStroke(i, new BasicStroke(2));
        }
        renderer.setBaseToolTipGenerator(new StandardXYToolTipGenerator());
        plot.setRenderer(renderer);

        return chart;
    }

    @Override
    protected ChartPanel createChartPanel() {
        return new ChartPanel(createChart(createDataset()), false);
    }
}
