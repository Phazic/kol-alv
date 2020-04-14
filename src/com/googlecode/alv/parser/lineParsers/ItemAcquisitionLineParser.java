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

import java.util.Scanner;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.googlecode.alv.logData.Item;
import com.googlecode.alv.logData.LogDataHolder;
import com.googlecode.alv.logData.turn.Turn;
import com.googlecode.alv.parser.UsefulPatterns;

/**
 * A parser for the item acquired notation in mafia logs.
 * <p>
 * The format looks like this:
 * <p>
 * {@code You acquire an item: _item name_}
 * <p>
 * OR
 * <p>
 * {@code You acquire _number of items_ _item name_}
 * <p>
 * OR
 * <p>
 * {@code You acquire _item name_ (_number of items_)}
 */
public final class ItemAcquisitionLineParser extends AbstractLineParser {
    private static final Pattern MULTIPLE_ITEMS_OLD = Pattern.compile("You acquire \\d+ [\\s\\w\\p{Punct}]+");

    private static final Pattern MULTIPLE_ITEMS_NEW = Pattern.compile("You acquire [\\s\\w\\p{Punct}]+ \\(\\d+\\)");

    private static final Pattern MULTIPLE_ITEMS_OLD_CAPTURE_PATTERN = Pattern.compile("You acquire (\\d*,?\\d+) (.+)");

    private static final Pattern MULTIPLE_ITEMS_NEW_CAPTURE_PATTERN = Pattern.compile("You acquire (.+) \\((\\d*,?\\d+)\\)");

    private static final String SINGLE_ITEM_STRING = "You acquire an item: ";

    private static final String ACQUIRE_STRING = "You acquire";

    private static final String ACQUIRE_EFFECT = "You acquire an effect:";

    private final Matcher multipleItemsOldMatcher = MULTIPLE_ITEMS_OLD.matcher(UsefulPatterns.EMPTY_STRING);

    private final Matcher multipleItemsNewMatcher = MULTIPLE_ITEMS_NEW.matcher(UsefulPatterns.EMPTY_STRING);

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doParsing(
                             final String line, final LogDataHolder logData) {
        final String itemName;
        int amount = 1;

        // Check whether it's only a single item acquisition or not and act
        // based on it.
        if (line.startsWith(SINGLE_ITEM_STRING))
            itemName = line.substring(SINGLE_ITEM_STRING.length());
        else if (multipleItemsOldMatcher.reset(line).matches()) {
            final Scanner scanner = new Scanner(line);
            scanner.findInLine(MULTIPLE_ITEMS_OLD_CAPTURE_PATTERN);
            final MatchResult result = scanner.match();

            if (result.group(1).contains(UsefulPatterns.COMMA))
                amount = Integer.parseInt(result.group(1).replace(UsefulPatterns.COMMA,
                                                                  UsefulPatterns.EMPTY_STRING));
            else
                amount = Integer.parseInt(result.group(1));
            itemName = result.group(2);

            scanner.close();
        } else {
            final Scanner scanner = new Scanner(line);
            scanner.findInLine(MULTIPLE_ITEMS_NEW_CAPTURE_PATTERN);
            final MatchResult result = scanner.match();

            itemName = result.group(1);
            if (result.group(2).contains(UsefulPatterns.COMMA))
                amount = Integer.parseInt(result.group(2).replace(UsefulPatterns.COMMA,
                                                                  UsefulPatterns.EMPTY_STRING));
            else
                amount = Integer.parseInt(result.group(2));

            scanner.close();
        }

        final Turn currentTurn = logData.getLastTurnSpent();
        currentTurn.addDroppedItem(new Item(itemName, amount, currentTurn.getTurnNumber()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean isCompatibleLine(
                                       final String line) {

        // Filter out you acquire an effect from items
        return !line.startsWith(ACQUIRE_EFFECT) &&
                line.startsWith(ACQUIRE_STRING)
               && (line.startsWith(SINGLE_ITEM_STRING)
                   || multipleItemsOldMatcher.reset(line).matches() || multipleItemsNewMatcher.reset(line)
                                                                                              .matches());
    }
}
