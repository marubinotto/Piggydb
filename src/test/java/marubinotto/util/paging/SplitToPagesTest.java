package marubinotto.util.paging;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class SplitToPagesTest {

	@Test
	public void total0_size1() throws Exception {
		List<Page<Object>> pages = PageUtils.splitToPages(createList(0), 1);	
		assertEquals(0, pages.size());
	}

	@Test
	public void total1_size1() throws Exception {
		List<Page<Object>> pages = PageUtils.splitToPages(createList(1), 1);
		assertEquals(1, pages.size());
	}

	@Test
	public void total2_size1() throws Exception {
		List<Page<Object>> pages = PageUtils.splitToPages(createList(2), 1);
		assertEquals(2, pages.size());
	}

	@Test
	public void total3_size2() throws Exception {
		List<Page<Object>> pages = PageUtils.splitToPages(createList(3), 2);
		assertEquals(2, pages.size());
		assertEquals(2, pages.get(0).size());
		assertEquals(1, pages.get(1).size());
	}
		
	private static List<Object> createList(int size) {
		List<Object> list = new ArrayList<Object>();
		for (int i = 0; i < size; i++) {
			list.add(new Object());
		}
		return list;
	}
}
