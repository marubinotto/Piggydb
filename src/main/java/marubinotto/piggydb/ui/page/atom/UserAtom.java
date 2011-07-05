package marubinotto.piggydb.ui.page.atom;

import java.util.List;

import marubinotto.piggydb.model.Fragment;
import marubinotto.piggydb.ui.page.AbstractBorderPage;

import org.apache.commons.codec.net.URLCodec;

public class UserAtom extends AbstractAtom {

	public String name;
	
	@Override
	protected void setFeedInfo() throws Exception {
		super.setFeedInfo();
		
		if (this.name == null) return;
		
		this.name = modifyIfGarbledByTomcat(this.name);
		
		String encodedName = new URLCodec().encode(this.name, CHAR_ENCODING);
		this.feedId = this.feedId + PARAM_PREFIX_IN_ID + encodedName;
		appendQueryToUrls("?name=" + encodedName);
		
		this.feedTitle  = this.feedTitle + AbstractBorderPage.HTML_TITLE_SEP + this.name;
	}
	
	@Override
	protected List<Fragment> getFragments() throws Exception {
		if (this.name == null) return null;
		return getFragmentRepository().findByUser(this.name, this.fragmentsOptions);
	}
}
