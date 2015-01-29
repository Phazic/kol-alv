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

import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.entity.CategoryItemEntity;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;

import com.googlecode.logVisualizer.logData.LogDataHolder;
import com.googlecode.logVisualizer.logData.MPGain;
import com.googlecode.logVisualizer.logData.turn.SingleTurn;
import com.googlecode.logVisualizer.util.DataNumberPair;

public final class MPGainsPerLevelBarChart extends HorizontalStackedBarChartBuilder {
    public MPGainsPerLevelBarChart(
                                   final LogDataHolder logData) {
        super(logData, "MP gains per level", "Level", "Meat", true);
    }

    @Override
    protected CategoryDataset createDataset() {
        final DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        for (final DataNumberPair<MPGain> dnp : getLogData().getLogSummary()
                                                            .getMPGainSummary()
                                                            .getAllLevelsData()) {
            final String levelStr = "Level " + dnp.getNumber();

            dataset.addValue(dnp.getData().encounterMPGain, "Inside encounters", levelStr);
            dataset.addValue(dnp.getData().starfishMPGain, "Starfish familiars", levelStr);
            dataset.addValue(dnp.getData().restingMPGain, "Resting", levelStr);
            dataset.addValue(dnp.getData().outOfEncounterMPGain, "Outside encounters", levelStr);
            dataset.addValue(dnp.getData().consumableMPGain, "Consumables", levelStr);
        }

        return dataset;
    }

    @Override
    protected void addChartPanelListeners(
                                          final ChartPanel cp) {
        if (getLogData().isDetailedLog())
            cp.addChartMouseListener(new MPGainsPerLevelChartMouseEventListener());
    }

    private final class MPGainsPerLevelChartMouseEventListener implements ChartMouseListener {
        public void chartMouseMoved(
                                    final ChartMouseEvent arg0) {}

        public void chartMouseClicked(
                                      final ChartMouseEvent e) {
            if (e.getEntity() instanceof CategoryItemEntity) {
                final CategoryItemEntity entity = (CategoryItemEntity) e.getEntity();
                final String levelString = (String) entity.getColumnKey();
                // The beginning ("Level ") is always the same.
                final int level = Integer.parseInt(levelString.substring(6));

                final StringBuilder str = new StringBuilder(100);
                str.append("Meat gained/spent on every turn of the level (encounter MP; starfish MP; resting MP; out-of-encounter MP; consumables MP):\n");
                for (final SingleTurn st : getLogData().getTurnsSpent()) {
                    final int currentLevel = getLogData().getCurrentLevel(st.getTurnNumber())
                                                         .getLevelNumber();
                    if (currentLevel > level)
                        break;
                    else if (currentLevel == level) {
                        final MPGain mpGains = st.getMPGain();
                        str.append(st.getTurnNumber() + ":    " + mpGains.encounterMPGain + ";  "
                                   + mpGains.starfishMPGain + ";  " + mpGains.restingMPGain + ";  "
                                   + mpGains.outOfEncounterMPGain + ";  "
                                   + mpGains.consumableMPGain + "\n");
                    }
                }

                final JScrollPane text = new JScrollPane(new JTextArea(str.toString()));
                text.setPreferredSize(new Dimension(500, 450));
                JOptionPane.showMessageDialog(null,
                                              text,
                                              "MP gains during level " + level,
                                              JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }
}