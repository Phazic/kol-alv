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

import java.awt.BorderLayout;

import javax.swing.JPanel;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;

import com.googlecode.logVisualizer.logData.LogDataHolder;

/**
 * A class to handle some basic structures which all charts have in common.
 */
public abstract class AbstractChart extends JPanel {
    private final String title;

    private final boolean isIncludeLegend;

    private final LogDataHolder logData;

    protected AbstractChart(
                            final String title, final LogDataHolder logData,
                            final boolean isIncludeLegend) {
        super(new BorderLayout());
        this.title = title;
        this.logData = logData;
        this.isIncludeLegend = isIncludeLegend;
    }

    /**
     * Adds the chart to this Panel.
     */
    protected void addChart() {
        final ChartPanel cp = createChartPanel();
        addChartPanelListeners(cp);
        add(cp, BorderLayout.CENTER);
    }

    /**
     * Empty method body. Override in implementing classes to add listeners of
     * your choice to the finished chart panel.
     */
    protected void addChartPanelListeners(
                                          final ChartPanel cp) {}

    /**
     * @return The finished chart.
     */
    protected abstract ChartPanel createChartPanel();

    /**
     * @return The title of the chart.
     */
    protected String getTitle() {
        return title;
    }

    /**
     * @return A flag for whether a legend should be shown in the chart.
     */
    protected boolean isIncludeLegend() {
        return isIncludeLegend;
    }

    /**
     * @return The ascension log from whose data the chart is created.
     */
    protected LogDataHolder getLogData() {
        return logData;
    }

    /**
     * Sets the visibility of bar shadows.
     * <p>
     * JFreeChart 1.0.11 changed the <b>default</b> look by painting shadows for
     * bars. To revert back to the old look, you can disable the shadows with
     * this method.
     * 
     * @param chart
     *            The chart for which the shadow visibility should be set.
     * @param isVisibile
     *            The flag for the shadow visibility.
     */
    public static void setBarShadowVisible(
                                           final JFreeChart chart, final boolean isVisibile) {
        if (chart != null)
            if (chart.getPlot() instanceof CategoryPlot) {
                final CategoryItemRenderer cir = ((CategoryPlot) chart.getPlot()).getRenderer();
                if (cir instanceof BarRenderer)
                    ((BarRenderer) cir).setShadowVisible(isVisibile);
            } else if (chart.getPlot() instanceof XYPlot) {
                final XYItemRenderer xyir = ((XYPlot) chart.getPlot()).getRenderer();
                if (xyir instanceof XYBarRenderer)
                    ((XYBarRenderer) xyir).setShadowVisible(isVisibile);
            }
    }
}
