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

import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.googlecode.alv.logData.Item;
import com.googlecode.alv.logData.LogDataHolder;
import com.googlecode.alv.logData.turn.TurnInterval;
import com.googlecode.alv.parser.UsefulPatterns;

/**
 * A parser for the dropped item notation in preparsed ascension logs.
 * <p>
 * The format looks like this:
 * <p>
 * {@code +> [*turnNumber*] Got *itemName* (if there was more than one itemdrop,
 * this will be appended as long as needed->, *itemName*)}
 */
public final class DroppedItemLineParser extends AbstractLineParser {
    private final Matcher droppedItemMatcher = UsefulPatterns.ITEM_FOUND.matcher(UsefulPatterns.EMPTY_STRING);

    private static final Pattern NOT_ITEM_NAME = Pattern.compile("^.*\\]\\s*Got\\s*|,\\s*");

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doParsing(
                             final String line, final LogDataHolder logData) {
        // Parse the turn number
        final int foundTurn = Integer.parseInt(line.substring(line.indexOf(UsefulPatterns.SQUARE_BRACKET_OPEN) + 1,
                                                              line.indexOf(UsefulPatterns.SQUARE_BRACKET_CLOSE)));

        // Parse out the item names and add all items
        final Scanner scanner = new Scanner(line);
        scanner.useDelimiter(NOT_ITEM_NAME);
        final TurnInterval lastInterval = (TurnInterval) logData.getLastTurnSpent();
        while (scanner.hasNext())
            lastInterval.addDroppedItem(new Item(scanner.next(), 1, foundTurn));
        scanner.close();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean isCompatibleLine(
                                       final String line) {
        return droppedItemMatcher.reset(line).matches();
    }
}