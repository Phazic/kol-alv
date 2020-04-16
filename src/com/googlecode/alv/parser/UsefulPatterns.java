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

package com.googlecode.alv.parser;

import java.io.File;
import java.util.Calendar;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Pattern;

import com.googlecode.alv.logData.LogDataHolder.CharacterClass;
import com.googlecode.alv.util.Maps;
import com.googlecode.alv.util.Pair;
import com.googlecode.alv.util.Sets;

/**
 * This utility class holds various useful regex patterns, strings and static
 * methods which are used for parsing preparsed or normal mafia ascension logs.
 */
public final class UsefulPatterns {
    public static final Set<String> MUSCLE_SUBSTAT_NAMES = Sets.immutableSetOf("Beefiness",
                                                                               "Fortitude",
                                                                               "Muscleboundness",
                                                                               "Strengthliness",
                                                                               "Strongness");

    public static final Set<String> MYST_SUBSTAT_NAMES = Sets.immutableSetOf("Enchantedness",
                                                                             "Magicalness",
                                                                             "Mysteriousness",
                                                                             "Wizardliness");

    public static final Set<String> MOXIE_SUBSTAT_NAMES = Sets.immutableSetOf("Cheek",
                                                                              "Chutzpah",
                                                                              "Roguishness",
                                                                              "Sarcasm",
                                                                              "Smarm");

    public static final Set<String> MP_NAMES = Sets.immutableSetOf("Muscularity Points",
                                                                   "Mana Points",
                                                                   "Mojo Points");

    public static final Set<String> TRIVIAL_COMBAT_SKILL_NAMES = Sets.immutableSetOf("clobber",
                                                                                     "toss",
                                                                                     "spaghetti spear",
                                                                                     "salsaball",
                                                                                     "suckerpunch",
                                                                                     "sing");

    public static final Set<String> TRACKED_COMBAT_ITEMS = Sets.immutableSetOf(
            "alpine watercolor set", "talisman of renenutet");

    public static final Set<String> BANISH_SKILLS = Sets.immutableSetOf(
            "curse of vacation", // Ed
            "batter up", // Seal Clubber
            "talk about politics", // Pantsgiving skill
            "creepy grin", // V for Viola mask skill
            "banishing shout", // AoB
            "howl of the alpha", // Zombie Slayer
            "peel out", // AoSP
            "walk away from explosion", // AoSP
            "thunder clap"// Heavy Rains
    );

    public static final Set<String> BANISH_ITEMS = Sets.immutableSetOf(
            "louder than bomb", // Smiths tome
            "crystal skull", // Clip-art
            "ice house",// winter garden
            "divine champagne popper", // Summon Party Favor
            "Harold's bell", "pulled indigo taffy", // Summon Taffy
            "classy monkey", // Class Act
            "dirty stinkbomb", // KOLHS
            "deathchucks", // KOLHS
            "smoke grenade", // AoSP
            "cocktail napkin" // Only against clingy pirate
    );

    @SuppressWarnings("unchecked")
    public static final Map<String, CharacterClass> TRIVAL_COMBAT_SKILL_CHARACTER_CLASS_MAP = Maps.immutableMapOf(Pair.of("clobber",
                                                                                                                          CharacterClass.SEAL_CLUBBER),
                                                                                                                  Pair.of("toss",
                                                                                                                          CharacterClass.TURTLE_TAMER),
                                                                                                                  Pair.of("spaghetti spear",
                                                                                                                          CharacterClass.PASTAMANCER),
                                                                                                                  Pair.of("salsaball",
                                                                                                                          CharacterClass.SAUCEROR),
                                                                                                                  Pair.of("suckerpunch",
                                                                                                                          CharacterClass.DISCO_BANDIT),
                                                                                                                  Pair.of("sing",
                                                                                                                          CharacterClass.ACCORDION_THIEF));

    public static final Set<String> SPECIAL_CONSUMABLES = Sets.immutableSetOf("steel margarita",
                                                                               "steel lasagna",
                                                                               "steel-scented air freshener",
                                                                               "spice melange",
                                                                               "synthetic dog hair pill",
                                                                               "mojo filter");

    public static final Pattern NON_ASCII = Pattern.compile("[^\\p{ASCII}]+");

    public static final Pattern NOT_A_NUMBER = Pattern.compile("\\D+");

    public static final Pattern ALL_BEFORE_COLON = Pattern.compile("^.*:\\s*");

    public static final Pattern NAME_COLON_NUMBER = Pattern.compile("^\\S.*:\\s+\\d+.*");

    public static final Pattern TURNS_USED = Pattern.compile("^\\[\\d+(?:\\-\\d+)?].+");

    public static final Pattern NOT_AREA_NAME = Pattern.compile("^\\[[\\d\\p{Punct}]+\\]\\s*|\\s+$|\\s*\\[[\\d\\p{Punct}]+\\]\\s*$");

    public static final Pattern NOT_TURNCOUNT_STRING = Pattern.compile("^\\[|\\][\\w\\p{Punct}\\s]+.*");

    public static final Pattern AREA_STATGAIN = Pattern.compile(".*\\[\\-?\\d+,\\-?\\d+,\\-?\\d+\\].*");

    public static final Pattern ITEM_FOUND = Pattern.compile("^\\s*\\+>.+");

    public static final Pattern CONSUMED = Pattern.compile("^\\s*o>\\s(?:Ate|Drank|Used|Chew).+");

    public static final Pattern FAMILIAR_CHANGED = Pattern.compile("^\\s*->\\sTurn.+");

    public static final Pattern PULL = Pattern.compile("^\\s*#>\\sTurn\\s\\[\\d+\\]\\spulled.+");

    public static final Pattern DAY_CHANGE = Pattern.compile("^=+Day\\s+(?:[2-9]|\\d\\d+).*");

    public static final Pattern SEMIRARE = Pattern.compile("^\\s*#>\\s\\[\\d+\\]\\sSemirare:\\s.+");

    public static final Pattern BADMOON = Pattern.compile("^\\s*%>.+");

    public static final Pattern HUNTED_COMBAT = Pattern.compile("^\\s*\\*>\\s\\[\\d+\\]\\sStarted\\shunting.*");

    public static final Pattern DISINTEGRATED_COMBAT = Pattern.compile("^\\s*\\}> \\[\\d+\\] Disintegrated .*");

    public static final Pattern FREE_RUNAWAYS_USAGE = Pattern.compile("^\\s*\\&> \\d+ \\\\ \\d+ free retreats.*");

    public static final Pattern CONSUMABLE_USED = Pattern.compile("(?:(?:use|eat|drink|chew)|Buy and (?:eat|drink))(?: \\d+)? .+");

    public static final Pattern GAIN = Pattern.compile("^You gain \\d*,?\\d+ [\\w\\s]+");

    public static final Pattern GAIN_LOSE_CAPTURE_PATTERN = Pattern.compile("^You (?:gain|lose) (\\d*,?\\d+) ([\\w\\s]+)");

    public static final Pattern GAIN_LOSE = Pattern.compile("^(?:After Battle: )?You (?:gain|lose) \\d*,?\\d+ [\\w\\s]+");

    public static final Pattern USUAL_FORMAT_LOG_NAME = Pattern.compile(".+\\-\\d{8}$");

    public static final String COMBAT_ROUND_LINE_BEGINNING_STRING = "Round ";

    public static final String ACQUIRE_EFFECT_STRING = "You acquire an effect:";

    public static final String AFTER_BATTLE_STRING = "After Battle: ";

    public static final String COMMA = ",";

    public static final String MINUS = "-";

    public static final String COLON = ":";

    public static final String PERCENTAGE_SIGN = "%";

    public static final String SQUARE_BRACKET_OPEN = "[";

    public static final String SQUARE_BRACKET_CLOSE = "]";

    public static final String ROUND_BRACKET_OPEN = "(";

    public static final String ROUND_BRACKET_CLOSE = ")";

    public static final String WHITE_SPACE = " ";

    public static final String EMPTY_STRING = "";

    /**
     * Returns the creation date of the given mafia log file. Uses the method
     * {@link #getMafiaLogCalendarDate(String)} to parse out the creation date.
     *
     * @param mafiaLog
     *            The file name of the mafia log of which the creation date
     *            should be returned.
     * @return The creation date of the given mafia log.
     * @throws NullPointerException
     *             if mafiaLog is {@code null}
     */
    public static final Calendar getMafiaLogCalendarDate(
                                                         final File mafiaLog) {
        return getMafiaLogCalendarDate(mafiaLog.getName());
    }

    /**
     * Returns the creation date of the given mafia log file. Uses the method
     * {@link #getMafiaLogCalendarDate(String)} to parse out the creation date.
     *
     * @param mafiaLogFileName
     *            The file name of the mafia log of which the creation date
     *            should be returned.
     * @return The creation date of the given mafia log.
     * @throws NullPointerException
     *             if mafiaLogFileName is {@code null}
     */
    public static final Calendar getMafiaLogCalendarDate(
                                                         final String mafiaLogFileName) {
        final Calendar logDate = Calendar.getInstance();
        final String ascensionDate = String.valueOf(getLogDate(mafiaLogFileName));

        logDate.clear();
        logDate.set(Integer.parseInt(ascensionDate.substring(0, 4)),
                    Integer.parseInt(ascensionDate.substring(4, 6)) - 1,
                    Integer.parseInt(ascensionDate.substring(6)));

        return logDate;
    }

    /**
     * Parses the creation date out from the given mafia log file. This method
     * makes use of the mafia log file name format which always looks like
     * {@code USERNAME_YYYYMMDD.txt}, where Y is year, M is month and D is day.
     *
     * @param mafiaLog
     *            The file name of the mafia log of which the creation date
     *            should be returned.
     * @return The creation date of the given mafia log. The format of the
     *         returned integer is YYYYMMDD, where Y is year, M is month and D
     *         is day.
     * @throws NullPointerException
     *             if mafiaLog is {@code null}
     */
    public static final int getMafiaLogDate(
                                            final File mafiaLog) {
        return getLogDate(mafiaLog.getName());
    }

    /**
     * Parses the creation date out from the given log name. This method makes
     * use of the often used log name format which looks like {@code
     * SOMETHINGYYYYMMDD.txt} or {@code SOMETHINGYYYYMMDD} (essentially the date
     * is at the end and not directly interlinked with a number before/after
     * it), where Y is year, M is month and D is day. Log names that do not
     * follow this format cannot be parsed by this method.
     *
     * @param logName
     *            The name of the log of which the creation date should be
     *            returned.
     * @return The creation date of the given log. The format of the returned
     *         integer is YYYYMMDD, where Y is year, M is month and D is day.
     * @throws NullPointerException
     *             if mafiaLogFileName is {@code null}
     */
    public static final int getLogDate(
                                       final String logName) {
        if (logName == null)
            throw new NullPointerException("logName must not be null.");

        int ascensionDate;
        final Scanner scanner = new Scanner(logName);
        scanner.useDelimiter(UsefulPatterns.NOT_A_NUMBER);
        // The last number in the file name is the date.
        do
            ascensionDate = scanner.nextInt();
        while (scanner.hasNextInt());

        scanner.close();

        return ascensionDate;
    }

    // This class is not to be instanced.
    private UsefulPatterns() {}
}
