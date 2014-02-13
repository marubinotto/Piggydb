package marubinotto.piggydb.ui.page.partial;

import static marubinotto.util.CollectionUtils.set;
import marubinotto.piggydb.model.Filter;
import marubinotto.piggydb.model.MutableClassification;
import marubinotto.piggydb.model.Tag;
import marubinotto.piggydb.model.entity.RawFilter;

public class FragmentsByTag extends AbstractFragments {
	
	public Long id;
	public Tag tag;
	
	@Override 
	protected Filter createFilter() throws Exception {
	  if (this.id == null) return null;
    
    this.tag = getDomain().getTagRepository().get(this.id.longValue());
    if (this.tag == null) return null;
    
    this.contextTags = new MutableClassification(set(this.tag));
    
    RawFilter filter = new RawFilter();
    filter.getIncludes().addTag(this.tag);
    return filter;
	}
}
