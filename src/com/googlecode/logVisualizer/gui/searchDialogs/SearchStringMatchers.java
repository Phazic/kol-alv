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

package com.googlecode.logVisualizer.gui.searchDialogs;

import java.util.List;

import com.googlecode.logVisualizer.logData.Item;
import com.googlecode.logVisualizer.logData.Skill;
import com.googlecode.logVisualizer.logData.consumables.Consumable;
import com.googlecode.logVisualizer.logData.turn.Encounter;
import com.googlecode.logVisualizer.logData.turn.TurnEntity;
import com.googlecode.logVisualizer.util.Lists;

/**
 * Utility class that holds matchers for searches to make it easier to change
 * between search modes.
 */
final class SearchStringMatchers {
    /**
     * Matches turns based on their area name.
     */
    public static final SearchStringMatcher AREA_NAME_MATCHER = new SearchStringMatcher() {
        public boolean matches(
                               final TurnEntity t, final String searchString) {
            return t.getAreaName().toLowerCase().contains(searchString);
        }

        @Override
        public String toString() {
            return "By Area Name";
        }
    };

    /**
     * Matches turns based on their encounter name. Only works with
     * {@link Encounter} implementations. Other {@link TurnEntity}
     * implementations will return {@code false} at all times.
     */
    public static final SearchStringMatcher ENCOUNTER_NAME_MATCHER = new SearchStringMatcher() {
        public boolean matches(
                               final TurnEntity t, final String searchString) {
            if (t instanceof Encounter)
                return ((Encounter) t).getEncounterName().toLowerCase().contains(searchString);
            else
                return false;
        }

        @Override
        public String toString() {
            return "By Encounter Name";
        }
    };

    /**
     * Matches turns based on the names of their itemdrops.
     */
    public static final SearchStringMatcher ITEMDROP_NAME_MATCHER = new SearchStringMatcher() {
        public boolean matches(
                               final TurnEntity t, final String searchString) {
            for (final Item i : t.getDroppedItems())
                if (i.getName().toLowerCase().contains(searchString))
                    return true;

            return false;
        }

        @Override
        public String toString() {
            return "By Itemdrops";
        }
    };

    /**
     * Matches turns based on the names of their skill casts.
     */
    public static final SearchStringMatcher SKILL_NAME_MATCHER = new SearchStringMatcher() {
        public boolean matches(
                               final TurnEntity t, final String searchString) {
            for (final Skill s : t.getSkillsCast())
                if (s.getName().toLowerCase().contains(searchString))
                    return true;

            return false;
        }

        @Override
        public String toString() {
            return "By Skills Cast";
        }
    };

    /**
     * Matches turns based on the names of their consumable usages.
     */
    public static final SearchStringMatcher CONSUMABLE_NAME_MATCHER = new SearchStringMatcher() {
        public boolean matches(
                               final TurnEntity t, final String searchString) {
            for (final Consumable c : t.getConsumablesUsed())
                if (c.getName().toLowerCase().contains(searchString))
                    return true;

            return false;
        }

        @Override
        public String toString() {
            return "By Consumable";
        }
    };

    /**
     * A read-only list of all existing search string matchers.
     */
    public static final List<SearchStringMatcher> MATCHERS = Lists.immutableListOf(AREA_NAME_MATCHER,
                                                                                   ENCOUNTER_NAME_MATCHER,
                                                                                   ITEMDROP_NAME_MATCHER,
                                                                                   SKILL_NAME_MATCHER,
                                                                                   CONSUMABLE_NAME_MATCHER);

    public interface SearchStringMatcher {
        boolean matches(
                        final TurnEntity e, final String searchString);
    }
}
