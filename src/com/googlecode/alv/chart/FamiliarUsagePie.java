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

import java.awt.Dimension;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.entity.PieSectionEntity;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.general.PieDataset;

import com.googlecode.alv.logData.LogDataHolder;
import com.googlecode.alv.logData.turn.turnAction.FamiliarChange;
import com.googlecode.alv.util.DataNumberPair;
import com.googlecode.alv.util.Lists;
import com.googlecode.alv.util.LookAheadIterator;
import com.googlecode.alv.util.Pair;

public final class FamiliarUsagePie extends PieChartBuilder {
    public FamiliarUsagePie(
                            final LogDataHolder logData) {
        super("Familiar usage", logData, false);
    }

    @Override
    protected PieDataset createDataset() {
        final DefaultPieDataset dataset = new DefaultPieDataset();

        for (final DataNumberPair<String> dn : getLogData().getLogSummary().getFamiliarUsage())
            dataset.setValue(dn.getData(), dn.getNumber());

        return dataset;
    }

    @Override
    protected void addChartPanelListeners(
                                          final ChartPanel cp) {
        cp.addChartMouseListener(new FamiliarChartMouseEventListener());
    }

    private final class FamiliarChartMouseEventListener implements ChartMouseListener {
        public void chartMouseMoved(
                                    final ChartMouseEvent e) {}

        public void chartMouseClicked(
                                      final ChartMouseEvent e) {
            if (e.getEntity() instanceof PieSectionEntity) {
                final PieSectionEntity entity = (PieSectionEntity) e.getEntity();
                final List<Pair<Integer, Integer>> familiarUsages = getFamiliarUsages(entity.getSectionKey()
                                                                                            .toString());
                final int combatTurnUsage = getNumberOfCombatTurnsUsed(entity.getSectionKey()
                                                                             .toString());

                final StringBuilder str = new StringBuilder(100);
                str.append("Total combat turns used: " + combatTurnUsage + "\n\n");
                str.append("Familiar used on the given turn intervals:\n");
                for (final Pair<Integer, Integer> p : familiarUsages)
                    if (p.getVar1().equals(p.getVar2()))
                        str.append(p.getVar1() + "\n");
                    else
                        str.append(p.getVar1() + "-" + p.getVar2() + "\n");

                final JScrollPane text = new JScrollPane(new JTextArea(str.toString()));
                text.setPreferredSize(new Dimension(400, 350));
                JOptionPane.showMessageDialog(null,
                                              text,
                                              "Usage of " + entity.getSectionKey(),
                                              JOptionPane.INFORMATION_MESSAGE);
            }
        }

        private List<Pair<Integer, Integer>> getFamiliarUsages(
                                                               final String familiar) {
            final List<Pair<Integer, Integer>> familiarUsages = Lists.newArrayList();
            final LookAheadIterator<FamiliarChange> index = new LookAheadIterator<FamiliarChange>(getLogData().getFamiliarChanges()
                                                                                                              .iterator());
            while (index.hasNext()) {
                final FamiliarChange fc = index.next();
                if (fc.getFamiliarName().equals(familiar)) {
                    final int startTurn = fc.getTurnNumber() + 1;
                    if (index.hasNext())
                        familiarUsages.add(Pair.of(startTurn, index.peek().getTurnNumber()));
                    else
                        familiarUsages.add(Pair.of(startTurn, getLogData().getLastTurnSpent()
                                                                          .getTurnNumber()));
                }
            }

            return familiarUsages;
        }

        private int getNumberOfCombatTurnsUsed(
                                               final String familiarName) {
            for (final DataNumberPair<String> dnp : getLogData().getLogSummary().getFamiliarUsage())
                if (dnp.getData().equals(familiarName))
                    return dnp.getNumber();

            return 0;
        }
    }
}
