package marubinotto.piggydb.impl.query;

import static org.apache.commons.lang.StringUtils.isNotBlank;

import java.util.List;

import marubinotto.piggydb.model.query.FragmentsByKeywords;

public class H2FragmentsByKeywords 
extends H2FragmentsQueryBase implements FragmentsByKeywords {

	private String keywords;
	
	public void setKeywords(String keywords) {
		this.keywords = keywords;
	}
	
	protected void appendFromWhere(StringBuilder sql, List<Object> args) throws Exception {
	  sql.append("from fragment");
    if (isNotBlank(this.keywords)) {
      sql.append(", FT_SEARCH_DATA(?, 0, 0) ft");
      sql.append(" where ft.TABLE ='FRAGMENT' and fragment.fragment_id = ft.KEYS[0]");
      args.add(keywords);
    }
	}
}
