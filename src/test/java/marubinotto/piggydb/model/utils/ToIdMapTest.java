package marubinotto.piggydb.model.utils;

import static marubinotto.piggydb.fixture.EntityFixtures.newFragmentWithTitle;
import static marubinotto.util.CollectionUtils.list;
import static org.junit.Assert.assertEquals;

import java.util.Map;

import marubinotto.piggydb.model.Fragment;
import marubinotto.piggydb.model.ModelUtils;
import marubinotto.piggydb.model.entity.RawFragment;

import org.junit.Test;

public class ToIdMapTest {

	@Test
	public void oneEntity() throws Exception {
		RawFragment fragment = newFragmentWithTitle("fragment1");
		fragment.setId(1L);
		
		Map<Long, Fragment> map = ModelUtils.<Fragment>toIdMap(list(fragment));
		
		assertEquals(1, map.size());
		assertEquals("fragment1", map.get(1L).getTitle());
	}

	@Test
	public void twoEntities() throws Exception {
		RawFragment fragment1 = newFragmentWithTitle("fragment1");
		fragment1.setId(1L);
		RawFragment fragment2 = newFragmentWithTitle("fragment2");
		fragment2.setId(2L);
		
		Map<Long, Fragment> map = ModelUtils.<Fragment>toIdMap(list(fragment1, fragment2));
		
		assertEquals(2, map.size());
		assertEquals("fragment1", map.get(1L).getTitle());
		assertEquals("fragment2", map.get(2L).getTitle());
	}
}
