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

package com.googlecode.alv.parser;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.googlecode.alv.Settings;
import com.googlecode.alv.logData.HeaderFooterComment;
import com.googlecode.alv.logData.LogDataHolder;
import com.googlecode.alv.logData.turn.SingleTurn;
import com.googlecode.alv.logData.turn.turnAction.DayChange;
import com.googlecode.alv.logData.turn.turnAction.EquipmentChange;
import com.googlecode.alv.logData.turn.turnAction.FamiliarChange;
import com.googlecode.alv.parser.MafiaSessionLogReader.LogBlock;
import com.googlecode.alv.parser.MafiaSessionLogReader.LogBlockType;
import com.googlecode.alv.parser.lineParsers.DayChangeLineParser;
import com.googlecode.alv.parser.lineParsers.EquipmentLineParser;
import com.googlecode.alv.parser.lineParsers.ItemAcquisitionLineParser;
import com.googlecode.alv.parser.lineParsers.MPGainLineParser;
import com.googlecode.alv.parser.lineParsers.MafiaFamiliarChangeLineParser;
import com.googlecode.alv.parser.lineParsers.MafiaLearnedSkillLineParser;
import com.googlecode.alv.parser.lineParsers.MafiaPullLineParser;
import com.googlecode.alv.parser.lineParsers.MeatLineParser;
import com.googlecode.alv.parser.lineParsers.MeatSpentLineParser;
import com.googlecode.alv.parser.lineParsers.NotesLineParser;
import com.googlecode.alv.parser.lineParsers.PoolMPBuffLineParser;
import com.googlecode.alv.parser.lineParsers.SkillCastLineParser;
import com.googlecode.alv.parser.lineParsers.StatLineParser;
import com.googlecode.alv.parser.lineParsers.MPGainLineParser.MPGainType;
import com.googlecode.alv.parser.lineParsers.MeatLineParser.MeatGainType;
import com.googlecode.alv.parser.mafiaLogBlockParsers.AscensionDataBlockParser;
import com.googlecode.alv.parser.mafiaLogBlockParsers.ConsumableBlockParser;
import com.googlecode.alv.parser.mafiaLogBlockParsers.EncounterBlockParser;
import com.googlecode.alv.parser.mafiaLogBlockParsers.HybridDataBlockParser;
import com.googlecode.alv.parser.mafiaLogBlockParsers.PlayerSnapshotBlockParser;
import com.googlecode.alv.parser.mafiaLogBlockParsers.ServiceBlockParser;
import com.googlecode.alv.util.Lists;
import com.googlecode.alv.util.Maps;
import com.googlecode.alv.util.Pair;
import com.googlecode.alv.util.Stack;

public final class MafiaLogParser implements LogParser 
{
    //private static final Pattern THREE_FIGURE_STATGAIN = Pattern.compile("You gain \\d{3} [\\w\\s]+");

    private static final String WINS_THE_FIGHT = "wins the fight!";

    private static final String NAUGHTY_SORCERESS_3RD_FORM = "Naughty Sorceress (3)";

    private static final String RAIN_KING = "The Rain King";

    private static final String AVATAR_OF_JARLSBERG = "Avatar of Jarlsberg";

    //private static final String NAUGHTY_SORCERESS_FIGHT_STRING = "Sorceress Tower: Naughty Sorceress";

    private static final String NAUGHTY_SORCERESS_FIGHT_STRING_2015 = "The Naughty Sorceress' Chamber";
    
    private static final String DONATE_BODY = "Took choice 1089/30";
    
    private static final Matcher ROUND0 
        = Pattern.compile("Round 0: (.*) +(wins|loses) initiative!").matcher("");
    
    private static final Matcher ENCOUNTER 
        = Pattern.compile("Encounter: (.*) *$").matcher("");

    private final LogDataHolder logData = new LogDataHolder(true);

    private final File log;

    private final Stack<EquipmentChange> equipmentStack = Stack.newStack();
    {
        equipmentStack.push(logData.getLastEquipmentChange());
    }

    private final Map<String, String> familiarEquipmentMap = Maps.newHashMap();

    private final EncounterBlockParser encounterParser 
        = new EncounterBlockParser(equipmentStack, familiarEquipmentMap);

    private final ConsumableBlockParser consumableParser 
        = new ConsumableBlockParser(equipmentStack, familiarEquipmentMap);

    private final PlayerSnapshotBlockParser playerSnapshotParser 
        = new PlayerSnapshotBlockParser(equipmentStack, familiarEquipmentMap);

    private final AscensionDataBlockParser ascensionDataParser = new AscensionDataBlockParser();

    private final HybridDataBlockParser hybridDataParser = new HybridDataBlockParser();
    
    private final ServiceBlockParser serviceParser = new ServiceBlockParser();

    private final List<LineParser> lineParsers = Lists.newArrayList();

    /**
     * @param log
     *         The mafia ascension log which is intended to be parsed to set.
     * @param isIncludeMafiaLogNotes
     *         Whether the file includes Mafia log notes that need to be parsed
     * @throws NullPointerException
     *             if log is {@code null}
     */
    public MafiaLogParser(final File log, final boolean isIncludeMafiaLogNotes) 
    {
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
        lineParsers.add(new MafiaLearnedSkillLineParser() );
        if (isIncludeMafiaLogNotes)
            lineParsers.add(new NotesLineParser());
    }

    /**
     * {@inheritDoc}
     */
    public void parse()
            throws IOException 
    {
        final MafiaSessionLogReader reader = new MafiaSessionLogReader(log);
        final boolean isOldAscensionCounting = Settings.getBoolean("Using old ascension counting");
        boolean nsFightWon = false;

        while (reader.hasNext() && !nsFightWon) {
            final LogBlock block = reader.next();

            // In case old ascension turn counting is turned off and the current
            // block is an encounter block, we need to check whether the Naughty
            // Sorceress was beaten in it.
            if (! isOldAscensionCounting) {
                if (block.getBlockType() == LogBlockType.ENCOUNTER_BLOCK) {
                    // Get the encounter name and the location
                    List<String> lines = block.getBlockLines();
                    String tmp = lines.size() > 1 ? lines.get(1) : "";
                    String tmp0 = lines.size() > 0 ? lines.get(0) : "";

                    // First check the encounter type to see if the location
                    // is the Naughty Sorceress or one of the end bosses
                    if (tmp.endsWith(NAUGHTY_SORCERESS_3RD_FORM))
                        nsFightWon = isFightWon(block);
                    else if (tmp.endsWith(RAIN_KING))
                        nsFightWon = isFightWon(block);
                    else if (tmp.endsWith(AVATAR_OF_JARLSBERG))
                        nsFightWon = isFightWon(block);
                    else if (tmp0.contains(NAUGHTY_SORCERESS_FIGHT_STRING_2015)) {
                        // Dark Gyffte - final boss's name is the player's backwards
                        if (isFinalDarkGyffteBattle(tmp, lines))
                            nsFightWon = isFightWon(block);
                        // Path of the Plumber - final boss varies, but always the same place
                        else if (tmp.contains("Encounter: Wa"))
                            nsFightWon = isFightWon(block);
                    }
                } else if (block.getBlockType() == LogBlockType.SERVICE_BLOCK) {
                    // Community Service - last block is donating your body
                    if (block.getBlockLines().get(0).startsWith(DONATE_BODY))
                        nsFightWon = true;
                } else if (block.getBlockType() == LogBlockType.OTHER_BLOCK) {
                    // Actually Ed
                    if (block.getBlockLines().size() > 2 
                            && block.getBlockLines().get(1).contains("Encounter: Returning the MacGuffin")) {
                        for (String line : block.getBlockLines()) {
                            if (line.equals( "choice.php?pwd&whichchoice=1054&option=1" ))
                                nsFightWon = true;
                        }
                    }
                    // If all else fails, find out if we freed the King
                    else if (block.getBlockLines().size() > 1 &&
                             block.getBlockLines().get(1).contains("Tower: Freeing King Ralph")) {
                        nsFightWon = true;
                    }
                }

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
            case HYBRID_DATA_BLOCK:
                hybridDataParser.parseBlock( block.getBlockLines(), logData );
                break;
            case SERVICE_BLOCK:
                serviceParser.parseBlock(block.getBlockLines(), logData);
                break;
            case COMBING_BLOCK:        // TODO Fill this in
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

        logData.handleParseFinished();

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
     * Return true if and only if the given lines, which must represent an encounter block,
     * represents the final Dark Gyffte battle.  This is recognized by the final boss
     * having the name of the player, but backwards.
     * 
     * @param encounter The Encounter: line of the encounter block
     * @param lines The sequence of lines for the encounter, which must be an encounter block
     * @return true if and only if this is the Dark Gyffte final encounter
     */
    private boolean isFinalDarkGyffteBattle(String encounter, List<String> lines)
    {
        if (lines.size() < 3)
            return false;
        if (! ROUND0.reset(lines.get(2)).find())
            return false;
        if (! ENCOUNTER.reset(encounter).find())
            return false;
        String lcPlayerName = ROUND0.group(1).toLowerCase();
        String lcBossName = ENCOUNTER.group(1).toLowerCase();
        StringBuilder reverser = new StringBuilder(lcPlayerName);
        reverser.reverse();
        String revPlayerName = reverser.toString();
        return lcBossName.equals(revPlayerName);
    }
    
    /*
     * This method checks whether the Naughty Sorceress has been beaten.
     *
     * @param block
     *            The Naughty Sorceress encounter block.
     * @return True if the Naughty Sorceress was beaten, otherwise false.
     
    private boolean isNaughtySorceressBeaten(final LogBlock block) 
    {
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
    */

    /**
     * This method checks whether the fight was won
     *
     * @param block
     *            The encounter block.
     * @return True if fight was won.
     */
    private boolean isFightWon(final LogBlock block)
    {
        for (final String line : block.getBlockLines())
        {
            if (line.endsWith(WINS_THE_FIGHT))
            {
                return true;
            }
        }
        return false;
    }



    /**
     * {@inheritDoc}
     */
    public LogDataHolder getLogData() 
    {
        return logData;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isDetailedLogData() 
    {
        return true;
    }
}
