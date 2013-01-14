package marubinotto.piggydb.impl;

import static marubinotto.util.CollectionUtils.joinToString;
import static marubinotto.util.CollectionUtils.list;
import static marubinotto.util.CollectionUtils.set;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import marubinotto.piggydb.impl.mapper.FragmentRelationRowMapper;
import marubinotto.piggydb.impl.mapper.FragmentRowMapper;
import marubinotto.piggydb.impl.query.H2FragmentsAllButTrash;
import marubinotto.piggydb.impl.query.H2FragmentsByFilter;
import marubinotto.piggydb.impl.query.H2FragmentsByIds;
import marubinotto.piggydb.impl.query.H2FragmentsByKeywords;
import marubinotto.piggydb.impl.query.H2FragmentsByTime;
import marubinotto.piggydb.impl.query.H2FragmentsByUser;
import marubinotto.piggydb.impl.query.H2FragmentsOfUser;
import marubinotto.piggydb.model.Fragment;
import marubinotto.piggydb.model.FragmentList;
import marubinotto.piggydb.model.FragmentRelation;
import marubinotto.piggydb.model.FragmentRepository;
import marubinotto.piggydb.model.Tag;
import marubinotto.piggydb.model.auth.User;
import marubinotto.piggydb.model.entity.RawClassifiable;
import marubinotto.piggydb.model.entity.RawEntityFactory;
import marubinotto.piggydb.model.entity.RawFilter;
import marubinotto.piggydb.model.entity.RawFragment;
import marubinotto.piggydb.model.enums.FragmentField;
import marubinotto.piggydb.model.exception.DuplicateException;
import marubinotto.piggydb.model.exception.NoSuchEntityException;
import marubinotto.util.Assert;
import marubinotto.util.CollectionUtils;
import marubinotto.util.time.Month;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
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
		registerQuery(H2FragmentsByTime.class);
		registerQuery(H2FragmentsByUser.class);
		registerQuery(H2FragmentsByKeywords.class);
		registerQuery(H2FragmentsByIds.class);
		registerQuery(H2FragmentsOfUser.class);
		registerQuery(H2FragmentsByFilter.class);
		registerQuery(H2FragmentsAllButTrash.class);
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
		return this.jdbcTemplate;
	}

	public void setFragmentIdIncrementer(
		DataFieldMaxValueIncrementer fragmentIdIncrementer) {
		this.fragmentIdIncrementer = fragmentIdIncrementer;
	}

	public void setRelationIdIncrementer(
		DataFieldMaxValueIncrementer relationIdIncrementer) {
		this.relationIdIncrementer = relationIdIncrementer;
	}
	
	public FragmentRowMapper getFragmentRowMapper() {
		return this.fragmentRowMapper;
	}

	public boolean containsId(Long id) throws Exception {
		return this.jdbcTemplate.queryForInt(
	    "select count(*) from fragment where fragment_id = ?", 
	    new Object[]{id}) > 0;
	}

	public long register(Fragment fragment) throws Exception {
		Assert.Arg.notNull(fragment, "fragment");
		Assert.require(fragment instanceof RawFragment, "fragment instanceof RawFragment");
		Assert.Property.requireNotNull(fragmentIdIncrementer, "fragmentIdIncrementer");
		Assert.Property.requireNotNull(fileRepository, "fileRepository");
		
		if (fragment.getId() == null)
			((RawFragment)fragment).setId(this.fragmentIdIncrementer.nextLongValue());
		
		// Save the tag side
		saveTagSide((RawFragment)fragment);
		
		// Insert the fragment
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

	public Fragment get(long id, boolean fetchRelations) throws Exception {
		logger.debug("get: " + id);
		
		// entity
		RawFragment fragment = queryForOneFragment(
			"select " + this.fragmentRowMapper.selectAll() + " from fragment where fragment_id = ?",
			new Object[]{new Long(id)});
		if (fragment == null) return null;
		
		// classification
		refreshClassifications(list(fragment));
		
		// relationships
		if (fetchRelations) {
			setParentsTo(fragment);
			
			FragmentList<RawFragment> fragment2 = new FragmentList<RawFragment>(fragment);
			setChildrenToEach(fragment2);
			
			FragmentList<RawFragment> children = fragment2.getChildren();
			refreshClassifications(children.getFragments());
			setParentsAndChildrenWithGrandchildrenToEach(children.getFragments());
			fragment.checkTwoWayRelations();
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
	
	public List<Fragment> getFragmentsAtHome(User user) throws Exception {
		Assert.Arg.notNull(user, "user");
		
		RawFragment home = (RawFragment)getHome(true, user);
		if (home == null) return new ArrayList<Fragment>();
		
		FragmentList<RawFragment> homeAsList = new FragmentList<RawFragment>(home);
		FragmentList<RawFragment> children = homeAsList.getChildren();
		if (!children.isEmpty()) {
			refreshClassifications(children.getFragments());
			setParentsToEach(children);
			for (RawFragment child : children) child.checkTwoWayRelations();
		}
		
		return CollectionUtils.<Fragment>covariantCast(children.getFragments());
	}

	public void updateFragment(Fragment fragment, boolean updateTimestamp) 
	throws Exception {
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
	}
	
	@Override
	protected void doDelete(Fragment fragment, User user) throws Exception {
		// Delete the tag role
		if (fragment.getTagId() != null) {
			getTagRepository().delete(fragment.getTagId(), user);
		}
		
		// Delete the related taggings
		this.jdbcTemplate.update(
	    "delete from tagging where target_id = ? and target_type = ?", 
	    new Object[]{
	    	fragment.getId(),
	    	QueryUtils.TAGGING_TARGET_FRAGMENT
	    });
		
		// Delete the relations
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
		appendConditionToExcludeSpecialFragments(sql);
		appendConditionToExcludeTrash(sql, "fragment.fragment_id");

		return new HashSet<Integer>(
			this.jdbcTemplate.queryForList(sql.toString(), new Object[]{
				new Integer(month.getYear()), new Integer(month.getMonth())},
				Integer.class));
	}
	
	@SuppressWarnings("unchecked")
	public List<RawFragment> query(String sql,  Object[] args) throws Exception {
		return this.jdbcTemplate.query(sql.toString(), args, this.fragmentRowMapper);
	}
	
	public Map<Long, String> getNames(Set<Long> ids) throws Exception {
		Assert.Arg.notNull(ids, "ids");
		return QueryUtils.getValuesForIds("fragment", "title", ids, this.jdbcTemplate);
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
				new Object[] { new Long(relationId) }, 
				new FragmentRelationRowMapper(this.relationFactory, this));
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

	private List<Long> selectIdsClassifiedAsTrash() throws Exception {
		Tag trashTag = this.tagRepository.getTrashTag();
		if (trashTag == null) return new ArrayList<Long>();
		
		RawFilter filter = new RawFilter();
		filter.getClassification().addTag(trashTag);
		
		H2FragmentsByFilter query = (H2FragmentsByFilter)getQuery(H2FragmentsByFilter.class);
		query.setFilter(filter);
		return query.getFilteredIds(false);
	}

	public void appendConditionToExcludeTrash(StringBuilder sql, String columnNameForId) 
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
	
	public void appendConditionToExcludeSpecialFragments(StringBuilder sql) {
		sql.append(" and fragment.fragment_id > 0");
	}
	
	
// Resolve relations
	
	private void setParentsTo(RawFragment fragment) throws Exception {
		List<FragmentRelation> parents = 
			getParentsForEach(set(fragment.getId())).get(fragment.getId());
		if (parents == null) return;		
		fragment.setParentRelations(parents);
	}
	
	public void setParentsAndChildrenWithGrandchildrenToEach(List<RawFragment> fragments) 
	throws Exception {
		if (fragments.isEmpty()) return;
		
		FragmentList<RawFragment> fragments2 = new FragmentList<RawFragment>(fragments);
		setParentsToEach(fragments2);
		setChildrenToEach(fragments2);
		setChildrenToEach(fragments2.getChildren());
		for (RawFragment fragment : fragments2)
			fragment.checkTwoWayRelations();
	}
	
	public void setParentsToEach(FragmentList<RawFragment> fragments) throws Exception {
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
	
	public void setChildrenToEach(FragmentList<RawFragment> fragments) throws Exception {
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
				this.relationFactory, 
				"fragment_relation.", 
				this.fragmentRowMapper, 
				null, 
				results);
		
		StringBuilder sql = new StringBuilder();
		sql.append("select ");
		sql.append(parentMapper.selectAll());
		sql.append(", " + this.fragmentRowMapper.selectAll());
		sql.append(" from fragment_relation, fragment");
		sql.append(" where fragment_relation.from_id = fragment.fragment_id");
		sql.append(" and fragment_relation.to_id in (");
		sql.append(joinToString(fragmentIds, ", "));
		sql.append(")");
		appendConditionToExcludeTrash(sql, "fragment_relation.from_id");

		if (logger.isDebugEnabled()) logger.debug("getParentsForEach: " + fragmentIds);
		this.jdbcTemplate.query(sql.toString(), parentMapper);

		return results;
	}

	private Map<Long, List<FragmentRelation>> getChildrenForEach(Set<Long> fragmentIds)
	throws Exception {
		Map<Long, List<FragmentRelation>> results = 
			new HashMap<Long, List<FragmentRelation>>();
		if (fragmentIds.isEmpty()) return results;
		
		FragmentRelationRowMapper childMapper =
			new FragmentRelationRowMapper(
				this.relationFactory, 
				"fragment_relation.", 
				null, 
				this.fragmentRowMapper, 
				results);
		
		StringBuilder sql = new StringBuilder();
		sql.append("select ");
		sql.append(childMapper.selectAll());
		sql.append(", " + this.fragmentRowMapper.selectAll());
		sql.append(" from fragment_relation, fragment");
		sql.append(" where fragment_relation.to_id = fragment.fragment_id");
		sql.append(" and fragment_relation.from_id in (");
		sql.append(joinToString(fragmentIds, ", "));
		sql.append(")");
		appendConditionToExcludeTrash(sql, "fragment_relation.to_id");
		
		sql.append(" order by fragment_relation.priority desc nulls last");
		sql.append(", fragment_relation.fragment_relation_id");
		
		if (logger.isDebugEnabled()) logger.debug("getChildrenForEach: " + fragmentIds);
		this.jdbcTemplate.query(sql.toString(), childMapper);
		
		return results;
	}
}
