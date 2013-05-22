package marubinotto.piggydb.model.query;

import marubinotto.piggydb.model.Filter;
import marubinotto.piggydb.model.RelatedTags;

public interface FragmentsByFilter extends FragmentsQuery {

	public void setFilter(Filter filter);
	
	public void setKeywords(String keywords);
	
	public RelatedTags getRelatedTags() throws Exception;
}
