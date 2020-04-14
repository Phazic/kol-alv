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

package com.googlecode.alv.chart;

import org.jfree.chart.axis.NumberAxis;

/**
 * A "hacked" NumberAxis class. The only difference is that it zooms out to 0 to
 * the maximum x value instead of simply turning autoRange on, which gives less
 * than desirable results in the form of blank unused space behind the graphs.
 */
final class FixedZoomNumberAxis extends NumberAxis {
    private final double lastXValue;

    FixedZoomNumberAxis(
                        final double lastXValue) {
        super(null);

        this.lastXValue = lastXValue;
    }

    @Override
    public void resizeRange(
                            final double percent, final double anchorValue) {
        if (percent > 0.0)
            super.resizeRange(percent, anchorValue);
        else
            setRange(0, lastXValue);
    }
}