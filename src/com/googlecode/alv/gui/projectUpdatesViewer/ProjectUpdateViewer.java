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

package com.googlecode.alv.gui.projectUpdatesViewer;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.List;
import java.util.regex.Pattern;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableColumn;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.jfree.ui.RefineryUtilities;
import org.xml.sax.SAXException;

/**
 * This class opens a frame with a table of updates that happened since this
 * specific version of the ALV was uploaded (basically the UI is akin to feed
 * reader light).
 */
public final class ProjectUpdateViewer extends JFrame {
    private static final Pattern ALV_VERSION_FILE_NAME = Pattern.compile("AscensionLogVisualizer \\d+.\\d+.\\d+.jar.*");

    /**
     * Static instancing method to show the project update viewer frame.
     * <p>
     * If there were problems with the connection or the feed parsing, an error
     * dialog will be shown instead.
     */
    public static void showProjectUpdateViewer() {
        try {
            final List<ProjectUpdateContainer> updates = ProjectFeedReader.readUpdatesFeed("http://code.google.com/feeds/p/ascension-log-visualizer/updates/basic");
            new ProjectUpdateViewer(updates);
        } catch (final UnknownHostException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                                          "Could not connect to the server.",
                                          "Connection Error",
                                          JOptionPane.ERROR_MESSAGE);
        } catch (final IOException e) {
            e.printStackTrace();
            showStandardErrorMessage();
        } catch (final XPathExpressionException e) {
            e.printStackTrace();
            showStandardErrorMessage();
        } catch (final ParserConfigurationException e) {
            e.printStackTrace();
            showStandardErrorMessage();
        } catch (final SAXException e) {
            e.printStackTrace();
            showStandardErrorMessage();
        }
    }

    /**
     * Checks the project website for a newer version of the ALV. In case the
     * server couldn't be reached or the data couldn't be parsed, this method
     * will return false.
     * 
     * @return True if there is a version newer than the current one uploaded to
     *         the project website, false otherwise.
     */
    public static boolean isNewerVersionUploaded() {
        try {
            for (final ProjectUpdateContainer puc : ProjectFeedReader.readUpdatesFeed("http://code.google.com/feeds/p/ascension-log-visualizer/downloads/basic"))
                if (ALV_VERSION_FILE_NAME.matcher(puc.getTitle()).matches()
                    && !puc.getTitle().startsWith(ProjectFeedReader.CURRENT_ALV_VERSION_FILE_NAME))
                    return true;
        } catch (final Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    private static void showStandardErrorMessage() {
        JOptionPane.showMessageDialog(null,
                                      "An error occurred while checking for project news updates.",
                                      "Error Occurred",
                                      JOptionPane.ERROR_MESSAGE);
    }

    private final JTextArea contentArea;

    private ProjectUpdateViewer(
                                final List<ProjectUpdateContainer> updates) {
        super("Project Updates since the release of this version");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        final JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        contentArea = new JTextArea();
        contentArea.setLineWrap(true);
        contentArea.setWrapStyleWord(true);
        contentArea.setEditable(false);

        splitPane.setTopComponent(new JScrollPane(createUpdatesTable(updates)));
        splitPane.setBottomComponent(new JScrollPane(contentArea));
        splitPane.setDividerLocation(250);

        setContentPane(splitPane);
        setSize(800, 600);
        RefineryUtilities.centerFrameOnScreen(this);
        setVisible(true);
    }

    private JTable createUpdatesTable(
                                      final List<ProjectUpdateContainer> updates) {
        final ProjectFeedUpdateTableModel updatesTableData = new ProjectFeedUpdateTableModel(updates);
        final JTable projectUpdatesTable = new JTable(updatesTableData);

        projectUpdatesTable.setShowGrid(false);
        final TableColumn dateCol = projectUpdatesTable.getColumnModel().getColumn(1);
        dateCol.setMinWidth(150);
        dateCol.setMaxWidth(150);
        dateCol.setPreferredWidth(150);
        projectUpdatesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        projectUpdatesTable.getSelectionModel()
                           .addListSelectionListener(new ListSelectionListener() {
                               public void valueChanged(
                                                        final ListSelectionEvent arg0) {
                                   contentArea.setText(updates.get(projectUpdatesTable.getSelectedRow())
                                                              .getContent());
                                   contentArea.setCaretPosition(0);
                               }
                           });

        return projectUpdatesTable;
    }
}
