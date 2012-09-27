package marubinotto.piggydb.impl.query;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;

import marubinotto.piggydb.impl.QueryUtils;
import marubinotto.piggydb.model.Filter;
import marubinotto.piggydb.model.Fragment;
import marubinotto.piggydb.model.TagRepository;
import marubinotto.piggydb.model.query.FragmentsByFilter;
import marubinotto.util.Assert;
import marubinotto.util.paging.Page;
import marubinotto.util.paging.PageImpl;
import marubinotto.util.paging.PageUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jdbc.core.RowMapper;

public class H2FragmentsByFilter 
extends H2FragmentsQueryBase implements FragmentsByFilter {
	
	private static Log logger = LogFactory.getLog(H2FragmentsByFilter.class);

	private Filter filter;
	
	public void setFilter(Filter filter) {
		this.filter = filter;
	}
	
	protected void appendFromWhere(StringBuilder sql, List<Object> args) throws Exception {
		// Do nothing
	}
	
	public List<Fragment> getAll() throws Exception {
		return getRepository().getByIds(getFilteredIds(), getSortOption(), isEagerFetching());
	}
	
	public Page<Fragment> getPage(int pageSize, int pageIndex) throws Exception {
		List<Long> filteredIds = getFilteredIds();	
		if (filteredIds.isEmpty()) return PageUtils.empty(pageSize);
		
		// Get ONLY the fragments in the page, which is why the IDs needs to be sorted
		Page<Long> pagedIds = PageUtils.getPage(filteredIds, pageSize, pageIndex);
		return new PageImpl<Fragment>(
			getRepository().getByIds(pagedIds, getSortOption(), isEagerFetching()), 
			pagedIds.getPageSize(), 
			pagedIds.getPageIndex(), 
			filteredIds.size());
	}
	
	@SuppressWarnings("unchecked")
	private List<Long> getFilteredIds() throws Exception {
		Assert.Property.requireNotNull(filter, "filter");
		
		StringBuilder sql  = new StringBuilder();
		
		TagRepository tagRepository = getRepository().getTagRepository();
        
		// Classifications
		List<Set<Long>> expandedTags = filter.getClassification().expandEach(tagRepository);
		if (expandedTags.size() > 0) {
			for (Set<Long> tagTree : expandedTags) {
				if (sql.length() > 0) sql.append(" intersect ");
				appendSelectIdsByTagTree(sql, tagTree);
			}
		}
		else {
			sql.append("select ");
			appendFieldsForIdAndSort(sql);
			sql.append(" from fragment as f");
		}
       
		// Excludes
		Set<Long> excludes = filter.getExcludes().expandAll(tagRepository);
		if (excludes.size() > 0) {
			sql.append(" minus ");
			appendSelectIdsByTagTree(sql, excludes);
		}
		
		// Order
		appendSortOption(sql, "f.");

		logger.debug("selectIdsByFilter: " + sql);
		return getJdbcTemplate().query(sql.toString(), new RowMapper() {
			public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
				return rs.getLong(1);
			}
		});
	}
	
	private void appendFieldsForIdAndSort(StringBuilder sql) {
		// Fragment ID
		sql.append("f.fragment_id");

		// Column for Sort
		if (getSortOption().orderBy.isString()) 
			sql.append(", " + normalizedStringColumnForSort(getSortOption().orderBy.getName(), "f."));
		else
			sql.append(", f." + getSortOption().orderBy.getName());
	}

	private void appendSelectIdsByTagTree(StringBuilder sql, Set<Long> tagTree) {		
		sql.append("select distinct ");
		appendFieldsForIdAndSort(sql);
    sql.append(" from fragment as f, tagging as t");
    sql.append(" where f.fragment_id = t.target_id");
    sql.append(" and t.target_type = " + QueryUtils.TAGGING_TARGET_FRAGMENT);
		sql.append(" and t.tag_id in (");
		boolean first = true;
		for (Long tagId : tagTree) {
			if (first) first = false; else sql.append(", ");
			sql.append(tagId);
		}
		sql.append(")");
	}
}
