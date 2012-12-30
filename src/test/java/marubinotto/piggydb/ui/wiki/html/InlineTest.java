package marubinotto.piggydb.ui.wiki.html;

import static marubinotto.piggydb.fixture.EntityFixtures.newFragmentWithTitle;
import static org.junit.Assert.assertEquals;
import marubinotto.piggydb.model.entity.RawFragment;

import org.junit.Before;
import org.junit.Test;

public class InlineTest extends HtmlBuilderTestBase {

	private RawFragment fragment;
	
	@Before
	public void given() throws Exception {
		super.given();
		
		this.fragment = newFragmentWithTitle("title");
		this.fragment.setId(999L);
	}

	@Test
	public void httpUrl() throws Exception {
		String url = "http://marubinotto.net";
		String result = this.object.processStandardUrl(this.context, url, false);
		assertEquals(
			String.format("<a class=\"url-link\" href=\"%s\">%s</a>", url, url),
			result);
	}
	
	@Test
	public void httpImageUrl() throws Exception {
		String url = "http://marubinotto.net/image.png";
		String result = this.object.processStandardUrl(this.context, url, false);

		assertEquals(
			String.format(
				"<a class=\"img-link\" href=\"%s\"><img src=\"%s\" alt=\"%s\"/></a>", 
				url, url, url),
			result);
	}
	
	@Test
	public void makeLinkToFragment() throws Exception {
		String result = this.object.makeLinkToFragment(this.context, 123L, "fragment:123");
		assertEquals("<a href=\"/fragment.htm?id=123\">fragment:123</a>", result);
	}
	
	@Test
	public void makeLinkToFragmentWithDetail() throws Exception {
		String result = this.object.makeLinkToFragmentWithDetail(this.context, this.fragment);
		assertEquals("<a href=\"/fragment.htm?id=999\">#999</a> title", result);
	}
	
	@Test
	public void processLabeledLink() throws Exception {
		String result = this.object.processLabeledLink(context, "label", "http://www.google.com/");
		assertEquals("<a href=\"http://www.google.com/\">label</a>", result);
	}
	
	@Test
	public void processHttpImageLabeledLink() throws Exception {
		String result = this.object.processLabeledLink(
			context, "http://marubinotto.net/image.png", "http://marubinotto.net/");
		assertEquals(
			"<a href=\"http://marubinotto.net/\">" +
			"<img src=\"http://marubinotto.net/image.png\" alt=\"\"/></a>", 
			result);
	}
}
