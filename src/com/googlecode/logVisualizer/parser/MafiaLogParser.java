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

package com.googlecode.logVisualizer.parser;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Pattern;

import com.googlecode.logVisualizer.Settings;
import com.googlecode.logVisualizer.logData.HeaderFooterComment;
import com.googlecode.logVisualizer.logData.LogDataHolder;
import com.googlecode.logVisualizer.logData.turn.SingleTurn;
import com.googlecode.logVisualizer.logData.turn.turnAction.DayChange;
import com.googlecode.logVisualizer.logData.turn.turnAction.EquipmentChange;
import com.googlecode.logVisualizer.logData.turn.turnAction.FamiliarChange;
import com.googlecode.logVisualizer.parser.MafiaSessionLogReader.LogBlock;
import com.googlecode.logVisualizer.parser.MafiaSessionLogReader.LogBlockType;
import com.googlecode.logVisualizer.parser.lineParsers.*;
import com.googlecode.logVisualizer.parser.lineParsers.MPGainLineParser.MPGainType;
import com.googlecode.logVisualizer.parser.lineParsers.MeatLineParser.MeatGainType;
import com.googlecode.logVisualizer.parser.mafiaLogBlockParsers.AscensionDataBlockParser;
import com.googlecode.logVisualizer.parser.mafiaLogBlockParsers.ConsumableBlockParser;
import com.googlecode.logVisualizer.parser.mafiaLogBlockParsers.EncounterBlockParser;
import com.googlecode.logVisualizer.parser.mafiaLogBlockParsers.PlayerSnapshotBlockParser;
import com.googlecode.logVisualizer.util.*;

public final class MafiaLogParser implements LogParser {
    private static final Pattern THREE_FIGURE_STATGAIN = Pattern.compile("You gain \\d{3} [\\w\\s]+");

    private static final String NAUGHTY_SORCERESS_FIGHT_STRING = "Sorceress Tower: Naughty Sorceress";

    private final LogDataHolder logData = new LogDataHolder(true);

    private final File log;

    private final Stack<EquipmentChange> equipmentStack = Stack.newStack();
    {
        equipmentStack.push(logData.getLastEquipmentChange());
    }

    private final Map<String, String> familiarEquipmentMap = Maps.newHashMap();

    private final EncounterBlockParser encounterParser = new EncounterBlockParser(equipmentStack,
                                                                                  familiarEquipmentMap);

    private final ConsumableBlockParser consumableParser = new ConsumableBlockParser(equipmentStack,
                                                                                     familiarEquipmentMap);

    private final PlayerSnapshotBlockParser playerSnapshotParser = new PlayerSnapshotBlockParser(equipmentStack,
                                                                                                 familiarEquipmentMap);

    private final AscensionDataBlockParser ascensionDataParser = new AscensionDataBlockParser();

    private final List<LineParser> lineParsers = Lists.newArrayList();

    /**
     * @param log
     *            The mafia ascension log which is intended to be parsed to set.
     * @throws NullPointerException
     *             if log is {@code null}
     */
    public MafiaLogParser(
                          final File log, final boolean isIncludeMafiaLogNotes) {
        this.log = log;

        // Set the log name
        getLogData().setLogName(log.getName().replace(".txt", UsefulPatterns.EMPTY_STRING));

        lineParsers.add(new ItemAcquisitionLineParser());
        lineParsers.add(new SkillCastLineParser());
        lineParsers.add(new MafiaFamiliarChangeLineParser(equipmentStack, familiarEquipmentMap));
        lineParsers.add(new MeatLineParser(MeatGainType.OTHER));
        lineParsers.add(new MeatSpentLineParser());
        lineParsers.add(new StatLineParser());
        lineParsers.add(new MPGainLineParser(MPGainType.NOT_ENCOUNTER));
        lineParsers.add(new EquipmentLineParser(equipmentStack, familiarEquipmentMap));
        lineParsers.add(new MafiaPullLineParser());
        lineParsers.add(new PoolMPBuffLineParser());
        lineParsers.add(new DayChangeLineParser());
        if (isIncludeMafiaLogNotes)
            lineParsers.add(new NotesLineParser());
    }

    /**
     * {@inheritDoc}
     */
    public void parse()
                       throws IOException {
        final MafiaSessionLogReader reader = new MafiaSessionLogReader(log);
        final boolean isOldAscensionCounting = Settings.getSettingBoolean("Using old ascension counting");
        boolean nsFightWon = false;

        while (reader.hasNext() && !nsFightWon) {
            final LogBlock block = reader.next();

            // In case old ascension turn counting is turned off and the current
            // block is an encounter block, we need to check whether the Naughty
            // Sorceress was beaten in it.
            if (!isOldAscensionCounting && block.getBlockType() == LogBlockType.ENCOUNTER_BLOCK) {
                final String tmp = block.getBlockLines().get(0);
                if (tmp.endsWith(NAUGHTY_SORCERESS_FIGHT_STRING))
                    nsFightWon = isNaughtySorceressBeaten(block);
            }

            // Now, we do the actual parsing.
            switch (block.getBlockType()) {
                case ENCOUNTER_BLOCK:
                    encounterParser.parseBlock(block.getBlockLines(), logData);
                    break;
                case CONSUMABLE_BLOCK:
                    consumableParser.parseBlock(block.getBlockLines(), logData);
                    break;
                case PLAYER_SNAPSHOT_BLOCK:
                    playerSnapshotParser.parseBlock(block.getBlockLines(), logData);
                    break;
                case ASCENSION_DATA_BLOCK:
                    ascensionDataParser.parseBlock(block.getBlockLines(), logData);
                    break;
                case OTHER_BLOCK:
                    for (final String line : block.getBlockLines())
                        for (final LineParser lp : lineParsers)
                            // If the line parser can parse the line, this
                            // method also returns true. This is used to cut
                            // back on the amount of loops.
                            if (lp.parseLine(line, logData))
                                break;
            }
        }

        reader.close();

        // Before creating the summary data, we first need to add MP
        // regeneration from equipment where applicable.
        for (final SingleTurn st : getLogData().getTurnsSpent())
            st.addMPRegen();

        // Recreate day changes from the data in the single turns, since there
        // are situations where the turn numbers from preliminary day change
        // parsing may not be entirely correct. (this most often occurs when
        // there were free runaways or non-turns present at the end of a day)
        int currentDay = 1;
        final Map<Integer, HeaderFooterComment> dayComments = Maps.newHashMap();
        for (final Pair<DayChange, HeaderFooterComment> dayComment : getLogData().getHeaderFooterComments())
            dayComments.put(dayComment.getVar1().getDayNumber(), dayComment.getVar2());
        for (final SingleTurn st : getLogData().getTurnsSpent())
            while (currentDay < st.getDayNumber()) {
                currentDay++;

                final DayChange newDay = new DayChange(currentDay, st.getTurnNumber() - 1);
                getLogData().addDayChange(newDay);

                final HeaderFooterComment newDayComment = getLogData().getHeaderFooterComment(newDay);
                final HeaderFooterComment oldDayComment = dayComments.get(newDay.getDayNumber());
                newDayComment.setHeaderComments(oldDayComment.getHeaderComments());
                newDayComment.setFooterComments(oldDayComment.getFooterComments());
            }

        // Do the same thing for familiar and equipment changes, as there can
        // also be problems in the face of free runaways and non-turns.
        final List<FamiliarChange> famChanges = Lists.newArrayList(getLogData().getLastTurnSpent()
                                                                               .getTurnNumber());
        final List<EquipmentChange> equipChanges = Lists.newArrayList(getLogData().getLastTurnSpent()
                                                                                  .getTurnNumber());
        for (final SingleTurn st : getLogData().getTurnsSpent()) {
            famChanges.add(st.getUsedFamiliar());
            equipChanges.add(st.getUsedEquipment());
        }
        getLogData().setFamiliarChanges(famChanges);
        getLogData().setEquipmentChanges(equipChanges);

        getLogData().createLogSummary();
    }

    /**
     * This method checks whether the Naughty Sorceress has been beaten.
     * 
     * @param block
     *            The Naughty Sorceress encounter block.
     * @return True if the Naughty Sorceress was beaten, otherwise false.
     */
    private boolean isNaughtySorceressBeaten(
                                             final LogBlock block) {
        for (final String line : block.getBlockLines())
            // Three figure stat gains aren't possible through combat items
            // while winning against the NS will give these amounts, so if there
            // is such a line, it means the fight has been won.
            if (THREE_FIGURE_STATGAIN.matcher(line).matches()) {
                final Scanner scanner = new Scanner(line);
                scanner.findInLine(UsefulPatterns.GAIN_LOSE_CAPTURE_PATTERN);
                final String substatName = scanner.match().group(2);
                scanner.close();

                if (UsefulPatterns.MUSCLE_SUBSTAT_NAMES.contains(substatName)
                    || UsefulPatterns.MYST_SUBSTAT_NAMES.contains(substatName)
                    || UsefulPatterns.MOXIE_SUBSTAT_NAMES.contains(substatName))
                    return true;
            }

        return false;
    }

    /**
     * {@inheritDoc}
     */
    public LogDataHolder getLogData() {
        return logData;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isDetailedLogData() {
        return true;
    }
}
