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
import org.jfree.chart.entity.PieSectionEntity;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.general.PieDataset;

import com.googlecode.logVisualizer.logData.LogDataHolder;
import com.googlecode.logVisualizer.logData.turn.SingleTurn;
import com.googlecode.logVisualizer.logData.turn.TurnVersion;

public final class TotalTurnsSpentPie extends PieChartBuilder {
    public TotalTurnsSpentPie(
                              final LogDataHolder logData) {
        super("Total turns spent", logData, false);
    }

    @Override
    protected PieDataset createDataset() {
        final DefaultPieDataset dataset = new DefaultPieDataset();

        dataset.setValue("Combats", getLogData().getLogSummary().getTotalTurnsCombat());
        dataset.setValue("Noncombats", getLogData().getLogSummary().getTotalTurnsNoncombat());
        dataset.setValue("Other", getLogData().getLogSummary().getTotalTurnsOther());

        return dataset;
    }

    @Override
    protected void addChartPanelListeners(
                                          final ChartPanel cp) {
        if (getLogData().isDetailedLog())
            cp.addChartMouseListener(new TotalTurnsSpentChartMouseEventListener());
    }

    private final class TotalTurnsSpentChartMouseEventListener implements ChartMouseListener {
        public void chartMouseMoved(
                                    final ChartMouseEvent e) {}

        public void chartMouseClicked(
                                      final ChartMouseEvent e) {
            if (e.getEntity() instanceof PieSectionEntity) {
                final PieSectionEntity entity = (PieSectionEntity) e.getEntity();
                final String turnVersionName = (String) entity.getSectionKey();
                TurnVersion version = TurnVersion.NOT_DEFINED;
                if (turnVersionName.equals("Combats"))
                    version = TurnVersion.COMBAT;
                else if (turnVersionName.equals("Noncombats"))
                    version = TurnVersion.NONCOMBAT;
                else if (turnVersionName.equals("Other"))
                    version = TurnVersion.OTHER;

                if (version == TurnVersion.NOT_DEFINED)
                    return;

                final StringBuilder str = new StringBuilder(1500);
                for (final SingleTurn st : getLogData().getTurnsSpent())
                    if (st.getTurnVersion() == version)
                        str.append(st + "\n");

                final JScrollPane text = new JScrollPane(new JTextArea(str.toString()));
                text.setPreferredSize(new Dimension(500, 400));
                JOptionPane.showMessageDialog(null,
                                              text,
                                              "Occurences of " + version + " turns",
                                              JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }
}
