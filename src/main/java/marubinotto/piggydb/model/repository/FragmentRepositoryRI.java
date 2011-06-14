package marubinotto.piggydb.model.repository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import marubinotto.piggydb.model.BaseDataObsoleteException;
import marubinotto.piggydb.model.DuplicateException;
import marubinotto.piggydb.model.Entity;
import marubinotto.piggydb.model.FileRepository;
import marubinotto.piggydb.model.Filter;
import marubinotto.piggydb.model.Fragment;
import marubinotto.piggydb.model.FragmentRelation;
import marubinotto.piggydb.model.FragmentRepository;
import marubinotto.piggydb.model.FragmentsOptions;
import marubinotto.piggydb.model.ModelUtils;
import marubinotto.piggydb.model.NoSuchEntityException;
import marubinotto.piggydb.model.RelatedTags;
import marubinotto.piggydb.model.Tag;
import marubinotto.piggydb.model.User;
import marubinotto.piggydb.model.FragmentsOptions.SortOption;
import marubinotto.piggydb.model.entity.RawFilter;
import marubinotto.piggydb.model.entity.RawFragment;
import marubinotto.piggydb.model.enums.FragmentField;
import marubinotto.util.Assert;
import marubinotto.util.paging.Page;
import marubinotto.util.paging.PageUtils;
import marubinotto.util.time.DateTime;
import marubinotto.util.time.Interval;
import marubinotto.util.time.Month;

public class FragmentRepositoryRI extends FragmentRepository.Base {

	private ArrayList<RawFragment> fragments = new ArrayList<RawFragment>();
	
	private TagRepositoryRI tagRepository; 
	
	public FragmentRepositoryRI() {
		this.tagRepository = new TagRepositoryRI();
		setFileRepository(new FileRepository.InMemory());
	}
	
	public synchronized void setTagRepository(TagRepositoryRI tagRepository) {
		this.tagRepository = tagRepository;
	}

	public synchronized TagRepositoryRI getTagRepository() {
		return this.tagRepository;
	}
	
	public synchronized long register(Fragment fragment) throws Exception {
		Assert.Arg.notNull(fragment, "fragment");
		Assert.require(fragment instanceof RawFragment, "fragment instanceof RawFragment");
		Assert.require(fragment.getId() == null, "fragment.getId() == null");
		Assert.Property.requireNotNull(fileRepository, "fileRepository");

		DateTime now = DateTime.getCurrentTime();
		((RawFragment)fragment).setId(new Long(size() + 1));
		((RawFragment)fragment).setCreationDatetime(now);
		((RawFragment)fragment).setUpdateDatetime(now);
		
		// To avoid registering only a record (should register a file before the record)
		// To avoid registering only a file (no problem about this version)
		if (fragment.isFile()) {
			this.fileRepository.putFile(fragment);
		}
	
		makeTagsWithoutIdRegistered(fragment);
		this.fragments.add((RawFragment)((RawFragment)fragment).getDeepCopy());

		return fragment.getId().longValue();
	}

	public synchronized Fragment get(long id) throws Exception {
		Assert.Property.requireNotNull(tagRepository, "tagRepository");
		
		if (!containsId(id)) return null;
		
		refreshInternal();
		
		RawFragment fragment = (RawFragment)internalGet(id).getDeepCopy();
		
		// Parents
		setParentsTo(fragment);
		
		// Children & Children's parents, Grandchildren, Great-grandchildren
		setChildrenTo(fragment);
		for (Fragment child : fragment.getChildren()) {
			setParentsTo((RawFragment)child);
			setChildrenTo((RawFragment)child);
		}
		List<Fragment> grandchildren = 
			ModelUtils.collectChildrenOfEach(fragment.getChildren());
		setChildrenToEach(grandchildren);

		return fragment;
	}
	
	private void refreshInternal() throws Exception {
		for (RawFragment fragment : this.fragments) {
			fragment.getMutableClassification().refreshEachTag(this.tagRepository);
		}
	}
	
	private RawFragment internalGet(long id) throws Exception {
		return this.fragments.get((int)(id - 1));
	}
	
	private boolean containsId(long id) {
		return id >= 1 && id <= size();
	}
	
	public synchronized long size() {
		return this.fragments.size();
	}
	
	public synchronized boolean update(Fragment fragment, boolean updateTimestamp) 
	throws BaseDataObsoleteException, Exception {
		Assert.Arg.notNull(fragment, "fragment");
		Assert.require(fragment instanceof RawFragment, "fragment instanceof RawFragment");
		Assert.Arg.notNull(fragment.getId(), "fragment.getId()");
		Assert.Property.requireNotNull(fileRepository, "fileRepository");

		Fragment latest = get(fragment.getId().longValue());		
		if (latest == null) return false;
		
		if (fragment.getUpdateDatetime() == null || 
			!fragment.getUpdateDatetime().equals(latest.getUpdateDatetime())) {
			throw new BaseDataObsoleteException();
		}
		
		if (updateTimestamp)
			((RawFragment)fragment).setUpdateDatetime(DateTime.getCurrentTime());
		
		// To avoid registering only a record (should register a file before the record)
		// To avoid registering only a file (no problem about this version)
		if (fragment.isFile() && fragment.getFileInput() != null) {
			this.fileRepository.putFile(fragment);
		}
		
		makeTagsWithoutIdRegistered(fragment);	
		this.fragments.set(
			(int)(fragment.getId() - 1), 
			(RawFragment)((RawFragment)fragment).getDeepCopy());	
		return true;
	}
	
	public synchronized void delete(long id, User user) throws Exception {
		super.delete(id, user);
	}

	@Override
	protected void doDelete(Fragment fragment, User user) throws Exception {
		// Delete a file
		if (fragment.isFile()) this.fileRepository.deleteFile(fragment);
		
		// Delete relations
		Set<Long> relationsToDelete = new HashSet<Long>();
		for (StoredRelation relation : this.relations.values()) {
			if (relation.from == fragment.getId() || relation.to == fragment.getId()) 
				relationsToDelete.add(relation.id);
		}
		for (Long relationId : relationsToDelete) doDeleteRelation(relationId);

		// Delete the fragment 
		this.fragments.remove((int)(fragment.getId() - 1));
	}
	
	public synchronized void refreshClassifications(List<? extends Fragment> fragments) 
	throws Exception {
		Assert.Arg.notNull(fragments, "fragments");
		
		refreshInternal();
		for (Fragment output : fragments) {
			Fragment genuine = internalGet(output.getId());
			if (genuine == null) continue;
			((RawFragment)output).getMutableClassification().syncWith(genuine.getClassification());
		}
	}
	
	public synchronized void deleteTrashes(User user) throws Exception {
		Tag trashTag = this.tagRepository.getTrashTag();
		if (trashTag == null) return;
		
		RawFilter filter = new RawFilter();
		filter.getClassification().addTag(trashTag);
		
		for (Fragment fragment : internalFindByFilter(filter)) 
			delete(fragment.getId(), user);
	}

	@SuppressWarnings("unchecked")
	public synchronized Page<Fragment> getFragments(FragmentsOptions options) 
	throws Exception {
		Assert.Arg.notNull(options, "options");
		
		refreshInternal();
		
		ArrayList<RawFragment> results = (ArrayList<RawFragment>)this.fragments.clone();
		excludeTrash(results);
		options.sortOption.sort(results);
		
		Page<RawFragment> page = 
			PageUtils.getPage(results, options.pageSize, options.pageIndex);
		if (options.eagerFetching) {
			setParentsToAll(page);
			setChildrenToEach(page);
		}	
		return PageUtils.<Fragment>covariantCast(page);
	}

	private void excludeTrash(List<? extends Fragment> fragments) throws Exception {
		for (Iterator<? extends Fragment> i = fragments.iterator(); i.hasNext();) {
			if (i.next().isTrash()) i.remove();
		}
	}
	
	public synchronized Page<Fragment> findByFilter(Filter filter, FragmentsOptions options) 
	throws Exception {
		Assert.Arg.notNull(filter, "filter");
		Assert.Arg.notNull(options, "options");
		Assert.Property.requireNotNull(tagRepository, "tagRepository");

		List<Fragment> results = internalFindByFilter(filter);
		options.sortOption.sort(results);
		
		Page<Fragment> page =  
			PageUtils.getPage(results, options.pageSize, options.pageIndex);	
		if (options.eagerFetching) {
			setParentsToAll(page);
			setChildrenToEach(page);
		}	
		return page;
	}
	
	public synchronized RelatedTags getRelatedTags(Filter filter) throws Exception {
		Assert.Arg.notNull(filter, "filter");
		Assert.Property.requireNotNull(tagRepository, "tagRepository");

		List<Fragment> all = internalFindByFilter(filter);
		
		RelatedTags relatedTags = new RelatedTags();
		relatedTags.setFilter(filter);
		for (Fragment fragment : all) {
			for (Tag tag : fragment.getClassification()) relatedTags.add(tag.getId(), 1);
		}
		return relatedTags;
	}

	private List<Fragment> internalFindByFilter(Filter filter) throws Exception {
		refreshInternal();
		
		List<Set<Long>> expandedTags = filter.getClassification().expandEach(this.tagRepository);
		Set<Long> excludes = filter.getExcludes().expandAll(this.tagRepository);
		
		List<Fragment> results = new ArrayList<Fragment>();
		for (RawFragment fragment : this.fragments) {
			// Should be classified by all tags
			if (!fragment.getClassification().isClassifiedByAll(expandedTags)) {
				continue;
			}
			// Should not be classified by any excludes
			if (fragment.getClassification().containsAny(excludes)) {
				continue;
			}
			results.add(fragment);
		}
		Collections.sort(results, new Entity.RecentChangeComparator());
		return results;
	}
	
	public synchronized Set<Integer> getDaysOfMonth(FragmentField field, Month month) 
	throws Exception {
		Assert.Arg.notNull(field, "field");
		Assert.Arg.notNull(month, "month");
		
		Set<Integer> days = new HashSet<Integer>();
		for (Fragment fragment : this.fragments) {
			if (fragment.isTrash()) {
				continue;
			}
			if (field.equals(FragmentField.CREATION_DATETIME)) {
				if (month.containsInstant(fragment.getCreationDatetime()))
					days.add(new Integer(fragment.getCreationDatetime().getDayOfMonth()));
			}
			else if (field.equals(FragmentField.UPDATE_DATETIME)) {
				if (month.containsInstant(fragment.getUpdateDatetime()))
					days.add(new Integer(fragment.getUpdateDatetime().getDayOfMonth()));
			}
		}
		return days;
	}

	public synchronized Page<Fragment> findByTime(
		Interval interval, 
		FragmentField field, 
		FragmentsOptions options)
	throws Exception {
		Assert.Arg.notNull(interval, "interval");
		Assert.Arg.notNull(field, "field");
		Assert.Arg.notNull(options, "options");
		
		refreshInternal();
		
		List<RawFragment> results = new ArrayList<RawFragment>();
		for (RawFragment fragment : this.fragments) {
			if (field.equals(FragmentField.CREATION_DATETIME)) {
				if (interval.containsInstant(fragment.getCreationDatetime()))
					results.add(fragment);
			}
			else if (field.equals(FragmentField.UPDATE_DATETIME)) {
				if (interval.containsInstant(fragment.getUpdateDatetime()))
					results.add(fragment);
			}		
		}
		excludeTrash(results);
		options.sortOption.sort(results);
		
		Page<RawFragment> page = PageUtils.getPage(results, options.pageSize, options.pageIndex);
		
		if (options.eagerFetching) {
			setParentsToAll(page);
			setChildrenToEach(page);
		}
		
		return PageUtils.<Fragment>covariantCast(page);
	}

	private void makeTagsWithoutIdRegistered(Fragment fragment) throws Exception {
		Assert.Property.requireNotNull(tagRepository, "tagRepository");
		
		for (Tag tag : fragment.getClassification()) {
			if (tag.getId() == null) this.tagRepository.register(tag);
		}
	}
	
	public synchronized Map<Long, String> getNames(Set<Long> ids) throws Exception {
		Assert.Arg.notNull(ids, "ids");
		
		Map<Long, String> titles = new HashMap<Long, String>();
		for (Long id : ids) {
			Fragment fragment = get(id);
			if (fragment != null) titles.put(id, fragment.getTitle());
		}
		return titles;
	}
	
	public synchronized Page<Fragment> findByKeywords(
		String keywords, 
		FragmentsOptions options)
	throws Exception {
		return PageUtils.empty(options.pageSize);
	}
	
	public synchronized Page<Fragment> findByUser(String userName, FragmentsOptions options)
	throws Exception {
		Assert.Arg.notNull(userName, "userName");
		Assert.Arg.notNull(options, "options");
		
		refreshInternal();
		
		List<RawFragment> results = new ArrayList<RawFragment>();
		for (RawFragment fragment : this.fragments) {
			if (userName.equals(fragment.getCreator()) || 
				userName.equals(fragment.getUpdater())) {
				results.add(fragment);
			}
		}
		excludeTrash(results);
		options.sortOption.sort(results);
		
		Page<RawFragment> page = PageUtils.getPage(results, options.pageSize, options.pageIndex);
		
		if (options.eagerFetching) {
			setParentsToAll(page);
			setChildrenToEach(page);
		}
		
		return PageUtils.<Fragment>covariantCast(page);
	}
	
	public synchronized List<Fragment> getByIds(
		Collection<Long> fragmentIds, 
		SortOption sortOption, 
		boolean eagerFetching) 
	throws Exception {
		List<Fragment> fragments = new ArrayList<Fragment>();
		for (Long id : fragmentIds) {
			Fragment fragment = get(id);
			if (fragment != null) fragments.add(fragment);
		}
		sortOption.sort(fragments);
		return fragments;
	}
	
	public synchronized Fragment getUserFragment(String userName) throws Exception {
		Assert.Arg.notNull(userName, "userName");
		
		Tag userTag = getTagRepository().getByName(Tag.NAME_USER);
		if (userTag == null) return null;
		
		RawFilter filter = new RawFilter();
		filter.getClassification().addTag(userTag);

		List<Fragment> fragments = internalFindByFilter(filter);
		if (fragments.isEmpty()) return null;
		excludeTrash(fragments);
		
		for (Fragment fragment : fragments) {
			if (userName.equals(fragment.getTitle())) return fragment;
		}
		return null;
	}

	private static class StoredRelation {
		public long id;
		public long from;
		public long to;
		public int priority;
		public DateTime creationDatetime;
		public DateTime updateDatetime;
		public String creator;
	}
	
	private long relationIdCounter = 0;
	private Map<Long, StoredRelation> relations = new HashMap<Long, StoredRelation>();
	
	public synchronized Long countRelations() throws Exception {
		return (long)this.relations.size();
	}
	
	@Override
	protected synchronized long doCreateRelation(long from, long to, User user)
	throws NoSuchEntityException, DuplicateException, Exception {
		if (!containsId(from)) {
			throw new NoSuchEntityException(from);
		}
		if (!containsId(to)) {
			throw new NoSuchEntityException(to);
		}
		
		for (StoredRelation relation : this.relations.values()) {
			if (relation.from == from && relation.to == to) {
				throw new DuplicateException();
			}
		}
		
		DateTime now = DateTime.getCurrentTime();
		StoredRelation relation = new StoredRelation();
		relation.id = ++this.relationIdCounter;
		relation.from = from;
		relation.to = to;
		relation.creationDatetime = now;
		relation.updateDatetime = now;
		relation.creator = user.getName();
		
		this.relations.put(relation.id, relation);
		
		return relation.id;
	}
	
	public synchronized FragmentRelation getRelation(long relationId) throws Exception {
		StoredRelation storedRelation = this.relations.get(relationId);
		return storedRelation != null ? restoreFragmentRelation(storedRelation) : null;
	}
	
	private FragmentRelation restoreFragmentRelation(StoredRelation storedRelation) 
	throws Exception {
		FragmentRelation relation = new FragmentRelation();
		relation.setId(storedRelation.id);
		relation.from = internalGet(storedRelation.from);
		relation.to = internalGet(storedRelation.to);
		relation.priority = storedRelation.priority;
		relation.setCreationDatetime(storedRelation.creationDatetime);
		relation.setUpdateDatetime(storedRelation.updateDatetime);
		relation.setCreator(storedRelation.creator);
		return relation;
	}
	
	@Override
	protected synchronized void doDeleteRelation(long relationId) throws Exception {
		this.relations.remove(new Long(relationId));
	}
	
	@Override
	protected void doUpdateChildRelationPriorities(List<Long> relationOrder) throws Exception {
		int count = relationOrder.size();
		for (Long relationId : relationOrder) {
			this.relations.get(relationId).priority = count--;
		}
	}
	
	
// Utilities
	
	private void setParentsTo(RawFragment fragment) throws Exception {
		List<FragmentRelation> parents = new ArrayList<FragmentRelation>();
		for (StoredRelation relation : this.relations.values()) {
			if (relation.to == fragment.getId()) {
				parents.add(restoreFragmentRelation(relation));
			}
		}
		excludeTrash(parents, true);
		fragment.setParentRelations(parents);
	}
	
	private void setParentsToAll(List<? extends Fragment> fragments) throws Exception {
		for (Fragment fragment : fragments) setParentsTo((RawFragment)fragment);
	}
	
	private void setChildrenTo(final RawFragment fragment) throws Exception {
		List<FragmentRelation> children = new ArrayList<FragmentRelation>();
		for (StoredRelation relation : this.relations.values()) {
			if (relation.from == fragment.getId()) {
				children.add(restoreFragmentRelation(relation));
			}
		}
		excludeTrash(children, false);
		fragment.setChildRelations(children);
		fragment.sortChildRelations();
	}
	
	private void setChildrenToEach(List<? extends Fragment> fragments) throws Exception {
		for (Fragment fragment : fragments) setChildrenTo((RawFragment)fragment);
	}
	
	private void excludeTrash(List<FragmentRelation> relations, boolean parent) 
	throws Exception {
		for (Iterator<FragmentRelation> i = relations.iterator(); i.hasNext();) {
			if (parent) {
				if (i.next().from.isTrash()) i.remove();
			}
			else {
				if (i.next().to.isTrash()) i.remove();
			}
		}
	}
}
