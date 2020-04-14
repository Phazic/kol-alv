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

package com.googlecode.alv.util.dataTables;

/**
 * This class represents an outfit and holds the data on which equipment slots
 * will be overwritten when a given outfit is equipped.
 */
public final class Outfit {
    /**
     * As the names says, an outfit that doesn't change any equipment slots.
     */
    public static final Outfit NO_CHANGE = new Outfit("no change",
                                                      false,
                                                      false,
                                                      false,
                                                      false,
                                                      false,
                                                      false,
                                                      false,
                                                      false);

    public final String outfitName;

    public final boolean hat;

    public final boolean weapon;

    public final boolean offhand;

    public final boolean shirt;

    public final boolean pants;

    public final boolean acc1;

    public final boolean acc2;

    public final boolean acc3;

    public Outfit(
                  final String outfitName, final boolean hat, final boolean weapon,
                  final boolean offhand, final boolean shirt, final boolean pants,
                  final boolean acc1, final boolean acc2, final boolean acc3) {
        if (outfitName == null)
            throw new NullPointerException("The outfit name must not be null.");

        this.outfitName = outfitName;
        this.hat = hat;
        this.weapon = weapon;
        this.offhand = offhand;
        this.shirt = shirt;
        this.pants = pants;
        this.acc1 = acc1;
        this.acc2 = acc2;
        this.acc3 = acc3;
    }

    @Override
    public boolean equals(
                          Object obj) {
        if (this == obj)
            return true;

        if (obj == null)
            return false;

        if (!(obj instanceof Outfit))
            return false;

        Outfit other = (Outfit) obj;
        return outfitName.equals(other.outfitName) && hat == other.hat && weapon == other.weapon
               && offhand == other.offhand && shirt == other.shirt && pants == other.pants
               && acc1 == other.acc1 && acc2 == other.acc2 && acc3 == other.acc3;
    }

    @Override
    public int hashCode() {
        int result = 482;
        result = 31 * result + outfitName.hashCode();
        result = 31 * result + (hat ? 1231 : 1237);
        result = 31 * result + (weapon ? 1231 : 1237);
        result = 31 * result + (offhand ? 1231 : 1237);
        result = 31 * result + (shirt ? 1231 : 1237);
        result = 31 * result + (pants ? 1231 : 1237);
        result = 31 * result + (acc1 ? 1231 : 1237);
        result = 31 * result + (acc2 ? 1231 : 1237);
        result = 31 * result + (acc3 ? 1231 : 1237);

        return result;
    }
}
