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

import java.awt.CardLayout;
import java.awt.FlowLayout;

import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSplitPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.googlecode.logVisualizer.chart.StatDevelopmentLineChart;
import com.googlecode.logVisualizer.chart.SubstatDevelopmentLineChart;
import com.googlecode.logVisualizer.logData.LogDataHolder;

final class StatDevelopmentPanelGUI extends JSplitPane {
    private static final String EFFECTIVE_STATS = "effective stats";

    private static final String SUBSTATS = "substats";

    /**
     * @param logData
     *            The {@link LogDataHolder} with all the data of the ascension
     *            log.
     */
    StatDevelopmentPanelGUI(
                            final LogDataHolder logData) {
        super(VERTICAL_SPLIT);
        final JPanel optionsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 100, 5));
        final JPanel chartPanel = new JPanel(new CardLayout());

        final JRadioButton substatSelector = new JRadioButton("Total Substats", true);
        final JRadioButton effectiveStatSelector = new JRadioButton("Effective Stats", false);
        final ButtonGroup statSelectors = new ButtonGroup();
        statSelectors.add(substatSelector);
        statSelectors.add(effectiveStatSelector);
        substatSelector.setToolTipText("Show total substats chart");
        substatSelector.setToolTipText("Show effective stats chart");
        optionsPanel.add(substatSelector);
        optionsPanel.add(effectiveStatSelector);

        chartPanel.add(new SubstatDevelopmentLineChart(logData), SUBSTATS);
        chartPanel.add(new StatDevelopmentLineChart(logData), EFFECTIVE_STATS);

        substatSelector.addChangeListener(new ChangeListener() {
            public void stateChanged(
                                     final ChangeEvent e) {
                if (substatSelector.isFocusOwner())
                    ((CardLayout) chartPanel.getLayout()).show(chartPanel, SUBSTATS);
            }
        });
        effectiveStatSelector.addChangeListener(new ChangeListener() {
            public void stateChanged(
                                     final ChangeEvent e) {
                if (effectiveStatSelector.isFocusOwner())
                    ((CardLayout) chartPanel.getLayout()).show(chartPanel, EFFECTIVE_STATS);
            }
        });

        setDividerLocation(35);
        setTopComponent(optionsPanel);
        setBottomComponent(chartPanel);
    }
}
