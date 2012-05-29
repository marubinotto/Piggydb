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
		public void validateAsTag() throws Exception {
			this.object.validateAsTag(this.normalUser, this.tagRepository);
		}
		
		@Test(expected = InvalidTitleException.class)
		public void tagShouldHaveTitle() throws Exception {
			this.object.setAsTagByUser(true, this.normalUser);
			this.object.validateAsTag(this.normalUser, this.tagRepository);
		}
		
		@Test(expected = InvalidTagNameException.class)
		public void invalidTagName() throws Exception {
			this.object.setTitleByUser("a", this.normalUser);
			this.object.setAsTagByUser(true, this.normalUser);
			this.object.validateAsTag(this.normalUser, this.tagRepository);
		}
		
		@Test(expected = DuplicateException.class)
		public void duplicateTagName() throws Exception {
			this.tagRepository.register(this.tagRepository.newInstance("test", this.normalUser));
			this.object.setTitleByUser("test", this.normalUser);
			this.object.setAsTagByUser(true, this.normalUser);
			this.object.validateAsTag(this.normalUser, this.tagRepository);
		}
		
		@Test(expected = InvalidTaggingException.class)
		public void loopTagging() throws Exception {
			this.tagRepository.register(this.tagRepository.newInstance("test", this.normalUser));
			this.object.setTitleByUser("test", this.normalUser);
			this.object.setAsTagByUser(true, this.normalUser);
			this.object.addTagByUser("test", this.tagRepository, this.normalUser);
			this.object.validateAsTag(this.normalUser, this.tagRepository);
		}
		
		@Test
		public void validTag() throws Exception {
			this.object.setTitleByUser("test", this.normalUser);
			this.object.setAsTagByUser(true, this.normalUser);
			this.object.addTagByUser("parent", this.tagRepository, this.normalUser);
			this.object.validateAsTag(this.normalUser, this.tagRepository);
			
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
			fragment.validateAsTag(this.normalUser, this.tagRepository);
			
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
		public void validateAsTag() throws Exception {
			this.object.validateAsTag(this.normalUser, this.tagRepository);
			
			Tag tag = this.object.asTag();
			assertEquals(this.tag.getId(), tag.getId());
			assertEquals("test", tag.getName());
			assertEquals("(parent)", tag.getClassification().toString());
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
			this.object.setTitleByUser("test2", this.normalUser);
			this.object.addTagByUser("parent2", this.tagRepository, this.normalUser);
			this.object.validateAsTag(this.normalUser, this.tagRepository);
			this.fragmentRepository.update(this.object);
			
			Fragment fragment = this.fragmentRepository.get(this.object.getId());
			assertEquals("test2", fragment.getTitle());
			
			fragment.validateAsTag(this.normalUser, this.tagRepository);
			Tag tag = fragment.asTag();
			assertEquals("test2", tag.getName());
			assertEquals("(parent, parent2)", tag.getClassification().toString());
		}
		
		@Test
		public void updateViaTag() throws Exception {
			this.tag.setNameByUser("test2", this.normalUser);
			this.tag.addTagByUser("parent2", this.tagRepository, this.normalUser);
			this.fragmentRepository.update(this.tag, this.normalUser);
			
			Fragment fragment = this.fragmentRepository.get(this.object.getId());
			assertEquals("test2", fragment.getTitle());
			assertEquals("(parent, parent2)", fragment.getClassification().toString());
			
			fragment.validateAsTag(this.normalUser, this.tagRepository);
			Tag tag = fragment.asTag();
			assertEquals("test2", tag.getName());
			assertEquals("(parent, parent2)", tag.getClassification().toString());
		}
		
		@Test
		public void deleteTagRoleViaFragment() throws Exception {
			this.object.setAsTagByUser(false, this.normalUser);
			this.object.validateAsTag(this.normalUser, this.tagRepository);
			this.fragmentRepository.update(this.object);
			
			Fragment fragment = this.fragmentRepository.get(this.object.getId());
			assertEquals(null, fragment.getTagId());		
			assertEquals(false, this.tagRepository.containsName("test"));
		}
		
		@Test
		public void deleteTagRoleViaTag() throws Exception {
			Fragment fragment1 = this.fragmentRepository.delete(this.tag, this.normalUser);
			assertEquals(this.object.getId(), fragment1.getId());
			assertEquals(null, fragment1.getTagId());
			
			Fragment fragment2 = this.fragmentRepository.get(this.object.getId());
			assertEquals(null, fragment2.getTagId());
			assertEquals(false, this.tagRepository.containsName("test"));
		}
	}
	
	public static class ConvertTagToTagFragmentTest extends TestBase {
		
		private Tag object;
		private Long fragmentId;
		
		@Before
		public void given() throws Exception {
			this.object = this.tagRepository.newInstance("test", this.normalUser);
			this.object.addTagByUser("parent", this.tagRepository, this.normalUser);
			this.tagRepository.register(this.object);
			
			this.fragmentId = this.fragmentRepository.
				registerFragmentIfNotExists(this.object, this.normalUser);
		}
		
		@Test
		public void tag() throws Exception {
			assertEquals(this.fragmentId, this.object.getFragmentId());
		}
		
		@Test
		public void fragment() throws Exception {
			Fragment fragment = this.fragmentRepository.asFragment(this.object);
			assertEquals(this.fragmentId, fragment.getId());
			assertEquals(this.object.getId(), fragment.getTagId());
			assertEquals("test", fragment.getTitle());
			assertEquals("(parent)", fragment.getClassification().toString());
		}
	}
}
