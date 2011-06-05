package marubinotto.util;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

public class PileValueTest {
	
	private Map<String, List<String>> map;
	
	@Before
	public void given() throws Exception {
		this.map = new HashMap<String, List<String>>();
	}

	@Test
    public void putSingleValue() throws Exception {
		CollectionUtils.pileValue(this.map, "key", "value");
		
		List<String> values = this.map.get("key");
		assertEquals(1, values.size());
		assertEquals("value", values.get(0));
	}
	
	@Test
    public void putTwoValues() throws Exception {
		CollectionUtils.pileValue(this.map, "key", "value1");
		CollectionUtils.pileValue(this.map, "key", "value2");
		
		List<String> values = this.map.get("key");
		assertEquals(2, values.size());
		assertEquals("value1", values.get(0));
		assertEquals("value2", values.get(1));
	}
}
