package marubinotto.piggydb.ui.wiki;

import static org.junit.Assert.assertEquals;
import marubinotto.piggydb.impl.InMemoryDatabase;
import marubinotto.piggydb.model.FragmentRepository;
import marubinotto.piggydb.model.Tag;
import marubinotto.piggydb.model.User;
import marubinotto.piggydb.ui.page.util.WebResources;
import marubinotto.piggydb.ui.wiki.DefaultWikiParser;
import marubinotto.piggydb.ui.wiki.HtmlBlock;
import marubinotto.piggydb.ui.wiki.HtmlBuilder;

import org.apache.commons.lang.text.StrBuilder;
import org.junit.Before;
import org.junit.Test;

/**
 * WikiParser is responsible for defining syntax rules and
 * parsing a wiki text with the rules.
 */
public class DefaultWikiParserTest {

	private DefaultWikiParser object = new DefaultWikiParser();
	
	protected User user = new User();
	private WebResources webResources = new WebResources("", "test");
	private FragmentRepository fragmentRepository = 
		new InMemoryDatabase().getFragmentRepository();
	private HtmlBuilder documentBuilder = new HtmlBuilder();
	
	@Before
	public void given() throws Exception {
		this.object.setDocumentBuilder(this.documentBuilder);
		this.object.setFragmentRepository(this.fragmentRepository);
		this.object.setTagRepository(this.fragmentRepository.getTagRepository());
		
		Tag newTag = this.fragmentRepository.getTagRepository().
			newInstance("java", new User("daisuke"));
		this.fragmentRepository.getTagRepository().register(newTag);
	}
	
	@Test
	public void onePlainLine() throws Exception {
		String result = this.object.parse("Hello world!", null, this.user, this.webResources);
		
		StrBuilder expect = new StrBuilder();
		expect.appendln(HtmlBlock.Paragraph.OPEN);
		expect.appendln("Hello world!");
		expect.appendln(HtmlBlock.Paragraph.CLOSE);
		assertEquals(expect.toString(), result);
	}
	
	@Test
	public void bold() throws Exception {
		String result = this.object.parse("'''bold'''", null, this.user, this.webResources);
		
		StrBuilder expect = new StrBuilder();
		expect.appendln(HtmlBlock.Paragraph.OPEN);
		expect.appendln("<b>bold</b>");
		expect.appendln(HtmlBlock.Paragraph.CLOSE);
		assertEquals(expect.toString(), result);
	}
	
	@Test
	public void italic() throws Exception {
		String result = this.object.parse("''italic''", null, this.user, this.webResources);
		
		StrBuilder expect = new StrBuilder();
		expect.appendln(HtmlBlock.Paragraph.OPEN);
		expect.appendln("<i>italic</i>");
		expect.appendln(HtmlBlock.Paragraph.CLOSE);
		assertEquals(expect.toString(), result);
	}
	
	@Test
	public void url() throws Exception {
		String result = this.object.parse(
			"http://marubinotto.net  http://marubinotto.net/image.png", 
			null, this.user, this.webResources);
		
		StrBuilder expect = new StrBuilder();
		expect.appendln(HtmlBlock.Paragraph.OPEN);
		expect.appendln(
			"<a href=\"http://marubinotto.net\">http://marubinotto.net</a>" +
			"  <a class=\"img-link\" href=\"http://marubinotto.net/image.png\">" +
			"<img src=\"http://marubinotto.net/image.png\" alt=\"http://marubinotto.net/image.png\"/></a>");
		expect.appendln(HtmlBlock.Paragraph.CLOSE);
		assertEquals(expect.toString(), result);
	}
	
	@Test
	public void labeledUrl() throws Exception {
		String result = this.object.parse("[http://marubinotto.net Marubinotto]", 
			null, this.user, this.webResources);
		
		StrBuilder expect = new StrBuilder();
		expect.appendln(HtmlBlock.Paragraph.OPEN);
		expect.appendln("<a href=\"http://marubinotto.net\">Marubinotto</a>");
		expect.appendln(HtmlBlock.Paragraph.CLOSE);
		assertEquals(expect.toString(), result);
	}
	
	@Test
	public void labeledFragmentUrl() throws Exception {
		String result = this.object.parse("[fragment:1 hogehoge]", 
			null, this.user, this.webResources);
		
		StrBuilder expect = new StrBuilder();
		expect.appendln(HtmlBlock.Paragraph.OPEN);
		expect.appendln("<a href=\"/fragment.htm?id=1\">hogehoge</a>");
		expect.appendln(HtmlBlock.Paragraph.CLOSE);
		assertEquals(expect.toString(), result);
	}
	
	@Test
	public void fragmentRef() throws Exception {
		String result = this.object.parse("#123", null, this.user, this.webResources);
		
		StrBuilder expect = new StrBuilder();
		expect.appendln(HtmlBlock.Paragraph.OPEN);
		expect.appendln("<a href=\"/fragment.htm?id=123\">#123</a>");
		expect.appendln(HtmlBlock.Paragraph.CLOSE);
		assertEquals(expect.toString(), result);
	}
	
	@Test
	public void tagName() throws Exception {
		String result = this.object.parse("piggydb is powered by java.", null, this.user, this.webResources);
		
		StrBuilder expect = new StrBuilder();
		expect.appendln(HtmlBlock.Paragraph.OPEN);
		expect.appendln("piggydb is powered by <a class=\"tag\" href=\"/tag.htm?name=java\">java</a>.");
		expect.appendln(HtmlBlock.Paragraph.CLOSE);
		assertEquals(expect.toString(), result);
	}
}
