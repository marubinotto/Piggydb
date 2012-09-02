package marubinotto.piggydb.model;

import static marubinotto.piggydb.fixture.EntityFixtures.tagWithId;
import static org.junit.Assert.assertEquals;
import marubinotto.piggydb.impl.InMemoryDatabase;
import marubinotto.piggydb.model.auth.User;
import marubinotto.piggydb.model.entity.RawFilter;

import org.junit.Before;
import org.junit.Test;

public class RelatedTagsTest {
	
	private RelatedTags object = new RelatedTags();
	
	private RawFilter filter = new RawFilter();
	private TagRepository tagRepository = new InMemoryDatabase().getTagRepository();

	@Before
	public void given() {
		this.object.setFilter(this.filter);
	}
	
	@Test
	public void add() throws Exception {
		this.object.add(1, 1);
		
		assertEquals(
			"[1(null) × 1]", 
			this.object.orderByCount(this.tagRepository).toString());
	}
	
	@Test
	public void addTwoDifferentTags() throws Exception {
		this.object.add(1, 1);
		this.object.add(2, 1);
		
		assertEquals(
			"[1(null) × 1, 2(null) × 1]", 
			this.object.orderByCount(this.tagRepository).toString());
	}
	
	@Test
	public void addThreeDifferentTagsOrderByCountDesc() throws Exception {
		this.object.add(1, 2);
		this.object.add(2, 3);
		this.object.add(3, 1);
		
		assertEquals(
			"[2(null) × 3, 1(null) × 2, 3(null) × 1]", 
			this.object.orderByCount(this.tagRepository).toString());
	}
	
	@Test
	public void returnSizeLimit_oneOver() throws Exception {
		this.object.add(1, 2);
		this.object.add(2, 3);
		this.object.add(3, 1);
		
		this.object.setReturnSizeLimit(2);
		
		assertEquals(
			"[2(null) × 3, 1(null) × 2]", 
			this.object.orderByCount(this.tagRepository).toString());
	}
	
	@Test
	public void returnSizeLimit_same() throws Exception {
		this.object.add(1, 1);
		
		this.object.setReturnSizeLimit(1);
		
		assertEquals(
			"[1(null) × 1]", 
			this.object.orderByCount(this.tagRepository).toString());
	}
	
	@Test
	public void addTwoSameTags() throws Exception {
		this.object.add(1, 1);
		this.object.add(1, 1);
		
		assertEquals(
			"[1(null) × 2]", 
			this.object.orderByCount(this.tagRepository).toString());
	}
	
	@Test
	public void setOneName() throws Exception {
		long tagId = this.tagRepository.register(
			this.tagRepository.newInstance("foo", new User("daisuke")));
		
		this.object.add(tagId, 1);
		
		assertEquals(
			"[1(foo) × 1]", 
			this.object.orderByCount(this.tagRepository).toString());
	}
	
	@Test
	public void shouldExcludeTagsInCondition_classification() throws Exception {
		this.filter.getClassification().addTag(tagWithId("foo", 1));

		this.object.add(1, 1);
		this.object.add(2, 1);
		
		assertEquals(
			"[2(null) × 1]", 
			this.object.orderByCount(this.tagRepository).toString());
	}
	
	@Test
	public void shouldExcludeTagsInCondition_excludes() throws Exception {
		this.filter.getExcludes().addTag(tagWithId("bar", 2));

		this.object.add(1, 1);
		this.object.add(2, 1);
		
		assertEquals(
			"[1(null) × 1]", 
			this.object.orderByCount(this.tagRepository).toString());
	}
}
