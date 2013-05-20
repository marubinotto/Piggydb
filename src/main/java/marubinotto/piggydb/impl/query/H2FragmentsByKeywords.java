package marubinotto.piggydb.impl.query;

import java.util.List;

import marubinotto.piggydb.model.query.FragmentsByKeywords;

import org.apache.commons.lang.StringUtils;

public class H2FragmentsByKeywords 
extends H2FragmentsQueryBase implements FragmentsByKeywords {

	private String keywords;
	
	public void setKeywords(String keywords) {
		this.keywords = keywords;
	}
	
	protected void appendFromWhere(StringBuilder sql, List<Object> args) throws Exception {
	  appendFromWhereForKeywordSearch(sql, args, this.keywords);
	}
	
	public static void appendFromWhereForKeywordSearch(
	  StringBuilder sql, 
	  List<Object> args, 
	  String keywords) 
	throws Exception {
	  sql.append("from fragment");
    if (StringUtils.isNotBlank(keywords)) {
      sql.append(", FT_SEARCH_DATA(?, 0, 0) ft");
      sql.append(" where ft.TABLE ='FRAGMENT' and fragment.fragment_id = ft.KEYS[0]");
      args.add(keywords);
    }
	}
}
