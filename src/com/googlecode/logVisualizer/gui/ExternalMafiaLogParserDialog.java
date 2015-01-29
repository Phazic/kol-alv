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

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;

import net.java.swingfx.waitwithstyle.PerformanceInfiniteProgressPanel;

import org.jfree.ui.RefineryUtilities;

import com.googlecode.logVisualizer.Settings;

/**
 * A dialog to give the possibility to use the AFH MafiaLog Parser from inside
 * the Ascension Log Visualizer as long as the command line of the machine which
 * is running this program can correctly handle the command {@code perl}.
 * (Basically, this means that there has to be a Perl Runtime Environment
 * installed on the given machine)
 */
final class ExternalMafiaLogParserDialog extends JDialog {

    private static final FileFilter PERL_SCRIPT_FILTER = new FileFilter() {
        @Override
        public boolean accept(
                              final File f) {
            return f.isDirectory() || f.getName().toLowerCase().endsWith(".pl");
        }

        @Override
        public String getDescription() {
            return "Perl Script";
        }
    };

    private final ActionListener runAFHParserAction = new ActionListener() {
        public void actionPerformed(
                                    final ActionEvent e) {
            if (!fileLocationField.getText().equals("") && !argumentsField.getText().equals("")
                && !userNameField.getText().equals("")) {
                setWaitingForProcessEnd(true);
                runAFHParser();
            } else
                JOptionPane.showMessageDialog(null,
                                              "Please fill out all text fields.",
                                              "Missing input",
                                              JOptionPane.WARNING_MESSAGE);
        }
    };

    private JTextField fileLocationField;

    private JTextField argumentsField;

    private JTextField userNameField;

    ExternalMafiaLogParserDialog(
                                 final JFrame owner) {
        super(owner, true);
        setLayout(new BorderLayout(5, 10));
        setTitle("External Mafia Log Parser");
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setGlassPane(new PerformanceInfiniteProgressPanel());

        final JButton runButton = new JButton("Run parser");
        final JButton cancelButton = new JButton("Cancel");
        runButton.addActionListener(runAFHParserAction);
        cancelButton.addActionListener(new ActionListener() {

            public void actionPerformed(
                                        final ActionEvent e) {
                dispose();
            }
        });

        final JPanel afhParserPanel = new JPanel(new GridLayout(0, 1, 5, 5));
        afhParserPanel.add(createAFHParserLocationPanel());
        afhParserPanel.add(createAFHParserOptionsPanel());
        add(afhParserPanel, BorderLayout.NORTH);

        add(new JLabel("<html>Please refer to the AFH MafiaLog Parser documention on how to use that parser."
                       + "<p>Note that the AFH MafiaLog Parser is not distributed together with this program.</html>"),
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

    private JPanel createAFHParserLocationPanel() {
        final JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("AFH MafiaLog Parser location"));

        fileLocationField = new JTextField(Settings.getSettingString("AFH Parser location"));
        final JButton fileChooserButton = new JButton("Find File");
        GridBagConstraints gbc;

        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.insets = new Insets(5, 10, 5, 0);
        panel.add(fileLocationField, gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.insets = new Insets(5, 25, 5, 10);
        panel.add(fileChooserButton, gbc);

        fileLocationField.addActionListener(runAFHParserAction);

        File mafiaLogsDirectory = new File(Settings.getSettingString("Mafia logs location"));
        if (!mafiaLogsDirectory.exists())
            mafiaLogsDirectory = null;

        final JFileChooser logChooser = new JFileChooser(mafiaLogsDirectory);
        logChooser.setFileFilter(PERL_SCRIPT_FILTER);
        fileChooserButton.addActionListener(new ActionListener() {

            public void actionPerformed(
                                        final ActionEvent e) {
                final int state = logChooser.showOpenDialog(null);
                if (state == JFileChooser.APPROVE_OPTION)
                    fileLocationField.setText(logChooser.getSelectedFile().getAbsolutePath());
            }
        });

        return panel;
    }

    private JPanel createAFHParserOptionsPanel() {
        final JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("AFH MafiaLog Parser options"));

        argumentsField = new JTextField(Settings.getSettingString("AFH Parser arguments"));
        userNameField = new JTextField(Settings.getSettingString("AFH Parser user name"));
        final JLabel argumentsLabel = new JLabel("Arguments:");
        final JLabel userNameLabel = new JLabel("User Name:");
        GridBagConstraints gbc;

        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 10, 5, 0);
        panel.add(argumentsLabel, gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 10, 5, 0);
        panel.add(userNameLabel, gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.insets = new Insets(5, 25, 5, 10);
        panel.add(argumentsField, gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.insets = new Insets(5, 25, 5, 10);
        panel.add(userNameField, gbc);

        argumentsField.addActionListener(runAFHParserAction);
        userNameField.addActionListener(runAFHParserAction);

        return panel;
    }

    /**
     * @param isProcessNotDone
     *            A flag showing whether the process has ended or not.
     */
    private void setWaitingForProcessEnd(
                                         final boolean isProcessNotDone) {
        getGlassPane().setVisible(isProcessNotDone);
    }

    /**
     * Runs the parser with the data from the TextFields of the GUI.
     */
    private void runAFHParser() {
        // Save the used text field inputs.
        Settings.setSettingString("AFH Parser location", fileLocationField.getText());
        Settings.setSettingString("AFH Parser arguments", argumentsField.getText());
        Settings.setSettingString("AFH Parser user name", userNameField.getText());

        final ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(new Runnable() {
            public void run() {
                try {
                    final File workingDirectory = new File(fileLocationField.getText()).getParentFile();
                    final Process process = Runtime.getRuntime()
                                                   .exec("perl " + fileLocationField.getText()
                                                                 + " " + argumentsField.getText()
                                                                 + " " + userNameField.getText(),
                                                         null,
                                                         workingDirectory);

                    final BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
                    final ParserOutputViewer outputViewer = new ParserOutputViewer();
                    ExternalMafiaLogParserDialog.this.toFront();

                    String tmp;
                    while ((tmp = br.readLine()) != null)
                        outputViewer.addParserOutput(tmp);

                    br.close();
                    process.destroy();

                    dispose();

                    outputViewer.toFront();
                } catch (final IOException e) {
                    setWaitingForProcessEnd(false);
                    JOptionPane.showMessageDialog(null,
                                                  "There was a problem while running the parser. Please check whether the parsed logs were created.",
                                                  "Problem occurred",
                                                  JOptionPane.ERROR_MESSAGE);
                    e.printStackTrace();
                }
            }
        });
        executor.shutdown();
    }

    /**
     * A small class to display the output of the parser inside another frame.
     */
    private static final class ParserOutputViewer extends JFrame {
        private static final String NEW_LINE = "\n";

        private final JTextArea parserOutput;

        ParserOutputViewer() {
            super("Parser output");
            setLayout(new GridLayout(1, 0));
            setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

            parserOutput = new JTextArea();
            parserOutput.setLineWrap(true);
            parserOutput.setWrapStyleWord(true);
            add(new JScrollPane(parserOutput));

            pack();
            setSize(640, 480);
            RefineryUtilities.centerFrameOnScreen(this);
            setVisible(true);
        }

        void addParserOutput(
                             final String s) {
            parserOutput.append(s);
            parserOutput.append(NEW_LINE);
        }
    }
}
