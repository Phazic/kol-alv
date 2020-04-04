package com.sun.java.forums;

import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.*;
import javax.swing.event.EventListenerList;
import javax.swing.plaf.basic.BasicTabbedPaneUI;

/**
 * <p>
 * A {@code JTabbedPane} which has a close ('X') icon on each tab.
 * <p>
 * To add a tab, use the method addTab(String, Component)
 * <p>
 * To have an extra icon on each tab (e.g. like in JBuilder, showing the file
 * type) use the method addTab(String, Component, Icon). Only clicking the 'X'
 * closes the tab.
 * <p>
 * <b>Source</b>: <br>
 * <a href="http://forums.java.sun.com/thread.jspa?threadID=337070&start=15"> Java
 * Forums - JTabbedPane with close icons, Post #15 </a>
 */
public class CloseableTabbedPane extends JTabbedPane implements MouseListener, MouseMotionListener {

    /**
     * The {@code EventListenerList}.
     */
    private EventListenerList listenerList = null;

    /**
     * The viewport of the scrolled tabs.
     */
    private JViewport headerViewport = null;

    /**
     * The normal closeicon.
     */
    private Icon normalCloseIcon = null;

    /**
     * The closeicon when the mouse is over.
     */
    private Icon hooverCloseIcon = null;

    /**
     * The closeicon when the mouse is pressed.
     */
    private Icon pressedCloseIcon = null;

    /**
     * Creates a new instance of {@code CloseableTabbedPane}.
     */
    public CloseableTabbedPane() {
        this(SwingConstants.LEFT);
    }

    /**
     * Creates a new instance of {@code CloseableTabbedPane}.
     * 
     * @param horizontalTextPosition
     *            the horizontal position of the text (e.g.
     *            SwingUtilities.TRAILING or SwingUtilities.LEFT)
     */
    public CloseableTabbedPane(
                               final int horizontalTextPosition) {
        super(JTabbedPane.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);
        init(horizontalTextPosition);
    }

    @Override
    public void setSelectedIndex(
                                 final int index) {
        final int lastIndex = getSelectedIndex();

        super.setSelectedIndex(index);

        if (lastIndex != -1 && lastIndex < getTabCount()) {
            setBackgroundAt(lastIndex, null);
            setForegroundAt(lastIndex, null);
        }

        setBackgroundAt(index, new Color(205, 205, 255));
        setForegroundAt(index, Color.black);
    }

    /**
     * Initializes the {@code CloseableTabbedPane}.
     * 
     * @param horizontalTextPosition
     *            the horizontal position of the text (e.g.
     *            SwingUtilities.TRAILING or SwingUtilities.LEFT)
     */
    private void init(
                      final int horizontalTextPosition) {
        listenerList = new EventListenerList();
        addMouseListener(this);
        addMouseMotionListener(this);
        this.setUI(new CloseableTabbedPaneUI(horizontalTextPosition));
    }

    /**
     * Allows setting own closeicons.
     * 
     * @param normal
     *            the normal closeicon
     * @param hoover
     *            the closeicon when the mouse is over
     * @param pressed
     *            the closeicon when the mouse is pressed
     */
    public void setCloseIcons(
                              final Icon normal, final Icon hoover, final Icon pressed) {
        normalCloseIcon = normal;
        hooverCloseIcon = hoover;
        pressedCloseIcon = pressed;
    }

    /**
     * Adds a {@code Component} represented by a title and no icon.
     * 
     * @param title
     *            the title to be displayed in this tab
     * @param component
     *            the component to be displayed when this tab is clicked
     */
    @Override
    public void addTab(
                       final String title, final Component component) {
        this.addTab(title, component, null);
    }

    /**
     * Adds a {@code Component} represented by a title and an icon.
     * 
     * @param title
     *            the title to be displayed in this tab
     * @param component
     *            the component to be displayed when this tab is clicked
     * @param extraIcon
     *            the icon to be displayed in this tab
     */
    public void addTab(
                       final String title, final Component component, final Icon extraIcon) {
        insertTab(title, extraIcon, component, title, getTabCount());
    }

    @Override
    public void insertTab(
                          String title, final Icon extraIcon, final Component component,
                          final String tooltip, final int index) {
        boolean doPaintCloseIcon = true;
        try {

            title = "    " + title + "        ";

            Object prop = null;
            if ((prop = ((JComponent) component).getClientProperty("isClosable")) != null)
                doPaintCloseIcon = ((Boolean) prop).booleanValue();
        } catch (final Exception ignored) {
            // Could probably be a ClassCastException
        }

        component.addPropertyChangeListener("isClosable", new PropertyChangeListener() {
            public void propertyChange(
                                       final PropertyChangeEvent e) {
                final Object newVal = e.getNewValue();
                int index = -1;
                if (e.getSource() instanceof Component)
                    index = CloseableTabbedPane.this.indexOfComponent((Component) e.getSource());
                if (index != -1 && newVal != null && newVal instanceof Boolean)
                    CloseableTabbedPane.this.setCloseIconVisibleAt(index,
                                                                   ((Boolean) newVal).booleanValue());
            }
        });

        super.insertTab(title,
                        doPaintCloseIcon ? new CloseTabIcon(extraIcon) : null,
                        component,
                        tooltip,
                        index);

        if (headerViewport == null)
            for (final Component c : getComponents())
                if ("TabbedPane.scrollableViewport".equals(c.getName()))
                    headerViewport = (JViewport) c;
    }

    /**
     * Sets the closeicon at {@code index}.
     * 
     * @param index
     *            the tab index where the icon should be set
     * @param icon
     *            the icon to be displayed in the tab
     * @throws IndexOutOfBoundsException
     *             if index is out of range (index < 0 || index >= tab count)
     */
    private void setCloseIconVisibleAt(
                                       final int index, final boolean iconVisible)
                                                                                  throws IndexOutOfBoundsException {
        super.setIconAt(index, iconVisible ? new CloseTabIcon(null) : null);
    }

    /**
     * Invoked when the mouse button has been clicked (pressed and released) on
     * a component.
     * 
     * @param e
     *            the {@code MouseEvent}
     */
    public void mouseClicked(
                             final MouseEvent e) {
        processMouseEvents(e);
    }

    /**
     * Invoked when the mouse enters a component.
     * 
     * @param e
     *            the {@code MouseEvent}
     */
    public void mouseEntered(
                             final MouseEvent e) {}

    /**
     * Invoked when the mouse exits a component.
     * 
     * @param e
     *            the {@code MouseEvent}
     */
    public void mouseExited(
                            final MouseEvent e) {
        for (int i = 0; i < getTabCount(); i++) {
            final CloseTabIcon icon = (CloseTabIcon) getIconAt(i);
            if (icon != null)
                icon.mouseover = false;
        }
        this.repaint();
    }

    /**
     * Invoked when a mouse button has been pressed on a component.
     * 
     * @param e
     *            the {@code MouseEvent}
     */
    public void mousePressed(
                             final MouseEvent e) {
        processMouseEvents(e);
    }

    /**
     * Invoked when a mouse button has been released on a component.
     * 
     * @param e
     *            the {@code MouseEvent}
     */
    public void mouseReleased(
                              final MouseEvent e) {}

    /**
     * Invoked when a mouse button is pressed on a component and then dragged.
     * {@code MOUSE_DRAGGED} events will continue to be delivered to the
     * component where the drag originated until the mouse button is released
     * (regardless of whether the mouse position is within the bounds of the
     * component).
     * <p>
     * Due to platform-dependent Drag and Drop implementations, {@code
     * MOUSE_DRAGGED} events may not be delivered during a native Drag and Drop
     * operation.
     * 
     * @param e
     *            the {@code MouseEvent}
     */
    public void mouseDragged(
                             final MouseEvent e) {
        processMouseEvents(e);
    }

    /**
     * Invoked when the mouse cursor has been moved onto a component but no
     * buttons have been pushed.
     * 
     * @param e
     *            the {@code MouseEvent}
     */
    public void mouseMoved(
                           final MouseEvent e) {
        processMouseEvents(e);
    }

    /**
     * Processes all caught {@code MouseEvent}s.
     * 
     * @param e
     *            the {@code MouseEvent}
     */
    private void processMouseEvents(
                                    final MouseEvent e) {
        final int tabNumber = getUI().tabForCoordinate(this, e.getX(), e.getY());
        if (tabNumber < 0)
            return;
        boolean otherWasOver = false;
        for (int i = 0; i < getTabCount(); i++)
            if (i != tabNumber) {
                final CloseTabIcon ic = (CloseTabIcon) getIconAt(i);
                if (ic != null) {
                    if (ic.mouseover)
                        otherWasOver = true;
                    ic.mouseover = false;
                }
            }
        if (otherWasOver)
            this.repaint();
        final CloseTabIcon icon = (CloseTabIcon) getIconAt(tabNumber);
        if (icon != null) {
            final Rectangle rect = icon.getBounds();
            final boolean vpIsNull = headerViewport == null;
            final Point pos = vpIsNull ? new Point() : headerViewport.getViewPosition();
            final int vpDiffX = vpIsNull ? 0 : headerViewport.getX();
            final int vpDiffY = vpIsNull ? 0 : headerViewport.getY();
            final Rectangle drawRect = new Rectangle(rect.x - pos.x + vpDiffX,
                                                     rect.y - pos.y + vpDiffY,
                                                     rect.width,
                                                     rect.height);

            if (e.getID() == MouseEvent.MOUSE_PRESSED) {
                icon.mousepressed = e.getModifiers() == InputEvent.BUTTON1_MASK;
                this.repaint(drawRect);
            } else if (e.getID() == MouseEvent.MOUSE_MOVED || e.getID() == MouseEvent.MOUSE_DRAGGED
                       || e.getID() == MouseEvent.MOUSE_CLICKED) {
                pos.x += e.getX() - vpDiffX;
                pos.y += e.getY() - vpDiffY;
                if (rect.contains(pos)) {
                    if (e.getID() == MouseEvent.MOUSE_CLICKED) {
                        final int selIndex = getSelectedIndex();
                        if (fireCloseTab(selIndex)) {
                            if (selIndex > 0) {
                                // to prevent uncatchable null-pointers
                                final Rectangle rec = getUI().getTabBounds(this, selIndex - 1);

                                final MouseEvent event = new MouseEvent((Component) e.getSource(),
                                                                        e.getID() + 1,
                                                                        System.currentTimeMillis(),
                                                                        e.getModifiers(),
                                                                        rec.x,
                                                                        rec.y,
                                                                        e.getClickCount(),
                                                                        e.isPopupTrigger(),
                                                                        e.getButton());
                                dispatchEvent(event);
                            }
                            // the tab is being closed
                            // removeTabAt(tabNumber);
                            this.remove(selIndex);
                        } else {
                            icon.mouseover = false;
                            icon.mousepressed = false;
                            this.repaint(drawRect);
                        }
                    } else {
                        icon.mouseover = true;
                        icon.mousepressed = e.getModifiers() == InputEvent.BUTTON1_MASK;
                    }
                } else
                    icon.mouseover = false;
                this.repaint(drawRect);
            }
        }
    }

    /**
     * Adds an {@code CloseableTabbedPaneListener} to the tabbed pane.
     * 
     * @param l
     *            the {@code CloseableTabbedPaneListener} to be added
     */
    public void addCloseableTabbedPaneListener(
                                               final CloseableTabbedPaneListener l) {
        listenerList.add(CloseableTabbedPaneListener.class, l);
    }

    /**
     * Removes an {@code CloseableTabbedPaneListener} from the tabbed pane.
     * 
     * @param l
     *            the listener to be removed
     */
    public void removeCloseableTabbedPaneListener(
                                                  final CloseableTabbedPaneListener l) {
        listenerList.remove(CloseableTabbedPaneListener.class, l);
    }

    /**
     * Returns an array of all the {@code CloseableTabbedPaneListener}s added to
     * this {@code CloseableTabbedPane} with
     * {@link #addCloseableTabbedPaneListener(CloseableTabbedPaneListener)}.
     * 
     * @return all of the {@code CloseableTabbedPaneListener}s added or an empty
     *         array if no listeners have been added
     */
    public CloseableTabbedPaneListener[] getCloseableTabbedPaneListeners() {
        return listenerList.getListeners(CloseableTabbedPaneListener.class);
    }

    /**
     * Notifies all listeners that have registered interest for notification on
     * this event type.
     * 
     * @param tabIndexToClose
     *            the index of the tab which should be closed
     * @return true if the tab can be closed, false otherwise
     */
    protected boolean fireCloseTab(
                                   final int tabIndexToClose) {
        boolean closeit = true;

        // Guaranteed to return a non-null array
        for (final Object listener : listenerList.getListenerList())
            if (listener instanceof CloseableTabbedPaneListener)
                if (!((CloseableTabbedPaneListener) listener).closeTab(tabIndexToClose)) {
                    closeit = false;
                    break;
                }

        return closeit;
    }

    /**
     * The class which generates the 'X' icon for the tabs. The constructor
     * accepts an icon which is extra to the 'X' icon, so you can have tabs like
     * in JBuilder. This value is null if no extra icon is required.
     */
    class CloseTabIcon implements Icon {
        /**
         * the x position of the icon
         */
        private int x_pos;

        /**
         * the y position of the icon
         */
        private int y_pos;

        /**
         * the width the icon
         */
        private final int width;

        /**
         * the height the icon
         */
        private final int height;

        /**
         * the additional fileicon
         */
        private final Icon fileIcon;

        /**
         * true whether the mouse is over this icon, false otherwise
         */
        private boolean mouseover = false;

        /**
         * true whether the mouse is pressed on this icon, false otherwise
         */
        private boolean mousepressed = false;

        /**
         * Creates a new instance of {@code CloseTabIcon}.
         * 
         * @param fileIcon
         *            the additional fileicon, if there is one set
         */
        public CloseTabIcon(
                            final Icon fileIcon) {
            this.fileIcon = fileIcon;
            width = 16;
            height = 16;
        }

        /**
         * Draw the icon at the specified location. Icon implementations may use
         * the Component argument to get properties useful for painting, e.g.
         * the foreground or background color.
         * 
         * @param c
         *            the component which the icon belongs to
         * @param g
         *            the graphic object to draw on
         * @param x
         *            the upper left point of the icon in the x direction
         * @param y
         *            the upper left point of the icon in the y direction
         */
        public void paintIcon(
                              final Component c, final Graphics g, final int x, final int y) {
            boolean doPaintCloseIcon = true;
            try {
                // JComponent.putClientProperty("isClosable", new
                // Boolean(false));
                final JTabbedPane tabbedpane = (JTabbedPane) c;
                final int tabNumber = tabbedpane.getUI().tabForCoordinate(tabbedpane, x, y);
                final JComponent curPanel = (JComponent) tabbedpane.getComponentAt(tabNumber);
                Object prop = null;
                if ((prop = curPanel.getClientProperty("isClosable")) != null)
                    doPaintCloseIcon = ((Boolean) prop).booleanValue();
            } catch (final Exception ignored) {/*
                                                * Could probably be a
                                                * ClassCastException
                                                */
            }
            if (doPaintCloseIcon) {
                x_pos = x;
                y_pos = y;
                int y_p = y + 1;

                if (normalCloseIcon != null && !mouseover)
                    normalCloseIcon.paintIcon(c, g, x, y_p);
                else if (hooverCloseIcon != null && mouseover && !mousepressed)
                    hooverCloseIcon.paintIcon(c, g, x, y_p);
                else if (pressedCloseIcon != null && mousepressed)
                    pressedCloseIcon.paintIcon(c, g, x, y_p);
                else {
                    y_p++;

                    final Color col = g.getColor();

                    if (mousepressed && mouseover) {
                        g.setColor(Color.WHITE);
                        g.fillRect(x + 1, y_p, 12, 13);
                    }

                    g.setColor(Color.black);
                    /*
                     * g.drawLine(x+1, y_p, x+12, y_p); g.drawLine(x+1, y_p+13,
                     * x+12, y_p+13); g.drawLine(x, y_p+1, x, y_p+12);
                     * g.drawLine(x+13, y_p+1, x+13, y_p+12);
                     */
                    if (mouseover)
                        g.setColor(Color.GRAY);
                    g.drawLine(x + 4, y_p + 4, x + 9, y_p + 9);
                    g.drawLine(x + 4, y_p + 5, x + 8, y_p + 9);
                    g.drawLine(x + 5, y_p + 4, x + 9, y_p + 8);
                    g.drawLine(x + 9, y_p + 4, x + 4, y_p + 9);
                    g.drawLine(x + 9, y_p + 5, x + 5, y_p + 9);
                    g.drawLine(x + 8, y_p + 4, x + 4, y_p + 8);

                    g.setColor(col);
                    if (fileIcon != null)
                        fileIcon.paintIcon(c, g, x + width, y_p);
                }
            }
        }

        /**
         * Returns the icon's width.
         * 
         * @return an int specifying the fixed width of the icon.
         */
        public int getIconWidth() {
            return width + (fileIcon != null ? fileIcon.getIconWidth() : 0);
        }

        /**
         * Returns the icon's height.
         * 
         * @return an int specifying the fixed height of the icon.
         */
        public int getIconHeight() {
            return height;
        }

        /**
         * Gets the bounds of this icon in the form of a {@code Rectangle}
         * object. The bounds specify this icon's width, height, and location
         * relative to its parent.
         * 
         * @return a rectangle indicating this icon's bounds
         */
        public Rectangle getBounds() {
            return new Rectangle(x_pos, y_pos, width, height);
        }
    }

    /**
     * A specific {@code BasicTabbedPaneUI}.
     */
    class CloseableTabbedPaneUI extends BasicTabbedPaneUI {

        /**
         * the horizontal position of the text
         */
        private final int horizontalTextPosition;

        /**
         * Creates a new instance of {@code CloseableTabbedPaneUI}.
         */
        public CloseableTabbedPaneUI() {
            this(SwingConstants.LEFT);
        }

        /**
         * Creates a new instance of {@code CloseableTabbedPaneUI}
         * 
         * @param horizontalTextPosition
         *            the horizontal position of the text (e.g.
         *            SwingUtilities.TRAILING or SwingUtilities.LEFT)
         */
        public CloseableTabbedPaneUI(
                                     final int horizontalTextPosition) {
            this.horizontalTextPosition = horizontalTextPosition;
        }

        /**
         * Layouts the label
         * 
         * @param tabPlacement
         *            the placement of the tabs
         * @param metrics
         *            the font metrics
         * @param tabIndex
         *            the index of the tab
         * @param title
         *            the title of the tab
         * @param icon
         *            the icon of the tab
         * @param tabRect
         *            the tab boundaries
         * @param iconRect
         *            the icon boundaries
         * @param textRect
         *            the text boundaries
         * @param isSelected
         *            true whether the tab is selected, false otherwise
         */
        @Override
        protected void layoutLabel(
                                   final int tabPlacement, final FontMetrics metrics,
                                   final int tabIndex, final String title, final Icon icon,
                                   final Rectangle tabRect, final Rectangle iconRect,
                                   final Rectangle textRect, final boolean isSelected) {

            textRect.x = textRect.y = iconRect.x = iconRect.y = 0;

            final javax.swing.text.View v = getTextViewForTab(tabIndex);
            if (v != null)
                tabPane.putClientProperty("html", v);

            SwingUtilities.layoutCompoundLabel(tabPane,
                                               metrics,
                                               title,
                                               icon,
                                               SwingConstants.CENTER,
                                               SwingConstants.CENTER,
                                               SwingConstants.CENTER,
                                               horizontalTextPosition,
                                               tabRect,
                                               iconRect,
                                               textRect,
                                               textIconGap + 10);

            tabPane.putClientProperty("html", null);

            final int xNudge = getTabLabelShiftX(tabPlacement, tabIndex, isSelected);
            final int yNudge = getTabLabelShiftY(tabPlacement, tabIndex, isSelected);
            iconRect.x += xNudge;
            iconRect.y += yNudge;
            textRect.x += xNudge;
            textRect.y += yNudge;
        }
    }

    public void highlightTab(
                             final int tabIndex) {
        setBackgroundAt(tabIndex, new Color(255, 205, 205));
        setForegroundAt(tabIndex, Color.black);
    }

    @Override
    public boolean isFocusable() {
        return false;
    }

    @Override
    public final void requestFocus() {
        super.requestFocus();
        transferFocus();
    }

    @Override
    public final boolean requestFocus(
                                      final boolean temporary) {
        super.requestFocus(temporary);
        transferFocus();
        return false;
    }

    @Override
    public final boolean requestFocusInWindow() {
        super.requestFocusInWindow();
        transferFocus();
        return false;
    }

    @Override
    public final boolean requestFocusInWindow(
                                              final boolean temporary) {
        super.requestFocusInWindow(temporary);
        transferFocus();
        return false;
    }
}
