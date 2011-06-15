package marubinotto.piggydb.model.fragments.fetching;

import static marubinotto.piggydb.model.Assert.assertClassificationEquals;
import static marubinotto.util.CollectionUtils.map;
import static marubinotto.util.CollectionUtils.set;
import static marubinotto.util.time.DateTime.setCurrentTimeForTest;
import static org.junit.Assert.assertEquals;
import marubinotto.piggydb.impl.jdbc.h2.H2FragmentRepository;
import marubinotto.piggydb.model.Fragment;
import marubinotto.piggydb.model.FragmentRepository;
import marubinotto.piggydb.model.FragmentsOptions;
import marubinotto.piggydb.model.TagRepository;
import marubinotto.piggydb.model.FragmentsOptions.SortOption;
import marubinotto.piggydb.model.entity.RawFilter;
import marubinotto.piggydb.model.enums.FragmentField;
import marubinotto.piggydb.model.fragments.FragmentRepositoryTestBase;
import marubinotto.util.time.DateTime;

import org.junit.Before;
import org.junit.Test;

public class EagerFetchingTest extends FragmentRepositoryTestBase {

	private Long targetId;
	private Long parentId;
	private Long childId;
	
	public EagerFetchingTest(RepositoryFactory<FragmentRepository> factory) {
		super(factory);
	}
	
	@Before
	public void given() throws Exception {
		super.given();
		
		TagRepository tagRepository = this.object.getTagRepository();
		tagRepository.register(newTagWithTags("tag", "tagOfTag"));
		
		setCurrentTimeForTest(2010, 1, 1);
		this.parentId = this.object.register(newFragmentWithTitle("parent"));
		
		setCurrentTimeForTest(2010, 1, 2);
		this.childId = this.object.register(newFragmentWithTitle("child"));
		
		setCurrentTimeForTest(2010, 1, 3);
		this.targetId = this.object.register(newFragmentWithTitleAndTags("target", "tag"));
		
		setCurrentTimeForTest(null);

		this.object.createRelation(this.parentId, this.targetId, getPlainUser());
		this.object.createRelation(this.targetId, this.childId, getPlainUser());
	}
	
	private void checkEagerFetching(Fragment target) throws Exception {
		// attributes
		assertEquals(this.targetId, target.getId());
		assertEquals("target", target.getTitle());
		
		// classification
		assertClassificationEquals(
			map("tag", map("tagOfTag", null)), 
			target.getClassification());
		
		// parents
		assertEquals(1, target.getParents().size());
		Fragment parent = target.getParents().get(0);
		assertEquals(this.parentId, parent.getId());
		assertEquals("parent", parent.getTitle());
		
		// children
		assertEquals(1, target.getChildren().size());
		Fragment child = target.getChildren().get(0);
		assertEquals(this.childId, child.getId());
		assertEquals("child", child.getTitle());
	}
	
	@Test
	public void getFragments() throws Exception {
		Fragment target = this.object.getFragments(new FragmentsOptions(3, 0, true)).get(0);
		checkEagerFetching(target);
	}
	
	@Test
	public void findByTime() throws Exception {
		Fragment target = this.object.findByTime(
			new DateTime(2010, 1, 3).toDayInterval(),
			FragmentField.CREATION_DATETIME,
			new FragmentsOptions(2, 0, true)).get(0);
		checkEagerFetching(target);
	}
	
	@Test
	public void findByFilter() throws Exception {
		Fragment target = this.object.findByFilter(
			new RawFilter(), new FragmentsOptions(3, 0, true)).get(0);
		checkEagerFetching(target);
	}
	
	@Test
	public void findByUser() throws Exception {
		Fragment target = this.object.findByUser(
			getPlainUser().getName(), new FragmentsOptions(3, 0, true)).get(0);
		checkEagerFetching(target);
	}
	
	@Test
	public void findByKeywords() throws Exception {
		if (this.object instanceof H2FragmentRepository) {
			Fragment target = this.object.findByKeywords(
				"target", new FragmentsOptions(3, 0, true)).get(0);
			checkEagerFetching(target);
		}
	}
	
	@Test
	public void getByIds() throws Exception {
		Fragment target = 
			this.object.getByIds(set(this.targetId), SortOption.getDefault(), true).get(0);
		checkEagerFetching(target);
	}
}
