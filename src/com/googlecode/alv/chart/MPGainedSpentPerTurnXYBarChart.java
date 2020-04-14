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

import java.awt.Color;
import java.awt.Dimension;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.entity.XYItemEntity;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.data.xy.IntervalXYDataset;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import com.googlecode.alv.logData.LogDataHolder;
import com.googlecode.alv.logData.Skill;
import com.googlecode.alv.logData.turn.SingleTurn;
import com.googlecode.alv.util.Lists;

public final class MPGainedSpentPerTurnXYBarChart extends VerticalXYBarChartBuilder {
    public MPGainedSpentPerTurnXYBarChart(
                                          final LogDataHolder logData) {
        super(logData, "MP gained/spent per turn", "Turn number", "MP gained/spent", true);
    }

    /**
     * Method overridden to make the MP spent bar colour use a bit of alpha,
     * otherwise MP gains below MP spent wouldn't be viewable.
     */
    @Override
    protected ChartPanel createChartPanel() {
        final ChartPanel panel = super.createChartPanel();
        final XYPlot plot = (XYPlot) panel.getChart().getPlot();
        final XYBarRenderer renderer = (XYBarRenderer) plot.getRenderer();
        final XYDataset dataset = plot.getDataset();

        for (int i = 0; i < dataset.getSeriesCount(); i++)
            if (dataset.getSeriesKey(i).equals("MP spent"))
                renderer.setSeriesPaint(i, new Color(255, 80, 80, 200));
            else if (dataset.getSeriesKey(i).equals("MP gained"))
                renderer.setSeriesPaint(i, new Color(100, 100, 255));

        return panel;
    }

    @Override
    protected IntervalXYDataset createDataset() {
        final XYSeriesCollection datasets = new XYSeriesCollection();

        if (getLogData().isDetailedLog()) {
            final XYSeries gainedDataset = new XYSeries("MP gained", false);
            final XYSeries spentDataset = new XYSeries("MP spent", false);

            for (final SingleTurn st : getLogData().getTurnsSpent()) {
                gainedDataset.add(st.getTurnNumber(), st.getMPGain().getTotalMPGains());

                int spentMP = 0;
                for (final Skill s : st.getSkillsCast())
                    spentMP += s.getMpCost();
                spentDataset.add(st.getTurnNumber(), spentMP);
            }

            datasets.addSeries(spentDataset);
            datasets.addSeries(gainedDataset);
        }

        return datasets;
    }

    @Override
    protected void addChartPanelListeners(
                                          final ChartPanel cp) {
        if (getLogData().isDetailedLog())
            cp.addChartMouseListener(new MPGainedSpentPerTurnChartMouseListener());
    }

    private final class MPGainedSpentPerTurnChartMouseListener implements ChartMouseListener {
        public void chartMouseMoved(
                                    final ChartMouseEvent arg0) {}

        public void chartMouseClicked(
                                      final ChartMouseEvent e) {
            if (e.getEntity() instanceof XYItemEntity) {
                final XYItemEntity entity = (XYItemEntity) e.getEntity();
                final int turnNumber = entity.getDataset().getX(0, entity.getItem()).intValue();

                SingleTurn turn = null;
                for (final SingleTurn st : getLogData().getTurnsSpent())
                    if (st.getTurnNumber() == turnNumber) {
                        turn = st;
                        break;
                    }

                final StringBuilder str = new StringBuilder(250);
                str.append("MP spent:\n\n");
                int totalSpent = 0;
                final List<Skill> skills = Lists.newArrayList(turn.getSkillsCast());
                Collections.sort(skills, new Comparator<Skill>() {
                    public int compare(
                                       final Skill o1, final Skill o2) {
                        return o2.getMpCost() - o1.getMpCost();
                    }
                });
                for (final Skill s : skills) {
                    totalSpent += s.getMpCost();
                    str.append("Cast " + s.getAmount() + " " + s.getName() + ": " + s.getMpCost()
                               + " MP\n");
                }
                str.append("\nTotal MP spent: " + totalSpent + " MP\n");
                str.append("\n\nMP gained:\n\n");
                str.append("Inside encounter: " + turn.getMPGain().encounterMPGain + " MP\n");
                str.append("Starfish: " + turn.getMPGain().starfishMPGain + " MP\n");
                str.append("Resting: " + turn.getMPGain().restingMPGain + " MP\n");
                str.append("Outside encounter: " + turn.getMPGain().outOfEncounterMPGain + " MP\n");
                str.append("Consumables: " + turn.getMPGain().consumableMPGain + " MP\n");
                str.append("\nTotal MP gained: " + turn.getMPGain().getTotalMPGains() + " MP\n");

                final JScrollPane text = new JScrollPane(new JTextArea(str.toString()));
                text.setPreferredSize(new Dimension(550, 400));
                JOptionPane.showMessageDialog(null,
                                              text,
                                              "MP gained/spent on turn " + turn,
                                              JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }
}
