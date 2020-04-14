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

package com.googlecode.alv.parser.lineParsers;

import java.util.Locale;
import java.util.Map;

import com.googlecode.alv.logData.LogDataHolder;
import com.googlecode.alv.logData.turn.turnAction.EquipmentChange;
import com.googlecode.alv.parser.UsefulPatterns;
import com.googlecode.alv.util.Maps;
import com.googlecode.alv.util.Stack;
import com.googlecode.alv.util.dataTables.DataTablesHandler;
import com.googlecode.alv.util.dataTables.Outfit;

/**
 * A parser for the equipment change notation in mafia session logs.
 * <p>
 * The format looks like this:
 * <p>
 * {@code equip _slotName_ _itemName_}
 * <p>
 * OR
 * <p>
 * {@code unequip _slotName_ _itemName_}
 * <p>
 * OR
 * <p>
 * {@code outfit _outfitName_}
 * <p>
 * OR
 * <p>
 * {@code custom outfit _outfitName_}
 */
public final class EquipmentLineParser extends AbstractLineParser {
    private static final String EQUIP_STRING = "equip";

    private static final String UNEQUIP_STRING = "unequip";

    private static final String FAM_UNEQUIP_STRING = "Unequip";

    private static final String OUTFIT_STRING = "outfit";

    private static final String CUSTOM_OUTFIT_STRING = "custom outfit";

    private final Stack<EquipmentChange> usedEquipment;

    private final Map<String, String> familiarEquipmentMap;

    public EquipmentLineParser(
                               final Stack<EquipmentChange> equipmentStack,
                               final Map<String, String> familiarEquipmentMap) {
        usedEquipment = equipmentStack;
        this.familiarEquipmentMap = familiarEquipmentMap;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean parseLine(
                             final String line, final LogDataHolder logData) {
        // Sometimes the action classifier strings (equip/unequip/outfit) are
        // written with a capital first letter, sometimes not. This side-steps
        // that problem.
        return super.parseLine(line.toLowerCase(Locale.ENGLISH), logData);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doParsing(
                             final String line, final LogDataHolder logData) {
        // Outfit handling
        if (line.startsWith(OUTFIT_STRING)) {
            final Outfit outfit = DataTablesHandler.HANDLER.getOutfitChange(line.substring(line.indexOf(UsefulPatterns.WHITE_SPACE) + 1));
            if (outfit != null) {
                final EquipmentChange lastChange = usedEquipment.peek().get();

                final String hat = outfit.hat ? EquipmentChange.NO_EQUIPMENT_STRING
                                             : lastChange.getHat();
                final String weapon = outfit.weapon ? EquipmentChange.NO_EQUIPMENT_STRING
                                                   : lastChange.getWeapon();
                final String offhand = outfit.offhand ? EquipmentChange.NO_EQUIPMENT_STRING
                                                     : lastChange.getOffhand();
                final String shirt = outfit.shirt ? EquipmentChange.NO_EQUIPMENT_STRING
                                                 : lastChange.getShirt();
                final String pants = outfit.pants ? EquipmentChange.NO_EQUIPMENT_STRING
                                                 : lastChange.getPants();
                final String acc1 = outfit.acc1 ? EquipmentChange.NO_EQUIPMENT_STRING
                                               : lastChange.getAcc1();
                final String acc2 = outfit.acc2 ? EquipmentChange.NO_EQUIPMENT_STRING
                                               : lastChange.getAcc2();
                final String acc3 = outfit.acc3 ? EquipmentChange.NO_EQUIPMENT_STRING
                                               : lastChange.getAcc3();

                addEquipmentChangeToLog(new EquipmentChange(logData.getLastTurnSpent()
                                                                   .getTurnNumber(),
                                                            hat,
                                                            weapon,
                                                            offhand,
                                                            shirt,
                                                            pants,
                                                            acc1,
                                                            acc2,
                                                            acc3,
                                                            lastChange.getFamEquip()), logData);
            }
            return;
        } else if (line.startsWith(CUSTOM_OUTFIT_STRING)) {
            if (line.equals("custom outfit backup")
                || line.equals("custom outfit your previous outfit")) {
                // Those two custom outfits roll the equipment back to the last
                // used one, so we do the same.
                usedEquipment.pop();
                final EquipmentChange lastUsedEquipment = usedEquipment.isEmpty() ? EquipmentChange.NO_EQUIPMENT
                                                                                 : usedEquipment.peek()
                                                                                                .get();
                logData.addEquipmentChange(new EquipmentChange(logData.getLastTurnSpent()
                                                                      .getTurnNumber(),
                                                               lastUsedEquipment.getHat(),
                                                               lastUsedEquipment.getWeapon(),
                                                               lastUsedEquipment.getOffhand(),
                                                               lastUsedEquipment.getShirt(),
                                                               lastUsedEquipment.getPants(),
                                                               lastUsedEquipment.getAcc1(),
                                                               lastUsedEquipment.getAcc2(),
                                                               lastUsedEquipment.getAcc3(),
                                                               lastUsedEquipment.getFamEquip()));
            } else {
                // We cannot guarantee for anything as far as custom outfits are
                // concerned, so the only sensible thing is to assume nothing in
                // the way of character equipment.
                final EquipmentChange lastChange = usedEquipment.peek().get();
                addEquipmentChangeToLog(new EquipmentChange(logData.getLastTurnSpent()
                                                                   .getTurnNumber(),
                                                            EquipmentChange.NO_EQUIPMENT_STRING,
                                                            EquipmentChange.NO_EQUIPMENT_STRING,
                                                            EquipmentChange.NO_EQUIPMENT_STRING,
                                                            EquipmentChange.NO_EQUIPMENT_STRING,
                                                            EquipmentChange.NO_EQUIPMENT_STRING,
                                                            EquipmentChange.NO_EQUIPMENT_STRING,
                                                            EquipmentChange.NO_EQUIPMENT_STRING,
                                                            EquipmentChange.NO_EQUIPMENT_STRING,
                                                            lastChange.getFamEquip()), logData);
            }
            return;
        }

        // Equip/Unequip handling
        final String tmp = line.substring(line.indexOf(UsefulPatterns.WHITE_SPACE) + 1);
        final int whiteSpaceIndex = tmp.indexOf(UsefulPatterns.WHITE_SPACE);

        // Familiar equipment removed from a familiar that isn't currently used.
        if (line.startsWith(FAM_UNEQUIP_STRING)) {
            familiarEquipmentMap.put(tmp, EquipmentChange.NO_EQUIPMENT_STRING);
            return;
        }

        // Unequip string sometimes looks different than the equip string
        if (line.startsWith(UNEQUIP_STRING) && whiteSpaceIndex == -1) {
            addEquipmentChange(tmp, EquipmentChange.NO_EQUIPMENT_STRING, logData);
            return;
        }

        // Strings that don't fit the format should be ignored.
        if (whiteSpaceIndex < 0)
            return;

        final String slotName = tmp.substring(0, whiteSpaceIndex);
        final String itemName = tmp.substring(whiteSpaceIndex + 1);

        // Act depending on equip or unequip.
        if (line.startsWith(EQUIP_STRING))
            addEquipmentChange(slotName, itemName, logData);
        else
            addEquipmentChange(slotName, EquipmentChange.NO_EQUIPMENT_STRING, logData);
    }

    private void addEquipmentChange(
                                    final String slotName, final String itemName,
                                    final LogDataHolder logData) {
        final EquipmentChange lastChange = usedEquipment.isEmpty() ? EquipmentChange.NO_EQUIPMENT
                                                                  : usedEquipment.peek().get();
        // Switch constructs are ugly, but there isn't really a better way to do
        // this since at some point which equipment slot is used has to be
        // checked.
        final EquipmentChange equipmentChange;
        switch (EquipmentSlot.fromString(slotName)) {
            case HAT:
                equipmentChange = new EquipmentChange(logData.getLastTurnSpent().getTurnNumber(),
                                                      itemName,
                                                      lastChange.getWeapon(),
                                                      lastChange.getOffhand(),
                                                      lastChange.getShirt(),
                                                      lastChange.getPants(),
                                                      lastChange.getAcc1(),
                                                      lastChange.getAcc2(),
                                                      lastChange.getAcc3(),
                                                      lastChange.getFamEquip());
                break;
            case WEAPON:
                equipmentChange = new EquipmentChange(logData.getLastTurnSpent().getTurnNumber(),
                                                      lastChange.getHat(),
                                                      itemName,
                                                      lastChange.getOffhand(),
                                                      lastChange.getShirt(),
                                                      lastChange.getPants(),
                                                      lastChange.getAcc1(),
                                                      lastChange.getAcc2(),
                                                      lastChange.getAcc3(),
                                                      lastChange.getFamEquip());
                break;
            case OFF_HAND:
            case OFFHAND:
                equipmentChange = new EquipmentChange(logData.getLastTurnSpent().getTurnNumber(),
                                                      lastChange.getHat(),
                                                      lastChange.getWeapon(),
                                                      itemName,
                                                      lastChange.getShirt(),
                                                      lastChange.getPants(),
                                                      lastChange.getAcc1(),
                                                      lastChange.getAcc2(),
                                                      lastChange.getAcc3(),
                                                      lastChange.getFamEquip());
                break;
            case SHIRT:
                equipmentChange = new EquipmentChange(logData.getLastTurnSpent().getTurnNumber(),
                                                      lastChange.getHat(),
                                                      lastChange.getWeapon(),
                                                      lastChange.getOffhand(),
                                                      itemName,
                                                      lastChange.getPants(),
                                                      lastChange.getAcc1(),
                                                      lastChange.getAcc2(),
                                                      lastChange.getAcc3(),
                                                      lastChange.getFamEquip());
                break;
            case PANTS:
                equipmentChange = new EquipmentChange(logData.getLastTurnSpent().getTurnNumber(),
                                                      lastChange.getHat(),
                                                      lastChange.getWeapon(),
                                                      lastChange.getOffhand(),
                                                      lastChange.getShirt(),
                                                      itemName,
                                                      lastChange.getAcc1(),
                                                      lastChange.getAcc2(),
                                                      lastChange.getAcc3(),
                                                      lastChange.getFamEquip());
                break;
            case ACC1:
                equipmentChange = new EquipmentChange(logData.getLastTurnSpent().getTurnNumber(),
                                                      lastChange.getHat(),
                                                      lastChange.getWeapon(),
                                                      lastChange.getOffhand(),
                                                      lastChange.getShirt(),
                                                      lastChange.getPants(),
                                                      itemName,
                                                      lastChange.getAcc2(),
                                                      lastChange.getAcc3(),
                                                      lastChange.getFamEquip());
                break;
            case ACC2:
                equipmentChange = new EquipmentChange(logData.getLastTurnSpent().getTurnNumber(),
                                                      lastChange.getHat(),
                                                      lastChange.getWeapon(),
                                                      lastChange.getOffhand(),
                                                      lastChange.getShirt(),
                                                      lastChange.getPants(),
                                                      lastChange.getAcc1(),
                                                      itemName,
                                                      lastChange.getAcc3(),
                                                      lastChange.getFamEquip());
                break;
            case ACC3:
                equipmentChange = new EquipmentChange(logData.getLastTurnSpent().getTurnNumber(),
                                                      lastChange.getHat(),
                                                      lastChange.getWeapon(),
                                                      lastChange.getOffhand(),
                                                      lastChange.getShirt(),
                                                      lastChange.getPants(),
                                                      lastChange.getAcc1(),
                                                      lastChange.getAcc2(),
                                                      itemName,
                                                      lastChange.getFamEquip());
                break;
            case FAMEQUIP:
            case FAM_EQUIP:
                familiarEquipmentMap.put(logData.getLastFamiliarChange().getFamiliarName(),
                                         itemName);
                equipmentChange = new EquipmentChange(logData.getLastTurnSpent().getTurnNumber(),
                                                      lastChange.getHat(),
                                                      lastChange.getWeapon(),
                                                      lastChange.getOffhand(),
                                                      lastChange.getShirt(),
                                                      lastChange.getPants(),
                                                      lastChange.getAcc1(),
                                                      lastChange.getAcc2(),
                                                      lastChange.getAcc3(),
                                                      itemName);
                break;
            default:
                equipmentChange = null;
                break;
        }

        // Add the equipment change.
        if (equipmentChange != null)
            addEquipmentChangeToLog(equipmentChange, logData);
    }

    private void addEquipmentChangeToLog(
                                         final EquipmentChange equipmentChange,
                                         final LogDataHolder logData) {
        usedEquipment.push(equipmentChange);
        logData.addEquipmentChange(equipmentChange);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean isCompatibleLine(
                                       final String line) {
        return line.startsWith(EQUIP_STRING) || line.startsWith(UNEQUIP_STRING)
               || line.startsWith(OUTFIT_STRING) || line.startsWith(CUSTOM_OUTFIT_STRING)
               || line.startsWith(FAM_UNEQUIP_STRING);
    }

    /**
     * This enumeration represents all equipment slots.
     */
    private static enum EquipmentSlot {
        HAT("hat"),
        WEAPON("weapon"),
        OFF_HAND("off-hand"),
        OFFHAND("offhand"),
        SHIRT("shirt"),
        PANTS("pants"),
        ACC1("acc1"),
        ACC2("acc2"),
        ACC3("acc3"),
        FAMEQUIP("familiarequip"),
        FAM_EQUIP("familiar"),
        NOT_DEFINED("not defined");

        private static final Map<String, EquipmentSlot> stringToEnum = Maps.newHashMap();

        static {
            for (final EquipmentSlot op : values())
                stringToEnum.put(op.toString(), op);
        }

        private final String slotName;

        EquipmentSlot(
                      final String slotName) {
            this.slotName = slotName;
        }

        @Override
        public String toString() {
            return slotName;
        }

        /**
         * @return The enum whose toString method returns a string which is
         *         equal to the given string. If no match is found this method
         *         will return {@code NOT_DEFINED}.
         */
        public static EquipmentSlot fromString(
                                               final String slotName) {
            if (slotName == null)
                throw new NullPointerException("Slot name must not be null.");

            final EquipmentSlot equipmentSlot = stringToEnum.get(slotName);

            return equipmentSlot != null ? equipmentSlot : NOT_DEFINED;
        }
    }
}
