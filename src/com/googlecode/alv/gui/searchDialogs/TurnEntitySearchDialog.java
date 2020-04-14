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

package com.googlecode.alv.gui.searchDialogs;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Collection;
import java.util.List;

import javax.swing.*;

import org.jfree.ui.RefineryUtilities;

import com.googlecode.alv.gui.searchDialogs.SearchStringMatchers.SearchStringMatcher;
import com.googlecode.alv.logData.LogDataHolder;
import com.googlecode.alv.logData.turn.TurnEntity;
import com.googlecode.alv.util.Lists;

/**
 * This is the start of an implementation of a search dialog targeted at any
 * sort of {@link TurnEntity}.
 * <p>
 * The dialog itself and the search part are already part of this class, the
 * display and handling of the search results is left to the implementing class.
 * <p>
 * Note that implementation classes should not add any kind UI elements other
 * than through the {@link #createResultsPane()} method.
 */
abstract class TurnEntitySearchDialog extends JDialog {
    private final List<TurnContainer> turnList;

    private SearchStringMatcher searchMatcher;

    private TurnEntity selectedTurn;

    /**
     * Creates the search dialog with
     * {@link SearchStringMatchers#AREA_NAME_MATCHER} as the default search
     * string matcher.
     * 
     * @param owner
     *            The frame owning this dialog.
     * @param logData
     *            The log data.
     * @param title
     *            The title of the dialog window.
     * @throws NullPointerException
     *             if logData is {@code null}
     */
    TurnEntitySearchDialog(
                           final JFrame owner, final LogDataHolder logData, final String title) {
        this(owner, logData, title, SearchStringMatchers.AREA_NAME_MATCHER);
    }

    /**
     * Creates the search dialog.
     * 
     * @param owner
     *            The frame owning this dialog.
     * @param logData
     *            The log data.
     * @param title
     *            The title of the dialog window.
     * @param defaultMatcher
     *            The search mode selected when this dialog opens.
     * @throws NullPointerException
     *             if logData is {@code null}; if defaultMatcher is {@code null}
     */
    TurnEntitySearchDialog(
                           final JFrame owner, final LogDataHolder logData, final String title,
                           final SearchStringMatcher defaultMatcher) {
        super(owner, title, true);

        if (logData == null)
            throw new NullPointerException("The log data must not be null.");
        if (defaultMatcher == null)
            throw new NullPointerException("The default search string matcher must not be null.");
        turnList = createTurnList(logData);
        searchMatcher = defaultMatcher;

        setLayout(new BorderLayout(0, 15));
        add(createSearchFieldPanel(defaultMatcher), BorderLayout.NORTH);
        add(createResultsPane(), BorderLayout.CENTER);

        setSize(800, 600);
        RefineryUtilities.centerFrameOnScreen(this);
    }

    private JPanel createSearchFieldPanel(
                                          final SearchStringMatcher defaultMatcher) {
        final JPanel searchFieldPanel = new JPanel(new GridBagLayout());
        final JComboBox searchModeChooser = new JComboBox(SearchStringMatchers.MATCHERS.toArray());
        final JTextField searchField = new JTextField();
        final JLabel modelabel = new JLabel("Search mode:");
        final JLabel searchLabel = new JLabel("Search string:");

        searchModeChooser.setSelectedItem(defaultMatcher);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(10, 20, 0, 30);
        searchFieldPanel.add(modelabel, gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 0, 0, 20);
        searchFieldPanel.add(searchModeChooser, gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(25, 20, 0, 30);
        searchFieldPanel.add(searchLabel, gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(25, 0, 0, 20);
        searchFieldPanel.add(searchField, gbc);

        searchModeChooser.addActionListener(new ActionListener() {
            public void actionPerformed(
                                        ActionEvent e) {
                if (searchModeChooser.isFocusOwner())
                    searchMatcher = (SearchStringMatcher) searchModeChooser.getSelectedItem();
            }
        });
        searchField.addKeyListener(new KeyAdapter() {
            private final char SPACE = ' ';

            @Override
            public void keyTyped(
                                 final KeyEvent e) {
                final char key = e.getKeyChar();

                // Adding a space does nothing for the search, so no action
                // necessary.
                if (key == SPACE)
                    return;

                // The last character isn't added to the text field yet, so we
                // add it manually if it is valid.
                final String searchString;
                if (key != KeyEvent.CHAR_UNDEFINED)
                    searchString = (searchField.getText() + key).trim().toLowerCase();
                else
                    searchString = searchField.getText().trim().toLowerCase();

                final SearchStringMatcher matcher = searchMatcher;

                // Populate the results display with valid turns.
                clearResults();
                final List<TurnContainer> newResults = Lists.newArrayList(50);
                for (final TurnContainer tc : turnList)
                    if (searchString.length() <= 0 || matcher.matches(tc.getTurn(), searchString))
                        newResults.add(tc);

                addResults(newResults);
            }
        });

        return searchFieldPanel;
    }

    /**
     * Returns a read-only list of all turns that are used for the search in
     * this dialog.
     * <p>
     * Only use turns part of this collection for the result display.
     * 
     * @return A read-only list of all turns enclosed in their respective
     *         {@link TurnContainer}.
     */
    protected List<TurnContainer> getTurnsList() {
        return Lists.immutableListOf(turnList);
    }

    /**
     * Sets the turn that was selected in some way before this dialog was
     * closed.
     */
    protected void setSelectedTurn(
                                   final TurnEntity selectedTurn) {
        this.selectedTurn = selectedTurn;
    }

    /**
     * Returns the turn that was selected in some way before this dialog was
     * closed. This method is the only one that can be considered safe to call
     * once the dialog is closed.
     * <p>
     * How the turn was selected is specific to the implementation class.
     * 
     * @return The selected turn.
     */
    protected TurnEntity getSelectedTurn() {
        return selectedTurn;
    }

    /**
     * This method has to return the complete UI for displaying the search
     * results.
     * 
     * @return The UI for the search results.
     */
    protected abstract JComponent createResultsPane();

    /**
     * This methods needs to create the create the complete turn list that is
     * later used for search and result display.
     * 
     * @param logData
     *            The log data.
     * @return The complete list of all turns later used for search and result
     *         display purposes.
     */
    protected abstract List<TurnContainer> createTurnList(
                                                          final LogDataHolder logData);

    /**
     * Clears the displayed search results.
     */
    protected abstract void clearResults();

    /**
     * Add the given turn to the results and display it.
     */
    protected abstract void addResult(
                                      final TurnContainer tc);

    /**
     * Add the given turns to the results and display them.
     * <p>
     * This method needs to be able to add the given list of turns without UI
     * slow-downs in the result display.
     */
    protected abstract void addResults(
                                       final Collection<TurnContainer> results);
}
