package marubinotto.piggydb.model.authorization;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;
import marubinotto.piggydb.model.entity.RawFilter;
import marubinotto.piggydb.model.entity.RawTag;
import marubinotto.piggydb.model.exception.AuthorizationException;

import org.junit.Test;

public class FilterTest extends AuthorizationTestBase {

	private RawFilter object = new RawFilter();
	
	// Can change
	
	@Test
	public void plainUserCanChangeNewFilter() throws Exception {
		assertTrue(this.object.canChange(getPlainUser()));
	}
	
	@Test
	public void viewerCanChangeNewFilter() throws Exception {
		assertTrue(this.object.canChange(getViewer()));
	}
	
	@Test
	public void plainUserCanChangeExistingFilter() throws Exception {
		this.object.setId(1L);
		assertTrue(this.object.canChange(getPlainUser()));
	}
	
	@Test
	public void viewerCannotChangeNewFilterExistingFilter() throws Exception {
		this.object.setId(1L);
		assertFalse(this.object.canChange(getViewer()));
	}
	
	// Add classification
	
	@Test
	public void plainUserCanAddClassificationToExistingFilter() throws Exception {
		RawTag tag = new RawTag("tag");
		tag.setId(1L);
		
		this.object.addClassificationByUser(tag, getViewer());
	}

	@Test
	public void viewerCannotAddClassificationToExistingFilter() throws Exception {
		this.object.setId(1L);
		this.object.setName("hogehoge");
		
		RawTag tag = new RawTag("tag");
		tag.setId(1L);
		
		try {
			this.object.addClassificationByUser(tag, getViewer());
			fail();
		} 
		catch (AuthorizationException e) {
			assertEquals(AuthErrors.toChangeFilter(this.object), e);
		}
		assertTrue(this.object.isEmpty());
	}
}
