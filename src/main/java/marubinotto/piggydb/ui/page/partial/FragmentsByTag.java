package marubinotto.piggydb.ui.page.partial;

import static marubinotto.util.CollectionUtils.set;
import static org.apache.commons.lang.StringUtils.isNotBlank;
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
		filter.getIncludes().addTag(tag);
		
		marubinotto.piggydb.model.query.FragmentsByFilter query = 
			(marubinotto.piggydb.model.query.FragmentsByFilter)getQuery(
				marubinotto.piggydb.model.query.FragmentsByFilter.class);
		query.setFilter(filter);
		if (isNotBlank(this.query)) {
		  query.setKeywords(this.query);
		  setKeywordRegex(this.query);
		}
		this.fragments = getPage(query);
		
		this.label = "<span class=\"" + this.html.miniTagIconClass(tag.getName()) + 
      "\">&nbsp;</span> " + tag.getName();
		appendKeywordSearchLabel();
	}
}
