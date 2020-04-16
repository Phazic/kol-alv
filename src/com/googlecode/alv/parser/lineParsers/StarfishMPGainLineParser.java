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

package com.googlecode.alv.parser.lineParsers;

import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;

import com.googlecode.alv.logData.LogDataHolder;
import com.googlecode.alv.logData.MPGain;
import com.googlecode.alv.logData.turn.Turn;
import com.googlecode.alv.parser.UsefulPatterns;
import com.googlecode.alv.util.Lists;

public final class StarfishMPGainLineParser extends AbstractLineParser {
    private static final Pattern STARFISH_ATK = Pattern.compile("Round \\d+: .+ floats behind your opponent, and begins to glow brightly.\\s*Starlight"
                                                                + " shines through your opponent, doing \\d+ damage, and pours into your body.");

    private static final Pattern SPIRIT_HOBO_ATK = Pattern.compile("Round \\d+: .+ holds up an empty bottle of booze and gazes at it sadly."
                                                                   + "\\s*Starlight filters through the bottle, through the spirit hobo, and"
                                                                   + " through the booze inside the spirit hobo, then pierces your opponent"
                                                                   + " for \\d+ damage, and then shines into you.\\s*What the hell\\?");

    private static final Pattern GGG_ATK1 = Pattern.compile("Round \\d+: .+ slimes your opponent thoroughly, dealing \\d+ damage."
                                                            + "\\s*The resulting ectoplasmic shock wave gives you a mystical jolt.");

    private static final Pattern GGG_ATK2 = Pattern.compile("Round \\d+: .+ swoops through your opponent, somehow transferring \\d+ points"
                                                            + " of \\w+ lifeforce into \\w+ Points for you.\\s*You feel slightly skeeved out.");

    private static final Pattern GGG_ATK3 = Pattern.compile("Round \\d+: .+ swoops back and forth through your opponent, scaring the bejeezus"
                                                            + " out of \\w+ to the tune of \\d+ damage.\\s*Then he converts the bejeezus into \\w+ Points!");

    private static final Pattern SLIMELING_ATK = Pattern.compile("Round \\d+: .+ leaps on your opponent, sliming \\w+ for \\d+ damage.\\s*It's inspiring!");

    private static final Pattern ROUGE_ATK1 = Pattern.compile("Round \\d+: .+ de-rezzes \\w+ for \\d+ damage, then offers you a drink out"
                                                              + " of his identity disc.\\s*It's a little too intimate for your comfort,"
                                                              + " but it's still refreshing.");

    private static final Pattern ROUGE_ATK2 = Pattern.compile("Round \\d+: .+ tosses his identity disc at \\w+ for \\d+ damage, then invites"
                                                              + " you to drink some glowing blue liquid out of the disc.\\s*The whole thing's a"
                                                              + " little more intimate than you're comfortable with, but it's still refreshing.");

    private static final Pattern ROUGE_ATK3 = Pattern.compile("Round \\d+: .+ bounces his disc off of \\w+ for \\d+ damage, and it ricochets into you, giving you quite a shock.");

    private static final Pattern CLOWNFISH_ATK = Pattern.compile("Round \\d+: .+ flops toward \\w+, gasping for water, and manages to tailsmack \\w+ for \\d+ slimy, clammy damage.");

    private static final Pattern DUCK_ATK = Pattern.compile("Round \\d+: .+ quacks loudly, and a bolt of enriched wheat energy tears through your opponent for \\d+ damage, then arcs toward you, energizing your nervous system.");

    private static final Pattern ANGEL_ATK = Pattern.compile("Round \\d+: .+ rises into the air and spreads her wings, bathing your opponent in cold light and dealing \\d+ damage.\\s*It's inspiring.");

    private static final Pattern BONSAI_ATK = Pattern.compile("Round \\d+: .+ fixes an evil glare on your opponent, causing \\w+ to suffer \\d+ damage worth of heebie-jeebies.\\s*A plume of oily black smoke emerges from his bark, and you accidentally inhale some of it.\\s*You realize, to your horror, that it smells... good.");

    private static final List<Pattern> STARFISH_ATTACKS = Lists.immutableListOf(STARFISH_ATK,
                                                                                SLIMELING_ATK,
                                                                                ROUGE_ATK1,
                                                                                ROUGE_ATK2,
                                                                                ROUGE_ATK3,
                                                                                CLOWNFISH_ATK,
                                                                                DUCK_ATK,
                                                                                ANGEL_ATK,
                                                                                BONSAI_ATK,
                                                                                SPIRIT_HOBO_ATK,
                                                                                GGG_ATK1,
                                                                                GGG_ATK2,
                                                                                GGG_ATK3);

    private static final String OPPONENT_STRING = "opponent";

    private static final String ROUGE_SPECIFIC_STRING = "disc";

    private static final String CLOWNFISH_SPECIFIC_STRING = "tailsmack";

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doParsing(
                             final String line, final LogDataHolder logData) {
        // Most starfish familiars have the word "opponent" inside their attack
        // messages before the damage is given. The Rouge Program and Clownfish
        // do not however.
        final String tmp;
        if (line.contains(OPPONENT_STRING))
            tmp = line.substring(line.lastIndexOf(OPPONENT_STRING));
        else if (line.contains(CLOWNFISH_SPECIFIC_STRING))
            tmp = line.substring(line.lastIndexOf(CLOWNFISH_SPECIFIC_STRING));
        else if (line.contains("de-rezzes"))
            tmp = line.substring(line.lastIndexOf("de-rezzes"));
        else {
            final String subStr = line.substring(0, line.lastIndexOf("damage"));
            tmp = subStr.substring(subStr.lastIndexOf(ROUGE_SPECIFIC_STRING));
        }
        final Scanner scanner = new Scanner(tmp);
        scanner.useDelimiter(UsefulPatterns.NOT_A_NUMBER);
        final int dmg = scanner.nextInt();

        final Turn lastTurn = logData.getLastTurnSpent();
        lastTurn.addMPGain(new MPGain(0, dmg, 0, 0, 0));
        // Subtract from encounter mp gains the amount of starfish mp, because
        // mafia throws the mp gain of the starfish also out in a way that will
        // be catched by the MPGainLineParser.
        lastTurn.addMPGain(new MPGain(dmg * -1, 0, 0, 0, 0));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean isCompatibleLine(
                                       final String line) {
        if (line.startsWith(UsefulPatterns.COMBAT_ROUND_LINE_BEGINNING_STRING)
            && (line.contains(OPPONENT_STRING) || line.contains(ROUGE_SPECIFIC_STRING) || line.contains(CLOWNFISH_SPECIFIC_STRING)))
            for (final Pattern p : STARFISH_ATTACKS)
                if (p.matcher(line).matches())
                    return true;

        return false;
    }
}