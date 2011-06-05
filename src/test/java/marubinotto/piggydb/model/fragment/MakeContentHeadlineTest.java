package marubinotto.piggydb.model.fragment;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import marubinotto.piggydb.model.entity.RawFragment;

import org.apache.commons.lang.text.StrBuilder;
import org.junit.Test;

public class MakeContentHeadlineTest {

	private RawFragment object = new RawFragment();
	
	@Test
    public void contentIsNull() throws Exception {
		assertNull(this.object.makeContentHeadline());
	}
	
	@Test
    public void empty() throws Exception {
		this.object.setContent("");
		assertNull(this.object.makeContentHeadline());
	}
	
	@Test
    public void oneSentenceUnderLimit() throws Exception {
		this.object.setContent("This is a pen");
		assertEquals(
			"This is a pen", 
			this.object.makeContentHeadline());
	}
	
	@Test
    public void oneSentenceOverLimit() throws Exception {
		this.object.setContent(
			"Piggydb is a Web notebook application that provides you with" +
			" a platform to build your knowledge personally or collaboratively.");
		assertEquals(
			"Piggydb is a Web notebook application that provides you with" +
			" a platform to build your knowledge pers...", 
			this.object.makeContentHeadline());
	}
	
	@Test
    public void twoSentencesUnderLimit() throws Exception {
		this.object.setContent("This is a pen. I am Nancy.");
		assertEquals(
			"This is a pen. ...", 
			this.object.makeContentHeadline());
	}
	
	@Test
	public void multilines() throws Exception {
		StrBuilder content = new StrBuilder();
		content.appendln("Norwegian Wood");
		content.appendln("I once had a girl, or should I say, she once had me.");
		this.object.setContent(content.toString());
		
		assertEquals(
			"Norwegian Wood ...", 
			this.object.makeContentHeadline());
	}
}
