package marubinotto.h2.fulltext;

import static marubinotto.h2.fulltext.InternalUtils.*;

import java.io.IOException;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import marubinotto.util.Assert;

import org.apache.commons.lang.UnhandledException;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.cjk.CJKAnalyzer;
import org.h2.util.New;
import org.h2.util.SoftHashMap;

public class FullTextSearchContext {
	
	private static final Map<String, FullTextSearchContext> CONTEXTS = New.hashMap();
	
	public static FullTextSearchContext getContext(Connection conn) throws SQLException {
        String path = getIndexPath(conn);
        FullTextSearchContext context = CONTEXTS.get(path);
        if (context == null) {
        	context = new FullTextSearchContext();
            CONTEXTS.put(path, context);
        }
        return context;
    }
    	
	private HashMap<String, Integer> words = New.hashMap();
	private HashMap<Integer, IndexedTableInfo> indexedTables = New.hashMap();

	private FullTextSearchContext() {
	}
	
	public void clearAll() {
		this.words.clear();
		this.indexedTables.clear();
	}

    public Map<String, Integer> getWordList() {
        return this.words;
    }

    public IndexedTableInfo getIndexedTableInfo(int infoId) {
        return this.indexedTables.get(infoId);
    }

    public void addIndexedTableInfo(IndexedTableInfo info) {
    	Assert.Arg.notNull(info, "info");
    	Assert.Arg.notNull(info.id, "info.id");
        this.indexedTables.put(info.id, info);
    }

    public void removeIndexedTableInfo(IndexedTableInfo info) {
    	Assert.Arg.notNull(info, "info");
    	Assert.Arg.notNull(info.id, "info.id");
    	this.indexedTables.remove(info.id);
    }
    
    public String convertWord(String word) {
        word = word.toUpperCase();
        return word;
    }
    
    private final static Analyzer ANALYZER = new CJKAnalyzer();

    public void splitIntoWords(String text, Set<String> words) {
    	TokenStream stream = ANALYZER.tokenStream("F", new StringReader(text));
    	Token token = new Token();
        try {
			while ((token = stream.next(token)) != null) {
				String word = token.term();
				word = convertWord(word);
	            if (word != null) words.add(word);
			}
		} 
        catch (IOException e) {
			throw new UnhandledException(e);
		}
    }
    
    public void splitIntoWords(IndexedTableInfo tableInfo, Object[] row, Set<String> words) 
    throws SQLException {
    	for (int columnIndex : tableInfo.indexColumns) {
    		Object data = row[columnIndex];
            int type = tableInfo.columnTypes.get(columnIndex);
            // NOTE: omitted the case: type == Types.CLOB for large clob
            String string = InternalUtils.toString(data, type);
            splitIntoWords(string, words);
        }
    }
    
    protected SoftHashMap<Connection, SoftHashMap<String, PreparedStatement>> cache = 
    	new SoftHashMap<Connection, SoftHashMap<String, PreparedStatement>>();

    protected synchronized PreparedStatement prepare(Connection conn, String sql) throws SQLException {
        SoftHashMap<String, PreparedStatement> preps = cache.get(conn);
        if (preps == null) {
            preps = new SoftHashMap<String, PreparedStatement>();
            this.cache.put(conn, preps);
        }
        PreparedStatement prep = preps.get(sql);
        if (prep != null && prep.getConnection().isClosed()) {
            prep = null;
        }
        if (prep == null) {
            prep = conn.prepareStatement(sql);	// what if the connection isClosed?
            preps.put(sql, prep);
        }
        return prep;
    }
}
