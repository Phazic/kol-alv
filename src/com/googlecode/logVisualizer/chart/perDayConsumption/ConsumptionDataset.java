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

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Map;

import org.jfree.data.category.DefaultCategoryDataset;

import com.googlecode.logVisualizer.logData.consumables.Consumable;
import com.googlecode.logVisualizer.logData.logSummary.ConsumptionSummary.ConsumptionDayStats;
import com.googlecode.logVisualizer.util.dataTables.DataTablesHandler;
import com.googlecode.logVisualizer.util.Maps;

final class ConsumptionDataset extends DefaultCategoryDataset {
    // NumberFormat isn't thread-safe, but from what I could gather, as long as
    // one doesn't change the state of a NumberFormat object and only calls the
    // format() methods, everything should be OK.
    private final NumberFormat FORMATTER = new DecimalFormat("#0.00");

    private final Map<String, Consumable> consumables = Maps.newHashMap();

    private final String foodCategoryString;

    private final String boozeCategoryString;

    private final String spleenCategoryString;

    ConsumptionDataset(
                       final ConsumptionDayStats consumptionStats) {
        super();

        foodCategoryString = "Food; "
                             + consumptionStats.getTotalTurnsFromFood()
                             + " adv/"
                             + consumptionStats.getTotalFullnessHit()
                             + " full ("
                             + FORMATTER.format(consumptionStats.getTotalTurnsFromFood() * 1.0
                                                / consumptionStats.getTotalFullnessHit()) + " avg)";
        boozeCategoryString = "Booze; "
                              + consumptionStats.getTotalTurnsFromBooze()
                              + " adv/"
                              + consumptionStats.getTotalDrunkennessHit()
                              + " drunk ("
                              + FORMATTER.format(consumptionStats.getTotalTurnsFromBooze() * 1.0
                                                 / consumptionStats.getTotalDrunkennessHit())
                              + " avg)";
        spleenCategoryString = "Spleen; "
                               + consumptionStats.getTotalTurnsFromSpleen()
                               + " adv/"
                               + consumptionStats.getTotalSpleenHit()
                               + " spleen ("
                               + FORMATTER.format(consumptionStats.getTotalTurnsFromSpleen() * 1.0
                                                  / consumptionStats.getTotalSpleenHit()) + " avg)";

        // Dummy values added and removed again to establish the same category
        // order in all charts. There may possibly be a simpler way to do it.
        addValue(0, "", foodCategoryString);
        addValue(0, "", boozeCategoryString);
        addValue(0, "", spleenCategoryString);
        removeRow("");
    }

    void addConsumable(
                       final Consumable c) {
        consumables.put(c.getName(), c);

        switch (c.getConsumableVersion()) {
            case FOOD:
                addValue(DataTablesHandler.HANDLER.getFullnessHit(c.getName()) * c.getAmount(),
                         c.getName(),
                         foodCategoryString);
                break;
            case BOOZE:
                addValue(DataTablesHandler.HANDLER.getDrunkennessHit(c.getName()) * c.getAmount(),
                         c.getName(),
                         boozeCategoryString);
                break;
            default:
                addValue(DataTablesHandler.HANDLER.getSpleenHit(c.getName()) * c.getAmount(),
                         c.getName(),
                         spleenCategoryString);
        }
    }

    void addLeftoverOrganHits(
                              final ConsumptionDayStats consumptionStats) {
        if (consumptionStats.getTotalFullnessHit() < 15)
            addValue(15 - consumptionStats.getTotalFullnessHit(), "Nothing", foodCategoryString);
        if (consumptionStats.getTotalDrunkennessHit() < 15)
            addValue(15 - consumptionStats.getTotalDrunkennessHit(), "Nothing", boozeCategoryString);
        if (consumptionStats.getTotalSpleenHit() < 15)
            addValue(15 - consumptionStats.getTotalSpleenHit(), "Nothing", spleenCategoryString);
    }

    Consumable getConsumable(
                             final String consumableName) {
        return consumables.get(consumableName);
    }
}
