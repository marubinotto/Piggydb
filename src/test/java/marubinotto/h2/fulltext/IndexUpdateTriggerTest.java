package marubinotto.h2.fulltext;

import static marubinotto.util.CollectionUtils.list;
import static marubinotto.util.CollectionUtils.set;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Set;

import org.h2.api.Trigger;
import org.junit.Before;
import org.junit.Test;

public class IndexUpdateTriggerTest extends TestWithDataSource {
	
	private IndexUpdateTrigger object;

	@Before
	public void given() throws Exception {
		super.given();
		
		FullTextSearch.init(getConnection());
		
		createTestTable();
		insertIndex("PUBLIC", "TEST", "COLUMN1,COLUMN2");
		
		this.object = new IndexUpdateTrigger();
		this.object.init(
			getConnection(), 
			"PUBLIC", "FT_TEST", "TEST", 
			false, Trigger.INSERT);
	}
	
	@Test
    public void init() throws Exception {
		assertNotNull(this.object.context);
		
		IndexedTableInfo tableInfo = this.object.tableInfo;
		assertTableInfoIsTest(tableInfo);
		assertEquals(list(1, 2), tableInfo.indexColumns);
		
		assertSame(tableInfo, this.object.context.getIndexedTableInfo(tableInfo.id));
	}
	
	@Test
    public void insert() throws Exception {
		 this.object.insert(new Object[]{1, "knowledge", null});
		 
		 // ROWS
		 assertEquals(
			 "{ID=1, HASH=1042868569, INDEXID=1, KEY=\"ID\"=1}", 
			 this.jdbcTemplate.queryForMap("select * from FT.ROWS").toString());
		 
		 // WORDS
		 assertEquals(
			 "{ID=1, NAME=KNOWLEDGE}", 
			 this.jdbcTemplate.queryForMap("select * from FT.WORDS").toString());
		 
		 // MAP
		 assertEquals(
			 "{ROWID=1, WORDID=1}", 
			 this.jdbcTemplate.queryForMap("select * from FT.MAP").toString());
	}
	
	@Test
    public void delete() throws Exception {
		this.object.insert(new Object[]{1, "knowledge", null});
		this.object.insert(new Object[]{2, "fragment", null});
		this.object.delete(new Object[]{1, "knowledge", null});
		
		 // ROWS
		 assertEquals(
			 "{ID=2, HASH=1042868570, INDEXID=1, KEY=\"ID\"=2}", 
			 this.jdbcTemplate.queryForMap("select * from FT.ROWS").toString());
		 
		 // WORDS
		 assertEquals(
			 "[{ID=1, NAME=KNOWLEDGE}, {ID=2, NAME=FRAGMENT}]", 
			 this.jdbcTemplate.queryForList("select * from FT.WORDS order by ID").toString());
		 
		 // MAP
		 assertEquals(
			 "{ROWID=2, WORDID=2}", 
			 this.jdbcTemplate.queryForMap("select * from FT.MAP").toString());
	}
	
	@Test
	@SuppressWarnings("unchecked")
	public void getWordIds_withoutExistingWords() throws Exception {
		Set<Integer> results = this.object.getWordIds(
			new Object[]{1, "notebook application", "knowledge fragment"});
		
		// Return value
		assertEquals(4, results.size());
		assertEquals(
			new HashSet<Integer>(this.jdbcTemplate.queryForList("select ID from FT.WORDS", Integer.class)), 
			results);
		
		// FullTextSearchContext.wordList
		FullTextSearchContext context = FullTextSearchContext.getContext(getConnection());
		assertEquals(4, context.getWordList().size());
		assertEquals(
			set("NOTEBOOK", "APPLICATION", "KNOWLEDGE", "FRAGMENT"), 
			context.getWordList().keySet());
		
		// WORDS
		assertEquals(
			list("APPLICATION", "FRAGMENT", "KNOWLEDGE", "NOTEBOOK"),
			selectAllWords());
	}
	
	@Test
    public void getWordIds_withExistingWords() throws Exception {
		// Existing word
		Integer knowledgeId = this.object.getWordIds(new Object[]{1, "knowledge", null}).iterator().next();
		FullTextSearchContext context = FullTextSearchContext.getContext(getConnection());
		assertEquals(set("KNOWLEDGE"), context.getWordList().keySet());
		assertEquals(list("KNOWLEDGE"), selectAllWords());
		
		// Return value
		Set<Integer> results = this.object.getWordIds(new Object[]{2, null, "knowledge fragment"});
		assertEquals(2, results.size());
		assertTrue(results.contains(knowledgeId));
		
		// FullTextSearchContext.wordList
		assertEquals(2, context.getWordList().size());
		assertEquals(set("KNOWLEDGE", "FRAGMENT"), context.getWordList().keySet());
		assertEquals(knowledgeId, context.getWordList().get("KNOWLEDGE"));
		
		// WORDS
		assertEquals(list("FRAGMENT", "KNOWLEDGE"), selectAllWords());
	}
}
