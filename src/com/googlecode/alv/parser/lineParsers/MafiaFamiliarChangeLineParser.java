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

package com.googlecode.alv.parser.lineParsers;

import java.util.Map;
import java.util.Scanner;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.googlecode.alv.logData.LogDataHolder;
import com.googlecode.alv.logData.turn.turnAction.EquipmentChange;
import com.googlecode.alv.logData.turn.turnAction.FamiliarChange;
import com.googlecode.alv.parser.UsefulPatterns;
import com.googlecode.alv.util.Stack;

/**
 * A parser for the familiar change notation in mafia logs.
 * <p>
 * The format looks like this:
 * <p>
 * {@code familiar _familiarClassName_ (_weight_ lbs)}
 */
public final class MafiaFamiliarChangeLineParser extends AbstractLineParser {
    private static final Pattern FAMILIAR_CHANGE_CAPTURE_PATTERN = Pattern.compile("familiar ([\\w\\p{Punct}\\s]+) \\((\\d+) lbs\\)");

    private static final Pattern  ED_CHANGE_SERVANT_PATTERN = Pattern.compile("choice\\.php\\?whichchoice=1053&option=[0-9].*&sid=([0-9])");

    private final Matcher edChangedServant = ED_CHANGE_SERVANT_PATTERN.matcher(UsefulPatterns.EMPTY_STRING);

    private static final String FAMILIAR_CHANGE_START_STRING = "familiar ";

    private static final String NO_FAMILIAR_STRING = "none";

    private static final String LOCK_STRING = "lock";

    private final Stack<EquipmentChange> usedEquipment;

    private final Map<String, String> familiarEquipmentMap;

    public MafiaFamiliarChangeLineParser(
            final Stack<EquipmentChange> equipmentStack,
            final Map<String, String> familiarEquipmentMap) {
        usedEquipment = equipmentStack;
        this.familiarEquipmentMap = familiarEquipmentMap;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doParsing(
            final String line, final LogDataHolder logData) {
        if (!line.endsWith(LOCK_STRING)) {

            boolean edChanged = edChangedServant.reset(line).find();

            final String familiarName;

            if (edChanged)
            {
                int sid = Integer.parseInt(edChangedServant.group(1));

                switch (sid)
                {
                case 1:
                    familiarName = "Cat";
                    break;
                case 2:
                    familiarName = "Belly-Dancer";
                    break;
                case 3:
                    familiarName = "Maid";
                    break;
                case 4:
                    familiarName = "Bodyguard";
                    break;
                case 5:
                    familiarName = "Scribe";
                    break;
                case 6:
                    familiarName = "Priest";
                    break;
                case 7:
                    familiarName = "Assassin";
                    break;
                default:
                    familiarName = "Unknown";
                }

            }
            else
            {
                if (!line.endsWith(NO_FAMILIAR_STRING)) {
                    final Scanner scanner = new Scanner(line);
                    scanner.findInLine(FAMILIAR_CHANGE_CAPTURE_PATTERN);
                    final MatchResult result = scanner.match();

                    familiarName = result.group(1);
                } else
                    familiarName = NO_FAMILIAR_STRING;

                final EquipmentChange lastChange = usedEquipment.peek().get();
                final EquipmentChange equipmentChange;
                if (familiarName == NO_FAMILIAR_STRING)
                    equipmentChange = new EquipmentChange(logData.getLastTurnSpent().getTurnNumber(),
                            lastChange.getHat(),
                            lastChange.getWeapon(),
                            lastChange.getOffhand(),
                            lastChange.getShirt(),
                            lastChange.getPants(),
                            lastChange.getAcc1(),
                            lastChange.getAcc2(),
                            lastChange.getAcc3(),
                            EquipmentChange.NO_EQUIPMENT_STRING);
                else {
                    final String famEquip = familiarEquipmentMap.get(familiarName);
                    equipmentChange = new EquipmentChange(logData.getLastTurnSpent().getTurnNumber(),
                            lastChange.getHat(),
                            lastChange.getWeapon(),
                            lastChange.getOffhand(),
                            lastChange.getShirt(),
                            lastChange.getPants(),
                            lastChange.getAcc1(),
                            lastChange.getAcc2(),
                            lastChange.getAcc3(),
                            famEquip != null ? famEquip
                                    : EquipmentChange.NO_EQUIPMENT_STRING);
                }
                usedEquipment.push(equipmentChange);
                logData.addEquipmentChange(equipmentChange);
            }

            logData.addFamiliarChange(new FamiliarChange(familiarName, logData.getLastTurnSpent()
                    .getTurnNumber()));

        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean isCompatibleLine(
            final String line) {

        boolean famChanged = line.startsWith(FAMILIAR_CHANGE_START_STRING);

        boolean edChanged = edChangedServant.reset(line).matches();

        return famChanged || edChanged;
    }
}
