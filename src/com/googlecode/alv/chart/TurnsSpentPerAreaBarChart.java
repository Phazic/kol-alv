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

package com.googlecode.alv.chart;

import org.jfree.chart.ChartPanel;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;

import com.googlecode.alv.logData.LogDataHolder;
import com.googlecode.alv.util.DataNumberPair;

public final class TurnsSpentPerAreaBarChart extends HorizontalBarChartBuilder {

    public TurnsSpentPerAreaBarChart(
                                     final LogDataHolder logData) {
        super(logData, "Turns spent per area", "Area", "Turns spent", false);
    }

    @Override
    protected CategoryDataset createDataset() {
        final DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        final String seriesName = "Turns spent per area";

        // Add areas to the dataset. They are sorted from most visited to least
        // visited.
        for (final DataNumberPair<String> dn : getLogData().getLogSummary().getTurnsPerArea()) {
            dataset.addValue(dn.getNumber(), seriesName, dn.getData());

            // The chart isn't readable anymore with too many entries
            if (dataset.getColumnCount() > 45)
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
