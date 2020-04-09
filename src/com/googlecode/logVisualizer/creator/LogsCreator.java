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

package com.googlecode.logVisualizer.creator;

import java.io.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.java.dev.spellcast.utilities.UtilityConstants;

import com.googlecode.logVisualizer.Settings;
import com.googlecode.logVisualizer.logData.turn.Encounter;
import com.googlecode.logVisualizer.util.Lists;
import com.googlecode.logVisualizer.util.LogOutputFormat;
import com.googlecode.logVisualizer.util.Pair;

/**
 * This class gives access to methods to create condensed mafia ascension logs
 * or parsed ascension logs.
 * <p>
 * Note that all methods of this class are thread-safe as long as not the same
 * files are given to different threads or the created files by the given method
 * have the same file name and are stored in the same directory.
 */
public final class LogsCreator {

    // This class is not to be instanced.
    private LogsCreator() {}

    /**
     * Creates and returns condensed mafia logs which hold single ascensions
     * from start to end in a single file.
     * <p>
     * Day changes (the junction between two normal log files of a single
     * ascension) will be separated by the string {@code ===Day _dayNumber_===},
     * which is in essence the same as the one used in parsed ascension logs to
     * show day changes. Note that if the were multiple days on which no login
     * occurred, it will be catched by the parsing mechanism behind this method,
     * resulting in the right number of day change strings right after each
     * other.
     * <p>
     * There are sometimes cases of KolMafia stating that it is the same
     * real-life date, while the KoL date changed (this strongly depends on the
     * users time zone). If such a case is recognised, the line
     * "Day change occurred" will be added to the player snapshot in which this
     * date change was noticed.
     * <p>
     * The number of files depends on the number of ascensions present inside
     * the given mafia logs.
     * <p>
     * Please note that the condensed mafia logs created by this method are
     * stored in the directory for temporal data as denoted by
     * {@link UtilityConstants#TEMP_LOCATION}. It is the <b>responsibility of
     * the programmer using this method to delete these files as soon as they
     * are not needed anymore</b> to ensure that no bugs appear because of
     * leftover files and no size bloat of the Ascension Log Visualizer
     * directory happens.
     * 
     * @param mafiaLogs
     *            The mafia logs which should be condensed into mafia logs which
     *            each holds a single ascension.
     * @return The condensed mafia logs.
     * @throws IOException
     *             if there was a problem while accessing the given mafia logs
     *             or writing the condensed ones
     * @throws NullPointerException
     *             if mafiaLogs is {@code null}
     * @throws IllegalArgumentException
     *             if mafiaLogs does not contain any elements
     */
    public static final File[] createCondensedMafiaLogs(final File[] mafiaLogs)
    throws IOException 
    {
        return new CondensedMafiaLogsCreator(mafiaLogs).condense();
    }

    /**
     * Creates and saves parsed ascension logs. The format of those logs is
     * similar to the one used by the AFH MafiaLog Parser. (
     * {@link TextLogCreator} handles the log format)
     * <p>
     * The file names of the created logs have the format
     * {@code USERNAME_ascendYYYYMMDD.txt}, where Y is the year, M is the month
     * and D is the day of the first day of that ascension.
     * 
     * @param mafiaLogs
     *    	The mafia logs which should be turned into parsed ascension
     *   	logs.
     * @param savingDestDir
     *    	The directory inside which the parsed ascension logs should be
     *    	saved in.
     * @param logVersion
     *     	The output format of the parsed logs.
     * @return A list containing pairs with filenames and turns of condensed
     *   	mafia log files that were attempted to be parsed, but had an
     *  	exception thrown during the parsing process. The included turn
     * 		the turn after which the exception occurred. This list will be
     *    	empty if all files were correctly parsed.
     * @throws IOException
     *     	if there was a problem while accessing or writing files
     *    	handled by this method
     * @throws NullPointerException
     *     	if mafiaLogs is {@code null}; if savingDestDir is
     *     	{@code null}
     * @throws IllegalArgumentException
     *    	if mafiaLogs does not contain any elements; if the directory
     *    	savingDestDir does not exist; if savingDestDir is not a
     *     	directory
     */
    public static final List<Pair<String, Encounter>> createParsedLogs(final File[] mafiaLogs,
                                                                       final File savingDestDir,
                                                                       final LogOutputFormat logVersion)
    throws IOException 
    {
        return createParsedLogs(mafiaLogs, savingDestDir, logVersion, Integer.MAX_VALUE);
    }

    /**
     * Creates and saves parsed ascension logs. The format of those logs is
     * similar to the one used by the AFH MafiaLog Parser. (
     * {@link TextLogCreator} handles the log format)
     * <p>
     * The file names of the created logs have the format
     * {@code USERNAME_ascendYYYYMMDD.txt}, where Y is the year, M is the month
     * and D is the day of the first day of that ascension.
     * <p>
     * Note that only the last n ascensions will be parsed.
     * 
     * @param mafiaLogs
     *   	The mafia logs which should be turned into parsed ascension
     *    	logs.
     * @param savingDestDir
     *    	The directory inside which the parsed ascension logs should be
     *     	saved in.
     * @param logVersion
     *    	The output format of the parsed logs.
     * @param logsToParse
     *    	The last n ascensions that should be parsed.
     * @return A list containing pairs with filenames and turns of condensed
     *    	mafia log files that were attempted to be parsed, but had an
     *   	exception thrown during the parsing process. The included turn
     *    	the turn after which the exception occurred. This list will be
     *    	empty if all files were correctly parsed.
     * @throws IOException
     *    	if there was a problem while accessing or writing files
     *   	handled by this method
     * @throws NullPointerException
     *     	if mafiaLogs is {@code null}; if savingDestDir is {@code null}
     * @throws IllegalArgumentException
     *    	if mafiaLogs does not contain any elements; if the directory
     *     	savingDestDir does not exist; if savingDestDir is not a
     *     	directory; if logsToParse is smaller than 1
     */
    public static final List<Pair<String, Encounter>> createParsedLogs(final File[] mafiaLogs,
                                                                       final File savingDestDir,
                                                                       final LogOutputFormat logVersion,
                                                                       final int logsToParse)
    throws IOException 
    {
        if (!savingDestDir.exists())
            throw new IllegalArgumentException("The directory doesn't exist.");
        if (!savingDestDir.isDirectory())
            throw new IllegalArgumentException("The given file is not a directory.");
        if (logsToParse <= 0)
            throw new IllegalArgumentException("The number of logs to parse must not be below 1.");

        final List<Pair<String, Encounter>> errorFileList 
        	= Collections.synchronizedList(new ArrayList<Pair<String, Encounter>>());

        final File[] condensedMafiaLogs = createCondensedMafiaLogs(mafiaLogs);
        Arrays.sort(condensedMafiaLogs, new Comparator<File>() {
            public int compare(final File o1, final File o2) 
            {
                return o2.getName().compareToIgnoreCase(o1.getName());
            }
        });

        // 4 Threads per CPU should be a high enough number to not slow the
        // computation too much down by scheduler overhead while still making
        // use of threaded computing.
        final ExecutorService executor 
        	= Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 4);

        int logsLeftToParse = logsToParse;
        for (final File f : condensedMafiaLogs) {
            if (logsLeftToParse <= 0)
                break;
            else
                logsLeftToParse--;

            executor.execute(new Runnable() {
                public void run() {
                    final MafiaLogParser parser 
                    	= new MafiaLogParser(f, Settings.getSettingBoolean("Include mafia log notes"));

                    try {
                        parser.parse();

                        if (logVersion == LogOutputFormat.XML_LOG)
                            XMLLogCreator.createXMLLog(parser.getLogData(), savingDestDir);
                        else {
                            final File parsedLog 
                            	= new File(savingDestDir, 
                            			   getParsedLogNameFromCondensedMafiaLog(f.getName(),
                            					   								 logVersion));
                            if (parsedLog.exists())
                                parsedLog.delete();
                            parsedLog.createNewFile();

                            TextLogCreator.saveTextualLogToFile(parser.getLogData(),
                                                                parsedLog,
                                                                logVersion);
                        }
                    } catch (final Exception e) {
                        // Add the erroneous log to the error file list.
                        errorFileList.add(Pair.of(getParsedLogNameFromCondensedMafiaLog(f.getName(),
                                                                                        logVersion),
                                                  (Encounter) parser.getLogData()
                                                                    .getLastTurnSpent()));
                        // Print stack trace and the file name of the file in
                        // which the error happened.
                        System.err.println(f.getName());
                        e.printStackTrace();
                    }
                    // TODO: Flesh out notification when parsing an ascension is complete
                    System.out.println(parser.getLogData().getLogName());
                }
            });
        }

        // Wait for all threads to finish.
        executor.shutdown();
        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
        } catch (final InterruptedException e) {
            e.printStackTrace();
        }

        // Temporary files should be deleted after use. Possible subdirectories
        // are ignored here.
        for (final File f : UtilityConstants.TEMP_LOCATION.listFiles())
            if (!f.isDirectory())
                f.delete();

        return errorFileList;
    }

    /**
     * Takes the file name of a condensed mafia log and changes it into the
     * proper format for parsed ascension logs.
     * <p>
     * File names of condensed mafia logs use the format
     * {@code USERNAME-YYYYMMDD.txt}, where Y is the year, M is the month and D
     * is the day of the first day of that ascension.
     * 
     * @param condensedMafiaLogFileName
     *    	File name of the condensed mafia log.
     * @param logVersion
     *     	The output format of the parsed log.
     * @return The proper parsed ascension log file name.
     */
    public static String getParsedLogNameFromCondensedMafiaLog(final String condensedMafiaLogFileName,
                                                               final LogOutputFormat logVersion) 
    {
        final String userName = condensedMafiaLogFileName.substring(0,
                                                                    condensedMafiaLogFileName.lastIndexOf("-"));
        String tail = condensedMafiaLogFileName.substring(userName.length())
                                               .replace("-", "_ascend");
        if (logVersion == LogOutputFormat.HTML_LOG)
            tail = tail.replace(".txt", ".html");

        return userName + tail;
    }

    /**
     * A helper class to condense mafia logs into holding a single ascension per
     * file.
     */
    private static final class CondensedMafiaLogsCreator {
        private static final Pattern NOT_USER_NAME_PATTERN = Pattern.compile("_\\d+\\.txt");

        private static final Pattern ASCENDED_PATTERN = Pattern.compile("ascend\\.php\\?action=ascend.*confirm=on.*confirm2=on.*|\\s+Beginning New Ascension\\s+");

        private static final List<String> months = Lists.immutableListOf("January",
                                                                         "February",
                                                                         "March",
                                                                         "April",
                                                                         "May",
                                                                         "June",
                                                                         "July",
                                                                         "August",
                                                                         "September",
                                                                         "October",
                                                                         "November",
                                                                         "December");

        private static final FilenameFilter CONDENSED_MAFIA_LOG_FILTER = new FilenameFilter() {
            private final Matcher mafiaLogMatcher = Pattern.compile(".*\\-\\d+\\.txt$").matcher("");

            public boolean accept(
                                  final File dir, final String name) {
                return mafiaLogMatcher.reset(name).matches();
            }
        };

        private final Matcher ascendedMatcher = ASCENDED_PATTERN.matcher("");

        private final File[] mafiaLogs;

        private PrintWriter currentWritingFile;

        /**
         * @param mafiaLogs
         *            The mafia logs which should be turned into parsed
         *            ascension logs.
         * @throws NullPointerException
         *             if mafiaLogs is {@code null}
         * @throws IllegalArgumentException
         *             if mafiaLogs does not contain any elements
         */
        CondensedMafiaLogsCreator(final File[] mafiaLogs) 
        {
            if (mafiaLogs == null)
                throw new NullPointerException("The File array mafiaLogs must not be null.");
            if (mafiaLogs.length == 0)
                throw new IllegalArgumentException("The File array mafiaLogs must not be empty.");

            // Sort array in case it isn't already in the proper order, which is
            // oldest mafia log first.
            Arrays.sort(mafiaLogs, new Comparator<File>() {

                public int compare(
                                   final File o1, final File o2) {
                    return o1.getName().compareToIgnoreCase(o2.getName());
                }
            });
            this.mafiaLogs = mafiaLogs;
        }

        /**
         * Creates and returns condensed mafia logs which hold single ascensions
         * from start to end in a single file.
         * <p>
         * Day changes (the junction between two normal log files of a single
         * ascension) will be separated by the string {@code ===Day
         * _dayNumber_===}, which is in essence the same as the one used in
         * parsed ascension logs to show day changes. Note that if the were
         * multiple days on which no login occurred, it will be catched by the
         * parsing mechanism behind this method, resulting in the right number
         * of day change strings right after each other.
         * <p>
         * There are sometimes cases of KolMafia stating that it is the same
         * real-life date, while the KoL date changed (this strongly depends on
         * the users time zone). If such a case is recognised, the line
         * "Day change occurred" will be added to the player snapshot in which
         * this date change was noticed.
         * <p>
         * Please note that the condensed mafia logs created by this method are
         * stored in the directory for temporal data as denoted by
         * {@link UtilityConstants#TEMP_LOCATION}. These files should be deleted
         * after use.
         * 
         * @return The condensed mafia logs.
         * @throws IOException
         *     	if there was a problem while accessing the given mafia
         *    	logs or writing the condensed ones
         */
        File[] condense()
        throws IOException 
        {
            String userName = mafiaLogs[0].getName()
                                          .substring(0, mafiaLogs[0].getName().lastIndexOf("_"))
                                          .toLowerCase();
            String lastKolDate = null;
            int dayNumber = 1;

            Calendar lastLogDate = UsefulPatterns.getMafiaLogCalendarDate(mafiaLogs[0]);
            openNextWritingFile(mafiaLogs[0].getName());

            for (final File f : mafiaLogs) {
                final String currentLogUserName = f.getName()
                                                   .substring(0, f.getName().lastIndexOf("_"))
                                                   .toLowerCase();
                if (!userName.equals(currentLogUserName)) {
                    openNextWritingFile(f.getName());
                    dayNumber = 1;
                    lastLogDate = UsefulPatterns.getMafiaLogCalendarDate(f);
                    userName = currentLogUserName;
                    lastKolDate = null;
                }

                final Calendar currentLogDate = UsefulPatterns.getMafiaLogCalendarDate(f);
                long dayDiff = (currentLogDate.getTimeInMillis() - lastLogDate.getTimeInMillis()) / 86400000;
                while (dayDiff > 0) {
                    dayDiff--;
                    dayNumber++;
                    lastKolDate = null;
                    currentWritingFile.println();
                    currentWritingFile.println("===Day " + dayNumber + "===");
                    currentWritingFile.println();
                }
                lastLogDate = currentLogDate;

                final BufferedReader br = new BufferedReader(new FileReader(f));
                String tmpLine;

                while ((tmpLine = br.readLine()) != null) {
                    currentWritingFile.println(tmpLine);

                    for (final String s : months)
                        if (tmpLine.startsWith(s) && !tmpLine.startsWith("April Fool's Day")) {
                            final String currentKolDate = tmpLine.substring(tmpLine.lastIndexOf("-") + 2);
                            if (lastKolDate == null)
                                lastKolDate = currentKolDate;
                            else if (!currentKolDate.equals(lastKolDate)) {
                                currentWritingFile.println("Day change occurred");
                                dayNumber++;
                                lastLogDate.add(Calendar.DAY_OF_MONTH, 1);
                                lastKolDate = currentKolDate;
                            }
                        }

                    if (ascendedMatcher.reset(tmpLine).matches()) {
                        openNextWritingFile(f.getName());
                        dayNumber = 1;
                    }
                }

                br.close();
            }

            // Close print-stream after the last log was read.
            if (currentWritingFile != null)
                currentWritingFile.close();

            final File[] condensedMafiaLogs = UtilityConstants.TEMP_LOCATION.listFiles(CONDENSED_MAFIA_LOG_FILTER);
            // Sort array in case it isn't already in the proper order, which is
            // oldest mafia log first.
            Arrays.sort(condensedMafiaLogs, new Comparator<File>() {
                public int compare(final File o1, final File o2)
                {
                    return o1.getName().compareToIgnoreCase(o2.getName());
                }
            });
            return condensedMafiaLogs;
        }

        /**
         * Closes the current PrintWriter if one is present starts a new
         * condensed mafia log with a file name based on the current mafia log.
         * <p>
         * The file name will use the format {@code USERNAME-YYYYMMDD.txt},
         * where Y is the year, M is the month and D is the day of the current
         * mafia log, which also is the start date of the ascension represented
         * be the condensed mafia log.
         * 
         * @param currentMafiaLogFileName
         *     	The file name of the current mafia log.
         */
        private void openNextWritingFile(final String currentMafiaLogFileName)
        throws IOException 
        {
            if (currentWritingFile != null)
                currentWritingFile.close();

            final Scanner scanner = new Scanner(currentMafiaLogFileName);
            scanner.useDelimiter(NOT_USER_NAME_PATTERN);

            final String fileName = scanner.next().replace("_", " ") + "-"
                                    + UsefulPatterns.getLogDate(currentMafiaLogFileName) + ".txt";

            scanner.close();

            currentWritingFile = new PrintWriter(new File(UtilityConstants.TEMP_LOCATION, fileName).getAbsolutePath());
        }
    }
}
