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

package com.googlecode.alv.logData;

/**
 * An immutable container class to store mp gains of all sorts.
 */
public final class MPGain {
    public static final MPGain NO_MP = new MPGain(0, 0, 0, 0, 0);

    public final int encounterMPGain;

    public final int starfishMPGain;

    public final int restingMPGain;

    public final int outOfEncounterMPGain;

    public final int consumableMPGain;

    /**
     * Creates a MPGain object with the given values.
     * 
     * @param encounterMPGain
     *            The encounter mp gain to set. This should not include starfish
     *            mp gains or mp gains from resting.
     * @param starfishMPGain
     *            The starfish mp gain to set.
     * @param restingMPGain
     *            The resting mp gain to set.
     * @param outOfEncounterMPGain
     *            The out-of-encounter mp gain to set.
     * @param consumableMPGain
     *            The consumable mp gain to set.
     */
    public MPGain(
                  final int encounterMPGain, final int starfishMPGain, final int restingMPGain,
                  final int outOfEncounterMPGain, final int consumableMPGain) {
        this.encounterMPGain = encounterMPGain;
        this.starfishMPGain = starfishMPGain;
        this.restingMPGain = restingMPGain;
        this.outOfEncounterMPGain = outOfEncounterMPGain;
        this.consumableMPGain = consumableMPGain;
    }

    /**
     * @return The total mp gains.
     */
    public int getTotalMPGains() {
        return encounterMPGain + consumableMPGain + outOfEncounterMPGain + restingMPGain
               + starfishMPGain;
    }

    /**
     * @return True if all values of this MPGain instance are zero, otherwise
     *         false.
     */
    public boolean isMPGainZero() {
        return encounterMPGain == 0 && consumableMPGain == 0 && outOfEncounterMPGain == 0
               && restingMPGain == 0 && starfishMPGain == 0;
    }

    /**
     * @param encounterMPGain
     *            The encounter mp gain to set. This should not include starfish
     *            mp gains or mp gains from resting.
     * @return The MPGain object with the new values.
     */
    public MPGain setEncounterMPGain(
                                     final int encounterMPGain) {
        return new MPGain(encounterMPGain,
                          starfishMPGain,
                          restingMPGain,
                          outOfEncounterMPGain,
                          consumableMPGain);
    }

    /**
     * @param encounterMPGain
     *            The encounter mp gain to add. This should not include starfish
     *            mp gains or mp gains from resting.
     * @return The MPGain object with the new values.
     */
    public MPGain addEncounterMPGain(
                                     final int encounterMPGain) {
        return new MPGain(this.encounterMPGain + encounterMPGain,
                          starfishMPGain,
                          restingMPGain,
                          outOfEncounterMPGain,
                          consumableMPGain);
    }

    /**
     * @param starfishMPGain
     *            The starfish mp gain to set.
     * @return The MPGain object with the new values.
     */
    public MPGain setStarfishMPGain(
                                    final int starfishMPGain) {
        return new MPGain(encounterMPGain,
                          starfishMPGain,
                          restingMPGain,
                          outOfEncounterMPGain,
                          consumableMPGain);
    }

    /**
     * @param starfishMPGain
     *            The starfish mp gain to add.
     * @return The MPGain object with the new values.
     */
    public MPGain addStarfishMPGain(
                                    final int starfishMPGain) {
        return new MPGain(encounterMPGain,
                          this.starfishMPGain + starfishMPGain,
                          restingMPGain,
                          outOfEncounterMPGain,
                          consumableMPGain);
    }

    /**
     * @param restingMPGain
     *            The resting mp gain to set.
     * @return The MPGain object with the new values.
     */
    public MPGain setRestingMPGain(
                                   final int restingMPGain) {
        return new MPGain(encounterMPGain,
                          starfishMPGain,
                          restingMPGain,
                          outOfEncounterMPGain,
                          consumableMPGain);
    }

    /**
     * @param restingMPGain
     *            The resting mp gain to add.
     * @return The MPGain object with the new values.
     */
    public MPGain addRestingMPGain(
                                   final int restingMPGain) {
        return new MPGain(encounterMPGain,
                          starfishMPGain,
                          this.restingMPGain + restingMPGain,
                          outOfEncounterMPGain,
                          consumableMPGain);
    }

    /**
     * @param outOfEncounterMPGain
     *            The out-of-encounter mp gain to set.
     * @return The MPGain object with the new values.
     */
    public MPGain setOutOfEncounterMPGain(
                                          final int outOfEncounterMPGain) {
        return new MPGain(encounterMPGain,
                          starfishMPGain,
                          restingMPGain,
                          outOfEncounterMPGain,
                          consumableMPGain);
    }

    /**
     * @param outOfEncounterMPGain
     *            The out-of-encounter mp gain to add.
     * @return The MPGain object with the new values.
     */
    public MPGain addOutOfEncounterMPGain(
                                          final int outOfEncounterMPGain) {
        return new MPGain(encounterMPGain,
                          starfishMPGain,
                          restingMPGain,
                          this.outOfEncounterMPGain + outOfEncounterMPGain,
                          consumableMPGain);
    }

    /**
     * @param consumableMPGain
     *            The consumable mp gain to set.
     * @return The MPGain object with the new values.
     */
    public MPGain setConsumableMPGain(
                                      final int consumableMPGain) {
        return new MPGain(encounterMPGain,
                          starfishMPGain,
                          restingMPGain,
                          outOfEncounterMPGain,
                          consumableMPGain);
    }

    /**
     * @param consumableMPGain
     *            The consumable mp gain to add.
     * @return The MPGain object with the new values.
     */
    public MPGain addConsumableMPGain(
                                      final int consumableMPGain) {
        return new MPGain(encounterMPGain,
                          starfishMPGain,
                          restingMPGain,
                          outOfEncounterMPGain,
                          this.consumableMPGain + consumableMPGain);
    }

    /**
     * @param mpGains
     *            The mp gains to add.
     * @return The MPGain object with the new values.
     */
    public MPGain addMPGains(
                             final MPGain mpGains) {
        return addMPGains(mpGains.encounterMPGain,
                          mpGains.starfishMPGain,
                          mpGains.restingMPGain,
                          mpGains.outOfEncounterMPGain,
                          mpGains.consumableMPGain);
    }

    /**
     * @param encounterMPGain
     *            The encounter mp gain to add. This should not include starfish
     *            mp gains or mp gains from resting.
     * @param starfishMPGain
     *            The starfish mp gain to add.
     * @param restingMPGain
     *            The resting mp gain to add.
     * @param outOfEncounterMPGain
     *            The out-of-encounter mp gain to add.
     * @param consumableMPGain
     *            The consumable mp gain to add.
     * @return The MPGain object with the new values.
     */
    public MPGain addMPGains(
                             final int encounterMPGain, final int starfishMPGain,
                             final int restingMPGain, final int outOfEncounterMPGain,
                             final int consumableMPGain) {
        return new MPGain(this.encounterMPGain + encounterMPGain,
                          this.starfishMPGain + starfishMPGain,
                          this.restingMPGain + restingMPGain,
                          this.outOfEncounterMPGain + outOfEncounterMPGain,
                          this.consumableMPGain + consumableMPGain);
    }

    @Override
    public int hashCode() {
        int result = 743;
        result = 31 * result + encounterMPGain;
        result = 31 * result + consumableMPGain;
        result = 31 * result + outOfEncounterMPGain;
        result = 31 * result + restingMPGain;
        result = 31 * result + starfishMPGain;

        return result;
    }

    @Override
    public boolean equals(
                          final Object obj) {
        if (this == obj)
            return true;

        if (obj == null)
            return false;

        if (obj instanceof MPGain) {
            final MPGain other = (MPGain) obj;
            return encounterMPGain == other.encounterMPGain
                   && consumableMPGain == other.consumableMPGain
                   && outOfEncounterMPGain == other.outOfEncounterMPGain
                   && restingMPGain == other.restingMPGain
                   && starfishMPGain == other.starfishMPGain;
        }

        return false;
    }
}
