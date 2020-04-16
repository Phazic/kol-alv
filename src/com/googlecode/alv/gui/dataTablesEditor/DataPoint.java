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

package com.googlecode.alv.gui.dataTablesEditor;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import com.googlecode.alv.util.Maps;
import com.googlecode.alv.util.Pair;

/**
 * A representation of a single data point of one of the programs internal data
 * tables.
 * <p>
 * A data point can have multiple value-pairs linked to it. Every value-pair
 * consist of a value name and the actual value and the values can only be
 * accessed through the value names.
 * <p>
 * Every data point has at least one value-pair, namely its name.
 * <p>
 * All methods in this class throw a {@link NullPointerException} if a null
 * object reference is passed in any parameter.
 */
final class DataPoint implements Comparable<DataPoint> {
    private final Map<String, Object> nameValuePairs;

    /**
     * Creates a data point consisting of only the mandatory name value-pair.
     */
    @SuppressWarnings("unchecked")
    DataPoint(
              final String name) {
        if (name == null)
            throw new NullPointerException("The name must not be null.");

        nameValuePairs = Maps.newHashMap(Pair.of("name", (Object) name));
    }

    /**
     * Creates a data point consisting of the mandatory name and a single other
     * value-pair.
     */
    @SuppressWarnings("unchecked")
    DataPoint(
              final String name, final Pair<String, ? extends Object> singleValuePair) {
        if (name == null)
            throw new NullPointerException("The name must not be null.");
        if (singleValuePair == null)
            throw new NullPointerException("The name value pair must not be null.");

        nameValuePairs = Maps.newHashMap(Pair.of("name", name), singleValuePair);
    }

    /**
     * Creates a data point consisting of the mandatory name and the given other
     * value-pairs.
     * <p>
     * Please note that the given map is directly used internally and as such
     * needs to allow write operations.
     */
    @SuppressWarnings("unchecked")
    DataPoint(
              final String name, final Map<String, ? extends Object> nameValuePairs) {
        if (name == null)
            throw new NullPointerException("The name must not be null.");
        if (nameValuePairs == null)
            throw new NullPointerException("The name value pairs map must not be null.");

        this.nameValuePairs = (Map<String, Object>) nameValuePairs;

        this.nameValuePairs.put("name", name);
    }

    /**
     * Creates a copy of the given data point.
     */
    DataPoint(
              final DataPoint toClone) {
        if (toClone == null)
            throw new NullPointerException("The data point to clone must not be null.");

        nameValuePairs = Maps.newHashMap(toClone.getNameValuePairs());
    }

    /**
     * Convenience method, equal to {@code nameValuePairs.get("name")}.
     * 
     * @return The name of this data point.
     */
    String getName() {
        return (String) nameValuePairs.get("name");
    }

    /**
     * @param name
     *            The name of the value which should be returned.
     * @return The value with the given name. Returns {@code null} in case the
     *         value name doesn't exist.
     */
    Object getValueOf(
                      final String name) {
        return nameValuePairs.get(name);
    }

    /**
     * Sets the value of the value-pair with the given name.
     * 
     * @param name
     *            The name of the value which should be set.
     * @param value
     *            The new value of the value-pair.
     * @throws IllegalArgumentException
     *             if there doesn't exist a value with the given name
     */
    void setValueOf(
                    final String name, final Object value) {
        if (!nameValuePairs.containsKey(name))
            throw new IllegalArgumentException("The given value name doesn't exist.");

        nameValuePairs.put(name, value);
    }

    /**
     * @return A read-only set of all value-pairs.
     */
    Set<Entry<String, Object>> getNameValuePairs() {
        return Collections.unmodifiableSet(nameValuePairs.entrySet());
    }

    /**
     * Compares the given data point with this data point based on the names of
     * the data points, ignoring case.
     * 
     * @see Comparable#compareTo(Object)
     */
    public int compareTo(
                         final DataPoint o) {
        return getName().compareToIgnoreCase(o.getName());
    }

    @Override
    public String toString() {
        return getName();
    }
}