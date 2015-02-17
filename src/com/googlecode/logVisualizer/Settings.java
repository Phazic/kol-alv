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

package com.googlecode.logVisualizer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

import net.java.dev.spellcast.utilities.UtilityConstants;

/**
 * This class handles access to general settings and preferences of the
 * Ascension Log Visualizer.
 * <p>
 * Here is a list of all setting keys (note that the values of these do not
 * necessarily have to be valid):
 * <p>
 * <li>{@code "Version"}: The version string of the Ascension Log Visualizer.</li>
 * <li>{@code "Check Updates"}: TRUE if a check for new versions is done at
 * program startup, otherwise FALSE.</li>
 * <li>{@code "XML format version"}: The version string of the Ascension Log XML
 * file format.</li>
 * <li>{@code "cached XML format version"}: The version string of the Ascension
 * Log XML file format currently used by cached ascensions.</li>
 * <li>{@code "LookAndFeel"}: Preferred look&feel to use by the program.</li>
 * <li>{@code "Mafia logs location"}: File path to the directory that holds
 * mafia logs.</li>
 * <li>{@code "Parsed logs saving location"}: File path to the directory in
 * which parsed ascension logs are saved in.</li>
 * <li>{@code "AFH Parser location"}: File path to the AFH MafiaLog Parser Perl
 * script.</li>
 * <li>{@code "AFH Parser arguments"}: Arguments given to the AFH MafiaLog
 * Parser when it is called through the command line.</li>
 * <li>{@code "AFH Parser user name"}: User name given to the AFH MafiaLog
 * Parser when it is called through the command line.</li>
 * <li>{@code "Using old ascension counting"}: TRUE if old ascension counting is
 * used, otherwise FALSE. Old ascension counting means that an ascension as it
 * appears on the leaderboards ends on ascending, not when King Ralph is freed.</li>
 * <li>{@code "Include mafia log notes"}: TRUE if mafia session log notes are
 * parsed, otherwise FALSE.</li>
 * <li>{@code "Show non-ASCII characters in parsed logs"}: TRUE if non-ASCII
 * characters are to be at all included in parsed logs, otherwise FALSE.</li>
 * <li>{@code "First program startup"}: TRUE if this is the very first startup
 * of the ALV on this machine, even spanning different versions, otherwise
 * FALSE.</li>
 */
public final class Settings {
    private static final String TRUE_STRING = "TRUE";

    private static final String FALSE_STRING = "FALSE";

    private static final Properties DEFAULT_SETTINGS = new Properties();

    private static final File SETTINGS_FILE;

    static {
        SETTINGS_FILE = new File(UtilityConstants.ROOT_DIRECTORY + File.separator
                + UtilityConstants.DATA_DIRECTORY + "ALV settings.txt");

        boolean isNimbusLafPresent = false;
        for (final LookAndFeelInfo lafi : UIManager.getInstalledLookAndFeels())
            if (lafi.getName().equals("Nimbus")) {
                DEFAULT_SETTINGS.setProperty("LookAndFeel", "Nimbus");
                isNimbusLafPresent = true;
                break;
            }
        if (!isNimbusLafPresent)
            DEFAULT_SETTINGS.setProperty("LookAndFeel", "Metal");

        DEFAULT_SETTINGS.setProperty("Version", "3.7.3");
        DEFAULT_SETTINGS.setProperty("Check Updates", TRUE_STRING);

        DEFAULT_SETTINGS.setProperty("XML format version", "1.4");
        DEFAULT_SETTINGS.setProperty("cached XML format version", "0.0");

        DEFAULT_SETTINGS.setProperty("Mafia logs location", "");
        DEFAULT_SETTINGS.setProperty("Parsed logs saving location", "");

        DEFAULT_SETTINGS.setProperty("AFH Parser location", "");
        DEFAULT_SETTINGS.setProperty("AFH Parser arguments", "-all -stats");
        DEFAULT_SETTINGS.setProperty("AFH Parser user name", "");

        DEFAULT_SETTINGS.setProperty("Using old ascension counting", FALSE_STRING);
        DEFAULT_SETTINGS.setProperty("Include mafia log notes", TRUE_STRING);
        DEFAULT_SETTINGS.setProperty("Show non-ASCII characters in parsed logs", TRUE_STRING);

        DEFAULT_SETTINGS.setProperty("First program startup", TRUE_STRING);

        // If settings file hasn't been created yet, create it with default
        // values. Otherwise only make sure that the version number is correct.
        if (SETTINGS_FILE.exists())
            try {
                final Properties p = new Properties();
                final FileInputStream fis = new FileInputStream(SETTINGS_FILE);
                p.load(fis);
                fis.close();

                // The version and XML format version properties have to be set
                // anyway, so that they hold the correct version string.
                p.setProperty("Version", DEFAULT_SETTINGS.getProperty("Version"));
                p.setProperty("XML format version",
                        DEFAULT_SETTINGS.getProperty("XML format version"));

                saveSettingsToFile(p);
            } catch (final IOException e) {
                e.printStackTrace();
                // In case something went wrong go back to default values.
                saveSettingsToFile(DEFAULT_SETTINGS);
            }
        else
            saveSettingsToFile(DEFAULT_SETTINGS);
    }

    /**
     * Writes the given Properties object to the file system.
     */
    private static void saveSettingsToFile(
            final Properties p) {
        synchronized (DEFAULT_SETTINGS) {
            try {
                SETTINGS_FILE.delete();
                SETTINGS_FILE.createNewFile();

                final FileOutputStream fos = new FileOutputStream(SETTINGS_FILE);
                p.store(fos, "This file stores the settings of the Ascension Log Visualizer."
                        + System.getProperty("line.separator")
                        + "#It is not advisable to edit this file by hand.");
                fos.close();
            } catch (final IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Loads the current Properties from the file system.
     */
    private static Properties loadSettingsFromFile()
            throws IOException {
        final Properties p = new Properties(DEFAULT_SETTINGS);

        synchronized (DEFAULT_SETTINGS) {
            final FileInputStream fis = new FileInputStream(SETTINGS_FILE);
            p.load(fis);
            fis.close();
        }

        return p;
    }

    /**
     * Will set the specified setting to the new value.
     *
     * @param key
     *            The name of the setting.
     * @param value
     *            The value of the setting.
     */
    public static void setSettingString(
            final String key, final String value) {
        try {
            final Properties p = loadSettingsFromFile();
            p.setProperty(key, value);
            saveSettingsToFile(p);
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * @param key
     *            The name of the setting.
     * @return The value of the setting. Returns {@code null} if the setting
     *         doesn't exist or there was a problem with reading the settings
     *         file.
     */
    public static String getSettingString(
            final String key) {
        String value = null;

        try {
            value = loadSettingsFromFile().getProperty(key);
        } catch (final IOException e) {
            e.printStackTrace();
        }

        return value;
    }

    /**
     * Will set the specified setting to the new value.
     * <p>
     * Note that settings which aren't already present inside the settings file
     * will be ignored.
     *
     * @param key
     *            The name of the setting.
     * @param value
     *            The value of the setting.
     */
    public static void setSettingBoolean(
            final String key, final Boolean value) {
        if (value)
            setSettingString(key, TRUE_STRING);
        else
            setSettingString(key, FALSE_STRING);
    }

    /**
     * @param key
     *            The name of the setting.
     * @return True if the value string of the setting is equal to TRUE,
     *         otherwise false. Returns {@code null} if the setting doesn't
     *         exist or there was a problem with reading the settings file.
     */
    public static Boolean getSettingBoolean(
            final String key) {
        final String value = getSettingString(key);

        if (value == null)
            return null;

        return value.equals(TRUE_STRING) ? Boolean.TRUE : Boolean.FALSE;
    }

    // This class is not to be instanced.
    private Settings() {}
}
