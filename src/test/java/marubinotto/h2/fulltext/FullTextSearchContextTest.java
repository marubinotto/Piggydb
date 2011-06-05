package marubinotto.h2.fulltext;

import static marubinotto.util.CollectionUtils.set;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

public class FullTextSearchContextTest extends TestWithDataSource {
	
	private FullTextSearchContext object;
	
	@Before
	public void given() throws Exception {
		super.given();
		
		this.object = FullTextSearchContext.getContext(getConnection());
	}
	
	@Test
    public void getContext() throws Exception {
		FullTextSearchContext context = FullTextSearchContext.getContext(getConnection());
		assertSame(this.object, context);
	}
	
	@Test
    public void splitIntoWords() throws Exception {
		Set<String> words = new HashSet<String>();
		this.object.splitIntoWords("Piggydb is a Web notebook application", words);
		assertEquals(set("PIGGYDB", "WEB", "NOTEBOOK", "APPLICATION"), words);
	}
	
	@Test
    public void splitEmptyIntoWords() throws Exception {
		Set<String> words = new HashSet<String>();
		this.object.splitIntoWords("", words);
		assertTrue(words.isEmpty());
	}
}
