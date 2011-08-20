package marubinotto.piggydb.model.fragment;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import marubinotto.piggydb.impl.InMemoryDatabase;
import marubinotto.piggydb.model.FragmentRepository;
import marubinotto.piggydb.model.Tag;
import marubinotto.piggydb.model.TagRepository;
import marubinotto.piggydb.model.User;
import marubinotto.piggydb.model.entity.RawFragment;
import marubinotto.piggydb.model.exception.InvalidTitleException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Enclosed;

@RunWith(Enclosed.class)
public class TagRoleTest {
	
	private static class TestBase {
		
		protected FragmentRepository fragmentRepository = 
			new InMemoryDatabase().getFragmentRepository();
		protected TagRepository tagRepository = fragmentRepository.getTagRepository();
		
		protected User normalUser = new User("normal");
	}
	
	public static class NewInstanceTest extends TestBase {

		private RawFragment object = new RawFragment();		
		
		@Test
		public void isNotTag() throws Exception {
			assertEquals(false, this.object.isTag());
			assertNull(this.object.getTagId());
			assertNull(this.object.asTag());
		}
		
		@Test
		public void validate() throws Exception {
			this.object.validateTagRole(this.normalUser, this.tagRepository);
		}
		
		@Test(expected = InvalidTitleException.class)
		public void newTagFragmentShouldHaveTitle() throws Exception {
			this.object.setAsTagByUser(true, this.normalUser);
			this.object.validateTagRole(this.normalUser, this.tagRepository);
		}
		
		@Test
		public void newTagRole() throws Exception {
			this.object.setTitleByUser("test", this.normalUser);
			this.object.setAsTagByUser(true, this.normalUser);
			this.object.validateTagRole(this.normalUser, this.tagRepository);
			
			Tag tag = this.object.asTag();
			
			assertEquals("test", tag.getName());
			assertEquals("normal", tag.getCreator());
		}
	}
}
