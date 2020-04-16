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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;
import javax.swing.UIManager.LookAndFeelInfo;

import org.jfree.ui.RefineryUtilities;

import com.googlecode.alv.Settings;

/**
 * An options dialog to change the used Look&amp;Feel of the Ascension Log
 * Visualizer.
 */
final class LafChangerDialog extends JDialog {
    private final JComboBox lafLister;

    LafChangerDialog(
                     final JFrame owner) {
        super(owner, true);
        setLayout(new BorderLayout(0, 10));
        setTitle("Look&Feel changer");
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        lafLister = new JComboBox();
        lafLister.setEditable(false);
        addLafs();

        final JButton okButton = new JButton("OK");
        final JButton cancelButton = new JButton("Cancel");
        okButton.addActionListener(new ActionListener() {

            public void actionPerformed(
                                        final ActionEvent e) {
                changeUsedLaf(owner);
            }
        });
        cancelButton.addActionListener(new ActionListener() {

            public void actionPerformed(
                                        final ActionEvent e) {
                dispose();
            }
        });

        add(lafLister, BorderLayout.NORTH);

        add(new JLabel("Note that changes to the Look&Feel will be remembered for future program starts."),
            BorderLayout.CENTER);

        final JPanel buttonPanel = new JPanel(new GridLayout(1, 0, 10, 0));
        buttonPanel.setPreferredSize(new Dimension(150, 50));
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);
        add(buttonPanel, BorderLayout.SOUTH);

        pack();
        RefineryUtilities.centerFrameOnScreen(this);
        setVisible(true);
    }

    private void addLafs() {
        for (final LookAndFeelInfo lafi : UIManager.getInstalledLookAndFeels())
            lafLister.addItem(lafi.getName());

        lafLister.setSelectedItem(Settings.getString("LookAndFeel"));
    }

    private void changeUsedLaf(
                               final JFrame owner) {
        final String lafName = (String) lafLister.getSelectedItem();

        Settings.setString("LookAndFeel", lafName);
        for (final LookAndFeelInfo lafi : UIManager.getInstalledLookAndFeels())
            if (lafi.getName().equals(lafName)) {
                try {
                    UIManager.setLookAndFeel(lafi.getClassName());
                    SwingUtilities.updateComponentTreeUI(owner);
                    owner.pack();
                } catch (final Exception e) {
                    e.printStackTrace();
                }
                break;
            }

        dispose();
    }
}
