package marubinotto.piggydb.model.fragments;

import static marubinotto.util.CollectionUtils.set;
import static marubinotto.util.time.DateTime.setCurrentTimeForTest;
import static org.junit.Assert.assertEquals;

import java.util.Set;

import marubinotto.piggydb.model.Fragment;
import marubinotto.piggydb.model.FragmentRepository;
import marubinotto.piggydb.model.FragmentsOptions;
import marubinotto.piggydb.model.Tag;
import marubinotto.piggydb.model.TagRepository;
import marubinotto.piggydb.model.entity.RawFilter;
import marubinotto.piggydb.model.enums.FragmentField;
import marubinotto.piggydb.model.query.FragmentsAllButTrash;
import marubinotto.piggydb.model.query.FragmentsQuery;
import marubinotto.util.paging.Page;
import marubinotto.util.time.DateTime;
import marubinotto.util.time.Month;

import org.junit.Before;
import org.junit.Test;

public class ExcludeTrashTest extends FragmentRepositoryTestBase {
	
	protected long id;
	
	public ExcludeTrashTest(RepositoryFactory<FragmentRepository> factory) {
		super(factory);
	}

	@Before
	public void given() throws Exception {
		super.given();
		
		TagRepository tagRepository = this.object.getTagRepository();
		tagRepository.register(newTag("important"));
		tagRepository.register(newTag("#trash"));
		
		Tag tempTag = newTag("temp");
		tempTag.addTagByUser("#trash", tagRepository, getOwner());
		tagRepository.register(tempTag);
		
		setCurrentTimeForTest(2008, 1, 1);
		this.id = this.object.register(newFragmentWithTitle("No tags"));
		
		setCurrentTimeForTest(2008, 1, 2);
		long id1 = this.object.register(
			newFragmentWithTitleAndTags("Piggydb is fun", "important"));
		
		setCurrentTimeForTest(2008, 1, 3);
		long id2 = this.object.register(
			newFragmentWithTitleAndTags("A trash", "#trash"));
		
		setCurrentTimeForTest(2008, 1, 4);
		long id3 = this.object.register(
			newFragmentWithTitleAndTags("A kind of trash", "temp"));
		
		this.object.createRelation(this.id, id1, getPlainUser());
		this.object.createRelation(this.id, id2, getPlainUser());
		this.object.createRelation(this.id, id3, getPlainUser());
		
		this.object.createRelation(id1, this.id, getPlainUser());
		this.object.createRelation(id2, this.id, getPlainUser());
		this.object.createRelation(id3, this.id, getPlainUser());
	}
	
	@Test
	public void sizeShouldIncludeTrashes() throws Exception {
		assertEquals(4, this.object.size());
	}
	
	@Test
	public void get() throws Exception {
		Fragment fragment = this.object.get(this.id);
		
		relationsShouldNotContainTrashes(fragment);
	}
	
	@Test
	public void getFragments() throws Exception {
		FragmentsQuery query = (FragmentsQuery)this.object.getQuery(FragmentsAllButTrash.class);
		query.setEagerFetching(true);
		Page<Fragment> results = query.getPage(5, 0);
		
		assertEquals(2, results.size());
		assertEquals("Piggydb is fun", results.get(0).getTitle());
		assertEquals("No tags", results.get(1).getTitle());
		
		relationsShouldNotContainTrashes(results.get(1));
	}
	
	private Page<Fragment> findByDate(DateTime date) throws Exception {
		return this.object.findByTime(
			date.toDayInterval(), 
			FragmentField.CREATION_DATETIME, 
			new FragmentsOptions(5, 0, true));
	}
	
	@Test
	public void findByDay20080101() throws Exception {
		Page<Fragment> results = findByDate(new DateTime(2008, 1, 1));
		
		assertEquals(1, results.size());
		assertEquals("No tags", results.get(0).getTitle());
		relationsShouldNotContainTrashes(results.get(0));
	}
	
	@Test
	public void findByDay20080102() throws Exception {
		Page<Fragment> results = findByDate(new DateTime(2008, 1, 2));
		
		assertEquals(1, results.size());
		assertEquals("Piggydb is fun", results.get(0).getTitle());
	}
	
	@Test
	public void findByDay20080103() throws Exception {
		Page<Fragment> results = findByDate(new DateTime(2008, 1, 3));
		
		assertEquals(0, results.size());
	}
	
	@Test
	public void findByDay20080104() throws Exception {
		Page<Fragment> results = findByDate(new DateTime(2008, 1, 4));
		
		assertEquals(0, results.size());
	}
	
	@Test
	public void findByEmptyFilter() throws Exception {
		Page<Fragment> results = 
			this.object.findByFilter(new RawFilter(), new FragmentsOptions(5, 0, true));
		
		assertEquals(4, results.size());
		assertEquals("A kind of trash", results.get(0).getTitle());
		assertEquals("A trash", results.get(1).getTitle());
		assertEquals("Piggydb is fun", results.get(2).getTitle());
		assertEquals("No tags", results.get(3).getTitle());
		
		relationsShouldNotContainTrashes(results.get(3));
	}
	
	private void relationsShouldNotContainTrashes(Fragment fragment) throws Exception {
		assertEquals(this.id, fragment.getId().longValue());
		
		assertEquals(1, fragment.getParentRelations().size());
		assertEquals("Piggydb is fun", fragment.getParentRelations().get(0).from.getTitle());
		
		assertEquals(1, fragment.getChildRelations().size());
		assertEquals("Piggydb is fun", fragment.getChildRelations().get(0).to.getTitle());
	}
	
	@Test
	public void getCreationDaysOfMonth() throws Exception {
		Set<Integer> days = this.object.getDaysOfMonth(
			FragmentField.CREATION_DATETIME, 
			new Month(new DateTime(2008, 1, 1)));
		assertEquals(set(1, 2), days);
	}
	
	@Test
	public void deleteTrashes() throws Exception {
		this.object.deleteTrashes(getPlainUser());
		
		assertEquals(2, this.object.size());
	}
}
