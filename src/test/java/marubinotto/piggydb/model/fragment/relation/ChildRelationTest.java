package marubinotto.piggydb.model.fragment.relation;

import static marubinotto.piggydb.fixture.EntityFixtures.newFragmentWithTitle;
import static org.junit.Assert.assertEquals;

import java.util.List;

import marubinotto.piggydb.model.FragmentRelation;
import marubinotto.piggydb.model.Tag;
import marubinotto.piggydb.model.entity.RawFragment;
import marubinotto.piggydb.model.entity.RawTag;

import org.junit.Before;
import org.junit.Test;

public class ChildRelationTest {

	private RawFragment object = new RawFragment();
	
	@Before
	public void given() throws Exception {
		this.object.addChild(newFragmentWithTitle("private"));
		
		RawFragment fragment = newFragmentWithTitle("public");
		fragment.getMutableClassification().addTag(new RawTag(Tag.NAME_PUBLIC));
		this.object.addChild(fragment);
	}
	
	@Test
	public void getChildRelations() throws Exception {
		List<FragmentRelation> relations = this.object.getChildRelations();
		
		assertEquals(2, relations.size());
		assertEquals("private", relations.get(0).to.getTitle());
		assertEquals("public", relations.get(1).to.getTitle());
	}
	
	@Test
	public void getAllChildRelations() throws Exception {
		List<FragmentRelation> relations = this.object.getChildRelations(false);
		
		assertEquals(2, relations.size());
		assertEquals("private", relations.get(0).to.getTitle());
		assertEquals("public", relations.get(1).to.getTitle());
	}
	
	@Test
	public void getPublicChildRelationsOnly() throws Exception {
		List<FragmentRelation> relations = this.object.getChildRelations(true);
		
		assertEquals(1, relations.size());
		assertEquals("public", relations.get(0).to.getTitle());
	}
}
