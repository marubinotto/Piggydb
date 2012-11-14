package marubinotto.piggydb.ui.page.partial;

import static org.apache.commons.lang.StringUtils.isBlank;

import java.util.HashMap;
import java.util.Map;

import marubinotto.piggydb.model.query.FragmentsAllButTrash;
import marubinotto.piggydb.model.query.FragmentsQuery;

public class FragmentsByBuiltinFilter extends AbstractFragments {
	
	public static Map<String, Class<? extends FragmentsQuery>> queryTypes = 
		new HashMap<String, Class<? extends FragmentsQuery>>();
	
	static {
		queryTypes.put("all", FragmentsAllButTrash.class);
	}

	public String name;
	
	@Override 
	protected void setFragments() throws Exception {
		if (isBlank(this.name)) return;
		
		Class<? extends FragmentsQuery> queryType = queryTypes.get(this.name);
		if (queryType == null) return;
		
		FragmentsQuery query = (FragmentsQuery)
			getDomain().getFragmentRepository().getQuery(queryType);
		this.fragments = getPage(query);
		
		this.label = getMessage(this.name);
	}
}
