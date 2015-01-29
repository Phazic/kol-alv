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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.entity.CategoryItemEntity;

import com.googlecode.logVisualizer.logData.LogDataHolder;
import com.googlecode.logVisualizer.logData.turn.TurnInterval;

final class TurnsSpentInLevelChartMouseEventListener implements ChartMouseListener {
    private final LogDataHolder logData;

    private final Pattern levelNumberExtractor;

    TurnsSpentInLevelChartMouseEventListener(
                                             final LogDataHolder logData,
                                             final Pattern levelNumberExtractor) {
        this.logData = logData;
        this.levelNumberExtractor = levelNumberExtractor;
    }

    public void chartMouseMoved(
                                final ChartMouseEvent arg0) {}

    public void chartMouseClicked(
                                  final ChartMouseEvent e) {
        if (e.getEntity() instanceof CategoryItemEntity) {
            final CategoryItemEntity entity = (CategoryItemEntity) e.getEntity();
            final Matcher m = levelNumberExtractor.matcher((String) entity.getColumnKey());
            m.find();
            final int level = Integer.parseInt(m.group(1));

            final StringBuilder str = new StringBuilder(100);
            for (final TurnInterval ti : logData.getTurnIntervalsSpent()) {
                final int levelOnStart = logData.getCurrentLevel(ti.getStartTurn())
                                                .getLevelNumber();
                final int levelOnEnd = logData.getCurrentLevel(ti.getEndTurn()).getLevelNumber();

                if (levelOnStart > level)
                    break;

                if (levelOnStart == level || levelOnEnd == level)
                    str.append(ti + "\n");
            }

            final JScrollPane text = new JScrollPane(new JTextArea(str.toString()));
            text.setPreferredSize(new Dimension(500, 450));
            JOptionPane.showMessageDialog(null,
                                          text,
                                          "Areas visited during level " + level,
                                          JOptionPane.INFORMATION_MESSAGE);
        }
    }
}
