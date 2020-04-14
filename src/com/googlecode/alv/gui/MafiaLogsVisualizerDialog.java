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

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.AbstractTableModel;

import net.java.dev.spellcast.utilities.UtilityConstants;
import net.java.swingfx.waitwithstyle.PerformanceInfiniteProgressPanel;

import org.jfree.ui.RefineryUtilities;

import com.googlecode.alv.Settings;
import com.googlecode.alv.creator.LogsCreator;
import com.googlecode.alv.creator.XMLLogCreator;
import com.googlecode.alv.creator.util.XMLLogReader;
import com.googlecode.alv.logData.turn.Encounter;
import com.googlecode.alv.parser.UsefulPatterns;
import com.googlecode.alv.util.Lists;
import com.googlecode.alv.util.LogsCache;
import com.googlecode.alv.util.Pair;

/**
 * A dialog to select mafia logs for direct visualisation without having to
 * first turn them into parsed ascension logs.
 * <p>
 * This class gives controls to select mafia logs for visualisation and uses a
 * caching mechanism based on the Ascension Log XML format (see
 * {@link XMLLogCreator} and {@link XMLLogReader}) in the background to make
 * future calls faster. As such, logs to be visualised are in the Ascension Log
 * XML format and are delegated from this class through the
 * {@link AscensionLogLoaderListener} instance which is needed for the
 * constructor.
 * <p>
 * Note that the ascension log delegation through the
 * {@link AscensionLogLoaderListener} interface must be able to handle
 * concurrent method calls from this class.
 */
final class MafiaLogsVisualizerDialog extends JDialog {
    private static final FilenameFilter MAFIA_LOG_FILTER = new FilenameFilter() {
        private final Matcher mafiaLogMatcher = Pattern.compile(".*_\\d+\\.txt$").matcher("");

        private final String preparsedLogPartialFileString = "_ascend";

        public boolean accept(
                              final File dir, final String name) {
            return mafiaLogMatcher.reset(name).matches()
                   && !name.contains(preparsedLogPartialFileString);
        }
    };

    private static final List<File> EMPTY_MAFIA_LOGS_LIST = Collections.emptyList();

    private final TaskQueue taskQueue = new TaskQueue();

    private final LogsCache logsCache = LogsCache.CACHE;

    private final JTextField mafiaLogsDirectoryField;

    private final JTable visualizableAscensionLogsTable;

    private final JCheckBox toggleAllBox;

    private final AscensionLogLoaderListener ascensionLogLoaderListener;

    /**
     * Constructs the object.
     * <p>
     * Note that the mafia log delegation through the
     * {@link AscensionLogLoaderListener} interface must be able to handle
     * concurrent method calls from this class.
     * 
     * @param owner
     *            The owner of this dialog.
     * @param mafiaLogLoaderListener
     *            The interface through which the selected condensed mafia logs
     *            are delegated to another class.
     * @throws NullPointerException
     *             if owner is {@code null}; if mafiaLogLoaderListener is
     *             {@code null}
     */
    MafiaLogsVisualizerDialog(
                              final JFrame owner,
                              final AscensionLogLoaderListener mafiaLogLoaderListener) {
        super(owner, true);

        if (mafiaLogLoaderListener == null)
            throw new NullPointerException("mafiaLogLoaderListener must not be null.");

        setLayout(new BorderLayout(5, 10));
        setTitle("Mafia Logs Parser");
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setGlassPane(new PerformanceInfiniteProgressPanel());

        ascensionLogLoaderListener = mafiaLogLoaderListener;
        visualizableAscensionLogsTable = new JTable(new AscensionLogsTableModel(EMPTY_MAFIA_LOGS_LIST));
        toggleAllBox = new JCheckBox("Visualize all logs");
        toggleAllBox.addChangeListener(new ChangeListener() {
            public void stateChanged(
                                     final ChangeEvent e) {
                if (toggleAllBox.hasFocus())
                    ((AscensionLogsTableModel) visualizableAscensionLogsTable.getModel()).setVisualizeAll(toggleAllBox.isSelected());
            }
        });
        mafiaLogsDirectoryField = new JTextField(Settings.getString("Mafia logs location"));
        mafiaLogsDirectoryField.addActionListener(new ActionListener() {
            public void actionPerformed(
                                        final ActionEvent e) {
                logsCache.deleteCache();
                createAscensionLogsTable();
            }
        });
        createAscensionLogsTable();

        final JButton runButton = new JButton("Run parser");
        final JButton cancelButton = new JButton("Cancel");
        runButton.addActionListener(new ActionListener() {
            public void actionPerformed(
                                        final ActionEvent e) {
                if (((AscensionLogsTableModel) visualizableAscensionLogsTable.getModel()).isVisualizationsOccur()) {
                    runParser();
                    taskQueue.runBackgroundTask(new Runnable() {
                        public void run() {
                            EventQueue.invokeLater(new Runnable() {
                                public void run() {
                                    dispose();
                                }
                            });
                        }
                    });
                    taskQueue.waitForComputationEnd();
                } else
                    JOptionPane.showMessageDialog(MafiaLogsVisualizerDialog.this,
                                                  "There are no mafia logs selected to be visualized.",
                                                  "Nothing to visualize",
                                                  JOptionPane.INFORMATION_MESSAGE);
            }
        });
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(
                                        final ActionEvent e) {
                dispose();
            }
        });

        add(createMafiaLogsDirectoryFinderPanel(), BorderLayout.NORTH);

        add(createAscensionLogsTablePanel(), BorderLayout.CENTER);

        final JPanel buttonPanel = new JPanel(new GridLayout(1, 0, 10, 0));
        buttonPanel.setPreferredSize(new Dimension(150, 50));
        buttonPanel.add(runButton);
        buttonPanel.add(cancelButton);
        add(buttonPanel, BorderLayout.SOUTH);

        pack();
        final Dimension currentSize = getSize();
        if (currentSize.height < 500) {
            currentSize.height = 500;
            setSize(currentSize);
        } else if (currentSize.height > 700) {
            currentSize.height = 700;
            setSize(currentSize);
        }
        RefineryUtilities.centerFrameOnScreen(this);
        setVisible(true);
    }

    private JPanel createMafiaLogsDirectoryFinderPanel() {
        final JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Mafia logs location"));

        final JButton directoryChooserButton = new JButton("Find Directory");
        GridBagConstraints gbc;

        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.insets = new Insets(5, 10, 5, 0);
        panel.add(mafiaLogsDirectoryField, gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.insets = new Insets(5, 25, 5, 10);
        panel.add(directoryChooserButton, gbc);

        File mafiaLogsDirectory = new File(Settings.getString("Mafia logs location"));
        if (!mafiaLogsDirectory.exists())
            mafiaLogsDirectory = null;

        final JFileChooser directoryChooser = new JFileChooser(mafiaLogsDirectory);
        directoryChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        directoryChooserButton.addActionListener(new ActionListener() {
            public void actionPerformed(
                                        final ActionEvent e) {
                final int state = directoryChooser.showOpenDialog(null);
                if (state == JFileChooser.APPROVE_OPTION) {
                    mafiaLogsDirectoryField.setText(directoryChooser.getSelectedFile()
                                                                    .getAbsolutePath());
                    logsCache.deleteCache();
                    createAscensionLogsTable();
                }
            }
        });

        return panel;
    }

    private JPanel createAscensionLogsTablePanel() {
        final JPanel panel = new JPanel(new BorderLayout(5, 10));

        final JPanel bottomPanel = new JPanel(new GridBagLayout());
        final JButton refreshButton = new JButton("Refresh logs list");
        GridBagConstraints gbc;

        refreshButton.setToolTipText("Reload and refresh the logs cache with the list of logs from the given directory.");

        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 10, 5, 0);
        bottomPanel.add(toggleAllBox, gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.insets = new Insets(0, 0, 5, 10);
        bottomPanel.add(refreshButton, gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.insets = new Insets(10, 5, 5, 5);
        bottomPanel.add(new JLabel("<html>Note that depending on the number of ascension logs selected and the amount and contents in those logs"
                                   + "<p>the parsing process may take a while to finish."
                                   + "<p><p>Also, note that it is not advisable to have much more than 10 ascension log charts open at the same time.</html>"),
                        gbc);

        final JScrollPane scrollPane = new JScrollPane(visualizableAscensionLogsTable);
        scrollPane.setPreferredSize(new Dimension(300, 300));

        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(bottomPanel, BorderLayout.SOUTH);

        refreshButton.addActionListener(new ActionListener() {
            public void actionPerformed(
                                        final ActionEvent e) {
                logsCache.deleteCache();
                createAscensionLogsTable();
            }
        });

        return panel;
    }

    private void createAscensionLogsTable() {
        final File mafiaLogsDirectory = new File(mafiaLogsDirectoryField.getText());
        if (!mafiaLogsDirectory.exists() || !mafiaLogsDirectory.isDirectory()) {
            JOptionPane.showMessageDialog(MafiaLogsVisualizerDialog.this,
                                          "Please only specify existing directories.",
                                          "Problem occurred",
                                          JOptionPane.WARNING_MESSAGE);
            return;
        }

        final File[] mafiaLogs = mafiaLogsDirectory.listFiles(MAFIA_LOG_FILTER);
        if (mafiaLogs.length == 0) {
            JOptionPane.showMessageDialog(MafiaLogsVisualizerDialog.this,
                                          "The specified directory does not contain any mafia logs.",
                                          "Problem occurred",
                                          JOptionPane.WARNING_MESSAGE);
            return;
        }

        // If the input seems to be correct, save the directory used.
        Settings.setString("Mafia logs location", mafiaLogsDirectoryField.getText());

        // In case there are still some logs in the temporary data directory
        // delete all of its contents.
        for (final File f : UtilityConstants.TEMP_LOCATION.listFiles())
            if (!f.isDirectory())
                f.delete();

        // Start the actual computation.
        taskQueue.runBackgroundTask(new Runnable() {
            public void run() {
                try {
                    createDataTable(mafiaLogs);
                } catch (final IOException e) {
                    e.printStackTrace();
                    EventQueue.invokeLater(new Runnable() {
                        public void run() {
                            JOptionPane.showMessageDialog(MafiaLogsVisualizerDialog.this,
                                                          "<html>One or more of the mafia logs couldn't be properly read.<br><br>"
                                                                  + "Check whether there is any corruption in the session logs and try again. If it still doesn't work,<br>get in contact with one of the developers.</html>",
                                                          "Parsing error",
                                                          JOptionPane.ERROR_MESSAGE);
                        }
                    });
                }
            }
        });
        taskQueue.waitForComputationEnd();
    }

    private void createDataTable(
                                 final File[] mafiaLogs)
                                                        throws IOException {
        Arrays.sort(mafiaLogs, new Comparator<File>() {
            public int compare(
                               final File o1, final File o2) {
                return o1.getName().compareToIgnoreCase(o2.getName());
            }
        });

        final List<Pair<String, Encounter>> errorFileList;

        // If the cache is empty, we need to create all logs. Otherwise, we only
        // need to re-parse the last cached ascension (in case more turns were
        // played) and the ascensions that follow it.
        if (logsCache.getLogsByCharacter().isEmpty())
            errorFileList = logsCache.createCache(LogsCreator.createCondensedMafiaLogs(mafiaLogs));
        else {
            final Map<String, List<File>> cachedLogs = logsCache.getLogsByCharacter();
            final List<Pair<String, String>> lastLogsCached = Lists.newArrayList(cachedLogs.size());
            for (final String character : cachedLogs.keySet()) {
                final List<File> logs = cachedLogs.get(character);
                final File lastLog = logs.get(logs.size() - 1);

                lastLogsCached.add(Pair.of(character, lastLog.getName().replace(".xml", "")));
            }

            final List<File> logsToParse = Lists.newArrayList();
            for (final Pair<String, String> lastLogCached : lastLogsCached) {
                boolean isLastCachedLogFound = false;
                for (final File log : mafiaLogs) {
                    final int delimiterIndex = log.getName().lastIndexOf("_");
                    final String characterName = log.getName()
                                                    .substring(0, delimiterIndex)
                                                    .replaceAll("_", " ");

                    if (characterName.equals(lastLogCached.getVar1()))
                        if (!isLastCachedLogFound) {
                            final String date = Integer.toString(UsefulPatterns.getMafiaLogDate(log));
                            if (lastLogCached.getVar2().endsWith(date)) {
                                logsToParse.add(log);
                                isLastCachedLogFound = true;
                            }
                        } else
                            logsToParse.add(log);
                }
            }

            errorFileList = logsCache.createCache(LogsCreator.createCondensedMafiaLogs(logsToParse.toArray(new File[0])));
        }

        EventQueue.invokeLater(new Runnable() {
            public void run() {
                ((AscensionLogsTableModel) visualizableAscensionLogsTable.getModel()).setAscensionLogs(logsCache.getLogs());
                toggleAllBox.setSelected(false);

                // If there were error logs, give the user feedback on them.
                if (!errorFileList.isEmpty())
                    ErrorLogTableDialog.showErrorLogTableDialog(MafiaLogsVisualizerDialog.this,
                                                                errorFileList);
            }
        });
    }

    private void runParser() {
        taskQueue.runBackgroundTask(new Runnable() {
            public void run() {
                // 4 Threads per CPU should be a high enough number to not slow
                // the computation too much down by scheduler overhead while
                // still making use of threaded computing.
                final ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime()
                                                                                     .availableProcessors() * 4);

                for (final File f : ((AscensionLogsTableModel) visualizableAscensionLogsTable.getModel()).getVisualizableMafiaLogs())
                    executor.execute(new Runnable() {
                        public void run() {
                            ascensionLogLoaderListener.visualizeAscensionLog(f);
                        }
                    });

                // Wait for all threads to finish.
                executor.shutdown();
                try {
                    executor.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
                } catch (final InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void dispose() {
        // Wait for any leftover background tasks to finish.
        taskQueue.waitForComputationEnd();

        // Delete any leftover temporary data.
        for (final File f : UtilityConstants.TEMP_LOCATION.listFiles())
            if (!f.isDirectory())
                f.delete();

        super.dispose();
    }

    /**
     * Helper class to manage background tasks and wait for them to finish when
     * needed.
     * <p>
     * The current implementation is a bit wasteful by often recreating its
     * ExecutorService, but its performance is adequate enough for this
     * particular use-case.
     */
    private final class TaskQueue {
        private ExecutorService taskQueue;

        private final Lock lock = new ReentrantLock(true);

        /**
         * Adds a task to the task queue. The given task will be directly
         * executed in background if no other task is already being executed,
         * otherwise it will wait for all other tasks added before it to finish
         * before executing itself.
         * 
         * @param r
         *            The task to be executed.
         */
        private void runBackgroundTask(
                                       final Runnable r) {
            if (taskQueue == null || taskQueue.isTerminated()) {
                taskQueue = Executors.newSingleThreadExecutor();
                taskQueue.execute(r);
            } else if (taskQueue.isShutdown())
                new Thread(new Runnable() {
                    public void run() {
                        lock.lock();
                        try {
                            taskQueue = Executors.newSingleThreadExecutor();
                            taskQueue.execute(r);
                        } finally {
                            lock.unlock();
                        }
                    }
                }).start();
            else
                taskQueue.execute(r);
        }

        /**
         * A non-blocking method call that that makes the UI unusable and
         * displays a wait-animation until all pending tasks are finished.
         */
        private void waitForComputationEnd() {
            if (taskQueue == null || taskQueue.isShutdown())
                return;

            EventQueue.invokeLater(new Runnable() {
                public void run() {
                    getGlassPane().setVisible(true);
                }
            });
            new Thread(new Runnable() {
                public void run() {
                    lock.lock();
                    try {
                        taskQueue.shutdown();
                        taskQueue.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
                    } catch (final InterruptedException e) {
                        e.printStackTrace();
                    } finally {
                        EventQueue.invokeLater(new Runnable() {
                            public void run() {
                                getGlassPane().setVisible(false);
                            }
                        });
                        lock.unlock();
                    }
                }
            }).start();
        }
    }

    /**
     * TableModel used by the JTable which handles the selection of detailed
     * ascension logs which should be visualised.
     */
    private static final class AscensionLogsTableModel extends AbstractTableModel {
        private static final String[] columnNames = { "Mafia log", "Should be visualized?" };

        private List<File> ascensionLogs;

        private List<Boolean> visualizables;

        AscensionLogsTableModel(
                                final Collection<File> ascensionLogs) {
            this.ascensionLogs = Lists.newArrayList(ascensionLogs);
            visualizables = Lists.newArrayList(ascensionLogs.size() + 1);
            for (int i = 0; i < ascensionLogs.size(); i++)
                visualizables.add(false);
        }

        @Override
        public Class<?> getColumnClass(
                                       final int columnIndex) {
            if (columnIndex == 1)
                return Boolean.class;

            return String.class;
        }

        @Override
        public boolean isCellEditable(
                                      final int rowIndex, final int columnIndex) {
            return columnIndex == 1;
        }

        @Override
        public void setValueAt(
                               final Object aValue, final int rowIndex, final int columnIndex) {
            visualizables.set(rowIndex, (Boolean) aValue);
        }

        @Override
        public String getColumnName(
                                    final int column) {
            return columnNames[column];
        }

        public int getColumnCount() {
            return columnNames.length;
        }

        public int getRowCount() {
            return ascensionLogs.size();
        }

        public Object getValueAt(
                                 final int rowIndex, final int columnIndex) {
            return columnIndex == 0 ? ascensionLogs.get(rowIndex).getName().replace(".xml", "")
                                   : visualizables.get(rowIndex);
        }

        /**
         * Sets the contents of this model to the given collection of detailed
         * ascension logs.
         * 
         * @param ascensionLogs
         *            A collection of detailed ascension logs.
         */
        void setAscensionLogs(
                              final Collection<File> ascensionLogs) {
            this.ascensionLogs = Lists.newArrayList(ascensionLogs);
            visualizables = Lists.newArrayList(ascensionLogs.size());
            for (int i = 0; i < ascensionLogs.size(); i++)
                visualizables.add(false);

            fireTableDataChanged();
        }

        /**
         * @return The detailed ascension logs which should be visualised.
         */
        List<File> getVisualizableMafiaLogs() {
            final List<File> visualizableMafiaLogs = Lists.newArrayList(ascensionLogs.size());

            for (int i = 0; i < visualizables.size(); i++)
                if (visualizables.get(i))
                    visualizableMafiaLogs.add(ascensionLogs.get(i));

            return visualizableMafiaLogs;
        }

        /**
         * @param isVisualizeAll
         *            A flag showing whether all detailed ascension logs inside
         *            this model should be visualised or not.
         */
        void setVisualizeAll(
                             final boolean isVisualizeAll) {
            Collections.fill(visualizables, isVisualizeAll);
            fireTableDataChanged();
        }

        /**
         * @return {@code true} in case at least one detailed ascension log from
         *         inside this model should be visualised.
         */
        boolean isVisualizationsOccur() {
            return visualizables.size() > 0 ? visualizables.contains(true) : false;
        }
    }

    /**
     * Interface used to delegate the visualisation of mafia logs to the place
     * where it is actually handled (which is not inside the
     * {@link MafiaLogsVisualizerDialog} class).
     */
    public static interface AscensionLogLoaderListener {
        public void visualizeAscensionLog(
                                          final File ascensionLog);
    }
}
