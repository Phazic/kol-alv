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

package com.googlecode.logVisualizer.gui.projectUpdatesViewer;

import java.io.IOException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.*;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.googlecode.logVisualizer.Settings;
import com.googlecode.logVisualizer.util.Lists;

final class ProjectFeedReader {
    static final String CURRENT_ALV_VERSION_FILE_NAME 
        = "AscensionLogVisualizer " + Settings.getSettingString("Version") + ".jar";

    private static final Pattern REVISION_PATTERN = Pattern.compile("\\w{10}\\:");

    private static final Pattern BETWEEN_REVISIONS_PATTERN = Pattern.compile("\\s{5}");

    // Class not to be instanced.
    private ProjectFeedReader() {}

    /**
     * Attempts to read the project updates feed and returns a list with all
     * updates that occurred after this version of the ALV was uploaded.
     * 
     * @return A list of updates newer than this version of the ALV.
     * @throws UnknownHostException
     *             if the server cannot be connected
     */
    static List<ProjectUpdateContainer> readUpdatesFeed(final String url)
    throws IOException,
           ParserConfigurationException,
           SAXException,
           XPathExpressionException 
    {
        final DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
        domFactory.setNamespaceAware(true);
        final DocumentBuilder builder = domFactory.newDocumentBuilder();
        final Document doc = builder.parse(new URL(url).openStream());
        final XPath xpath = XPathFactory.newInstance().newXPath();
        xpath.setNamespaceContext(new GoogleProjectFeedNamespaceContext());

        // First, we want a list of all entry nodes.
        final XPathExpression entries = xpath.compile("//pre:entry");
        final NodeList nodes = (NodeList) entries.evaluate(doc, XPathConstants.NODESET);

        // Now, we'll pull all the interesting bits of data out of every entry
        // node and put them into container classes and put those container
        // classes into the list we want to return in the end.
        final List<ProjectUpdateContainer> updates = Lists.newArrayList(nodes.getLength());
        for (int i = 0; i < nodes.getLength(); i++) {
            String title = "";
            String updated = "";
            String content = "";
            final NodeList childNodes = nodes.item(i).getChildNodes();
            for (int j = 0; j < childNodes.getLength(); j++) {
                final Node node = childNodes.item(j);
                if (node.getNodeName().equals("title"))
                    title = decodeXMLEntities(node.getTextContent().replaceAll("<.+?>|\n", "")).trim();
                else if (node.getNodeName().equals("updated")) {
                    final String tmp = node.getTextContent();
                    updated = tmp.substring(0, tmp.indexOf("T")).trim();
                } else if (node.getNodeName().equals("content")) {
                    content = decodeXMLEntities(node.getTextContent()
                                                    .replaceAll("\n", "")
                                                    .replaceAll("<br>", "\n")
                                                    .replaceAll("<.+?>", "")).trim();

                    // An admittedly rather ugly hack to make the content output
                    // of pushes look a little bit nicer.
                    if (REVISION_PATTERN.matcher(content).find())
                        // Match the strings and put in two \n in place of every
                        // occurrence of the BETWEEN_REVISIONS_PATTERN.
                        content = BETWEEN_REVISIONS_PATTERN.matcher(content).replaceAll("\n\n");
                }
            }

            updates.add(new ProjectUpdateContainer(title, updated, content));

            // We are only interested in updates newer than this version, so we
            // stop the loop on the entry denoting the upload of this version to
            // the hosting site.
            if (title.startsWith(CURRENT_ALV_VERSION_FILE_NAME))
                break;
        }

        return updates;
    }

    private static String decodeXMLEntities(final String xml)
    {
        if (xml.contains("&"))
            return xml.replaceAll("&quot;", "\"")
                      .replaceAll("&amp;", "&")
                      .replaceAll("&apos;", "\u0027")
                      .replaceAll("&lt;", "<")
                      .replaceAll("&gt;", ">");
        else
            return xml;
    }

    /**
     * Namespace interface implementation necessary due to the usage of the Atom
     * namespace by the project updates feed.
     */
    private static final class GoogleProjectFeedNamespaceContext implements NamespaceContext 
    {
        public String getNamespaceURI(final String prefix) 
        {
            if (prefix == null)
                throw new NullPointerException("Null prefix");
            else if ("pre".equals(prefix))
                return "http://www.w3.org/2005/Atom";
            else if ("xml".equals(prefix))
                return XMLConstants.XML_NS_URI;
            return XMLConstants.NULL_NS_URI;
        }

        public String getPrefix(final String uri) 
        {
            return uri.equals("http://www.w3.org/2005/Atom") ? "pre"
                                                            : XMLConstants.DEFAULT_NS_PREFIX;
        }

        // This method isn't necessary for XPath processing.
        public Iterator<String> getPrefixes(final String uri) 
        {
            throw new UnsupportedOperationException();
        }

    }
}
