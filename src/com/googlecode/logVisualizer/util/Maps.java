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

package com.googlecode.logVisualizer.util;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Utility class with static helper methods for maps.
 */
public final class Maps {
    private Maps() {}

    /**
     * @param <K> Type of map key
     * @param <V> Type of map value
     * @return A new {@link HashMap}.
     */
    public static <K, V> Map<K, V> newHashMap() {
        return new HashMap<K, V>();
    }

    /**
     * @param <K> Type of map key
     * @param <V> Type of map value
     * @param initialCapacity
     *            The initial capacity of the new {@link HashMap}.
     * @return A new {@link HashMap} with the given initial capacity.
     */
    public static <K, V> Map<K, V> newHashMap(
                                              final int initialCapacity) {
        return new HashMap<K, V>(initialCapacity);
    }

    /**
     * @param <K> Type of map key
     * @param <V> Type of map value
     * @param elements
     *            The key-value-pairs to be added to the new {@link HashMap}.
     * @return A new {@link HashMap} populated with the given key-value-pairs.
     */
    public static <K, V> Map<K, V> newHashMap(
                                              final Collection<Pair<? extends K, ? extends V>> elements) {
        final Map<K, V> map = newHashMap((int) (elements.size() * 1.4));
        for (final Pair<? extends K, ? extends V> element : elements)
            map.put(element.getVar1(), element.getVar2());

        return map;
    }

    /**
     * @param <K> Type of map key
     * @param <V> Type of map value
     * @param elements
     *            The key-value-pairs to be added to the new {@link HashMap}.
     * @return A new {@link HashMap} populated with the given key-value-pairs.
     */
    public static <K, V> Map<K, V> newHashMap(
                                              final Iterable<Entry<K, V>> elements) {
        final Map<K, V> map = newHashMap();
        for (final Entry<K, V> element : elements)
            map.put(element.getKey(), element.getValue());

        return map;
    }

    /**
     * @param <K> Type of map key
     * @param <V> Type of map value
     * @param elements
     *            The key-value-pairs to be added to the new {@link HashMap}.
     * @return A new {@link HashMap} populated with the given key-value-pairs.
     */
    public static <K, V> Map<K, V> newHashMap(
                                              final Pair<? extends K, ? extends V>... elements) {
        final Map<K, V> map = newHashMap((int) (elements.length * 1.4));
        for (final Pair<? extends K, ? extends V> element : elements)
            map.put(element.getVar1(), element.getVar2());

        return map;
    }

    /**
     * @param <K> Type of map key
     * @param <V> Type of map value
     * @param elements
     *            The key-value-pairs to be added to the new immutable map.
     * @return A new immutable map populated with the given key-value-pairs.
     */
    public static <K, V> Map<K, V> immutableMapOf(
                                                  final Collection<Pair<? extends K, ? extends V>> elements) {
        return Collections.unmodifiableMap(newHashMap(elements));
    }

    /**
     * @param <K> Type of map key
     * @param <V> Type of map value
     * @param elements
     *            The key-value-pairs to be added to the new immutable map.
     * @return A new immutable map populated with the given key-value-pairs.
     */
    public static <K, V> Map<K, V> immutableMapOf(
                                                  final Iterable<Entry<K, V>> elements) {
        return Collections.unmodifiableMap(newHashMap(elements));
    }

    /**
     * @param <K> Type of map key
     * @param <V> Type of map value
     * @param elements
     *            The key-value-pairs to be added to the new immutable map.
     * @return A new immutable map populated with the given key-value-pairs.
     */
    public static <K, V> Map<K, V> immutableMapOf(
                                                  final Pair<? extends K, ? extends V>... elements) {
        return Collections.unmodifiableMap(newHashMap(elements));
    }
}
