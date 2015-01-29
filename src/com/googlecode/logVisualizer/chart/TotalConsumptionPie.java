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

import java.awt.Dimension;
import java.awt.Paint;

import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.jfree.chart.ChartColor;
import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.entity.PieSectionEntity;
import org.jfree.chart.plot.PiePlot;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.general.PieDataset;

import com.googlecode.logVisualizer.logData.LogDataHolder;
import com.googlecode.logVisualizer.logData.consumables.Consumable;

public final class TotalConsumptionPie extends PieChartBuilder {
    private final static Paint[] DEFAULT_COLORS = ChartColor.createDefaultPaintArray();

    public TotalConsumptionPie(
                               final LogDataHolder logData) {
        super("Turns got from ...", logData, false);
    }

    @Override
    protected ChartPanel createChartPanel() {
        final ChartPanel panel = super.createChartPanel();
        final PiePlot plot = (PiePlot) panel.getChart().getPlot();

        plot.setSectionPaint("Eating", DEFAULT_COLORS[0]);
        plot.setSectionPaint("Drinking", DEFAULT_COLORS[1]);
        plot.setSectionPaint("Other", DEFAULT_COLORS[2]);
        plot.setSectionPaint("Rollover", DEFAULT_COLORS[3]);

        return panel;
    }

    @Override
    protected PieDataset createDataset() {
        final DefaultPieDataset dataset = new DefaultPieDataset();

        dataset.setValue("Eating", getLogData().getLogSummary().getTotalTurnsFromFood());
        dataset.setValue("Drinking", getLogData().getLogSummary().getTotalTurnsFromBooze());
        dataset.setValue("Other", getLogData().getLogSummary().getTotalTurnsFromOther());
        dataset.setValue("Rollover", getLogData().getLogSummary().getTotalTurnsFromRollover());

        return dataset;
    }

    @Override
    protected void addChartPanelListeners(
                                          final ChartPanel cp) {
        cp.addChartMouseListener(new TotalConsumptionChartMouseEventListener());
    }

    private final class TotalConsumptionChartMouseEventListener implements ChartMouseListener {
        public void chartMouseMoved(
                                    final ChartMouseEvent e) {}

        public void chartMouseClicked(
                                      final ChartMouseEvent e) {
            if (e.getEntity() instanceof PieSectionEntity) {
                final PieSectionEntity entity = (PieSectionEntity) e.getEntity();
                final String consumableVersionName = (String) entity.getSectionKey();

                final StringBuilder str = new StringBuilder(250);
                if (consumableVersionName.equals("Eating"))
                    for (final Consumable c : getLogData().getLogSummary().getFoodConsumablesUsed())
                        str.append(c + "\n");
                else if (consumableVersionName.equals("Drinking"))
                    for (final Consumable c : getLogData().getLogSummary()
                                                          .getBoozeConsumablesUsed())
                        str.append(c + "\n");
                else if (consumableVersionName.equals("Other")) {
                    for (final Consumable c : getLogData().getLogSummary()
                                                          .getSpleenConsumablesUsed())
                        str.append(c + "\n");
                    for (final Consumable c : getLogData().getLogSummary()
                                                          .getOtherConsumablesUsed())
                        str.append(c + "\n");
                } else
                    return;

                final JScrollPane text = new JScrollPane(new JTextArea(str.toString()));
                text.setPreferredSize(new Dimension(500, 400));
                JOptionPane.showMessageDialog(null,
                                              text,
                                              "All " + consumableVersionName + " consumables used",
                                              JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }
}
