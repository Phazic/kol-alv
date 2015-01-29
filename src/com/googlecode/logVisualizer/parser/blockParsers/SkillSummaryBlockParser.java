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

package com.googlecode.logVisualizer.parser.blockParsers;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Scanner;
import java.util.regex.Pattern;

import com.googlecode.logVisualizer.logData.LogDataHolder;
import com.googlecode.logVisualizer.logData.Skill;
import com.googlecode.logVisualizer.parser.UsefulPatterns;

/**
 * A parser for the skill summary at the end of preparsed ascension logs.
 */
public final class SkillSummaryBlockParser extends AbstractBlockParser {
    private static final Pattern NOT_SKILL_NAME = Pattern.compile("Cast\\s+\\d+\\s+");

    private static final String CAST_STRING = "Cast";

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doParsing(
                             final BufferedReader reader, final LogDataHolder logData)
                                                                                      throws IOException {
        int emptyLineCounter = 0;
        String line;
        Scanner scanner;

        while ((line = reader.readLine()) != null)
            if (!line.equals(UsefulPatterns.EMPTY_STRING)) {
                if (line.startsWith(CAST_STRING)) {
                    final int numberOfCasts;
                    final String skillName;

                    // Parse number of casts
                    scanner = new Scanner(line);
                    scanner.useDelimiter(UsefulPatterns.NOT_A_NUMBER);
                    numberOfCasts = scanner.nextInt();
                    scanner.close();

                    // Parse skill name
                    scanner = new Scanner(line);
                    scanner.useDelimiter(NOT_SKILL_NAME);
                    skillName = scanner.next();
                    scanner.close();

                    // Add skill
                    // This summary is already correctly sorted, so no
                    // additional actions are necessary here.
                    final Skill skill = new Skill(skillName);
                    skill.setCasts(numberOfCasts, 0);

                    // Trivial combat skills don't cost MP for the natural
                    // class.
                    if (UsefulPatterns.TRIVIAL_COMBAT_SKILL_NAMES.contains(skillName)
                        && logData.getCharacterClass() == UsefulPatterns.TRIVAL_COMBAT_SKILL_CHARACTER_CLASS_MAP.get(skillName))
                        skill.setMpCost(0);

                    logData.getLogSummary().getSkillsCast().add(skill);
                }

                emptyLineCounter = 0;
            } else {
                emptyLineCounter++;
                if (emptyLineCounter >= 2)
                    break;
            }

        // Calculate and set total amount of casts and MP used.
        int totalSkillCasts = 0;
        int totalMPUsed = 0;
        for (final Skill s : logData.getLogSummary().getSkillsCast()) {
            totalSkillCasts += s.getAmount();
            totalMPUsed += s.getMpCost();
        }

        logData.getLogSummary().setTotalAmountSkillCasts(totalSkillCasts);
        logData.getLogSummary().setTotalMPUsed(totalMPUsed);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean isCompatibleBlock(
                                        final String line) {
        return line.contains("CASTS");
    }
}
