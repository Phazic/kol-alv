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

package com.googlecode.alv.gui.projectUpdatesViewer;

import java.util.Collection;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import com.googlecode.alv.util.Lists;

/**
 * A very small and basic implementation of a {@code TableModel} for project
 * feed updates.
 */
final class ProjectFeedUpdateTableModel extends AbstractTableModel {
    private static final String[] COLUMN_NAMES = { "Title", "Date" };

    private final List<ProjectUpdateContainer> updates;

    ProjectFeedUpdateTableModel(
                                final Collection<ProjectUpdateContainer> updates) {
        super();
        this.updates = Lists.newArrayList(updates);
    }

    @Override
    public boolean isCellEditable(
                                  final int rowIndex, final int columnIndex) {
        return false;
    }

    @Override
    public void setValueAt(
                           final Object aValue, final int rowIndex, final int columnIndex) {
        throw new UnsupportedOperationException();
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
        return updates.size();
    }

    public Object getValueAt(
                             final int rowIndex, final int columnIndex) {
        return columnIndex == 0 ? updates.get(rowIndex).getTitle() : updates.get(rowIndex)
                                                                            .getUpdated();
    }
}
