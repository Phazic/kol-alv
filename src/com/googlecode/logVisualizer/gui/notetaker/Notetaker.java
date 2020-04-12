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

package com.googlecode.logVisualizer.gui.notetaker;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.jfree.ui.RefineryUtilities;

import com.googlecode.logVisualizer.creator.TextLogCreator;
import com.googlecode.logVisualizer.gui.ExportDialog;
import com.googlecode.logVisualizer.gui.MultiLineCellRenderer;
import com.googlecode.logVisualizer.logData.HeaderFooterComment;
import com.googlecode.logVisualizer.logData.LogComment;
import com.googlecode.logVisualizer.logData.LogDataHolder;
import com.googlecode.logVisualizer.logData.turn.TurnInterval;
import com.googlecode.logVisualizer.logData.turn.turnAction.DayChange;
import com.googlecode.logVisualizer.util.LogOutputFormat;
import com.googlecode.logVisualizer.util.LookAheadIterator;
import com.googlecode.logVisualizer.util.Pair;

/**
 * This class is ascension log notes editor, that gives the user a basic
 * interface to manage log notes.
 */
public final class Notetaker extends JFrame 
{
    /**
     * Will show a dialog to let the user choose which turncounts should be
     * included inside the Notetaker and then show the actual Notetaker
     * interface based on that decision.
     * @param log Collection of log data
     */
    public static void showNotetaker(final LogDataHolder log) 
    {
        // When trying to open the Notetaker with a non-detailed log, users are
        // only shown an info dialog, telling them that the given log cannot be
        // used with the Notetaker.
        if (!log.isDetailedLog()) {
            JOptionPane.showMessageDialog(null,
                                          "The given log cannot be used with the Notetaker, because it is not detailed enough.\n"
                                                  + "(For example, pre-parsed logs cannot be used with the Notetaker)",
                                          "Cannot use given log",
                                          JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        final JComboBox selectionBox = new JComboBox();
        selectionBox.setPreferredSize(new Dimension(400, selectionBox.getPreferredSize().height));

        // Populate the selection combo box with all possible intervals.
        selectionBox.addItem(new LogInterval("Full log", -1, Integer.MAX_VALUE));
        final LookAheadIterator<DayChange> index = new LookAheadIterator<DayChange>(log.getDayChanges()
                                                                                       .iterator());
        while (index.hasNext()) {
            final DayChange dc = index.next();
            final int nextDayChange = index.peek() != null ? index.peek().getTurnNumber()
                                                          : Integer.MAX_VALUE;
            selectionBox.addItem(new LogInterval("Day " + dc.getDayNumber(),
                                                 dc.getTurnNumber(),
                                                 nextDayChange));
        }
        selectionBox.setSelectedIndex(0);

        // Show the interval selection dialog.
        JOptionPane.showMessageDialog(null,
                                      selectionBox,
                                      "Select what interval that should be displayed in the Notetaker",
                                      JOptionPane.QUESTION_MESSAGE);

        // Create and show the actual Notetaker interface with the chosen
        // interval.
        final LogInterval selection = (LogInterval) selectionBox.getSelectedItem();
        if (selection.getName().equals("Full log"))
            new Notetaker(log, log);
        else
            new Notetaker(log.getSubIntervalLogData(selection.getStartTurn(),
                                                    selection.getEndTurn()), log);
    }

    private final LogDataHolder log;

    private final LogDataHolder baseLog;

    private final JButton showLogButton;

    private final JButton expandButton;

    private final JTextArea preCommentArea;

    private final JTextArea postCommentArea;

    private final TurnIntervalMenuList turnIntervalMenu;

    private LogCommentContainer activeTurnInterval;

    private Runnable showPostIntervalCommentAreaOnly;

    private Runnable showBothCommentAreas;

    /**
     * Creates the notes editor frame.
     * 
     * @param log
     *            The log data whose notes should be managed.
     * @param baseLog
     *            The log data on which the other given log is based on. May be
     *            the same log in case the other log is not a sub interval log.
     */
    Notetaker(final LogDataHolder log, final LogDataHolder baseLog) 
    {
        super("Notetaker for " + log.getLogName());
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(5, 25));

        this.log = log;
        this.baseLog = baseLog;
        showLogButton = new JButton("Show annotated log");
        expandButton = new JButton("+");
        expandButton.setPreferredSize(new Dimension(45, 35));
        expandButton.setToolTipText("<html>Opens or closes a pre-interval comment area.<br>Pre-interval comments will be shown before the interval in parsed log outputs.</html>");
        turnIntervalMenu = new TurnIntervalMenuList(log);
        preCommentArea = new JTextArea();
        postCommentArea = new JTextArea();
        preCommentArea.setLineWrap(true);
        preCommentArea.setWrapStyleWord(true);
        postCommentArea.setLineWrap(true);
        postCommentArea.setWrapStyleWord(true);
        getContentPane().add(createNotePanel(), BorderLayout.CENTER);
        getContentPane().add(createButtonPanel(), BorderLayout.SOUTH);
        addListeners();
        turnIntervalMenu.setSelectedIndex(0);

        setSize(800, 600);
        RefineryUtilities.centerFrameOnScreen(this);
        setVisible(true);
    }

    private JComponent createNotePanel() 
    {
        final JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        final JPanel commentPanel = new JPanel(new BorderLayout());
        final JScrollPane fullPostCommentArea = new JScrollPane(postCommentArea);

        final JPanel expandButtonPanel = new JPanel(new BorderLayout());
        expandButtonPanel.add(expandButton, BorderLayout.NORTH);

        final JPanel bothCommentsPanel = new JPanel(new GridLayout(0, 1, 0, 2));
        bothCommentsPanel.add(new JScrollPane(preCommentArea));
        bothCommentsPanel.add(fullPostCommentArea);

        final JPanel singleCommentPanel = new JPanel(new BorderLayout());
        singleCommentPanel.add(fullPostCommentArea, BorderLayout.CENTER);

        final JPanel commentsAreaPanel = new JPanel(new CardLayout());
        commentsAreaPanel.add(singleCommentPanel, "single comment area");
        commentsAreaPanel.add(bothCommentsPanel, "both comments area");

        commentPanel.add(expandButtonPanel, BorderLayout.WEST);
        commentPanel.add(commentsAreaPanel, BorderLayout.CENTER);

        splitPane.setTopComponent(new JScrollPane(turnIntervalMenu));
        splitPane.setBottomComponent(commentPanel);
        splitPane.setDividerLocation(400);

        showPostIntervalCommentAreaOnly = new Runnable() {
            public void run() {
                ((CardLayout) commentsAreaPanel.getLayout()).show(commentsAreaPanel,
                                                                  "single comment area");
                singleCommentPanel.add(fullPostCommentArea, BorderLayout.CENTER);
                expandButton.setText("+");
            }
        };
        showBothCommentAreas = new Runnable() {
            public void run() {
                ((CardLayout) commentsAreaPanel.getLayout()).show(commentsAreaPanel,
                                                                  "both comments area");
                bothCommentsPanel.add(fullPostCommentArea);
                expandButton.setText("-");
            }
        };

        return splitPane;
    }

    private JPanel createButtonPanel() 
    {
        final JPanel buttonPanel = new JPanel(new GridLayout(1, 0, 200, 0));
        final JButton closeButton = new JButton("Close window");

        showLogButton.setPreferredSize(new Dimension(0, 40));
        closeButton.setPreferredSize(new Dimension(0, 40));
        closeButton.addActionListener(new ActionListener() {
            public void actionPerformed(
                                        final ActionEvent e) {
                saveCommentsToCurrentContainer();

                dispose();
            }
        });

        buttonPanel.add(showLogButton);
        buttonPanel.add(closeButton);

        return buttonPanel;
    }

    private void addListeners() 
    {
        turnIntervalMenu.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(
                                     final ListSelectionEvent lse) {
                if (!lse.getValueIsAdjusting()) {
                    saveCommentsToCurrentContainer();

                    activeTurnInterval = turnIntervalMenu.getCurrentlySelectedTurnInterval();
                    if (!activeTurnInterval.isSingleCommentContainer())
                        preCommentArea.setText(activeTurnInterval.getPreIntervalComment()
                                                                 .getComments());
                    postCommentArea.setText(activeTurnInterval.getPostIntervalComment()
                                                              .getComments());

                    if (activeTurnInterval.isSingleCommentContainer()) {
                        showPostIntervalCommentAreaOnly.run();
                        expandButton.setEnabled(false);
                    } else if (activeTurnInterval.getPreIntervalComment().isEmpty()) {
                        showPostIntervalCommentAreaOnly.run();
                        expandButton.setEnabled(true);
                    } else {
                        showBothCommentAreas.run();
                        expandButton.setEnabled(true);
                    }
                }
            }
        });
        showLogButton.addActionListener(new ActionListener() {
            public void actionPerformed(
                                        final ActionEvent e) {
                saveCommentsToCurrentContainer();

                ExportDialog.showExportDialog(Notetaker.this, log);
            }
        });
        expandButton.addActionListener(new ActionListener() {
            public void actionPerformed(
                                        final ActionEvent e) {
                if (expandButton.getText().equals("+"))
                    showBothCommentAreas.run();
                else
                    showPostIntervalCommentAreaOnly.run();
            }
        });
    }

    private void saveCommentsToCurrentContainer() 
    {
        if (activeTurnInterval != null) {
            if (!activeTurnInterval.isSingleCommentContainer()) {
                setLogEdited(preCommentArea.getText(), activeTurnInterval.getPreIntervalComment());
                activeTurnInterval.getPreIntervalComment().setComments(preCommentArea.getText());
            }

            setLogEdited(postCommentArea.getText(), activeTurnInterval.getPostIntervalComment());
            activeTurnInterval.getPostIntervalComment().setComments(postCommentArea.getText());
        }
    }

    private void setLogEdited(final String currentComment, final LogComment matchingLogComment) 
    {
        if (!baseLog.isEdited())
            if (!currentComment.equals(matchingLogComment.getComments()))
                baseLog.setEdited(true);
    }

    /**
     * Just a little helper class make instantiation of the turn interval menu a
     * little nicer.
     */
    private static final class TurnIntervalMenuList extends JList 
    {

        /**
         * Creates the turn interval menu.
         * 
         * @param log
         *            The log data whose notes should be managed.
         */
        TurnIntervalMenuList(final LogDataHolder log) 
        {
            super(new DefaultListModel());
            setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            setCellRenderer(new MultiLineCellRenderer());

            final DefaultListModel model = (DefaultListModel) getModel();
            final Iterator<String> turnRundownListIndex 
                = TextLogCreator.getTurnRundownList(log).iterator();
            final LookAheadIterator<Pair<DayChange, HeaderFooterComment>> headerFooterIndex 
                = new LookAheadIterator<Pair<DayChange, HeaderFooterComment>>(log.getHeaderFooterComments()
                                                                                                                                                             .iterator());
            Pair<DayChange, HeaderFooterComment> currentHeaderFooter = headerFooterIndex.next();

            model.addElement(new LogCommentContainer(currentHeaderFooter.getVar2().getHeader(),
                                                     getHeaderFooterDescription(currentHeaderFooter,
                                                                                " header")));
            for (final TurnInterval ti : log.getTurnIntervalsSpent()) {
                while (headerFooterIndex.hasNext()
                       && headerFooterIndex.peek().getVar1().getTurnNumber() <= ti.getEndTurn()) {
                    addHeaderFooter(false, currentHeaderFooter);
                    currentHeaderFooter = headerFooterIndex.next();
                    addHeaderFooter(true, currentHeaderFooter);
                }

                model.addElement(new LogCommentContainer(ti.getPreIntervalComment(),
                                                         ti.getPostIntervalComment(),
                                                         turnRundownListIndex.next()));
            }
            model.addElement(new LogCommentContainer(currentHeaderFooter.getVar2().getFooter(),
                                                     getHeaderFooterDescription(currentHeaderFooter,
                                                                                " footer")));
        }

        private void addHeaderFooter(final boolean isHeader,
                                     final Pair<DayChange, HeaderFooterComment> headerFooter) 
        {
            final LogComment comment;
            final String description;
            if (isHeader) {
                comment = headerFooter.getVar2().getHeader();
                description = getHeaderFooterDescription(headerFooter, " header");
            } else {
                comment = headerFooter.getVar2().getFooter();
                description = getHeaderFooterDescription(headerFooter, " footer");
            }

            ((DefaultListModel) getModel()).addElement(new LogCommentContainer(comment, description));
        }

        private static String getHeaderFooterDescription(final Pair<DayChange, HeaderFooterComment> headerFooter,
                                                         final String headerFooterEnding) 
        {
            return "===Day " + headerFooter.getVar1().getDayNumber() + headerFooterEnding + "===";
        }

        LogCommentContainer getCurrentlySelectedTurnInterval() 
        {
            if (isSelectionEmpty())
                throw new IllegalStateException("No turn interval is currently selected.");

            return (LogCommentContainer) ((DefaultListModel) getModel()).get(getSelectedIndex());
        }
    }

    private static final class LogInterval 
    {
        private final String name;

        private final int startTurn;

        private final int endTurn;

        LogInterval(final String name, final int startTurn, final int endTurn) 
        {
            if (name == null)
                throw new IllegalArgumentException("The name must not be null.");

            this.name = name;
            this.startTurn = startTurn;
            this.endTurn = endTurn;
        }

        String getName() 
        {
            return name;
        }

        int getStartTurn() 
        {
            return startTurn;
        }

        int getEndTurn() 
        {
            return endTurn;
        }

        @Override
        public String toString() 
        {
            return name;
        }

        @Override
        public int hashCode() 
        {
            final int prime = 31;
            int result = 8421;
            result = prime * result + startTurn;
            result = prime * result + endTurn;
            result = prime * result + name.hashCode();
            return result;
        }

        @Override
        public boolean equals(final Object obj) 
        {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (!(obj instanceof LogInterval))
                return false;
            final LogInterval other = (LogInterval) obj;
            if (startTurn != other.startTurn)
                return false;
            if (endTurn != other.endTurn)
                return false;
            if (name == null) {
                if (other.name != null)
                    return false;
            } else if (!name.equals(other.name))
                return false;

            return true;
        }
    }
}
