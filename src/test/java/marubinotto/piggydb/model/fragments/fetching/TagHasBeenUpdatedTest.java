package marubinotto.piggydb.model.fragments.fetching;

import static marubinotto.piggydb.model.Assert.assertClassificationEquals;
import static marubinotto.util.CollectionUtils.list;
import static marubinotto.util.CollectionUtils.set;
import static marubinotto.util.time.DateTime.setCurrentTimeForTest;
import static org.junit.Assert.assertEquals;
import marubinotto.piggydb.model.Fragment;
import marubinotto.piggydb.model.FragmentRepository;
import marubinotto.piggydb.model.FragmentsOptions;
import marubinotto.piggydb.model.Tag;
import marubinotto.piggydb.model.entity.RawFilter;
import marubinotto.piggydb.model.entity.RawFragment;
import marubinotto.piggydb.model.enums.FragmentField;
import marubinotto.piggydb.model.fragments.FragmentRepositoryTestBase;
import marubinotto.piggydb.model.query.FragmentsAllButTrash;
import marubinotto.piggydb.model.query.FragmentsQuery;
import marubinotto.piggydb.model.query.FragmentsSortOption;
import marubinotto.util.time.DateTime;

import org.junit.Before;
import org.junit.Test;

public class TagHasBeenUpdatedTest extends FragmentRepositoryTestBase {
	
	private Long id;

	public TagHasBeenUpdatedTest(RepositoryFactory<FragmentRepository> factory) {
		super(factory);
	}
	
	@Before
	public void given() throws Exception {
		super.given();
		
		setCurrentTimeForTest(2010, 1, 1);
		long childId = this.object.register(newFragmentWithTitleAndTags("child", "tag"));
		
		setCurrentTimeForTest(2010, 1, 2);
		this.id = this.object.register(newFragmentWithTitleAndTags("title", "tag"));
		
		this.object.createRelation(this.id, childId, getPlainUser());
		setCurrentTimeForTest(null);

		// update the tag
		Tag tag = this.object.getTagRepository().getByName("tag");
		tag.setNameByUser("renamed", getPlainUser());
		this.object.getTagRepository().update(tag);
	}
	
	private void checkTargetFragment(Fragment fragment) {
		assertEquals(this.id, fragment.getId());
		assertClassificationEquals(set("renamed"),  fragment.getClassification());
	}
	
	@Test
	public void getById() throws Exception {
		Fragment fragment = this.object.get(this.id);
		checkTargetFragment(fragment);
	}
	
	@Test
	public void child() throws Exception {
		Fragment fragment = this.object.get(this.id);
		Fragment child = fragment.getChildren().get(0);
		
		assertClassificationEquals(set("renamed"),  child.getClassification());
	}
	
	@Test
	public void getFragments() throws Exception {
		FragmentsQuery query = (FragmentsQuery)this.object.getQuery(FragmentsAllButTrash.class);
		query.setEagerFetching(true);
		Fragment fragment = query.getPage(3, 0).get(0);
		
		checkTargetFragment(fragment);
	}
	
	@Test
	public void findByTime() throws Exception {
		Fragment fragment = this.object.findByTime(
			new DateTime(2010, 1, 2).toDayInterval(),
			FragmentField.CREATION_DATETIME,
			new FragmentsOptions(2, 0, true)).get(0);
		checkTargetFragment(fragment);
	}
	
	@Test
	public void findByFilter() throws Exception {
		RawFilter filter = new RawFilter();
		filter.getClassification().addTag(storedTag("renamed"));
		Fragment fragment = this.object.findByFilter(
			filter, new FragmentsOptions(3, 0, true)).get(0);
		checkTargetFragment(fragment);
	}
	
	@Test
	public void findByUser() throws Exception {
		Fragment fragment = this.object.findByUser(
			getPlainUser().getName(), new FragmentsOptions(3, 0, true)).get(0);
		checkTargetFragment(fragment);
	}
	
	@Test
	public void getByIds() throws Exception {
		Fragment fragment = 
			this.object.getByIds(set(this.id), FragmentsSortOption.getDefault(), true).get(0);
		checkTargetFragment(fragment);
	}
	
	@Test
	public void refreshClassifications() throws Exception {
		RawFragment fragment = new RawFragment();
		fragment.setId(this.id);
		this.object.refreshClassifications(list(fragment));
		
		checkTargetFragment(fragment);
	}
}
