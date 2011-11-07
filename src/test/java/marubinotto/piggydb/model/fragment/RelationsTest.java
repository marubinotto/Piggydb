package marubinotto.piggydb.model.fragment;

import static marubinotto.piggydb.fixture.EntityFixtures.fragment;
import static marubinotto.piggydb.fixture.EntityFixtures.newFragmentWithTitle;
import static marubinotto.util.CollectionUtils.list;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.List;

import marubinotto.piggydb.model.FragmentRelation;
import marubinotto.piggydb.model.Tag;
import marubinotto.piggydb.model.entity.RawFragment;
import marubinotto.piggydb.model.entity.RawTag;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Enclosed;

@RunWith(Enclosed.class)
public class RelationsTest {
	
	private static class TestBase {
		protected RawFragment object = new RawFragment();
	}

	public static class DefaultTest extends TestBase {
		
		@Test
		public void getParentRelationByParentId() throws Exception {
			this.object.addParent(fragment(1));
			this.object.addParent(fragment(2));
				
			FragmentRelation relation = this.object.getParentRelationByParentId(100);
			assertNull(relation);
			
			relation = this.object.getParentRelationByParentId(1L);
			assertEquals(1, relation.from.getId().longValue());
		}
	}
	
	public static class GetChildRelationsTest extends TestBase {
		
		@Before
		public void given() throws Exception {
			this.object.addChild(newFragmentWithTitle("private"));
			
			RawFragment fragment = newFragmentWithTitle("public");
			fragment.getMutableClassification().addTag(new RawTag(Tag.NAME_PUBLIC));
			this.object.addChild(fragment);
		}
		
		@Test
		public void get() throws Exception {
			List<FragmentRelation> relations = this.object.getChildRelations();
			
			assertEquals(2, relations.size());
			assertEquals("private", relations.get(0).to.getTitle());
			assertEquals("public", relations.get(1).to.getTitle());
		}
		
		@Test
		public void getAll() throws Exception {
			List<FragmentRelation> relations = this.object.getChildRelations(false);
			
			assertEquals(2, relations.size());
			assertEquals("private", relations.get(0).to.getTitle());
			assertEquals("public", relations.get(1).to.getTitle());
		}
		
		@Test
		public void getPublicOnly() throws Exception {
			List<FragmentRelation> relations = this.object.getChildRelations(true);
			
			assertEquals(1, relations.size());
			assertEquals("public", relations.get(0).to.getTitle());
		}
	}
	
	public static class SortChildRelationsTest extends TestBase {
		
		private static FragmentRelation createChildRelation(Long id, Integer priority) {
			FragmentRelation relation = new FragmentRelation();
			relation.setId(id);
			relation.priority = priority;
			relation.to = new RawFragment(); 
			return relation;
		}
		
		private List<Long> getChildrenOrder() {
			List<Long> order = new ArrayList<Long>();
			for (FragmentRelation relation : this.object.getChildRelations()) {
				order.add(relation.getId());
			}
			return order;
		}
		
		@Test
		public void sortByPriority() throws Exception {
			this.object.addChildRelation(createChildRelation(1L, 1));
			this.object.addChildRelation(createChildRelation(2L, 2));
			
			this.object.sortChildRelations();
			
			assertEquals(list(2L, 1L), getChildrenOrder());
		}
		
		@Test
		public void sortByIdWhenPriorityIsSame() throws Exception {
			this.object.addChildRelation(createChildRelation(1L, 0));
			this.object.addChildRelation(createChildRelation(2L, 0));
			
			this.object.sortChildRelations();
			
			assertEquals(list(1L, 2L), getChildrenOrder());
		}
		
		@Test
		public void nullPriorityIsLow() throws Exception {
			this.object.addChildRelation(createChildRelation(1L, null));
			this.object.addChildRelation(createChildRelation(2L, 0));
			
			this.object.sortChildRelations();
			
			assertEquals(list(2L, 1L), getChildrenOrder());
		}
	}
}
