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

import com.googlecode.logVisualizer.logData.LogComment;
import com.googlecode.logVisualizer.parser.UsefulPatterns;
import com.googlecode.logVisualizer.util.Countable;

/**
 * As {@link AbstractTurn} does for the {@link Turn} interface (and which this
 * class extends), this abstract class handles most of the data which a turn
 * interval can collect and should be used as a starting point of an actual
 * implementation of the {@link TurnInterval} interface.
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
 * <p>
 * Note: This class has a natural ordering that is inconsistent with equals and
 * all subclasses of this class should also contain this note that they do so.
 */
public abstract class AbstractTurnInterval extends AbstractTurn implements TurnInterval,
        Comparable<TurnInterval> {
    private LogComment preComment = new LogComment();

    /**
     * @param areaName
     *            The name of the area to set.
     */
    public AbstractTurnInterval(
                                final String areaName) {
        super(areaName);
    }

    /**
     * Returns the last turn number of this turn interval.
     * 
     * @see TurnEntity
     */
    public int getTurnNumber() {
        return getEndTurn();
    }

    /**
     * The turn version of a turn interval is always
     * {@link TurnVersion#NOT_DEFINED}.
     * 
     * @see TurnEntity
     */
    public TurnVersion getTurnVersion() {
        return TurnVersion.NOT_DEFINED;
    }

    /**
     * @see TurnInterval
     */
    public int getTotalTurns() {
        return getEndTurn() - getStartTurn();
    }

    /**
     * @see TurnInterval
     */
    public void setPreIntervalComment(
                                      final LogComment comment) {
        if (comment == null)
            throw new IllegalArgumentException("The comment must not be null.");

        preComment = comment;
    }

    /**
     * @see TurnInterval
     */
    public LogComment getPreIntervalComment() {
        return preComment;
    }

    /**
     * @see TurnInterval
     */
    public void setPostIntervalComment(
                                       final LogComment comment) {
        if (comment == null)
            throw new IllegalArgumentException("The comment must not be null.");

        this.comment = comment;
    }

    /**
     * @see TurnInterval
     */
    public LogComment getPostIntervalComment() {
        return comment;
    }

    /**
     * @return The difference between the start turns of this turn interval and
     *         the given turn interval. If both start turns are the same, the
     *         end turns will be compared.
     */
    public int compareTo(
                         final TurnInterval turn) {
        final int result = getStartTurn() - turn.getStartTurn();
        return result == 0 ? getEndTurn() - turn.getEndTurn() : result;
    }

    @Override
    public String toString() {
        final StringBuilder str = new StringBuilder(60);

        str.append(UsefulPatterns.SQUARE_BRACKET_OPEN);

        if (getTotalTurns() > 1) {
            str.append(getStartTurn() + 1);
            str.append(UsefulPatterns.MINUS);
        }

        str.append(getEndTurn());
        str.append(UsefulPatterns.SQUARE_BRACKET_CLOSE);
        str.append(UsefulPatterns.WHITE_SPACE);
        str.append(getAreaName());
        str.append(UsefulPatterns.WHITE_SPACE);
        str.append(getStatGain().toString());

        return str.toString();
    }

    @Override
    public boolean equals(
                          final Object obj) {
        if (super.equals(obj) && obj instanceof AbstractTurnInterval)
            return true;

        return false;
    }

    @Override
    public int hashCode() {
        int result = 11;
        result = 31 * result + super.hashCode();

        return result;
    }
}
