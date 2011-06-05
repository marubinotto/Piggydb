package marubinotto.piggydb.model.entity;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import marubinotto.piggydb.model.AuthorizationException;
import marubinotto.piggydb.model.Entity;
import marubinotto.piggydb.model.OwnerAuth;
import marubinotto.piggydb.model.User;
import marubinotto.util.Assert;
import marubinotto.util.time.DateTime;

import org.apache.commons.lang.SerializationUtils;

public abstract class RawEntity implements Entity, Serializable {

	private Long id;
	
	private Map<String, Object> attributes = new HashMap<String, Object>();
	
	private DateTime creationDatetime = DateTime.getCurrentTime();
	private DateTime updateDatetime = this.creationDatetime;
	
	private String creator;
	private String updater;
	
	public static boolean changeableOnlyForCreator = false;
	
	public RawEntity() {
	}
	
	public RawEntity(User user) {
		Assert.Arg.notNull(user, "user");
		setCreator(user.getName());
	}
	
	public Long getId() {
		return this.id;
	}
	
	public void setId(Long id) {
		this.id = id;
	}
	
	public Map<String, Object> getAttributes() {
		return this.attributes;
	}

	public DateTime getCreationDatetime() {
		return this.creationDatetime;
	}
	
	public void setCreationDatetime(DateTime creationDatetime) {
		this.creationDatetime = creationDatetime;
	}

	public DateTime getUpdateDatetime() {
		return this.updateDatetime;
	}
	
	public void setUpdateDatetime(DateTime updateDatetime) {
		this.updateDatetime = updateDatetime;
	}

	public boolean isUpdated() {
		Assert.Property.requireNotNull(creationDatetime, "creationDatetime");
		Assert.Property.requireNotNull(updateDatetime, "updateDatetime");
		
		return !this.updateDatetime.equals(this.creationDatetime);
	}
	
	public String getCreator() {
		if (this.creator == null) return OwnerAuth.USER_NAME_OWNER;
		return this.creator;
	}

	public void setCreator(String creator) {
		this.creator = creator;
	}

	public String getUpdater() {
		if (isUpdated() && this.updater == null) {
			return OwnerAuth.USER_NAME_OWNER;
		}
		return this.updater;
	}

	public void setUpdater(String updater) {
		this.updater = updater;
	}

	public RawEntity getDeepCopy() {
		return (RawEntity)SerializationUtils.clone(this);
	}
	
	protected void onPropertyChange(User user) {
		Assert.Arg.notNull(user, "user");
		if (this.id != null) setUpdater(user.getName());
	}
	
	public String getLastUpdaterOrCreator() {
		String updater = getUpdater();
		if (updater != null) return updater;
		
		String creator = getCreator();
		if (creator != null) return creator;
		
		return null;
	}
	
	public void touch(User user, boolean ignoreAuth) {
		if (!ignoreAuth) ensureCanChange(user);
		onPropertyChange(user);
	}
	
	
	//
	// Authorization
	//   - canDoSomething: check method whether the user is permitted to do something
	//   - ensureCanDoSomething: the body of check method; to be overridden; throw an error with a code
	//
	
	public final boolean canChange(User user) {
		Assert.Arg.notNull(user, "user");
		
		try { ensureCanChange(user); return true; } 
		catch (AuthorizationException e) { return false; }
	}
	
	public final boolean canDelete(User user) {
		Assert.Arg.notNull(user, "user");
		
		try { ensureCanDelete(user); return true; } 
		catch (AuthorizationException e) { return false; }
	}
	
	public void ensureCanChange(User user) throws AuthorizationException {
		if (changeableOnlyForCreator) {
			if (!user.isOwner() && !user.getName().equals(getCreator())) 
				throw new AuthorizationException("no-auth-to-change-entity", toString());
		}
	}
	
	public void ensureCanDelete(User user) throws AuthorizationException {
		ensureCanChange(user);
	}
}
