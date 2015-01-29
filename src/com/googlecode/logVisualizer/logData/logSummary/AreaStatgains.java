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

package com.googlecode.logVisualizer.logData.logSummary;

import java.util.*;

import com.googlecode.logVisualizer.logData.Statgain;
import com.googlecode.logVisualizer.logData.consumables.Consumable;
import com.googlecode.logVisualizer.logData.turn.TurnInterval;
import com.googlecode.logVisualizer.util.Lists;
import com.googlecode.logVisualizer.util.Maps;
import com.googlecode.logVisualizer.util.Pair;
import com.googlecode.logVisualizer.util.Sets;

/**
 * This class gives the tools to save and compare area statgains.
 */
public final class AreaStatgains {
    private static final String TAB = "\t";

    private final Pair<String, Statgain> areaStatgainPair;

    /**
     * @param areaName
     *            The area name to set.
     * @param areaStatgain
     *            The area statgain to set.
     * @throws NullPointerException
     *             if areaName is {@code null}; if areaStatgain is {@code null}
     */
    private AreaStatgains(
                          final String areaName, final Statgain areaStatgain) {
        areaStatgainPair = Pair.of(areaName, areaStatgain);
    }

    /**
     * @return The area name.
     */
    public String getAreaName() {
        return areaStatgainPair.getVar1();
    }

    /**
     * @return The area statgain.
     */
    public Statgain getStatgain() {
        return areaStatgainPair.getVar2();
    }

    @Override
    public String toString() {
        final StringBuilder str = new StringBuilder(50);

        str.append(areaStatgainPair.getVar1());
        str.append(TAB);
        str.append(areaStatgainPair.getVar2().mus);
        str.append(TAB);
        str.append(areaStatgainPair.getVar2().myst);
        str.append(TAB);
        str.append(areaStatgainPair.getVar2().mox);

        return str.toString();
    }

    /**
     * This method creates and returns a sorted area statgain list from the
     * given turn rundown. Note that this list does also contain stat gains from
     * consumables as one of the list elements.
     * 
     * @param turns
     *            The turn rundown.
     * @param comparator
     *            The comparator used to sort the returned list.
     * @return A sorted list of area statgains from the given turn rundown.
     */
    public static List<AreaStatgains> getSortedAreaStatgains(
                                                             final Collection<TurnInterval> turns,
                                                             final Comparator<AreaStatgains> comparator) {
        final int initialHashCapacity = (int) (turns.size() * 0.75) + 1;
        final Set<String> areas = Sets.newHashSet(initialHashCapacity);
        final Map<String, Statgain> areaStatgains = Maps.newHashMap(initialHashCapacity);
        Statgain consumablesStatgain = Statgain.NO_STATS;

        // Count the statgains.
        for (final TurnInterval ti : turns) {
            final Statgain previousStats;
            if (areas.contains(ti.getAreaName()))
                previousStats = areaStatgains.get(ti.getAreaName());
            else {
                previousStats = Statgain.NO_STATS;
                areas.add(ti.getAreaName());
            }

            areaStatgains.put(ti.getAreaName(), previousStats.addStats(ti.getStatGain()));

            // Add consumable statgains.
            for (final Consumable c : ti.getConsumablesUsed())
                consumablesStatgain = consumablesStatgain.addStats(c.getStatGain());
        }

        // Create area statgain list.
        final List<AreaStatgains> areaStatgainsList = Lists.newArrayList(areas.size() + 1);
        for (final String s : areas)
            areaStatgainsList.add(new AreaStatgains(s, areaStatgains.get(s)));

        // Add consumable statgains as its own area.
        areaStatgainsList.add(new AreaStatgains("From consumables", consumablesStatgain));

        // Sort the area statgains and return them.
        return Lists.sort(areaStatgainsList, comparator);
    }
}
