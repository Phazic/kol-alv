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

package com.googlecode.alv.logData.logSummary;

import java.util.Collection;

import com.googlecode.alv.logData.Item;
import com.googlecode.alv.logData.turn.TurnInterval;

/**
 * This immutable class calculates and holds all the data on various quest
 * turncounts.
 */
public final class QuestTurncounts {
    public final int mosquitoQuestTurns;

    public final int templeOpeningTurns;

    public final int tavernQuestTurns;

    public final int batQuestTurns;

    public final int knobQuestTurns;

    public final int friarsQuestTurns;

    public final int pandamoniumQuestTurns;

    public final int cyrptQuestTurns;

    public final int trapzorQuestTurns;

    public final int chasmQuestTurns;

    public final int airshipQuestTurns;

    public final int castleQuestTurns;

    public final int spookyravenFirstFloor;

    public final int spookyravenSecondFloor;
    
    public final int copperheadClubTurns;
    
    public final int redZeppelinTurns;

    public final int pirateQuestTurns;

    public final int blackForrestQuestTurns;

    public final int desertOasisQuestTurns;

    public final int spookyravenQuestTurns;

    public final int templeCityQuestTurns;

    public final int palindomeQuestTurns;

    public final int pyramidQuestTurns;

    public final int warIslandOpeningTurns;

    public final int warIslandQuestTurns;

    public final int dodQuestTurns;

    public final int dailyDungeonTurns;

    public final int nsTurns;

    /**
     * Constructs a new instance and calculates all quest turncounts.
     *
     * @param turns
     *            The turn rundown of the ascension.
     * @param droppedItems
     *            All dropped items during the ascension.
     */
    public QuestTurncounts(final Collection<TurnInterval> turns, 
                           final Collection<Item> droppedItems) 
    {
        if (turns == null)
            throw new NullPointerException("Turn rundown set must not be null.");
        if (droppedItems == null)
            throw new NullPointerException("Dropped items list must not be null.");

        mosquitoQuestTurns = getTurnsUntilItemFound("The Spooky Forest",
                "mosquito larva",
                turns,
                droppedItems);

        templeOpeningTurns = getTurnsInLocation("The Spooky Forest", turns);

        tavernQuestTurns = getTurnsInLocation("Tavern Cellar", turns)
                + getTurnsInLocation("The Typical Tavern Cellar (row 1, col 1)", turns)
                + getTurnsInLocation("The Typical Tavern Cellar (row 1, col 2)", turns)
                + getTurnsInLocation("The Typical Tavern Cellar (row 1, col 3)", turns)
                + getTurnsInLocation("The Typical Tavern Cellar (row 1, col 4)", turns)
                + getTurnsInLocation("The Typical Tavern Cellar (row 1, col 5)", turns)
                + getTurnsInLocation("The Typical Tavern Cellar (row 2, col 1)", turns)
                + getTurnsInLocation("The Typical Tavern Cellar (row 2, col 2)", turns)
                + getTurnsInLocation("The Typical Tavern Cellar (row 2, col 3)", turns)
                + getTurnsInLocation("The Typical Tavern Cellar (row 2, col 4)", turns)
                + getTurnsInLocation("The Typical Tavern Cellar (row 2, col 5)", turns)
                + getTurnsInLocation("The Typical Tavern Cellar (row 3, col 1)", turns)
                + getTurnsInLocation("The Typical Tavern Cellar (row 3, col 2)", turns)
                + getTurnsInLocation("The Typical Tavern Cellar (row 3, col 3)", turns)
                + getTurnsInLocation("The Typical Tavern Cellar (row 3, col 4)", turns)
                + getTurnsInLocation("The Typical Tavern Cellar (row 3, col 5)", turns)
                + getTurnsInLocation("The Typical Tavern Cellar (row 4, col 1)", turns)
                + getTurnsInLocation("The Typical Tavern Cellar (row 4, col 2)", turns)
                + getTurnsInLocation("The Typical Tavern Cellar (row 4, col 3)", turns)
                + getTurnsInLocation("The Typical Tavern Cellar (row 4, col 4)", turns)
                + getTurnsInLocation("The Typical Tavern Cellar (row 4, col 5)", turns)
                + getTurnsInLocation("The Typical Tavern Cellar (row 5, col 1)", turns)
                + getTurnsInLocation("The Typical Tavern Cellar (row 5, col 2)", turns)
                + getTurnsInLocation("The Typical Tavern Cellar (row 5, col 3)", turns)
                + getTurnsInLocation("The Typical Tavern Cellar (row 5, col 4)", turns)
                + getTurnsInLocation("The Typical Tavern Cellar (row 5, col 5)", turns);

        batQuestTurns = getTurnsInLocation("The Bat Hole Entryway", turns)
                + getTurnsInLocation("The Guano Junction", turns)
                + getTurnsInLocation("The Beanbat Chamber", turns)
                + getTurnsInLocation("The Batrat and Ratbat Burrow", turns)
                + getTurnsInLocation("The Boss Bat's Lair", turns);

        knobQuestTurns = getTurnsInLocation("The Outskirts of Cobb's Knob",turns)
                + getTurnsInLocation("Cobb's Knob Harem", turns)
                + getTurnsInLocation("Cobb's Knob Barracks", turns)
                + getTurnsInLocation("Cobb's Knob Kitchens",turns)
                + getTurnsInLocation("Throne Room", turns);

        friarsQuestTurns = getTurnsUntilItemFound("The Dark Neck of the Woods",
                "dodecagram",
                turns,
                droppedItems)
                + getTurnsUntilItemFound("The Dark Heart of the Woods",
                        "box of birthday candles",
                        turns,
                        droppedItems)
                        + getTurnsUntilItemFound("The Dark Elbow of the Woods",
                                "eldritch butterknife",
                                turns,
                                droppedItems);

        pandamoniumQuestTurns = getTurnsInLocation("Hey Deze Arena", turns)
                + getTurnsInLocation("Belilafs Comedy Club", turns);

        cyrptQuestTurns = getTurnsInLocation("The Defiled Cranny", turns)
                + getTurnsInLocation("The Defiled Nook", turns)
                + getTurnsInLocation("The Defiled Alcove", turns)
                + getTurnsInLocation("The Defiled Niche", turns)
                + getTurnsInLocation("Haert of the Cyrpt", turns);

        trapzorQuestTurns = getTurnsInLocation("Itznotyerzitz Mine", turns)
                + getTurnsInLocation("Mining (In Disguise)", turns)
                + getTurnsInLocation("Itznotyerzitz Mine (in Disguise)", turns)
                + getTurnsInLocation("The Goatlet", turns)
                + getTurnsInLocation("Lair of the Ninja Snowmen", turns)
                + getTurnsInLocation("The eXtreme Slope", turns)
                + getTurnsInLocation("Mist-Shrouded Peak", turns);

        chasmQuestTurns = getTurnsInLocation("Smut Orc Logging Camp", turns)
                + getTurnsInLocation("A-Boo Peak", turns)
                + getTurnsInLocation("Oil Peak", turns)
                + getTurnsInLocation("Twin Peak", turns);

        airshipQuestTurns = getTurnsUntilItemFound("The Penultimate Fantasy Airship",
                "S.O.C.K.",
                turns,
                droppedItems);

        castleQuestTurns = getTurnsInLocation("The Castle in the Clouds in the Sky (Basement)", turns)
                + getTurnsInLocation("The Castle in the Clouds in the Sky (Ground Floor)", turns)
                + getTurnsInLocation("The Castle in the Clouds in the Sky (Top Floor)", turns);

        spookyravenFirstFloor = getTurnsInLocation("The Haunted Kitchen", turns)
                + getTurnsInLocation("The Haunted Billiards Room", turns)
                + getTurnsInLocation("The Haunted Library", turns);

        spookyravenSecondFloor = getTurnsInLocation("The Haunted Bathroom", turns)
                + getTurnsInLocation("The Haunted Bedroom", turns)
                + getTurnsInLocation("The Haunted Gallery", turns)
                + getTurnsInLocation("The Haunted Ballroom", turns);

        pirateQuestTurns = getTurnsInLocation("The Obligatory Pirate's Cove", turns)
                + getTurnsInLocation("Barrrney's Barrr", turns)
                + getTurnsInLocation("Cap'm Caronch's Map", turns)
                + getTurnsInLocation("The Infiltrationist", turns)
                + getTurnsInLocation("The F'c'le", turns)
                + getTurnsInLocation("The Poop Deck", turns)
                + getTurnsInLocation("Belowdecks", turns);

        copperheadClubTurns = getTurnsInLocation("The Copperhead Club", turns);
        
        redZeppelinTurns = getTurnsInLocation("A Mob of Zeppelin Protesters", turns)
                + getTurnsInLocation("The Red Zeppelin", turns);
        
        blackForrestQuestTurns = getTurnsInLocation("The Black Forest", turns)
                + getTurnsInLocation("Wu Tang the Betrayer", turns);

        desertOasisQuestTurns = getTurnsInLocation("The Arid, Extra-Dry Desert", turns)
                + getTurnsInLocation("The Oasis", turns);

        spookyravenQuestTurns = getTurnsInLocation("The Haunted Wine Cellar", turns)
                + getTurnsInLocation("The Haunted Laundry Room", turns)
                + getTurnsInLocation("The Haunted Boiler Room", turns)
                + getTurnsInLocation("Lord Spookyraven", turns);

        templeCityQuestTurns = getTurnsInLocation("The Hidden Temple", turns) + 2
                + getTurnsInLocation("An Overgrown Shrine (Northwest)", turns)
                + getTurnsInLocation("An Overgrown Shrine (Northeast)", turns)
                + getTurnsInLocation("An Overgrown Shrine (Southeast)", turns)
                + getTurnsInLocation("An Overgrown Shrine (Southwest)", turns)
                + getTurnsInLocation("The Hidden Office Building", turns)
                + getTurnsInLocation("The Hidden Apartment Building", turns)
                + getTurnsInLocation("The Hidden Hospital", turns)
                + getTurnsInLocation("The Hidden Bowling Alley", turns)
                + getTurnsInLocation("A Massive Ziggurat", turns)
                + getTurnsInLocation("The Hidden Park", turns);

        palindomeQuestTurns = getTurnsInLocation("Inside the Palindome", turns)
                + getTurnsInLocation("Whitey's Grove", turns) + 1;

        pyramidQuestTurns = getTurnsInLocation("The Upper Chamber", turns)
                + getTurnsInLocation("The Middle Chamber", turns)
                + getTurnsInLocation("The Lower Chamber", turns)
                + getTurnsInLocation("The Lower Chambers (Token/Empty)", turns)
                + getTurnsInLocation("The Lower Chambers (Rubble/Bomb)", turns)
                + getTurnsInLocation("The Lower Chambers (Empty/Empty/Ed's Chamber)", turns)
                + getTurnsInLocation("The Lower Chambers (Empty/Rubble)", turns)
                + getTurnsInLocation("Ed the Undying", turns);

        warIslandOpeningTurns = getTurnsInLocation("Hippy Camp", turns)
                + getTurnsInLocation("Wartime Hippy Camp (Hippy Disguise)", turns)
                + getTurnsInLocation("Wartime Hippy Camp (Frat Disguise)", turns)
                + getTurnsInLocation("Frat House", turns)
                + getTurnsInLocation("Wartime Frat House (Hippy Disguise)", turns)
                + getTurnsInLocation("Wartime Frat House (Frat Disguise)", turns);

        warIslandQuestTurns = getTurnsInLocation("The Hatching Chamber", turns)
                + getTurnsInLocation("The Feeding Chamber", turns)
                + getTurnsInLocation("The Royal Guard Chamber", turns)
                + getTurnsInLocation("The Filthworm Queen's Chamber", turns)
                + getTurnsInLocation("Next to that Barrel with Something Burning in it", turns)
                + getTurnsInLocation("Over Where the Old Tires Are", turns)
                + getTurnsInLocation("Near an Abandoned Refrigerator", turns)
                + getTurnsInLocation("Out by that Rusted-Out Car", turns)
                + getTurnsInLocation("Sonofa Beach", turns)
                + getTurnsInLocation("The Themthar Hills", turns)
                + getTurnsInLocation("The Barn", turns)
                + getTurnsInLocation("The Family Plot", turns)
                + getTurnsInLocation("The Pond", turns)
                + getTurnsInLocation("The Other Back 40", turns)
                + getTurnsInLocation("The Back 40", turns)
                + getTurnsInLocation("The Granary", turns)
                + getTurnsInLocation("The Bog", turns)
                + getTurnsInLocation("The Shady Thicket", turns)
                + getTurnsInLocation("The Battlefield (Frat Uniform)", turns)
                + getTurnsInLocation("The Battlefield (Hippy Uniform)", turns)
                + getTurnsInLocation("The Big Wisniewski", turns)
                + getTurnsInLocation("The Man", turns);

        dodQuestTurns = getTurnsInLocation("The Enormous Greater-Than Sign", turns)
                + getTurnsUntilItemFound("The Dungeons of Doom", "dead mimic", turns, droppedItems);

        dailyDungeonTurns = getTurnsInLocation("The Daily Dungeon", turns);

        nsTurns = getTurnsInLocation("Fastest Adventurer Contest", turns)
                + getTurnsInLocation("Smartest Adventurer Contest", turns)
                + getTurnsInLocation("Strongest Adventurer Contest", turns)
                + getTurnsInLocation("Smoothest Adventurer Contest", turns)
                + getTurnsInLocation("Hottest Adventurer Contest", turns)
                + getTurnsInLocation("Sleaziest Adventurer Contest", turns)
                + getTurnsInLocation("Spookiest Adventurer Contest", turns)
                + getTurnsInLocation("Coldest Adventurer Contest", turns)
                + getTurnsInLocation("Stinkiest Adventurer Contest", turns)
                + getTurnsInLocation("The Hedge Maze (Room 1)", turns)
                + getTurnsInLocation("The Hedge Maze (Room 2)", turns)
                + getTurnsInLocation("The Hedge Maze (Room 3)", turns)
                + getTurnsInLocation("The Hedge Maze (Room 4)", turns)
                + getTurnsInLocation("The Hedge Maze (Room 5)", turns)
                + getTurnsInLocation("The Hedge Maze (Room 6)", turns)
                + getTurnsInLocation("The Hedge Maze (Room 7)", turns)
                + getTurnsInLocation("The Hedge Maze (Room 8)", turns)
                + getTurnsInLocation("Tower Level 1", turns)
                + getTurnsInLocation("Tower Level 2", turns)
                + getTurnsInLocation("Tower Level 3", turns)
                + getTurnsInLocation("Tower Level 4", turns)
                + getTurnsInLocation("Tower Level 5", turns)
                + getTurnsInLocation("The Naughty Sorceress' Chamber", turns);
    }

    /**
     * @param turns
     *            The turn rundown of the ascension.
     * @param droppedItems
     *            All dropped items during the ascension.
     * @return Turns spent.
     */
    /*private int calculateTempleOpening(final Collection<TurnInterval> turns,
                                       final Collection<Item> droppedItems) 
    {
        final int templeMap = getTurnsUntilItemFound("The Spooky Forest",
                "Spooky Temple map",
                turns,
                droppedItems);
        final int spookyGro = getTurnsUntilItemFound("The Spooky Forest",
                "Spooky-Gro fertilizer",
                turns,
                droppedItems);
        final int sapling = getTurnsUntilItemFound("The Spooky Forest",
                "spooky sapling",
                turns,
                droppedItems);
        final int coin = getTurnsUntilItemFound("The Spooky Forest",
                "tree-holed coin",
                turns,
                droppedItems);
        final int mosquito = getTurnsUntilItemFound("The Spooky Forest",
                "mosquito larva",
                turns,
                droppedItems);

        return Math.max(mosquito, Math.max(coin, Math.max(templeMap, Math.max(spookyGro, sapling))));
    }*/

    /**
     * @param areaName
     *            The name of the area the turns are spent in.
     * @param turns
     *            The turn rundown of the ascension.
     * @return Turns spent.
     */
    private int getTurnsInLocation(final String areaName, final Collection<TurnInterval> turns) 
    {
        int turnsSpent = 0;

        for (final TurnInterval ti : turns)
            if (ti.getAreaName().equals(areaName))
                turnsSpent += ti.getTotalTurns();

        return turnsSpent;
    }

    /**
     * @param areaName
     *            The name of the area the turns are spent in.
     * @param openedLocation
     *            The name of the area whose opening stops the turncounting.
     * @param turns
     *            The turn rundown of the ascension.
     * @return Turns spent.
     */
    /*private int getTurnsUntilOtherLocationOpen(final String areaName, 
                                               final String openedLocation,
                                               final Collection<TurnInterval> turns) 
    {
        int turnsSpent = 0;
        int firstTurnInOpenedLocation = Integer.MAX_VALUE;
        for (final TurnInterval ti : turns)
            if (ti.getAreaName().equals(openedLocation)) {
                firstTurnInOpenedLocation = ti.getStartTurn();
                break;
            }

        for (final TurnInterval ti : turns)
            if (ti.getStartTurn() >= firstTurnInOpenedLocation)
                break;
            else if (ti.getAreaName().equals(areaName))
                turnsSpent += ti.getTotalTurns();

        return turnsSpent;
    }*/

    /**
     * @param areaName
     *            The name of the area the turns are spent in.
     * @param alreadyOpenLocation
     *            The name of the area which has to be already open before the
     *            turncounting starts.
     * @param turns
     *            The turn rundown of the ascension.
     * @return Turns spent.
     */
    /*private int getTurnsAfterLocationOpen(final String areaName, 
                                          final String alreadyOpenLocation,
                                          final Collection<TurnInterval> turns) 
    {
        return getTurnsAfterLocationOpenUntilOtherLocationOpen(areaName,
                alreadyOpenLocation,
                "",
                turns);
    }*/

    /**
     * @param areaName
     *            The name of the area the turns are spent in.
     * @param alreadyOpenLocation
     *            The name of the area which has to be already open before the
     *            turncounting starts.
     * @param toBeOpenedLocation
     *            The name of the area whose opening stops the turncounting.
     * @param turns
     *            The turn rundown of the ascension.
     * @return Turns spent.
     */
    /*private int getTurnsAfterLocationOpenUntilOtherLocationOpen(final String areaName,
                                                                final String alreadyOpenLocation,
                                                                final String toBeOpenedLocation,
                                                                final Collection<TurnInterval> turns) 
    {
        int turnsSpent = 0;
        int firstTurnInAlreadyOpenedLocation = Integer.MIN_VALUE;
        int firstTurnInToBeOpenedLocation = Integer.MAX_VALUE;

        for (final TurnInterval ti : turns)
            if (firstTurnInAlreadyOpenedLocation == Integer.MIN_VALUE
            && ti.getAreaName().equals(alreadyOpenLocation))
                firstTurnInAlreadyOpenedLocation = ti.getStartTurn();
            else if (ti.getAreaName().equals(toBeOpenedLocation)) {
                firstTurnInToBeOpenedLocation = ti.getStartTurn();
                break;
            }

        for (final TurnInterval ti : turns)
            if (ti.getStartTurn() >= firstTurnInAlreadyOpenedLocation)
                if (ti.getStartTurn() >= firstTurnInToBeOpenedLocation)
                    break;
                else if (ti.getAreaName().equals(areaName))
                    turnsSpent += ti.getTotalTurns();

        return turnsSpent;
    }*/

    /**
     * @param areaName
     *            The name of the area the turns are spent in.
     * @param itemName
     *            The name of the item which has to be found.
     * @param turns
     *            The turn rundown of the ascension.
     * @param droppedItems
     *            All dropped items during the ascension.
     * @return Turns spent.
     */
    private int getTurnsUntilItemFound(final String areaName, 
                                       final String itemName,
                                       final Collection<TurnInterval> turns,
                                       final Collection<Item> droppedItems) 
    {
        int turnsSpent = 0;
        int finishedOnTurn = Integer.MAX_VALUE;
        for (final Item i : droppedItems)
            if (i.getName().equals(itemName)) {
                finishedOnTurn = i.getFoundOnTurn();
                break;
            }

        for (final TurnInterval ti : turns)
            if (ti.getAreaName().equals(areaName))
                if (ti.getStartTurn() <= finishedOnTurn && ti.getEndTurn() <= finishedOnTurn)
                    turnsSpent += ti.getTotalTurns();
                else if (ti.getStartTurn() <= finishedOnTurn && ti.getEndTurn() > finishedOnTurn) {
                    turnsSpent += finishedOnTurn - ti.getStartTurn();
                    break;
                }

        return turnsSpent;
    }
}
