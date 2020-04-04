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

package com.googlecode.logVisualizer.logData.turn;

import java.util.Collection;

import com.googlecode.logVisualizer.logData.*;
import com.googlecode.logVisualizer.logData.consumables.Consumable;

/**
 * An interface for immutable turns of an ascension log.
 * <p>
 * This interface should define access points to all the data that is generally
 * interesting about turns, but not methods to change said data.
 */
public interface TurnEntity {
    /**
     * @return The turn number.
     */
    public int getTurnNumber();

    /**
     * @return The name of the area this turn was spent in.
     */
    public String getAreaName();

    /**
     * @return The stat gain from this turn. This doesn't entail stat gains from
     *         consumables used during this turn.
     */
    public Statgain getStatGain();

    /**
     * @return The stat gain from this turn including those from consumables.
     */
    public Statgain getTotalStatGain();

    /**
     * @return The mp gains from this turn.
     */
    public MPGain getMPGain();

    /**
     * @return The meat data object.
     */
    public MeatGain getMeat();

    /**
     * @return The dropped items from this turn.
     */
    public Collection<Item> getDroppedItems();

    /**
     * @param i Item to check for dropping
     * @return {@code true} if the given item has dropped on this turn,
     *         otherwise {@code false}. This check is solely based on
     *         {@link Item#getComparator()} and nothing else.
     */
    public boolean isItemDropped(
                                 final Item i);

    /**
     * @param i Name of item to check for dropping
     * @return {@code true} if an item with the given name has been dropped on
     *         this turn, otherwise {@code false}. This check is solely based on
     *         {@link Item#getName()} and nothing else.
     */
    public boolean isItemDropped(
                                 final String i);

    /**
     * @return The skills cast this turn.
     */
    public Collection<Skill> getSkillsCast();
    
    /**
     * @param s Skill to check for having been cast
     * @return {@code true} if the given skill has been cast on this turn,
     *         otherwise {@code false}. This check is solely based on
     *         {@link Skill#getComparator()} and nothing else.
     */
    public boolean isSkillCast(
                               final Skill s);

    /**
     * @param s Skill to check for having been cast
     * @return {@code true} if a skill with the given name has been cast on this
     *         turn, otherwise {@code false}. This check is solely based on
     *         {@link Skill#getName()} and nothing else.
     */
    public boolean isSkillCast(
                               final String s);
    
    /**
     * @param i Combat item to check for having been used
     * @return {@code true} if the given combat item has been used on this turn,
     *         otherwise {@code false}. This check is solely based on
     *         {@link CombatItem#getComparator()} and nothing else.
     */
    public boolean isCombatItemUsed(final CombatItem i);

    /**
     * @param s Name of combat item to check for having been used
     * @return {@code true} if a combat item with the given name has been used on this
     *         turn, otherwise {@code false}. This check is solely based on
     *         {@link Skill#getName()} and nothing else.
     */
    public boolean isCombatItemUsed(final String s);

    /**
     * @return The combat items used this turn.
     */
    public Collection<CombatItem> getCombatItemsUsed();
    
    /**
     * @return The consumables used this turn.
     */
    public Collection<Consumable> getConsumablesUsed();

    /**
     * @param c Consumable to check for having been used
     * @return {@code true} if the given consumable has been used on this turn,
     *         otherwise {@code false}. This check is solely based on
     *         {@link Consumable#getComparator()} and nothing else.
     */
    public boolean isConsumableUsed(
                                    final Consumable c);

    /**
     * @param c Name of consumable to check for having been consumed
     * @return {@code true} if a consumable with the given name has been used on
     *         this turn, otherwise {@code false}. This check is solely based on
     *         {@link Consumable#getName()} and nothing else.
     */
    public boolean isConsumableUsed(
                                    final String c);

    /**
     * @return The number of free runaways.
     */
    public int getFreeRunaways();

    /**
     * @return The notes tagged to this turn.
     */
    public String getNotes();

    /**
     * @return The turn version.
     */
    public TurnVersion getTurnVersion();
}
