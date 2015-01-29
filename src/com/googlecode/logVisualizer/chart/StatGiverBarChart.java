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

import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;

import com.googlecode.logVisualizer.logData.LogDataHolder;
import com.googlecode.logVisualizer.logData.Statgain;

public final class StatGiverBarChart extends HorizontalStackedBarChartBuilder {
    public StatGiverBarChart(
                             final LogDataHolder logData) {
        super(logData, "Stat gains from ...", "Stat giver", "Stats gained", true);
    }

    @Override
    protected CategoryDataset createDataset() {
        final DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        addValue(dataset, getLogData().getLogSummary().getCombatsStatgains(), "Combats");
        addValue(dataset, getLogData().getLogSummary().getNoncombatsStatgains(), "Noncombats");
        addValue(dataset, getLogData().getLogSummary().getOthersStatgains(), "Others");
        addValue(dataset, getLogData().getLogSummary().getFoodConsumablesStatgains(), "Food");
        addValue(dataset, getLogData().getLogSummary().getBoozeConsumablesStatgains(), "Booze");
        addValue(dataset, getLogData().getLogSummary().getUsedConsumablesStatgains(), "Using");

        return dataset;
    }

    private void addValue(
                          final DefaultCategoryDataset dataset, final Statgain stats,
                          final String name) {
        dataset.addValue(stats.mus, "Muscle", name);
        dataset.addValue(stats.myst, "Mysticality", name);
        dataset.addValue(stats.mox, "Moxie", name);
    }
}
