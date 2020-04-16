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

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;

import com.googlecode.alv.logData.LogDataHolder;
import com.googlecode.alv.logData.turn.SingleTurn;
import com.googlecode.alv.logData.turn.TurnInterval;
import com.googlecode.alv.parser.UsefulPatterns;
import com.googlecode.alv.util.DataNumberPair;

final class MiscPanel extends JTabbedPane {
    private static final String NEW_LINE = "\n";

    /**
     * @param logData
     *            The {@link LogDataHolder} with all the data of the ascension
     *            log.
     */
    MiscPanel(
              final LogDataHolder logData) {
        super();
        setTabLayoutPolicy(SCROLL_TAB_LAYOUT);

        addTab("Semirares", createSummaryPanel(logData.getLogSummary().getSemirares()));
        addTab("Bad Moon", createSummaryPanel(logData.getLogSummary().getBadmoonAdventures()));
        addTab("Hunted Combats", createSummaryPanel(logData.getHuntedCombats()));
        addTab("Yellow Destruction", createSummaryPanel(logData.getLogSummary()
                                                               .getDisintegratedCombats()));
        addTab("Copied Combats", createCopiedMonstersSummaryPanel(logData));
        addTab("Wandering Encounters", createSummaryPanel(logData.getLogSummary()
                                                                 .getWanderingAdventures()));
        addTab("Lost Combats", createSummaryPanel(logData.getLostCombats()));
        addTab("Hipster Combats", createSummaryPanel(logData.getLogSummary().getHipsterCombats()));
    }

    private static JPanel createSummaryPanel(
                                             final Iterable<DataNumberPair<String>> summaryData) {
        final JPanel panel = new JPanel(new BorderLayout());
        final JTextArea lister = new JTextArea();

        for (final DataNumberPair<String> dn : summaryData) {
            lister.append(UsefulPatterns.SQUARE_BRACKET_OPEN);
            lister.append(dn.getNumber().toString());
            lister.append(UsefulPatterns.SQUARE_BRACKET_CLOSE);
            lister.append(UsefulPatterns.WHITE_SPACE);
            lister.append(dn.getData());
            lister.append(NEW_LINE);
        }

        panel.add(new JScrollPane(lister), BorderLayout.CENTER);

        return panel;
    }

    private static JPanel createCopiedMonstersSummaryPanel(
                                                           final LogDataHolder logData) {
        final JPanel panel = new JPanel(new BorderLayout());
        final JTextArea lister = new JTextArea();

        for (final TurnInterval ti : logData.getCopiedTurns()) {
            // If the single turns collection is empty (for example because
            // base data was from a pre-parsed log), give the area name so
            // the user has at least some kind of feedback, otherwise use the
            // existing encounter names.
            if (ti.getTurns().isEmpty()) {
                for (int i = ti.getStartTurn() + 1; i <= ti.getEndTurn(); i++)
                    addCopiedMonsterLine(lister, i, ti.getAreaName());
            } else
                for (final SingleTurn st : ti.getTurns())
                    addCopiedMonsterLine(lister, st.getTurnNumber(), st.getEncounterName());
        }
        if (!logData.getLogSummary().getRomanticArrowUsages().isEmpty()) {
            lister.append(NEW_LINE);
            lister.append(NEW_LINE);
            lister.append("Romantic Arrow usage:" + NEW_LINE);
            for (final DataNumberPair<String> dn : logData.getLogSummary().getRomanticArrowUsages()) {
                lister.append(UsefulPatterns.SQUARE_BRACKET_OPEN);
                lister.append(dn.getNumber().toString());
                lister.append(UsefulPatterns.SQUARE_BRACKET_CLOSE);
                lister.append(UsefulPatterns.WHITE_SPACE);
                lister.append(dn.getData());
                lister.append(NEW_LINE);
            }
        }

        panel.add(new JScrollPane(lister), BorderLayout.CENTER);

        return panel;
    }

    private static final void addCopiedMonsterLine(
                                                   final JTextArea lister, final int turnNumber,
                                                   final String description) {
        lister.append(UsefulPatterns.SQUARE_BRACKET_OPEN);
        lister.append(String.valueOf(turnNumber));
        lister.append(UsefulPatterns.SQUARE_BRACKET_CLOSE);
        lister.append(UsefulPatterns.WHITE_SPACE);
        lister.append(description);
        lister.append(NEW_LINE);
    }
}