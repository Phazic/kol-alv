/* Copyright (c) 2008-2020, developers of the Ascension Log Visualizer
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

package com.googlecode.alv.parser;

import java.io.BufferedReader;
import java.io.IOException;

import com.googlecode.alv.logData.LogDataHolder;

/**
 * An interface for a parser that can parse multiple lines (= a block).
 */
public interface BlockParser {
    /**
     * Parses a certain amount of lines (= a block). How many lines exactly
     * depends on the actual implementation.
     * <p>
     * Note that parsing will only happen if the current line matches the
     * specified start of the block. If it doesn't match, the reader will stay
     * at it's current position.
     * <p>
     * The {@link BufferedReader} is not closed by this method. Also, this
     * method will only read as many lines as necessary to parse out the
     * information it needs (and recognise that the block is finished). However,
     * it will not reset the reader to the position it started in.
     * 
     * @param nextLine
     *            The next line that the reader will produce.
     * @param reader
     *            The reader of the log of which a block should be parsed.
     * @param logData
     *            The log data instance in which the parsing results should be
     *            saved in.
     * @throws IOException
     *             Will be throw, if there was a problem with reading data from
     *             the BufferedReader.
     */
    public void parseBlock(
                           String nextLine, BufferedReader reader, LogDataHolder logData)
                                                                                         throws IOException;
}
