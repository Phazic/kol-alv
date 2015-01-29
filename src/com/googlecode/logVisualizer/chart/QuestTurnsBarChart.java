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

import static com.googlecode.logVisualizer.chart.QuestTurnsBarChart.QuestAreas.*;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;

import com.googlecode.logVisualizer.logData.LogDataHolder;

public final class QuestTurnsBarChart extends HorizontalBarChartBuilder {
    private static final String QUEST_TURNS_STRING = "Quest turns";

    public QuestTurnsBarChart(
                              final LogDataHolder logData) {
        super(logData, QUEST_TURNS_STRING, "Area", "Turns used", false);
    }

    @Override
    protected ChartPanel createChartPanel() {
        final ChartPanel panel = super.createChartPanel();
        final JFreeChart chart = panel.getChart();
        final CategoryDataset dataset = ((CategoryPlot) chart.getPlot()).getDataset();
        final NumberAxis numberAxis = (NumberAxis) ((CategoryPlot) chart.getPlot()).getRangeAxis();

        int maxTurncount = 0;
        for (int i = 0; i < dataset.getColumnCount(); i++)
            if (dataset.getValue(0, i).intValue() > maxTurncount)
                maxTurncount = dataset.getValue(0, i).intValue();

        // Use hard ranges to make comparison between different logs easier, but
        // don't cut off the bars if some are too long.
        numberAxis.setAutoRange(false);
        numberAxis.setLowerBound(0);
        numberAxis.setUpperBound(maxTurncount <= 125 ? 125 : maxTurncount + 10);

        return panel;
    }

    @Override
    protected CategoryDataset createDataset() {
        final DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        addDatasetValue(MOSQUITO,
                        getLogData().getLogSummary().getQuestTurncounts().mosquitoQuestTurns,
                        dataset);

        addDatasetValue(TEMPLE,
                        getLogData().getLogSummary().getQuestTurncounts().templeOpeningTurns,
                        dataset);

        addDatasetValue(TAVERN,
                        getLogData().getLogSummary().getQuestTurncounts().tavernQuestTurns,
                        dataset);

        addDatasetValue(BAT,
                        getLogData().getLogSummary().getQuestTurncounts().batQuestTurns,
                        dataset);

        addDatasetValue(KNOB,
                        getLogData().getLogSummary().getQuestTurncounts().knobQuestTurns,
                        dataset);

        addDatasetValue(FRIARS,
                        getLogData().getLogSummary().getQuestTurncounts().friarsQuestTurns,
                        dataset);

        addDatasetValue(PANDAMONIUM,
                        getLogData().getLogSummary().getQuestTurncounts().pandamoniumQuestTurns,
                        dataset);

        addDatasetValue(CYRPT,
                        getLogData().getLogSummary().getQuestTurncounts().cyrptQuestTurns,
                        dataset);

        addDatasetValue(TRAPZOR,
                        getLogData().getLogSummary().getQuestTurncounts().trapzorQuestTurns,
                        dataset);

        addDatasetValue(CHASM,
                        getLogData().getLogSummary().getQuestTurncounts().chasmQuestTurns,
                        dataset);

        addDatasetValue(AIRSHIP,
                        getLogData().getLogSummary().getQuestTurncounts().airshipQuestTurns,
                        dataset);

        addDatasetValue(CASTLE,
                        getLogData().getLogSummary().getQuestTurncounts().castleQuestTurns,
                        dataset);

        addDatasetValue(SPOOKYRAVENFIRST,
                        getLogData().getLogSummary().getQuestTurncounts().spookyravenFirstFloor,
                        dataset);
        
        addDatasetValue(SPOOKYRAVENSECOND,
                getLogData().getLogSummary().getQuestTurncounts().spookyravenSecondFloor,
                dataset);

        addDatasetValue(PIRATE,
                        getLogData().getLogSummary().getQuestTurncounts().pirateQuestTurns,
                        dataset);

        addDatasetValue(BLACK_FOREST,
                        getLogData().getLogSummary().getQuestTurncounts().blackForrestQuestTurns,
                        dataset);

        addDatasetValue(DESERT_OASIS,
                        getLogData().getLogSummary().getQuestTurncounts().desertOasisQuestTurns,
                        dataset);

        addDatasetValue(SPOOKYRAVEN,
                        getLogData().getLogSummary().getQuestTurncounts().spookyravenQuestTurns,
                        dataset);

        addDatasetValue(HIDDEN_CITY,
                        getLogData().getLogSummary().getQuestTurncounts().templeCityQuestTurns,
                        dataset);

        addDatasetValue(PALINDOME,
                        getLogData().getLogSummary().getQuestTurncounts().palindomeQuestTurns,
                        dataset);

        addDatasetValue(PYRAMID,
                        getLogData().getLogSummary().getQuestTurncounts().pyramidQuestTurns,
                        dataset);

        addDatasetValue(STARTING_WAR,
                        getLogData().getLogSummary().getQuestTurncounts().warIslandOpeningTurns,
                        dataset);

        addDatasetValue(WAR,
                        getLogData().getLogSummary().getQuestTurncounts().warIslandQuestTurns,
                        dataset);

        addDatasetValue(DOD,
                        getLogData().getLogSummary().getQuestTurncounts().dodQuestTurns,
                        dataset);

        addDatasetValue(DAILY_DUNGEON,
                        getLogData().getLogSummary().getQuestTurncounts().dailyDungeonTurns,
                        dataset);

        return dataset;
    }

    /**
     * A helper method to make adding data to the dataset easier.
     * 
     * @param area
     *            The enum related to the given turncount.
     * @param turncount
     *            The number of turns a certain quest actually took.
     * @param dataset
     *            The dataset to which the should be added to.
     */
    private static void addDatasetValue(
                                        final QuestAreas area, final int turncount,
                                        final DefaultCategoryDataset dataset) {
        dataset.addValue(turncount, QUEST_TURNS_STRING, area.getDescription());
    }

    /**
     * An enum for all the names of the quest areas.
     */
    static enum QuestAreas {
        MOSQUITO("Mosquito Larva"),
        TEMPLE("Opening the Hidden Temple"),
        TAVERN("Tavern quest"),
        BAT("Bat quest"),
        KNOB("Cobb's Knob quest"),
        FRIARS("Friars' quest"),
        PANDAMONIUM("Pandamonium quest"),
        CYRPT("Defiled Cyrpt quest"),
        TRAPZOR("Trapzor quest"),
        CHASM("Orc Chasm quest"),
        AIRSHIP("Airship"),
        CASTLE("Giant's Castle"),
        SPOOKYRAVENFIRST("Spookyraven First Floor"),
        SPOOKYRAVENSECOND("Spookyraven Second Floor"),
        PIRATE("Pirate quest"),
        BLACK_FOREST("Black Forest quest"),
        DESERT_OASIS("Desert Oasis quest"),
        SPOOKYRAVEN("Spookyraven quest"),
        HIDDEN_CITY("Hidden City quest"),
        PALINDOME("Palindome quest"),
        PYRAMID("Pyramid quest"),
        STARTING_WAR("Starting the War"),
        WAR("War Island quest"),
        DOD("DoD quest"),
        DAILY_DUNGEON("Daily Dungeon");

        private final String description;

        private QuestAreas(
                           final String description) {
            this.description = description;
        }

        String getDescription() {
            return description;
        }
    }
}
