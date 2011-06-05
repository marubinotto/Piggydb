package marubinotto.piggydb.model.fragments.fetching;

import static marubinotto.util.CollectionUtils.makeMap;
import static marubinotto.util.CollectionUtils.set;
import static marubinotto.util.time.DateTime.setCurrentTimeForTest;
import static org.junit.Assert.assertEquals;

import java.util.Map;

import marubinotto.piggydb.model.Fragment;
import marubinotto.piggydb.model.FragmentRepository;
import marubinotto.piggydb.model.TagRepository;
import marubinotto.piggydb.model.fragments.FragmentRepositoryTestBase;

import org.junit.Before;
import org.junit.Test;

public class FetchingOfGetTest extends FragmentRepositoryTestBase {
	
	public FetchingOfGetTest(RepositoryFactory<FragmentRepository> factory) {
		super(factory);
	}

	@Before
	public void given() throws Exception {
		super.given();
		
		TagRepository tagRepository = this.object.getTagRepository();	
		tagRepository.register(newTagWithTags("apple", "fruit"));
		
		for (int i = 1; i <= 10; i++) {
			setCurrentTimeForTest(2009, 1, i);
			this.object.register(newFragmentWithTags("apple"));
		}
		setCurrentTimeForTest(null);
	}
	
	@Test
	public void parent() throws Exception {
		this.object.createRelation(2, 1, getPlainUser());
		
		Fragment fragment = this.object.get(1);
		
		assertEquals(1, fragment.getParents().size());
		assertEquals(2, fragment.getParents().get(0).getId().longValue());
	}
	
	@Test
	public void child() throws Exception {
		this.object.createRelation(1, 2, getPlainUser());
		
		Fragment fragment = this.object.get(1);
		
		assertEquals("#1 (#2)", fragment.toStringWithDescendents());
	}
	
	@Test
	public void tagsOfChild() throws Exception {
		this.object.createRelation(1, 2, getPlainUser());
		
		Fragment fragment = this.object.get(1);
		
		Fragment child = fragment.getChildren().get(0);
		assertEquals("(apple (fruit))", child.getClassification().toString());
	}
	
	@Test
	public void parentsOfChild() throws Exception {
		this.object.createRelation(1, 2, getPlainUser());
		this.object.createRelation(3, 2, getPlainUser());
		
		Fragment fragment = this.object.get(1);
		
		Fragment child = fragment.getChildren().get(0);
		Map<Long, Fragment> parentsOfChild = makeMap(child.getParents(), "id");
		assertEquals(set(1L, 3L), parentsOfChild.keySet());
	}
	
	@Test
	public void grandchild() throws Exception {
		this.object.createRelation(1, 2, getPlainUser());
		this.object.createRelation(2, 3, getPlainUser());
		
		Fragment fragment = this.object.get(1);
		
		assertEquals("#1 (#2 (#3))", fragment.toStringWithDescendents());
	}
	
	@Test
	public void greatgrandchild() throws Exception {
		this.object.createRelation(1, 2, getPlainUser());
		this.object.createRelation(2, 3, getPlainUser());
		this.object.createRelation(3, 4, getPlainUser());
		
		Fragment fragment = this.object.get(1);
		
		assertEquals("#1 (#2 (#3 (#4)))", fragment.toStringWithDescendents());
	}
	
	@Test
	public void greatgrandchildrenWithDifferentParent() throws Exception {
		this.object.createRelation(1, 2, getPlainUser());
		this.object.createRelation(1, 3, getPlainUser());
		
		this.object.createRelation(2, 4, getPlainUser());
		this.object.createRelation(3, 5, getPlainUser());
		
		this.object.createRelation(4, 6, getPlainUser());
		this.object.createRelation(5, 7, getPlainUser());
		
		Fragment fragment = this.object.get(1);
		
		assertEquals(
			"#1 (#2 (#4 (#6)), #3 (#5 (#7)))", 
			fragment.toStringWithDescendents());
	}
	
	@Test
	public void greatgrandchildrenWithSameParent() throws Exception {
		this.object.createRelation(1, 2, getPlainUser());
		this.object.createRelation(1, 3, getPlainUser());
		
		this.object.createRelation(2, 4, getPlainUser());
		this.object.createRelation(3, 4, getPlainUser());
		
		this.object.createRelation(4, 5, getPlainUser());
		
		Fragment fragment = this.object.get(1);
		
		assertEquals(
			"#1 (#2 (#4 (#5)), #3 (#4 (#5)))", 
			fragment.toStringWithDescendents());
	}
	
	@Test
	public void loop() throws Exception {
		// Given
		this.object.createRelation(1, 2, getPlainUser());
		this.object.createRelation(2, 1, getPlainUser());
		
		// When
		Fragment fragment = this.object.get(1);
		
		// Then
		assertEquals(1, fragment.getParents().size());
		assertEquals(2, fragment.getParents().get(0).getId().longValue());
		
		assertEquals(1, fragment.getChildren().size());
		assertEquals(2, fragment.getChildren().get(0).getId().longValue());
	}
}
