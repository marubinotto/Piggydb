package marubinotto.piggydb.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import marubinotto.piggydb.model.entity.RawFilter;
import marubinotto.util.Assert;

public class RelatedTags {
	
	private Filter filter = new RawFilter();
	
	private Map<Long, RelatedTag> idToEntry = new HashMap<Long, RelatedTag>();
	
	public static class RelatedTag {
		public Long id;
		public Integer count;
		
		public String name;
		public Boolean isTagFragment;
		
		public RelatedTag(long id, int count) {
			this.id = id;
			this.count = count;
		}
		
		public void add(int count) {
			this.count += count;
		}
		
		public String toString() {
			return this.id + "(" + this.name  + ") × " + this.count;
		}
	}
	
	// In descending order by "count" and ascending order by "id"
	public static final Comparator<RelatedTag> ORDER_BY_COUNT = 
		new Comparator<RelatedTag>() {
			public int compare(RelatedTag o1, RelatedTag o2) {
				if (o1.count.equals(o2.count)) {
					return o1.id.compareTo(o2.id);
				}
				else {
					return o2.count.compareTo(o1.count);
				}
			}
		};

	public RelatedTags() {	
	}
	
	public void setFilter(Filter filter) {
		Assert.Arg.notNull(filter, "filter");
		this.filter = filter;
	}

	public void add(long id, int count) {
		if (this.filter.getIncludes().containsTagId(id) || 
			this.filter.getExcludes().containsTagId(id)) {
			return;
		}
		
		RelatedTag entry = this.idToEntry.get(id);
		if (entry == null) {
			entry = new RelatedTag(id, count);
			this.idToEntry.put(id, entry);
		}
		else {
			entry.add(count);
		}
	}


	private static final int DEFAULT_RETURN_SIZE_LIMIT = 30;
	private int returnSizeLimit = DEFAULT_RETURN_SIZE_LIMIT;
	
	public void setReturnSizeLimit(int returnSizeLimit) {
		this.returnSizeLimit = returnSizeLimit;
	}

	public List<RelatedTag> orderByCount(TagRepository tagRepository) throws Exception {
		List<RelatedTag> relatedTags = new ArrayList<RelatedTag>(this.idToEntry.values());
		Collections.sort(relatedTags, ORDER_BY_COUNT);
		
		if (relatedTags.size() > this.returnSizeLimit) {
			relatedTags = relatedTags.subList(0, this.returnSizeLimit);
		}
		
		setTagInfo(relatedTags, tagRepository);
		
		return relatedTags;
	}
	
	private static void setTagInfo(List<RelatedTag> relatedTags, TagRepository tagRepository) 
	throws Exception {
		if (relatedTags.isEmpty()) return;
		
		Set<Long> relatedTagIds = new HashSet<Long>();
		for (RelatedTag relatedTag : relatedTags) relatedTagIds.add(relatedTag.id);
		
		Map<Long, Tag> tags = ModelUtils.toIdMap(tagRepository.getByIds(relatedTagIds));
		for (RelatedTag relatedTag : relatedTags) {
		  Tag tag = tags.get(relatedTag.id);
		  if (tag != null) {
		    relatedTag.name = tag.getName();
		    relatedTag.isTagFragment = tag.isTagFragment();
		  }
		}
	}
}
