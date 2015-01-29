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

package com.googlecode.logVisualizer.logData.logSummary;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.googlecode.logVisualizer.util.DataNumberPair;
import com.googlecode.logVisualizer.util.Lists;
import com.googlecode.logVisualizer.util.Maps;

/**
 * A helper class to make easy to implement per level summaries.
 * <p>
 * It more or less implements all the necessary logic, all that the
 * implementation class needs to do is define an add-operation for the used data
 * class.
 */
abstract class DataPerLevelSummary<T> {
    private final Map<Integer, T> levelData = Maps.newHashMap();

    /**
     * @return The result of both given data objects added together.
     */
    abstract T getDataAddition(
                               final T data1, final T data2);

    /**
     * Adds the given data to the given level.
     * 
     * @param levelNumber
     *            The level number.
     * @param meatData
     *            The data to add to the given level.
     */
    public void addLevelData(
                             final int levelNumber, final T data) {
        final Integer lvl = Integer.valueOf(levelNumber);
        if (levelData.containsKey(lvl)) {
            final T newData = getDataAddition(levelData.get(lvl), data);
            levelData.put(lvl, newData);
        } else
            levelData.put(lvl, data);
    }

    /**
     * @param levelNumber
     *            The level number of the wanted data.
     * @return The data of the given level.
     */
    public T getLevelData(
                          final int levelNumber) {
        return levelData.get(levelNumber);
    }

    /**
     * @return A sorted list of the data of all levels.
     */
    public List<DataNumberPair<T>> getAllLevelsData() {
        final List<DataNumberPair<T>> result = Lists.newArrayList(levelData.size());
        for (final Integer i : levelData.keySet())
            result.add(DataNumberPair.of(levelData.get(i), i));

        Collections.sort(result);

        return result;
    }
}
