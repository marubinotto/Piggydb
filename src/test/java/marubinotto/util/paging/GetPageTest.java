package marubinotto.util.paging;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class GetPageTest {

	@Test
	public void total0_size1_index0() throws Exception {
		Page<Object> page = PageUtils.getPage(createList(0), 1, 0);
		assertEquals(0, page.size());
		assertEquals(1, page.getPageSize());
		assertEquals(0, page.getPageIndex());
		assertEquals(0, page.getPageCount());
		assertEquals(0, page.getTotalSize());
		assertTrue(page.isFirstPage());
		assertTrue(page.isLastPage());
	}
	
	@Test
	public void total1_size1_index0() throws Exception {
		Page<Object> page = PageUtils.getPage(createList(1), 1, 0);
		assertEquals(1, page.size());
		assertEquals(1, page.getPageSize());
		assertEquals(0, page.getPageIndex());
		assertEquals(1, page.getPageCount());
		assertEquals(1, page.getTotalSize());
		assertTrue(page.isFirstPage());
		assertTrue(page.isLastPage());
	}
	
	@Test
	public void total1_size1_indexMinus1_indexOutOfBounds() throws Exception {
		Page<Object> page = PageUtils.getPage(createList(1), 1, -1);
		assertEquals(1, page.size());
		assertEquals(0, page.getPageIndex());
	}
	
	@Test
	public void total1_size1_index1_indexOutOfBounds() throws Exception {
		Page<Object> page = PageUtils.getPage(createList(1), 1, 1);
		assertEquals(1, page.size());
		assertEquals(0, page.getPageIndex());
	}
	
	@Test
	public void total2_size1_index0() throws Exception {
		Page<Object> page = PageUtils.getPage(createList(2), 1, 0);
		assertEquals(1, page.size());
		assertEquals(1, page.getPageSize());
		assertEquals(0, page.getPageIndex());
		assertEquals(2, page.getPageCount());
		assertEquals(2, page.getTotalSize());
		assertTrue(page.isFirstPage());
		assertFalse(page.isLastPage());
	}
	
	@Test
	public void total2_size1_index1() throws Exception {
		Page<Object> page = PageUtils.getPage(createList(2), 1, 1);
		assertEquals(1, page.size());
		assertEquals(1, page.getPageSize());
		assertEquals(1, page.getPageIndex());
		assertEquals(2, page.getPageCount());
		assertEquals(2, page.getTotalSize());
		assertFalse(page.isFirstPage());
		assertTrue(page.isLastPage());
	}
	
	@Test
	public void total2_size1_indexMinus1_indexOutOfBounds() throws Exception {
		Page<Object> page = PageUtils.getPage(createList(2), 1, -1);
		assertEquals(1, page.size());
		assertEquals(0, page.getPageIndex());		// the last page
	}
	
	@Test
	public void total2_size1_index2_indexOutOfBounds() throws Exception {
		Page<Object> page = PageUtils.getPage(createList(2), 1, 2);
		assertEquals(1, page.size());
		assertEquals(1, page.getPageIndex());		// the last page
	}
	
	@Test
	public void total3_size2_index1() throws Exception {
		Page<Object> page = PageUtils.getPage(createList(3), 2, 1);
		assertEquals(1, page.size());
		assertEquals(2, page.getPageSize());
		assertEquals(1, page.getPageIndex());
		assertEquals(2, page.getPageCount());
		assertEquals(3, page.getTotalSize());
		assertFalse(page.isFirstPage());
		assertTrue(page.isLastPage());
	}
	
	private static List<Object> createList(int size) {
		List<Object> list = new ArrayList<Object>();
		for (int i = 0; i < size; i++) {
			list.add(new Object());
		}
		return list;
	}
}
