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

import java.awt.Component;
import java.awt.Dimension;

import javax.swing.JList;
import javax.swing.JTextArea;
import javax.swing.ListCellRenderer;

/**
 * ListCellRenderer able to correctly display multi-line strings.
 */
public final class MultiLineCellRenderer extends JTextArea implements ListCellRenderer {
    private static final char NEW_LINE = '\n';

    public MultiLineCellRenderer() {
        setLineWrap(true);
        setWrapStyleWord(true);
        setEditable(false);
        setOpaque(true);
    }

    public Component getListCellRendererComponent(
                                                  final JList list, final Object value,
                                                  final int index, final boolean isSelected,
                                                  final boolean cellHasFocus) {
        final String text = value.toString();
        setText(text);

        // A little mucking around necessary to make sure the list items don't
        // end up being far bigger than their content would mandate.
        setPreferredSize(new Dimension(0, 0));
        int rowCount = 1;
        for (int i = 0; i < text.length(); i++)
            if (text.charAt(i) == NEW_LINE)
                rowCount++;
        setRows(rowCount);
        setPreferredSize(getPreferredSize());

        if (isSelected) {
            setBackground(list.getSelectionBackground());
            setForeground(list.getSelectionForeground());
        } else {
            setBackground(list.getBackground());
            setForeground(list.getForeground());
        }

        return this;
    }
}
