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

package com.googlecode.logVisualizer.util.xmlLogs;

import java.io.*;
import java.nio.charset.Charset;
import java.util.regex.Pattern;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import com.bea.xml.stream.XMLOutputFactoryBase;
import com.googlecode.logVisualizer.Settings;
import com.googlecode.logVisualizer.logData.*;
import com.googlecode.logVisualizer.logData.consumables.Consumable;
import com.googlecode.logVisualizer.logData.logSummary.LevelData;
import com.googlecode.logVisualizer.logData.turn.Encounter;
import com.googlecode.logVisualizer.logData.turn.SingleTurn;
import com.googlecode.logVisualizer.logData.turn.TurnInterval;
import com.googlecode.logVisualizer.logData.turn.turnAction.*;
import com.googlecode.logVisualizer.util.DataNumberPair;
import com.googlecode.logVisualizer.util.dataTables.DataTablesHandler;

/**
 * This class gives access to methods to create XML data files from detailed log
 * data.
 * <p>
 * All methods in this class throw a {@link NullPointerException} if a null
 * object reference is passed in any parameter.
 */
public final class XMLLogCreator {

    /**
     * Creates an XML file containing the data of the given log data in the
     * given directory.
     * 
     * @param logData
     *            The log which should be turned into an XML file.
     * @param saveDst
     *            The directory in which the log should be saved in. Note that
     *            the filename will be created from the given log.
     * @throws IllegalArgumentException
     *             if the given log is not a detailed log (see
     *             {@link LogDataHolder#isDetailedLog()}); if the given file
     *             isn't a directory
     */
    public static void createXMLLog(
                                    final LogDataHolder logData, final File saveDst)
                                                                                    throws FileAccessException,
                                                                                    XMLAccessException {
        if (logData == null)
            throw new NullPointerException("The log data must not be null.");
        if (saveDst == null)
            throw new NullPointerException("The save destination must not be null.");
        if (!logData.isDetailedLog())
            throw new IllegalArgumentException("Only detailed logs can be turned into xml.");
        if (!saveDst.isDirectory())
            throw new IllegalArgumentException("Incorrect saving destination, needs to be a directory.");

        OutputStreamWriter out;
        try {
            out = new OutputStreamWriter(new FileOutputStream(new File(saveDst,
                                                                       logData.getLogName()
                                                                               + ".xml")),
                                         Charset.forName("UTF-8"));
        } catch (final FileNotFoundException e) {
            e.printStackTrace();
            throw new FileAccessException("File stream could not be created.");
        }

        try {
            final XMLOutputFactory factory = XMLOutputFactoryBase.newInstance();
            final XMLStreamWriter writer = factory.createXMLStreamWriter(out);

            final XMLLogCreator logCreator = new XMLLogCreator(writer, logData);
            logCreator.createLog();

            writer.close();
        } catch (final XMLStreamException e) {
            e.printStackTrace();
            throw new XMLAccessException("Could not write to XML file.");
        }

        try {
            out.close();
        } catch (final IOException e) {
            e.printStackTrace();
            throw new FileAccessException("File stream could not be closed.");
        }
    }

    private static final Pattern LINEBREAK_FINDER = Pattern.compile("\n");

    private final XMLStreamWriter writer;

    private final LogDataHolder logData;

    private XMLLogCreator(
                          final XMLStreamWriter writer, final LogDataHolder logData) {
        if (writer == null)
            throw new NullPointerException("The xml writer must not be null.");
        if (logData == null)
            throw new NullPointerException("The log data must not be null.");

        this.writer = writer;
        this.logData = logData;
    }

    private void createLog()
                            throws XMLStreamException {
        final int delimiterIndex = logData.getLogName().lastIndexOf("-");
        final String characterName = logData.getLogName().substring(0, delimiterIndex);
        final String startDate = logData.getLogName().substring(delimiterIndex + 1);

        writer.writeStartDocument("UTF-8", "1.0");
        writer.writeStartElement("ascensionlogxml");
        writer.writeAttribute("version", Settings.getSettingString("XML format version"));
        writer.writeStartElement("filecreator");
        writer.writeAttribute("program", "Ascension Log Visualizer");
        writer.writeAttribute("programversion", Settings.getSettingString("Version"));
        writer.writeEndElement();
        writer.writeStartElement("ascension");
        writer.writeAttribute("charactername", characterName);
        writer.writeAttribute("characterclass", logData.getCharacterClass().toString());
        writer.writeAttribute("gamemode", logData.getGameMode().toString());
        writer.writeAttribute("ascensionpath", logData.getAscensionPath().toString());
        writer.writeAttribute("startdate", startDate);

        createTurnRundown();
        createSummaries();

        writer.writeEndElement();
        writer.writeEndElement();
        writer.writeEndDocument();
    }

    private void createTurnRundown()
                                    throws XMLStreamException {
        final int startTurn = logData.getTurnsSpent().get(0).getTurnNumber();
        final int endTurn = logData.getLastTurnSpent().getTurnNumber();

        writer.writeStartElement("turnrundown");
        writer.writeAttribute("startturn", Integer.toString(startTurn));
        writer.writeAttribute("endturn", Integer.toString(endTurn));

        for (final TurnInterval ti : logData.getTurnIntervalsSpent()) {
            final int tiStartTurn = ti.getEndTurn() > 0 ? ti.getStartTurn() + 1 : 0;
            writer.writeStartElement("turninterval");
            writer.writeAttribute("startturn", Integer.toString(tiStartTurn));
            writer.writeAttribute("endturn", Integer.toString(ti.getEndTurn()));
            writer.writeAttribute("area", ti.getAreaName());

            for (final SingleTurn st : ti.getTurns())
                for (final Encounter e : st.getEncounters()) {
                    writer.writeStartElement("turn");
                    writer.writeAttribute("turnnumber", Integer.toString(e.getTurnNumber()));
                    writer.writeAttribute("turnversion", e.getTurnVersion().toString());

                    writer.writeStartElement("areaname");
                    writer.writeCharacters(e.getAreaName());
                    writer.writeEndElement();

                    writer.writeStartElement("encountername");
                    writer.writeCharacters(e.getEncounterName());
                    writer.writeEndElement();

                    writer.writeStartElement("day");
                    writer.writeCharacters(Integer.toString(e.getDayNumber()));
                    writer.writeEndElement();

                    writer.writeStartElement("familiar");
                    writer.writeCharacters(e.getUsedFamiliar().getFamiliarName());
                    writer.writeEndElement();

                    writer.writeStartElement("equipment");
                    writer.writeStartElement("hat");
                    writer.writeCharacters(e.getUsedEquipment().getHat());
                    writer.writeEndElement();
                    writer.writeStartElement("weapon");
                    writer.writeCharacters(e.getUsedEquipment().getWeapon());
                    writer.writeEndElement();
                    writer.writeStartElement("offhand");
                    writer.writeCharacters(e.getUsedEquipment().getOffhand());
                    writer.writeEndElement();
                    writer.writeStartElement("shirt");
                    writer.writeCharacters(e.getUsedEquipment().getShirt());
                    writer.writeEndElement();
                    writer.writeStartElement("pants");
                    writer.writeCharacters(e.getUsedEquipment().getPants());
                    writer.writeEndElement();
                    writer.writeStartElement("acc1");
                    writer.writeCharacters(e.getUsedEquipment().getAcc1());
                    writer.writeEndElement();
                    writer.writeStartElement("acc2");
                    writer.writeCharacters(e.getUsedEquipment().getAcc2());
                    writer.writeEndElement();
                    writer.writeStartElement("acc3");
                    writer.writeCharacters(e.getUsedEquipment().getAcc3());
                    writer.writeEndElement();
                    writer.writeStartElement("famequip");
                    writer.writeCharacters(e.getUsedEquipment().getFamEquip());
                    writer.writeEndElement();
                    writer.writeEndElement();

                    writer.writeStartElement("statgain");
                    writeStatgainAttributes(e.getStatGain());
                    writer.writeEndElement();

                    writer.writeStartElement("meatgain");
                    writer.writeStartElement("insideencounter");
                    writer.writeCharacters(Integer.toString(e.getMeat().encounterMeatGain));
                    writer.writeEndElement();
                    writer.writeStartElement("other");
                    writer.writeCharacters(Integer.toString(e.getMeat().otherMeatGain));
                    writer.writeEndElement();
                    writer.writeStartElement("meatspent");
                    writer.writeCharacters(Integer.toString(e.getMeat().meatSpent));
                    writer.writeEndElement();
                    writer.writeEndElement();

                    writer.writeStartElement("mpgain");
                    writer.writeStartElement("insideencounter");
                    writer.writeCharacters(Integer.toString(e.getMPGain().encounterMPGain));
                    writer.writeEndElement();
                    writer.writeStartElement("starfish");
                    writer.writeCharacters(Integer.toString(e.getMPGain().starfishMPGain));
                    writer.writeEndElement();
                    writer.writeStartElement("resting");
                    writer.writeCharacters(Integer.toString(e.getMPGain().restingMPGain));
                    writer.writeEndElement();
                    writer.writeStartElement("outofencounter");
                    writer.writeCharacters(Integer.toString(e.getMPGain().outOfEncounterMPGain));
                    writer.writeEndElement();
                    writer.writeStartElement("consumable");
                    writer.writeCharacters(Integer.toString(e.getMPGain().consumableMPGain));
                    writer.writeEndElement();
                    writer.writeEndElement();

                    writer.writeStartElement("disintegration");
                    writer.writeAttribute("used", Boolean.toString(e.isDisintegrated()));
                    writer.writeEndElement();

                    writer.writeStartElement("freerunaways");
                    writer.writeCharacters(Integer.toString(e.getFreeRunaways()));
                    writer.writeEndElement();

                    writer.writeStartElement("notes");
                    writer.writeCharacters(LINEBREAK_FINDER.matcher(e.getNotes())
                                                           .replaceAll("\\{n\\}"));
                    writer.writeEndElement();

                    for (final Item i : e.getDroppedItems()) {
                        writer.writeStartElement("itemdrop");
                        writer.writeAttribute("amount", Integer.toString(i.getAmount()));
                        writer.writeStartElement("name");
                        writer.writeCharacters(i.getName());
                        writer.writeEndElement();
                        writer.writeEndElement();
                    }

                    for (final Skill s : e.getSkillsCast()) {
                        writer.writeStartElement("skillcast");
                        writer.writeAttribute("amount", Integer.toString(s.getAmount()));
                        writer.writeAttribute("mpcost", Integer.toString(s.getMpCost()));
                        writer.writeStartElement("name");
                        writer.writeCharacters(s.getName());
                        writer.writeEndElement();
                        writer.writeEndElement();
                    }

                    for (final Consumable c : e.getConsumablesUsed()) {
                        final int organHit;
                        switch (c.getConsumableVersion()) {
                            case FOOD:
                                organHit = DataTablesHandler.HANDLER.getFullnessHit(c.getName());
                                break;
                            case BOOZE:
                                organHit = DataTablesHandler.HANDLER.getDrunkennessHit(c.getName());
                                break;
                            case SPLEEN:
                                organHit = DataTablesHandler.HANDLER.getSpleenHit(c.getName());
                                break;
                            default:
                                organHit = 0;
                        }

                        writer.writeStartElement("consumable");
                        writer.writeAttribute("amount", Integer.toString(c.getAmount()));
                        writer.writeAttribute("version", c.getConsumableVersion().toString());
                        writer.writeStartElement("name");
                        writer.writeCharacters(c.getName());
                        writer.writeEndElement();
                        writer.writeStartElement("adventuregain");
                        writer.writeCharacters(Integer.toString(c.getAdventureGain()));
                        writer.writeEndElement();
                        writer.writeStartElement("statgain");
                        writeStatgainAttributes(c.getStatGain());
                        writer.writeEndElement();
                        writer.writeStartElement("organhit");
                        writer.writeCharacters(Integer.toString(organHit));
                        writer.writeEndElement();
                        writer.writeStartElement("consumedonday");
                        writer.writeCharacters(Integer.toString(c.getDayNumberOfUsage()));
                        writer.writeEndElement();
                        writer.writeEndElement();
                    }

                    writer.writeEndElement();
                }

            writer.writeStartElement("freerunaways");
            writer.writeStartElement("attemptedfreerunaways");
            writer.writeCharacters(Integer.toString(ti.getRunawayAttempts()
                                                      .getNumberOfAttemptedRunaways()));
            writer.writeEndElement();
            writer.writeStartElement("successfulfreerunaways");
            writer.writeCharacters(Integer.toString(ti.getRunawayAttempts()
                                                      .getNumberOfSuccessfulRunaways()));
            writer.writeEndElement();
            writer.writeEndElement();

            writer.writeStartElement("notes");
            writer.writeStartElement("preintervalnotes");
            writer.writeCharacters(LINEBREAK_FINDER.matcher(ti.getPreIntervalComment()
                                                              .getComments()).replaceAll("\\{n\\}"));
            writer.writeEndElement();
            writer.writeStartElement("postintervalnotes");
            writer.writeCharacters(LINEBREAK_FINDER.matcher(ti.getPostIntervalComment()
                                                              .getComments()).replaceAll("\\{n\\}"));
            writer.writeEndElement();
            writer.writeEndElement();

            writer.writeEndElement();
        }

        writer.writeEndElement();
    }

    private void createSummaries()
                                  throws XMLStreamException {
        writer.writeStartElement("daychanges");
        for (final DayChange dc : logData.getDayChanges()) {
            final HeaderFooterComment hfc = logData.getHeaderFooterComment(dc);

            writer.writeStartElement("day");
            writer.writeAttribute("daynumber", Integer.toString(dc.getDayNumber()));
            writer.writeStartElement("turnwhenreached");
            writer.writeCharacters(Integer.toString(dc.getTurnNumber()));
            writer.writeEndElement();
            writer.writeStartElement("headernotes");
            writer.writeCharacters(LINEBREAK_FINDER.matcher(hfc.getHeaderComments())
                                                   .replaceAll("\\{n\\}"));
            writer.writeEndElement();
            writer.writeStartElement("footernotes");
            writer.writeCharacters(LINEBREAK_FINDER.matcher(hfc.getFooterComments())
                                                   .replaceAll("\\{n\\}"));
            writer.writeEndElement();
            writer.writeEndElement();
        }
        writer.writeEndElement();

        writer.writeStartElement("familiarchanges");
        for (final FamiliarChange fc : logData.getFamiliarChanges()) {
            writer.writeStartElement("familiar");
            writer.writeAttribute("familiarname", fc.getFamiliarName());
            writer.writeStartElement("changeonturn");
            writer.writeCharacters(Integer.toString(fc.getTurnNumber()));
            writer.writeEndElement();
            writer.writeEndElement();
        }
        writer.writeEndElement();

        writer.writeStartElement("levels");
        for (final LevelData ld : logData.getLevels()) {
            writer.writeStartElement("level");
            writer.writeAttribute("levelnumber", Integer.toString(ld.getLevelNumber()));
            writer.writeAttribute("onturn", Integer.toString(ld.getLevelReachedOnTurn()));
            writer.writeStartElement("combatturns");
            writer.writeCharacters(Integer.toString(ld.getCombatTurns()));
            writer.writeEndElement();
            writer.writeStartElement("noncombatturns");
            writer.writeCharacters(Integer.toString(ld.getNoncombatTurns()));
            writer.writeEndElement();
            writer.writeStartElement("otherturns");
            writer.writeCharacters(Integer.toString(ld.getOtherTurns()));
            writer.writeEndElement();
            writer.writeStartElement("statswhenreached");
            writeStatgainAttributes(ld.getStatsAtLevelReached());
            writer.writeEndElement();
            writer.writeStartElement("mainstatgainperturn");
            writer.writeCharacters(Double.toString(ld.getStatGainPerTurn()));
            writer.writeEndElement();
            writer.writeEndElement();
        }
        writer.writeEndElement();

        writer.writeStartElement("equipmentchanges");
        for (final EquipmentChange ec : logData.getEquipmentChanges()) {
            writer.writeStartElement("equipmentchange");
            writer.writeAttribute("onturn", Integer.toString(ec.getTurnNumber()));
            writer.writeStartElement("hat");
            writer.writeCharacters(ec.getHat());
            writer.writeEndElement();
            writer.writeStartElement("weapon");
            writer.writeCharacters(ec.getWeapon());
            writer.writeEndElement();
            writer.writeStartElement("offhand");
            writer.writeCharacters(ec.getOffhand());
            writer.writeEndElement();
            writer.writeStartElement("shirt");
            writer.writeCharacters(ec.getShirt());
            writer.writeEndElement();
            writer.writeStartElement("pants");
            writer.writeCharacters(ec.getPants());
            writer.writeEndElement();
            writer.writeStartElement("acc1");
            writer.writeCharacters(ec.getAcc1());
            writer.writeEndElement();
            writer.writeStartElement("acc2");
            writer.writeCharacters(ec.getAcc2());
            writer.writeEndElement();
            writer.writeStartElement("acc3");
            writer.writeCharacters(ec.getAcc3());
            writer.writeEndElement();
            writer.writeStartElement("famequip");
            writer.writeCharacters(ec.getFamEquip());
            writer.writeEndElement();
            writer.writeEndElement();
        }
        writer.writeEndElement();

        writer.writeStartElement("playersnapshots");
        for (final PlayerSnapshot ps : logData.getPlayerSnapshots()) {
            writer.writeStartElement("playersnapshot");
            writer.writeAttribute("onturn", Integer.toString(ps.getTurnNumber()));
            writer.writeStartElement("stats");
            writeStatgainAttributes(new Statgain(ps.getMuscleStats(),
                                                 ps.getMystStats(),
                                                 ps.getMoxieStats()));
            writer.writeEndElement();
            writer.writeStartElement("adventuresleft");
            writer.writeCharacters(Integer.toString(ps.getAdventuresLeft()));
            writer.writeEndElement();
            writer.writeStartElement("currentmeat");
            writer.writeCharacters(Integer.toString(ps.getCurrentMeat()));
            writer.writeEndElement();
            writer.writeEndElement();
        }
        writer.writeEndElement();

        writer.writeStartElement("pulls");
        for (final Pull p : logData.getPulls()) {
            writer.writeStartElement("pull");
            writer.writeAttribute("daynumber", Integer.toString(p.getDayNumber()));
            writer.writeAttribute("onturn", Integer.toString(p.getTurnNumber()));
            writer.writeStartElement("itemname");
            writer.writeCharacters(p.getItemName());
            writer.writeEndElement();
            writer.writeStartElement("amount");
            writer.writeCharacters(Integer.toString(p.getAmount()));
            writer.writeEndElement();
            writer.writeEndElement();
        }
        writer.writeEndElement();

        writer.writeStartElement("huntedcombats");
        for (final DataNumberPair<String> dnp : logData.getHuntedCombats()) {
            writer.writeStartElement("combat");
            writer.writeAttribute("name", dnp.getData());
            writer.writeAttribute("onturn", dnp.getNumber().toString());
            writer.writeEndElement();
        }
        writer.writeEndElement();

        writer.writeStartElement("lostcombats");
        for (final DataNumberPair<String> dnp : logData.getLostCombats()) {
            writer.writeStartElement("combat");
            writer.writeAttribute("name", dnp.getData());
            writer.writeAttribute("onturn", dnp.getNumber().toString());
            writer.writeEndElement();
        }
        writer.writeEndElement();
    }

    private void writeStatgainAttributes(
                                         final Statgain stats)
                                                              throws XMLStreamException {
        writer.writeAttribute("muscle", Integer.toString(stats.mus));
        writer.writeAttribute("myst", Integer.toString(stats.myst));
        writer.writeAttribute("moxie", Integer.toString(stats.mox));
    }
}
