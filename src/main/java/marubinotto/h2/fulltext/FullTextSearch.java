package marubinotto.h2.fulltext;

import static marubinotto.h2.fulltext.InternalUtils.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.h2.api.Trigger;
import org.h2.jdbc.JdbcConnection;
import org.h2.message.DbException;
import org.h2.tools.SimpleResultSet;
import org.h2.util.New;
import org.h2.util.StringUtils;

public class FullTextSearch {
	
	private static Log logger = LogFactory.getLog(FullTextSearch.class);
	
	public static final String SCHEMA = "FT";
	private static final String TRIGGER_PREFIX = "FT_";

    public static void init(Connection conn) throws SQLException {
        Statement stat = conn.createStatement();
        
        stat.execute("CREATE SCHEMA IF NOT EXISTS " + SCHEMA);
        
        // the structure of the index: word -> word ID -> row ID -> row info -> table info (IndexedTableInfo)
        
        // indexed table info
        //   COLUMNS: a list of column names (column separated) to index; null -> all columns are indexed
        stat.execute("CREATE TABLE IF NOT EXISTS " + SCHEMA
        	+ ".INDEXES(" +
        		"ID INT AUTO_INCREMENT PRIMARY KEY, " +
        		"SCHEMA VARCHAR, " +
        		"TABLE VARCHAR, " +
        		"COLUMNS VARCHAR, " +
        		"UNIQUE(SCHEMA, TABLE))");
        
        // word -> word ID
        stat.execute("CREATE TABLE IF NOT EXISTS " + SCHEMA
        	+ ".WORDS(" +
        		"ID INT AUTO_INCREMENT PRIMARY KEY, " +
        		"NAME VARCHAR, " +
        		"UNIQUE(NAME))");
        
        // indexed row
        //   KEY: condition(where) SQL by the key values for selecting the row
        stat.execute("CREATE TABLE IF NOT EXISTS " + SCHEMA
        	+ ".ROWS(" +
        		"ID IDENTITY, " +
        		"HASH INT, " +
        		"INDEXID INT, " +
        		"KEY VARCHAR, " +
        		"UNIQUE(HASH, INDEXID, KEY))");
        
        // word ID -> indexed row ID
        stat.execute("CREATE TABLE IF NOT EXISTS " + SCHEMA
        	+ ".MAP(" +
        		"ROWID INT, " +
        		"WORDID INT, " +
        		"PRIMARY KEY(WORDID, ROWID))");
        
        // NOTE: Should re-create alias because Java's name could be changed by refactoring
        stat.execute("DROP ALIAS IF EXISTS FT_CREATE_INDEX");
        stat.execute("DROP ALIAS IF EXISTS FT_DROP_INDEX");
        stat.execute("DROP ALIAS IF EXISTS FT_SEARCH");
        stat.execute("DROP ALIAS IF EXISTS FT_SEARCH_DATA");
        stat.execute("DROP ALIAS IF EXISTS FT_REINDEX");
        stat.execute("DROP ALIAS IF EXISTS FT_DROP_ALL");
        
        // aliases for static method of this class
        stat.execute("CREATE ALIAS IF NOT EXISTS FT_CREATE_INDEX FOR \"" + FullTextSearch.class.getName() + ".createIndex\"");
        stat.execute("CREATE ALIAS IF NOT EXISTS FT_DROP_INDEX FOR \"" + FullTextSearch.class.getName() + ".dropIndex\"");
        stat.execute("CREATE ALIAS IF NOT EXISTS FT_SEARCH FOR \"" + FullTextSearch.class.getName() + ".search\"");
        stat.execute("CREATE ALIAS IF NOT EXISTS FT_SEARCH_DATA FOR \"" + FullTextSearch.class.getName() + ".searchData\"");
        stat.execute("CREATE ALIAS IF NOT EXISTS FT_REINDEX FOR \"" + FullTextSearch.class.getName() + ".reindex\"");
        stat.execute("CREATE ALIAS IF NOT EXISTS FT_DROP_ALL FOR \"" + FullTextSearch.class.getName() + ".dropAll\"");
        
        // NOTE: Should re-create triggers because Java's name could be changed by refactoring
        recreateTriggers(conn);
        
        FullTextSearchContext context = FullTextSearchContext.getContext(conn);
        
        ResultSet rs = stat.executeQuery("SELECT * FROM " + SCHEMA + ".WORDS");
        Map<String, Integer> map = context.getWordList();
        map.clear();
        while (rs.next()) {
            String word = rs.getString("NAME");
            int id = rs.getInt("ID");
            word = context.convertWord(word);
            if (word != null) map.put(word, id);
        }
        
        logger.info("Initialized: (words " + map.size() + ")");
    }
    
    public static void createIndex(Connection conn, String schema, String table, String columnList) throws SQLException {
        init(conn);
        
        // Add an entry to INDEXES
        PreparedStatement prep = conn.prepareStatement("INSERT INTO " + SCHEMA
        	+ ".INDEXES(SCHEMA, TABLE, COLUMNS) VALUES(?, ?, ?)");
        prep.setString(1, schema);
        prep.setString(2, table);
        prep.setString(3, columnList);
        prep.execute();
        
        createTrigger(conn, schema, table);
        
        indexExistingRows(conn, schema, table);
    }
    
    /**
     * Re-creates the full text index for this database. Calling this method is
     * usually not needed, as the index is kept up-to-date automatically.
     */
    public static void reindex(Connection conn) throws SQLException {
        init(conn);
	
        removeAllTriggers(conn);
	
        // Clear the index cache
        FullTextSearchContext context = FullTextSearchContext.getContext(conn);
        context.getWordList().clear();
	
        // Delete all the indexes
        Statement stat = conn.createStatement();
        stat.execute("TRUNCATE TABLE " + SCHEMA + ".WORDS");
        stat.execute("TRUNCATE TABLE " + SCHEMA + ".ROWS");
        stat.execute("TRUNCATE TABLE " + SCHEMA + ".MAP");
	
        // Recreate triggers and indexes
        ResultSet rs = stat.executeQuery("SELECT * FROM " + SCHEMA + ".INDEXES");
        while (rs.next()) {
            String schema = rs.getString("SCHEMA");
            String table = rs.getString("TABLE");
            createTrigger(conn, schema, table);
            indexExistingRows(conn, schema, table);
        }
    }
    
    /**
     * Searches from the full text index for this database.
     * The returned result set has the following column:
     * <ul><li>QUERY (varchar): the query to use to get the data.
     * The query does not include 'SELECT * FROM '. Example:
     * PUBLIC.TEST WHERE ID = 1
     * </li><li>SCORE (float) the relevance score. This value is always 1.0
     * for the native fulltext search.
     * </li></ul>
     *
     * @param conn the connection
     * @param text the search query
     * @param limit the maximum number of rows or 0 for no limit
     * @param offset the offset or 0 for no offset
     * @return the result set
     */
    public static ResultSet search(Connection conn, String text, int limit, int offset) throws SQLException {
        try {
            return search(conn, text, limit, offset, false);
        } 
        catch (DbException e) {
            throw DbException.toSQLException(e);
        }
    }

    /**
     * Searches from the full text index for this database. The result contains
     * the primary key data as an array. The returned result set has the
     * following columns:
     * <ul>
     * <li>SCHEMA (varchar): the schema name. Example: PUBLIC </li>
     * <li>TABLE (varchar): the table name. Example: TEST </li>
     * <li>COLUMNS (array of varchar): comma separated list of quoted column
     * names. The column names are quoted if necessary. Example: (ID) </li>
     * <li>KEYS (array of values): comma separated list of values. Example: (1)
     * </li>
     * <li>SCORE (float) the relevance score. This value is always 1.0
     * for the native fulltext search.
     * </li>
     * </ul>
     *
     * @param conn the connection, which must be JdbcConnection
     * @param text the search query
     * @param limit the maximum number of rows or 0 for no limit
     * @param offset the offset or 0 for no offset
     * @return the result set
     */
    public static ResultSet searchData(Connection conn, String text, int limit, int offset) throws SQLException {
        try {
            return search(conn, text, limit, offset, true);
        } 
        catch (DbException e) {
            throw DbException.toSQLException(e);
        }
    }

    /**
     * Drop an existing full text index for a table. This method returns
     * silently if no index for this table exists.
     *
     * @param conn the connection
     * @param schema the schema name of the table (case sensitive)
     * @param table the table name (case sensitive)
     */
    public static void dropIndex(Connection conn, String schema, String table) throws SQLException {
        init(conn);
        
        // Delete the table info from INDEXES
        PreparedStatement prep = conn.prepareStatement(
        	"SELECT ID FROM " + SCHEMA + ".INDEXES WHERE SCHEMA=? AND TABLE=?");
        prep.setString(1, schema);
        prep.setString(2, table);
        ResultSet rs = prep.executeQuery();
        if (!rs.next()) {
            return;
        }
        int indexId = rs.getInt(1);
        prep = conn.prepareStatement("DELETE FROM " + SCHEMA  + ".INDEXES WHERE ID=?");
        prep.setInt(1, indexId);
        prep.execute();
        
        // Delete the trigger
        createOrDropTrigger(conn, schema, table, false);
        
        // Delete the entries in ROWS
        prep = conn.prepareStatement(
        	"DELETE FROM " + SCHEMA + ".ROWS WHERE INDEXID=? AND ROWNUM<10000");
        while (true) {
            prep.setInt(1, indexId);
            int deleted = prep.executeUpdate();
            if (deleted == 0) {
                break;
            }
        }
        
        // Delete the entries in MAP
        prep = conn.prepareStatement(
        	"DELETE FROM " + SCHEMA + ".MAP M " +
                "WHERE NOT EXISTS (SELECT * FROM " + SCHEMA + ".ROWS R WHERE R.ID=M.ROWID) AND ROWID<10000");
        while (true) {
            int deleted = prep.executeUpdate();
            if (deleted == 0) {
                break;
            }
        }
    }
    
    /**
     * Drops all full text indexes from the database.
     *
     * @param conn the connection
     */
    public static void dropAll(Connection conn) throws SQLException {
        init(conn);
        Statement stat = conn.createStatement();
        stat.execute("DROP SCHEMA IF EXISTS " + SCHEMA);
        removeAllTriggers(conn);
        FullTextSearchContext.getContext(conn).clearAll();
    }

    
// Internals
    
    protected static void indexExistingRows(Connection conn, String schema, String table) throws SQLException {
    	IndexUpdateTrigger existing = new IndexUpdateTrigger();
        existing.init(conn, schema, null, table, false, Trigger.INSERT);
        
        String sql = "SELECT * FROM " + StringUtils.quoteIdentifier(schema) + "." + StringUtils.quoteIdentifier(table);
        ResultSet rs = conn.createStatement().executeQuery(sql);
        int columnCount = rs.getMetaData().getColumnCount();
        while (rs.next()) {
            Object[] row = new Object[columnCount];
            for (int i = 0; i < columnCount; i++) {
                row[i] = rs.getObject(i + 1);
            }
            existing.fire(conn, null, row);
        }
    }

    protected static void createTrigger(Connection conn, String schema, String table) throws SQLException {
        createOrDropTrigger(conn, schema, table, true);
    }

    private static void createOrDropTrigger(Connection conn, String schema, String table, boolean create) 
    throws SQLException {
        Statement stat = conn.createStatement();
        String trigger = StringUtils.quoteIdentifier(schema) + "."
        	+ StringUtils.quoteIdentifier(TRIGGER_PREFIX + table);
        stat.execute("DROP TRIGGER IF EXISTS " + trigger);
        if (create) {
            StringBuilder buff = new StringBuilder("CREATE TRIGGER IF NOT EXISTS ");
            buff.append(trigger).
                append(" AFTER INSERT, UPDATE, DELETE ON ").
                append(StringUtils.quoteIdentifier(schema)).
                append('.').
                append(StringUtils.quoteIdentifier(table)).
                append(" FOR EACH ROW CALL \"").
                append(IndexUpdateTrigger.class.getName()).
                append("\"");
            stat.execute(buff.toString());
        }
    }
    
    protected static void removeAllTriggers(Connection conn) throws SQLException {
        Statement selectAllTriggers = conn.createStatement();
        ResultSet triggers = selectAllTriggers.executeQuery("SELECT * FROM INFORMATION_SCHEMA.TRIGGERS");
        Statement dropTrigger = conn.createStatement();
        while (triggers.next()) {
            String schema = triggers.getString("TRIGGER_SCHEMA");
            String name = triggers.getString("TRIGGER_NAME");
            if (name.startsWith(TRIGGER_PREFIX)) {
                name = StringUtils.quoteIdentifier(schema) + "." + StringUtils.quoteIdentifier(name);
                dropTrigger.execute("DROP TRIGGER " + name);
            }
        }
    }
    
    protected static void recreateTriggers(Connection conn) throws SQLException {
    	removeAllTriggers(conn);
    	
    	Statement stat = conn.createStatement();
    	ResultSet indexedTables = stat.executeQuery("SELECT * FROM " + SCHEMA + ".INDEXES");
        while (indexedTables.next()) {
            String schema = indexedTables.getString("SCHEMA");
            String table = indexedTables.getString("TABLE");
            createTrigger(conn, schema, table);
        }
    }
    
    
    //
    // search internals
    //
    
    private static final String SELECT_MAP_BY_WORD_ID = "SELECT ROWID FROM " + SCHEMA + ".MAP WHERE WORDID=?";
    private static final String SELECT_ROW_BY_ID = "SELECT KEY, INDEXID FROM " + SCHEMA + ".ROWS WHERE ID=?";
   
    /**
     * if asKeyValues is true, then conn must be JdbcConnection
     */
    protected static ResultSet search(Connection conn, String text, int limit, int offset, boolean asKeyValues) 
    throws SQLException {
        SimpleResultSet result = newSearchResultSet(asKeyValues);
        if (conn.getMetaData().getURL().startsWith("jdbc:columnlist:")) {
            // this is just to query the result set columns
            return result;
        }
        if (text == null || text.trim().length() == 0) {
            return result;
        }
        
        FullTextSearchContext context = FullTextSearchContext.getContext(conn);
        Set<String> inputWords = New.hashSet();
        context.splitIntoWords(text, inputWords);

        Map<String, Integer> indexedWords = context.getWordList();

        // Select the rows that contain all the words
        Set<Integer> rowIds = null, lastRowIds = null;	
        PreparedStatement prepSelectMapByWordId = context.prepare(conn, SELECT_MAP_BY_WORD_ID);
        for (String word : inputWords) {
            lastRowIds = rowIds;
            rowIds = New.hashSet();
            
            // Word ID
            Integer wordId = indexedWords.get(word);
            if (wordId == null) continue;
            
            // MAP: Word ID -> ROW IDs
            prepSelectMapByWordId.setInt(1, wordId.intValue());
            ResultSet maps = prepSelectMapByWordId.executeQuery();
            while (maps.next()) {
                Integer rowId = maps.getInt(1);
                if (lastRowIds == null || lastRowIds.contains(rowId)) {		// add a row that contains all the previous words
                    rowIds.add(rowId);
                }
            }
        }
        if (rowIds == null || rowIds.size() == 0) {
            return result;
        }
        
        // Get each of the rows by key values or condition SQL
        PreparedStatement prepSelectRowById = context.prepare(conn, SELECT_ROW_BY_ID);
        int rowCount = 0;
        for (int rowId : rowIds) {
            prepSelectRowById.setInt(1, rowId);
            ResultSet rs = prepSelectRowById.executeQuery();
            if (!rs.next()) continue;	// Missing the row corresponding to the ID in MAP
            
            if (offset > 0) {	// Skip rows until during the offset 
                offset--;
            } 
            else {
                String conditionSql = rs.getString(1);
                int tableInfoId = rs.getInt(2);
                IndexedTableInfo tableInfo = context.getIndexedTableInfo(tableInfoId);
                if (asKeyValues) {
                	// NOTE: the conn must be JdbcConnection
                    Object[][] columnAndValue = 
                    	parseConditionSqlToColumnsAndValues(conditionSql, (JdbcConnection)conn);
                    result.addRow(
                    	tableInfo.schema,
                    	tableInfo.table,
                        columnAndValue[0],
                        columnAndValue[1],
                        1.0);
                } 
                else {
                    String query = StringUtils.quoteIdentifier(tableInfo.schema) +
                        "." + StringUtils.quoteIdentifier(tableInfo.table) +
                        " WHERE " + conditionSql;
                    result.addRow(query, 1.0);
                }
                rowCount++;
                if (limit > 0 && rowCount >= limit) {
                    break;
                }
            }
        }
        return result;
    }
    
    protected static final String FIELD_SCORE = "SCORE";
    // A column name of the result set returned by the searchData method.
    protected static final String FIELD_SCHEMA = "SCHEMA";
    protected static final String FIELD_TABLE = "TABLE";
    protected static final String FIELD_COLUMNS = "COLUMNS";		// column names for keys
    protected static final String FIELD_KEYS = "KEYS";					// key values
    // The column name of the result set returned by the search method.
    private static final String FIELD_QUERY = "QUERY";
    
    protected static SimpleResultSet newSearchResultSet(boolean asKeyValues) {
        SimpleResultSet result = new SimpleResultSet();
        if (asKeyValues) {
            result.addColumn(FIELD_SCHEMA, Types.VARCHAR, 0, 0);
            result.addColumn(FIELD_TABLE, Types.VARCHAR, 0, 0);
            result.addColumn(FIELD_COLUMNS, Types.ARRAY, 0, 0);
            result.addColumn(FIELD_KEYS, Types.ARRAY, 0, 0);
        } 
        else {
            result.addColumn(FIELD_QUERY, Types.VARCHAR, 0, 0);
        }
        result.addColumn(FIELD_SCORE, Types.FLOAT, 0, 0);
        return result;
    }
}
