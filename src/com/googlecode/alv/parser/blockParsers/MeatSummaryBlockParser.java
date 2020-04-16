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

package com.googlecode.alv.parser.blockParsers;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Scanner;

import com.googlecode.alv.logData.LogDataHolder;
import com.googlecode.alv.logData.MeatGain;
import com.googlecode.alv.parser.UsefulPatterns;

/**
 * A parser for the meat summary at the end of preparsed ascension logs.
 */
public final class MeatSummaryBlockParser extends AbstractBlockParser {
    private static final String TOTAL_MEAT_GAIN = "Total meat gained:";

    private static final String TOTAL_MEAT_SPENT = "Total meat spent:";

    private static final String LEVEL = "Level ";

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
                if (line.startsWith(TOTAL_MEAT_GAIN)) {
                    // Parse and set total meat gain
                    scanner = new Scanner(line);
                    scanner.useDelimiter(UsefulPatterns.NOT_A_NUMBER);
                    logData.getLogSummary().setTotalMeatGain(scanner.nextInt());
                    scanner.close();
                } else if (line.startsWith(TOTAL_MEAT_SPENT)) {
                    // Parse and set total meat spent
                    scanner = new Scanner(line);
                    scanner.useDelimiter(UsefulPatterns.NOT_A_NUMBER);
                    logData.getLogSummary().setTotalMeatSpent(scanner.nextInt());
                    scanner.close();
                } else if (line.startsWith(LEVEL)) {
                    final int level;
                    final int[] meatLevelData = new int[3];

                    scanner = new Scanner(line);
                    scanner.useDelimiter(UsefulPatterns.NOT_A_NUMBER);
                    level = scanner.nextInt();
                    scanner.close();

                    for (int i = 0; i < 3; i++) {
                        scanner = new Scanner(reader.readLine());
                        scanner.useDelimiter(UsefulPatterns.NOT_A_NUMBER);
                        meatLevelData[i] = scanner.nextInt();
                        scanner.close();
                    }

                    logData.getLogSummary()
                           .getMeatSummary()
                           .addLevelData(level,
                                         new MeatGain(meatLevelData[0],
                                                      meatLevelData[1],
                                                      meatLevelData[2]));
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
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean isCompatibleBlock(
                                        final String line) {
        return line.contains("MEAT");
    }
}
