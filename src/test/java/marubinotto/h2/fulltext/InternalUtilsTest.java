package marubinotto.h2.fulltext;

import static org.junit.Assert.assertEquals;

import java.sql.Connection;

import marubinotto.util.RdbUtils;

import org.apache.commons.lang.ArrayUtils;
import org.junit.Test;

public class InternalUtilsTest {

	@Test
    public void getIndexPathOfPrivateInMemoryDb() throws Exception {
		Connection conn = RdbUtils.getInMemoryDataSource(null).getConnection();
		String result = InternalUtils.getIndexPath(conn);
		assertEquals("MEM:UNNAMED", result);
	}

	@Test
    public void getIndexPathOfInMemoryDb() throws Exception {
		Connection conn = RdbUtils.getInMemoryDataSource("test").getConnection();
		String result = InternalUtils.getIndexPath(conn);
		assertEquals("MEM:TEST", result);
	}
	
	@Test
    public void quoteString() throws Exception {
		assertEquals("'hogehoge'", InternalUtils.quoteString("hogehoge"));
	}
	
	@Test
    public void quoteStringWithQuote() throws Exception {
		assertEquals("'hoge''hoge'", InternalUtils.quoteString("hoge'hoge"));
	}
	
	@Test
    public void parseConditionSqlToColumnsAndValues() throws Exception {
		Object[][] nameAndValues = 
			InternalUtils.parseConditionSqlToColumnsAndValues(
				"key1 = 1 and key2 = 'Akane'", 
				TestWithDataSource.toJdbcConnection(
					RdbUtils.getInMemoryDataSource(null).getConnection()));
		assertEquals("{KEY1,KEY2}", ArrayUtils.toString(nameAndValues[0]));
		assertEquals("{1,Akane}", ArrayUtils.toString(nameAndValues[1]));
	}
}
