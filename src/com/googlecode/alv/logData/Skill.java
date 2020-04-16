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
import com.googlecode.alv.util.AbstractCountable;
import com.googlecode.alv.util.Countable;
import com.googlecode.alv.util.dataTables.DataTablesHandler;

/**
 * This class is a representation of a skill. It is intended to be used where
 * ever a skill is used and thus is able to hold all the data on the skill that
 * might be useful such as name, MP cost, amount of casts and so on.
 * <p>
 * All methods in this class throw a {@link NullPointerException} if a null
 * object reference is passed in any parameter.
 * <p>
 * Note: This class has a natural ordering that is inconsistent with equals.
 */
public final class Skill extends AbstractCountable<Skill> {
    private static final String CAST_START_STRING = "Cast ";

    private int mpCost;

    private int turnNumberOfCast = -1;

    /**
     * Constructs a new skill.
     * 
     * @param name
     *            The name of the skill to set.
     */
    public Skill(
                 final String name) {
        super(name, 0);
    }

    /**
     * Constructs a new skill.
     * 
     * @param name
     *            The name of the skill to set.
     * @param turnNumberOfCast
     *            The turn number this skill was casted on to set.
     * @throws IllegalArgumentException
     *             if turnNumberOfCast is below 0
     */
    public Skill(
                 final String name, final int turnNumberOfCast) {
        this(name);
        setTurnNumberOfCast(turnNumberOfCast);
    }

    /**
     * @param amountOfCasts
     *            The amount of skill casts to set.
     * @param mpCostOffset
     *            The MP cost offset when this skill was cast.
     * @throws IllegalArgumentException
     *             if castsAmount is below 0; if mpCostOffset is below -3
     */
    public void setCasts(
                         final int amountOfCasts, final int mpCostOffset) {
        if (amountOfCasts < 0)
            throw new IllegalArgumentException("Amount of casts below 0.");
        if (mpCostOffset < -3)
            throw new IllegalArgumentException("MP cost offset below -3.");

        super.setAmount(amountOfCasts);

        int skillCost = DataTablesHandler.HANDLER.getSkillMPCost(getName());
        if (skillCost > 0) {
            skillCost += mpCostOffset;
            if (skillCost < 1)
                skillCost = 1;
        }
        mpCost = skillCost * amountOfCasts;
    }

    /**
     * @throws UnsupportedOperationException
     *             use {@link #setCasts(int, int)} instead
     */
    @Override
    public void setAmount(
                          final int amountOfCasts) {
        throw new UnsupportedOperationException("Changing amount of casts can only be done when also changing MP cost at the same time. Use setCasts(int, int) instead.");
    }

    /**
     * This method allows to set the MP cost of this skill.
     * <p>
     * Note that it is generally recommended to let the MP cost be automatically
     * calculated by the {@link #setCasts(int, int)} method. This method should
     * only be used if the MP cost offset is not known for every single cast
     * (but the total MP cost is known), or there is some kind of special case
     * present that the automatic calculation is not able to handle.
     * 
     * @param mpCost
     *            The total MP cost to set.
     */
    public void setMpCost(
                          final int mpCost) {
        this.mpCost = mpCost;
    }

    /**
     * @return The total MP cost.
     */
    public int getMpCost() {
        return mpCost;
    }

    /**
     * @param turnNumberOfCast
     *            The turn number this skill was casted on to set.
     * @throws IllegalArgumentException
     *             if turnNumberOfCast is below 0
     */
    public void setTurnNumberOfCast(
                                    final int turnNumberOfCast) {
        if (turnNumberOfCast < 0)
            throw new IllegalArgumentException("Turn number below 0.");

        this.turnNumberOfCast = turnNumberOfCast;
    }

    /**
     * @return The turn number this skill was casted on.
     */
    public int getTurnNumberOfCast() {
        return turnNumberOfCast;
    }

    /**
     * @see Countable
     */
    @Override
    public void merge(
                      final Skill s) {
        super.setAmount(s.getAmount() + getAmount());
        mpCost += s.getMpCost();

        if (s.getTurnNumberOfCast() < turnNumberOfCast)
            turnNumberOfCast = s.getTurnNumberOfCast();
    }

    /**
     * @return The name of this skill.
     * @see Countable
     */
    public Comparable<String> getComparator() {
        return getName();
    }

    /**
     * @return A deep copy of this object.
     * @see Countable
     */
    public Skill newInstance() {
        final Skill newSkill = turnNumberOfCast < 0 ? new Skill(getName())
                                                   : new Skill(getName(), turnNumberOfCast);
        newSkill.setCasts(getAmount(), 0);
        newSkill.mpCost = mpCost;

        return newSkill;
    }

    @Override
    public String toString() {
        final StringBuilder str = new StringBuilder(40);

        str.append(CAST_START_STRING);
        str.append(getAmount());
        str.append(UsefulPatterns.WHITE_SPACE);
        str.append(getName());

        return str.toString();
    }

    @Override
    public boolean equals(
                          final Object o) {
        if (super.equals(o) && o instanceof Skill) {
            final Skill other = (Skill) o;

            return other.getTurnNumberOfCast() == turnNumberOfCast && other.getMpCost() == mpCost;
        }

        return false;
    }

    @Override
    public int hashCode() {
        int result = 30;
        result = 31 * result + super.hashCode();
        result = 31 * result + turnNumberOfCast;
        result = 31 * result + mpCost;

        return result;
    }
}
