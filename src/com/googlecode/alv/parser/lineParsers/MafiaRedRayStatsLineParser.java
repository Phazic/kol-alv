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

package com.googlecode.alv.parser.lineParsers;

import com.googlecode.alv.logData.LogDataHolder;
import com.googlecode.alv.parser.UsefulPatterns;

/**
 * A parser for the substats gained from red rays in mafia logs.
 * <p>
 * The format looks like this:
 * <p>
 * {@code Round _roundNumber_: _familiarName_ swings his eyestalk toward your
 * opponent, firing a searing ray of heat at it, dealing _damageAmount_ damage.
 * Man. That was way more entertaining than fireworks!You gain _amount_
 * Muscleboundness.You gain _amount_ Magicalness.You gain _amount_ Chutzpah.}
 * <p>
 * Things such as {@code You gain a Muscle point!} can be intermixed between the
 * stat gains.
 */
public final class MafiaRedRayStatsLineParser extends AbstractLineParser {
    private static final String RED_RAY_STRING = " swings his eyestalk toward your opponent, "
                                                 + "firing a searing ray of heat at it, dealing ";

    private static final String YOU_GAIN_STRING = "You gain ";

    private final StatLineParser statParser = new StatLineParser();

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doParsing(
                             final String line, final LogDataHolder logData) {
        final String[] gains = line.split("That was way more entertaining than fireworks!")[1].split("\\.|\\!");
        for (final String s : gains)
            statParser.parseLine(s, logData);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean isCompatibleLine(
                                       final String line) {
        return line.startsWith(UsefulPatterns.COMBAT_ROUND_LINE_BEGINNING_STRING)
               && line.contains(RED_RAY_STRING) && line.contains(YOU_GAIN_STRING);
    }
}
