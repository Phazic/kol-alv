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

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.googlecode.logVisualizer.chart.*;
import com.googlecode.logVisualizer.chart.perDayConsumption.PerDayConsumptionBarCharts;
import com.googlecode.logVisualizer.chart.turnrundownGantt.TurnrundownGantt;
import com.googlecode.logVisualizer.creator.TextLogCreator;
import com.googlecode.logVisualizer.gui.notetaker.Notetaker;
import com.googlecode.logVisualizer.gui.searchDialogs.SearchDialogs;
import com.googlecode.logVisualizer.logData.LogDataHolder;
import com.googlecode.logVisualizer.logData.turn.TurnInterval;
import com.googlecode.logVisualizer.util.LogOutputFormat;

public final class LogGUI extends JSplitPane {
    private static final String[] LIST_MENU_ITEM_NAMES = { "Turn rundown gantt",
                                                          "Total turns spent",
                                                          "Turns spent per area",
                                                          "Turns spent per level",
                                                          "Turns got from ...",
                                                          "Turns per consumable",
                                                          "Per day consumable stats",
                                                          "Familiar usage", "Skills cast",
                                                          "Skill MP costs", "Total MP gains",
                                                          "MP gains per level",
                                                          "MP gained/spent per turn",
                                                          "Meat gained/spent", "Pulls",
                                                          "Stat gains overview", "Stats per area",
                                                          "Stats per turn per level",
                                                          "Stat development", "Quest Turns",
                                                          "Misc", "Ascension log" };

    private final GanttPanelGUI ganttPanel;

    private final LogDataHolder logData;

    /**
     * @param log
     *            The file holding the ascension log.
     * @param logData
     *            The {@link LogDataHolder} with all the data of the given
     *            ascension log.
     * @param isTextLogFromFile
     *            A flag denoting whether the textual ascension log already
     *            exists in the given file. (should only be true if the file
     *            holds a pre-parsed ascension log)
     */
    public LogGUI(
                  final File log, final LogDataHolder logData, final boolean isTextLogFromFile) {
        super();

        if (log == null)
            throw new NullPointerException("Log must not be null.");
        if (logData == null)
            throw new NullPointerException("Log data must not be null.");

        ganttPanel = new GanttPanelGUI(logData);
        this.logData = logData;
        final JPanel chartArea = new JPanel(new CardLayout());
        final JList navigation = new JList();

        int i = 0;
        chartArea.add(ganttPanel, LIST_MENU_ITEM_NAMES[i++]);
        chartArea.add(new TotalTurnsSpentPie(logData), LIST_MENU_ITEM_NAMES[i++]);
        chartArea.add(new TurnsSpentPerAreaBarChart(logData), LIST_MENU_ITEM_NAMES[i++]);
        chartArea.add(new TurnsSpentPerLevelBarChart(logData), LIST_MENU_ITEM_NAMES[i++]);
        chartArea.add(new TotalConsumptionPie(logData), LIST_MENU_ITEM_NAMES[i++]);
        chartArea.add(new ConsumptionBarChart(logData), LIST_MENU_ITEM_NAMES[i++]);
        chartArea.add(new PerDayConsumptionBarCharts(logData), LIST_MENU_ITEM_NAMES[i++]);
        chartArea.add(new FamiliarUsagePie(logData), LIST_MENU_ITEM_NAMES[i++]);
        chartArea.add(new SkillCastsBarChart(logData), LIST_MENU_ITEM_NAMES[i++]);
        chartArea.add(new SkillMPCostBarChart(logData), LIST_MENU_ITEM_NAMES[i++]);
        chartArea.add(new MPGainsBarChart(logData), LIST_MENU_ITEM_NAMES[i++]);
        chartArea.add(new MPGainsPerLevelBarChart(logData), LIST_MENU_ITEM_NAMES[i++]);
        chartArea.add(new MPGainedSpentPerTurnXYBarChart(logData), LIST_MENU_ITEM_NAMES[i++]);
        chartArea.add(new MeatPerLevelBarChart(logData), LIST_MENU_ITEM_NAMES[i++]);
        chartArea.add(new PullsPanel(logData), LIST_MENU_ITEM_NAMES[i++]);
        chartArea.add(new StatGiverBarChart(logData), LIST_MENU_ITEM_NAMES[i++]);
        chartArea.add(new StatsPerAreaBarChart(logData), LIST_MENU_ITEM_NAMES[i++]);
        chartArea.add(new StatsPerTurnPerLevelBarChart(logData), LIST_MENU_ITEM_NAMES[i++]);
        chartArea.add(new StatDevelopmentPanelGUI(logData), LIST_MENU_ITEM_NAMES[i++]);
        chartArea.add(new QuestTurnsBarChart(logData), LIST_MENU_ITEM_NAMES[i++]);
        chartArea.add(new MiscPanel(logData), LIST_MENU_ITEM_NAMES[i++]);
        if (isTextLogFromFile)
            chartArea.add(new LogViewer(log), LIST_MENU_ITEM_NAMES[i++]);
        else
            chartArea.add(new LogViewer(logData), LIST_MENU_ITEM_NAMES[i++]);

        navigation.setModel(new AbstractListModel() {
            public int getSize() {
                return LIST_MENU_ITEM_NAMES.length;
            }

            public Object getElementAt(
                                       final int i) {
                return LIST_MENU_ITEM_NAMES[i];
            }
        });
        navigation.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        navigation.setSelectedIndex(0);
        navigation.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(
                                     final ListSelectionEvent lse) {
                if (!lse.getValueIsAdjusting())
                    ((CardLayout) chartArea.getLayout()).show(chartArea,
                                                              navigation.getSelectedValue()
                                                                        .toString());
            }
        });

        final JScrollPane navigationScrollPane = new JScrollPane(navigation);
        navigationScrollPane.setMinimumSize(new Dimension(190, 0));

        setRightComponent(chartArea);
        setLeftComponent(navigationScrollPane);
    }

    /**
     * @return The data of the ascension log.
     */
    public LogDataHolder getLogData() {
        return logData;
    }

    /**
     * @return The name of the ascension log.
     */
    public String getLogName() {
        return logData.getLogName();
    }

    /**
     * @param gpbl
     *            The button listener for the {@link GanttPanelGUI} to set.
     */
    public void setGanttPanelButtonListener(
                                            final GanttPaneButtonListener gpbl) {
        ganttPanel.setButtonListener(gpbl);
    }

    /**
     * A simple JTextArea that holds the whole contents of the given log file.
     */
    private final static class LogViewer extends JPanel {
        private static final String NEW_LINE = "\n";

        LogViewer(
                  final LogDataHolder logData) {
            super(new BorderLayout());

            final JTextArea log = new JTextArea(TextLogCreator.getTextualLog(logData,
                                                                             LogOutputFormat.TEXT_LOG));
            log.setWrapStyleWord(true);
            log.setLineWrap(true);
            log.setCaretPosition(0);
            log.setEditable(false);

            final JPanel buttonPanel = new JPanel(new GridLayout(1, 0, 0, 10));
            final JButton exportLogButton = new JButton("View/Export log");
            exportLogButton.setToolTipText("View the log in one of the output formats and/or save it to a file.");
            exportLogButton.addActionListener(new ActionListener() {
                public void actionPerformed(
                                            final ActionEvent e) {
                    ExportDialog.showExportDialog(null, logData);
                }
            });
            final JButton areaSearchButton = new JButton("Area Search");
            areaSearchButton.setToolTipText("Search for specific turn intervals.");
            areaSearchButton.addActionListener(new ActionListener() {
                public void actionPerformed(
                                            final ActionEvent e) {
                    final TurnInterval ti = SearchDialogs.showAreaSearchDialog(null, logData);

                    jumpToInterval(log, ti);
                }
            });
            final JButton turnSearchButton = new JButton("Turn Search");
            turnSearchButton.setToolTipText("Search for specific turns.");
            turnSearchButton.addActionListener(new ActionListener() {
                public void actionPerformed(
                                            final ActionEvent e) {
                    final TurnInterval ti = SearchDialogs.showTurnSearchDialog(null, logData);

                    jumpToInterval(log, ti);
                }
            });
            final JButton notetakerButton = new JButton("Open Notetaker");
            notetakerButton.addActionListener(new ActionListener() {
                public void actionPerformed(
                                            final ActionEvent e) {
                    Notetaker.showNotetaker(logData);
                }
            });
            final JButton detailedLogButton = new JButton("Open Detailed Log Viewer");
            detailedLogButton.addActionListener(new ActionListener() {
                public void actionPerformed(
                                            final ActionEvent e) {
                    new DetailedLogViewer(logData);
                }
            });
            buttonPanel.add(exportLogButton);
            buttonPanel.add(areaSearchButton);
            buttonPanel.add(turnSearchButton);
            buttonPanel.add(notetakerButton);
            buttonPanel.add(detailedLogButton);

            final JSplitPane splitter = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
            splitter.setTopComponent(buttonPanel);
            splitter.setBottomComponent(new JScrollPane(log));
            splitter.setDividerLocation(50);

            add(splitter, BorderLayout.CENTER);
        }

        LogViewer(
                  final File logFile) {
            super(new BorderLayout());

            final JTextArea log = new JTextArea();
            log.setWrapStyleWord(true);
            log.setLineWrap(true);

            try {
                final FileInputStream fis = new FileInputStream(logFile);
                final BufferedReader br = new BufferedReader(new InputStreamReader(fis));
                final StringBuilder tmpLines = new StringBuilder(50000);
                String tmpLine;

                while ((tmpLine = br.readLine()) != null) {
                    tmpLines.append(tmpLine);
                    tmpLines.append(NEW_LINE);

                    final int tmpLinesLength = tmpLines.length();
                    if (tmpLinesLength > 49500) {
                        log.append(tmpLines.toString());
                        tmpLines.delete(0, tmpLinesLength);
                    }
                }

                if (tmpLines.length() != 0)
                    log.append(tmpLines.toString());

                br.close();
            } catch (final IOException e) {
                e.printStackTrace();
            }
            log.setCaretPosition(0);

            add(new JScrollPane(log), BorderLayout.CENTER);
        }

        private void jumpToInterval(
                                    final JTextArea text, final TurnInterval ti) {
            // If a turn interval was selected, jump to it in the ascension log.
            // We want the turn interval to be the first displayed line in the
            // text area. For that to always work correctly we first jump to the
            // very end of the log.
            // Sadly, that call seems to be optimised away one way or another if
            // the the caret position changes are done right after each other,
            // which made it necessary to use that rather ridiculous looking
            // call to the EventQueue.
            if (ti != null) {
                final int position = text.getText().indexOf(ti.toString());
                if (position >= 0) {
                    text.setCaretPosition(text.getText().length());
                    EventQueue.invokeLater(new Runnable() {
                        public void run() {
                            text.setCaretPosition(position);
                        }
                    });
                }
            }
        }
    }

    /**
     * A listener for all gantt chart configurators.
     */
    public interface GanttPaneButtonListener {
        /**
         * This method will fire if the button for the area category customizer
         * has been pressed.
         * 
         * @param turnrundownChart
         *            The turnrundown gantt chart on which certain actions can
         *            be performed.
         */
        public void areaCategoryCustomizerPressed(
                                                  TurnrundownGantt turnrundownChart);

        /**
         * This method will fire if the button for the familiar color customizer
         * has been pressed.
         * 
         * @param turnrundownChart
         *            The turnrundown gantt chart on which certain actions can
         *            be performed.
         */
        public void familiarColorizerPressed(
                                             TurnrundownGantt turnrundownChart);
    }
}
