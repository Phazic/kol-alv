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

import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;

import com.googlecode.alv.logData.LogDataHolder;

public final class MPGainsBarChart extends HorizontalBarChartBuilder {
    public MPGainsBarChart(
                           final LogDataHolder logData) {
        super(logData, "MP gains", "Category", "MP gain", false);
    }

    @Override
    protected CategoryDataset createDataset() {
        final DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        final String seriesName = "MP gains";

        dataset.addValue(getLogData().getLogSummary().getTotalMPGains().encounterMPGain,
                         seriesName,
                         "Inside Encounters");
        dataset.addValue(getLogData().getLogSummary().getTotalMPGains().starfishMPGain,
                         seriesName,
                         "Starfish Familiars");
        dataset.addValue(getLogData().getLogSummary().getTotalMPGains().restingMPGain,
                         seriesName,
                         "Resting");
        dataset.addValue(getLogData().getLogSummary().getTotalMPGains().outOfEncounterMPGain,
                         seriesName,
                         "Outside Encounters");
        dataset.addValue(getLogData().getLogSummary().getTotalMPGains().consumableMPGain,
                         seriesName,
                         "Consumables");

        return dataset;
    }
}
