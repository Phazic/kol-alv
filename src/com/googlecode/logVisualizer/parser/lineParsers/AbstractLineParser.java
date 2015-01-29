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

package com.googlecode.logVisualizer.parser.lineParsers;

import com.googlecode.logVisualizer.logData.LogDataHolder;
import com.googlecode.logVisualizer.parser.LineParser;

/**
 * A basic class for a parser that can only parse one single line.
 * <p>
 * This class has almost no implementation and only gives the structure for
 * actual parsers.
 */
public abstract class AbstractLineParser implements LineParser {
    /**
     * Parses the given line. If it isn't parsable by this parser nothing will
     * be done and <code>false</code> will be returned.
     * <p>
     * This implementation checks whether the given line is parsable by calling
     * {@link #isCompatibleLine(String)}. If that method returns true, the
     * method {@link #doParsing(String, LogDataHolder)} is called to do the
     * actual parsing.
     * 
     * @param line
     *            The line to be parsed.
     * @param logData
     *            The log data instance in which the parsing results should be
     *            saved in.
     * @return True if the line is compatible with this parser and thus has been
     *         parsed.
     */
    public boolean parseLine(
                             final String line, final LogDataHolder logData) {
        final boolean isParsable = isCompatibleLine(line);
        if (isParsable)
            doParsing(line, logData);

        return isParsable;
    }

    /**
     * Checks whether the given line can be parsed by this parser.
     * 
     * @param line
     *            The line to be parsed.
     * @return True if the line can be parsed.
     */
    protected abstract boolean isCompatibleLine(
                                                String line);

    /**
     * Parses the wanted information out of the given line and saves it in the
     * given {@link LogDataHolder} instance.
     * 
     * @param line
     *            The line to be parsed.
     * @param logData
     *            The log data instance in which the parsing results should be
     *            saved in.
     */
    protected abstract void doParsing(
                                      String line, LogDataHolder logData);
}
