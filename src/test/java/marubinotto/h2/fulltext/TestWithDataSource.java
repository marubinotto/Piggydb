package marubinotto.h2.fulltext;

import static marubinotto.util.CollectionUtils.list;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.List;

import javax.sql.DataSource;

import marubinotto.util.RdbUtils;

import org.h2.jdbc.JdbcConnection;
import org.junit.Before;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.ConnectionProxy;

public class TestWithDataSource {

	protected DataSource dataSource;
	protected JdbcTemplate jdbcTemplate;
	
	@Before
	public void given() throws Exception {
		this.dataSource = RdbUtils.getInMemoryDataSource(null);
		this.jdbcTemplate = new JdbcTemplate(this.dataSource);
	}
	
	protected Connection getConnection() throws SQLException {
		return this.dataSource.getConnection();
	}
	
	protected JdbcConnection getJdbcConnection() throws SQLException {
		return toJdbcConnection(getConnection());
	}
	
	public static JdbcConnection toJdbcConnection(Connection connection) {
		return (JdbcConnection)((ConnectionProxy)connection).getTargetConnection();
	}
	
	protected void createTestTable() throws SQLException {
		Statement stat = this.dataSource.getConnection().createStatement();
		stat.execute("CREATE TABLE TEST(" +
			"ID INT PRIMARY KEY, " +
			"COLUMN1 VARCHAR, " +
			"COLUMN2 VARCHAR " +
		")");
	}
	
	protected void insertTestRow(Integer id, String column1, String column2) 
	throws SQLException {
		this.jdbcTemplate.update(
			"INSERT INTO TEST(ID, COLUMN1, COLUMN2) VALUES(?, ?, ?)", 
			new Object[]{id, column1, column2});	
	}
	
	protected void assertTableInfoIsTest(IndexedTableInfo tableInfo) throws Exception {
		assertNotNull(tableInfo.id);
		assertEquals("PUBLIC", tableInfo.schema);
		assertEquals("TEST", tableInfo.table);
		assertEquals(list("ID", "COLUMN1", "COLUMN2"), tableInfo.columns);
		assertEquals(list(Types.INTEGER, Types.VARCHAR, Types.VARCHAR), tableInfo.columnTypes);
		assertEquals(list(0), tableInfo.keys);
	}
	
	protected void insertIndex(String schema, String table, String indexColumns) {
		this.jdbcTemplate.update(
			"INSERT INTO FT.INDEXES(SCHEMA, TABLE, COLUMNS) VALUES(?, ?, ?)", 
			new Object[]{schema, table, indexColumns});	
	}
	
	@SuppressWarnings("unchecked")
	protected List<String> selectAllWords() {
		return this.jdbcTemplate.queryForList("select NAME from FT.WORDS order by NAME", String.class);
	}
	
	@SuppressWarnings("unchecked")
	protected List<String> selectAllTriggerNames() {
		return this.jdbcTemplate.queryForList(
			"SELECT TRIGGER_NAME FROM INFORMATION_SCHEMA.TRIGGERS order by TRIGGER_NAME", 
			String.class);
	}
}
