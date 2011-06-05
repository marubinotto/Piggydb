package marubinotto.h2.fulltext;

import static marubinotto.util.CollectionUtils.list;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.sql.Types;

import org.junit.Before;
import org.junit.Test;

public class IndexedTableInfoTest extends TestWithDataSource {

	@Before
	public void given() throws Exception {
		super.given();

		FullTextSearch.init(getConnection());
		createTestTable();
	}
	
	@Test
    public void indexAllColumns() throws Exception {
		insertIndex("PUBLIC", "TEST", null);
		
		IndexedTableInfo result = IndexedTableInfo.newInstance(getConnection(), "PUBLIC", "TEST");
		
		assertTableInfoIsTest(result);
		assertEquals(list(0, 1, 2), result.indexColumns);
	}
	
	@Test
    public void indexOneColumn() throws Exception {
		insertIndex("PUBLIC", "TEST", "COLUMN1");
		
		IndexedTableInfo result = IndexedTableInfo.newInstance(getConnection(), "PUBLIC", "TEST");
		
		assertTableInfoIsTest(result);
		assertEquals(list(1), result.indexColumns);
	}
	
	@Test
    public void indexTwoColumns() throws Exception {
		insertIndex("PUBLIC", "TEST", "COLUMN1,COLUMN2");
		
		IndexedTableInfo result = IndexedTableInfo.newInstance(getConnection(), "PUBLIC", "TEST");
		
		assertTableInfoIsTest(result);
		assertEquals(list(1, 2), result.indexColumns);
	}
	
	@Test
	public void haveIndexedColumnsChanged() throws Exception {
		IndexedTableInfo info = new IndexedTableInfo();
		info.indexColumns = list(1);
		Object[] oldRow = new Object[]{1, "foo", "bar"};
		
		assertFalse(info.haveIndexedColumnsChanged(oldRow, new Object[]{1, "foo", "bar"}));
		assertTrue(info.haveIndexedColumnsChanged(oldRow, new Object[]{1, "changed", "bar"}));
		assertFalse(info.haveIndexedColumnsChanged(oldRow, new Object[]{1, "foo", "changed"}));
	}
	
	@Test
    public void createConditionSqlWithKeys() throws Exception {
		IndexedTableInfo object = new IndexedTableInfo();
		object.columns = list("ID", "NAME");
		object.columnTypes = list(Types.INTEGER, Types.VARCHAR);
		object.keys = list(0);
		
		String result = object.createConditionSqlWithKeys(new Object[]{1, "Akane"});
		assertEquals("\"ID\"=1", result);
	}
}
