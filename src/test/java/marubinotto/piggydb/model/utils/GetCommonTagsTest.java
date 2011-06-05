package marubinotto.piggydb.model.utils;

import static marubinotto.piggydb.fixture.EntityFixtures.newFragmentWithTags;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import marubinotto.piggydb.model.Fragment;
import marubinotto.piggydb.model.ModelUtils;
import marubinotto.piggydb.model.Tag;
import marubinotto.piggydb.model.entity.RawFragment;
import marubinotto.piggydb.model.entity.RawTag;

import org.junit.Test;

public class GetCommonTagsTest {

	@Test
	public void zeroFragments() throws Exception {
		Set<Tag> tags = ModelUtils.getCommonTags(new ArrayList<Fragment>());
		assertTrue(tags.isEmpty());
	}
	
	@Test
	public void oneFragmentWithoutTags() throws Exception {
		List<Fragment> fragments = new ArrayList<Fragment>();
		fragments.add(new RawFragment());
		
		Set<Tag> tags = ModelUtils.getCommonTags(fragments);
		
		assertTrue(tags.isEmpty());
	}
	
	@Test
	public void oneFragmentWithOneTag() throws Exception {
		List<Fragment> fragments = new ArrayList<Fragment>();
		fragments.add(newFragmentWithTags("tag"));
		
		Set<Tag> tags = ModelUtils.getCommonTags(fragments);
		
		assertEquals(1, tags.size());
		assertTrue(tags.contains(new RawTag("tag")));
	}
	
	@Test
	public void twoFragmentsWithoutCommonTags() throws Exception {
		List<Fragment> fragments = new ArrayList<Fragment>();
		fragments.add(newFragmentWithTags("tag1"));
		fragments.add(newFragmentWithTags("tag2"));
		
		Set<Tag> tags = ModelUtils.getCommonTags(fragments);
		
		assertTrue(tags.isEmpty());
	}
	
	@Test
	public void twoFragmentsWithOneCommonTags() throws Exception {
		List<Fragment> fragments = new ArrayList<Fragment>();
		fragments.add(newFragmentWithTags("tag", "tag1"));
		fragments.add(newFragmentWithTags("tag", "tag2"));
		
		Set<Tag> tags = ModelUtils.getCommonTags(fragments);
		
		assertEquals(1, tags.size());
		assertTrue(tags.contains(new RawTag("tag")));
	}
}
