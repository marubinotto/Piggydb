package marubinotto.piggydb.ui.wiki.html;

import static org.junit.Assert.assertEquals;


import marubinotto.piggydb.ui.wiki.HtmlBlock;

import org.apache.commons.lang.text.StrBuilder;
import org.junit.Test;

public class BlockTest extends HtmlBuilderTestBase {

	@Test
	public void sectionLevel1() throws Exception {
		this.object.startSection(this.context, 1, "Section Title");
		this.object.finish(this.context);
		
		assertEquals("<h2>Section Title</h2>", this.output.toString().trim());
	}
	
	@Test
	public void oneParagraph() throws Exception {
		this.object.appendToParagraph(this.context, "hoge");
		this.object.appendToParagraph(this.context, "huga");
		this.object.finish(this.context);
		
		String result = this.output.toString();
		
		StrBuilder expect = new StrBuilder();
		expect.appendln(HtmlBlock.Paragraph.OPEN);
		expect.appendln("hoge");
		expect.appendln("huga");
		expect.appendln(HtmlBlock.Paragraph.CLOSE);
		assertEquals(expect.toString(), result);
	}
	
	@Test
	public void twoParagraphs() throws Exception {
		this.object.appendToParagraph(this.context, "hoge");
		this.object.breakBlocks(this.context);
		this.object.appendToParagraph(this.context, "huga");
		this.object.finish(this.context);
		
		String result = this.output.toString();
		
		StrBuilder expect = new StrBuilder();
		expect.appendln(HtmlBlock.Paragraph.OPEN);
		expect.appendln("hoge");
		expect.appendln(HtmlBlock.Paragraph.CLOSE);
		expect.appendln(HtmlBlock.Paragraph.OPEN);
		expect.appendln("huga");
		expect.appendln(HtmlBlock.Paragraph.CLOSE);
		assertEquals(expect.toString(), result);
	}
	
	@Test
	public void unorderedList() throws Exception {
		this.object.addUnorderedListElement(this.context, 1, "Akane");
		this.object.addUnorderedListElement(this.context, 1, "Daisuke");
		this.object.finish(this.context);
		
		String result = this.output.toString();
		
		StrBuilder expect = new StrBuilder();
		expect.appendln("<ul class=\"level1\"><li>Akane</li>");
		expect.appendln("<li>Daisuke</li></ul>");
		assertEquals(expect.toString(), result);
	}

	@Test
	public void unorderedListNested() throws Exception {
		this.object.addUnorderedListElement(this.context, 1, "Akane");
		this.object.addUnorderedListElement(this.context, 2, "Bintai");
		this.object.addUnorderedListElement(this.context, 1, "Daisuke");
		this.object.finish(this.context);
		
		String result = this.output.toString();
		
		StrBuilder expect = new StrBuilder();
		expect.appendln("<ul class=\"level1\"><li>Akane<ul class=\"level2\"><li>Bintai</li></ul>");
		expect.appendln("</li>");
		expect.appendln("<li>Daisuke</li></ul>");
		assertEquals(expect.toString(), result);
	}
}
