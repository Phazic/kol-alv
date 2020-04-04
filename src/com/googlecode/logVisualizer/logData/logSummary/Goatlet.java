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

package com.googlecode.logVisualizer.logData.logSummary;

/**
 * A data container for data concerning the Goatlet, specifically the RNG of
 * finding the dairy Goats and their item drops.
 */
public final class Goatlet {
    private int turnsSpent;

    private int dairyGoatsFound;

    private int cheeseFound;

    private int milkFound;

    Goatlet() {}

    /**
     * @param turnsSpent
     *            The number of turns spent in the Goatlet to set.
     * @throws IllegalArgumentException
     *             if turnsSpent is below 0
     */
    public void setTurnsSpent(
                              final int turnsSpent) {
        if (turnsSpent < 0)
            throw new IllegalArgumentException("You cannot spent less than 0 turns somewhere.");

        this.turnsSpent = turnsSpent;
    }

    /**
     * @return The number of turns spent in the Goatlet.
     */
    public int getTurnsSpent() {
        return turnsSpent;
    }

    /**
     * @param dairyGoatsFound
     *            The number of dairy Goats found in the Goatlet to set.
     * @throws IllegalArgumentException
     *             if dairyGoatsFound is below 0
     */
    public void setDairyGoatsFound(
                                   final int dairyGoatsFound) {
        if (dairyGoatsFound < 0)
            throw new IllegalArgumentException("You cannot find less than 0 Dairy Goats.");

        this.dairyGoatsFound = dairyGoatsFound;
    }

    /**
     * @return The number of dairy Goats found in the Goatlet.
     */
    public int getDairyGoatsFound() {
        return dairyGoatsFound;
    }

    /**
     * @param cheeseFound
     *            The number of goat cheese found in the Goatlet to set.
     * @throws IllegalArgumentException
     *             if cheeseFound is below 0
     */
    public void setCheeseFound(
                               final int cheeseFound) {
        if (cheeseFound < 0)
            throw new IllegalArgumentException("You cannot find less than 0 goat cheeses.");

        this.cheeseFound = cheeseFound;
    }

    /**
     * @return The number of goat cheese found in the Goatlet.
     */
    public int getCheeseFound() {
        return cheeseFound;
    }

    /**
     * @param milkFound
     *            The number of goat milks found in the Goatlet to set.
     * @throws IllegalArgumentException
     *             if milkFound is below 0
     */
    public void setMilkFound(
                             final int milkFound) {
        if (milkFound < 0)
            throw new IllegalArgumentException("You cannot find less than 0 glasses of goat milk.");

        this.milkFound = milkFound;
    }

    /**
     * @return The number of goat milks found in the Goatlet.
     */
    public int getMilkFound() {
        return milkFound;
    }

    @Override
    public boolean equals(
                          final Object o) {
        if (o == this)
            return true;

        if (o == null)
            return false;

        if (o instanceof Goatlet)
            return ((Goatlet) o).getTurnsSpent() == turnsSpent
                   && ((Goatlet) o).getDairyGoatsFound() == dairyGoatsFound
                   && ((Goatlet) o).getCheeseFound() == cheeseFound
                   && ((Goatlet) o).getMilkFound() == milkFound;

        return false;
    }

    @Override
    public int hashCode() {
        int result = 111;
        result = 31 * result + turnsSpent;
        result = 31 * result + dairyGoatsFound;
        result = 31 * result + cheeseFound;
        result = 31 * result + milkFound;

        return result;
    }
}
