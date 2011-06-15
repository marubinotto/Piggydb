package marubinotto.piggydb.ui.page.model;

import java.util.ArrayList;
import java.util.List;

import marubinotto.piggydb.model.Classification;
import marubinotto.piggydb.model.Tag;
import marubinotto.util.Assert;

public class FragmentTags {

	public List<Tag> toShow = new ArrayList<Tag>();
	public List<Tag> toHide = new ArrayList<Tag>();
	
	public FragmentTags newInstance(Classification fragmentTags, Classification contextTags) {
		Assert.Arg.notNull(fragmentTags, "fragmentTags");
		
		FragmentTags newInstance = new FragmentTags();
		for (Tag tag : fragmentTags) {
			if (contextTags != null && contextTags.containsTagName(tag.getName())) 
				newInstance.toHide.add(tag);
			else 
				newInstance.toShow.add(tag);
		}
		return newInstance;
	}
}
