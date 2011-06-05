package marubinotto.piggydb.model.fragments.fetching;

import static junit.framework.Assert.assertTrue;
import static marubinotto.piggydb.model.Assert.assertClassificationEquals;
import static marubinotto.util.CollectionUtils.map;
import static marubinotto.util.CollectionUtils.set;
import static org.junit.Assert.assertEquals;
import marubinotto.piggydb.model.Fragment;
import marubinotto.piggydb.model.FragmentRepository;
import marubinotto.piggydb.model.Tag;
import marubinotto.piggydb.model.TagRepository;
import marubinotto.piggydb.model.User;
import marubinotto.piggydb.model.fragments.FragmentRepositoryTestBase;

import org.junit.Before;
import org.junit.Test;

public class WithTagsTest extends FragmentRepositoryTestBase {
	
	private TagRepository tagRepository;
	
	public WithTagsTest(RepositoryFactory<FragmentRepository> factory) {
		super(factory);
	}
	
	@Before
	public void given() throws Exception {
		super.given();
		this.tagRepository = this.object.getTagRepository();
	}

	@Test
	public void registerWithOneNewTag() throws Exception {
		// When
		User user = getPlainUser();
		Fragment original = this.object.newInstance(user);
		original.addTagByUser("tag", this.tagRepository, user);
		
		long fragmentId = this.object.register(original);

		// Then
		Fragment retrieved = this.object.get(fragmentId);
		
		assertEquals(1, retrieved.getClassification().size());
		
		Tag tag = this.tagRepository.getByName("tag");
		assertEquals(tag.getId(), original.getClassification().getTag("tag").getId());
		assertEquals(tag.getId(), retrieved.getClassification().getTag("tag").getId());
	}
	
	@Test
	public void registerWithOneExistingTag() throws Exception {
		// Given
		Long tagId = this.tagRepository.register(newTag("tag"));
		
		// When
		Fragment original = newFragmentWithTags("tag");
		long fragmentId = this.object.register(original);
		
		// Then
		Fragment retrieved = this.object.get(fragmentId);
		assertEquals(1, retrieved.getClassification().size());
		assertEquals(tagId, retrieved.getClassification().getTag("tag").getId());
	}

	@Test
	public void registerWithTwoNewTags() throws Exception {
		// When
		Fragment original = newFragmentWithTags("tag1", "tag2");
		long fragmentId = this.object.register(original);
		
		// Then
		Fragment retrieved = this.object.get(fragmentId);
		
		assertEquals(2, retrieved.getClassification().size());
		
		Tag tag1 = this.tagRepository.getByName("tag1");			
		assertEquals(tag1.getId(), original.getClassification().getTag("tag1").getId());
		assertEquals(tag1.getId(), retrieved.getClassification().getTag("tag1").getId());
		
		Tag tag2 = this.tagRepository.getByName("tag2");
		assertEquals(tag2.getId(), original.getClassification().getTag("tag2").getId());
		assertEquals(tag2.getId(), retrieved.getClassification().getTag("tag2").getId());
	}
	
	@Test
	public void registerWithNewParentAndGrandparentTag() throws Exception {
		// When
		Fragment original = newFragment();
		Tag parent = newTag("parent");
		Tag grandparent = newTag("grandparent");
		
		parent.addTagByUser(grandparent, getPlainUser());
		original.addTagByUser(parent, getPlainUser());
		
		long fragmentId = this.object.register(original);
		
		// Then
		assertEquals(2, this.tagRepository.size());
		assertTrue(this.tagRepository.containsName("parent"));
		assertTrue(this.tagRepository.containsName("grandparent"));
		
		Fragment retrieved = this.object.get(fragmentId);
		assertClassificationEquals(
			map("parent", map("grandparent", null)), 
			retrieved.getClassification());
	}

	@Test
	public void updateWithOneNewTag() throws Exception {
		// Given
		long fragmentId = this.object.register(newFragment());
		
		// When
		Fragment toUpdate = this.object.get(fragmentId);
		toUpdate.addTagByUser("tag", this.tagRepository, getPlainUser());
		this.object.update(toUpdate);
		
		// Then
		Fragment retrieved = this.object.get(fragmentId);
		assertEquals(1, retrieved.getClassification().size());
		
		Tag tag = this.tagRepository.getByName("tag");
		assertEquals(tag.getId(), toUpdate.getClassification().getTag("tag").getId());
		assertEquals(tag.getId(), retrieved.getClassification().getTag("tag").getId());
	}
	
	@Test
	public void getWithTagWhoseNameChanged() throws Exception {
		// Given
		long fragmentId = this.object.register(newFragmentWithTags("tag"));
		
		Tag tag = this.tagRepository.getByName("tag");
		tag.setNameByUser("modified-tag", getPlainUser());
		this.tagRepository.update(tag);
		
		// When
		Fragment retrieved = this.object.get(fragmentId);
		
		// Then
		assertClassificationEquals(set("modified-tag"),  retrieved.getClassification());
	}
	
	@Test
	public void getWithTagDeleted() throws Exception {
		// Given
		long tagId = this.tagRepository.register(newTag("tag"));
		long fragmentId = this.object.register(newFragmentWithTags("tag"));
		
		this.tagRepository.delete(tagId, getPlainUser());
		
		// When
		Fragment retrieved = this.object.get(fragmentId);
		
		// Then
		assertEquals(0, retrieved.getClassification().size());
	}
	
	@Test
	public void getWithGrandparentTag() throws Exception {
		// Given
		this.tagRepository.register(newTagWithTags("parent", "grandparent"));
		long fragmentId = this.object.register(newFragmentWithTags("parent"));
		
		// When
		Fragment retrieved = this.object.get(fragmentId);
		
		// Then
		assertClassificationEquals(
			map("parent", map("grandparent", null)), 
			retrieved.getClassification());
	}
}
