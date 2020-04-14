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

import java.util.Map;

import com.googlecode.alv.util.Maps;

/**
 * A simple enumeration for various turn types.
 */
public enum TurnVersion {
    COMBAT, NONCOMBAT, OTHER, NOT_DEFINED;

    private static final Map<String, TurnVersion> stringToEnum = Maps.newHashMap();

    static {
        for (final TurnVersion op : values())
            stringToEnum.put(op.toString(), op);
    }

    /**
     * @param turnVersionName Name a a turn version to translate
     * @return The enum whose toString method returns a string which is equal to
     *         the given string. If no match is found this method will return
     *         {@code NOT_DEFINED}.
     */
    public static TurnVersion fromString(
                                         final String turnVersionName) {
        if (turnVersionName == null)
            throw new NullPointerException("The turn version name must not be null.");

        final TurnVersion turnVersion = stringToEnum.get(turnVersionName);

        return turnVersion != null ? turnVersion : NOT_DEFINED;
    }
}
