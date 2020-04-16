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

import java.awt.BorderLayout;
import java.awt.Component;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import javax.swing.*;

import com.googlecode.alv.logData.turn.Encounter;
import com.googlecode.alv.parser.UsefulPatterns;
import com.googlecode.alv.util.Pair;

/**
 * A helper class only containing static fields and methods to easily display an
 * error dialog showing a table of log names and turn numbers of when the
 * parsing error occurred.
 */
public final class ErrorLogTableDialog {
    private static final String ERROR_PREFACE = "<html>There were problems parsing the following logs. Please check the underlaying mafia session logs to see<br>"
                                                + "if they contained any corrupted data or lines longer than 500 characters and try to remove any problems.<br><br><br>"
                                                + "The given list lists the erroneous ascension and turn number after which the error occurred in the mafia<br>"
                                                + "session logs upon which the ascension is based on.<br><br></html>";

    public static void showErrorLogTableDialog(
                                               final Component owner,
                                               final List<Pair<String, Encounter>> errorFileList) {
        final JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JLabel(ERROR_PREFACE), BorderLayout.NORTH);
        panel.add(new JScrollPane(createErrorLogTable(errorFileList)), BorderLayout.CENTER);

        JOptionPane.showMessageDialog(owner, panel, "Error occurred", JOptionPane.ERROR_MESSAGE);
    }

    private static JTable createErrorLogTable(
                                              final List<Pair<String, Encounter>> errorFileList) {
        final NumberFormat formatter = NumberFormat.getNumberInstance(Locale.ENGLISH);
        formatter.setMaximumFractionDigits(0);
        formatter.setMinimumIntegerDigits(2);
        final String[] columNames = { "Player name", "Ascension start date",
                                     "Specific session log", "Turn number" };
        final String[][] rowData = new String[errorFileList.size()][4];
        for (int i = 0; i < errorFileList.size(); i++) {
            final Calendar logDate = UsefulPatterns.getMafiaLogCalendarDate(errorFileList.get(i)
                                                                                         .getVar1());
            for (int j = 1; j < errorFileList.get(i).getVar2().getDayNumber(); j++)
                logDate.add(Calendar.DAY_OF_MONTH, 1);

            final String[] logData = parseLogName(errorFileList.get(i).getVar1());
            rowData[i][0] = logData[0];
            rowData[i][1] = logData[1];
            rowData[i][2] = logData[0] + "_" + logDate.get(Calendar.YEAR) + ""
                            + formatter.format(logDate.get(Calendar.MONTH) + 1) + ""
                            + formatter.format(logDate.get(Calendar.DAY_OF_MONTH));
            rowData[i][3] = Integer.toString(errorFileList.get(i).getVar2().getTurnNumber());
        }

        return new JTable(rowData, columNames);
    }

    private static String[] parseLogName(
                                         final String logName) {
        final String[] result = new String[2];

        if (logName.contains("_ascend")) {
            final String[] tmp = logName.split("_ascend|\\.txt|\\.html");
            result[0] = tmp[0];
            result[1] = tmp[1];
        } else {
            final int delimiterIndex = logName.lastIndexOf("-");
            result[0] = logName.substring(0, delimiterIndex);
            result[1] = logName.substring(delimiterIndex + 1, logName.lastIndexOf("."));
        }

        return result;
    }
}
