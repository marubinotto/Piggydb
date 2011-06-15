package marubinotto.piggydb.ui.page.control.form;

import static marubinotto.util.CollectionUtils.set;
import static org.junit.Assert.assertEquals;
import marubinotto.piggydb.ui.page.control.form.PublicFieldForm;
import net.sf.click.control.TextField;

import org.junit.Test;

public class PublicFieldFormTest {

	static class TestForm extends PublicFieldForm {
		public TextField namelessField = new TextField();
		public TextField namedField = new TextField("hello");
		private TextField privateField = new TextField();
		
		public TestForm() {
			super();
		}
		
		public TestForm(Object listener, String method) {
			super(listener, method);
		}

		public TextField getPrivateField() {
			return this.privateField;
		}
	}

	@Test
	public void onInit() throws Exception {
		TestForm form = new TestForm();
		form.onInit();
		
		assertEquals(set("namelessField", "hello"), form.getFields().keySet());
	}
}
