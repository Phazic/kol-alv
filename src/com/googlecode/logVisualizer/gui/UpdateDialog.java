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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public final class UpdateDialog {
    private static final String URL = "http://code.google.com/p/ascension-log-visualizer/";

    public static void showDialog(
                                  final JFrame frame) {
        JOptionPane.showMessageDialog(frame,
                                      createDialogUI(),
                                      "Newer Version Available",
                                      JOptionPane.INFORMATION_MESSAGE);
    }

    private static JPanel createDialogUI() {
        final JPanel infoPanel = new JPanel(new BorderLayout());
        final JLabel info = new JLabel("<html>There is a newer version of Ascension Log Visualizer available.<br><br>The project home can be found here:</html>");
        final JLabel url = new JLabel(URL);
        url.setForeground(Color.BLUE);
        url.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        url.addMouseListener(new MouseListener() {
            public void mouseClicked(
                                     final MouseEvent e) {
                startBrowser();
            }

            public void mouseReleased(
                                      final MouseEvent e) {}

            public void mousePressed(
                                     final MouseEvent e) {}

            public void mouseExited(
                                    final MouseEvent e) {}

            public void mouseEntered(
                                     final MouseEvent e) {}
        });
        infoPanel.add(info, BorderLayout.CENTER);
        infoPanel.add(url, BorderLayout.SOUTH);

        return infoPanel;
    }

    private static void startBrowser() {
        final String os = System.getProperty("os.name").toLowerCase();
        final Runtime rt = Runtime.getRuntime();

        try {
            if (os.contains("win")) {
                final String[] cmd = new String[4];
                cmd[0] = "cmd.exe";
                cmd[1] = "/C";
                cmd[2] = "start";
                cmd[3] = URL;
                rt.exec(cmd);
            } else if (os.contains("mac"))
                rt.exec("open " + URL);
            else {
                // prioritised guesses since there is no default browser command
                final String[] browsers = { "firefox", "chromium-browser", "google-chrome",
                                           "opera", "konqueror", "epiphany", "mozilla", "netscape",
                                           "lynx" };

                final StringBuilder cmd = new StringBuilder();
                for (int i = 0; i < browsers.length; i++)
                    cmd.append((i == 0 ? "" : " || ") + browsers[i] + " \"" + URL + "\"");

                rt.exec(new String[] { "sh", "-c", cmd.toString() });
            }
        } catch (final IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                                          "The system failed to invoke your default web browser while attempting to access:\n\n"
                                                  + URL,
                                          "Could Not Open Browser",
                                          JOptionPane.WARNING_MESSAGE);
        }
    }
}
