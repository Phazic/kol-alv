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

package com.googlecode.alv.parser.lineParsers;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.googlecode.alv.logData.LogDataHolder;
import com.googlecode.alv.logData.turn.SingleTurn;
import com.googlecode.alv.parser.UsefulPatterns;

public final class MafiaDisintegrateLineParser extends AbstractLineParser {
    private static final Pattern YELLOW_EFFECT_ACQUISITION = Pattern.compile("You acquire an effect:\\s*Everything Looks Yellow.*$");

    private final Matcher yellowRayMatcher = YELLOW_EFFECT_ACQUISITION.matcher(UsefulPatterns.EMPTY_STRING);

    private static final Pattern MAJOR_YELLOW_RAY = Pattern.compile("Round \\d+: .+? swings his eyestalk around and unleashes a massive"
                                                                    + " ray of yellow energy, completely disintegrating your opponent.");

    private final Matcher majorYellowRayMatcher = MAJOR_YELLOW_RAY.matcher(UsefulPatterns.EMPTY_STRING);

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doParsing(
                             final String line, final LogDataHolder logData) {
        ((SingleTurn) logData.getLastTurnSpent()).setDisintegrated(true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean isCompatibleLine(
                                       final String line) {
        boolean yellowRay = line.startsWith(UsefulPatterns.ACQUIRE_EFFECT_STRING) &&
                yellowRayMatcher.reset(line).matches();

        boolean heRay = (line.startsWith(UsefulPatterns.COMBAT_ROUND_LINE_BEGINNING_STRING) && majorYellowRayMatcher.reset(
                line).matches());

        return yellowRay || heRay;
    }
}
