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

import com.googlecode.logVisualizer.logData.HeaderFooterComment;
import com.googlecode.logVisualizer.logData.LogDataHolder;

/**
 * A parser to recognise notes in mafia logs.
 * <p>
 * The format looks like this:
 * <p>
 * {@code > Note: _actualNote_}
 * <p>
 * {@code > Header: _actualNote_}
 * <p>
 * {@code > Footer: _actualNote_}
 */
public final class NotesLineParser extends AbstractLineParser {
    private static final String NOTES_START_STRING = " > Note: ";

    private static final String HEADER_START_STRING = " > Header: ";

    private static final String FOOTER_START_STRING = " > Footer: ";

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doParsing(
                             final String line, final LogDataHolder logData) {
        if (line.startsWith(NOTES_START_STRING))
            logData.getLastTurnSpent().addNotes(line.substring(NOTES_START_STRING.length()));
        else {
            final HeaderFooterComment currentHeadFoot = logData.getLastHeaderFooterComment();
            // Header and footer strings have the same length.
            final String comment = line.substring(HEADER_START_STRING.length());

            if (line.startsWith(HEADER_START_STRING))
                currentHeadFoot.addHeaderComments(comment);
            else
                currentHeadFoot.addFooterComments(comment);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean isCompatibleLine(
                                       final String line) {
        return line.startsWith(NOTES_START_STRING) || line.startsWith(HEADER_START_STRING)
               || line.startsWith(FOOTER_START_STRING);
    }
}
