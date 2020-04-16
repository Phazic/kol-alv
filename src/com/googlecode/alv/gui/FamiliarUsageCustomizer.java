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

package com.googlecode.alv.gui;

import java.awt.Dimension;
import java.awt.GridLayout;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;

import org.jfree.ui.RefineryUtilities;

import com.googlecode.alv.chart.turnrundownGantt.TurnrundownGantt;
import com.googlecode.alv.chart.turnrundownGantt.FamiliarColor.Colors;

public final class FamiliarUsageCustomizer extends JDialog {
    private final TurnrundownGantt turnrundownChart;

    /**
     * @param owner
     *            The JFrame which owns this dialog.
     * @param turnrundownChart
     *            The turnrundown gantt chart on which certain actions can be
     *            performed.
     */
    public FamiliarUsageCustomizer(
                                   final JFrame owner, final TurnrundownGantt turnrundownChart) {
        super(owner, true);
        this.turnrundownChart = turnrundownChart;
        final FamiliarCustomizationTableModel familiarTableData = new FamiliarCustomizationTableModel(turnrundownChart.getFamiliarColors());
        final JTable familiarTable = new JTable(familiarTableData);
        final JComboBox box = new JComboBox();
        final DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();

        // Special combobox renderer to be able to choose a familiar colour.
        for (final Colors c : Colors.values())
            box.addItem(c);

        familiarTable.getColumnModel().getColumn(1).setCellEditor(new DefaultCellEditor(box));
        familiarTable.getColumnModel().getColumn(1).setCellRenderer(renderer);
        renderer.setToolTipText("Click to choose a color");

        // Size set so when the dialog is opened, the table will always have at
        // least this size.
        familiarTable.setPreferredScrollableViewportSize(new Dimension(500, 100));

        setLayout(new GridLayout());
        add(new JScrollPane(familiarTable));
        pack();

        setTitle("Familiar usage customization");
        RefineryUtilities.centerFrameOnScreen(this);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setVisible(true);
    }

    @Override
    public void dispose() {
        super.dispose();
        turnrundownChart.updateChart();
    }
}
