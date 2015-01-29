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

package com.googlecode.logVisualizer.gui.searchDialogs;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.swing.*;

import com.googlecode.logVisualizer.gui.MultiLineCellRenderer;
import com.googlecode.logVisualizer.logData.LogDataHolder;
import com.googlecode.logVisualizer.logData.turn.TurnInterval;
import com.googlecode.logVisualizer.util.Lists;
import com.googlecode.logVisualizer.util.textualLogs.TextLogCreator;

/**
 * A dialog displaying all turn intervals of the given log data with a text
 * field to search for specific intervals by name.
 */
final class AreaSearchDialog extends TurnEntitySearchDialog {
    private JList turnIntervalResultsList;

    AreaSearchDialog(
                     final JFrame owner, final LogDataHolder logData) {
        super(owner, logData, "Search For Specific Areas");

        turnIntervalResultsList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(
                                     final MouseEvent e) {
                if (e.getClickCount() >= 2 && !turnIntervalResultsList.isSelectionEmpty()) {
                    final DefaultListModel model = (DefaultListModel) turnIntervalResultsList.getModel();
                    setSelectedTurn(((TurnContainer) model.getElementAt(turnIntervalResultsList.getSelectedIndex())).getTurn());
                    dispose();
                }
            }
        });

        setVisible(true);
    }

    @Override
    protected JComponent createResultsPane() {
        turnIntervalResultsList = new JList(new DefaultListModel());

        turnIntervalResultsList.setToolTipText("Double-click area to jump to it in the proper ascension log");
        turnIntervalResultsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        turnIntervalResultsList.setCellRenderer(new MultiLineCellRenderer());
        for (final TurnContainer tc : getTurnsList())
            addResult(tc);

        return new JScrollPane(turnIntervalResultsList,
                               JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                               JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    }

    @Override
    protected List<TurnContainer> createTurnList(
                                                 final LogDataHolder logData) {
        final List<TurnContainer> result = Lists.newArrayList(logData.getTurnIntervalsSpent()
                                                                     .size());

        final Iterator<String> turnIntervalStrings = TextLogCreator.getTurnRundownList(logData)
                                                                   .iterator();
        for (final TurnInterval ti : logData.getTurnIntervalsSpent())
            result.add(new TurnContainer(ti, turnIntervalStrings.next()));

        return result;
    }

    @Override
    protected void clearResults() {
        ((DefaultListModel) turnIntervalResultsList.getModel()).clear();
    }

    @Override
    protected void addResult(
                             final TurnContainer tc) {
        ((DefaultListModel) turnIntervalResultsList.getModel()).addElement(tc);
    }

    @Override
    protected void addResults(
                              final Collection<TurnContainer> results) {
        // Default models already in use fire a lot of events when elements get
        // added, to the point that there are very noticeable slow-downs.
        // We can side-step this issue by simply using a new model in case there
        // are a lot of elements to be added.
        // A cleaner approach would probably be to implement a ListModel that
        // doesn't fire events when they are not needed, but this is adequate
        // enough for now.
        if (results.size() <= 25)
            for (final TurnContainer tc : results)
                addResult(tc);
        else {
            final DefaultListModel newModel = new DefaultListModel();
            final DefaultListModel currentModel = (DefaultListModel) turnIntervalResultsList.getModel();
            if (!currentModel.isEmpty())
                for (int i = 0; i < currentModel.size(); i++)
                    newModel.addElement(currentModel.get(i));

            for (final TurnContainer tc : results)
                newModel.addElement(tc);

            turnIntervalResultsList.setModel(newModel);
        }
    }
}
