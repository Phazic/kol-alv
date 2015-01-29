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

import static com.googlecode.logVisualizer.parser.UsefulPatterns.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.googlecode.logVisualizer.logData.LogDataHolder;
import com.googlecode.logVisualizer.logData.logSummary.LogSummaryData;
import com.googlecode.logVisualizer.parser.blockParsers.*;
import com.googlecode.logVisualizer.parser.lineParsers.*;
import com.googlecode.logVisualizer.util.DataNumberPair;
import com.googlecode.logVisualizer.util.Lists;

/**
 * A parser for pre-parsed ascension logs with a format equal or at least very
 * similar to the one which the AFH parser uses.
 * <p>
 * It tries to get as much information out of these ascension logs as possible,
 * although not all the possibilities the {@link LogDataHolder} class gives can
 * be filled out, due to the data simply not being present in these logs.
 * <p>
 * Note that this class is immutable.
 */
public final class PreparsedLogParser extends AbstractLogParser {
    private static final String ASCENDED_STRING = "Ascended!";

    private static final String TURN_RUNDOWN_FINISHED_STRING = "Turn rundown finished!";

    private final File log;

    private final List<DataNumberPair<String>> semirares = Lists.newArrayList();

    private final List<DataNumberPair<String>> badMoonAdventures = Lists.newArrayList();

    private final List<DataNumberPair<String>> disintegratedCombats = Lists.newArrayList();

    /**
     * @param log
     *            The pre-parsed ascension log which is intended to be parsed to
     *            set.
     * @throws NullPointerException
     *             if log is {@code null}
     */
    public PreparsedLogParser(
                              final File log) {
        super(new LogDataHolder(false));
        this.log = log;

        // Set the log name
        if (log.getName().contains("_ascend")) {
            final Scanner scanner = new Scanner(log.getName());
            scanner.useDelimiter("_ascend|(?:_\\d+_\\d+)?\\..+$");
            getLogData().setLogName(scanner.next() + "-" + scanner.next());
            scanner.close();
        } else
            getLogData().setLogName(log.getName().replace(".txt", UsefulPatterns.EMPTY_STRING));

        // Add line parsers for parsing the turn rundown data
        addLineParser(new TurnsSpentLineParser());
        addLineParser(new DroppedItemLineParser());
        addLineParser(new ConsumableLineParser());
        addLineParser(new FamiliarChangeLineParse());
        addLineParser(new PullLineParser());
        addLineParser(new FreeRunawaysLineParser());
        addLineParser(new DayChangeLineParser());
        addSpecialLineParsers();

        // Add block parsers for parsing the summaries at the end of the log.
        addBlockParser(new LevelSummaryBlockParser());
        addBlockParser(new StatsSummaryBlockParser());
        addBlockParser(new FamiliarSummaryBlockParser());
        addBlockParser(new SemirareSummaryBlockParser());
        addBlockParser(new SkillSummaryBlockParser());
        addBlockParser(new MPSummaryBlockParser());
        addBlockParser(new MeatSummaryBlockParser());
        addBlockParser(new BottleneckSummaryBlockParser());
    }

    /**
     * While line parsers normally should be put into their own classes, some of
     * these have to have access to data structures inside this class and thus
     * are implemented as anonymous classes.
     * <p>
     * This has to be done, because of deficits or design decisions of the
     * {@link LogDataHolder} class. The most notable example for this is the
     * fact that the {@link LogSummaryData} can only be created after the turn
     * rundown data has been parsed and put into the {@link LogDataHolder}.
     * Thus, access to members of the {@link LogSummaryData} class is only
     * possible after the turn rundown part of preparsed ascension logs has been
     * parsed. But some of the data present in the turn rundown part of the
     * mentioned logs belongs inside {@link LogSummaryData}. Thus, this data has
     * to be saved in data structures inside this class temporarily, which in
     * turn can only be accessed be anonymous classes.
     * <p>
     * There might be other ways to accomplish this, but as long as the number
     * special classes is relatively low this will do.
     */
    private void addSpecialLineParsers() {
        // Semirare parser
        addLineParser(new AbstractLineParser() {
            private final Matcher semirareMatcher = SEMIRARE.matcher("");

            @Override
            protected void doParsing(
                                     final String line, final LogDataHolder logData) {
                // Parse the turn number
                Scanner scanner = new Scanner(line);
                scanner.useDelimiter(NOT_A_NUMBER);
                final int turnNumber = scanner.nextInt();
                scanner.close();

                // Parse semirare name
                scanner = new Scanner(line);
                scanner.useDelimiter(ALL_BEFORE_COLON);
                final String semirareName = scanner.next();
                scanner.close();

                // Add semirare
                semirares.add(DataNumberPair.of(semirareName, turnNumber));
            }

            @Override
            protected boolean isCompatibleLine(
                                               final String line) {
                return semirareMatcher.reset(line).matches();
            }
        });

        // Bad Moon Adventure parser
        addLineParser(new AbstractLineParser() {
            private final Matcher badmoonMatcher = BADMOON.matcher("");

            @Override
            protected void doParsing(
                                     final String line, final LogDataHolder logData) {
                // Parse the turn number
                Scanner scanner = new Scanner(line);
                scanner.useDelimiter(NOT_A_NUMBER);
                final int turnNumber = scanner.nextInt();
                scanner.close();

                // Parse adventure name
                scanner = new Scanner(line);
                scanner.useDelimiter(ALL_BEFORE_COLON);
                final String badMoonAdventureName = scanner.next();
                scanner.close();

                // Add Bad Moon adventure
                badMoonAdventures.add(DataNumberPair.of(badMoonAdventureName, turnNumber));
            }

            @Override
            protected boolean isCompatibleLine(
                                               final String line) {
                return badmoonMatcher.reset(line).matches();
            }
        });

        // Hunted combat parser
        addLineParser(new AbstractLineParser() {
            private final Matcher huntedCombatMatcher = HUNTED_COMBAT.matcher("");

            private final Pattern notCombatName = Pattern.compile("^.*Started hunting\\s+");

            @Override
            protected void doParsing(
                                     final String line, final LogDataHolder logData) {
                // Parse the turn number
                Scanner scanner = new Scanner(line);
                scanner.useDelimiter(NOT_A_NUMBER);
                final int turnNumber = scanner.nextInt();
                scanner.close();

                // Parse the combat name
                scanner = new Scanner(line);
                scanner.useDelimiter(notCombatName);
                if (scanner.hasNext()) {
                    final String combatName = scanner.next();

                    // Add hunted combat
                    logData.addHuntedCombat(DataNumberPair.of(combatName, turnNumber));
                }
                scanner.close();
            }

            @Override
            protected boolean isCompatibleLine(
                                               final String line) {
                return huntedCombatMatcher.reset(line).matches();
            }
        });

        // Disintegrated combat parser
        addLineParser(new AbstractLineParser() {
            private final Matcher disintegrateMatcher = UsefulPatterns.DISINTEGRATED_COMBAT.matcher(UsefulPatterns.EMPTY_STRING);

            private final Pattern notCombatName = Pattern.compile("^.*Disintegrated\\s+");

            @Override
            protected void doParsing(
                                     final String line, final LogDataHolder logData) {
                // Parse the turn number
                Scanner scanner = new Scanner(line);
                scanner.useDelimiter(NOT_A_NUMBER);
                final int turnNumber = scanner.nextInt();
                scanner.close();

                // Parse the combat name
                scanner = new Scanner(line);
                scanner.useDelimiter(notCombatName);
                if (scanner.hasNext()) {
                    final String combatName = scanner.next();

                    // Add disintegrated combat
                    disintegratedCombats.add(DataNumberPair.of(combatName, turnNumber));
                }
                scanner.close();
            }

            @Override
            protected boolean isCompatibleLine(
                                               final String line) {
                return disintegrateMatcher.reset(line).matches();
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    public void parse()
                       throws IOException {
        final BufferedReader reader = new BufferedReader(new FileReader(log));
        String line;

        // Parse the turn rundown part of the log.
        while ((line = reader.readLine()) != null)
            if (line.length() > 0) {
                // Stop the loop if the turn rundown is finished.
                if (line.startsWith(ASCENDED_STRING)
                    || line.startsWith(TURN_RUNDOWN_FINISHED_STRING))
                    break;

                parseLine(line);
            }

        // Create the log summary from the turn rundown data. Since these
        // preparsed logs don't hold enough data, some of the summaries will
        // stay empty.
        getLogData().createLogSummary();
        getLogData().getLogSummary().setSemirares(semirares);
        getLogData().getLogSummary().setBadmoonAdventures(badMoonAdventures);
        getLogData().getLogSummary().setDisintegratedCombats(disintegratedCombats);

        // Parse the summaries at the end of the log.
        while ((line = reader.readLine()) != null)
            parseBlock(reader);

        reader.close();
    }
}
