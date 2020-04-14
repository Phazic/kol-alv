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
import java.util.Scanner;

import com.googlecode.alv.logData.LogDataHolder;
import com.googlecode.alv.logData.MPGain;
import com.googlecode.alv.parser.UsefulPatterns;

/**
 * A parser for the mp summary at the end of preparsed ascension logs.
 */
public final class MPSummaryBlockParser extends AbstractBlockParser {
    private static final String ENCOUNTER_MP_BEGINNING_STRING = "Inside Encounters: ";

    private static final String STARFISH_MP_BEGINNING_STRING = "Starfish Familiars: ";

    private static final String RESTING_MP_BEGINNING_STRING = "Resting: ";

    private static final String OUTSIDE_ENCOUNTERS_MP_BEGINNING_STRING = "Outside Encounters: ";

    private static final String CONSUMABLES_MP_BEGINNING_STRING = "Consumables: ";

    private static final String LEVEL_BEGINNING_STRING = "Level ";

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doParsing(
                             final BufferedReader reader, final LogDataHolder logData)
                                                                                      throws IOException {
        MPGain totalMPGains = MPGain.NO_MP;
        int emptyLineCounter = 0;
        String line;

        while ((line = reader.readLine()) != null)
            if (line.length() > 0) {
                if (line.startsWith(ENCOUNTER_MP_BEGINNING_STRING))
                    totalMPGains = totalMPGains.setEncounterMPGain(parseOutMP(line,
                                                                              ENCOUNTER_MP_BEGINNING_STRING));
                else if (line.startsWith(STARFISH_MP_BEGINNING_STRING))
                    totalMPGains = totalMPGains.setStarfishMPGain(parseOutMP(line,
                                                                             STARFISH_MP_BEGINNING_STRING));
                else if (line.startsWith(RESTING_MP_BEGINNING_STRING))
                    totalMPGains = totalMPGains.setRestingMPGain(parseOutMP(line,
                                                                            RESTING_MP_BEGINNING_STRING));
                else if (line.startsWith(OUTSIDE_ENCOUNTERS_MP_BEGINNING_STRING))
                    totalMPGains = totalMPGains.setOutOfEncounterMPGain(parseOutMP(line,
                                                                                   OUTSIDE_ENCOUNTERS_MP_BEGINNING_STRING));
                else if (line.startsWith(CONSUMABLES_MP_BEGINNING_STRING))
                    totalMPGains = totalMPGains.setConsumableMPGain(parseOutMP(line,
                                                                               CONSUMABLES_MP_BEGINNING_STRING));
                else if (line.startsWith(LEVEL_BEGINNING_STRING)) {
                    final int level;
                    final int[] mpLevelData = new int[5];

                    Scanner scanner = new Scanner(line);
                    scanner.useDelimiter(UsefulPatterns.NOT_A_NUMBER);
                    level = scanner.nextInt();
                    scanner.close();

                    for (int i = 0; i < 5; i++) {
                        scanner = new Scanner(reader.readLine());
                        scanner.useDelimiter(UsefulPatterns.NOT_A_NUMBER);
                        mpLevelData[i] = scanner.nextInt();
                        scanner.close();
                    }

                    logData.getLogSummary()
                           .getMPGainSummary()
                           .addLevelData(level,
                                         new MPGain(mpLevelData[0],
                                                    mpLevelData[1],
                                                    mpLevelData[2],
                                                    mpLevelData[3],
                                                    mpLevelData[4]));
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

        logData.getLogSummary().setTotalMPGains(totalMPGains);
    }

    private static int parseOutMP(
                                  final String line, final String prefix) {
        return Integer.parseInt(line.substring(prefix.length()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean isCompatibleBlock(
                                        final String line) {
        return line.contains("MP GAINS");
    }
}
