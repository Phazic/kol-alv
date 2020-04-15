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

package com.googlecode.alv.util;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.googlecode.alv.Settings;
import com.googlecode.alv.creator.LogsCreator;
import com.googlecode.alv.creator.XMLLogCreator;
import com.googlecode.alv.creator.util.FileAccessException;
import com.googlecode.alv.creator.util.XMLAccessException;
import com.googlecode.alv.logData.turn.Encounter;
import com.googlecode.alv.parser.LogParser;
import com.googlecode.alv.parser.MafiaLogParser;
import com.googlecode.alv.util.Lists;

import net.java.dev.spellcast.utilities.UtilityConstants;

/**
 * This class should be used to handle ascension log caching to limit the amount
 * of parsing necessary to visualise ascension logs. It uses the Ascension Log
 * XML format to store the logs.
 * <p>
 * This class is implemented as an enum to guarantee that there is only one
 * instance present in the program at all times.
 * <p>
 * This implementation is thread-safe, but won't prevent the user from deleting
 * files that he still has a reference to somewhere.
 */
public enum LogsCache {
    CACHE;

    public static final Comparator<File> FILE_COMPARATOR = new Comparator<File>() {
        public int compare(final File o1, final File o2) 
        {
            return o1.getName().compareToIgnoreCase(o2.getName());
        }
    };

    private Map<String, List<File>> logsByCharacterMap = Collections.emptyMap();

    private LogsCache() 
    {
        // If the XML format version changed, we want to delete all cached logs,
        // because there might be incompatibilities.
        final String currentXMLVersion = Settings.getString("XML format version");
        if (!currentXMLVersion.equals(Settings.getString("cached XML format version"))) {
            deleteCache();
            Settings.setString("cached XML format version", currentXMLVersion);
        } else
            reloadCache();
    }

    /**
     * @return A read-only map of all cached log files with their corresponding
     *         character name used as the key.
     */
    public synchronized Map<String, List<File>> getLogsByCharacter() 
    {
        return Collections.unmodifiableMap(logsByCharacterMap);
    }

    /**
     * @return A list of all cached log files sorted alphabetically.
     */
    public synchronized List<File> getLogs() 
    {
        final List<File> logs = Lists.newArrayList(50);
        for (final String character : logsByCharacterMap.keySet())
            logs.addAll(logsByCharacterMap.get(character));

        return Lists.sort(logs, FILE_COMPARATOR);
    }

    /**
     * Caches the given logs. If there were already logs cached with a given
     * name, they will be overwritten.
     * <p>
     * Please note that this class expects condensed mafia logs (see
     * {@link LogsCreator#createCondensedMafiaLogs(File[])}) for further
     * processing.
     * 
     * @param condensedMafiaLogs
     *            The condensed mafia logs to be cached.
     * @return A list containing pairs with filenames and turns of condensed
     *         mafia log files that were attempted to be parsed, but had an
     *         exception thrown during the parsing process. The included turn
     *         the turn after which the exception occurred. This list will be
     *         empty if all files were correctly parsed.
     */
    public synchronized List<Pair<String, Encounter>> createCache(final File[] condensedMafiaLogs) 
    {
        final List<Pair<String, Encounter>> errorFileList 
            = Collections.synchronizedList(new ArrayList<Pair<String, Encounter>>());
        final ExecutorService executor 
            = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2);

        for (final File log : condensedMafiaLogs)
            executor.execute(new Runnable() {
                public void run() {
                    final LogParser logParser 
                        = new MafiaLogParser(log, Settings.getBoolean("Include mafia log notes"));

                    try {
                        logParser.parse();
                        XMLLogCreator.createXMLLog(logParser.getLogData(),
                                                   UtilityConstants.CACHE_LOCATION);
                    } catch (final IOException e) {
                        // Add the erroneous log to the error file list.
                        errorFileList.add(Pair.of(log.getName(),
                                                  (Encounter) logParser.getLogData()
                                                                       .getLastTurnSpent()));
                        e.printStackTrace();
                    } catch (final FileAccessException e) {
                        e.printStackTrace();
                    } catch (final XMLAccessException e) {
                        e.printStackTrace();
                    }
                }
            });

        executor.shutdown();
        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
        } catch (final InterruptedException e) {
            e.printStackTrace();
        }

        reloadCache();

        return errorFileList;
    }

    /**
     * Reloads the internal cached logs collection with the current content of
     * the cache folder. This method will dereference the up until now used map
     * which backs {@link #getLogsByCharacter()}, but won't clear it in case it
     * is still needed on the users side.
     */
    public synchronized void reloadCache() 
    {
        logsByCharacterMap = Maps.newHashMap();

        final File[] cachedFiles = UtilityConstants.CACHE_LOCATION.listFiles();
        Arrays.sort(cachedFiles, FILE_COMPARATOR);

        for (final File f : cachedFiles)
            if (!f.isDirectory()) {
                final int delimiterIndex = f.getName().lastIndexOf("-");
                final String characterName = f.getName().substring(0, delimiterIndex);

                final List<File> characterLogsList;
                if (logsByCharacterMap.containsKey(characterName))
                    characterLogsList = logsByCharacterMap.get(characterName);
                else
                    characterLogsList = Lists.newArrayList(50);

                characterLogsList.add(f);

                logsByCharacterMap.put(characterName, characterLogsList);
            }
    }

    /**
     * Deletes all cached ascension logs.
     */
    public synchronized void deleteCache() 
    {
        for (final File f : UtilityConstants.CACHE_LOCATION.listFiles())
            if (!f.isDirectory())
                f.delete();

        logsByCharacterMap = Collections.emptyMap();
    }
}
