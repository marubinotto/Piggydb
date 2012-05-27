package marubinotto.piggydb.ui.page.partial;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class FragmentsViewTest {
	
	private FragmentsView object = new FragmentsView("test");
	
	@Test
	public void scale_0() throws Exception {
		this.object.setScale(0);
		
		assertEquals(0, this.object.getScale());
		
		assertEquals("multicolumn", this.object.viewType);
		assertEquals(200, this.object.getPageSize());
		assertEquals(false, this.object.needsEagerFetching());
		
		assertEquals(120, this.object.columnWidth);
		assertEquals(true, this.object.compactColumn);
	}
	
	@Test
	public void scale_400() throws Exception {
		this.object.setScale(400);
		
		assertEquals(400, this.object.getScale());
		
		assertEquals("multicolumn", this.object.viewType);
		assertEquals(100, this.object.getPageSize());
		assertEquals(false, this.object.needsEagerFetching());
		
		assertEquals(600, this.object.columnWidth);
		assertEquals(false, this.object.compactColumn);
	}
	
	@Test
	public void scale_500() throws Exception {
		this.object.setScale(500);
		
		assertEquals(500, this.object.getScale());
		
		assertEquals("tree", this.object.viewType);
		assertEquals(50, this.object.getPageSize());
		assertEquals(true, this.object.needsEagerFetching());
		assertEquals(true, this.object.lightNode);
	}
	
	@Test
	public void scale_700() throws Exception {
		this.object.setScale(700);
		
		assertEquals(700, this.object.getScale());
		
		assertEquals("tree", this.object.viewType);
		assertEquals(50, this.object.getPageSize());
		assertEquals(true, this.object.needsEagerFetching());
		assertEquals(false, this.object.lightNode);
	}
	
	@Test
	public void scale_1000() throws Exception {
		this.object.setScale(1000);
		
		assertEquals(1000, this.object.getScale());
		
		assertEquals("detail", this.object.viewType);
		assertEquals(10, this.object.getPageSize());
		assertEquals(true, this.object.needsEagerFetching());
	}
}
