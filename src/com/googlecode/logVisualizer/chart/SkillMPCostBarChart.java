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

package com.googlecode.logVisualizer.chart;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.jfree.chart.ChartPanel;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;

import com.googlecode.logVisualizer.logData.LogDataHolder;
import com.googlecode.logVisualizer.logData.Skill;
import com.googlecode.logVisualizer.util.Lists;

public final class SkillMPCostBarChart extends HorizontalBarChartBuilder {
    public SkillMPCostBarChart(
                               final LogDataHolder logData) {
        super(logData, "MP cost of Skills", "Skill", "MP cost", false);
    }

    @Override
    protected CategoryDataset createDataset() {
        final DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        final String seriesName = "MP cost of Skills";

        // Create new list to not destroy the sorting of the old one.
        final List<Skill> skills = Lists.newArrayList(getLogData().getAllSkillsCast());

        // Sort from highest MP cost to lowest MP cost.
        Collections.sort(skills, new Comparator<Skill>() {

            public int compare(
                               final Skill o1, final Skill o2) {
                return o2.getMpCost() - o1.getMpCost();
            }
        });

        // Add skills to dataset.
        for (final Skill s : skills) {
            if (s.getMpCost() > 0)
                dataset.addValue(s.getMpCost(), seriesName, s.getName());

            // The chart isn't readable anymore with too many entries
            if (dataset.getColumnCount() > 45)
                break;
        }

        return dataset;
    }

    @Override
    protected void addChartPanelListeners(
                                          final ChartPanel cp) {
        cp.addChartMouseListener(new SkillCastOnTurnsChartMouseEventListener(getLogData()));
    }
}
