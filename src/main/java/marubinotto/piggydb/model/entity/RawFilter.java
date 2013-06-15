package marubinotto.piggydb.model.entity;

import marubinotto.piggydb.model.Filter;
import marubinotto.piggydb.model.MutableClassification;
import marubinotto.piggydb.model.Tag;
import marubinotto.piggydb.model.auth.User;
import marubinotto.piggydb.model.exception.AuthorizationException;
import marubinotto.piggydb.model.exception.InvalidTaggingException;
import marubinotto.util.Assert;

import org.apache.commons.lang.UnhandledException;

public class RawFilter extends RawEntity implements Filter {
	
	private String name; 
	private MutableClassification includes = new MutableClassification();
	private boolean and = true;
	private MutableClassification excludes = new MutableClassification();
	
	public RawFilter() {
	}
	
	public RawFilter(User user) {
		super(user);
	}
	
	public RawFilter(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public void setNameByUser(String name, User user) {
		Assert.Arg.notNull(user, "user");
		
		ensureCanChange(user);
		
		setName(name);
		onPropertyChange(user);
	}

	public MutableClassification getIncludes() {
		return this.includes;
	}
	
	public void addIncludeByUser(Tag tag, User user) {
		Assert.Arg.notNull(tag, "tag");
		Assert.Arg.notNull(tag.getId(), "tag.getId()");
		Assert.Arg.notNull(user, "user");
		
		ensureCanChange(user);
		
		try {
			this.includes.addTag(tag);
		} 
		catch (InvalidTaggingException e) {
			throw new UnhandledException(e);
		}
		onPropertyChange(user);
	}
	
	public void removeIncludeByUser(String tagName, User user) {
		Assert.Arg.notNull(tagName, "tagName");
		Assert.Arg.notNull(user, "user");
		
		ensureCanChange(user);
		
		this.includes.removeTag(tagName);
		onPropertyChange(user);
	}
	
	public boolean isAnd() {
	  return this.and;
	}
	
	public void setAnd(boolean and) {
	  this.and = and;
	}
  
  public void setAndByUser(boolean and, User user) {
    Assert.Arg.notNull(user, "user");
    
    ensureCanChange(user);
    
    this.and = and;
    onPropertyChange(user);
  }

	public MutableClassification getExcludes() {
		return this.excludes;
	}
	
	public void addExcludeByUser(Tag tag, User user) {
		Assert.Arg.notNull(tag, "tag");
		Assert.Arg.notNull(tag.getId(), "tag.getId()");
		Assert.Arg.notNull(user, "user");
		
		ensureCanChange(user);
		
		try {
			this.excludes.addTag(tag);
		} 
		catch (InvalidTaggingException e) {
			throw new UnhandledException(e);
		}
		onPropertyChange(user);
	}
	
	public void removeExcludeByUser(String tagName, User user) {
		Assert.Arg.notNull(tagName, "tagName");
		Assert.Arg.notNull(user, "user");
		
		ensureCanChange(user);
		
		this.excludes.removeTag(tagName);
		onPropertyChange(user);
	}
	
	public boolean isEmpty() {
		return this.includes.isEmpty() && this.excludes.isEmpty();
	}
	
	
	//
	// Authorization
	//
	
	@Override
	public void ensureCanChange(User user) throws AuthorizationException {
		super.ensureCanChange(user);
		if (getId() != null && user.isViewer()) {
			throw new AuthorizationException("no-auth-to-change-filter", getName());
		}
	}
}
