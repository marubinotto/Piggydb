package marubinotto.util.rdb;

import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.sql.Connection;

import javax.sql.DataSource;

import marubinotto.util.FileSystemUtils;
import marubinotto.util.RdbUtils;

import org.junit.Before;
import org.junit.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.transaction.TransactionStatus;

/**
 * @see RdbUtils
 */
public class SpringTransactionTest {

	private DataSource dataSource;
	private JdbcTemplate jdbcTemplate;
	
	@Before
	public void given() throws Exception {
		Class.forName("org.h2.Driver");
		this.dataSource = new DriverManagerDataSource(jdbcUrl(), "sa", "sa");
		this.jdbcTemplate = new JdbcTemplate(this.dataSource);
		
		this.jdbcTemplate.update(
			"create table test (" +
			"  name varchar, " +
			"  value varchar, " +
			"  primary key (name));");
		this.jdbcTemplate.update("insert into test (name, value) values('version', '1');");
	}
	
	private String jdbcUrl() throws IOException {
		String dbPath = FileSystemUtils.getEmptyDirectory().toURI() + "h2";
		return "jdbc:h2:" + dbPath + ";DB_CLOSE_DELAY=-1";
	}
	
	@Test
	public void query() throws Exception {
		assertEquals(
			"[{NAME=version, VALUE=1}]", 
			this.jdbcTemplate.queryForList("select * from test;").toString());
	}
	
	@Test
	public void disableSpringTransaction() throws Exception {
		DataSourceTransactionManager tm = new DataSourceTransactionManager(this.dataSource);
		
		TransactionStatus ts = tm.getTransaction(null);
		Connection notTransactional = this.dataSource.getConnection();
		RdbUtils.deleteAll(notTransactional, "test");
		tm.rollback(ts);
		
		assertTrue(this.jdbcTemplate.queryForList("select * from test;").isEmpty());
	}
	
	@Test
	public void enableSpringTransaction() throws Exception {
		DataSourceTransactionManager tm = new DataSourceTransactionManager(this.dataSource);
		
		TransactionStatus ts = tm.getTransaction(null);
		Connection transactional = RdbUtils.getSpringTransactionalConnection(this.dataSource);
		RdbUtils.deleteAll(transactional, "test");
		tm.rollback(ts);
		
		assertEquals(	// should be rollbacked
			"[{NAME=version, VALUE=1}]", 
			this.jdbcTemplate.queryForList("select * from test;").toString());
	}	
}
