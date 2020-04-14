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

package com.googlecode.alv.logData.turn.turnAction;

/**
 * This immutable class is a representation of an equipment change. It holds the
 * turn number of when the change occurred and the names of all the equipment
 * used at that particular turn after the change.
 * <p>
 * If no equipment was worn in a particular slot, that slot will return
 * {@link #NO_EQUIPMENT_STRING}.
 * <p>
 * Note: This class has a natural ordering that is inconsistent with equals.
 */
public final class EquipmentChange extends AbstractTurnAction<EquipmentChange> {
    public static final EquipmentChange NO_EQUIPMENT = new EquipmentChange(0);

    public static final String NO_EQUIPMENT_STRING = "none";

    private final String hat;

    private final String weapon;

    private final String offhand;

    private final String shirt;

    private final String pants;

    private final String acc1;

    private final String acc2;

    private final String acc3;

    private final String famEquip;

    /**
     * Constructs a new equipment change object.
     * <p>
     * The object will be initialised with no equipment being worn.
     * 
     * @param turnNumber
     *            The turn number of this equipment change to set.
     * @throws IllegalArgumentException
     *             if turnNumber is below 0
     */
    public EquipmentChange(
                           final int turnNumber) {
        this(turnNumber,
             NO_EQUIPMENT_STRING,
             NO_EQUIPMENT_STRING,
             NO_EQUIPMENT_STRING,
             NO_EQUIPMENT_STRING,
             NO_EQUIPMENT_STRING,
             NO_EQUIPMENT_STRING,
             NO_EQUIPMENT_STRING,
             NO_EQUIPMENT_STRING,
             NO_EQUIPMENT_STRING);
    }

    /**
     * Constructs a new equipment change object.
     * <p>
     * In case one wants to denote a particular slot as not sporting any
     * equipment, one has to pass {@link #NO_EQUIPMENT_STRING} for the given
     * slot.
     * 
     * @param turnNumber
     *            The turn number of this equipment change to set.
     * @param hat
     *            The name of the equipment used in the hat slot.
     * @param weapon
     *            The name of the equipment used in the weapon slot.
     * @param offhand
     *            The name of the equipment used in the offhand slot.
     * @param shirt
     *            The name of the equipment used in the shirt slot.
     * @param pants
     *            The name of the equipment used in the pants slot.
     * @param acc1
     *            The name of the equipment used in the acc1 slot.
     * @param acc2
     *            The name of the equipment used in the acc2 slot.
     * @param acc3
     *            The name of the equipment used in the acc3 slot.
     * @param famEquip
     *            The name of the equipment used in the familiar equipment slot.
     * @throws IllegalArgumentException
     *             if turnNumber is below 0
     * @throws NullPointerException
     *             if one of the equipment names is {@code null}
     */
    public EquipmentChange(
                           final int turnNumber, final String hat, final String weapon,
                           final String offhand, final String shirt, final String pants,
                           final String acc1, final String acc2, final String acc3,
                           final String famEquip) {
        super(turnNumber);

        if (hat == null || weapon == null || offhand == null || shirt == null || pants == null
            || acc1 == null || acc2 == null || acc3 == null || famEquip == null)
            throw new NullPointerException("No null objects allowed as parameters.");

        this.hat = hat;
        this.weapon = weapon;
        this.offhand = offhand;
        this.shirt = shirt;
        this.pants = pants;
        this.acc1 = acc1;
        this.acc2 = acc2;
        this.acc3 = acc3;
        this.famEquip = famEquip;
    }

    /**
     * @param equipment
     *            The name of the equipment which is looked for.
     * @return {@code true} if one of the equipment slots has equipment with a
     *         name equal to the given one, otherwise {@code false}.
     */
    public boolean isEquiped(
                             final String equipment) {
        return hat.equals(equipment) || weapon.equals(equipment) || offhand.equals(equipment)
               || shirt.equals(equipment) || pants.equals(equipment) || acc1.equals(equipment)
               || acc2.equals(equipment) || acc3.equals(equipment) || famEquip.equals(equipment);
    }

    /**
     * @param equipment
     *            The name of the equipment which is looked for.
     * @return The amount of times the given equipment is used in any equipment
     *         slots.
     */
    public int getNumberOfEquips(
                                 final String equipment) {
        // Equipment that can only appear once.
        if (hat.equals(equipment) || shirt.equals(equipment) || famEquip.equals(equipment)
            || pants.equals(equipment))
            return 1;

        // Weapons can appear once or twice.
        if (weapon.equals(equipment))
            return offhand.equals(equipment) ? 2 : 1;
        if (offhand.equals(equipment))
            return 1;

        // Accessories can appear multiple times without certain order.
        int number = 0;
        if (acc1.equals(equipment))
            number++;
        if (acc2.equals(equipment))
            number++;
        if (acc3.equals(equipment))
            number++;

        return number;
    }

    /**
     * @return The equipment used on the hat slot. {@link #NO_EQUIPMENT_STRING}
     *         if no equipment is used.
     */
    public String getHat() {
        return hat;
    }

    /**
     * @return The equipment used on the weapon slot.
     *         {@link #NO_EQUIPMENT_STRING} if no equipment is used.
     */
    public String getWeapon() {
        return weapon;
    }

    /**
     * @return The equipment used on the offhand slot.
     *         {@link #NO_EQUIPMENT_STRING} if no equipment is used.
     */
    public String getOffhand() {
        return offhand;
    }

    /**
     * @return The equipment used on the shirt slot.
     *         {@link #NO_EQUIPMENT_STRING} if no equipment is used.
     */
    public String getShirt() {
        return shirt;
    }

    /**
     * @return The equipment used on the pants slot.
     *         {@link #NO_EQUIPMENT_STRING} if no equipment is used.
     */
    public String getPants() {
        return pants;
    }

    /**
     * @return The equipment used on the acc1 slot. {@link #NO_EQUIPMENT_STRING}
     *         if no equipment is used.
     */
    public String getAcc1() {
        return acc1;
    }

    /**
     * @return The equipment used on the acc2 slot. {@link #NO_EQUIPMENT_STRING}
     *         if no equipment is used.
     */
    public String getAcc2() {
        return acc2;
    }

    /**
     * @return The equipment used on the acc3 slot. {@link #NO_EQUIPMENT_STRING}
     *         if no equipment is used.
     */
    public String getAcc3() {
        return acc3;
    }

    /**
     * @return The equipment used on the familiar equipment slot.
     *         {@link #NO_EQUIPMENT_STRING} if no equipment is used.
     */
    public String getFamEquip() {
        return famEquip;
    }

    public boolean equalsIgnoreTurn(
                                    final EquipmentChange that) {
        if (that == this)
            return true;

        if (that == null)
            return false;

        return hat.equals(that.getHat()) && weapon.equals(that.getWeapon())
               && offhand.equals(that.getOffhand()) && shirt.equals(that.getShirt())
               && pants.equals(that.getPants()) && acc1.equals(that.getAcc1())
               && acc2.equals(that.getAcc2()) && acc3.equals(that.getAcc3())
               && famEquip.equals(that.getFamEquip());
    }

    @Override
    public String toString() {
        final String newLine = System.getProperty("line.separator");
        final StringBuilder str = new StringBuilder(100);

        str.append("Equipment change on turn ");
        str.append(getTurnNumber());
        str.append(newLine);
        str.append("Hat: ");
        str.append(hat);
        str.append(newLine);
        str.append("Weapon: ");
        str.append(weapon);
        str.append(newLine);
        str.append("Offhand: ");
        str.append(offhand);
        str.append(newLine);
        str.append("Shirt: ");
        str.append(shirt);
        str.append(newLine);
        str.append("Pants: ");
        str.append(pants);
        str.append(newLine);
        str.append("Acc1: ");
        str.append(acc1);
        str.append(newLine);
        str.append("Acc2: ");
        str.append(acc2);
        str.append(newLine);
        str.append("Acc3: ");
        str.append(acc3);
        str.append(newLine);
        str.append("Fam. equipment: ");
        str.append(famEquip);

        return str.toString();
    }

    @Override
    public boolean equals(
                          final Object o) {
        if (super.equals(o) && o instanceof EquipmentChange)
            return hat.equals(((EquipmentChange) o).getHat())
                   && weapon.equals(((EquipmentChange) o).getWeapon())
                   && offhand.equals(((EquipmentChange) o).getOffhand())
                   && shirt.equals(((EquipmentChange) o).getShirt())
                   && pants.equals(((EquipmentChange) o).getPants())
                   && acc1.equals(((EquipmentChange) o).getAcc1())
                   && acc2.equals(((EquipmentChange) o).getAcc2())
                   && acc3.equals(((EquipmentChange) o).getAcc3())
                   && famEquip.equals(((EquipmentChange) o).getFamEquip());

        return false;
    }

    @Override
    public int hashCode() {
        int result = 2935;
        result = result * 31 + super.hashCode();
        result = result * 31 + hat.hashCode();
        result = result * 31 + weapon.hashCode();
        result = result * 31 + offhand.hashCode();
        result = result * 31 + shirt.hashCode();
        result = result * 31 + pants.hashCode();
        result = result * 31 + acc1.hashCode();
        result = result * 31 + acc2.hashCode();
        result = result * 31 + acc3.hashCode();
        result = result * 31 + famEquip.hashCode();

        return result;
    }
}
