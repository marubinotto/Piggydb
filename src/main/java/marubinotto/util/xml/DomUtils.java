package marubinotto.util.xml;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import marubinotto.util.Assert;

import org.apache.commons.lang.UnhandledException;
import org.w3c.dom.Attr;
import org.w3c.dom.CharacterData;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * <p>Utilities related to handling DOM.</p>
 *
 * <p>This utility class uses JAXP(Java API for XML Processing)
 * to parse and transform XML documents internally. JAXP is independent of a particular
 * XML processing implementation. To switch parser implementation behind JAXP,
 * you can find detail instructions at {@link DocumentBuilderFactory#newInstance}.</p>
 *
 * <h4>XML parser configuration</h4>
 * <p>The list below describes the configurations of JAXP configured
 * by this utility as default.</p>
 * <ul>
 *   <li>{@link DocumentBuilderFactory#isCoalescing} => false</li>
 *   <li>{@link DocumentBuilderFactory#isExpandEntityReferences} => true</li>
 *   <li>{@link DocumentBuilderFactory#isIgnoringComments} => false</li>
 *   <li>{@link DocumentBuilderFactory#isIgnoringElementContentWhitespace} => false</li>
 *   <li>{@link DocumentBuilderFactory#isNamespaceAware} => true</li>
 * </ul>
 *
 * <p>Check the XML1.0 W3C Recommendation
 * (<a href="http://www.w3.org/TR/2000/REC-xml-20001006">
 * http://www.w3.org/TR/2000/REC-xml-20001006</a>)
 * for more detail about XML specification.</p>
 *
 * @version $Id: DomUtils.java 1286 2008-03-20 15:39:37Z morita $
 */
public class DomUtils {

    public static final String XML_NAMESPACE =
        "http://www.w3.org/XML/1998/namespace";
    public static final String XML_SCHEMA_NAMESPACE =
        "http://www.w3.org/2001/XMLSchema-instance";

    public static Document buildNewDocument() {
        DocumentBuilder builder = createDocumentBuilder(false);
        return builder.newDocument();
    }

    /**
     * <p>Builds a DOM tree parsing the given String.</p>
     */
    public static Document buildDocument(String xml)
    throws SAXException, IOException {
        Assert.Arg.notNull(xml, "xml");
        DocumentBuilder builder = createDocumentBuilder(false);
        return builder.parse(new InputSource(new StringReader(xml)));
    }

    /**
     * <p>Builds a DOM tree parsing the specificed file.</p>
     */
    public static Document buildDocument(File xmlFile)
    throws SAXException, IOException {
        Assert.Arg.notNull(xmlFile, "xmlFile");
        DocumentBuilder builder = createDocumentBuilder(false);
        return builder.parse(xmlFile);
    }

    /**
     * <p>Builds a DOM tree parsing the given stream data.</p>
     */
    public static Document buildDocument(InputStream input)
    throws SAXException, IOException {
        Assert.Arg.notNull(input, "input");
        DocumentBuilder builder = createDocumentBuilder(false);
        return builder.parse(input);
    }

    /**
     * <p>Converts the given DOM node to String.</p>
     *
     * <p>If the given node is an <b>Attr</b> or
     * <b>CharacterData (CDATASection, Comment, Text),</b>
     * the node will be converted to its value.</p>
     */
    public static String toString(Node node) {
        Assert.Arg.notNull(node, "node");

        if (node instanceof Attr) {
            return ((Attr)node).getValue();
        }
        else if (node instanceof CharacterData) {
            return ((CharacterData)node).getData();
        }
        StringWriter buffer = new StringWriter();
        try {
            serialize(node, new StreamResult(buffer), "UTF-8");
        }
        catch (TransformerException e) {
            throw new UnhandledException(e);
        }
        return buffer.toString();
    }

    /**
     * <p>Serializes the given DOM node to the specified file.</p>
     *
     * @param node node to be serialized
     * @param file file to which the given node will be serialized
     * @param encoding name of a supported XML charset
     * @throws IOException
     */
    public static void serialize(Node node, File file, String encoding)
    throws TransformerException, IOException {
        // J2SE 5.0 StreamResult bug?
        // serialize(node, new StreamResult(file), encoding);

        OutputStream output = new BufferedOutputStream(new FileOutputStream(file));
        try {
            serialize(node, output, encoding);
        }
        finally {
            output.close();
        }
    }

    /**
     * <p>Serializes the given DOM node to the given OutputStream.</p>
     *
     * @param node node to be serialized
     * @param output OutputStream to which the given node will be serialized
     * @param encoding name of a supported XML charset
     */
    public static void serialize(Node node, OutputStream output, String encoding)
    throws TransformerException {
        serialize(node, new StreamResult(output), encoding);
    }

    /**
     * Returns a boolean value indicating whether the given Element is a root Element.
     */
    public static boolean isRootElement(Element element) {
        Assert.Arg.notNull(element, "element");
        return element.getParentNode() instanceof Document;
    }

    /**
     * Returns the language code of the given node.
     * The language code is specified with an "xml:lang" attribute.
     * If the given node does not have an "xml:lang" attribute,
     * the parents of the node will be examined recursively.
     * If no "xml:lang" attributes are found, this method returns null.
     */
    public static String getLang(Node node) {
        Assert.Arg.notNull(node, "node");
        while (true) {
            if (node instanceof Element) {
                Attr lang = ((Element) node).getAttributeNodeNS(XML_NAMESPACE, "lang");
                if (lang != null) {
                    return lang.getValue();
                }
            }
            node = node.getParentNode();
            if (node == null) {
                break;
            }
        }
        return null;
    }

    /**
     * Selects the node from the given list, that is the first node
     * of those which correspond to the specified locale(language code).
     * If a corresponding node is not found, the first node of those which
     * do not have an "xml:lang" attribute will be returned as a default.
     */
    public static Node selectByLang(List<Node> nodes, Locale locale) {
        Assert.Arg.notNull(nodes, "nodes");
        Assert.Arg.notNull(locale, "locale");

        if (nodes.size() == 0) {
            return null;
        }

        Node firstNoLangNode = null;
        for (Iterator<Node> i = nodes.iterator(); i.hasNext();) {
            Node node = i.next();
            String lang = getLang(node);
            if (lang == null && firstNoLangNode == null) {
                firstNoLangNode = node;
            }
            if (lang != null && lang.equals(locale.getLanguage())) {
                return node;
            }
        }
        return firstNoLangNode;
    }

    /**
     * Sets the specified locale(language code) to the given element
     * as an "xml:lang" attribute.
     */
    public static void setLang(Element element, Locale locale) {
        Assert.Arg.notNull(element, "element");
        Assert.Arg.notNull(locale, "locale");
        element.setAttributeNS(XML_NAMESPACE, "xml:lang", locale.getLanguage());
    }

    /**
     * Adds the given XML fragment to the given node as the last child.
     * The context of namespace will be applied to the fragment.
     */
    public static Node addFragment(String fragment, Node node)
	throws SAXException, IOException {
        Assert.Arg.notNull(fragment, "fragment");
        Assert.Arg.notNull(node, "node");
        return node.appendChild(buildFragment(fragment, node));
    }

    /**
     * Adds the given XML fragment to the given node as the first child.
     * The context of namespace will be applied to the fragment.
     */
    public static Node addFragmentFirst(String fragment, Node node)
	throws SAXException, IOException {
        Assert.Arg.notNull(fragment, "fragment");
        Assert.Arg.notNull(node, "node");
        if (node.getChildNodes().getLength() > 0) {
            return node.insertBefore(buildFragment(fragment, node), node.getFirstChild());
        }
        else {
            return node.appendChild(buildFragment(fragment, node));
        }
    }

    /**
     * <p>Evaluates the given node as a string.</p>
     *
     * <p>If the given node is an <b>Attr</b> or
     * <b>CharacterData (CDATASection, Comment, Text),</b>
     * Its value will be returned.</p>
     *
     * <p>If the given node is an element,
     * returns the textual content directly held under this element as a string.
     * This includes all text within this single element,
     * including whitespace and CDATA sections if they exist.
     * The call does not recurse into child elements.
     * If no textual value exists for the element, a null value is returned.</p>
     */
    public static String getValue(Node node) {
        Assert.Arg.notNull(node, "node");
        if (node instanceof CharacterData) {
            return ((CharacterData)node).getData();
        }
        else if (node instanceof Element) {
            return getText((Element)node);
        }
        else {
            return node.getNodeValue();
        }
    }

    /**
     * <p>Sets the String value to the given node.</p>
     *
     * <p>If the given node is an element,
     * Sets the content of the element to be the text given.
     * All existing text content and non-text context is removed.</p>
     */
    public static void setValue(Node node, String value) {
        Assert.Arg.notNull(node, "node");
        if (node instanceof CharacterData) {
            ((CharacterData)node).setData(value);
        }
        else if (node instanceof Element) {
            removeAllChildNodes(node);
            node.appendChild(node.getOwnerDocument().createTextNode(value));
        }
        else {
            node.setNodeValue(value);
        }
    }

    /**
     * Removes all child nodes from the given node.
     */
    public static void removeAllChildNodes(Node node) {
        NodeList children = node.getChildNodes();
        while (children.getLength() > 0) {
            node.removeChild(children.item(0));
        }
    }

    /**
     * Returns all namespace declaration attributes that
     * the ancestor-or-self nodes of the given node have.
     * A name of a namespace declaration attribute starts with "xmlns".
     */
    public static Attr[] collectNamespaceDefinitions(Node node) {
        Assert.Arg.notNull(node, "node");

        List<Attr> nsAttrs = new ArrayList<Attr>();
        do {
            if (!(node instanceof Element)) {
                continue;
            }
            NamedNodeMap attrs = ((Element)node).getAttributes();
            for (int i = 0; i < attrs.getLength(); i++) {
                Attr attr = (Attr)attrs.item(i);
                if (attr.getName().startsWith("xmlns")) {
                    nsAttrs.add(attr);
                }
            }
        } while ((node = node.getParentNode()) != null);
        return (Attr[])nsAttrs.toArray(new Attr[0]);
    }

    public static List<Node> toList(NodeList nodeList) {
        Assert.Arg.notNull(nodeList, "nodeList");
        List<Node> nodes = new ArrayList<Node>(nodeList.getLength());
        for (int i = 0; i < nodeList.getLength(); i++) {
            nodes.add(nodeList.item(i));
        }
        return nodes;
    }

    public static List<Element> getChildElements(Node node, String name) {
        Assert.Arg.notNull(node, "node");
        Assert.Arg.notNull(name, "name");

        List<Element> elements = new ArrayList<Element>();
        NodeList children = node.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE &&
                    child.getNodeName().equals(name)) {
                elements.add((Element)child);
            }
        }
        return elements;
    }

    public static Element getFirstChildElement(Node node, String name) {
        Assert.Arg.notNull(node, "node");
        Assert.Arg.notNull(name, "name");

        NodeList children = node.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE &&
                    child.getNodeName().equals(name)) {
                return (Element)child;
            }
        }
        return null;
    }

// Private methods

    private static DocumentBuilder createDocumentBuilder(boolean validating) {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setCoalescing(false);
        factory.setExpandEntityReferences(true);
        factory.setIgnoringComments(false);
        factory.setIgnoringElementContentWhitespace(false);
        factory.setNamespaceAware(true);
        factory.setValidating(validating);
        try {
            return factory.newDocumentBuilder();
        }
        catch (ParserConfigurationException e) {
            throw new UnhandledException(e);
        }
    }

    private static void serialize(Node node, StreamResult result, String encoding)
        throws TransformerException {
        Assert.Arg.notNull(node, "node");
        Assert.Arg.notNull(result, "result");

        Source source = new DOMSource(node);
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        if (encoding != null) {
            transformer.setOutputProperty(OutputKeys.ENCODING, encoding);
        }
        if (node instanceof Document) {
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
        }
        else {
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        }
        transformer.transform(source, result);
    }

    private static Node buildFragment(String fragment, Node context)
        throws SAXException, IOException {
        Attr[] nsAttrs = collectNamespaceDefinitions(context);
        StringBuffer startTag = new StringBuffer("<Fragment");
        for (int i = 0; i < nsAttrs.length; i++) {
            startTag.append(" " + nsAttrs[i]);
        }
        startTag.append(">");

        fragment = startTag.toString() + fragment + "</Fragment>";
        Node fragmentTree = buildDocument(fragment)
            .getDocumentElement().getFirstChild();
        return context.getOwnerDocument().importNode(fragmentTree, true);
    }

    /**
     * Returns the textual content directly held under this element as a string.
     * This includes all text within this single element,
     * including whitespace and CDATA sections if they exist.
     * The call does not recurse into child elements.
     * If no textual value exists for the element, a null value is returned.
     */
    public static String getText(Element element) {
        StringBuffer text = new StringBuffer();
        NodeList children = element.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child instanceof CharacterData && !(child instanceof Comment)) {
                text.append(((CharacterData)child).getData());
            }
        }
        if (text.length() > 0) {
            return text.toString();
        }
        else {
            return null;
        }
    }

    private DomUtils() {
    }
}
