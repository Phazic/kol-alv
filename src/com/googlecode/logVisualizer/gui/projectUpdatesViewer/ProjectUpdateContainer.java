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

package com.googlecode.logVisualizer.gui.projectUpdatesViewer;

/**
 * Small, simple and immutable container class for the interesting parts of a
 * project feed entry.
 */
final class ProjectUpdateContainer {
    private final String title;

    private final String updated;

    private final String content;

    /**
     * @param title
     *            Contents of the title node of a project feed entry.
     * @param updated
     *            Contents of the updated node of a project feed entry.
     * @param content
     *            Contents of the content node of a project feed entry.
     * @throws NullPointerException
     *             if any of the parameters is {@code null}
     */
    ProjectUpdateContainer(
                           final String title, final String updated, final String content) {
        if (title == null)
            throw new NullPointerException("title must not be null.");
        if (updated == null)
            throw new NullPointerException("updated must not be null.");
        if (content == null)
            throw new NullPointerException("content must not be null.");

        this.title = title;
        this.updated = updated;
        this.content = content;
    }

    /**
     * @return The contents of the title node of a project feed entry.
     */
    String getTitle() {
        return title;
    }

    /**
     * @return The contents of the updated node of a project feed entry.
     */
    String getUpdated() {
        return updated;
    }

    /**
     * @return The contents of the content node of a project feed entry.
     */
    String getContent() {
        return content;
    }

    @Override
    public String toString() {
        return title;
    }

    @Override
    public int hashCode() {
        int result = 5545;
        result = 31 * result + title.hashCode();
        result = 31 * result + updated.hashCode();
        result = 31 * result + content.hashCode();
        return result;
    }

    @Override
    public boolean equals(
                          final Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;

        final ProjectUpdateContainer other = (ProjectUpdateContainer) obj;
        if (content == null) {
            if (other.content != null)
                return false;
        } else if (!content.equals(other.content))
            return false;
        if (title == null) {
            if (other.title != null)
                return false;
        } else if (!title.equals(other.title))
            return false;
        if (updated == null) {
            if (other.updated != null)
                return false;
        } else if (!updated.equals(other.updated))
            return false;

        return true;
    }
}
