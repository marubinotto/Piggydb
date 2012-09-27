package marubinotto.piggydb.impl.query;

import static marubinotto.piggydb.impl.QueryUtils.appendLimit;
import static org.apache.commons.lang.ObjectUtils.defaultIfNull;

import java.util.ArrayList;
import java.util.List;

import marubinotto.piggydb.impl.H2FragmentRepository;
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

import org.springframework.jdbc.core.JdbcTemplate;

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
	
	public FragmentsSortOption getSortOption() {
		return this.sortOption;
	}
	
	// -----

	private boolean eagerFetching = false;
	
	public void setEagerFetching(boolean eagerFetching) {
		this.eagerFetching = eagerFetching;
	}
	
	public boolean isEagerFetching() {
		return this.eagerFetching;
	}

	private void eagerFetch(List<RawFragment> fragments) throws Exception {
		if (this.eagerFetching) {
			getRepository().refreshClassifications(fragments);
			getRepository().setParentsAndChildrenWithGrandchildrenToEach(fragments);
		}
	}
	
	// -----
	
	private StringBuilder sql;
	private List<Object> sqlArgs;
	private String fromWhere;
	
	private void buildSelectFromWhere() throws Exception {
		this.sql = new StringBuilder();
		this.sqlArgs = new ArrayList<Object>();
		
		appendSelect(this.sql);
		
		StringBuilder fromWhere = new StringBuilder();
		appendFromWhere(fromWhere, this.sqlArgs);
		this.fromWhere = fromWhere.toString();
		this.sql.append(" " + this.fromWhere);
	}
	
	public List<Fragment> getAll() throws Exception {
		buildSelectFromWhere();
		appendSortOption(this.sql, getRowMapper().getColumnPrefix());
		
		List<RawFragment> results = getRepository().query(this.sql.toString(), this.sqlArgs.toArray());
		
		eagerFetch(results);
		
		return CollectionUtils.<Fragment>covariantCast(results);
	}
	
	public Page<Fragment> getPage(int pageSize, int pageIndex) throws Exception {
		buildSelectFromWhere();
		appendSortOption(this.sql, getRowMapper().getColumnPrefix());
		appendLimit(this.sql, pageSize, pageIndex);
		
		List<RawFragment> results = getRepository().query(this.sql.toString(), this.sqlArgs.toArray());
		
		eagerFetch(results);
		
		return PageUtils.<Fragment>covariantCast(
			PageUtils.toPage(results, pageSize, pageIndex, getTotalCounter()));
	}

	protected void appendSelect(StringBuilder sql) {
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
	
	protected abstract void appendFromWhere(StringBuilder sql, List<Object> args) 
	throws Exception;
	
	protected void appendSortOption(StringBuilder sql, String columnPrefix) {
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
	
	protected static String normalizedStringColumnForSort(String columnName, String prefix) {
		return "UPPER(" + prefix + columnName + ") as ns_" + columnName;
	}
	
	protected PageUtils.TotalCounter getTotalCounter() {
		final String countSql = "select count(*) " + this.fromWhere;
		final Object[] args = this.sqlArgs.toArray();
		return new PageUtils.TotalCounter() {
			public long getTotalSize() throws Exception {
				return (Long)getJdbcTemplate().queryForObject(countSql, args, Long.class);
			}
		};
	}
}
