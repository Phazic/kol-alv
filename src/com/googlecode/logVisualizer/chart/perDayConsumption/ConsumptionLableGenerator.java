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

package com.googlecode.logVisualizer.chart.perDayConsumption;

import org.jfree.chart.labels.CategoryItemLabelGenerator;
import org.jfree.data.category.CategoryDataset;

import com.googlecode.logVisualizer.logData.consumables.Consumable;

final class ConsumptionLableGenerator implements CategoryItemLabelGenerator {

    public String generateLabel(
                                final CategoryDataset dataset, final int row, final int column) {
        final ConsumptionDataset data = (ConsumptionDataset) dataset;
        final Consumable c = data.getConsumable(data.getRowKey(row).toString());
        if (c != null) {
            final StringBuilder str = new StringBuilder(50);
            str.append(c.getName());
            str.append(" (");
            str.append(c.getAdventureGain());
            str.append(")");

            return str.toString();
        }
        return null;
    }

    public String generateColumnLabel(
                                      final CategoryDataset dataset, final int column) {
        return dataset.getColumnKey(column).toString();
    }

    public String generateRowLabel(
                                   final CategoryDataset dataset, final int row) {
        return dataset.getRowKey(row).toString();
    }
}
