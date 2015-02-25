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

package com.googlecode.logVisualizer.parser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.googlecode.logVisualizer.parser.mafiaLogBlockParsers.HybridDataBlockParser;
import com.googlecode.logVisualizer.util.Lists;
import com.googlecode.logVisualizer.util.Sets;

/**
 * This class can read mafia session logs and return them to the caller in nice
 * and easier to handle chunks.
 */
public final class MafiaSessionLogReader {
    public static final Set<String> BROKEN_AREAS_ENCOUNTER_SET = Sets.immutableSetOf("Encounter: Big Wisniewski",
            "Encounter: The Big Wisniewski",
            "Encounter: The Man",
            "Encounter: Lord Spookyraven",
            "Encounter: Ed the Undying",
            "Encounter: The Infiltrationist",
            "Encounter: giant sandworm",
            "Encounter: Wu Tang the Betrayer");

    private static final String ENCOUNTER_START_STRING = "Encounter: ";

    private static final String FAMILIAR_POUND_GAIN_END_STRING = "gains a pound!";

    private static final String USE_STRING = "use";

    private static final String EAT_STRING = "eat";

    private static final String DRINK_STRING = "drink";
    
    private static final String SPLEEN_STRING = "chew";

    private static final String BUY_STRING = "Buy";

    private static final String SNAPSHOT_START_END = "=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=";

    private static final String PLAYER_SNAPSHOT_STRING = "Player Snapshot";

    private static final String ASCENSION_DATA_START_STRING = "Ascension #";

    private static final String LEVEL_12_QUEST_BOSSFIGHT_BEGINNING_STRING = "bigisland.php?";

    private final BufferedReader log;

    private boolean hasNext = true;

    // The standard constructor should not be accessible.
    @SuppressWarnings("unused")
    private MafiaSessionLogReader() {
        log = null;
    }

    /**
     * @param log
     *            The condensed mafia session log that is supposed to be parsed.
     * @throws IOException
     *             if there were issues with accessing the log
     */
    MafiaSessionLogReader(
            final File log)
                    throws IOException {
        if (!log.exists())
            throw new IllegalArgumentException("Log file must exist.");
        if (log.isDirectory())
            throw new IllegalArgumentException("Log file has to be a file, not a directory.");

        this.log = new BufferedReader(new FileReader(log));
    }

    /**
     * This method reads and returns the next block of text in the session log.
     * <p>
     * Currently, there are four possible versions of text blocks that can
     * recognised:
     * <li>Encounter blocks</li>
     * <li>Consumable blocks</li>
     * <li>Player snapshot blocks</li>
     * <li>Other blocks (for everything else that wouldn't fit in the above
     * categories)</li>
     *
     * @return The parsed out text block from the session log.
     * @throws IOException
     *             if there were issues with reading the log; in certain
     *             circumstances, if the was a line with more than 500
     *             characters
     * @throws IllegalStateException
     *             if there is no more block to parse in the session log
     */
    LogBlock next()
            throws IOException {
        final LogBlock block;

        log.mark(500);
        String line = log.readLine();
        String line2 = log.readLine();
        log.reset();

        if (line == null)
            throw new IllegalStateException("There are no more blocks to be read.");
        if (line2 == null)
            line2 = UsefulPatterns.EMPTY_STRING;

        if (isEncounterBlockStart(line, line2))
            block = new LogBlockImpl(parseEncounterBlock(), LogBlockType.ENCOUNTER_BLOCK);
        else if (isConsumableBlockStart(line))
            block = new LogBlockImpl(parseNormalBlock(), LogBlockType.CONSUMABLE_BLOCK);
        else if (line.equals(SNAPSHOT_START_END) && line2.contains(PLAYER_SNAPSHOT_STRING))
            block = new LogBlockImpl(parsePlayerSnapshotBlock(), LogBlockType.PLAYER_SNAPSHOT_BLOCK);
        else if (line.startsWith(ASCENSION_DATA_START_STRING))
            block = new LogBlockImpl(parseNormalBlock(), LogBlockType.ASCENSION_DATA_BLOCK);
        else if (HybridDataBlockParser.isHybridBlock(line))
        	block = new LogBlockImpl(parseNormalBlock(), LogBlockType.HYBRID_DATA_BLOCK);
        else
            block = new LogBlockImpl(parseNormalBlock(), LogBlockType.OTHER_BLOCK);

        // Skip empty/too long lines and decide at the end whether the log is
        // finished.
        do
            log.mark(500);
        while ((line = log.readLine()) != null
                && (line.length() <= 0 || line.length() >= 450 || isLineOnBlackList(line)));
        if (line == null)
            hasNext = false;
        else
            log.reset();

        return block;
    }

    private boolean isLineOnBlackList(
            final String line) {
        return line.startsWith("mall.php") || line.startsWith("manageprices.php")
                || line.startsWith("familiarnames.php");
    }

    private boolean isEncounterBlockStart(
            String line, String line2) {
        // Add support for Rain Man detection

        boolean isAdventure = (line.startsWith(UsefulPatterns.SQUARE_BRACKET_OPEN) &&
                UsefulPatterns.TURNS_USED.matcher(line).matches()) ||
                (line2.startsWith(ENCOUNTER_START_STRING) &&
                        BROKEN_AREAS_ENCOUNTER_SET.contains(line2));

        boolean isRainman = line.contains("cast 1 Rain Man");

        return isAdventure || isRainman;

    }

    private boolean isConsumableBlockStart(
            String line) {
        boolean isConsumable = (line.startsWith(USE_STRING) || line.startsWith(EAT_STRING)
                || line.startsWith(DRINK_STRING) || line.startsWith(BUY_STRING) 
                || line.startsWith( SPLEEN_STRING ))   
                && UsefulPatterns.CONSUMABLE_USED.matcher(line).matches();

        return isConsumable;

    }

    private List<String> parseEncounterBlock()
            throws IOException {
        final List<String> result = Lists.newArrayList();
        String line;

        while ((line = log.readLine()) != null) {
            /**
             * Mafia saves a familiar pound gain this way in older versions:
             *
             * <pre>
             * Round _NUMBER_: _FAMNAME_ gains a pound!
             *
             * familiar _FAMTYPE_ (_POUNDS_ lbs)
             *
             * </pre>
             *
             * This is problematic because empty lines will end the while loop
             * even though the combat rundown isn't over. Thus we attempt to
             * skip the above mentioned lines.
             */
            if (line.endsWith(FAMILIAR_POUND_GAIN_END_STRING)) {
                // Remember current position.
                log.mark(500);

                // Check next line, if it is empty, the problematic logging is
                // occurring, otherwise reset back to the original position.
                final String tmpLine = log.readLine();
                if (tmpLine.length() <= 0) {
                    log.readLine();
                    log.readLine();
                    line = log.readLine();

                    if (line == null)
                        break;
                } else
                    log.reset();
            }

            // If there is an empty line, it means the encounter is over. There
            // are cases were this is not true for combats however, because
            // sometimes mafia puts empty lines in which aren't actually
            // supposed to be there. Such "false" empty lines should be
            // attempted to be recognised and skipped.
            if (line.length() <= 0) {
                // Remember current position.
                log.mark(600);

                // Look-ahead of three lines to try and see whether the combat
                // is actually continued.
                boolean isFightContinued = false;
                for (int i = 0; i < 3; i++) {
                    final String tmpLine = log.readLine();
                    // A square bracket means that a new turn was started. Extra
                    // check for the level 12 quest bossfight.
                    if (tmpLine == null || tmpLine.startsWith(UsefulPatterns.SQUARE_BRACKET_OPEN)
                            || tmpLine.startsWith(LEVEL_12_QUEST_BOSSFIGHT_BEGINNING_STRING))
                        break;
                    else if (tmpLine.startsWith(UsefulPatterns.COMBAT_ROUND_LINE_BEGINNING_STRING)) {
                        isFightContinued = true;
                        line = tmpLine;
                        break;
                    }
                }

                // If the fight has ended, set the reader back to the original
                // position and stop the while loop.
                if (!isFightContinued) {
                    log.reset();
                    break;
                }
            }

            result.add(line);
        }

        if (line == null)
            hasNext = false;

        return result;
    }

    private List<String> parsePlayerSnapshotBlock()
            throws IOException {
        final List<String> result = Lists.newArrayList();
        String line;

        // Add first three lines of the snapshot without check, so that the end
        // of the snapshot is not prematurely recognised.
        result.add(log.readLine());
        result.add(log.readLine());
        result.add(log.readLine());
        while ((line = log.readLine()) != null && !line.equals(SNAPSHOT_START_END))
            result.add(line);

        if (line == null)
            hasNext = false;

        return result;
    }

    private List<String> parseNormalBlock()
            throws IOException {
        final List<String> result = Lists.newArrayList();
        String line;

        while ((line = log.readLine()) != null && line.length() > 0)
            result.add(line);

        if (line == null)
            hasNext = false;

        return result;
    }

    /**
     * Use this method to check whether {@link #next()} is still able to return
     * another {@link LogBlock}.
     *
     * @return True if there are still blocks left to parse in the session log.
     */
    boolean hasNext() {
        return hasNext;
    }

    /**
     * Closes the {@link Reader} used to read the session log.
     */
    void close() {
        // Calling close() on a reader should not actually throw an exception,
        // so we'll just catch it in here.
        try {
            log.close();
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * An enumeration of all the possible types that a {@link LogBlock} can
     * have.
     */
    static enum LogBlockType {
        ENCOUNTER_BLOCK, CONSUMABLE_BLOCK, PLAYER_SNAPSHOT_BLOCK, ASCENSION_DATA_BLOCK, HYBRID_DATA_BLOCK, OTHER_BLOCK;
    }

    /**
     * Implementations of this interface are container classes to hold the block
     * of text that was parsed by a {@link MafiaSessionLogReader} and link it
     * with a certain version of {@link LogBlockType}.
     */
    static interface LogBlock {
        List<String> getBlockLines();

        LogBlockType getBlockType();
    }

    private static class LogBlockImpl implements LogBlock {
        private final List<String> blockLines;

        private final LogBlockType blockType;

        LogBlockImpl(
                final List<String> blockLines, final LogBlockType blockType) {
            if (blockLines == null)
                throw new NullPointerException("The list of lines must not be null.");
            if (blockType == null)
                throw new NullPointerException("The block type must not be null.");

            this.blockLines = blockLines;
            this.blockType = blockType;
        }

        public List<String> getBlockLines() {
            return Collections.unmodifiableList(blockLines);
        }

        public LogBlockType getBlockType() {
            return blockType;
        }
    }
}
