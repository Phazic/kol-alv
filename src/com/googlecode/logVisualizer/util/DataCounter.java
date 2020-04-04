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

import java.util.*;

/**
 * This utility class counts how many times certain elements were added to it.
 * Note that while letting this class count big amounts of data shouldn't be a
 * problem, letting it count big amounts of different objects can be very memory
 * taxing and lessen the performance of this class.
 */
public final class DataCounter<E> {
    private final Map<E, Integer> counterBucket;

    private final Set<E> dataSet;

    /**
     * Constructs the DataCounter with an initial capacity for the internal hash
     * data containers of 20. The load factor for the internal hash data
     * containers is 0.75.
     * <p>
     * This constructor should be used only if the number of different objects
     * to be counted is known to be pretty low.
     */
    public DataCounter() {
        counterBucket = Maps.newHashMap(20);
        dataSet = Sets.newHashSet(20);
    }

    /**
     * Constructs the DataCounter with the given initial capacity for the
     * internal hash data containers. All reglementations of both
     * {@link HashSet} and {@link HashMap} also come into action here. The load
     * factor for the internal hash data containers is 0.75.
     * 
     * @param intialHashCapacity
     *            The initial capacity of the internal hash data containers.
     */
    public DataCounter(
                       final int intialHashCapacity) {
        counterBucket = Maps.newHashMap(intialHashCapacity);
        dataSet = Sets.newHashSet(intialHashCapacity);
    }

    /**
     * Adds these objects to the counter and starts counting them, or if they
     * already are counted, continues to do so.
     * 
     * @param elements
     *            The objects to be counted.
     * @throws NullPointerException
     *             if elements is {@code null}
     */
    public void addDataElements(
                                final Collection<? extends E> elements) {
        if (elements == null)
            throw new NullPointerException("Data element list must not be null.");

        for (final E e : elements)
            addDataElement(e);
    }

    /**
     * Adds this object to the counter and starts counting it, or if it already
     * is counted, continues to do so.
     * 
     * @param e
     *            The object to be counted.
     * @throws NullPointerException
     *             if e is {@code null}
     */
    public void addDataElement(
                               final E e) {
        addDataElement(e, 1);
    }

    /**
     * Adds this object to the counter and starts counting it, or if it already
     * is counted, continues to do so.
     * <p>
     * The object will be seen as being added the given {@code amount} of times.
     * 
     * @param e
     *            The object to be counted.
     * @param amount
     *            The amount of the given object.
     * @throws NullPointerException
     *             if e is {@code null}
     */
    public void addDataElement(
                               final E e, final int amount) {
        if (e == null)
            throw new NullPointerException("Element to be added must not be null.");

        if (!dataSet.contains(e)) {
            dataSet.add(e);
            counterBucket.put(e, amount);
        } else
            counterBucket.put(e, counterBucket.get(e) + amount);
    }

    /**
     * Returns an unsorted list of {@link DataNumberPair} objects, which hold the
     * counted objects and how many times they appeared.
     * 
     * @return The list of objects and how many times they were counted.
     */
    public List<DataNumberPair<E>> getCountedData() {
        final List<DataNumberPair<E>> countedData = Lists.newArrayList(dataSet.size());
        for (final E e : dataSet)
            countedData.add(DataNumberPair.of(e, counterBucket.get(e)));

        return countedData;
    }
}
