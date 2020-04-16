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
import java.util.SortedSet;

import com.googlecode.alv.logData.LogComment;

/**
 * An interface for turn intervals of an ascension log.
 * <p>
 * This interface extends the general {@link Turn} interface with methods that
 * are specific to turn intervals.
 */
public interface TurnInterval extends Turn {
    /**
     * Will return the turn number of the last turn before this turn interval
     * starts.
     * <p>
     * The reasoning behind this is that a turn interval consisting of one
     * single turn is the interval from {@code turn.getTurnNumber()-1} to
     * {@code turn.getTurnNumber()}.
     * 
     * @return The start of this turn interval.
     */
    public int getStartTurn();

    /**
     * @return The last turn of this turn interval.
     */
    public int getEndTurn();

    /**
     * @return The amount of turns in this turn interval.
     */
    public int getTotalTurns();

    /**
     * Adds the given turn to this turn interval.
     * 
     * @param turn
     *            The turn to add.
     * @throws IllegalArgumentException
     *             if area name of the turn interval is not equal to the area
     *             name of the given turn
     */
    public void addTurn(
                        final SingleTurn turn);

    /**
     * Adds the given turns to this turn interval.
     * 
     * @param turns
     *            The turns to add.
     */
    public void addTurns(
                         final Collection<SingleTurn> turns);

    /**
     * Returns the collection of single turns spent during this turn interval.
     * 
     * @return The single turns of this turn interval. Can be empty, if no turns
     *         have been set.
     */
    public SortedSet<SingleTurn> getTurns();

    /**
     * @return The runaway data of this turn.
     */
    public FreeRunaways getRunawayAttempts();

    /**
     * Sets the pre-interval log comment associated with this turn interval to
     * the given log comment.
     * 
     * @param comment
     *            The log comment to set.
     */
    public void setPreIntervalComment(
                                      final LogComment comment);

    /**
     * @return The pre-interval log comment associated with this turn interval.
     */
    public LogComment getPreIntervalComment();

    /**
     * Sets the post-interval log comment associated with this turn interval to
     * the given log comment.
     * 
     * @param comment
     *            The log comment to set.
     */
    public void setPostIntervalComment(
                                       final LogComment comment);

    /**
     * @return The post-interval log comment associated with this turn interval.
     */
    public LogComment getPostIntervalComment();
}
