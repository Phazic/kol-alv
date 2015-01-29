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
 * A data container for data concerning the Inexplicable Door aka the 8-Bit
 * Realm, specifically the RNG of finding Bloopers and Bullet Bills.
 */
public final class InexplicableDoor {
    private int turnsSpent;

    private int bloopersFound;

    private int bulletsFound;

    InexplicableDoor() {}

    /**
     * @param turnsSpent
     *            The number of turns spent in the 8-Bit Realm to set.
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
     * @return The number of turns spent in the 8-Bit Realm.
     */
    public int getTurnsSpent() {
        return turnsSpent;
    }

    /**
     * @param bloopersFound
     *            The number of Bloopers found in the 8-Bit Realm to set.
     * @throws IllegalArgumentException
     *             if bloopersFound is below 0
     */
    public void setBloopersFound(
                                 final int bloopersFound) {
        if (bloopersFound < 0)
            throw new IllegalArgumentException("You cannot find less than 0 Bloopers.");

        this.bloopersFound = bloopersFound;
    }

    /**
     * @return The number of Bloopers found in the 8-Bit Realm.
     */
    public int getBloopersFound() {
        return bloopersFound;
    }

    /**
     * @param bulletsFound
     *            The number of Bullet Bills found in the 8-Bit Realm to set.
     * @throws IllegalArgumentException
     *             if bulletsFound is below 0
     */
    public void setBulletsFound(
                                final int bulletsFound) {
        if (bloopersFound < 0)
            throw new IllegalArgumentException("You cannot find less than 0 Bullet Bills.");

        this.bulletsFound = bulletsFound;
    }

    /**
     * @return The number of Bullet Bills found in the 8-Bit Realm.
     */
    public int getBulletsFound() {
        return bulletsFound;
    }

    @Override
    public boolean equals(
                          final Object o) {
        if (o == this)
            return true;

        if (o == null)
            return false;

        if (o instanceof InexplicableDoor)
            return ((InexplicableDoor) o).getTurnsSpent() == turnsSpent
                   && ((InexplicableDoor) o).getBloopersFound() == bloopersFound
                   && ((InexplicableDoor) o).getBulletsFound() == bulletsFound;

        return false;
    }

    @Override
    public int hashCode() {
        int result = 222;
        result = 31 * result + turnsSpent;
        result = 31 * result + bloopersFound;
        result = 31 * result + bulletsFound;

        return result;
    }
}
