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

package com.googlecode.logVisualizer.util;

import java.io.*;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

import com.googlecode.logVisualizer.chart.turnrundownGantt.TurnAreaCategory;

/**
 * An utility class to handle saving and loading of category views.
 * <p>
 * Category views are used for the turn rundown gantt charts to put certain
 * areas into one single category. For example all Daily Dungeon rooms could be
 * put into one single Daily Dungeon category.
 * <p>
 * All methods in this class throw a {@link NullPointerException} if a null
 * object reference is passed in any parameter.
 */
public final class CategoryViewFileHandler {
    private static final Pattern CATEGORY = Pattern.compile("^Category name:\\s.+");

    private static final Pattern AREA = Pattern.compile("^\\s{3}.+");

    /**
     * Parses out the area categories from the given category view file.
     * 
     * @param viewFile
     *            A category view file.
     * @return The list of area categories.
     * @throws IOException
     *             if there was a problem with reading the given file
     */
    public static List<TurnAreaCategory> parseOutCategories(
                                                            final File viewFile)
                                                                                throws IOException {
        final List<TurnAreaCategory> categories = Lists.newArrayList();
        final FileInputStream fis = new FileInputStream(viewFile);
        final BufferedReader br = new BufferedReader(new InputStreamReader(fis));
        String tmpLine;

        while ((tmpLine = br.readLine()) != null)
            // Ignore empty lines and comments.
            if (!tmpLine.equals("") && !tmpLine.startsWith("//")) {
                if (CATEGORY.matcher(tmpLine).matches()) {
                    final String categoryName = tmpLine.substring(tmpLine.indexOf(":") + 2);
                    categories.add(new TurnAreaCategory(categoryName));
                }
                if (!categories.isEmpty() && AREA.matcher(tmpLine).matches()) {
                    final String areaName = tmpLine.substring(3);
                    categories.get(categories.size() - 1).addTurnAreaName(areaName);
                }
            }

        br.close();

        return categories;
    }

    /**
     * Saves the given area categories to the given file.
     * <p>
     * Note that if the given file already exists, that file's contents will be
     * replaced with the given categories collection.
     * 
     * @param categories
     *            The area categories to be saved.
     * @param viewFile
     *            The file in which the given category view will be saved in. If
     *            the file already exists, it will be deleted before the
     *            category view is saved.
     * @throws IllegalArgumentException
     *             if the given file is a directory
     * @throws IOException
     *             if there was a problem with writing into the given file
     */
    public static void createCategoryViewFile(
                                              final Collection<TurnAreaCategory> categories,
                                              File viewFile)
                                                            throws IOException {
        if (categories == null)
            throw new NullPointerException("Category list must not be null.");
        if (viewFile.isDirectory())
            throw new IllegalArgumentException("The given file must not be a directory.");

        if (!viewFile.getName().endsWith(".cvw"))
            viewFile = new File(viewFile.getPath() + ".cvw");
        if (viewFile.exists())
            viewFile.delete();

        viewFile.createNewFile();

        final PrintWriter logWriter = new PrintWriter(new BufferedWriter(new FileWriter(viewFile)));

        for (final TurnAreaCategory tlc : categories) {
            logWriter.println("Category name: " + tlc.getCategoryName());

            for (final String s : tlc.getTurnAreaNames())
                logWriter.println("   " + s);

            logWriter.println();
        }

        logWriter.close();
    }

    // This class is not to be instanced.
    private CategoryViewFileHandler() {}
}
