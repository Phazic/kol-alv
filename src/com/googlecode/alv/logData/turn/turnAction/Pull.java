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
import com.googlecode.alv.util.DataNumberPair;

/**
 * A representation of a single item pull. This means that this class only holds
 * the single item which was pulled and the amount of that item which was
 * pulled, but not more than that one item.
 * <p>
 * This class is immutable.
 * <p>
 * Note: This class has a natural ordering that is inconsistent with equals.
 */
public final class Pull extends AbstractTurnAction<Pull> {
    private static final String PULLED_STRING = "pulled";

    private final DataNumberPair<String> pulledItem;

    private final int dayNumber;

    /**
     * @param itemName
     *            The name of the pulled item to set.
     * @param amount
     *            The number of items pulled to set.
     * @param turnNumber
     *            The turn number this item was pulled on to set.
     * @param dayNumber
     *            The day number this item was pulled on to set.
     * @throws NullPointerException
     *             if itemName is {@code null}
     * @throws IllegalArgumentException
     *             if amount is below 1; if turnNumber is below 0; if dayNumber
     *             is below 1
     */
    public Pull(
                final String itemName, final int amount, final int turnNumber, final int dayNumber) {
        super(turnNumber);
        pulledItem = DataNumberPair.of(itemName, amount);

        if (dayNumber < 1)
            throw new IllegalArgumentException("The is no day below day 1.");

        this.dayNumber = dayNumber;
    }

    /**
     * @return The pulled item's name.
     */
    public String getItemName() {
        return pulledItem.getData();
    }

    /**
     * @return The amount of pulled items.
     */
    public int getAmount() {
        return pulledItem.getNumber();
    }

    /**
     * @return The day number of this pull.
     */
    public int getDayNumber() {
        return dayNumber;
    }

    @Override
    public String toString() {
        final StringBuilder str = new StringBuilder(50);

        str.append(UsefulPatterns.SQUARE_BRACKET_OPEN);
        str.append(getTurnNumber());
        str.append(UsefulPatterns.SQUARE_BRACKET_CLOSE);
        str.append(UsefulPatterns.WHITE_SPACE);
        str.append(PULLED_STRING);
        str.append(UsefulPatterns.WHITE_SPACE);
        str.append(getAmount());
        str.append(UsefulPatterns.WHITE_SPACE);
        str.append(getItemName());

        return str.toString();
    }

    @Override
    public boolean equals(
                          final Object o) {
        if (super.equals(o) && o instanceof Pull) {
            final Pull p = (Pull) o;
            return p.getDayNumber() == dayNumber && p.pulledItem.equals(pulledItem);
        }

        return false;
    }

    @Override
    public int hashCode() {
        int result = 33;
        result = 31 * result + super.hashCode();
        result = 31 * result + dayNumber;
        result = 31 * result + pulledItem.hashCode();

        return result;
    }
}
