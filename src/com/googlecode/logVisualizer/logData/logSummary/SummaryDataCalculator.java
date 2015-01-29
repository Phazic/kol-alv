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

import com.googlecode.logVisualizer.logData.*;
import com.googlecode.logVisualizer.logData.LogDataHolder.CharacterClass;
import com.googlecode.logVisualizer.logData.consumables.Consumable;
import com.googlecode.logVisualizer.logData.turn.*;
import com.googlecode.logVisualizer.logData.turn.turnAction.PlayerSnapshot;
import com.googlecode.logVisualizer.util.*;
import com.googlecode.logVisualizer.util.dataTables.DataTablesHandler;

/**
 * A calculator for various summaries of an ascension log. This class makes use
 * of pretty much all features of the {@link OldTurnInterval} class, thus if not
 * all possibilities of the {@link OldTurnInterval} class are used, because for
 * example the source data from which the turn interval was created didn't
 * contain the information, this calculator might not be able to create some of
 * its summaries. These cases should be pretty obvious though. If for example
 * the turn interval does not contain a record of every single turn, it cannot
 * make calculations which are based on such data.
 * <p>
 * Note that this class is immutable, while some of its members may be mutable.
 * This has to be taken into account while using this class.
 */
final class SummaryDataCalculator {
    private static final String GUILD_CHALLENGE = "Guild Challenge";

    private static final String ENCHANTED_BARBELL = "enchanted barbell";

    private static final String CONCENTRATED_MAGICALNESS_PILL = "concentrated magicalness pill";

    private static final String GIANT_MOXIE_WEED = "giant moxie weed";

    private static final Set<String> HIPSTER_COMBAT_NAMES_SET = Sets.immutableSetOf("angry bassist",
                                                                                    "blue-haired girl",
                                                                                    "evil ex-girlfriend",
                                                                                    "peeved roommate",
                                                                                    "random scenester",
                                                                                    "black crayon beast",
                                                                                    "black crayon beetle",
                                                                                    "black crayon constellation",
                                                                                    "black crayon golem",
                                                                                    "black crayon demon",
                                                                                    "black crayon man",
                                                                                    "black crayon elemental",
                                                                                    "black crayon crimbo elf",
                                                                                    "black crayon fish",
                                                                                    "black crayon goblin",
                                                                                    "black crayon hippy",
                                                                                    "black crayon hobo",
                                                                                    "black crayon shambling monstrosity",
                                                                                    "black crayon manloid",
                                                                                    "black crayon mer-kin",
                                                                                    "black crayon frat orc",
                                                                                    "black crayon penguin",
                                                                                    "black crayon pirate",
                                                                                    "black crayon flower",
                                                                                    "black crayon slime",
                                                                                    "black crayon undead thing",
                                                                                    "black crayon spiraling shape");

    private static final Map<Integer, Integer> LEVEL_STAT_BOARDERS_MAP;

    static {
        LEVEL_STAT_BOARDERS_MAP = Maps.newHashMap(45);

        // Sets the stat boarders from level 1 to 35.
        LEVEL_STAT_BOARDERS_MAP.put(1, 0);
        for (int i = 2; i <= 35; i++)
            LEVEL_STAT_BOARDERS_MAP.put(i, (i - 1) * (i - 1) + 4);
    }

    private final CountableSet<Consumable> consumablesUsed = new CountableSet<Consumable>();

    private final CountableSet<Item> droppedItems = new CountableSet<Item>();

    private final CountableSet<Skill> skillsCast = new CountableSet<Skill>();

    private final DataCounter<String> turnsPerArea = new DataCounter<String>(200);

    private final DataCounter<String> familiarUsage = new DataCounter<String>();

    private final List<LevelData> levels = Lists.newArrayList(15);

    private final List<DataNumberPair<String>> disintegratedCombats = Lists.newArrayList();

    private final List<DataNumberPair<String>> semirares = Lists.newArrayList();

    private final List<DataNumberPair<String>> badmoonAdventures = Lists.newArrayList();

    private final List<DataNumberPair<String>> romanticArrowUsages = Lists.newArrayList();

    private final List<DataNumberPair<String>> wanderingAdventures = Lists.newArrayList();

    private final List<DataNumberPair<String>> hipsterCombats = Lists.newArrayList();

    private final List<Encounter> freeRunawayCombats = Lists.newArrayList();

    private final ConsumptionSummary consumptionSummary;

    private final FreeRunaways freeRunaways;

    private final Goatlet goatlet = new Goatlet();

    private final InexplicableDoor nesRealm = new InexplicableDoor();

    private final QuestTurncounts questTurncounts;

    private Statgain totalStatgains = Statgain.NO_STATS;

    private Statgain combatsStatgains = Statgain.NO_STATS;

    private Statgain noncombatsStatgains = Statgain.NO_STATS;

    private Statgain othersStatgains = Statgain.NO_STATS;

    private final MeatSummary meatSummary = new MeatSummary();

    private final MPGainSummary mpGainSummary = new MPGainSummary();

    private MPGain totalMPGains = MPGain.NO_MP;

    private final int totalAmountSkillCasts;

    private final int totalMPUsed;

    private final int totalMeatGain;

    private final int totalMeatSpent;

    private final int totalTurnsFromRollover;

    private final int totalTurnsCombat;

    private final int totalTurnsNoncombat;

    private final int totalTurnsOther;

    SummaryDataCalculator(
                          final LogDataHolder logData) {
        if (logData == null)
            throw new NullPointerException("Log data holder must not be null.");

        final List<Consumable> consumables = Lists.newArrayList(100);

        int totalFreeRunawaysTries = 0;
        int successfulFreeRunaways = 0;
        int totalTurnsCombat = 0;
        int totalTurnsNoncombat = 0;
        int totalTurnsOther = 0;
        int totalMeatGain = 0;
        int totalMeatSpent = 0;
        for (final TurnInterval ti : logData.getTurnIntervalsSpent()) {
            // Consumables summary, day of usage is only a hindrance here.
            for (final Consumable c : ti.getConsumablesUsed()) {
                totalStatgains = totalStatgains.addStats(c.getStatGain());

                final Consumable tmp = c.newInstance();
                tmp.setDayNumberOfUsage(Integer.MAX_VALUE);
                consumablesUsed.addElement(tmp);
            }
            consumables.addAll(ti.getConsumablesUsed());

            // Item summary
            for (final Item i : ti.getDroppedItems())
                droppedItems.addElement(i);

            // Skill summary
            for (final Skill s : ti.getSkillsCast())
                skillsCast.addElement(s);

            // MP summary
            totalMPGains = totalMPGains.addMPGains(ti.getMPGain());

            // Turns per area summary
            if (ti.getTotalTurns() > 0)
                turnsPerArea.addDataElement(ti.getAreaName(), ti.getTotalTurns());

            for (final SingleTurn st : ti.getTurns()) {
                // Total turncounts and stats of different turn versions.
                totalStatgains = totalStatgains.addStats(st.getStatGain());
                switch (st.getTurnVersion()) {
                    case COMBAT:
                        totalTurnsCombat++;
                        combatsStatgains = combatsStatgains.addStats(st.getStatGain());
                        break;
                    case NONCOMBAT:
                        totalTurnsNoncombat++;
                        noncombatsStatgains = noncombatsStatgains.addStats(st.getStatGain());
                        break;
                    case OTHER:
                        totalTurnsOther++;
                        othersStatgains = othersStatgains.addStats(st.getStatGain());
                        break;
                    default:
                        break;
                }

                // Familiar usage summary
                if (st.getTurnVersion() == TurnVersion.COMBAT)
                    familiarUsage.addDataElement(st.getUsedFamiliar().getFamiliarName());

                // Disintegrated combats summary
                if (st.isDisintegrated())
                    disintegratedCombats.add(DataNumberPair.of(st.getEncounterName(),
                                                               st.getTurnNumber()));

                // Semirare summary
                if (DataTablesHandler.HANDLER.isSemirareEncounter(st))
                    semirares.add(DataNumberPair.of(st.getEncounterName(), st.getTurnNumber()));

                // Bad Moon summary
                if (DataTablesHandler.HANDLER.isBadMoonEncounter(st))
                    badmoonAdventures.add(DataNumberPair.of(st.getEncounterName(),
                                                            st.getTurnNumber()));

                for (final Encounter e : st.getEncounters()) {
                    // Wandering adventure summary
                    if (DataTablesHandler.HANDLER.isWanderingEncounter(e))
                        wanderingAdventures.add(DataNumberPair.of(e.getEncounterName(),
                                                                  e.getTurnNumber()));

                    if (e.getTurnVersion() == TurnVersion.COMBAT) {
                        // Romantic arrow usages
                        if (e.isSkillCast("fire a badly romantic arrow")
                        		|| e.isSkillCast("wink at"))
                            romanticArrowUsages.add(DataNumberPair.of(e.getEncounterName(),
                                                                      e.getTurnNumber()));

                        // Hipster combat summary
                        if (HIPSTER_COMBAT_NAMES_SET.contains(e.getEncounterName()))
                            hipsterCombats.add(DataNumberPair.of(e.getAreaName(), e.getTurnNumber()));

                        // Free runaway combats summary
                        if (e.getFreeRunaways() > 0)
                            freeRunawayCombats.add(e);
                    }
                }
            }

            // Free runaways summary
            final FreeRunaways runaways = ti.getRunawayAttempts();
            totalFreeRunawaysTries += runaways.getNumberOfAttemptedRunaways();
            successfulFreeRunaways += runaways.getNumberOfSuccessfulRunaways();

            // Goatlet summary
            if (ti.getAreaName().equals("Goatlet")) {
                goatlet.setTurnsSpent(goatlet.getTurnsSpent() + ti.getTotalTurns());

                for (final SingleTurn st : ti.getTurns())
                    if (st.getEncounterName().equals("dairy goat"))
                        goatlet.setDairyGoatsFound(goatlet.getDairyGoatsFound() + 1);

                for (final Item i : ti.getDroppedItems())
                    if (i.getName().equals("goat cheese"))
                        goatlet.setCheeseFound(goatlet.getCheeseFound() + i.getAmount());
                    else if (i.getName().equals("glass of goat's milk"))
                        goatlet.setMilkFound(goatlet.getMilkFound() + i.getAmount());
            }

            // 8-Bit Realm summary
            if (ti.getAreaName().equals("8-Bit Realm")) {
                nesRealm.setTurnsSpent(nesRealm.getTurnsSpent() + ti.getTotalTurns());

                for (final SingleTurn st : ti.getTurns())
                    if (st.getEncounterName().equals("Bullet Bill"))
                        nesRealm.setBulletsFound(nesRealm.getBulletsFound() + 1);
                    else if (st.getEncounterName().equals("Blooper"))
                        nesRealm.setBloopersFound(nesRealm.getBloopersFound() + 1);
            }

            // Meat gain/spent
            // Nuns encounter meat ignored here.
            if (!ti.getAreaName().equals("Themthar Hills"))
                totalMeatGain += ti.getMeat().encounterMeatGain;
            totalMeatGain += ti.getMeat().otherMeatGain;
            totalMeatSpent += ti.getMeat().meatSpent;
        }
        freeRunaways = new FreeRunaways(totalFreeRunawaysTries, successfulFreeRunaways);
        this.totalTurnsCombat = totalTurnsCombat;
        this.totalTurnsNoncombat = totalTurnsNoncombat;
        this.totalTurnsOther = totalTurnsOther;

        // Consumption summary
        consumptionSummary = new ConsumptionSummary(consumables, logData.getDayChanges());
        final int tempRolloverTurns = logData.getLastTurnSpent().getTurnNumber()
                                      - consumptionSummary.getTotalTurnsFromFood()
                                      - consumptionSummary.getTotalTurnsFromBooze()
                                      - consumptionSummary.getTotalTurnsFromOther();
        totalTurnsFromRollover = tempRolloverTurns < 0 ? 0 : tempRolloverTurns;

        // Total meat gain/spent
        this.totalMeatGain = totalMeatGain;
        this.totalMeatSpent = totalMeatSpent;

        // Total amount of skill casts and total MP used
        int totalAmountSkillCasts = 0;
        int totalMPUsed = 0;
        for (final Skill s : skillsCast.getElements()) {
            totalAmountSkillCasts += s.getAmount();
            totalMPUsed += s.getMpCost();
        }
        this.totalAmountSkillCasts = totalAmountSkillCasts;
        this.totalMPUsed = totalMPUsed;

        // Level data summary
        if (!logData.isSubintervalLog())
            createLevelSummaryData(logData);
        else
            for (final LevelData ld : logData.getLevels())
                levels.add(ld);

        // Meat and MP gain per level summary
        for (final TurnInterval ti : logData.getTurnIntervalsSpent())
            for (final SingleTurn st : ti.getTurns()) {
                final int currentLevel = logData.getCurrentLevel(st.getTurnNumber())
                                                .getLevelNumber();
                if (!st.getMeat().isMeatGainSpentZero())
                    meatSummary.addLevelData(currentLevel, st.getMeat());
                if (!st.getMPGain().isMPGainZero())
                    mpGainSummary.addLevelData(currentLevel, st.getMPGain());
            }

        // Quest turncount summary
        questTurncounts = new QuestTurncounts(logData.getTurnIntervalsSpent(),
                                              droppedItems.getElements());
    }

    /**
     * Automatically creates the level summary from the turn rundown of the
     * ascension log.
     */
    private void createLevelSummaryData(
                                        final LogDataHolder logData) {
        final Iterator<PlayerSnapshot> plSsIter = logData.getPlayerSnapshots().iterator();
        PlayerSnapshot currentPlayerSnapshot = plSsIter.hasNext() ? plSsIter.next() : null;
        int currentStatBoarder = LEVEL_STAT_BOARDERS_MAP.get(2);
        Statgain stats = Statgain.NO_STATS;
        int combatTurns = 0;
        int noncombatTurns = 0;
        int otherTurns = 0;

        // Try to guess the character class if it isn't set yet.
        if (logData.getCharacterClass() == CharacterClass.NOT_DEFINED) {
            final Set<String> guildItems = Sets.newHashSet(5);
            for (final TurnInterval ti : logData.getTurnIntervalsSpent())
                if (ti.getAreaName().equals(GUILD_CHALLENGE))
                    for (final Item i : ti.getDroppedItems())
                        if (i.getName().equals(ENCHANTED_BARBELL)
                            || i.getName().equals(CONCENTRATED_MAGICALNESS_PILL)
                            || i.getName().equals(GIANT_MOXIE_WEED))
                            guildItems.add(i.getName());

            if (totalStatgains.mus > totalStatgains.myst && totalStatgains.mus > totalStatgains.mox) {
                if (guildItems.contains(GIANT_MOXIE_WEED))
                    logData.setCharacterClass("Seal Clubber");
                else
                    logData.setCharacterClass("Turtle Tamer");
            } else if (totalStatgains.myst > totalStatgains.mus
                       && totalStatgains.myst > totalStatgains.mox) {
                if (guildItems.contains(GIANT_MOXIE_WEED))
                    logData.setCharacterClass("Sauceror");
                else
                    logData.setCharacterClass("Pastamancer");
            } else if (guildItems.contains(CONCENTRATED_MAGICALNESS_PILL))
                logData.setCharacterClass("Accordion Thief");
            else
                logData.setCharacterClass("Disco Bandit");
        }

        // Substats at the start of an ascension.
        switch (logData.getCharacterClass()) {
            case SEAL_CLUBBER:
                stats = new Statgain(9, 1, 4);
                break;
            case TURTLE_TAMER:
                stats = new Statgain(9, 4, 1);
                break;
            case PASTAMANCER:
                stats = new Statgain(4, 9, 1);
                break;
            case SAUCEROR:
                stats = new Statgain(1, 9, 4);
                break;
            case DISCO_BANDIT:
                stats = new Statgain(4, 1, 9);
                break;
            case ACCORDION_THIEF:
                stats = new Statgain(1, 4, 9);
                break;
        }
        // Set level 1.
        levels.add(new LevelData(1, 0));
        levels.get(0).setStatsAtLevelReached(stats);

        for (final TurnInterval ti : logData.getTurnIntervalsSpent())
            for (final SingleTurn st : ti.getTurns()) {
                // Add stats to the stat counter.
                stats = stats.addStats(st.getStatGain());
                for (final Consumable c : st.getConsumablesUsed())
                    stats = stats.addStats(c.getStatGain());

                if (currentPlayerSnapshot != null
                    && currentPlayerSnapshot.getTurnNumber() <= st.getTurnNumber()) {
                    final int playerMus = currentPlayerSnapshot.getMuscleStats()
                                          * currentPlayerSnapshot.getMuscleStats();
                    final int playerMyst = currentPlayerSnapshot.getMystStats()
                                           * currentPlayerSnapshot.getMystStats();
                    final int playerMox = currentPlayerSnapshot.getMoxieStats()
                                          * currentPlayerSnapshot.getMoxieStats();

                    // Player snapshot is always right, so if it says the player
                    // stats are higher, set them to that value.
                    if (playerMus > stats.mus)
                        stats = stats.setMuscle(playerMus);
                    if (playerMyst > stats.myst)
                        stats = stats.setMyst(playerMyst);
                    if (playerMox > stats.mox)
                        stats = stats.setMoxie(playerMox);

                    currentPlayerSnapshot = plSsIter.hasNext() ? plSsIter.next() : null;
                }

                // Increment the correct turn counter.
                switch (st.getTurnVersion()) {
                    case COMBAT:
                        combatTurns++;
                        break;
                    case NONCOMBAT:
                        noncombatTurns++;
                        break;
                    case OTHER:
                        otherTurns++;
                        break;
                }

                // Check whether a new level is reached and act accordingly.
                while (isNewLevelReached(logData, currentStatBoarder, stats)) {
                    final LevelData newLevel = computeNewLevelReached(st.getTurnNumber(),
                                                                      stats,
                                                                      combatTurns,
                                                                      noncombatTurns,
                                                                      otherTurns);

                    levels.add(newLevel);

                    currentStatBoarder = LEVEL_STAT_BOARDERS_MAP.get(newLevel.getLevelNumber() + 1);
                    combatTurns = 0;
                    noncombatTurns = 0;
                    otherTurns = 0;
                }
            }

        // Add level data to the LogDataHolder if it isn't created from a
        // pre-parsed ascension log.
        if (logData.isDetailedLog())
            for (final LevelData lvl : levels)
                logData.addLevel(lvl);
    }

    private boolean isNewLevelReached(
                                      final LogDataHolder logData, final int currentStatBoarder,
                                      final Statgain stats) {
        boolean isNewLevelReached = false;
        switch (logData.getCharacterClass().getStatClass()) {
            case MUSCLE:
                isNewLevelReached = currentStatBoarder <= Math.sqrt(stats.mus);
                break;
            case MYSTICALITY:
                isNewLevelReached = currentStatBoarder <= Math.sqrt(stats.myst);
                break;
            case MOXIE:
                isNewLevelReached = currentStatBoarder <= Math.sqrt(stats.mox);
                break;
        }

        return isNewLevelReached;
    }

    /**
     * Adds the still missing data to the current level and returns the next
     * level.
     */
    private LevelData computeNewLevelReached(
                                             final int currentTurnNumber,
                                             final Statgain currentStats, final int combatTurns,
                                             final int noncombatTurns, final int otherTurns) {
        final LevelData currentLevel = levels.get(levels.size() - 1);
        final LevelData newLevel = new LevelData(currentLevel.getLevelNumber() + 1,
                                                 currentTurnNumber);
        final int turnDifference = currentTurnNumber - currentLevel.getLevelReachedOnTurn();
        final int substatAmountCurrentLevel = LEVEL_STAT_BOARDERS_MAP.get(currentLevel.getLevelNumber())
                                              * LEVEL_STAT_BOARDERS_MAP.get(currentLevel.getLevelNumber());
        final int substatAmountNewLevel = LEVEL_STAT_BOARDERS_MAP.get(newLevel.getLevelNumber())
                                          * LEVEL_STAT_BOARDERS_MAP.get(newLevel.getLevelNumber());

        currentLevel.setCombatTurns(combatTurns);
        currentLevel.setNoncombatTurns(noncombatTurns);
        currentLevel.setOtherTurns(otherTurns);
        if (turnDifference > 0)
            currentLevel.setStatGainPerTurn((substatAmountNewLevel - substatAmountCurrentLevel)
                                            * 1.0 / turnDifference);
        else
            currentLevel.setStatGainPerTurn(substatAmountNewLevel - substatAmountCurrentLevel);

        newLevel.setStatsAtLevelReached(currentStats);

        return newLevel;
    }

    /**
     * @return A list of areas and the turns spent in them.
     */
    List<DataNumberPair<String>> getTurnsPerArea() {
        return turnsPerArea.getCountedData();
    }

    /**
     * @return A list of all consumables used.
     */
    Collection<Consumable> getConsumablesUsed() {
        return consumablesUsed.getElements();
    }

    /**
     * @return A list of all items dropped.
     */
    Collection<Item> getDroppedItems() {
        return droppedItems.getElements();
    }

    /**
     * @return A list of all skills cast.
     */
    Collection<Skill> getSkillsCast() {
        return skillsCast.getElements();
    }

    /**
     * @return A list of all levels.
     */
    List<LevelData> getLevelData() {
        return levels;
    }

    /**
     * @return A list of all used familiars and how often they were used.
     */
    List<DataNumberPair<String>> getFamiliarUsage() {
        return familiarUsage.getCountedData();
    }

    /**
     * @return A list of all disintegrated combats.
     */
    List<DataNumberPair<String>> getDisintegratedCombats() {
        return disintegratedCombats;
    }

    /**
     * @return A list of all semirares.
     */
    List<DataNumberPair<String>> getSemirares() {
        return semirares;
    }

    /**
     * @return A list of all Bad Moon adventures.
     */
    List<DataNumberPair<String>> getBadmoonAdventures() {
        return badmoonAdventures;
    }

    /**
     * @return A list of all romantic arrow usages.
     */
    List<DataNumberPair<String>> getRomanticArrowUsages() {
        return romanticArrowUsages;
    }

    /**
     * @return A list of all wandering adventures.
     */
    List<DataNumberPair<String>> getWanderingAdventures() {
        return wanderingAdventures;
    }

    /**
     * @return A list of all Hipster combats.
     */
    List<DataNumberPair<String>> getHipsterCombats() {
        return hipsterCombats;
    }

    /**
     * @return A list of all combats on which free runaways were successfully
     *         used.
     */
    List<Encounter> getFreeRunawaysCombats() {
        return freeRunawayCombats;
    }

    /**
     * @return A summary on consumables used during the ascension.
     */
    ConsumptionSummary getConsumptionSummary() {
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
    Goatlet getGoatlet() {
        return goatlet;
    }

    /**
     * @return The RNG data of the 8-Bit Realm.
     */
    InexplicableDoor get8BitRealm() {
        return nesRealm;
    }

    /**
     * @return The quest turncounts.
     */
    QuestTurncounts getQuestTurncounts() {
        return questTurncounts;
    }

    /**
     * @return The total mp gains collected during this ascension.
     */
    MPGain getTotalMPGains() {
        return totalMPGains;
    }

    /**
     * @return The mp gains per level summary.
     */
    MPGainSummary getMPGainSummary() {
        return mpGainSummary;
    }

    /**
     * @return The meat per level summary.
     */
    MeatSummary getMeatSummary() {
        return meatSummary;
    }

    /**
     * @return The total amount of substats collected during this ascension.
     */
    Statgain getTotalStatgains() {
        return totalStatgains;
    }

    /**
     * @return The total amount of substats from combats collected during this
     *         ascension.
     */
    Statgain getCombatsStatgains() {
        return combatsStatgains;
    }

    /**
     * @return The total amount of substats from noncombats collected during
     *         this ascension.
     */
    Statgain getNoncombatsStatgains() {
        return noncombatsStatgains;
    }

    /**
     * @return The total amount of substats from other encounters collected
     *         during this ascension.
     */
    Statgain getOthersStatgains() {
        return othersStatgains;
    }

    /**
     * @return The total amount of skill casts.
     */
    int getTotalAmountSkillCasts() {
        return totalAmountSkillCasts;
    }

    /**
     * @return The total amount of MP spent on skills.
     */
    int getTotalMPUsed() {
        return totalMPUsed;
    }

    /**
     * @return The total amount of meat gathered.
     */
    int getTotalMeatGain() {
        return totalMeatGain;
    }

    /**
     * @return The total amount of meat spent.
     */
    int getTotalMeatSpent() {
        return totalMeatSpent;
    }

    /**
     * @return The total amount of turns gained from rollover.
     */
    int getTotalTurnsFromRollover() {
        return totalTurnsFromRollover;
    }

    /**
     * @return The total amount of combat turns.
     */
    int getTotalTurnsCombat() {
        return totalTurnsCombat;
    }

    /**
     * @return The total amount of noncombat turns.
     */
    int getTotalTurnsNoncombat() {
        return totalTurnsNoncombat;
    }

    /**
     * @return The total amount of other (smithing, mixing, cooking, etc.)
     *         turns.
     */
    int getTotalTurnsOther() {
        return totalTurnsOther;
    }
}
