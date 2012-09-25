package marubinotto.piggydb.impl.query;

import static org.apache.commons.lang.ObjectUtils.defaultIfNull;

import java.util.ArrayList;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;

import marubinotto.piggydb.impl.H2FragmentRepository;
import marubinotto.piggydb.impl.QueryUtils;
import marubinotto.piggydb.impl.mapper.FragmentRowMapper;
import marubinotto.piggydb.model.Fragment;
import marubinotto.piggydb.model.base.Repository;
import marubinotto.piggydb.model.entity.RawFragment;
import marubinotto.piggydb.model.query.FragmentsQuery;
import marubinotto.piggydb.model.query.FragmentsSortOption;
import marubinotto.util.Assert;
import marubinotto.util.CollectionUtils;
import marubinotto.util.paging.Page;
import marubinotto.util.paging.PageUtils;

public abstract class H2FragmentsQueryBase implements FragmentsQuery {
	
	private H2FragmentRepository repository;
	
	public void setRepository(Repository<Fragment> repository) {
		Assert.Arg.notNull(repository, "repository");
		this.repository = (H2FragmentRepository)repository;
	}
	
	public H2FragmentRepository getRepository() {
		return this.repository;
	}
	
	public JdbcTemplate getJdbcTemplate() {
		return getRepository().getJdbcTemplate();
	}

	public FragmentRowMapper getRowMapper() {
		return getRepository().getFragmentRowMapper();
	}
	
	// -----
	
	private FragmentsSortOption sortOption = new FragmentsSortOption();
	
	public void setSortOption(FragmentsSortOption sortOption) {
		Assert.Arg.notNull(sortOption, "sortOption");
		this.sortOption = sortOption;
	}
	
	// -----
	
	private boolean eagerFetching = false;
	
	public void setEagerFetching(boolean eagerFetching) {
		this.eagerFetching = eagerFetching;
	}
	
	private void eagerFetch(List<RawFragment> fragments) throws Exception {
		if (this.eagerFetching) {
			getRepository().refreshClassifications(fragments);
			getRepository().setParentsAndChildrenWithGrandchildrenToEach(fragments);
		}
	}
	
	// -----
	
	public final List<Fragment> getAll() throws Exception {
		StringBuilder sql = new StringBuilder();
		List<Object> args = new ArrayList<Object>();
		
		// select - from - where
		buildSelectFromWhereSql(sql, args);
		
		// order by
		appendSortOption(sql, getRowMapper().getColumnPrefix());
		
		// execute
		List<RawFragment> results = getRepository().query(sql.toString(), args.toArray());
		
		eagerFetch(results);
		
		return CollectionUtils.<Fragment>covariantCast(results);
	}
	
	public final Page<Fragment> getPage(int pageSize, int pageIndex) throws Exception {
		StringBuilder sql = new StringBuilder();
		List<Object> args = new ArrayList<Object>();
		
		// select - from - where
		buildSelectFromWhereSql(sql, args);
		String selectFromWhere = sql.toString();
		Object[] argsArray = args.toArray();
		
		// order by - limit
		appendSortOption(sql, getRowMapper().getColumnPrefix());
		QueryUtils.appendLimit(sql, pageSize, pageIndex);
		
		// execute
		List<RawFragment> results = getRepository().query(sql.toString(), argsArray);
		
		eagerFetch(results);
		
		return PageUtils.<Fragment>covariantCast(
			PageUtils.toPage(results, pageSize, pageIndex, getTotalCounter(selectFromWhere, argsArray)));
	}
	
	protected abstract void buildSelectFromWhereSql(StringBuilder sql, List<Object> args) 
	throws Exception;
	
	protected PageUtils.TotalCounter getTotalCounter(String sql, final Object[] args) {
		final String countSql = "select count(*)" + sql.substring(sql.indexOf(" from "));
		return new PageUtils.TotalCounter() {
			public long getTotalSize() throws Exception {
				return (Long)getJdbcTemplate().queryForObject(countSql, args, Long.class);
			}
		};
	}

	// -----
	
	protected void appendSelectAll(StringBuilder sql) {
		sql.append("select ");
		sql.append(getRowMapper().selectAll());
		if (this.sortOption != null && this.sortOption.orderBy.isString()) {
			sql.append(", ");
			sql.append(
				normalizedStringColumnForSort(
					this.sortOption.orderBy.getName(), 
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
}
