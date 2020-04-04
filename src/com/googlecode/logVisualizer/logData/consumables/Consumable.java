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

package com.googlecode.logVisualizer.logData.consumables;

import java.util.Map;

import com.googlecode.logVisualizer.logData.Statgain;
import com.googlecode.logVisualizer.util.AbstractCountable;
import com.googlecode.logVisualizer.util.Countable;
import com.googlecode.logVisualizer.util.Maps;

/**
 * A representation of a consumable item. This class should be used whenever an
 * item is used and/or consumed. All necessary data should be able to be stored
 * inside an instance of this class.
 * <p>
 * This class is instanced by calling its static factory methods. There are
 * different versions for Food, Booze, Spleen and other consumables.
 * <p>
 * All methods in this class throw a {@link NullPointerException} if a null
 * object reference is passed in any parameter.
 * <p>
 * Note: This class has a natural ordering that is inconsistent with equals.
 */
public abstract class Consumable extends AbstractCountable<Consumable> {
    static final String ADVENTURES_GAINED_STRING = "adventures gained";

    private int adventureGain;

    private int turnNumberOfUsage = -1;

    private int dayNumberOfUsage = -1;

    private Statgain statGain = Statgain.NO_STATS;

    private final ConsumableComparator comparator = new ConsumableComparator();

    /**
     * @param name
     *            The name of this consumable to set.
     * @param adventureGain
     *            The adventure gain of this consumable to set. A value below 0
     *            isn't a valid parameter.
     * @param amountUsed
     *            The amount used of this consumable to set. A value below 1
     *            isn't a valid parameter.
     * @throws IllegalArgumentException
     *             if adventureGain is below 0; if amountUsed is below 1
     */
    Consumable(
               final String name, final int adventureGain, final int amountUsed) {
        super(name, amountUsed);

        if (adventureGain < 0)
            throw new IllegalArgumentException("Adventure gain below 0.");
        if (amountUsed < 1)
            throw new IllegalArgumentException("Amount used below 1.");

        this.adventureGain = adventureGain;
    }

    /**
     * @param name
     *            The name of this consumable to set.
     * @param adventureGain
     *            The adventure gain of this consumable to set. A value below 0
     *            isn't a valid parameter.
     * @param amountUsed
     *            The amount used of this consumable to set. A value below 1
     *            isn't a valid parameter.
     * @param turnNumberOfUsage
     *            The turn this consumable was used on to set. A value below 0
     *            isn't a valid parameter.
     * @throws IllegalArgumentException
     *             if adventureGain is below 0; if amountUsed is below 1; if
     *             turnNumberOfUsage is below 0
     */
    Consumable(
               final String name, final int adventureGain, final int amountUsed,
               final int turnNumberOfUsage) {
        this(name, adventureGain, amountUsed);

        if (turnNumberOfUsage < 0)
            throw new IllegalArgumentException("Turn number below 0.");

        this.turnNumberOfUsage = turnNumberOfUsage;
    }

    /**
     * @param name
     *            The name of this consumable to set.
     * @param adventureGain
     *            The adventure gain of this consumable to set.
     * @param amountUsed
     *            The amount used of this consumable to set.
     * @return A new Consumable with the specified parameters.
     * @throws IllegalArgumentException
     *             if adventureGain is below 0; if amountUsed is below 1
     */
    public static Consumable newFoodConsumable(
                                               final String name, final int adventureGain,
                                               final int amountUsed) {
        return new FoodConsumable(name, adventureGain, amountUsed);
    }

    /**
     * @param name
     *            The name of this consumable to set.
     * @param adventureGain
     *            The adventure gain of this consumable to set.
     * @param amountUsed
     *            The amount used of this consumable to set.
     * @param turnNumberOfUsage
     *            The turn this consumable was used on to set.
     * @return A new Consumable with the specified parameters.
     * @throws IllegalArgumentException
     *             if adventureGain is below 0; if amountUsed is below 1; if
     *             turnNumberOfUsage is below 0
     */
    public static Consumable newFoodConsumable(
                                               final String name, final int adventureGain,
                                               final int amountUsed, final int turnNumberOfUsage) {
        return new FoodConsumable(name, adventureGain, amountUsed, turnNumberOfUsage);
    }

    /**
     * @param name
     *            The name of this consumable to set.
     * @param adventureGain
     *            The adventure gain of this consumable to set.
     * @param amountUsed
     *            The amount used of this consumable to set.
     * @return A new Consumable with the specified parameters.
     * @throws IllegalArgumentException
     *             if adventureGain is below 0; if amountUsed is below 1
     */
    public static Consumable newBoozeConsumable(
                                                final String name, final int adventureGain,
                                                final int amountUsed) {
        return new BoozeConsumable(name, adventureGain, amountUsed);
    }

    /**
     * @param name
     *            The name of this consumable to set.
     * @param adventureGain
     *            The adventure gain of this consumable to set.
     * @param amountUsed
     *            The amount used of this consumable to set.
     * @param turnNumberOfUsage
     *            The turn this consumable was used on to set.
     * @return A new Consumable with the specified parameters.
     * @throws IllegalArgumentException
     *             if adventureGain is below 0; if amountUsed is below 1; if
     *             turnNumberOfUsage is below 0
     */
    public static Consumable newBoozeConsumable(
                                                final String name, final int adventureGain,
                                                final int amountUsed, final int turnNumberOfUsage) {
        return new BoozeConsumable(name, adventureGain, amountUsed, turnNumberOfUsage);
    }

    /**
     * @param name
     *            The name of this consumable to set.
     * @param adventureGain
     *            The adventure gain of this consumable to set.
     * @param amountUsed
     *            The amount used of this consumable to set.
     * @return A new Consumable with the specified parameters.
     * @throws IllegalArgumentException
     *             if adventureGain is below 0; if amountUsed is below 1
     */
    public static Consumable newSpleenConsumable(
                                                 final String name, final int adventureGain,
                                                 final int amountUsed) {
        return new SpleenConsumable(name, adventureGain, amountUsed);
    }

    /**
     * @param name
     *            The name of this consumable to set.
     * @param adventureGain
     *            The adventure gain of this consumable to set.
     * @param amountUsed
     *            The amount used of this consumable to set.
     * @param turnNumberOfUsage
     *            The turn this consumable was used on to set.
     * @return A new Consumable with the specified parameters.
     * @throws IllegalArgumentException
     *             if adventureGain is below 0; if amountUsed is below 1; if
     *             turnNumberOfUsage is below 0
     */
    public static Consumable newSpleenConsumable(
                                                 final String name, final int adventureGain,
                                                 final int amountUsed, final int turnNumberOfUsage) {
        return new SpleenConsumable(name, adventureGain, amountUsed, turnNumberOfUsage);
    }

    /**
     * @param name
     *            The name of this consumable to set.
     * @param adventureGain
     *            The adventure gain of this consumable to set.
     * @param amountUsed
     *            The amount used of this consumable to set.
     * @return A new Consumable with the specified parameters.
     * @throws IllegalArgumentException
     *             if adventureGain is below 0; if amountUsed is below 1
     */
    public static Consumable newOtherConsumable(
                                                final String name, final int adventureGain,
                                                final int amountUsed) {
        return new OtherConsumable(name, adventureGain, amountUsed);
    }

    /**
     * @param name
     *            The name of this consumable to set.
     * @param adventureGain
     *            The adventure gain of this consumable to set.
     * @param amountUsed
     *            The amount used of this consumable to set.
     * @param turnNumberOfUsage
     *            The turn this consumable was used on to set.
     * @return A new Consumable with the specified parameters.
     * @throws IllegalArgumentException
     *             if adventureGain is below 0; if amountUsed is below 1; if
     *             turnNumberOfUsage is below 0
     */
    public static Consumable newOtherConsumable(
                                                final String name, final int adventureGain,
                                                final int amountUsed, final int turnNumberOfUsage) {
        return new OtherConsumable(name, adventureGain, amountUsed, turnNumberOfUsage);
    }

    /**
     * @return The {@link ConsumableVersion} of this consumable.
     */
    public abstract ConsumableVersion getConsumableVersion();

    /**
     * @return The adventure gain of this consumable.
     */
    public int getAdventureGain() {
        return adventureGain;
    }

    /**
     * @param amountUsed
     *            The amount used of this consumable to set. A value below 1
     *            isn't a valid parameter.
     * @see Countable
     * @throws IllegalArgumentException
     *             if amountUsed is below 1
     */
    @Override
    public void setAmount(
                          final int amountUsed) {
        if (amountUsed < 1)
            throw new IllegalArgumentException("Amount used below 1.");

        super.setAmount(amountUsed);
    }

    /**
     * @param turnNumberOfUsage
     *            The turn number of when this consumable was used to set.
     * @throws IllegalArgumentException
     *             if turnNumberOfUsage is below 0
     */
    public void setTurnNumberOfUsage(
                                     final int turnNumberOfUsage) {
        if (turnNumberOfUsage < 0)
            throw new IllegalArgumentException("Turn number must not be below 0.");

        this.turnNumberOfUsage = turnNumberOfUsage;
    }

    /**
     * @return The turn number of when this consumable was used. If no value has
     *         been specified, -1 will be return.
     */
    public int getTurnNumberOfUsage() {
        return turnNumberOfUsage;
    }

    /**
     * @param dayNumberOfUsage
     *            The day number of when this consumable was used to set.
     * @throws IllegalArgumentException
     *             if dayNumberOfUsage is below 1
     */
    public void setDayNumberOfUsage(
                                    final int dayNumberOfUsage) {
        if (dayNumberOfUsage < 1)
            throw new IllegalArgumentException("Day number must not be below 1.");

        this.dayNumberOfUsage = dayNumberOfUsage;
    }

    /**
     * @return The day number of when this consumable was used. If no value has
     *         been specified, -1 will be return.
     */
    public int getDayNumberOfUsage() {
        return dayNumberOfUsage;
    }

    /**
     * @param statGain
     *            The stat gain of this consumable to set.
     */
    public void setStatGain(
                            final Statgain statGain) {
        this.statGain = statGain;
    }

    /**
     * @return The stat gain of this consumable.
     */
    public Statgain getStatGain() {
        return statGain;
    }

    /**
     * @return The name of this consumable and the day it was used on.
     * @see Countable
     */
    public ConsumableComparator getComparator() {
        return comparator;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void merge(
                      final Consumable c) {
        super.merge(c);
        adventureGain += c.getAdventureGain();
        statGain = statGain.addStats(c.getStatGain());
    }

    @Override
    public boolean equals(
                          final Object o) {
        if (super.equals(o) && o instanceof Consumable) {
            final Consumable c = (Consumable) o;

            return c.getAdventureGain() == adventureGain
                   && c.getTurnNumberOfUsage() == turnNumberOfUsage
                   && c.getDayNumberOfUsage() == dayNumberOfUsage
                   && c.getConsumableVersion() == getConsumableVersion()
                   && statGain.equals(c.getStatGain());
        }

        return false;
    }

    @Override
    public int hashCode() {
        int result = 1701;
        result = 31 * result + adventureGain;
        result = 31 * result + turnNumberOfUsage;
        result = 31 * result + dayNumberOfUsage;
        result = 31 * result + getConsumableVersion().hashCode();
        result = 31 * result + statGain.hashCode();

        return result;
    }

    /**
     * Helper class to handle comparator implementation of the {@link Countable}
     * interface.
     */
    private class ConsumableComparator implements Comparable<ConsumableComparator> {
        private String getName() {
            return Consumable.this.getName();
        }

        private int getDayNumber() {
            return dayNumberOfUsage;
        }

        public int compareTo(
                             final ConsumableComparator o) {
            final int tmp = Consumable.this.getName().compareTo(o.getName());

            return tmp != 0 ? tmp : dayNumberOfUsage - o.getDayNumber();
        }

        @Override
        public boolean equals(
                              final Object obj) {
            if (obj == null)
                return false;

            if (this == obj)
                return true;

            if (obj instanceof ConsumableComparator) {
                final ConsumableComparator that = (ConsumableComparator) obj;
                return getDayNumber() == that.getDayNumber() && getName().equals(that.getName());
            }

            return false;
        }

        @Override
        public int hashCode() {
            int result = 579;
            result = 31 * result + getDayNumber();
            result = 31 * result + getName().hashCode();

            return result;
        }
    }

    /**
     * A simple enumeration for various consumable types.
     */
    public static enum ConsumableVersion {
        FOOD, BOOZE, SPLEEN, OTHER;

        private static final Map<String, ConsumableVersion> stringToEnum = Maps.newHashMap();

        static {
            for (final ConsumableVersion op : values())
                stringToEnum.put(op.toString(), op);
        }

        /**
         * @param consumableVersionName Name of consumable version
         * @return The enum whose toString method returns a string which is
         *         equal to the given string. If no match is found this method
         *         will return {@code OTHER}.
         */
        public static ConsumableVersion fromString(
                                                   final String consumableVersionName) {
            if (consumableVersionName == null)
                throw new NullPointerException("The turn version name must not be null.");

            final ConsumableVersion turnVersion = stringToEnum.get(consumableVersionName);

            return turnVersion != null ? turnVersion : OTHER;
        }
    }
}
