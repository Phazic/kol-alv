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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.googlecode.alv.logData.LogDataHolder;
import com.googlecode.alv.logData.MeatGain;
import com.googlecode.alv.parser.UsefulPatterns;

/**
 * A parser for the meat spent notation in mafia logs.
 * <p>
 * The format looks like this:
 * <p>
 * {@code You spent _amount_ Meat}
 * <p>
 * OR
 * <p>
 * {@code You lose _amount_ Meat}
 */
public final class MeatSpentLineParser extends AbstractLineParser {
    private static final String MEAT_SPENT_START_STRING = "You spent ";

    private static final Pattern MEAT_SPENT = Pattern.compile("^You (?:spent|lose) \\d*,?\\d+ Meat");

    private final Matcher meatSpentMatcher = MEAT_SPENT.matcher(UsefulPatterns.EMPTY_STRING);

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doParsing(
                             final String line, final LogDataHolder logData) {
        final String informationPart;
        // Either the string starts with "You spent " or with "You lose ".
        if (line.startsWith(MEAT_SPENT_START_STRING))
            informationPart = line.substring(10);
        else
            informationPart = line.substring(9);
        final int whiteSpaceIndex = informationPart.indexOf(UsefulPatterns.WHITE_SPACE);

        final String amountString = informationPart.substring(0, whiteSpaceIndex);
        final int amount = Integer.parseInt(amountString.replace(UsefulPatterns.COMMA,
                                                                 UsefulPatterns.EMPTY_STRING));

        logData.getLastTurnSpent().addMeat(new MeatGain(0, 0, amount));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean isCompatibleLine(
                                       final String line) {
        return meatSpentMatcher.reset(line).matches();
    }
}
