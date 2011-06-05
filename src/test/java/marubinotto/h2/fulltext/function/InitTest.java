package marubinotto.h2.fulltext.function;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.sql.Connection;

import marubinotto.h2.fulltext.FullTextSearch;
import marubinotto.h2.fulltext.FullTextSearchContext;
import marubinotto.h2.fulltext.TestWithDataSource;

import org.junit.Before;
import org.junit.Test;

public class InitTest extends TestWithDataSource {
	
	private FullTextSearchContext context;

	@Before
	public void given() throws Exception {
		super.given();
		
		Connection connection = getConnection();
		FullTextSearch.init(connection);
		this.context = FullTextSearchContext.getContext(connection);
	}
	
	@Test
    public void init() throws Exception {
		assertTrue(this.context.getWordList().isEmpty());
	}
	
	@Test
    public void wordList() throws Exception {
		this.jdbcTemplate.update("insert into FT.WORDS(NAME) VALUES(?)", new Object[]{"hogehoge"});
		FullTextSearch.init(getConnection());
		
		assertEquals(1, this.context.getWordList().size());
		assertNotNull(this.context.getWordList().get("HOGEHOGE"));
	}
}
