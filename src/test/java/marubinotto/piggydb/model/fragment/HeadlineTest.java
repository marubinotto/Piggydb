package marubinotto.piggydb.model.fragment;

import static org.junit.Assert.*;
import marubinotto.piggydb.model.entity.RawFragment;

import org.junit.Test;

public class HeadlineTest {

	private RawFragment object = new RawFragment();

	@Test
	public void empty() throws Exception {
		assertNull(this.object.makeHeadline());
		assertFalse(this.object.hasMoreThanHeadline());
	}

	@Test
	public void title() throws Exception {
		this.object.setTitle("title");
		assertEquals("title", this.object.makeHeadline());
		assertFalse(this.object.hasMoreThanHeadline());
	}

	@Test
	public void fileName() throws Exception {
		this.object.setFileName("hoge.txt");
		assertEquals("hoge.txt", this.object.makeHeadline());
		assertTrue(this.object.hasMoreThanHeadline());
	}

	@Test
	public void contentOnly() throws Exception {
		this.object.setContent("This is a content. I'm hungry.");
		assertEquals("This is a content. ...", this.object.makeHeadline());
		assertTrue(this.object.hasMoreThanHeadline());
	}

	@Test
	public void titleOverFileName() throws Exception {
		this.object.setTitle("title");
		this.object.setFileName("hoge.txt");
		assertEquals("title", this.object.makeHeadline());
		assertTrue(this.object.hasMoreThanHeadline());
	}

	@Test
	public void titleOverContentHeadline() throws Exception {
		this.object.setTitle("title");
		this.object.setContent("This is a content. I'm hungry.");
		assertEquals("title", this.object.makeHeadline());
		assertTrue(this.object.hasMoreThanHeadline());
	}
}
