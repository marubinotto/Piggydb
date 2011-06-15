package marubinotto.piggydb.model;

import java.util.HashMap;
import java.util.Map;

import marubinotto.util.time.DateTime;

import org.apache.commons.lang.StringUtils;

public abstract class GlobalSetting {

	public abstract void put(String name, String value) throws Exception;
	
	public abstract String get(String name) throws Exception;
	
	
	public static class InMemory extends GlobalSetting {
		
		private Map<String, String> entries = new HashMap<String, String>();
		
		public void put(String name, String value) throws Exception {
			this.entries.put(name, value);
		}
		
		public String get(String name) throws Exception {
			return this.entries.get(name);
		}
	}
	

	// Global Setting Key (GSK) definitions
	public static final String GSK_DATABASE_TITLE = "database.title";
	public static final String GSK_DATABASE_TIMESTAMP = "database.timestamp";
	
	
	private static final String DEFAULT_DATABASE_TITLE = "Piggydb";
	
	public String getDatabaseTitle() throws Exception {
		String title = get(GSK_DATABASE_TITLE);
		return title != null ? title : DEFAULT_DATABASE_TITLE;
	}
	
	public void setDatabaseTitle(String databaseTitle) throws Exception {
		databaseTitle = StringUtils.trimToNull(databaseTitle);
		put(GSK_DATABASE_TITLE, databaseTitle);
	}
	
	public static final String URI_SEP = ":";
	private static final String TAG_URI_PREFIX = "tag:";
	private static final String DEFAULT_DOMAIN = "piggydb.net,2009";
	private static final String DB_TIMESTAMP_PREFIX = "db-";
	private static final String DB_TIMESTAMP_FORMAT = "yyyyMMddHHmmssS";
	
	public String getDatabaseId() throws Exception {
		return TAG_URI_PREFIX + DEFAULT_DOMAIN + 
			URI_SEP + DB_TIMESTAMP_PREFIX + getDatabaseTimestampString();
	}
	
	private String getDatabaseTimestampString() throws Exception {
		String timestamp = get(GSK_DATABASE_TIMESTAMP);
		if (timestamp == null) {
			timestamp = DateTime.getCurrentTime().format(DB_TIMESTAMP_FORMAT);
			put(GSK_DATABASE_TIMESTAMP, timestamp);
		}
		return timestamp;
	}
	
	public DateTime getDatabaseTimestamp() throws Exception {
		return new DateTime(getDatabaseTimestampString(), DB_TIMESTAMP_FORMAT);
	}

	// Implementation-specific GSKs
	// 	database.version - marubinotto.piggydb.impl.jdbc.DatabaseSchema
	// 	owner.password - marubinotto.piggydb.model.OwnerAuth
}
