package marubinotto.piggydb.ui.page.html;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import marubinotto.piggydb.ui.page.html.FragmentsView;

import org.junit.Test;

public class FragmentsViewTest {
	
	private FragmentsView object = new FragmentsView("test");
	
	@Test
	public void scale_0() throws Exception {
		this.object.setScale(0);
		
		assertEquals(0, this.object.getScale());
		
		assertEquals("multicolumn", this.object.viewType);
		assertEquals(200, this.object.getPageSize());
		assertFalse(this.object.needsEagerFetching());
		
		assertEquals(120, this.object.columnWidth);
		assertTrue(this.object.compactColumn);
	}
	
	@Test
	public void scale_400() throws Exception {
		this.object.setScale(400);
		
		assertEquals(400, this.object.getScale());
		
		assertEquals("multicolumn", this.object.viewType);
		assertEquals(100, this.object.getPageSize());
		assertFalse(this.object.needsEagerFetching());
		
		assertEquals(600, this.object.columnWidth);
		assertFalse(this.object.compactColumn);
	}
	
	@Test
	public void scale_500() throws Exception {
		this.object.setScale(500);
		
		assertEquals(500, this.object.getScale());
		
		assertEquals("tree", this.object.viewType);
		assertEquals(50, this.object.getPageSize());
		assertTrue(this.object.needsEagerFetching());
	}
	
	@Test
	public void scale_1000() throws Exception {
		this.object.setScale(1000);
		
		assertEquals(1000, this.object.getScale());
		
		assertEquals("detail", this.object.viewType);
		assertEquals(10, this.object.getPageSize());
		assertTrue(this.object.needsEagerFetching());
	}
}
