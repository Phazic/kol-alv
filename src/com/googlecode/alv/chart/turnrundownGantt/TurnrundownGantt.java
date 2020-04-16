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

package com.googlecode.alv.chart.turnrundownGantt;

import org.jfree.data.gantt.SlidingGanttCategoryDataset;

import com.googlecode.alv.logData.LogDataHolder;
import com.googlecode.alv.logData.turn.TurnInterval;

public final class TurnrundownGantt extends GanttChartBuilder {
    public TurnrundownGantt(
                            final LogDataHolder logData) {
        super("Turn rundown", logData);
    }

    @Override
    protected SlidingGanttCategoryDataset createDataset() {
        final TurnRundownDataset dataset = new TurnRundownDataset();

        for (final TurnInterval ti : getLogData().getTurnIntervalsSpent())
            dataset.addTurnInterval(ti, findCategoryName(ti));

        return new SlidingGanttCategoryDataset(dataset, 0, 30);
    }

    private String findCategoryName(
                                    final TurnInterval area) {
        for (final TurnAreaCategory tac : getCategories())
            for (final String s : tac.getTurnAreaNames())
                if (area.getAreaName().startsWith(s))
                    return tac.getCategoryName();

        return null;
    }

    @Override
    public LogDataHolder getLogData() {
        return super.getLogData();
    }
}
