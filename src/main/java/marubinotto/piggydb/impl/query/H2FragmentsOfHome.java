package marubinotto.piggydb.impl.query;

import marubinotto.piggydb.model.Filter;
import marubinotto.piggydb.model.Tag;
import marubinotto.piggydb.model.entity.RawFilter;
import marubinotto.piggydb.model.query.FragmentsOfHome;

public class H2FragmentsOfHome 
extends H2FragmentsByPredefinedFilterBase implements FragmentsOfHome {
	
	public H2FragmentsOfHome() {
		setEagerFetching(true);
		setEagerFetchingMore(true);
	}
	
	protected Filter createFilter() throws Exception {
		RawFilter filter = new RawFilter();

		Tag homeTag = getRepository().getTagRepository().getByName(Tag.NAME_HOME);
		if (homeTag == null) return null;
		filter.getClassification().addTag(homeTag);

		Tag trashTag = getRepository().getTagRepository().getTrashTag();
		if (trashTag != null) filter.getExcludes().addTag(trashTag);
		
		return filter;
	}
}
