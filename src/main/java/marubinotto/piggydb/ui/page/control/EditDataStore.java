package marubinotto.piggydb.ui.page.control;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import marubinotto.piggydb.model.Fragment;
import net.sf.click.Context;

/**
 * - A user session -> edit sessions (editSessionId is stored in hidden field)
 * - editSessionId -> an edit data
 */
public class EditDataStore implements Serializable {
	
	public Map<String, Fragment> data = new HashMap<String, Fragment>();
	
	public static EditDataStore getStore(Context context) {
		EditDataStore store = (EditDataStore)
			context.getSessionAttribute(EditDataStore.class.getName());
		if (store == null) {
			store = new EditDataStore();
			context.setSessionAttribute(EditDataStore.class.getName(), store);
		}
		return store;
	}
}