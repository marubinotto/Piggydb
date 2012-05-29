package marubinotto.piggydb.ui.page.partial;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class AbstractSubmitFragmentFormTest {

	static class SubmitFragmentForm extends AbstractSubmitFragmentForm {
	}
	
	private SubmitFragmentForm object = new SubmitFragmentForm();
	
	@Test
	public void asTag() throws Exception {
		assertEquals(false, this.object.asTag());
		
		this.object.asTag = "on";
		assertEquals(true, this.object.asTag());
	}
	
	@Test
	public void isMinorEdit() throws Exception {
		assertEquals(false, this.object.isMinorEdit());
		
		this.object.minorEdit = "on";
		assertEquals(true, this.object.isMinorEdit());
	}
	
	@Test
	public void hasNoErrorByDefault() throws Exception {
		assertEquals(false, this.object.hasErrors());
	}
	
	@Test
	public void hasGlobalError() throws Exception {
		this.object.error = "error message";
		assertEquals(true, this.object.hasErrors());
	}
	
	@Test
	public void hasFieldError() throws Exception {
		this.object.fieldErrors.put("title", "too long");
		assertEquals(true, this.object.hasErrors());
	}
}
