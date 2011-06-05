package marubinotto.piggydb.presentation.page.control.form;

import static org.junit.Assert.assertEquals;
import marubinotto.piggydb.presentation.page.control.form.FragmentForm;

import org.junit.Test;

public class SplitIntoTagNamesTest {
	
	private String execute(String input) throws Exception {
		return FragmentForm.splitIntoTagNames(input).toString();
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
