package marubinotto.piggydb.model.entity;

import static marubinotto.util.CollectionUtils.set;

import java.util.HashSet;
import java.util.Set;

import marubinotto.piggydb.model.Tag;
import marubinotto.piggydb.model.TagRepository;
import marubinotto.piggydb.model.User;
import marubinotto.piggydb.model.exception.AuthorizationException;
import marubinotto.util.Assert;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.builder.HashCodeBuilder;

public class RawTag extends RawClassifiable implements Tag {

	private String name;
	private Long popularity;

	public RawTag() {
	}
	
	public RawTag(String name) {
		setName(name);
	}
	
	public RawTag(String name, User user) {
		super(user);
		
		ensureCanUse(new RawTag(name), user);
		
		setName(name);
		onPropertyChange(user);
	}
	
	public RawTag(String name, long id) {
		this.name = name;
		setId(new Long(id));
	}
	
	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public void setNameByUser(String name, User user) {
		Assert.Arg.notNull(name, "name");
		Assert.Arg.notNull(user, "user");
		
		if (ObjectUtils.equals(name, this.name)) return;
		
		ensureCanChange(user);		// this tag
		ensureCanUse(new RawTag(name), user);	// rename to
		
		setName(name);
		onPropertyChange(user);
	}
	
	public boolean isClassifiedAs(String name) {
		Assert.Arg.notNull(name, "name");
		if (name.equals(this.name) || getClassification().isSubordinateOf(name)) {
			return true;
		}
		else {
			return false;
		}
	}

	public Long getPopularity() {
		return this.popularity;
	}

	public void setPopularity(Long popularity) {
		this.popularity = popularity;
	}
	
	public void addPopularity() {
		this.popularity++;
	}

	public Set<Long> expandToIdsOfSubtree(TagRepository tagRepository) 
	throws Exception {
		Assert.Arg.notNull(tagRepository, "tagRepository");
		Assert.Property.requireNotNull(getId(), "id");
		
		Set<Long> ids = new HashSet<Long>();
		ids.add(getId());
		ids.addAll(tagRepository.getAllSubordinateTagIds(set(getId())));
		return ids;
	}
	
	public boolean isTrashTag() {
		return NAME_TRASH.equals(this.name);
	}
	
	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(this.name).toHashCode();
	}
	
	@Override
	public boolean equals(Object object) {
		if (!(object instanceof Tag)) {
			return false;
		}
		String targetName = ((Tag)object).getName();
		if (targetName == null && this.name == null) return true;
		if (targetName == null) return false;
		if (this.name == null) return false;
		return targetName.equals(this.name);
	}
	
	@Override
	public String toString() {
		return getId() + ":" + this.name;
	}


	//
	// Authorization
	//
	
	public static boolean canUse(Tag tag, User user) {
		Assert.Arg.notNull(tag, "tag");
		Assert.Arg.notNull(user, "user");
		
		try { ensureCanUse(tag, user); return true; } 
		catch (AuthorizationException e) { return false; }
	}
	
	private static final String CODE_NO_AUTH_FOR_TAG = "no-auth-for-tag";
	
	public static void ensureCanUse(Tag tag, User user) {
		if (user.isViewer()) {
			throw new AuthorizationException(CODE_NO_AUTH_FOR_TAG, tag.getName());
		}
		if (tag.isClassifiedAs(Tag.NAME_USER) || 
			tag.isClassifiedAs(Tag.NAME_PUBLIC) ||
			tag.isClassifiedAs(Tag.NAME_HOME) ||
			tag.isClassifiedAs(Tag.NAME_BOOKMARK)) {
			if (!user.isOwner())
				throw new AuthorizationException(CODE_NO_AUTH_FOR_TAG, tag.getName());
		}
	}

	public boolean authorizes(User user) {
		return canUse(this, user);
	}
	
	public final boolean canRename(User user) {
		Assert.Arg.notNull(user, "user");
		
		try { ensureCanChange(user); return true; } 
		catch (AuthorizationException e) { return false; }
	}

	@Override
	public void ensureCanChange(User user) throws AuthorizationException {
		super.ensureCanChange(user);
		ensureCanUse(this, user);
	}
	
	@Override
	protected void ensureCanAddTag(Tag tag, User user) throws AuthorizationException {
		super.ensureCanAddTag(tag, user);
		if (!user.isOwner() && tag.isClassifiedAs(Tag.NAME_TRASH)) 
			throw new AuthorizationException("no-auth-to-extend-trash");
	}

	@Override
	public void ensureCanDelete(User user) throws AuthorizationException {
		super.ensureCanDelete(user);
		ensureCanUse(this, user);
	}
}
