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

import com.googlecode.logVisualizer.logData.consumables.Consumable;
import com.googlecode.logVisualizer.parser.UsefulPatterns;
import com.googlecode.logVisualizer.util.AbstractCountable;
import com.googlecode.logVisualizer.util.Countable;

/**
 * A relatively simple representation for combat items used during combat. 
 * <p>
 * All methods in this class throw a {@link NullPointerException} if a null
 * object reference is passed in any parameter.
 * <p>
 * Note: This class has a natural ordering that is inconsistent with equals.
 */
public final class CombatItem extends AbstractCountable<CombatItem> {
    private int turnNumberUsed = -1;

    /**
     * @param name
     *            The name of this item to set.
     * @param amount
     *            The amount of this item being used
     * @throws IllegalArgumentException
     *             if amount is below 1
     */
    public CombatItem( final String name, int amount) {
        super(name, amount);
    }

    /**
     * @param name
     *            The name of this item to set.
     * @param amount
     *            The amount of this item to set.
     * @param turnNumberUsed
     *            The turn on which this item was found to set.
     * @throws IllegalArgumentException
     *             if amount is below 1; if foundOnTurn is below 0
     */
    public CombatItem( final String name, final int amount, final int turnNumberUsed) {
        this(name, amount);
        setTurnUsed( turnNumberUsed );
    }

    /**
     * @param turnNumberUsed
     *            The turn on which this item was found to set.
     * @throws IllegalArgumentException
     *             if foundOnTurn is below 0
     */
    public void setTurnUsed(final int turnNumberUsed) {
        if (turnNumberUsed < 0)
            throw new IllegalArgumentException("Turn number below 0.");

        this.turnNumberUsed = turnNumberUsed;
    }

    /**
     * @return The turn on which this item was used. If no turn was specified, -1 will be returned.
     */
    public int getTurnUsed() {
        return this.turnNumberUsed;
    }

    /**
     * @param amount
     *            The amount of this item to set.
     * @throws IllegalArgumentException
     *             if amount is below 1
     * @see Countable
     */
    @Override
    public void setAmount( final int amount ) {
        if (amount < 1)
            throw new IllegalArgumentException("Amount below 1.");

        super.setAmount(amount);
    }

    /**
     * This method not only adds the amount of the given Item to this instance,
     * it also compares the turn number of the given item with the one from this
     * instance and sets this instance's turn number to the one which was
     * smaller.
     * 
     * @see Countable
     */
    @Override
    public void merge(
                      final CombatItem i) {
        super.merge(i);
        
        if (i.getTurnUsed() < turnNumberUsed)
        	turnNumberUsed = i.getTurnUsed();
    }

    /**
     * @return The name of this item.
     * @see Countable
     */
    public Comparable<String> getComparator() {
        return getName();
    }

    /**
     * @return A deep copy of this object.
     * @see Countable
     */
    public CombatItem newInstance() {
        return turnNumberUsed < 0 ? new CombatItem(getName(), getAmount()) : new CombatItem(getName(), getAmount(), turnNumberUsed);
    }

    /**
     * @return An alphabetic comparison between this item and another one.
     *         Equal to {@code this.getName().compareToIgnoreCase(i.getName())}.
     */
    @Override
    public int compareTo(final CombatItem i) {
        return getAmount() - i.getAmount();
    }

    @Override
    public String toString() {
        final StringBuilder str = new StringBuilder(40);

        str.append(getName());
        str.append(UsefulPatterns.WHITE_SPACE);
        str.append(UsefulPatterns.ROUND_BRACKET_OPEN);
        str.append(getAmount());
        str.append(UsefulPatterns.ROUND_BRACKET_CLOSE);

        return str.toString();
    }

    @Override
    public boolean equals(final Object o) {
        if (super.equals(o) && o instanceof CombatItem)
            return ((CombatItem) o).getTurnUsed() == turnNumberUsed;

        return false;
    }

    @Override
    public int hashCode() {
        int result = 65;
        result = 31 * result + super.hashCode();
        result = 31 * result + turnNumberUsed;

        return result;
    }
}
