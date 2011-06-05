package marubinotto.piggydb.model.tags;

import static marubinotto.util.CollectionUtils.set;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import marubinotto.piggydb.model.TagRepository;

import org.junit.Before;
import org.junit.Test;

public class SelectAllThatHaveChildrenTest extends TagRepositoryTestBase {
	
	private Long tagId_sport;
	private Long tagId_pingpong;
	
	public SelectAllThatHaveChildrenTest(
			RepositoryFactory<TagRepository> factory) {
		super(factory);
	}

	@Before
	public void given() throws Exception {
		super.given();
		
		this.tagId_sport = this.object.register(newTag("sport"));
		this.tagId_pingpong = this.object.register(newTagWithTags("pingpong", "sport"));
	}
	
	@Test
	public void oneNodeAndOneLeaf() throws Exception {
		Set<Long> tagIds = set(this.tagId_sport, this.tagId_pingpong);
		Set<Long> result = this.object.selectAllThatHaveChildren(tagIds);
		
		assertEquals(1, result.size());
		assertTrue(result.contains(this.tagId_sport));
	}
	
	@Test
	public void oneLeaf() throws Exception {
		Set<Long> tagIds = set(this.tagId_pingpong);
		Set<Long> result = this.object.selectAllThatHaveChildren(tagIds);
		
		assertEquals(0, result.size());
	}
}
