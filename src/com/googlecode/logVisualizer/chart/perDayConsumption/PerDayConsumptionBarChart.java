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

package com.googlecode.logVisualizer.chart.perDayConsumption;

import com.googlecode.logVisualizer.logData.consumables.Consumable;
import com.googlecode.logVisualizer.logData.logSummary.ConsumptionSummary.ConsumptionDayStats;
import com.googlecode.logVisualizer.util.dataTables.DataTablesHandler;

final class PerDayConsumptionBarChart extends ConsumptionBarChartBuilder {
    private final ConsumptionDayStats consumptionStats;

    PerDayConsumptionBarChart(
                              final ConsumptionDayStats consumptionStats) {
        super("Organ hit per consumable", "Consumable version", "Organ hit per consumable", false);
        this.consumptionStats = consumptionStats;
        addChart();
    }

    @Override
    protected ConsumptionDataset createDataset() {
        final ConsumptionDataset dataset = new ConsumptionDataset(consumptionStats);

        for (final Consumable c : consumptionStats.getConsumablesUsed())
            if (DataTablesHandler.HANDLER.getFullnessHit(c.getName()) > 0
                || DataTablesHandler.HANDLER.getDrunkennessHit(c.getName()) > 0
                || DataTablesHandler.HANDLER.getSpleenHit(c.getName()) > 0)
                dataset.addConsumable(c);

        dataset.addLeftoverOrganHits(consumptionStats);

        return dataset;
    }
}
