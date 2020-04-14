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

package com.googlecode.alv.logData.turn.turnAction;

import com.googlecode.alv.parser.UsefulPatterns;

/**
 * This immutable class is a representation of a familiar change. It holds the
 * turn number of when the change occurred (<b>not</b> the turn number of when
 * the new familiar was first used) and the name of the familiar which is in use
 * after the change.
 */
public final class FamiliarChange extends AbstractTurnAction<FamiliarChange> {
    public static final FamiliarChange NO_FAMILIAR = new FamiliarChange("none", 0);

    private final String familiarName;

    /**
     * @param familiarName
     *            The name of the equipped familiar after this familiar change
     *            occurred to set.
     * @param turnNumber
     *            The turn number of this familiar change to set.
     * @throws NullPointerException
     *             if famliarName is {@code null}
     * @throws IllegalArgumentException
     *             if turnNumber is below 0
     */
    public FamiliarChange(
                          final String familiarName, final int turnNumber) {
        super(turnNumber);

        if (familiarName == null)
            throw new NullPointerException("Familiar name must not be null.");

        this.familiarName = familiarName;
    }

    /**
     * @return The name of the equipped familiar after this familiar change
     *         occurred.
     */
    public String getFamiliarName() {
        return familiarName;
    }

    @Override
    public String toString() {
        final StringBuilder str = new StringBuilder(30);

        str.append(UsefulPatterns.SQUARE_BRACKET_OPEN);
        str.append(getTurnNumber());
        str.append(UsefulPatterns.SQUARE_BRACKET_CLOSE);
        str.append(UsefulPatterns.WHITE_SPACE);
        str.append(familiarName);

        return str.toString();
    }

    @Override
    public boolean equals(
                          final Object o) {
        if (super.equals(o))
            return o instanceof FamiliarChange;

        return false;
    }

    @Override
    public int hashCode() {
        int result = 22;
        result = 31 * result + getTurnNumber();

        return result;
    }
}
