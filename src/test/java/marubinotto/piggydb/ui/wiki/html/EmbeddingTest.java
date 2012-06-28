package marubinotto.piggydb.ui.wiki.html;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import marubinotto.piggydb.fixture.mock.FileItemMock;
import marubinotto.piggydb.model.entity.RawFragment;
import marubinotto.piggydb.model.entity.RawTag;
import marubinotto.piggydb.model.predicate.Preformatted;
import marubinotto.util.time.DateTime;

import org.junit.Before;
import org.junit.Test;

public class EmbeddingTest extends HtmlBuilderTestBase {

	private RawFragment fragment = new RawFragment();
	
	@Before
	public void given() throws Exception {
		super.given();
		
		this.fragment.setId(123L);
		this.fragment.setTitle("title");
		this.fragment.setContent("content");
	}
	
	/**
	 * Embedding a text fragment will be done by a chain 
	 * [builder -> parser -> builder -> parser ...].
	 * 
	 * When the builder delegates parsing a nested fragment to the parser,
	 * the builder should stack the fragment ID, which will be used
	 * to detect an embedding loop.
	 */
	@Test
	public void embed() throws Exception {
		// When
		String result = this.object.makeEmbeddedFragment(this.context, this.fragment);
		
		// Then
		assertEquals("This was printed at WikiParserMock.doParse", result);
		assertEquals(this.fragment.getContent(), this.wikiParserMock.arg_wikiText);
		
		assertTrue(this.context.getFragmentStack().isEmpty());
		assertEquals("[" + this.fragment.getId() + "]", this.wikiParserMock.arg_fragmentStack);
	}
	
	@Test
	public void sameFragmentIdHasBeenStacked() throws Exception {
		// Given
		this.context.getFragmentStack().push(this.fragment.getId());
		
		// When
		String result = this.object.makeEmbeddedFragment(this.context, this.fragment);
		
		// Then
		assertNull(result);
		assertEquals(
			"[" + this.fragment.getId() + "]", 
			this.context.getFragmentStack().toString());
	}
	
	@Test
	public void embedFileFragment() throws Exception {
		// Given
		this.fragment.setFileInput(
			new FileItemMock("file", "/path/to/file.txt", "hello".getBytes()));
		
		// When
		String result = this.object.makeEmbeddedFragment(this.context, this.fragment);
		
		// Then
		assertEquals(
			"<img src=\"/images/file-icons/default.png\"" +
				" border=\"0\" style=\"vertical-align: middle;\" alt=\"\"/>" +
				" <span><a class=\"file-name\" href=\"/command/get-file.htm?id=123\">file.txt</a>" +
				" <span class=\"file-size\">(0.00 KByte)</span></span>", 
			result);
	}
	
	@Test
	public void embedImageFragment() throws Exception {
		// Given
		this.fragment.setFileInput(
			new FileItemMock("file", "/path/to/file.png", "hello".getBytes()));
		this.fragment.setUpdateDatetime(new DateTime(1));
		
		// When
		String result = this.object.makeEmbeddedFragment(this.context, this.fragment);
		
		// Then
		assertEquals(
			"<a class=\"img-link\" href=\"/command/get-file.htm?id=123&t=1\">" +
				"<img class=\"fragment-img\" src=\"/command/get-file.htm?id=123&t=1\"" +
				" border=\"0\" alt=\"\"/></a>", 
			result);
	}
	
	@Test
	public void embedPreformattedFragment() throws Exception {
		// Given
		this.fragment.getMutableClassification().addTag(
			new RawTag(Preformatted.TAG_NAME));
		
		// When
		String result = this.object.makeEmbeddedFragment(this.context, this.fragment);
		
		// Then
		assertEquals(
			"<pre class=\"pre-fragment\">" +
				"WikiParserMock.doParsePreformattedText: content</pre>", 
			result);
	}
}
