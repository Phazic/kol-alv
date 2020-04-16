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

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JSplitPane;

import com.googlecode.alv.chart.turnrundownGantt.TurnrundownGantt;
import com.googlecode.alv.gui.LogGUI.GanttPaneButtonListener;
import com.googlecode.alv.logData.LogDataHolder;

final class GanttPanelGUI extends JSplitPane {
    private GanttPaneButtonListener buttonListener;

    /**
     * @param logData
     *            The {@link LogDataHolder} with all the data of the ascension
     *            log.
     */
    GanttPanelGUI(
                  final LogDataHolder logData) {
        super(VERTICAL_SPLIT);
        final JPanel optionsArea = new JPanel(new GridLayout(1, 0, 10, 0));
        final TurnrundownGantt turnrundown = new TurnrundownGantt(logData);
        final JButton areaCategoryOptions = new JButton("Area categories");
        final JButton familiarOptions = new JButton("Familiar usage");

        areaCategoryOptions.setToolTipText("Customize categories of the turn rundown gantt chart.");
        familiarOptions.setToolTipText("Set color coding for familiar usage.");

        areaCategoryOptions.addActionListener(new ActionListener() {
            public void actionPerformed(
                                        final ActionEvent ae) {
                if (buttonListener != null)
                    buttonListener.areaCategoryCustomizerPressed(turnrundown);
            }
        });
        familiarOptions.addActionListener(new ActionListener() {
            public void actionPerformed(
                                        final ActionEvent ae) {
                if (buttonListener != null)
                    buttonListener.familiarColorizerPressed(turnrundown);
            }
        });

        optionsArea.add(areaCategoryOptions);
        optionsArea.add(familiarOptions);

        setDividerLocation(50);
        setTopComponent(optionsArea);
        setBottomComponent(turnrundown);
    }

    /**
     * @param buttonListener
     *            The button listener to set.
     */
    void setButtonListener(
                           final GanttPaneButtonListener buttonListener) {
        this.buttonListener = buttonListener;
    }
}
