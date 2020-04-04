/*
 * Copyright (c) 2005, romain guy (romain.guy@jext.org) and craig wickesser (craig@codecraig.com) and henry story
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 * 
 *     * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 *     * Neither the name of the <ORGANIZATION> nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package net.java.swingfx.waitwithstyle;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

/**
 * A InfiniteProgressPanel-like component, but more efficient. This is the
 * preferred class to use unless you need the total control over the appearance
 * that InfiniteProgressPanel gives you.<br>
 * <br>
 * An infinite progress panel displays a rotating figure and a message to notice
 * the user of a long, duration unknown task. The shape and the text are drawn
 * upon a white veil which alpha level (or shield value) lets the underlying
 * component shine through. This panel is meant to be used as a <i>glass
 * pane</i> in the window performing the long operation. <br>
 * <br>
 * Calling setVisible(true) makes the component visible and starts the
 * animation. Calling setVisible(false) halts the animation and makes the
 * component invisible. Once you've started the animation all the mouse events
 * are intercepted by this panel, preventing them from being forwared to the
 * underlying components. <br>
 * <br>
 * The panel can be controlled by the <code>setVisible()</code>, method. <br>
 * <br>
 * This version of the infinite progress panel does not display any fade in/out
 * when the animation is started/stopped.<br>
 * <br>
 * Example: <br>
 * <br>
 * 
 * <pre>
 * PerformanceInfiniteProgressPanel pane = new PerformanceInfiniteProgressPanel();
 * frame.setGlassPane(pane);
 * pane.setVisible(true);
 * // Do something here, presumably launch a new thread
 * // ...
 * // When the thread terminates:
 * pane.setVisible(false);
 * </pre>
 * 
 * @see InfiniteProgressPanel <br>
 * <br>
 *      $Revision: 1.5 $
 * @author Romain Guy
 * @author Henry Story
 * @version 1.0
 */

public class PerformanceInfiniteProgressPanel extends JComponent implements ActionListener,
        CancelableAdaptee {
    private static final int DEFAULT_NUMBER_OF_BARS = 12;

    private final int numBars;

    protected InfiniteProgressAdapter infiniteProgressAdapter;

    private final double dScale = 1.2d;

    private final MouseAdapter mouseAdapter = new MouseAdapter() {};

    private final MouseMotionAdapter mouseMotionAdapter = new MouseMotionAdapter() {};

    private final KeyAdapter keyAdapter = new KeyAdapter() {};

    private final ComponentAdapter componentAdapter = new ComponentAdapter() {
        @Override
        public void componentResized(
                                     final ComponentEvent e) {
            if (useBackBuffer == true) {
                setOpaque(false);
                imageBuf = null;
                iterate = 3;
            }
        }
    };

    private BufferedImage imageBuf = null;

    private final Area[] bars;

    private Rectangle barsBounds = null;

    private Rectangle barsScreenBounds = null;

    private AffineTransform centerAndScaleTransform = null;

    private final Timer timer = new Timer(1000 / 4, this);

    private Color[] colors = null;

    private int colorOffset = 0;

    private final boolean useBackBuffer;

    private final boolean tempHide = false;

    private String text;

    /**
     * Defaults to using a back buffer.
     */
    public PerformanceInfiniteProgressPanel() {
        this(true);
    }

    public PerformanceInfiniteProgressPanel(
                                            final boolean i_bUseBackBuffer) {
        this(i_bUseBackBuffer, DEFAULT_NUMBER_OF_BARS);
    }

    public PerformanceInfiniteProgressPanel(
                                            final int numBars) {
        this(true, numBars, null);
    }

    public PerformanceInfiniteProgressPanel(
                                            final InfiniteProgressAdapter infiniteProgressAdapter) {
        this(true, DEFAULT_NUMBER_OF_BARS, infiniteProgressAdapter);
    }

    public PerformanceInfiniteProgressPanel(
                                            final boolean i_bUseBackBuffer, final int numBars) {
        this(i_bUseBackBuffer, numBars, null);
    }

    public PerformanceInfiniteProgressPanel(
                                            final boolean i_bUseBackBuffer,
                                            final InfiniteProgressAdapter infiniteProgressAdapter) {
        this(i_bUseBackBuffer, DEFAULT_NUMBER_OF_BARS, infiniteProgressAdapter);
    }

    public PerformanceInfiniteProgressPanel(
                                            final int numBars,
                                            final InfiniteProgressAdapter infiniteProgressAdapter) {
        this(true, numBars, infiniteProgressAdapter);
    }

    public PerformanceInfiniteProgressPanel(
                                            final boolean i_bUseBackBuffer, final int numBars,
                                            final InfiniteProgressAdapter infiniteProgressAdapter) {
        useBackBuffer = i_bUseBackBuffer;
        this.numBars = numBars;
        setInfiniteProgressAdapter(infiniteProgressAdapter);

        colors = new Color[numBars * 2];
        // build bars
        bars = buildTicker(numBars);
        // calculate bars bounding rectangle
        barsBounds = new Rectangle();
        for (final Area bar : bars)
            barsBounds = barsBounds.union(bar.getBounds());
        // create colors
        for (int i = 0; i < bars.length; i++) {
            final int channel = 224 - 128 / (i + 1);
            colors[i] = new Color(channel, channel, channel);
            colors[numBars + i] = colors[i];
        }
        // set cursor
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        // set opaque
        setOpaque(useBackBuffer);
    }

    protected void setInfiniteProgressAdapter(
                                              final InfiniteProgressAdapter infiniteProgressAdapter) {
        this.infiniteProgressAdapter = infiniteProgressAdapter;
    }

    int iterate; // we draw use transparency to draw a number of iterations

    // before making a snapshot

    /**
     * Called to animate the rotation of the bar's colors
     */
    public void actionPerformed(
                                final ActionEvent e) {
        // rotate colors
        if (colorOffset == numBars - 1)
            colorOffset = 0;
        else
            colorOffset++;
        // repaint
        if (barsScreenBounds != null)
            repaint(barsScreenBounds);
        else
            repaint();
        if (useBackBuffer && imageBuf == null)
            if (iterate < 0)
                try {
                    makeSnapshot();
                    setOpaque(true);
                } catch (final AWTException e1) {
                    e1.printStackTrace(); // todo: decide what exception to
                    // throw
                }
            else
                iterate--;
    }

    /**
     * Show/Hide the pane, starting and stopping the animation as you go
     */
    @Override
    public void setVisible(
                           final boolean i_bIsVisible) {
        setOpaque(false);
        // capture
        if (i_bIsVisible) {
            if (useBackBuffer) {
                // add window resize listener
                final Window w = SwingUtilities.getWindowAncestor(this);
                if (w != null)
                    w.addComponentListener(componentAdapter);
                else
                    addAncestorListener(new AncestorListener() {
                        public void ancestorAdded(
                                                  final AncestorEvent event) {
                            final Window w = SwingUtilities.getWindowAncestor(PerformanceInfiniteProgressPanel.this);
                            if (w != null)
                                w.addComponentListener(componentAdapter);
                        }

                        public void ancestorRemoved(
                                                    final AncestorEvent event) {}

                        public void ancestorMoved(
                                                  final AncestorEvent event) {}
                    });
                iterate = 3;
            }
            // capture events
            addMouseListener(mouseAdapter);
            addMouseMotionListener(mouseMotionAdapter);
            addKeyListener(keyAdapter);
            // start anim
            if (infiniteProgressAdapter != null) {
                infiniteProgressAdapter.animationStarting();
                infiniteProgressAdapter.rampUpEnded();
            }
            timer.start();
        } else {
            // stop anim
            timer.stop();
            if (infiniteProgressAdapter != null)
                infiniteProgressAdapter.animationStopping();
            // / free back buffer
            imageBuf = null;
            // stop capturing events
            removeMouseListener(mouseAdapter);
            removeMouseMotionListener(mouseMotionAdapter);
            removeKeyListener(keyAdapter);
            // remove window resize listener
            final Window oWindow = SwingUtilities.getWindowAncestor(this);
            if (oWindow != null)
                oWindow.removeComponentListener(componentAdapter);
        }
        super.setVisible(i_bIsVisible);
    }

    private void makeSnapshot()
                               throws AWTException {
        final Window oWindow = SwingUtilities.getWindowAncestor(this);
        final Insets oInsets = oWindow.getInsets();
        final Rectangle oRectangle = new Rectangle(oWindow.getBounds());
        oRectangle.x += oInsets.left;
        oRectangle.y += oInsets.top;
        oRectangle.width -= oInsets.left + oInsets.right;
        oRectangle.height -= oInsets.top + oInsets.bottom;
        // capture window contents
        imageBuf = new Robot().createScreenCapture(oRectangle);
        // no need to fade because we are allready using an image that is
        // showing through
    }

    /**
     * Recalc bars based on changes in size
     */
    @Override
    public void setBounds(
                          final int x, final int y, final int width, final int height) {
        super.setBounds(x, y, width, height);
        // update centering transform
        centerAndScaleTransform = new AffineTransform();
        centerAndScaleTransform.translate(getWidth() / 2d, getHeight() / 2d);
        centerAndScaleTransform.scale(dScale, dScale);
        // calc new bars bounds
        if (barsBounds != null) {
            final Area oBounds = new Area(barsBounds);
            oBounds.transform(centerAndScaleTransform);
            barsScreenBounds = oBounds.getBounds();
        }
    }

    /**
     * paint background dimed and bars over top
     */
    @Override
    protected void paintComponent(
                                  final Graphics g) {
        if (!tempHide) {
            final Rectangle oClip = g.getClipBounds();
            if (imageBuf != null) {
                // draw background image
                // g.drawImage(imageBuf, 0, 0,
                // null);
            } else {
                g.setColor(new Color(255, 255, 255, 180));
                g.fillRect(oClip.x, oClip.y, oClip.width, oClip.height);
            }
            // move to center
            final Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.transform(centerAndScaleTransform);
            // draw ticker
            for (int i = 0; i < bars.length; i++) {
                g2.setColor(colors[i + colorOffset]);
                g2.fill(bars[i]);
            }
            final double maxY = InfiniteProgressPanel.drawTextAt(text,
                                                                 getFont(),
                                                                 g2,
                                                                 getWidth(),
                                                                 barsScreenBounds.getMaxY(),
                                                                 getForeground());
            if (infiniteProgressAdapter != null)
                infiniteProgressAdapter.paintSubComponents(maxY);
        }
    }

    /**
     * Builds the circular shape and returns the result as an array of
     * <code>Area</code>. Each <code>Area</code> is one of the bars composing
     * the shape.
     */
    private static Area[] buildTicker(
                                      final int i_iBarCount) {
        final Area[] ticker = new Area[i_iBarCount];
        final Point2D.Double center = new Point2D.Double(0, 0);
        final double fixedAngle = 2.0 * Math.PI / i_iBarCount;

        for (double i = 0.0; i < i_iBarCount; i++) {
            final Area primitive = buildPrimitive();

            final AffineTransform toCenter = AffineTransform.getTranslateInstance(center.getX(),
                                                                                  center.getY());
            final AffineTransform toBorder = AffineTransform.getTranslateInstance(45.0, -6.0);
            final AffineTransform toCircle = AffineTransform.getRotateInstance(-i * fixedAngle,
                                                                               center.getX(),
                                                                               center.getY());

            final AffineTransform toWheel = new AffineTransform();
            toWheel.concatenate(toCenter);
            toWheel.concatenate(toBorder);

            primitive.transform(toWheel);
            primitive.transform(toCircle);

            ticker[(int) i] = primitive;
        }

        return ticker;
    }

    /**
     * Builds a bar.
     */
    private static Area buildPrimitive() {
        final Rectangle2D.Double body = new Rectangle2D.Double(6, 0, 30, 12);
        final Ellipse2D.Double head = new Ellipse2D.Double(0, 0, 12, 12);
        final Ellipse2D.Double tail = new Ellipse2D.Double(30, 0, 12, 12);

        final Area tick = new Area(body);
        tick.add(new Area(head));
        tick.add(new Area(tail));

        return tick;
    }

    public void start() {
        setVisible(true);
    }

    public void stop() {
        setVisible(false);
    }

    public void setText(
                        final String text) {
        this.text = text;
        repaint();
    }

    public String getText() {
        return text;
    }

    public JComponent getComponent() {
        return this;
    }

    /**
     * Adds a listener to the cancel button in this progress panel.
     * 
     * @throws RuntimeException
     *             if the infiniteProgressAdapter is null or is not a
     *             CancelableProgessAdapter
     * @param listener Listener to add to the Cancel button
     */
    public void addCancelListener(
                                  final ActionListener listener) {
        if (infiniteProgressAdapter instanceof CancelableProgessAdapter)
            ((CancelableProgessAdapter) infiniteProgressAdapter).addCancelListener(listener);
        else
            throw new RuntimeException("Expected CancelableProgessAdapter for cancel listener.  Adapter is "
                                       + infiniteProgressAdapter);
    }

    /**
     * Removes a listener to the cancel button in this progress panel.
     * 
     * @throws RuntimeException
     *             if the infiniteProgressAdapter is null or is not a
     *             CancelableProgessAdapter
     * @param listener Listener to remove from the Cancel button
     */
    public void removeCancelListener(
                                     final ActionListener listener) {
        if (infiniteProgressAdapter instanceof CancelableProgessAdapter)
            ((CancelableProgessAdapter) infiniteProgressAdapter).removeCancelListener(listener);
        else
            throw new RuntimeException("Expected CancelableProgessAdapter for cancel listener.  Adapter is "
                                       + infiniteProgressAdapter);
    }
}
