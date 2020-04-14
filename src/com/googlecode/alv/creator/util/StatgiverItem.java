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

package com.googlecode.alv.creator.util;

import java.util.Locale;

import com.googlecode.alv.logData.LogDataHolder.StatClass;
import com.googlecode.alv.parser.UsefulPatterns;
import com.googlecode.alv.util.dataTables.ExtraStats;

/**
 * Helper class used to help handle the +STAT BREAKDOWN summary, namely counting
 * the stats gained from specific items.
 */
final public class StatgiverItem {
    private static final String TAB = "\t";

    private final String itemName;

    private final String itemNameLower;

    private final double perTurnStatgain;

    private double lvl1Stats;

    private double lvl2Stats;

    private double lvl3Stats;

    private double lvl4Stats;

    private double lvl5Stats;

    private double lvl6Stats;

    private double lvl7Stats;

    private double lvl8Stats;

    private double lvl9Stats;

    private double lvl10Stats;

    private double lvl11Stats;

    private double lvl12Stats;

    public StatgiverItem(final String itemName, 
                         final ExtraStats perTurnStatgain, 
                         final StatClass mainStat) 
    {
        this.itemName = itemName;
        itemNameLower = itemName.toLowerCase(Locale.ENGLISH);

        double stats = perTurnStatgain.generalGain;
        switch (mainStat) {
            case MUSCLE:
                stats += perTurnStatgain.musGain;
                break;
            case MYSTICALITY:
                stats += perTurnStatgain.mystGain;
                break;
            case MOXIE:
                stats += perTurnStatgain.moxGain;
                break;
        }
        this.perTurnStatgain = stats;
    }

    public String getItemName() 
    {
        // The ALV internally saves item names only in lower case.
        return itemNameLower;
    }

    public void incrementLvlStatgain(final int levelNumber,
                                     final int numberOfIncrements) 
    {
        for (int i = 0; i < numberOfIncrements; i++)
            switch (levelNumber) {
                case 1:
                    lvl1Stats += perTurnStatgain;
                    break;
                case 2:
                    lvl2Stats += perTurnStatgain;
                    break;
                case 3:
                    lvl3Stats += perTurnStatgain;
                    break;
                case 4:
                    lvl4Stats += perTurnStatgain;
                    break;
                case 5:
                    lvl5Stats += perTurnStatgain;
                    break;
                case 6:
                    lvl6Stats += perTurnStatgain;
                    break;
                case 7:
                    lvl7Stats += perTurnStatgain;
                    break;
                case 8:
                    lvl8Stats += perTurnStatgain;
                    break;
                case 9:
                    lvl9Stats += perTurnStatgain;
                    break;
                case 10:
                    lvl10Stats += perTurnStatgain;
                    break;
                case 11:
                    lvl11Stats += perTurnStatgain;
                    break;
                case 12:
                    lvl12Stats += perTurnStatgain;
                    break;
                default:
                    break;
            }
    }

    public int getLvl1Stats() {
        return (int) lvl1Stats;
    }

    public int getLvl2Stats() {
        return (int) lvl2Stats;
    }

    public int getLvl3Stats() {
        return (int) lvl3Stats;
    }

    public int getLvl4Stats() {
        return (int) lvl4Stats;
    }

    public int getLvl5Stats() {
        return (int) lvl5Stats;
    }

    public int getLvl6Stats() {
        return (int) lvl6Stats;
    }

    public int getLvl7Stats() {
        return (int) lvl7Stats;
    }

    public int getLvl8Stats() {
        return (int) lvl8Stats;
    }

    public int getLvl9Stats() {
        return (int) lvl9Stats;
    }

    public int getLvl10Stats() {
        return (int) lvl10Stats;
    }

    public int getLvl11Stats() {
        return (int) lvl11Stats;
    }

    public int getLvl12Stats() {
        return (int) lvl12Stats;
    }

    public int getTotalStats() {
        return (int) (lvl1Stats + lvl2Stats + lvl3Stats + lvl4Stats + lvl5Stats + lvl6Stats
                      + lvl7Stats + lvl8Stats + lvl9Stats + lvl10Stats + lvl11Stats + lvl12Stats);
    }

    @Override
    public String toString() {
        final StringBuilder str = new StringBuilder(200);
        str.append(itemName);
        str.append(UsefulPatterns.COLON);
        for (int i = itemName.length(); i < 55; i++)
            str.append(UsefulPatterns.WHITE_SPACE);

        str.append(TAB);
        str.append(getLvl1Stats());
        str.append(TAB);
        str.append(getLvl2Stats());
        str.append(TAB);
        str.append(getLvl3Stats());
        str.append(TAB);
        str.append(getLvl4Stats());
        str.append(TAB);
        str.append(getLvl5Stats());
        str.append(TAB);
        str.append(getLvl6Stats());
        str.append(TAB);
        str.append(getLvl7Stats());
        str.append(TAB);
        str.append(getLvl8Stats());
        str.append(TAB);
        str.append(getLvl9Stats());
        str.append(TAB);
        str.append(getLvl10Stats());
        str.append(TAB);
        str.append(getLvl11Stats());
        str.append(TAB);
        str.append(getLvl12Stats());
        str.append(TAB);
        str.append(getTotalStats());

        return str.toString();
    }
}
