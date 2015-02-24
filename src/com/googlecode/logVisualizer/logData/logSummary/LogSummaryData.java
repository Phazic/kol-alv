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

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.googlecode.logVisualizer.logData.*;
import com.googlecode.logVisualizer.logData.consumables.Consumable;
import com.googlecode.logVisualizer.logData.consumables.Consumable.ConsumableVersion;
import com.googlecode.logVisualizer.logData.turn.Encounter;
import com.googlecode.logVisualizer.logData.turn.FreeRunaways;
import com.googlecode.logVisualizer.util.DataNumberPair;
import com.googlecode.logVisualizer.util.Lists;

/**
 * This is a data container class for various summaries on ascension logs. This
 * class merely holds the data and accessing methods to it, the calculation of
 * these summaries is being done in the {@link SummaryDataCalculator} class.
 * <p>
 * Note that depending on how detailed the ascension log (or in this case the
 * turn rundown log) is, some of these data summary calculations might not
 * capture all the possible data or none at all. For these cases this class
 * makes it possible to set the summaries manually.
 * <p>
 * All methods in this class throw a {@link NullPointerException} if a null
 * object reference is passed in any parameter.
 */
public class LogSummaryData {
    private final List<Consumable> consumablesUsed;

    private final List<Item> droppedItems;

    private final List<Skill> skillsCast;

    private final List<CombatItem> combatItemsUsed;
    
    private final List<AreaStatgains> areasStatgains;

    private final List<DataNumberPair<String>> turnsPerArea;

    private final List<LevelData> levels;

    private List<DataNumberPair<String>> familiarUsage;

    private List<DataNumberPair<String>> trackedCombatItemUses;
    
    private List<DataNumberPair<String>> disintegratedCombats;
    
    private List<DataNumberPair<String>> banishedCombats;

    private List<DataNumberPair<String>> semirares;

    private List<DataNumberPair<String>> badmoonAdventures;

    private final List<DataNumberPair<String>> romanticArrowUsages;

    private final List<DataNumberPair<String>> wanderingAdventures;

    private final List<DataNumberPair<String>> hipsterCombats;

    private final List<Encounter> freeRunawayCombats;

    private final FreeRunaways freeRunaways;

    private final ConsumptionSummary consumptionSummary;

    private final Goatlet goatlet;

    private final InexplicableDoor nesRealm;

    private final QuestTurncounts questTurncounts;

    private Statgain totalStatgains;

    private Statgain combatsStatgains;

    private Statgain noncombatsStatgains;

    private Statgain othersStatgains;

    private final MeatSummary meatSummary;

    private final MPGainSummary mpGainSummary;

    private MPGain totalMPGains;

    private int totalAmountSkillCasts;

    private int totalMPUsed;

    private int totalMeatGain;

    private int totalMeatSpent;

    private int totalTurnsFromRollover;

    private int totalTurnsCombat;

    private int totalTurnsNoncombat;

    private int totalTurnsOther;

    /**
     * Constructs an ascension log summary. A summaries will be calculated as
     * good as possible from the given data.
     * 
     * @param logData
     *            The ascension log.
     */
    public LogSummaryData(
                          final LogDataHolder logData) {
        if (logData == null)
            throw new NullPointerException("Log data holder must not be null.");

        final SummaryDataCalculator data = new SummaryDataCalculator(logData);

        areasStatgains = AreaStatgains.getSortedAreaStatgains(logData.getTurnIntervalsSpent(),
                                                              new Comparator<AreaStatgains>() {

                                                                  public int compare(
                                                                                     final AreaStatgains o1,
                                                                                     final AreaStatgains o2) {
                                                                      return o2.getStatgain()
                                                                               .getTotalStatgain()
                                                                             - o1.getStatgain()
                                                                                 .getTotalStatgain();
                                                                  }
                                                              });

        turnsPerArea = data.getTurnsPerArea();
        Collections.sort(turnsPerArea, new Comparator<DataNumberPair<String>>() {
            public int compare(
                               final DataNumberPair<String> o1, final DataNumberPair<String> o2) {
                // Reverse order, so that the list starts with the highest
                // element. Also, in case of a tie, sort alphabetically.
                int diff = o2.compareTo(o1);
                if (diff == 0)
                    diff = o1.getData().compareToIgnoreCase(o2.getData());
                return diff;
            }
        });

        consumablesUsed = Lists.newArrayList(data.getConsumablesUsed());
        Collections.sort(consumablesUsed, new Comparator<Consumable>() {

            public int compare(
                               final Consumable o1, final Consumable o2) {
                // Reverse order, so that the list starts with the highest
                // element.
                return o2.compareTo(o1);
            }
        });

        droppedItems = Lists.newArrayList(data.getDroppedItems());
        Collections.sort(droppedItems, new Comparator<Item>() {

            public int compare(
                               final Item o1, final Item o2) {
                // Reverse order, so that the list starts with the highest
                // element.
                return o2.getAmount() - o1.getAmount();
            }
        });

        combatItemsUsed = Lists.newArrayList(data.getCombatItemsUsed());
        Collections.sort( combatItemsUsed, new Comparator<CombatItem>() {
        	public int compare(final CombatItem o1, final CombatItem o2) {
                // Reverse order, so that the list starts with the highest
                // element.
        		return o2.compareTo( o1 );
        	}
        });
        
        skillsCast = Lists.newArrayList(data.getSkillsCast());
        Collections.sort(skillsCast, new Comparator<Skill>() {

            public int compare(
                               final Skill o1, final Skill o2) {
                // Reverse order, so that the list starts with the highest
                // element.
                return o2.compareTo(o1);
            }
        });

        familiarUsage = data.getFamiliarUsage();
        Collections.sort(familiarUsage, new Comparator<DataNumberPair<?>>() {

            public int compare(
                               final DataNumberPair<?> o1, final DataNumberPair<?> o2) {
                // Reverse order, so that the list starts with the highest
                // element.
                return o2.compareTo(o1);
            }
        });

        levels = data.getLevelData();
        disintegratedCombats = data.getDisintegratedCombats();
        banishedCombats = data.getBanishedCombats();//Bombar: Add support for banished combats
        trackedCombatItemUses = data.getTrackedCombatItemUses();//Bombar: Add support for combat Item usage
        semirares = data.getSemirares();
        badmoonAdventures = data.getBadmoonAdventures();
        romanticArrowUsages = data.getRomanticArrowUsages();
        wanderingAdventures = data.getWanderingAdventures();
        hipsterCombats = data.getHipsterCombats();
        freeRunawayCombats = data.getFreeRunawaysCombats();
        freeRunaways = data.getFreeRunaways();
        consumptionSummary = data.getConsumptionSummary();
        goatlet = data.getGoatlet();
        nesRealm = data.get8BitRealm();
        questTurncounts = data.getQuestTurncounts();
        totalStatgains = data.getTotalStatgains();
        combatsStatgains = data.getCombatsStatgains();
        noncombatsStatgains = data.getNoncombatsStatgains();
        othersStatgains = data.getOthersStatgains();
        meatSummary = data.getMeatSummary();
        mpGainSummary = data.getMPGainSummary();
        totalMPGains = data.getTotalMPGains();
        totalAmountSkillCasts = data.getTotalAmountSkillCasts();
        totalMPUsed = data.getTotalMPUsed();
        totalMeatGain = data.getTotalMeatGain();
        totalMeatSpent = data.getTotalMeatSpent();
        totalTurnsFromRollover = data.getTotalTurnsFromRollover();
        totalTurnsCombat = data.getTotalTurnsCombat();
        totalTurnsNoncombat = data.getTotalTurnsNoncombat();
        totalTurnsOther = data.getTotalTurnsOther();
    }

    /**
     * @return A sorted list of areas and the turns spent in them. This list
     *         starts with the area that has the highest amount turns.
     */
    public List<DataNumberPair<String>> getTurnsPerArea() {
        return turnsPerArea;
    }

    /**
     * @return A sorted list of all consumables used. This list starts with the
     *         consumable that was used the most.
     */
    public List<Consumable> getAllConsumablesUsed() {
        return consumablesUsed;
    }

    /**
     * @return A sorted list of all food consumables used. This list starts with
     *         the consumable that was used the most.
     */
    public List<Consumable> getFoodConsumablesUsed() {
        final List<Consumable> consumables = Lists.newArrayList(consumablesUsed.size());

        for (final Consumable c : consumablesUsed)
            if (c.getConsumableVersion() == ConsumableVersion.FOOD)
                consumables.add(c);

        return consumables;
    }

    /**
     * @return A sorted list of all booze consumables used. This list starts
     *         with the consumable that was used the most.
     */
    public List<Consumable> getBoozeConsumablesUsed() {
        final List<Consumable> consumables = Lists.newArrayList(consumablesUsed.size());

        for (final Consumable c : consumablesUsed)
            if (c.getConsumableVersion() == ConsumableVersion.BOOZE)
                consumables.add(c);

        return consumables;
    }

    /**
     * @return A sorted list of all spleen consumables used. This list starts
     *         with the consumable that was used the most.
     */
    public List<Consumable> getSpleenConsumablesUsed() {
        final List<Consumable> consumables = Lists.newArrayList(consumablesUsed.size());

        for (final Consumable c : consumablesUsed)
            if (c.getConsumableVersion() == ConsumableVersion.SPLEEN)
                consumables.add(c);

        return consumables;
    }

    /**
     * @return A sorted list of all other consumables used. This list starts
     *         with the consumable that was used the most.
     */
    public List<Consumable> getOtherConsumablesUsed() {
        final List<Consumable> consumables = Lists.newArrayList(consumablesUsed.size());

        for (final Consumable c : consumablesUsed)
            if (c.getConsumableVersion() == ConsumableVersion.OTHER)
                consumables.add(c);

        return consumables;
    }

    /**
     * @return A sorted list of all items dropped during this ascension. This
     *         list starts with the item that dropped the most.
     */
    public List<Item> getDroppedItems() {
        return droppedItems;
    }

    /**
     * 
     * @return A sorted list of all combat items used during this ascension, the list starts with skill that was used most.
     */
    public List<CombatItem> getCombatItemsUsed() {
    	return combatItemsUsed;
    }
    
    /**
     * @return A sorted list of all skills cast during this ascension. This list
     *         starts with the skill that was cast the most.
     */
    public List<Skill> getSkillsCast() {
        return skillsCast;
    }

    /**
     * @return A sorted list of all area statgains during this ascension. This
     *         list starts with the area statgain that had the highest total
     *         statgains.
     */
    public List<AreaStatgains> getAreasStatgains() {
        return areasStatgains;
    }

    /**
     * @return A read-only list of all levels. No guarantees are made concerning
     *         it's order.
     */
    public List<LevelData> getLevelData() {
        return Collections.unmodifiableList(levels);
    }

    /**
     * @param familiarUsage
     *            All used familiars and how often they were used to set.
     */
    public void setFamiliarUsage(
                                 final List<DataNumberPair<String>> familiarUsage) {
        if (familiarUsage == null)
            throw new NullPointerException("Familiar usage list must not be null.");

        this.familiarUsage = familiarUsage;
    }

    /**
     * @return A sorted list of all used familiars and how often they were used.
     *         This list starts with the familiar that was used the most.
     */
    public List<DataNumberPair<String>> getFamiliarUsage() {
        return familiarUsage;
    }

    public void setTrackedCombatItemUses(final List<DataNumberPair<String>> trackedCombatItemUses) {
    	this.trackedCombatItemUses = trackedCombatItemUses;
    }
    public List<DataNumberPair<String>> getTrackedCombatItemUses() {
    	return this.trackedCombatItemUses;
    }
    
    /**
     * @param banishedCombats
     *            All banished combats to set.
     */
    public void setBanishedCombats(
                                        final List<DataNumberPair<String>> banishedCombats) {
        if (banishedCombats == null)
            throw new NullPointerException("banishedCombats combats list must not be null.");

        this.banishedCombats = banishedCombats;
    }

    /**
     * @return A sorted list of all disintegrated combats. This list starts with
     *         the earliest disintegrated combat.
     */
    public List<DataNumberPair<String>> getBanishedCombats() {
        return banishedCombats;
    }
    
    /**
     * @param disintegratedCombats
     *            All disintegrated combats to set.
     */
    public void setDisintegratedCombats(
                                        final List<DataNumberPair<String>> disintegratedCombats) {
        if (disintegratedCombats == null)
            throw new NullPointerException("Disintegrated combats list must not be null.");

        this.disintegratedCombats = disintegratedCombats;
    }

    /**
     * @return A sorted list of all disintegrated combats. This list starts with
     *         the earliest disintegrated combat.
     */
    public List<DataNumberPair<String>> getDisintegratedCombats() {
        return disintegratedCombats;
    }

    /**
     * @param semirares
     *            All encountered semirares to set.
     */
    public void setSemirares(
                             final List<DataNumberPair<String>> semirares) {
        if (semirares == null)
            throw new NullPointerException("Semirares list must not be null.");

        this.semirares = semirares;
    }

    /**
     * @return A sorted list of all encountered semirares. This list starts with
     *         the semirare that was encountered the earliest.
     */
    public List<DataNumberPair<String>> getSemirares() {
        return semirares;
    }

    /**
     * @param badmoonAdventures
     *            All encountered Bad Moon adventures to set.
     */
    public void setBadmoonAdventures(
                                     final List<DataNumberPair<String>> badmoonAdventures) {
        if (badmoonAdventures == null)
            throw new NullPointerException("Bad Moon adventures list must not be null.");

        this.badmoonAdventures = badmoonAdventures;
    }

    /**
     * @return A list of all Bad Moon adventures.
     */
    public List<DataNumberPair<String>> getBadmoonAdventures() {
        return badmoonAdventures;
    }

    /**
     * @return A list of all romantic arrow usages.
     */
    public List<DataNumberPair<String>> getRomanticArrowUsages() {
        return romanticArrowUsages;
    }

    /**
     * @return A list of all wandering adventures.
     */
    public List<DataNumberPair<String>> getWanderingAdventures() {
        return wanderingAdventures;
    }

    /**
     * @return A list of all Hipster combats.
     */
    public List<DataNumberPair<String>> getHipsterCombats() {
        return hipsterCombats;
    }

    /**
     * @return A list of all combats on which free runaways were successfully
     *         used.
     */
    public List<Encounter> getFreeRunawaysCombats() {
        return freeRunawayCombats;
    }

    /**
     * @return A summary on consumables used during the ascension.
     */
    public ConsumptionSummary getConsumptionSummary() {
        return consumptionSummary;
    }

    /**
     * @return The free runaways over the whole ascension.
     */
    public FreeRunaways getFreeRunaways() {
        return freeRunaways;
    }

    /**
     * @return The RNG data of the Goatlet.
     */
    public Goatlet getGoatlet() {
        return goatlet;
    }

    /**
     * @return The RNG data of the 8-Bit Realm.
     */
    public InexplicableDoor get8BitRealm() {
        return nesRealm;
    }

    /**
     * @return The quest turncounts.
     */
    public QuestTurncounts getQuestTurncounts() {
        return questTurncounts;
    }

    /**
     * @param mpGains
     *            The total amount of mp gains to set.
     */
    public void setTotalMPGains(
                                final MPGain mpGains) {
        totalMPGains = mpGains;
    }

    /**
     * @return The total mp gains collected during this ascension.
     */
    public MPGain getTotalMPGains() {
        return totalMPGains;
    }

    /**
     * @return The mp gains per level summary.
     */
    public MPGainSummary getMPGainSummary() {
        return mpGainSummary;
    }

    /**
     * @return The meat per level summary.
     */
    public MeatSummary getMeatSummary() {
        return meatSummary;
    }

    /**
     * @param stats
     *            The total amount of statgains to set.
     */
    public void setTotalStatgains(
                                  final Statgain stats) {
        totalStatgains = stats;
    }

    /**
     * @return The total amount of substats collected during this ascension.
     */
    public Statgain getTotalStatgains() {
        return totalStatgains;
    }

    /**
     * @param stats
     *            The amount of combat statgains to set.
     */
    public void setCombatsStatgains(
                                    final Statgain stats) {
        combatsStatgains = stats;
    }

    /**
     * @return The total amount of substats from combats collected during this
     *         ascension.
     */
    public Statgain getCombatsStatgains() {
        return combatsStatgains;
    }

    /**
     * @param stats
     *            The amount of noncombat statgains to set.
     */
    public void setNoncombatsStatgains(
                                       final Statgain stats) {
        noncombatsStatgains = stats;
    }

    /**
     * @return The total amount of substats from noncombats collected during
     *         this ascension.
     */
    public Statgain getNoncombatsStatgains() {
        return noncombatsStatgains;
    }

    /**
     * @param stats
     *            The amount of other statgains to set.
     */
    public void setOthersStatgains(
                                   final Statgain stats) {
        othersStatgains = stats;
    }

    /**
     * @return The total amount of substats from other encounters collected
     *         during this ascension.
     */
    public Statgain getOthersStatgains() {
        return othersStatgains;
    }

    /**
     * @return The total amount of substats from food collected during this
     *         ascension.
     */
    public Statgain getFoodConsumablesStatgains() {
        return consumptionSummary.getFoodConsumablesStatgains();
    }

    /**
     * @return The total amount of substats from booze collected during this
     *         ascension.
     */
    public Statgain getBoozeConsumablesStatgains() {
        return consumptionSummary.getBoozeConsumablesStatgains();
    }

    /**
     * @return The total amount of substats from used consumables collected
     *         during this ascension.
     */
    public Statgain getUsedConsumablesStatgains() {
        return consumptionSummary.getUsedConsumablesStatgains();
    }

    /**
     * @param totalAmountSkillsCast
     *            The total amount of skill casts to set.
     * @throws IllegalArgumentException
     *             if totalAmountSkillCasts is below 0
     */
    public void setTotalAmountSkillCasts(
                                         final int totalAmountSkillCasts) {
        if (totalAmountSkillCasts < 0)
            throw new IllegalArgumentException("Amount must not be below 0.");

        this.totalAmountSkillCasts = totalAmountSkillCasts;
    }

    /**
     * @return The total amount of skill casts.
     */
    public int getTotalAmountSkillCasts() {
        return totalAmountSkillCasts;
    }

    /**
     * @param totalAmountMP
     *            The total amount of MP spent on skills to set.
     * @throws IllegalArgumentException
     *             if totalMPUsed is below 0
     */
    public void setTotalMPUsed(
                               final int totalMPUsed) {
        if (totalMPUsed < 0)
            throw new IllegalArgumentException("MP used must not be below 0.");

        this.totalMPUsed = totalMPUsed;
    }

    /**
     * @return The total amount of MP spent on skills.
     */
    public int getTotalMPUsed() {
        return totalMPUsed;
    }

    /**
     * @param totalMeatGain
     *            The total amount of meat gathered to set.
     * @throws IllegalArgumentException
     *             if totalMeatGain is below 0
     */
    public void setTotalMeatGain(
                                 final int totalMeatGain) {
        if (totalMeatGain < 0)
            throw new IllegalArgumentException("Meat gain must not be below 0.");

        this.totalMeatGain = totalMeatGain;
    }

    /**
     * @return The total amount of meat gathered.
     */
    public int getTotalMeatGain() {
        return totalMeatGain;
    }

    /**
     * @param totalMeatGain
     *            The total amount of meat spent to set.
     * @throws IllegalArgumentException
     *             if totalMeatGain is below 0
     */
    public void setTotalMeatSpent(
                                  final int totalMeatSpent) {
        if (totalMeatSpent < 0)
            throw new IllegalArgumentException("Meat spent must not be below 0.");

        this.totalMeatSpent = totalMeatSpent;
    }

    /**
     * @return The total amount of meat spent.
     */
    public int getTotalMeatSpent() {
        return totalMeatSpent;
    }

    /**
     * @return The total amount of turns gained from food.
     */
    public int getTotalTurnsFromFood() {
        return consumptionSummary.getTotalTurnsFromFood();
    }

    /**
     * @return The total amount of turns gained from booze.
     */
    public int getTotalTurnsFromBooze() {
        return consumptionSummary.getTotalTurnsFromBooze();
    }

    /**
     * @return The total amount of turns gained from spleen and other sources.
     */
    public int getTotalTurnsFromOther() {
        return consumptionSummary.getTotalTurnsFromOther();
    }

    /**
     * @param totalTurnsFromRollover
     *            The total amount of turns gained from rollover to set.
     * @throws IllegalArgumentException
     *             if totalTurnsFromRollover is below 0
     */
    public void setTotalTurnsFromRollover(
                                          final int totalTurnsFromRollover) {
        if (totalTurnsFromRollover < 0)
            throw new IllegalArgumentException("Turn gain must not be below 0.");

        this.totalTurnsFromRollover = totalTurnsFromRollover;
    }

    /**
     * @return The total amount of turns gained from rollover.
     */
    public int getTotalTurnsFromRollover() {
        return totalTurnsFromRollover;
    }

    /**
     * @param totalTurnsCombat
     *            The total amount of combat turns to set.
     * @throws IllegalArgumentException
     *             if totalTurnsCombat is below 0
     */
    public void setTotalTurnsCombat(
                                    final int totalTurnsCombat) {
        if (totalTurnsCombat < 0)
            throw new IllegalArgumentException("Turn spent must not be below 0.");

        this.totalTurnsCombat = totalTurnsCombat;
    }

    /**
     * @return The total amount of combat turns.
     */
    public int getTotalTurnsCombat() {
        return totalTurnsCombat;
    }

    /**
     * @param totalTurnsNoncombat
     *            The total amount of noncombat turns to set.
     * @throws IllegalArgumentException
     *             if totalTurnsNoncombat is below 0
     */
    public void setTotalTurnsNoncombat(
                                       final int totalTurnsNoncombat) {
        if (totalTurnsNoncombat < 0)
            throw new IllegalArgumentException("Turn spent must not be below 0.");

        this.totalTurnsNoncombat = totalTurnsNoncombat;
    }

    /**
     * @return The total amount of noncombat turns.
     */
    public int getTotalTurnsNoncombat() {
        return totalTurnsNoncombat;
    }

    /**
     * @param totalTurnsOther
     *            The total amount of other (smithing, mixing, cooking, etc.)
     *            turns to set.
     * @throws IllegalArgumentException
     *             if totalTurnsOther is below 0
     */
    public void setTotalTurnsOther(
                                   final int totalTurnsOther) {
        if (totalTurnsOther < 0)
            throw new IllegalArgumentException("Turn spent must not be below 0.");

        this.totalTurnsOther = totalTurnsOther;
    }

    /**
     * @return The total amount of other (smithing, mixing, cooking, etc.)
     *         turns.
     */
    public int getTotalTurnsOther() {
        return totalTurnsOther;
    }
}
