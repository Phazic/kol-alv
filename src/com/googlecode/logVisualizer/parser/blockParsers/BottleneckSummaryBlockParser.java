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
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.googlecode.logVisualizer.logData.LogDataHolder;
import com.googlecode.logVisualizer.parser.UsefulPatterns;
import com.googlecode.logVisualizer.util.DataNumberPair;

/**
 * A parser for the bottleneck summary at the end of preparsed ascension logs.
 */
public final class BottleneckSummaryBlockParser extends AbstractBlockParser {
    private static final Pattern LOST_COMBAT_PATTERN = Pattern.compile("\\s*(.+?)\\: (\\d+)");

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doParsing(
                             final BufferedReader reader, final LogDataHolder logData)
                                                                                      throws IOException {
        int emptyLineCounter = 0;
        String line;
        Scanner scanner;

        while ((line = reader.readLine()) != null)
            if (!line.equals(UsefulPatterns.EMPTY_STRING)) {
                try {
                    if (line.endsWith("bloopers")) {
                        scanner = new Scanner(line);
                        scanner.useDelimiter(UsefulPatterns.NOT_A_NUMBER);

                        // Set the number of Bloopers found
                        if (scanner.hasNextInt())
                            logData.getLogSummary()
                                   .get8BitRealm()
                                   .setBloopersFound(scanner.nextInt());

                        scanner.close();
                    } else if (line.contains("dairy goats")) {
                        scanner = new Scanner(line);
                        scanner.useDelimiter(UsefulPatterns.NOT_A_NUMBER);

                        // Set the number of Dairy Goats found and how many
                        // cheeses dropped.
                        if (scanner.hasNextInt()) {
                            final int goatsFound = scanner.nextInt();
                            final int cheeseFound = scanner.nextInt();

                            logData.getLogSummary().getGoatlet().setDairyGoatsFound(goatsFound);
                            logData.getLogSummary().getGoatlet().setCheeseFound(cheeseFound);
                        }

                        scanner.close();
                    } else if (line.startsWith("Number of lost combats: ")) {
                        reader.mark(500);
                        while ((line = reader.readLine()) != null && line.length() > 0) {
                            final Matcher m = LOST_COMBAT_PATTERN.matcher(line);
                            m.find();
                            logData.addLostCombat(DataNumberPair.of(m.group(1),
                                                                    Integer.parseInt(m.group(2))));

                            reader.mark(500);
                        }
                        reader.reset();
                    }
                } catch (final NoSuchElementException e) {
                    e.printStackTrace();
                }

                emptyLineCounter = 0;
            } else {
                emptyLineCounter++;
                if (emptyLineCounter >= 2)
                    break;
            }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean isCompatibleBlock(
                                        final String line) {
        return line.contains("BOTTLENECKS");
    }
}
