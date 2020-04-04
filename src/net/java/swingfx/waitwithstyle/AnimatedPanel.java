/*
 * Copyright (c) 2005, romain guy (romain.guy@jext.org) and craig wickesser (craig@codecraig.com)
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
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;

import javax.swing.ImageIcon;
import javax.swing.JPanel;

/**
 * Display an animated panel. The panel contains a picture and a text message.
 * As soon as <code>start()</code> is called, the pictures and the text glow in
 * cycles. The animation can be stopped at anytime by calling
 * <code>stop()</code>. You can set the font and its color by calling
 * <code>setFont()</code> and <code>setForeground()</code>.
 * 
 * @author Romain Guy, 17/02/2005
 * @since 1.0 <br>
 *        $Revision: 1.2 $
 */
public class AnimatedPanel extends JPanel {
    private static final long serialVersionUID = 3257288036894324529L;

    protected float gradient;

    protected String message;

    protected Thread animator;

    protected BufferedImage convolvedImage;

    protected BufferedImage originalImage;

    protected static AlphaComposite composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER);

    protected RenderingHints brightnessHints;

    /**
     * Creates an animated panel with a message and a picture.
     * 
     * @param message
     *            The message to display, can not be null nor empty.
     * @param icon
     *            The picture to display, can not be null
     */
    public AnimatedPanel(
                         final String message, final ImageIcon icon) {
        // since the message can not be null or empty, validate it
        validateMessage(message);
        // the icon can't be null either
        if (icon == null)
            throw new IllegalArgumentException("Icon can not be null.");

        this.message = message;

        final Image image = icon.getImage();
        originalImage = new BufferedImage(icon.getIconWidth(),
                                          icon.getIconHeight(),
                                          BufferedImage.TYPE_INT_ARGB);
        convolvedImage = new BufferedImage(icon.getIconWidth(),
                                           icon.getIconHeight(),
                                           BufferedImage.TYPE_INT_ARGB);
        final Graphics g = originalImage.createGraphics();
        g.drawImage(image, 0, 0, this);
        g.dispose();

        brightnessHints = new RenderingHints(RenderingHints.KEY_RENDERING,
                                             RenderingHints.VALUE_RENDER_QUALITY);

        setBrightness(1.0f);
        setOpaque(false);
    }

    /**
     * Performs basic validation on the given <code>msg</code>
     * 
     * @param msg
     *            the message to validate
     * @throws IllegalArgumentException
     *             if <code>msg</code> is <code>null</code> or empty
     */
    private void validateMessage(
                                 final String msg)
                                                  throws IllegalArgumentException {
        final boolean isNullOrEmpty = msg == null || msg.equals("");

        if (isNullOrEmpty)
            throw new IllegalArgumentException("Invalid message.  Message can not be null or empty.");
    }

    /**
     * Changes the displayed message at runtime.
     * 
     * @param text
     *            The message to be displayed. Can not be null or empty.
     */
    public void setText(
                        final String text) {
        // since the message can not be null or empty, validate it
        validateMessage(text);

        message = text;
        repaint();
    }

    /**
     * @return the current message.
     */
    public String getText() {
        return message;
    }

    @Override
    public void paintComponent(
                               final Graphics g) {
        super.paintComponent(g);

        if (convolvedImage != null) {
            final int width = getWidth();
            final int height = getHeight();

            synchronized (convolvedImage) {
                final Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_RENDERING,
                                    RenderingHints.VALUE_RENDER_QUALITY);
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                    RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS,
                                    RenderingHints.VALUE_FRACTIONALMETRICS_ON);

                final FontRenderContext context = g2.getFontRenderContext();
                final TextLayout layout = new TextLayout(message, getFont(), context);
                final Rectangle2D bounds = layout.getBounds();

                final int x = (width - convolvedImage.getWidth(null)) / 2;
                final int y = (int) (height - (convolvedImage.getHeight(null) + bounds.getHeight() + layout.getAscent())) / 2;

                g2.drawImage(convolvedImage, x, y, this);
                final Color foreground = getForeground();
                g2.setColor(new Color(foreground.getRed(),
                                      foreground.getGreen(),
                                      foreground.getBlue(),
                                      (int) (gradient * 255)));
                layout.draw(g2,
                            (float) (width - bounds.getWidth()) / 2,
                            (float) (y + convolvedImage.getHeight(null) + bounds.getHeight() + layout.getAscent()));
            }
        }
    }

    /**
     * Changes the image luminosity.
     */
    private void setBrightness(
                               final float multiple) {
        final float[] brightKernel = { multiple };
        final BufferedImageOp bright = new ConvolveOp(new Kernel(1, 1, brightKernel),
                                                      ConvolveOp.EDGE_NO_OP,
                                                      brightnessHints);
        bright.filter(originalImage, convolvedImage);
        repaint();
    }

    /**
     * Changes the text gradient control value.
     */
    private void setGradientFactor(
                                   final float gradient) {
        this.gradient = gradient;
    }

    /**
     * Starts the animation. A thread called "Highlighter" is spawned and can be
     * interrupted at anytime by invoking <code>stop()</code>.
     */
    public void start() {
        animator = new Thread(new HighlightCycler(), "Highlighter");
        animator.start();
    }

    /**
     * Safely stops the animation.
     */
    public void stop() {
        if (animator != null)
            animator.interrupt();
        animator = null;
    }

    /**
     * Makes the image luminosity and the text gradient to cycle.
     */
    class HighlightCycler implements Runnable {

        private int way = 1;

        private final int LOWER_BOUND = 10;

        private final int UPPER_BOUND = 35;

        private int value = LOWER_BOUND;

        public void run() {
            while (true) {
                try {
                    Thread.sleep(1000 / (UPPER_BOUND - LOWER_BOUND));
                } catch (final InterruptedException e) {
                    return;
                }

                value += way;
                if (value > UPPER_BOUND) {
                    value = UPPER_BOUND;
                    way = -1;
                } else if (value < LOWER_BOUND) {
                    value = LOWER_BOUND;
                    way = 1;
                }

                synchronized (convolvedImage) {
                    setBrightness((float) value / 10);
                    setGradientFactor((float) value / UPPER_BOUND);
                }
            }
        }
    }
}
