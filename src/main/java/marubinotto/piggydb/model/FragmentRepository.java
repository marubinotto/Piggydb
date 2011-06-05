package marubinotto.piggydb.model;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import marubinotto.piggydb.model.FragmentsOptions.SortOption;
import marubinotto.piggydb.model.enums.FragmentField;
import marubinotto.util.paging.Page;
import marubinotto.util.time.Interval;
import marubinotto.util.time.Month;

public interface FragmentRepository extends Repository<Fragment> {
	
	public Fragment newInstance(User user);
	
	// NOTE: "updateTimestamp = false" will disable optimistic lock. 
	// That means the update might be overwritten by another, without notice
	public boolean update(Fragment fragment, boolean updateTimestamp) 
	throws BaseDataObsoleteException, Exception;
	
	public TagRepository getTagRepository();
	
	public void setFileRepository(FileRepository fileRepository);
	
	public void refreshClassifications(List<? extends Fragment> fragments) throws Exception;

	public void deleteTrashes(User user) throws Exception;

	public Page<Fragment> getFragments(FragmentsOptions options) throws Exception;
	
	public Set<Integer> getDaysOfMonth(FragmentField field, Month month) 
	throws Exception;

	public Page<Fragment> findByTime(
		Interval interval, 
		FragmentField field, 
		FragmentsOptions options)
	throws Exception;

	public Page<Fragment> findByFilter(Filter filter, FragmentsOptions options) 
	throws Exception;
	
	public RelatedTags getRelatedTags(Filter filter) throws Exception;
	
	public Page<Fragment> findByKeywords(String keywords, FragmentsOptions options)
	throws Exception;
	
	public Page<Fragment> findByUser(String userName, FragmentsOptions options)
	throws Exception;
	
	public List<Fragment> getByIds(
		Collection<Long> fragmentIds, 
		SortOption sortOption, 
		boolean eagerFetching) 
	throws Exception;
	
	public Fragment getUserFragment(String userName) throws Exception;

	public long createRelation(long from, long to, User user)
	throws NoSuchEntityException, DuplicateException, Exception;
	
	public FragmentRelation getRelation(long relationId) throws Exception;
	
	public FragmentRelation deleteRelation(long relationId, User user) throws Exception;
	
	public Long countRelations() throws Exception;
	
	public void updateChildRelationPriorities(Fragment parent, List<Long> relationOrder, User user)
	throws Exception;
}
