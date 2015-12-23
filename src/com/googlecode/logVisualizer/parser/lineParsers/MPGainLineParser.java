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
import com.googlecode.logVisualizer.logData.MPGain;
import com.googlecode.logVisualizer.logData.turn.Turn;
import com.googlecode.logVisualizer.parser.UsefulPatterns;

/**
 * A parser for the mp gain notation in mafia logs.
 * <p>
 * {@code You gain _amount_ Mana/Mojo/Muscularity Points}
 */
public final class MPGainLineParser extends AbstractLineParser {
    private static final String LOSE_STRING = "You lose";

    private static final int GAIN_START_STRING_LENGTH = 9;

    private final Matcher gainLoseMatcher = UsefulPatterns.GAIN_LOSE.matcher(UsefulPatterns.EMPTY_STRING);

    private final MPGainType mpGainType;

    /**
     * @param type
     *            The mp gain type which decides to which kind of mp gain all
     *            parsed mp gains from this line parser will be added to.
     */
    public MPGainLineParser(
                            final MPGainType type) {
        mpGainType = type;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doParsing(
                             final String line, final LogDataHolder logData) {
        int substrLength = GAIN_START_STRING_LENGTH;

        if (line.startsWith(UsefulPatterns.AFTER_BATTLE_STRING)) {
            substrLength += UsefulPatterns.AFTER_BATTLE_STRING.length();
        }

        final String informationPart = line.substring(substrLength);

        final int whiteSpaceIndex = informationPart.indexOf(UsefulPatterns.WHITE_SPACE);

        final String amountString = informationPart.substring(0, whiteSpaceIndex);

        // MP gains higher than the integer limit should not happen and will be
        // ignored.
        final int amount;
        try {
            amount = Integer.parseInt(amountString.replace(UsefulPatterns.COMMA,
                                                           UsefulPatterns.EMPTY_STRING));
        } catch (final NumberFormatException e) {
            e.printStackTrace();
            return;
        }

        final Turn lastInterval = logData.getLastTurnSpent();
        switch (mpGainType) {
            case ENCOUNTER:
                if (lastInterval.getAreaName().equals("Rest in your dwelling"))
                    lastInterval.addMPGain(new MPGain(0, 0, amount, 0, 0));
                else if (lastInterval.getAreaName().equals(
                        "Rest in your bed in the Chateau"))
                    lastInterval.addMPGain(new MPGain(0, 0, amount, 0, 0));
                else
                    lastInterval.addMPGain(new MPGain(amount, 0, 0, 0, 0));
                break;
            case NOT_ENCOUNTER:
                lastInterval.addMPGain(new MPGain(0, 0, 0, amount, 0));
                break;
            case CONSUMABLE:
                lastInterval.addMPGain(new MPGain(0, 0, 0, 0, amount));
                break;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean isCompatibleLine(
                                       final String line) {
        if (gainLoseMatcher.reset(line).matches() && !line.startsWith(LOSE_STRING))
            for (final String s : UsefulPatterns.MP_NAMES)
                if (line.endsWith(s))
                    return true;

        return false;
    }

    public static enum MPGainType {
        ENCOUNTER, NOT_ENCOUNTER, CONSUMABLE;
    }
}
