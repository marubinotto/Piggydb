package marubinotto.piggydb.model.tag;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;
import marubinotto.piggydb.model.User;
import marubinotto.piggydb.model.entity.RawTag;
import marubinotto.piggydb.model.exception.InvalidTagNameException;

import org.junit.Test;

public class UserCreatesTagTest {
	
	@Test
	public void normalCase() throws Exception {
		RawTag object = new RawTag("test", new User("daisuke"));
		
		assertNull(object.getId());  // indicating being not yet registered
		assertEquals("test", object.getName());
		assertEquals("daisuke", object.getCreator());
		assertNull(object.getUpdater());
	}
	
	@Test(expected=InvalidTagNameException.class)
	public void invalidChars() throws Exception {
		new RawTag("--\\--", new User("daisuke"));
	}
	
	@Test(expected=InvalidTagNameException.class)
	public void tooShortName() throws Exception {
		new RawTag("a", new User("daisuke"));
	}
	
	@Test(expected=InvalidTagNameException.class)
	public void tooLongName() throws Exception {
		new RawTag("12345678901234567890123456789012345678901234567890a", new User("daisuke"));
	}
}
