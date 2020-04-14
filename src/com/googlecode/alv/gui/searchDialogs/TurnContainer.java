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

package com.googlecode.alv.gui.searchDialogs;

import com.googlecode.alv.logData.turn.TurnEntity;

/**
 * As the name suggests, a simple container class for {@link TurnEntity}s that
 * makes it easier to use them for UI elements due to this classes
 * {@link #toString()} implementation.
 */
final class TurnContainer {
    private final TurnEntity turn;

    private final String turnString;

    /**
     * @param turn
     *            The actual turn.
     * @param turnString
     *            The string returned by this classes {@link #toString()}
     *            method. Will be displayed in UI elements and as such should be
     *            an adequate representation of the given turn.
     */
    TurnContainer(
                  final TurnEntity turn, final String turnString) {
        this.turn = turn;
        this.turnString = turnString;
    }

    TurnEntity getTurn() {
        return turn;
    }

    /**
     * Returns the previously given turnString.
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return turnString;
    }
}
