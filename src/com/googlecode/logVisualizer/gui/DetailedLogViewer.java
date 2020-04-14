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

package com.googlecode.logVisualizer.gui;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeSelectionModel;

import org.jfree.ui.RefineryUtilities;

import com.googlecode.logVisualizer.creator.HTMLLogCreator;
import com.googlecode.logVisualizer.creator.TextLogCreator;
import com.googlecode.logVisualizer.logData.LogDataHolder;
import com.googlecode.logVisualizer.logData.turn.*;
import com.googlecode.logVisualizer.parser.UsefulPatterns;
import com.googlecode.logVisualizer.util.LogOutputFormat;

/**
 * A useful and mostly self-contained class that can be used to view a more
 * detailed report of an ascension log.
 */
final class DetailedLogViewer extends JFrame {
    private final TurnDataPane dataPane = new TurnDataPane();

    private final String htmlLog;

    /**
     * Constructs and opens a frame with a more detailed view of the given
     * ascension log.
     */
    DetailedLogViewer(
                      final LogDataHolder logData) {
        super("DetailedLogViewer");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        if (logData == null)
            throw new NullPointerException("The log data holder must not be null.");

        htmlLog = HTMLLogCreator.getTextualLog(logData, LogOutputFormat.HTML_LOG);

        final JSplitPane splitter = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitter.setLeftComponent(new JScrollPane(createTurnRundownTree(logData)));
        splitter.setRightComponent(new JScrollPane(dataPane,
                                                   JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                                                   JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED));
        splitter.setDividerLocation(300);
        setContentPane(splitter);

        setSize(800, 600);
        RefineryUtilities.centerFrameOnScreen(this);
        setVisible(true);
    }

    /**
     * Creates the turn rundown tree menu to navigate between all turns.
     */
    private JTree createTurnRundownTree(
                                        final LogDataHolder logData) {
        final DefaultMutableTreeNode root = new DefaultMutableTreeNode(logData.getLogName());

        for (final TurnInterval ti : logData.getTurnIntervalsSpent()) {
            final DefaultMutableTreeNode tiRoot = new DefaultMutableTreeNode(new TurnIntervalContainer(ti));
            for (final SingleTurn st : ti.getTurns()) {
                final DefaultMutableTreeNode stRoot = new DefaultMutableTreeNode(new SingleEncounterContainer(st));
                for (final Encounter e : st.getEncounters())
                    stRoot.add(new DefaultMutableTreeNode(new SingleEncounterContainer(e)));

                tiRoot.add(stRoot);
            }

            root.add(tiRoot);
        }

        final JTree tree = new JTree(root);
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        tree.addTreeSelectionListener(new TreeSelectionListener() {
            public void valueChanged(
                                     final TreeSelectionEvent e) {
                final DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();

                if (node != null) {
                    final Object nodeContents = node.getUserObject();
                    if (nodeContents instanceof TurnContainer)
                        dataPane.displayTurnEntityInfo(((TurnContainer) nodeContents).getTurnObject());
                    else
                        dataPane.setText(htmlLog);
                    dataPane.setCaretPosition(0);
                }
            }
        });
        tree.setSelectionInterval(0, 0);

        return tree;
    }

    private static interface TurnContainer {
        TurnEntity getTurnObject();
    }

    private static class TurnIntervalContainer implements TurnContainer {
        private final TurnInterval ti;

        TurnIntervalContainer(
                              final TurnInterval ti) {
            this.ti = ti;
        }

        public Turn getTurnObject() {
            return ti;
        }

        @Override
        public String toString() {
            final StringBuilder str = new StringBuilder(50);

            str.append(UsefulPatterns.SQUARE_BRACKET_OPEN);

            if (ti.getTotalTurns() > 1) {
                str.append(ti.getStartTurn() + 1);
                str.append(UsefulPatterns.MINUS);
            }

            str.append(ti.getEndTurn());
            str.append(UsefulPatterns.SQUARE_BRACKET_CLOSE);
            str.append(UsefulPatterns.WHITE_SPACE);
            str.append(ti.getAreaName());

            return str.toString();
        }
    }

    private static class SingleEncounterContainer implements TurnContainer {
        private final Encounter e;

        SingleEncounterContainer(
                                 final Encounter e) {
            this.e = e;
        }

        public Encounter getTurnObject() {
            return e;
        }

        @Override
        public String toString() {
            final StringBuilder str = new StringBuilder(40);

            str.append(UsefulPatterns.SQUARE_BRACKET_OPEN);
            str.append(e.getTurnNumber());
            str.append(UsefulPatterns.SQUARE_BRACKET_CLOSE);
            str.append(UsefulPatterns.WHITE_SPACE);
            str.append(e.getEncounterName());

            return str.toString();
        }
    }
}
