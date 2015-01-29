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

package com.googlecode.logVisualizer.gui;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;

import net.java.dev.spellcast.utilities.UtilityConstants;

import com.googlecode.logVisualizer.LogVisualizer;
import com.googlecode.logVisualizer.Settings;
import com.googlecode.logVisualizer.gui.MafiaLogsVisualizerDialog.AscensionLogLoaderListener;
import com.googlecode.logVisualizer.gui.dataTablesEditor.DataTablesEditor;
import com.googlecode.logVisualizer.gui.notetaker.Notetaker;
import com.googlecode.logVisualizer.gui.projectUpdatesViewer.ProjectUpdateViewer;
import com.googlecode.logVisualizer.logData.LogDataHolder;
import com.googlecode.logVisualizer.util.LogOutputFormat;
import com.googlecode.logVisualizer.util.dataTables.DataTablesHandler;
import com.sun.java.forums.CloseableTabbedPane;
import com.sun.java.forums.CloseableTabbedPaneListener;

public final class LogVisualizerGUI extends JFrame {
    private static final FileFilter ASCENSION_LOG_FILTER = new FileFilter() {
        @Override
        public boolean accept(
                              final File f) {
            return f.isDirectory() || f.getName().toLowerCase().endsWith(".txt")
                   || f.getName().toLowerCase().endsWith(".xml");
        }

        @Override
        public String getDescription() {
            return "Logs";
        }
    };

    private final JMenu removeMenu;

    private final CloseableTabbedPane logsPane;

    public LogVisualizerGUI(
                            final LogLoaderListener logLoaderlistener) {
        super("Ascension Log Visualizer");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setPreferredSize(new Dimension(800, 600));

        if (logLoaderlistener == null)
            throw new NullPointerException("The LogLoaderListener must not be null.");

        File mafiaLogsDirectory = new File(Settings.getSettingString("Mafia logs location"));
        if (!mafiaLogsDirectory.exists())
            mafiaLogsDirectory = null;

        final JFileChooser logChooser = new JFileChooser(mafiaLogsDirectory);
        logChooser.setFileFilter(ASCENSION_LOG_FILTER);

        logsPane = new CloseableTabbedPane();
        logsPane.addCloseableTabbedPaneListener(new CloseableTabbedPaneListener() {
            public boolean closeTab(
                                    final int tabIndexToClose) {
                checkLogEdited(((LogGUI) logsPane.getComponentAt(tabIndexToClose)).getLogData());
                removeMenu.remove(tabIndexToClose);
                return true;
            }
        });

        removeMenu = new JMenu("Remove tab");
        final JMenuBar menuBar = new JMenuBar();
        final JMenu fileMenu = new JMenu("File");
        final JMenu extraMenu = new JMenu("Extra");
        final JMenu helpMenu = new JMenu("Help");

        final JCheckBoxMenuItem ascensionCountingMenu = new JCheckBoxMenuItem("Using old ascension day/turn counting",
                                                                              Settings.getSettingBoolean("Using old ascension counting"));
        ascensionCountingMenu.addItemListener(new ItemListener() {
            public void itemStateChanged(
                                         final ItemEvent e) {
                Settings.setSettingBoolean("Using old ascension counting",
                                           ascensionCountingMenu.isSelected());
            }
        });
        final JCheckBoxMenuItem mafiaNotesParsingMenu = new JCheckBoxMenuItem("Include mafia log notes",
                                                                              Settings.getSettingBoolean("Include mafia log notes"));
        mafiaNotesParsingMenu.addItemListener(new ItemListener() {
            public void itemStateChanged(
                                         final ItemEvent e) {
                Settings.setSettingBoolean("Include mafia log notes",
                                           mafiaNotesParsingMenu.isSelected());
            }
        });
        final JCheckBoxMenuItem showNonASCIIInLogsMenu = new JCheckBoxMenuItem("Show non-ASCII characters in parsed logs",
                                                                               Settings.getSettingBoolean("Show non-ASCII characters in parsed logs"));
        showNonASCIIInLogsMenu.addItemListener(new ItemListener() {
            public void itemStateChanged(
                                         final ItemEvent e) {
                Settings.setSettingBoolean("Show non-ASCII characters in parsed logs",
                                           showNonASCIIInLogsMenu.isSelected());
            }
        });

        fileMenu.add(new AbstractAction("Parse mafia logs") {
            public void actionPerformed(
                                        final ActionEvent arg0) {
                new InternalMafiaLogParserDialog(LogVisualizerGUI.this);
            }
        });
        fileMenu.add(new AbstractAction("Parse mafia logs with external parser") {
            public void actionPerformed(
                                        final ActionEvent arg0) {
                new ExternalMafiaLogParserDialog(LogVisualizerGUI.this);
            }
        });
        fileMenu.add(new AbstractAction("Visualize mafia logs") {
            public void actionPerformed(
                                        final ActionEvent arg0) {
                new MafiaLogsVisualizerDialog(LogVisualizerGUI.this,
                                              new AscensionLogLoaderListener() {
                                                  public void visualizeAscensionLog(
                                                                                    final File logFile) {
                                                      if (logFile.getName().endsWith(".xml"))
                                                          logLoaderlistener.loadXMLLog(logFile);
                                                      else
                                                          logLoaderlistener.loadMafiaLog(logFile);
                                                  }
                                              });
            }
        });
        fileMenu.add(new AbstractAction("Visualize preparsed ascension log") {
            public void actionPerformed(
                                        final ActionEvent arg0) {
                final int state = logChooser.showOpenDialog(null);
                if (state == JFileChooser.APPROVE_OPTION) {
                    setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

                    final File logFile = logChooser.getSelectedFile();
                    if (logFile.getName().endsWith(".txt"))
                        logLoaderlistener.loadPreparsedLog(logFile);
                    else
                        logLoaderlistener.loadXMLLog(logFile);

                    setCursor(Cursor.getDefaultCursor());
                }
            }
        });
        fileMenu.addSeparator();
        fileMenu.add(removeMenu);
        fileMenu.add(new AbstractAction("Remove all tabs") {
            public void actionPerformed(
                                        final ActionEvent arg0) {
                logsPane.removeAll();
                removeMenu.removeAll();

                // Try to explicitly reclaim memory previously used by the
                // removed LogGUIs. This method doesn't necessarily do the
                // garbage collecting right now, but one should at least
                // try.
                // This is done because multiple LogGUIs take up quite a bit
                // of memory and when the memory is mostly used up the
                // garbage collector starts multiple cycles, slowing down
                // the program in the process. This is an attempt to move
                // the garbage collection to a part of the program that is
                // not that performance critical.
                System.gc();
            }
        });
        fileMenu.addSeparator();
        fileMenu.add(ascensionCountingMenu);
        fileMenu.add(mafiaNotesParsingMenu);
        fileMenu.add(showNonASCIIInLogsMenu);
        fileMenu.addSeparator();
        fileMenu.add(new AbstractAction("Exit") {
            public void actionPerformed(
                                        final ActionEvent arg0) {
                System.exit(0);
            }
        });

        extraMenu.add(new AbstractAction("Notetaker") {
            public void actionPerformed(
                                        final ActionEvent arg0) {
                if (logsPane.getTabCount() == 0)
                    JOptionPane.showMessageDialog(LogVisualizerGUI.this,
                                                  "There has to be a log loaded to start the Notetaker with.",
                                                  "Problem occurred",
                                                  JOptionPane.WARNING_MESSAGE);
                else
                    Notetaker.showNotetaker(((LogGUI) logsPane.getSelectedComponent()).getLogData());
            }
        });
        extraMenu.add(new AbstractAction("Detailed Log Viewer") {
            public void actionPerformed(
                                        final ActionEvent arg0) {
                if (logsPane.getTabCount() == 0)
                    JOptionPane.showMessageDialog(LogVisualizerGUI.this,
                                                  "There has to be a log loaded to start the Detailed Log Viewer with.",
                                                  "Problem occurred",
                                                  JOptionPane.WARNING_MESSAGE);
                else
                    new DetailedLogViewer(((LogGUI) logsPane.getSelectedComponent()).getLogData());
            }
        });
        extraMenu.addSeparator();
        extraMenu.add(new AbstractAction("Look&Feel changer") {
            public void actionPerformed(
                                        final ActionEvent arg0) {
                new LafChangerDialog(LogVisualizerGUI.this);
            }
        });
        extraMenu.addSeparator();
        extraMenu.add(new AbstractAction("Reset data files") {
            public void actionPerformed(
                                        final ActionEvent arg0) {
                final int state = JOptionPane.showConfirmDialog(LogVisualizerGUI.this,
                                                                "<html>You may have to restart the program before changes will take effect."
                                                                        + "<p>All manual changes to the data files you may have done will be overwritten."
                                                                        + "<p>Continue?</html>",
                                                                "Manual changes will be overwritten",
                                                                JOptionPane.OK_CANCEL_OPTION,
                                                                JOptionPane.INFORMATION_MESSAGE);
                if (state == JOptionPane.OK_OPTION) {
                    for (final File file : UtilityConstants.KOL_DATA_LOCATION.listFiles())
                        if (!file.isDirectory())
                            file.delete();

                    LogVisualizer.writeDataFilesToFileSystem();
                    DataTablesHandler.HANDLER.reloadDataTables();
                }
            }
        });
        extraMenu.add(new AbstractAction("Edit data files"){
            public void actionPerformed(
                                        ActionEvent e) {
                DataTablesEditor.showDataTablesEditor();
            }
        });

        final JCheckBoxMenuItem updatesCheckMenu = new JCheckBoxMenuItem("Automatically check for newer versions",
                                                                         Settings.getSettingBoolean("Check Updates"));
        updatesCheckMenu.addItemListener(new ItemListener() {
            public void itemStateChanged(
                                         final ItemEvent e) {
                Settings.setSettingBoolean("Check Updates", updatesCheckMenu.isSelected());
            }
        });
        helpMenu.add(updatesCheckMenu);
        helpMenu.add(new AbstractAction("Check Project News") {
            public void actionPerformed(
                                        final ActionEvent arg0) {
                ProjectUpdateViewer.showProjectUpdateViewer();
            }
        });
        helpMenu.add(new AbstractAction("About Licenses") {
            public void actionPerformed(
                                        final ActionEvent arg0) {
                new LicenseViewer(LogVisualizerGUI.this);
            }
        });
        helpMenu.addSeparator();
        helpMenu.add("Version: " + Settings.getSettingString("Version"));

        menuBar.add(fileMenu);
        menuBar.add(extraMenu);
        menuBar.add(helpMenu);
        setJMenuBar(menuBar);
        add(logsPane);
    }

    private void checkLogEdited(
                                final LogDataHolder logData) {
        if (logData.isEdited()) {
            final int state = JOptionPane.showConfirmDialog(this,
                                                            "<html>The log you are about to close was edited.<br><br>Should it be saved to a file?</html>",
                                                            "Should the log be saved?",
                                                            JOptionPane.YES_NO_OPTION,
                                                            JOptionPane.INFORMATION_MESSAGE);
            if (state == JOptionPane.YES_OPTION)
                ExportDialog.showExportDialog(this, logData, LogOutputFormat.XML_LOG);
        }
    }

    /**
     * Adds the LogGUI to the main tabbed pane. Also adds an item to the remove
     * menu.
     * 
     * @param logPanel
     *            The log panel to be added to the tabbed log pane.
     */
    public void addLogTab(
                          final LogGUI logPanel) {
        logsPane.add(logPanel.getLogName(), logPanel);
        removeMenu.add(new RemoveMenuItem(logPanel.getLogName()));
    }

    private void removeLogTab(
                              final int tabIndex) {
        checkLogEdited(((LogGUI) logsPane.getComponentAt(tabIndex)).getLogData());
        logsPane.remove(tabIndex);
        removeMenu.remove(tabIndex);

        // Try to explicitly reclaim memory previously used by the removed
        // LogGUI. This method doesn't necessarily do the garbage collecting
        // right now, but one should at least try.
        // This is done because multiple LogGUIs take up quite a bit of memory
        // and when the memory is mostly used up the garbage collector starts
        // multiple cycles, slowing down the program in the process. This is an
        // attempt to move the garbage collection to a part of the program that
        // is not that performance critical.
        System.gc();
    }

    /**
     * A class to handle removing of log tabs through the frame menu bar.
     */
    private final class RemoveMenuItem extends JMenuItem {
        RemoveMenuItem(
                       final String tabName) {
            super(tabName);
            addActionListener(new ActionListener() {
                public void actionPerformed(
                                            final ActionEvent e) {
                    int index = 0;
                    for (int i = 0; i < logsPane.getTabCount(); i++)
                        if (logsPane.getTitleAt(i).equals(getText()))
                            index = i;

                    removeLogTab(index);
                }
            });
        }
    }

    /**
     * A listener to handle parsing and showing of ascension logs.
     */
    public interface LogLoaderListener {
        /**
         * @param file
         *            A mafia ascension log.
         */
        public void loadMafiaLog(
                                 File file);

        /**
         * @param file
         *            A XML ascension log.
         */
        public void loadXMLLog(
                               File file);

        /**
         * @param file
         *            A preparsed ascension log.
         */
        public void loadPreparsedLog(
                                     File file);
    }
}
