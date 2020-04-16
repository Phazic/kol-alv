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

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.googlecode.alv.util.Lists;
import com.googlecode.alv.util.Maps;

/**
 * A collection class for {@link DataPoint}s to make handling of them easier.
 * <p>
 * Note that this class doesn't update the names by which the {@link DataPoint}s
 * can be found through {@link #getDataPointByName(String)} in case the name of
 * a {@link DataPoint} is changed. Handling these situations has be done on the
 * use-site.
 * <p>
 * All methods in this class throw a {@link NullPointerException} if a null
 * object reference is passed in any parameter.
 */
final class DataTableContainer {
    private final Map<String, DataPoint> dataTable;

    private final DataPoint emptyDataPoint;

    /**
     * Creates a {@link DataPoint} collection that only consist of names.
     */
    DataTableContainer(
                       final Iterable<String> names) {
        dataTable = Maps.newHashMap(150);
        for (final String name : names)
            dataTable.put(name, new DataPoint(name));

        emptyDataPoint = new DataPoint("none");
    }

    /**
     * Creates a {@link DataPoint} collection based on the given
     * {@link DataPoint}s.
     */
    DataTableContainer(
                       final Collection<DataPoint> dataPoints) {
        if (dataPoints.isEmpty())
            throw new IllegalArgumentException("The data point collection must not be empty.");

        dataTable = Maps.newHashMap((int) (dataPoints.size() * 1.4));
        DataPoint tmp = null;
        for (final DataPoint dp : dataPoints) {
            tmp = dp;
            dataTable.put(dp.getName(), dp);
        }

        emptyDataPoint = new DataPoint(tmp);
        emptyDataPoint.setValueOf("name", "none");
    }

    /**
     * @param name
     *            The name of the {@link DataPoint} that should be returned.
     * @return The {@link DataPoint} with the given name. Returns {@code null}
     *         if no data point has the given name.
     */
    DataPoint getDataPointByName(
                                 final String name) {
        return dataTable.get(name);
    }

    /**
     * @param dp
     *            The {@link DataPoint} to add to the collection.
     */
    void addDataPoint(
                      final DataPoint dp) {
        dataTable.put(dp.getName(), dp);
    }

    /**
     * @param dp
     *            The {@link DataPoint} to remove from the collection. Note that
     *            this operation is based on the {@link DataPoint#getName()}
     *            property and not equals.
     */
    void removeDataPoint(
                         final DataPoint dp) {
        dataTable.remove(dp.getName());
    }

    /**
     * @return A collection containing all {@link DataPoint}s. The collection is
     *         sorted by name.
     */
    Collection<DataPoint> getDataTable() {
        final List<DataPoint> list = Lists.newArrayList(dataTable.values());
        Collections.sort(list);

        return list;
    }

    /**
     * @return A new {@link DataPoint} point instance containing all the
     *         value-pairs (with dummy values) that are also used by the
     *         {@link DataPoint}s in this collection.
     */
    DataPoint getDataPointTemplate() {
        return new DataPoint(emptyDataPoint);
    }
}