package marubinotto.piggydb.model.fragment;

import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Enclosed;

import marubinotto.piggydb.impl.InMemoryDatabase;
import marubinotto.piggydb.model.TagRepository;
import marubinotto.piggydb.model.User;
import marubinotto.piggydb.model.entity.RawFragment;
import marubinotto.piggydb.model.exception.InvalidTitleException;

@RunWith(Enclosed.class)
public class TagRoleTest {
	
	public static class DefaultTest {

		private RawFragment object = new RawFragment();
		
		private User normalUser = new User("normal");
		private TagRepository tagRepository = new InMemoryDatabase().getTagRepository();
		
		@Test
		public void isNotTagByDefault() throws Exception {
			assertEquals(false, this.object.isTag());
			assertNull(this.object.getTagId());
			assertNull(this.object.asTag());
		}
		
		@Test
		public void validateDefaultState() throws Exception {
			this.object.validateTagRole(this.normalUser, this.tagRepository);
		}
		
		@Test(expected = InvalidTitleException.class)
		public void newTagFragmentShouldHaveTitle() throws Exception {
			this.object.setAsTagByUser(true, this.normalUser);
			this.object.validateTagRole(this.normalUser, this.tagRepository);
		}
	}
}
