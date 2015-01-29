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

import java.awt.BasicStroke;
import java.awt.Color;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.RectangleAnchor;
import org.jfree.ui.TextAnchor;

import com.googlecode.logVisualizer.logData.LogDataHolder;
import com.googlecode.logVisualizer.logData.Statgain;
import com.googlecode.logVisualizer.logData.consumables.Consumable;
import com.googlecode.logVisualizer.logData.turn.SingleTurn;
import com.googlecode.logVisualizer.logData.turn.Turn;
import com.googlecode.logVisualizer.logData.turn.TurnInterval;
import com.googlecode.logVisualizer.logData.turn.turnAction.DayChange;

public final class StatDevelopmentLineChart extends LineChartBuilder {
    public StatDevelopmentLineChart(
                                    final LogDataHolder logData) {
        super(logData, "Stat development", "Turn number", "Stats reached", true);
    }

    @Override
    protected ChartPanel createChartPanel() {
        final ChartPanel panel = super.createChartPanel();
        final XYPlot plot = (XYPlot) panel.getChart().getPlot();

        for (final DayChange dc : getLogData().getDayChanges()) {
            final ValueMarker day = new ValueMarker(dc.getTurnNumber());
            day.setLabel("Day " + dc.getDayNumber());
            day.setLabelAnchor(RectangleAnchor.TOP_RIGHT);
            day.setLabelTextAnchor(TextAnchor.TOP_LEFT);
            day.setStroke(new BasicStroke(2));
            day.setPaint(new Color(175, 175, 255));
            plot.addDomainMarker(day);
        }

        return panel;
    }

    @Override
    protected XYDataset createDataset() {
        final XYSeriesCollection datasets = new XYSeriesCollection();
        final XYSeries muscleDataset = new XYSeries("Muscle", false);
        final XYSeries mystDataset = new XYSeries("Mysticality", false);
        final XYSeries moxieDataset = new XYSeries("Moxie", false);

        Statgain stats = Statgain.NO_STATS;
        if (getLogData().isDetailedLog())
            for (final SingleTurn si : getLogData().getTurnsSpent())
                stats = addStatValues(muscleDataset, mystDataset, moxieDataset, stats, si);
        else
            for (final TurnInterval ti : getLogData().getTurnIntervalsSpent())
                stats = addStatValues(muscleDataset, mystDataset, moxieDataset, stats, ti);

        // If the log actually held any statgain data, add it to the collection.
        if (!stats.isAllStatsZero()) {
            datasets.addSeries(muscleDataset);
            datasets.addSeries(mystDataset);
            datasets.addSeries(moxieDataset);
        }

        return datasets;
    }

    private Statgain addStatValues(
                                   final XYSeries muscleDataset, final XYSeries mystDataset,
                                   final XYSeries moxieDataset, Statgain stats, final Turn t) {
        // Add statgain of the current turn interval to the total statgains.
        stats = stats.addStats(t.getStatGain());
        for (final Consumable c : t.getConsumablesUsed())
            stats = stats.addStats(c.getStatGain());

        // Add current total statgains to the datasets.
        muscleDataset.add(t.getTurnNumber(), (int) Math.sqrt(stats.mus));
        mystDataset.add(t.getTurnNumber(), (int) Math.sqrt(stats.myst));
        moxieDataset.add(t.getTurnNumber(), (int) Math.sqrt(stats.mox));

        return stats;
    }

    @Override
    protected void addChartPanelListeners(
                                          final ChartPanel cp) {
        cp.addChartMouseListener(new StatDevelopmentChartMouseEventListener(getLogData()));
    }
}