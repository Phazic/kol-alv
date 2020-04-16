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

package com.googlecode.alv;

import static net.java.dev.spellcast.utilities.UtilityConstants.*;

import java.awt.Dimension;
import java.awt.EventQueue;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

import net.java.dev.spellcast.utilities.DataUtilities;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.StandardChartTheme;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.ui.RefineryUtilities;

import com.googlecode.alv.chart.turnrundownGantt.TurnrundownGantt;
import com.googlecode.alv.creator.util.FileAccessException;
import com.googlecode.alv.creator.util.XMLAccessException;
import com.googlecode.alv.creator.util.XMLLogReader;
import com.googlecode.alv.gui.*;
import com.googlecode.alv.gui.LogGUI.GanttPaneButtonListener;
import com.googlecode.alv.gui.LogVisualizerGUI.LogLoaderListener;
import com.googlecode.alv.gui.projectUpdatesViewer.ProjectUpdateViewer;
import com.googlecode.alv.logData.LogDataHolder;
import com.googlecode.alv.parser.LogParser;
import com.googlecode.alv.parser.MafiaLogParser;
import com.googlecode.alv.parser.PreparsedLogParser;
import com.googlecode.alv.util.Lists;
import com.googlecode.alv.util.LogOutputFormat;

public final class LogVisualizer 
{
    static {
        // Create data directories if they do not exist.
        if (!ROOT_LOCATION.exists())
            ROOT_LOCATION.mkdir();
        if (!TEMP_LOCATION.exists())
            TEMP_LOCATION.mkdir();
        if (!CACHE_LOCATION.exists())
            CACHE_LOCATION.mkdir();
        if (!DATA_LOCATION.exists())
            DATA_LOCATION.mkdir();
        if (!KOL_DATA_LOCATION.exists())
            KOL_DATA_LOCATION.mkdir();

        // Delete all files in the temporary directory. Ignore subdirectories.
        for (final File f : TEMP_LOCATION.listFiles())
            if (!f.isDirectory())
                f.delete();

        writeDataFilesToFileSystem();

        // Create normal data files if they do not exist.
        final List<File> normalDataFiles = Lists.newArrayList();
        normalDataFiles.add(new File(ROOT_DIRECTORY + File.separator + DATA_DIRECTORY
                                     + "standardView.cvw"));

        for (final File f : normalDataFiles)
            if (!f.exists()) {
                String tmpLine;
                final BufferedReader br = DataUtilities.getReader(DATA_DIRECTORY, f.getName());

                try {
                    f.createNewFile();
                    final PrintWriter fileWriter = new PrintWriter(f);

                    while ((tmpLine = br.readLine()) != null)
                        fileWriter.println(tmpLine);

                    fileWriter.close();
                    br.close();
                } catch (final IOException e) {
                    e.printStackTrace();
                }
            }

        // Set chart theme back to what it looked like before JFreeChart 1.0.11.
        ChartFactory.setChartTheme(StandardChartTheme.createLegacyTheme());
        BarRenderer.setDefaultBarPainter(new StandardBarPainter());
    }

    private final LogVisualizerGUI gui;

    private LogVisualizer() {
        try {
            final String wantedLaf = Settings.getString("LookAndFeel");
            LookAndFeelInfo usedLaf = null;
            for (final LookAndFeelInfo lafi : UIManager.getInstalledLookAndFeels())
                if (lafi.getName().equals(wantedLaf)) {
                    usedLaf = lafi;
                    break;
                }

            if (usedLaf != null)
                UIManager.setLookAndFeel(usedLaf.getClassName());
            else
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (final Exception e) {
            e.printStackTrace();
        }

        gui = new LogVisualizerGUI(new LogLoaderListener() 
        {
            public void loadMafiaLog(final File file) 
            {
                loadLog(file,
                        new MafiaLogParser(file,
                                           Settings.getBoolean("Include mafia log notes")));
            }

            public void loadPreparsedLog(final File file) 
            {
                loadLog(file, new PreparsedLogParser(file));
            }

            public void loadXMLLog(final File file) 
            {
                try {
                    final LogDataHolder logData = XMLLogReader.parseXMLLog(file);
                    addLogGUI(file, logData);
                } catch (final FileAccessException e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(gui,
                                                  "A problem occurred while reading the file.",
                                                  "Error occurred",
                                                  JOptionPane.ERROR_MESSAGE);
                } catch (final XMLAccessException e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(gui,
                                                  "A problem occurred while parsing the XML.",
                                                  "Error occurred",
                                                  JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        gui.setSize(800, 600);
        RefineryUtilities.centerFrameOnScreen(gui);
        gui.setVisible(true);

        if (Settings.getBoolean("First program startup")) {
            final JLabel text = new JLabel("<html>Note that <b>for the purpose of logging your own runs with KolMafia, it is best</b> to "
                                           + "turn on all options but <i>Log adventures left instead of adventures used</i> under "
                                           + "<i>General->Preferences->Session Logs</i> in KolMafia."
                                           + "<br><br><br>This informational popup will only be displayed this once.</html>");
            text.setPreferredSize(new Dimension(550, 100));
            JOptionPane.showMessageDialog(gui,
                                          text,
                                          "KolMafia logging options",
                                          JOptionPane.INFORMATION_MESSAGE);

            Settings.setBoolean("First program startup", false);
        }

        if (Settings.getBoolean("Check Updates"))
            new Thread(new Runnable() {
                public void run() {
                    if (ProjectUpdateViewer.isNewerVersionUploaded())
                        EventQueue.invokeLater(new Runnable() {
                            public void run() {
                                UpdateDialog.showDialog(gui);
                            }
                        });
                }
            }).start();
    }

    private void loadLog(final File file, final LogParser parser) 
    {
        try {
            parser.parse();
            addLogGUI(file, parser.getLogData());
        } catch (final IOException e) {
            // If there was an IO error of some kind while reading the
            // log file, print the stack trace and show an error dialog,
            // so the user actually knows something is up.
            e.printStackTrace();
            JOptionPane.showMessageDialog(gui,
                                          "There was a problem with reading and/or parsing the ascension log.\n\n"
                                                  + "Make sure that the ascension log file isn't corrupted or doesn't contain any line longer than 500 characters\n"
                                                  + "and try again. If it still doesn't work, try contacting the developers of the Ascension Log Visualizer for\n"
                                                  + "further help.",
                                          "Problem occurred",
                                          JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Adds a {@link LogGUI} with the given logData to the log pane as a task to
     * do on the EventQueue.
     */
    private void addLogGUI(final File log, final LogDataHolder logData) 
    {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                final LogGUI logGUI = new LogGUI(log, logData, !logData.isDetailedLog());
                logGUI.setGanttPanelButtonListener(new GanttPaneButtonListener() {
                    public void areaCategoryCustomizerPressed(
                                                              final TurnrundownGantt turnrundownChart) {
                        new LocationCategoryCustomizer(gui, turnrundownChart);
                    }

                    public void familiarColorizerPressed(
                                                         final TurnrundownGantt turnrundownChart) {
                        new FamiliarUsageCustomizer(gui, turnrundownChart);
                    }
                });

                gui.addLogTab(logGUI);
            }
        });
    }

    /**
     * Creates KoL data files if they do not already exist in the file system.
     */
    public static void writeDataFilesToFileSystem() 
    {
        final List<File> kolDataFiles = Lists.newArrayList();
        kolDataFiles.add(new File(ROOT_DIRECTORY + File.separator + KOL_DATA_DIRECTORY
                                  + "bbcodeAugmentations.txt"));
        kolDataFiles.add(new File(ROOT_DIRECTORY + File.separator + KOL_DATA_DIRECTORY
                                  + "htmlAugmentations.txt"));
        kolDataFiles.add(new File(ROOT_DIRECTORY + File.separator + KOL_DATA_DIRECTORY
                                  + "textAugmentations.txt"));

        for (final File f : kolDataFiles)
            if (!f.exists()) {
                String tmpLine;
                final BufferedReader br = DataUtilities.getReader(KOL_DATA_DIRECTORY, f.getName());

                try {
                    f.createNewFile();
                    final PrintWriter fileWriter = new PrintWriter(f);

                    while ((tmpLine = br.readLine()) != null)
                        fileWriter.println(tmpLine);

                    fileWriter.close();
                    br.close();
                } catch (final IOException e) {
                    e.printStackTrace();
                }
            }
    }

    /**
     * Class that parses and holds command line parameters.
     */
    public static class ALVParameters
    {
        private static final Matcher VALID_DATE = Pattern.compile("^[0-9]{8}$").matcher(""); 
        public boolean isParsing = false;
        public boolean hasError = false;
        public LogOutputFormat format = LogOutputFormat.TEXT_LOG;
        public File srcDir = null;
        public File destDir = null;
        public int ascensionCount = Integer.MAX_VALUE;
        public int ascensionNumber = 0;
        public String playerName = null;
        public String date = null;
        
        public ALVParameters(final String[] args)
        {
            int arg = 0;
            // First, look for options (start with -)
            while (arg < args.length) {
                // Break loop if we're not at an option
                if (! args[arg].startsWith("-"))
                    break;
                // Process the options
                String opt = args[arg];
                switch (opt) {
                case "-p":
                case "-parse":
                case "--parse":
                    isParsing = true;
                    break;
                case "-html":
                case "--html":
                    format = LogOutputFormat.HTML_LOG;
                    break;
                case "-bbcode":
                case "--bbcode":
                    format = LogOutputFormat.BBCODE_LOG;
                    break;
                case "-xml":
                case "--xml":
                    format = LogOutputFormat.XML_LOG;
                    break;
                case "-text":
                case "--text":
                    format = LogOutputFormat.TEXT_LOG;
                    break;
                case "-c":
                case "-count":
                case "--count":
                    arg++;
                    ascensionCount = Integer.parseInt(args[arg]);
                    if (ascensionCount <= 0) {
                        System.out.println("Ascension count must be a positive number");
                        hasError = true;
                        return;
                    }
                    break;
                case "-a":
                case "--ascension":
                    arg++;
                    ascensionNumber = Integer.parseInt(args[arg]);
                    if (ascensionNumber <= 0) {
                        System.out.println("Ascension number must be a positive number");
                        hasError = true;
                        return;
                    }
                   break;
                case "-n":
                case "--name":
                    arg++;
                    playerName = args[arg];
                    break;
                case "-d":
                case "--date":
                    arg++;
                    date = args[arg];
                    VALID_DATE.reset(date);
                    if (! VALID_DATE.find()) {
                        System.out.println("Date must be in the formay yyyymmdd");
                        hasError = true;
                        return;
                    }
                    break;
                default:
                    System.out.println("Unrecognized option " + opt);
                    hasError = true;
                    return;
                }
                
                // Next!
                arg++;
            }
            // arg now points to the first non-option
            // Non-option parameters are source log directory and destination log directory
            if (arg < args.length)
                srcDir = new File(args[arg]);
            if (arg+1 < args.length)
                destDir = new File(args[arg+1]);
        }
    }
    
    /**
     * Main method for Ascension Log Visualizer.
     * @param args Arguments passed in from the caller.
     */
    public static void main(final String[] args) 
    {
        System.out.println("Ascension Log Visualizer build " + Settings.ALV_VERSION);
        ALVParameters params = new ALVParameters(args);
        if (params.hasError)
            return;
        if (params.isParsing) {
            LogVisualizerCLI.runCLIParsing(params);
        }
        else
            EventQueue.invokeLater(new Runnable() {
                public void run() 
                {
                    new LogVisualizer();
                }
            });
    }
}
