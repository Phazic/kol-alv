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

package com.googlecode.alv.logData.logSummary;

import java.util.Collection;
import java.util.Collections;
import java.util.SortedSet;
import java.util.TreeSet;

import com.googlecode.alv.logData.Statgain;
import com.googlecode.alv.logData.consumables.Consumable;
import com.googlecode.alv.logData.turn.turnAction.DayChange;
import com.googlecode.alv.util.CountableSet;
import com.googlecode.alv.util.dataTables.DataTablesHandler;

/**
 * This class is a consumption data holder. It orders consumption by day and has
 * various summaries available.
 */
public final class ConsumptionSummary {
    private final SortedSet<ConsumptionDayStats> dayStatistics = new TreeSet<ConsumptionDayStats>();

    /**
     * Constructs a ConsumptionSummary instance.
     * 
     * @param consumables
     *            A collection containing all the consumables used during an
     *            ascension.
     * @param dayChanges
     *            A collection containing all day changes of an ascension.
     */
    ConsumptionSummary(
                       final Collection<Consumable> consumables,
                       final Collection<DayChange> dayChanges) {
        for (final DayChange dc : dayChanges) {
            final ConsumptionDayStats dayStats = new ConsumptionDayStats(dc.getDayNumber());
            for (final Consumable c : consumables)
                if (c.getDayNumberOfUsage() == dc.getDayNumber())
                    dayStats.addConsumable(c);

            dayStatistics.add(dayStats);
        }
    }

    /**
     * @return A read-only sorted set of all the consumption summaries by day.
     */
    public SortedSet<ConsumptionDayStats> getDayStatistics() {
        return Collections.unmodifiableSortedSet(dayStatistics);
    }

    /**
     * @return The total amount of turns gained from food.
     */
    public int getTotalTurnsFromFood() {
        int totalTurnsFromFood = 0;
        for (final ConsumptionDayStats cds : dayStatistics)
            totalTurnsFromFood += cds.getTotalTurnsFromFood();

        return totalTurnsFromFood;
    }

    /**
     * @return The total amount of turns gained from booze.
     */
    public int getTotalTurnsFromBooze() {
        int totalTurnsFromBooze = 0;
        for (final ConsumptionDayStats cds : dayStatistics)
            totalTurnsFromBooze += cds.getTotalTurnsFromBooze();

        return totalTurnsFromBooze;
    }

    /**
     * @return The total amount of turns gained from other consumables.
     */
    public int getTotalTurnsFromOther() {
        int totalTurnsFromOther = 0;
        for (final ConsumptionDayStats cds : dayStatistics)
            totalTurnsFromOther += cds.getTotalTurnsFromSpleen() + cds.getTotalTurnsFromOther();

        return totalTurnsFromOther;
    }

    /**
     * @return The total amount of substats from food collected during an
     *         ascension.
     */
    public Statgain getFoodConsumablesStatgains() {
        Statgain stats = Statgain.NO_STATS;
        for (final ConsumptionDayStats cds : dayStatistics)
            stats = stats.addStats(cds.getFoodConsumablesStatgains());

        return stats;
    }

    /**
     * @return The total amount of substats from booze collected during an
     *         ascension.
     */
    public Statgain getBoozeConsumablesStatgains() {
        Statgain stats = Statgain.NO_STATS;
        for (final ConsumptionDayStats cds : dayStatistics)
            stats = stats.addStats(cds.getBoozeConsumablesStatgains());

        return stats;
    }

    /**
     * @return The total amount of substats from other consumables collected
     *         during an ascension.
     */
    public Statgain getUsedConsumablesStatgains() {
        Statgain stats = Statgain.NO_STATS;
        for (final ConsumptionDayStats cds : dayStatistics)
            stats = stats.addStats(cds.getUsedConsumablesStatgains());

        return stats;
    }

    /**
     * @return The total amount of substats from all possible consumables
     *         collected during an ascension.
     */
    public Statgain getTotalConsumablesStatgains() {
        Statgain stats = Statgain.NO_STATS;
        for (final ConsumptionDayStats cds : dayStatistics)
            stats = stats.addStats(cds.getTotalConsumablesStatgains());

        return stats;
    }

    /**
     * A container class able to hold various consumption data of an ascension
     * day.
     */
    public static final class ConsumptionDayStats implements Comparable<ConsumptionDayStats> {
        private final int dayNumber;

        private final CountableSet<Consumable> consumablesUsed = new CountableSet<Consumable>();

        private int totalTurnsFromFood;

        private int totalTurnsFromBooze;

        private int totalTurnsFromSpleen;

        private int totalTurnsFromOther;

        private int totalFullnessHit;

        private int totalDrunkennessHit;

        private int totalSpleenHit;

        private Statgain foodConsumablesStatgains = Statgain.NO_STATS;

        private Statgain boozeConsumablesStatgains = Statgain.NO_STATS;

        private Statgain usedConsumablesStatgains = Statgain.NO_STATS;

        private Statgain totalConsumablesStatgains = Statgain.NO_STATS;

        private ConsumptionDayStats(
                                    final int dayNumber) {
            this.dayNumber = dayNumber;
        }

        private void addConsumable(
                                   final Consumable c) {
            consumablesUsed.addElement(c);
            totalConsumablesStatgains = totalConsumablesStatgains.addStats(c.getStatGain());
            switch (c.getConsumableVersion()) {
                case FOOD:
                    totalTurnsFromFood += c.getAdventureGain();
                    totalFullnessHit += DataTablesHandler.HANDLER.getFullnessHit(c.getName())
                                        * c.getAmount();
                    foodConsumablesStatgains = foodConsumablesStatgains.addStats(c.getStatGain());
                    break;
                case BOOZE:
                    totalTurnsFromBooze += c.getAdventureGain();
                    totalDrunkennessHit += DataTablesHandler.HANDLER.getDrunkennessHit(c.getName())
                                           * c.getAmount();
                    boozeConsumablesStatgains = boozeConsumablesStatgains.addStats(c.getStatGain());
                    break;
                case SPLEEN:
                    totalTurnsFromSpleen += c.getAdventureGain();
                    totalSpleenHit += DataTablesHandler.HANDLER.getSpleenHit(c.getName())
                                      * c.getAmount();
                    usedConsumablesStatgains = usedConsumablesStatgains.addStats(c.getStatGain());
                    break;
                default:
                    totalTurnsFromOther += c.getAdventureGain();
                    usedConsumablesStatgains = usedConsumablesStatgains.addStats(c.getStatGain());
            }
        }

        /**
         * @return The day number of this consumption summary.
         */
        public int getDayNumber() {
            return dayNumber;
        }

        /**
         * @return A list of all consumables used during the day.
         */
        public Collection<Consumable> getConsumablesUsed() {
            return consumablesUsed.getElements();
        }

        /**
         * @return The total amount of turns gained from food.
         */
        public int getTotalTurnsFromFood() {
            return totalTurnsFromFood;
        }

        /**
         * @return The total amount of turns gained from booze.
         */
        public int getTotalTurnsFromBooze() {
            return totalTurnsFromBooze;
        }

        /**
         * @return The total amount of turns gained from spleen consumables.
         */
        public int getTotalTurnsFromSpleen() {
            return totalTurnsFromSpleen;
        }

        /**
         * @return The total amount of turns gained from other consumables.
         */
        public int getTotalTurnsFromOther() {
            return totalTurnsFromOther;
        }

        /**
         * @return The total amount of fullness used during an ascension day.
         */
        public int getTotalFullnessHit() {
            return totalFullnessHit;
        }

        /**
         * @return The total amount of drunkenness used during an ascension day.
         */
        public int getTotalDrunkennessHit() {
            return totalDrunkennessHit;
        }

        /**
         * @return The total amount of spleen used during an ascension day.
         */
        public int getTotalSpleenHit() {
            return totalSpleenHit;
        }

        /**
         * @return The total amount of substats from food collected during an
         *         ascension day.
         */
        public Statgain getFoodConsumablesStatgains() {
            return foodConsumablesStatgains;
        }

        /**
         * @return The total amount of substats from booze collected during an
         *         ascension day.
         */
        public Statgain getBoozeConsumablesStatgains() {
            return boozeConsumablesStatgains;
        }

        /**
         * @return The total amount of substats from other consumables collected
         *         during an ascension day.
         */
        public Statgain getUsedConsumablesStatgains() {
            return usedConsumablesStatgains;
        }

        /**
         * @return The total amount of substats from all possible consumables
         *         collected during an ascension day.
         */
        public Statgain getTotalConsumablesStatgains() {
            return totalConsumablesStatgains;
        }

        /**
         * Compares this ConsumptionDayStats instances day number with that of
         * the given ConsumptionDayStats instance.
         * 
         * @see Comparable
         */
        public int compareTo(
                             final ConsumptionDayStats o) {
            return dayNumber - o.getDayNumber();
        }
    }
}
