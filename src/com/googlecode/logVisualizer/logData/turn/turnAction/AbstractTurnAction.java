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

package com.googlecode.logVisualizer.logData.turn.turnAction;

/**
 * This class is a means to implement certain actions that can happen during a
 * turn. This class only holds the turn number and gives the ability to compare
 * two instances of this class based on the turn number. Everything else is up
 * to actual implementations of this class.
 * <p>
 * Note that this does not make it possible to change the turn number after
 * initialisation and thus makes its subclasses immutable if all fields of the
 * given subclass are also immutable.
 */
public abstract class AbstractTurnAction<T extends AbstractTurnAction<?>> implements Comparable<T> {
    private final int turnNumber;

    /**
     * @param turnNumber
     *            The turn number of this turn action to set.
     * @throws IllegalArgumentException
     *             if turnNumber is below 0
     */
    public AbstractTurnAction(
                              final int turnNumber) {
        if (turnNumber < 0)
            throw new IllegalArgumentException("Turn number below 0.");

        this.turnNumber = turnNumber;
    }

    /**
     * @return The turn number of the turn action.
     */
    public int getTurnNumber() {
        return turnNumber;
    }

    /**
     * @return The difference between the turn number of this turn action and
     *         the turn number of the given turn action.
     */
    public int compareTo(
                         final T t) {
        return turnNumber - t.getTurnNumber();
    }

    @Override
    public boolean equals(
                          final Object o) {
        if (o != null)
            if (o instanceof AbstractTurnAction<?>)
                return ((AbstractTurnAction<?>) o).getTurnNumber() == turnNumber;

        return false;
    }

    @Override
    public int hashCode() {
        int result = 37;
        result = 31 * result + turnNumber;

        return result;
    }
}
