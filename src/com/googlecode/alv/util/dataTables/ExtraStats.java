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
 * This class is a simple container class for extra stats from equipment.
 */
public final class ExtraStats implements Comparable<ExtraStats> {
    /**
     * As the names says, an ExtraStats object with zero for every field.
     */
    public static final ExtraStats NO_STATS = new ExtraStats(0, 0, 0, 0);

    public final double generalGain;

    public final int musGain;

    public final int mystGain;

    public final int moxGain;

    public ExtraStats(
                      final double generalGain, final int musGain, final int mystGain,
                      final int moxGain) {
        this.generalGain = generalGain;
        this.musGain = musGain;
        this.mystGain = mystGain;
        this.moxGain = moxGain;
    }

    public ExtraStats(
                      final double generalGain) {
        this(generalGain, 0, 0, 0);
    }

    public int compareTo(
                         final ExtraStats that) {
        if (generalGain == that.generalGain) {
            if (musGain == that.musGain) {
                if (mystGain == that.mystGain)
                    return moxGain - that.moxGain;
                else
                    return mystGain - that.mystGain;
            } else
                return musGain - that.musGain;
        } else if (generalGain < that.generalGain)
            return -1;
        else
            return 1;
    }

    @Override
    public boolean equals(
                          final Object obj) {
        if (this == obj)
            return true;

        if (obj == null)
            return false;

        if (!(obj instanceof ExtraStats))
            return false;

        ExtraStats other = (ExtraStats) obj;
        return generalGain == other.generalGain && musGain == other.musGain
               && mystGain == other.mystGain && moxGain == other.moxGain;
    }

    @Override
    public int hashCode() {
        int result = 483;
        result = 31 * result + (int) generalGain;
        result = 31 * result + musGain;
        result = 31 * result + mystGain;
        result = 31 * result + moxGain;

        return result;
    }
}
