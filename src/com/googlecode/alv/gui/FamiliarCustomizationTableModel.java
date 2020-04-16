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

import java.util.Collection;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import com.googlecode.alv.chart.turnrundownGantt.FamiliarColor;
import com.googlecode.alv.chart.turnrundownGantt.FamiliarColor.Colors;
import com.googlecode.alv.util.Lists;

/**
 * A very basic implementation of a table model which is able to handle the
 * familiar colourisation of ascension logs.
 */
final class FamiliarCustomizationTableModel extends AbstractTableModel {
    private static final String[] COLUMN_NAMES = { "Familiar", "Color used" };

    private List<FamiliarColor> familiars;

    FamiliarCustomizationTableModel(
                                    final Collection<FamiliarColor> familiars) {
        super();
        this.familiars = Lists.newArrayList(familiars);
    }

    void addFamiliar(
                     final FamiliarColor familiar) {
        if (familiars == null)
            familiars = Lists.newArrayList();

        familiars.add(familiar);
    }

    void setFamiliars(
                      final List<FamiliarColor> familiars) {
        this.familiars = familiars;
    }

    List<FamiliarColor> getFamiliars() {
        return familiars;
    }

    @Override
    public boolean isCellEditable(
                                  final int rowIndex, final int columnIndex) {
        return columnIndex == 1;
    }

    @Override
    public void setValueAt(
                           final Object aValue, final int rowIndex, final int columnIndex) {
        familiars.get(rowIndex).setColor((Colors) aValue);
    }

    @Override
    public String getColumnName(
                                final int column) {
        return COLUMN_NAMES[column];
    }

    public int getColumnCount() {
        return COLUMN_NAMES.length;
    }

    public int getRowCount() {
        return familiars.size();
    }

    public Object getValueAt(
                             final int rowIndex, final int columnIndex) {
        return columnIndex == 0 ? familiars.get(rowIndex).getFamiliarName()
                               : familiars.get(rowIndex).getColor();
    }
}
