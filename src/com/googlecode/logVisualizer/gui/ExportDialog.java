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
import java.io.IOException;

import javax.swing.*;

import org.jfree.ui.RefineryUtilities;

import com.googlecode.logVisualizer.Settings;
import com.googlecode.logVisualizer.creator.BBCodeLogCreator;
import com.googlecode.logVisualizer.creator.HTMLLogCreator;
import com.googlecode.logVisualizer.creator.LogsCreator;
import com.googlecode.logVisualizer.creator.TextLogCreator;
import com.googlecode.logVisualizer.creator.XMLLogCreator;
import com.googlecode.logVisualizer.creator.util.FileAccessException;
import com.googlecode.logVisualizer.creator.util.XMLAccessException;
import com.googlecode.logVisualizer.logData.LogDataHolder;
import com.googlecode.logVisualizer.util.LogOutputFormat;

/**
 * A dialog that can be used to preview the parsed output of a log and save it
 * to a file.
 */
public final class ExportDialog extends JDialog {
    private final JTextField directoryLocationField = new JTextField(Settings.getString("Parsed logs saving location"));

    private final LogDataHolder logData;

    private LogOutputFormat logVersion;

    /**
     * Creates and shows the export dialog in the centre of the screen.
     * 
     * Please note that this dialog is also modal on the given frame. If the
     * dialog should instead be modal over the whole application, you may use a
     * null reference instead.
     * @param frame Frame
     * @param logData Collection of log data
     */
    public static void showExportDialog(
                                        final JFrame frame, final LogDataHolder logData) {
        new ExportDialog(frame, logData, LogOutputFormat.TEXT_LOG);
    }

    /**
     * Creates and shows the export dialog in the centre of the screen with the
     * given log output version preselected.
     * <p>
     * Please note that this dialog is also modal on the given frame. If the
     * dialog should instead be modal over the whole application, you may use a
     * null reference instead.
     * @param frame Frame 
     * @param logData Collection of log data
     * @param logVersion Version of log data
     */
    public static void showExportDialog(
                                        final JFrame frame, final LogDataHolder logData,
                                        final LogOutputFormat logVersion) {
        new ExportDialog(frame, logData, logVersion);
    }

    private ExportDialog(
                         final JFrame frame, final LogDataHolder logData,
                         final LogOutputFormat logVersion) {
        super(frame, true);

        if (logData == null)
            throw new NullPointerException("The to be exported log must not be null.");
        if (logVersion == null)
            throw new NullPointerException("The log output version must not be null.");

        this.logData = logData;
        this.logVersion = logVersion;

        setTitle("Export Dialog");
        setLayout(new BorderLayout(0, 10));

        final JPanel lowerPanel = new JPanel(new GridLayout(0, 1, 0, 10));
        lowerPanel.add(createDirectoryFinderPanel());
        lowerPanel.add(createButtonPanel());
        getContentPane().add(createLogVersionChooserPanel(), BorderLayout.CENTER);
        getContentPane().add(lowerPanel, BorderLayout.SOUTH);

        pack();
        setSize(800, 600);
        RefineryUtilities.centerFrameOnScreen(this);
        setVisible(true);
    }

    private JPanel createLogVersionChooserPanel() {
        final JPanel panel = new JPanel(new BorderLayout(10, 10));
        final JPanel buttonPanel = new JPanel(new GridLayout(1, 0, 10, 10));
        final JTextArea previewArea = new JTextArea();
        final JRadioButton textButton = new JRadioButton("Text", false);
        final JRadioButton htmlButton = new JRadioButton("HTML", false);
        final JRadioButton bbcodeButton = new JRadioButton("BBCode", false);
        final JRadioButton xmlButton = new JRadioButton("XML", false);
        final ButtonGroup group = new ButtonGroup();
        group.add(textButton);
        group.add(htmlButton);
        group.add(bbcodeButton);
        group.add(xmlButton);

        final ActionListener listener = new ActionListener() {
            public void actionPerformed(final ActionEvent e) {
                previewArea.setEnabled(true);
                if (htmlButton.isSelected())
                    previewArea.setText(HTMLLogCreator.getTextualLog(logData, 
                                                                     LogOutputFormat.HTML_LOG));
                else if (bbcodeButton.isSelected())
                    previewArea.setText(BBCodeLogCreator.getTextualLog(logData,
                                                                       LogOutputFormat.BBCODE_LOG));
                else
                    previewArea.setText(TextLogCreator.getTextualLog(logData,
                                                                     LogOutputFormat.TEXT_LOG));
                previewArea.setCaretPosition(0);
            }
        };
        textButton.addActionListener(listener);
        htmlButton.addActionListener(listener);
        bbcodeButton.addActionListener(listener);
        xmlButton.addActionListener(new ActionListener() {
            public void actionPerformed(
                                        final ActionEvent e) {
                logVersion = LogOutputFormat.XML_LOG;

                previewArea.setText("");
                previewArea.setEnabled(false);
            }
        });

        buttonPanel.add(textButton);
        buttonPanel.add(htmlButton);
        buttonPanel.add(bbcodeButton);
        buttonPanel.add(xmlButton);

        final JScrollPane scrollPane = new JScrollPane(previewArea);
        previewArea.setWrapStyleWord(true);
        previewArea.setLineWrap(true);
        scrollPane.setPreferredSize(new Dimension(0, 300));

        panel.add(buttonPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        switch (logVersion) {
            case TEXT_LOG:
                textButton.doClick();
                break;
            case HTML_LOG:
                htmlButton.doClick();
                break;
            case BBCODE_LOG:
                bbcodeButton.doClick();
                break;
            case XML_LOG:
                xmlButton.doClick();
                break;
        }

        return panel;
    }

    private JPanel createDirectoryFinderPanel() {
        final JPanel panel = new JPanel(new GridBagLayout());
        final JButton directoryChooserButton = new JButton("Find Directory");
        GridBagConstraints gbc;

        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.insets = new Insets(10, 10, 5, 0);
        panel.add(directoryLocationField, gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.insets = new Insets(10, 25, 5, 10);
        panel.add(directoryChooserButton, gbc);

        File logsDirectory = new File(directoryLocationField.getText());
        if (!logsDirectory.exists())
            logsDirectory = null;

        final JFileChooser directoryChooser = new JFileChooser(logsDirectory);
        directoryChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
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

    private JPanel createButtonPanel() {
        final JPanel buttonPanel = new JPanel(new GridLayout(1, 0, 100, 0));
        final JButton closeButton = new JButton("Back");
        final JButton saveButton = new JButton("Save log to file");

        saveButton.setPreferredSize(new Dimension(0, 30));
        saveButton.addActionListener(new ActionListener() {
            public void actionPerformed(
                                        final ActionEvent e) {
                setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

                String filePath = directoryLocationField.getText();
                if (!filePath.endsWith(File.separator))
                    filePath += File.separator;

                if (logVersion != LogOutputFormat.XML_LOG)
                    saveTextualLog(filePath, logVersion);
                else
                    saveXMLLog(filePath, logVersion);
                logData.setEdited(false);

                setCursor(Cursor.getDefaultCursor());
                dispose();
            }
        });
        closeButton.setPreferredSize(new Dimension(0, 30));
        closeButton.addActionListener(new ActionListener() {
            public void actionPerformed(
                                        final ActionEvent e) {
                dispose();
            }
        });

        buttonPanel.add(saveButton);
        buttonPanel.add(closeButton);

        return buttonPanel;
    }

    private void saveTextualLog(final String directoryPath, final LogOutputFormat format) 
    {
        try {
            final File directory = new File(directoryPath);
            if (!directory.exists())
                directory.mkdir();

            final File logDest = new File(directory,
                                          LogsCreator.getParsedLogNameFromCondensedMafiaLog(logData.getLogName()
                                                                                                    + ".txt",
                                                                                            format));
            if (logDest.exists())
                logDest.delete();
            logDest.createNewFile();

            
            TextLogCreator.saveTextualLogToFile(logData, logDest, format);

            Settings.setString("Parsed logs saving location",
                                      directoryLocationField.getText());
        } catch (final IOException e) {
            JOptionPane.showMessageDialog(null,
                                          "A problem occurred while creating the log.",
                                          "Error occurred",
                                          JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void saveXMLLog(
                            final String directoryPath, final LogOutputFormat format) {
        try {
            final File logDest = new File(directoryPath);
            if (!logDest.exists())
                logDest.mkdir();

            XMLLogCreator.createXMLLog(logData, logDest);

            Settings.setString("Parsed logs saving location",
                               directoryLocationField.getText());
        } catch (final FileAccessException e) {
            JOptionPane.showMessageDialog(null,
                                          "A problem occurred while creating/writing to the file.",
                                          "Error occurred",
                                          JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        } catch (final XMLAccessException e) {
            JOptionPane.showMessageDialog(null,
                                          "A problem occurred while creating the XML.",
                                          "Error occurred",
                                          JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
}