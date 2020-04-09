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

package com.googlecode.logVisualizer;

import java.io.File;
import java.io.IOException;
import java.util.List;

import com.googlecode.logVisualizer.creator.LogsCreator;
import com.googlecode.logVisualizer.gui.InternalMafiaLogParserDialog;
import com.googlecode.logVisualizer.logData.turn.Encounter;
import com.googlecode.logVisualizer.parser.MafiaLogIndex;
import com.googlecode.logVisualizer.util.LogOutputFormat;
import com.googlecode.logVisualizer.util.Pair;

public final class LogVisualizerCLI 
{
    public static void runCLIParsing(LogVisualizer.ALVParameters params) 
    {
        final LogOutputFormat outputFormat = params.format;
        final int numberToParse = params.ascensionCount;
        final File mafiaLogsDirectory 
            = (params.srcDir == null) ? new File(Settings.getSettingString("Mafia logs location"))
                                      : params.srcDir ;
        final File parsedLogsSavingDirectory 
            = (params.destDir == null) ? new File(Settings.getSettingString("Parsed logs saving location"))
                                       : params.destDir ;
        //final Pair<File, File> logFolders = getLogsSrcDestFolders(args);
        //final File mafiaLogsDirectory = logFolders.getVar1();
        //final File parsedLogsSavingDirectory = logFolders.getVar2();

        if (!mafiaLogsDirectory.isDirectory() || !parsedLogsSavingDirectory.isDirectory()) {
            System.out.println("Please specify only existing directories.");
            return;
        }

        final File[] allMafiaLogs = mafiaLogsDirectory.listFiles(InternalMafiaLogParserDialog.MAFIA_LOG_FILTER);
        if (allMafiaLogs.length == 0) {
            System.out.println("The directory specified for mafia logs does not contain any mafia logs.");
            return;
        }
        
        // Load the index
        MafiaLogIndex mafiaLogIndex = null;
        try {
            mafiaLogIndex = MafiaLogIndex.getMafiaLogIndex(mafiaLogsDirectory.getAbsolutePath());
            //mafiaLogIndex.dump();
        } catch (IOException e) {
            System.out.println(e);
            return;
        }

        // Make sure we have only the necessary logs
        File[] mafiaLogs = { };
        String playerName = params.playerName;    // null if not supplied in command line
        if (params.ascensionNumber > 0) {
            mafiaLogs = mafiaLogIndex.getAscensionN(params.ascensionNumber, playerName);
        } else if (params.date != null) {
            mafiaLogs = mafiaLogIndex.getAscensionForDate(params.date, playerName);
        } else if (params.ascensionCount > 0) {
            mafiaLogs = mafiaLogIndex.getLastNMafiaLogs(params.ascensionCount, playerName);
        }
        // Make sure we have ANY logs
        if (mafiaLogs.length == 0) {
            System.out.println("No Mafia logs found for this request.");
            return;
        }
        
        // If the input seems to be correct, save the directories used.
        Settings.setSettingString("Mafia logs location", mafiaLogsDirectory.getAbsolutePath());
        Settings.setSettingString("Parsed logs saving location",
                                  parsedLogsSavingDirectory.getAbsolutePath());

        // Now, the actual parsing can start.
        try {
            System.out.println("Parsing, please wait.");
            final List<Pair<String, Encounter>> errorFileList 
                = LogsCreator.createParsedLogs(mafiaLogs,
                                               parsedLogsSavingDirectory,
                                               outputFormat,
                                               numberToParse);
            System.out.println("Parsing finished.\n\n");

            // If there were error logs, give the user feedback on them.
            if (!errorFileList.isEmpty()) {
                final StringBuilder str = new StringBuilder(100);
                str.append("There were problems parsing the following logs. Please check the underlaying mafia session logs to see\n"
                           + "if they contained any corrupted data or lines longer than 500 characters and try to remove any problems.\n\n\n");
                str.append("The given list lists the erroneous log name and turn number after which the error occurred in the mafia\n"
                           + "session log upon which the log is based on.\n\n");
                for (final Pair<String, Encounter> p : errorFileList)
                    str.append(p.getVar1() + ": " + p.getVar2().getTurnNumber() + "\n");

                System.out.println(str);
            }
        } catch (final IOException e) {
            System.out.println("There was a problem while running the parser. Please check whether the parsed logs were created.");
            e.printStackTrace();
        }
    }
}
