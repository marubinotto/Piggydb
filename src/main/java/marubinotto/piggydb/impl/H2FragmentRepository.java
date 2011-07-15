package marubinotto.piggydb.impl;

import static marubinotto.util.CollectionUtils.list;
import static org.apache.commons.lang.ObjectUtils.defaultIfNull;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import marubinotto.piggydb.impl.mapper.FragmentRelationRowMapper;
import marubinotto.piggydb.impl.mapper.FragmentRowMapper;
import marubinotto.piggydb.model.Filter;
import marubinotto.piggydb.model.Fragment;
import marubinotto.piggydb.model.FragmentRelation;
import marubinotto.piggydb.model.FragmentRepository;
import marubinotto.piggydb.model.FragmentsOptions;
import marubinotto.piggydb.model.FragmentsOptions.SortOption;
import marubinotto.piggydb.model.OwnerAuth;
import marubinotto.piggydb.model.RelatedTags;
import marubinotto.piggydb.model.Tag;
import marubinotto.piggydb.model.User;
import marubinotto.piggydb.model.entity.RawClassifiable;
import marubinotto.piggydb.model.entity.RawEntityFactory;
import marubinotto.piggydb.model.entity.RawFilter;
import marubinotto.piggydb.model.entity.RawFragment;
import marubinotto.piggydb.model.enums.FragmentField;
import marubinotto.piggydb.model.exception.BaseDataObsoleteException;
import marubinotto.piggydb.model.exception.DuplicateException;
import marubinotto.piggydb.model.exception.NoSuchEntityException;
import marubinotto.util.Assert;
import marubinotto.util.CollectionUtils;
import marubinotto.util.paging.Page;
import marubinotto.util.paging.PageImpl;
import marubinotto.util.paging.PageUtils;
import marubinotto.util.time.Interval;
import marubinotto.util.time.Month;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.incrementer.DataFieldMaxValueIncrementer;

public class H2FragmentRepository extends FragmentRepository.Base 
implements RawEntityFactory<RawFragment> {

	private static Log logger = LogFactory.getLog(H2FragmentRepository.class);
	
	private H2TagRepository tagRepository;
	
	protected JdbcTemplate jdbcTemplate;
	private DataFieldMaxValueIncrementer fragmentIdIncrementer;
	private DataFieldMaxValueIncrementer relationIdIncrementer;
	
	private FragmentRowMapper fragmentRowMapper = new FragmentRowMapper(this, "fragment.");
	
	public H2FragmentRepository() {
	}
	
	public RawEntityFactory<FragmentRelation> relationFactory = 
		new RawEntityFactory<FragmentRelation>() {
			public FragmentRelation newRawEntity() {
				return new FragmentRelation();
			}
		};

	public void setTagRepository(H2TagRepository tagRepository) {
		this.tagRepository = tagRepository;
	}

	public H2TagRepository getTagRepository() {
		return this.tagRepository;
	}

	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public JdbcTemplate getJdbcTemplate() {
		return jdbcTemplate;
	}

	public void setFragmentIdIncrementer(
		DataFieldMaxValueIncrementer fragmentIdIncrementer) {
		this.fragmentIdIncrementer = fragmentIdIncrementer;
	}

	public void setRelationIdIncrementer(
		DataFieldMaxValueIncrementer relationIdIncrementer) {
		this.relationIdIncrementer = relationIdIncrementer;
	}

	public long register(Fragment fragment) throws Exception {
		Assert.Arg.notNull(fragment, "fragment");
		Assert.require(fragment instanceof RawFragment, "fragment instanceof RawFragment");
		Assert.require(fragment.getId() == null, "fragment.getId() == null");	
		Assert.Property.requireNotNull(fragmentIdIncrementer, "fragmentIdIncrementer");
		Assert.Property.requireNotNull(fileRepository, "fileRepository");		
		
		((RawFragment)fragment).setId(new Long(this.fragmentIdIncrementer.nextLongValue()));	
		FragmentRowMapper.insert((RawFragment)fragment, this.jdbcTemplate);
		QueryUtils.registerTaggings(
			fragment, 
			QueryUtils.TAGGING_TARGET_FRAGMENT, 
			this.jdbcTemplate, 
			this.tagRepository);
		
		// To avoid registering only a record (transaction should be rolled back when IO error)
		// To avoid registering only a file (should register a file after the db query succeeds)
		if (fragment.isFile()) {
			this.fileRepository.putFile(fragment);
		}
		
		return fragment.getId();
	}
	
	public long size() throws Exception {
		return (Long)this.jdbcTemplate.queryForObject(
			"select count(*) from fragment", Long.class);
	}

	public Fragment get(long id, boolean fetchingRelations) throws Exception {
		logger.debug("get: " + id);
		
		// entity
		RawFragment fragment = queryForOneFragment(
			"select " + this.fragmentRowMapper.selectAll() + " from fragment where fragment_id = ?",
			new Object[]{new Long(id)});
		if (fragment == null) return null;
		
		// classification
		refreshClassifications(list(fragment));
		
		// relationships
		if (fetchingRelations) {
			setParentsTo(fragment);
			Map<Long, RawFragment> id2child = setChildrenWithTagsTo(fragment);
			setParentsAndChildrenWithGrandchildrenToEach(id2child);
		}

		return fragment;
	}

	private RawFragment queryForOneFragment(String sql, Object[] args) {
		try {
			return (RawFragment) this.jdbcTemplate.queryForObject(
				sql, args, this.fragmentRowMapper);
		}
		catch (EmptyResultDataAccessException e) {
			return null;
		}
	}

	public boolean update(Fragment fragment, boolean updateTimestamp) 
	throws BaseDataObsoleteException, Exception {
		Assert.Arg.notNull(fragment, "fragment");
		Assert.require(fragment instanceof RawFragment, "fragment instanceof RawFragment");
		Assert.Arg.notNull(fragment.getId(), "fragment.getId()");
		Assert.Property.requireNotNull(fileRepository, "fileRepository");
		
		// Check preconditions
		if (!containsId(fragment.getId())) {
			logger.info("[update] No such fragment ID: " + fragment.getId()); 
			return false;
		}
		if (fragment.getUpdateDatetime() == null) {
			throw new BaseDataObsoleteException();
		}
		
		// Do update
		FragmentRowMapper.update((RawFragment)fragment, updateTimestamp, this.jdbcTemplate);
		QueryUtils.updateTaggings(
			(RawFragment)fragment, 
			QueryUtils.TAGGING_TARGET_FRAGMENT,
			this.jdbcTemplate,
			this.tagRepository);
		
		// To avoid registering only a record (transaction should be rolled back when IO error)
		// To avoid registering only a file (should register a file after the db query succeeds)
		if (fragment.isFile() && fragment.getFileInput() != null) {
			this.fileRepository.putFile(fragment);
		}
		
		return true;
	}
	
	private boolean containsId(Long id) throws Exception {
		return this.jdbcTemplate.queryForInt(
	    "select count(*) from fragment where fragment_id = ?", 
	    new Object[]{id}) > 0;
	}
	
	@Override
	protected void doDelete(Fragment fragment, User user) throws Exception {
		// Delete related taggings
		this.jdbcTemplate.update(
	    "delete from tagging where target_id = ? and target_type = ?", 
	    new Object[]{
	    	fragment.getId(),
	    	QueryUtils.TAGGING_TARGET_FRAGMENT
	    });
		
		// Delete relations
		this.jdbcTemplate.update(
      "delete from fragment_relation where from_id = ? or to_id = ?", 
      new Object[]{
      	fragment.getId(),
      	fragment.getId()
      });
		
		// Delete the fragment 
		this.jdbcTemplate.update(
      "delete from fragment where fragment_id = ?", 
      new Object[]{fragment.getId()});
		
		// To avoid deleting only a record (transaction should be rolled back when IO error)
		// To avoid deleting only a file (should delete a file after the db query succeeds)
		if (fragment.isFile()) {
			this.fileRepository.deleteFile(fragment);
		}
	}
	
	public void refreshClassifications(List<? extends Fragment> fragments) throws Exception {
		Assert.Arg.notNull(fragments, "fragments");
		
		if (fragments.isEmpty()) return;
		
		// list -> map & duplications, clear all classifications
		Map<Long, RawClassifiable> id2fragment = new HashMap<Long, RawClassifiable>();
		List<RawFragment> duplications = new ArrayList<RawFragment>();
		for (Fragment fragment : fragments) {
			RawFragment fragmentImpl = (RawFragment)fragment;
			fragmentImpl.getMutableClassification().clear();
			if (id2fragment.containsKey(fragment.getId())) 
				duplications.add(fragmentImpl);
			else 
				id2fragment.put(fragment.getId(), fragmentImpl);
		}
		
		// get classifications
		QueryUtils.setTagsRecursively(
			id2fragment,  
			QueryUtils.TAGGING_TARGET_FRAGMENT, 
			this.jdbcTemplate, 
			getTagRepository());
		logger.debug("Classifications refreshed: " + id2fragment.keySet());
		
		for (RawFragment duplication : duplications) {
			duplication.getMutableClassification().syncWith(
				id2fragment.get(duplication.getId()).getClassification());
		}
	}

	public void deleteTrashes(User user) throws Exception {
		for (Long trashId : selectIdsClassifiedAsTrash()) delete(trashId, user);
	}

	@SuppressWarnings("unchecked")
	public Set<Integer> getDaysOfMonth(FragmentField field, Month month)
		throws Exception {
		Assert.Arg.notNull(field, "field");
		Assert.Arg.notNull(month, "month");

		StringBuilder sql = new StringBuilder();
		sql.append("select distinct extract(DAY from " + field.getName() + ")");
		sql.append(" from fragment");
		sql.append(" where extract(YEAR from " + field.getName() + ") = ?");
		sql.append(" and extract(MONTH from " + field.getName() + ") = ?");
		appendConditionToExcludeTrash(sql, "fragment.fragment_id");

		return new HashSet<Integer>(
			this.jdbcTemplate.queryForList(sql.toString(), new Object[]{
				new Integer(month.getYear()), new Integer(month.getMonth())},
				Integer.class));
	}

	@SuppressWarnings("unchecked")
	public Page<Fragment> getFragments(FragmentsOptions options) throws Exception {
		Assert.Arg.notNull(options, "options");
		
		StringBuilder sql = new StringBuilder();
		appendSelectAll(sql, this.fragmentRowMapper, options.sortOption);
		sql.append(" from fragment where 0 = 0");
		appendConditionToExcludeTrash(sql, "fragment.fragment_id");
		appendOptions(sql, options, this.fragmentRowMapper.getColumnPrefix());
        
		List<RawFragment> results = 
			this.jdbcTemplate.query(sql.toString(), this.fragmentRowMapper);
		
		if (options.eagerFetching) {
			refreshClassifications(results);
			setParentsAndChildrenWithGrandchildrenToEach(id2fragment(results));
		}
		
		return PageUtils.<Fragment>covariantCast(
			PageUtils.toPage(results, options.pageSize, options.pageIndex, 
				new PageUtils.TotalCounter() {
					public long getTotalSize() throws Exception {
						return size();
					}
				}));
	}

	@SuppressWarnings("unchecked")
	public Page<Fragment> findByTime(
		Interval interval, 
		FragmentField field,
		FragmentsOptions options) 
	throws Exception {
		Assert.Arg.notNull(interval, "interval");
		Assert.Arg.notNull(field, "field");
		Assert.Arg.notNull(options, "options");

		StringBuilder sql = new StringBuilder();
		appendSelectAll(sql, this.fragmentRowMapper, options.sortOption);

		StringBuilder condition = new StringBuilder();
		condition.append(" from fragment where");
		condition.append(" (" + field.getName() + " between ? and ?)");
		appendConditionToExcludeTrash(condition, "fragment.fragment_id");

		sql.append(condition);
		appendOptions(sql, options, this.fragmentRowMapper.getColumnPrefix());

		final Object[] params = new Object[] { 
			interval.getStartInstant().toDate(),
			interval.getEndInstant().toDate() 
		};
		List<RawFragment> results = this.jdbcTemplate.query(sql.toString(), params,
				this.fragmentRowMapper);

		if (options.eagerFetching) {
			refreshClassifications(results);
			setParentsAndChildrenWithGrandchildrenToEach(id2fragment(results));
		}

		final String queryAll = "select count(*)" + condition;
		return PageUtils.<Fragment> covariantCast(PageUtils.toPage(results,
			options.pageSize, options.pageIndex, new PageUtils.TotalCounter() {
				public long getTotalSize() throws Exception {
					return (Long) getJdbcTemplate().queryForObject(queryAll, params,
						Long.class);
				}
			}));
	}

	public Page<Fragment> findByFilter(Filter filter, FragmentsOptions options) 
	throws Exception {
		Assert.Arg.notNull(filter, "filter");
		Assert.Arg.notNull(options, "options");

		// Get all IDs by the filter, sorted by the option
		List<Long> selectedIds = getIdsByFilter(filter, options.sortOption);
		if (selectedIds.isEmpty()) return PageUtils.empty(options.pageSize);

		// Get ONLY the fragments in the page, which is why the IDs needs to be sorted
		Page<Long> pagedIds = PageUtils.getPage(selectedIds, options.pageSize, options.pageIndex);
		return new PageImpl<Fragment>(
			getByIds(pagedIds, options.sortOption, options.eagerFetching), 
			pagedIds.getPageSize(), 
			pagedIds.getPageIndex(), 
			selectedIds.size());
	}
	
	@SuppressWarnings("unchecked")
	private List<Long> getIdsByFilter(Filter filter, SortOption sortOption) throws Exception {
		Assert.Arg.notNull(filter, "filter");
		
		StringBuilder sql  = new StringBuilder();
        
		// Classifications
		List<Set<Long>> expandedTags = filter.getClassification().expandEach(this.tagRepository);
		if (expandedTags.size() > 0) {
			for (Set<Long> tagTree : expandedTags) {
				if (sql.length() > 0) sql.append(" intersect ");
				appendSelectIdsByTagTree(sql, tagTree, sortOption);
			}
		}
		else {
			sql.append("select ");
			appendFieldsForIdAndSort(sql, sortOption);
			sql.append(" from fragment as f");
		}
       
		// Excludes
		Set<Long> excludes = filter.getExcludes().expandAll(this.tagRepository);
		if (excludes.size() > 0) {
			sql.append(" minus ");
			appendSelectIdsByTagTree(sql, excludes, sortOption);
		}
		
		// Order
		appendSortOption(sql, sortOption, "f.");

		logger.debug("selectIdsByFilter: " + sql);
		return this.jdbcTemplate.query(sql.toString(), new RowMapper() {
			public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
				return rs.getLong(1);
			}
		});
	}
	
	private static void appendFieldsForIdAndSort(StringBuilder sql, SortOption sortOption) {
		// Fragment ID
		sql.append("f.fragment_id");

		// Column for Sort
		if (sortOption != null) {
			if (sortOption.orderBy.isString()) 
				sql.append(", " + normalizedStringColumnForSort(sortOption.orderBy.getName(), "f."));
			else
				sql.append(", f." + sortOption.orderBy.getName());
		}
	}
	
	private static String normalizedStringColumnForSort(String columnName, String prefix) {
		return "UPPER(" + prefix + columnName + ") as ns_" + columnName;
	}

	private static void appendSelectIdsByTagTree(
		StringBuilder sql, 
		Set<Long> tagTree, 
		SortOption sortOption) {
		
		sql.append("select distinct ");
		appendFieldsForIdAndSort(sql, sortOption);
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
	
	public RelatedTags getRelatedTags(Filter filter) throws Exception {
		Assert.Arg.notNull(filter, "filter");
		
		RelatedTags relatedTags = new RelatedTags();
		relatedTags.setFilter(filter);
		
		List<Long> selectedIds = getIdsByFilter(filter, null);
		if (selectedIds.isEmpty()) return relatedTags;

		List<Page<Long>> pages = 
			PageUtils.splitToPages(selectedIds, COLLECT_RELATED_TAGS_AT_ONCE);
		for (Page<Long> ids : pages) collectRelatedTags(ids, relatedTags);
		
		return relatedTags;
	}
	
	private static final int COLLECT_RELATED_TAGS_AT_ONCE = 1000;
	
	private void collectRelatedTags(List<Long> fragmentIds,
		final RelatedTags relatedTags) {
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
		this.jdbcTemplate.query(sql.toString(), new RowMapper() {
			public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
				relatedTags.add(rs.getLong(1), rs.getInt(2));
				return null;
			}
		});
	}

	@SuppressWarnings("unchecked")
	public Page<Fragment> findByKeywords(final String keywords, FragmentsOptions options)
	throws Exception {
		if (StringUtils.isBlank(keywords)) return PageUtils.empty(options.pageSize);
		
		StringBuilder sql  = new StringBuilder();
		appendSelectAll(sql, this.fragmentRowMapper, options.sortOption);
		
		StringBuilder condition = new StringBuilder();
		condition.append(" from FT_SEARCH_DATA(?, 0, 0) ft, fragment");
		condition.append(" where ft.TABLE ='FRAGMENT' and fragment.fragment_id = ft.KEYS[0]");
		
		sql.append(condition);
		appendOptions(sql, options, this.fragmentRowMapper.getColumnPrefix());
        
		List<RawFragment> results = this.jdbcTemplate.query(
			sql.toString(), new Object[]{keywords}, this.fragmentRowMapper);
		
		if (options.eagerFetching) {
			refreshClassifications(results);
			setParentsAndChildrenWithGrandchildrenToEach(id2fragment(results));
		}

		final String queryAll = "select count(*)" + condition;
		return PageUtils.<Fragment>covariantCast(
			PageUtils.toPage(results, options.pageSize, options.pageIndex, 
				new PageUtils.TotalCounter() {
					public long getTotalSize() throws Exception {
						return (Long)getJdbcTemplate().queryForObject(
							queryAll, new Object[]{keywords}, Long.class);
					}
				}));
	}
	
	@SuppressWarnings("unchecked")
	public Page<Fragment> findByUser(
		final String userName,
		FragmentsOptions options) 
	throws Exception {
		Assert.Arg.notNull(userName, "userName");
		Assert.Arg.notNull(options, "options");

		StringBuilder sql = new StringBuilder();
		appendSelectAll(sql, this.fragmentRowMapper, options.sortOption);

		StringBuilder condition = new StringBuilder();
		condition.append(" from fragment");
		condition.append(" where (creator = ? or updater = ?");
		if (userName.equals(OwnerAuth.USER_NAME_OWNER)) {
			condition.append(" or creator is null");
			condition.append(" or (creation_datetime <> update_datetime and updater is null)");
		}
		condition.append(")");
		appendConditionToExcludeTrash(condition, "fragment.fragment_id");

		sql.append(condition);
		appendOptions(sql, options, this.fragmentRowMapper.getColumnPrefix());

		List<RawFragment> results = this.jdbcTemplate.query(
			sql.toString(), new Object[]{userName, userName}, this.fragmentRowMapper);

		if (options.eagerFetching) {
			refreshClassifications(results);
			setParentsAndChildrenWithGrandchildrenToEach(id2fragment(results));
		}

		final String queryAll = "select count(*)" + condition;
		return PageUtils.<Fragment> covariantCast(PageUtils.toPage(results,
			options.pageSize, options.pageIndex, new PageUtils.TotalCounter() {
				public long getTotalSize() throws Exception {
					return (Long) getJdbcTemplate().queryForObject(queryAll,
							new Object[]{userName, userName}, Long.class);
				}
			}));
	}
	
	private static void appendSelectAll(
		StringBuilder sql, 
		FragmentRowMapper mapper, 
		SortOption sortOption) {
		
		sql.append("select ");
		sql.append(mapper.selectAll());
		if (sortOption != null && sortOption.orderBy.isString()) {
			sql.append(", ");
			sql.append(normalizedStringColumnForSort(
				sortOption.orderBy.getName(), 
				mapper.getColumnPrefix()));
		}
	}

	@SuppressWarnings("unchecked")
	public List<Fragment> getByIds(
		Collection<Long> fragmentIds, 
		SortOption sortOption, 
		boolean eagerFetching) 
	throws Exception {
		StringBuilder sql  = new StringBuilder();
		
		// select
		appendSelectAll(sql, this.fragmentRowMapper, sortOption);
		
		// from
		sql.append(" from fragment");
		
		// where
		sql.append(" where fragment_id in (");
		boolean first = true;
		for (Long fragmentId : fragmentIds) {
			if (first) first = false; else sql.append(", ");
			sql.append(fragmentId);
		}
		sql.append(")");
		
		// order by
		appendSortOption(sql, sortOption, this.fragmentRowMapper.getColumnPrefix());
		
		List<RawFragment> results = 
			this.jdbcTemplate.query(sql.toString(), this.fragmentRowMapper);
		
		if (eagerFetching) {
			refreshClassifications(results);
			setParentsAndChildrenWithGrandchildrenToEach(id2fragment(results));
		}
		
		return CollectionUtils.<Fragment>covariantCast(results);
	}
	
	public Map<Long, String> getNames(Set<Long> ids) throws Exception {
		Assert.Arg.notNull(ids, "ids");
		return QueryUtils.getValuesForIds("fragment", "title", ids, this.jdbcTemplate);
	}
	
	@SuppressWarnings("unchecked")
	public Fragment getUserFragment(String userName) throws Exception {
		Assert.Arg.notNull(userName, "userName");
		
		Tag userTag = getTagRepository().getByName(Tag.NAME_USER);
		if (userTag == null) return null;

		StringBuilder sql  = new StringBuilder();
		appendSelectIdsByTagTree(sql, userTag.expandToIdsOfSubtree(getTagRepository()), null);
		sql.append(" and f.title = ?");
		
		Tag trashTag = this.tagRepository.getTrashTag();
		if (trashTag != null) {
			sql.append(" minus ");
			appendSelectIdsByTagTree(sql, trashTag.expandToIdsOfSubtree(getTagRepository()), null);
		}

		List<Long> ids = (List<Long>)this.jdbcTemplate.query(
			sql.toString(), new Object[]{userName}, new RowMapper() {
				public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
					return rs.getLong(1);
				}
			});
		if (ids.isEmpty()) return null;
		
		List<Fragment> fragments = getByIds(ids, SortOption.getDefault(), false);
		return fragments.isEmpty() ? null : fragments.get(0);
	}

	@Override
	protected long doCreateRelation(long from, long to, User user)
	throws NoSuchEntityException, DuplicateException, Exception {
		Assert.Property.requireNotNull(relationIdIncrementer, "relationIdIncrementer");
		
		if (!containsId(from)) {
			throw new NoSuchEntityException(from);
		}
		if (!containsId(to)) {
			throw new NoSuchEntityException(to);
		}
		
		FragmentRelation newRelation = new FragmentRelation(user);
		newRelation.setId(this.relationIdIncrementer.nextLongValue());
		
		FragmentRelationRowMapper.insert(newRelation, from, to, this.jdbcTemplate);
		
        return newRelation.getId();
	}
	
	public FragmentRelation getRelation(long relationId) throws Exception {
		try {
			return (FragmentRelation) this.jdbcTemplate.queryForObject(
				"select * from fragment_relation where fragment_relation_id = ?",
				new Object[] { new Long(relationId) }, new FragmentRelationRowMapper(
						this.relationFactory, this));
		}
		catch (EmptyResultDataAccessException e) {
			return null;
		}
	}
	
	@Override
	protected void doDeleteRelation(long relationId) throws Exception {
		this.jdbcTemplate.update(
	    "delete from fragment_relation where fragment_relation_id = ?", 
	    new Object[]{ new Long(relationId) });
	}
	
	public Long countRelations() throws Exception {
		return (Long)this.jdbcTemplate.queryForObject(
			"select count(*) from fragment_relation", Long.class);
	}

	@Override
	protected void doUpdateChildRelationPriorities(List<Long> relationOrder) throws Exception {
		int count = relationOrder.size();
		for (Long relationId : relationOrder) {
			this.jdbcTemplate.update(
				"update fragment_relation set priority = " + (count--) + " where fragment_relation_id = ?", 
				new Object[]{ relationId });
		}
	}
	
	
// Utilities
	
	private Map<Long, RawFragment> id2fragment(List<RawFragment> fragments) {
		Map<Long, RawFragment> id2fragment = new HashMap<Long, RawFragment>();
		for (RawFragment fragment : fragments) id2fragment.put(fragment.getId(), fragment);
		return id2fragment;
	}

	private List<Long> selectIdsClassifiedAsTrash() throws Exception {
		logger.debug("selectIdsClassifiedAsTrash ...");
		
		Tag trashTag = this.tagRepository.getTrashTag();
		if (trashTag == null) return new ArrayList<Long>();
		
		RawFilter filter = new RawFilter();
		filter.getClassification().addTag(trashTag);
		return getIdsByFilter(filter, null);
	}
	
	private static void appendOptions(StringBuilder sql, FragmentsOptions options, String columnPrefix) {
		appendSortOption(sql, options.sortOption, columnPrefix);
		QueryUtils.appendLimit(sql, options.pageSize, options.pageIndex);
	}
	
	private static void appendSortOption(StringBuilder sql, SortOption sortOption, String columnPrefix) {
		if (sortOption == null) return;
		
		sql.append(" order by ");

		if (sortOption.orderBy.isString())
			sql.append("ns_" + sortOption.orderBy.getName());
		else
			sql.append(defaultIfNull(columnPrefix, "") + sortOption.orderBy.getName());
		
		if (sortOption.ascending)
			sql.append(" nulls last");
		else
			sql.append(" desc nulls first");
	}

	private void appendConditionToExcludeTrash(StringBuilder sql, String columnNameForId) 
	throws Exception {
		List<Long> trashIds = selectIdsClassifiedAsTrash();
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
	
	
// Resolve relations
	
	private void setParentsTo(RawFragment fragment) throws Exception {
		List<FragmentRelation> parents = 
			getParentsForEach(list(fragment.getId())).get(fragment.getId());
		if (parents == null) return;		
		fragment.setParentRelations(parents);
	}
	
	private Map<Long, RawFragment> setChildrenWithTagsTo(RawFragment fragment) 
	throws Exception {
		Map<Long, RawFragment> id2child = new HashMap<Long, RawFragment>();
		
		// Get the sorted children of the given fragment
		List<FragmentRelation> children = 
			getChildrenForEach(list(fragment.getId())).get(fragment.getId());
		if (children == null) return id2child;
		
		// Fetch tags for the children
		List<Fragment> childFragments = new ArrayList<Fragment>();
		for (FragmentRelation relation : children) {
			RawFragment child = (RawFragment)relation.to;
			childFragments.add(child);
			id2child.put(child.getId(), child);
		}
		refreshClassifications(childFragments);
		
		fragment.setChildRelations(children);
		return id2child;
	}
	
	private void setParentsAndChildrenWithGrandchildrenToEach(
		Map<Long, RawFragment> id2fragment) 
	throws Exception {
		if (id2fragment.isEmpty()) return;
		
		List<Long> ids = new ArrayList<Long>(id2fragment.keySet());
		
		// Parents
		Map<Long, List<FragmentRelation>> id2parents = getParentsForEach(ids);
		for (Long id : id2parents.keySet()) {
			id2fragment.get(id).setParentRelations(id2parents.get(id));
		}
		
		// Children
		Map<Long, List<FragmentRelation>> id2children = getChildrenForEach(ids);
		
		List<RawFragment> allChildren = new ArrayList<RawFragment>();
		for (Long id : id2children.keySet()) {
			List<FragmentRelation> children = id2children.get(id);
			
			// Set children
			RawFragment fragment = id2fragment.get(id);
			fragment.setChildRelations(children);
			
			// Collect all children
			for (FragmentRelation relation : children) 
				allChildren.add((RawFragment)relation.to);
		}
		
		// Grandchildren
		setChildrenToEach(allChildren);
	}
	
	private void setChildrenToEach(List<RawFragment> fragments) throws Exception {
		Assert.Arg.notNull(fragments, "fragments");
		
		if (fragments.isEmpty()) return;
		
		// list -> map & duplications, clear all classifications
		Map<Long, RawFragment> id2fragment = new HashMap<Long, RawFragment>();
		List<RawFragment> duplications = new ArrayList<RawFragment>();
		for (RawFragment fragment : fragments) {
			if (id2fragment.containsKey(fragment.getId())) 
				duplications.add(fragment);
			else 
				id2fragment.put(fragment.getId(), fragment);
		}
		
		// get children (without sorting)
		Map<Long, List<FragmentRelation>> id2children = 
			getChildrenForEach(new ArrayList<Long>(id2fragment.keySet()));
		for (Long id : id2children.keySet()) {
			RawFragment fragment = id2fragment.get(id);
			fragment.setChildRelations(id2children.get(id));
		}
		
		for (RawFragment duplication : duplications) {
			duplication.setChildRelations(
				id2fragment.get(duplication.getId()).getChildRelations());
		}
	}

	private Map<Long, List<FragmentRelation>> getParentsForEach(List<Long> fragmentIds)
	throws NoSuchEntityException, Exception {
		Map<Long, List<FragmentRelation>> results = 
			new HashMap<Long, List<FragmentRelation>>();
		if (fragmentIds.isEmpty()) return results;
		
		FragmentRelationRowMapper parentMapper = 
			new FragmentRelationRowMapper(
				this.relationFactory, "fragment_relation.", 
				this.fragmentRowMapper, null, 
				results);
		
		StringBuilder sql = new StringBuilder();
		sql.append("select ");
		sql.append(parentMapper.selectAll());
		sql.append(", " + this.fragmentRowMapper.selectAll());
		sql.append(" from fragment_relation, fragment");
		sql.append(" where fragment_relation.from_id = fragment.fragment_id");
		sql.append(" and fragment_relation.to_id in (");
		for (int i = 0; i < fragmentIds.size(); i++) {
			if (i > 0) sql.append(", ");
			sql.append(fragmentIds.get(i));
		}
		sql.append(")");
		appendConditionToExcludeTrash(sql, "fragment_relation.from_id");

		if (logger.isDebugEnabled()) logger.debug("getParentsForEach: " + fragmentIds);
		this.jdbcTemplate.query(sql.toString(), parentMapper);

		return results;
	}

	private Map<Long, List<FragmentRelation>> getChildrenForEach(List<Long> fragmentIds)
	throws Exception {
		Map<Long, List<FragmentRelation>> results = 
			new HashMap<Long, List<FragmentRelation>>();
		if (fragmentIds.isEmpty()) return results;
		
		FragmentRelationRowMapper childMapper =
			new FragmentRelationRowMapper(
				this.relationFactory, "fragment_relation.", 
				null, this.fragmentRowMapper, 
				results);
		
		StringBuilder sql = new StringBuilder();
		sql.append("select ");
		sql.append(childMapper.selectAll());
		sql.append(", " + this.fragmentRowMapper.selectAll());
		sql.append(" from fragment_relation, fragment");
		sql.append(" where fragment_relation.to_id = fragment.fragment_id");
		sql.append(" and fragment_relation.from_id in (");
		for (int i = 0; i < fragmentIds.size(); i++) {
			if (i > 0) sql.append(", ");
			sql.append(fragmentIds.get(i));
		}
		sql.append(")");
		appendConditionToExcludeTrash(sql, "fragment_relation.to_id");
		
		sql.append(" order by fragment_relation.priority desc nulls last");
		sql.append(", fragment_relation.fragment_relation_id");
		
		if (logger.isDebugEnabled()) logger.debug("getChildrenForEach: " + fragmentIds);
		this.jdbcTemplate.query(sql.toString(), childMapper);
		
		return results;
	}
}
