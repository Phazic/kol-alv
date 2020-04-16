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

package com.googlecode.alv.parser.blockParsers;

import java.io.BufferedReader;
import java.io.IOException;

import com.googlecode.alv.logData.LogDataHolder;
import com.googlecode.alv.parser.BlockParser;

/**
 * A basic class for a parser that can parse multiple lines (= a block).
 * <p>
 * This class has almost no implementation and only gives the structure for
 * actual parsers.
 */
public abstract class AbstractBlockParser implements BlockParser {
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
     * <p>
     * This implementation reads the current line of the given BufferedReader
     * and then checks whether it is equal to the start of a parsable block by
     * calling the method {@link #isCompatibleBlock(String)}. If that method
     * returns true, the method
     * {@link #doParsing(BufferedReader, LogDataHolder)} is called to do the
     * actual parsing.
     * 
     * @param nextLine
     *            The next line that the reader will produce.
     * @param reader
     *            The reader of the log of which a block should be parsed.
     * @param logData
     *            The log data instance in which the parsing results should be
     *            saved in.
     * @throws IOException
     *             If there was a problem with reading data from the
     *             BufferedReader.
     */
    public void parseBlock(
                           final String nextLine, final BufferedReader reader,
                           final LogDataHolder logData)
                                                       throws IOException {
        if (nextLine != null && isCompatibleBlock(nextLine))
            doParsing(reader, logData);
    }

    /**
     * A check to see whether this is the first line of the parsable block.
     * 
     * @param line
     *            The first line of the block to be parsed.
     * @return True if the line indeed marks the start of a parsable block by
     *         this class.
     */
    protected abstract boolean isCompatibleBlock(
                                                 String line);

    /**
     * Parses the wanted information out of the given log reader and saves it in
     * the given {@link LogDataHolder} instance.
     * <p>
     * The {@link BufferedReader} is not closed by this method. Also, this
     * method will only read as many lines as necessary to parse out the
     * information it needs (and recognise that the block is finished). However,
     * it will not reset the reader to the position it started in.
     * 
     * @param reader
     *            The reader of the log of which a block should be parsed.
     * @param logData
     *            The log data instance in which the parsing results should be
     *            saved in.
     * @throws java.io.IOException If an error occurs in reading
     */
    protected abstract void doParsing(
                                      BufferedReader reader, LogDataHolder logData)
                                                                                   throws IOException;
}
