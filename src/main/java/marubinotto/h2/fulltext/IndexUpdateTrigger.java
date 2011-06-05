package marubinotto.h2.fulltext;

import static marubinotto.h2.fulltext.FullTextSearch.SCHEMA;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.h2.api.Trigger;
import org.h2.util.New;

public class IndexUpdateTrigger implements Trigger {
	
	protected FullTextSearchContext context;
	protected IndexedTableInfo tableInfo;
	
	private PreparedStatement 
		prepInsertWord, prepInsertRow, prepInsertMap,
		prepDeleteRow, prepDeleteMap,
		prepSelectRow;

	public void init(
		Connection conn, 
		String schemaName, 
		String triggerName,
        String tableName, 
        boolean before, 
        int type) 
	throws SQLException {
		this.context = FullTextSearchContext.getContext(conn);
		this.tableInfo = IndexedTableInfo.newInstance(conn, schemaName, tableName);
		this.context.addIndexedTableInfo(this.tableInfo);
        
		this.prepInsertWord = conn.prepareStatement(
        	"INSERT INTO " + SCHEMA + ".WORDS(NAME) VALUES(?)");
		this.prepInsertRow = conn.prepareStatement(
        	"INSERT INTO " + SCHEMA + ".ROWS(HASH, INDEXID, KEY) VALUES(?, ?, ?)");
		this.prepInsertMap = conn.prepareStatement(
        	"INSERT INTO " + SCHEMA + ".MAP(ROWID, WORDID) VALUES(?, ?)");
		this.prepDeleteRow = conn.prepareStatement(
        	"DELETE FROM " + SCHEMA + ".ROWS WHERE HASH=? AND INDEXID=? AND KEY=?");
		this.prepDeleteMap = conn.prepareStatement(
        	"DELETE FROM " + SCHEMA + ".MAP WHERE ROWID=? AND WORDID=?");
		this.prepSelectRow = conn.prepareStatement(
        	"SELECT ID FROM " + SCHEMA + ".ROWS WHERE HASH=? AND INDEXID=? AND KEY=?");	
	}

	public void fire(Connection conn, Object[] oldRow, Object[] newRow)
	throws SQLException {
		if (oldRow != null) {
            if (newRow != null) {
                // update
                if (this.tableInfo.haveIndexedColumnsChanged(oldRow, newRow)) {
                    delete(oldRow);
                    insert(newRow);
                }
            } else {
                // delete
                delete(oldRow);
            }
        } else if (newRow != null) {
            // insert
            insert(newRow);
        }
	}
	
	/**
	 * Entries will be added to WORDS, ROWS, MAP
	 */
	protected void insert(Object[] row) throws SQLException {
		// Add an entry to ROWS
		String condition = this.tableInfo.createConditionSqlWithKeys(row);
        int hash = condition.hashCode();
        this.prepInsertRow.setInt(1, hash);
        this.prepInsertRow.setInt(2, this.tableInfo.id);
        this.prepInsertRow.setString(3, condition);
        this.prepInsertRow.execute();
        
        ResultSet rs = this.prepInsertRow.getGeneratedKeys();
        rs.next();
        int rowId = rs.getInt(1);
        
        // Add entries to MAP
        this.prepInsertMap.setInt(1, rowId);
        Set<Integer> wordIds = getWordIds(row);
        for (int id : wordIds) {
        	this.prepInsertMap.setInt(2, id);
        	this.prepInsertMap.execute();
        }
	}
	
	/**
	 * Entries will be deleted from MAP, ROWS
	 *  - WORDS will remain unchanged
	 */
	protected void delete(Object[] row) throws SQLException {
		String condition = this.tableInfo.createConditionSqlWithKeys(row);
        int hash = condition.hashCode();
        this.prepSelectRow.setInt(1, hash);
        this.prepSelectRow.setInt(2, this.tableInfo.id);
        this.prepSelectRow.setString(3, condition);
        ResultSet rs = this.prepSelectRow.executeQuery();
        if (rs.next()) {
            int rowId = rs.getInt(1);
            this.prepDeleteMap.setInt(1, rowId);
            Set<Integer> wordIds = getWordIds(row);
            for (int id : wordIds) {
            	this.prepDeleteMap.setInt(2, id);
            	this.prepDeleteMap.executeUpdate();
            }
            this.prepDeleteRow.setInt(1, hash);
            this.prepDeleteRow.setInt(2, this.tableInfo.id);
            this.prepDeleteRow.setString(3, condition);
            this.prepDeleteRow.executeUpdate();
        }
	}
	
	public void close() throws SQLException {
		this.context.removeIndexedTableInfo(this.tableInfo);
	}

	public void remove() throws SQLException {
		this.context.removeIndexedTableInfo(this.tableInfo);
	}
	
	public Set<Integer> getWordIds(Object[] row) throws SQLException {
		Set<String> words = New.hashSet();
		this.context.splitIntoWords(this.tableInfo, row, words);
		
		Map<String, Integer> allWords = this.context.getWordList();
		Set<Integer> wordIds = new TreeSet<Integer>();
		for (String word : words) {
			Integer wordId = allWords.get(word);
			if (wordId == null) {		// New word
				this.prepInsertWord.setString(1, word);
				this.prepInsertWord.execute();
                ResultSet rs = this.prepInsertWord.getGeneratedKeys();
                rs.next();
                wordId = rs.getInt(1);
                allWords.put(word, wordId);
			}
			wordIds.add(wordId);
		}
		return wordIds;
	}
}
