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

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Utility class with static helper methods for sets.
 */
public final class Sets {
    private Sets() {}

    /**
     * @param <E> Type of element for the hash set
     * @return A new {@link HashSet}.
     */
    public static <E> Set<E> newHashSet() {
        return new HashSet<E>();
    }

    /**
     * @param <E> Type of element for the hash set
     * @param initialCapacity
     *            The initial capacity of the new {@link HashSet}.
     * @return A new {@link HashSet} with the given initial capacity.
     */
    public static <E> Set<E> newHashSet(
                                        final int initialCapacity) {
        return new HashSet<E>(initialCapacity);
    }

    /**
     * @param <E> Type of element for the hash set
     * @param elements
     *            The elements to be added to the new {@link HashSet}.
     * @return A new {@link HashSet} populated with the given elements.
     */
    public static <E> Set<E> newHashSet(
                                        final Collection<? extends E> elements) {
        return new HashSet<E>(elements);
    }

    /**
     * @param <E> Type of element for the hash set
     * @param elements
     *            The elements to be added to the new {@link HashSet}.
     * @return A new {@link HashSet} populated with the given elements.
     */
    public static <E> Set<E> newHashSet(
                                        final Iterable<? extends E> elements) {
        final Set<E> set = newHashSet();
        for (final E element : elements)
            set.add(element);

        return set;
    }

    /**
     * @param <E> Type of element for the hash set
     * @param elements
     *            The elements to be added to the new {@link HashSet}.
     * @return A new {@link HashSet} populated with the given elements.
     */
    public static <E> Set<E> newHashSet(
                                        final E... elements) {
        final Set<E> set = newHashSet((int) (elements.length * 1.4));
        for (final E element : elements)
            set.add(element);

        return set;
    }

    /**
     * @param <E> Type of element for the immutable set
     * @param elements
     *            The elements to be added to the new immutable set.
     * @return A new immutable set populated with the given elements.
     */
    public static <E> Set<E> immutableSetOf(
                                            final Collection<? extends E> elements) {
        return Collections.unmodifiableSet(newHashSet(elements));
    }

    /**
     * @param <E> Type of element for the immutable set
     * @param elements
     *            The elements to be added to the new immutable set.
     * @return A new immutable set populated with the given elements.
     */
    public static <E> Set<E> immutableSetOf(
                                            final Iterable<? extends E> elements) {
        return Collections.unmodifiableSet(newHashSet(elements));
    }

    /**
     * @param <E> Type of element for the immutable set
     * @param elements
     *            The elements to be added to the new immutable set.
     * @return A new immutable set populated with the given elements.
     */
    public static <E> Set<E> immutableSetOf(
                                            final E... elements) {
        return Collections.unmodifiableSet(newHashSet(elements));
    }
}
