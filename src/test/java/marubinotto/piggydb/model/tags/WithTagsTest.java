package marubinotto.piggydb.model.tags;

import static marubinotto.piggydb.model.Assert.assertClassificationEquals;
import static marubinotto.util.CollectionUtils.map;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import marubinotto.piggydb.model.Tag;
import marubinotto.piggydb.model.TagRepository;

import org.junit.Test;

public class WithTagsTest extends TagRepositoryTestBase {
	
	public WithTagsTest(RepositoryFactory<TagRepository> factory) {
		super(factory);
	}

	@Test
	public void registerWithOneNewTag() throws Exception {
		// When
		Tag originalTag = newTagWithTags("tag", "parent");
		long tagId = this.object.register(originalTag);
		
		// Then
		Tag tag = this.object.get(tagId);
		assertEquals(1, tag.getClassification().size());
		
		Tag parent = this.object.getByName("parent");	
		assertEquals(parent.getId(), originalTag.getClassification().getTag("parent").getId());
		assertEquals(parent.getId(), tag.getClassification().getTag("parent").getId());
	}
	
	@Test
	public void registerWithOneExistingTag() throws Exception {
		// Given
		long parentId = this.object.register(newTag("parent"));
		
		// When
		Tag originalTag = newTagWithTags("tag", "parent");
		long tagId = this.object.register(originalTag);
		
		// Then
		Tag tag = this.object.get(tagId);
		assertEquals(1, tag.getClassification().size());
		assertEquals(parentId, tag.getClassification().getTag("parent").getId().longValue());
	}

	@Test
	public void updateWithOneNewTag() throws Exception {
		// Given
		long tagId = this.object.register(newTag("tag"));
		
		// When
		Tag originalTag = this.object.get(tagId);
		originalTag.addTagByUser("parent", this.object, getPlainUser());
		this.object.update(originalTag);
		
		// Then
		Tag tag = this.object.get(tagId);
		assertEquals(1, tag.getClassification().size());
		
		Tag parent = this.object.getByName("parent");	
		assertEquals(parent.getId(), originalTag.getClassification().getTag("parent").getId());
		assertEquals(parent.getId(), tag.getClassification().getTag("parent").getId());
	}
	
	@Test
	public void updateWithTagRemoved() throws Exception {
		// Given
		long tagId = this.object.register(newTagWithTags("tag", "parent"));

		// When
		Tag originalTag = this.object.get(tagId);
		originalTag.removeTagByUser("parent", getPlainUser());
		this.object.update(originalTag);
		
		// Then
		Tag tag = this.object.get(tagId);
		assertEquals(0, tag.getClassification().size());
	}
	
	@Test
	public void getByIdWithTagWhoseNameChanged() throws Exception {
		// Given
		long tagId = registerTagWithOneTagAndModifyItsName();
		
		// When
		Tag tag = this.object.get(tagId);
		
		// Then
		assertClassificationEquals(map("modified-parent", null), tag.getClassification());
	}
	
	@Test
	public void getByNameWithTagWhoseNameChanged() throws Exception {
		// Given
		registerTagWithOneTagAndModifyItsName();
		
		// When
		Tag tag = this.object.getByName("tag");
		
		// Then
		assertClassificationEquals(map("modified-parent", null), tag.getClassification());
	}
	
	@Test
	public void getWithGrandparentTag() throws Exception {
		// Given
		this.object.register(newTagWithTags("parent", "grandparent"));
		this.object.register(newTagWithTags("tag", "parent"));
		
		// When
		Tag tag = this.object.getByName("tag");
		
		// Then
		assertClassificationEquals(
			map("parent", map("grandparent", null)), 
			tag.getClassification());
	}
	
	@Test
	public void getWithGrandparentTagRegisteredLater() throws Exception {
		// Given
		this.object.register(newTagWithTags("tag", "parent"));
		
		Tag parent = this.object.getByName("parent");
		parent.addTagByUser("grandparent", this.object, getPlainUser());
		this.object.update(parent);	// should be "tag" -> "parent" -> "grandparent"
		
		// When
		Tag tag = this.object.getByName("tag");
		
		// Then
		assertClassificationEquals(
			map("parent", map("grandparent", null)), 
			tag.getClassification());
	}
	
	@Test
	public void getWithParentTagsThatHaveSameGrandparentTags() throws Exception {
		// Given
		this.object.register(newTagWithTags("grandparent", "grand grandparent"));
		this.object.register(newTagWithTags("parent1", "grandparent"));
		this.object.register(newTagWithTags("parent2", "grandparent"));
		this.object.register(newTagWithTags("tag", "parent1", "parent2"));
		
		// When
		Tag tag = this.object.getByName("tag");
		
		// Then
		assertClassificationEquals(
			map("parent1", map("grandparent", map("grand grandparent", null)))
				.map("parent2", map("grandparent", map("grand grandparent", null))), 
			tag.getClassification());
	}
	
	@Test
	public void removeParentTag() throws Exception {
		// Given
		this.object.register(newTagWithTags("tag", "parent"));
		
		Tag parent = this.object.getByName("parent");
		this.object.delete(parent.getId(), getPlainUser());
		assertFalse(this.object.containsName("parent"));
		
		// When
		Tag tag = this.object.getByName("tag");
		
		// Then
		assertEquals(0, tag.getClassification().size());
	}
	
// Internal
	
	private long registerTagWithOneTagAndModifyItsName() throws Exception {
		long tagId = this.object.register(newTagWithTags("tag", "parent"));
		
		Tag parent = this.object.getByName("parent");
		parent.setNameByUser("modified-parent", getPlainUser());
		this.object.update(parent);
		
		return tagId;
	}
}
