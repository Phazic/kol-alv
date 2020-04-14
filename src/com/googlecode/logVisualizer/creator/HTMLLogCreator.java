package com.googlecode.logVisualizer.creator;

import java.util.Collections;

import com.googlecode.logVisualizer.logData.LogDataHolder;
import com.googlecode.logVisualizer.logData.Skill;
import com.googlecode.logVisualizer.logData.turn.turnAction.DayChange;

public class HTMLLogCreator extends TextLogCreator {

    public HTMLLogCreator(LogDataHolder logData) {
        super(logData);
        // Nothing else to do for this class
    }

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
    
    @Override
    protected void printTitle(final LogDataHolder logData, final int ascensionStartDate)
    {
        writeln("<h1>NEW " + logData.getCharacterClass() + " " + logData.getGameMode() + " "
                + logData.getAscensionPath() + " ASCENSION STARTED " + ascensionStartDate + "</h1>");
    }
    
    protected void printTOCLine(String text, String anchor)
    {
        writeln("<br><a href=\"#" + anchor + "\">" + text + "</a>");
    }
    
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
    
    @Override
    protected void printSectionHeader(String title, String anchor)
    {
        writeln("<a name=\"" + anchor + "\"/><h2>" + title + "</h2>");
    }

    @Override
    protected void printTableStart()
    {
        writeln("<table>");
    }
    
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

    @Override
    protected void printDayChange(DayChange nextDay) 
    {
        write(logAdditionsMap.get("dayChangeLineStart"));
        write("<h2>" + nextDay.toString() + "</h2>");
        writeln(logAdditionsMap.get("dayChangeLineEnd"));
    }
    
    @Override
    protected void printParagraphStart()
    {
        write("<p>");
    }
    
    @Override
    protected void printParagraphEnd()
    {
        write("</p>");
    }
    
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
