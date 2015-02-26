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
import java.util.Collections;
import java.util.List;

import com.googlecode.logVisualizer.logData.Item;
import com.googlecode.logVisualizer.logData.MPGain;
import com.googlecode.logVisualizer.logData.Skill;
import com.googlecode.logVisualizer.logData.consumables.Consumable;
import com.googlecode.logVisualizer.logData.turn.turnAction.EquipmentChange;
import com.googlecode.logVisualizer.logData.turn.turnAction.FamiliarChange;
import com.googlecode.logVisualizer.parser.UsefulPatterns;
import com.googlecode.logVisualizer.util.Lists;
import com.googlecode.logVisualizer.util.SingleElementList;
import com.googlecode.logVisualizer.util.dataTables.DataTablesHandler;

/**
 * An implementation of a single turn. It uses the services of the
 * {@link AbstractTurn} class and additionally implements turn number and
 * encounter name handling among other things.
 * <p>
 * All methods in this class throw a {@link NullPointerException} if a null
 * object reference is passed in any parameter.
 * <p>
 * Note: This class has a natural ordering that is inconsistent with equals.
 */
public final class SingleTurn extends AbstractTurn implements Encounter, Comparable<TurnEntity> {
    private static final String DELIMITER_STRING = "--";

    private static final String NAVEL_RING_OF_NAVEL_GAZING = "navel ring of navel gazing";

    private static final String GREATEST_AMERICAN_PANTS = "greatest american pants";

    private static final Skill RUN = new Skill("return");

    private final String encounterName;

    private final int turnNumber;

    private final int dayNumber;

    private final EquipmentChange usedEquipment;

    private final FamiliarChange usedFamiliar;

    private boolean isDisintegrated = false;

    private boolean isBanished = false;

    private String banishedInfo = null;
    
    private TurnVersion turnVersion = TurnVersion.NOT_DEFINED;

    private List<Encounter> encounters;
    
    /**
     * @param areaName
     *            The name of the area of this turn to set.
     * @param encounterName
     *            The name of the encounter of this turn to set.
     * @param turnNumber
     *            The turn number of this turn to set.
     * @param dayNumber
     *            The day number of this turn to set.
     * @param usedEquipment
     *            The equipment used on this turn to set.
     * @param usedFamiliar
     *            The familiar used on this turn to set.
     * @throws IllegalArgumentException
     *             if turnNumber is below 0
     */
    public SingleTurn(
                      final String areaName, final String encounterName, final int turnNumber,
                      final int dayNumber, final EquipmentChange usedEquipment,
                      final FamiliarChange usedFamiliar) {
        super(areaName);

        if (encounterName == null)
            throw new NullPointerException("Encounter name must not be null.");
        if (usedEquipment == null)
            throw new NullPointerException("The equipment must not be null.");
        if (usedFamiliar == null)
            throw new NullPointerException("The familiar must not be null.");
        if (turnNumber < 0)
            throw new IllegalArgumentException("Turn number below 0.");
        if (dayNumber < 1)
            throw new IllegalArgumentException("Day number below 1.");

        this.encounterName = encounterName;
        this.turnNumber = turnNumber;
        this.dayNumber = dayNumber;
        this.usedEquipment = usedEquipment;
        this.usedFamiliar = usedFamiliar;
    }

    /**
     * Adds the data of the given turn to this turn.
     * <p>
     * This method does not add an {@link Encounter} representation of the given
     * turn to this turn. A separate call to {@link #addEncounter(Encounter)}
     * has to be made for that. However it is generally recommended to first
     * call {@link #addEncounter(Encounter)} and then this method, as otherwise
     * it could result in duplication of data in normal use-cases of turn
     * intervals.
     * 
     * @param turn
     *            The turn whose data will be added to this turn.
     */
    public void addSingleTurnData(
                                  final SingleTurn turn) {
        addTurnData(turn);
    }

    @Override
    public void addDroppedItem(
                               Item droppedItem) {
        // On single turns, item drops linked with them necessarily had to
        // happen on that particular turn.
        if (droppedItem.getFoundOnTurn() != turnNumber) {
            droppedItem = droppedItem.newInstance();
            droppedItem.setFoundOnTurn(turnNumber);
        }

        super.addDroppedItem(droppedItem);
    }

    @Override
    public void addSkillCast(
                             Skill skill) {
        // On single turns, skill casts linked with them necessarily had to
        // happen on that particular turn.
        if (skill.getTurnNumberOfCast() != turnNumber) {
            skill = skill.newInstance();
            skill.setTurnNumberOfCast(turnNumber);
        }

        super.addSkillCast(skill);
    }

    @Override
    public void addConsumableUsed(
                                  Consumable consumable) {
        // On single turns, consumables usage linked with them necessarily had
        // to happen on that particular turn.
        if (consumable.getTurnNumberOfUsage() != turnNumber) {
            consumable = consumable.newInstance();
            consumable.setTurnNumberOfUsage(turnNumber);
        }

        super.addConsumableUsed(consumable);
    }

    /**
     * @return The number of the day on which this turn was spent on.
     */
    public int getDayNumber() {
        return dayNumber;
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
     * @return {@code true} if equipment that allows for free runaways was
     *         equipped on this turn, otherwise {@code false}.
     */
    public boolean isRunawaysEquipmentEquipped() {
        return usedEquipment.isEquiped(NAVEL_RING_OF_NAVEL_GAZING)
               || usedEquipment.isEquiped(GREATEST_AMERICAN_PANTS);
    }

    /**
     * @return {@code true} if this turn is a combat and was run away from,
     *         otherwise {@code false}.
     */
    public boolean isRanAwayOnThisTurn() {
        return turnVersion == TurnVersion.COMBAT ? isSkillCast(RUN) : false;
    }

    /**
     * This flag can only be changed to {@code true} if this turn is a combat.
     * 
     * @param isDisintegrated
     *            Sets the flag on whether this combat was disintegrated or not.
     */
    public void setDisintegrated(
                                 final boolean isDisintegrated) {
        this.isDisintegrated = turnVersion == TurnVersion.COMBAT ? isDisintegrated : false;
    }

    /**
     * @return {@code true} if this combat was disintegrated. Will always return
     *         {@code false} if this turn is not a combat.
     */
    public boolean isDisintegrated() {
        return turnVersion == TurnVersion.COMBAT ? isDisintegrated : false;
    }

    /**
     * This flag can only be changed to {@code true} if this turn is a combat.
     * 
     * @param isBanished
     *            Sets the flag on whether this combat was banished or not.
     */
    public void setBanished(final boolean isBanished) {
    	setBanished(isBanished, null, null);
    }
    
    public void setBanished(final boolean isBanished, String banishName, String turns) {
    	this.isBanished = turnVersion == TurnVersion.COMBAT ? isBanished : false;
    	if (isBanished) {
    		this.banishedInfo = getEncounterName() + " {" + (banishName != null ? banishName : "unknown") + " (" + (turns != null ? turns : "???") + " turns )}";  
    	}
    }
    
    public String getBanishedInfo() {
    	return this.banishedInfo;
    }
    

    /**
     * @return {@code true} if this combat was banished. Will always return
     *         {@code false} if this turn is not a combat.
     */
    public boolean isBanished() {
        return turnVersion == TurnVersion.COMBAT ? isBanished : false;
    }

    /**
     * @see TurnEntity
     */
    public int getTurnNumber() {
        return turnNumber;
    }

    /**
     * @param turnVersion
     *            The turn version to set.
     */
    public void setTurnVersion(
                               final TurnVersion turnVersion) {
        if (turnVersion == null)
            throw new NullPointerException("Turn version must not be null.");

        this.turnVersion = turnVersion;
    }

    /**
     * @see TurnEntity
     */
    public TurnVersion getTurnVersion() {
        return turnVersion;
    }

    /**
     * Calculates the per turn MP regeneration from equipment such as the
     * Plexiglass Pith Helmet based on the used equipment and adds it to this
     * turn and the encounter of this turn if it was already created.
     * <p>
     * Note: Only call this method if you are sure that this turn actually spent
     * an adventure.
     */
    public void addMPRegen() {
        final DataTablesHandler dth = DataTablesHandler.HANDLER;
        final int tmp = dth.getMPFromEquipment(usedEquipment.getHat())
                        + dth.getMPFromEquipment(usedEquipment.getWeapon())
                        + dth.getMPFromEquipment(usedEquipment.getOffhand())
                        + dth.getMPFromEquipment(usedEquipment.getShirt())
                        + dth.getMPFromEquipment(usedEquipment.getPants())
                        + dth.getMPFromEquipment(usedEquipment.getAcc1())
                        + dth.getMPFromEquipment(usedEquipment.getAcc2())
                        + dth.getMPFromEquipment(usedEquipment.getAcc3());
        final MPGain mpRegen = new MPGain(tmp, 0, 0, 0, 0);

        addMPGain(mpRegen);

        if (encounters != null) {
            final Encounter e = encounters.get(0);
            encounters.set(0, new ImmutableEncounter(e.getAreaName(),
                                                     e.getEncounterName(),
                                                     e.getTurnNumber(),
                                                     e.getDayNumber(),
                                                     e.getUsedEquipment(),
                                                     e.getUsedFamiliar(),
                                                     e.isDisintegrated(),
                                                     e.getStatGain(),
                                                     e.getMPGain().addMPGains(mpRegen),
                                                     e.getMeat(),
                                                     e.getFreeRunaways(),
                                                     e.getTurnVersion(),
                                                     e.getNotes(),
                                                     e.getDroppedItems(),
                                                     e.getSkillsCast(),
                                                     e.getConsumablesUsed(),
                                                     e.getCombatItemsUsed()));
        }
    }

    /**
     * Adds the encounter to the encounter list of this turn.
     * <p>
     * Note that if no other encounter was present in the list, this turn will
     * also be added to the encounter list as the first element. This means that
     * data added to this turn afterwards will not appear in the linked
     * encounter. Users have to keep this in mind and have to be careful to not
     * suffer a data loss through naive usage of the {@link SingleTurn} class.
     * <p>
     * This method does not add the data of the given encounter to this turn. A
     * separate call to {@link #addSingleTurnData(SingleTurn)} has to be made
     * for that. However it is generally recommended to first call this method
     * and then {@link #addSingleTurnData(SingleTurn)}, as otherwise it could
     * result in duplication of data in normal use-cases of turn intervals.
     * 
     * @param e
     *            Adds the given encounter to the encounter list of this turn.
     */
    public void addEncounter(
                             final Encounter e) {
        if (encounters == null) {
            encounters = Lists.newArrayList();
            encounters.add(toEncounter());
        }

        encounters.add(e);
    }

    /**
     * Returns the encounters that occurred during this turn. By convention, the
     * first element of the collection is the encounter that actually spent an
     * adventure.
     * 
     * @return The encounters that occurred during this turn.
     */
    public Collection<Encounter> getEncounters() {
        if (encounters == null)
            return new SingleElementList<Encounter>(this);

        return Collections.unmodifiableList(encounters);
    }

    /**
     * @return An immutable {@link Encounter} representation of this turn with
     *         all of its data.
     */
    public Encounter toEncounter() {
        return toEncounter(turnNumber);
    }

    /**
     * @param turnNumber
     *            The turn number the returned encounter should use.
     * @return An immutable {@link Encounter} representation of this turn with
     *         all of its data, but using the given turn number.
     */
    public Encounter toEncounter(
                                 final int turnNumber) {
        return new ImmutableEncounter(getAreaName(),
                                      encounterName,
                                      turnNumber,
                                      dayNumber,
                                      usedEquipment,
                                      usedFamiliar,
                                      isDisintegrated,
                                      getStatGain(),
                                      getMPGain(),
                                      getMeat(),
                                      getFreeRunaways(),
                                      turnVersion,
                                      getNotes(),
                                      getDroppedItems(),
                                      getSkillsCast(),
                                      getConsumablesUsed(),
                                      getCombatItemsUsed());
    }

    /**
     * @return The difference between this turns turn number and the turn number
     *         of the given {@link TurnEntity}.
     */
    public int compareTo(
                         final TurnEntity te) {
    	if (te instanceof SingleTurn)
    		return 100 * (dayNumber - ((SingleTurn) te).getDayNumber()) + turnNumber - te.getTurnNumber();
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
        str.append(DELIMITER_STRING);
        str.append(UsefulPatterns.WHITE_SPACE);
        str.append(encounterName);
        str.append(UsefulPatterns.WHITE_SPACE);
        str.append(getStatGain().toString());

        return str.toString();
    }

    @Override
    public boolean equals(
                          final Object o) {
        if (super.equals(o) && o instanceof SingleTurn) {
            final SingleTurn st = (SingleTurn) o;

            return turnNumber == st.getTurnNumber() && turnVersion == st.getTurnVersion()
                   && dayNumber == st.getDayNumber() && encounterName.equals(st.getEncounterName())
                   && isDisintegrated == st.isDisintegrated()
                   && usedEquipment.equals(st.getUsedEquipment())
                   && usedFamiliar.equals(st.getUsedFamiliar())
                   && getEncounters().equals(st.getEncounters());
        }

        return false;
    }
    
    @Override
    public int hashCode() {
        int result = 23;
        result = 31 * result + turnNumber;
        result = 31 * result + turnVersion.hashCode();
        result = 31 * result + dayNumber;
        result = 31 * result + encounterName.hashCode();
        result = 31 * result + (isDisintegrated ? 1231 : 1237);
        result = 31 * result + usedEquipment.hashCode();
        result = 31 * result + usedFamiliar.hashCode();
        result = 31 * result + getEncounters().hashCode();
        result = 31 * result + super.hashCode();

        return result;
    }
}
