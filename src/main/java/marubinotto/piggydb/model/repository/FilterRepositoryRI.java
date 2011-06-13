package marubinotto.piggydb.model.repository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import marubinotto.piggydb.model.BaseDataObsoleteException;
import marubinotto.piggydb.model.DuplicateException;
import marubinotto.piggydb.model.Entity;
import marubinotto.piggydb.model.Filter;
import marubinotto.piggydb.model.FilterRepository;
import marubinotto.piggydb.model.User;
import marubinotto.piggydb.model.entity.RawFilter;
import marubinotto.util.Assert;
import marubinotto.util.paging.Page;
import marubinotto.util.paging.PageUtils;
import marubinotto.util.time.DateTime;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class FilterRepositoryRI extends FilterRepository.Base {

	private static Log logger = LogFactory.getLog(FilterRepositoryRI.class);
	
	private TagRepositoryRI tagRepository; 
	
	private long idCounter = 0;
	private Map<Long, RawFilter> idMap = new HashMap<Long, RawFilter>();
	private Map<String, RawFilter> nameMap = new HashMap<String, RawFilter>();
	
	public FilterRepositoryRI() {
		this.tagRepository = new TagRepositoryRI();
	}
	
	public synchronized void setTagRepository(TagRepositoryRI tagRepository) {
		this.tagRepository = tagRepository;
	}
	
	public synchronized TagRepositoryRI getTagRepository() {
		return this.tagRepository;
	}

	public synchronized long register(Filter filter) throws Exception {
		Assert.Arg.notNull(filter, "filter");
		Assert.require(filter instanceof RawFilter, "filter instanceof RawFilter");
		Assert.Arg.notNull(filter.getName(), "filter.getName()");
		Assert.require(filter.getId() == null, "filter.getId() == null");
		
		if (this.nameMap.containsKey(filter.getName())) {
			throw new DuplicateException("Duplicate: " + filter.getName());
		}
		
		DateTime now = DateTime.getCurrentTime();
		((RawFilter)filter).setId(new Long(++idCounter));
		((RawFilter)filter).setCreationDatetime(now);
		((RawFilter)filter).setUpdateDatetime(now);
		
		internalRegister((RawFilter)filter);
		
		return filter.getId().longValue();
	}
	
	private void internalRegister(RawFilter filterWithId) {
		RawFilter copy = (RawFilter)filterWithId.getDeepCopy();
		this.idMap.put(copy.getId(), copy);
		this.nameMap.put(copy.getName(), copy);
	}
	
	public synchronized Filter get(long id) throws Exception {
		return returnDeepCopyOrNull(this.idMap.get(id));
	}
	
	private Filter returnDeepCopyOrNull(RawFilter internalFilter) throws Exception {
		if (internalFilter != null) {
			internalFilter.getClassification().refreshEachTag(this.tagRepository);
			internalFilter.getExcludes().refreshEachTag(this.tagRepository);
			return (Filter)internalFilter.getDeepCopy();
		}
		else {
			return null;
		}
	}
	
	public synchronized Filter getByName(String name) throws Exception {
		return returnDeepCopyOrNull(this.nameMap.get(name));
	}
	
	public synchronized Long getIdByName(String name) throws Exception {
		Filter filter = this.nameMap.get(name);
		return filter != null ? filter.getId() : null;
	}
	
	public synchronized boolean update(Filter filter) 
	throws BaseDataObsoleteException, Exception {
		Assert.Arg.notNull(filter, "filter");
		Assert.require(filter instanceof RawFilter, "filter instanceof RawFilter");
		Assert.Arg.notNull(filter.getId(), "filter.getId()");
		Assert.Arg.notNull(filter.getName(), "filter.getName()");
		
		Filter latest = get(filter.getId());
		if (latest == null) {
			return false;	
		}
		
		if (filter.getUpdateDatetime() == null || 
			!filter.getUpdateDatetime().equals(latest.getUpdateDatetime())) {
			throw new BaseDataObsoleteException();
		}
		
		Filter sameNameFilter = this.nameMap.get(filter.getName());
		if (sameNameFilter != null && !filter.getId().equals(sameNameFilter.getId())) {
			throw new DuplicateException("Duplicate: " + filter.getName());
		}
		
		((RawFilter)filter).setUpdateDatetime(DateTime.getCurrentTime());
		
		internalDelete(filter);			
		internalRegister((RawFilter)filter);
		return true;
	}
	
	public synchronized List<String> getNamesLike(String criteria) throws Exception {
		Assert.Arg.notNull(criteria, "criteria");
		logger.debug("getNamesLike: " + criteria);
		List<String> names = new ArrayList<String>();
		for (Filter filter : this.idMap.values()) {
			if (filter.getName().startsWith(criteria)) {
				names.add(filter.getName());
			}
		}			
		return names;
	}

	public Page<Filter> getRecentChanges(int pageSize, int pageIndex)
	throws Exception {
		ArrayList<Filter> orderd = new ArrayList<Filter>(this.idMap.values());
		Collections.sort(orderd, new Entity.RecentChangeComparator());
		Page<Filter> page = PageUtils.getPage(orderd, pageSize, pageIndex);
		return page;
	}
	
	public synchronized void delete(long id, User user) throws Exception {
		super.delete(id, user);
	}

	@Override
	protected void doDelete(Filter filter, User user) {
		internalDelete(filter);
	}
	
	private void internalDelete(Filter filter) {
		this.idMap.remove(filter.getId());
		this.nameMap.remove(filter.getName());
	}

	public long size() throws Exception {
		return this.idMap.size();
	}

	public Map<Long, String> getNames(Set<Long> ids) throws Exception {
		Assert.Arg.notNull(ids, "ids");
		
		Map<Long, String> names = new HashMap<Long, String>();
		for (Long id : ids) {
			Filter filter = get(id);
			if (filter != null) names.put(id, filter.getName());
		}
		return names;
	}
}
