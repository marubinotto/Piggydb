package marubinotto.piggydb.model.classification;

import static marubinotto.piggydb.model.Assert.assertClassificationEquals;
import static marubinotto.util.CollectionUtils.map;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;

import marubinotto.piggydb.impl.InMemoryDatabase;
import marubinotto.piggydb.model.MutableClassification;
import marubinotto.piggydb.model.Tag;
import marubinotto.piggydb.model.TagRepository;
import marubinotto.piggydb.model.User;
import marubinotto.piggydb.model.entity.RawTag;

import org.junit.Test;

public class MiscTest {

	private MutableClassification object = new MutableClassification();

	@Test
	public void orderTagsByName() throws Exception {
		// Given
		this.object.addTag(new RawTag("Daisuke"));
		this.object.addTag(new RawTag("Akane"));
		
		// Then
		assertEquals(2, this.object.size());
		
		Iterator<Tag> iterator = this.object.getTagIterator();
		assertEquals("Akane", iterator.next().getName());
		assertEquals("Daisuke", iterator.next().getName());
	}
	
	@Test
	public void stringRepresentation() throws Exception {
		// Given
		RawTag techTag = new RawTag("tech");
		RawTag javaTag = new RawTag("java");
		javaTag.getMutableClassification().addTag(techTag);
		
		RawTag memoTag = new RawTag("memo");
		
		this.object.addTag(javaTag);
		this.object.addTag(memoTag);
		
		// When
		String result = this.object.toString();
		
		// Then
		assertEquals("(java (tech), memo)", result);
	}

	@Test
	public void replaceTagInSameHierarchy() throws Exception {
		// Given
		RawTag techTag = new RawTag("tech");
		RawTag javaTag = new RawTag("java");
		javaTag.getMutableClassification().addTag(techTag);
		
		this.object.addTag(techTag);
		
		// When
		Tag deletedTag = this.object.addTag(javaTag);
		
		// Then
		assertTrue(deletedTag.equals(techTag));
		assertEquals(1, this.object.size());
		assertTrue(this.object.containsTagName("java"));
	}
	
	@Test
	public void refreshStoredTag() throws Exception {
		// Given
		TagRepository repository = new InMemoryDatabase().getTagRepository();
		repository.register(new RawTag("stored"));
		
		this.object.addTag(repository.getByName("stored"));
		this.object.addTag(new RawTag("not-stored"));
		assertTrue(this.object.containsTagName("stored"));
		
		Tag storedTag = repository.getByName("stored");
		storedTag.setNameByUser("stored-modified", new User("daisuke"));
		repository.update(storedTag);
		
		// When
		this.object.refreshEachTag(repository);
		
		// Then
		assertFalse(this.object.containsTagName("stored"));
		assertTrue(this.object.containsTagName("stored-modified"));
		assertTrue(this.object.containsTagName("not-stored"));
	}
	
	@Test
	public void syncWith() throws Exception {
		MutableClassification another = new MutableClassification();
		another.addTag(new RawTag("foo"));
		another.addTag(new RawTag("bar"));
		
		this.object.syncWith(another);
		
		assertClassificationEquals(
			map("foo", null).map("bar", null), 
			this.object);
	}
}
