package marubinotto.h2.fulltext;

import static marubinotto.h2.fulltext.InternalUtils.quoteSQL;
import static marubinotto.h2.fulltext.InternalUtils.throwException;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import marubinotto.util.Assert;

import org.h2.util.JdbcUtils;
import org.h2.util.New;
import org.h2.util.StatementBuilder;
import org.h2.util.StringUtils;

/**
 * To be created at Trigger.init
 */
public class IndexedTableInfo {	

    /**
     * The index id.  
     * (from FT_INDEXES.ID)
     */
	public Integer id;

    /**
     * The schema name. 
     * (from Trigger.init arg)
     */
	public String schema;

    /**
     * The table name. 
     * (from Trigger.init arg)
     */
	public String table;

    /**
     * The column names. 
     * (from DatabaseMetaData.getColumns["COLUMN_NAME"])
     */
	public List<String> columns;
	
	/**
	 * from DatabaseMetaData.getColumns["DATA_TYPE"]
	 */
	public List<Integer> columnTypes;

    /**
     * The column indexes of the key columns.
     * (from DatabaseMetaData.getPrimaryKeys["COLUMN_NAME"])
     */
	public List<Integer> keys;

    /**
     * The column indexes of the index columns.
     * (from FT_INDEXES.COLUMNS)
     */
	public List<Integer> indexColumns;
	
	
	/**
	 * - the specified table must be indexed in "FT.INDEXES"
	 */
	public static IndexedTableInfo newInstance(
		Connection conn, 
		String schemaName, 
        String tableName) 
	throws SQLException {
		IndexedTableInfo info = new IndexedTableInfo();
        info.schema = schemaName;
        info.table = tableName;
		
		// column names and types
        DatabaseMetaData meta = conn.getMetaData();
        ResultSet rs = meta.getColumns(null,
            JdbcUtils.escapeMetaDataPattern(schemaName),
            JdbcUtils.escapeMetaDataPattern(tableName),
            null);
        info.columns = New.arrayList();
        info.columnTypes = New.arrayList();
        while (rs.next()) {
        	info.columns.add(rs.getString("COLUMN_NAME"));
        	info.columnTypes.add(rs.getInt("DATA_TYPE"));
        }
        
        // primary keys
        List<String> keyNames = New.arrayList();
        rs = meta.getPrimaryKeys(null,
            JdbcUtils.escapeMetaDataPattern(schemaName),
            tableName);
	    while (rs.next()) {
	    	keyNames.add(rs.getString("COLUMN_NAME"));
	    }
	    if (keyNames.isEmpty()) {
            throw throwException("No primary key for table " + tableName);
        }
	    info.keys = info.toColumnIndexes(keyNames);
	    
	    // indexed columns
	    List<String> indexNames = New.arrayList();
        PreparedStatement prep = conn.prepareStatement(
        	"SELECT ID, COLUMNS FROM " + FullTextSearch.SCHEMA + ".INDEXES WHERE SCHEMA=? AND TABLE=?");
        prep.setString(1, schemaName);
        prep.setString(2, tableName);
        rs = prep.executeQuery();
        if (rs.next()) {
        	info.id = rs.getInt(1);
            String columns = rs.getString(2);
            if (columns != null) {
                for (String s : StringUtils.arraySplit(columns, ',', true)) {
                	indexNames.add(s);
                }
            }
        }
        if (info.id == null) throw throwException("Not to be indexed: " + tableName);
        if (indexNames.size() == 0) indexNames.addAll(info.columns);	// all
        info.indexColumns = info.toColumnIndexes(indexNames);
        
        return info;
	}
	
	private List<Integer> toColumnIndexes(List<String> names) throws SQLException {
		Assert.Property.requireNotNull(columns, "columns");
		
		List<Integer> indexes = New.arrayList();
		for (String name : names) {
			int index = this.columns.indexOf(name);
			if (index < 0) throw throwException("Column not found: " + name);
			indexes.add(index);
		}
		return indexes;
	}
	
    /**
     * Check if a the indexed columns of a row probably have changed. It may
     * return true even if the change was minimal (for example from 0.0 to 0.00).
     */
    public boolean haveIndexedColumnsChanged(Object[] oldRow, Object[] newRow) {
        for (int columnIndex : this.indexColumns) {
            Object o = oldRow[columnIndex], n = newRow[columnIndex];
            if (o == null) {
                if (n != null) return true;
            } 
            else if (!o.equals(n)) {
                return true;
            }
        }
        return false;
    }
    
    public String createConditionSqlWithKeys(Object[] row) throws SQLException {
		StatementBuilder buff = new StatementBuilder();
        for (int columnIndex : this.keys) {
            buff.appendExceptFirst(" AND ");
            buff.append(StringUtils.quoteIdentifier(this.columns.get(columnIndex)));
            Object value = row[columnIndex];
            if (value == null) {
                buff.append(" IS NULL");
            } 
            else {
                buff.append('=').append(quoteSQL(value, this.columnTypes.get(columnIndex)));
            }
        }
        return buff.toString();
	}
}
