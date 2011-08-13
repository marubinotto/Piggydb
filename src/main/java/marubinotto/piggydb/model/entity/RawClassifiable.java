package marubinotto.piggydb.model.entity;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import marubinotto.piggydb.model.Classifiable;
import marubinotto.piggydb.model.Classification;
import marubinotto.piggydb.model.MutableClassification;
import marubinotto.piggydb.model.Tag;
import marubinotto.piggydb.model.TagRepository;
import marubinotto.piggydb.model.User;
import marubinotto.piggydb.model.exception.AuthorizationException;
import marubinotto.piggydb.model.exception.InvalidTaggingException;
import marubinotto.util.Assert;

public abstract class RawClassifiable extends RawEntity implements Classifiable {

	private MutableClassification classification = new MutableClassification(this);

	public RawClassifiable() {
	}
	
	public RawClassifiable(User user) {
		super(user);
	}

	public Classification getClassification() {
		return this.classification;
	}
	
	public MutableClassification getMutableClassification() {
		return this.classification;
	}
	
	
	//
	// Authorization
	//
	
	public final boolean canChangeClassification(User user) {
		Assert.Arg.notNull(user, "user");
		
		try { ensureCanChangeClassification(user); return true; } 
		catch (AuthorizationException e) { return false; }
	}
	
	public final boolean canAddTag(Tag tag, User user) {
		Assert.Arg.notNull(tag, "tag");
		Assert.Arg.notNull(user, "user");
		
		try { ensureCanAddTag(tag, user); return true; } 
		catch (AuthorizationException e) { return false; }
	}
	
	public final boolean canAddTag(String tagName, User user) {
		Assert.Arg.notNull(tagName, "tagName");
		Assert.Arg.notNull(user, "user");
		
		return canAddTag(new RawTag(tagName), user);
	}
	
	public final boolean canRemoveTag(Tag tag, User user) {
		Assert.Arg.notNull(tag, "tag");
		Assert.Arg.notNull(user, "user");
		
		try { ensureCanRemoveTag(tag, user); return true; } 
		catch (AuthorizationException e) { return false; }
	}

	protected void ensureCanChangeClassification(User user) throws AuthorizationException {
		ensureCanChange(user);
	}
	
	protected void ensureCanAddTag(Tag tag, User user) throws AuthorizationException {
		ensureCanChangeClassification(user);
		RawTag.ensureCanUse(tag, user);
	}
	
	protected void ensureCanRemoveTag(Tag tag, User user) throws AuthorizationException {
		ensureCanChangeClassification(user);
		RawTag.ensureCanUse(tag, user);
	}
	
	
	//
	// Update
	//
	
	public void addTagByUser(Tag tag, User user) throws InvalidTaggingException {
		Assert.Arg.notNull(tag, "tag");
		Assert.Arg.notNull(user, "user");
		
		// Avoid an auth error when the classification won't be changed
		if (this.classification.containsTagName(tag.getName())) return;
		
		ensureCanAddTag(tag, user);
		
		this.classification.addTag(tag);
		onPropertyChange(user);
	}
	
	public final void addTagByUser(String name, TagRepository tagRepository, User user) 
	throws InvalidTaggingException, Exception {
		Assert.Arg.notNull(name, "name");
		Assert.Arg.notNull(tagRepository, "tagRepository");
		Assert.Arg.notNull(user, "user");
		
		addTagByUser(tagRepository.getOrCreateTag(name, user), user);
	}

	public final void updateTagsByUser(
		Collection<String> tagNames, 
		TagRepository tagRepository, 
		User user) 
	throws InvalidTaggingException, Exception {
		Assert.Arg.notNull(tagNames, "tagNames");
		Assert.Arg.notNull(tagRepository, "tagRepository");
		Assert.Arg.notNull(user, "user");
		
		Classification newTags = selectMostConcreteTags(tagNames, tagRepository, user);
		Set<String> originalTags = new HashSet<String>(this.classification.getTagNames());
		for (Tag newTag : newTags) {
			addTagByUser(newTag, user);
			originalTags.remove(newTag.getName());
		}
		for (String tagToRemove : originalTags) {	// the remains should be deleted
			removeTagByUser(tagToRemove, user);
		}
	}
	
	public static MutableClassification selectMostConcreteTags(
		Collection<String> tagNames, 
		TagRepository tagRepository, 
		User user) 
	throws Exception {
		MutableClassification classification = new MutableClassification();
		for (String name : tagNames) {
			if (!classification.isSubordinateOf(name)) {
				classification.addTag(tagRepository.getOrCreateTag(name, user));
			}
		}
		return classification;
	}
	
	public void removeTagByUser(String name, User user) {
		Assert.Arg.notNull(name, "name");
		Assert.Arg.notNull(user, "user");
		
		Tag tag = this.classification.getTag(name);
		if (tag == null) return;
		
		ensureCanRemoveTag(tag, user);
		
		this.classification.removeTag(name);
		onPropertyChange(user);
	}
	
	public void removeTagsByUserClassifiedAs(String tagName, User user) {
		Assert.Arg.notNull(tagName, "tagName");
		Assert.Arg.notNull(user, "user");
		
		removeTagByUser(tagName, user);
		for (Tag tag : this.classification) {
			if (tag.getClassification().isSubordinateOf(tagName)) 
				removeTagByUser(tag.getName(), user);
		}
	}
	
	public void syncClassificationWith(Classifiable classifiable) 
	throws InvalidTaggingException {
		Assert.Arg.notNull(classifiable, "classifiable");
		getMutableClassification().syncWith(classifiable.getClassification());
	}
	
	public static  void refreshEachTag(
		List<? extends RawClassifiable> classifiables, 
		TagRepository tagRepository) 
	throws Exception {
		for (RawClassifiable classifiable : classifiables) {
			classifiable.getMutableClassification().refreshEachTag(tagRepository);
		}
	}
}
