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

package com.googlecode.logVisualizer.parser;

import java.io.IOException;

import com.googlecode.logVisualizer.logData.LogDataHolder;
import com.googlecode.logVisualizer.util.textualLogs.TextLogCreator;

/**
 * An interface for an ascension log parser.
 */
public interface LogParser {
    /**
     * Starts the parsing of a given ascension log.
     * <p>
     * From where this log comes (input stream, buffered reader, file, etc.),
     * depends in the actual implementation of this class.
     * 
     * @throws IOException
     *             if there was a problem with reading an IO source (probably
     *             the log file); In such a case the parsing will be
     *             discontinued and the {@link LogDataHolder} of this class
     *             shouldn't be used for further computations
     */
    public void parse()
                       throws IOException;

    /**
     * @return The log data of this log parser.
     */
    public LogDataHolder getLogData();

    /**
     * @return True if the log data created by this parser is detailed enough to
     *         create a textual log from it with {@link TextLogCreator} class.
     */
    public boolean isDetailedLogData();
}
