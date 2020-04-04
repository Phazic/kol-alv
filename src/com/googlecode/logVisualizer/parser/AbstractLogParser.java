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

import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;

import com.googlecode.logVisualizer.logData.LogDataHolder;
import com.googlecode.logVisualizer.util.Lists;
import com.googlecode.logVisualizer.util.textualLogs.TextLogCreator;

/**
 * A basic implementation of the {@link LogParser} interface for an ascension
 * log parser. The needed back end data structures and generic helper-methods
 * for parsing are implemented.
 * <p>
 * All methods in this class throw a {@link NullPointerException} if a null
 * object reference is passed in any parameter.
 */
public abstract class AbstractLogParser implements LogParser {
    private final LogDataHolder logData;

    private final List<LineParser> lineParsers = Lists.newArrayList();

    private final List<BlockParser> blockParsers = Lists.newArrayList();

    /**
     * @param logData
     *            The log data of this log parser to set.
     */
    public AbstractLogParser(
                             final LogDataHolder logData) {
        if (logData == null)
            throw new NullPointerException("The LogDataHolder must not be null.");

        this.logData = logData;
    }

    /**
     * This method tries to parse the given line with one of the line parsers it
     * has.
     * <p>
     * This should always be the method of choice to parse lines in actual
     * implementations of this class.
     * <p>
     * This implementation iterates over the internal line parser list and lets
     * all line parsers try to parse data out of the given line. If one of the
     * line parsers was able to parse the line, the loop is ended without
     * checking another parser. Thus the line parser list shouldn't contain
     * parsers which are able to parse the same line.
     * 
     * @param line
     *            The line to be parsed.
     */
    protected void parseLine(
                             final String line) {
        for (final LineParser lp : lineParsers)
            // If the parser can parse the line, this method also returns true.
            // This is used to cut back on the amount of loops.
            if (lp.parseLine(line, logData))
                break;
    }

    /**
     * This method tries to parse multiple lines from the given log reader with
     * one of the block parsers it has.
     * <p>
     * This should always be the method of choice to parse blocks in actual
     * implementations of this class.
     * <p>
     * This implementation saves the next line returned by the BufferedReader
     * and checks it with ever block parser to see whether one of them
     * recognises it as the first line of parsable block. Thus, this method will
     * not catch different parsable blocks that directly follow each other in
     * one runthrough. Developers have to plan for such cases accordingly by
     * making sure that block parsers leave the BufferedReader in a position
     * that makes it possible for another block parser to recognise a compatible
     * block in a following call of this method.
     * 
     * @param reader
     *            The line to be parsed.
     * @throws java.io.IOException If a read failure occurs
     */
    protected void parseBlock(
                              final BufferedReader reader)
                                                          throws IOException {
        if (!blockParsers.isEmpty()) {
            // Get the next line and reset the reader back to its previous
            // position afterwards.
            // A 500 characters limit should be enough for this (maybe too
            // much).
            reader.mark(500);
            final String line = reader.readLine();
            reader.reset();

            if (line != null && line.length() > 0)
                for (final BlockParser bp : blockParsers)
                    bp.parseBlock(line, reader, logData);
        }
    }

    /**
     * @return The log data of this log parser.
     */
    public LogDataHolder getLogData() {
        return logData;
    }

    /**
     * Calls through to {@link LogDataHolder#isDetailedLog()} in this
     * implementation to figure you whether the log data is detailed enough for
     * the {@link TextLogCreator}.
     * 
     * @see LogParser
     */
    public boolean isDetailedLogData() {
        return logData.isDetailedLog();
    }

    /**
     * @param lineParser
     *            The line parser to add.
     */
    protected void addLineParser(
                                 final LineParser lineParser) {
        lineParsers.add(lineParser);
    }

    /**
     * @param blockParser
     *            The block parser to add.
     */
    protected void addBlockParser(
                                  final BlockParser blockParser) {
        blockParsers.add(blockParser);
    }
}
