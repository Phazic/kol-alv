/* Copyright (c) 2008-2020, developers of the Ascension Log Visualizer
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

package com.googlecode.alv.chart.perDayConsumption;

import java.awt.Color;
import java.awt.Paint;

import org.jfree.chart.ChartColor;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.StackedBarRenderer;

import com.googlecode.alv.chart.AbstractChart;
import com.googlecode.alv.logData.consumables.Consumable;

abstract class ConsumptionBarChartBuilder extends AbstractChart {
    private final static Paint[] DEFAULT_COLORS = ChartColor.createDefaultPaintArray();

    private final String xLable;

    private final String yLable;

    protected ConsumptionBarChartBuilder(
                                         final String title, final String xLable,
                                         final String yLable, final boolean includeLegend) {
        super(title, null, includeLegend);
        this.xLable = xLable;
        this.yLable = yLable;
    }

    protected abstract ConsumptionDataset createDataset();

    private JFreeChart createChart(
                                   final ConsumptionDataset dataset) {
        final JFreeChart chart = ChartFactory.createStackedBarChart(getTitle(),
                                                                    xLable,
                                                                    yLable,
                                                                    dataset,
                                                                    PlotOrientation.VERTICAL,
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
        setStackColors(dataset, renderer);

        renderer.setDrawBarOutline(false);
        renderer.setBaseItemLabelsVisible(true);
        renderer.setBaseItemLabelGenerator(new ConsumptionLableGenerator());
        renderer.setBaseToolTipGenerator(new ConsumptionToolTipGenerator());

        categoryAxis.setCategoryMargin(0.07);
        categoryAxis.setUpperMargin(0.01);
        categoryAxis.setLowerMargin(0.01);
        numberAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        numberAxis.setUpperMargin(0.1);

        return chart;
    }

    private void setStackColors(
                                final ConsumptionDataset dataset, final StackedBarRenderer renderer) {
        int foodCounter = 0;
        int boozeCounter = 0;
        int spleenCounter = 0;

        for (int i = 0; i < dataset.getRowCount(); i++) {
            final Consumable c = dataset.getConsumable(dataset.getRowKey(i).toString());
            if (c != null)
                switch (c.getConsumableVersion()) {
                    case FOOD:
                        renderer.setSeriesPaint(i, DEFAULT_COLORS[foodCounter]);
                        foodCounter++;
                        break;
                    case BOOZE:
                        renderer.setSeriesPaint(i, DEFAULT_COLORS[boozeCounter]);
                        boozeCounter++;
                        break;
                    default:
                        renderer.setSeriesPaint(i, DEFAULT_COLORS[spleenCounter]);
                        spleenCounter++;
                }
            else
                renderer.setSeriesPaint(i, ChartColor.LIGHT_GRAY);
        }
    }

    @Override
    protected ChartPanel createChartPanel() {
        return new ChartPanel(createChart(createDataset()), false);
    }
}
