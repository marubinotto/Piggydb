package marubinotto.piggydb.model.classification;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;

import marubinotto.piggydb.model.MutableClassification;
import marubinotto.piggydb.model.Tag;
import marubinotto.piggydb.model.entity.RawTag;

import org.junit.Test;

public class DefaultTest {

	private MutableClassification object = new MutableClassification();

	@Test
	public void sizeShouldBeZero() throws Exception {
		assertEquals(0, this.object.size());
	}
	
	@Test
	public void shouldBeEmpty() throws Exception {
		assertTrue(this.object.isEmpty());
	}
	
	@Test
	public void addTag() throws Exception {
		this.object.addTag(new RawTag("tag"));

		Iterator<Tag> iterator = this.object.getTagIterator();
		assertEquals("tag", iterator.next().getName());
	}
}
