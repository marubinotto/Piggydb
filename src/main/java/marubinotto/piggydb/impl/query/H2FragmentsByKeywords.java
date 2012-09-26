package marubinotto.piggydb.impl.query;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import marubinotto.piggydb.model.Fragment;
import marubinotto.piggydb.model.query.FragmentsByKeywords;
import marubinotto.util.Assert;
import marubinotto.util.paging.Page;
import marubinotto.util.paging.PageUtils;

public class H2FragmentsByKeywords 
extends H2FragmentsQueryBase implements FragmentsByKeywords {

	private String keywords;
	
	public void setKeywords(String keywords) {
		this.keywords = keywords;
	}
	
	protected void appendFromWhere(StringBuilder sql, List<Object> args) 
	throws Exception {
		Assert.Property.requireNotNull(keywords, "keywords");

		sql.append("from FT_SEARCH_DATA(?, 0, 0) ft, fragment");
		sql.append(" where ft.TABLE ='FRAGMENT' and fragment.fragment_id = ft.KEYS[0]");
		
		args.add(this.keywords);
	}
	
	public List<Fragment> getAll() throws Exception {
		if (StringUtils.isBlank(this.keywords)) return new ArrayList<Fragment>();		
		return super.getAll();
	}
	
	public Page<Fragment> getPage(int pageSize, int pageIndex) throws Exception {
		if (StringUtils.isBlank(keywords)) return PageUtils.empty(pageSize);
		return super.getPage(pageSize, pageIndex);
	}
}
