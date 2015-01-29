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

package com.googlecode.logVisualizer.logData;

/**
 * A simple immutable class for meat gains and spendings.
 */
public final class MeatGain {
    public static final MeatGain NO_MEAT = new MeatGain(0, 0, 0);

    public final int encounterMeatGain;

    public final int otherMeatGain;

    public final int meatSpent;

    /**
     * Constructs a MeatGain object with the given values.
     * 
     * @param encounterMeatGain
     *            The encounter meat gain.
     * @param otherMeatGain
     *            The other meat gain.
     * @param meatSpent
     *            The meat spent.
     * @throws IllegalArgumentException
     *             if either meat gained or spent is negative.
     */
    public MeatGain(
                    final int encounterMeatGain, final int otherMeatGain, final int meatSpent) {
        if (encounterMeatGain < 0)
            throw new IllegalArgumentException("Encounter meat gained must not be negative.");
        if (otherMeatGain < 0)
            throw new IllegalArgumentException("Other meat gained must not be negative.");
        if (meatSpent < 0)
            throw new IllegalArgumentException("Meat spent must not be negative.");

        this.encounterMeatGain = encounterMeatGain;
        this.otherMeatGain = otherMeatGain;
        this.meatSpent = meatSpent;
    }

    /**
     * @param encounterMeatGain
     *            The encounter meat gain to set.
     * @return A new MeatGain object with the new value for encounter meat
     *         gained.
     */
    public MeatGain setEncounterMeatGain(
                                         final int encounterMeatGain) {
        return new MeatGain(encounterMeatGain, otherMeatGain, meatSpent);
    }

    /**
     * @param meatGain
     *            The other meat gain to set.
     * @return A new MeatGain object with the new value for other meat gained.
     */
    public MeatGain setOtherMeatGain(
                                     final int otherMeatGain) {
        return new MeatGain(encounterMeatGain, otherMeatGain, meatSpent);
    }

    /**
     * @param meatSpent
     *            The meat spent to set.
     * @return A new MeatGain object with the new value for meat spent.
     */
    public MeatGain setMeatSpent(
                                 final int meatSpent) {
        return new MeatGain(encounterMeatGain, otherMeatGain, meatSpent);
    }

    /**
     * @param encounterMeatGain
     *            The encounter meat gain to add.
     * @return A new MeatGain object with the new value for encounter meat
     *         gained.
     */
    public MeatGain addEncounterMeatGain(
                                         final int encounterMeatGain) {
        return new MeatGain(this.encounterMeatGain + encounterMeatGain, otherMeatGain, meatSpent);
    }

    /**
     * @param otherMeatGain
     *            The other meat gain to add.
     * @return A new MeatGain object with the new value for other meat gained.
     */
    public MeatGain addOtherMeatGain(
                                     final int otherMeatGain) {
        return new MeatGain(encounterMeatGain, this.otherMeatGain + otherMeatGain, meatSpent);
    }

    /**
     * @param meatSpent
     *            The meat spent to add.
     * @return A new MeatGain object with the new value for meat spent.
     */
    public MeatGain addMeatSpent(
                                 final int meatSpent) {
        return new MeatGain(encounterMeatGain, otherMeatGain, this.meatSpent + meatSpent);
    }

    /**
     * @param meatData
     *            The meat data to add.
     * @return A new MeatGain object with the new meat data values.
     */
    public MeatGain addMeatData(
                                final MeatGain meatData) {
        return addMeatData(meatData.encounterMeatGain, meatData.otherMeatGain, meatData.meatSpent);
    }

    /**
     * @param encounterMeatGain
     *            The encounter meat gain to add.
     * @param otherMeatGain
     *            The other meat gain to add.
     * @param meatSpent
     *            The meat spent to add.
     * @return A new MeatGain object with the new meat data values.
     */
    public MeatGain addMeatData(
                                final int encounterMeatGain, final int otherMeatGain,
                                final int meatSpent) {
        return new MeatGain(this.encounterMeatGain + encounterMeatGain,
                            this.otherMeatGain + otherMeatGain,
                            this.meatSpent + meatSpent);
    }

    /**
     * @return True if both meat gained and spent are zero, otherwise false.
     */
    public boolean isMeatGainSpentZero() {
        return encounterMeatGain == 0 && otherMeatGain == 0 && meatSpent == 0;
    }

    @Override
    public boolean equals(
                          final Object obj) {
        if (this == obj)
            return true;

        if (obj == null)
            return false;

        if (obj instanceof MeatGain) {
            final MeatGain other = (MeatGain) obj;
            return encounterMeatGain == other.encounterMeatGain
                   && otherMeatGain == other.otherMeatGain && meatSpent == other.meatSpent;
        }

        return false;
    }

    @Override
    public int hashCode() {
        int result = 852;
        result = 31 * result + encounterMeatGain;
        result = 31 * result + otherMeatGain;
        result = 31 * result + meatSpent;

        return result;
    }
}
