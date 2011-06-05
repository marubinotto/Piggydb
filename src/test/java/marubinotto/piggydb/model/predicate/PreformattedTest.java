package marubinotto.piggydb.model.predicate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import marubinotto.piggydb.model.entity.RawFragment;
import marubinotto.piggydb.model.entity.RawTag;

import org.junit.Before;
import org.junit.Test;

public class PreformattedTest {
	
	private Preformatted object;
	private RawFragment fragment;
	
	@Before
	public void given() {
		this.object = new Preformatted();
		this.fragment = new RawFragment();
	}

	@Test
	public void trueIfWithPreTag() throws Exception {
		assertFalse(this.object.evaluate(this.fragment));
		
		this.fragment.getMutableClassification().addTag(new RawTag("#pre"));
		assertTrue(this.object.evaluate(this.fragment));
	}
	
	@Test
	public void isCodeIfWithCodeTag() throws Exception {
		assertFalse(this.object.isCode(this.fragment));
		
		this.fragment.getMutableClassification().addTag(new RawTag("#code"));
		assertTrue(this.object.isCode(this.fragment));
	}
	
	@Test
	public void languageNameShouldBeNullByDefault() throws Exception {
		assertNull(this.object.getLanguageName(this.fragment));
	}
	
	@Test
	public void getLanguageName() throws Exception {
		this.fragment.getMutableClassification().addTag(new RawTag("#lang-html"));
		assertEquals("html", this.object.getLanguageName(this.fragment));
	}
}
