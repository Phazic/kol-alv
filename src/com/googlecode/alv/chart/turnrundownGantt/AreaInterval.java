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

import java.util.List;

import com.googlecode.alv.logData.turn.SimpleTurnInterval;
import com.googlecode.alv.logData.turn.TurnInterval;
import com.googlecode.alv.util.Lists;

public final class AreaInterval {
    private final String name;

    private int startTurn;

    private int endTurn;

    private List<TurnInterval> subIntervals = Lists.newArrayList();

    public AreaInterval(
                        final TurnInterval area, final String areaIntervalName) {
        name = areaIntervalName;
        startTurn = area.getStartTurn();
        endTurn = area.getEndTurn();
        subIntervals.add(area);
    }

    public AreaInterval(
                        final String name, final int startTurn, final int endTurn) {
        this(new SimpleTurnInterval(name, startTurn, endTurn), name);
    }

    public String getName() {
        return name;
    }

    public int getStartTurn() {
        return startTurn;
    }

    public int getEndTurn() {
        return endTurn;
    }

    public void addSubInterval(
                               final TurnInterval interval) {
        if (interval.getStartTurn() < startTurn)
            startTurn = interval.getStartTurn();
        if (interval.getEndTurn() > endTurn)
            endTurn = interval.getEndTurn();

        subIntervals.add(interval);
    }

    public TurnInterval getSubInterval(
                                       final int index) {
        return index < subIntervals.size() && index >= 0 ? subIntervals.get(index) : null;
    }

    public void setSubIntervals(
                                final List<TurnInterval> subIntervals) {
        this.subIntervals = subIntervals;
    }

    public List<TurnInterval> getSubIntervals() {
        return subIntervals;
    }

    @Override
    public boolean equals(
                          final Object o) {
        if (o != null && o instanceof AreaInterval)
            return startTurn == ((AreaInterval) o).getStartTurn()
                   && endTurn == ((AreaInterval) o).getEndTurn()
                   && name.equals(((AreaInterval) o).getName())
                   && subIntervals.equals(((AreaInterval) o).getSubIntervals());

        return false;
    }

    @Override
    public int hashCode() {
        int result = 3242;
        result = 31 * result + startTurn;
        result = 31 * result + endTurn;
        result = 31 * result + name.hashCode();
        result = 31 * result + subIntervals.hashCode();

        return result;
    }
}
