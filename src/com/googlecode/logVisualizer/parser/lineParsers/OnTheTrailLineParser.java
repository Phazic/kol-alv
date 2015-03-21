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

package com.googlecode.logVisualizer.parser.lineParsers;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.googlecode.logVisualizer.logData.LogDataHolder;
import com.googlecode.logVisualizer.logData.turn.SingleTurn;
import com.googlecode.logVisualizer.parser.UsefulPatterns;
import com.googlecode.logVisualizer.util.DataNumberPair;

/**
 * A parser to recognise On The Trail effect acquisition.
 * <p>
 * The format looks like this:
 * <p>
 * {@code You acquire an effect: On the Trail (duration: 40 Adventures)}
 */
public final class OnTheTrailLineParser extends AbstractLineParser {
    private static final Pattern ON_THE_TRAIL_ACQUISITION = Pattern.compile("You acquire an effect:\\s*On the Trail.*$");

    private final Matcher trailMatcher = ON_THE_TRAIL_ACQUISITION.matcher(UsefulPatterns.EMPTY_STRING);

    /**
     * @see AbstractLineParser#doParsing(String, LogDataHolder)
     */
    @Override
    protected void doParsing(
                             final String line, final LogDataHolder logData) {
        final SingleTurn turn = (SingleTurn) logData.getLastTurnSpent();
        logData.addHuntedCombat(DataNumberPair.of(turn.getEncounterName(), turn.getTurnNumber()));
    }

    /**
     * @see AbstractLineParser#isCompatibleLine(String)
     */
    @Override
    protected boolean isCompatibleLine(
                                       final String line) {
        return line.startsWith(UsefulPatterns.ACQUIRE_EFFECT_STRING) &&
                trailMatcher.reset(line).matches();
    }
}
