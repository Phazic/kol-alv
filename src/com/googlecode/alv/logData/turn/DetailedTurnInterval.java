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

package com.googlecode.alv.logData.turn;

import java.util.Collection;
import java.util.Collections;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * An implementation of {@link TurnInterval}. It consists of a sorted collection
 * of single turns.
 * <p>
 * All methods in this class throw a {@link NullPointerException} if a null
 * object reference is passed in any parameter.
 * <p>
 * Note: This class has a natural ordering that is inconsistent with equals.
 */
public final class DetailedTurnInterval extends AbstractTurnInterval {
    private final SortedSet<SingleTurn> turns = new TreeSet<SingleTurn>();

    private int startTurn;

    private int endTurn;

    private int unsuccessfulFreeRunaways = 0;

    public DetailedTurnInterval(final SingleTurn turn, boolean isFreeTurnInterval) {
        super(turn.getAreaName());

        final int tmpStartTurn = isFreeTurnInterval ? turn.getTurnNumber() : turn.getTurnNumber() - 1;
        
        startTurn = tmpStartTurn < 0 ? 0 : tmpStartTurn;
        endTurn = turn.getTurnNumber();
        addTurnData(turn);
        turns.add(turn);
       
    }
    
    /**
     * Constructs a turn interval with the given turn as a part of it.
     * 
     * @param turn
     *            Starting point of this turn interval to set.
     */
    public DetailedTurnInterval(final SingleTurn turn) {
        this(turn, false);
    }

    /**
     * Constructs a turn interval with the given turns as a part of it.
     * 
     * @param turns
     *            The turns of this turn interval to set.
     * @param areaName
     *            the area name of this turn interval to set.
     */
    public DetailedTurnInterval(
                                final Collection<SingleTurn> turns, final String areaName) {
        super(areaName);

        startTurn = Integer.MAX_VALUE;
        endTurn = Integer.MIN_VALUE;

        addTurns(turns);
    }

    /**
     * @see TurnInterval
     */
    public int getStartTurn() {
        return startTurn;
    }

    /**
     * @see TurnInterval
     */
    public int getEndTurn() {
        return endTurn;
    }

    /**
     * Checks whether the start and end turn numbers still fit with the given
     * turn and adjusts them if needed.
     */
    private void checkStartEndBounds(
                                     final SingleTurn turn) {
        if (startTurn >= turn.getTurnNumber())
            startTurn = turn.getTurnNumber() - 1;
        if (endTurn < turn.getTurnNumber())
            endTurn = turn.getTurnNumber();
    }

    /**
     * Adds the given turn to this turn interval.
     * <p>
     * Please note that if the given turn has a turn number that is already
     * present in this turn interval, it will simply not be added, it's data
     * however will be. In general, it is advisable not to let such a case
     * happen when using this implementation of {@link TurnInterval}.
     * 
     * @throws IllegalArgumentException
     *             if area name of the turn interval is not equal to the area
     *             name of the given turn
     * @see TurnInterval
     */
    public void addTurn(
                        final SingleTurn turn) {
        if (!turn.getAreaName().equals(getAreaName()))
            throw new IllegalArgumentException("The area name of the turn must be the same as that of the turn interval.");

        checkStartEndBounds(turn);
        addTurnData(turn);
        if (turn.getTurnVersion() == TurnVersion.COMBAT && turn.isRanAwayOnThisTurn()
            && turn.isRunawaysEquipmentEquipped())
            unsuccessfulFreeRunaways++;

        turns.add(turn);
    }

    /**
     * Adds the given turns to this turn interval.
     * <p>
     * Please note that this method internally calls
     * {@link #addTurn(SingleTurn)} to actually add the given turns, so all of
     * that methods limitations will apply.
     * 
     * @see TurnInterval
     */
    public void addTurns(
                         final Collection<SingleTurn> turns) {
        for (final SingleTurn st : turns)
            addTurn(st);
    }

    /**
     * Returns a read-only collection of the turns of this turn interval.
     * 
     * @see TurnInterval
     */
    public SortedSet<SingleTurn> getTurns() {
        return Collections.unmodifiableSortedSet(turns);
    }

    /**
     * @see TurnInterval
     */
    public FreeRunaways getRunawayAttempts() {
        return new FreeRunaways(getFreeRunaways() + unsuccessfulFreeRunaways, getFreeRunaways());
    }

    @Override
    public boolean equals(
                          final Object obj) {
        if (super.equals(obj) && obj instanceof DetailedTurnInterval) {
            final DetailedTurnInterval other = (DetailedTurnInterval) obj;

            return startTurn == other.startTurn && endTurn == other.endTurn
                   && unsuccessfulFreeRunaways == other.unsuccessfulFreeRunaways
                   && turns.equals(other.turns);
        }

        return false;
    }

    @Override
    public int hashCode() {
        int result = 12;
        result = 31 * result + startTurn;
        result = 31 * result + endTurn;
        result = 31 * result + unsuccessfulFreeRunaways;
        result = 31 * result + turns.hashCode();
        result = 31 * result + super.hashCode();

        return result;
    }
}
