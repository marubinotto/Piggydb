package marubinotto.piggydb.model.entity;

import marubinotto.piggydb.model.AuthorizationException;
import marubinotto.piggydb.model.Filter;
import marubinotto.piggydb.model.InvalidTaggingException;
import marubinotto.piggydb.model.MutableClassification;
import marubinotto.piggydb.model.Tag;
import marubinotto.piggydb.model.User;
import marubinotto.util.Assert;

import org.apache.commons.lang.UnhandledException;

public class RawFilter extends RawEntity implements Filter {
	
	private String name; 

	private MutableClassification classification = new MutableClassification();
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

	public MutableClassification getClassification() {
		return this.classification;
	}
	
	public void addClassificationByUser(Tag tag, User user) {
		Assert.Arg.notNull(tag, "tag");
		Assert.Arg.notNull(tag.getId(), "tag.getId()");
		Assert.Arg.notNull(user, "user");
		
		ensureCanChange(user);
		
		try {
			this.classification.addTag(tag);
		} 
		catch (InvalidTaggingException e) {
			throw new UnhandledException(e);
		}
		onPropertyChange(user);
	}
	
	public void removeClassificationByUser(String tagName, User user) {
		Assert.Arg.notNull(tagName, "tagName");
		Assert.Arg.notNull(user, "user");
		
		ensureCanChange(user);
		
		this.classification.removeTag(tagName);
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
		return this.classification.isEmpty() && this.excludes.isEmpty();
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
