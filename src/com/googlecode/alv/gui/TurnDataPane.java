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

package com.googlecode.alv.gui;

import java.awt.Font;
import java.util.Set;

import javax.swing.JEditorPane;
import javax.swing.UIManager;
import javax.swing.text.html.HTMLDocument;

import com.googlecode.alv.logData.Item;
import com.googlecode.alv.logData.MPGain;
import com.googlecode.alv.logData.Skill;
import com.googlecode.alv.logData.consumables.Consumable;
import com.googlecode.alv.logData.turn.Encounter;
import com.googlecode.alv.logData.turn.SingleTurn;
import com.googlecode.alv.logData.turn.TurnEntity;
import com.googlecode.alv.logData.turn.TurnInterval;
import com.googlecode.alv.logData.turn.turnAction.EquipmentChange;
import com.googlecode.alv.parser.UsefulPatterns;
import com.googlecode.alv.util.Sets;

public final class TurnDataPane extends JEditorPane {
    public TurnDataPane() {
        super("text/html", null);

        // add a CSS rule to force body tags to use the default label font
        // instead of the value in javax.swing.text.html.default.css
        final Font font = UIManager.getFont("Label.font");
        final String bodyRule = "body { font-family: " + font.getFamily() + "; " + "font-size: "
                                + font.getSize() + "pt; }";
        ((HTMLDocument) getDocument()).getStyleSheet().addRule(bodyRule);

        setEditable(false);
    }

    public void displayTurnEntityInfo(
                                      final TurnEntity t) {
        if (t instanceof TurnInterval)
            displayTurnIntervalInfo((TurnInterval) t);
        else if (t instanceof Encounter)
            displayEncounterInfo((Encounter) t);
        else {
            final StringBuilder str = new StringBuilder(1000);

            str.append("<html>");

            // Caption
            str.append("<h1>");
            str.append(UsefulPatterns.SQUARE_BRACKET_OPEN);
            str.append(t.getTurnNumber());
            str.append(UsefulPatterns.SQUARE_BRACKET_CLOSE);
            str.append(UsefulPatterns.WHITE_SPACE);
            str.append(t.getAreaName());
            str.append("</h1><p>");

            // General data
            str.append("<h2>");
            str.append("General Data");
            str.append("</h2><br>");
            str.append("Meat gained inside the encounter: " + t.getMeat().encounterMeatGain
                       + "<br>");
            str.append("Meat gained outside the encounter: " + t.getMeat().otherMeatGain + "<br>");
            str.append("Meat spent: " + t.getMeat().meatSpent + "<br>");
            str.append("Stat gains: " + t.getStatGain() + "<br>");
            str.append("Stat gains (including consumables): " + t.getTotalStatGain() + "<br>");
            int mpCosts = 0;
            for (final Skill s : t.getSkillsCast())
                mpCosts += s.getMpCost();
            str.append("MP spent: " + mpCosts + "<br>");
            str.append("Number of dropped items: " + t.getDroppedItems().size() + "<br>");
            str.append("Number of consumables used: " + t.getConsumablesUsed().size() + "<br>");
            str.append("Number of skills cast: " + t.getSkillsCast().size());
            str.append("<p>");

            str.append(createDetailedTurnInfoString(t));

            str.append("</html>");

            setText(str.toString());
            setCaretPosition(0);
        }
    }

    public void displayTurnIntervalInfo(
                                        final TurnInterval ti) {
        final StringBuilder str = new StringBuilder(2000);

        str.append("<html>");

        // Caption
        str.append("<h1>");
        str.append(UsefulPatterns.SQUARE_BRACKET_OPEN);
        if (ti.getTotalTurns() > 1) {
            str.append(ti.getStartTurn() + 1);
            str.append(UsefulPatterns.MINUS);
        }
        str.append(ti.getEndTurn());
        str.append(UsefulPatterns.SQUARE_BRACKET_CLOSE);
        str.append(UsefulPatterns.WHITE_SPACE);
        str.append(ti.getAreaName());
        str.append("</h1><p>");

        // General data
        str.append("<h2>");
        str.append("General Data");
        str.append("</h2><br>");
        str.append("Meat gained inside encounters: " + ti.getMeat().encounterMeatGain + "<br>");
        str.append("Meat gained outside encounters: " + ti.getMeat().otherMeatGain + "<br>");
        str.append("Meat spent: " + ti.getMeat().meatSpent + "<br>");
        str.append("Stat gains: " + ti.getStatGain() + "<br>");
        str.append("Stat gains (including consumables): " + ti.getTotalStatGain() + "<br>");
        str.append("Free runaways: " + ti.getFreeRunaways() + "<br>");

        int mpCosts = 0;
        for (final Skill s : ti.getSkillsCast())
            mpCosts += s.getMpCost();
        str.append("MP spent: " + mpCosts + "<br>");

        final Set<String> usedFamiliars = Sets.newHashSet();
        for (final SingleTurn st : ti.getTurns())
            usedFamiliars.add(st.getUsedFamiliar().getFamiliarName());
        str.append("Used familiars: ");
        for (final String s : usedFamiliars)
            str.append(s + ", ");
        str.replace(str.length() - 2, str.length(), "");
        str.append("<br>");

        str.append("Number of encounters: " + ti.getTotalTurns() + "<br>");
        str.append("Number of dropped items: " + ti.getDroppedItems().size() + "<br>");
        str.append("Number of consumables used: " + ti.getConsumablesUsed().size() + "<br>");
        str.append("Number of skills cast: " + ti.getSkillsCast().size());
        str.append("<p>");

        // Encounter list
        str.append("<h2>");
        str.append("Encounters");
        str.append("</h2><br>");
        for (final SingleTurn st : ti.getTurns()) {
            str.append(UsefulPatterns.SQUARE_BRACKET_OPEN);
            str.append(st.getTurnNumber());
            str.append(UsefulPatterns.SQUARE_BRACKET_CLOSE);
            str.append(UsefulPatterns.WHITE_SPACE);
            str.append(st.getEncounterName());
            str.append("<br>");
        }
        str.append("<p>");

        str.append(createDetailedTurnInfoString(ti));

        str.append("</html>");

        setText(str.toString());
        setCaretPosition(0);
    }

    public void displayEncounterInfo(
                                     final Encounter e) {
        final StringBuilder str = new StringBuilder(1000);

        str.append("<html>");

        // Caption
        str.append("<h1>");
        str.append(UsefulPatterns.SQUARE_BRACKET_OPEN);
        str.append(e.getTurnNumber());
        str.append(UsefulPatterns.SQUARE_BRACKET_CLOSE);
        str.append(UsefulPatterns.WHITE_SPACE);
        str.append(e.getEncounterName());
        str.append("</h1><p>");

        // General data
        str.append("<h2>");
        str.append("General Data");
        str.append("</h2><br>");
        str.append("Turn spent on day: " + e.getDayNumber() + "<br>");
        str.append("Meat gained inside the encounter: " + e.getMeat().encounterMeatGain + "<br>");
        str.append("Meat gained outside the encounter: " + e.getMeat().otherMeatGain + "<br>");
        str.append("Meat spent: " + e.getMeat().meatSpent + "<br>");
        str.append("Stat gains: " + e.getStatGain() + "<br>");
        str.append("Stat gains (including consumables): " + e.getTotalStatGain() + "<br>");
        int mpCosts = 0;
        for (final Skill s : e.getSkillsCast())
            mpCosts += s.getMpCost();
        str.append("MP spent: " + mpCosts + "<br>");
        str.append("Familiar: " + e.getUsedFamiliar().getFamiliarName() + "<br>");
        str.append("Number of dropped items: " + e.getDroppedItems().size() + "<br>");
        str.append("Number of consumables used: " + e.getConsumablesUsed().size() + "<br>");
        str.append("Number of skills cast: " + e.getSkillsCast().size());
        str.append("<p>");

        // Equipment
        str.append("<h2>");
        str.append("Equipment");
        str.append("</h2><br>");
        final EquipmentChange equip = e.getUsedEquipment();
        str.append("Hat: " + equip.getHat() + "<br>");
        str.append("Weapon: " + equip.getWeapon() + "<br>");
        str.append("Offhand: " + equip.getOffhand() + "<br>");
        str.append("Shirt: " + equip.getShirt() + "<br>");
        str.append("Pants: " + equip.getPants() + "<br>");
        str.append("Acc1: " + equip.getAcc1() + "<br>");
        str.append("Acc2: " + equip.getAcc2() + "<br>");
        str.append("Acc3: " + equip.getAcc3() + "<br>");
        str.append("Familiar equip: " + equip.getFamEquip());
        str.append("<p>");

        str.append(createDetailedTurnInfoString(e));

        str.append("</html>");

        setText(str.toString());
        setCaretPosition(0);
    }

    private String createDetailedTurnInfoString(
                                                final TurnEntity t) {
        final StringBuilder str = new StringBuilder(1000);

        // Dropped items
        str.append("<h2>");
        str.append("Dropped Items");
        str.append("</h2><br>");
        for (final Item i : t.getDroppedItems())
            str.append(i + "<br>");
        str.append("<p>");

        // Consumables used
        str.append("<h2>");
        str.append("Consumables Used");
        str.append("</h2><br>");
        for (final Consumable c : t.getConsumablesUsed())
            str.append(c + "<br>");
        str.append("<p>");

        // Skills cast
        str.append("<h2>");
        str.append("Skills Cast");
        str.append("</h2><br>");
        for (final Skill s : t.getSkillsCast()) {
            str.append(s.getAmount());
            str.append(UsefulPatterns.WHITE_SPACE);
            str.append(s.getName());
            str.append(UsefulPatterns.WHITE_SPACE);
            str.append(UsefulPatterns.ROUND_BRACKET_OPEN);
            str.append(s.getMpCost());
            str.append(UsefulPatterns.WHITE_SPACE);
            str.append("MP");
            str.append(UsefulPatterns.ROUND_BRACKET_CLOSE);
            str.append("<br>");
        }
        str.append("<p>");

        // MP summary
        final MPGain mpGains = t.getMPGain();
        str.append("<h2>");
        str.append("MP Gains");
        str.append("</h2><br>");
        str.append("Total mp gained: " + mpGains.getTotalMPGains() + "<br><br>");
        str.append("Inside Encounters: " + mpGains.encounterMPGain + "<br>");
        str.append("Starfish Familiars: " + mpGains.starfishMPGain + "<br>");
        str.append("Resting: " + mpGains.restingMPGain + "<br>");
        str.append("Outside Encounters: " + mpGains.outOfEncounterMPGain + "<br>");
        str.append("Consumables: " + mpGains.consumableMPGain + "<br>");
        str.append("<p>");

        return str.toString();
    }
}
