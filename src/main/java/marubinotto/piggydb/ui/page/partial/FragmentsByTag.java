package marubinotto.piggydb.ui.page.partial;

import static marubinotto.util.CollectionUtils.set;
import marubinotto.piggydb.model.MutableClassification;
import marubinotto.piggydb.model.Tag;
import marubinotto.piggydb.model.entity.RawFilter;

public class FragmentsByTag extends AbstractFragments {
	
	public Long id;

	@Override 
	protected void setFragments() throws Exception {
		if (this.id == null) return;
		
		Tag tag = getDomain().getTagRepository().get(this.id.longValue());
		if (tag == null) return;
		
		this.contextTags = new MutableClassification(set(tag));
		
		RawFilter filter = new RawFilter();
		filter.getClassification().addTag(tag);
		this.fragments = getDomain().getFragmentRepository().findByFilter(filter, this.options);
	}
}
