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

package com.googlecode.logVisualizer.chart.turnrundownGantt;

import java.awt.Color;
import java.awt.Paint;
import java.util.Map;

import com.googlecode.logVisualizer.util.Maps;

public final class FamiliarColor {
    private final String familiarName;

    private Colors color;

    public FamiliarColor(
                         final String familiarName, final String colorName) {
        this.familiarName = familiarName;
        color = Colors.fromString(colorName);
    }

    public String getFamiliarName() {
        return familiarName;
    }

    public void setColor(
                         final Colors color) {
        setColor(color.toString());
    }

    public void setColor(
                         final String colorName) {
        color = Colors.fromString(colorName);
    }

    public Colors getColor() {
        return color;
    }

    public Paint getColorPaint() {
        return color.getColor();
    }

    @Override
    public boolean equals(
                          final Object o) {
        if (o != null)
            if (o instanceof FamiliarColor)
                return ((FamiliarColor) o).getFamiliarName().equals(familiarName)
                       && ((FamiliarColor) o).getColor().equals(color);

        return false;
    }

    @Override
    public int hashCode() {
        int result = 3253;
        result = result * 31 + familiarName.hashCode();
        result = result * 31 + color.hashCode();

        return result;
    }

    public static enum Colors {
        NONE("none", Color.WHITE),
        BLUE("blue", new Color(200, 225, 255)),
        YELLOW("yellow", new Color(255, 255, 200)),
        GREEN("green", new Color(200, 255, 200)),
        RED("red", new Color(255, 200, 255)),
        TURQUOISE("turquoise", new Color(200, 255, 255)),
        GRAY("gray", new Color(200, 200, 175));

        private static final Map<String, Colors> stringToEnum = Maps.newHashMap();

        static {
            for (final Colors op : values())
                stringToEnum.put(op.toString(), op);
        }

        private final String colorName;

        private final Paint color;

        Colors(
               final String colorName, final Paint color) {
            this.colorName = colorName;
            this.color = color;
        }

        public Paint getColor() {
            return color;
        }

        @Override
        public String toString() {
            return colorName;
        }

        /**
         * @param colorName The name of a color
         * @return The enum whose toString method returns a string which is
         *         equal to the given string. If no match is found this method
         *         will return <code>WHITE</code>.
         */
        public static Colors fromString(
                                        final String colorName) {
            final Colors color = stringToEnum.get(colorName);

            return color != null ? color : NONE;
        }
    }
}
