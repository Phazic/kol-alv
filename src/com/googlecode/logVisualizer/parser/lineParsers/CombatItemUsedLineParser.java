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

package com.googlecode.logVisualizer.parser.lineParsers;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.googlecode.logVisualizer.logData.CombatItem;
import com.googlecode.logVisualizer.logData.LogDataHolder;
import com.googlecode.logVisualizer.logData.turn.Turn;
import com.googlecode.logVisualizer.logData.turn.SingleTurn;
import com.googlecode.logVisualizer.parser.UsefulPatterns;

/**
 * A parser for the skill cast notation in mafia logs.
 * <p>
 * The format looks like this:
 * <p>
 * {@code Round _roundNumber_: _accountName_ uses _skillName_!}
 * <p>
 * OR
 * <p>
 * {@code Round _roundNumber_: _accountName_ casts _skillName_! (auto-attack)}
 */
public final class CombatItemUsedLineParser extends AbstractLineParser {
    private static final Pattern COMBAT_ITEM_USED_CAPTURE_PATTERN = Pattern.compile(".*uses ([\\p{L}\\d\\p{Punct}\\s]+)!(?: \\(auto-attack\\))?");

    private static final String COMBAT_ITEM_USED_STRING = "uses";

    private final Matcher combatItemUsedMatcher = COMBAT_ITEM_USED_CAPTURE_PATTERN.matcher(UsefulPatterns.EMPTY_STRING);

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doParsing(final String line, final LogDataHolder logData) {
        final String combatItemName;
        int amount = 1;

        final Matcher result = COMBAT_ITEM_USED_CAPTURE_PATTERN.matcher( line );
        result.find();
        
        combatItemName = result.group(1).toLowerCase(Locale.ENGLISH);

        // Add the skill to the current turn.
        final Turn currentTurn = logData.getLastTurnSpent();
        final CombatItem combatItem = new CombatItem(combatItemName, 1, currentTurn.getTurnNumber());
        
        currentTurn.addCombatItemUsed( combatItem );
        
        System.out.println("Found Combat Item: " + combatItem + " turn: " + currentTurn.getTurnNumber());
        
        //Check for Banishing combat items
        if (UsefulPatterns.BANISH_ITEMS.contains( combatItemName )) {
        	((SingleTurn) logData.getLastTurnSpent()).setBanished(true, combatItemName, null);        
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean isCompatibleLine(final String line) {
        return line.contains(COMBAT_ITEM_USED_STRING) && combatItemUsedMatcher.reset(line).matches();
    }
}
