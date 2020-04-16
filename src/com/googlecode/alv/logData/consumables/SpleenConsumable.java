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

package com.googlecode.alv.logData.consumables;

import com.googlecode.alv.parser.UsefulPatterns;
import com.googlecode.alv.util.Countable;

/**
 * A simple implementation of the Consumable class for items hitting spleen.
 * 
 * @see Consumable
 */
final class SpleenConsumable extends Consumable {
    private static final String CONSUMED_START_STRING = "Used";

    SpleenConsumable(
                     final String name, final int adventureGain, final int amountUsed) {
        super(name, adventureGain, amountUsed);
    }

    SpleenConsumable(
                     final String name, final int adventureGain, final int amountUsed,
                     final int turnNumberOfUsage) {
        super(name, adventureGain, amountUsed, turnNumberOfUsage);
    }

    /** {@inheritDoc} */
    @Override
    public ConsumableVersion getConsumableVersion() {
        return ConsumableVersion.SPLEEN;
    }

    /**
     * @return A deep copy of this object.
     * @see Countable
     */
    public SpleenConsumable newInstance() {
        final SpleenConsumable consumable = getTurnNumberOfUsage() < 0 ? new SpleenConsumable(getName(),
                                                                                              getAdventureGain(),
                                                                                              getAmount())
                                                                      : new SpleenConsumable(getName(),
                                                                                             getAdventureGain(),
                                                                                             getAmount(),
                                                                                             getTurnNumberOfUsage());

        consumable.setDayNumberOfUsage(getDayNumberOfUsage());
        consumable.setStatGain(getStatGain());

        return consumable;
    }

    @Override
    public String toString() {
        final StringBuilder str = new StringBuilder(50);

        str.append(CONSUMED_START_STRING);
        str.append(UsefulPatterns.WHITE_SPACE);
        str.append(getAmount());
        str.append(UsefulPatterns.WHITE_SPACE);
        str.append(getName());
        str.append(UsefulPatterns.WHITE_SPACE);

        if (getAdventureGain() > 0) {
            str.append(UsefulPatterns.ROUND_BRACKET_OPEN);
            str.append(getAdventureGain());
            str.append(UsefulPatterns.WHITE_SPACE);
            str.append(ADVENTURES_GAINED_STRING);
            str.append(UsefulPatterns.ROUND_BRACKET_CLOSE);
            str.append(UsefulPatterns.WHITE_SPACE);
        }

        str.append(getStatGain().toString());

        return str.toString();
    }
}
