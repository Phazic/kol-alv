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

import java.util.List;

import org.jfree.chart.ChartPanel;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;

import com.googlecode.logVisualizer.logData.LogDataHolder;
import com.googlecode.logVisualizer.logData.logSummary.AreaStatgains;

public final class StatsPerAreaBarChart extends HorizontalStackedBarChartBuilder {
    public StatsPerAreaBarChart(
                                final LogDataHolder logData) {
        super(logData, "Stats gained per area", "Area", "Stats gained", true);
    }

    @Override
    protected CategoryDataset createDataset() {
        final DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        // Create sorted area statgains list.
        final List<AreaStatgains> areas = getLogData().getLogSummary().getAreasStatgains();

        // Add the values to the chart dataset.
        for (final AreaStatgains as : areas) {
            if (as.getStatgain().getTotalStatgain() > 0) {
                dataset.addValue(as.getStatgain().mus, "Muscle", as.getAreaName());
                dataset.addValue(as.getStatgain().myst, "Mysticality", as.getAreaName());
                dataset.addValue(as.getStatgain().mox, "Moxie", as.getAreaName());
            }

            // The chart looks ugly with too many entries.
            if (dataset.getColumnCount() >= 40)
                break;
        }

        return dataset;
    }

    @Override
    protected void addChartPanelListeners(
                                          final ChartPanel cp) {
        cp.addChartMouseListener(new AreaListChartMouseEventListener(getLogData()));
    }
}
