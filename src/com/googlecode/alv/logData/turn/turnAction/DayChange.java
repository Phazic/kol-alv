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

package com.googlecode.alv.logData.turn.turnAction;

import com.googlecode.alv.parser.UsefulPatterns;

/**
 * This immutable class is a representation of a day change. It holds the turn
 * number of when the change occurred and the day number after the change.
 * <p>
 * Note: This class has a natural ordering that is inconsistent with equals.
 */
public final class DayChange extends AbstractTurnAction<DayChange> {
    private static final String DAY_STRING = "Day";

    private static final String DELIMITER_STRING = "===";

    private final int dayNumber;

    /**
     * @param dayNumber
     *            The day number after this day change to set.
     * @param turnNumber
     *            The turn number of this day change to set.
     * @throws IllegalArgumentException
     *             if dayNumber is below 1; if turnNumber is below 0
     */
    public DayChange(
                     final int dayNumber, final int turnNumber) {
        super(turnNumber);

        if (dayNumber < 1)
            throw new IllegalArgumentException("Day number below 1.");

        this.dayNumber = dayNumber;
    }

    /**
     * @return The day number after this day change.
     */
    public int getDayNumber() {
        return dayNumber;
    }

    @Override
    public String toString() {
        final StringBuilder str = new StringBuilder(15);

        str.append(DELIMITER_STRING);
        str.append(DAY_STRING);
        str.append(UsefulPatterns.WHITE_SPACE);
        str.append(dayNumber);
        str.append(DELIMITER_STRING);

        return str.toString();
    }

    @Override
    public boolean equals(
                          final Object o) {
        if (super.equals(o) && o instanceof DayChange)
            return ((DayChange) o).getDayNumber() == dayNumber;

        return false;
    }

    @Override
    public int hashCode() {
        int result = 11;
        result = 31 * result + super.hashCode();
        result = 31 * result + dayNumber;

        return result;
    }
}