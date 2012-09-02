package marubinotto.piggydb.model;

import marubinotto.piggydb.model.auth.User;

public interface Filter extends Entity {
	
	public String getName();
	
	public void setNameByUser(String name, User user);
	
	public Classification getClassification();
	
	public void addClassificationByUser(Tag tag, User user);
	
	public void removeClassificationByUser(String tagName, User user);
	
	public Classification getExcludes();
	
	public void addExcludeByUser(Tag tag, User user);
	
	public void removeExcludeByUser(String tagName, User user);
	
	public boolean isEmpty();
}
