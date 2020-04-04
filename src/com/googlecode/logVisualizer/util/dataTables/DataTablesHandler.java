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

package com.googlecode.logVisualizer.util.dataTables;

import static com.googlecode.logVisualizer.parser.UsefulPatterns.NON_ASCII;
import static net.java.dev.spellcast.utilities.UtilityConstants.KOL_DATA_DIRECTORY;
import static net.java.dev.spellcast.utilities.UtilityConstants.ROOT_DIRECTORY;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import net.java.dev.spellcast.utilities.DataUtilities;
import net.java.dev.spellcast.utilities.UtilityConstants;

import com.googlecode.logVisualizer.logData.turn.Encounter;
import com.googlecode.logVisualizer.logData.turn.turnAction.EquipmentChange;
import com.googlecode.logVisualizer.parser.UsefulPatterns;
import com.googlecode.logVisualizer.util.Lists;
import com.googlecode.logVisualizer.util.Maps;
import com.googlecode.logVisualizer.util.Pair;
import com.googlecode.logVisualizer.util.Sets;
import com.googlecode.logVisualizer.util.dataTables.XMLDataFilesWriter.DataWriter;
import com.googlecode.logVisualizer.util.xmlLogs.XMLAccessException;

/**
 * This class is a centralised place to handle access to various useful data
 * tables.
 */
public enum DataTablesHandler {
    HANDLER;

    private static final String FLOWERS_FOR_BAD_MOON_ADVENUTRE = "flowers for ";

    private final Map<String, Integer> fullnessHitMap;

    private final Map<String, Integer> drunkennessHitMap;

    private final Map<String, Integer> spleenHitMap;

    private Set<String> badmoonAdventuresSet;

    private Set<String> semirareAdventuresSet;

    private Set<String> wanderingAdventuresSet;

    private Map<String, Boolean> itemdropsMap;

    private Map<String, Integer> skillsMap;

    private Map<String, Integer> mpRegenEquipmentsMap;

    private Map<String, Integer> mpCostEquipmentsMap;

    private Map<String, ExtraStats> statsEquipmentsMap;

    private Map<String, Outfit> outfitsMap;

    private DataTablesHandler() {
        fullnessHitMap = Maps.newHashMap(500);
        drunkennessHitMap = Maps.newHashMap(500);
        spleenHitMap = Maps.newHashMap(300);

        final Pattern tableDataExtractionPattern = Pattern.compile("([.[^\t]]+)\\s+(\\d+)\\s+.+");
        readFormattedTable(DataUtilities.getReader(UtilityConstants.KOL_DATA_DIRECTORY,
                                                   "fullness.txt"),
                           fullnessHitMap,
                           tableDataExtractionPattern);
        readFormattedTable(DataUtilities.getReader(UtilityConstants.KOL_DATA_DIRECTORY,
                                                   "inebriety.txt"),
                           drunkennessHitMap,
                           tableDataExtractionPattern);
        readFormattedTable(DataUtilities.getReader(UtilityConstants.KOL_DATA_DIRECTORY,
                                                   "spleenhit.txt"),
                           spleenHitMap,
                           tableDataExtractionPattern);

        reloadDataTables();
    }

    /**
     * Reloads the collections from the data tables.
     * <p>
     * Please note that current collections will only be dereferenced, not
     * emptied and repopulated. This means that references to the old
     * collections will continue to link to the old data.
     */
    public synchronized void reloadDataTables() {
        final Set<String> badmoonAdventuresSet = Sets.newHashSet(150);
        final Set<String> semirareAdventuresSet = Sets.newHashSet(150);
        final Set<String> wanderingAdventuresSet = Sets.newHashSet(150);
        final Map<String, Boolean> itemdropsMap = Maps.newHashMap(500);
        final Map<String, Integer> skillsMap = Maps.newHashMap(300);
        final Map<String, Integer> mpRegenEquipmentsMap = Maps.newHashMap(300);
        final Map<String, Integer> mpCostEquipmentsMap = Maps.newHashMap(300);
        final Map<String, ExtraStats> statsEquipmentsMap = Maps.newHashMap(300);
        final Map<String, Outfit> outfitsMap = Maps.newHashMap(300);

        final ArgumetsHandler badmoonArgumentsHandler = new ArgumetsHandler() {
            public void parseArguments(
                                       List<Pair<String, String>> arguments) {
                for (final Pair<String, String> arg : arguments)
                    if (arg.getVar1().equals("name")) {
                        badmoonAdventuresSet.add(arg.getVar2());
                        badmoonAdventuresSet.add(NON_ASCII.matcher(arg.getVar2())
                                                          .replaceAll(UsefulPatterns.EMPTY_STRING));
                    }
            }
        };

        final ArgumetsHandler semirareArgumentsHandler = new ArgumetsHandler() {
            public void parseArguments(
                                       List<Pair<String, String>> arguments) {
                for (final Pair<String, String> arg : arguments)
                    if (arg.getVar1().equals("name")) {
                        semirareAdventuresSet.add(arg.getVar2());
                        semirareAdventuresSet.add(NON_ASCII.matcher(arg.getVar2())
                                                           .replaceAll(UsefulPatterns.EMPTY_STRING));
                    }
            }
        };

        final ArgumetsHandler wanderingAdventureArgumentsHandler = new ArgumetsHandler() {
            public void parseArguments(
                                       List<Pair<String, String>> arguments) {
                for (final Pair<String, String> arg : arguments)
                    if (arg.getVar1().equals("name")) {
                        wanderingAdventuresSet.add(arg.getVar2());
                        wanderingAdventuresSet.add(NON_ASCII.matcher(arg.getVar2())
                                                            .replaceAll(UsefulPatterns.EMPTY_STRING));
                    }
            }
        };

        final ArgumetsHandler itemdropArgumentsHandler = new ArgumetsHandler() {
            public void parseArguments(
                                       List<Pair<String, String>> arguments) {
                String name = null;
                Boolean onetimeOnly = null;

                for (final Pair<String, String> arg : arguments)
                    if (arg.getVar1().equals("name"))
                        name = arg.getVar2();
                    else if (arg.getVar1().equals("onetimeOnly"))
                        onetimeOnly = Boolean.valueOf(arg.getVar2());

                if (name != null && onetimeOnly != null) {
                    itemdropsMap.put(name, onetimeOnly);
                    itemdropsMap.put(NON_ASCII.matcher(name)
                                              .replaceAll(UsefulPatterns.EMPTY_STRING), onetimeOnly);
                }
            }
        };

        final ArgumetsHandler skillArgumentsHandler = new NameIntegerArgumentsHandler(skillsMap,
                                                                                      "mpCost");

        final ArgumetsHandler mpRegenEquipmentArgumentsHandler = new NameIntegerArgumentsHandler(mpRegenEquipmentsMap,
                                                                                                 "mpRegen");

        final ArgumetsHandler mpCostEquipmentArgumentsHandler = new NameIntegerArgumentsHandler(mpCostEquipmentsMap,
                                                                                                "mpCost");

        final ArgumetsHandler statsEquipmentArgumentsHandler = new ArgumetsHandler() {
            public void parseArguments(
                                       List<Pair<String, String>> arguments) {
                String name = null;
                ExtraStats value = ExtraStats.NO_STATS;

                for (final Pair<String, String> arg : arguments)
                    if (arg.getVar1().equals("name"))
                        name = arg.getVar2();
                    else if (arg.getVar1().equals("statgain"))
                        value = new ExtraStats(Double.parseDouble(arg.getVar2()),
                                               value.musGain,
                                               value.mystGain,
                                               value.moxGain);
                    else if (arg.getVar1().equals("musStatgain"))
                        value = new ExtraStats(value.generalGain,
                                               Integer.parseInt(arg.getVar2()),
                                               value.mystGain,
                                               value.moxGain);
                    else if (arg.getVar1().equals("mystStatgain"))
                        value = new ExtraStats(value.generalGain,
                                               value.musGain,
                                               Integer.parseInt(arg.getVar2()),
                                               value.moxGain);
                    else if (arg.getVar1().equals("moxStatgain"))
                        value = new ExtraStats(value.generalGain,
                                               value.musGain,
                                               value.mystGain,
                                               Integer.parseInt(arg.getVar2()));

                if (name != null && !value.equals(ExtraStats.NO_STATS)) {
                    statsEquipmentsMap.put(name, value);
                    statsEquipmentsMap.put(NON_ASCII.matcher(name)
                                                    .replaceAll(UsefulPatterns.EMPTY_STRING),
                                           value);
                }
            }
        };

        final ArgumetsHandler outfitArgumentsHandler = new ArgumetsHandler() {
            public void parseArguments(
                                       List<Pair<String, String>> arguments) {
                String name = null;
                boolean hat = false;
                boolean weapon = false;
                boolean offhand = false;
                boolean shirt = false;
                boolean pants = false;
                boolean acc1 = false;
                boolean acc2 = false;
                boolean acc3 = false;

                for (final Pair<String, String> arg : arguments)
                    if (arg.getVar1().equals("name"))
                        name = arg.getVar2();
                    else if (arg.getVar1().equals("hat"))
                        hat = Boolean.parseBoolean(arg.getVar2());
                    else if (arg.getVar1().equals("weapon"))
                        weapon = Boolean.parseBoolean(arg.getVar2());
                    else if (arg.getVar1().equals("offhand"))
                        offhand = Boolean.parseBoolean(arg.getVar2());
                    else if (arg.getVar1().equals("shirt"))
                        shirt = Boolean.parseBoolean(arg.getVar2());
                    else if (arg.getVar1().equals("pants"))
                        pants = Boolean.parseBoolean(arg.getVar2());
                    else if (arg.getVar1().equals("acc1"))
                        acc1 = Boolean.parseBoolean(arg.getVar2());
                    else if (arg.getVar1().equals("acc2"))
                        acc2 = Boolean.parseBoolean(arg.getVar2());
                    else if (arg.getVar1().equals("acc3"))
                        acc3 = Boolean.parseBoolean(arg.getVar2());

                if (name != null) {
                    final String nameNoASCII = NON_ASCII.matcher(name)
                                                        .replaceAll(UsefulPatterns.EMPTY_STRING);
                    outfitsMap.put(name, new Outfit(name,
                                                    hat,
                                                    weapon,
                                                    offhand,
                                                    shirt,
                                                    pants,
                                                    acc1,
                                                    acc2,
                                                    acc3));
                    outfitsMap.put(nameNoASCII, new Outfit(nameNoASCII,
                                                           hat,
                                                           weapon,
                                                           offhand,
                                                           shirt,
                                                           pants,
                                                           acc1,
                                                           acc2,
                                                           acc3));
                }
            }
        };

        // Remember to first read the defaults, then the user files. That way
        // additions can be automatically handled without bothering the user by
        // adding them to the internal defaults.
        readXMLDataFile("badmoonDefault.xml", "adventure", badmoonArgumentsHandler);
        readXMLDataFile("badmoon.xml", "adventure", badmoonArgumentsHandler);
        readXMLDataFile("semirareDefault.xml", "adventure", semirareArgumentsHandler);
        readXMLDataFile("semirare.xml", "adventure", semirareArgumentsHandler);
        readXMLDataFile("wanderingEncountersDefault.xml",
                        "adventure",
                        wanderingAdventureArgumentsHandler);
        readXMLDataFile("wanderingEncounters.xml", "adventure", wanderingAdventureArgumentsHandler);
        readXMLDataFile("itemsDefault.xml", "item", itemdropArgumentsHandler);
        readXMLDataFile("items.xml", "item", itemdropArgumentsHandler);
        readXMLDataFile("skillsDefault.xml", "skill", skillArgumentsHandler);
        readXMLDataFile("skills.xml", "skill", skillArgumentsHandler);
        readXMLDataFile("mpRegenEquipmentDefault.xml",
                        "equipment",
                        mpRegenEquipmentArgumentsHandler);
        readXMLDataFile("mpRegenEquipment.xml", "equipment", mpRegenEquipmentArgumentsHandler);
        readXMLDataFile("mpCostEquipmentDefault.xml", "equipment", mpCostEquipmentArgumentsHandler);
        readXMLDataFile("mpCostEquipment.xml", "equipment", mpCostEquipmentArgumentsHandler);
        readXMLDataFile("statsEquipmentDefault.xml", "equipment", statsEquipmentArgumentsHandler);
        readXMLDataFile("statsEquipment.xml", "equipment", statsEquipmentArgumentsHandler);
        readXMLDataFile("outfitsDefault.xml", "outfit", outfitArgumentsHandler);
        readXMLDataFile("outfits.xml", "outfit", outfitArgumentsHandler);

        this.badmoonAdventuresSet = Collections.unmodifiableSet(badmoonAdventuresSet);
        this.semirareAdventuresSet = Collections.unmodifiableSet(semirareAdventuresSet);
        this.wanderingAdventuresSet = Collections.unmodifiableSet(wanderingAdventuresSet);
        this.itemdropsMap = Collections.unmodifiableMap(itemdropsMap);
        this.skillsMap = Collections.unmodifiableMap(skillsMap);
        this.mpRegenEquipmentsMap = Collections.unmodifiableMap(mpRegenEquipmentsMap);
        this.mpCostEquipmentsMap = Collections.unmodifiableMap(mpCostEquipmentsMap);
        this.statsEquipmentsMap = Collections.unmodifiableMap(statsEquipmentsMap);
        this.outfitsMap = Collections.unmodifiableMap(outfitsMap);
    }

    /**
     * Updates the data tables with the data from the given collections and sets
     * it as the current data set.
     * 
     * <p>Please note that using {@code null} as a parameter will result in the
     * given data table and linked collection to stay the way it is.
     * 
     * @param badmoonAdventuresSet Set of Bad Moon adventures
     * @param semirareAdventuresSet Set of semi-rare adventures
     * @param wanderingAdventuresSet Set of adventures with wandering monsters
     * @param itemdropsMap Map of items and whether they drop more than once
     * @param skillsMap Map of skills to numbers
     * @param mpRegenEquipmentsMap Map of equipment to regen rates
     * @param mpCostEquipmentsMap Map of MP equipment to cost
     * @param statsEquipmentsMap Map of equipment to extra stats offered
     * @param outfitsMap Map of outfit names to outfits
     */
    public synchronized void updateDataTables(
                                              final Set<String> badmoonAdventuresSet,
                                              final Set<String> semirareAdventuresSet,
                                              final Set<String> wanderingAdventuresSet,
                                              final Map<String, Boolean> itemdropsMap,
                                              final Map<String, Integer> skillsMap,
                                              final Map<String, Integer> mpRegenEquipmentsMap,
                                              final Map<String, Integer> mpCostEquipmentsMap,
                                              final Map<String, ExtraStats> statsEquipmentsMap,
                                              final Map<String, Outfit> outfitsMap) {
        if (badmoonAdventuresSet != null)
            writeXMLDataFile("badmoon.xml", new DataWriter<String>(badmoonAdventuresSet,
                                                                   "badmoonAdventures",
                                                                   "adventure") {
                @Override
                void writeArguments(
                                    XMLStreamWriter writer, String item)
                                                                        throws XMLStreamException {
                    writer.writeAttribute("name", item);
                }

            });
        if (semirareAdventuresSet != null)
            writeXMLDataFile("semirare.xml", new DataWriter<String>(semirareAdventuresSet,
                                                                    "semirareAdventures",
                                                                    "adventure") {
                @Override
                void writeArguments(
                                    XMLStreamWriter writer, String item)
                                                                        throws XMLStreamException {
                    writer.writeAttribute("name", item);
                }

            });
        if (wanderingAdventuresSet != null)
            writeXMLDataFile("wanderingEncounters.xml", new DataWriter<String>(wanderingAdventuresSet,
                                                                    "wanderingAdventures",
                                                                    "adventure") {
                @Override
                void writeArguments(
                                    XMLStreamWriter writer, String item)
                                                                        throws XMLStreamException {
                    writer.writeAttribute("name", item);
                }

            });
        if (itemdropsMap != null)
            writeXMLDataFile("items.xml",
                             newNameSimpleObjectDataWriter(itemdropsMap.entrySet(),
                                                           "itemdropsData",
                                                           "item",
                                                           "onetimeOnly"));
        if (skillsMap != null)
            writeXMLDataFile("skills.xml",
                             newNameSimpleObjectDataWriter(skillsMap.entrySet(),
                                                           "skillCosts",
                                                           "skill",
                                                           "mpCost"));
        if (mpRegenEquipmentsMap != null)
            writeXMLDataFile("mpRegenEquipment.xml",
                             newNameSimpleObjectDataWriter(mpRegenEquipmentsMap.entrySet(),
                                                           "mpRegenEquipment",
                                                           "equipment",
                                                           "mpRegen"));
        if (mpCostEquipmentsMap != null)
            writeXMLDataFile("mpCostEquipment.xml",
                             newNameSimpleObjectDataWriter(mpCostEquipmentsMap.entrySet(),
                                                           "mpCostEquipment",
                                                           "equipment",
                                                           "mpCost"));
        if (statsEquipmentsMap != null)
            writeXMLDataFile("statsEquipment.xml",
                             new DataWriter<Entry<String, ExtraStats>>(statsEquipmentsMap.entrySet(),
                                                                       "statsEquipment",
                                                                       "equipment") {
                                 @Override
                                 void writeArguments(
                                                     XMLStreamWriter writer,
                                                     Entry<String, ExtraStats> item)
                                                                                    throws XMLStreamException {
                                     final ExtraStats stats = item.getValue();
                                     writer.writeAttribute("name", item.getKey());
                                     writer.writeAttribute("statgain",
                                                           Double.toString(stats.generalGain));
                                     writer.writeAttribute("musStatgain",
                                                           Integer.toString(stats.musGain));
                                     writer.writeAttribute("mystStatgain",
                                                           Integer.toString(stats.mystGain));
                                     writer.writeAttribute("moxStatgain",
                                                           Integer.toString(stats.moxGain));
                                 }

                             });
        if (outfitsMap != null)
            writeXMLDataFile("outfits.xml",
                             new DataWriter<Entry<String, Outfit>>(outfitsMap.entrySet(),
                                                                   "outfits",
                                                                   "outfit") {
                                 @Override
                                 void writeArguments(
                                                     XMLStreamWriter writer,
                                                     Entry<String, Outfit> item)
                                                                                throws XMLStreamException {
                                     final Outfit outfit = item.getValue();
                                     writer.writeAttribute("name", item.getKey());
                                     writer.writeAttribute("hat", Boolean.toString(outfit.hat));
                                     writer.writeAttribute("weapon",
                                                           Boolean.toString(outfit.weapon));
                                     writer.writeAttribute("offhand",
                                                           Boolean.toString(outfit.offhand));
                                     writer.writeAttribute("shirt", Boolean.toString(outfit.shirt));
                                     writer.writeAttribute("pants", Boolean.toString(outfit.pants));
                                     writer.writeAttribute("acc1", Boolean.toString(outfit.acc1));
                                     writer.writeAttribute("acc2", Boolean.toString(outfit.acc2));
                                     writer.writeAttribute("acc3", Boolean.toString(outfit.acc3));
                                 }

                             });

        reloadDataTables();
    }

    /**
     * @param consumableName
     *            The consumable name whose fullness hit should be returned.
     * @return The fullness hit of the given consumable.
     */
    public int getFullnessHit(
                              final String consumableName) {
        final String name = NON_ASCII.matcher(consumableName)
                                     .replaceAll(UsefulPatterns.EMPTY_STRING);
        return getOrZero(fullnessHitMap.get(name.toLowerCase(Locale.ENGLISH)));
    }

    /**
     * @param consumableName
     *            The consumable name whose drunkenness hit should be returned.
     * @return The drunkenness hit of the given consumable.
     */
    public int getDrunkennessHit(
                                 final String consumableName) {
        final String name = NON_ASCII.matcher(consumableName)
                                     .replaceAll(UsefulPatterns.EMPTY_STRING);
        return getOrZero(drunkennessHitMap.get(name.toLowerCase(Locale.ENGLISH)));
    }

    /**
     * @param consumableName
     *            The consumable name whose spleen hit should be returned.
     * @return The spleen hit of the given consumable.
     */
    public int getSpleenHit(
                            final String consumableName) {
        final String name = NON_ASCII.matcher(consumableName)
                                     .replaceAll(UsefulPatterns.EMPTY_STRING);
        return getOrZero(spleenHitMap.get(name.toLowerCase(Locale.ENGLISH)));
    }

    /**
     * @param encounter
     *            The single turn which should be checked on whether it is a
     *            semi-rare.
     * @return True if the encounter is a semi-rare, otherwise false.
     */
    public boolean isSemirareEncounter(
                                       final Encounter encounter) {
        return isSemirareEncounter(encounter.getEncounterName());
    }

    /**
     * @param encounterName
     *            The encounter name which should be checked on whether it is a
     *            semi-rare.
     * @return True if the encounter is a semi-rare, otherwise false.
     */
    public boolean isSemirareEncounter(
                                       final String encounterName) {
        final String name = NON_ASCII.matcher(encounterName)
                                     .replaceAll(UsefulPatterns.EMPTY_STRING);

        return semirareAdventuresSet.contains(name.toLowerCase(Locale.ENGLISH));
    }

    /**
     * @param encounter
     *            The single turn which should be checked on whether it is a Bad
     *            Moon adventure.
     * @return True if the encounter is a Bad Moon adventure, otherwise false.
     */
    public boolean isBadMoonEncounter(
                                      final Encounter encounter) {
        return isBadMoonEncounter(encounter.getEncounterName());
    }

    /**
     * @param encounterName
     *            The encounter name which should be checked on whether it is a
     *            Bad Moon adventure.
     * @return True if the encounter is a Bad Moon adventure, otherwise false.
     */
    public boolean isBadMoonEncounter(
                                      final String encounterName) {
        final String name = NON_ASCII.matcher(encounterName.toLowerCase(Locale.ENGLISH))
                                     .replaceAll(UsefulPatterns.EMPTY_STRING);

        return badmoonAdventuresSet.contains(name) ? true
                                                  : name.startsWith(FLOWERS_FOR_BAD_MOON_ADVENUTRE);
    }

    /**
     * @param encounter
     *            The single turn which should be checked on whether it is a
     *            wandering adventure.
     * @return True if the encounter is a wandering adventure, otherwise false.
     */
    public boolean isWanderingEncounter(
                                        final Encounter encounter) {
        return isWanderingEncounter(encounter.getEncounterName());
    }

    /**
     * @param encounterName
     *            The encounter name which should be checked on whether it is a
     *            wandering adventure.
     * @return True if the encounter is a wandering adventure, otherwise false.
     */
    public boolean isWanderingEncounter(
                                        final String encounterName) {
        final String name = NON_ASCII.matcher(encounterName.toLowerCase(Locale.ENGLISH))
                                     .replaceAll(UsefulPatterns.EMPTY_STRING);

        return wanderingAdventuresSet.contains(name);
    }

    /**
     * @param skillName
     *            The skill name whose MP cost should be returned.
     * @return The MP cost of the given skill.
     */
    public int getSkillMPCost(
                              final String skillName) {
        final String name = NON_ASCII.matcher(skillName).replaceAll(UsefulPatterns.EMPTY_STRING);
        return getOrZero(skillsMap.get(name.toLowerCase(Locale.ENGLISH)));
    }

    /**
     * @param equipment
     *            The used equipment.
     * @return The MP cost offset for skill casts, based on the given
     *         parameters. Can be -3 at minimum, but not lower.
     */
    public int getMPCostOffset(
                               final EquipmentChange equipment) {
        int mpCostOffset = 0;
        mpCostOffset += getOrZero(mpCostEquipmentsMap.get(equipment.getHat()));
        mpCostOffset += getOrZero(mpCostEquipmentsMap.get(equipment.getWeapon()));
        mpCostOffset += getOrZero(mpCostEquipmentsMap.get(equipment.getOffhand()));
        mpCostOffset += getOrZero(mpCostEquipmentsMap.get(equipment.getShirt()));
        mpCostOffset += getOrZero(mpCostEquipmentsMap.get(equipment.getPants()));
        mpCostOffset += getOrZero(mpCostEquipmentsMap.get(equipment.getAcc1()));
        mpCostOffset += getOrZero(mpCostEquipmentsMap.get(equipment.getAcc2()));
        mpCostOffset += getOrZero(mpCostEquipmentsMap.get(equipment.getAcc3()));
        mpCostOffset += getOrZero(mpCostEquipmentsMap.get(equipment.getFamEquip()));

        // -3 is minimum
        if (mpCostOffset < -3)
            mpCostOffset = -3;

        return mpCostOffset;
    }

    /**
     * @param equipmentName
     *            The equipment name whose MP per turn regeneration should be
     *            returned.
     * @return The MP per turn regeneration of the given equipment.
     */
    public int getMPFromEquipment(
                                  final String equipmentName) {
        final String name = NON_ASCII.matcher(equipmentName)
                                     .replaceAll(UsefulPatterns.EMPTY_STRING);
        return getOrZero(mpRegenEquipmentsMap.get(name.toLowerCase(Locale.ENGLISH)));
    }

    /**
     * @return A list of items and the amount of main substats they give per
     *         turn. The list is sorted from the highest stats per turn to the
     *         lowest with the order of general stats, muscle stats, mysticality
     *         stats and moxie stats.
     */
    public List<Pair<String, ExtraStats>> getStatsItems() {
        final List<Pair<String, ExtraStats>> result = Lists.newArrayList(statsEquipmentsMap.size());

        for (final String s : statsEquipmentsMap.keySet())
            result.add(Pair.of(s, statsEquipmentsMap.get(s)));

        return Lists.sort(result, new Comparator<Pair<String, ExtraStats>>() {
            public int compare(
                               final Pair<String, ExtraStats> p1, final Pair<String, ExtraStats> p2) {
                return p2.getVar2().compareTo(p1.getVar2());
            }
        });
    }

    /**
     * @param outfitName
     *            The name of the outfit whose resulting equipment changes
     *            should be returned.
     * @return The equipment changes resulting from putting on the given outfit.
     *         If the outfit is unknown, {@link Outfit#NO_CHANGE} will be
     *         returned.
     */
    public Outfit getOutfitChange(
                                  final String outfitName) {
        final Outfit outfit = outfitsMap.get(outfitName);

        return outfit != null ? outfit : Outfit.NO_CHANGE;
    }

    /**
     * Check whether the given item is important. Important items are items that
     * are always listed in textual logs.
     * 
     * @param itemName
     *            The item name which should be checked on whether it is an
     *            important item.
     * @return True if the item is an important item, otherwise false.
     */
    public boolean isImportantItem(
                                   final String itemName) {
        final Boolean onetimeOnly = itemdropsMap.get(itemName);

        return onetimeOnly != null ? !onetimeOnly : false;
    }

    /**
     * Check whether the given item is an onetime-only item. Onetime-only items
     * are items that are listed in textual logs only the first time they
     * dropped.
     * 
     * @param itemName
     *            The item name which should be checked on whether it is an
     *            onetime-only item.
     * @return True if the item is an one-time item, otherwise false.
     */
    public boolean isOnetimeItem(
                                 final String itemName) {
        final Boolean onetimeOnly = itemdropsMap.get(itemName);

        return onetimeOnly != null ? onetimeOnly : false;
    }

    /**
     * @return A set of Bad Moon adventure names. Note that the set is
     *         read-only.
     */
    public Set<String> getBadmoonAdventuresSet() {
        return badmoonAdventuresSet;
    }

    /**
     * @return A set of semi-rare adventure names. Note that the set is
     *         read-only.
     */
    public Set<String> getSemirareAdventuresSet() {
        return semirareAdventuresSet;
    }

    /**
     * @return A set of wandering adventure names. Note that the set is
     *         read-only.
     */
    public Set<String> getWanderingAdventuresSet() {
        return wanderingAdventuresSet;
    }

    /**
     * @return A map of skills and their MP costs. Note that the map is
     *         read-only.
     */
    public Map<String, Integer> getSkillCostsMap() {
        return skillsMap;
    }

    /**
     * @return A map of equipment names and their MP cost offsets. Note that the
     *         map is read-only.
     */
    public Map<String, Integer> getMPCostOffsetEquipmentMap() {
        return mpCostEquipmentsMap;
    }

    /**
     * @return A map of equipment names and their MP regeneration per turn. Note
     *         that the map is read-only.
     */
    public Map<String, Integer> getMPFromEquipmentMap() {
        return mpRegenEquipmentsMap;
    }

    /**
     * @return A map of equipment names and their substat gain per turn. Note
     *         that the map is read-only.
     */
    public Map<String, ExtraStats> getStatsEquipmentMap() {
        return statsEquipmentsMap;
    }

    /**
     * @return A map of outfit names and how and whether they change equipment
     *         slots. Note that the map is read-only.
     */
    public Map<String, Outfit> getOutfitsMap() {
        return outfitsMap;
    }

    /**
     * @return A map of item names and whether they are shown only once in
     *         textual logs. Items not named don't show up at all in textual
     *         logs. Note that the map is read-only.
     */
    public Map<String, Boolean> getItemdropsMap() {
        return itemdropsMap;
    }

    /**
     * @return The given value or, if the value is null, 0.
     */
    private static int getOrZero(
                                 final Integer value) {
        return value == null ? 0 : value.intValue();
    }

    private static void readFormattedTable(
                                           final BufferedReader br,
                                           final Map<String, Integer> savedToMap,
                                           final Pattern capturePattern) {
        String tmpLine;

        try {
            while ((tmpLine = br.readLine()) != null)
                // Ignore empty lines and comments
                if (tmpLine.length() > 0 && !tmpLine.startsWith("//") && !tmpLine.startsWith("#")) {
                    final Matcher m = capturePattern.matcher(tmpLine);
                    if (m.matches()) {
                        final String name = NON_ASCII.matcher(m.group(1))
                                                     .replaceAll(UsefulPatterns.EMPTY_STRING);
                        savedToMap.put(name.toLowerCase(Locale.ENGLISH),
                                       Integer.valueOf(Integer.parseInt(m.group(2))));
                    }
                }

            br.close();
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    private static void readXMLDataFile(
                                        final String filename, final String dataNodeName,
                                        final ArgumetsHandler argumentsParser) {
        try {
            for (final List<Pair<String, String>> args : XMLDataFilesReader.parseXMLDataFile(filename,
                                                                                             dataNodeName))
                argumentsParser.parseArguments(args);
        } catch (XMLAccessException e) {
            e.printStackTrace();
        }
    }

    private static <T> void writeXMLDataFile(
                                             final String filename, final DataWriter<T> dataWriter) {
        final File saveDest = new File(ROOT_DIRECTORY + File.separator + KOL_DATA_DIRECTORY
                                       + filename);
        try {
            XMLDataFilesWriter.writeXMLDataFile(saveDest, dataWriter);
        } catch (XMLAccessException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private interface ArgumetsHandler {
        void parseArguments(
                            final List<Pair<String, String>> arguments);
    }

    private static final class NameIntegerArgumentsHandler implements ArgumetsHandler {
        private final Map<String, Integer> map;

        private final String integerArgumentName;

        NameIntegerArgumentsHandler(
                                    final Map<String, Integer> map, final String integerArgumentName) {
            if (map == null)
                throw new NullPointerException("The map must not be null.");
            if (integerArgumentName == null)
                throw new NullPointerException("The integer argument name must not be null.");

            this.map = map;
            this.integerArgumentName = integerArgumentName;
        }

        public void parseArguments(
                                   List<Pair<String, String>> arguments) {
            String name = null;
            Integer intValue = null;

            for (final Pair<String, String> arg : arguments)
                if (arg.getVar1().equals("name"))
                    name = arg.getVar2();
                else if (arg.getVar1().equals(integerArgumentName))
                    intValue = Integer.valueOf(arg.getVar2());

            if (name != null && intValue != null) {
                map.put(name, intValue);
                map.put(NON_ASCII.matcher(name).replaceAll(UsefulPatterns.EMPTY_STRING), intValue);
            }
        }
    }

    private static <T> NameSimpleObjectDataWriter<T> newNameSimpleObjectDataWriter(
                                                                                   Iterable<Entry<String, T>> dataItems,
                                                                                   String rootNodeName,
                                                                                   String dataNodeName,
                                                                                   final String simpleObjectKeyName) {
        return new NameSimpleObjectDataWriter<T>(dataItems,
                                                 rootNodeName,
                                                 dataNodeName,
                                                 simpleObjectKeyName);
    }

    private static final class NameSimpleObjectDataWriter<T> extends DataWriter<Entry<String, T>> {
        private final String simpleObjectKeyName;

        private NameSimpleObjectDataWriter(
                                           Iterable<Entry<String, T>> dataItems,
                                           String rootNodeName, String dataNodeName,
                                           final String simpleObjectKeyName) {
            super(dataItems, rootNodeName, dataNodeName);

            if (simpleObjectKeyName == null)
                throw new NullPointerException("The key name of the simple object must not be null.");

            this.simpleObjectKeyName = simpleObjectKeyName;
        }

        @Override
        void writeArguments(
                            XMLStreamWriter writer, Entry<String, T> item)
                                                                          throws XMLStreamException {
            writer.writeAttribute("name", item.getKey());
            writer.writeAttribute(simpleObjectKeyName, item.getValue().toString());
        }
    }
}
