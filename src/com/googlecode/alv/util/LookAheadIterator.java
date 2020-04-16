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

package com.googlecode.alv.util;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * This iterator implementation is very similar to a normal iterator with the
 * only differences being that it does not support the remove operation and
 * gives one the ability to look ahead and peek at the next element in line of
 * the underlying collection.
 * <p>
 * Basically this iterator sits atop another iterator and simply looks one
 * element ahead of the current index to give the possibility to peek at the
 * next element.
 * <p>
 * Please note that this implementation does not work well with collections that
 * contain {@code null} as an element. Such collections are thus not supported.
 * 
 * @see Iterator
 */
public final class LookAheadIterator<E> implements Iterator<E> {
    private final Iterator<E> iterator;

    private E peek;

    /**
     * Constructs the iterator with the given iterator as the basis.
     * 
     * @param iterator
     *            The iterator whose elements are the basis of this iterator.
     * @throws NullPointerException
     *             if the iterator is {@code null}
     */
    public LookAheadIterator(
                             final Iterator<E> iterator) {
        if (iterator == null)
            throw new NullPointerException("Iterator must not be null.");

        this.iterator = iterator;
        peek = iterator.hasNext() ? iterator.next() : null;
    }

    /**
     * {@inheritDoc}
     */
    public boolean hasNext() {
        return peek != null;
    }

    /**
     * {@inheritDoc}
     */
    public E next() {
        if (peek == null)
            throw new NoSuchElementException("There is no next element.");

        final E current = peek;
        peek = iterator.hasNext() ? iterator.next() : null;

        return current;
    }

    /**
     * Returns the next element in the collection without moving the index
     * ahead.
     * 
     * @return The next element in the collection. Will return {@code null} if
     *         there is no more element left.
     */
    public E peek() {
        return peek;
    }

    /**
     * This method is not supported by this iterator.
     * 
     * @throws UnsupportedOperationException
     *             if this method is called
     */
    public void remove() {
        throw new UnsupportedOperationException("This iterator doesn't support the remove opperation.");
    }
}
