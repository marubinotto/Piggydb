package marubinotto.piggydb.impl.query;

import static marubinotto.util.CollectionUtils.joinToString;
import static org.apache.commons.lang.ObjectUtils.defaultIfNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import marubinotto.piggydb.impl.H2FragmentRepository;
import marubinotto.piggydb.impl.QueryUtils;
import marubinotto.piggydb.impl.mapper.FragmentRelationRowMapper;
import marubinotto.piggydb.impl.mapper.FragmentRowMapper;
import marubinotto.piggydb.model.Fragment;
import marubinotto.piggydb.model.FragmentList;
import marubinotto.piggydb.model.FragmentRelation;
import marubinotto.piggydb.model.base.Repository;
import marubinotto.piggydb.model.entity.RawFragment;
import marubinotto.piggydb.model.exception.NoSuchEntityException;
import marubinotto.piggydb.model.query.FragmentsQuery;
import marubinotto.piggydb.model.query.FragmentsSortOption;
import marubinotto.util.Assert;
import marubinotto.util.CollectionUtils;
import marubinotto.util.paging.Page;
import marubinotto.util.paging.PageUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public abstract class H2FragmentsQueryBase implements FragmentsQuery {
	
	private static Log logger = LogFactory.getLog(H2FragmentsQueryBase.class);
	
	protected H2FragmentRepository repository;
	
	protected FragmentsSortOption sortOption = new FragmentsSortOption();
	protected boolean eagerFetching = false;

	public void setRepository(Repository<Fragment> repository) {
		Assert.Arg.notNull(repository, "repository");
		this.repository = (H2FragmentRepository)repository;
	}
	
	public H2FragmentRepository getRepository() {
		return this.repository;
	}

	public FragmentRowMapper getRowMapper() {
		return this.repository.getFragmentRowMapper();
	}
	
	public void setSortOption(FragmentsSortOption sortOption) {
		Assert.Arg.notNull(sortOption, "sortOption");
		this.sortOption = sortOption;
	}
	
	public void setEagerFetching(boolean eagerFetching) {
		this.eagerFetching = eagerFetching;
	}
	
	private void eagerFetch(List<RawFragment> fragments) throws Exception {
		if (this.eagerFetching) {
			this.repository.refreshClassifications(fragments);
			setParentsAndChildrenWithGrandchildrenToEach(fragments);
		}
	}
	
	protected abstract StringBuilder buildSql() throws Exception;
	
	protected abstract PageUtils.TotalCounter getTotalCounter();
	
	public final List<Fragment> getAll() throws Exception {
		StringBuilder sql = buildSql();
		appendSortOption(sql, this.repository.getFragmentRowMapper().getColumnPrefix());
		
		List<RawFragment> results = this.repository.query(sql.toString());
		eagerFetch(results);
		return CollectionUtils.<Fragment>covariantCast(results);
	}
	
	public final Page<Fragment> getPage(int pageSize, int pageIndex) throws Exception {
		StringBuilder sql = buildSql();
		appendSortOption(sql, this.repository.getFragmentRowMapper().getColumnPrefix());
		QueryUtils.appendLimit(sql, pageSize, pageIndex);
		
		List<RawFragment> results = this.repository.query(sql.toString());
		eagerFetch(results);
		
		return PageUtils.<Fragment>covariantCast(
			PageUtils.toPage(results, pageSize, pageIndex, getTotalCounter()));
	}
	
	protected void appendSelectAll(StringBuilder sql) {
		sql.append("select ");
		sql.append(getRowMapper().selectAll());
		if (sortOption != null && sortOption.orderBy.isString()) {
			sql.append(", ");
			sql.append(
				normalizedStringColumnForSort(
					sortOption.orderBy.getName(), 
					getRowMapper().getColumnPrefix()));
		}
	}
	
	private void appendSortOption(StringBuilder sql, String columnPrefix) {
		if (this.sortOption == null) return;
		
		sql.append(" order by ");

		if (this.sortOption.orderBy.isString())
			sql.append("ns_" + this.sortOption.orderBy.getName());
		else
			sql.append(defaultIfNull(columnPrefix, "") + this.sortOption.orderBy.getName());
		
		if (this.sortOption.ascending)
			sql.append(" nulls last");
		else
			sql.append(" desc nulls first");
	}
	
	private static String normalizedStringColumnForSort(String columnName, String prefix) {
		return "UPPER(" + prefix + columnName + ") as ns_" + columnName;
	}
	
	protected void appendConditionToExcludeTrash(StringBuilder sql, String columnNameForId) 
	throws Exception {
		List<Long> trashIds = this.repository.selectIdsClassifiedAsTrash();
		if (trashIds.isEmpty()) return;
		
		sql.append(" and ");
		sql.append(columnNameForId);
		sql.append(" not in (");
		boolean first = true;
		for (Long fragmentId : trashIds) {
			if (first) first = false; else sql.append(", ");
			sql.append(fragmentId);
		}
		sql.append(")");
	}
	
	private void setParentsAndChildrenWithGrandchildrenToEach(List<RawFragment> fragments) 
	throws Exception {
		if (fragments.isEmpty()) return;
		
		FragmentList<RawFragment> fragments2 = new FragmentList<RawFragment>(fragments);
		setParentsToEach(fragments2);
		setChildrenToEach(fragments2);
		setChildrenToEach(fragments2.getChildren());
		for (RawFragment fragment : fragments2)
			fragment.checkTwoWayRelations();
	}
	
	private void setParentsToEach(FragmentList<RawFragment> fragments) throws Exception {
		Assert.Arg.notNull(fragments, "fragments");
		
		if (fragments.isEmpty()) return;
		
		// get & set
		Map<Long, List<FragmentRelation>> id2parents = getParentsForEach(fragments.ids());
		for (Long id : id2parents.keySet()) {
			fragments.get(id).setParentRelations(id2parents.get(id));
		}
		
		// set to the duplicates
		for (RawFragment duplication : fragments.getDuplicates()) {
			duplication.setParentRelations(
				fragments.get(duplication.getId()).getParentRelations());
		}
	}
	
	private void setChildrenToEach(FragmentList<RawFragment> fragments) throws Exception {
		Assert.Arg.notNull(fragments, "fragments");
		
		if (fragments.isEmpty()) return;
		
		// get & set (without sorting)
		Map<Long, List<FragmentRelation>> id2children = getChildrenForEach(fragments.ids());
		for (Long id : id2children.keySet()) {
			fragments.get(id).setChildRelations(id2children.get(id));
		}
		
		// set to the duplicates
		for (RawFragment duplication : fragments.getDuplicates()) {
			duplication.setChildRelations(
				fragments.get(duplication.getId()).getChildRelations());
		}
	}

	private Map<Long, List<FragmentRelation>> getParentsForEach(Set<Long> fragmentIds)
	throws NoSuchEntityException, Exception {
		Map<Long, List<FragmentRelation>> results = 
			new HashMap<Long, List<FragmentRelation>>();
		if (fragmentIds.isEmpty()) return results;
		
		FragmentRelationRowMapper parentMapper = 
			new FragmentRelationRowMapper(
				this.repository.relationFactory, 
				"fragment_relation.", 
				getRowMapper(), 
				null, 
				results);
		
		StringBuilder sql = new StringBuilder();
		sql.append("select ");
		sql.append(parentMapper.selectAll());
		sql.append(", " + getRowMapper().selectAll());
		sql.append(" from fragment_relation, fragment");
		sql.append(" where fragment_relation.from_id = fragment.fragment_id");
		sql.append(" and fragment_relation.to_id in (");
		sql.append(joinToString(fragmentIds, ", "));
		sql.append(")");
		appendConditionToExcludeTrash(sql, "fragment_relation.from_id");

		if (logger.isDebugEnabled()) logger.debug("getParentsForEach: " + fragmentIds);
		this.repository.getJdbcTemplate().query(sql.toString(), parentMapper);

		return results;
	}

	private Map<Long, List<FragmentRelation>> getChildrenForEach(Set<Long> fragmentIds)
	throws Exception {
		Map<Long, List<FragmentRelation>> results = new HashMap<Long, List<FragmentRelation>>();
		if (fragmentIds.isEmpty()) return results;
		
		FragmentRelationRowMapper childMapper =
			new FragmentRelationRowMapper(
				this.repository.relationFactory, 
				"fragment_relation.", 
				null, 
				getRowMapper(), 
				results);
		
		StringBuilder sql = new StringBuilder();
		sql.append("select ");
		sql.append(childMapper.selectAll());
		sql.append(", " + getRowMapper().selectAll());
		sql.append(" from fragment_relation, fragment");
		sql.append(" where fragment_relation.to_id = fragment.fragment_id");
		sql.append(" and fragment_relation.from_id in (");
		sql.append(joinToString(fragmentIds, ", "));
		sql.append(")");
		appendConditionToExcludeTrash(sql, "fragment_relation.to_id");
		
		sql.append(" order by fragment_relation.priority desc nulls last");
		sql.append(", fragment_relation.fragment_relation_id");
		
		if (logger.isDebugEnabled()) logger.debug("getChildrenForEach: " + fragmentIds);
		this.repository.getJdbcTemplate().query(sql.toString(), childMapper);
		
		return results;
	}
}
