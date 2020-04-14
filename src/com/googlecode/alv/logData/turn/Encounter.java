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

package com.googlecode.alv.logData.turn;

import com.googlecode.alv.logData.turn.turnAction.EquipmentChange;
import com.googlecode.alv.logData.turn.turnAction.FamiliarChange;

/**
 * The Encounter interface basically defines most of the getter methods of a
 * {@link SingleTurn} without the setter methods. It thus allows to implement
 * immutable single turns while still making it possible to interoperate with
 * {@link SingleTurn} instances when needed.
 */
public interface Encounter extends TurnEntity {
    /**
     * @return The number of the day on which this turn was spent on.
     */
    public int getDayNumber();

    /**
     * @return The name of the encounter found on this turn.
     */
    public String getEncounterName();

    /**
     * @return The familiar used on this turn.
     */
    public FamiliarChange getUsedFamiliar();

    /**
     * @return The equipment used on this turn.
     */
    public EquipmentChange getUsedEquipment();

    /**
     * @return {@code true} if this combat was disintegrated. Will always return
     *         {@code false} if this turn is not a combat.
     */
    public boolean isDisintegrated();
    
    /**
     * @return {@code true} if during this combat a banishment occurred. Will always return
     *         {@code false} if this turn is not a combat.
     */
    public boolean isBanished();
    
    /**
     * @return {@code null} if isBanished == false, else returns the banishment string.
     */
    public String getBanishedInfo();
}
