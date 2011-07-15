package marubinotto.piggydb.model.tag;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import marubinotto.piggydb.model.entity.RawTag;

public class DefaultTest {

	private RawTag object = new RawTag();
	
	@Test
	public void name() throws Exception {
		this.object.setName("hogehoge");
		assertEquals("hogehoge", this.object.getName());
	}
}
