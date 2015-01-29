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

package com.googlecode.logVisualizer.parser.blockParsers;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;

import com.googlecode.logVisualizer.logData.LogDataHolder;
import com.googlecode.logVisualizer.parser.UsefulPatterns;
import com.googlecode.logVisualizer.util.DataNumberPair;
import com.googlecode.logVisualizer.util.Lists;

/**
 * A parser for the semirare summary at the end of preparsed ascension logs.
 */
public final class SemirareSummaryBlockParser extends AbstractBlockParser {
    private static final Pattern NUMBER_COLON_NAME = Pattern.compile("^\\d+\\s*:\\s*\\w+.*");

    private static final Pattern NOT_SEMIRARE_NAME = Pattern.compile(".+:\\s*");

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doParsing(
                             final BufferedReader reader, final LogDataHolder logData)
                                                                                      throws IOException {
        final List<DataNumberPair<String>> semirares = Lists.newArrayList();
        int emptyLineCounter = 0;
        String line;
        Scanner scanner;

        while ((line = reader.readLine()) != null)
            if (!line.equals(UsefulPatterns.EMPTY_STRING)) {
                if (NUMBER_COLON_NAME.matcher(line).matches()) {
                    final String semirareName;
                    final int turnNumber;

                    // Pares the semirare name
                    scanner = new Scanner(line);
                    scanner.useDelimiter(NOT_SEMIRARE_NAME);
                    semirareName = scanner.next();
                    scanner.close();

                    // Parse the turn number
                    scanner = new Scanner(line);
                    scanner.useDelimiter(UsefulPatterns.NOT_A_NUMBER);
                    turnNumber = scanner.nextInt();
                    scanner.close();

                    // Add the semirare to the list
                    semirares.add(DataNumberPair.of(semirareName, turnNumber));
                }

                emptyLineCounter = 0;
            } else {
                emptyLineCounter++;
                if (emptyLineCounter >= 2) {
                    reader.reset();
                    break;
                }
                reader.mark(10);
            }

        // Set the semirare list
        logData.getLogSummary().setSemirares(semirares);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean isCompatibleBlock(
                                        final String line) {
        return line.contains("SEMI-RARES");
    }
}
