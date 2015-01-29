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

package com.googlecode.logVisualizer.gui.dataTablesEditor;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.jfree.ui.RefineryUtilities;

import com.googlecode.logVisualizer.util.Lists;
import com.googlecode.logVisualizer.util.Maps;
import com.googlecode.logVisualizer.util.Pair;
import com.googlecode.logVisualizer.util.Sets;
import com.googlecode.logVisualizer.util.dataTables.DataTablesHandler;
import com.googlecode.logVisualizer.util.dataTables.ExtraStats;
import com.googlecode.logVisualizer.util.dataTables.Outfit;

/**
 * This class is an editor that allows the user to edit the data tables used by
 * the program, such itemdrops, MP regen equipment, skill MP cost, etc.
 */
public final class DataTablesEditor extends JFrame {

    /**
     * Displays a frame containing the data tables editor. The data shown is
     * based on the currently used internal data tables.
     */
    public static void showDataTablesEditor() {
        final DataTablesHandler data = DataTablesHandler.HANDLER;

        new DataTablesEditor(data.getBadmoonAdventuresSet(),
                             data.getSemirareAdventuresSet(),
                             data.getWanderingAdventuresSet(),
                             data.getItemdropsMap(),
                             data.getSkillCostsMap(),
                             data.getMPFromEquipmentMap(),
                             data.getMPCostOffsetEquipmentMap(),
                             data.getStatsEquipmentMap(),
                             data.getOutfitsMap());
    }

    private static String[] DATA_TABLE_LABELS = { "Bad Moon Adventures", "Semi-rare Adventures",
                                                 "Wandering Adventures", "Itemdrops", "Skills",
                                                 "MP Cost Equipment", "MP Regen Equipment",
                                                 "Stats Equipment", "Outfits" };

    private final DataTableContainer badmoonAdventures;

    private final DataTableContainer semirareAdventures;

    private final DataTableContainer wanderingAdventures;

    private final DataTableContainer itemdrops;

    private final DataTableContainer skills;

    private final DataTableContainer mpRegenEquipments;

    private final DataTableContainer mpCostEquipments;

    private final DataTableContainer statsEquipments;

    private final DataTableContainer outfits;

    private final Map<String, DataTableContainer> dataTables;

    private final JComboBox chooser;

    private final JList dataList;

    @SuppressWarnings("unchecked")
    private DataTablesEditor(
                             final Set<String> badmoonAdventuresSet,
                             final Set<String> semirareAdventuresSet,
                             final Set<String> wanderingAdventuresSet,
                             final Map<String, Boolean> itemdropsMap,
                             final Map<String, Integer> skillsMap,
                             final Map<String, Integer> mpRegenEquipmentsMap,
                             final Map<String, Integer> mpCostEquipmentsMap,
                             final Map<String, ExtraStats> statsEquipmentsMap,
                             final Map<String, Outfit> outfitsMap) {
        super("Data Tables Editor");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(20, 25));

        final List<DataPoint> statsEquipmentList = Lists.newArrayList(statsEquipmentsMap.size());
        for (final Entry<String, ExtraStats> e : statsEquipmentsMap.entrySet()) {
            final ExtraStats es = e.getValue();
            final Map<String, Number> statMapping = Maps.newHashMap(10);
            statMapping.put("general main substat gain", es.generalGain);
            statMapping.put("muscle gain", es.musGain);
            statMapping.put("mysticality gain", es.mystGain);
            statMapping.put("moxi gain", es.moxGain);

            statsEquipmentList.add(new DataPoint(e.getKey(), statMapping));
        }

        final List<DataPoint> outfitsList = Lists.newArrayList(outfitsMap.size());
        for (final Entry<String, Outfit> e : outfitsMap.entrySet()) {
            final Outfit o = e.getValue();
            final Map<String, Boolean> outfitMapping = Maps.newHashMap(10);
            outfitMapping.put("hat", o.hat);
            outfitMapping.put("weapon", o.weapon);
            outfitMapping.put("offhand", o.offhand);
            outfitMapping.put("shirt", o.shirt);
            outfitMapping.put("pants", o.pants);
            outfitMapping.put("acc1", o.acc1);
            outfitMapping.put("acc2", o.acc2);
            outfitMapping.put("acc3", o.acc3);

            outfitsList.add(new DataPoint(e.getKey(), outfitMapping));
        }

        badmoonAdventures = new DataTableContainer(badmoonAdventuresSet);
        semirareAdventures = new DataTableContainer(semirareAdventuresSet);
        wanderingAdventures = new DataTableContainer(wanderingAdventuresSet);
        itemdrops = new DataTableContainer(dataPointsFromMap(itemdropsMap, "onetime only"));
        skills = new DataTableContainer(dataPointsFromMap(skillsMap, "MP cost"));
        mpRegenEquipments = new DataTableContainer(dataPointsFromMap(mpRegenEquipmentsMap,
                                                                     "MP regeneration"));
        mpCostEquipments = new DataTableContainer(dataPointsFromMap(mpCostEquipmentsMap, "MP cost"));
        statsEquipments = new DataTableContainer(statsEquipmentList);
        outfits = new DataTableContainer(outfitsList);

        dataTables = Maps.immutableMapOf(Pair.of(DATA_TABLE_LABELS[0], badmoonAdventures),
                                         Pair.of(DATA_TABLE_LABELS[1], semirareAdventures),
                                         Pair.of(DATA_TABLE_LABELS[2], wanderingAdventures),
                                         Pair.of(DATA_TABLE_LABELS[3], itemdrops),
                                         Pair.of(DATA_TABLE_LABELS[4], skills),
                                         Pair.of(DATA_TABLE_LABELS[5], mpCostEquipments),
                                         Pair.of(DATA_TABLE_LABELS[6], mpRegenEquipments),
                                         Pair.of(DATA_TABLE_LABELS[7], statsEquipments),
                                         Pair.of(DATA_TABLE_LABELS[8], outfits));

        chooser = new JComboBox(DATA_TABLE_LABELS);
        dataList = new JList(new DefaultListModel());
        createGeneralButtons();
        chooser.setSelectedIndex(0);

        setSize(800, 600);
        RefineryUtilities.centerFrameOnScreen(this);
        setVisible(true);
    }

    private void createGeneralButtons() {
        chooser.addActionListener(new ActionListener() {
            public void actionPerformed(
                                        final ActionEvent e) {
                loadTable(dataTables.get(chooser.getSelectedItem()));
            }
        });
        add(chooser, BorderLayout.NORTH);

        dataList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        add(new JScrollPane(dataList), BorderLayout.CENTER);

        final JPanel rightPanel = new JPanel(new GridLayout(0, 1, 0, 30));
        final JButton addEntryButton = new JButton("Add Entry");
        final JButton editEntryButton = new JButton("Edit Entry");
        final JButton removeEntryButton = new JButton("Remove Entry");

        addEntryButton.addActionListener(new ActionListener() {
            public void actionPerformed(
                                        final ActionEvent e) {
                final DataTableContainer currentTable = dataTables.get(chooser.getSelectedItem());
                final DataPoint newDataPoint = currentTable.getDataPointTemplate();
                final DefaultListModel listModel = (DefaultListModel) dataList.getModel();

                final int state = JOptionPane.showConfirmDialog(DataTablesEditor.this,
                                                                createDataPointEditorPanel(newDataPoint),
                                                                "Edit Entry",
                                                                JOptionPane.OK_CANCEL_OPTION,
                                                                JOptionPane.PLAIN_MESSAGE);
                if (state == JOptionPane.OK_OPTION) {
                    final int index = listModel.size() > 0 ? dataList.getSelectedIndex() : 0;

                    currentTable.addDataPoint(newDataPoint);
                    loadTable(currentTable);
                    dataList.setSelectedIndex(index);
                }
            }
        });
        editEntryButton.addActionListener(new ActionListener() {
            public void actionPerformed(
                                        final ActionEvent e) {
                if (!dataList.isSelectionEmpty()) {
                    final DataPoint dp = (DataPoint) ((DefaultListModel) dataList.getModel()).get(dataList.getSelectedIndex());
                    final DataPoint clone = new DataPoint(dp);

                    final int state = JOptionPane.showConfirmDialog(DataTablesEditor.this,
                                                                    createDataPointEditorPanel(dp),
                                                                    "Edit Entry",
                                                                    JOptionPane.OK_CANCEL_OPTION,
                                                                    JOptionPane.PLAIN_MESSAGE);
                    if (state == JOptionPane.OK_OPTION) {
                        final int index = dataList.getSelectedIndex();
                        final DataTableContainer currentTable = dataTables.get(chooser.getSelectedItem());

                        currentTable.removeDataPoint(clone);
                        currentTable.addDataPoint(dp);
                        loadTable(currentTable);

                        dataList.setSelectedIndex(index);
                    } else
                        for (final Entry<String, Object> entry : clone.getNameValuePairs())
                            dp.setValueOf(entry.getKey(), entry.getValue());
                }
            }
        });
        removeEntryButton.addActionListener(new ActionListener() {
            public void actionPerformed(
                                        final ActionEvent e) {
                if (!dataList.isSelectionEmpty()) {
                    final int index = dataList.getSelectedIndex();
                    final DefaultListModel listModel = (DefaultListModel) dataList.getModel();
                    final DataPoint dp = (DataPoint) listModel.get(index);
                    final DataTableContainer currentTable = dataTables.get(chooser.getSelectedItem());

                    listModel.remove(index);
                    currentTable.removeDataPoint(dp);

                    if (listModel.size() > index)
                        dataList.setSelectedIndex(index);
                    else if (listModel.size() > 0)
                        dataList.setSelectedIndex(index - 1);
                }
            }
        });

        rightPanel.add(addEntryButton);
        rightPanel.add(editEntryButton);
        rightPanel.add(removeEntryButton);
        add(rightPanel, BorderLayout.EAST);

        final JPanel bottomPanel = new JPanel(new GridLayout(1, 0, 150, 0));
        final JButton okButton = new JButton("OK");
        final JButton cancelButton = new JButton("Cancel");

        okButton.setPreferredSize(new Dimension(0, 50));
        okButton.addActionListener(new ActionListener() {
            public void actionPerformed(
                                        final ActionEvent e) {
                updateDataTables();
                dispose();
            }
        });
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(
                                        final ActionEvent e) {
                dispose();
            }
        });

        bottomPanel.add(okButton);
        bottomPanel.add(cancelButton);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    /**
     * This method generates an editor to change the contents of the given
     * {@link DataPoint}. It is automatically build based upon what data the
     * {@link DataPoint} holds.
     */
    private JPanel createDataPointEditorPanel(
                                              final DataPoint dataPoint) {
        final JPanel dataPanel = new JPanel(new GridBagLayout());
        final JTextField nameField = new JTextField(dataPoint.getName());
        nameField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(
                                 final KeyEvent e) {
                final char key = e.getKeyChar();
                final String name;
                if (key != KeyEvent.CHAR_UNDEFINED)
                    name = (nameField.getText() + key).trim();
                else
                    name = nameField.getText().trim();

                dataPoint.setValueOf("name", name);
            }
        });
        nameField.addActionListener(new ActionListener() {
            public void actionPerformed(
                                        final ActionEvent e) {
                dataPoint.setValueOf("name", nameField.getText());
            }
        });

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 0, 15, 40);
        gbc.anchor = GridBagConstraints.WEST;
        dataPanel.add(new JLabel("Name:"), gbc);

        gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.gridx = 2;
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 0, 15, 0);
        gbc.anchor = GridBagConstraints.EAST;
        dataPanel.add(nameField, gbc);

        int y = 2;
        for (final Entry<String, Object> e : dataPoint.getNameValuePairs()) {
            final String valueName = e.getKey();
            final JLabel label = new JLabel(valueName + ":");
            JComponent component = null;

            if (e.getValue().getClass() == Boolean.class) {
                final JCheckBox box = new JCheckBox(null, null, (Boolean) e.getValue());
                box.addActionListener(new ActionListener() {
                    public void actionPerformed(
                                                final ActionEvent e) {
                        if (box.isFocusOwner())
                            dataPoint.setValueOf(valueName, box.isSelected());
                    }
                });

                component = box;
            } else if (e.getValue().getClass() == Integer.class
                       || e.getValue().getClass() == Double.class) {
                final JSpinner spinner = new JSpinner(new SpinnerNumberModel((Number) e.getValue(),
                                                                             null,
                                                                             null,
                                                                             1));
                spinner.addChangeListener(new ChangeListener() {
                    public void stateChanged(
                                             final ChangeEvent e) {
                        dataPoint.setValueOf(valueName, spinner.getValue());
                    }
                });

                component = spinner;
            } else if (e.getValue().getClass() == String.class && !e.getKey().equals("name")) {
                final JTextField field = new JTextField((String) e.getValue());
                field.addKeyListener(new KeyAdapter() {
                    @Override
                    public void keyTyped(
                                         final KeyEvent e) {
                        final char key = e.getKeyChar();
                        final String value;
                        if (key != KeyEvent.CHAR_UNDEFINED)
                            value = (field.getText() + key).trim();
                        else
                            value = field.getText().trim();

                        dataPoint.setValueOf(valueName, value);
                    }
                });

                component = field;
            }

            if (component != null) {
                gbc = new GridBagConstraints();
                gbc.gridx = 1;
                gbc.gridy = y;
                gbc.insets = new Insets(0, 0, 15, 40);
                gbc.anchor = GridBagConstraints.WEST;
                dataPanel.add(label, gbc);

                gbc = new GridBagConstraints();
                gbc.fill = GridBagConstraints.HORIZONTAL;
                gbc.weightx = 1.0;
                gbc.gridx = 2;
                gbc.gridy = y++;
                gbc.insets = new Insets(0, 0, 15, 0);
                gbc.anchor = GridBagConstraints.EAST;
                dataPanel.add(component, gbc);
            }
        }

        final JPanel outerPanel = new JPanel(new GridBagLayout());
        outerPanel.setPreferredSize(new Dimension(400, y * 35));
        gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.insets = new Insets(20, 20, 0, 20);
        gbc.anchor = GridBagConstraints.CENTER;
        outerPanel.add(dataPanel, gbc);

        return outerPanel;
    }

    /**
     * Loads the given data table into the display list.
     */
    private void loadTable(
                           final DataTableContainer table) {
        final DefaultListModel listModel = (DefaultListModel) dataList.getModel();

        listModel.clear();
        for (final DataPoint dp : table.getDataTable())
            listModel.addElement(dp);

        if (listModel.size() > 0)
            dataList.setSelectedIndex(0);
    }

    /**
     * Updates all of the program's internal data tables with the data as it is
     * currently held in this {@link DataTablesEditor} instance.
     */
    private void updateDataTables() {
        final Set<String> badmoonAdventuresSet = Sets.newHashSet(150);
        final Set<String> semirareAdventuresSet = Sets.newHashSet(150);
        final Set<String> wanderingAdventuresSet = Sets.newHashSet(150);
        for (final DataPoint dp : badmoonAdventures.getDataTable())
            badmoonAdventuresSet.add(dp.getName());
        for (final DataPoint dp : semirareAdventures.getDataTable())
            semirareAdventuresSet.add(dp.getName());
        for (final DataPoint dp : wanderingAdventures.getDataTable())
            wanderingAdventuresSet.add(dp.getName());

        final Map<String, Boolean> itemdropsMap = dataPointsToMap(itemdrops,
                                                                  "onetime only",
                                                                  Boolean.class);
        final Map<String, Integer> skillsMap = dataPointsToMap(skills, "MP cost", Integer.class);
        final Map<String, Integer> mpRegenEquipmentsMap = dataPointsToMap(mpRegenEquipments,
                                                                          "MP regeneration",
                                                                          Integer.class);
        final Map<String, Integer> mpCostEquipmentsMap = dataPointsToMap(mpCostEquipments,
                                                                         "MP cost",
                                                                         Integer.class);

        final Map<String, ExtraStats> statsEquipmentsMap = Maps.newHashMap(300);
        for (final DataPoint dp : statsEquipments.getDataTable()) {
            String name = null;
            ExtraStats stats = ExtraStats.NO_STATS;
            for (final Entry<String, Object> e : dp.getNameValuePairs())
                if (e.getKey().equals("name"))
                    name = (String) e.getValue();
                else if (e.getKey().equals("general main substat gain"))
                    stats = new ExtraStats((Double) e.getValue(),
                                           stats.musGain,
                                           stats.mystGain,
                                           stats.moxGain);
                else if (e.getKey().equals("muscle gain"))
                    stats = new ExtraStats(stats.generalGain,
                                           (Integer) e.getValue(),
                                           stats.mystGain,
                                           stats.moxGain);
                else if (e.getKey().equals("mysticality gain"))
                    stats = new ExtraStats(stats.generalGain,
                                           stats.musGain,
                                           (Integer) e.getValue(),
                                           stats.moxGain);
                else if (e.getKey().equals("moxie gain"))
                    stats = new ExtraStats(stats.generalGain,
                                           stats.musGain,
                                           stats.mystGain,
                                           (Integer) e.getValue());

            if (name != null)
                statsEquipmentsMap.put(name, stats);
        }

        final Map<String, Outfit> outfitsMap = Maps.newHashMap(300);
        for (final DataPoint dp : outfits.getDataTable()) {
            String name = null;
            Boolean hat = null;
            Boolean weapon = null;
            Boolean offhand = null;
            Boolean shirt = null;
            Boolean pants = null;
            Boolean acc1 = null;
            Boolean acc2 = null;
            Boolean acc3 = null;
            for (final Entry<String, Object> e : dp.getNameValuePairs())
                if (e.getKey().equals("name"))
                    name = (String) e.getValue();
                else if (e.getKey().equals("hat"))
                    hat = (Boolean) e.getValue();
                else if (e.getKey().equals("weapon"))
                    weapon = (Boolean) e.getValue();
                else if (e.getKey().equals("offhand"))
                    offhand = (Boolean) e.getValue();
                else if (e.getKey().equals("shirt"))
                    shirt = (Boolean) e.getValue();
                else if (e.getKey().equals("pants"))
                    pants = (Boolean) e.getValue();
                else if (e.getKey().equals("acc1"))
                    acc1 = (Boolean) e.getValue();
                else if (e.getKey().equals("acc2"))
                    acc2 = (Boolean) e.getValue();
                else if (e.getKey().equals("acc3"))
                    acc3 = (Boolean) e.getValue();

            if (name != null && hat != null && weapon != null && offhand != null && shirt != null
                && pants != null && acc1 != null && acc2 != null && acc3 != null)
                outfitsMap.put(name, new Outfit(name,
                                                hat,
                                                weapon,
                                                offhand,
                                                shirt,
                                                pants,
                                                acc1,
                                                acc2,
                                                acc3));
        }

        DataTablesHandler.HANDLER.updateDataTables(badmoonAdventuresSet,
                                                   semirareAdventuresSet,
                                                   wanderingAdventuresSet,
                                                   itemdropsMap,
                                                   skillsMap,
                                                   mpRegenEquipmentsMap,
                                                   mpCostEquipmentsMap,
                                                   statsEquipmentsMap,
                                                   outfitsMap);
    }

    /**
     * Generates a list of {@link DataPoint}s with two value-pairs from the
     * given map.
     * <p>
     * The name value uses the key of the map entry and the other value-pair
     * uses the value of the map entry with valueName as its value name.
     */
    private static <T> List<DataPoint> dataPointsFromMap(
                                                         final Map<String, T> dataMap,
                                                         final String valueName) {
        final List<DataPoint> dataList = Lists.newArrayList(dataMap.size());
        for (final Entry<String, T> e : dataMap.entrySet())
            dataList.add(new DataPoint(e.getKey(), Pair.of(valueName, e.getValue())));

        return dataList;
    }

    /**
     * Generates a map with the names as keys and another value as the map entry
     * values from a data table that consists of {@link DataPoint}s with two
     * value-pairs.
     * 
     * @param data
     *            The data table consisting of {@link DataPoint}s with two
     *            value-pairs.
     * @param valueName
     *            The value name of the second value-pair of the data points
     *            (the first is always the name value-pair).
     * @param clazz
     *            The type of the value of the second value-pair.
     */
    @SuppressWarnings("unchecked")
    private static <T> Map<String, T> dataPointsToMap(
                                                      final DataTableContainer data,
                                                      final String valueName, final Class<T> clazz) {
        final Map<String, T> map = Maps.newHashMap(300);
        for (final DataPoint dp : data.getDataTable()) {
            String name = null;
            T value = null;
            for (final Entry<String, Object> e : dp.getNameValuePairs())
                if (e.getKey().equals("name"))
                    name = (String) e.getValue();
                else if (e.getKey().equals(valueName))
                    value = (T) e.getValue();

            if (name != null && value != null)
                map.put(name, value);
        }

        return map;
    }
}
