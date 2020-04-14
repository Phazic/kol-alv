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
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.*;

import net.java.swingfx.waitwithstyle.PerformanceInfiniteProgressPanel;

import org.jfree.ui.RefineryUtilities;

import com.googlecode.logVisualizer.Settings;
import com.googlecode.logVisualizer.creator.LogsCreator;
import com.googlecode.logVisualizer.logData.turn.Encounter;
import com.googlecode.logVisualizer.util.LogOutputFormat;
import com.googlecode.logVisualizer.util.Pair;

/**
 * Dialog giving all controls necessary to parse mafia logs into ascension logs
 * using the internal parser of the Ascension Log Visualizer.
 */
public final class InternalMafiaLogParserDialog extends JDialog {
    public static final FilenameFilter MAFIA_LOG_FILTER = new FilenameFilter() {
        private final Matcher mafiaLogMatcher = Pattern.compile(".*_\\d+\\.txt$").matcher("");

        private final String preparsedLogPartialFileString = "_ascend";

        public boolean accept(
                              final File dir, final String name) {
            return mafiaLogMatcher.reset(name).matches()
                   && !name.contains(preparsedLogPartialFileString);
        }
    };

    private final ActionListener runParserAction = new ActionListener() {
        public void actionPerformed(
                                    final ActionEvent e) {
            if (!mafiaLogsDirectoryField.getText().equals("")
                && !parsedLogsSavingDirectoryField.getText().equals("")) {
                setWaitingForComputationEnd(true);
                runParser();
            } else
                JOptionPane.showMessageDialog(null,
                                              "Please fill out all text fields.",
                                              "Missing input",
                                              JOptionPane.WARNING_MESSAGE);
        }
    };

    private final JTextField mafiaLogsDirectoryField;

    private final JTextField parsedLogsSavingDirectoryField;

    private final JFileChooser directoryChooser;

    private final JSpinner numberToParseSpinner;

    private LogOutputFormat logOutputFormat = LogOutputFormat.TEXT_LOG;

    InternalMafiaLogParserDialog(
                                 final JFrame owner) {
        super(owner, true);
        setLayout(new BorderLayout(5, 20));
        setTitle("Mafia Logs Parser");
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setGlassPane(new PerformanceInfiniteProgressPanel());

        mafiaLogsDirectoryField = new JTextField(Settings.getString("Mafia logs location"));
        parsedLogsSavingDirectoryField = new JTextField(Settings.getString("Parsed logs saving location"));

        File mafiaLogsDirectory = new File(mafiaLogsDirectoryField.getText());
        if (!mafiaLogsDirectory.exists())
            mafiaLogsDirectory = null;

        directoryChooser = new JFileChooser(mafiaLogsDirectory);
        directoryChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        numberToParseSpinner = new JSpinner(new SpinnerNumberModel(0, 0, Integer.MAX_VALUE, 1));

        final JButton runButton = new JButton("Run parser");
        final JButton cancelButton = new JButton("Cancel");
        runButton.addActionListener(runParserAction);
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(
                                        final ActionEvent e) {
                dispose();
            }
        });

        final JPanel parserPanel = new JPanel(new BorderLayout(5, 5));
        final JPanel directoryPanel = new JPanel(new GridLayout(0, 1, 5, 5));
        directoryPanel.add(createDirectoryFinderPanel(mafiaLogsDirectoryField,
                                                      "Mafia logs directory location"));
        directoryPanel.add(createDirectoryFinderPanel(parsedLogsSavingDirectoryField,
                                                      "Parsed logs saving destination"));
        parserPanel.add(directoryPanel, BorderLayout.CENTER);
        parserPanel.add(createLogOutputCustomizer(), BorderLayout.SOUTH);
        add(parserPanel, BorderLayout.NORTH);

        add(new JLabel("<html>Note that the parsing process may take a while depending on the amount and contents<br>of the mafia logs.</html>"),
            BorderLayout.CENTER);

        final JPanel buttonPanel = new JPanel(new GridLayout(1, 0, 10, 0));
        buttonPanel.setPreferredSize(new Dimension(150, 50));
        buttonPanel.add(runButton);
        buttonPanel.add(cancelButton);
        add(buttonPanel, BorderLayout.SOUTH);

        pack();
        RefineryUtilities.centerFrameOnScreen(this);
        setVisible(true);
    }

    private JPanel createDirectoryFinderPanel(
                                              final JTextField directoryLocationField,
                                              final String description) {
        final JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder(description));

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
        panel.add(directoryLocationField, gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.insets = new Insets(5, 25, 5, 10);
        panel.add(directoryChooserButton, gbc);

        directoryLocationField.addActionListener(runParserAction);

        directoryChooserButton.addActionListener(new ActionListener() {

            public void actionPerformed(
                                        final ActionEvent e) {
                final int state = directoryChooser.showOpenDialog(null);
                if (state == JFileChooser.APPROVE_OPTION)
                    directoryLocationField.setText(directoryChooser.getSelectedFile()
                                                                   .getAbsolutePath());
            }
        });

        return panel;
    }

    private JPanel createLogOutputCustomizer() {
        final JPanel panel = new JPanel(new BorderLayout(5, 5));

        final JPanel logsAmountPanel = new JPanel(new GridBagLayout());
        final JLabel label = new JLabel("Parse the last n ascensions (0 to parse all):");

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 10, 0, 15);
        logsAmountPanel.add(label, gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.insets = new Insets(0, 0, 0, 10);
        logsAmountPanel.add(numberToParseSpinner, gbc);

        panel.add(logsAmountPanel, BorderLayout.NORTH);
        panel.add(createOutputFormatChooserPanel(), BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createOutputFormatChooserPanel() {
        final JPanel panel = new JPanel(new GridLayout(1, 0, 10, 10));
        final JRadioButton textButton = new JRadioButton("Text", true);
        final JRadioButton htmlButton = new JRadioButton("HTML", false);
        final JRadioButton bbcodeButton = new JRadioButton("BBCode", false);
        final JRadioButton xmlButton = new JRadioButton("XML", false);
        final ButtonGroup group = new ButtonGroup();
        group.add(textButton);
        group.add(htmlButton);
        group.add(bbcodeButton);
        group.add(xmlButton);

        final ActionListener listener = new ActionListener() {
            public void actionPerformed(
                                        final ActionEvent e) {
                if (htmlButton.isSelected())
                    logOutputFormat = LogOutputFormat.HTML_LOG;
                else if (bbcodeButton.isSelected())
                    logOutputFormat = LogOutputFormat.BBCODE_LOG;
                else
                    logOutputFormat = LogOutputFormat.TEXT_LOG;
            }
        };
        textButton.addActionListener(listener);
        htmlButton.addActionListener(listener);
        bbcodeButton.addActionListener(listener);
        xmlButton.addActionListener(new ActionListener() {
            public void actionPerformed(
                                        final ActionEvent e) {
                logOutputFormat = LogOutputFormat.XML_LOG;
            }
        });

        panel.add(textButton);
        panel.add(htmlButton);
        panel.add(bbcodeButton);
        panel.add(xmlButton);

        return panel;
    }

    /**
     * @param isComputationNotDone
     *            A flag showing whether the computation has ended or not.
     */
    private void setWaitingForComputationEnd(
                                             final boolean isComputationNotDone) {
        getGlassPane().setVisible(isComputationNotDone);
    }

    /**
     * Runs the parser with the data from the TextFields of the GUI.
     */
    private void runParser() {
        final File mafiaLogsDirectory = new File(mafiaLogsDirectoryField.getText());
        final File parsedLogsSavingDirectory = new File(parsedLogsSavingDirectoryField.getText());

        if (!mafiaLogsDirectory.isDirectory() || !parsedLogsSavingDirectory.isDirectory()) {
            setWaitingForComputationEnd(false);
            JOptionPane.showMessageDialog(null,
                                          "Please only specify existing directories.",
                                          "Problem occurred",
                                          JOptionPane.WARNING_MESSAGE);
            return;
        }

        final File[] mafiaLogs = mafiaLogsDirectory.listFiles(MAFIA_LOG_FILTER);
        if (mafiaLogs.length == 0) {
            setWaitingForComputationEnd(false);
            JOptionPane.showMessageDialog(null,
                                          "The directory specified for mafia logs does not contain any mafia logs.",
                                          "Problem occurred",
                                          JOptionPane.WARNING_MESSAGE);
            return;
        }

        // If the input seems to be correct, save the directories used.
        Settings.setString("Mafia logs location", mafiaLogsDirectoryField.getText());
        Settings.setString("Parsed logs saving location",
                                  parsedLogsSavingDirectoryField.getText());

        final ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(new Runnable() {
            public void run() {
                try {
                    final int logToParse = ((Integer) numberToParseSpinner.getModel().getValue()).intValue();
                    final List<Pair<String, Encounter>> errorFileList = LogsCreator.createParsedLogs(mafiaLogs,
                                                                                                    parsedLogsSavingDirectory,
                                                                                                    logOutputFormat,
                                                                                                    logToParse > 0 ? logToParse
                                                                                                                  : Integer.MAX_VALUE);

                    EventQueue.invokeLater(new Runnable() {
                        public void run() {
                            // If there were error logs, give the user feedback
                            // on them.
                            if (!errorFileList.isEmpty())
                                ErrorLogTableDialog.showErrorLogTableDialog(InternalMafiaLogParserDialog.this,
                                                                            errorFileList);

                            dispose();
                        }
                    });
                } catch (final IOException e) {
                    setWaitingForComputationEnd(false);
                    JOptionPane.showMessageDialog(null,
                                                  "There was a problem while running the parser. Please check whether the parsed logs were created.",
                                                  "Error occurred",
                                                  JOptionPane.ERROR_MESSAGE);
                    e.printStackTrace();
                }
            }
        });
        executor.shutdown();
    }
}
