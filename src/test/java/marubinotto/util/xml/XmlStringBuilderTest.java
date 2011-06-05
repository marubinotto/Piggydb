package marubinotto.util.xml;

import org.custommonkey.xmlunit.XMLAssert;
import org.junit.Before;
import org.junit.Test;

public class XmlStringBuilderTest {

	private XmlStringBuilder object;
	
	@Before
	public void given() {
		this.object = new XmlStringBuilder();
	}
	
	@Test
	public void element() throws Exception {
		String result = this.object.element("hoge").toString();
		
		XMLAssert.assertXMLEqual("<hoge/>", result);
	}
	
	@Test
	public void elementWithOneAttribute() throws Exception {
		String result = this.object.element("hoge")
			.attribute("foo", "bar")
			.toString();
		
		XMLAssert.assertXMLEqual("<hoge foo=\"bar\"/>", result);
	}
	
	@Test
	public void elementWithTwoAttributes() throws Exception {
		String result = this.object.element("akane")
			.attribute("gender", "female")
			.attribute("feature", "pig")
			.toString();
		
		XMLAssert.assertXMLEqual("<akane gender=\"female\" feature=\"pig\"/>", result);
	}
	
	@Test
	public void elementWithOneChildElement() throws Exception {
		String result = this.object.element("foo")
			.element("bar")
			.toString();
		
		XMLAssert.assertXMLEqual("<foo><bar/></foo>", result);
	}
	
	@Test
	public void elementWithTwoNested() throws Exception {
		String result = this.object.element("a")
			.element("b")
			.element("c")
			.toString();
		
		XMLAssert.assertXMLEqual("<a><b><c/></b></a>", result);
	}
	
	@Test
	public void elementWithTwoChildElements() throws Exception {
		String result = this.object.element("morita")
			.element("akane").end()
			.element("daisuke")
			.toString();
		
		XMLAssert.assertXMLEqual("<morita><akane/><daisuke/></morita>", result);
	}
	
	@Test
	public void elementWithText() throws Exception {
		String result = this.object.element("foo")
			.text("bar")
			.toString();
		
		XMLAssert.assertXMLEqual("<foo>bar</foo>", result);
	}
	
	@Test
	public void nextedSameTags() throws Exception {
		String result = this.object.element("a").element("a").toString();
		
		XMLAssert.assertXMLEqual("<a><a/></a>", result);
	}
}
