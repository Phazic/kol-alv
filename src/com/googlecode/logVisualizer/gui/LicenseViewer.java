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

import static net.java.dev.spellcast.utilities.UtilityConstants.LICENSE_DIRECTORY;

import java.awt.CardLayout;
import java.awt.GridLayout;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import net.java.dev.spellcast.utilities.DataUtilities;

import org.jfree.ui.RefineryUtilities;

import com.googlecode.logVisualizer.util.Lists;

public final class LicenseViewer extends JDialog {
    private final List<BufferedReader> licenses;

    /**
     * @param owner
     *            The JFrame which owns this dialog.
     */
    public LicenseViewer(
                         final JFrame owner) {
        super(owner, true);
        licenses = Lists.newArrayList();
        licenses.add(DataUtilities.getReader(LICENSE_DIRECTORY,
                                             "License Ascension Log Visualizer.txt"));
        licenses.add(DataUtilities.getReader(LICENSE_DIRECTORY, "License JCommon.txt"));
        licenses.add(DataUtilities.getReader(LICENSE_DIRECTORY, "License JFreeChart.txt"));
        licenses.add(DataUtilities.getReader(LICENSE_DIRECTORY, "License Spellcast.txt"));
        licenses.add(DataUtilities.getReader(LICENSE_DIRECTORY, "License SwingFX.txt"));
        licenses.add(DataUtilities.getReader(LICENSE_DIRECTORY, "License stax.txt"));

        setLayout(new GridLayout());
        addGUIElements();

        setSize(800, 600);
        setTitle("License and copyright notices");
        RefineryUtilities.centerFrameOnScreen(this);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setVisible(true);
    }

    private void addGUIElements() {
        final JSplitPane splitter = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        final JList licenseMenu = new JList();
        final JPanel licenseArea = new JPanel(new CardLayout());

        licenseMenu.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        licenseMenu.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Ascension Log Visualizer", "License JCommon",
                                "License JFreeChart", "License Spellcast", "License SwingFX",
                                "License StAX" };

            public int getSize() {
                return strings.length;
            }

            public Object getElementAt(
                                       final int i) {
                return strings[i];
            }
        });
        licenseMenu.setSelectedIndex(0);
        licenseMenu.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(
                                     final ListSelectionEvent lse) {
                if (!lse.getValueIsAdjusting())
                    ((CardLayout) licenseArea.getLayout()).show(licenseArea,
                                                                licenseMenu.getSelectedValue()
                                                                           .toString());
            }
        });

        for (int i = 0; i < licenses.size(); i++) {
            final JTextArea licenseText = new JTextArea();
            final JScrollPane scrollPane = new JScrollPane(licenseText);
            licenseText.setLineWrap(true);
            licenseText.setWrapStyleWord(true);

            String tmpLine;
            try {
                while ((tmpLine = licenses.get(i).readLine()) != null)
                    licenseText.append(tmpLine + "\n");
                licenses.get(i).close();
            } catch (final IOException e) {
                e.printStackTrace();
            }

            licenseText.setCaretPosition(0);

            licenseArea.add((String) licenseMenu.getModel().getElementAt(i), scrollPane);
        }

        splitter.setLeftComponent(new JScrollPane(licenseMenu));
        splitter.setRightComponent(licenseArea);
        splitter.setDividerLocation(175);
        add(splitter);
    }
}
