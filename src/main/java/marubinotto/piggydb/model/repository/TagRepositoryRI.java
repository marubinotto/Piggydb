package marubinotto.piggydb.model.repository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import marubinotto.piggydb.model.BaseDataObsoleteException;
import marubinotto.piggydb.model.DuplicateException;
import marubinotto.piggydb.model.Entity;
import marubinotto.piggydb.model.InvalidTaggingException;
import marubinotto.piggydb.model.Tag;
import marubinotto.piggydb.model.TagRepository;
import marubinotto.piggydb.model.User;
import marubinotto.piggydb.model.entity.RawClassifiable;
import marubinotto.piggydb.model.entity.RawTag;
import marubinotto.piggydb.util.PiggydbUtils;
import marubinotto.util.Assert;
import marubinotto.util.paging.Page;
import marubinotto.util.paging.PageUtils;
import marubinotto.util.time.DateTime;

import org.apache.commons.collections.comparators.NullComparator;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class TagRepositoryRI extends TagRepository.Base {

	private static Log logger = LogFactory.getLog(TagRepositoryRI.class);
	
	private long idCounter = 0;
	private Map<Long, RawTag> idMap = new HashMap<Long, RawTag>();
	private Map<String, RawTag> nameMap = new HashMap<String, RawTag>();
	private SortedSet<RawTag> tags = new TreeSet<RawTag>(Tag.TAG_NAME_COMPARATOR);
	
	private boolean dirty = false;

	public synchronized long register(Tag tag) throws Exception {
		Assert.Arg.notNull(tag, "tag");
		Assert.require(tag instanceof RawTag, "tag instanceof RawTag");
		Assert.Arg.notNull(tag.getName(), "tag.getName()");
		Assert.require(tag.getId() == null, "tag.getId() == null");
		
		if (this.nameMap.containsKey(tag.getName())) {
			throw new DuplicateException("Duplicate: " + tag.getName());
		}
		
		DateTime now = DateTime.getCurrentTime();
		((RawTag)tag).setId(new Long(++this.idCounter));
		((RawTag)tag).setCreationDatetime(now);
		((RawTag)tag).setUpdateDatetime(now);
		
		registerNewSuperTags(tag);
		internalRegister((RawTag)tag);

		return tag.getId().longValue();
	}
	
	protected void registerNewSuperTags(Tag tag) throws Exception {
		for (Tag parentTag : tag.getClassification()) {
			if (parentTag.getId() == null) register(parentTag);
		}
	}

	/**
	 * The target tag and its super tags all should have an ID.
	 */
	private void internalRegister(RawTag tagWithId) {
		RawTag copy = (RawTag)tagWithId.getDeepCopy();
		this.idMap.put(copy.getId(), copy);
		this.nameMap.put(copy.getName(), copy);
		this.tags.add(copy);
	}

	public synchronized Tag get(long id) throws Exception {
		return returnDeepCopyOrNull(this.idMap.get(id));
	}
	
	private Tag returnDeepCopyOrNull(RawTag internalTag) throws Exception {
		if (internalTag != null) {
			if (this.dirty) {
				internalTag.getMutableClassification().refreshEachTag(this);
			}
			return (Tag)internalTag.getDeepCopy();
		}
		else {
			return null;
		}
	}
	
	public synchronized Tag getByName(String name) throws Exception {
		return returnDeepCopyOrNull(this.nameMap.get(name));
	}

	public synchronized boolean update(Tag tag) 
	throws BaseDataObsoleteException, Exception {
		Assert.Arg.notNull(tag, "tag");
		Assert.require(tag instanceof RawTag, "tag instanceof RawTag");
		Assert.Arg.notNull(tag.getId(), "tag.getId()");
		Assert.Arg.notNull(tag.getName(), "tag.getName()");		
		
		Tag latest = get(tag.getId().longValue());
		if (latest == null) {
			return false;	
		}
		
		if (tag.getUpdateDatetime() == null || 
			!tag.getUpdateDatetime().equals(latest.getUpdateDatetime())) {
			throw new BaseDataObsoleteException();
		}
		
		Tag sameNameTag = this.nameMap.get(tag.getName());
		if (sameNameTag != null && !tag.getId().equals(sameNameTag.getId())) {
			throw new DuplicateException("Duplicate: " + tag.getName());
		}
		
		((RawTag)tag).setUpdateDatetime(DateTime.getCurrentTime());

		internalDelete(latest);
		registerNewSuperTags(tag);
		internalRegister((RawTag)tag);
		this.dirty = true;
		return true;
	}
	
	public synchronized void delete(long id, User user) throws Exception {
		super.delete(id, user);
	}
	
	@Override
	protected void doDelete(Tag tag, User user) throws Exception {
		internalDelete(tag);
	}
	
	private void internalDelete(Tag tag) {
		this.idMap.remove(tag.getId());
		this.nameMap.remove(tag.getName());
		this.tags.remove(tag);
		this.dirty = true;
	}
	
	public synchronized long size() throws Exception {
		return this.tags.size();
	}

	public synchronized boolean containsName(String name) throws Exception {
		return this.nameMap.containsKey(name);
	}
	
	public synchronized Long getIdByName(String name) throws Exception {
		Assert.Arg.notNull(name, "name");
		Tag tag = getByName(name);
		if (tag == null) {
			return null;
		}
		return tag.getId();
	}

	public synchronized Iterator<String> iterateAllTagNames() throws Exception {
		final Iterator<RawTag> itag = this.tags.iterator();
		return new Iterator<String>() {
			public boolean hasNext() {
				return itag.hasNext();
			}

			public String next() {
				return itag.next().getName();
			}

			public void remove() {
				itag.remove();
			}	
		};
	}

	public synchronized Page<Tag> findByParentTag(
		long parentTagId, 
		int pageSize, 
		int pageIndex) 
	throws Exception {
		updateRelationsInternally();
		return PageUtils.getPage(
			internalFindByParentTag(parentTagId), pageSize, pageIndex);
	}
	
	private List<Tag> internalFindByParentTag(long parentTagId) 
	throws Exception {
		List<Tag> hits = new ArrayList<Tag>();
		for (Tag tag : this.tags) {
			if (tag.getClassification().containsTagId(parentTagId)) {
				hits.add(tag);
			}
		}
		return hits;
	}
	
	private void updateRelationsInternally() throws InvalidTaggingException {
		if (!this.dirty) {
			return;
		}
		logger.debug("updateRelationsInternally");
		for (RawTag tag : this.tags) {
			for (Tag superTag : tag.getClassification()) {
				if (superTag.getId() != null) {
					tag.getMutableClassification().removeTag(superTag.getName());
					Tag latestSuperTag = this.idMap.get(superTag.getId());
					if (latestSuperTag != null) {
						tag.getMutableClassification().addTag(latestSuperTag);
					}
				}
			}
		}
		this.dirty = false;
	}
	
	public Page<Tag> findByKeywords(String keywords, int pageSize, int pageIndex)
	throws Exception {
		if (StringUtils.isBlank(keywords)) return PageUtils.empty(pageSize);
		
		String[] keywordList = PiggydbUtils.splitToKeywords(keywords.toLowerCase());
		List<RawTag> hit = new ArrayList<RawTag>();
		for (RawTag tag : this.tags) {
			for (int i = 0; i < keywordList.length; i++) {
				if (!tag.getName().contains(keywordList[i])) break;
				if (i == (keywordList.length - 1)) hit.add(tag);
			}
		}
		
		Page<RawTag> page = PageUtils.getPage(hit, pageSize, pageIndex);
		RawClassifiable.refreshEachTag(page, this);
		return PageUtils.<Tag>covariantCast(page);
	}
	
	public synchronized Page<Tag> getRecentChanges(int pageSize, int pageIndex)
	throws Exception {
		List<RawTag> orderd = new ArrayList<RawTag>(this.tags);
		Collections.sort(orderd, new Entity.RecentChangeComparator());
		Page<RawTag> page = PageUtils.getPage(orderd, pageSize, pageIndex);
		RawClassifiable.refreshEachTag(page, this);
		return PageUtils.<Tag>covariantCast(page);
	}

	public synchronized Page<Tag> getRootTags(int pageSize, int pageIndex) 
	throws Exception {
		List<RawTag> roots = new ArrayList<RawTag>();
		for (RawTag tag : this.tags) {
			if (tag.getClassification().size() == 0) roots.add(tag);
		}
		Page<RawTag> page = PageUtils.getPage(roots, pageSize, pageIndex);
		RawClassifiable.refreshEachTag(page, this);
		return PageUtils.<Tag>covariantCast(page);
	}
	
	public synchronized List<String> getNamesLike(String criteria) throws Exception {
		Assert.Arg.notNull(criteria, "criteria");
		
		logger.debug("getNamesLike: " + criteria);
		criteria = criteria.toLowerCase();
		
		List<String> names = new ArrayList<String>();
		for (Tag tag : this.tags) {
			if (tag.getName().toLowerCase().startsWith(criteria)) 
				names.add(tag.getName());
		}			
		return names;
	}
	
	public synchronized Set<Long> getAllSubordinateTagIds(Set<Long> tagIds) 
	throws Exception {
		Assert.Arg.notNull(tagIds, "tagIds");

		updateRelationsInternally();
		Set<Long> results = new HashSet<Long>();
		for (Long tagId : tagIds) {
			getSubordinateTagIdsRecursively(tagId, results);
		}
		return results;
	}
	
	private void getSubordinateTagIdsRecursively(long tagId, Set<Long> tagIds) 
	throws Exception {
		List<Tag> children = internalFindByParentTag(tagId);
		for (Tag child : children) {
			tagIds.add(child.getId());
			getSubordinateTagIdsRecursively(child.getId(), tagIds);
		}
	}

	public synchronized Map<Long, String> getNames(Set<Long> ids) 
	throws Exception {
		Assert.Arg.notNull(ids, "ids");
		
		Map<Long, String> names = new HashMap<Long, String>();
		for (Long id : ids) {
			Tag tag = get(id);
			if (tag != null) names.put(id, tag.getName());
		}
		return names;
	}
	
	public synchronized Set<Long> selectAllThatHaveChildren(Set<Long> tagIds) 
	throws Exception {
		Set<Long> result = new HashSet<Long>();
		for (Long tagId : tagIds) {
			if (findByParentTag(tagId, 1, 0).size() > 0) {
				result.add(tagId);
			}
		}
		return result;
	}
	
	@SuppressWarnings("rawtypes")
	private static final Comparator nullHighComparator = new NullComparator(true);
	
	public synchronized List<Tag> getPopularTags(int maxSize) 
	throws Exception {
		SortedSet<Tag> results = new TreeSet<Tag>(new Comparator<Tag>() {
			@SuppressWarnings("unchecked")
			public int compare(Tag tag1, Tag tag2) {
				return nullHighComparator.compare(tag2.getPopularity(), tag1.getPopularity());
			}
		});
		updateRelationsInternally();
		for (RawTag tag : this.tags) {
			tag.setPopularity(0L);
			for (Tag tagged : this.tags) {
				if (tagged.getClassification().containsTagId(tag.getId())) {
					tag.addPopularity();
				}
			}
			if (tag.getPopularity() > 0) results.add(tag);
		}
		if (results.size() <= maxSize) 
			return new ArrayList<Tag>(results);
		else
			return new ArrayList<Tag>(results).subList(0, maxSize);
	}
	
	public synchronized Page<Tag> orderByName(int pageSize, int pageIndex)
	throws Exception {
		Page<RawTag> page = PageUtils.getPage(
			new ArrayList<RawTag>(this.tags), pageSize, pageIndex);
		RawClassifiable.refreshEachTag(page, this);
		return PageUtils.<Tag>covariantCast(page);
	}
	
	public synchronized Long countTaggings() throws Exception {
		return null;
	}
}
