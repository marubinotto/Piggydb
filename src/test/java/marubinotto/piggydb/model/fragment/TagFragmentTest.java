package marubinotto.piggydb.model.fragment;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import marubinotto.piggydb.impl.InMemoryDatabase;
import marubinotto.piggydb.model.Fragment;
import marubinotto.piggydb.model.FragmentRepository;
import marubinotto.piggydb.model.Tag;
import marubinotto.piggydb.model.TagRepository;
import marubinotto.piggydb.model.User;
import marubinotto.piggydb.model.entity.RawFragment;
import marubinotto.piggydb.model.exception.DuplicateException;
import marubinotto.piggydb.model.exception.InvalidTagNameException;
import marubinotto.piggydb.model.exception.InvalidTaggingException;
import marubinotto.piggydb.model.exception.InvalidTitleException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Enclosed;

@RunWith(Enclosed.class)
public class TagFragmentTest {
	
	private static class TestBase {
		
		protected FragmentRepository fragmentRepository = 
			new InMemoryDatabase().getFragmentRepository();
		protected TagRepository tagRepository = fragmentRepository.getTagRepository();
		
		protected User normalUser = new User("normal");
	}
	
	public static class NewInstanceTest extends TestBase {

		private RawFragment object = new RawFragment();		
		
		@Test
		public void isNotTagByDefault() throws Exception {
			assertEquals(false, this.object.isTag());
			assertNull(this.object.getTagId());
			assertNull(this.object.asTag());
		}
		
		@Test
		public void validateTagRole() throws Exception {
			this.object.validateTagRole(this.normalUser, this.tagRepository);
		}
		
		@Test(expected = InvalidTitleException.class)
		public void newTagFragmentShouldHaveTitle() throws Exception {
			this.object.setAsTagByUser(true, this.normalUser);
			this.object.validateTagRole(this.normalUser, this.tagRepository);
		}
		
		@Test(expected = InvalidTagNameException.class)
		public void invalidTagName() throws Exception {
			this.object.setTitleByUser("a", this.normalUser);
			this.object.setAsTagByUser(true, this.normalUser);
			this.object.validateTagRole(this.normalUser, this.tagRepository);
		}
		
		@Test(expected = DuplicateException.class)
		public void duplicateTagName() throws Exception {
			this.tagRepository.register(this.tagRepository.newInstance("test", this.normalUser));
			this.object.setTitleByUser("test", this.normalUser);
			this.object.setAsTagByUser(true, this.normalUser);
			this.object.validateTagRole(this.normalUser, this.tagRepository);
		}
		
		@Test(expected = InvalidTaggingException.class)
		public void loopTagging() throws Exception {
			this.tagRepository.register(this.tagRepository.newInstance("test", this.normalUser));
			this.object.setTitleByUser("test", this.normalUser);
			this.object.setAsTagByUser(true, this.normalUser);
			this.object.addTagByUser("test", this.tagRepository, this.normalUser);
			this.object.validateTagRole(this.normalUser, this.tagRepository);
		}
		
		@Test
		public void newTagRole() throws Exception {
			this.object.setTitleByUser("test", this.normalUser);
			this.object.setAsTagByUser(true, this.normalUser);
			this.object.addTagByUser("parent", this.tagRepository, this.normalUser);
			this.object.validateTagRole(this.normalUser, this.tagRepository);
			
			Tag tag = this.object.asTag();		
			assertEquals("test", tag.getName());
			assertEquals("normal", tag.getCreator());
			assertEquals("(parent)", tag.getClassification().toString());
		}
	}
	
	public static class StoredTagFragmentTest extends TestBase {
		
		private Fragment object;
		private Tag tag;
		
		@Before
		public void given() throws Exception {
			RawFragment fragment = new RawFragment();
			fragment.setTitleByUser("test", this.normalUser);
			fragment.setAsTagByUser(true, this.normalUser);
			fragment.addTagByUser("parent", this.tagRepository, this.normalUser);
			fragment.validateTagRole(this.normalUser, this.tagRepository);
			
			long id = this.fragmentRepository.register(fragment);
			
			this.object = this.fragmentRepository.get(id);
			this.tag = this.tagRepository.getByName("test");
		}
		
		@Test
		public void name() throws Exception {
			assertEquals("test", this.object.getTitle());
			assertEquals("test", this.tag.getName());
		}
		
		@Test
		public void classification() throws Exception {
			assertEquals("(parent)", this.object.getClassification().toString());
			assertEquals("(parent)", tag.getClassification().toString());
		}
		
		@Test
		public void mutualIdRef() throws Exception {
			assertEquals(this.tag.getId(), this.object.getTagId());
			assertEquals(this.object.getId(), this.tag.getFragmentId());
		}
		
		@Test
		public void setTagRole() throws Exception {
			this.object.validateTagRole(this.normalUser, this.tagRepository);
			
			Tag tagRole = this.object.asTag();
			assertEquals(this.tag.getId(), tagRole.getId());
			assertEquals("test", tagRole.getName());
			assertEquals("(parent)", tagRole.getClassification().toString());
		}
		
		@Test
		public void getFragmentViaTag() throws Exception {
			Fragment fragment = this.fragmentRepository.asFragment(this.tag);
			assertEquals(this.object.getId(), fragment.getId());
			assertEquals("test", fragment.getTitle());
			assertEquals(this.tag.getId(), fragment.getTagId());
			assertEquals("(parent)", fragment.getClassification().toString());
		}
		
		@Test
		public void updateViaFragment() throws Exception {
			this.object.setTitleByUser("hogehoge", this.normalUser);
			this.object.validateTagRole(this.normalUser, this.tagRepository);
			this.fragmentRepository.update(this.object);
			
			Fragment fragment = this.fragmentRepository.get(this.object.getId());
			assertEquals("hogehoge", fragment.getTitle());
			
			fragment.validateTagRole(this.normalUser, this.tagRepository);
			Tag tagRole = fragment.asTag();
			assertEquals("hogehoge", tagRole.getName());
		}
		
		@Test
		public void deleteTagRoleViaFragment() throws Exception {
			assertEquals(true, this.tagRepository.containsName("test"));
			
			this.object.setAsTagByUser(false, this.normalUser);
			this.object.validateTagRole(this.normalUser, this.tagRepository);
			this.fragmentRepository.update(this.object);
			
			Fragment fragment = this.fragmentRepository.get(this.object.getId());
			assertEquals(null, fragment.getTagId());
			
			assertEquals(false, this.tagRepository.containsName("test"));
		}
	}
}
