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

package com.googlecode.alv.parser.blockParsers;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;

import com.googlecode.alv.logData.LogDataHolder;
import com.googlecode.alv.parser.UsefulPatterns;
import com.googlecode.alv.util.DataNumberPair;
import com.googlecode.alv.util.Lists;

/**
 * A parser for the familiar summary at the end of preparsed ascension logs.
 */
public final class FamiliarSummaryBlockParser extends AbstractBlockParser {
    private static final Pattern NOT_FAMILIAR_NAME = Pattern.compile("\\s*:.*");

    // Slightly untidy looking regex used so future familiars with numbers in
    // their name will work with this.
    private static final Pattern NOT_TURNS_SPENT = Pattern.compile("^[\\w\\p{Punct}\\s]+:\\s*|[\\p{L}\\s]+\\(.*\\)\\s*");

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doParsing(
                             final BufferedReader reader, final LogDataHolder logData)
                                                                                      throws IOException {
        final List<DataNumberPair<String>> familiarUsage = Lists.newArrayList();
        int emptyLineCounter = 0;
        String line;
        Scanner scanner;

        while ((line = reader.readLine()) != null)
            if (!line.equals(UsefulPatterns.EMPTY_STRING)) {
                if (UsefulPatterns.NAME_COLON_NUMBER.matcher(line).matches()) {
                    String familiarName;
                    int turns;

                    // Parse familiar name
                    scanner = new Scanner(line);
                    scanner.useDelimiter(NOT_FAMILIAR_NAME);
                    familiarName = scanner.next();
                    scanner.close();

                    // Parse turns spent with this familiar
                    scanner = new Scanner(line);
                    scanner.useDelimiter(NOT_TURNS_SPENT);
                    turns = scanner.nextInt();
                    scanner.close();

                    // Add familiar usage to the list
                    familiarUsage.add(DataNumberPair.of(familiarName, turns));
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

        // Set the familiar usage list to the on created here
        logData.getLogSummary().setFamiliarUsage(familiarUsage);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean isCompatibleBlock(
                                        final String line) {
        return line.contains("FAMILIARS");
    }
}
