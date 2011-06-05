package marubinotto.piggydb.model.tag;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import marubinotto.piggydb.model.entity.RawTag;

public class DefaultTest {

	private RawTag object = new RawTag("idiot");
	
	@Test
	public void getName() throws Exception {
		assertEquals("idiot", this.object.getName());
	}
}
