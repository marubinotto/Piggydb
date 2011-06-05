package marubinotto.util.xml;

import static org.custommonkey.xmlunit.XMLAssert.assertXMLEqual;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Test;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

/**
 * marubinotto.util.xml.XmlTreeTestBase
 */
public abstract class XmlTreeTestBase {

    protected abstract XmlTree createXmlTree() throws Exception;

    public static Document createDocument(String namespaceURI, String qualifiedName)
    throws ParserConfigurationException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        DOMImplementation domImpl = builder.getDOMImplementation();
        return domImpl.createDocument(namespaceURI, qualifiedName, null);
    }

// Input

    @Test
    public void shouldSetRootViaSetter() throws Exception {
        Document document = createDocument(null, "root");

        XmlTree tree = createXmlTree();
        tree.setRoot(document);
        assertSame(document, tree.getRoot());
    }

    @Test
    public void shouldReturnOwnerDocumentIfRootIsADocument() throws Exception {
        Document document = createDocument(null, "root");

        XmlTree tree = createXmlTree();
        tree.setRoot(document);
        assertSame(document, tree.getOwnerDocument());
    }

    @Test
    public void shouldReturnOwnerDocumentIfRootIsAnElement() throws Exception {
        Document document = DomUtils.buildDocument("<element>value</element>");

        XmlTree tree = createXmlTree();
        tree.setRoot(document.getDocumentElement());
        assertSame(document, tree.getOwnerDocument());
    }

    @Test
    public void shouldReadXmlFromInputStream() throws Exception {
        InputStream in = new ByteArrayInputStream("<element>value</element>".getBytes());

        XmlTree tree = createXmlTree();
        tree.readFrom(in);

        assertTrue(tree.getRoot() instanceof Document);
        assertXMLEqual(
            XMLUnit.buildControlDocument("<element>value</element>"),
            (Document)tree.getRoot());
    }

    @Test
    public void shouldBuildTreeFromXmlString() throws Exception {
        XmlTree tree = createXmlTree();
        tree.fromXml("<element>value</element>");

        assertTrue(tree.getRoot() instanceof Document);
        assertXMLEqual(
            XMLUnit.buildControlDocument("<element>value</element>"),
            (Document)tree.getRoot());
    }

    @Test
    public void shouldBuildTreeFromDomDocument() throws Exception {
        Document document = createDocument(null, "root");

        XmlTree tree = createXmlTree();
        tree.fromDom(document);
        assertSame(document, tree.getRoot());
    }

    @Test
    public void shouldBuildTreeFromDomElement() throws Exception {
        Document document = createDocument(null, "root");
        Element element = document.createElement("sub");

        XmlTree tree = createXmlTree();
        tree.fromDom(element);
        assertSame(element, tree.getRoot());
    }

// Output

    @Test
    public void shouldWriteTreeToOutputStream() throws Exception {
        XmlTree tree = createXmlTree();
        tree.fromXml("<element>hello</element>");

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        tree.writeTo(out);
        assertXMLEqual(
            "<element>hello</element>",
            new String(out.toByteArray(), "UTF-8"));
    }

    @Test
    public void shouldReturnTreeAsXmlString() throws Exception {
        XmlTree tree = createXmlTree();
        tree.fromXml("<element>value</element>");
        assertXMLEqual(
            "<element>value</element>",
            tree.toXml());
    }

    @Test
    public void shouldReturnTreeAsDom() throws Exception {
        Document document = createDocument(null, "root");
        XmlTree tree = createXmlTree();
        tree.setRoot(document);
        assertSame(document, tree.toDom());
    }

// XPath

    @Test
    public void shouldSelectSingleNodeByXPath() throws Exception {
        XmlTree tree = createXmlTree();
        tree.fromXml("<element>value</element>");

        Node node = tree.selectSingleNode("/element/text()");
        assertTrue(node instanceof Text);
        assertEquals("value", node.getNodeValue());
    }

    @Test
    public void shouldSelectTheFirstNodeFromSelectableNodesByXPath() 
    throws Exception {
        XmlTree tree = createXmlTree();
        tree.fromXml(
            "<root>"
            + "<sub>foo</sub>"
            + "<sub>bar</sub>"
            + "<sub>hoge</sub>"
            + "</root>");

        Node node = tree.selectSingleNode("/root/sub/text()");
        assertTrue(node instanceof Text);
        assertEquals("foo", node.getNodeValue());
    }

    @Test
    public void shouldSelectSingleNodeWithLocaleByXPath() throws Exception {
        XmlTree tree = createXmlTree();
        tree.fromXml(
            "<root>"
            + "<sub xml:lang='en'>English</sub>"
            + "<sub xml:lang='ja'>Japanese</sub>"
            + "</root>");

        Node node = tree.selectSingleNode("/root/sub/text()", Locale.ENGLISH);
        assertEquals("English", node.getNodeValue());

        node = tree.selectSingleNode("/root/sub/text()", Locale.JAPANESE);
        assertEquals("Japanese", node.getNodeValue());
    }

    @Test
    public void shouldSelectTheFirstNodeWithLocaleFromSelectableNodesByXPath()
    throws Exception {
        XmlTree tree = createXmlTree();
        tree.fromXml(
            "<root>"
            + "<sub xml:lang='en'>English</sub>"
            + "<sub xml:lang='ja'>Japanese1</sub>"
            + "<sub xml:lang='ja'>Japanese2</sub>"
            + "<sub xml:lang='ja'>Japanese3</sub>"
            + "</root>");

        Node node = tree.selectSingleNode("/root/sub/text()", Locale.JAPANESE);
        assertEquals("Japanese1", node.getNodeValue());
    }

    @Test
    public void shouldSelectNodesByXPath() throws Exception {
        XmlTree tree = createXmlTree();
        tree.fromXml(
            "<messages>"
            + "<message>Hello</message>"
            + "<message>World</message>"
            + "</messages>");

        List<Node> nodes = tree.selectNodes("/messages/message/text()");
        assertEquals(2, nodes.size());
        assertEquals("Hello", ((Text)nodes.get(0)).getNodeValue());
        assertEquals("World", ((Text)nodes.get(1)).getNodeValue());
    }

    @Test
    public void shouldSelectSubtreesByXPath() throws Exception {
        XmlTree tree = createXmlTree();
        tree.fromXml(
            "<messages>"
            + "<message>Hello</message>"
            + "<message>World</message>"
            + "</messages>");

        List<XmlTree> subtrees = tree.selectSubtrees("/messages/message");
        assertEquals(2, subtrees.size());
        assertXMLEqual(
            "<message>Hello</message>",
            ((XmlTree)subtrees.get(0)).toXml());
        assertXMLEqual(
            "<message>World</message>",
            ((XmlTree)subtrees.get(1)).toXml());
    }

    @Test
    public void shouldSelectValuesByXPath() throws Exception {
        XmlTree tree = createXmlTree();
        tree.fromXml(
            "<root>"
            + "<sub xml:lang='en'>English</sub>"
            + "<sub xml:lang='ja'>Japanese</sub>"
            + "<sub>Hogehoge</sub>"
            + "<sub/>"
            + "</root>");

        List<String> values = tree.selectValues("/root/sub");
        assertEquals(4, values.size());
        assertEquals("English", values.get(0));
        assertEquals("Japanese", values.get(1));
        assertEquals("Hogehoge", values.get(2));
        assertNull(values.get(3));
    }

    @Test
    public void shouldReturnStringValueOfNodeByXPath() throws Exception {
        XmlTree tree = createXmlTree();
        tree.fromXml("<element attribute='foo'>bar</element>");

        assertEquals("bar", tree.stringValueOf("/element"));
        assertEquals("bar", tree.stringValueOf("/element/text()"));
        assertEquals("foo", tree.stringValueOf("/element/@attribute"));
    }

    @Test
    public void shouldReturnStringValueOfNodeWithLocaleByXPath() 
    throws Exception {
        XmlTree tree = createXmlTree();
        tree.fromXml(
            "<root>"
            + "<sub xml:lang='en'>English</sub>"
            + "<sub xml:lang='ja'>Japanese</sub>"
            + "</root>");

        assertEquals(
            "English",
            tree.stringValueOf("/root/sub/text()", Locale.ENGLISH));
        assertEquals(
            "Japanese",
            tree.stringValueOf("/root/sub/text()", Locale.JAPANESE));

        assertNull(tree.stringValueOf("/foo/bar", Locale.JAPANESE));
    }

    @Test
    public void shouldReturnBooleanValueOfNodeByXPath() throws Exception {
        XmlTree tree = createXmlTree();
        tree.fromXml(
            "<root>"
            + "<sub>hoge</sub>"
            + "<sub/>"
            + "</root>");

        assertTrue(tree.booleanValueOf("/root/sub[1]/text()"));
        assertTrue(!tree.booleanValueOf("/root/sub[2]/text()"));
        assertTrue(tree.booleanValueOf("/root"));
        assertTrue(!tree.booleanValueOf("/notexisting"));
    }

    @Test
    public void shouldReturnNumberValueOfNodeByXPath() throws Exception {
        XmlTree tree = createXmlTree();
        tree.fromXml(
            "<root>"
            + "<sub>4649</sub>"
            + "<sub>0</sub>"
            + "<sub>-123</sub>"
            + "<sub>3.14</sub>"
            + "<sub>hoge</sub>"
            + "<sub/>"
            + "</root>");

        assertEquals("NaN", tree.numberValueOf("/root/sub[1]/text()").toString());
        assertEquals(0, tree.numberValueOf("/root/sub[1]/text()").intValue());

        assertEquals(4649, tree.numberValueOf("/root/sub[1]").intValue());
        assertEquals(0, tree.numberValueOf("/root/sub[2]").intValue());
        assertEquals(-123, tree.numberValueOf("/root/sub[3]").intValue());
        assertEquals(3.14f, tree.numberValueOf("/root/sub[4]").floatValue(), 0.01f);

        assertEquals("NaN", tree.numberValueOf("/root/sub[5]").toString());
        assertEquals(0, tree.numberValueOf("/root/sub[5]").intValue());

        assertEquals("NaN", tree.numberValueOf("/root/sub[6]").toString());
        assertEquals(0, tree.numberValueOf("/root/sub[6]").intValue());
    }

    @Test
    public void shouldReturnStringValueOfNullByXPath() throws Exception {
        XmlTree tree = createXmlTree();
        tree.fromXml(
            "<root>"
            + "<sub/>"
            + "<sub>hello</sub>"
            + "</root>");

        assertNull(tree.stringValueOf("/root/sub[1]"));
        assertNull(tree.stringValueOf("/root/sub[1]/text()"));
        assertNotNull(tree.stringValueOf("/root/sub[2]"));
    }

// XPath mappings

    @Test
    public void shouldSupportXpathMappings() throws Exception {
        XmlTree tree = createXmlTree();
        tree.fromXml("<message>Hello world!</message>");

        Map<String, String> xpaths = new HashMap<String, String>();
        xpaths.put("message", "/message");
        tree.setXpaths(xpaths);

        assertEquals("/message", tree.getXpath("message"));
        assertEquals("/message", tree.getXpaths().get("message"));

        assertEquals("Hello world!", tree.get("message"));

        assertTrue(tree.set("message", "Updated!"));
        assertEquals("Updated!", tree.get("message"));
        assertXMLEqual(
            "<message>Updated!</message>",
            tree.toXml());
    }

    @Test
    public void shouldSupportXpathMappingsWithLocale() throws Exception {
        XmlTree tree = createXmlTree();
        tree.fromXml(
            "<root>"
            + "<message xml:lang='en'>English</message>"
            + "<message xml:lang='ja'>Japanese</message>"
            + "</root>");

        Map<String, String> xpaths = new HashMap<String, String>();
        xpaths.put("message", "/root/message");
        tree.setXpaths(xpaths);

        assertEquals("English", tree.get("message"));
        assertEquals("English", tree.get("message", Locale.ENGLISH));
        assertEquals("Japanese", tree.get("message", Locale.JAPANESE));
        assertNull(tree.get("message", Locale.FRENCH));

        assertTrue(tree.set("message", Locale.ENGLISH, "Updated-English"));
        assertEquals("Updated-English", tree.get("message", Locale.ENGLISH));
        assertEquals("Japanese", tree.get("message", Locale.JAPANESE));

        assertTrue(tree.set("message", Locale.JAPANESE, "Updated-Japanese"));
        assertEquals("Updated-English", tree.get("message", Locale.ENGLISH));
        assertEquals("Updated-Japanese", tree.get("message", Locale.JAPANESE));

        assertTrue(!tree.set("message", Locale.ITALY, "Updated-Italy"));
    }

    @Test(expected=IllegalArgumentException.class)
    public void shouldThrowExceptionWhenSpecifyInvalidMappingName() 
    throws Exception {
        XmlTree tree = createXmlTree();
        tree.fromXml("<message>Hello world!</message>");
        tree.get("foo");
    }
}
