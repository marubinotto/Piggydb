package marubinotto.util;

import static junit.framework.Assert.assertEquals;
import static marubinotto.util.CollectionUtils.*;

import java.util.Collection;
import java.util.Map;

import org.junit.Test;

public class CollectionUtilsTest {

	@Test
	@SuppressWarnings("unchecked")
	public void mapBuilder() throws Exception {
		Map map = 
			map("daisuke", map("gender", "male")).
			map("akane", map("gender", "female"));
		System.out.println(map);
	}
	
	static class Entity {
		public Integer id;
		public String name;
		public Entity(Integer id, String name) {
			this.id = id;
			this.name = name;
		}
	}
	
	@Test
	public void makeMap() throws Exception {
		Collection<Entity> entities = list(new Entity(1, "Daisuke"), new Entity(2, "Akane"));
		
		Map<Integer, Entity> map = CollectionUtils.makeMap(entities, "id");
		
		assertEquals(2, map.size());
		assertEquals("Daisuke", map.get(1).name);
		assertEquals("Akane", map.get(2).name);
	}
}
