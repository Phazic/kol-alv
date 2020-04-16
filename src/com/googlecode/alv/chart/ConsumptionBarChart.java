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
import org.jfree.chart.entity.CategoryItemEntity;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.renderer.category.StackedBarRenderer;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;

import com.googlecode.alv.logData.LogDataHolder;
import com.googlecode.alv.logData.consumables.Consumable;
import com.googlecode.alv.logData.turn.SingleTurn;
import com.googlecode.alv.util.Lists;

public final class ConsumptionBarChart extends HorizontalStackedBarChartBuilder {
    public ConsumptionBarChart(
                               final LogDataHolder logData) {
        super(logData, "Turns gained per consumable", "Consumable", "Adventures gained", true);
    }

    @Override
    protected ChartPanel createChartPanel() {
        final ChartPanel panel = super.createChartPanel();
        final CategoryPlot plot = (CategoryPlot) panel.getChart().getPlot();
        final StackedBarRenderer renderer = (StackedBarRenderer) plot.getRenderer();
        final CategoryDataset dataset = plot.getDataset();

        for (int i = 0; i < dataset.getRowCount(); i++)
            if (dataset.getRowKey(i).equals("Food"))
                renderer.setSeriesPaint(i, new Color(255, 80, 80));
            else if (dataset.getRowKey(i).equals("Booze"))
                renderer.setSeriesPaint(i, new Color(100, 100, 255));
            else if (dataset.getRowKey(i).equals("Spleen"))
                renderer.setSeriesPaint(i, new Color(80, 255, 80));
            else if (dataset.getRowKey(i).equals("Other"))
                renderer.setSeriesPaint(i, Color.LIGHT_GRAY);

        return panel;
    }

    @Override
    protected CategoryDataset createDataset() {
        final DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        final List<Consumable> consumables = Lists.newArrayList(getLogData().getAllConsumablesUsed()
                                                                            .size());

        for (final Consumable c : getLogData().getAllConsumablesUsed())
            if (c.getAdventureGain() > 0)
                consumables.add(c);

        // Sort consumables from highest to lowest adventure gain.
        Collections.sort(consumables, new Comparator<Consumable>() {

            public int compare(
                               final Consumable o1, final Consumable o2) {
                return o2.getAdventureGain() - o1.getAdventureGain();
            }
        });

        // Add consumables to the dataset. Differentiate between the consumable
        // versions.
        for (final Consumable c : consumables)
            switch (c.getConsumableVersion()) {
                case FOOD:
                    dataset.addValue(c.getAdventureGain(), "Food", c.getName() + " ("
                                                                   + c.getAmount() + ")");
                    break;
                case BOOZE:
                    dataset.addValue(c.getAdventureGain(), "Booze", c.getName() + " ("
                                                                    + c.getAmount() + ")");
                    break;
                case SPLEEN:
                    dataset.addValue(c.getAdventureGain(), "Spleen", c.getName() + " ("
                                                                     + c.getAmount() + ")");
                    break;
                default:
                    dataset.addValue(c.getAdventureGain(), "Other", c.getName() + " ("
                                                                    + c.getAmount() + ")");
            }

        return dataset;
    }

    @Override
    protected void addChartPanelListeners(
                                          final ChartPanel cp) {
        cp.addChartMouseListener(new ConsumptionChartMouseEventListener());
    }

    private final class ConsumptionChartMouseEventListener implements ChartMouseListener {
        public void chartMouseMoved(
                                    final ChartMouseEvent arg0) {}

        public void chartMouseClicked(
                                      final ChartMouseEvent e) {
            if (e.getEntity() instanceof CategoryItemEntity) {
                final CategoryItemEntity entity = (CategoryItemEntity) e.getEntity();
                final String columnKeyName = (String) entity.getColumnKey();
                final String consumableName = columnKeyName.substring(0,
                                                                      columnKeyName.indexOf(" ("));

                Consumable totalConsumable = null;
                for (final Consumable c : getLogData().getAllConsumablesUsed())
                    if (c.getName().equals(consumableName)) {
                        totalConsumable = c;
                        break;
                    }
                final List<Consumable> usedConsumables = Lists.newArrayList();
                for (final SingleTurn st : getLogData().getTurnsSpent())
                    for (final Consumable c : st.getConsumablesUsed())
                        if (c.getName().equals(consumableName)) {
                            usedConsumables.add(c);
                            break;
                        }

                final StringBuilder str = new StringBuilder(100);
                str.append("Total adventures gained from the consumable: "
                           + totalConsumable.getAdventureGain() + "\n");
                str.append("Total stats gained from the consumable: "
                           + totalConsumable.getStatGain() + "\n\n");
                str.append("Consumable used on the given turns:\n");
                for (final Consumable c : usedConsumables)
                    str.append(c.getTurnNumberOfUsage() + ": " + c + "\n");

                final JScrollPane text = new JScrollPane(new JTextArea(str.toString()));
                text.setPreferredSize(new Dimension(500, 250));
                JOptionPane.showMessageDialog(null,
                                              text,
                                              "Consumption of " + consumableName,
                                              JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }
}
