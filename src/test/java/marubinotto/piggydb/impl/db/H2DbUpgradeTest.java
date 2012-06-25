package marubinotto.piggydb.impl.db;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;

import marubinotto.piggydb.impl.db.DatabaseSchema;
import marubinotto.piggydb.impl.db.H2DbUpgrade;
import marubinotto.util.FileSystemUtils;
import marubinotto.util.procedure.Transaction;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.ArrayUtils;
import org.junit.Before;
import org.junit.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

public class H2DbUpgradeTest {
	
	private H2DbUpgrade object = new H2DbUpgrade();

	private File databaseDir;
	private String databasePrefix;
	
	private H2JdbcUrl h2JdbcUrl = new H2JdbcUrl();
	private DriverManagerDataSource dataSource = new DriverManagerDataSource();
	
	@Before
	public void given() throws Exception {	
		this.databaseDir = FileSystemUtils.getEmptyDirectory();
		this.databasePrefix = this.databaseDir.toURI().toString();
		
		this.h2JdbcUrl.setDatabasePrefix(this.databasePrefix);
		this.object.setH2JdbcUrl(this.h2JdbcUrl);
		
		this.dataSource.setDriverClassName("org.h2.Driver");
		this.dataSource.setUrl(this.h2JdbcUrl.getUrl());
		this.dataSource.setUsername("sa");
		this.dataSource.setPassword("");
		this.object.setDataSource(this.dataSource);
		
		DatabaseSchema schema = new DatabaseSchema();
		schema.setJdbcTemplate(new JdbcTemplate(this.dataSource));
		this.object.setDatabaseSchema(schema);
		
		this.object.setUsername("sa");
		this.object.setPassword("");
		
		Transaction transaction = new Transaction();
		transaction.setTransactionManager(new DataSourceTransactionManager(this.dataSource));
		this.object.setTransaction(transaction);
	}
	
	@Test
	public void isDatabaseFileFormatVersion1_1_empty() throws Exception {
		assertFalse(this.object.isDatabaseFileFormatVersion1_1());
	}
	
	@Test
	public void isDatabaseFileFormatVersion1_1() throws Exception {
		File testDbFiles = new File(FileSystemUtils.getPackageDirectory(getClass()), "v1_1");
		FileUtils.copyDirectory(testDbFiles, this.databaseDir);
		assertEquals(
			"{piggydb.1.log.db," +
			"piggydb.data.db," +
			"piggydb.index.db," +
			"piggydb.trace.db}", 
			ArrayUtils.toString(this.databaseDir.list()));
		
		assertTrue(this.object.isDatabaseFileFormatVersion1_1());
	}
	
	@Test
	public void connectWithVersion1_1() throws Exception {
		isDatabaseFileFormatVersion1_1();
		
		Connection connection = this.object.connectWithVersion1_1();
		checkDatabaseContent(connection);
		connection.close();
	}
	
	@Test
	public void renameDatabaseFiles() throws Exception {
		isDatabaseFileFormatVersion1_1();
		
		File exportFilePath = this.object.getExportFilePath();
		FileUtils.writeStringToFile(exportFilePath, "dummy");
		this.object.renameDatabaseFiles(exportFilePath);
		
		assertEquals(
			"{piggydb.1.log.db.v1_1," +
			"piggydb.data.db.v1_1," +
			"piggydb.dump.xml," +		// must not be targeted
			"piggydb.index.db.v1_1," +
			"piggydb.trace.db.v1_1}", 
			ArrayUtils.toString(this.databaseDir.list()));
	}
	
	@Test
	public void exportAndRestore() throws Exception {
		// Export
		isDatabaseFileFormatVersion1_1();
		File exportFilePath = this.object.exportDatabase();
		
		// Delete files except for the dump
		for (File file : this.databaseDir.listFiles()) {
			if (file.isFile() && !file.equals(exportFilePath)) FileUtils.forceDelete(file);
		}
		assertEquals("{piggydb.dump.xml}", ArrayUtils.toString(this.databaseDir.list()));
		
		// Restore
		this.object.restoreDatabaseWithNewVersion(exportFilePath);
		assertTrue(Arrays.asList(this.databaseDir.list()).contains("piggydb.h2.db"));
		
		Connection connection = this.dataSource.getConnection();
		checkDatabaseContent(connection);
		connection.close();
	}
	
	
// Internals
	
	private void checkDatabaseContent(Connection connection) throws SQLException	 {
		Statement stat = connection.createStatement();
		ResultSet rs = stat.executeQuery(
			"select setting_value from global_setting where setting_name = 'database.title'");
		assertTrue(rs.next());
		assertEquals("Akane", rs.getString("setting_value"));
	}
}
