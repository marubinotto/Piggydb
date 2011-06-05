package marubinotto.piggydb.model.tag;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;

import org.junit.Before;
import org.junit.Test;

import marubinotto.piggydb.model.User;
import marubinotto.piggydb.model.entity.RawTag;

public class UserCreatesTagTest {

	private RawTag object = new RawTag("test", new User("daisuke"));
	
	@Before
	public void given() throws Exception {
		assertNull(this.object.getId());  // indicating being not yet registered
	}
	
	@Test
	public void name() throws Exception {
		assertEquals("test", this.object.getName());
	}
	
	@Test
	public void creator() throws Exception {
		assertEquals("daisuke", this.object.getCreator());
	}
	
	@Test
	public void updater() throws Exception {
		assertNull(this.object.getUpdater());
	}
}
