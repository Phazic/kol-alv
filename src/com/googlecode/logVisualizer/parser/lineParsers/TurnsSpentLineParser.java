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

package com.googlecode.logVisualizer.parser.lineParsers;

import java.util.regex.Matcher;

import com.googlecode.logVisualizer.logData.LogDataHolder;
import com.googlecode.logVisualizer.logData.Statgain;
import com.googlecode.logVisualizer.logData.LogDataHolder.ParsedLogClass;
import com.googlecode.logVisualizer.logData.turn.SimpleTurnInterval;
import com.googlecode.logVisualizer.logData.turn.TurnInterval;
import com.googlecode.logVisualizer.parser.UsefulPatterns;

/**
 * A parser for the turns spent notation in preparsed ascension logs.
 * <p>
 * The format looks like this (the statgains may be missing):
 * <p>
 * {@code [_turnNumber_] _areaName_ [_mus_,_myst_,_mox_]}
 * <p>
 * OR
 * <p>
 * {@code [_startTurn_-_endTurn_] _areaName_ [_mus_,_myst_,_mox_]}
 */
public final class TurnsSpentLineParser extends AbstractLineParser {
    private static final String ASCENSION_START_STRING = "Ascension Start";

    private final Matcher turnsUsedMatcher = UsefulPatterns.TURNS_USED.matcher(UsefulPatterns.EMPTY_STRING);

    private final Matcher areaStatgainMatcher = UsefulPatterns.AREA_STATGAIN.matcher(UsefulPatterns.EMPTY_STRING);

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doParsing(
                             final String line, final LogDataHolder logData) {
        final boolean isStatgainsPresent = areaStatgainMatcher.reset(line).matches();

        // Area name
        final String areaName;
        if (isStatgainsPresent)
            areaName = line.substring(line.indexOf(UsefulPatterns.WHITE_SPACE) + 1,
                                      line.lastIndexOf(UsefulPatterns.SQUARE_BRACKET_OPEN) - 1);
        else
            areaName = line.substring(line.indexOf(UsefulPatterns.WHITE_SPACE) + 1);

        // Parse out the turncount string
        final String turnCounts = line.substring(line.indexOf(UsefulPatterns.SQUARE_BRACKET_OPEN) + 1,
                                                 line.indexOf(UsefulPatterns.SQUARE_BRACKET_CLOSE));

        // Depending on the turncount string format do further processing
        // and create the turn interval.
        final TurnInterval area;
        if (!turnCounts.contains(UsefulPatterns.MINUS)) {
            final int turnCount = Integer.parseInt(turnCounts);

            if (turnCount == 0 && areaName.equals(ASCENSION_START_STRING))
                area = new SimpleTurnInterval(areaName, turnCount, turnCount);
            else
                area = new SimpleTurnInterval(areaName, turnCount - 1, turnCount);
        } else {
            final int turnCountMin = Integer.parseInt(turnCounts.substring(0,
                                                                           turnCounts.indexOf(UsefulPatterns.MINUS)));
            final int turnCountMax = Integer.parseInt(turnCounts.substring(turnCounts.indexOf(UsefulPatterns.MINUS) + 1));

            area = new SimpleTurnInterval(areaName, turnCountMin - 1, turnCountMax);
        }

        // Check for area statgain string and add the statgains if it is
        // present.
        if (isStatgainsPresent) {
            final String statGains = line.substring(line.lastIndexOf(UsefulPatterns.SQUARE_BRACKET_OPEN) + 1,
                                                    line.lastIndexOf(UsefulPatterns.SQUARE_BRACKET_CLOSE));

            final int muscle = Integer.parseInt(statGains.substring(0,
                                                                    statGains.indexOf(UsefulPatterns.COMMA)));
            final int myst = Integer.parseInt(statGains.substring(statGains.indexOf(UsefulPatterns.COMMA) + 1,
                                                                  statGains.lastIndexOf(UsefulPatterns.COMMA)));
            final int moxie = Integer.parseInt(statGains.substring(statGains.lastIndexOf(UsefulPatterns.COMMA) + 1));

            area.setStatGain(new Statgain(muscle, myst, moxie));
        }

        // Add the turn interval to the turn rundown.
        logData.addTurnIntervalSpent(area);

        // Set the log creator if it isn't set yet.
        if (logData.getParsedLogCreator() == ParsedLogClass.NOT_DEFINED)
            // Check for specific lines currently only used by the internal
            // KolMafia log parser of the Ascension Log Visualizer.
            // This is not really good design, but it should be the easiest way
            // to find out the log creator. The header would also give it away,
            // but it isn't always put into logs posted on forums.
            if (area.getEndTurn() != 0 && !isStatgainsPresent)
                logData.setParsedLogCreator(ParsedLogClass.AFH_PARSER);
            else
                logData.setParsedLogCreator(ParsedLogClass.LOG_VISUALIZER);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean isCompatibleLine(
                                       final String line) {
        return turnsUsedMatcher.reset(line).matches();
    }

}
