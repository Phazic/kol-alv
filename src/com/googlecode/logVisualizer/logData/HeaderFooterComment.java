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

/**
 * A helper class for easy handling of header and footer comments as they get
 * attached to single days of an ascension log.
 * <p>
 * This class uses the {@link LogComment} class as its underlying foundation and
 * as such, all limitations when adding and setting comments apply.
 * <p>
 * All methods in this class throw a {@link NullPointerException} if a null
 * object reference is passed in any parameter.
 */
public final class HeaderFooterComment {
    private final LogComment header = new LogComment();

    private final LogComment footer = new LogComment();

    /**
     * @return The header log comment.
     */
    public LogComment getHeader() {
        return header;
    }

    /**
     * @return The footer log comment.
     */
    public LogComment getFooter() {
        return footer;
    }

    /**
     * @param comment
     *            The header comment to set.
     */
    public void setHeaderComments(
                                  final String comment) {
        header.setComments(comment);
    }

    /**
     * @param comment
     *            The header comment to add.
     */
    public void addHeaderComments(
                                  final String comment) {
        header.addComments(comment);
    }

    /**
     * @return The header comment or an empty string if there is none.
     */
    public String getHeaderComments() {
        return header.getComments();
    }

    /**
     * @param comment
     *            The footer comment to set.
     */
    public void setFooterComments(
                                  final String comment) {
        footer.setComments(comment);
    }

    /**
     * @param comment
     *            The footer comment to add.
     */
    public void addFooterComments(
                                  final String comment) {
        footer.addComments(comment);
    }

    /**
     * @return The footer comment or an empty string if there is none.
     */
    public String getFooterComments() {
        return footer.getComments();
    }
}
