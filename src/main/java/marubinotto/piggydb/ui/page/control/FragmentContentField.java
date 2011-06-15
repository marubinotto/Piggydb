package marubinotto.piggydb.ui.page.control;

import net.sf.click.control.TextArea;

public class FragmentContentField extends TextArea {
	
	public static final String CLASS = "fragment-content";
	
	public FragmentContentField(String name, boolean required) {
		super(name, required);
		setAttribute("class", CLASS);
	}

	@Override
	protected String getRequestValue() {
        return getContext().getRequestParameter(getName());
    }
}
