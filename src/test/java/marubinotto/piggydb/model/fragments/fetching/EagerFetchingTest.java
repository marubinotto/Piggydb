package marubinotto.piggydb.model.fragments.fetching;

import static marubinotto.piggydb.model.Assert.assertClassificationEquals;
import static marubinotto.util.CollectionUtils.map;
import static marubinotto.util.CollectionUtils.set;
import static marubinotto.util.time.DateTime.setCurrentTimeForTest;
import static org.junit.Assert.assertEquals;
import marubinotto.piggydb.impl.H2FragmentRepository;
import marubinotto.piggydb.model.Fragment;
import marubinotto.piggydb.model.FragmentRepository;
import marubinotto.piggydb.model.TagRepository;
import marubinotto.piggydb.model.entity.RawFilter;
import marubinotto.piggydb.model.enums.FragmentField;
import marubinotto.piggydb.model.fragments.FragmentRepositoryTestBase;
import marubinotto.piggydb.model.query.FragmentsAllButTrash;
import marubinotto.piggydb.model.query.FragmentsByFilter;
import marubinotto.piggydb.model.query.FragmentsByIds;
import marubinotto.piggydb.model.query.FragmentsByKeywords;
import marubinotto.piggydb.model.query.FragmentsByTime;
import marubinotto.piggydb.model.query.FragmentsByUser;
import marubinotto.piggydb.model.query.FragmentsQuery;
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
	
	private void assertEagerFetched(Fragment target) throws Exception {
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
	
	@SuppressWarnings("unchecked")
  private <Q extends FragmentsQuery> Q createQuery(Class<Q> queryType) 
	throws Exception {
	  Q query = (Q)this.object.getQuery(queryType);
	  query.setEagerFetching(true);
	  return query;
	}
	
	@Test
	public void fragmentsAllButTrash() throws Exception {
		FragmentsQuery query = createQuery(FragmentsAllButTrash.class);
		
		assertEagerFetched(query.getPage(1, 0).get(0));
	}
	
	@Test
	public void fragmentsByTime() throws Exception {
		FragmentsByTime query = createQuery(FragmentsByTime.class);
		query.setCriteria(
			new DateTime(2010, 1, 3).toDayInterval(),
			FragmentField.CREATION_DATETIME);
		
		assertEagerFetched(query.getPage(1, 0).get(0));
	}
	
	@Test
	public void fragmentsByFilter() throws Exception {
		FragmentsByFilter query = createQuery(FragmentsByFilter.class);
		query.setFilter(new RawFilter());
		
		assertEagerFetched(query.getPage(1, 0).get(0));
	}
	
	@Test
	public void fragmentsByUser() throws Exception {
		FragmentsByUser query = createQuery(FragmentsByUser.class);
		query.setUserName(getPlainUser().getName());
		
		assertEagerFetched(query.getPage(1, 0).get(0));
	}
	
	@Test
	public void fragmentsByKeywords() throws Exception {
		if (this.object instanceof H2FragmentRepository) {
			FragmentsByKeywords query = createQuery(FragmentsByKeywords.class);
			query.setKeywords("target");
			
			assertEagerFetched(query.getPage(1, 0).get(0));
		}
	}
	
	@Test
	public void fragmentsByIds() throws Exception {
		FragmentsByIds query = createQuery(FragmentsByIds.class);
		query.setIds(set(this.targetId));
		
		assertEagerFetched(query.getAll().get(0));
	}
}
