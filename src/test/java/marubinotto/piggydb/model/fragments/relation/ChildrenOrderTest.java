package marubinotto.piggydb.model.fragments.relation;

import static marubinotto.util.CollectionUtils.list;
import static marubinotto.util.time.DateTime.setCurrentTimeForTest;
import static org.junit.Assert.assertEquals;
import marubinotto.piggydb.model.Fragment;
import marubinotto.piggydb.model.FragmentRepository;
import marubinotto.piggydb.model.entity.RawFilter;
import marubinotto.piggydb.model.fragments.FragmentRepositoryTestBase;
import marubinotto.piggydb.model.query.FragmentsAllButTrash;
import marubinotto.piggydb.model.query.FragmentsByFilter;
import marubinotto.piggydb.model.query.FragmentsQuery;

import org.junit.Before;
import org.junit.Test;

public class ChildrenOrderTest extends FragmentRepositoryTestBase {
	
	private Long parentId;

	private Long relation1;
	private Long relation2;
	private Long relation3;
	private Long relation4;

	public ChildrenOrderTest(RepositoryFactory<FragmentRepository> factory) {
		super(factory);
	}
	
	@Before
	public void given() throws Exception {
		super.given();
		
		setCurrentTimeForTest(2008, 1, 1);
		this.parentId = this.object.register(newFragmentWithTags("parent"));
		
		setCurrentTimeForTest(2008, 1, 2);
		Long child1Id = this.object.register(newFragment());
		Long child2Id = this.object.register(newFragment());
		Long child3Id = this.object.register(newFragment());
		Long child4Id = this.object.register(newFragment());
		
		this.relation1 = this.object.createRelation(this.parentId, child1Id, getPlainUser());
		this.relation2 = this.object.createRelation(this.parentId, child2Id, getPlainUser());
		this.relation3 = this.object.createRelation(this.parentId, child3Id, getPlainUser());
		this.relation4 = this.object.createRelation(this.parentId, child4Id, getPlainUser());
		
		setCurrentTimeForTest(null);
		
		Fragment parent = this.object.get(this.parentId);
		this.object.updateChildRelationPriorities(parent, list(this.relation1, this.relation3), getPlainUser());
	}
	
	private void checkChildrenOrder(Fragment fragment) {
		assertEquals(4, fragment.getChildRelations().size());
		assertEquals(this.relation1, fragment.getChildRelations().get(0).getId());
		assertEquals(this.relation3, fragment.getChildRelations().get(1).getId());
		assertEquals(this.relation2, fragment.getChildRelations().get(2).getId());
		assertEquals(this.relation4, fragment.getChildRelations().get(3).getId());
	}
	
	@Test
	public void get() throws Exception {
		Fragment fragment = this.object.get(this.parentId);
		checkChildrenOrder(fragment);
	}
	
	@Test
	public void fragmentsAllButTrash() throws Exception {
		FragmentsQuery query = (FragmentsQuery)this.object.getQuery(FragmentsAllButTrash.class);
		query.setEagerFetching(true);
		Fragment fragment = query.getPage(5, 0).get(4); 	// the least recent
		
		checkChildrenOrder(fragment);
	}
	
	@Test
	public void fragmentsByFilter() throws Exception {
		FragmentsByFilter query = (FragmentsByFilter)this.object.getQuery(FragmentsByFilter.class);
		query.setEagerFetching(true);
		
		RawFilter filter = new RawFilter();
		filter.getIncludes().addTag(storedTag("parent"));
		query.setFilter(filter);
		
		Fragment fragment = query.getPage(5, 0).get(0);
		
		checkChildrenOrder(fragment);
	}
}
