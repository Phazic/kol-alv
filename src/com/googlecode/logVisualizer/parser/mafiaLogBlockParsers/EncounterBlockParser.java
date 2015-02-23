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

package com.googlecode.logVisualizer.parser.mafiaLogBlockParsers;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.java.dev.spellcast.utilities.DataUtilities;
import net.java.dev.spellcast.utilities.UtilityConstants;

import com.googlecode.logVisualizer.Settings;
import com.googlecode.logVisualizer.logData.LogDataHolder;
import com.googlecode.logVisualizer.logData.LogDataHolder.AscensionPath;
import com.googlecode.logVisualizer.logData.MeatGain;
import com.googlecode.logVisualizer.logData.turn.SingleTurn;
import com.googlecode.logVisualizer.logData.turn.TurnVersion;
import com.googlecode.logVisualizer.logData.turn.turnAction.EquipmentChange;
import com.googlecode.logVisualizer.logData.turn.turnAction.FamiliarChange;
import com.googlecode.logVisualizer.parser.LineParser;
import com.googlecode.logVisualizer.parser.MafiaSessionLogReader;
import com.googlecode.logVisualizer.parser.UsefulPatterns;
import com.googlecode.logVisualizer.parser.lineParsers.CombatRecognizerLineParser;
import com.googlecode.logVisualizer.parser.lineParsers.EquipmentLineParser;
import com.googlecode.logVisualizer.parser.lineParsers.ItemAcquisitionLineParser;
import com.googlecode.logVisualizer.parser.lineParsers.MPGainLineParser;
import com.googlecode.logVisualizer.parser.lineParsers.MPGainLineParser.MPGainType;
import com.googlecode.logVisualizer.parser.lineParsers.MafiaBanishLineParser;
import com.googlecode.logVisualizer.parser.lineParsers.MafiaDisintegrateLineParser;
import com.googlecode.logVisualizer.parser.lineParsers.MafiaFreeRunawaysLineParser;
import com.googlecode.logVisualizer.parser.lineParsers.MafiaRedRayStatsLineParser;
import com.googlecode.logVisualizer.parser.lineParsers.MeatLineParser;
import com.googlecode.logVisualizer.parser.lineParsers.MeatLineParser.MeatGainType;
import com.googlecode.logVisualizer.parser.lineParsers.MeatSpentLineParser;
import com.googlecode.logVisualizer.parser.lineParsers.NotesLineParser;
import com.googlecode.logVisualizer.parser.lineParsers.OnTheTrailLineParser;
import com.googlecode.logVisualizer.parser.lineParsers.SkillCastLineParser;
import com.googlecode.logVisualizer.parser.lineParsers.StarfishMPGainLineParser;
import com.googlecode.logVisualizer.parser.lineParsers.StatLineParser;
import com.googlecode.logVisualizer.util.DataNumberPair;
import com.googlecode.logVisualizer.util.Lists;
import com.googlecode.logVisualizer.util.Maps;
import com.googlecode.logVisualizer.util.Sets;
import com.googlecode.logVisualizer.util.Stack;

/**
 * A parser for the turn spent notation in mafia logs.
 * <p>
 * The format looks like this:
 * <p>
 * {@code [_turnNumber_] _areaName_}
 * <p>
 * {@code Encounter: _encounterName_}
 */
public final class EncounterBlockParser implements LogBlockParser {
    private static final Map<String, String> areaNameStandardizerMap;

    static {
        areaNameStandardizerMap = Maps.newHashMap(50);
        final Pattern areaNameMappingPattern = Pattern.compile(".+\\|\\s*.+");
        final Pattern splitPattern = Pattern.compile("\\s*\\|\\s*");
        final String commentStart = "//";
        String tmpLine;
        final BufferedReader br = DataUtilities.getReader(UtilityConstants.KOL_DATA_DIRECTORY,
                "areaNameMappings.txt");

        try {
            while ((tmpLine = br.readLine()) != null)
                if (!tmpLine.startsWith(commentStart)
                        && areaNameMappingPattern.matcher(tmpLine).matches()) {
                    final Scanner s = new Scanner(tmpLine);
                    s.useDelimiter(splitPattern);

                    final String areaName = s.next();
                    final String newAreaName = s.next();
                    s.close();

                    areaNameStandardizerMap.put(areaName, newAreaName);
                }

            br.close();
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    private static final Set<String> OTHER_ENCOUNTER_AREAS_SET = Sets.immutableSetOf("Unlucky Sewer",
            "Sewer With Clovers",
            "Lemon Party",
            "Guild Challenge",
            "Mining (In Disguise)",
            "Itznotyerzitz Mine (in Disguise)");

    private static final Set<String> GAME_GRID_AREAS_SET = Sets.immutableSetOf("DemonStar",
            "Meteoid",
            "The Fighters of Fighting",
            "Dungeon Fist!",
            "Space Trip",
            "Jackass Plumber");

    private static final String ENCOUNTER_START_STRING = "Encounter: ";

    private static final String COOKING_START_STRING = "Cook ";

    private static final String MIXING_START_STRING = "Mix ";

    private static final String SMITHING_START_STRING = "Smith ";

    private static final String SHORE_AREAS_END_STRING = " Vacation";

    private static final String HYBRIDIZING_AREANAME= "Hybridizing yourself";

    private static final Pattern HYBRIDIZE_PATTERN = Pattern.compile("You acquire an intrinsic: (.+) Hybrid$");

    private static final String CLOWNLORD_CHOICE_ENCOUNTER_STRING = "Adventurer, $1.99";

    private static final String OUTFIT_STRING = "outfit";

    private static final String MCD_STRING = "mcd";

    private static final String HP_LOSE_STRING_BEGINNING = "You lose ";

    private static final Pattern HP_LOSE_PATTERN = Pattern.compile("You lose \\d+ hit points.?");

    private static final Pattern FIGHT_WON_PATTERN = Pattern.compile("Round \\d+: .+ wins the fight!");

    private final List<LineParser> lineParsers = Lists.newArrayList();

    public EncounterBlockParser(
            final Stack<EquipmentChange> equipmentStack,
            final Map<String, String> familiarEquipmentMap) {
        lineParsers.add(new ItemAcquisitionLineParser());
        lineParsers.add(new SkillCastLineParser());
        lineParsers.add(new MeatLineParser(MeatGainType.ENCOUNTER));
        lineParsers.add(new MeatSpentLineParser());
        lineParsers.add(new StatLineParser());
        lineParsers.add(new MPGainLineParser(MPGainType.ENCOUNTER));
        lineParsers.add(new CombatRecognizerLineParser());
        lineParsers.add(new EquipmentLineParser(equipmentStack, familiarEquipmentMap));
        lineParsers.add(new OnTheTrailLineParser());
        lineParsers.add(new MafiaFreeRunawaysLineParser());
        lineParsers.add(new MafiaDisintegrateLineParser());
        lineParsers.add(new StarfishMPGainLineParser());
        lineParsers.add(new MafiaRedRayStatsLineParser());
        lineParsers.add(new MafiaBanishLineParser());
        // Add a note parser to encounter blocks
        if (Settings.getSettingBoolean("Include mafia log notes"))
            lineParsers.add(new NotesLineParser());
    }

    /**
     * {@inheritDoc}
     */
    public void parseBlock(
            final List<String> block, final LogDataHolder logData) {
        String turnSpentLine = block.get(0).startsWith(UsefulPatterns.SQUARE_BRACKET_OPEN) ? block.get(0)
                : block.get(1);
        final SingleTurn turn;
        final int dayNumber = logData.getLastDayChange().getDayNumber();

        // Parse hybridizing of DNA
        if (HYBRIDIZE_PATTERN.matcher(turnSpentLine).matches())
        {

            final Matcher result = HYBRIDIZE_PATTERN.matcher(turnSpentLine);

            if (result.find())
            {

                // Create an encounter with the name of the intrinsic
                // Log it on the following turn so it gets combined
                final SingleTurn tmp = new SingleTurn(HYBRIDIZING_AREANAME,
                        result.group(1),
                        logData.getLastTurnSpent().getTurnNumber() + 1,
                        dayNumber,
                        logData.getLastEquipmentChange(),
                        logData.getLastFamiliarChange());
                tmp.setTurnVersion(TurnVersion.OTHER);
                logData.addTurnSpent(tmp);
            }

            // Reset turnSpentLine to be line 3 or 4
            if (block.size() >= 3)
            	turnSpentLine = block.get(3).startsWith(UsefulPatterns.SQUARE_BRACKET_OPEN) ? block.get(3)
                  		: block.get(4);
            else 
            	return;
        }

        // Some areas have broken turn spent strings. If a turn is recognised as
        // being spent in such an area, the block will start with the encounter
        // name. We attempt to parse these encounters here.
        // If it is not a broken area, use the normal parsing.
        if (turnSpentLine.startsWith(ENCOUNTER_START_STRING)) {
            final String encounterName = turnSpentLine.substring(ENCOUNTER_START_STRING.length());
            final int turnNumber = logData.getLastTurnSpent().getTurnNumber() + 1;

            turn = new SingleTurn(encounterName,
                    encounterName,
                    turnNumber,
                    dayNumber,
                    logData.getLastEquipmentChange(),
                    logData.getLastFamiliarChange());
            turn.setTurnVersion(TurnVersion.OTHER);
        } else {
            // Area name
            final int positionTurnEndBrace = turnSpentLine.indexOf(UsefulPatterns.SQUARE_BRACKET_CLOSE);

            String areaName = turnSpentLine.substring(positionTurnEndBrace + 2);

            // Check whether there is a mapping for the given area name
            final String mappedAreaName = areaNameStandardizerMap.get(areaName);
            areaName = mappedAreaName != null ? mappedAreaName : areaName;

            // Special handling for crafting turns is needed, because mafia
            // screws up the turn number, plus the turn version of these should
            // be marked as OTHER, so this check has to be done anyway later on.
            final boolean isCraftingTurn = areaName.startsWith(COOKING_START_STRING)
                    || areaName.startsWith(MIXING_START_STRING)
                    || areaName.startsWith(SMITHING_START_STRING);

            // Turn number
            // Special handling for crafting turns, because mafia screws up the
            // turn numbers of those.
            int turnNumber;
            final int positionTurnStartBrace = turnSpentLine.indexOf(UsefulPatterns.SQUARE_BRACKET_OPEN);
            if (isCraftingTurn)
                turnNumber = Integer.parseInt(turnSpentLine.substring(positionTurnStartBrace + 1,
                        positionTurnEndBrace)) - 1;
            else
                turnNumber = Integer.parseInt(turnSpentLine.substring(positionTurnStartBrace + 1,
                        positionTurnEndBrace));

            // Now parse the encounter name.
            String encounterName = UsefulPatterns.EMPTY_STRING;
            boolean isMultipleCombatsHandling = false;
            for (final String line : block)
            {
                if (line.startsWith(ENCOUNTER_START_STRING)) {
                    if (line.length() == ENCOUNTER_START_STRING.length()) {
                        // Something strange happened here. Do not count this
                        // turn. (clicking on a already cleansed cyrpt area can
                        // result in this)
                        // We do however still want to parse all lines for
                        // possible information.
                        parseAllLines(block, logData);
                        return;
                    }

                    encounterName = line.substring(ENCOUNTER_START_STRING.length());
                    isMultipleCombatsHandling = MafiaSessionLogReader.BROKEN_AREAS_ENCOUNTER_SET.contains(line);

                    if (encounterName.contains("Rainy Fax Dreams on your Wedding Day") == false) {
                        break;
                    } else {
                        // Rain man has dual Encounter
                        continue;
                    }
                }
            }

            // If a combat may span over multiple turns, it will be handled in
            // here.
            if (isMultipleCombatsHandling) {
                areaName = encounterName;
                int combatCounter = -1;
                for (final String line : block)
                    if (line.startsWith(ENCOUNTER_START_STRING))
                        combatCounter++;

                // Every extra combat counted should be added now. This will
                // result in stats, meat and so on all being added to the last
                // combat only, but this problem shouldn't be happening often
                // enough to be a big deal. (currently only Ed the Undying falls
                // into this category here)
                for (int i = 0; i < combatCounter; i++) {
                    final SingleTurn tmp = new SingleTurn(areaName,
                            encounterName,
                            turnNumber,
                            dayNumber,
                            logData.getLastEquipmentChange(),
                            logData.getLastFamiliarChange());
                    tmp.setTurnVersion(TurnVersion.COMBAT);
                    logData.addTurnSpent(tmp);
                    turnNumber++;
                }
            }

            turn = new SingleTurn(areaName,
                    encounterName,
                    turnNumber,
                    dayNumber,
                    logData.getLastEquipmentChange(),
                    logData.getLastFamiliarChange());

            // Set turn version. If the turn is a crafting turn, or the area
            // name is inside the other-encounters set, set the turn version to
            // OTHER, otherwise set it to NONCOMBAT. Combats are recognised
            // separately.
            if (isCraftingTurn || OTHER_ENCOUNTER_AREAS_SET.contains(areaName))
                turn.setTurnVersion(TurnVersion.OTHER);
            else
                turn.setTurnVersion(TurnVersion.NONCOMBAT);
        }

        int turnNum = turn.getTurnNumber();
        
        
        // Check handling for special encounters. If the encounter is indeed
        // a special encounter, the specialEncounterHandling() method will
        // handle adding the turn to the LogDataHolder.
        if (!specialEncounterHandling(turn, block, logData))
            // Add the turn to the given LogDataHolder instance.
            logData.addTurnSpent(turn);

        parseAllLines(block, logData);

        lostCombatHandling(block, turn, logData);
    }

    /**
     * Parses all lines of the given block using the {@link LineParser}s from
     * the lineParsers list.
     */
    private void parseAllLines(
            final List<String> block, final LogDataHolder logData) {
        for (final String line : block)
            for (final LineParser lp : lineParsers)
                // If the line parser can parse the line, this method also
                // returns true. This is used to cut back on the amount of
                // loops.
                if (lp.parseLine(line, logData))
                    break;
    }

    /**
     * Handling of special encounters which need additional computation, because
     * most of the time KolMafia doesn't log them correctly, such as multi-turn
     * encounters (e.g. the shore).
     * <p>
     * If an encounter is special and thus was processed here, this method will
     * return {@code true} and otherwise {@code false}.
     * <p>
     * Additionally, this method has to and will handle adding the given turn to
     * the LogDataHolder if the encounter is special.
     *
     * @param turn
     *            The encounter which is tested on whether it is special.
     * @param block
     *            The current working block of an ascension log.
     * @param logData
     *            The LogDataHolder of an ascension log in which the data should
     *            be saved in.
     * @return {@code true} if this method did some computation, because the
     *         given encounter is special, otherwise {@code false}.
     */
    private boolean specialEncounterHandling(
            final SingleTurn turn, final List<String> block,
            final LogDataHolder logData) {
        if (turn.getAreaName().endsWith(SHORE_AREAS_END_STRING)) {
            final EquipmentChange lastEquipment = logData.getLastEquipmentChange();
            final FamiliarChange lastFamiliar = logData.getLastFamiliarChange();
            final List<SingleTurn> shoreTrips = Lists.newArrayList();

            // Normally, shore trips take 3 turns and 500 meat, but in the Way
            // Of The Surprising Fist challenge path, they take 5 turns and no
            // meat.
            if (logData.getAscensionPath() == AscensionPath.WAY_OF_THE_SURPRISING_FIST) {
                shoreTrips.add(turn);
                shoreTrips.add(new SingleTurn(turn.getAreaName(),
                        turn.getEncounterName(),
                        turn.getTurnNumber() + 1,
                        turn.getDayNumber(),
                        lastEquipment,
                        lastFamiliar));
                shoreTrips.add(new SingleTurn(turn.getAreaName(),
                        turn.getEncounterName(),
                        turn.getTurnNumber() + 2,
                        turn.getDayNumber(),
                        lastEquipment,
                        lastFamiliar));
                shoreTrips.add(new SingleTurn(turn.getAreaName(),
                        turn.getEncounterName(),
                        turn.getTurnNumber() + 3,
                        turn.getDayNumber(),
                        lastEquipment,
                        lastFamiliar));
                shoreTrips.add(new SingleTurn(turn.getAreaName(),
                        turn.getEncounterName(),
                        turn.getTurnNumber() + 4,
                        turn.getDayNumber(),
                        lastEquipment,
                        lastFamiliar));
            } else {
                shoreTrips.add(turn);
                shoreTrips.add(new SingleTurn(turn.getAreaName(),
                        turn.getEncounterName(),
                        turn.getTurnNumber() + 1,
                        turn.getDayNumber(),
                        lastEquipment,
                        lastFamiliar));
                shoreTrips.add(new SingleTurn(turn.getAreaName(),
                        turn.getEncounterName(),
                        turn.getTurnNumber() + 2,
                        turn.getDayNumber(),
                        lastEquipment,
                        lastFamiliar));

                // Shore trip costs 500 meat.
                logData.getLastTurnSpent().addMeat(new MeatGain(0, 0, 500));
            }

            for (final SingleTurn st : shoreTrips) {
                st.setTurnVersion(TurnVersion.OTHER);
                logData.addTurnSpent(st);
            }

            return true;
        } else if (turn.getEncounterName().equals(CLOWNLORD_CHOICE_ENCOUNTER_STRING)) {
            String firstChoice = UsefulPatterns.EMPTY_STRING;
            String secondChoice = UsefulPatterns.EMPTY_STRING;
            for (final String line : block)
                if (line.contains("choice.php?")) {
                    final String tmp = line.replace("pwd", "").replace("&", "");
                    if (firstChoice.equals(UsefulPatterns.EMPTY_STRING))
                        firstChoice = tmp;
                    else
                        secondChoice = tmp;
                }

            // Check if the correct choices were taken.
            if (firstChoice.equals("choice.php?whichchoice=151option=1")
                    && secondChoice.equals("choice.php?whichchoice=152option=1")) {
                final SingleTurn clownlord = new SingleTurn(turn.getAreaName(),
                        "Clownlord Beelzebozo",
                        turn.getTurnNumber() + 1,
                        turn.getDayNumber(),
                        logData.getLastEquipmentChange(),
                        logData.getLastFamiliarChange());

                logData.addTurnSpent(turn);
                logData.addTurnSpent(clownlord);

                return true;
            }
        } else if (GAME_GRID_AREAS_SET.contains(turn.getAreaName())) {
            final EquipmentChange lastEquipment = logData.getLastEquipmentChange();
            final FamiliarChange lastFamiliar = logData.getLastFamiliarChange();

            turn.setTurnVersion(TurnVersion.OTHER);
            logData.addTurnSpent(turn);
            for (int i = 1; i < 5; i++) {
                final SingleTurn tmpTurn = new SingleTurn(turn.getAreaName(),
                        turn.getEncounterName(),
                        turn.getTurnNumber() + i,
                        turn.getDayNumber(),
                        lastEquipment,
                        lastFamiliar);
                tmpTurn.setTurnVersion(TurnVersion.OTHER);
                logData.addTurnSpent(tmpTurn);
            }

            return true;
        }

        return false;
    }

    /**
     * Checks whether the given turn was a lost combat and if true, logs it as
     * such.
     */
    private void lostCombatHandling(
            final List<String> block, final SingleTurn turn,
            final LogDataHolder logData) {
        if (turn.getTurnVersion() != TurnVersion.COMBAT)
            return;

        boolean isPotentiallyLost = false;

        // Check whether the turn was lost. (If you lose a combat the last line
        // of the block is you losing HP.)
        String lastLine = block.get(block.size() - 1);
        if (lastLine.contains(OUTFIT_STRING) || lastLine.startsWith(MCD_STRING))
            lastLine = block.get(block.size() - 2);
        if (lastLine.startsWith(HP_LOSE_STRING_BEGINNING)
                && HP_LOSE_PATTERN.matcher(lastLine).matches())
            isPotentiallyLost = true;

        if (isPotentiallyLost) {
            // In case the encounter block contains multiple encounters, we only
            // want to check the very last encounter for whether it was won or
            // lost.
            int lastEncounterLineIndex = 0;
            if (!turn.getEncounterName().equals(UsefulPatterns.EMPTY_STRING))
                for (int i = block.size() - 1; i >= 0; i--)
                    if (block.get(i).startsWith(ENCOUNTER_START_STRING)) {
                        lastEncounterLineIndex = i;
                        break;
                    }

            // Check for KolMafia's won fight message, if it occurred, tread
            // this combat as not lost.
            for (int i = lastEncounterLineIndex; i < block.size(); i++)
                if (FIGHT_WON_PATTERN.matcher(block.get(i)).matches()) {
                    isPotentiallyLost = false;
                    break;
                }
        }

        // Log the lost combat.
        if (isPotentiallyLost)
            logData.addLostCombat(DataNumberPair.of(turn.getEncounterName(), turn.getTurnNumber()));
    }
}
