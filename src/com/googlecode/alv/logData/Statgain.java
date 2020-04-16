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

package com.googlecode.alv.logData;

import com.googlecode.alv.parser.UsefulPatterns;

/**
 * A simple immutable container class for stat gains from adventures,
 * consumables and so on.
 */
public final class Statgain {
    public static final Statgain NO_STATS = new Statgain(0, 0, 0);

    public final int mus;

    public final int myst;

    public final int mox;

    /**
     * @param mus
     *            The muscle stat gain to set.
     * @param myst
     *            The mysticality stat gain to set.
     * @param mox
     *            The moxie stat gain to set.
     */
    public Statgain(
                    final int mus, final int myst, final int mox) {
        this.mus = mus;
        this.myst = myst;
        this.mox = mox;
    }

    /**
     * @param mus
     *            The muscle stats to set.
     * @return The new Statgain.
     */
    public Statgain setMuscle(
                              final int mus) {
        return new Statgain(mus, myst, mox);
    }

    /**
     * @param myst
     *            The myst stats to set.
     * @return The new Statgain.
     */
    public Statgain setMyst(
                            final int myst) {
        return new Statgain(mus, myst, mox);
    }

    /**
     * @param mox
     *            The moxie stats to set.
     * @return The new Statgain.
     */
    public Statgain setMoxie(
                             final int mox) {
        return new Statgain(mus, myst, mox);
    }

    /**
     * @return True if all stats are zero, otherwise false.
     */
    public boolean isAllStatsZero() {
        return mus == 0 && myst == 0 && mox == 0;
    }

    /**
     * @return The total stat gain.
     */
    public int getTotalStatgain() {
        return mus + myst + mox;
    }

    /**
     * @param stats
     *            Adds all stat gains of that Statgain object to this instance.
     * @return The new Statgain.
     * @throws NullPointerException
     *             if stats is {@code null}
     */
    public Statgain addStats(
                             final Statgain stats) {
        return addStats(stats.mus, stats.myst, stats.mox);
    }

    /**
     * @param mus
     *            The muscle stat gain to add.
     * @param myst
     *            The mysticality stat gain to add.
     * @param mox
     *            The moxie stat gain to add.
     * @return The new Statgain.
     */
    public Statgain addStats(
                             final int mus, final int myst, final int mox) {
        return new Statgain(this.mus + mus, this.myst + myst, this.mox + mox);
    }

    @Override
    public String toString() {
        final StringBuilder str = new StringBuilder(20);

        str.append(UsefulPatterns.SQUARE_BRACKET_OPEN);
        str.append(mus);
        str.append(UsefulPatterns.COMMA);
        str.append(myst);
        str.append(UsefulPatterns.COMMA);
        str.append(mox);
        str.append(UsefulPatterns.SQUARE_BRACKET_CLOSE);

        return str.toString();
    }

    @Override
    public boolean equals(
                          final Object o) {
        if (this == o)
            return true;

        if (o != null)
            if (o instanceof Statgain)
                return ((Statgain) o).mus == mus && ((Statgain) o).myst == myst
                       && ((Statgain) o).mox == mox;

        return false;
    }

    @Override
    public int hashCode() {
        int result = 25;
        result = 31 * result + mus;
        result = 31 * result + myst;
        result = 31 * result + mox;

        return result;
    }
}
