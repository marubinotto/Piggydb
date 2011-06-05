package marubinotto.piggydb.model.repository;

import java.util.List;

import marubinotto.piggydb.model.BaseDataObsoleteException;
import marubinotto.piggydb.model.DuplicateException;
import marubinotto.piggydb.model.FileRepository;
import marubinotto.piggydb.model.Fragment;
import marubinotto.piggydb.model.FragmentRelation;
import marubinotto.piggydb.model.FragmentRepository;
import marubinotto.piggydb.model.NoSuchEntityException;
import marubinotto.piggydb.model.User;
import marubinotto.piggydb.model.entity.RawFragment;
import marubinotto.util.Assert;

public abstract class AbstractFragmentRepository 
extends AbstractRepository<Fragment, RawFragment> implements FragmentRepository {

	protected FileRepository fileRepository;

	public RawFragment newRawEntity() {
		return new RawFragment();
	}

	public Fragment newInstance(User user) {
		Assert.Arg.notNull(user, "user");
		return new RawFragment(user);
	}

	public void setFileRepository(FileRepository fileRepository) {
		this.fileRepository = fileRepository;
	}

	public FileRepository getFileRepository() {
		return fileRepository;
	}
	
	public final boolean update(Fragment fragment) 
	throws BaseDataObsoleteException, Exception {
		return update(fragment, true); 
	}
	
	public final long createRelation(long from, long to, User user)
	throws NoSuchEntityException, DuplicateException, Exception {
		Assert.Arg.notNull(user, "user");
		Assert.require(from != to, "from != to");
		
		FragmentRelation.ensureCanCreate(user);
		return doCreateRelation(from, to, user);
	}
	
	protected abstract long doCreateRelation(long from, long to, User user)
	throws NoSuchEntityException, DuplicateException, Exception;
	
	public final FragmentRelation deleteRelation(long relationId, User user) throws Exception {
		Assert.Arg.notNull(user, "user");
		
		FragmentRelation relation = getRelation(relationId);
		if (relation == null) return null;
		
		relation.ensureCanDelete(user);
		doDeleteRelation(relation.getId());
		
		return relation;
	}
	
	protected abstract void doDeleteRelation(long relationId) throws Exception;
	
	public final void updateChildRelationPriorities(Fragment parent, List<Long> relationOrder, User user)
	throws Exception {
		Assert.Arg.notNull(parent, "parent");
		Assert.Arg.notNull(relationOrder, "relationOrder");
		Assert.Arg.notNull(user, "user");
		
		parent.ensureCanChange(user);
		
		if (relationOrder.isEmpty()) return;
		doUpdateChildRelationPriorities(relationOrder);
	}
	
	protected abstract void doUpdateChildRelationPriorities(List<Long> relationOrder)
	throws Exception;
}
