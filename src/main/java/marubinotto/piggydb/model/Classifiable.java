package marubinotto.piggydb.model;

import java.util.List;

public interface Classifiable extends Entity {

	public Classification getClassification();
	
	public boolean canChangeClassification(User user);
	
	public void addTagByUser(Tag tag, User user) throws InvalidTaggingException;
	
	public void addTagByUser(String name, TagRepository tagRepository, User user) 
	throws InvalidTaggingException, Exception;
	
	public boolean canAddTag(Tag tag, User user);
	
	public boolean canAddTag(String tagName, User user);
	
	public void updateTagsByUser(
		List<String> tagNames, 
		TagRepository tagRepository, 
		User user) 
	throws InvalidTaggingException, Exception;
	
	public void removeTagByUser(String name, User user);
	
	public boolean canRemoveTag(Tag tag, User user);
	
	public void removeTagsByUserClassifiedAs(String tagName, User user);
}
