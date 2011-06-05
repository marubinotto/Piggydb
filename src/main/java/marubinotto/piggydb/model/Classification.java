package marubinotto.piggydb.model;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface Classification extends Iterable<Tag> {
	
	public Classifiable getClassifiable();

	public int size();
	
	public boolean isEmpty();
	
	public boolean containsTagName(String name);
	
	public boolean containsTagId(long tagId);
	
	public boolean containsAny(Collection<Long> tagIds);
	
	public Tag getTag(String name);
	
	public Set<String> getTagNames();
	
	public Collection<Tag> getTags();
	
	public boolean isSubordinateOf(String name);
	
	public boolean isInSameHierarchyOf(Tag tag);
	
	public List<Set<Long>> expandEach(TagRepository tagRepository) 
	throws Exception;
	
	public Set<Long> expandAll(TagRepository tagRepository) 
	throws Exception;
	
	public boolean isClassifiedByAll(List<Set<Long>> expandedTags);
}
