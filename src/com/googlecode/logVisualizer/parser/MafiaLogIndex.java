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

package com.googlecode.logVisualizer.parser;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.googlecode.logVisualizer.gui.InternalMafiaLogParserDialog;

/**
 * This class represents an index of Mafia logs.  It associates ascensions with the
 * dates on which they begin, and hence the Mafia log files in which they start,
 * 
 */
public class MafiaLogIndex {

    private static final String INDEX_NAME = "alv-index";
    
    private static final Matcher ASCENSION_NUMBER_MATCHER
        = Pattern.compile("^Ascension #([0-9]*):").matcher("");
    
    private static final Matcher INDEX_LINE_MATCHER 
        = Pattern.compile("([^ ]*) ([^ ]*) (.*_([0-9]+)[.].*)").matcher("");
    
    private static final Matcher LOG_FILE_NAME_MATCHER 
        = Pattern.compile("(.*)_([0-9]+).txt").matcher("");
    
    private static final Map<String, MafiaLogIndex> indexCache
        = new TreeMap<String, MafiaLogIndex>();
    
    private static final File[] emptyFiles = { };
    
    private File indexFile;
    
    private final Map<String, TreeMap<Integer, SortedSet<File>>> indexByNumber
        = new TreeMap<String, TreeMap<Integer, SortedSet<File>>>();
    
    private final Map<String, TreeMap<String, Integer>> indexByDate
        = new TreeMap<String, TreeMap<String, Integer>>();
    
    private final Map<String, TreeSet<File>> includedFiles = new HashMap<String, TreeSet<File>>();
    
    private String logDirectoryPath;

    /**
     * Get the current index for the given Mafia log directory from cache, loading the
     * index from the filesystem if necessary.
     * 
     * @param logDirPath Path to the Mafia log directory
     * @return MafiaLogIndex for the given directory
     * @throws IOException If an error occurs when building the index from the filesystem.
     */
    public static MafiaLogIndex getMafiaLogIndex(String logDirPath)
    throws IOException
    {
        MafiaLogIndex result = indexCache.get(logDirPath);
        if (result == null) {
            result = new MafiaLogIndex(logDirPath);
            indexCache.put(logDirPath, result);
        }
        return result;
    }
    
    /**
     * Create an index with Mafia log data from the filesystem.
     * 
     * @param logDirectoryPath Path to the Mafia log directory
     * @throws IOException If an error occurs when accessing the filesystem
     */
    private MafiaLogIndex(String logDirPath)
    throws IOException
    {
        System.out.println("Constructing index...");
        logDirectoryPath = logDirPath;
        
        indexFile = new File(logDirPath, INDEX_NAME);
        // Read in the current index file if it exists
        if (indexFile.exists())
            load();
        // Add whatever log files are missing
        updateIndex(logDirectoryPath);
        // Save the result
        System.out.println("Index constructed.");
        save();
    }

    private void addDateEntry(String playerName, String date, int number)
    {
        // Do nothing if number is invalid
        if (number < 0)
            return;
        
        TreeMap<String, Integer> ascDates = indexByDate.get(playerName);
        if (ascDates == null) {
            ascDates = new TreeMap<String, Integer>();
            indexByDate.put(playerName, ascDates);
        }
        ascDates.put(date, number);        
    }
    
    private void addNumberEntry(String playerName, int number, File file)
    {
        // Do nothing if number is invalid
        if (number < 0)
            return;
        
        TreeMap<Integer, SortedSet<File>> ascFileLists = indexByNumber.get(playerName);
        if (ascFileLists == null) {
            ascFileLists = new TreeMap<Integer, SortedSet<File>>();
            indexByNumber.put(playerName, ascFileLists);
        }
        SortedSet<File> files = ascFileLists.get(number);
        if (files == null) {
            files = new TreeSet<File>();
            ascFileLists.put(number, files);
        }
        files.add(file);
        TreeSet<File> playersFiles = includedFiles.get(playerName);
        if (playersFiles == null) {
            playersFiles = new TreeSet<File>();
            includedFiles.put(playerName, playersFiles);
        }
        playersFiles.add(file);
    }
    
    /**
     * Load index file into internal Map.
     */
    private void load()
            throws IOException
    {
        try ( BufferedReader br = new BufferedReader(new FileReader(indexFile)) ) {
            String line;
            while ((line = br.readLine()) != null) {
                INDEX_LINE_MATCHER.reset(line);
                if (INDEX_LINE_MATCHER.find()) {
                    String playerName = INDEX_LINE_MATCHER.group(1);
                    int ascNumber = Integer.parseInt(INDEX_LINE_MATCHER.group(2));
                    String fileName = INDEX_LINE_MATCHER.group(3);
                    String date = INDEX_LINE_MATCHER.group(4);
                    File namedFile = new File(logDirectoryPath, fileName);
                    addDateEntry(playerName, date, ascNumber);
                    addNumberEntry(playerName, ascNumber, namedFile);
                }
            }
        }
    }
    
    /**
     * Save data to index file.
     */
    private void save()
            throws IOException
    {
        BufferedWriter bw = new BufferedWriter(new FileWriter(indexFile));
        for (Map.Entry<String, TreeMap<Integer, SortedSet<File>>> entry : indexByNumber.entrySet()) {
            String playerName = entry.getKey();
            for (Map.Entry<Integer, SortedSet<File>> subentry : entry.getValue().entrySet()) {
                int ascNumber = subentry.getKey();
                for (File f : subentry.getValue()) {
                    bw.append(playerName + " " + ascNumber + " " + f.getName());
                    bw.append(System.lineSeparator());
                    bw.flush();
                }
            }
        }
        bw.close();
    }
    
    /**
     * 
     * @param f File object for Mafia log to read
     * @param ascensionNumber Current ascension number
     * @return New ascension number (may be the same)
     * @throws IOException If an error occurred reading the file
     */
    private int readLogFileIntoIndex(File f, int ascensionNumber)
    throws IOException
    {
        // If we can't open the file, return unchanged number
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(f));
        } catch (IOException e) {
            System.out.println(e);
        }
        if (br == null) {
            return ascensionNumber;
        }
        
        // Extract player name and date from file name
        String logFileName = f.getName();
        LOG_FILE_NAME_MATCHER.reset(logFileName);
        if (! LOG_FILE_NAME_MATCHER.find()) {
            // If it doesn't conform, return unchanged number
            System.out.println("Warning: Filename " + logFileName 
                               + "Does not conform to log name pattern.");
            return ascensionNumber;
        }
        // Extract player name and date (yyyymmdd)
        String playerName = LOG_FILE_NAME_MATCHER.group(1);
        String date = LOG_FILE_NAME_MATCHER.group(2);
        
        String line;
        try {
            int newNumber = ascensionNumber;    // initialize
            // Search file for ascension transitions
            while ((line = br.readLine()) != null) {
                if (line.indexOf("Ascension #") < 0) 
                    continue;
                ASCENSION_NUMBER_MATCHER.reset(line);
                if (ASCENSION_NUMBER_MATCHER.find()) {
                    // Extract ascension number
                    newNumber = Integer.parseInt(ASCENSION_NUMBER_MATCHER.group(1));
                    // Add data to indexes
                    addNumberEntry(playerName, newNumber, f);
                    addDateEntry(playerName, date, newNumber);
                }
            }
            if (ascensionNumber == newNumber) {
                // If the file did not introduce a new ascension, add entries anyway
                addDateEntry(playerName, date, ascensionNumber);
                addNumberEntry(playerName, ascensionNumber, f);
            } else {
                // If the file did have a new ascension, add entries for previous ascensions
                if (ascensionNumber >= 0) {
                    for (int n = ascensionNumber; n < newNumber; n++)
                        addNumberEntry(playerName, n, f);
                }
                // On new ascension now
                ascensionNumber = newNumber;
            }
        } catch (IOException e) {
            System.out.println(e);
        } finally {
            br.close();
        }    
        return ascensionNumber;
    }
    
    /**
     * We've already loaded the info for this file, so we're just going to pretend we
     * read the file for ascension number info, but really we're just getting it from 
     * the index.
     * 
     * @param f The Mafia log file whose info will come from the index
     * @param ascensionNumber The current ascension number in our scan
     * @return The new ascension number (may be unchanged)
     */
    private int readLogFileFromIndex(File f, int ascensionNumber)
    {
        // Look up file in index; get date
        LOG_FILE_NAME_MATCHER.reset(f.getName());
        if (! LOG_FILE_NAME_MATCHER.find())
            // This shouldn't happen
            return ascensionNumber;
        String playerName = LOG_FILE_NAME_MATCHER.group(1);
        String date = LOG_FILE_NAME_MATCHER.group(2);
        // Return which ascension this date belongs to.  
        return indexByDate.get(playerName).get(date);
    }
    
    /**
     * Search the Mafia logs in the directory for the line "Ascension #{number}:"
     * Use these lines to build index file.
     * 
     * @param logDirectoryPath Full path to the log directory
     * @throws IOException if a problem occurs with reading the Mafia logs
     */
    private void updateIndex(String logDirectoryPath)
            throws IOException
    {
        // We'll have to search all Mafia log files in the directory
        File mafiaLogsDirectory = new File(logDirectoryPath);
        final File[] mafiaLogs = mafiaLogsDirectory.listFiles(InternalMafiaLogParserDialog.MAFIA_LOG_FILTER);
        // Make sure they're sorted.  Alphabetical separates by player name as well as sorting by date
        List<File> sortedMafiaLogs = Arrays.asList(mafiaLogs);
        Collections.sort(sortedMafiaLogs);
        // If we're going through the files in order now, then we can keep track of 
        // ascension numbers with an int
        String playerName = "";
        int ascensionNumber = -1;
        for (final File f : sortedMafiaLogs) {
            // If player name has changed, reset ascension number
            LOG_FILE_NAME_MATCHER.reset(f.getName());
            if (! LOG_FILE_NAME_MATCHER.find())
                // If file name doesn't match pattern, skip it
                continue;
            String currName = LOG_FILE_NAME_MATCHER.group(1);
            if (! currName.equals(playerName)) {
                playerName = currName;
                ascensionNumber = -1;
            }
            // If file doesn't exist, read it for ascension number changes
            TreeSet<File> playerFiles = includedFiles.get(playerName);
            if (playerFiles != null
                    && playerFiles.contains(f)) {
                // If file already present, derive info from log
                ascensionNumber = readLogFileFromIndex(f, ascensionNumber);
            } else {
                // If file new, load it in
                ascensionNumber = readLogFileIntoIndex(f, ascensionNumber);    
            }
        }
    }
    
    private String mostActivePlayer()
    {
        String result = null;
        int max = 0;
        for (Map.Entry<String, TreeSet<File>> entry : includedFiles.entrySet()) {
            int s = entry.getValue().size();
            if (s > max) {
                result = entry.getKey();
                max = s;
            }
        }        
        return result;
    }
    
    /**
     * Get a list of Mafia log files covering the last N ascensions for a given player.
     * 
     * @param n Number of ascensions
     * @param playerName Name of the player for whom to get files
     * @return Array of File objects representing the Mafia logs covering those ascensions
     */
    public File[] getLastNMafiaLogs(int n, String playerName)
    {
        if (playerName == null) {
            // If no player name, choose player with the most log files
            playerName = mostActivePlayer();
        }
        if ((playerName == null) || (!includedFiles.containsKey(playerName)) ) 
            return emptyFiles;
        
        // What's the player's last ascension?
        TreeMap<Integer, SortedSet<File>> ascensions = indexByNumber.get(playerName);
        Integer lastAsc = ascensions.lastKey();
        // Collect all the files, in order, for the last n ascensions
        TreeSet<File> mafiaLogs = new TreeSet<File>();
        while ((lastAsc != null) && (n > 0)) {
            mafiaLogs.addAll(ascensions.get(lastAsc));
            n--;
            lastAsc = ascensions.lowerKey(lastAsc);
        }
        return mafiaLogs.toArray(new File[mafiaLogs.size()]);
    }
    
    /**
     * Get a list of Mafia log files covering the Nth ascension for a given player.
     * 
     * @param asc Integer identifying the ascension to process
     * @param playerName Player whose ascension logs are to be gathered
     * @return Array of File objects representing the Mafia logs covering that ascension
     */
    public File[] getAscensionN(int asc, String playerName)
    {
        if (playerName == null) {
            // If no player name, choose player with the most log files
            playerName = mostActivePlayer();
        }
        if ((playerName == null) || (!includedFiles.containsKey(playerName)) ) 
            return emptyFiles;
        
        // Get files for that ascension
        SortedSet<File> mafiaLogs = indexByNumber.get(playerName).get(asc);
        return mafiaLogs.toArray(new File[mafiaLogs.size()]);        
    }
    
    /**
     * Get a list of Mafia log files covering the ascension for a given player that
     * includes that date.  On days where the player ascends (maybe twice), the date 
     * is assumed to belong to the last ascension that was begun on that date.
     * 
     * @param date String of the form yyyymmdd denoting the date
     * @param playerName Player whose ascension logs are to be gathered
     * @return Array of File objects representing the Mafia logs covering that ascension
     */
    public File[] getAscensionForDate(String date, String playerName)
    {
        if (playerName == null) {
            // If no player name, choose player with the most log files
            playerName = mostActivePlayer();
        }
        if ((playerName == null) || (!includedFiles.containsKey(playerName)) ) 
            return emptyFiles;
        
        // Get ascension number
        Integer asc = indexByDate.get(playerName).get(date);
        if (asc == null)
            return emptyFiles;
        // Get files for that ascension
        SortedSet<File> mafiaLogs = indexByNumber.get(playerName).get(asc);
        return mafiaLogs.toArray(new File[mafiaLogs.size()]);
    }
    
    /**
     * Dump the contents of the indexes to stdout.  Useful only for debugging.
     */
    public void dump()
    {
        for (Map.Entry<String, TreeMap<Integer, SortedSet<File>>> entry : indexByNumber.entrySet()) {
            System.out.println(entry.getKey());
            for (Map.Entry<Integer, SortedSet<File>> subentry : entry.getValue().entrySet()) {
                System.out.println("    " + subentry.getKey());
                for (File f : subentry.getValue())
                    System.out.println("        " + f.getName());
            }
        }
        for (Map.Entry<String, TreeMap<String, Integer>> entry : indexByDate.entrySet()) {
            System.out.println(entry.getKey());
            for (Map.Entry<String, Integer> subentry : entry.getValue().entrySet()) { 
                System.out.println("    " + subentry.getKey() + ": " + subentry.getValue());
            }
        }
    }
}
