package marubinotto.piggydb.model.fragments;

import static org.junit.Assert.assertEquals;
import marubinotto.piggydb.model.FragmentRepository;
import marubinotto.piggydb.model.RelatedTags;
import marubinotto.piggydb.model.TagRepository;
import marubinotto.piggydb.model.entity.RawFilter;
import marubinotto.piggydb.model.query.FragmentsByFilter;

import org.junit.Before;
import org.junit.Test;

public class RelatedTagsTest extends FragmentRepositoryTestBase {
	
	private long tagId_todo;
	private long tagId_life;
	private long tagId_book;
	private long tagId_tech;
	
	public RelatedTagsTest(RepositoryFactory<FragmentRepository> factory) {
		super(factory);
	}

	@Before
	public void given() throws Exception {
		super.given();
		
		TagRepository tagRepository = this.object.getTagRepository();
		this.tagId_todo = tagRepository.register(newTag("todo"));
		this.tagId_life = tagRepository.register(newTag("life"));
		this.tagId_book = tagRepository.register(newTag("book"));
		this.tagId_tech = tagRepository.register(newTag("tech"));
		
		this.object.register(newFragmentWithTitleAndTags("Pay the tax", "todo", "life"));
		this.object.register(newFragmentWithTitleAndTags("Norwegian Wood", "todo", "book"));
		this.object.register(newFragmentWithTitleAndTags("Object Design", "todo", "book", "tech"));
	}

	private RelatedTags execute(String ... tagNames) throws Exception {
		RawFilter filter = new RawFilter();
		for (String tagName : tagNames) 
			filter.getClassification().addTag(storedTag(tagName));
		
		FragmentsByFilter query = (FragmentsByFilter)this.object.getQuery(FragmentsByFilter.class);
		query.setFilter(filter);
		return query.getRelatedTags();
	}
	
	@Test
	public void one() throws Exception {
		// When
		RelatedTags relatedTags = execute("life");
		
		// Then
		assertEquals(
			"[" + this.tagId_todo + "(todo) × 1]", 
			relatedTags.orderByCount(this.object.getTagRepository()).toString());
	}
	
	@Test
	public void two() throws Exception {
		// When	
		RelatedTags relatedTags = execute("tech");
		
		// Then
		assertEquals(
			"[" + this.tagId_todo + "(todo) × 1, " + 
				this.tagId_book + "(book) × 1]", 
			relatedTags.orderByCount(this.object.getTagRepository()).toString());
	}
	
	@Test
	public void threeAndCountUp() throws Exception {
		// When
		RelatedTags relatedTags = execute("todo");
		
		// Then
		assertEquals(
			"[" + this.tagId_book + "(book) × 2, " + 
				this.tagId_life + "(life) × 1, " + 
				this.tagId_tech + "(tech) × 1]", 
			relatedTags.orderByCount(this.object.getTagRepository()).toString());
	}
}
