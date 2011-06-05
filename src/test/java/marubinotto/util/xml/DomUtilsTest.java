package marubinotto.util.xml;

import static org.custommonkey.xmlunit.XMLAssert.assertXMLEqual;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Test;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;
import org.xml.sax.SAXParseException;

/**
 * @see DomUtils
 */
public class DomUtilsTest {

// buildDocument(String)

	@Test
    public void shouldBuildDocumentFromString() throws Exception {
        String src = "<element>value</element>";
        Document document = DomUtils.buildDocument(src);
        assertXMLEqual(
            XMLUnit.buildControlDocument(src),
            document);
    }

	@Test(expected=SAXParseException.class)
    public void shouldThrowExceptionWhenBuildDocumentFromInvaidString() 
	throws Exception {
    	DomUtils.buildDocument("This is an invalid XML String");
    }

// buildDocument(InputStream)

	@Test
    public void shouldBuildDocumentFromInputStream() throws Exception {
        String src = "<element>value</element>";
        InputStream in = new ByteArrayInputStream(src.getBytes());
        Document document = DomUtils.buildDocument(in);
        assertXMLEqual(
            XMLUnit.buildControlDocument(src),
            document);
    }

// toString(Node)

	@Test
    public void shouldConvertDocumentToString() throws Exception {
        Document document = DomUtils.buildDocument("<element>value</element>");
        assertXMLEqual(
            "<element>value</element>",
            DomUtils.toString(document));
    }

	@Test
    public void shouldConvertElementToString() throws Exception {
        Document document = DomUtils.buildDocument("<element>value</element>");
        Element element = document.getDocumentElement();
        assertEquals("<element>value</element>", DomUtils.toString(element));
    }

	@Test
    public void shouldConvertTextToString() throws Exception {
        Document document = DomUtils.buildDocument("<element>value</element>");
        Node node = document.getDocumentElement().getFirstChild();
        assertEquals("value", DomUtils.toString(node));
    }

	@Test
    public void shouldConvertAttrToString() throws Exception {
        Document document = DomUtils.buildDocument("<element><sub value='hello'/></element>");
        Node node = document.getDocumentElement().getFirstChild().getAttributes().getNamedItem("value");
        assertEquals("hello", DomUtils.toString(node));
    }

// serialize(Node, java.io.OutputStream, String)

	@Test
    public void shouldSerializeDocumentToOutputStream() throws Exception {
        Document document = DomUtils.buildDocument("<element>value</element>");

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        DomUtils.serialize(document, out, null);
        assertXMLEqual(
            "<element>value</element>",
            new String(out.toByteArray()));
    }

// isRootElement

	@Test
    public void shouldDecideWhetherSpecifiedElementIsRoot() throws Exception {
        Document document = DomUtils.buildDocument("<root><sub/></root>");
        Element root = document.getDocumentElement();
        Element sub = (Element)root.getFirstChild();

        assertTrue(DomUtils.isRootElement(root));
        assertTrue(!DomUtils.isRootElement(sub));
    }

// getLang, setLang

	@Test
    public void shouldReturnLangOfElementWhichHasLangAttribute()
    throws Exception {
        Document document = DomUtils.buildDocument("<element xml:lang='ja'/>");
        assertEquals("ja", DomUtils.getLang(document.getDocumentElement()));
    }

	@Test
    public void shouldReturnLangOfTextNodeWhoseParentHasLangAttribute()
    throws Exception {
        Document document = DomUtils.buildDocument(
            "<element xml:lang='en'>This is a pen.</element>");
        Node node = document.getDocumentElement().getFirstChild();
        assertTrue(node instanceof Text);
        assertEquals("en", DomUtils.getLang(node));
    }

	@Test
    public void shouldReturnLangOfElementWhoseParentsHaveNoLangAttribute()
    throws Exception {
        Document document = DomUtils.buildDocument(
            "<root><sub/></root>");
        Node node = document.getDocumentElement().getFirstChild();
        assertNull(DomUtils.getLang(node));
    }

	@Test
    public void shouldReturnLangOfTextNodeWhoseParentsHaveNoLangAttribute()
    throws Exception {
        Document document = DomUtils.buildDocument(
        "<element>This is a pen.</element>");
        Node node = document.getDocumentElement().getFirstChild();
        assertTrue(node instanceof Text);
        assertNull(DomUtils.getLang(node));
    }

	@Test
    public void shouldSetLangToElement() throws Exception {
        Document document = DomUtils.buildDocument("<element/>");
        DomUtils.setLang(document.getDocumentElement(), Locale.JAPANESE);
        assertEquals("ja", DomUtils.getLang(document.getDocumentElement()));
    }

// selectByLang

	@Test
    public void shouldSelectNodeByLang() throws Exception {
        Document document = DomUtils.buildDocument(
            "<root>" +
            "<sub xml:lang='en'>English</sub>" +
            "<sub xml:lang='ja'>Japanese</sub>" +
            "</root>");

        List<Node> nodes = DomUtils.toList(document.getDocumentElement().getChildNodes());

        Node node = DomUtils.selectByLang(nodes, Locale.JAPANESE);
        assertEquals("Japanese", node.getFirstChild().getNodeValue());

        node = DomUtils.selectByLang(nodes, Locale.ENGLISH);
        assertEquals("English", node.getFirstChild().getNodeValue());

        node = DomUtils.selectByLang(nodes, Locale.CHINESE);
        assertNull(node);
    }

// addFragment

	@Test
    public void shouldAddXmlFragmentToNode() throws Exception {
		// Given
        Document document = DomUtils.buildDocument("<element/>");

        // When
        DomUtils.addFragment(
            "<message>hello.</message>",
            document.getDocumentElement());

        // Then
        assertXMLEqual(
            XMLUnit.buildControlDocument(
                "<element><message>hello.</message></element>"),
            document);
    }

	@Test
    public void shouldAddFragmentToNodeWithNamespace() throws Exception {
        Document document = DomUtils.buildDocument(
            "<element xmlns='urn:mpeg:mpeg7:schema:2001'/>");

        DomUtils.addFragment(
            "<message>hello.</message>",
            document.getDocumentElement());
        assertXMLEqual(
            XMLUnit.buildControlDocument(
                "<element xmlns='urn:mpeg:mpeg7:schema:2001'><message>hello.</message></element>"),
            document);

        assertEquals(
            "urn:mpeg:mpeg7:schema:2001",
            document.getDocumentElement().getNamespaceURI());
        assertEquals(
            "urn:mpeg:mpeg7:schema:2001",
            document.getDocumentElement().getFirstChild().getNamespaceURI());
    }

	@Test
    public void shouldAddFragmentToEmptyNodeAtFirst() throws Exception {
        Document document = DomUtils.buildDocument("<element/>");

        DomUtils.addFragmentFirst(
            "<message>hello.</message>",
            document.getDocumentElement());

        assertXMLEqual(
            XMLUnit.buildControlDocument(
                "<element><message>hello.</message></element>"),
            document);
    }

	@Test
    public void shouldAddFragmentToNodeAtFirst() throws Exception {
        Document document = DomUtils.buildDocument("<root><sub/></root>");

        DomUtils.addFragmentFirst(
            "<message>hello.</message>",
            document.getDocumentElement());

        assertXMLEqual(
            XMLUnit.buildControlDocument(
                "<root><message>hello.</message><sub/></root>"),
            document);
    }

// getValue, setValue

	@Test
    public void shouldReturnValueOfSpecifiedNode() throws Exception {
        Document document = DomUtils.buildDocument(
            "<root>" +
            "<sub1>value</sub1>" +
            "<sub2>This is a <![CDATA[pen]]>.</sub2>" +
            "<sub3 value='hello'/>" +
            "</root>");

        Element root = document.getDocumentElement();

        assertEquals("value", DomUtils.getValue(root.getFirstChild()));
        assertEquals("value", DomUtils.getValue(root.getFirstChild().getFirstChild()));
        assertEquals("This is a pen.", DomUtils.getValue(root.getChildNodes().item(1)));
        assertEquals("hello", DomUtils.getValue(
            root.getChildNodes().item(2).getAttributes().getNamedItem("value")));
    }

	@Test
    public void shouldSetValueToNodeAndAttribute() throws Exception {
		// Given
        Document document = DomUtils.buildDocument(
            "<root><sub1/><sub2 value=''/></root>");

        Element root = document.getDocumentElement();

        // When
        DomUtils.setValue(root.getFirstChild(), "value1");
        DomUtils.setValue(root.getLastChild().getAttributes().getNamedItem("value"), "value2");

        // Then
        assertXMLEqual(
            XMLUnit.buildControlDocument(
                "<root><sub1>value1</sub1><sub2 value='value2'/></root>"),
            document);
    }

// removeAllChildNodes

	@Test
    public void shouldRemoveAllChildNodesOfSpecifiedNode() throws Exception {
		// Given
        Document document = DomUtils.buildDocument(
            "<root>" +
            "<sub1>value</sub1>" +
            "<sub2>This is a <![CDATA[pen]]>.</sub2>" +
            "<sub3 value='hello'/>" +
            "</root>");

        Element root = document.getDocumentElement();

        // When
        Node sub2 = root.getChildNodes().item(1);
        DomUtils.removeAllChildNodes(sub2);
        
        // Then
        assertEquals(0, sub2.getChildNodes().getLength());

        // When
        DomUtils.removeAllChildNodes(root);
        
        // Then
        assertEquals(0, root.getChildNodes().getLength());
    }

// collectNamespaceDefinitions

	@Test
    public void shouldCollectNamespaceDefinitionsOfSpecifiedElement() throws Exception {
		// Given
        Document document = DomUtils.buildDocument(
            "<foo xmlns='bar' xmlns:xsi='hoge'/>");

        // When
        Map<String, String> attrs = attrsToMap(
            DomUtils.collectNamespaceDefinitions(document.getDocumentElement()));
        
        // Then
        assertEquals("bar", attrs.get("xmlns"));
        assertEquals("hoge", attrs.get("xmlns:xsi"));
    }

	@Test
    public void shouldReturnChildElementsOfSpecifiedNodeByName() throws Exception {
		// Given
        Document document = DomUtils.buildDocument(
            "<root><sub1><sub2/></sub1><sub2>hogehoge</sub2></root>");
        
        // When
        List<Element> elements = DomUtils.getChildElements(document.getDocumentElement(), "sub2");
        
        // Then
        assertEquals(1, elements.size());
        assertEquals("hogehoge", DomUtils.getText((Element)elements.get(0)));
    }

	@Test
    public void shouldReturnFirstChildElementOfSpecifiedNodeByName() throws Exception {
        Document document = DomUtils.buildDocument(
            "<root><sub1><sub2/></sub1><sub2>hogehoge</sub2></root>");
        Element element = DomUtils.getFirstChildElement(document.getDocumentElement(), "sub2");
        assertEquals("hogehoge", DomUtils.getText(element));
    }

// Private methods

    private static Map<String, String> attrsToMap(Attr[] attrs) {
        Map<String, String> attrMap = new HashMap<String, String>();
        for (int i = 0; i < attrs.length; i++) {
            attrMap.put(attrs[i].getName(), attrs[i].getValue());
        }
        return attrMap;
    }
}
