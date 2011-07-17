package marubinotto.piggydb.fixture;

import marubinotto.piggydb.model.entity.RawFragment;
import marubinotto.piggydb.model.entity.RawTag;

public class EntityFixtures {

	public static RawTag newTagWithTags(String tagName, String ... parentTags) 
	throws Exception {
		RawTag tag = new RawTag(tagName);
		for (String parent : parentTags) {
			tag.getMutableClassification().addTag(new RawTag(parent));
		}
		return tag;
	}
	
	public static RawFragment newFragmentWithTitle(String title) {
		RawFragment fragment = new RawFragment();
		fragment.setTitle(title);
		return fragment;
	}
	
	public static RawFragment newFragmentWithTags(String ... tagNames)
	throws Exception {
		return newFragmentWithTitleAndTags(null, tagNames);
	}
	
	public static RawFragment newFragmentWithTitleAndTags(
		String title, 
		String ... tagNames)
	throws Exception {
		RawFragment fragment = newFragmentWithTitle(title);
		for (String tagName : tagNames) {
			fragment.getMutableClassification().addTag(new RawTag(tagName));
		}
		return fragment;
	}
	
	public static RawTag tagWithId(String name, long id) {
		RawTag tag = new RawTag(name);
		tag.setId(id);
		return tag;
	}
}
