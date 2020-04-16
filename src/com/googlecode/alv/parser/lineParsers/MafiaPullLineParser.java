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
import com.googlecode.alv.logData.turn.turnAction.Pull;
import com.googlecode.alv.parser.UsefulPatterns;

/**
 * A parser for the pull notation in mafia logs.
 * <p>
 * The format looks like this:
 * <p>
 * {@code pull: _amount_ _itemName_ (if there was more than one pull, this will
 * be appended as long as needed->, _amount_ _itemName_)}
 */
public final class MafiaPullLineParser extends AbstractLineParser {
    private static final Pattern PULL_PATTERN = Pattern.compile("pull: \\d+ .+");

    private static final Pattern COMMA_WHITESPACE_PATTERN = Pattern.compile(", ");

    private final Matcher pullMatcher = PULL_PATTERN.matcher(UsefulPatterns.EMPTY_STRING);

    private static final Pattern PULLED_ITEM_PATTERN = Pattern.compile("([0-9]+ ((?:[^,]+)|(?:, [^0-9]))*)(?:, )?");

    private final Matcher itemMatcher = PULLED_ITEM_PATTERN.matcher(UsefulPatterns.EMPTY_STRING);


    /**
     * {@inheritDoc}
     */
    @Override
    protected void doParsing(
                             final String line, final LogDataHolder logData) {

        itemMatcher.reset(line);
        while (itemMatcher.find()) {
            String s = itemMatcher.group(1);

            final int firstWhiteSpacePosition = s.indexOf(UsefulPatterns.WHITE_SPACE);
            final int amount = Math.max(1,
                                        Integer.parseInt(s.substring(0, firstWhiteSpacePosition)));
            final String itemName = s.substring(firstWhiteSpacePosition + 1);

            logData.addPull(new Pull(itemName,
                                     amount,
                                     logData.getLastTurnSpent().getTurnNumber(),
                                     logData.getLastDayChange().getDayNumber()));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean isCompatibleLine(
                                       final String line) {
        return pullMatcher.reset(line).matches();
    }
}
