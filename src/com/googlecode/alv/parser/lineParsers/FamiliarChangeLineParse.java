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

import com.googlecode.alv.logData.LogDataHolder;
import com.googlecode.alv.logData.LogDataHolder.ParsedLogClass;
import com.googlecode.alv.logData.turn.turnAction.FamiliarChange;
import com.googlecode.alv.parser.UsefulPatterns;

/**
 * A parser for the familiar change notation in preparsed ascension logs.
 * <p>
 * The format looks like this:
 * <p>
 * {@code -> Turn [*turnNumber*] *familiarName* (*familiarPoundage* lbs)}
 */
public final class FamiliarChangeLineParse extends AbstractLineParser {
    private final Matcher familiarChangedMatcher = UsefulPatterns.FAMILIAR_CHANGED.matcher(UsefulPatterns.EMPTY_STRING);

    private static final Pattern NOT_FAMILIAR_NAME = Pattern.compile("^.*\\]\\s*|\\s*\\(.*\\)\\s*$");

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doParsing(
            final String line, final LogDataHolder logData) {
        // Parse the turn number.
        // Note that a log by the AFH parser shows the turn on which the new
        // familiar was first used, which has to be changed to adhere to the
        // contract of the FamiliarChange class.
        Scanner scanner = new Scanner(line);
        scanner.useDelimiter(UsefulPatterns.NOT_A_NUMBER);
        final int changedTurn;
        if (logData.getParsedLogCreator() == ParsedLogClass.AFH_PARSER)
            changedTurn = scanner.nextInt() - 1;
        else
            changedTurn = scanner.nextInt();
        scanner.close();

        // The name of the now used familiar.
        scanner = new Scanner(line);
        scanner.useDelimiter(NOT_FAMILIAR_NAME);
        final String familiarName = scanner.next();
        scanner.close();

        // Add the familiar change
        logData.addFamiliarChange(new FamiliarChange(familiarName, changedTurn));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean isCompatibleLine(
            final String line) {
        return familiarChangedMatcher.reset(line).matches();
    }
}
