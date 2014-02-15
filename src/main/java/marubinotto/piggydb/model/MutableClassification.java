package marubinotto.piggydb.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import marubinotto.piggydb.model.exception.InvalidTaggingException;
import marubinotto.util.Assert;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class MutableClassification implements Classification, Serializable {
	
	static Log logger = LogFactory.getLog(MutableClassification.class);

	private Map<String, Tag> tags = new HashMap<String, Tag>();
		
	private Classifiable classifiable;
	private Tag targetTag;
	
	public MutableClassification() {
	}
	
	public MutableClassification(Collection<Tag> tags) throws InvalidTaggingException {
		for (Tag tag : tags) addTag(tag);
	}
	
	public MutableClassification(Classifiable classifiable) {
		Assert.Arg.notNull(classifiable, "classifiable");
		
		this.classifiable = classifiable;
		if (classifiable instanceof Tag) {
			this.targetTag = (Tag)classifiable;
		}	
	}
	
	public Classifiable getClassifiable() {
		return this.classifiable;
	}

	public Tag getTargetTag() {
		return this.targetTag;
	}

	public Tag addTag(Tag tag) throws InvalidTaggingException {
		Assert.Arg.notNull(tag, "tag");
		Assert.Arg.notNull(tag.getName(), "tag.getName()");
		
		if (this.targetTag != null) {
			if (tag.equals(this.targetTag)) {
				throw new InvalidTaggingException();
			}
			if (tag.getClassification().isSubordinateOf(this.targetTag.getName())) {
				throw new InvalidTaggingException();
			}
		}
		
		Tag removedTag = null;
		for (Iterator<Tag> i = getTagIterator(); i.hasNext();) {
			Tag memberTag = i.next();
			if (memberTag.getClassification().isInSameHierarchyOf(tag)) {
				removeTag(memberTag.getName());
				removedTag = memberTag;
			}
		}		
		this.tags.put(tag.getName(), tag);
		return removedTag;
	}
	
	public void syncWith(Classification classification) 
	throws InvalidTaggingException {
		Assert.Arg.notNull(classification, "classification");
		clear();
		for (Tag tag : classification) addTag(tag);
	}
	
	public void removeTag(String name) {
		Assert.Arg.notNull(name, "name");
		this.tags.remove(name);
	}

	public int size() {
		return this.tags.size();
	}
	
	public boolean isEmpty() {
		return this.tags.isEmpty();
	}
	
	public boolean containsTagName(String name) {
		Assert.Arg.notNull(name, "name");
		return this.tags.containsKey(name);
	}
	
	public boolean containsTagId(long tagId) {
		for (Tag tag : this.tags.values()) {
			if (tag.getId() != null && tag.getId().longValue() == tagId) {
				return true;
			}
		}
		return false;
	}
	
	public boolean containsAny(Collection<Long> tagIds) {
		Assert.Arg.notNull(tagIds, "tagIds");
		for (Long tagId : tagIds) {
			if (containsTagId(tagId)) {
				return true;
			}
		}
		return false;
	}
	
	public Tag getTag(String name) {
		Assert.Arg.notNull(name, "name");
		return this.tags.get(name);
	}
	
	public Iterator<Tag> getTagIterator() {
		SortedSet<Tag> sortedTags = new TreeSet<Tag>(Tag.TAG_NAME_COMPARATOR);
		sortedTags.addAll(this.tags.values());
		return sortedTags.iterator();
	}
	
	public Iterator<Tag> iterator() {
		return getTagIterator();
	}
	
	public Set<String> getTagNames() {
		return this.tags.keySet();
	}
	
	public String toCommaSeparated() {
	  StringBuilder tags = new StringBuilder();
	  for (String tagName : getTagNames()) {
	    if (tags.length() > 0) tags.append(", ");
	    tags.append(tagName);
	  }
	  return tags.toString();
	}
	
	public Collection<Tag> getTags() {
		return this.tags.values();
	}
	
	public boolean isSubordinateOf(String name) {
		Assert.Arg.notNull(name, "name");	
		if (containsTagName(name)) {
			return true;
		}
		else {
			for (Iterator<Tag> i = getTagIterator(); i.hasNext();) {
				if (i.next().getClassification().isSubordinateOf(name)) {
					return true;
				}
			}
		}
		return false;
	}
	
	public boolean isInSameHierarchyOf(Tag tag) {
		Assert.Arg.notNull(tag, "tag");
		Assert.Arg.notNull(tag.getName(), "tag.getName()");
		
		if (this.targetTag != null && tag.equals(this.targetTag)) {
			return true;
		}
		if (isSubordinateOf(tag.getName())) {
			return true;
		}
		if (this.targetTag != null && 
				tag.getClassification().isSubordinateOf(this.targetTag.getName())) {
			return true;
		}
		return false;
	}
	
	public void refreshEachTag(TagRepository tagRepository) throws Exception {
		Assert.Arg.notNull(tagRepository, "tagRepository");
			
		for (Tag tag : this) {
			if (tag.getId() != null) {
				removeTag(tag.getName());
				Tag latest = tagRepository.get(tag.getId().longValue());
				if (latest != null) addTag(latest);
			}
		}
	}
	
	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		toStringRecursively(this, buffer);
		return buffer.toString();
	}
	
	private static void toStringRecursively(Classification classification, StringBuffer buffer) {
		if (classification.size() == 0) return;
		
		if (buffer.length() > 0) buffer.append(" ");
		buffer.append("(");
		boolean first = true;
		for (Tag tag : classification) {
			if (first) first = false; else buffer.append(", ");
			buffer.append(tag.getName());
			toStringRecursively(tag.getClassification(), buffer);
		}
		buffer.append(")");
	}
	
	public void clear() {
		this.tags.clear();
	}
	
	public List<Set<Long>> expandEach(TagRepository tagRepository) 
	throws Exception {
		Assert.Arg.notNull(tagRepository, "tagRepository");
		
		List<Set<Long>> expandedTags = new ArrayList<Set<Long>>();
		for (Iterator<Tag> i = getTagIterator(); i.hasNext();) {
			expandedTags.add(i.next().expandToIdsOfSubtree(tagRepository));
		}
		return expandedTags;
	}
	
	public Set<Long> expandAll(TagRepository tagRepository) 
	throws Exception {
		Assert.Arg.notNull(tagRepository, "tagRepository");
		
		Set<Long> tagIds = new HashSet<Long>();
		for (Tag tag : this.tags.values()) {
			if (tag.getId() == null) {
				throw new IllegalStateException("Cannot expand for missing ID: " + tag);
			}
			tagIds.add(tag.getId());
		}
		tagIds.addAll(tagRepository.getAllSubordinateTagIds(tagIds));
		return tagIds;
	}
	
	public boolean isClassifiedByAll(List<Set<Long>> expandedTags) {
		Assert.Arg.notNull(expandedTags, "expandedTags");
		
		for (Set<Long> tagTree : expandedTags) {
			if (!containsAny(tagTree)) {
				return false;
			}
		}
		return true;
	}
}
