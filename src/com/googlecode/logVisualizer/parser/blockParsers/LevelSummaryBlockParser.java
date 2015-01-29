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
import java.util.Scanner;
import java.util.regex.Pattern;

import com.googlecode.logVisualizer.logData.LogDataHolder;
import com.googlecode.logVisualizer.logData.logSummary.LevelData;
import com.googlecode.logVisualizer.parser.UsefulPatterns;

/**
 * A parser for the level summary at the end of preparsed ascension logs.
 */
public final class LevelSummaryBlockParser extends AbstractBlockParser {
    private static final Pattern STATS_PER_TURN_PATTERN = Pattern.compile("Hit Level \\d+ on turn \\d+, \\d+ from last level. \\((\\d+\\.\\d+) substats / turn\\)");

    private static final Pattern ENDS_WITH_DIGIT = Pattern.compile(".*\\d$");

    private static final String HIT_LEVEL_STRING = "Hit Level";

    private static final String COMBAT_STRING = "Combats";

    private static final String NONCOMBAT_STRING = "Noncombats";

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

                // Check whether this line contains level data or total turn
                // version distribution data.
                if (line.startsWith(HIT_LEVEL_STRING)) {
                    final int levelNumber;
                    final int turnNumber;
                    double statsPerTurn = 0;

                    // Parse level and turn number
                    scanner = new Scanner(line);
                    scanner.useDelimiter(UsefulPatterns.NOT_A_NUMBER);
                    levelNumber = scanner.nextInt();
                    turnNumber = scanner.nextInt();
                    scanner.close();

                    // Parse stats per turn if they are there
                    if (STATS_PER_TURN_PATTERN.matcher(line).matches()) {
                        scanner = new Scanner(line);
                        scanner.findInLine(STATS_PER_TURN_PATTERN);
                        statsPerTurn = Double.parseDouble(scanner.match().group(1));
                        scanner.close();
                    }

                    final LevelData lastLevel = logData.getLastLevel();

                    // The data given here is are actually the turn distribution
                    // of the level before the one noted in the line above these
                    // lines.
                    for (int i = 0; i < 3; i++) {
                        line = reader.readLine();

                        if (ENDS_WITH_DIGIT.matcher(line).matches()) {
                            scanner = new Scanner(line);
                            scanner.useDelimiter(UsefulPatterns.NOT_A_NUMBER);

                            if (line.contains(COMBAT_STRING))
                                lastLevel.setCombatTurns(scanner.nextInt());
                            else if (line.contains(NONCOMBAT_STRING))
                                lastLevel.setNoncombatTurns(scanner.nextInt());
                            else
                                lastLevel.setOtherTurns(scanner.nextInt());

                            scanner.close();
                        }
                    }

                    // Add the new level
                    final LevelData newLevel = new LevelData(levelNumber, turnNumber);
                    newLevel.setStatGainPerTurn(statsPerTurn);
                    logData.addLevel(newLevel);
                } else if (UsefulPatterns.NAME_COLON_NUMBER.matcher(line).matches()) {
                    // Parse turns spent
                    scanner = new Scanner(line);
                    scanner.useDelimiter(UsefulPatterns.NOT_A_NUMBER);
                    final int turns = scanner.nextInt();
                    scanner.close();

                    // Add total turn numbers
                    if (line.contains("NONCOMBATS"))
                        logData.getLogSummary().setTotalTurnsNoncombat(turns);
                    else if (line.contains("COMBATS"))
                        logData.getLogSummary().setTotalTurnsCombat(turns);
                    else if (line.contains("OTHER"))
                        logData.getLogSummary().setTotalTurnsOther(turns);
                }

                emptyLineCounter = 0;
            } else {
                emptyLineCounter++;
                if (emptyLineCounter >= 3) {
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
        return line.contains("LEVELS");
    }
}
