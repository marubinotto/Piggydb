package marubinotto.util.paging;

import static org.junit.Assert.*;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class PageImplTest {

	private PageImpl<String> object;

// Constructor
	
	@Test
	public void shouldConstructObjectWithSomeProperties() throws Exception {
		int pageSize = 1;
		int pageIndex = 0;
		int totalSize = 0;
		this.object = new PageImpl<String>(
			new ArrayList<String>(), pageSize, pageIndex, totalSize);
		
		assertEquals(pageSize, this.object.getPageSize());
		assertEquals(pageIndex, this.object.getPageIndex());
		assertEquals(totalSize, this.object.getTotalSize());
	}

// InternalList
	
	@Test
	public void shouldProvideAccessToInternalListSeamlessly() throws Exception {
		List<String> list = new ArrayList<String>();
		list.add("Hello");
		
		this.object = new PageImpl<String>(list, 1, 0, list.size());
		
		assertEquals(1, this.object.size());
		assertEquals("Hello", this.object.get(0));
	}

// pageCount
	
	@Test
	public void pageCountShouldBeZeroWhenTotalSizeIsZero() 
	throws Exception {
		int pageSize = 1;
		int totalSize = 0;
		this.object = new PageImpl<String>(
			new ArrayList<String>(), pageSize, 0, totalSize);
		
		assertEquals(0, this.object.getPageCount());
	}
	
	@Test
	public void pageCountShouldBeOneWhen_pageSize2_total1()
	throws Exception {
		int pageSize = 2;
		int totalSize = 1;
		this.object = new PageImpl<String>(
			new ArrayList<String>(), pageSize, 0, totalSize);
		
		assertEquals(1, this.object.getPageCount());
	}
	
	@Test
	public void pageCountShouldBeOneWhen_pageSize2_total2()
	throws Exception {
		int pageSize = 2;
		int totalSize = 2;
		this.object = new PageImpl<String>(
			new ArrayList<String>(), pageSize, 0, totalSize);
		
		assertEquals(1, this.object.getPageCount());
	}
	
	@Test
	public void pageCountShouldBeTwoWhen_pageSize2_total3()
	throws Exception {
		int pageSize = 2;
		int totalSize = 3;
		this.object = new PageImpl<String>(
			new ArrayList<String>(), pageSize, 0, totalSize);
		
		assertEquals(2, this.object.getPageCount());
	}

// FirstPage
	
	@Test
	public void shouldBeFirstPageWhenPageIndexIsZero() throws Exception {
		int pageIndex = 0;
		this.object = new PageImpl<String>(
			new ArrayList<String>(), 1, pageIndex, 0);
		
		assertTrue(this.object.isFirstPage());
	}
	
// LastPage
	
	@Test
	public void shouldBeLastPageWhenTotalSizeIsZero() throws Exception {
		int pageSize = 1;
		int pageIndex = 0;
		int totalSize = 0;
		this.object = new PageImpl<String>(
			new ArrayList<String>(), pageSize, pageIndex, totalSize);
		
		assertTrue(this.object.isLastPage());
	}
	
	@Test
	public void shouldBeLastPageWhenPageIndexIsLast() throws Exception {
		int pageSize = 1;
		int pageIndex = 1;
		int totalSize = 2;
		this.object = new PageImpl<String>(
			new ArrayList<String>(), pageSize, pageIndex, totalSize);
		
		assertTrue(this.object.isLastPage());
	}
	
// indexOfFirstElement
	
	@Test
	public void indexOfFirstElementShouldBeMinusOneWhenNoElements()
	throws Exception {
		this.object = new PageImpl<String>(
			new ArrayList<String>(), 1, 0, 0);
		
		assertEquals(-1, this.object.getIndexOfFirstElement());
	}
	
// indexOfLastElement
	
	@Test
	public void indexOfLastElementShouldBeMinusOneWhenNoElements()
	throws Exception {
		this.object = new PageImpl<String>(
			new ArrayList<String>(), 1, 0, 0);
		
		assertEquals(-1, this.object.getIndexOfLastElement());
	}
}
