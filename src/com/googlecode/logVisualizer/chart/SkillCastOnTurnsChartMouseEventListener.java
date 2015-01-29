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
import org.jfree.chart.entity.CategoryItemEntity;

import com.googlecode.logVisualizer.logData.LogDataHolder;
import com.googlecode.logVisualizer.logData.Skill;
import com.googlecode.logVisualizer.logData.turn.SingleTurn;
import com.googlecode.logVisualizer.logData.turn.Turn;
import com.googlecode.logVisualizer.logData.turn.TurnInterval;

final class SkillCastOnTurnsChartMouseEventListener implements ChartMouseListener {
    private final LogDataHolder logData;

    SkillCastOnTurnsChartMouseEventListener(
                                            final LogDataHolder logData) {
        this.logData = logData;
    }

    public void chartMouseMoved(
                                final ChartMouseEvent arg0) {}

    public void chartMouseClicked(
                                  final ChartMouseEvent e) {
        if (e.getEntity() instanceof CategoryItemEntity) {
            final CategoryItemEntity entity = (CategoryItemEntity) e.getEntity();
            final String skillName = (String) entity.getColumnKey();

            final StringBuilder str = new StringBuilder(250);
            if (logData.isDetailedLog())
                for (final SingleTurn st : logData.getTurnsSpent()) {
                    if (st.isSkillCast(skillName))
                        str.append(st.getTurnNumber() + ": "
                                   + getCastedSkill(st, skillName).getAmount() + "\n");
                }
            else
                for (final TurnInterval ti : logData.getTurnIntervalsSpent())
                    if (ti.isSkillCast(skillName))
                        str.append(ti.getStartTurn() + "-" + ti.getEndTurn() + ": "
                                   + getCastedSkill(ti, skillName).getAmount() + "\n");

            final JScrollPane text = new JScrollPane(new JTextArea(str.toString()));
            text.setPreferredSize(new Dimension(500, 450));
            JOptionPane.showMessageDialog(null, text, "Turn numbers and amount of casts of "
                                                      + skillName, JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private Skill getCastedSkill(
                                 final Turn turn, final String skillName) {
        Skill castedSkill = null;
        for (final Skill s : turn.getSkillsCast())
            if (s.getName().equals(skillName)) {
                castedSkill = s;
                break;
            }

        return castedSkill;
    }
}