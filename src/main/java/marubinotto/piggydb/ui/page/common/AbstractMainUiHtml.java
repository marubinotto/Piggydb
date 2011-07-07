package marubinotto.piggydb.ui.page.common;

import java.util.Map;

import marubinotto.piggydb.model.Tag;
import marubinotto.piggydb.ui.page.model.FragmentTags;
import marubinotto.piggydb.ui.page.model.SelectedFragments;

import org.apache.velocity.app.FieldMethodizer;

public abstract class AbstractMainUiHtml extends AbstractTemplateWebResource {

	public FieldMethodizer tagConstants = TAG_CONSTANTS;
	public FragmentTags fragmentTagsPrototype = new FragmentTags();
	public Map<Long, String> selectedFragments;
	
	private static final FieldMethodizer TAG_CONSTANTS = new FieldMethodizer(Tag.class.getName());
	
	public AbstractMainUiHtml() {
	}
	
	protected void setSelectedFragments() throws Exception {
		SelectedFragments fragments = getSelectedFragments();
		if (!fragments.isEmpty()) {
			this.selectedFragments = fragments.getTitles(getDomain().getFragmentRepository());
		}
	}
}
