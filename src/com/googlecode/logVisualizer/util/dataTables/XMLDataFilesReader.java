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

package com.googlecode.logVisualizer.util.dataTables;

import java.io.*;

import java.util.List;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import net.java.dev.spellcast.utilities.DataUtilities;
import net.java.dev.spellcast.utilities.UtilityConstants;

import com.googlecode.logVisualizer.util.Lists;
import com.googlecode.logVisualizer.util.Pair;
import com.googlecode.logVisualizer.util.xmlLogs.XMLAccessException;

/**
 * This class can read the simple XML data files used by the Ascension Log
 * Visualizer for storing data such as important itemdrops, semi-rare or Bad
 * Moon adventure names, MP regen from equipment, etc.
 * <p>
 * All methods in this class throw a {@link NullPointerException} if a null
 * object reference is passed in any parameter.
 */
final class XMLDataFilesReader {
    static {
        System.setProperty("javax.xml.stream.XMLInputFactory", "com.bea.xml.stream.MXParserFactory");
        System.setProperty("javax.xml.stream.XMLEventFactory", "com.bea.xml.stream.EventFactory");
    }

    /**
     * Returns a list of lists containing the key-value pair of every argument
     * in the given data node.
     * 
     * @param filename
     *            The XML data file name which is supposed to be parsed.
     * @param dataNodeName
     *            The name of the data node in the XML data file, which has all
     *            its data contained in its arguments.
     * @return The resulting data from the given XML data file.
     * @throws IllegalArgumentException
     *             if the given file name doesn't contain any characters; if the
     *             given data node name doesn't contain any characters
     */
    static List<List<Pair<String, String>>> parseXMLDataFile(
                                                             final String filename,
                                                             final String dataNodeName)
                                                                                       throws XMLAccessException {
        if (filename == null)
            throw new NullPointerException("The file name must not be null.");
        if (dataNodeName == null)
            throw new NullPointerException("The data node name must not be null.");

        if (filename.length() <= 0)
            throw new IllegalArgumentException("The given file name doesn't contain any characters.");
        if (dataNodeName.length() <= 0)
            throw new IllegalArgumentException("The given data node name doesn't contain any characters.");

        final List<List<Pair<String, String>>> resultList;

        final InputStream in = DataUtilities.getInputStream(UtilityConstants.KOL_DATA_DIRECTORY,
                                                            filename);

        // File doesn't exist
        if (in == DataUtilities.EMPTY_STREAM)
            return Lists.newArrayList();

        try {
            final XMLInputFactory factory = XMLInputFactory.newInstance();
            final XMLStreamReader parser = factory.createXMLStreamReader(in);

            final XMLDataFilesReader reader = new XMLDataFilesReader(parser, dataNodeName);
            resultList = reader.parseDataFile();

            parser.close();
        } catch (final XMLStreamException e) {
            e.printStackTrace();
            throw new XMLAccessException("Could not read XML file.");
        }

        try {
            in.close();
        } catch (final IOException e) {
            e.printStackTrace();
        }

        return resultList;
    }

    private final XMLStreamReader parser;

    private final String dataNodeName;

    private XMLDataFilesReader(
                               final XMLStreamReader parser, final String dataNodeName) {
        if (parser == null)
            throw new NullPointerException("The XML parser must not be null.");
        if (dataNodeName == null)
            throw new NullPointerException("The data node name must not be null.");

        this.parser = parser;
        this.dataNodeName = dataNodeName;
    }

    private List<List<Pair<String, String>>> parseDataFile()
                                                            throws XMLStreamException {
        final List<List<Pair<String, String>>> result = Lists.newArrayList(200);

        while (parser.hasNext()) {
            switch (parser.getEventType()) {
                case XMLStreamConstants.START_ELEMENT:
                    if (parser.getLocalName().equals(dataNodeName)) {
                        final List<Pair<String, String>> arguments = Lists.newArrayList(4);
                        for (int i = 0; i < parser.getAttributeCount(); i++)
                            arguments.add(Pair.of(parser.getAttributeLocalName(i),
                                                  parser.getAttributeValue(i)));

                        result.add(arguments);
                    }

                    break;
                default:
                    break;
            }
            parser.next();
        }

        return result;
    }
}
