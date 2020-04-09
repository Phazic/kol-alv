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

package com.googlecode.logVisualizer.logData.turn;

import java.util.Collection;

import com.googlecode.logVisualizer.logData.*;
import com.googlecode.logVisualizer.logData.consumables.Consumable;
import com.googlecode.logVisualizer.util.Countable;
import com.googlecode.logVisualizer.util.CountableSet;

/**
 * This abstract class handles most of the data which a turn can collect and
 * should be used as a starting point of an actual implementation of the
 * {@link Turn} interface.
 * <p>
 * Note that all value classes handled by this class, which implement the
 * {@link Countable} interface, don't need to take special actions to make sure
 * no data corruption happens by sharing instances. The internal data
 * collections of this class will take care of this on their own. However, when
 * an object is added to this class, it should always be expected that it has
 * been cloned in some way.
 * <p>
 * All methods in this class throw a {@link NullPointerException} if a null
 * object reference is passed in any parameter.
 */
public abstract class AbstractTurn implements Turn {
    private final String areaName;

    private MeatGain meat = MeatGain.NO_MEAT;

    private MPGain mpGain = MPGain.NO_MP;

    private Statgain statGain = Statgain.NO_STATS;

    private final CountableSet<Item> droppedItems = new CountableSet<Item>();

    private final CountableSet<Skill> skillsCast = new CountableSet<Skill>();

    private final CountableSet<CombatItem> combatItemsUsed = new CountableSet<CombatItem>();
    
    private final CountableSet<Consumable> consumablesUsed = new CountableSet<Consumable>();

    private int successfulFreeRunaways = 0;

    protected LogComment comment = new LogComment();

    private boolean isFreeTurn = false;
    
    /**
     * @param areaName
     *            The name of the area to set.
     */
    public AbstractTurn(
                        final String areaName) {
        if (areaName == null)
            throw new NullPointerException("Area name must not be null.");

        this.areaName = areaName;
    }

    /**
     * @see TurnEntity
     */
    public String getAreaName() {
        return areaName;
    }

    /**
     * @see Turn
     */
    public void addStatGain(
                            final Statgain stats) {
        statGain = statGain.addStats(stats);
    }

    /**
     * @see Turn
     */
    public void setStatGain(
                            final Statgain stats) {
        statGain = stats;
    }

    /**
     * @see TurnEntity
     */
    public Statgain getStatGain() {
        return statGain;
    }

    /**
     * @see TurnEntity
     */
    public Statgain getTotalStatGain() {
        Statgain totalStatgain = statGain;
        for (final Consumable c : consumablesUsed.getElements())
            totalStatgain = totalStatgain.addStats(c.getStatGain());

        return totalStatgain;
    }

    /**
     * @see Turn
     */
    public void setMPGain(
                          final MPGain mpGain) {
        this.mpGain = mpGain;
    }

    /**
     * @see Turn
     */
    public void addMPGain(
                          final MPGain mpGain) {
        this.mpGain = this.mpGain.addMPGains(mpGain);
    }

    /**
     * @see TurnEntity
     */
    public MPGain getMPGain() {
        return mpGain;
    }

    /**
     * @see Turn
     */
    public void setMeat(
                        final MeatGain meat) {
        this.meat = meat;
    }

    /**
     * @see Turn
     */
    public void addMeat(
                        final MeatGain meat) {
        this.meat = this.meat.addMeatData(meat);
    }

    /**
     * @see TurnEntity
     */
    public MeatGain getMeat() {
        return meat;
    }

    /**
     * @see Turn
     */
    public void addDroppedItem(
                               final Item droppedItem) {
        droppedItems.addElement(droppedItem);
    }

    /**
     * @see Turn
     */
    public void setDroppedItems(
                                final Collection<Item> droppedItems) {
        this.droppedItems.setElements(droppedItems);
    }

    /**
     * @see TurnEntity
     */
    public Collection<Item> getDroppedItems() {
        return droppedItems.getElements();
    }

    /**
     * @see TurnEntity
     */
    public boolean isItemDropped(
                                 final Item i) {
        return droppedItems.contains(i);
    }

    /**
     * @see TurnEntity
     */
    public boolean isItemDropped(
                                 final String i) {
        return droppedItems.containsByName(i);
    }

    /**
     * @see Turn
     */
    public void addCombatItemUsed(CombatItem ci) {
        this.combatItemsUsed.addElement( ci );
    }
    
    /**
     * @see Turn
     */
    public void setCombatItemsUsed(final Collection<CombatItem> combatItemsUsed) {
        this.combatItemsUsed.setElements( combatItemsUsed );
    }
    
    /**
     * @see TurnEntity
     */
    public Collection<CombatItem> getCombatItemsUsed() {
        return this.combatItemsUsed.getElements();
    }
    
    /**
     * @see TurnEntity
     */
    public boolean isCombatItemUsed(final CombatItem ci) {
        return this.combatItemsUsed.contains( ci );
    }
    
    /**
     * @see TurnEntity
     */
    public boolean isCombatItemUsed(String combatItemName) {
        return this.combatItemsUsed.containsByName( combatItemName );
    }
    
    /**
     * @see Turn
     */
    public void addSkillCast(
                             final Skill skill) {
        skillsCast.addElement(skill);
    }

    /**
     * @see Turn
     */
    public void setSkillsCast(
                              final Collection<Skill> skillsCast) {
        this.skillsCast.setElements(skillsCast);
    }

    /**
     * @see TurnEntity
     */
    public Collection<Skill> getSkillsCast() {
        return skillsCast.getElements();
    }

    /**
     * @see TurnEntity
     */
    public boolean isSkillCast(
                               final Skill s) {
        return skillsCast.contains(s);
    }

    /**
     * @see TurnEntity
     */
    public boolean isSkillCast(
                               final String s) {
        return skillsCast.containsByName(s);
    }

    /**
     * @see Turn
     */
    public void addConsumableUsed(
                                  final Consumable consumable) {
        consumablesUsed.addElement(consumable);
    }

    /**
     * @see Turn
     */
    public void setConsumablesUsed(
                                   final Collection<Consumable> consumablesUsed) {
        this.consumablesUsed.setElements(consumablesUsed);
    }

    /**
     * @see TurnEntity
     */
    public Collection<Consumable> getConsumablesUsed() {
        return consumablesUsed.getElements();
    }

    /**
     * @see TurnEntity
     */
    public boolean isConsumableUsed(
                                    final Consumable c) {
        return consumablesUsed.contains(c);
    }

    /**
     * @see TurnEntity
     */
    public boolean isConsumableUsed(
                                    final String c) {
        return consumablesUsed.containsByName(c);
    }

    /**
     * @see Turn
     */
    public void addFreeRunaways(
                                final int freeRunaways) {
        successfulFreeRunaways += freeRunaways;
    }

    /**
     * @see Turn
     */
    public void setFreeRunaways(
                                final int freeRunaways) {
        successfulFreeRunaways = freeRunaways;
    }

    /**
     * @see TurnEntity
     */
    public int getFreeRunaways() {
        return successfulFreeRunaways;
    }

    /**
     * Flags a turn as being free or not
     * @param isFreeTurn Whether the turn should be marked free
     */
    public void setFreeTurn(boolean isFreeTurn) {
        this.isFreeTurn = isFreeTurn;
    }
    
    /**
     * @return Whether or not this turn was "Free"
     */
    public boolean isFreeTurn() {
        return this.isFreeTurn;
    }
    
    /**
     * @see Turn
     */
    public void setNotes(
                         final String notes) {
        comment.setComments(notes);
    }

    /**
     * @see Turn
     */
    public void addNotes(
                         final String notes) {
        comment.addComments(notes);
    }

    /**
     * @see TurnEntity
     */
    public String getNotes() {
        return comment.getComments();
    }

    /**
     * @param turn
     *            The turn whose data will be added to this turn.
     */
    protected void addTurnData(
                               final Turn turn) {
        if (turn == null)
            throw new NullPointerException("Turn must not be null.");

        meat = meat.addMeatData(turn.getMeat());
        statGain = statGain.addStats(turn.getStatGain());
        mpGain = mpGain.addMPGains(turn.getMPGain());
        successfulFreeRunaways += turn.getFreeRunaways();
        addNotes(turn.getNotes());
        for (final Item i : turn.getDroppedItems())
            addDroppedItem(i);
        for (final Skill s : turn.getSkillsCast())
            addSkillCast(s);
        for (final Consumable c : turn.getConsumablesUsed())
            addConsumableUsed(c);
        for (final CombatItem ci : turn.getCombatItemsUsed())
            addCombatItemUsed( ci );
    }

    /**
     * Empties out all internal data collections.
     */
    protected void clearAllTurnDataCollections() {
        droppedItems.clear();
        skillsCast.clear();
        consumablesUsed.clear();
    }

    @Override
    public boolean equals(
                          final Object o) {
        if (o == null)
            return false;

        if (o == this)
            return true;

        if (o instanceof AbstractTurn) {
            final AbstractTurn at = (AbstractTurn) o;

            return meat.equals(at.getMeat()) && mpGain.equals(at.getMPGain())
                   && statGain.equals(at.getStatGain()) && areaName.equals(at.getAreaName())
                   && droppedItems.getElements().equals(at.droppedItems)
                   && skillsCast.getElements().equals(at.skillsCast)
                   && consumablesUsed.getElements().equals(at.consumablesUsed)
                   && comment.equals(at.comment) && successfulFreeRunaways == at.getFreeRunaways();
        }

        return false;
    }

    @Override
    public int hashCode() {
        int result = 48;
        result = 31 * result + meat.hashCode();
        result = 31 * result + mpGain.hashCode();
        result = 31 * result + statGain.hashCode();
        result = 31 * result + areaName.hashCode();
        result = 31 * result + droppedItems.hashCode();
        result = 31 * result + skillsCast.hashCode();
        result = 31 * result + consumablesUsed.hashCode();
        result = 31 * result + comment.hashCode();
        result = 31 * result + successfulFreeRunaways;

        return result;
    }
}
