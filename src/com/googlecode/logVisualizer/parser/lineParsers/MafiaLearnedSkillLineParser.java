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

package com.googlecode.logVisualizer.parser.lineParsers;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.googlecode.logVisualizer.logData.CombatItem;
import com.googlecode.logVisualizer.logData.LogDataHolder;
import com.googlecode.logVisualizer.logData.turn.Turn;
import com.googlecode.logVisualizer.logData.turn.SingleTurn;
import com.googlecode.logVisualizer.parser.UsefulPatterns;
import com.googlecode.logVisualizer.util.DataNumberPair;

/**
 * A parser for the skill cast notation in mafia logs.
 * <p>
 * The format looks like this:
 * <p>
 * {@code You learned a new skill: _skillName_}
 */
public final class MafiaLearnedSkillLineParser extends AbstractLineParser 
{
    private static final String LEARNED_SKILL_STRING = "You learned a new skill: ";

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doParsing(final String line, final LogDataHolder logData) 
    {
        String skillName = line.substring( LEARNED_SKILL_STRING.length());
        int currentTurn = logData.getLastTurnSpent().getTurnNumber();
        DataNumberPair<String> learnedSkillData = DataNumberPair.of( skillName, currentTurn );
        logData.addLearnedSkill( learnedSkillData );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean isCompatibleLine(final String line) 
    {
        return line.startsWith( LEARNED_SKILL_STRING );
    }    
}
