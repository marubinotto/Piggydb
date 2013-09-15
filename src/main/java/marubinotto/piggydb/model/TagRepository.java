package marubinotto.piggydb.model;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import marubinotto.piggydb.model.auth.User;
import marubinotto.piggydb.model.base.Repository;
import marubinotto.piggydb.model.entity.RawTag;
import marubinotto.util.Assert;
import marubinotto.util.paging.Page;

public interface TagRepository extends Repository<Tag> {
	
	public Tag newInstance(String name, User user);
	
	public Tag getOrCreateTag(String name, User user) throws Exception;
	
	public void validate(Tag tag) throws Exception;
	
	public boolean containsId(Long id) throws Exception;
	
	
	// By name

	public Tag getByName(String name) throws Exception;
	
	public boolean containsName(String name) throws Exception;
	
	public Long getIdByName(String name) throws Exception;

	public Iterator<String> iterateAllTagNames() throws Exception;
	
	public List<String> getNamesLike(String criteria) throws Exception;
	
	public Page<Tag> orderByName(int pageSize, int pageIndex) throws Exception;
	
	
	// Tree
	
	public Page<Tag> getRootTags(int pageSize, int pageIndex) throws Exception;
	
	public Page<Tag> findByParentTag(long parentTagId, int pageSize, int pageIndex) 
	throws Exception;

	public Set<Long> getAllSubordinateTagIds(Set<Long> tagIds) throws Exception;

	public Set<Long> selectAllThatHaveChildren(Set<Long> tagIds) throws Exception;

	
	// Others
	
	public Page<Tag> getRecentChanges(int pageSize, int pageIndex)
	throws Exception;

	public Page<Tag> findByKeywords(String keywords, int pageSize, int pageIndex)
	throws Exception;
	
	public List<Tag> getByIds(Collection<Long> tagIds) throws Exception;

	public List<Tag> getPopularTags(int maxSize) throws Exception;
	
	public Tag getTrashTag() throws Exception;
	
	public Long countTaggings() throws Exception;
	
	
	public static abstract class Base
	extends Repository.Base<Tag, RawTag> implements TagRepository {
		
		public RawTag newRawEntity() {
			return new RawTag();
		}

		public Tag newInstance(String name, User user) {
			Assert.Arg.notNull(user, "user");
			return new RawTag(name, user);
		}
		
		public Tag getOrCreateTag(String name, User user) throws Exception {
			Assert.Arg.notNull(name, "name");
			Assert.Arg.notNull(user, "user");
			
			Tag tag = getByName(name);
			return tag != null ? tag : newInstance(name, user);
		}

		public Tag getTrashTag() throws Exception {
			return getByName(Tag.NAME_TRASH);
		}
		
		@Override
		protected void doDelete(Tag tag, User user) throws Exception {
			delete(tag.getId());
		}
		
		protected abstract void delete(Long id) throws Exception;
	}
}
