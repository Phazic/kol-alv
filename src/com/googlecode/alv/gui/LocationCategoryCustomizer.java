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

package com.googlecode.alv.gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;

import org.jfree.ui.RefineryUtilities;

import com.googlecode.alv.chart.turnrundownGantt.TurnAreaCategory;
import com.googlecode.alv.chart.turnrundownGantt.TurnrundownGantt;
import com.googlecode.alv.logData.turn.TurnInterval;
import com.googlecode.alv.util.CategoryViewFileHandler;
import com.googlecode.alv.util.Lists;

public final class LocationCategoryCustomizer extends JDialog {
    private static final FileFilter CATEGORY_VIEW_FILES = new FileFilter() {
        @Override
        public boolean accept(
                              final File f) {
            return f.isDirectory() || f.getName().toLowerCase().endsWith(".cvw");
        }

        @Override
        public String getDescription() {
            return "Category Views";
        }
    };

    private final TurnrundownGantt turnrundownChart;

    private final JFileChooser viewChooser;

    private JSplitPane splitter;

    private JButton deleteCategory;

    private JButton addLoation;

    private JButton removeArea;

    private JButton addCategory;

    private JButton updateChart;

    private JButton loadCategoryView;

    private JButton saveCategoryView;

    private JTextField categoryName;

    private JComboBox categoryList;

    private JList categoryInventory;

    private JList areas;

    private JList areasAddList;

    /**
     * @param owner
     *            The JFrame which owns this dialog.
     * @param turnrundownChart
     *            The turnrundown gantt chart on which certain actions can be
     *            performed.
     */
    public LocationCategoryCustomizer(
                                      final JFrame owner, final TurnrundownGantt turnrundownChart) {
        super(owner, true);
        this.turnrundownChart = turnrundownChart;
        viewChooser = new JFileChooser();
        viewChooser.setFileSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        viewChooser.setFileFilter(CATEGORY_VIEW_FILES);

        setLayout(new GridBagLayout());
        addGUIElements();
        addActions();
        addToolTips();

        updateData();

        pack();
        setTitle("Area categories customization");
        RefineryUtilities.centerFrameOnScreen(this);
        splitter.setDividerLocation(0.6);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setVisible(true);
    }

    private void addGUIElements() {
        splitter = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        updateChart = new JButton("Update Chart");
        loadCategoryView = new JButton("Load Category View");
        saveCategoryView = new JButton("Save Category View");
        final JPanel categoryCreaterPane = new JPanel(new GridLayout(1, 0, 20, 5));
        GridBagConstraints gbc;

        categoryCreaterPane.add(createAreaAddingPane());
        categoryCreaterPane.add(createCategoryAddingPane());

        splitter.setTopComponent(categoryCreaterPane);
        splitter.setBottomComponent(createCategoryPane());

        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 3;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(5, 5, 5, 5);
        add(splitter, gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.VERTICAL;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(25, 5, 5, 0);
        add(loadCategoryView, gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.VERTICAL;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(25, 5, 5, 5);
        add(saveCategoryView, gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.VERTICAL;
        gbc.ipadx = 150;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.insets = new Insets(25, 0, 5, 5);
        add(updateChart, gbc);
    }

    private JPanel createCategoryPane() {
        final JPanel categoryPane = new JPanel(new GridBagLayout());
        categoryInventory = new JList(new DefaultListModel());
        deleteCategory = new JButton("Delete Category");
        categoryList = new JComboBox();
        GridBagConstraints gbc;

        categoryInventory.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(5, 0, 5, 0);
        categoryPane.add(new JScrollPane(categoryInventory), gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.BOTH;
        categoryPane.add(deleteCategory, gbc);

        gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        categoryPane.add(categoryList, gbc);

        return categoryPane;
    }

    private JPanel createAreaAddingPane() {
        final JPanel areaAddingPane = new JPanel(new GridBagLayout());
        areas = new JList(new DefaultListModel());
        addLoation = new JButton("Add Area");
        GridBagConstraints gbc;

        areas.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.insets = new Insets(0, 0, 5, 0);
        areaAddingPane.add(new JScrollPane(areas), gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.BOTH;
        areaAddingPane.add(addLoation, gbc);

        return areaAddingPane;
    }

    private JPanel createCategoryAddingPane() {
        final JPanel categoryAddingPane = new JPanel(new GridBagLayout());
        areasAddList = new JList(new DefaultListModel());
        categoryName = new JTextField();
        addCategory = new JButton("Create Category");
        removeArea = new JButton("Remove Area");
        final JLabel categoryNameL = new JLabel("Category name:");
        GridBagConstraints gbc;

        areasAddList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.insets = new Insets(5, 0, 5, 0);
        categoryAddingPane.add(new JScrollPane(areasAddList), gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.VERTICAL;
        gbc.ipadx = 150;
        gbc.anchor = GridBagConstraints.EAST;
        categoryAddingPane.add(categoryName, gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 5, 0, 0);
        categoryAddingPane.add(categoryNameL, gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.fill = GridBagConstraints.VERTICAL;
        gbc.anchor = GridBagConstraints.EAST;
        categoryAddingPane.add(addCategory, gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.fill = GridBagConstraints.VERTICAL;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 0, 0, 10);
        categoryAddingPane.add(removeArea, gbc);

        return categoryAddingPane;
    }

    private void addActions() {
        categoryList.addItemListener(new ItemListener() {

            public void itemStateChanged(
                                         final ItemEvent e) {
                if (categoryList.getSelectedItem() != null) {
                    ((DefaultListModel) categoryInventory.getModel()).removeAllElements();
                    for (final String s : ((TurnAreaCategory) categoryList.getItemAt(categoryList.getSelectedIndex())).getTurnAreaNames())
                        ((DefaultListModel) categoryInventory.getModel()).addElement(s);
                }
            }
        });
        addLoation.addActionListener(new ActionListener() {

            public void actionPerformed(
                                        final ActionEvent e) {
                if (!areas.isSelectionEmpty()) {
                    ((DefaultListModel) areasAddList.getModel()).addElement(areas.getSelectedValue());
                    ((DefaultListModel) areas.getModel()).remove(areas.getSelectedIndex());
                    areas.setSelectedIndex(0);
                    areasAddList.setSelectedIndex(0);
                }
            }
        });
        areas.addMouseListener(new MouseListener() {

            public void mouseClicked(
                                     final MouseEvent e) {
                if (e.getClickCount() >= 2)
                    if (!areas.isSelectionEmpty()) {
                        ((DefaultListModel) areasAddList.getModel()).addElement(areas.getSelectedValue());
                        ((DefaultListModel) areas.getModel()).remove(areas.getSelectedIndex());
                        areas.setSelectedIndex(0);
                        areasAddList.setSelectedIndex(0);
                    }
            }

            public void mouseEntered(
                                     final MouseEvent e) {}

            public void mouseExited(
                                    final MouseEvent e) {}

            public void mousePressed(
                                     final MouseEvent e) {}

            public void mouseReleased(
                                      final MouseEvent e) {}
        });
        removeArea.addActionListener(new ActionListener() {

            public void actionPerformed(
                                        final ActionEvent e) {
                if (!areasAddList.isSelectionEmpty()) {
                    addLocation((String) areasAddList.getSelectedValue());
                    ((DefaultListModel) areasAddList.getModel()).remove(areasAddList.getSelectedIndex());
                    areas.setSelectedIndex(0);
                    areasAddList.setSelectedIndex(0);
                }
            }
        });
        areasAddList.addMouseListener(new MouseListener() {

            public void mouseClicked(
                                     final MouseEvent e) {
                if (e.getClickCount() >= 2)
                    if (!areasAddList.isSelectionEmpty()) {
                        addLocation((String) areasAddList.getSelectedValue());
                        ((DefaultListModel) areasAddList.getModel()).remove(areasAddList.getSelectedIndex());
                        areas.setSelectedIndex(0);
                        areasAddList.setSelectedIndex(0);
                    }
            }

            public void mouseEntered(
                                     final MouseEvent e) {}

            public void mouseExited(
                                    final MouseEvent e) {}

            public void mousePressed(
                                     final MouseEvent e) {}

            public void mouseReleased(
                                      final MouseEvent e) {}
        });
        addCategory.addActionListener(new ActionListener() {

            public void actionPerformed(
                                        final ActionEvent e) {
                addCategory();
            }
        });
        categoryName.addKeyListener(new KeyListener() {

            public void keyPressed(
                                   final KeyEvent e) {
                if (e.getKeyChar() == '\n')
                    addCategory();
            }

            public void keyReleased(
                                    final KeyEvent e) {}

            public void keyTyped(
                                 final KeyEvent e) {}
        });
        deleteCategory.addActionListener(new ActionListener() {

            public void actionPerformed(
                                        final ActionEvent e) {
                if (categoryList.getSelectedItem() != null) {
                    turnrundownChart.getCategories().remove(categoryList.getSelectedIndex());
                    updateData();
                }
            }
        });
        updateChart.addActionListener(new ActionListener() {

            public void actionPerformed(
                                        final ActionEvent e) {
                dispose();
            }
        });
        loadCategoryView.addActionListener(new ActionListener() {

            public void actionPerformed(
                                        final ActionEvent e) {
                final int state = viewChooser.showOpenDialog(null);
                if (state == JFileChooser.APPROVE_OPTION)
                    try {
                        turnrundownChart.setCategories(CategoryViewFileHandler.parseOutCategories(viewChooser.getSelectedFile()));
                        updateData();
                    } catch (final IOException e1) {
                        e1.printStackTrace();
                    }
            }
        });
        saveCategoryView.addActionListener(new ActionListener() {

            public void actionPerformed(
                                        final ActionEvent e) {
                final int state = viewChooser.showSaveDialog(null);
                if (state == JFileChooser.APPROVE_OPTION)
                    try {
                        CategoryViewFileHandler.createCategoryViewFile(turnrundownChart.getCategories(),
                                                                       viewChooser.getSelectedFile());
                    } catch (final IOException e1) {
                        e1.printStackTrace();
                    }
            }
        });
    }

    private void addToolTips() {
        deleteCategory.setToolTipText("Delete the currently selected category");
        addLoation.setToolTipText("Add an area to the current category in construction");
        removeArea.setToolTipText("Remove an area from the current category in construction");
        addCategory.setToolTipText("<html>Saves the category in the category list. Note that you"
                                   + "<p>need to name a category before you can save it.</p></html>");
        updateChart.setToolTipText("<html>Closes this window and updates the turn rundown gantt"
                                   + "<p>chart with the new category configuration.</p></html>");
        loadCategoryView.setToolTipText("Load a previously saved category configuration");
        saveCategoryView.setToolTipText("Save the current category configuration to a file for future use");
        categoryName.setToolTipText("<html>Name of the current category in construction. Note that you"
                                    + "<p>need to name a category before you can save it.</p></html>");
        categoryList.setToolTipText("List of categories currently used");
        categoryInventory.setToolTipText("List of all areas inside the currently selected category");
        areas.setToolTipText("<html>All uncategorized areas. Every location in here will"
                             + "<p>be its own category in the turn rundown gantt chart.</p></html>");
        areasAddList.setToolTipText("List of all areas inside the category currently in construction");
    }

    private void updateData() {
        ((DefaultListModel) areas.getModel()).removeAllElements();
        ((DefaultListModel) areasAddList.getModel()).removeAllElements();
        ((DefaultListModel) categoryInventory.getModel()).removeAllElements();
        categoryName.setText("");

        updateCategoryList();

        final List<String> areaNames = Lists.newArrayList(200);
        for (final TurnInterval ti : turnrundownChart.getLogData().getTurnIntervalsSpent()) {
            final boolean isInLocationList = areaNames.contains(ti.getAreaName());
            if (!isInLocationList && !isInCategories(ti.getAreaName()))
                areaNames.add(ti.getAreaName());
        }
        Collections.sort(areaNames);
        for (final String s : areaNames)
            ((DefaultListModel) areas.getModel()).addElement(s);
        areas.setSelectedIndex(0);
    }

    private void updateCategoryList() {
        categoryList.removeAllItems();
        for (final TurnAreaCategory tlc : turnrundownChart.getCategories())
            categoryList.addItem(tlc);
    }

    private boolean isInCategories(
                                   final String area) {
        for (int i = 0; i < categoryList.getItemCount(); i++)
            for (final String s : ((TurnAreaCategory) categoryList.getItemAt(i)).getTurnAreaNames())
                if (area.startsWith(s))
                    return true;

        return false;
    }

    private void addLocation(
                             final String areaName) {
        final List<String> areaNames = Lists.newArrayList(200);

        for (int i = 0; i < ((DefaultListModel) areas.getModel()).getSize(); i++)
            areaNames.add((String) ((DefaultListModel) areas.getModel()).get(i));

        areaNames.add(areaName);
        Collections.sort(areaNames);

        ((DefaultListModel) areas.getModel()).removeAllElements();
        for (final String s : areaNames)
            ((DefaultListModel) areas.getModel()).addElement(s);
    }

    private void addCategory() {
        if (!areasAddList.isSelectionEmpty() && !categoryName.getText().equals("")) {
            final TurnAreaCategory tac = new TurnAreaCategory(categoryName.getText());
            for (int i = 0; i < ((DefaultListModel) areasAddList.getModel()).getSize(); i++)
                tac.addTurnAreaName((String) ((DefaultListModel) areasAddList.getModel()).get(i));

            turnrundownChart.addCategory(tac);

            updateData();
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        turnrundownChart.updateChart();
    }
}
