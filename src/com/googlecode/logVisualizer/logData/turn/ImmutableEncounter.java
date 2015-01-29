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
import java.util.List;
import java.util.Map;

import com.googlecode.logVisualizer.logData.*;
import com.googlecode.logVisualizer.logData.consumables.Consumable;
import com.googlecode.logVisualizer.logData.turn.turnAction.EquipmentChange;
import com.googlecode.logVisualizer.logData.turn.turnAction.FamiliarChange;
import com.googlecode.logVisualizer.parser.UsefulPatterns;
import com.googlecode.logVisualizer.util.Countable;
import com.googlecode.logVisualizer.util.CountableSet;
import com.googlecode.logVisualizer.util.Lists;
import com.googlecode.logVisualizer.util.Maps;

/**
 * A completely immutable encounter implementation (if there are objects
 * returned by the getters that could be modified, they will be cloned before
 * they are returned).
 * <p>
 * This class can be considered the sister class of {@link SingleTurn} and it is
 * recommended to use it in that way. For example, it is probably better to use
 * {@link SingleTurn} as the builder for instances of this class, as its
 * constructor is rather unhandily.
 * <p>
 * Note: This class has a natural ordering that is inconsistent with equals.
 */
public final class ImmutableEncounter implements Encounter, Comparable<TurnEntity> {
    private final String areaName;

    private final String encounterName;

    private final String notes;

    private final int turnNumber;

    private final int dayNumber;

    private final int freeRunaways;

    private final EquipmentChange usedEquipment;

    private final FamiliarChange usedFamiliar;

    private final Statgain statgain;

    private final MPGain mpGain;

    private final MeatGain meatgain;

    private final boolean isDisintegrated;

    private final TurnVersion turnVersion;

    private final Map<String, Item> itemdrops;

    private final Map<String, Skill> skillCasts;

    private final CountableSet<Consumable> consumables;

    public ImmutableEncounter(
                              final String areaName, final String encounterName,
                              final int turnNumber, final int dayNumber,
                              final EquipmentChange usedEquipment,
                              final FamiliarChange usedFamiliar, final boolean isDisintegrated,
                              final Statgain statgain, final MPGain mpGain,
                              final MeatGain meatgain, final int freeRunaways,
                              final TurnVersion turnVersion, final String notes,
                              final Collection<Item> itemdrops, final Collection<Skill> skillCasts,
                              final Collection<Consumable> consumables) {
        if (areaName == null)
            throw new NullPointerException("Area name must not be null.");
        if (encounterName == null)
            throw new NullPointerException("Encounter name must not be null.");
        if (usedEquipment == null)
            throw new NullPointerException("The equipment must not be null.");
        if (usedFamiliar == null)
            throw new NullPointerException("The familiar must not be null.");
        if (statgain == null)
            throw new NullPointerException("The statgain must not be null.");
        if (mpGain == null)
            throw new NullPointerException("The MP gain must not be null.");
        if (meatgain == null)
            throw new NullPointerException("The meat gain must not be null.");
        if (turnVersion == null)
            throw new NullPointerException("The turn version must not be null.");
        if (notes == null)
            throw new NullPointerException("The notes must not be null.");
        if (itemdrops == null)
            throw new NullPointerException("The itemdrops must not be null.");
        if (skillCasts == null)
            throw new NullPointerException("The skill casts must not be null.");
        if (consumables == null)
            throw new NullPointerException("The consumables must not be null.");
        if (turnNumber < 0)
            throw new IllegalArgumentException("Turn number below 0.");
        if (dayNumber < 1)
            throw new IllegalArgumentException("Day number below 1.");
        if (freeRunaways < 0)
            throw new IllegalArgumentException("The number of free runaways must not be below 0.");

        this.areaName = areaName;
        this.encounterName = encounterName;
        this.turnNumber = turnNumber;
        this.dayNumber = dayNumber;
        this.usedEquipment = usedEquipment;
        this.usedFamiliar = usedFamiliar;
        this.isDisintegrated = isDisintegrated;
        this.statgain = statgain;
        this.mpGain = mpGain;
        this.meatgain = meatgain;
        this.freeRunaways = freeRunaways;
        this.turnVersion = turnVersion;
        this.notes = notes;

        this.itemdrops = Maps.newHashMap((int) (itemdrops.size() * 1.4));
        for (final Item i : itemdrops)
            this.itemdrops.put(i.getName(), i.newInstance());

        this.skillCasts = Maps.newHashMap((int) (skillCasts.size() * 1.4));
        for (final Skill s : skillCasts)
            this.skillCasts.put(s.getName(), s.newInstance());

        this.consumables = new CountableSet<Consumable>();
        for (final Consumable c : consumables)
            this.consumables.addElement(c);
    }

    /**
     * @return The turn number.
     */
    public int getTurnNumber() {
        return turnNumber;
    }

    /**
     * @return The number of the day on which this turn was spent on.
     */
    public int getDayNumber() {
        return dayNumber;
    }

    /**
     * @return The name of the area this turn was spent in.
     */
    public String getAreaName() {
        return areaName;
    }

    /**
     * @return The name of the encounter found on this turn.
     */
    public String getEncounterName() {
        return encounterName;
    }

    /**
     * @return The familiar used on this turn.
     */
    public FamiliarChange getUsedFamiliar() {
        return usedFamiliar;
    }

    /**
     * @return The equipment used on this turn.
     */
    public EquipmentChange getUsedEquipment() {
        return usedEquipment;
    }

    /**
     * @return {@code true} if this combat was disintegrated. Will always return
     *         {@code false} if this turn is not a combat.
     */
    public boolean isDisintegrated() {
        return turnVersion == TurnVersion.COMBAT ? isDisintegrated : false;
    }

    /**
     * @return The stat gain from this turn. This doesn't entail stat gains from
     *         consumables used during this turn.
     */
    public Statgain getStatGain() {
        return statgain;
    }

    /**
     * @return The stat gain from this turn including those from consumables.
     */
    public Statgain getTotalStatGain() {
        Statgain result = statgain;
        for (final Consumable c : consumables.getElements())
            result = result.addStats(c.getStatGain());

        return result;
    }

    /**
     * @return The mp gains from this turn.
     */
    public MPGain getMPGain() {
        return mpGain;
    }

    /**
     * @return The meat data object.
     */
    public MeatGain getMeat() {
        return meatgain;
    }

    /**
     * @return The number of free runaways.
     */
    public int getFreeRunaways() {
        return freeRunaways;
    }

    /**
     * @return The notes tagged to this turn.
     */
    public String getNotes() {
        return notes;
    }

    /**
     * @return The turn version.
     */
    public TurnVersion getTurnVersion() {
        return turnVersion;
    }

    /**
     * @return The dropped items from this turn.
     */
    public Collection<Item> getDroppedItems() {
        return getCollectionFromMap(itemdrops);
    }

    /**
     * @return {@code true} if the given item has dropped on this turn,
     *         otherwise {@code false}. This check is solely based on
     *         {@link Item#getComparator()} and nothing else.
     */
    public boolean isItemDropped(
                                 final Item i) {
        return isItemDropped(i.getName());
    }

    /**
     * @return {@code true} if an item with the given name has been dropped on
     *         this turn, otherwise {@code false}. This check is solely based on
     *         {@link Item#getName()} and nothing else.
     */
    public boolean isItemDropped(
                                 final String i) {
        return itemdrops.containsKey(i);
    }

    /**
     * @return The skills cast this turn.
     */
    public Collection<Skill> getSkillsCast() {
        return getCollectionFromMap(skillCasts);
    }

    /**
     * @return {@code true} if the given skill has been cast on this turn,
     *         otherwise {@code false}. This check is solely based on
     *         {@link Skill#getComparator()} and nothing else.
     */
    public boolean isSkillCast(
                               final Skill s) {
        return isSkillCast(s.getName());
    }

    /**
     * @return {@code true} if a skill with the given name has been cast on this
     *         turn, otherwise {@code false}. This check is solely based on
     *         {@link Skill#getName()} and nothing else.
     */
    public boolean isSkillCast(
                               final String s) {
        return skillCasts.containsKey(s);
    }

    /**
     * @return The consumables used this turn.
     */
    public Collection<Consumable> getConsumablesUsed() {
        return consumables.getElementsDeepCopy();
    }

    /**
     * @return {@code true} if the given consumable has been used on this turn,
     *         otherwise {@code false}. This check is solely based on
     *         {@link Consumable#getComparator()} and nothing else.
     */
    public boolean isConsumableUsed(
                                    final Consumable c) {
        return consumables.contains(c);
    }

    /**
     * @return {@code true} if a consumable with the given name has been used on
     *         this turn, otherwise {@code false}. This check is solely based on
     *         {@link Consumable#getName()} and nothing else.
     */
    public boolean isConsumableUsed(
                                    final String c) {
        return consumables.containsByName(c);
    }

    private static <T extends Countable<T>> Collection<T> getCollectionFromMap(
                                                                               final Map<String, T> map) {
        final List<T> result = Lists.newArrayList(map.size());
        for (final T t : map.values())
            result.add(t.newInstance());

        return result;
    }

    /**
     * @return The difference between this encounters turn number and the turn
     *         number of the given {@link TurnEntity}.
     */
    public int compareTo(
                         TurnEntity te) {
        return turnNumber - te.getTurnNumber();
    }

    @Override
    public String toString() {
        final StringBuilder str = new StringBuilder(80);

        str.append(UsefulPatterns.SQUARE_BRACKET_OPEN);
        str.append(turnNumber);
        str.append(UsefulPatterns.SQUARE_BRACKET_CLOSE);
        str.append(UsefulPatterns.WHITE_SPACE);
        str.append(getAreaName());
        str.append(UsefulPatterns.WHITE_SPACE);
        str.append("--");
        str.append(UsefulPatterns.WHITE_SPACE);
        str.append(encounterName);
        str.append(UsefulPatterns.WHITE_SPACE);
        str.append(getStatGain().toString());

        return str.toString();
    }

    @Override
    public boolean equals(
                          Object obj) {
        if (obj == null)
            return false;

        if (this == obj)
            return true;

        if (obj instanceof ImmutableEncounter) {
            final ImmutableEncounter that = (ImmutableEncounter) obj;

            return turnNumber == that.getTurnNumber() && dayNumber == that.getDayNumber()
                   && freeRunaways == that.getFreeRunaways()
                   && isDisintegrated == that.isDisintegrated()
                   && turnVersion == that.getTurnVersion() && statgain.equals(that.getStatGain())
                   && mpGain.equals(that.getMPGain()) && meatgain.equals(that.getMeat())
                   && usedFamiliar.equals(that.getUsedFamiliar())
                   && usedEquipment.equals(that.getUsedEquipment())
                   && areaName.equals(that.getAreaName())
                   && encounterName.equals(that.getEncounterName())
                   && notes.equals(that.getNotes()) && itemdrops.equals(that.itemdrops)
                   && skillCasts.equals(that.skillCasts) && consumables.equals(that.consumables);
        }

        return false;
    }

    @Override
    public int hashCode() {
        int result = 23;
        result = 31 * result + areaName.hashCode();
        result = 31 * result + consumables.hashCode();
        result = 31 * result + dayNumber;
        result = 31 * result + encounterName.hashCode();
        result = 31 * result + freeRunaways;
        result = 31 * result + (isDisintegrated ? 1231 : 1237);
        result = 31 * result + itemdrops.hashCode();
        result = 31 * result + meatgain.hashCode();
        result = 31 * result + mpGain.hashCode();
        result = 31 * result + notes.hashCode();
        result = 31 * result + skillCasts.hashCode();
        result = 31 * result + statgain.hashCode();
        result = 31 * result + turnNumber;
        result = 31 * result + turnVersion.hashCode();
        result = 31 * result + usedEquipment.hashCode();
        result = 31 * result + usedFamiliar.hashCode();

        return result;
    }
}