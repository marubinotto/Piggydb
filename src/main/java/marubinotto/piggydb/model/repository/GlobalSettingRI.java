package marubinotto.piggydb.model.repository;

import java.util.HashMap;
import java.util.Map;

import marubinotto.piggydb.model.GlobalSetting;

public class GlobalSettingRI extends GlobalSetting {
	
	private Map<String, String> entries = new HashMap<String, String>();
	
	public void put(String name, String value) throws Exception {
		this.entries.put(name, value);
	}
	
	public String get(String name) throws Exception {
		return this.entries.get(name);
	}
}
