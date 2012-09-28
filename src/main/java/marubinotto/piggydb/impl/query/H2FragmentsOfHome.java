package marubinotto.piggydb.impl.query;

import java.util.ArrayList;
import java.util.List;

import marubinotto.piggydb.model.Filter;
import marubinotto.piggydb.model.Fragment;
import marubinotto.piggydb.model.FragmentList;
import marubinotto.piggydb.model.Tag;
import marubinotto.piggydb.model.entity.RawFilter;
import marubinotto.piggydb.model.entity.RawFragment;
import marubinotto.piggydb.model.query.FragmentsByFilter;
import marubinotto.piggydb.model.query.FragmentsOfHome;
import marubinotto.piggydb.model.query.FragmentsQuery;
import marubinotto.util.paging.Page;
import marubinotto.util.paging.PageUtils;

public class H2FragmentsOfHome 
extends H2FragmentsQueryBase implements FragmentsOfHome {
	
	public H2FragmentsOfHome() {
		setEagerFetching(true);
	}
	
	protected void appendFromWhere(StringBuilder sql, List<Object> args) throws Exception {
		// Do nothing
	}
	
	public List<Fragment> getAll() throws Exception {
		FragmentsQuery query = getQueryForHomeTag();
		if (query == null) return new ArrayList<Fragment>();
		
		List<Fragment> fragments = query.getAll();
		setAdditionalDependencies(fragments);
		return fragments;
	}
	
	public Page<Fragment> getPage(int pageSize, int pageIndex) throws Exception {
		FragmentsQuery query = getQueryForHomeTag();
		if (query == null) return PageUtils.empty(pageSize);
		
		Page<Fragment> fragments = query.getPage(pageSize, pageIndex);
		setAdditionalDependencies(fragments);
		return fragments;
	}
	
	private void setAdditionalDependencies(List<Fragment> fragments) throws Exception {
		if (fragments.isEmpty()) return;
		
		FragmentList<RawFragment> children = 
			FragmentList.<RawFragment>createByDownCast(fragments).getChildren();
		getRepository().refreshClassifications(children.getFragments());
		getRepository().setParentsToEach(children);
		for (RawFragment child : children) child.checkTwoWayRelations();
	}

	private FragmentsQuery getQueryForHomeTag() throws Exception {
		FragmentsByFilter query = (FragmentsByFilter)
			getRepository().getQuery(FragmentsByFilter.class);
		
		Filter filter = createFilter();
		if (filter == null) return null;
		
		query.setFilter(filter);
		query.setSortOption(getSortOption());
		query.setEagerFetching(isEagerFetching());
		
		return query;
	}
	
	private Filter createFilter() throws Exception {
		RawFilter filter = new RawFilter();

		Tag homeTag = getRepository().getTagRepository().getByName(Tag.NAME_HOME);
		if (homeTag == null) return null;
		filter.getClassification().addTag(homeTag);

		Tag trashTag = getRepository().getTagRepository().getTrashTag();
		if (trashTag != null) filter.getExcludes().addTag(trashTag);
		
		return filter;
	}
}
