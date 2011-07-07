package marubinotto.piggydb.ui.page.common;

import marubinotto.piggydb.model.Tag;
import marubinotto.piggydb.ui.page.model.FragmentTags;

import org.apache.velocity.app.FieldMethodizer;

public abstract class AbstractMainUiHtml extends AbstractTemplateWebResource {

	public FieldMethodizer tagConstants = TAG_CONSTANTS;
	private static final FieldMethodizer TAG_CONSTANTS = new FieldMethodizer(Tag.class.getName());
	public FragmentTags fragmentTagsPrototype = new FragmentTags();
	
	public AbstractMainUiHtml() {
	}
}
