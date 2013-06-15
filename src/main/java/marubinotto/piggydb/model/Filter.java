package marubinotto.piggydb.model;

import marubinotto.piggydb.model.auth.User;
import marubinotto.piggydb.model.base.Entity;

public interface Filter extends Entity {
	
	public String getName();
	
	public void setNameByUser(String name, User user);
	
	public Classification getIncludes();
	
	public void addIncludeByUser(Tag tag, User user);
	
	public void removeIncludeByUser(String tagName, User user);
	
	public Classification getExcludes();
	
	public void addExcludeByUser(Tag tag, User user);
	
	public void removeExcludeByUser(String tagName, User user);
	
	public boolean isEmpty();
}
