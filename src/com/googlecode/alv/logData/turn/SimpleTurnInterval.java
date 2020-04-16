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
 * An implementation for a {@link TurnInterval}. It consists of a specified
 * start and end turn number. Note that the start turn is the turn number of the
 * last turn <b>before</b> this turn interval.
 * <p>
 * Given this class' more limited implementation of the {@link TurnInterval}
 * interface than that of {@link DetailedTurnInterval}, it is more suited for
 * cases where only turn interval data is present or necessary. For other cases,
 * usage of {@link DetailedTurnInterval} is recommended.
 * <p>
 * All methods in this class throw a {@link NullPointerException} if a null
 * object reference is passed in any parameter.
 * <p>
 * Note: This class has a natural ordering that is inconsistent with equals.
 */
public final class SimpleTurnInterval extends AbstractTurnInterval {
    private static final SortedSet<SingleTurn> emptySet = Collections.unmodifiableSortedSet(new TreeSet<SingleTurn>());

    private final int startTurn;

    private final int endTurn;

    private int unsuccessfulFreeRunaways = 0;

    /**
     * While using this constructor, please adhere to the standard set by
     * {@link TurnInterval#getStartTurn()} for the start turn number, namely
     * that it is the number of the last turn <b>before</b> this turn interval
     * started.
     * <p>
     * Note that if the value of endTurn is smaller than the value of startTurn,
     * the ending turn of this interval will be set to {@code startTurn}.
     * 
     * @param areaName
     *            The name of the area of this turn interval to set.
     * @param startTurn
     *            The start of this turn interval to set.
     * @param endTurn
     *            The end of this turn interval to set.
     * @throws IllegalArgumentException
     *             if startTurn is below 0; if endTurn is below 0
     */
    public SimpleTurnInterval(
                              final String areaName, final int startTurn, final int endTurn) {
        super(areaName);

        if (startTurn < 0 || endTurn < 0)
            throw new IllegalArgumentException("Turn range below 0.");

        this.startTurn = startTurn;
        this.endTurn = endTurn >= startTurn ? endTurn : startTurn;
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
     * @param runaways
     *            The amount of unsuccessful free runaways to add.
     */
    public void addUnsuccessfulFreeRunaways(
                                            final int runaways) {
        unsuccessfulFreeRunaways = runaways;
    }

    /**
     * @param runaways
     *            The amount of unsuccessful free runaways to set.
     */
    public void setUnsuccessfulFreeRunaways(
                                            final int runaways) {
        unsuccessfulFreeRunaways = runaways;
    }

    /**
     * @see TurnInterval
     */
    public FreeRunaways getRunawayAttempts() {
        return new FreeRunaways(getFreeRunaways() + unsuccessfulFreeRunaways, getFreeRunaways());
    }

    /**
     * This method is not supported by this class.
     */
    public void addTurn(
                        final SingleTurn turn) {
        throw new UnsupportedOperationException("Method not supported by SimpleTurnInterval class.");
    }

    /**
     * This method is not supported by this class.
     */
    public void addTurns(
                         final Collection<SingleTurn> turns) {
        throw new UnsupportedOperationException("Method not supported by SimpleTurnInterval class.");
    }

    /**
     * Returns an empty, read-only collection.
     * 
     * @see TurnInterval
     */
    public SortedSet<SingleTurn> getTurns() {
        return emptySet;
    }

    @Override
    public boolean equals(
                          final Object obj) {
        if (super.equals(obj) && obj instanceof SimpleTurnInterval) {
            final SimpleTurnInterval other = (SimpleTurnInterval) obj;

            return startTurn == other.startTurn && endTurn == other.endTurn
                   && unsuccessfulFreeRunaways == other.unsuccessfulFreeRunaways;
        }

        return false;
    }

    @Override
    public int hashCode() {
        int result = 13;
        result = 31 * result + startTurn;
        result = 31 * result + endTurn;
        result = 31 * result + unsuccessfulFreeRunaways;
        result = 31 * result + super.hashCode();

        return result;
    }
}
