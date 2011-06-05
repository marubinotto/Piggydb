package marubinotto.piggydb.model.fragment;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import marubinotto.piggydb.model.entity.RawFragment;

import org.junit.Test;

public class MakeHeadlineTest {

	private RawFragment object = new RawFragment();
	
	@Test
    public void defaultState() throws Exception {
		assertNull(this.object.makeHeadline());
	}
	
	@Test
    public void title() throws Exception {
		this.object.setTitle("title");
		assertEquals("title", this.object.makeHeadline());
	}
	
	@Test
    public void fileName() throws Exception {
		this.object.setFileName("hoge.txt");
		assertEquals("hoge.txt", this.object.makeHeadline());
	}
	
	@Test
    public void contentHeadline() throws Exception {
		this.object.setContent("This is a content. I'm hungry.");
		assertEquals("This is a content. ...", this.object.makeHeadline());
	}
	
	@Test
    public void titleOverFileName() throws Exception {
		this.object.setTitle("title");
		this.object.setFileName("hoge.txt");
		assertEquals("title", this.object.makeHeadline());
	}
	
	@Test
    public void titleOverContentHeadline() throws Exception {
		this.object.setTitle("title");
		this.object.setContent("This is a content. I'm hungry.");
		assertEquals("title", this.object.makeHeadline());
	}
}
