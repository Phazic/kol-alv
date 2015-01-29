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

package com.googlecode.logVisualizer.logData;

import com.googlecode.logVisualizer.parser.UsefulPatterns;

/**
 * Helper class to make handling of turn notes and other log comments more
 * straight forward.
 * <p>
 * All methods in this class throw a {@link NullPointerException} if a null
 * object reference is passed in any parameter.
 */
public final class LogComment {
    // Lazy initialisation of the StringBuilder.
    private StringBuilder comment;

    private void checkCommentInitialization() {
        if (comment == null)
            comment = new StringBuilder(60);
    }

    /**
     * @return {@code true} if the log comment is empty, otherwise {@code false}
     *         .
     */
    public boolean isEmpty() {
        return comment == null ? true : comment.length() == 0;
    }

    /**
     * @param notes
     *            The log comment to set.
     */
    public void setComments(
                            final String notes) {
        if (notes == null)
            throw new NullPointerException("notes must not be null.");

        if (notes.length() == 0) {
            comment = null;
            return;
        }

        checkCommentInitialization();

        comment.delete(0, comment.length());
        comment.append(notes);
    }

    /**
     * Adds the given log comment. The already existing comments and the ones
     * added will be divided by a line break ({@code"\n"}).
     * 
     * @param notes
     *            The log comment to add.
     */
    public void addComments(
                            final String notes) {
        if (notes == null)
            throw new NullPointerException("notes must not be null.");

        if (notes.length() > 0) {
            checkCommentInitialization();

            if (!isEmpty())
                comment.append("\n");
            comment.append(notes);
        }
    }

    /**
     * @return The log comments, or an empty string if there are none.
     */
    public String getComments() {
        return isEmpty() ? UsefulPatterns.EMPTY_STRING : comment.toString();
    }

    @Override
    public String toString() {
        return getComments();
    }

    @Override
    public boolean equals(
                          final Object obj) {
        if (this == obj)
            return true;

        if (obj == null)
            return false;

        if (obj instanceof LogComment) {
            final LogComment that = (LogComment) obj;

            return isEmpty() && that.isEmpty() || comment.equals(that.comment);
        }

        return false;
    }

    @Override
    public int hashCode() {
        int result = 124;
        result = 31 * result + getComments().hashCode();

        return result;
    }
}
