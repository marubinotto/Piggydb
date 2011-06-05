package marubinotto.h2.fulltext;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

public class FullTextSearchTest extends TestWithDataSource {

	@Before
	public void given() throws Exception {
		super.given();
		
		FullTextSearch.init(getConnection());
	}
	
	@Test
    public void indexExistingRows() throws Exception {
		createTestTable();
		insertIndex("PUBLIC", "TEST", "COLUMN1,COLUMN2");
		
		insertTestRow(1, "knowledge", null);
		insertTestRow(2, null, "knowledge creation");
		
		FullTextSearch.indexExistingRows(getConnection(), "PUBLIC", "TEST");
		
		// ROWS
		assertEquals(
			"[{ID=1, HASH=1042868569, INDEXID=1, KEY=\"ID\"=1}, " +
			"{ID=2, HASH=1042868570, INDEXID=1, KEY=\"ID\"=2}]", 
			this.jdbcTemplate.queryForList("select * from FT.ROWS order by ID").toString());
		 
		// WORDS
		assertEquals(
			"[{ID=1, NAME=KNOWLEDGE}, " +
			"{ID=2, NAME=CREATION}]", 
			this.jdbcTemplate.queryForList("select * from FT.WORDS order by ID").toString());
		 
		// MAP
		assertEquals(
			"[{ROWID=1, WORDID=1}, " +
			"{ROWID=2, WORDID=1}, " +
			"{ROWID=2, WORDID=2}]", 
			this.jdbcTemplate.queryForList("select * from FT.MAP order by ROWID").toString());
	}
	
	@Test
	public void createTrigger() throws Exception {
		this.jdbcTemplate.update("CREATE TABLE TEST1(ID INT PRIMARY KEY, VALUE VARCHAR)");
		this.jdbcTemplate.update("CREATE TABLE TEST2(ID INT PRIMARY KEY, VALUE VARCHAR)");
		insertIndex("PUBLIC", "TEST1", null);
		insertIndex("PUBLIC", "TEST2", null);
		FullTextSearch.createTrigger(getConnection(), "PUBLIC", "TEST1");
		FullTextSearch.createTrigger(getConnection(), "PUBLIC", "TEST2");
		assertEquals("[FT_TEST1, FT_TEST2]", selectAllTriggerNames().toString());
	}
	
	@Test
	public void removeAllTriggers() throws Exception {
		createTrigger();
		FullTextSearch.removeAllTriggers(getConnection());
		assertTrue(selectAllTriggerNames().isEmpty());
	}
	
	@Test
	public void recreateTriggers() throws Exception {
		createTrigger();
		FullTextSearch.removeAllTriggers(getConnection());
		
		FullTextSearch.recreateTriggers(getConnection());
		assertEquals("[FT_TEST1, FT_TEST2]", selectAllTriggerNames().toString());
	}
}
