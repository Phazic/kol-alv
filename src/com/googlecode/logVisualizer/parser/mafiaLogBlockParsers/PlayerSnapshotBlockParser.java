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

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.googlecode.logVisualizer.logData.LogDataHolder;
import com.googlecode.logVisualizer.logData.LogDataHolder.CharacterClass;
import com.googlecode.logVisualizer.logData.turn.turnAction.DayChange;
import com.googlecode.logVisualizer.logData.turn.turnAction.EquipmentChange;
import com.googlecode.logVisualizer.logData.turn.turnAction.FamiliarChange;
import com.googlecode.logVisualizer.logData.turn.turnAction.PlayerSnapshot;
import com.googlecode.logVisualizer.parser.UsefulPatterns;
import com.googlecode.logVisualizer.util.Stack;

/**
 * A parser for the player login snapshot data in mafia logs.
 * <p>
 * The snapshot start with this:
 * <p>
 *
 * <pre>
 * =-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
 *                Player Snapshot
 * =-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
 * </pre>
 * <p>
 * And ends with this:
 * <p>
 *
 * <pre>
 * =-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
 * </pre>
 */
public final class PlayerSnapshotBlockParser implements LogBlockParser {
    private static final Pattern PLAYERSTATS_WBUFFED_PATTERN = Pattern.compile("(?:Mus|Mys|Mox)\\: \\d+ \\(\\d+\\).*");

    private static final Pattern PLAYERSTATS_WOBUFFED_PATTERN = Pattern.compile("(?:Mus|Mys|Mox)\\: \\d+(?:$|, tnp =.*)");

    private static final Pattern NOT_FAMILIAR_NAME_PATTERN = Pattern.compile("Pet: | \\(\\d+ lbs\\)\\s*");

    private static final String CLASS_LINE_BEGINNING_STRING = "Class: ";

    private static final String FAMILIAR_LINE_BEGINNING_STRING = "Pet: ";

    private static final String ADVENTURES_LINE_BEGINNING_STRING = "Advs: ";

    private static final String MEAT_LINE_BEGINNING_STRING = "Meat: ";

    private static final String HAT_BEGINNING_STRING = "Hat: ";

    private static final String WEAPON_BEGINNING_STRING = "Weapon: ";

    private static final String OFFHAND_BEGINNING_STRING = "Off-hand: ";

    private static final String SHIRT_BEGINNING_STRING = "Shirt: ";

    private static final String PANTS_BEGINNING_STRING = "Pants: ";

    private static final String ACC1_BEGINNING_STRING = "Acc. 1: ";

    private static final String ACC2_BEGINNING_STRING = "Acc. 2: ";

    private static final String ACC3_BEGINNING_STRING = "Acc. 3: ";

    private static final String FAM_EQUIP_BEGINNING_STRING = "Item: ";

    private static final String NO_EQUIP_STRING = "(none)";

    private static final String DAY_CHANGE_STRING = "Day change occurred";

    private final Matcher statsWithBuffed = PLAYERSTATS_WBUFFED_PATTERN.matcher(UsefulPatterns.EMPTY_STRING);

    private final Matcher statsWithoutBuffed = PLAYERSTATS_WOBUFFED_PATTERN.matcher(UsefulPatterns.EMPTY_STRING);

    private final Stack<EquipmentChange> equipmentStack;

    private final Map<String, String> familiarEquipmentMap;

    public PlayerSnapshotBlockParser(
            final Stack<EquipmentChange> equipmentStack,
            final Map<String, String> familiarEquipmentMap) {
        this.equipmentStack = equipmentStack;
        this.familiarEquipmentMap = familiarEquipmentMap;
    }

    /**
     * {@inheritDoc}
     */
    public void parseBlock(
            final List<String> block, final LogDataHolder logData) {
        String hat = EquipmentChange.NO_EQUIPMENT_STRING;
        String weapon = EquipmentChange.NO_EQUIPMENT_STRING;
        String offhand = EquipmentChange.NO_EQUIPMENT_STRING;
        String shirt = EquipmentChange.NO_EQUIPMENT_STRING;
        String pants = EquipmentChange.NO_EQUIPMENT_STRING;
        String acc1 = EquipmentChange.NO_EQUIPMENT_STRING;
        String acc2 = EquipmentChange.NO_EQUIPMENT_STRING;
        String acc3 = EquipmentChange.NO_EQUIPMENT_STRING;
        String famEquip = EquipmentChange.NO_EQUIPMENT_STRING;
        final int turnNumber = logData.getLastTurnSpent().getTurnNumber();
        int mus = -1;
        int myst = -1;
        int mox = -1;
        int adventuresLeft = 0;
        int meat = 0;

        for (final String line : block)
            if (line.length() > 0)
                if (statsWithBuffed.reset(line).matches()) {
                    if (mus < 0)
                        mus = parseStatWBuffed(line);
                    else if (myst < 0)
                        myst = parseStatWBuffed(line);
                    else if (mox < 0)
                        mox = parseStatWBuffed(line);
                } else if (statsWithoutBuffed.reset(line).matches()) {
                    if (mus < 0)
                        mus = parseStatWOBuffed(line);
                    else if (myst < 0)
                        myst = parseStatWOBuffed(line);
                    else if (mox < 0)
                        mox = parseStatWOBuffed(line);
                } else if (line.startsWith(FAMILIAR_LINE_BEGINNING_STRING)) {
                    final Scanner s = new Scanner(line);
                    s.useDelimiter(NOT_FAMILIAR_NAME_PATTERN);

                    // Don't record familiar clearing if path is Actually Ed.
                    if (logData.getAscensionPath() != LogDataHolder.AscensionPath.ED)
                    {
                        logData.addFamiliarChange(new FamiliarChange(s.next(), turnNumber));
                    }

                    s.close();
                } else if (line.startsWith(ADVENTURES_LINE_BEGINNING_STRING))
                    adventuresLeft = Integer.parseInt(line.substring(line.indexOf(UsefulPatterns.COLON) + 2));
                else if (line.startsWith(MEAT_LINE_BEGINNING_STRING)
                        && !line.contains(UsefulPatterns.PERCENTAGE_SIGN))
                    meat = Integer.parseInt(line.substring(line.indexOf(UsefulPatterns.COLON) + 2)
                            .replace(UsefulPatterns.COMMA,
                                    UsefulPatterns.EMPTY_STRING));
                else if (line.startsWith(HAT_BEGINNING_STRING))
                    hat = getEquipmentName(line);
                else if (line.startsWith(WEAPON_BEGINNING_STRING))
                    weapon = getEquipmentName(line);
                else if (line.startsWith(OFFHAND_BEGINNING_STRING))
                    offhand = getEquipmentName(line);
                else if (line.startsWith(SHIRT_BEGINNING_STRING))
                    shirt = getEquipmentName(line);
                else if (line.startsWith(PANTS_BEGINNING_STRING))
                    pants = getEquipmentName(line);
                else if (line.startsWith(ACC1_BEGINNING_STRING))
                    acc1 = getEquipmentName(line);
                else if (line.startsWith(ACC2_BEGINNING_STRING))
                    acc2 = getEquipmentName(line);
                else if (line.startsWith(ACC3_BEGINNING_STRING))
                    acc3 = getEquipmentName(line);
                else if (line.startsWith(FAM_EQUIP_BEGINNING_STRING)
                        && !line.contains(UsefulPatterns.PERCENTAGE_SIGN))
                    famEquip = getEquipmentName(line);
                else if (line.startsWith(DAY_CHANGE_STRING)) {
                    // Get day number of last day change
                    final int dayNumber = logData.getLastDayChange().getDayNumber();

                    // Get turn number of last turn spent
                    final int turn = logData.getLastTurnSpent().getTurnNumber();

                    // Add day change
                    logData.addDayChange(new DayChange(dayNumber + 1, turn));
                } else if (logData.getCharacterClass() == CharacterClass.NOT_DEFINED)
                    if (line.startsWith(CLASS_LINE_BEGINNING_STRING))
                        logData.setCharacterClass(line.substring(CLASS_LINE_BEGINNING_STRING.length()));

        // Add the currently worn equipment.
        familiarEquipmentMap.put(logData.getLastFamiliarChange().getFamiliarName(), famEquip);
        final EquipmentChange equip = new EquipmentChange(turnNumber,
                hat,
                weapon,
                offhand,
                shirt,
                pants,
                acc1,
                acc2,
                acc3,
                famEquip);
        if (!equip.equalsIgnoreTurn(equipmentStack.peek().get())) {
            equipmentStack.push(equip);
            logData.addEquipmentChange(equip);
        }

        // A check to make sure the parsing worked, if it did, add the player
        // snapshot.
        if (mus >= 0 && myst >= 0 && mox >= 0)
            logData.addPlayerSnapshot(new PlayerSnapshot(mus,
                    myst,
                    mox,
                    adventuresLeft,
                    meat,
                    turnNumber));
    }

    private String getEquipmentName(
            final String line) {
        String itemName = line.substring(line.indexOf(UsefulPatterns.COLON) + 2)
                .toLowerCase(Locale.ENGLISH);

        if (itemName.contains(NO_EQUIP_STRING))
            itemName = EquipmentChange.NO_EQUIPMENT_STRING;
        else if (itemName.endsWith(UsefulPatterns.ROUND_BRACKET_CLOSE))
            // If there is something in brackets in the end simply ignore that,
            // if the whole name is in brackets just pull the brackets out.
            if (itemName.startsWith(UsefulPatterns.ROUND_BRACKET_OPEN))
                itemName = itemName.substring(1, itemName.length() - 2);
            else
                itemName = itemName.substring(0,
                        itemName.lastIndexOf(UsefulPatterns.ROUND_BRACKET_OPEN) - 1);

        return itemName;
    }

    private static int parseStatWBuffed(
            final String line) {
        return Integer.parseInt(line.substring(line.indexOf(UsefulPatterns.ROUND_BRACKET_OPEN) + 1,
                line.indexOf(UsefulPatterns.ROUND_BRACKET_CLOSE)));
    }

    private static int parseStatWOBuffed(
            final String line) {
        final String tmp = line.substring(line.indexOf(UsefulPatterns.WHITE_SPACE) + 1);

        if (tmp.contains(UsefulPatterns.COMMA))
            return Integer.parseInt(tmp.substring(0, tmp.indexOf(UsefulPatterns.COMMA)));

        return Integer.parseInt(tmp);
    }
}
