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

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Paint;
import java.io.File;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.*;

import javax.swing.JScrollBar;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.java.dev.spellcast.utilities.UtilityConstants;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.IntervalCategoryToolTipGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.IntervalMarker;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.data.gantt.SlidingGanttCategoryDataset;
import org.jfree.ui.Layer;
import org.jfree.ui.RectangleAnchor;
import org.jfree.ui.TextAnchor;

import com.googlecode.alv.chart.AbstractChart;
import com.googlecode.alv.logData.LogDataHolder;
import com.googlecode.alv.logData.logSummary.LevelData;
import com.googlecode.alv.logData.turn.turnAction.DayChange;
import com.googlecode.alv.logData.turn.turnAction.FamiliarChange;
import com.googlecode.alv.util.*;

public abstract class GanttChartBuilder extends AbstractChart {
    private List<TurnAreaCategory> categories = Lists.newArrayList();

    private final Map<String, FamiliarColor> familiarColors = new LinkedHashMap<String, FamiliarColor>();

    private SlidingGanttCategoryDataset dataset;

    private int lastTurnNumber = Integer.MIN_VALUE;

    protected GanttChartBuilder(
                                final String title, final LogDataHolder logData) {
        super(title, logData, false);

        try {
            categories = CategoryViewFileHandler.parseOutCategories(new File(UtilityConstants.ROOT_DIRECTORY
                                                                             + File.separator
                                                                             + UtilityConstants.DATA_DIRECTORY
                                                                             + "standardView.cvw"));
        } catch (final IOException e) {
            e.printStackTrace();
        }

        // Create a list of all used familiars if there is none present
        if (logData.getLogSummary().getFamiliarUsage().isEmpty()) {
            final Set<String> usedFamiliars = Sets.newHashSet();

            for (final FamiliarChange fc : logData.getFamiliarChanges())
                usedFamiliars.add(fc.getFamiliarName());

            for (final String s : usedFamiliars)
                familiarColors.put(s, new FamiliarColor(s, "none"));
        } else
            for (final DataNumberPair<String> dn : logData.getLogSummary().getFamiliarUsage())
                familiarColors.put(dn.getData(), new FamiliarColor(dn.getData(), "none"));

        addChart();
    }

    protected abstract SlidingGanttCategoryDataset createDataset();

    private JFreeChart createChart(
                                   final SlidingGanttCategoryDataset dataset) {
        this.dataset = dataset;
        final JFreeChart chart = ChartFactory.createGanttChart(getTitle(),
                                                               null,
                                                               null,
                                                               dataset,
                                                               false,
                                                               true,
                                                               false);

        final CategoryPlot plot = (CategoryPlot) chart.getPlot();
        final CategoryItemRenderer renderer = plot.getRenderer();

        plot.getDomainAxis().setMaximumCategoryLabelWidthRatio(0.15f);
        plot.setRangeAxis(new FixedZoomNumberAxis());
        plot.getRangeAxis().setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        plot.getRangeAxis().setAutoRange(false);
        plot.setRangeGridlinePaint(Color.black);
        setBarShadowVisible(chart, false);

        for (final AreaInterval ai : ((TurnRundownDataset) dataset.getUnderlyingDataset()).getDataset())
            if (lastTurnNumber < ai.getEndTurn())
                lastTurnNumber = ai.getEndTurn();
        addDayMarkers(plot);
        addLevelMarkers(plot);
        addFamiliarMarkers(plot);

        plot.getRangeAxis().setUpperBound(lastTurnNumber + 10);

        renderer.setSeriesPaint(0, Color.red);
        renderer.setBaseToolTipGenerator(new IntervalCategoryToolTipGenerator("{1}, {3} - {4}",
                                                                              NumberFormat.getInstance()));

        return chart;
    }

    private void addDayMarkers(
                               final CategoryPlot plot) {
        for (final DayChange dc : getLogData().getDayChanges()) {
            final ValueMarker day = new ValueMarker(dc.getTurnNumber());
            day.setLabel("Day " + dc.getDayNumber());
            day.setLabelAnchor(RectangleAnchor.TOP_RIGHT);
            day.setLabelTextAnchor(TextAnchor.TOP_LEFT);
            day.setStroke(new BasicStroke(2));
            day.setPaint(Color.BLUE);
            plot.addRangeMarker(day);
        }
    }

    private void addLevelMarkers(
                                 final CategoryPlot plot) {
        for (final LevelData ld : getLogData().getLevels()) {
            final ValueMarker level = new ValueMarker(ld.getLevelReachedOnTurn());
            level.setLabel("Level " + ld.getLevelNumber());
            level.setLabelAnchor(RectangleAnchor.BOTTOM_RIGHT);
            level.setLabelTextAnchor(TextAnchor.BOTTOM_LEFT);
            level.setStroke(new BasicStroke(2));
            level.setPaint(new Color(0, 150, 0));
            plot.addRangeMarker(level);
        }
    }

    private void addFamiliarMarkers(
                                    final CategoryPlot plot) {
        final LookAheadIterator<FamiliarChange> index = new LookAheadIterator<FamiliarChange>(getLogData().getFamiliarChanges()
                                                                                                          .iterator());
        while (index.hasNext()) {
            final FamiliarChange currentItem = index.next();
            final int intervalEnd = index.hasNext() ? index.peek().getTurnNumber() : lastTurnNumber;
            final IntervalMarker familiarUsage = new IntervalMarker(currentItem.getTurnNumber(),
                                                                    intervalEnd);
            familiarUsage.setPaint(getColor(currentItem.getFamiliarName()));
            if (!familiarUsage.getPaint().equals(Color.white))
                plot.addRangeMarker(familiarUsage, Layer.BACKGROUND);
        }
    }

    private Paint getColor(
                           final String familiarName) {
        final FamiliarColor color = familiarColors.get(familiarName);

        return color == null ? Color.white : color.getColorPaint();
    }

    public void addCategory(
                            final TurnAreaCategory category) {
        categories.add(category);
    }

    public void setCategories(
                              final List<TurnAreaCategory> categories) {
        this.categories = categories;
        updateChart();
    }

    public List<TurnAreaCategory> getCategories() {
        return categories;
    }

    public void setFamiliarColors(
                                  final List<FamiliarColor> familiarColors) {
        this.familiarColors.clear();
        for (final FamiliarColor color : familiarColors)
            this.familiarColors.put(color.getFamiliarName(), color);
    }

    public Collection<FamiliarColor> getFamiliarColors() {
        return familiarColors.values();
    }

    public void updateChart() {
        removeAll();
        addChart();
        updateUI();
    }

    @Override
    protected void addChart() {
        super.addChart();

        final int scrollCaretExtend = 20;
        int scrollableAreaIntervals = ((TurnRundownDataset) dataset.getUnderlyingDataset()).getDataset()
                                                                                           .size()
                                      - (dataset.getMaximumCategoryCount() - scrollCaretExtend);
        scrollableAreaIntervals = scrollableAreaIntervals > 20 ? scrollableAreaIntervals : 20;
        final JScrollBar scrollBar = new JScrollBar(JScrollBar.VERTICAL,
                                                    0,
                                                    scrollCaretExtend,
                                                    0,
                                                    scrollableAreaIntervals);
        scrollBar.getModel().addChangeListener(new ChangeListener() {

            public void stateChanged(
                                     final ChangeEvent e) {
                dataset.setFirstCategoryIndex(scrollBar.getValue());
            }
        });

        add(scrollBar, BorderLayout.EAST);
    }

    @Override
    protected ChartPanel createChartPanel() {
        return new ChartPanel(createChart(createDataset()), false);
    }

    /**
     * A "hacked" NumberAxis class. The only difference is that it zooms out to
     * 0 to maximum turncount + 10 instead of simply turning autoRange on, which
     * gives less than desirable results when scrolling through the gantt chart.
     */
    private final class FixedZoomNumberAxis extends NumberAxis {

        FixedZoomNumberAxis() {
            super(null);
        }

        @Override
        public void resizeRange(
                                final double percent, final double anchorValue) {
            if (percent > 0.0)
                super.resizeRange(percent, anchorValue);
            else
                setRange(0, lastTurnNumber + 10);
        }
    }
}
