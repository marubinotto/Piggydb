package marubinotto.piggydb.ui.page.control.form;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Enclosed;

@RunWith(Enclosed.class)
public class FragmentFormUtilsTest {
	
	public static class SplitTagsStringTest {
		
		private String execute(String input) throws Exception {
			return FragmentFormUtils.splitTagsString(input).toString();
		}
	
		@Test
		public void empty() throws Exception {
			assertEquals("[]", execute(""));
		}
	
		@Test
		public void onlySeparator() throws Exception {
			assertEquals("[]", execute(","));
		}
	
		@Test
		public void one() throws Exception {
			assertEquals("[tag]", execute("tag"));
		}
	
		@Test
		public void oneWithSpaces() throws Exception {
			assertEquals("[tag]", execute(" tag "));
		}
	
		@Test
		public void oneWithSeparator() throws Exception {
			assertEquals("[tag]", execute("tag,"));
		}
	
		@Test
		public void two() throws Exception {
			assertEquals("[tag1, tag2]", execute("tag1,tag2"));
		}
	
		@Test
		public void twoWithSpaces() throws Exception {
			assertEquals("[tag1, tag2]", execute(" tag1, tag2 "));
		}
		
		@Test
		public void multipleWords() throws Exception {
			assertEquals("[This is a pen]", execute("This is a pen"));
		}
		
		@Test
		public void blankShouldBeRemoved() throws Exception {
			assertEquals("[tag1, tag2]", execute("tag1, , tag2"));
		}
	}
}
