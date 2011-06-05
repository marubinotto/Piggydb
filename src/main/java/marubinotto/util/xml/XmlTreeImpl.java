package marubinotto.util.xml;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.WeakHashMap;

import marubinotto.util.Assert;

import org.apache.commons.lang.UnhandledException;
import org.jaxen.JaxenException;
import org.jaxen.SimpleNamespaceContext;
import org.jaxen.XPath;
import org.jaxen.dom.DOMXPath;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * @version $Id: XmlTreeImpl.java 1286 2008-03-20 15:39:37Z morita $
 */
public class XmlTreeImpl implements XmlTree {

    private static Map<String, XPath> xpathCache = new WeakHashMap<String, XPath>();

    private Node root;
    private SimpleNamespaceContext namespaceContext = new SimpleNamespaceContext();
    private Map<String, String> xpaths = new HashMap<String, String>();


    public XmlTreeImpl() {
    }

    public XmlTreeImpl(Node rootNode) throws Exception {
        setRoot(rootNode);
    }

// XmlSerializable

    public void readFrom(InputStream xmlInput) throws Exception {
        Assert.Arg.notNull(xmlInput, "xmlInput");
        setRoot(DomUtils.buildDocument(xmlInput));
    }

    public void readFrom(File xmlFile) throws Exception {
        Assert.Arg.notNull(xmlFile, "xmlFile");
        setRoot(DomUtils.buildDocument(xmlFile));
    }

    public void fromXml(String xml) throws Exception {
        Assert.Arg.notNull(xml, "xml");
        setRoot(DomUtils.buildDocument(xml));
    }

    public void fromDom(Node node) throws Exception {
        setRoot(node);
    }

    public void writeTo(OutputStream xmlOut) throws Exception {
        Assert.Arg.notNull(xmlOut, "xmlOut");
        DomUtils.serialize(getRoot(), xmlOut, null);
    }

    public void writeTo(OutputStream xmlOut, String encoding)
    throws Exception {
        Assert.Arg.notNull(xmlOut, "xmlOut");
        DomUtils.serialize(getRoot(), xmlOut, encoding);
    }

    public void writeTo(File file) throws Exception {
        Assert.Arg.notNull(file, "file");
        DomUtils.serialize(getRoot(), file, null);
    }

    public void writeTo(File file, String encoding) throws Exception {
        Assert.Arg.notNull(file, "file");
        DomUtils.serialize(getRoot(), file, encoding);
    }

    public String toXml() {
        return DomUtils.toString(getRoot());
    }

    public Node toDom() {
        return getRoot();
    }

// XmlTree

    public Node getRoot() {
        return root;
    }

    public void setRoot(Node rootNode) throws Exception {
        Assert.Arg.notNull(rootNode, "rootNode");
        root = rootNode;
        initialize();
    }

    public Document getOwnerDocument() {
        Node root = getRoot();
        if (root == null) return null;

        if (root instanceof Document) {
            return (Document)root;
        }
        else {
            return root.getOwnerDocument();
        }
    }

    public void setNamespacePrefix(String prefix, String namespaceUri) {
        Assert.Arg.notNull(prefix, "prefix");
        Assert.Arg.notNull(namespaceUri, "namespaceUri");
        namespaceContext.addNamespace(prefix, namespaceUri);
    }

    public Node selectSingleNode(String xpath) throws Exception {
        Assert.Arg.notNull(xpath, "xpath");
        return (Node)getXPath(xpath).selectSingleNode(getRoot());
    }

    public Node selectSingleNode(String xpath, Locale locale)
    throws Exception {
        Assert.Arg.notNull(xpath, "xpath");
        Assert.Arg.notNull(locale, "locale");
        return DomUtils.selectByLang(selectNodes(xpath), locale);
    }

    @SuppressWarnings("unchecked")
	public List<Node> selectNodes(String xpath) throws Exception {
        Assert.Arg.notNull(xpath, "xpath");
        return getXPath(xpath).selectNodes(getRoot());
    }

    public List<XmlTree> selectSubtrees(String xpath) throws Exception {
        Assert.Arg.notNull(xpath, "xpath");
        List<XmlTree> subtrees = new ArrayList<XmlTree>();
        for (Iterator<Node> i = selectNodes(xpath).iterator(); i.hasNext();) {
            subtrees.add(createSubtree(i.next()));
        }
        return subtrees;
    }

    public List<String> selectValues(String xpath) throws Exception {
        Assert.Arg.notNull(xpath, "xpath");
        List<String> values = new ArrayList<String>();
        for (Iterator<Node> i = selectNodes(xpath).iterator(); i.hasNext();) {
            values.add(DomUtils.getValue(i.next()));
        }
        return values;
    }

    public String stringValueOf(String xpath) throws Exception {
        Assert.Arg.notNull(xpath, "xpath");
        Node node = selectSingleNode(xpath);
        if (node == null) {
            return null;
        }
        return DomUtils.getValue(node);
    }

    public String stringValueOf(String xpath, Locale locale)
    throws Exception {
        Assert.Arg.notNull(xpath, "xpath");
        Assert.Arg.notNull(locale, "locale");
        Node node = selectSingleNode(xpath, locale);
        if (node == null) {
            return null;
        }
        return DomUtils.getValue(node);
    }

    public boolean booleanValueOf(String xpath) throws Exception {
        Assert.Arg.notNull(xpath, "xpath");
        return getXPath(xpath).booleanValueOf(getRoot());
    }

    public Number numberValueOf(String xpath) throws Exception {
        Assert.Arg.notNull(xpath, "xpath");
        return getXPath(xpath).numberValueOf(getRoot());
    }

// XPath mappings

    public void setXpaths(Map<String, String> xpaths) {
        Assert.Arg.notNull(xpaths, "xpaths");
        this.xpaths.putAll(xpaths);
    }

    public Map<String, String> getXpaths() {
        return this.xpaths;
    }

    public String getXpath(String name) {
        Assert.Arg.notNull(name, "name");
        String xpath = (String)getXpaths().get(name);
        if (xpath == null) {
            throw new IllegalArgumentException("Missing xpath mapping: " + name);
        }
        return xpath;
    }

    public String get(String name) {
        Assert.Arg.notNull(name, "name");
        String xpath = getXpath(name);
        try {
            return stringValueOf(xpath);
        }
        catch (Exception e) {
            throw new UnhandledException(e);
        }
    }

    public String get(String name, Locale locale) {
        Assert.Arg.notNull(name, "name");
        Assert.Arg.notNull(locale, "locale");
        try {
            return stringValueOf(getXpath(name), locale);
        }
        catch (Exception e) {
            throw new UnhandledException(e);
        }
    }

    public boolean set(String name, String value) {
        return set(name, null, value);
    }

    public boolean set(String name, Locale locale, String value) {
        Assert.Arg.notNull(name, "name");

        String xpath = getXpath(name);
        Node node = null;
        try {
            if (locale != null) {
                node = selectSingleNode(xpath, locale);
            }
            else {
                node = selectSingleNode(xpath);
            }
        }
        catch (Exception e) {
            throw new UnhandledException(e);
        }
        if (node == null) {
            return false;
        }
        DomUtils.setValue(node, value);
        return true;
    }

// Protected Methods

    protected void initialize() throws Exception {
        setNamespacePrefix("xml", DomUtils.XML_NAMESPACE);
        setNamespacePrefix("xsi", DomUtils.XML_SCHEMA_NAMESPACE);
    }

// Private methods

    private XPath getXPath(String xpath) throws JaxenException {
        XPath result = xpathCache.get(xpath);
        if (result == null) {
            result = new DOMXPath(xpath);
            result.setNamespaceContext(namespaceContext);
            xpathCache.put(xpath, result);
        }
        return result;
    }

    private XmlTreeImpl createSubtree(Node subtreeRoot) throws Exception {
        XmlTreeImpl tree = new XmlTreeImpl(subtreeRoot);
        tree.namespaceContext = namespaceContext;
        return tree;
    }
}
