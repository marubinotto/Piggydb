package marubinotto.piggydb.impl.jdbc;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import marubinotto.h2.fulltext.FullTextSearch;
import marubinotto.util.Assert;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.core.JdbcTemplate;

public class DatabaseSchema {
	
	private static Log logger = LogFactory.getLog(DatabaseSchema.class);
	
	private static Map<String, String> sqlCache = new HashMap<String, String>();
	
	private static String getSql(String path) throws IOException {
		String sql = sqlCache.get(path);
		if (sql != null) {
			return sql;
		}
		
		logger.info("Loading :" + path);
		InputStream input = DatabaseSchema.class.getClassLoader().getResourceAsStream(path);
		if (input == null) {
			logger.info("Not found :" + path);
			return null;
		}
		try {
			sql = IOUtils.toString(input);
		}
		finally {
			IOUtils.closeQuietly(input);
		}
		sqlCache.put(path, sql);
		return sql;
	}
	
	private JdbcTemplate jdbcTemplate;
	
	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}
	
	private static final String INITIAL_DATABASE = "sql/initial-database.sql";
	private static final String DELTA_DIR = "sql/database-delta/";
	
	public static String getDeltaSql(int version) throws IOException {
		return getSql(DELTA_DIR + version + ".sql");
	}
	
	private void initFullText() throws SQLException {
		logger.info("Initializing full text search ...");	
		Connection connection = this.jdbcTemplate.getDataSource().getConnection();
		FullTextSearch.init(connection);
	}

	public void update() throws DataAccessException, IOException, SQLException {
		logger.info("Updating schema ...");
		
		initFullText();
		
		int version = getVersion();
		logger.info("Version: " + version);
		
		// Initial schema
		if (version == 0) {
			String sql = getSql(INITIAL_DATABASE);
			logger.info("Creating initial database ...");
			this.jdbcTemplate.update(sql);
			version = getVersion();
			Assert.assertTrue(version == 1, "version should be 1 after init");
		}
		
		// Apply delta
		while (true) {
			version++;
			String deltaSql = getDeltaSql(version);
			if (deltaSql == null) {
				break;
			}
			logger.info("Applying delta: " + version);
			this.jdbcTemplate.update(deltaSql);
			setVersion(version);
		}
		
		version = getVersion();
		logger.info("Database has been updated to: " + version);
	}
	
	private static final String KEY_VERSION = "database.version";
	
	public int getVersion() {
		try {
			return this.jdbcTemplate.queryForInt(
				"select setting_value from global_setting where setting_name = ?", 
				new Object[]{KEY_VERSION});
		}
		catch (BadSqlGrammarException e) {
			logger.info("Couldn't get a version: " + e.getSQLException().getMessage());
			return 0;
		}
	}
	
	public void setVersion(int version) {
		int affected = this.jdbcTemplate.update(
			"update global_setting set setting_value = ? where setting_name = ?",
			new Object[]{String.valueOf(version), KEY_VERSION});
		if (affected == 0) {
			this.jdbcTemplate.update(
				"insert into global_setting (setting_name, setting_value) values(?, ?)", 
				new Object[]{KEY_VERSION, String.valueOf(version)});
		}
	}
	
	public static interface MetaDataHandler {
		public void handle(DatabaseMetaData metaData) throws SQLException;
	}
	
	public void analyzeMetadata(MetaDataHandler handler) 
	throws SQLException {
		Assert.Arg.notNull(handler, "handler");
		Assert.Property.requireNotNull(jdbcTemplate, "jdbcTemplate");
		
		Connection connection = this.jdbcTemplate.getDataSource().getConnection();
		try {
			handler.handle(connection.getMetaData());
		}
		finally {
			connection.close();
		}
	}
}
