package marubinotto.h2.fulltext.function;

import static org.junit.Assert.assertEquals;
import marubinotto.h2.fulltext.FullTextSearch;
import marubinotto.h2.fulltext.TestWithDataSource;

import org.junit.Before;
import org.junit.Test;

public class CreateIndexTest extends TestWithDataSource {

	@Before
	public void given() throws Exception {
		super.given();
		
		FullTextSearch.init(getConnection());
		createTestTable();
	}
	
	@Test
	public void empty() throws Exception {
		FullTextSearch.createIndex(getConnection(), "PUBLIC", "TEST", null);
		
		assertEquals(
			"[{ID=1, SCHEMA=PUBLIC, TABLE=TEST, COLUMNS=null}]", 
			this.jdbcTemplate.queryForList("select * from FT.INDEXES").toString());	
		assertEquals("[FT_TEST]", selectAllTriggerNames().toString());
	}
	
	@Test
	public void withExistingOneRow() throws Exception {
		insertTestRow(1, "knowledge", null);
		
		FullTextSearch.createIndex(getConnection(), "PUBLIC", "TEST", "COLUMN1,COLUMN2");
		
		assertEquals(
			"[{ID=1, SCHEMA=PUBLIC, TABLE=TEST, COLUMNS=COLUMN1,COLUMN2}]", 
			this.jdbcTemplate.queryForList("select * from FT.INDEXES").toString());	
		assertEquals("[FT_TEST]", selectAllTriggerNames().toString());
		
		assertEquals(
			 "[{ID=1, HASH=1042868569, INDEXID=1, KEY=\"ID\"=1}]", 
			 this.jdbcTemplate.queryForList("select * from FT.ROWS").toString());
		assertEquals(
			 "[{ID=1, NAME=KNOWLEDGE}]", 
			 this.jdbcTemplate.queryForList("select * from FT.WORDS").toString());
		assertEquals(
			 "[{ROWID=1, WORDID=1}]", 
			 this.jdbcTemplate.queryForList("select * from FT.MAP").toString());
	}
}
