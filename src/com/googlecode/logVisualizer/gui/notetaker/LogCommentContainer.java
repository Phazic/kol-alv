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

package com.googlecode.logVisualizer.gui.notetaker;

import com.googlecode.logVisualizer.logData.LogComment;

/**
 * This container class contains the pre- and post-interval {@link LogComment}s
 * of a turn interval and the textual description associated with it.
 */
final class LogCommentContainer {
    private final LogComment preComment;

    private final LogComment postComment;

    private final String description;

    /**
     * @param preIntervalComment
     *            The pre-interval log comment.
     * @param postIntervalComment
     *            The log comment.
     * @param description
     *            The matching textual print-out associated with the log
     *            comments.
     */
    LogCommentContainer(
                        final LogComment logComment, final String description) {
        if (logComment == null)
            throw new IllegalArgumentException("The log comment must not be null.");
        if (description == null)
            throw new IllegalArgumentException("The description must not be null.");

        preComment = null;
        postComment = logComment;
        this.description = description;
    }

    /**
     * @param preIntervalComment
     *            The pre-interval log comment.
     * @param postIntervalComment
     *            The post-interval log comment.
     * @param description
     *            The matching textual print-out associated with the log
     *            comments.
     */
    LogCommentContainer(
                        final LogComment preIntervalComment, final LogComment postIntervalComment,
                        final String description) {
        if (preIntervalComment == null)
            throw new IllegalArgumentException("The pre-interval log comment must not be null.");
        if (postIntervalComment == null)
            throw new IllegalArgumentException("The post-interval log comment must not be null.");
        if (description == null)
            throw new IllegalArgumentException("The description must not be null.");

        preComment = preIntervalComment;
        postComment = postIntervalComment;
        this.description = description;
    }

    boolean isSingleCommentContainer() {
        return preComment == null;
    }

    /**
     * @return The pre-interval log comment.
     */
    LogComment getPreIntervalComment() {
        return preComment;
    }

    /**
     * @return The post-interval log comment.
     */
    LogComment getPostIntervalComment() {
        return postComment;
    }

    /**
     * @return The textual print-out description of the turn interval.
     */
    String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return description;
    }

    @Override
    public boolean equals(
                          final Object o) {
        if (o != null)
            if (o instanceof LogCommentContainer) {
                final LogCommentContainer that = (LogCommentContainer) o;

                return preComment.equals(that.getPreIntervalComment())
                       && postComment.equals(that.getPostIntervalComment())
                       && description.equals(that.getDescription());
            }

        return false;
    }

    @Override
    public int hashCode() {
        int result = 12;
        result = 31 * result + preComment.hashCode();
        result = 31 * result + postComment.hashCode();
        result = 31 * result + description.hashCode();

        return result;
    }
}
