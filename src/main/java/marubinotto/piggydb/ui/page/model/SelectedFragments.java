package marubinotto.piggydb.ui.page.model;

import static marubinotto.util.CollectionUtils.inReverseOrder;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import marubinotto.piggydb.model.Fragment;
import marubinotto.piggydb.model.FragmentRepository;
import marubinotto.piggydb.model.ModelUtils;
import marubinotto.piggydb.model.query.FragmentsByIds;
import marubinotto.util.Assert;
import marubinotto.util.paging.Page;
import marubinotto.util.paging.PageImpl;
import marubinotto.util.paging.PageUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class SelectedFragments implements Serializable, Iterable<Long> {
	
	private static Log logger = LogFactory.getLog(SelectedFragments.class);

	private LinkedHashSet<Long> ids = new LinkedHashSet<Long>();
	
	public SelectedFragments() {
	}

	public synchronized Iterator<Long> iterator() {
		return this.ids.iterator();
	}
	
	public synchronized boolean isEmpty() {
		return this.ids.isEmpty();
	}
	
	public synchronized int size() {
		return this.ids.size();
	}
	
	public synchronized void add(Long fragmentId) {
		Assert.Arg.notNull(fragmentId, "fragmentId");
		logger.debug("Adding: " + fragmentId);
		if (this.ids.contains(fragmentId)) this.ids.remove(fragmentId);
		this.ids.add(fragmentId);
	}
	
	public synchronized void remove(Long fragmentId) {
		Assert.Arg.notNull(fragmentId, "fragmentId");
		logger.debug("Removing: " + fragmentId);
		this.ids.remove(fragmentId);
	}
	
	public synchronized void clear() {
		logger.debug("Clearing ...");
		this.ids.clear();
	}
	
	public synchronized Map<Long, String> getTitles(FragmentRepository repository) 
	throws Exception {
		Map<Long, String> titles = repository.getNames(this.ids);	// not sorted		
		Map<Long, String> sortedTitles = new LinkedHashMap<Long, String>();
		for (Long id : inReverseOrder(this.ids)) {
			if (titles.containsKey(id))
				sortedTitles.put(id, titles.get(id));
			else
				remove(id);	// Remove a missing entry
		}
		return sortedTitles;
	}
	
	public synchronized List<Fragment> getAllFragments(
		FragmentRepository repository,
		boolean eagerFetching)
	throws Exception {
		return getFragmentsByIds(repository, this.ids, eagerFetching);
	}
	
	private static List<Fragment> getFragmentsByIds(
		FragmentRepository repository,
		Collection<Long> ids,
		boolean eagerFetching) 
	throws Exception {
		FragmentsByIds query = (FragmentsByIds)repository.getQuery(FragmentsByIds.class);
		query.setIds(ids);
		query.setEagerFetching(eagerFetching);
		return query.getAll();
	}
	
	public synchronized Page<Fragment> getFragments(
		FragmentRepository repository,
		int pageSize,
		int pageIndex,
		boolean eagerFetching) 
	throws Exception {
		if (size() == 0) return PageUtils.empty(pageSize);

		Page<Long> idsInPage = PageUtils.getPage(inReverseOrder(this.ids), pageSize, pageIndex);
		
		// getByIds doesn't preserve the elements' order
		List<Fragment> fragments = getFragmentsByIds(repository, idsInPage, eagerFetching);
		List<Fragment> sorted = ModelUtils.getByIds(idsInPage, fragments);
		
		return new PageImpl<Fragment>(
			sorted, idsInPage.getPageSize(), idsInPage.getPageIndex(), size());
	}
}
