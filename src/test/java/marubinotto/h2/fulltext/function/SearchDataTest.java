package marubinotto.h2.fulltext.function;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Set;
import java.util.TreeSet;

import marubinotto.h2.fulltext.FullTextSearch;
import marubinotto.h2.fulltext.TestWithDataSource;

import org.apache.commons.lang.ArrayUtils;
import org.junit.Before;
import org.junit.Test;

public class SearchDataTest extends TestWithDataSource {

	@Before
	public void given() throws Exception {
		super.given();
		
		FullTextSearch.init(this.dataSource.getConnection());
		createTestTable();
		FullTextSearch.createIndex(getConnection(), "PUBLIC", "TEST", "COLUMN1,COLUMN2");
	}
	
	@Test
	public void empty() throws Exception {
		ResultSet rs = FullTextSearch.searchData(getJdbcConnection(), "knowledge", 0, 0);
		assertFalse(rs.next());
	}
	
	@Test
	public void zeroOutOfOne() throws Exception {
		insertTestRow(1, "knowledge", null);	
		ResultSet rs = FullTextSearch.searchData(getJdbcConnection(), "fragment", 0, 0);
		assertFalse(rs.next());
	}
	
	@Test
	public void oneOutOfOne() throws Exception {
		insertTestRow(1, "knowledge", null);
		ResultSet rs = FullTextSearch.searchData(getJdbcConnection(), "knowledge", 0, 0);
		assertResultSetHasOneResult(rs, 1);
	}
	
	@Test
	public void japanese() throws Exception {
		insertTestRow(1, "Piggydbは新感覚の情報管理ツールです。", null);
		ResultSet rs = FullTextSearch.searchData(getJdbcConnection(), "情報", 0, 0);
		assertResultSetHasOneResult(rs, 1);
	}
	
	@Test
	public void multipleWords() throws Exception {
		insertTestRow(1, "knowledge", null);	
		insertTestRow(2, "knowledge fragment", null);
		
		ResultSet rs = FullTextSearch.searchData(getJdbcConnection(), "fragment knowledge", 0, 0);
		
		assertResultSetHasOneResult(rs, 2);
	}
	
	@Test
	public void twoOutOfTwo() throws Exception {
		insertTestRow(1, "knowledge", null);	
		insertTestRow(2, "knowledge fragment", null);
		
		ResultSet rs = FullTextSearch.searchData(getJdbcConnection(), "knowledge", 0, 0);
		
		Set<String> keyValues = new TreeSet<String>();
		assertTrue(rs.next());
		keyValues.add(ArrayUtils.toString(rs.getArray("KEYS").getArray()));
		assertTrue(rs.next());
		keyValues.add(ArrayUtils.toString(rs.getArray("KEYS").getArray()));
		assertFalse(rs.next());
		assertEquals("[{1}, {2}]", keyValues.toString());
	}
	
	
// Internals
	
	private void assertResultSetHasOneResult(ResultSet rs, int id) throws SQLException {
		assertTrue(rs.next());
		assertEquals("PUBLIC", rs.getString("SCHEMA"));
		assertEquals("TEST", rs.getString("TABLE"));
		assertEquals("{ID}", ArrayUtils.toString(rs.getArray("COLUMNS").getArray()));
		assertEquals("{" + id + "}", ArrayUtils.toString(rs.getArray("KEYS").getArray()));
		assertFalse(rs.next());
	}
}
