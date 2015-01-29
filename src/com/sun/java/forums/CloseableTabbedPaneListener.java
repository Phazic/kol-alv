package com.sun.java.forums;

import java.util.EventListener;

/**
 * <p>
 * For what needs is interface with Listener?
 * <p>
 * If you have something like this:
 * 
 * <pre>
 * public boolean closeTab(
 *                         int tabIndexToClose) {
 *     int ret = JOptionPane.showConfirmDialog(parentComponent,
 *                                             &quot;really close?&quot;,
 *                                             &quot;Question&quot;,
 *                                             YES_NO_OPTION,
 *                                             QUESTION_MESSAGE);
 * 
 *     return ret == JOptionPane.YES_OPTION;
 * }
 * </pre>
 * <p>
 * in a class which is added as <code>CloseableTabbedPaneListener</code> it
 * could be helpful to have a simple way to decide "to close" or "not to close"
 * a tab.
 * <p>
 * <b>Source</b>: <br>
 * <a href="http://forums.java.sun.com/thread.jspa?threadID=337070&start=22">
 * Java Forums - JTabbedPane with close icons, Post #22 </a>
 */
public interface CloseableTabbedPaneListener extends EventListener {
    public boolean closeTab(
                            int tabIndexToClose);
}
