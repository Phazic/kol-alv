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

package com.googlecode.alv.gui.searchDialogs;

import javax.swing.JFrame;

import com.googlecode.alv.logData.LogDataHolder;
import com.googlecode.alv.logData.turn.TurnInterval;

/**
 * Utility class to display various search dialogs, such as dialogs to search
 * through turn intervals or single turns.
 */
public final class SearchDialogs {
    /**
     * Displays a search dialog for areas (= turn intervals).
     * 
     * @param owner
     *            The Frame from which the dialog is displayed.
     * @param logData
     *            The log data.
     * @return The turn interval selected inside the dialog, may be {@code null}
     *         in case no turn interval was selected.
     */
    public static TurnInterval showAreaSearchDialog(
                                                    final JFrame owner, final LogDataHolder logData) {
        final AreaSearchDialog dialog = new AreaSearchDialog(owner, logData);

        return (TurnInterval) dialog.getSelectedTurn();
    }

    /**
     * Displays a search dialog for single turns.
     * 
     * @param owner
     *            The Frame from which the dialog is displayed.
     * @param logData
     *            The log data.
     * @return The turn interval corresponding to the selected turn inside the
     *         dialog, will be {@code null} in case no turn was selected.
     */
    public static TurnInterval showTurnSearchDialog(
                                                    final JFrame owner, final LogDataHolder logData) {
        final TurnSearchDialog dialog = new TurnSearchDialog(owner, logData);

        if (dialog.getSelectedTurn() != null) {
            final int turnNumber = dialog.getSelectedTurn().getTurnNumber();
            TurnInterval intervalOfTurn = null;
            for (final TurnInterval ti : logData.getTurnIntervalsSpent())
                if (ti.getStartTurn() < turnNumber && ti.getEndTurn() >= turnNumber) {
                    intervalOfTurn = ti;
                    break;
                }

            return intervalOfTurn;
        } else
            return null;
    }
}
