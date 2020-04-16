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
import java.util.regex.MatchResult;
import java.util.regex.Pattern;

import com.googlecode.alv.logData.LogDataHolder;
import com.googlecode.alv.logData.Statgain;

/**
 * A parser for the stats summary at the end of preparsed ascension logs.
 */
public final class StatsSummaryBlockParser extends AbstractBlockParser {
    private static final Pattern STAT_SUMMARY_LINE_CAPTURE_PATTERN = Pattern.compile("\\w+:\\s+(\\-?\\d+)\\s+(\\-?\\d+)\\s+(\\-?\\d+).*");

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doParsing(
                             final BufferedReader reader, final LogDataHolder logData)
                                                                                      throws IOException {
        int statSummaryLineCounter = 0;
        int emptyLineCounter = 0;
        String line;

        while ((line = reader.readLine()) != null)
            if (line.length() > 4) {
                if (statSummaryLineCounter < 4
                    && STAT_SUMMARY_LINE_CAPTURE_PATTERN.matcher(line).matches()) {
                    // Parse stats
                    final Scanner scanner = new Scanner(line);
                    scanner.findInLine(STAT_SUMMARY_LINE_CAPTURE_PATTERN);
                    final MatchResult result = scanner.match();
                    scanner.close();

                    final int muscleStats = Integer.parseInt(result.group(1));
                    final int mystStats = Integer.parseInt(result.group(2));
                    final int moxieStats = Integer.parseInt(result.group(3));

                    final Statgain stats = new Statgain(muscleStats, mystStats, moxieStats);

                    // Set stat gain summaries
                    // This switch construct is not really the most elegant way
                    // to go about things, but it does what it is supposed to
                    // do.
                    switch (statSummaryLineCounter) {
                        case 0:
                            logData.getLogSummary().setTotalStatgains(stats);
                            break;
                        case 1:
                            logData.getLogSummary().setCombatsStatgains(stats);
                            break;
                        case 2:
                            logData.getLogSummary().setNoncombatsStatgains(stats);
                            break;
                        case 3:
                            logData.getLogSummary().setOthersStatgains(stats);
                            break;
                        default:
                            break;
                    }

                    statSummaryLineCounter++;
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
        return line.contains("STATS");
    }

}
