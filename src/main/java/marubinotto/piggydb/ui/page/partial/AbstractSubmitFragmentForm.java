package marubinotto.piggydb.ui.page.partial;

import java.util.HashMap;
import java.util.Map;

public abstract class AbstractSubmitFragmentForm extends AbstractSingleFragment {

	public String asTag;
	public String title;
	public String tags;
	public String content;
	public String minorEdit;
	
	public Map<String, String> fieldErrors = new HashMap<String, String>();

	protected boolean isMinorEdit() {
		return this.minorEdit != null;
	}
}
