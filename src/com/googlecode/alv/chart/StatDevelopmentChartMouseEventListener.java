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

import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.entity.XYItemEntity;

import com.googlecode.alv.logData.Item;
import com.googlecode.alv.logData.LogDataHolder;
import com.googlecode.alv.logData.consumables.Consumable;
import com.googlecode.alv.logData.turn.SingleTurn;
import com.googlecode.alv.logData.turn.Turn;
import com.googlecode.alv.logData.turn.TurnInterval;
import com.googlecode.alv.logData.turn.turnAction.EquipmentChange;

final class StatDevelopmentChartMouseEventListener implements ChartMouseListener {
    private final LogDataHolder logData;

    StatDevelopmentChartMouseEventListener(
                                           final LogDataHolder logData) {
        this.logData = logData;
    }

    public void chartMouseMoved(
                                final ChartMouseEvent arg0) {}

    public void chartMouseClicked(
                                  final ChartMouseEvent e) {
        if (e.getEntity() instanceof XYItemEntity) {
            final XYItemEntity entity = (XYItemEntity) e.getEntity();
            final int turnNumber = entity.getDataset().getX(0, entity.getItem()).intValue();

            Turn turn = null;
            if (logData.isDetailedLog())
                for (final SingleTurn st : logData.getTurnsSpent()) {
                    if (st.getTurnNumber() == turnNumber) {
                        turn = st;
                        break;
                    }
                }
            else
                for (final TurnInterval ti : logData.getTurnIntervalsSpent())
                    if (ti.getEndTurn() == turnNumber) {
                        turn = ti;
                        break;
                    }

            EquipmentChange equipment = null;
            if (logData.isDetailedLog())
                equipment = ((SingleTurn) turn).getUsedEquipment();
            final StringBuilder str = new StringBuilder(250);
            str.append("Turn spent in area: " + turn.getAreaName() + "\n");
            if (logData.isDetailedLog())
                str.append("Encounter name: " + ((SingleTurn) turn).getEncounterName() + "\n");
            str.append("Stats gained: " + turn.getStatGain() + "\n");
            if (logData.isDetailedLog()) {
                str.append("Familiar used: "
                           + ((SingleTurn) turn).getUsedFamiliar().getFamiliarName() + "\n\n");
                str.append("Equipment:\n");
                str.append("    Hat: " + equipment.getHat() + "\n");
                str.append("    Weapon: " + equipment.getWeapon() + "\n");
                str.append("    Offhand: " + equipment.getOffhand() + "\n");
                str.append("    Shirt: " + equipment.getShirt() + "\n");
                str.append("    Pants: " + equipment.getPants() + "\n");
                str.append("    Acc1: " + equipment.getAcc1() + "\n");
                str.append("    Acc2: " + equipment.getAcc2() + "\n");
                str.append("    Acc3: " + equipment.getAcc3() + "\n");
                str.append("    FamEquip: " + equipment.getFamEquip() + "\n");
            }
            str.append("\nConsumables used:\n");
            for (final Consumable c : turn.getConsumablesUsed())
                str.append("    " + c + "\n");
            str.append("\nItems dropped:\n");
            for (final Item i : turn.getDroppedItems())
                str.append("    " + i + "\n");

            final JScrollPane text = new JScrollPane(new JTextArea(str.toString()));
            text.setPreferredSize(new Dimension(500, 450));
            JOptionPane.showMessageDialog(null,
                                          text,
                                          "Information on " + turn,
                                          JOptionPane.INFORMATION_MESSAGE);
        }
    }
}