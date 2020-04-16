/* Copyright (c) 2020-2020, developers of the Ascension Log Visualizer
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

package com.googlecode.alv.creator;

import java.util.Collections;

import com.googlecode.alv.logData.LogDataHolder;
import com.googlecode.alv.logData.Skill;
import com.googlecode.alv.logData.turn.turnAction.DayChange;

/**
 * This class extends TextLogCreator so as to create HTML log output.  It 
 * overrides several of TextLogCreator's output methods to take advantage 
 * of HTML's features.
 */
public class HTMLLogCreator extends TextLogCreator {

    /**
     * Creates an HTMLLogCreator instance for further use.
     *
     * @param logData
     *            The ascension log data from which the parsed ascension log
     *            should be created.
     */
    public HTMLLogCreator(LogDataHolder logData) {
        super(logData);
        // Nothing else to do for this class
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setAugmentationsMap()
    {
        logAdditionsMap = Collections.unmodifiableMap(readAugmentationsList("htmlAugmentations.txt"));
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void beginTextLog()
    {
        writeln("<html>");
        writeln("<head><style>");
        writeln("HTML { font-size: 11px; }");
        writeln("P { text-indent: -2em; margin-left: 2em; margin-top: 0; margin-bottom: 0; }");
        writeln("TD { font-size: 11px; padding-left: 1em; padding-right: 1em; \n" + 
                "     text-align: right; }");
        writeln("TD.toc { font-size: 11px; padding-left: 1em; padding-right: 1em; \n" + 
                "         text-align: left; background-color: #cccccc }");
        writeln("</style></head>");
        writeln("<body>");
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void printTitle(final LogDataHolder logData, final int ascensionStartDate)
    {
        writeln("<h1>NEW " + logData.getCharacterClass() + " " + logData.getGameMode() + " "
                + logData.getAscensionPath() + " ASCENSION STARTED " + ascensionStartDate + "</h1>");
    }

    /**
     * Print a line in the table of contents.  
     * 
     * @param text String, usually a section name, that should appear in the table of contents.
     * @param anchor Name of the HTML anchor to which to link the TOC entry.
     */
    protected void printTOCLine(String text, String anchor)
    {
        writeln("<br><a href=\"#" + anchor + "\">" + text + "</a>");
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void printTableOfContents(LogDataHolder logData)
    {
        writeln("<table><tr><td class=\"toc\"><b>TABLE OF CONTENTS</b>");
        printTOCLine("Adventures", "adventures");
        printTOCLine("Quest Turns", "questturns");
        printTOCLine("Pulls", "pulls");
        printTOCLine("Levels", "levels");
        printTOCLine("Stats", "stats");
        printTOCLine("+Stat Breakdown", "statbreakdown");
        if (logData.getLearnedSkills().size() > 0) 
            printTOCLine("Skills Learned", "skills");
        printTOCLine("Familiars", "familiars");
        printTOCLine("Semi-rares", "semirares");
        if (logData.getLogSummary().getTrackedCombatItemUses().size() > 0) 
            printTOCLine("Tracked Combat Items", "trackedcombatitems");
        if (logData.getHybridContent().size() > 0) 
            printTOCLine("DNA Lab", "hybrid");
        printTOCLine("Hunted Combats", "onthetrail");
        printTOCLine("Banishment", "banishment");
        printTOCLine("Yellow Destruction", "yellowray");
        printTOCLine("Copied Combats", "copies");
        printTOCLine("Free Runaways", "runaways");
        printTOCLine("Wandering Encounters", "wanderers");
        printTOCLine("Combat Items", "combatitems");
        printTOCLine("Casts", "casts");
        printTOCLine("MP Gains", "mpgains");
        printTOCLine("Eating and Drinking and Using", "consuming");
        printTOCLine("Meat", "meat");
        printTOCLine("Bottlenecks", "bottlenecks");
        writeln("</td></tr></table>");
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void printSectionHeader(String title, String anchor)
    {
        writeln("<a name=\"" + anchor + "\"/><h2>" + title + "</h2>");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void printTableStart()
    {
        writeln("<table>");
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void printTableRow(String ...strings)
    {
        write("<tr>");
        for (String s : strings) {
            write("<td>");
            write(s);
            writeln("</td>");
        }
        writeln("</tr>");
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void printTableEnd()
    {
        writeln("</table>");
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void printCastsSection(LogDataHolder logData)
    {
        // Skills cast summary
        printSectionHeader("CASTS", "casts");
        for (final Skill s : logData.getLogSummary().getSkillsCast()) {
            writelnWithBreak(s.toString());
        }
        writelnWithBreak();
        writelnWithBreak("Total Casts: " + logData.getLogSummary().getTotalAmountSkillCasts());
        writelnWithBreak("Total MP Spent: " + logData.getLogSummary().getTotalMPUsed());
        writelnWithBreak();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void printDayChange(DayChange nextDay) 
    {
        write(logAdditionsMap.get("dayChangeLineStart"));
        write("<h2>" + nextDay.toString() + "</h2>");
        writeln(logAdditionsMap.get("dayChangeLineEnd"));
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void printParagraphStart()
    {
        write("<p>");
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void printParagraphEnd()
    {
        write("</p>");
    }
    
    /**
     * {@inheritDoc}
     */
    protected void printLineBreak()
    {
        write("<br>");
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void endTextLog()
    {
        writeln("</body></html>");
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void writeEndLine()
    {
        writeln("<br>");
    }
}
