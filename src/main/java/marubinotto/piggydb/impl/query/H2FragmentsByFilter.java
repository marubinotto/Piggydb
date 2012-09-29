package marubinotto.piggydb.impl.query;

import static marubinotto.util.CollectionUtils.joinToString;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;

import marubinotto.piggydb.impl.QueryUtils;
import marubinotto.piggydb.model.Filter;
import marubinotto.piggydb.model.Fragment;
import marubinotto.piggydb.model.RelatedTags;
import marubinotto.piggydb.model.TagRepository;
import marubinotto.piggydb.model.query.FragmentsByFilter;
import marubinotto.util.Assert;
import marubinotto.util.paging.Page;
import marubinotto.util.paging.PageImpl;
import marubinotto.util.paging.PageUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jdbc.core.RowMapper;

public final class H2FragmentsByFilter 
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
		return getRepository().getByIds(getFilteredIds(true), getSortOption(), isEagerFetching());
	}
	
	public Page<Fragment> getPage(int pageSize, int pageIndex) throws Exception {
		List<Long> filteredIds = getFilteredIds(true);	
		if (filteredIds.isEmpty()) return PageUtils.empty(pageSize);
		
		// Get ONLY the fragments in the page, which is why the IDs needs to be sorted
		Page<Long> pagedIds = PageUtils.getPage(filteredIds, pageSize, pageIndex);
		return new PageImpl<Fragment>(
			getRepository().getByIds(pagedIds, getSortOption(), isEagerFetching()), 
			pagedIds.getPageSize(), 
			pagedIds.getPageIndex(), 
			filteredIds.size());
	}
	
	public RelatedTags getRelatedTags() throws Exception {
		Assert.Property.requireNotNull(filter, "filter");
		
		RelatedTags relatedTags = new RelatedTags();
		relatedTags.setFilter(this.filter);
		
		List<Long> filteredIds = getFilteredIds(false);
		if (filteredIds.isEmpty()) return relatedTags;
		
		List<Page<Long>> pages = 
			PageUtils.splitToPages(filteredIds, COLLECT_RELATED_TAGS_AT_ONCE);
		for (Page<Long> ids : pages) collectRelatedTags(ids, relatedTags);
		
		return relatedTags;
	}
	
	private static final int COLLECT_RELATED_TAGS_AT_ONCE = 1000;
	
	private void collectRelatedTags(List<Long> fragmentIds, final RelatedTags relatedTags) {
		if (fragmentIds.isEmpty()) return;

		StringBuilder sql = new StringBuilder();
		sql.append("select tag_id, count(tag_id) from tagging");
		sql.append(" where target_type = " + QueryUtils.TAGGING_TARGET_FRAGMENT);
		sql.append(" and target_id in (");
		for (int i = 0; i < fragmentIds.size(); i++) {
			if (i > 0) sql.append(", ");
			sql.append(fragmentIds.get(i));
		}
		sql.append(")");
		sql.append(" group by tag_id");

		logger.debug("collectRelatedTags: " + sql.toString());
		getJdbcTemplate().query(sql.toString(), new RowMapper() {
			public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
				relatedTags.add(rs.getLong(1), rs.getInt(2));
				return null;
			}
		});
	}
	
	@SuppressWarnings("unchecked")
	public List<Long> getFilteredIds(boolean sort) throws Exception {
		Assert.Property.requireNotNull(filter, "filter");
		
		StringBuilder sql  = new StringBuilder();
		
		TagRepository tagRepository = getRepository().getTagRepository();
        
		// Classifications
		List<Set<Long>> expandedTags = this.filter.getClassification().expandEach(tagRepository);
		if (expandedTags.size() > 0) {
			for (Set<Long> tagTree : expandedTags) {
				if (sql.length() > 0) sql.append(" intersect ");
				appendSqlToSelectFragmentIdsTaggedWithAnyOf(sql, tagTree, sort);
			}
		}
		else {
			sql.append("select ");
			appendFieldsForIdAndSort(sql, sort);
			sql.append(" from fragment as f");
		}
       
		// Excludes
		Set<Long> excludes = this.filter.getExcludes().expandAll(tagRepository);
		if (excludes.size() > 0) {
			sql.append(" minus ");
			appendSqlToSelectFragmentIdsTaggedWithAnyOf(sql, excludes, sort);
		}
		
		// Order
		if (sort) appendSortOption(sql, "f.");

		logger.debug("getFilteredIds: " + sql);
		return getJdbcTemplate().query(sql.toString(), new RowMapper() {
			public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
				return rs.getLong(1);
			}
		});
	}
	
	private void appendFieldsForIdAndSort(StringBuilder sql, boolean sort) {
		// Fragment ID
		sql.append("f.fragment_id");

		// Column for Sort
		if (sort) {
			if (getSortOption().orderBy.isString()) 
				sql.append(", " + ignoreCaseForSort(getSortOption().orderBy.getName(), "f."));
			else
				sql.append(", f." + getSortOption().orderBy.getName());
		}
	}

	private void appendSqlToSelectFragmentIdsTaggedWithAnyOf(
		StringBuilder sql, Set<Long> tagIds, boolean sort) {
		
		sql.append("select distinct ");
		appendFieldsForIdAndSort(sql, sort);
    sql.append(" from fragment as f, tagging as t");
    sql.append(" where f.fragment_id = t.target_id");
    sql.append(" and t.target_type = " + QueryUtils.TAGGING_TARGET_FRAGMENT);
		sql.append(" and t.tag_id in (");
		sql.append(joinToString(tagIds, ", "));
		sql.append(")");
	}
}
