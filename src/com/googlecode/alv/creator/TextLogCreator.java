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

package com.googlecode.alv.creator;

import static com.googlecode.alv.parser.UsefulPatterns.NON_ASCII;

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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import com.googlecode.alv.Settings;
import com.googlecode.alv.creator.util.SpookyravenPowerleveling;
import com.googlecode.alv.creator.util.StatgiverItem;
import com.googlecode.alv.logData.CombatItem;
import com.googlecode.alv.logData.Item;
import com.googlecode.alv.logData.LogDataHolder;
import com.googlecode.alv.logData.MPGain;
import com.googlecode.alv.logData.MeatGain;
import com.googlecode.alv.logData.Skill;
import com.googlecode.alv.logData.Statgain;
import com.googlecode.alv.logData.LogDataHolder.AscensionPath;
import com.googlecode.alv.logData.LogDataHolder.StatClass;
import com.googlecode.alv.logData.consumables.Consumable;
import com.googlecode.alv.logData.consumables.Consumable.ConsumableVersion;
import com.googlecode.alv.logData.logSummary.AreaStatgains;
import com.googlecode.alv.logData.logSummary.LevelData;
import com.googlecode.alv.logData.logSummary.QuestTurncounts;
import com.googlecode.alv.logData.turn.DetailedTurnInterval;
import com.googlecode.alv.logData.turn.Encounter;
import com.googlecode.alv.logData.turn.FreeRunaways;
import com.googlecode.alv.logData.turn.SingleTurn;
import com.googlecode.alv.logData.turn.TurnInterval;
import com.googlecode.alv.logData.turn.TurnVersion;
import com.googlecode.alv.logData.turn.turnAction.DayChange;
import com.googlecode.alv.logData.turn.turnAction.FamiliarChange;
import com.googlecode.alv.logData.turn.turnAction.PlayerSnapshot;
import com.googlecode.alv.logData.turn.turnAction.Pull;
import com.googlecode.alv.parser.UsefulPatterns;
import com.googlecode.alv.util.DataCounter;
import com.googlecode.alv.util.DataNumberPair;
import com.googlecode.alv.util.Lists;
import com.googlecode.alv.util.LogOutputFormat;
import com.googlecode.alv.util.Maps;
import com.googlecode.alv.util.Pair;
import com.googlecode.alv.util.Sets;
import com.googlecode.alv.util.dataTables.DataTablesHandler;
import com.googlecode.alv.util.dataTables.ExtraStats;

import java.util.Set;

import net.java.dev.spellcast.utilities.DataUtilities;
import net.java.dev.spellcast.utilities.UtilityConstants;

/**
 * This utility class creates a parsed plaintext ascension log from a
 * {@link LogDataHolder}. The format of the parsed log is similar to the one
 * which the AFH parser uses.  It is now designed to be subclassed so that its
 * code can be used to write other formats, just by overriding certain methods.
 * <p>
 * Note that this class only works with detailed LogDataHolders (see
 * {@link LogDataHolder#isDetailedLog()}), because non-detailed LogDataHolders
 * do not contain enough data.
 */
public class TextLogCreator {
    protected static final String NEW_LINE = System.getProperty("line.separator");

    protected static final String COMMA = ", ";

    protected static final String OPENING_TURN_BRACKET = " [";

    protected static final String CLOSING_TURN_BRACKET = "] ";

    // Parsed log prefixes denoting special events
    protected static final String ITEM_PREFIX               = "     +>";
    protected static final String CONSUMABLE_PREFIX         = "     o> ";
    protected static final String PULL_PREFIX               = "     #> Turn";
    protected static final String LEARN_SKILL_PREFIX        = "     @>";
    protected static final String LEVEL_CHANGE_PREFIX       = "     => Level ";
    protected static final String BANISHED_COMBAT_PREFIX    = "     b>";
    protected static final String HUNTED_COMBAT_PREFIX      = "     *>";
    protected static final String DISINTEGRATED_COMBAT_PREFIX="     }>";
    protected static final String SEMIRARE_PREFIX           = "     #>";
    protected static final String BAD_MOON_PREFIX           = "     %>";
    protected static final String FREE_RUNAWAYS_PREFIX      = "     &> ";
    protected static final String HYBRIDIZE_PREFIX          = "     h> ";
    protected static final String FAMILIAR_CHANGE_PREFIX    = "     -> Turn";

    protected static final String ITEM_MIDDLE_STRING = "Got ";

    protected static final String BANISHED_COMBAT_DESC = "Banished ";

    protected static final String HUNTED_COMBAT_MIDDLE_STRING = "Started hunting ";

    protected static final String DISINTEGRATED_COMBAT_MIDDLE_STRING = "Disintegrated ";

    protected static final String SEMIRARE_MIDDLE_STRING = "Semirare: ";

    protected static final String BAD_MOON_MIDDLE_STRING = "Badmoon: ";

    protected static final String ADVENTURES_LEFT_STRING = "Adventure count at day start: ";

    protected static final String CURRENT_MEAT_STRING = "Current meat: ";

    protected static final String CHATEAU_REST_AREA = "Rest in your bed in the Chateau";

    protected static final String SMITH_STRING = "smith ";

    protected static final String MIX_STRING = "mix ";

    protected static final String COOK_STRING = "cook ";

    protected static final DayChange NO_DAY_CHANGE = new DayChange(Integer.MAX_VALUE,
            Integer.MAX_VALUE);

    protected Map<String, String> logAdditionsMap;

    protected final Set<String> localeOnetimeItemsSet = Sets.newHashSet(300);

    protected final StringBuilder log;

    protected final Iterator<FamiliarChange> familiarChangeIter;

    protected FamiliarChange currentFamChange;

    protected final Iterator<Pull> pullIter;

    protected Pull currentPull;

    protected final Iterator<LevelData> levelIter;

    protected LevelData nextLevel;

    protected final Iterator<DataNumberPair<String>> huntedCombatIter;

    protected DataNumberPair<String> currentHuntedCombat;

    protected DataNumberPair<String> freeRunsByZone;

    protected final Iterator<DataNumberPair<String>> banishedCombatIter;

    protected final Iterator<DataNumberPair<String>> disintegratedCombatIter;

    protected final Iterator<DataNumberPair<String>> hybridDataIter;

    protected final Iterator<DataNumberPair<String>> learnedSkillIter;

    protected DataNumberPair<String> currentLearnedSkill;

    protected DataNumberPair<String> currentBanishedCombat;

    protected DataNumberPair<String> currentDisintegratedCombat;

    protected DataNumberPair<String> currentHybridData;

    protected boolean isShowNotes = true;

    protected Map<Integer,Integer> dailyKaEarned;

    protected static final String KA_EARNED_DAILY = "Ka earned today: ";

    /**
     * Creates a TextLogCreator instance for further use.
     *
     * @param logData
     *            The ascension log data from which the parsed ascension log
     *            should be created.
     */
    protected TextLogCreator(final LogDataHolder logData) 
    {
        if (logData == null)
            throw new NullPointerException("The LogDataHolder must not be null.");

        // Populate local one-time item set with all one-time items.
        for (final Entry<String, Boolean> item 
                : DataTablesHandler.HANDLER.getItemdropsMap().entrySet())
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

        dailyKaEarned = new HashMap<Integer, Integer>();
        setAugmentationsMap();
    }

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
     * 
     * @param augmentationsFile The name of the augmentations file to use
     * @return Map of augmentation key names to augmentations, read from the file
     */
    protected static Map<String, String> readAugmentationsList(String augmentationsFile) 
    {
        final Map<String, String> map = Maps.newHashMap();
        String tmpLine;

        try ( BufferedReader br = DataUtilities.getReader(UtilityConstants.KOL_DATA_DIRECTORY,
                                                          augmentationsFile)) {
            while ((tmpLine = br.readLine()) != null)
                // Ignore empty lines and comments
                if (tmpLine.length() > 0 && !tmpLine.startsWith("//") && !tmpLine.startsWith("#")) {
                    final String[] split = tmpLine.split("\\s+\\|\\s+");
                    map.put(split[0], split[1].replaceAll("N_L", NEW_LINE));
                }
        } catch (final IOException e) {
            e.printStackTrace();
        }

        return map;
    }

    /**
     * Return the flavor of TextLogCreator appropriate for this class.
     * @param logData LogDataHolder with data to be processed.
     * @return TextLogCreator formed from the log data.
     */
    private static TextLogCreator newTextLogCreator(LogDataHolder logData, LogOutputFormat format)
    {
        switch (format) {
        case TEXT_LOG:
            return new TextLogCreator(logData);
        case HTML_LOG:
            return new HTMLLogCreator(logData);
        case BBCODE_LOG:
            return new BBCodeLogCreator(logData);
        default:
            return null;  //shouldn't happen
        }
    }
    
    /**
     * Creates a list of all turn interval print-outs as they are composed in a
     * turn rundown inside a textual ascension log.
     *
     * @param logData
     *            The ascension log data from which the parsed ascension log
     *            should be created.
     * @param format The format of the log to be written
     * @return The turn rundown list.
     * @throws IllegalArgumentException
     *             if the given log data is not a detailed LogDataHolder, see
     *             {@link LogDataHolder#isDetailedLog()}
     */
    public static List<String> getTurnRundownList(final LogDataHolder logData,
                                                  final LogOutputFormat format) 
    {
        if (!logData.isDetailedLog())
            throw new IllegalArgumentException("Only detailed logs can be used by the TextualLogCreator.");

        final TextLogCreator logCreator = newTextLogCreator(logData, format);
        logCreator.isShowNotes = false;
        return logCreator.createTurnRundownList(logData);
    }

    /**
     * Creates a list of all turn interval print-outs as they are composed in a
     * turn rundown inside a textual ascension log.  The output format is assumed to be 
     * plaintext.
     * 
     * @param logData
     *      The ascension log data from which the parsed ascension log
     *            should be created.
     * @return The turn rundown list.
     */
    public static List<String> getTurnRundownList(final LogDataHolder logData) 
    {
        return getTurnRundownList(logData, LogOutputFormat.TEXT_LOG);
    }
    
    /**
     * Creates a parsed ascension log from the given {@link LogDataHolder} and
     * returns it as a String.
     *
     * @param logData
     *            The ascension log data from which the parsed ascension log
     *            should be created.
     * @param format
     *            The wanted version of the textual log output.
     * @return The textual ascension log.
     * @throws IllegalArgumentException
     *             if the given log data is not a detailed LogDataHolder, see
     *             {@link LogDataHolder#isDetailedLog()}
     */
    public static String getTextualLog(final LogDataHolder logData,
                                       final LogOutputFormat format) 
    {
        // Sometimes, geek jokes are fun! ;)
        int logDate = 404;
        if (UsefulPatterns.USUAL_FORMAT_LOG_NAME.matcher(logData.getLogName()).matches())
            logDate = UsefulPatterns.getLogDate(logData.getLogName());

        return getTextualLog(logData, format, logDate);
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
     * @param format
     *            The wanted version of the textual log output.
     * @return The textual ascension log.
     * @throws IllegalArgumentException
     *             if the given log data is not a detailed LogDataHolder, see
     *             {@link LogDataHolder#isDetailedLog()}
     */
    public static String getTextualLog(final LogDataHolder logData,
                                       final LogOutputFormat format,
                                       final int ascensionStartDate) 
    {
        if (!logData.isDetailedLog())
            throw new IllegalArgumentException("Only detailed logs can be used by the TextualLogCreator.");

        final TextLogCreator logCreator = newTextLogCreator(logData, format);

        final String logOutput;
        if (Settings.getBoolean("Show non-ASCII characters in parsed logs"))
            logOutput = logCreator.createTextLog(logData, ascensionStartDate);
        else
            logOutput = NON_ASCII.matcher(logCreator.createTextLog(logData, ascensionStartDate))
            .replaceAll(UsefulPatterns.EMPTY_STRING);

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
     * @param format The format in which to write the parsed log data.
     * @throws IllegalArgumentException
     *             if saveDest doesn't exist or is a directory; if the given log
     *             data is not a detailed LogDataHolder, see
     *             {@link LogDataHolder#isDetailedLog()}
     * @throws java.io.IOException If an error occurs writing the file
     */
    public static void saveTextualLogToFile(final LogDataHolder logData, 
                                            final File saveDest,
                                            final LogOutputFormat format)
    throws IOException 
    {
        if (!saveDest.exists())
            throw new IllegalArgumentException("The file doesn't exist.");
        if (saveDest.isDirectory())
            throw new IllegalArgumentException("The file is a directory.");

        final PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(saveDest),
                50000));
        writer.print(getTextualLog(logData, format));
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
     * @param format
     *            The wanted version of the textual log output.
     * @throws IllegalArgumentException
     *             if saveDest doesn't exist or is a directory; if the given log
     *             data is not a detailed LogDataHolder, see
     *             {@link LogDataHolder#isDetailedLog()}
     * @throws java.io.IOException If an error occurs writing the file
     */
    public static void saveTextualLogToFile(final LogDataHolder logData,
                                            final int ascensionStartDate, 
                                            final File saveDest,
                                            final LogOutputFormat format)
    throws IOException 
    {
        if (!saveDest.exists())
            throw new IllegalArgumentException("The file doesn't exist.");
        if (saveDest.isDirectory())
            throw new IllegalArgumentException("The file is a directory.");

        final PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(saveDest),
                50000));
        writer.print(getTextualLog(logData, format, ascensionStartDate));
        writer.close();
    }

    /**
     * Reads in the augmentations for this parsed log format.  This should be
     * overridden by subclasses.
     */
    protected void setAugmentationsMap()
    {
        logAdditionsMap = Collections.unmodifiableMap(readAugmentationsList("textAugmentations.txt"));
    }
    
    /**
     * Creates a parsed ascension log in a style similar to the format used by
     * the AFH parser.
     *
     * @param logData
     *            The LogDataHolder from which the ascension log should be
     *            created.
     * @return A list of Strings to be printed.
     */
    protected List<String> createTurnRundownList(final LogDataHolder logData) 
    {
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
            // If the current turn interval's end turn spans a day boundary
            if (!nextDay.equals(NO_DAY_CHANGE) && ti.getEndTurn() >= nextDay.getTurnNumber())

                //If the current turn interval ends on a day boundary
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
                    // Validate that start of the interval is in the previous day
                    // Split the interval into two pieces one for each day.
                    // This handles adventuring in the same area at the end of one day
                    // and the start of the next.
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
                    // New turn interval area at the start of the next day
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
     * @return A list of Strings to be printed.
     */
    protected String createTextLog(final LogDataHolder logData, 
                                   final int ascensionStartDate) 
    {
        beginTextLog();
        
        currentFamChange = familiarChangeIter.hasNext() ? familiarChangeIter.next() : null;
        currentPull = pullIter.hasNext() ? pullIter.next() : null;
        currentHuntedCombat = huntedCombatIter.hasNext() ? huntedCombatIter.next() : null;
        currentDisintegratedCombat 
            = disintegratedCombatIter.hasNext() ? disintegratedCombatIter.next() : null;
        currentBanishedCombat = banishedCombatIter.hasNext() ? banishedCombatIter.next() : null;
        currentHybridData = hybridDataIter.hasNext() ? hybridDataIter.next() : null;
        currentLearnedSkill = learnedSkillIter.hasNext() ? learnedSkillIter.next() : null;

        // Level 1 can be skipped.
        levelIter.next();
        nextLevel = levelIter.hasNext() ? levelIter.next() : null;

        final Iterator<DayChange> dayChangeIter = logData.getDayChanges().iterator();
        DayChange currentDay = dayChangeIter.next();
        DayChange nextDay = dayChangeIter.hasNext() ? dayChangeIter.next() : NO_DAY_CHANGE;

        printTitle(logData, ascensionStartDate);
        write(logAdditionsMap.get("logHeaderStart"));
        writeln("This log was created by the Ascension Log Visualizer " + Settings.ALV_VERSION + ".");
        writeln("The basic idea and the format of this parser have been borrowed from the AFH MafiaLog Parser by VladimirPootin and QuantumNightmare.");
        writeEndLine();
        write(logAdditionsMap.get("logHeaderEnd"));
        printTableOfContents(logData);
        printDayChange(currentDay);
        writeln();
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
                } else if (ti.getStartTurn() >= nextDay.getTurnNumber()) {
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

        // Log daily ka at end of run
        printDailyKa(logData, currentDay.getDayNumber());

        printNotes(logData.getHeaderFooterComment(currentDay).getFooterComments());
        writeEndLine();
        write("Turn rundown finished!");
        write(logAdditionsMap.get("turnRundownEnd"));
        writeEndLine();
        writeEndLine();
        
        printLogSummaries(logData);
        
        endTextLog();

        return log.toString();
    }

    /**
     * Print the daily report of Ka earned.
     * 
     * @param logData LogDataHolder containing the data.
     * @param day Number of the day.
     */
    protected void printDailyKa(final LogDataHolder logData, int day)
    {
        // Print out Ka acquisition for the day
        if (logData.getAscensionPath() == AscensionPath.ED)
        {
            int kaAcquired = 0;

            if (dailyKaEarned.containsKey(day)) {
                kaAcquired = dailyKaEarned.get(day);
            }

            writeln();
            write(KA_EARNED_DAILY);
            writeln(kaAcquired);
        }
        return;
    }

    /**
     * Print text that always appears at the beginning of the text log.
     */
    protected void beginTextLog()
    {
        // Do nothing for plain text
    }
    
    /**
     * Print the title of the parsed log.
     * 
     * @param logData LogDataHolder containing the data.
     * @param ascensionStartDate Number in yyyymmdd format representing the start date.
     */
    protected void printTitle(final LogDataHolder logData, final int ascensionStartDate)
    {
        // Add the log file header.
        writeln("NEW " + logData.getCharacterClass() + " " + logData.getGameMode() + " "
                + logData.getAscensionPath() + " ASCENSION STARTED " + ascensionStartDate);
        writeln("------------------------------");
        writeln();
    }
    
    /**
     * Print the table of contents for this log output format.
     * 
     * @param logData  LogDataHolder containing the data.
     */
    protected void printTableOfContents(LogDataHolder logData)
    {
        // Not used by plain text format
    }
    
    /**
     * Print the log format's section header.
     * 
     * @param title Title of the section.
     * @param anchor Name of the anchor to be reference by a table of contents.
     */
    protected void printSectionHeader(String title, String anchor)
    {
        // anchor parameter not used by plain text format
        writeln(title);
        writeln("----------");
    }
    
    /**
     * Print the change of the day.
     * 
     * @param nextDay DayChange representing the next day.
     */
    protected void printDayChange(DayChange nextDay) 
    {
        write(logAdditionsMap.get("dayChangeLineStart"));
        write(nextDay.toString());
        writeln(logAdditionsMap.get("dayChangeLineEnd"));
    }
    
    /**
     * Print the log output format's end-of-paragraph character sequence.
     * For example, for HTML it would be <code>&lt;p&gt;</code>.
     */
    protected void printParagraphStart()
    {
        // Does nothing for text files
    }
    
    /**
     * Print the log output format's end-of-paragraph character sequence.
     * For example, for HTML it would be <code>&lt;/p&gt;</code>.
     */
    protected void printParagraphEnd()
    {
        // Does nothing for text files
    }
    
    /**
     * Print the log output format's line break character sequence.  For example,
     * for HTML it would be <code>&lt;br&gt;</code>.
     */
    protected void printLineBreak()
    {
        // Does nothing for text files
    }
    
    /**
     * Print the beginning of a table.
     */
    protected void printTableStart()
    {
        // Does nothing for text files
    }
    
    /**
     * Print the given strings as elements of a row of a table.
     * 
     * @param strings Strings to add to the table.  Each string is a
     *      separate table data entry.
     */
    protected void printTableRow(String ...strings)
    {
        // For plain text, this just writes the given strings in one line
        for (String s : strings) 
            write(s);
        writeln();
    }
    
    /**
     * Print the end of a table.
     */
    protected void printTableEnd()
    {
        // Does nothing for text files
    }
    
    /**
     * Print text that always appears at the end of the text log.
     */
    protected void endTextLog()
    {
        // Do nothing for plain text
    }
    
    /**
     * Prints all day changes that occurred and returns the new current day
     * number and the next day change. If no day change occurred, the old values
     * will be returned.
     * 
     * @param logData LogDataHolder collecting all the ascension data.
     * @param currentTurnNumber Number of the current turn.
     * @param currentDay Day for which to print changes.
     * @param nextDay Day against which the older day is compared.
     * @param dayChangeIter Iterator which traverses the days between the current day
     *      and the next.
     * @return Pair composed of the two given days, with the older day incremented
     *      to the newer.
     */
    protected Pair<DayChange, DayChange> printDayChanges(final LogDataHolder logData,
                                                         final int currentTurnNumber,
                                                         DayChange currentDay, 
                                                         DayChange nextDay,
                                                         final Iterator<DayChange> dayChangeIter) 
    {
        while (!nextDay.equals(NO_DAY_CHANGE) && currentTurnNumber >= nextDay.getTurnNumber()) {
            final PlayerSnapshot currentSnapshot = logData.getFirstPlayerSnapshotAfterTurn(nextDay.getTurnNumber());

            printDailyKa(logData, currentDay.getDayNumber());

            if (logData.getHeaderFooterComment(currentDay) != null)
                printNotes(logData.getHeaderFooterComment(currentDay).getFooterComments());

            writeln();
            printDayChange(nextDay);
            
            if (currentSnapshot != null) {
                write(ADVENTURES_LEFT_STRING);
                writeln(currentSnapshot.getAdventuresLeft());
                write(CURRENT_MEAT_STRING);
                write(currentSnapshot.getCurrentMeat());
                writeln();
            }
            writeln();

            if (logData.getHeaderFooterComment(nextDay) != null)
                printNotes(logData.getHeaderFooterComment(nextDay).getHeaderComments());

            currentDay = nextDay;
            nextDay = dayChangeIter.hasNext() ? dayChangeIter.next() : NO_DAY_CHANGE;
        }

        return Pair.of(currentDay, nextDay);
    }

    /**
     * Prints all pulls from the given day up to the given turn number.
     * 
     * @param currentDayNumber Number of the day up to which to print pulls.
     * @param currentTurnNumber Turn number up to which to print pulls.
     */
    protected void printCurrentPulls(final int currentDayNumber, final int currentTurnNumber) 
    {
        while (currentPull != null && currentTurnNumber >= currentPull.getTurnNumber()) {
            // Only pulls of the current day should be added here.
            if (currentPull.getDayNumber() > currentDayNumber)
                break;

            printLineBreak();
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
            writeln(logAdditionsMap.get("pullEnd"));

            currentPull = pullIter.hasNext() ? pullIter.next() : null;
        }
    }

    /**
     * Prints all consumables from the given day.
     * 
     * @param consumables Collection of consumables to print.
     * @param currentDayNumber Number of day on which these consumables were consumed.
     */
    protected void printCurrentConsumables(final Collection<Consumable> consumables,
                                           final int currentDayNumber) 
    {
        for (final Consumable c : consumables)
            if (c.getDayNumberOfUsage() == currentDayNumber)
                if (c.getAdventureGain() > 0 || !c.getStatGain().isAllStatsZero()
                        || UsefulPatterns.SPECIAL_CONSUMABLES.contains(c.getName())) {
                    printLineBreak();
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
                    writeln(logAdditionsMap.get("statgainEnd"));
                }
    }

    /**
     * Prints the given notes. If the interval contains no notes or notes are
     * not supposed to be printed, this method won't print anything.
     * 
     * @param notes The notes to print out.
     */
    protected void printNotes(final String notes) 
    {
        if (isShowNotes && notes.length() > 0) {
            write(logAdditionsMap.get("notesStart"));
            write(notes.replaceAll("\r\n", NEW_LINE).replaceAll("[\r\n]", NEW_LINE));
            writeln(logAdditionsMap.get("notesEnd"));
        }
    }

    /**
     * Print the items acquired in a given turn.
     * 
     * @param turnNumber Number of the turn
     */
    protected void printItemAcquisitionStartString(final int turnNumber) 
    {
        printLineBreak();
        write(ITEM_PREFIX);
        write(OPENING_TURN_BRACKET);
        write(turnNumber);
        write(CLOSING_TURN_BRACKET);
        write(ITEM_MIDDLE_STRING);
    }

    /**
     * Print the details for adventures that happened in the given day number.
     * 
     * @param ti
     *      The turn interval whose contents should be printed.
     * @param currentDayNumber
     *      The number of the day in which this interval occurs.
     */
    protected void printTurnIntervalContents(final TurnInterval ti, final int currentDayNumber) 
    {
        printParagraphStart();
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

        // Report Ka acquisition
        // TODO: Store this in the turn interval itself
        int kaAcquired = 0;
        for (final SingleTurn st : ti.getTurns())
        {
            for (final Item i : st.getDroppedItems())
            {
                if (i.getName().equals("Ka coin"))
                {
                    kaAcquired += i.getAmount();
                }
                if (i.getName().equals("Ka coin (2)"))
                {
                    kaAcquired += 2 * i.getAmount();
                }

                if (i.getName().equals("Ka coin (3)"))
                {
                    kaAcquired += 3 * i.getAmount();
                }
            }
        }

        if (kaAcquired > 0)
        {
            write(UsefulPatterns.WHITE_SPACE);
            write(UsefulPatterns.ROUND_BRACKET_OPEN);
            write("Ka: ");
            write(kaAcquired);
            write(UsefulPatterns.ROUND_BRACKET_CLOSE);

            // Store ka for daily total
            int currentDailyKa = 0;

            if (dailyKaEarned.containsKey(currentDayNumber))
            {
                currentDailyKa = dailyKaEarned.get(currentDayNumber);
            }

            currentDailyKa += kaAcquired;

            dailyKaEarned.put(currentDayNumber, currentDailyKa);
        }

        writeln();

        for (final SingleTurn st : ti.getTurns()) {
            if (DataTablesHandler.HANDLER.isSemirareEncounter(st)) {
                printLineBreak();
                write(SEMIRARE_PREFIX);
                write(OPENING_TURN_BRACKET);
                write(st.getTurnNumber());
                write(CLOSING_TURN_BRACKET);
                write(SEMIRARE_MIDDLE_STRING);
                write(logAdditionsMap.get("specialEncounterStart"));
                write(st.getEncounterName());
                writeln(logAdditionsMap.get("specialEncounterEnd"));
            }
            if (DataTablesHandler.HANDLER.isBadMoonEncounter(st)) {
                printLineBreak();
                write(BAD_MOON_PREFIX);
                write(OPENING_TURN_BRACKET);
                write(st.getTurnNumber());
                write(CLOSING_TURN_BRACKET);
                write(BAD_MOON_MIDDLE_STRING);
                write(logAdditionsMap.get("specialEncounterStart"));
                write(st.getEncounterName());
                writeln(logAdditionsMap.get("specialEncounterEnd"));
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
                        printLineBreak();
                        write(FREE_RUNAWAYS_PREFIX);
                        write(UsefulPatterns.SQUARE_BRACKET_OPEN);
                        write(st.getTurnNumber());
                        write(UsefulPatterns.SQUARE_BRACKET_CLOSE);
                        write(UsefulPatterns.WHITE_SPACE);
                        write(e.getAreaName());
                        write(UsefulPatterns.WHITE_SPACE);
                        write(logAdditionsMap.get("statgainStart"));
                        write(e.getStatGain().toString());
                        writeln(logAdditionsMap.get("statgainEnd"));
                    }

                    // Log turn-free crafting as well

                    if (e.getAreaName().toLowerCase().startsWith(MIX_STRING) ||
                            e.getAreaName().toLowerCase().startsWith(SMITH_STRING) ||
                            e.getAreaName().toLowerCase().startsWith(COOK_STRING))
                    {
                        // Log it using the free runaway prefix
                        printLineBreak();
                        write(FREE_RUNAWAYS_PREFIX);
                        write(UsefulPatterns.SQUARE_BRACKET_OPEN);
                        write(st.getTurnNumber());
                        write(UsefulPatterns.SQUARE_BRACKET_CLOSE);
                        write(UsefulPatterns.WHITE_SPACE);
                        writeln(e.getAreaName());
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
                            writeln();
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
                                writeln();
                                printItemAcquisitionStartString(st.getTurnNumber());
                                itemCounter = 0;
                            } else if (i > 1)
                                write(COMMA);
                        }
                    }

                    if (aquiredItemsIter.hasNext() && itemCounter != 0)
                        write(COMMA);

                }

                writeln();
            }
        }

        printCurrentConsumables(ti.getConsumablesUsed(), currentDayNumber);

        printCurrentPulls(currentDayNumber, ti.getEndTurn());

        while (currentHybridData != null && ti.getEndTurn() >= currentHybridData.getNumber()) {
            printLineBreak();
            write(HYBRIDIZE_PREFIX);
            write(UsefulPatterns.SQUARE_BRACKET_OPEN);
            write(currentHybridData.getNumber());
            write(UsefulPatterns.SQUARE_BRACKET_CLOSE);
            write(UsefulPatterns.WHITE_SPACE);
            writeln(currentHybridData.getData());

            currentHybridData = hybridDataIter.hasNext() ? hybridDataIter.next() : null;
        }

        while (currentHuntedCombat != null && ti.getEndTurn() >= currentHuntedCombat.getNumber()) {
            printLineBreak();
            write(HUNTED_COMBAT_PREFIX);
            write(OPENING_TURN_BRACKET);
            write(currentHuntedCombat.getNumber());
            write(CLOSING_TURN_BRACKET);
            write(HUNTED_COMBAT_MIDDLE_STRING);
            write(logAdditionsMap.get("huntedStart"));
            write(currentHuntedCombat.getData());
            writeln(logAdditionsMap.get("huntedEnd"));

            currentHuntedCombat = huntedCombatIter.hasNext() ? huntedCombatIter.next() : null;
        }

        while (currentBanishedCombat != null && ti.getEndTurn() >= currentBanishedCombat.getNumber()) {
            printLineBreak();
            write(BANISHED_COMBAT_PREFIX);
            write(UsefulPatterns.WHITE_SPACE);
            write(UsefulPatterns.SQUARE_BRACKET_OPEN);
            write(currentBanishedCombat.getNumber());
            write(UsefulPatterns.SQUARE_BRACKET_CLOSE);
            write(UsefulPatterns.WHITE_SPACE);
            writeln(BANISHED_COMBAT_DESC + currentBanishedCombat.getData());

            currentBanishedCombat = banishedCombatIter.hasNext() ? banishedCombatIter.next() : null;
        }

        while (currentDisintegratedCombat != null
                && ti.getEndTurn() >= currentDisintegratedCombat.getNumber()) {
            printLineBreak();
            write(DISINTEGRATED_COMBAT_PREFIX);
            write(OPENING_TURN_BRACKET);
            write(currentDisintegratedCombat.getNumber());
            write(CLOSING_TURN_BRACKET);
            write(DISINTEGRATED_COMBAT_MIDDLE_STRING);
            write(logAdditionsMap.get("yellowRayStart"));
            write(currentDisintegratedCombat.getData());
            writeln(logAdditionsMap.get("yellowRayEnd"));

            currentDisintegratedCombat = disintegratedCombatIter.hasNext() ? disintegratedCombatIter.next()
                    : null;
        }

        while (currentFamChange != null && ti.getEndTurn() >= currentFamChange.getTurnNumber()) {
            printLineBreak();
            write(FAMILIAR_CHANGE_PREFIX);
            write(OPENING_TURN_BRACKET);
            write(currentFamChange.getTurnNumber());
            write(CLOSING_TURN_BRACKET);
            write(logAdditionsMap.get("familiarStart"));
            write(currentFamChange.getFamiliarName());
            writeln(logAdditionsMap.get("familiarEnd"));

            currentFamChange = familiarChangeIter.hasNext() ? familiarChangeIter.next() : null;
        }

        final FreeRunaways freeRunaways = ti.getRunawayAttempts();
        if (freeRunaways.getNumberOfAttemptedRunaways() > 0) {
            printLineBreak();
            write(FREE_RUNAWAYS_PREFIX);
            write(logAdditionsMap.get("runawayStart"));
            write(freeRunaways.toString());
            writeln(logAdditionsMap.get("runawayEnd"));
        }

        while (currentLearnedSkill != null && ti.getEndTurn() >= currentLearnedSkill.getNumber()) {
            printLineBreak();
            write(LEARN_SKILL_PREFIX);
            write(UsefulPatterns.WHITE_SPACE);
            writeln("Learned: " + currentLearnedSkill.getData() + " (Turn " + currentLearnedSkill.getNumber() + ")");

            currentLearnedSkill = learnedSkillIter.hasNext() ? learnedSkillIter.next() : null;
        }

        while (nextLevel != null && ti.getEndTurn() >= nextLevel.getLevelReachedOnTurn()) {
            // Only print the level if it actually *is* part of this turn
            // interval.
            if (ti.getStartTurn() <= nextLevel.getLevelReachedOnTurn()) {
                final int musStat = (int) Math.sqrt(nextLevel.getStatsAtLevelReached().mus);
                final int mystStat = (int) Math.sqrt(nextLevel.getStatsAtLevelReached().myst);
                final int moxStat = (int) Math.sqrt(nextLevel.getStatsAtLevelReached().mox);

                printLineBreak();
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
                writeln(logAdditionsMap.get("levelEnd"));
            }

            nextLevel = levelIter.hasNext() ? levelIter.next() : null;
        }

        printNotes(ti.getPostIntervalComment().getComments());
        printParagraphEnd();
    }

    /**
     * Print the log aummary sections.
     * 
     * @param logData LogDataHolder holding all the log data
     */
    protected void printLogSummaries(final LogDataHolder logData) 
    {
        printAdventuresSection(logData);
        printQuestsSection(logData);
        printPullsSection(logData);
        printLevelsSection(logData);
        printStatsSection(logData);
        printStatBreakdownSection(logData);
        printSkillsLearnedSection(logData);
        printFamiliarSection(logData);
        printSemirareSection(logData);
        printTrackedItemSection(logData);
        printHybridSection(logData);
        printHuntedCombatsSection(logData);
        printBanishedCombatsSection(logData);
        printDisintegratedCombatsSection(logData);
        printCopiedCombatsSection(logData);
        printFreeRunawaySection(logData);
        printWanderingEncountersSection(logData);
        printCombatItemsSection(logData);
        printCastsSection(logData);
        printMPSection(logData);
        printConsumablesSection(logData);
        printMeatSection(logData);
        printBottlenecksSection(logData);
    }

    /**
     * Print the adventure summary section.
     * 
     * @param logData LogDataHolder holding all the log data
     */
    protected void printAdventuresSection(LogDataHolder logData) 
    {
        // Turns spent per area summary
        printSectionHeader("ADVENTURES", "adventures");

        for (final DataNumberPair<String> dn : logData.getLogSummary().getTurnsPerArea()) {
            write(dn.getData());
            write(": ");
            writeln(dn.getNumber());
            printLineBreak();
        }
        writeln();
        writeln();
        writeln();
    }
    
    /**
     * Print the quests summary section.
     * 
     * @param logData LogDataHolder holding all the log data
     */
    protected void printQuestsSection(LogDataHolder logData) 
    {
        // Quest Turns summary
        printSectionHeader("QUEST TURNS", "questturns");
        QuestTurncounts questTurncounts = logData.getLogSummary().getQuestTurncounts();
        writelnWithBreak("Spooky Forest: " + questTurncounts.templeOpeningTurns);
        writelnWithBreak("Tavern quest: " + questTurncounts.tavernQuestTurns);
        writelnWithBreak("Bat quest: " + questTurncounts.batQuestTurns);
        writelnWithBreak("Cobb's Knob quest: " + questTurncounts.knobQuestTurns);
        writelnWithBreak("Friars' quest: " + questTurncounts.friarsQuestTurns);
        writelnWithBreak("Pandamonium quest: " + questTurncounts.pandamoniumQuestTurns);
        writelnWithBreak("Defiled Cyrpt quest: " + questTurncounts.cyrptQuestTurns);
        writelnWithBreak("Trapzor quest: " + questTurncounts.trapzorQuestTurns);
        writelnWithBreak("Orc Chasm quest: " + questTurncounts.chasmQuestTurns);
        writelnWithBreak("Airship: " + questTurncounts.airshipQuestTurns);
        writelnWithBreak("Giant's Castle: " + questTurncounts.castleQuestTurns);
        writelnWithBreak("Pirate quest: " + questTurncounts.pirateQuestTurns);
        writelnWithBreak("Copperhead Club: " + questTurncounts.copperheadClubTurns);
        writelnWithBreak("Red Zeppelin: " + questTurncounts.redZeppelinTurns);
        writelnWithBreak("Black Forest quest: " + questTurncounts.blackForrestQuestTurns);
        writelnWithBreak("Desert Oasis quest: " + questTurncounts.desertOasisQuestTurns);
        writelnWithBreak("Spookyraven First Floor: " + questTurncounts.spookyravenFirstFloor);
        writelnWithBreak("Spookyraven Second Floor: " + questTurncounts.spookyravenSecondFloor);
        writelnWithBreak("Spookyraven Cellar: " + questTurncounts.spookyravenQuestTurns);
        writelnWithBreak("Hidden City quest: " + questTurncounts.templeCityQuestTurns);
        writelnWithBreak("Palindome quest: " + questTurncounts.palindomeQuestTurns);
        writelnWithBreak("Pyramid quest: " + questTurncounts.pyramidQuestTurns);
        writelnWithBreak("Starting the War: " + questTurncounts.warIslandOpeningTurns);
        writelnWithBreak("War Island quest: " + questTurncounts.warIslandQuestTurns);
        writelnWithBreak("DoD quest: " + questTurncounts.dodQuestTurns);
        writelnWithBreak("Daily Dungeon: " + questTurncounts.dailyDungeonTurns);
        writeln();
        writeln();
        writeln();
    }

    /**
     * Print the pull summary section.
     * 
     * @param logData LogDataHolder holding all the log data
     */
    protected void printPullsSection(LogDataHolder logData) 
    {
        // Pulls summary
        printSectionHeader("PULLS", "pulls");
        final DataCounter<String> pullsCounter 
            = new DataCounter<String>((int) (logData.getPulls().size() * 1.4) + 1);
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
            writelnWithBreak(dn.getData());
        }
        writeln();
        writeln();
        writeln();
    }

    /**
     * Print the level summary section.
     * 
     * @param logData LogDataHolder holding all the log data
     */
    protected void printLevelsSection(LogDataHolder logData) 
    {
        // Level summary
        printSectionHeader("LEVELS", "levels");
        final NumberFormat formatter = NumberFormat.getNumberInstance(Locale.ENGLISH);
        formatter.setMaximumFractionDigits(1);
        formatter.setMinimumFractionDigits(1);
        LevelData lastLevel = null;
        for (final LevelData ld : logData.getLevels()) {
            final int turnDifference 
                = lastLevel == null ? 0
                                    : ld.getLevelReachedOnTurn() - lastLevel.getLevelReachedOnTurn();
            final double statsPerTurn = lastLevel != null ? lastLevel.getStatGainPerTurn() : 0;
            final int combatTurns = lastLevel != null ? lastLevel.getCombatTurns() : 0;
            final int noncombatTurns = lastLevel != null ? lastLevel.getNoncombatTurns() : 0;
            final int otherTurns = lastLevel != null ? lastLevel.getOtherTurns() : 0;
    
            printParagraphStart();
            write(ld.toString());
            write(COMMA);
            write(turnDifference);
            write(" from last level. (");
            write(formatter.format(statsPerTurn));
            writelnWithBreak(" substats / turn)");
    
            write("   Combats: ");
            writelnWithBreak(Integer.toString(combatTurns));
            write("   Noncombats: ");
            writelnWithBreak(Integer.toString(noncombatTurns));
            write("   Other: ");
            writelnWithBreak(Integer.toString(otherTurns));
            printParagraphEnd();
    
            lastLevel = ld;
        }
        writelnWithBreak();
        writelnWithBreak();
        final int totalTurns = logData.getLastTurnSpent().getTurnNumber();
        writelnWithBreak("Total COMBATS: " + logData.getLogSummary().getTotalTurnsCombat() + " ("
                + Math.round(logData.getLogSummary().getTotalTurnsCombat() * 1000.0 / totalTurns)
                / 10.0 + "%)" );
        writelnWithBreak("Total NONCOMBATS: " + logData.getLogSummary().getTotalTurnsNoncombat() + " ("
                + Math.round(logData.getLogSummary().getTotalTurnsNoncombat() * 1000.0 / totalTurns)
                / 10.0 + "%)");
        writelnWithBreak("Total OTHER: " + logData.getLogSummary().getTotalTurnsOther() + " ("
                + Math.round(logData.getLogSummary().getTotalTurnsOther() * 1000.0 / totalTurns)
                / 10.0 + "%)");
        writeln();
        writeln();
        writeln();
    }

    /**
     * Print the stat gain summary section.
     * 
     * @param logData LogDataHolder holding all the log data
     */
    protected void printStatsSection(LogDataHolder logData) 
    {
        // Stats summary
        printSectionHeader("STATS", "stats");
        final Statgain totalStats = logData.getLogSummary().getTotalStatgains();
        final Statgain combatStats = logData.getLogSummary().getCombatsStatgains();
        final Statgain noncombatStats = logData.getLogSummary().getNoncombatsStatgains();
        final Statgain otherStats = logData.getLogSummary().getOthersStatgains();
        final Statgain foodStats = logData.getLogSummary().getFoodConsumablesStatgains();
        final Statgain boozeStats = logData.getLogSummary().getBoozeConsumablesStatgains();
        final Statgain usingStats = logData.getLogSummary().getUsedConsumablesStatgains();
        printTableStart();
        printTableRow("           ", "\tMuscle", "\tMyst", "\tMoxie");
        printTableRow("Totals:   ", "\t" + totalStats.mus, "\t" + totalStats.myst, "\t" + totalStats.mox);
        printTableRow("Combats:", "\t" + combatStats.mus, "\t" + combatStats.myst, "\t" + combatStats.mox);
        printTableRow("Noncombats:", "\t" + noncombatStats.mus, "\t" + noncombatStats.myst, 
                      "\t" + noncombatStats.mox);
        printTableRow("Others:   ", "\t" + otherStats.mus, "\t" + otherStats.myst, "\t" + otherStats.mox);
        printTableRow("Eating:   ", "\t" + foodStats.mus, "\t" + foodStats.myst, "\t" + foodStats.mox);
        printTableRow("Drinking:", "\t" + boozeStats.mus, "\t" + boozeStats.myst, "\t" + boozeStats.mox);
        printTableRow("Using:   ", "\t" + usingStats.mus, "\t" + usingStats.myst, "\t" + usingStats.mox);
        printTableEnd();
        writelnWithBreak();
        writelnWithBreak();
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
        writelnWithBreak("Top 10 mainstat gaining areas:");
        writelnWithBreak();
        printTableStart();
        for (int i = 0; i < areas.size() && i < 10; i++) {
            String[] data = areas.get(i).toString().split("\t");
            for (int j = 1; j < data.length; j++)
                data[j] = "\t" + data[j];
            printTableRow(data);
        }
        printTableEnd();
        writeln();
        writeln();
        writeln();

    }

    /**
     * Print the stat breakdown summary section.
     * 
     * @param logData LogDataHolder holding all the log data
     */
    protected void printStatBreakdownSection(LogDataHolder logData) 
    {
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

        printSectionHeader("+STAT BREAKDOWN", "statbreakdown");
        printTableStart();
        printTableRow("Need to gain level (last is total):                          ",
                      "\t10", "\t39", "\t105", "\t231", "\t441", "\t759", "\t1209", "\t1815",
                      "\t2601", "\t3591", "\t4809", "\t6279", "\t21904");
        for (final StatgiverItem sgi : statGivers)
            if (sgi.getTotalStats() > 0) {
                String[] data = sgi.toString().split("\t");
                for (int i = 1; i<data.length; i++)
                    data[i] = "\t" + data[i];
                printTableRow(data);
            }
        printTableEnd();
        writeln();
        writeln();
        writeln();
    }

    /**
     * Print the skills learned summary section.
     * 
     * @param logData LogDataHolder holding all the log data
     */
    protected void printSkillsLearnedSection(LogDataHolder logData) 
    {
        if (logData.getLearnedSkills().size() > 0) {
            printSectionHeader("SKILLS LEARNED", "skills");
            for (final DataNumberPair<String> dn : logData.getLearnedSkills()) {
                write(dn.getNumber());
                write(" : ");
                writelnWithBreak(dn.getData());
            }
            writeln();
            writeln();
            writeln();
        }
    }
    
    /**
     * Print the familiar usage summary section.
     * 
     * @param logData LogDataHolder holding all the log data
     */
    protected void printFamiliarSection(LogDataHolder logData) 
    {
        // Familiars summary
        printSectionHeader("FAMILIARS", "familiars");
        for (final DataNumberPair<String> dn : logData.getLogSummary().getFamiliarUsage()) {
            write(dn.getData());
            write(" : ");
            write(dn.getNumber());
            write(" combat turns (");
            write(String.valueOf(Math.round(dn.getNumber() * 1000.0
                    / logData.getLogSummary().getTotalTurnsCombat()) / 10.0));
            writelnWithBreak("%)");
        }
        writeln();
        writeln();
        writeln();
    }

    /**
     * Print the semirare adventure summary section.
     * 
     * @param logData LogDataHolder holding all the log data
     */
    protected void printSemirareSection(LogDataHolder logData)
    {
        // Semi-rares summary
        printSectionHeader("SEMI-RARES", "semirares");
        for (final DataNumberPair<String> dn : logData.getLogSummary().getSemirares()) {
            write(dn.getNumber());
            write(" : ");
            writelnWithBreak(dn.getData());
        }
        writeln();
        writeln();
        writeln();
    }

    /**
     * Print the tracked item summary section.
     * 
     * @param logData LogDataHolder holding all the log data
     */
    protected void printTrackedItemSection(LogDataHolder logData)
    {
        if (logData.getLogSummary().getTrackedCombatItemUses().size() > 0) {
            printSectionHeader("TRACKED COMBAT ITEMS", "trackedcombatitems");
            for (final DataNumberPair<String> dn : logData.getLogSummary().getTrackedCombatItemUses()) {
                write(dn.getNumber());
                write(" : ");
                writelnWithBreak(dn.getData());
            }
            writeln();
            writeln();
            writeln();
        }
    }

    /**
     * Print the DNA Lab summary section.
     * 
     * @param logData LogDataHolder holding all the log data
     */
    protected void printHybridSection(LogDataHolder logData)
    {
        //Hybrid
        if (logData.getHybridContent().size() > 0) {
            printSectionHeader("DNA Lab", "hybrid");
            for (final DataNumberPair<String> dn : logData.getHybridContent()) {
                write(dn.getNumber());
                write(" : ");
                writelnWithBreak(dn.getData());
            }
            writeln();
            writeln();
            writeln();
        }
    }
    
    /**
     * Print the olfaction-affected combat summary section.
     * 
     * @param logData LogDataHolder holding all the log data
     */
    protected void printHuntedCombatsSection(LogDataHolder logData)
    {
        // Hunted combats summary
        printSectionHeader("HUNTED COMBATS", "onthetrail");
        for (final DataNumberPair<String> dn : logData.getHuntedCombats()) {
            write(dn.getNumber());
            write(" : ");
            writelnWithBreak(dn.getData());
        }
        writeln();
        writeln();
        writeln();
    }

    /**
     * Print the banishment summary section.
     * 
     * @param logData LogDataHolder holding all the log data
     */
    protected void printBanishedCombatsSection(LogDataHolder logData)
    {
        // Banished combats summary
        printSectionHeader("BANISHMENT", "banishment");
        for (final DataNumberPair<String> dn : logData.getLogSummary().getBanishedCombats()) {
            write(dn.getNumber());
            write(" : ");
            writelnWithBreak(dn.getData());
        }
        writeln();
        writeln();
        writeln();
    }

    /**
     * Print the disintegration summary section.
     * 
     * @param logData LogDataHolder holding all the log data
     */
    protected void printDisintegratedCombatsSection(LogDataHolder logData)
    {
        // Disintegrated combats summary
        printSectionHeader("YELLOW DESTRUCTION", "yellowray");
        for (final DataNumberPair<String> dn : logData.getLogSummary().getDisintegratedCombats()) {
            write(dn.getNumber());
            write(" : ");
            writelnWithBreak(dn.getData());
        }
        writeln();
        writeln();
        writeln();
    }

    /**
     * Print the copied combat summary section.
     * 
     * @param logData LogDataHolder holding all the log data
     */
    protected void printCopiedCombatsSection(LogDataHolder logData)
    {
        // Copied combats summary
        printSectionHeader("COPIED COMBATS", "copies");
        for (final TurnInterval ti : logData.getCopiedTurns())
            for (final SingleTurn st : ti.getTurns()) {
                write(st.getTurnNumber());
                write(" : ");
                writelnWithBreak(st.getEncounterName());
            }
        if (!logData.getLogSummary().getRomanticArrowUsages().isEmpty()) {
            writelnWithBreak();
            writelnWithBreak();
            writelnWithBreak("Familiar copy usage:");
            for (final DataNumberPair<String> dn : logData.getLogSummary().getRomanticArrowUsages()) {
                write(dn.getNumber());
                write(" : ");
                writelnWithBreak(dn.getData());
            }
        }
        writeln();
        writeln();
        writeln();
    }

    /**
     * Print the free runaway summary section.
     * 
     * @param logData LogDataHolder holding all the log data
     */
    protected void printFreeRunawaySection(LogDataHolder logData)
    {
        // Free runaway summary
        printSectionHeader("FREE RUNAWAYS", "runaways");
        write(logData.getLogSummary().getFreeRunaways().toString());
        writelnWithBreak(" overall");
        if (!logData.getLogSummary().getFreeRunawaysCombats().isEmpty()) {
            writelnWithBreak();
            writelnWithBreak();
        }
        for (final Encounter e : logData.getLogSummary().getFreeRunawaysCombats()) {
            write(e.getTurnNumber());
            write(" : ");
            write(e.getAreaName());
            write(" -- ");
            writelnWithBreak(e.getEncounterName());
        }
        writeln();
        writeln();
        writeln();
    }

    /**
     * Print the wandering encounter summary section.
     * 
     * @param logData LogDataHolder holding all the log data
     */
    protected void printWanderingEncountersSection(LogDataHolder logData)
    {
        // Wandering encounters summary
        printSectionHeader("WANDERING ENCOUNTERS", "wanderers");
        for (final DataNumberPair<String> dn : logData.getLogSummary().getWanderingAdventures()) {
            write(dn.getNumber());
            write(" : ");
            writelnWithBreak(dn.getData());
        }
        writeln();
        writeln();
        writeln();
    }
    
    /**
     * Print the combat item use section.
     * 
     * @param logData LogDataHolder holding all the log data
     */
    protected void printCombatItemsSection(LogDataHolder logData)
    {
        //Combat Items Used
        printSectionHeader("COMBAT ITEMS", "combatitems");
        for (final CombatItem ci : logData.getLogSummary().getCombatItemsUsed()) {
            write("Used ");
            write(ci.getAmount());
            write(UsefulPatterns.WHITE_SPACE);
            writelnWithBreak(ci.getName());
        }
        writeln();
        writeln();
        writeln();
    }
    
    /**
     * Print the skill casting summary section.
     * 
     * @param logData LogDataHolder holding all the log data
     */
    protected void printCastsSection(LogDataHolder logData)
    {
        // Skills cast summary
        printSectionHeader("CASTS", "casts");
        for (final Skill s : logData.getLogSummary().getSkillsCast()) {
            writelnWithBreak(s.toString());
        }
        writelnWithBreak();
        writelnWithBreak("------------------");
        writelnWithBreak("| Total Casts    |  "
                + logData.getLogSummary().getTotalAmountSkillCasts());
        writelnWithBreak("------------------");
        writelnWithBreak();
        writelnWithBreak("------------------");
        writelnWithBreak("| Total MP Spent    |  "
                + logData.getLogSummary().getTotalMPUsed());
        writelnWithBreak("------------------");
        writeln();
        writeln();
        writeln();
    }
    
    /**
     * Print the MP gain summary section.
     * 
     * @param logData LogDataHolder holding all the log data
     */
    protected void printMPSection(LogDataHolder logData)
    {
        // MP summary
        final MPGain mpGains = logData.getLogSummary().getTotalMPGains();
        printSectionHeader("MP GAINS", "mpgains");
        writelnWithBreak("Total mp gained: " + mpGains.getTotalMPGains());
        writelnWithBreak();
        writelnWithBreak("Inside Encounters: " + mpGains.encounterMPGain);
        writelnWithBreak("Starfish Familiars: " + mpGains.starfishMPGain);
        writelnWithBreak("Resting: " + mpGains.restingMPGain);
        writelnWithBreak("Outside Encounters: " + mpGains.outOfEncounterMPGain);
        writelnWithBreak("Consumables: " + mpGains.consumableMPGain);
        writelnWithBreak();
        for (final DataNumberPair<MPGain> dnp : logData.getLogSummary()
                .getMPGainSummary()
                .getAllLevelsData()) {
            printParagraphStart();
            writelnWithBreak("Level " + dnp.getNumber() + UsefulPatterns.COLON);
            writelnWithBreak("   Inside Encounters: " + dnp.getData().encounterMPGain);
            writelnWithBreak("   Starfish Familiars: " + dnp.getData().starfishMPGain);
            writelnWithBreak("   Resting: " + dnp.getData().restingMPGain);
            writelnWithBreak("   Outside Encounters: " + dnp.getData().outOfEncounterMPGain);
            writeln("   Consumables: " + dnp.getData().consumableMPGain);
            printParagraphEnd();
        }
        writeln();
        writeln();
        writeln();        
    }
    
    /**
     * Print the consumption summary section.
     * 
     * @param logData LogDataHolder holding all the log data
     */
    protected void printConsumablesSection(LogDataHolder logData)
    {
        // Consumables summary
        printSectionHeader("EATING AND DRINKING AND USING", "consuming");
        writelnWithBreak("Adventures gained eating: " + logData.getLogSummary().getTotalTurnsFromFood());
        writelnWithBreak("Adventures gained drinking: " + logData.getLogSummary().getTotalTurnsFromBooze());
        writelnWithBreak("Adventures gained using: " + logData.getLogSummary().getTotalTurnsFromOther());
        writelnWithBreak("Adventures gained rollover: " + logData.getLogSummary().getTotalTurnsFromRollover());
        writelnWithBreak();
        for (final Consumable c : logData.getLogSummary().getFoodConsumablesUsed()) {
            writelnWithBreak(c.toString());
        }
        writelnWithBreak();
        for (final Consumable c : logData.getLogSummary().getBoozeConsumablesUsed()) {
            writelnWithBreak(c.toString());
        }
        writelnWithBreak();
        for (final Consumable c : logData.getLogSummary().getSpleenConsumablesUsed()) {
            writelnWithBreak(c.toString());
        }
        writelnWithBreak();
        for (final Consumable c : logData.getLogSummary().getOtherConsumablesUsed())
            if (c.getAdventureGain() > 0 || !c.getStatGain().isAllStatsZero()
                    || UsefulPatterns.SPECIAL_CONSUMABLES.contains(c.getName())) {
                writelnWithBreak(c.toString());
            }
        writeln();
        writeln();
        writeln();
    }
    
    /**
     * Print the meat gain summary.
     * 
     * @param logData LogDataHolder holding all the log data
     */
    protected void printMeatSection(LogDataHolder logData)
    {
        printSectionHeader("MEAT", "meat");
        writelnWithBreak("Total meat gained: " + logData.getLogSummary().getTotalMeatGain());
        writelnWithBreak("Total meat spent: " + logData.getLogSummary().getTotalMeatSpent());
        writelnWithBreak("");
        for (final DataNumberPair<MeatGain> dnp : logData.getLogSummary()
                .getMeatSummary()
                .getAllLevelsData()) {
            printParagraphStart();
            writelnWithBreak("Level " + dnp.getNumber() + UsefulPatterns.COLON);
            writelnWithBreak("   Meat gain inside Encounters: " + dnp.getData().encounterMeatGain);
            writelnWithBreak("   Meat gain outside Encounters: " + dnp.getData().otherMeatGain);
            writeln("   Meat spent: " + dnp.getData().meatSpent);
            printParagraphEnd();
        }
        writeln();
        writeln();
        writeln();        
    }
    
    /**
     * Print the bottleneck section summary.
     * 
     * @param logData LogDataHolder holding all the log data
     */
    protected void printBottlenecksSection(LogDataHolder logData)
    {
        final List<DataNumberPair<String>> lostCombats = logData.getLostCombats();
        printSectionHeader("BOTTLENECKS", "bottlenecks");
        writelnWithBreak("Spent " + logData.getLogSummary().get8BitRealm().getTurnsSpent()
                + " turns in the 8-Bit Realm");
        writelnWithBreak("Fought " + logData.getLogSummary().get8BitRealm().getBloopersFound() + " bloopers");
        writelnWithBreak("Fought " + logData.getLogSummary().get8BitRealm().getBulletsFound()
                + " bullet bills");
        writelnWithBreak("Spent " + logData.getLogSummary().getGoatlet().getTurnsSpent()
                + " turns in the Goatlet");
        writelnWithBreak("Fought " + logData.getLogSummary().getGoatlet().getDairyGoatsFound()
                + " dairy goats for " + logData.getLogSummary().getGoatlet().getCheeseFound()
                + " cheeses and " + logData.getLogSummary().getGoatlet().getMilkFound()
                + " glasses of milk");

        final SpookyravenPowerleveling powerleveling = new SpookyravenPowerleveling(logData.getTurnIntervalsSpent());
        writelnWithBreak("Spent " + powerleveling.getBallroomTurns()
                + " turns in the Haunted Ballroom and found "
                + powerleveling.getBallroomStatNoncombats() + " Curtains");
        writelnWithBreak("Fought " + powerleveling.getZombieWaltzers() + " Zombie Waltzers and found "
                + powerleveling.getDanceCards() + " Dance Cards");
        writelnWithBreak("Spent " + powerleveling.getGalleryTurns()
                + " turns in the Haunted Gallery and found " + powerleveling.getLouvres()
                + " Louvres");
        writelnWithBreak("Spent " + powerleveling.getBathroomTurns()
                + " turns in the Haunted Bathroom and found " + powerleveling.getBathroomNoncombats()
                + " noncombats");

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
        printParagraphStart();
        writelnWithBreak("Garnishes received: " + (coconut + umbrella + cube));
        writelnWithBreak("     Coconuts: " + coconut);
        writelnWithBreak("     Umbrellas: " + umbrella);
        writelnWithBreak("     Ice Cubes: " + cube);
        printParagraphEnd();

        writelnWithBreak("Number of lost combats: " + lostCombats.size());
        for (final DataNumberPair<String> dnp : lostCombats)
            writelnWithBreak("     " + dnp);
        writelnWithBreak();
    }
    
    
    /**
     * Write a string to the log output.
     * 
     * @param s String to write.  If null, does nothing.
     */
    protected void write(final String s) 
    {
        if (s != null)
            log.append(s);
    }

    /**
     * Write an integer to the log output.
     * 
     * @param i Integer to write.
     */
    protected void write(final int i) {
        log.append(i);
    }
    
    /**
     * Write the end of a format's line to the log output.  
     */
    protected void writeEndLine()
    {
        log.append(NEW_LINE);
    }
    
    /**
     * Write the OS's newline string to the log output, followed by the
     * parsed log format's line break.
     */
    protected void writelnWithBreak()
    {
        writeln();
        printLineBreak();
    }
    
    /**
     * Write the OS's newline string to the log output.
     */
    protected void writeln() 
    {
        log.append(NEW_LINE);
    }
    
    /**
     * Write the given string to the log output, followed by the OS's newline string, 
     * followed by the parsed log format's line break.
     * 
     * @param s String to write.
     */
    protected void writelnWithBreak(final String s)
    {
        writeln(s);
        printLineBreak();
    }
    
    /**
     * Write the given string to the log output, followed by the OS's newline string.
     * 
     * @param s String to write.
     */
    protected void writeln(final String s)
    {
        if (s != null)
            log.append(s);
        log.append(NEW_LINE);
    }
    
    /**
     * Write the given integer to the log output, followed by the OS's newline string.
     * 
     * @param i Integer to write.
     */
    protected void writeln(final int i)
    {
        log.append(i).append(NEW_LINE);
    }
}
