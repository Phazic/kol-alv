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

package com.googlecode.alv.creator.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import com.googlecode.alv.logData.HeaderFooterComment;
import com.googlecode.alv.logData.Item;
import com.googlecode.alv.logData.LogComment;
import com.googlecode.alv.logData.LogDataHolder;
import com.googlecode.alv.logData.MPGain;
import com.googlecode.alv.logData.MeatGain;
import com.googlecode.alv.logData.Skill;
import com.googlecode.alv.logData.Statgain;
import com.googlecode.alv.logData.LogDataHolder.AscensionPath;
import com.googlecode.alv.logData.LogDataHolder.GameMode;
import com.googlecode.alv.logData.consumables.Consumable;
import com.googlecode.alv.logData.consumables.Consumable.ConsumableVersion;
import com.googlecode.alv.logData.logSummary.LevelData;
import com.googlecode.alv.logData.turn.DetailedTurnInterval;
import com.googlecode.alv.logData.turn.SingleTurn;
import com.googlecode.alv.logData.turn.TurnInterval;
import com.googlecode.alv.logData.turn.TurnVersion;
import com.googlecode.alv.logData.turn.turnAction.DayChange;
import com.googlecode.alv.logData.turn.turnAction.EquipmentChange;
import com.googlecode.alv.logData.turn.turnAction.FamiliarChange;
import com.googlecode.alv.logData.turn.turnAction.PlayerSnapshot;
import com.googlecode.alv.logData.turn.turnAction.Pull;
import com.googlecode.alv.util.DataNumberPair;
import com.googlecode.alv.util.Lists;
import com.googlecode.alv.util.Maps;
import com.googlecode.alv.util.Pair;
import com.googlecode.alv.util.Stack;

/**
 * This class gives access to methods to parse XML data files containing
 * ascension log data.
 * <p>
 * All methods in this class throw a {@link NullPointerException} if a null
 * object reference is passed in any parameter.
 */
public final class XMLLogReader {
    static {
        System.setProperty("javax.xml.stream.XMLInputFactory", "com.bea.xml.stream.MXParserFactory");
        System.setProperty("javax.xml.stream.XMLEventFactory", "com.bea.xml.stream.EventFactory");
    }

    /**
     * Returns a {@link LogDataHolder} object containing the data of the given
     * ascension log XML file.
     *
     * @param xmlLog
     *            The ascension log XML file which is supposed to be parsed.
     * @return The resulting log data from the given ascension log XML file.
     * @throws IllegalArgumentException
     *             if the given file isn't an existing file
     * @throws FileAccessException If the file stream could not be created
     * @throws XMLAccessException If the XML file could not be read
     */
    public static LogDataHolder parseXMLLog(
            final File xmlLog)
                    throws FileAccessException,
                    XMLAccessException {
        if (xmlLog == null)
            throw new NullPointerException("The XML log file reference must not be null.");
        if (!xmlLog.isFile())
            throw new IllegalArgumentException("The given file doesn't exist or is a directory.");

        final LogDataHolder logData;

        final InputStream in;
        try {
            in = new FileInputStream(xmlLog);
        } catch (final FileNotFoundException e) {
            e.printStackTrace();
            throw new FileAccessException("File stream could not be created.");
        }

        try {
            final XMLInputFactory factory = XMLInputFactory.newInstance();
            final XMLStreamReader parser = factory.createXMLStreamReader(in);

            final XMLLogReader reader = new XMLLogReader(parser);
            logData = reader.parseLog();

            parser.close();
        } catch (final XMLStreamException e) {
            e.printStackTrace();
            throw new XMLAccessException("Could not read XML file.");
        }

        try {
            in.close();
        } catch (final IOException e) {
            e.printStackTrace();
            throw new FileAccessException("File stream could not be closed.");
        }

        return logData;
    }

    private static final Pattern LINEBREAK_FINDER = Pattern.compile("\\{n\\}");

    private final XMLStreamReader parser;

    private final LogDataHolder logData = new LogDataHolder(true);

    private final Stack<FamiliarChange> familiarStack = Stack.newStack();

    private final Stack<EquipmentChange> equipmentStack = Stack.newStack();

    private XMLLogReader(
            final XMLStreamReader parser) {
        if (parser == null)
            throw new NullPointerException("The XML parser must not be null.");

        this.parser = parser;

        logData.setMafiaTurnIteration(false);
        familiarStack.push(FamiliarChange.NO_FAMILIAR);
        equipmentStack.push(EquipmentChange.NO_EQUIPMENT);
    }

    private LogDataHolder parseLog()
            throws XMLStreamException {
        String ascensionLogXMLVersion = "";
        String fileCreatorName = "";
        String fileCreatorVersion = "";

        while (parser.hasNext()) {
            switch (parser.getEventType()) {
            case XMLStreamConstants.START_ELEMENT:
                final String nodeName = parser.getLocalName();

                if (nodeName.equals("ascensionlogxml")) {
                    for (int i = 0; i < parser.getAttributeCount(); i++)
                        if (parser.getAttributeLocalName(i).equals("version"))
                            ascensionLogXMLVersion = parser.getAttributeValue(i);
                } else if (nodeName.equals("filecreator")) {
                    for (int i = 0; i < parser.getAttributeCount(); i++)
                        if (parser.getAttributeLocalName(i).equals("programname"))
                            fileCreatorName = parser.getAttributeValue(i);
                        else if (parser.getAttributeLocalName(i).equals("programversion"))
                            fileCreatorVersion = parser.getAttributeValue(i);
                } else if (nodeName.equals("ascension"))
                    parseAscension();

                break;
            default:
                break;
            }
            parser.next();
        }

        return logData;
    }

    private void parseAscension()
            throws XMLStreamException {
        Map<Integer, TurnInterval> intervals = null;
        String characterName = "";
        String startData = "";
        for (int i = 0; i < parser.getAttributeCount(); i++)
            if (parser.getAttributeLocalName(i).equals("charactername"))
                characterName = parser.getAttributeValue(i);
            else if (parser.getAttributeLocalName(i).equals("startdate"))
                startData = parser.getAttributeValue(i);
            else if (parser.getAttributeLocalName(i).equals("characterclass"))
                logData.setCharacterClass(parser.getAttributeValue(i));
            else if (parser.getAttributeLocalName(i).equals("gamemode"))
                logData.setGameMode(GameMode.fromString(parser.getAttributeValue(i)));
            else if (parser.getAttributeLocalName(i).equals("ascensionpath"))
                logData.setAscensionPath(AscensionPath.fromString(parser.getAttributeValue(i)));
        logData.setLogName(characterName + "-" + startData);

        ascensionParsing: {
            while (parser.hasNext()) {
                parser.next();
                switch (parser.getEventType()) {
                case XMLStreamConstants.START_ELEMENT:
                    final String nodeName = parser.getLocalName();

                    if (nodeName.equals("turnrundown"))
                        intervals = parseTurnRundown();
                    else if (nodeName.equals("daychanges"))
                        parseDayChanges();
                    else if (nodeName.equals("levels"))
                        parseLevels();
                    else if (nodeName.equals("playersnapshots"))
                        parsePlayerSnapshots();
                    else if (nodeName.equals("pulls"))
                        parsePulls();
                    else if (nodeName.equals("huntedcombats"))
                        parseHuntedCombats();
                    else if (nodeName.equals("lostcombats"))
                        parseLostCombats();

                    break;
                case XMLStreamConstants.END_ELEMENT:
                    if (parser.getLocalName().equals("ascension"))
                        break ascensionParsing;
                default:
                    break;
                }
            }
        }

        // Create the familiar and equipment change lists from the logged turns.
        final List<FamiliarChange> famChanges = Lists.newArrayList(logData.getLastTurnSpent()
                .getTurnNumber());
        final List<EquipmentChange> equipChanges = Lists.newArrayList(logData.getLastTurnSpent()
                .getTurnNumber());
        for (final SingleTurn st : logData.getTurnsSpent()) {
            famChanges.add(st.getUsedFamiliar());
            equipChanges.add(st.getUsedEquipment());
        }
        logData.setFamiliarChanges(famChanges);
        logData.setEquipmentChanges(equipChanges);

        logData.createLogSummary();

        // Setting the turn interval log notes.
        if (intervals != null)
            for (final TurnInterval ti : logData.getTurnIntervalsSpent()) {
                final TurnInterval other = intervals.get(ti.getEndTurn());

                if (other != null) {
                    ti.setPreIntervalComment(other.getPreIntervalComment());
                    ti.setPostIntervalComment(other.getPostIntervalComment());
                }
            }
    }

    private Map<Integer, TurnInterval> parseTurnRundown()
            throws XMLStreamException {
        final Map<Integer, TurnInterval> intervals = Maps.newHashMap();

        turnRundownParsing: {
            while (parser.hasNext()) {
                parser.next();
                switch (parser.getEventType()) {
                case XMLStreamConstants.START_ELEMENT:
                    if (parser.getLocalName().equals("turninterval")) {
                        final TurnInterval currentInterval = parseTurnInterval();
                        intervals.put(currentInterval.getEndTurn(), currentInterval);
                    }

                    break;
                case XMLStreamConstants.END_ELEMENT:
                    if (parser.getLocalName().equals("turnrundown"))
                        break turnRundownParsing;
                default:
                    break;
                }
            }
        }

        return intervals;
    }

    private TurnInterval parseTurnInterval()
            throws XMLStreamException {
        final List<SingleTurn> turns = Lists.newArrayList();
        String areaName = "";
        Pair<LogComment, LogComment> comments = null;
        for (int i = 0; i < parser.getAttributeCount(); i++)
            if (parser.getAttributeLocalName(i).equals("area"))
                areaName = parser.getAttributeValue(i);

        turnIntervalParsing: {
                while (parser.hasNext()) {
                    parser.next();
                    switch (parser.getEventType()) {
                    case XMLStreamConstants.START_ELEMENT:
                        if (parser.getLocalName().equals("turn"))
                            turns.add(parseTurn(areaName));
                        else if (parser.getLocalName().equals("notes"))
                            comments = parseIntervalNotes();

                        break;
                    case XMLStreamConstants.END_ELEMENT:
                        if (parser.getLocalName().equals("turninterval"))
                            break turnIntervalParsing;
                    default:
                        break;
                    }
                }
            }

        final List<SingleTurn> actualTurns = Lists.newArrayList(turns.size());
        for (final SingleTurn st : turns)
            if (!actualTurns.isEmpty()
                    && actualTurns.get(actualTurns.size() - 1).getTurnNumber() != st.getTurnNumber())
                actualTurns.add(st);

        final TurnInterval interval = new DetailedTurnInterval(actualTurns, areaName);
        if (comments != null) {
            interval.setPreIntervalComment(comments.getVar1());
            interval.setPostIntervalComment(comments.getVar2());

            // Add the notes to the actual log data
            logData.getLastTurnSpent().setNotes(comments.getVar2().toString());
        }

        return interval;
    }

    private SingleTurn parseTurn(
            final String area)
                    throws XMLStreamException {
        final List<Item> itemDrops = Lists.newArrayList();
        final List<Skill> skills = Lists.newArrayList();
        final List<Consumable> consumables = Lists.newArrayList();
        final LogComment comment = new LogComment();
        int turnNumber = 0;
        int dayNumber = 1;
        TurnVersion turnVersion = TurnVersion.NOT_DEFINED;
        String areaName = area;
        String encounterName = "";
        FamiliarChange familiar = FamiliarChange.NO_FAMILIAR;
        EquipmentChange equipment = EquipmentChange.NO_EQUIPMENT;
        Statgain statgains = Statgain.NO_STATS;
        MeatGain meatgains = MeatGain.NO_MEAT;
        MPGain mpgains = MPGain.NO_MP;
        boolean isDisintegrated = false;
        int freeRunaways = 0;
        for (int i = 0; i < parser.getAttributeCount(); i++)
            if (parser.getAttributeLocalName(i).equals("turnnumber"))
                turnNumber = Integer.parseInt(parser.getAttributeValue(i));
            else if (parser.getAttributeLocalName(i).equals("turnversion"))
                turnVersion = TurnVersion.fromString(parser.getAttributeValue(i));

        turnParsing: {
                while (parser.hasNext()) {
                    parser.next();
                    switch (parser.getEventType()) {
                    case XMLStreamConstants.START_ELEMENT:
                        final String nodeName = parser.getLocalName();

                        if (nodeName.equals("areaname")) {
                            parser.next();
                            if (parser.getEventType() == XMLStreamConstants.CHARACTERS
                                    && !parser.isWhiteSpace())
                                areaName = parser.getText();
                        } else if (nodeName.equals("encountername")) {
                            parser.next();
                            if (parser.getEventType() == XMLStreamConstants.CHARACTERS
                                    && !parser.isWhiteSpace())
                                encounterName = parser.getText();
                        } else if (nodeName.equals("day")) {
                            parser.next();
                            dayNumber = Integer.parseInt(parser.getText());
                        } else if (nodeName.equals("familiar")) {
                            parser.next();
                            final FamiliarChange oldFamiliar = familiarStack.peek().get();
                            if (!parser.getText().equals(oldFamiliar.getFamiliarName())) {
                                familiar = new FamiliarChange(parser.getText(),
                                        turnNumber > 0 ? turnNumber - 1 : 0);
                                if (oldFamiliar.getTurnNumber() != familiar.getTurnNumber())
                                    familiarStack.push(familiar);
                            } else
                                familiar = oldFamiliar;
                        } else if (nodeName.equals("equipment")) {
                            final int turnNo = turnNumber > 0 ? turnNumber - 1 : 0;
                            final EquipmentChange currentEquipment = parseEquipment(turnNo);
                            final EquipmentChange oldEquipment = equipmentStack.peek().get();
                            if (!currentEquipment.equalsIgnoreTurn(oldEquipment)) {
                                equipment = currentEquipment;
                                if (oldEquipment.getTurnNumber() != turnNo)
                                    equipmentStack.push(currentEquipment);
                            } else
                                equipment = oldEquipment;
                        } else if (nodeName.equals("statgain"))
                            statgains = parseStatgain();
                        else if (nodeName.equals("meatgain"))
                            meatgains = parseMeatgain();
                        else if (nodeName.equals("mpgain"))
                            mpgains = parseMPGain();
                        else if (nodeName.equals("disintegration")) {
                            for (int i = 0; i < parser.getAttributeCount(); i++)
                                if (parser.getAttributeLocalName(i).equals("used"))
                                    isDisintegrated = Boolean.parseBoolean(parser.getAttributeValue(i));
                        } else if (nodeName.equals("freerunaways")) {
                            parser.next();
                            freeRunaways = Integer.parseInt(parser.getText());
                        } else if (nodeName.equals("notes")) {
                            parser.next();
                            if (parser.getEventType() == XMLStreamConstants.CHARACTERS
                                    && !parser.isWhiteSpace())
                                comment.setComments(LINEBREAK_FINDER.matcher(parser.getText())
                                        .replaceAll("\n"));
                        } else if (nodeName.equals("itemdrop"))
                            itemDrops.add(parseItemdrop(turnNumber));
                        else if (nodeName.equals("skillcast"))
                            skills.add(parseSkillCast(turnNumber));
                        else if (nodeName.equals("consumable"))
                            consumables.add(parseConsumable(turnNumber));

                        break;
                    case XMLStreamConstants.END_ELEMENT:
                        if (parser.getLocalName().equals("turn"))
                            break turnParsing;
                    default:
                        break;
                    }
                }
            }

        final SingleTurn turn = new SingleTurn(areaName,
                encounterName,
                turnNumber,
                dayNumber,
                equipment,
                familiar);
        turn.setDroppedItems(itemDrops);
        turn.setSkillsCast(skills);
        turn.setConsumablesUsed(consumables);
        turn.setTurnVersion(turnVersion);
        turn.setStatGain(statgains);
        turn.setMeat(meatgains);
        turn.setMPGain(mpgains);
        turn.setDisintegrated(isDisintegrated);
        turn.setFreeRunaways(freeRunaways);
        // turn.setNotes(comment.getComments());

        logData.addTurnSpent(turn);

        return turn;
    }

    private EquipmentChange parseEquipment(
            final int turnNumber)
                    throws XMLStreamException {
        String hat = EquipmentChange.NO_EQUIPMENT_STRING;
        String weapon = EquipmentChange.NO_EQUIPMENT_STRING;
        String offhand = EquipmentChange.NO_EQUIPMENT_STRING;
        String shirt = EquipmentChange.NO_EQUIPMENT_STRING;
        String pants = EquipmentChange.NO_EQUIPMENT_STRING;
        String acc1 = EquipmentChange.NO_EQUIPMENT_STRING;
        String acc2 = EquipmentChange.NO_EQUIPMENT_STRING;
        String acc3 = EquipmentChange.NO_EQUIPMENT_STRING;
        String famEquip = EquipmentChange.NO_EQUIPMENT_STRING;

        equipmentParsing: {
            while (parser.hasNext()) {
                parser.next();
                switch (parser.getEventType()) {
                case XMLStreamConstants.START_ELEMENT:
                    final String nodeName = parser.getLocalName();

                    if (nodeName.equals("hat")) {
                        parser.next();
                        hat = parser.getText();
                    } else if (nodeName.equals("weapon")) {
                        parser.next();
                        weapon = parser.getText();
                    } else if (nodeName.equals("offhand")) {
                        parser.next();
                        offhand = parser.getText();
                    } else if (nodeName.equals("shirt")) {
                        parser.next();
                        shirt = parser.getText();
                    } else if (nodeName.equals("pants")) {
                        parser.next();
                        pants = parser.getText();
                    } else if (nodeName.equals("acc1")) {
                        parser.next();
                        acc1 = parser.getText();
                    } else if (nodeName.equals("acc2")) {
                        parser.next();
                        acc2 = parser.getText();
                    } else if (nodeName.equals("acc3")) {
                        parser.next();
                        acc3 = parser.getText();
                    } else if (nodeName.equals("famequip")) {
                        parser.next();
                        famEquip = parser.getText();
                    }

                    break;
                case XMLStreamConstants.END_ELEMENT:
                    if (parser.getLocalName().equals("equipment"))
                        break equipmentParsing;
                default:
                    break;
                }
            }
        }

        return new EquipmentChange(turnNumber,
                hat,
                weapon,
                offhand,
                shirt,
                pants,
                acc1,
                acc2,
                acc3,
                famEquip);
    }

    private MeatGain parseMeatgain()
            throws XMLStreamException {
        int insideEncounter = 0;
        int other = 0;
        int meatSpent = 0;

        meatParsing: {
            while (parser.hasNext()) {
                parser.next();
                switch (parser.getEventType()) {
                case XMLStreamConstants.START_ELEMENT:
                    if (parser.getLocalName().equals("insideencounter")) {
                        parser.next();
                        insideEncounter = Integer.parseInt(parser.getText());
                    } else if (parser.getLocalName().equals("other")) {
                        parser.next();
                        other = Integer.parseInt(parser.getText());
                    } else if (parser.getLocalName().equals("meatspent")) {
                        parser.next();
                        meatSpent = Integer.parseInt(parser.getText());
                    }

                    break;
                case XMLStreamConstants.END_ELEMENT:
                    if (parser.getLocalName().equals("meatgain"))
                        break meatParsing;
                default:
                    break;
                }
            }
        }

        final boolean isNonZero = insideEncounter != 0 || other != 0 || meatSpent != 0;

        return isNonZero ? new MeatGain(insideEncounter, other, meatSpent) : MeatGain.NO_MEAT;
    }

    private MPGain parseMPGain()
            throws XMLStreamException {
        int insideEncounter = 0;
        int starfish = 0;
        int resting = 0;
        int other = 0;
        int consumable = 0;

        mpParsing: {
            while (parser.hasNext()) {
                parser.next();
                switch (parser.getEventType()) {
                case XMLStreamConstants.START_ELEMENT:
                    if (parser.getLocalName().equals("insideencounter")) {
                        parser.next();
                        insideEncounter = Integer.parseInt(parser.getText());
                    } else if (parser.getLocalName().equals("starfish")) {
                        parser.next();
                        starfish = Integer.parseInt(parser.getText());
                    } else if (parser.getLocalName().equals("resting")) {
                        parser.next();
                        resting = Integer.parseInt(parser.getText());
                    } else if (parser.getLocalName().equals("outofencounter")) {
                        parser.next();
                        other = Integer.parseInt(parser.getText());
                    } else if (parser.getLocalName().equals("consumable")) {
                        parser.next();
                        consumable = Integer.parseInt(parser.getText());
                    }

                    break;
                case XMLStreamConstants.END_ELEMENT:
                    if (parser.getLocalName().equals("mpgain"))
                        break mpParsing;
                default:
                    break;
                }
            }
        }

        final boolean isNonZero = insideEncounter != 0 || starfish != 0 || resting != 0
                || other != 0 || consumable != 0;

        return isNonZero ? new MPGain(insideEncounter, starfish, resting, other, consumable)
        : MPGain.NO_MP;
    }

    private Item parseItemdrop(
            final int turnNumber)
                    throws XMLStreamException {
        String name = "";
        int amount = 1;
        for (int i = 0; i < parser.getAttributeCount(); i++)
            if (parser.getAttributeLocalName(i).equals("amount"))
                amount = Integer.parseInt(parser.getAttributeValue(i));

        itemParsing: {
                while (parser.hasNext()) {
                    parser.next();
                    switch (parser.getEventType()) {
                    case XMLStreamConstants.START_ELEMENT:
                        if (parser.getLocalName().equals("name")) {
                            parser.next();
                            name = parser.getText();
                        }

                        break;
                    case XMLStreamConstants.END_ELEMENT:
                        if (parser.getLocalName().equals("itemdrop"))
                            break itemParsing;
                    default:
                        break;
                    }
                }
            }

        return new Item(name, amount, turnNumber);
    }

    private Skill parseSkillCast(
            final int turnNumber)
                    throws XMLStreamException {
        String name = "";
        int amount = 1;
        int mpCost = 1;
        for (int i = 0; i < parser.getAttributeCount(); i++)
            if (parser.getAttributeLocalName(i).equals("amount"))
                amount = Integer.parseInt(parser.getAttributeValue(i));
            else if (parser.getAttributeLocalName(i).equals("mpcost"))
                mpCost = Integer.parseInt(parser.getAttributeValue(i));

        skillParsing: {
                while (parser.hasNext()) {
                    parser.next();
                    switch (parser.getEventType()) {
                    case XMLStreamConstants.START_ELEMENT:
                        if (parser.getLocalName().equals("name")) {
                            parser.next();
                            name = parser.getText();
                        }

                        break;
                    case XMLStreamConstants.END_ELEMENT:
                        if (parser.getLocalName().equals("skillcast"))
                            break skillParsing;
                    default:
                        break;
                    }
                }
            }

        final Skill skill = new Skill(name, turnNumber);
        skill.setCasts(amount, 0);
        skill.setMpCost(mpCost);

        return skill;
    }

    private Consumable parseConsumable(
            final int turnNumber)
                    throws XMLStreamException {
        String name = "";
        int amount = 1;
        int adventureGain = 0;
        int consumedOnDay = 1;
        Statgain stats = Statgain.NO_STATS;
        ConsumableVersion consumableVersion = ConsumableVersion.OTHER;
        for (int i = 0; i < parser.getAttributeCount(); i++)
            if (parser.getAttributeLocalName(i).equals("amount"))
                amount = Integer.parseInt(parser.getAttributeValue(i));
            else if (parser.getAttributeLocalName(i).equals("version"))
                consumableVersion = ConsumableVersion.fromString(parser.getAttributeValue(i));

        consumableParsing: {
                while (parser.hasNext()) {
                    parser.next();
                    switch (parser.getEventType()) {
                    case XMLStreamConstants.START_ELEMENT:
                        if (parser.getLocalName().equals("name")) {
                            parser.next();
                            name = parser.getText();
                        } else if (parser.getLocalName().equals("adventuregain")) {
                            parser.next();
                            adventureGain = Integer.parseInt(parser.getText());
                        } else if (parser.getLocalName().equals("consumedonday")) {
                            parser.next();
                            consumedOnDay = Integer.parseInt(parser.getText());
                        } else if (parser.getLocalName().equals("statgain"))
                            stats = parseStatgain();

                        break;
                    case XMLStreamConstants.END_ELEMENT:
                        if (parser.getLocalName().equals("consumable"))
                            break consumableParsing;
                    default:
                        break;
                    }
                }
            }

        final Consumable consumable;
        switch (consumableVersion) {
        case FOOD:
            consumable = Consumable.newFoodConsumable(name, adventureGain, amount, turnNumber);
            break;
        case BOOZE:
            consumable = Consumable.newBoozeConsumable(name, adventureGain, amount, turnNumber);
            break;
        case SPLEEN:
            consumable = Consumable.newSpleenConsumable(name, adventureGain, amount, turnNumber);
            break;
        default:
            consumable = Consumable.newOtherConsumable(name, adventureGain, amount, turnNumber);
            break;
        }
        consumable.setDayNumberOfUsage(consumedOnDay);
        consumable.setStatGain(stats);

        return consumable;
    }

    private Pair<LogComment, LogComment> parseIntervalNotes()
            throws XMLStreamException {
        final LogComment preComment = new LogComment();
        final LogComment postComment = new LogComment();

        notesParsing: {
            while (parser.hasNext()) {
                parser.next();
                switch (parser.getEventType()) {
                case XMLStreamConstants.START_ELEMENT:
                    if (parser.getLocalName().equals("preintervalnotes")) {
                        parser.next();
                        if (parser.getEventType() == XMLStreamConstants.CHARACTERS
                                && !parser.isWhiteSpace())
                            preComment.setComments(LINEBREAK_FINDER.matcher(parser.getText())
                                    .replaceAll("\n"));
                    } else if (parser.getLocalName().equals("postintervalnotes")) {
                        parser.next();
                        if (parser.getEventType() == XMLStreamConstants.CHARACTERS
                                && !parser.isWhiteSpace())
                            postComment.setComments(LINEBREAK_FINDER.matcher(parser.getText())
                                    .replaceAll("\n"));
                    }

                    break;
                case XMLStreamConstants.END_ELEMENT:
                    if (parser.getLocalName().equals("notes"))
                        break notesParsing;
                default:
                    break;
                }
            }
        }

        return Pair.of(preComment, postComment);
    }

    private void parseDayChanges()
            throws XMLStreamException {
        while (parser.hasNext()) {
            parser.next();
            switch (parser.getEventType()) {
            case XMLStreamConstants.START_ELEMENT:
                if (parser.getLocalName().equals("day")) {
                    final Pair<DayChange, HeaderFooterComment> currentDay = parseDayChange();
                    logData.addDayChange(currentDay.getVar1());

                    final HeaderFooterComment dayComments = logData.getHeaderFooterComment(currentDay.getVar1());
                    dayComments.setHeaderComments(currentDay.getVar2().getHeaderComments());
                    dayComments.setFooterComments(currentDay.getVar2().getFooterComments());
                }

                break;
            case XMLStreamConstants.END_ELEMENT:
                if (parser.getLocalName().equals("daychanges"))
                    return;
            default:
                break;
            }
        }
    }

    private Pair<DayChange, HeaderFooterComment> parseDayChange()
            throws XMLStreamException {
        int dayNumber = 1;
        int turnNumber = 0;
        final HeaderFooterComment comment = new HeaderFooterComment();

        for (int i = 0; i < parser.getAttributeCount(); i++)
            if (parser.getAttributeLocalName(i).equals("daynumber"))
                dayNumber = Integer.parseInt(parser.getAttributeValue(i));

        dayNodeParsing: {
                while (parser.hasNext()) {
                    parser.next();
                    switch (parser.getEventType()) {
                    case XMLStreamConstants.START_ELEMENT:
                        final String nodeName = parser.getLocalName();

                        if (nodeName.equals("turnwhenreached")) {
                            parser.next();
                            turnNumber = Integer.parseInt(parser.getText());
                        } else if (nodeName.equals("headernotes")) {
                            parser.next();
                            if (parser.getEventType() == XMLStreamConstants.CHARACTERS
                                    && !parser.isWhiteSpace())
                                comment.setHeaderComments(LINEBREAK_FINDER.matcher(parser.getText())
                                        .replaceAll("\n"));
                        } else if (nodeName.equals("footernotes")) {
                            parser.next();
                            if (parser.getEventType() == XMLStreamConstants.CHARACTERS
                                    && !parser.isWhiteSpace())
                                comment.setFooterComments(LINEBREAK_FINDER.matcher(parser.getText())
                                        .replaceAll("\n"));
                        }

                        break;
                    case XMLStreamConstants.END_ELEMENT:
                        if (parser.getLocalName().equals("day"))
                            break dayNodeParsing;
                    default:
                        break;
                    }
                }
            }

        return Pair.of(new DayChange(dayNumber, turnNumber), comment);
    }

    private void parseLevels()
            throws XMLStreamException {
        while (parser.hasNext()) {
            parser.next();
            switch (parser.getEventType()) {
            case XMLStreamConstants.START_ELEMENT:
                if (parser.getLocalName().equals("level"))
                    logData.addLevel(parseLevel());

                break;
            case XMLStreamConstants.END_ELEMENT:
                if (parser.getLocalName().equals("levels"))
                    return;
            default:
                break;
            }
        }
    }

    private LevelData parseLevel()
            throws XMLStreamException {
        int levelNumber = 1;
        int turnNumber = 0;
        for (int i = 0; i < parser.getAttributeCount(); i++)
            if (parser.getAttributeLocalName(i).equals("levelnumber"))
                levelNumber = Integer.parseInt(parser.getAttributeValue(i));
            else if (parser.getAttributeLocalName(i).equals("onturn"))
                turnNumber = Integer.parseInt(parser.getAttributeValue(i));

        final LevelData level = new LevelData(levelNumber, turnNumber);

        levelNodeParsing: {
            while (parser.hasNext()) {
                parser.next();
                switch (parser.getEventType()) {
                case XMLStreamConstants.START_ELEMENT:
                    final String nodeName = parser.getLocalName();

                    if (nodeName.equals("combatturns")) {
                        parser.next();
                        level.setCombatTurns(Integer.parseInt(parser.getText()));
                    } else if (nodeName.equals("noncombatturns")) {
                        parser.next();
                        level.setNoncombatTurns(Integer.parseInt(parser.getText()));
                    } else if (nodeName.equals("otherturns")) {
                        parser.next();
                        level.setOtherTurns(Integer.parseInt(parser.getText()));
                    } else if (nodeName.equals("mainstatgainperturn")) {
                        parser.next();
                        level.setStatGainPerTurn(Double.parseDouble(parser.getText()));
                    } else if (nodeName.equals("statswhenreached"))
                        level.setStatsAtLevelReached(parseStatgain());

                    break;
                case XMLStreamConstants.END_ELEMENT:
                    if (parser.getLocalName().equals("level"))
                        break levelNodeParsing;
                default:
                    break;
                }
            }
        }

        return level;
    }

    private void parsePlayerSnapshots()
            throws XMLStreamException {
        while (parser.hasNext()) {
            parser.next();
            switch (parser.getEventType()) {
            case XMLStreamConstants.START_ELEMENT:
                if (parser.getLocalName().equals("playersnapshot"))
                    logData.addPlayerSnapshot(parsePlayerSnapshot());

                break;
            case XMLStreamConstants.END_ELEMENT:
                if (parser.getLocalName().equals("playersnapshots"))
                    return;
            default:
                break;
            }
        }
    }

    private PlayerSnapshot parsePlayerSnapshot()
            throws XMLStreamException {
        int turnNumber = 0;
        int adventuresLeft = 0;
        int currentMeat = 0;
        Statgain stats = Statgain.NO_STATS;
        for (int i = 0; i < parser.getAttributeCount(); i++)
            if (parser.getAttributeLocalName(i).equals("onturn"))
                turnNumber = Integer.parseInt(parser.getAttributeValue(i));

        snapshotNodeParsing: {
                while (parser.hasNext()) {
                    parser.next();
                    switch (parser.getEventType()) {
                    case XMLStreamConstants.START_ELEMENT:
                        final String nodeName = parser.getLocalName();

                        if (nodeName.equals("adventuresleft")) {
                            parser.next();
                            adventuresLeft = Integer.parseInt(parser.getText());
                        } else if (nodeName.equals("currentmeat")) {
                            parser.next();
                            currentMeat = Integer.parseInt(parser.getText());
                        } else if (nodeName.equals("stats"))
                            stats = parseStatgain();

                        break;
                    case XMLStreamConstants.END_ELEMENT:
                        if (parser.getLocalName().equals("playersnapshot"))
                            break snapshotNodeParsing;
                    default:
                        break;
                    }
                }
            }

        return new PlayerSnapshot(stats, adventuresLeft, currentMeat, turnNumber);
    }

    private void parsePulls()
            throws XMLStreamException {
        while (parser.hasNext()) {
            parser.next();
            switch (parser.getEventType()) {
            case XMLStreamConstants.START_ELEMENT:
                if (parser.getLocalName().equals("pull"))
                    logData.addPull(parsePull());

                break;
            case XMLStreamConstants.END_ELEMENT:
                if (parser.getLocalName().equals("pulls"))
                    return;
            default:
                break;
            }
        }
    }

    private Pull parsePull()
            throws XMLStreamException {
        int dayNumber = 0;
        int turnNumber = 0;
        int amount = 1;
        String itemName = "";
        for (int i = 0; i < parser.getAttributeCount(); i++)
            if (parser.getAttributeLocalName(i).equals("daynumber"))
                dayNumber = Integer.parseInt(parser.getAttributeValue(i));
            else if (parser.getAttributeLocalName(i).equals("onturn"))
                turnNumber = Integer.parseInt(parser.getAttributeValue(i));

        pullNodeParsing: {
                while (parser.hasNext()) {
                    parser.next();
                    switch (parser.getEventType()) {
                    case XMLStreamConstants.START_ELEMENT:
                        final String nodeName = parser.getLocalName();

                        if (nodeName.equals("amount")) {
                            parser.next();
                            amount = Integer.parseInt(parser.getText());
                        } else if (nodeName.equals("itemname")) {
                            parser.next();
                            itemName = parser.getText();
                        }

                        break;
                    case XMLStreamConstants.END_ELEMENT:
                        if (parser.getLocalName().equals("pull"))
                            break pullNodeParsing;
                    default:
                        break;
                    }
                }
            }

        return new Pull(itemName, amount, turnNumber, dayNumber);
    }

    private void parseHuntedCombats()
            throws XMLStreamException {
        while (parser.hasNext()) {
            parser.next();
            switch (parser.getEventType()) {
            case XMLStreamConstants.START_ELEMENT:
                if (parser.getLocalName().equals("combat"))
                    logData.addHuntedCombat(parseCombat());

                break;
            case XMLStreamConstants.END_ELEMENT:
                if (parser.getLocalName().equals("huntedcombats"))
                    return;
            default:
                break;
            }
        }
    }

    private void parseLostCombats()
            throws XMLStreamException {
        while (parser.hasNext()) {
            parser.next();
            switch (parser.getEventType()) {
            case XMLStreamConstants.START_ELEMENT:
                if (parser.getLocalName().equals("combat"))
                    logData.addLostCombat(parseCombat());

                break;
            case XMLStreamConstants.END_ELEMENT:
                if (parser.getLocalName().equals("lostcombats"))
                    return;
            default:
                break;
            }
        }
    }

    private Statgain parseStatgain() {
        int muscle = 0;
        int myst = 0;
        int moxie = 0;
        for (int i = 0; i < parser.getAttributeCount(); i++)
            if (parser.getAttributeLocalName(i).equals("muscle"))
                muscle = Integer.parseInt(parser.getAttributeValue(i));
            else if (parser.getAttributeLocalName(i).equals("myst"))
                myst = Integer.parseInt(parser.getAttributeValue(i));
            else if (parser.getAttributeLocalName(i).equals("moxie"))
                moxie = Integer.parseInt(parser.getAttributeValue(i));

        final boolean isNonZero = muscle != 0 || myst != 0 || moxie != 0;

        return isNonZero ? new Statgain(muscle, myst, moxie) : Statgain.NO_STATS;
    }

    private DataNumberPair<String> parseCombat() {
        String name = "";
        int turnNumber = 0;
        for (int i = 0; i < parser.getAttributeCount(); i++)
            if (parser.getAttributeLocalName(i).equals("name"))
                name = parser.getAttributeValue(i);
            else if (parser.getAttributeLocalName(i).equals("onturn"))
                turnNumber = Integer.parseInt(parser.getAttributeValue(i));

        return DataNumberPair.of(name, turnNumber);
    }
}
