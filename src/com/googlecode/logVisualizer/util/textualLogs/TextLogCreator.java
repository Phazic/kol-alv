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

package com.googlecode.logVisualizer.util.textualLogs;

import static com.googlecode.logVisualizer.parser.UsefulPatterns.NON_ASCII;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.NumberFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import net.java.dev.spellcast.utilities.DataUtilities;
import net.java.dev.spellcast.utilities.UtilityConstants;

import com.googlecode.logVisualizer.Settings;
import com.googlecode.logVisualizer.logData.Item;
import com.googlecode.logVisualizer.logData.LogDataHolder;
import com.googlecode.logVisualizer.logData.LogDataHolder.StatClass;
import com.googlecode.logVisualizer.logData.CombatItem;
import com.googlecode.logVisualizer.logData.MPGain;
import com.googlecode.logVisualizer.logData.MeatGain;
import com.googlecode.logVisualizer.logData.Skill;
import com.googlecode.logVisualizer.logData.Statgain;
import com.googlecode.logVisualizer.logData.consumables.Consumable;
import com.googlecode.logVisualizer.logData.consumables.Consumable.ConsumableVersion;
import com.googlecode.logVisualizer.logData.logSummary.AreaStatgains;
import com.googlecode.logVisualizer.logData.logSummary.LevelData;
import com.googlecode.logVisualizer.logData.turn.DetailedTurnInterval;
import com.googlecode.logVisualizer.logData.turn.Encounter;
import com.googlecode.logVisualizer.logData.turn.FreeRunaways;
import com.googlecode.logVisualizer.logData.turn.SingleTurn;
import com.googlecode.logVisualizer.logData.turn.TurnInterval;
import com.googlecode.logVisualizer.logData.turn.TurnVersion;
import com.googlecode.logVisualizer.logData.turn.turnAction.DayChange;
import com.googlecode.logVisualizer.logData.turn.turnAction.FamiliarChange;
import com.googlecode.logVisualizer.logData.turn.turnAction.PlayerSnapshot;
import com.googlecode.logVisualizer.logData.turn.turnAction.Pull;
import com.googlecode.logVisualizer.parser.UsefulPatterns;
import com.googlecode.logVisualizer.util.DataCounter;
import com.googlecode.logVisualizer.util.DataNumberPair;
import com.googlecode.logVisualizer.util.Lists;
import com.googlecode.logVisualizer.util.LogOutputFormat;
import com.googlecode.logVisualizer.util.Maps;
import com.googlecode.logVisualizer.util.Pair;
import com.googlecode.logVisualizer.util.Sets;
import com.googlecode.logVisualizer.util.dataTables.DataTablesHandler;
import com.googlecode.logVisualizer.util.dataTables.ExtraStats;

/**
 * This utility class creates a parsed ascension log from a
 * {@link LogDataHolder}. The format of the parsed log is similar to the one
 * which the AFH parser uses.
 * <p>
 * Note that this class only works with detailed LogDataHolders (see
 * {@link LogDataHolder#isDetailedLog()}), because non-detailed LogDataHolders
 * do not contain enough data.
 * <p>
 * All methods in this class throw a {@link NullPointerException} if a null
 * object reference is passed in any parameter.
 */
public final class TextLogCreator {
    private static final String NEW_LINE = System.getProperty("line.separator");

    private static final String COMMA = ", ";

    private static final String OPENING_TURN_BRACKET = " [";

    private static final String CLOSING_TURN_BRACKET = "] ";

    private static final String ITEM_PREFIX = "     +>";

    private static final String ITEM_MIDDLE_STRING = "Got ";

    private static final String CONSUMABLE_PREFIX = "     o> ";

    private static final String PULL_PREFIX = "     #> Turn";

    private static final String LEARN_SKILL_PREFIX = "     @>";
    
    private static final String LEVEL_CHANGE_PREFIX = "     => Level ";

    private static final String BANISHED_COMBAT_PREFIX = "     b>";
    
    private static final String BANISHED_COMBAT_DESC = "Banished ";
    
    private static final String HUNTED_COMBAT_PREFIX = "     *>";

    private static final String HUNTED_COMBAT_MIDDLE_STRING = "Started hunting ";

    private static final String DISINTEGRATED_COMBAT_PREFIX = "     }>";

    private static final String DISINTEGRATED_COMBAT_MIDDLE_STRING = "Disintegrated ";

    private static final String FAMILIAR_CHANGE_PREFIX = "     -> Turn";

    private static final String SEMIRARE_PREFIX = "     #>";

    private static final String SEMIRARE_MIDDLE_STRING = "Semirare: ";

    private static final String BAD_MOON_PREFIX = "     %>";

    private static final String BAD_MOON_MIDDLE_STRING = "Badmoon: ";

    private static final String FREE_RUNAWAYS_PREFIX = "     &> ";

    private static final String HYBRIDIZE_PREFIX = "     h> ";
    
    private static final String ADVENTURES_LEFT_STRING = "Adventure count at day start: ";

    private static final String CURRENT_MEAT_STRING = "Current meat: ";

    private static final String CHATEAU_REST_AREA = "Rest in your bed in the Chateau";

    private static final String SMITH_STRING = "smith ";

    private static final String MIX_STRING = "mix ";

    private static final String COOK_STRING = "cook ";

    private static final DayChange NO_DAY_CHANGE = new DayChange(Integer.MAX_VALUE,
            Integer.MAX_VALUE);

    private static final Map<String, String> TEXT_LOG_ADDITIONS_MAP = readAugmentationsList(DataUtilities.getReader(UtilityConstants.KOL_DATA_DIRECTORY,
            "textAugmentations.txt"));

    private static final Map<String, String> HTML_LOG_ADDITIONS_MAP = readAugmentationsList(DataUtilities.getReader(UtilityConstants.KOL_DATA_DIRECTORY,
            "htmlAugmentations.txt"));

    private static final Map<String, String> BBCODE_LOG_ADDITIONS_MAP = readAugmentationsList(DataUtilities.getReader(UtilityConstants.KOL_DATA_DIRECTORY,
            "bbcodeAugmentations.txt"));

    private final Map<String, String> logAdditionsMap;

    private final Set<String> localeOnetimeItemsSet = Sets.newHashSet(300);

    private final StringBuilder log;

    private final Iterator<FamiliarChange> familiarChangeIter;

    private FamiliarChange currentFamChange;

    private final Iterator<Pull> pullIter;

    private Pull currentPull;

    private final Iterator<LevelData> levelIter;

    private LevelData nextLevel;

    private final Iterator<DataNumberPair<String>> huntedCombatIter;

    private DataNumberPair<String> currentHuntedCombat;

    private DataNumberPair<String> freeRunsByZone;

    private final Iterator<DataNumberPair<String>> banishedCombatIter;
    
    private final Iterator<DataNumberPair<String>> disintegratedCombatIter;

    private final Iterator<DataNumberPair<String>> hybridDataIter;
    
    private final Iterator<DataNumberPair<String>> learnedSkillIter;
    
    private DataNumberPair<String> currentLearnedSkill;
    
    private DataNumberPair<String> currentBanishedCombat;
    
    private DataNumberPair<String> currentDisintegratedCombat;

    private DataNumberPair<String> currentHybridData;
    private boolean isShowNotes = true;

    /**
     * Helper method to parse out the augmentation values for the textual log
     * outputs and return them in a map.
     * <p>
     * Currently used key names are:
     *
     * <pre>
     * logHeaderStart
     * logHeaderEnd
     * turnStart
     * turnEnd
     * dayChangeLineStart
     * dayChangeLineEnd
     * statgainStart
     * statgainEnd
     * pullStart
     * pullEnd
     * consumableStart
     * consumableEnd
     * itemStart
     * itemEnd
     * familiarStart
     * familiarEnd
     * huntedStart
     * huntedEnd
     * yellowRayStart
     * yellowRayEnd
     * specialEncounterStart
     * specialEncounterEnd
     * levelStart
     * levelEnd
     * runawayStart
     * runawayEnd
     * notesStart
     * notesEnd
     * </pre>
     */
    private static Map<String, String> readAugmentationsList(
            final BufferedReader br) {
        final Map<String, String> map = Maps.newHashMap();
        String tmpLine;

        try {
            while ((tmpLine = br.readLine()) != null)
                // Ignore empty lines and comments
                if (tmpLine.length() > 0 && !tmpLine.startsWith("//") && !tmpLine.startsWith("#")) {
                    final String[] split = tmpLine.split("\\s+\\|\\s+");
                    map.put(split[0], split[1].replaceAll("N_L", NEW_LINE));
                }

            br.close();
        } catch (final IOException e) {
            e.printStackTrace();
        }

        return map;
    }

    /**
     * Creates a list of all turn interval print-outs as they are composed in a
     * turn rundown inside a textual ascension log.
     *
     * @param logData
     *            The ascension log data from which the parsed ascension log
     *            should be created.
     * @return The turn rundown list.
     * @throws IllegalArgumentException
     *             if the given log data is not a detailed LogDataHolder, see
     *             {@link LogDataHolder#isDetailedLog()}
     */
    public static List<String> getTurnRundownList(
            final LogDataHolder logData) {
        if (!logData.isDetailedLog())
            throw new IllegalArgumentException("Only detailed logs can be used by the TextualLogCreator.");

        final TextLogCreator logCreator = new TextLogCreator(logData, LogOutputFormat.TEXT_LOG);
        logCreator.isShowNotes = false;
        return logCreator.createTurnRundownList(logData);
    }

    /**
     * Creates a parsed ascension log from the given {@link LogDataHolder} and
     * returns it as a String.
     *
     * @param logData
     *            The ascension log data from which the parsed ascension log
     *            should be created.
     * @param logVersion
     *            The wanted version of the textual log output.
     * @return The textual ascension log.
     * @throws IllegalArgumentException
     *             if the given log data is not a detailed LogDataHolder, see
     *             {@link LogDataHolder#isDetailedLog()}
     */
    public static String getTextualLog(
            final LogDataHolder logData, final LogOutputFormat logVersion) {
        // Sometimes, geek jokes are fun! ;)
        int logDate = 404;
        if (UsefulPatterns.USUAL_FORMAT_LOG_NAME.matcher(logData.getLogName()).matches())
            logDate = UsefulPatterns.getLogDate(logData.getLogName());

        return getTextualLog(logData, logDate, logVersion);
    }

    /**
     * Creates a parsed ascension log from the given {@link LogDataHolder} and
     * returns it as a String.
     *
     * @param logData
     *            The ascension log data from which the parsed ascension log
     *            should be created.
     * @param ascensionStartDate
     *            The real-time start date of the ascension as saved by
     *            KolMafia.
     * @param logVersion
     *            The wanted version of the textual log output.
     * @return The textual ascension log.
     * @throws IllegalArgumentException
     *             if the given log data is not a detailed LogDataHolder, see
     *             {@link LogDataHolder#isDetailedLog()}
     */
    public static String getTextualLog(
            final LogDataHolder logData, final int ascensionStartDate,
            final LogOutputFormat logVersion) {
        if (!logData.isDetailedLog())
            throw new IllegalArgumentException("Only detailed logs can be used by the TextualLogCreator.");

        final TextLogCreator logCreator = new TextLogCreator(logData, logVersion);

        final String logOutput;
        if (Settings.getSettingBoolean("Show non-ASCII characters in parsed logs"))
            logOutput = logCreator.createTextLog(logData, ascensionStartDate);
        else
            logOutput = NON_ASCII.matcher(logCreator.createTextLog(logData, ascensionStartDate))
            .replaceAll(UsefulPatterns.EMPTY_STRING);

        if (logVersion == LogOutputFormat.HTML_LOG)
            return "<html><body>" + logOutput.replace(NEW_LINE, "<br>" + NEW_LINE)
                    + "</body></html>";
        else
            return logOutput;
    }

    /**
     * Creates a parsed ascension log from the given {@link LogDataHolder} and
     * saves it to the given file.
     *
     * @param logData
     *            The ascension log data from which the parsed ascension log
     *            should be created.
     * @param saveDest
     *            The file in which the parsed ascension log should be saved in.
     * @param logVersion
     *            The wanted version of the textual log output.
     * @throws IllegalArgumentException
     *             if saveDest doesn't exist or is a directory; if the given log
     *             data is not a detailed LogDataHolder, see
     *             {@link LogDataHolder#isDetailedLog()}
     */
    public static void saveTextualLogToFile(
            final LogDataHolder logData, final File saveDest,
            final LogOutputFormat logVersion)
                    throws IOException {
        if (!saveDest.exists())
            throw new IllegalArgumentException("The file doesn't exist.");
        if (saveDest.isDirectory())
            throw new IllegalArgumentException("The file is a directory.");

        final PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(saveDest),
                50000));
        writer.print(getTextualLog(logData, logVersion));
        writer.close();
    }

    /**
     * Creates a parsed ascension log from the given {@link LogDataHolder} and
     * saves it to the given file.
     *
     * @param logData
     *            The ascension log data from which the parsed ascension log
     *            should be created.
     * @param ascensionStartDate
     *            The real-time start date of the ascension as saved by
     *            KolMafia.
     * @param saveDest
     *            The file in which the parsed ascension log should be saved in.
     * @param logVersion
     *            The wanted version of the textual log output.
     * @throws IllegalArgumentException
     *             if saveDest doesn't exist or is a directory; if the given log
     *             data is not a detailed LogDataHolder, see
     *             {@link LogDataHolder#isDetailedLog()}
     */
    public static void saveTextualLogToFile(
            final LogDataHolder logData,
            final int ascensionStartDate, final File saveDest,
            final LogOutputFormat logVersion)
                    throws IOException {
        if (!saveDest.exists())
            throw new IllegalArgumentException("The file doesn't exist.");
        if (saveDest.isDirectory())
            throw new IllegalArgumentException("The file is a directory.");

        final PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(saveDest),
                50000));
        writer.print(getTextualLog(logData, ascensionStartDate, logVersion));
        writer.close();
    }

    /**
     * Sets up a TextLogCreator instance for further use.
     *
     * @param logData
     *            The ascension log data from which the parsed ascension log
     *            should be created.
     * @param logVersion
     *            The wanted version of the textual log output.
     */
    private TextLogCreator(
            final LogDataHolder logData, final LogOutputFormat logVersion) {
        if (logData == null)
            throw new NullPointerException("The LogDataHolder must not be null.");

        switch (logVersion) {
        case HTML_LOG:
            logAdditionsMap = Collections.unmodifiableMap(HTML_LOG_ADDITIONS_MAP);
            break;
        case BBCODE_LOG:
            logAdditionsMap = Collections.unmodifiableMap(BBCODE_LOG_ADDITIONS_MAP);
            break;
        default:
            logAdditionsMap = Collections.unmodifiableMap(TEXT_LOG_ADDITIONS_MAP);
        }

        // Populate local one-time item set with all one-time items.
        for (final Entry<String, Boolean> item : DataTablesHandler.HANDLER.getItemdropsMap()
                .entrySet())
            if (item.getValue())
                localeOnetimeItemsSet.add(item.getKey());

        // Most logs stay below 50000 characters.
        log = new StringBuilder(50000);

        familiarChangeIter = logData.getFamiliarChanges().iterator();
        pullIter = logData.getPulls().iterator();
        levelIter = logData.getLevels().iterator();
        huntedCombatIter = logData.getHuntedCombats().iterator();
        disintegratedCombatIter = logData.getLogSummary().getDisintegratedCombats().iterator();
        banishedCombatIter = logData.getLogSummary().getBanishedCombats().iterator(); //Bombar: Add banished combat support
        hybridDataIter = logData.getHybridContent().iterator();
        learnedSkillIter = logData.getLearnedSkills().iterator();
    }

    /**
     * Creates a parsed ascension log in a style similar to the format used by
     * the AFH parser.
     *
     * @param logData
     *            The LogDataHolder from which the ascension log should be
     *            created.
     * @param ascensionStartDate
     *            The real-time start date of the ascension as saved by
     *            KolMafia.
     */
    private List<String> createTurnRundownList(
            final LogDataHolder logData) {
        final List<String> turnRundown = Lists.newArrayList(logData.getTurnsSpent().size());

        currentFamChange = familiarChangeIter.hasNext() ? familiarChangeIter.next() : null;
        currentPull = pullIter.hasNext() ? pullIter.next() : null;
        currentHuntedCombat = huntedCombatIter.hasNext() ? huntedCombatIter.next() : null;
        currentDisintegratedCombat = disintegratedCombatIter.hasNext() ? disintegratedCombatIter.next() : null;
        currentBanishedCombat = banishedCombatIter.hasNext() ? banishedCombatIter.next() : null;
        
        // Level 1 can be skipped.
        levelIter.next();
        nextLevel = levelIter.hasNext() ? levelIter.next() : null;

        final Iterator<DayChange> dayChangeIter = logData.getDayChanges().iterator();
        DayChange currentDay = dayChangeIter.next();
        DayChange nextDay = dayChangeIter.hasNext() ? dayChangeIter.next() : NO_DAY_CHANGE;

        for (final TurnInterval ti : logData.getTurnIntervalsSpent()) {
            if (!nextDay.equals(NO_DAY_CHANGE) && ti.getEndTurn() >= nextDay.getTurnNumber())
                if (ti.getEndTurn() == nextDay.getTurnNumber()) {
                    printTurnIntervalContents(ti, currentDay.getDayNumber());

                    final int currentStringLenght = log.length();
                    final Pair<DayChange, DayChange> newDayChangeData = printDayChanges(logData,
                            ti.getEndTurn(),
                            currentDay,
                            nextDay,
                            dayChangeIter);
                    currentDay = newDayChangeData.getVar1();
                    nextDay = newDayChangeData.getVar2();
                    log.delete(currentStringLenght, log.length());

                    // Consumables usage or pulls that happened nominally on the
                    // last turn before the day change, but were actually done
                    // on the next day.
                    printCurrentConsumables(ti.getConsumablesUsed(), currentDay.getDayNumber());
                    printCurrentPulls(currentDay.getDayNumber(), ti.getEndTurn());
                } else if (ti.getStartTurn() < nextDay.getTurnNumber()) {
                    SingleTurn dayChangeTurn = null;
                    for (final SingleTurn st : ti.getTurns())
                        if (st.getTurnNumber() > nextDay.getTurnNumber()) {
                            dayChangeTurn = st;
                            break;
                        }

                    final TurnInterval turnsBeforeDayChange = new DetailedTurnInterval(ti.getTurns()
                            .headSet(dayChangeTurn),
                            dayChangeTurn.getAreaName());
                    final TurnInterval turnsAfterDayChange = new DetailedTurnInterval(ti.getTurns()
                            .tailSet(dayChangeTurn),
                            dayChangeTurn.getAreaName());

                    printTurnIntervalContents(turnsBeforeDayChange, currentDay.getDayNumber());

                    final int currentStringLenght = log.length();
                    final Pair<DayChange, DayChange> newDayChangeData = printDayChanges(logData,
                            ti.getEndTurn(),
                            currentDay,
                            nextDay,
                            dayChangeIter);
                    currentDay = newDayChangeData.getVar1();
                    nextDay = newDayChangeData.getVar2();
                    log.delete(currentStringLenght, log.length());

                    // Consumables usage or pulls that happened nominally on the
                    // last turn before the day change, but were actually done
                    // on the next day.
                    printCurrentConsumables(turnsBeforeDayChange.getConsumablesUsed(),
                            currentDay.getDayNumber());
                    printCurrentPulls(currentDay.getDayNumber(), turnsBeforeDayChange.getEndTurn());
                    log.append(NEW_LINE);
                    printTurnIntervalContents(turnsAfterDayChange, currentDay.getDayNumber());
                } else {
                    final int currentStringLenght = log.length();
                    final Pair<DayChange, DayChange> newDayChangeData = printDayChanges(logData,
                            ti.getEndTurn(),
                            currentDay,
                            nextDay,
                            dayChangeIter);
                    currentDay = newDayChangeData.getVar1();
                    nextDay = newDayChangeData.getVar2();
                    log.delete(currentStringLenght, log.length());

                    printTurnIntervalContents(ti, currentDay.getDayNumber());
                }
            else
                printTurnIntervalContents(ti, currentDay.getDayNumber());

            turnRundown.add(log.toString());
            log.delete(0, log.length());
        }

        return turnRundown;
    }

    /**
     * //Bombar: Edit this to get free turns working correctly on cross boundary days
     * Creates a parsed ascension log in a style similar to the format used by
     * the AFH parser.
     *
     * @param logData
     *            The LogDataHolder from which the ascension log should be
     *            created.
     * @param ascensionStartDate
     *            The real-time start date of the ascension as saved by
     *            KolMafia.
     */
    private String createTextLog(
            final LogDataHolder logData, final int ascensionStartDate) {
        currentFamChange = familiarChangeIter.hasNext() ? familiarChangeIter.next() : null;
        currentPull = pullIter.hasNext() ? pullIter.next() : null;
        currentHuntedCombat = huntedCombatIter.hasNext() ? huntedCombatIter.next() : null;
        currentDisintegratedCombat = disintegratedCombatIter.hasNext() ? disintegratedCombatIter.next()
                : null;
        currentBanishedCombat = banishedCombatIter.hasNext() ? banishedCombatIter.next() : null;
        currentHybridData = hybridDataIter.hasNext() ? hybridDataIter.next() : null;
        currentLearnedSkill = learnedSkillIter.hasNext() ? learnedSkillIter.next() : null;
        
        // Level 1 can be skipped.
        levelIter.next();
        nextLevel = levelIter.hasNext() ? levelIter.next() : null;

        final Iterator<DayChange> dayChangeIter = logData.getDayChanges().iterator();
        DayChange currentDay = dayChangeIter.next();
        DayChange nextDay = dayChangeIter.hasNext() ? dayChangeIter.next() : NO_DAY_CHANGE;

        // Add the log file header.
        write("NEW " + logData.getCharacterClass() + " " + logData.getGameMode() + " "
                + logData.getAscensionPath() + " ASCENSION STARTED " + ascensionStartDate + NEW_LINE);
        write("------------------------------" + NEW_LINE + NEW_LINE);
        write(logAdditionsMap.get("logHeaderStart"));
        write("This log was created by the Ascension Log Visualizer "
                + Settings.getSettingString("Version") + "." + NEW_LINE);
        write("The basic idea and the format of this parser have been borrowed from the AFH MafiaLog Parser by VladimirPootin and QuantumNightmare."
                + NEW_LINE + NEW_LINE);
        write(logAdditionsMap.get("logHeaderEnd"));
        write(logAdditionsMap.get("dayChangeLineStart"));
        write(currentDay.toString());
        write(logAdditionsMap.get("dayChangeLineEnd"));
        write(NEW_LINE + NEW_LINE);
        if (logData.getHeaderFooterComment(currentDay) != null)
            printNotes(logData.getHeaderFooterComment(currentDay).getHeaderComments());

        for (int turnIntervalNdx = 0; turnIntervalNdx < logData.getTurnIntervalsSpent().size(); turnIntervalNdx++) {
        	final TurnInterval ti = logData.getTurnIntervalsSpent().get( turnIntervalNdx );
        	        	
            if (!nextDay.equals(NO_DAY_CHANGE) && ti.getEndTurn() >= nextDay.getTurnNumber()) {
                if (ti.getEndTurn() == nextDay.getTurnNumber()) {
                    printTurnIntervalContents(ti, currentDay.getDayNumber());

                    //Peek at next interval to make sure it doesn't contain any current day turns
                    if (turnIntervalNdx + 1 < logData.getTurnIntervalsSpent().size()) {
                    	final TurnInterval next = logData.getTurnIntervalsSpent().get( turnIntervalNdx + 1 );
                    	boolean hasOneOnCurrentDay = false;
                    	for (final SingleTurn st : next.getTurns()) {
                    		if (st.getDayNumber() == currentDay.getDayNumber()) {
                    			hasOneOnCurrentDay = true;
                    		}
                    	}
                    	
                    	if (hasOneOnCurrentDay)
                    		continue;
                    }
                    
                    final Pair<DayChange, DayChange> newDayChangeData = printDayChanges(logData,
                            ti.getEndTurn(),
                            currentDay,
                            nextDay,
                            dayChangeIter);
                    currentDay = newDayChangeData.getVar1();
                    nextDay = newDayChangeData.getVar2();

                    // Consumables usage or pulls that happened nominally on the
                    // last turn before the day change, but were actually done
                    // on the next day.
                    printCurrentConsumables(ti.getConsumablesUsed(), currentDay.getDayNumber());
                    printCurrentPulls(currentDay.getDayNumber(), ti.getEndTurn());
                } else if (ti.getStartTurn() < nextDay.getTurnNumber()) {
                    SingleTurn dayChangeTurn = null;
                    for (final SingleTurn st : ti.getTurns())
                        if (st.getTurnNumber() > nextDay.getTurnNumber()) {
                            dayChangeTurn = st;
                            break;
                        }

                    final TurnInterval turnsBeforeDayChange = new DetailedTurnInterval(ti.getTurns()
                            .headSet(dayChangeTurn),
                            dayChangeTurn.getAreaName());
                    final TurnInterval turnsAfterDayChange = new DetailedTurnInterval(ti.getTurns()
                            .tailSet(dayChangeTurn),
                            dayChangeTurn.getAreaName());

                    printTurnIntervalContents(turnsBeforeDayChange, currentDay.getDayNumber());

                    final Pair<DayChange, DayChange> newDayChangeData = printDayChanges(logData,
                            ti.getEndTurn(),
                            currentDay,
                            nextDay,
                            dayChangeIter);
                    currentDay = newDayChangeData.getVar1();
                    nextDay = newDayChangeData.getVar2();

                    // Consumables usage or pulls that happened nominally on the
                    // last turn before the day change, but were actually done
                    // on the next day.
                    printCurrentConsumables(turnsBeforeDayChange.getConsumablesUsed(),
                            currentDay.getDayNumber());
                    printCurrentPulls(currentDay.getDayNumber(), turnsBeforeDayChange.getEndTurn());

                    printTurnIntervalContents(turnsAfterDayChange, currentDay.getDayNumber());
                } else if (ti.getStartTurn() == nextDay.getTurnNumber()) {
                	//Check to see if a day change occurs within block
                	SingleTurn dayChangeTurn = null;
                    for (final SingleTurn st : ti.getTurns()) {
                        if (st.getDayNumber() == nextDay.getDayNumber()) {
                            dayChangeTurn = st;
                            break;
                        }
                    }
                    
                    if (dayChangeTurn == null)  {
                    	//No Day change occurred
                        printTurnIntervalContents(ti, currentDay.getDayNumber());

                        //Peek at next interval to make sure it doesn't contain any current day turns
                        if (turnIntervalNdx + 1 < logData.getTurnIntervalsSpent().size()) {
                        	final TurnInterval next = logData.getTurnIntervalsSpent().get( turnIntervalNdx + 1 );
                        	boolean hasOneOnCurrentDay = false;
                        	for (final SingleTurn st : next.getTurns()) {
                        		if (st.getDayNumber() == currentDay.getDayNumber()) {
                        			hasOneOnCurrentDay = true;
                        		}
                        	}
                        	
                        	if (hasOneOnCurrentDay)
                        		continue;
                        }
                        
                        final Pair<DayChange, DayChange> newDayChangeData = printDayChanges(logData,
                                ti.getEndTurn(),
                                currentDay,
                                nextDay,
                                dayChangeIter);
                        currentDay = newDayChangeData.getVar1();
                        nextDay = newDayChangeData.getVar2();

                        // Consumables usage or pulls that happened nominally on the
                        // last turn before the day change, but were actually done
                        // on the next day.
                        printCurrentConsumables(ti.getConsumablesUsed(), currentDay.getDayNumber());
                        printCurrentPulls(currentDay.getDayNumber(), ti.getEndTurn());                    	
                    } else {
                        final TurnInterval turnsBeforeDayChange = new DetailedTurnInterval(ti.getTurns()
                            .headSet(dayChangeTurn),
                            dayChangeTurn.getAreaName());
                        final TurnInterval turnsAfterDayChange = new DetailedTurnInterval(ti.getTurns()
                            .tailSet(dayChangeTurn),
                            dayChangeTurn.getAreaName());

                        printTurnIntervalContents(turnsBeforeDayChange, currentDay.getDayNumber());

                        final Pair<DayChange, DayChange> newDayChangeData = printDayChanges(logData,
                            ti.getEndTurn(),
                            currentDay,
                            nextDay,
                            dayChangeIter);
                        currentDay = newDayChangeData.getVar1();
                        nextDay = newDayChangeData.getVar2();

                        // Consumables usage or pulls that happened nominally on the
                        // last turn before the day change, but were actually done
                        // on the next day.
                        printCurrentConsumables(turnsBeforeDayChange.getConsumablesUsed(),
                        	currentDay.getDayNumber());
                        printCurrentPulls(currentDay.getDayNumber(), turnsBeforeDayChange.getEndTurn());

                        printTurnIntervalContents(turnsAfterDayChange, currentDay.getDayNumber());                    	
                    }
                } else {
                    final Pair<DayChange, DayChange> newDayChangeData = printDayChanges(logData,
                            ti.getEndTurn(),
                            currentDay,
                            nextDay,
                            dayChangeIter);
                    currentDay = newDayChangeData.getVar1();
                    nextDay = newDayChangeData.getVar2();

                    printTurnIntervalContents(ti, currentDay.getDayNumber());
                }
            } else
                printTurnIntervalContents(ti, currentDay.getDayNumber());
        }
        printNotes(logData.getHeaderFooterComment(currentDay).getFooterComments());
        write(NEW_LINE + "Turn rundown finished!");
        write(logAdditionsMap.get("turnRundownEnd"));
        write(NEW_LINE + NEW_LINE);

        printLogSummaries(logData);

        return log.toString();
    }

    /**
     * Prints all day changes that occurred and returns the new current day
     * number and the next day change. If no day change occurred, the old values
     * will be returned.
     */
    private Pair<DayChange, DayChange> printDayChanges(
            final LogDataHolder logData,
            final int currentTurnNumber,
            DayChange currentDay, DayChange nextDay,
            final Iterator<DayChange> dayChangeIter) {
        while (!nextDay.equals(NO_DAY_CHANGE) && currentTurnNumber >= nextDay.getTurnNumber()) {
            final PlayerSnapshot currentSnapshot = logData.getFirstPlayerSnapshotAfterTurn(nextDay.getTurnNumber());

            if (logData.getHeaderFooterComment(currentDay) != null)
                printNotes(logData.getHeaderFooterComment(currentDay).getFooterComments());

            write(NEW_LINE);
            write(logAdditionsMap.get("dayChangeLineStart"));
            write(nextDay.toString());
            write(logAdditionsMap.get("dayChangeLineEnd"));
            if (currentSnapshot != null) {
                write(NEW_LINE);
                write(ADVENTURES_LEFT_STRING);
                write(currentSnapshot.getAdventuresLeft());
                write(NEW_LINE);
                write(CURRENT_MEAT_STRING);
                write(currentSnapshot.getCurrentMeat());
            }
            write(NEW_LINE);
            write(NEW_LINE);

            if (logData.getHeaderFooterComment(nextDay) != null)
                printNotes(logData.getHeaderFooterComment(nextDay).getHeaderComments());

            currentDay = nextDay;
            nextDay = dayChangeIter.hasNext() ? dayChangeIter.next() : NO_DAY_CHANGE;
        }

        return Pair.of(currentDay, nextDay);
    }

    /**
     * Prints all pulls from the given day up to the given turn number.
     */
    private void printCurrentPulls(
            final int currentDayNumber, final int currentTurnNumber) {
        while (currentPull != null && currentTurnNumber >= currentPull.getTurnNumber()) {
            // Only pulls of the current day should be added here.
            if (currentPull.getDayNumber() > currentDayNumber)
                break;

            write(PULL_PREFIX);
            write(OPENING_TURN_BRACKET);
            write(currentPull.getTurnNumber());
            write(CLOSING_TURN_BRACKET);
            write("pulled");
            write(UsefulPatterns.WHITE_SPACE);
            write(logAdditionsMap.get("pullStart"));
            write(currentPull.getAmount());
            write(UsefulPatterns.WHITE_SPACE);
            write(currentPull.getItemName());
            write(logAdditionsMap.get("pullEnd"));
            write(NEW_LINE);

            currentPull = pullIter.hasNext() ? pullIter.next() : null;
        }
    }

    /**
     * Prints all consumables from the given day.
     */
    private void printCurrentConsumables(
            final Collection<Consumable> consumables,
            final int currentDayNumber) {
        for (final Consumable c : consumables)
            if (c.getDayNumberOfUsage() == currentDayNumber)
                if (c.getAdventureGain() > 0 || !c.getStatGain().isAllStatsZero()
                        || UsefulPatterns.SPECIAL_CONSUMABLES.contains(c.getName())) {
                    write(CONSUMABLE_PREFIX);

                    if (c.getConsumableVersion() == ConsumableVersion.FOOD)
                        write("Ate ");
                    else if (c.getConsumableVersion() == ConsumableVersion.BOOZE)
                        write("Drank ");
                    else
                        write("Used ");

                    write(logAdditionsMap.get("consumableStart"));
                    write(c.getAmount());
                    write(UsefulPatterns.WHITE_SPACE);
                    write(c.getName());
                    write(logAdditionsMap.get("consumableEnd"));

                    if (c.getAdventureGain() > 0
                            || c.getConsumableVersion() == ConsumableVersion.FOOD
                            || c.getConsumableVersion() == ConsumableVersion.BOOZE) {
                        write(UsefulPatterns.WHITE_SPACE);
                        write(UsefulPatterns.ROUND_BRACKET_OPEN);
                        write(c.getAdventureGain());
                        write(UsefulPatterns.WHITE_SPACE);
                        write("adventures gained");
                        write(UsefulPatterns.ROUND_BRACKET_CLOSE);
                    }

                    write(UsefulPatterns.WHITE_SPACE);
                    write(logAdditionsMap.get("statgainStart"));
                    write(c.getStatGain().toString());
                    write(logAdditionsMap.get("statgainEnd"));
                    write(NEW_LINE);
                }
    }

    /**
     * Prints the given notes. If the interval contains no notes or notes are
     * not supposed to be printed, this method won't print anything.
     */
    private void printNotes(
            final String notes) {
        if (isShowNotes && notes.length() > 0) {
            write(logAdditionsMap.get("notesStart"));
            write(notes.replaceAll("\r\n", NEW_LINE).replaceAll("[\r\n]", NEW_LINE));
            write(logAdditionsMap.get("notesEnd"));
            write(NEW_LINE);
        }
    }

    private void printItemAcquisitionStartString(
            final int turnNumber) {
        write(ITEM_PREFIX);
        write(OPENING_TURN_BRACKET);
        write(turnNumber);
        write(CLOSING_TURN_BRACKET);
        write(ITEM_MIDDLE_STRING);
    }

    /**
     * @param ti
     *            The turn interval whose contents should be printed.
     */
    private void printTurnIntervalContents(
            final TurnInterval ti, final int currentDayNumber) {
        printNotes(ti.getPreIntervalComment().getComments());

        write(logAdditionsMap.get("turnStart"));
        write(UsefulPatterns.SQUARE_BRACKET_OPEN);
        if (ti.getTotalTurns() > 1) {
            write(ti.getStartTurn() + 1);
            write(UsefulPatterns.MINUS);
        }
        write(ti.getEndTurn());
        write(UsefulPatterns.SQUARE_BRACKET_CLOSE);
        write(logAdditionsMap.get("turnEnd"));

        write(UsefulPatterns.WHITE_SPACE);
        write(ti.getAreaName());
        write(UsefulPatterns.WHITE_SPACE);

        write(logAdditionsMap.get("statgainStart"));
        write(ti.getStatGain().toString());
        write(logAdditionsMap.get("statgainEnd"));
        write(NEW_LINE);

        for (final SingleTurn st : ti.getTurns()) {
            if (DataTablesHandler.HANDLER.isSemirareEncounter(st)) {
                write(SEMIRARE_PREFIX);
                write(OPENING_TURN_BRACKET);
                write(st.getTurnNumber());
                write(CLOSING_TURN_BRACKET);
                write(SEMIRARE_MIDDLE_STRING);
                write(logAdditionsMap.get("specialEncounterStart"));
                write(st.getEncounterName());
                write(logAdditionsMap.get("specialEncounterEnd"));
                write(NEW_LINE);
            }
            if (DataTablesHandler.HANDLER.isBadMoonEncounter(st)) {
                write(BAD_MOON_PREFIX);
                write(OPENING_TURN_BRACKET);
                write(st.getTurnNumber());
                write(CLOSING_TURN_BRACKET);
                write(BAD_MOON_MIDDLE_STRING);
                write(logAdditionsMap.get("specialEncounterStart"));
                write(st.getEncounterName());
                write(logAdditionsMap.get("specialEncounterEnd"));
                write(NEW_LINE);
            }

            // Iterate all encounters on this turn
            // This helps with tracking interesting free turn things
            for (Encounter e : st.getEncounters())
            {            	            	
                // Don't log the encounter if it matches the parent turn interval
                if (st.getAreaName().contains(e.getAreaName()) == false)
                {
                    if (e.getAreaName().contains(CHATEAU_REST_AREA))
                    {
                        // Log it using the free runaway prefix
                        write(FREE_RUNAWAYS_PREFIX);
                        write(UsefulPatterns.SQUARE_BRACKET_OPEN);
                        write(st.getTurnNumber());
                        write(UsefulPatterns.SQUARE_BRACKET_CLOSE);
                        write(UsefulPatterns.WHITE_SPACE);
                        write(e.getAreaName());
                        write(UsefulPatterns.WHITE_SPACE);
                        write(logAdditionsMap.get("statgainStart"));
                        write(e.getStatGain().toString());
                        write(logAdditionsMap.get("statgainEnd"));
                        write(NEW_LINE);
                    }

                    // Log turn-free crafting as well

                    if (e.getAreaName().toLowerCase().startsWith(MIX_STRING) ||
                            e.getAreaName().toLowerCase().startsWith(SMITH_STRING) ||
                            e.getAreaName().toLowerCase().startsWith(COOK_STRING))
                    {
                        // Log it using the free runaway prefix
                        write(FREE_RUNAWAYS_PREFIX);
                        write(UsefulPatterns.SQUARE_BRACKET_OPEN);
                        write(st.getTurnNumber());
                        write(UsefulPatterns.SQUARE_BRACKET_CLOSE);
                        write(UsefulPatterns.WHITE_SPACE);
                        write(e.getAreaName());
                        write(NEW_LINE);

                    }
                }
            }

            final List<Item> importantItems = Lists.newArrayList();
            for (final Item i : st.getDroppedItems()) {
                final String itemName = NON_ASCII.matcher(i.getName().toLowerCase(Locale.ENGLISH))
                        .replaceAll(UsefulPatterns.EMPTY_STRING);
                if (DataTablesHandler.HANDLER.isImportantItem(itemName))
                    importantItems.add(i);
                else if (localeOnetimeItemsSet.contains(itemName)) {
                    importantItems.add(i);
                    localeOnetimeItemsSet.remove(itemName);
                }
            }
            
            final Iterator<Item> aquiredItemsIter = importantItems.iterator();
            if (aquiredItemsIter.hasNext()) {
                printItemAcquisitionStartString(st.getTurnNumber());

                int itemCounter = 0;
                while (aquiredItemsIter.hasNext()) {
                    final Item currentItem = aquiredItemsIter.next();

                    // If the number of items would be excessive, format it with a quantity number

                    // More than three is defined as excessive

                    if (currentItem.getAmount() >= 3)
                    {
                        write(logAdditionsMap.get("itemStart"));
                        write(currentItem.getName());
                        write(" x ");
                        write(currentItem.getAmount());
                        write(logAdditionsMap.get("itemEnd"));
                        itemCounter++;

                        if (aquiredItemsIter.hasNext() && itemCounter >= 4) {
                            write(NEW_LINE);
                            printItemAcquisitionStartString(st.getTurnNumber());
                            itemCounter = 0;
                        }

                    }
                    else
                    {
                        for (int i = currentItem.getAmount(); i > 0; i--) {
                            write(logAdditionsMap.get("itemStart"));
                            write(currentItem.getName());
                            write(logAdditionsMap.get("itemEnd"));
                            itemCounter++;

                            if ((aquiredItemsIter.hasNext() || i > 1) && itemCounter >= 4) {
                                write(NEW_LINE);
                                printItemAcquisitionStartString(st.getTurnNumber());
                                itemCounter = 0;
                            } else if (i > 1)
                                write(COMMA);
                        }
                    }

                    if (aquiredItemsIter.hasNext() && itemCounter != 0)
                        write(COMMA);

                }

                write(NEW_LINE);
            }
        }

        printCurrentConsumables(ti.getConsumablesUsed(), currentDayNumber);

        printCurrentPulls(currentDayNumber, ti.getEndTurn());
        
        while (currentHybridData != null && ti.getEndTurn() >= currentHybridData.getNumber()) {
            write(HYBRIDIZE_PREFIX);
            write(UsefulPatterns.SQUARE_BRACKET_OPEN);
            write(currentHybridData.getNumber());
            write(UsefulPatterns.SQUARE_BRACKET_CLOSE);
            write(UsefulPatterns.WHITE_SPACE);
            write(currentHybridData.getData());
            write(NEW_LINE);
            
            currentHybridData = hybridDataIter.hasNext() ? hybridDataIter.next() : null;
        }
        
        while (currentHuntedCombat != null && ti.getEndTurn() >= currentHuntedCombat.getNumber()) {
            write(HUNTED_COMBAT_PREFIX);
            write(OPENING_TURN_BRACKET);
            write(currentHuntedCombat.getNumber());
            write(CLOSING_TURN_BRACKET);
            write(HUNTED_COMBAT_MIDDLE_STRING);
            write(logAdditionsMap.get("huntedStart"));
            write(currentHuntedCombat.getData());
            write(logAdditionsMap.get("huntedEnd"));
            write(NEW_LINE);

            currentHuntedCombat = huntedCombatIter.hasNext() ? huntedCombatIter.next() : null;
        }

        while (currentBanishedCombat != null && ti.getEndTurn() >= currentBanishedCombat.getNumber()) {
        	write(BANISHED_COMBAT_PREFIX);
        	write(UsefulPatterns.WHITE_SPACE);
            write(UsefulPatterns.SQUARE_BRACKET_OPEN);
            write(currentBanishedCombat.getNumber());
            write(UsefulPatterns.SQUARE_BRACKET_CLOSE);
            write(UsefulPatterns.WHITE_SPACE);
            write(BANISHED_COMBAT_DESC + currentBanishedCombat.getData());
            write(NEW_LINE);
            
            currentBanishedCombat = banishedCombatIter.hasNext() ? banishedCombatIter.next() : null;
        }
        
        while (currentDisintegratedCombat != null
                && ti.getEndTurn() >= currentDisintegratedCombat.getNumber()) {
            write(DISINTEGRATED_COMBAT_PREFIX);
            write(OPENING_TURN_BRACKET);
            write(currentDisintegratedCombat.getNumber());
            write(CLOSING_TURN_BRACKET);
            write(DISINTEGRATED_COMBAT_MIDDLE_STRING);
            write(logAdditionsMap.get("yellowRayStart"));
            write(currentDisintegratedCombat.getData());
            write(logAdditionsMap.get("yellowRayEnd"));
            write(NEW_LINE);

            currentDisintegratedCombat = disintegratedCombatIter.hasNext() ? disintegratedCombatIter.next()
                    : null;
        }

        while (currentFamChange != null && ti.getEndTurn() >= currentFamChange.getTurnNumber()) {
            write(FAMILIAR_CHANGE_PREFIX);
            write(OPENING_TURN_BRACKET);
            write(currentFamChange.getTurnNumber());
            write(CLOSING_TURN_BRACKET);
            write(logAdditionsMap.get("familiarStart"));
            write(currentFamChange.getFamiliarName());
            write(logAdditionsMap.get("familiarEnd"));
            write(NEW_LINE);

            currentFamChange = familiarChangeIter.hasNext() ? familiarChangeIter.next() : null;
        }

        final FreeRunaways freeRunaways = ti.getRunawayAttempts();
        if (freeRunaways.getNumberOfAttemptedRunaways() > 0) {
            write(FREE_RUNAWAYS_PREFIX);
            write(logAdditionsMap.get("runawayStart"));
            write(freeRunaways.toString());
            write(logAdditionsMap.get("runawayEnd"));
            write(NEW_LINE);
        }

        while (currentLearnedSkill != null && ti.getEndTurn() >= currentLearnedSkill.getNumber()) {
        	write(LEARN_SKILL_PREFIX);
        	write("Learned: " + currentLearnedSkill.getData() + " (Turn " + currentLearnedSkill.getNumber() + ")");
        	write(NEW_LINE);
        	
        	currentLearnedSkill = learnedSkillIter.hasNext() ? learnedSkillIter.next() : null;
        }
        
        while (nextLevel != null && ti.getEndTurn() >= nextLevel.getLevelReachedOnTurn()) {
            // Only print the level if it actually *is* part of this turn
            // interval.
            if (ti.getStartTurn() <= nextLevel.getLevelReachedOnTurn()) {
                final int musStat = (int) Math.sqrt(nextLevel.getStatsAtLevelReached().mus);
                final int mystStat = (int) Math.sqrt(nextLevel.getStatsAtLevelReached().myst);
                final int moxStat = (int) Math.sqrt(nextLevel.getStatsAtLevelReached().mox);

                write(logAdditionsMap.get("levelStart"));
                write(LEVEL_CHANGE_PREFIX);
                write(nextLevel.getLevelNumber());
                write(" (Turn ");
                write(nextLevel.getLevelReachedOnTurn());
                write(")! (");
                write(musStat);
                write("/");
                write(mystStat);
                write("/");
                write(moxStat);
                write(UsefulPatterns.ROUND_BRACKET_CLOSE);
                write(logAdditionsMap.get("levelEnd"));
                write(NEW_LINE);
            }

            nextLevel = levelIter.hasNext() ? levelIter.next() : null;
        }

        printNotes(ti.getPostIntervalComment().getComments());
    }

    private void printLogSummaries(
            final LogDataHolder logData) {
        // Turns spent per area summary
        write("ADVENTURES" + NEW_LINE + "----------" + NEW_LINE);

        for (final DataNumberPair<String> dn : logData.getLogSummary().getTurnsPerArea()) {
            write(dn.getData());
            write(": ");
            write(dn.getNumber());
            write(NEW_LINE);
        }
        write(NEW_LINE + NEW_LINE + NEW_LINE);

        // Quest Turns summary
        write("QUEST TURNS" + NEW_LINE + "----------" + NEW_LINE);
        write("Spooky Forest: "
                + logData.getLogSummary().getQuestTurncounts().templeOpeningTurns + NEW_LINE);
        write("Tavern quest: " + logData.getLogSummary().getQuestTurncounts().tavernQuestTurns
                + NEW_LINE);
        write("Bat quest: " + logData.getLogSummary().getQuestTurncounts().batQuestTurns + NEW_LINE);
        write("Cobb's Knob quest: " + logData.getLogSummary().getQuestTurncounts().knobQuestTurns
                + NEW_LINE);
        write("Friars' quest: " + logData.getLogSummary().getQuestTurncounts().friarsQuestTurns
                + NEW_LINE);
        write("Pandamonium quest: "
                + logData.getLogSummary().getQuestTurncounts().pandamoniumQuestTurns + NEW_LINE);
        write("Defiled Cyrpt quest: "
                + logData.getLogSummary().getQuestTurncounts().cyrptQuestTurns + NEW_LINE);
        write("Trapzor quest: " + logData.getLogSummary().getQuestTurncounts().trapzorQuestTurns
                + NEW_LINE);
        write("Orc Chasm quest: " + logData.getLogSummary().getQuestTurncounts().chasmQuestTurns
                + NEW_LINE);
        write("Airship: " + logData.getLogSummary().getQuestTurncounts().airshipQuestTurns
                + NEW_LINE);
        write("Giant's Castle: " + logData.getLogSummary().getQuestTurncounts().castleQuestTurns
                + NEW_LINE);
        write("Pirate quest: " + logData.getLogSummary().getQuestTurncounts().pirateQuestTurns
                + NEW_LINE);
        write("Black Forest quest: "
                + logData.getLogSummary().getQuestTurncounts().blackForrestQuestTurns + NEW_LINE);
        write("Desert Oasis quest: "
                + logData.getLogSummary().getQuestTurncounts().desertOasisQuestTurns + NEW_LINE);
        write("Spookyraven First Floor: "
                + logData.getLogSummary().getQuestTurncounts().spookyravenFirstFloor + NEW_LINE);
        write("Spookyraven Second Floor: "
                + logData.getLogSummary().getQuestTurncounts().spookyravenSecondFloor + NEW_LINE);
        write("Spookyraven Cellar: "
                + logData.getLogSummary().getQuestTurncounts().spookyravenQuestTurns + NEW_LINE);
        write("Hidden City quest: "
                + logData.getLogSummary().getQuestTurncounts().templeCityQuestTurns + NEW_LINE);
        write("Palindome quest: "
                + logData.getLogSummary().getQuestTurncounts().palindomeQuestTurns + NEW_LINE);
        write("Pyramid quest: " + logData.getLogSummary().getQuestTurncounts().pyramidQuestTurns
                + NEW_LINE);
        write("Starting the War: "
                + logData.getLogSummary().getQuestTurncounts().warIslandOpeningTurns + NEW_LINE);
        write("War Island quest: "
                + logData.getLogSummary().getQuestTurncounts().warIslandQuestTurns + NEW_LINE);
        write("DoD quest: " + logData.getLogSummary().getQuestTurncounts().dodQuestTurns + NEW_LINE);
        write("Daily Dungeon: " + logData.getLogSummary().getQuestTurncounts().dailyDungeonTurns
                + NEW_LINE);
        write(NEW_LINE + NEW_LINE + NEW_LINE);

        // Pulls summary
        write("PULLS" + NEW_LINE + "----------" + NEW_LINE);
        final DataCounter<String> pullsCounter = new DataCounter<String>((int) (logData.getPulls()
                .size() * 1.4) + 1);
        for (final Pull p : logData.getPulls())
            pullsCounter.addDataElement(p.getItemName(), p.getAmount());
        final List<DataNumberPair<String>> pulls = pullsCounter.getCountedData();
        // ordered from highest to lowest amount
        Collections.sort(pulls, new Comparator<DataNumberPair<String>>() {

            public int compare(
                    final DataNumberPair<String> o1, final DataNumberPair<String> o2) {
                return o2.compareTo(o1);
            }
        });
        for (final DataNumberPair<String> dn : pulls) {
            write("Pulled ");
            write(dn.getNumber());
            write(" ");
            write(dn.getData());
            write(NEW_LINE);
        }
        write(NEW_LINE + NEW_LINE + NEW_LINE);

        // Level summary
        write("LEVELS" + NEW_LINE + "----------" + NEW_LINE);
        final NumberFormat formatter = NumberFormat.getNumberInstance(Locale.ENGLISH);
        formatter.setMaximumFractionDigits(1);
        formatter.setMinimumFractionDigits(1);
        LevelData lastLevel = null;
        for (final LevelData ld : logData.getLevels()) {
            final int turnDifference = lastLevel != null ? ld.getLevelReachedOnTurn()
                    - lastLevel.getLevelReachedOnTurn() : 0;
                    final double statsPerTurn = lastLevel != null ? lastLevel.getStatGainPerTurn() : 0;
                    final int combatTurns = lastLevel != null ? lastLevel.getCombatTurns() : 0;
                    final int noncombatTurns = lastLevel != null ? lastLevel.getNoncombatTurns() : 0;
                    final int otherTurns = lastLevel != null ? lastLevel.getOtherTurns() : 0;

                    write(ld.toString());
                    write(COMMA);
                    write(turnDifference);
                    write(" from last level. (");
                    write(formatter.format(statsPerTurn));
                    write(" substats / turn)");
                    write(NEW_LINE);

                    write("   Combats: ");
                    write(combatTurns);
                    write(NEW_LINE);
                    write("   Noncombats: ");
                    write(noncombatTurns);
                    write(NEW_LINE);
                    write("   Other: ");
                    write(otherTurns);
                    write(NEW_LINE);

                    lastLevel = ld;
        }
        write(NEW_LINE + NEW_LINE);
        final int totalTurns = logData.getLastTurnSpent().getTurnNumber();
        write("Total COMBATS: " + logData.getLogSummary().getTotalTurnsCombat() + " ("
                + Math.round(logData.getLogSummary().getTotalTurnsCombat() * 1000.0 / totalTurns)
                / 10.0 + "%)" + NEW_LINE);
        write("Total NONCOMBATS: " + logData.getLogSummary().getTotalTurnsNoncombat() + " ("
                + Math.round(logData.getLogSummary().getTotalTurnsNoncombat() * 1000.0 / totalTurns)
                / 10.0 + "%)" + NEW_LINE);
        write("Total OTHER: " + logData.getLogSummary().getTotalTurnsOther() + " ("
                + Math.round(logData.getLogSummary().getTotalTurnsOther() * 1000.0 / totalTurns)
                / 10.0 + "%)" + NEW_LINE);
        write(NEW_LINE + NEW_LINE + NEW_LINE);

        // Stats summary
        write("STATS" + NEW_LINE + "----------" + NEW_LINE);
        final Statgain totalStats = logData.getLogSummary().getTotalStatgains();
        final Statgain combatStats = logData.getLogSummary().getCombatsStatgains();
        final Statgain noncombatStats = logData.getLogSummary().getNoncombatsStatgains();
        final Statgain otherStats = logData.getLogSummary().getOthersStatgains();
        final Statgain foodStats = logData.getLogSummary().getFoodConsumablesStatgains();
        final Statgain boozeStats = logData.getLogSummary().getBoozeConsumablesStatgains();
        final Statgain usingStats = logData.getLogSummary().getUsedConsumablesStatgains();
        write("           \tMuscle\tMyst\tMoxie" + NEW_LINE);
        write("Totals:   \t" + totalStats.mus + "\t" + totalStats.myst + "\t" + totalStats.mox
                + NEW_LINE);
        write("Combats:\t" + combatStats.mus + "\t" + combatStats.myst + "\t" + combatStats.mox
                + NEW_LINE);
        write("Noncombats:\t" + noncombatStats.mus + "\t" + noncombatStats.myst + "\t"
                + noncombatStats.mox + NEW_LINE);
        write("Others:   \t" + otherStats.mus + "\t" + otherStats.myst + "\t" + otherStats.mox
                + NEW_LINE);
        write("Eating:   \t" + foodStats.mus + "\t" + foodStats.myst + "\t" + foodStats.mox
                + NEW_LINE);
        write("Drinking:\t" + boozeStats.mus + "\t" + boozeStats.myst + "\t" + boozeStats.mox
                + NEW_LINE);
        write("Using:   \t" + usingStats.mus + "\t" + usingStats.myst + "\t" + usingStats.mox
                + NEW_LINE);
        write(NEW_LINE + NEW_LINE);
        final List<AreaStatgains> areas = Lists.newArrayList(logData.getLogSummary()
                .getAreasStatgains());
        Collections.sort(areas, new Comparator<AreaStatgains>() {
            public int compare(
                    final AreaStatgains o1, final AreaStatgains o2) {
                if (logData.getCharacterClass().getStatClass() == StatClass.MUSCLE)
                    return o2.getStatgain().mus - o1.getStatgain().mus;
                else if (logData.getCharacterClass().getStatClass() == StatClass.MYSTICALITY)
                    return o2.getStatgain().myst - o1.getStatgain().myst;
                else
                    return o2.getStatgain().mox - o1.getStatgain().mox;
            }
        });
        write("Top 10 mainstat gaining areas:" + NEW_LINE + NEW_LINE);
        for (int i = 0; i < areas.size() && i < 10; i++) {
            write(areas.get(i).toString());
            write(NEW_LINE);
        }
        write(NEW_LINE + NEW_LINE + NEW_LINE);

        // +Stat Breakdown summary
        final List<StatgiverItem> statGivers = Lists.newArrayList(20);
        for (final Pair<String, ExtraStats> p : DataTablesHandler.HANDLER.getStatsItems())
            statGivers.add(new StatgiverItem(p.getVar1(), p.getVar2(), logData.getCharacterClass()
                    .getStatClass()));
        final StatgiverItem serpentineSword = new StatgiverItem("serpentine sword",
                new ExtraStats(1.25),
                logData.getCharacterClass()
                .getStatClass());
        final StatgiverItem snakeShield = new StatgiverItem("snake shield",
                new ExtraStats(1.25),
                logData.getCharacterClass()
                .getStatClass());

        final Iterator<LevelData> lvlIndex = logData.getLevels().iterator();
        LevelData nextLvl = lvlIndex.hasNext() ? lvlIndex.next() : null;
        int currentLvlNumber = 1;
        for (final TurnInterval ti : logData.getTurnIntervalsSpent())
            for (final SingleTurn st : ti.getTurns()) {
                while (nextLvl != null && nextLvl.getLevelReachedOnTurn() < st.getTurnNumber()) {
                    currentLvlNumber = nextLvl.getLevelNumber();
                    nextLvl = lvlIndex.hasNext() ? lvlIndex.next() : null;
                }

                if (currentLvlNumber >= 13)
                    break;

                if (st.getTurnVersion() == TurnVersion.COMBAT) {
                    for (final StatgiverItem sgi : statGivers)
                        sgi.incrementLvlStatgain(currentLvlNumber,
                                st.getUsedEquipment()
                                .getNumberOfEquips(sgi.getItemName()));

                    // Special cases
                    final int serpentineSwordEquips = st.getUsedEquipment()
                            .getNumberOfEquips(serpentineSword.getItemName());
                    serpentineSword.incrementLvlStatgain(currentLvlNumber, serpentineSwordEquips);
                    if (serpentineSwordEquips == 1)
                        snakeShield.incrementLvlStatgain(currentLvlNumber,
                                st.getUsedEquipment()
                                .getNumberOfEquips(snakeShield.getItemName()));
                }
            }
        // Add special cases to list for text print out.
        statGivers.add(serpentineSword);
        statGivers.add(snakeShield);

        // Sort item list from highest total stat gain to lowest.
        Collections.sort(statGivers, new Comparator<StatgiverItem>() {
            public int compare(
                    final StatgiverItem o1, final StatgiverItem o2) {
                return o2.getTotalStats() - o1.getTotalStats();
            }
        });

        write("+STAT BREAKDOWN" + NEW_LINE + "----------" + NEW_LINE);
        write("Need to gain level (last is total):                          \t10\t39\t105\t231\t441\t759\t1209\t1815\t2601\t3591\t4809\t6279\t21904"
                + NEW_LINE);
        for (final StatgiverItem sgi : statGivers)
            if (sgi.getTotalStats() > 0) {
                write(sgi.toString());
                write(NEW_LINE);
            }
        write(NEW_LINE + NEW_LINE + NEW_LINE);

        if (logData.getLearnedSkills().size() > 0) {
            write("SKILLS LEARNED" + NEW_LINE + "----------" + NEW_LINE);
            for (final DataNumberPair<String> dn : logData.getLearnedSkills()) {
                write(dn.getNumber());
                write(" : ");
                write(dn.getData());
                write(NEW_LINE);
            }
            write(NEW_LINE + NEW_LINE + NEW_LINE);        	
        }
        
        // Familiars summary
        write("FAMILIARS" + NEW_LINE + "----------" + NEW_LINE);
        for (final DataNumberPair<String> dn : logData.getLogSummary().getFamiliarUsage()) {
            write(dn.getData());
            write(" : ");
            write(dn.getNumber());
            write(" combat turns (");
            write(String.valueOf(Math.round(dn.getNumber() * 1000.0
                    / logData.getLogSummary().getTotalTurnsCombat()) / 10.0));
            write("%)");
            write(NEW_LINE);
        }
        write(NEW_LINE + NEW_LINE + NEW_LINE);

        // Semi-rares summary
        write("SEMI-RARES" + NEW_LINE + "----------" + NEW_LINE);
        for (final DataNumberPair<String> dn : logData.getLogSummary().getSemirares()) {
            write(dn.getNumber());
            write(" : ");
            write(dn.getData());
            write(NEW_LINE);
        }
        write(NEW_LINE + NEW_LINE + NEW_LINE);

        if (logData.getLogSummary().getTrackedCombatItemUses().size() > 0) {
            write("TRACKED COMBAT ITEMS" + NEW_LINE + "----------" + NEW_LINE);
            for (final DataNumberPair<String> dn : logData.getLogSummary().getTrackedCombatItemUses()) {
                write(dn.getNumber());
                write(" : ");
            	write(dn.getData());
                write(NEW_LINE);
            }
            write(NEW_LINE + NEW_LINE + NEW_LINE);        	
        }

        //Hybrid
        if (logData.getHybridContent().size() > 0) {
            write("DNA Lab" + NEW_LINE + "----------" + NEW_LINE);
            for (final DataNumberPair<String> dn : logData.getHybridContent()) {
                write(dn.getNumber());
                write(" : ");
                write(dn.getData());
                write(NEW_LINE);
            }
            write(NEW_LINE + NEW_LINE + NEW_LINE);
        }
        
        // Hunted combats summary
        write("HUNTED COMBATS" + NEW_LINE + "----------" + NEW_LINE);
        for (final DataNumberPair<String> dn : logData.getHuntedCombats()) {
            write(dn.getNumber());
            write(" : ");
            write(dn.getData());
            write(NEW_LINE);
        }
        write(NEW_LINE + NEW_LINE + NEW_LINE);

        // Banished combats summary
        write("BANISHMENT" + NEW_LINE + "----------" + NEW_LINE);
        for (final DataNumberPair<String> dn : logData.getLogSummary().getBanishedCombats()) {
            write(dn.getNumber());
            write(" : ");
            write(dn.getData());
            write(NEW_LINE);
        }
        write(NEW_LINE + NEW_LINE + NEW_LINE);
        
        // Disintegrated combats summary
        write("YELLOW DESTRUCTION" + NEW_LINE + "----------" + NEW_LINE);
        for (final DataNumberPair<String> dn : logData.getLogSummary().getDisintegratedCombats()) {
            write(dn.getNumber());
            write(" : ");
            write(dn.getData());
            write(NEW_LINE);
        }
        write(NEW_LINE + NEW_LINE + NEW_LINE);

        // Copied combats summary
        write("COPIED COMBATS" + NEW_LINE + "----------" + NEW_LINE);
        for (final TurnInterval ti : logData.getCopiedTurns())
            for (final SingleTurn st : ti.getTurns()) {
                write(st.getTurnNumber());
                write(" : ");
                write(st.getEncounterName());
                write(NEW_LINE);
            }
        if (!logData.getLogSummary().getRomanticArrowUsages().isEmpty()) {
            write(NEW_LINE + NEW_LINE);
            write("Familiar copy usage:" + NEW_LINE);
            for (final DataNumberPair<String> dn : logData.getLogSummary().getRomanticArrowUsages()) {
                write(dn.getNumber());
                write(" : ");
                write(dn.getData());
                write(NEW_LINE);
            }
        }
        write(NEW_LINE + NEW_LINE + NEW_LINE);

        // Free runaway summary
        write("FREE RUNAWAYS" + NEW_LINE + "----------" + NEW_LINE);
        write(logData.getLogSummary().getFreeRunaways().toString());
        write(" overall" + NEW_LINE);
        if (!logData.getLogSummary().getFreeRunawaysCombats().isEmpty())
            write(NEW_LINE + NEW_LINE);
        for (final Encounter e : logData.getLogSummary().getFreeRunawaysCombats()) {
            write(e.getTurnNumber());
            write(" : ");
            write(e.getAreaName());
            write(" -- ");
            write(e.getEncounterName());
            write(NEW_LINE);
        }
        write(NEW_LINE + NEW_LINE + NEW_LINE);

        // Wandering encounters summary
        write("WANDERING ENCOUNTERS" + NEW_LINE + "----------" + NEW_LINE);
        for (final DataNumberPair<String> dn : logData.getLogSummary().getWanderingAdventures()) {
            write(dn.getNumber());
            write(" : ");
            write(dn.getData());
            write(NEW_LINE);
        }
        write(NEW_LINE + NEW_LINE + NEW_LINE);

        //Combat Items Used
        write("COMBAT ITEMS" + NEW_LINE + "----------" + NEW_LINE);
        for (final CombatItem ci : logData.getLogSummary().getCombatItemsUsed()) {
        	write("Used ");
        	write(ci.getAmount());
        	write(UsefulPatterns.WHITE_SPACE);
        	write(ci.getName());
        	write(NEW_LINE);
        }
        write(NEW_LINE + NEW_LINE + NEW_LINE);
        
        // Skills cast summary
        write("CASTS" + NEW_LINE + "----------" + NEW_LINE);
        for (final Skill s : logData.getLogSummary().getSkillsCast()) {
            write(s.toString());
            write(NEW_LINE);
        }
        write(NEW_LINE + "------------------" + NEW_LINE + "| Total Casts    |  "
                + logData.getLogSummary().getTotalAmountSkillCasts() + NEW_LINE
                + "------------------" + NEW_LINE);
        write(NEW_LINE + "------------------" + NEW_LINE + "| Total MP Spent    |  "
                + logData.getLogSummary().getTotalMPUsed() + NEW_LINE + "------------------"
                + NEW_LINE);
        write(NEW_LINE + NEW_LINE + NEW_LINE);

        // MP summary
        final MPGain mpGains = logData.getLogSummary().getTotalMPGains();
        write("MP GAINS" + NEW_LINE + "----------" + NEW_LINE);
        write("Total mp gained: " + mpGains.getTotalMPGains() + NEW_LINE + NEW_LINE);
        write("Inside Encounters: " + mpGains.encounterMPGain + NEW_LINE);
        write("Starfish Familiars: " + mpGains.starfishMPGain + NEW_LINE);
        write("Resting: " + mpGains.restingMPGain + NEW_LINE);
        write("Outside Encounters: " + mpGains.outOfEncounterMPGain + NEW_LINE);
        write("Consumables: " + mpGains.consumableMPGain + NEW_LINE + NEW_LINE);
        for (final DataNumberPair<MPGain> dnp : logData.getLogSummary()
                .getMPGainSummary()
                .getAllLevelsData()) {
            write("Level " + dnp.getNumber() + UsefulPatterns.COLON + NEW_LINE);
            write("   Inside Encounters: " + dnp.getData().encounterMPGain + NEW_LINE);
            write("   Starfish Familiars: " + dnp.getData().starfishMPGain + NEW_LINE);
            write("   Resting: " + dnp.getData().restingMPGain + NEW_LINE);
            write("   Outside Encounters: " + dnp.getData().outOfEncounterMPGain + NEW_LINE);
            write("   Consumables: " + dnp.getData().consumableMPGain + NEW_LINE);
        }
        write(NEW_LINE + NEW_LINE + NEW_LINE);

        // Consumables summary
        write("EATING AND DRINKING AND USING" + NEW_LINE + "----------" + NEW_LINE);
        write("Adventures gained eating: " + logData.getLogSummary().getTotalTurnsFromFood()
                + NEW_LINE);
        write("Adventures gained drinking: " + logData.getLogSummary().getTotalTurnsFromBooze()
                + NEW_LINE);
        write("Adventures gained using: " + logData.getLogSummary().getTotalTurnsFromOther()
                + NEW_LINE);
        write("Adventures gained rollover: " + logData.getLogSummary().getTotalTurnsFromRollover()
                + NEW_LINE);
        write(NEW_LINE);
        for (final Consumable c : logData.getLogSummary().getFoodConsumablesUsed()) {
            write(c.toString());
            write(NEW_LINE);
        }
        write(NEW_LINE);
        for (final Consumable c : logData.getLogSummary().getBoozeConsumablesUsed()) {
            write(c.toString());
            write(NEW_LINE);
        }
        write(NEW_LINE);
        for (final Consumable c : logData.getLogSummary().getSpleenConsumablesUsed()) {
            write(c.toString());
            write(NEW_LINE);
        }
        write(NEW_LINE);
        for (final Consumable c : logData.getLogSummary().getOtherConsumablesUsed())
            if (c.getAdventureGain() > 0 || !c.getStatGain().isAllStatsZero()
                    || UsefulPatterns.SPECIAL_CONSUMABLES.contains(c.getName())) {
                write(c.toString());
                write(NEW_LINE);
            }
        write(NEW_LINE + NEW_LINE + NEW_LINE);

        // Meat summary
        write("MEAT" + NEW_LINE + "----------" + NEW_LINE);
        write("Total meat gained: " + logData.getLogSummary().getTotalMeatGain() + NEW_LINE);
        write("Total meat spent: " + logData.getLogSummary().getTotalMeatSpent() + NEW_LINE
                + NEW_LINE);
        for (final DataNumberPair<MeatGain> dnp : logData.getLogSummary()
                .getMeatSummary()
                .getAllLevelsData()) {
            write("Level " + dnp.getNumber() + UsefulPatterns.COLON + NEW_LINE);
            write("   Meat gain inside Encounters: " + dnp.getData().encounterMeatGain + NEW_LINE);
            write("   Meat gain outside Encounters: " + dnp.getData().otherMeatGain + NEW_LINE);
            write("   Meat spent: " + dnp.getData().meatSpent + NEW_LINE);
        }
        write(NEW_LINE + NEW_LINE + NEW_LINE);

        // Bottlenecks summary
        final List<DataNumberPair<String>> lostCombats = logData.getLostCombats();
        write("BOTTLENECKS" + NEW_LINE + "----------" + NEW_LINE);
        write("Spent " + logData.getLogSummary().get8BitRealm().getTurnsSpent()
                + " turns in the 8-Bit Realm" + NEW_LINE);
        write("Fought " + logData.getLogSummary().get8BitRealm().getBloopersFound() + " bloopers"
                + NEW_LINE);
        write("Fought " + logData.getLogSummary().get8BitRealm().getBulletsFound()
                + " bullet bills" + NEW_LINE);
        write("Spent " + logData.getLogSummary().getGoatlet().getTurnsSpent()
                + " turns in the Goatlet" + NEW_LINE);
        write("Fought " + logData.getLogSummary().getGoatlet().getDairyGoatsFound()
                + " dairy goats for " + logData.getLogSummary().getGoatlet().getCheeseFound()
                + " cheeses and " + logData.getLogSummary().getGoatlet().getMilkFound()
                + " glasses of milk" + NEW_LINE);

        final SpookyravenPowerleveling powerleveling = new SpookyravenPowerleveling(logData.getTurnIntervalsSpent());
        write("Spent " + powerleveling.getBallroomTurns()
                + " turns in the Haunted Ballroom and found "
                + powerleveling.getBallroomStatNoncombats() + " Curtains" + NEW_LINE);
        write("Fought " + powerleveling.getZombieWaltzers() + " Zombie Waltzers and found "
                + powerleveling.getDanceCards() + " Dance Cards" + NEW_LINE);
        write("Spent " + powerleveling.getGalleryTurns()
                + " turns in the Haunted Gallery and found " + powerleveling.getLouvres()
                + " Louvres" + NEW_LINE);
        write("Spent " + powerleveling.getBathroomTurns()
                + " turns in the Haunted Bathroom and found " + powerleveling.getBathroomNoncombats()
                + " noncombats" + NEW_LINE);

        int coconut = 0;
        int umbrella = 0;
        int cube = 0;
        for (final Item i : logData.getLogSummary().getDroppedItems())
            if (i.getName().equals("coconut shell"))
                coconut = i.getAmount();
            else if (i.getName().equals("little paper umbrella"))
                umbrella = i.getAmount();
            else if (i.getName().equals("magical ice cubes"))
                cube = i.getAmount();
        write("Garnishes received: " + (coconut + umbrella + cube) + NEW_LINE);
        write("     Coconuts: " + coconut + NEW_LINE);
        write("     Umbrellas: " + umbrella + NEW_LINE);
        write("     Ice Cubes: " + cube + NEW_LINE);

        write("Number of lost combats: " + lostCombats.size() + NEW_LINE);
        for (final DataNumberPair<String> dnp : lostCombats)
            write("     " + dnp + NEW_LINE);
        write(NEW_LINE);
    }

    private void write(
            final String s) {
        if (s != null)
            log.append(s);
        else
            log.append(UsefulPatterns.EMPTY_STRING);
    }

    private void write(
            final int i) {
        log.append(i);
    }
}
