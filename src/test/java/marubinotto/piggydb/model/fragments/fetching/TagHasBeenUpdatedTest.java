package marubinotto.piggydb.model.fragments.fetching;

import static marubinotto.piggydb.model.Assert.assertClassificationEquals;
import static marubinotto.util.CollectionUtils.list;
import static marubinotto.util.CollectionUtils.set;
import static marubinotto.util.time.DateTime.setCurrentTimeForTest;
import static org.junit.Assert.assertEquals;
import marubinotto.piggydb.model.Fragment;
import marubinotto.piggydb.model.FragmentRepository;
import marubinotto.piggydb.model.Tag;
import marubinotto.piggydb.model.entity.RawFilter;
import marubinotto.piggydb.model.entity.RawFragment;
import marubinotto.piggydb.model.enums.FragmentField;
import marubinotto.piggydb.model.fragments.FragmentRepositoryTestBase;
import marubinotto.piggydb.model.query.FragmentsAllButTrash;
import marubinotto.piggydb.model.query.FragmentsByFilter;
import marubinotto.piggydb.model.query.FragmentsByIds;
import marubinotto.piggydb.model.query.FragmentsByTime;
import marubinotto.piggydb.model.query.FragmentsByUser;
import marubinotto.piggydb.model.query.FragmentsQuery;
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
	public void fragmentsAllButTrash() throws Exception {
		FragmentsQuery query = (FragmentsQuery)this.object.getQuery(FragmentsAllButTrash.class);
		query.setEagerFetching(true);
		Fragment fragment = query.getPage(3, 0).get(0);
		
		checkTargetFragment(fragment);
	}
	
	@Test
	public void fragmentsByTime() throws Exception {
		FragmentsByTime query = (FragmentsByTime)this.object.getQuery(FragmentsByTime.class);
		query.setCriteria(
			new DateTime(2010, 1, 2).toDayInterval(),
			FragmentField.CREATION_DATETIME);
		query.setEagerFetching(true);
		
		Fragment fragment = query.getPage(2, 0).get(0);
		
		checkTargetFragment(fragment);
	}
	
	@Test
	public void fragmentsByFilter() throws Exception {
		FragmentsByFilter query = (FragmentsByFilter)this.object.getQuery(FragmentsByFilter.class);
		query.setEagerFetching(true);
		
		RawFilter filter = new RawFilter();
		filter.getClassification().addTag(storedTag("renamed"));
		query.setFilter(filter);
		
		Fragment fragment = query.getPage(3, 0).get(0);
		
		checkTargetFragment(fragment);
	}
	
	@Test
	public void fragmentsByUser() throws Exception {
		FragmentsByUser query = (FragmentsByUser)this.object.getQuery(FragmentsByUser.class);
		query.setUserName(getPlainUser().getName());
		query.setEagerFetching(true);
		
		Fragment fragment = query.getPage(3, 0).get(0);
		
		checkTargetFragment(fragment);
	}
	
	@Test
	public void fragmentsByIds() throws Exception {
		FragmentsByIds query = (FragmentsByIds)this.object.getQuery(FragmentsByIds.class);
		query.setIds(set(this.id));
		query.setEagerFetching(true);
		
		Fragment fragment = query.getAll().get(0);
		
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
