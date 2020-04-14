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

package com.googlecode.alv.parser.mafiaLogBlockParsers;

import java.util.List;

import com.googlecode.alv.logData.LogDataHolder;
import com.googlecode.alv.logData.LogDataHolder.AscensionPath;
import com.googlecode.alv.logData.LogDataHolder.CharacterClass;
import com.googlecode.alv.logData.LogDataHolder.GameMode;
import com.googlecode.alv.logData.turn.Turn;
import com.googlecode.alv.util.DataNumberPair;

/**
 * A parser for the ascension data found at the start of mafia logs.
 * <p>
 * The format of the first line looks like either of:
 * <p>
 * {@code Hybridizing yourself}
 * {@code Making Gene Tonic}
 */
public final class HybridDataBlockParser implements LogBlockParser 
{

    public static final String ACTION_HYBRID = "Hybridizing yourself";
    public static final String ACTION_MAKE_TONIC = "Making a Gene Tonic";
    public static final String ACQUIRE_INRINSIC = "You acquire an intrinsic: ";
    public static final String ACQUIRE_ITEM = "You acquire an item: ";
    public static final String GENE_TONIC = "Gene Tonic";
    
    public void parseBlock(final List<String> block, final LogDataHolder logData) 
    {
        
        String action = null; //Either Hybridinzing or Making
        String result = null; //Either the intrinsic or a gene tonic
        for (String line : block) {
            if (line.startsWith( ACQUIRE_INRINSIC )) {
                result = line.substring( ACQUIRE_INRINSIC.length() );
            } else if (line.startsWith( ACQUIRE_ITEM ) && line.contains( GENE_TONIC )) {
                result = line.substring( ACQUIRE_ITEM.length() );
            } else if (line.startsWith( ACTION_HYBRID )) {
                action = "Hybridizing";
            } else if (line.startsWith( ACTION_MAKE_TONIC )) {
                action = "Making";
            }
        }
    
        if (action != null && result != null) {
            final Turn currentInterval = logData.getLastTurnSpent();
            final int currentTurn = currentInterval.getTurnNumber();
           
            DataNumberPair<String> hybridData = DataNumberPair.of(action + " " + result, currentTurn);
            logData.addHybridContent( hybridData );
        }        
    }
    
    public static boolean isHybridBlock(String line) 
    {
        return line.startsWith( ACTION_HYBRID ) || line.startsWith( ACTION_MAKE_TONIC );
    }
}
