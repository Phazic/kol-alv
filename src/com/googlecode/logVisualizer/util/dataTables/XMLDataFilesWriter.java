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

package com.googlecode.logVisualizer.util.dataTables;

import java.io.*;
import java.nio.charset.Charset;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import com.bea.xml.stream.XMLOutputFactoryBase;
import com.googlecode.logVisualizer.creator.util.XMLAccessException;

/**
 * This class can write the simple XML data files used by the Ascension Log
 * Visualizer for storing data such as important itemdrops, semi-rare or Bad
 * Moon adventure names, MP regen from equipment, etc.
 * <p>
 * All methods in this class throw a {@link NullPointerException} if a null
 * object reference is passed in any parameter.
 */
final class XMLDataFilesWriter<T> 
{

    /**
     * @param saveDest
     *            The place the data file should be saved to.
     * @param dataWriter
     *            The data writer responsible for the content of the data file.
     * @throws IllegalArgumentException
     *             if the given saveDest references is a directory
     */
    public static <T> void writeXMLDataFile(final File saveDest, 
                                            final DataWriter<T> dataWriter)
    throws XMLAccessException,
            IOException 
    {
        if (saveDest == null)
            throw new NullPointerException("The save destination must not be null.");
        if (dataWriter == null)
            throw new NullPointerException("The data writer must not be null.");

        if (saveDest.isDirectory())
            throw new IllegalArgumentException("Incorrect saving destination, must not be a directory.");

        OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(saveDest),
                                                        Charset.forName("UTF-8"));

        try {
            final XMLOutputFactory factory = XMLOutputFactoryBase.newInstance();
            final XMLStreamWriter writer = factory.createXMLStreamWriter(out);

            writer.writeStartDocument("UTF-8", "1.0");
            writer.writeStartElement(dataWriter.getRootNodeName());
            for (final T item : dataWriter.getDataItems()) {
                writer.writeStartElement(dataWriter.getDataNodeName());
                dataWriter.writeArguments(writer, item);
                writer.writeEndElement();
            }
            writer.writeEndElement();
            writer.writeEndDocument();

            writer.close();
        } catch (final XMLStreamException e) {
            e.printStackTrace();
            throw new XMLAccessException("Could not write to XML file.");
        }

        out.close();
    }

    static abstract class DataWriter<T> 
    {
        private final Iterable<T> dataItems;

        private final String rootNodeName;

        private final String dataNodeName;

        DataWriter(
                   final Iterable<T> dataItems, final String rootNodeName, final String dataNodeName) {
            if (dataItems == null)
                throw new NullPointerException("The data items iterable must not be null.");
            if (rootNodeName == null)
                throw new NullPointerException("The root node name must not be null.");
            if (dataNodeName == null)
                throw new NullPointerException("The data node name must not be null.");

            if (rootNodeName.length() <= 0)
                throw new IllegalArgumentException("The given root node name doesn't contain any characters.");
            if (dataNodeName.length() <= 0)
                throw new IllegalArgumentException("The given data node name doesn't contain any characters.");

            this.dataItems = dataItems;
            this.rootNodeName = rootNodeName;
            this.dataNodeName = dataNodeName;
        }

        final Iterable<T> getDataItems() 
        {
            return dataItems;
        }

        final String getRootNodeName() 
        {
            return rootNodeName;
        }

        final String getDataNodeName() 
        {
            return dataNodeName;
        }

        abstract void writeArguments(final XMLStreamWriter writer, final T item)
        throws XMLStreamException;
    }
}
