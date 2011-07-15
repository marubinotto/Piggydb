package marubinotto.piggydb.model.fragments.relation;

import static marubinotto.piggydb.model.Assert.assertClassificationEquals;
import static marubinotto.util.CollectionUtils.set;
import static marubinotto.util.time.DateTime.setCurrentTimeForTest;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import marubinotto.piggydb.model.FragmentRelation;
import marubinotto.piggydb.model.FragmentRepository;
import marubinotto.piggydb.model.exception.DuplicateException;
import marubinotto.piggydb.model.exception.NoSuchEntityException;
import marubinotto.piggydb.model.fragments.FragmentRepositoryTestBase;
import marubinotto.util.time.DateTime;

import org.junit.Before;
import org.junit.Test;

public class OneFragmentRelationTest extends FragmentRepositoryTestBase {

	protected long id1;
	protected long id2;
	protected long relationId;
	protected DateTime relationCreationDateTime;
	
	public OneFragmentRelationTest(RepositoryFactory<FragmentRepository> factory) {
		super(factory);
	}

	@Before
	public void given() throws Exception {
		super.given();

		// Two fragments
		this.id1 = this.object.register(
			newFragmentWithTitleAndTags("Piggydb", "project"));
		this.id2 = this.object.register(
			newFragmentWithTitleAndTags("What is Piggydb?", "project", "draft"));
		
		// And one relation (id1 -> id2)
		this.relationCreationDateTime = new DateTime(2008, 1, 1);
		setCurrentTimeForTest(this.relationCreationDateTime);
		this.relationId = this.object.createRelation(this.id1, this.id2, getPlainUser());
		
		setCurrentTimeForTest(null);
	}
	
	@Test
	public void relationCountShouldBeOne() throws Exception {
		assertEquals(1, this.object.countRelations().longValue());
	}
	
	@Test
	public void getRelation() throws Exception {
		// When
		FragmentRelation relation = this.object.getRelation(this.relationId);
		
		// Then
		
		// Relation object
		assertEquals(this.relationId, relation.getId().longValue());
		assertEquals(this.relationCreationDateTime, relation.getCreationDatetime());
		assertEquals(this.relationCreationDateTime, relation.getUpdateDatetime());
		assertEquals(getPlainUser().getName(), relation.getCreator());
		assertNull(relation.getUpdater());
		
		// from
		assertEquals("Piggydb", relation.from.getTitle());
		assertClassificationEquals(set("project"), relation.from.getClassification());
		
		// to
		assertEquals("What is Piggydb?", relation.to.getTitle());
		assertClassificationEquals(set("project", "draft"), relation.to.getClassification());
	}
	
	@Test
	public void getRelationWithNonexistentId() throws Exception {
		assertNull(this.object.getRelation(12345));
	}
	
	@Test(expected=NoSuchEntityException.class)
	public void createRelationWithNonexistentParent() throws Exception {
		this.object.createRelation(123, this.id1, getPlainUser());
	}
	
	@Test(expected=NoSuchEntityException.class)
	public void createRelationWithNonexistentChild() throws Exception {
		this.object.createRelation(this.id1, 123, getPlainUser());
	}
	
	@Test(expected=DuplicateException.class)
	public void createDuplicateRelation() throws Exception {
		this.object.createRelation(this.id1, this.id2, getPlainUser());
	}
	
	@Test
	public void deleteRelation() throws Exception {
		// When
		this.object.deleteRelation(this.relationId, getPlainUser());
		
		// Then
		assertNull(this.object.getRelation(this.relationId));
	}
	
	@Test
	public void deleteParentFragment() throws Exception {
		// When
		this.object.delete(this.id1, getPlainUser());
		
		// Then
		assertEquals(1, this.object.size());
		assertNull(this.object.getRelation(this.relationId));
	}
	
	@Test
	public void deleteChildFragment() throws Exception {
		// When
		this.object.delete(this.id2, getPlainUser());
		
		// Then
		assertEquals(1, this.object.size());
		assertNull(this.object.getRelation(this.relationId));
	}
}
