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

package com.googlecode.alv.logData.turn;

import java.util.Collection;

import com.googlecode.alv.logData.*;
import com.googlecode.alv.logData.consumables.Consumable;

/**
 * An interface for mutable turns of an ascension log.
 * <p>
 * This interface should define all methods necessary to change the data
 * accessible through the {@link TurnEntity} interface.
 */
public interface Turn extends TurnEntity {
    /**
     * @param stats
     *            The stat gains to add.
     */
    public void addStatGain(
                            final Statgain stats);

    /**
     * @param stats
     *            The stat gains to set.
     */
    public void setStatGain(
                            final Statgain stats);

    /**
     * @param mpGain
     *            The MP gains to set.
     */
    public void setMPGain(
                          final MPGain mpGain);

    /**
     * @param mpGain
     *            The MP gains to add.
     */
    public void addMPGain(
                          final MPGain mpGain);

    /**
     * @param meat
     *            The meat data to set.
     */
    public void setMeat(
                        final MeatGain meat);

    /**
     * @param meat
     *            The meat data to add.
     */
    public void addMeat(
                        final MeatGain meat);

    /**
     * @param droppedItem
     *            The item to add.
     */
    public void addDroppedItem(
                               final Item droppedItem);

    /**
     * @param droppedItems
     *            The dropped items to set.
     */
    public void setDroppedItems(
                                final Collection<Item> droppedItems);

    /***
     * 
     * @param combatItem the combat item to add
     */
    public void addCombatItemUsed(final CombatItem combatItem);
    
    /**
     * @param combatItems collection of combat items to set
     */
    public void setCombatItemsUsed(final Collection<CombatItem> combatItems);
    
    /**
     * @param skill
     *            The skill to add.
     */
    public void addSkillCast(
                             final Skill skill);

    /**
     * @param skillsCast
     *            The skills cast to set.
     */
    public void setSkillsCast(
                              final Collection<Skill> skillsCast);

    /**
     * @param consumable
     *            The consumable to add.
     */
    public void addConsumableUsed(
                                  final Consumable consumable);

    /**
     * @param consumablesUsed
     *            The consumables used to set.
     */
    public void setConsumablesUsed(
                                   final Collection<Consumable> consumablesUsed);

    /**
     * @param freeRunaways
     *            The number of successful free runaways to add.
     */
    public void addFreeRunaways(
                                final int freeRunaways);

    /**
     * @param freeRunaways
     *            The number of successful free runaways to set.
     */
    public void setFreeRunaways(
                                final int freeRunaways);

    /**
     * @param notes
     *            The notes tagged to this turn to set.
     */
    public void setNotes(
                         final String notes);

    /**
     * Adds the given notes to this turn. The already existing notes and the
     * ones added will be divided by a line break ({@code"\n"}).
     * 
     * @param notes
     *            The notes tagged to this turn to add.
     */
    public void addNotes(
                         final String notes);
}