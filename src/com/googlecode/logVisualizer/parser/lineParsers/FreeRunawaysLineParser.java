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

import java.util.Scanner;
import java.util.regex.Matcher;

import com.googlecode.logVisualizer.logData.LogDataHolder;
import com.googlecode.logVisualizer.logData.turn.SimpleTurnInterval;
import com.googlecode.logVisualizer.parser.UsefulPatterns;

/**
 * A parser for the free runaways usage notation in preparsed ascension logs.
 * <p>
 * The format looks like this:
 * <p>
 * {@code &> _successfulAttempts_ / _totslAttempts_ free retreats}
 */
public final class FreeRunawaysLineParser extends AbstractLineParser {
    private final Matcher freeRunawaysMatcher = UsefulPatterns.FREE_RUNAWAYS_USAGE.matcher(UsefulPatterns.EMPTY_STRING);

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doParsing(
                             final String line, final LogDataHolder logData) {
        // Parse the usage numbers
        final Scanner scanner = new Scanner(line);
        scanner.useDelimiter(UsefulPatterns.NOT_A_NUMBER);
        final int successfulUsages = scanner.nextInt();
        final int attemptedUsages = scanner.nextInt();
        scanner.close();

        // Add usage numbers.
        final SimpleTurnInterval sti = (SimpleTurnInterval) logData.getLastTurnSpent();
        sti.setFreeRunaways(successfulUsages);
        sti.setUnsuccessfulFreeRunaways(attemptedUsages);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean isCompatibleLine(
                                       final String line) {
        return freeRunawaysMatcher.reset(line).matches();
    }
}
