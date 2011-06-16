package marubinotto.piggydb.ui.page.model;

import static marubinotto.piggydb.fixture.EntityFixtures.newFragmentWithTitle;
import static org.junit.Assert.assertEquals;
import marubinotto.piggydb.impl.InMemoryDatabase;
import marubinotto.piggydb.model.Fragment;
import marubinotto.piggydb.model.FragmentRepository;
import marubinotto.piggydb.ui.page.model.SelectedFragments;
import marubinotto.util.paging.Page;

import org.junit.Before;
import org.junit.Test;

public class SelectedFragmentsTest {

	private SelectedFragments object = new SelectedFragments();
	
	private FragmentRepository repository = 
		new InMemoryDatabase().getFragmentRepository();	
	private Long id1;
	private Long id2;
	private Long id3;
	
	@Before
	public void given() throws Exception {
		this.id1 = this.repository.register(newFragmentWithTitle("fragment1"));
		this.id2 = this.repository.register(newFragmentWithTitle("fragment2"));
		this.id3 = this.repository.register(newFragmentWithTitle("fragment3"));
	}
	
	@Test
	public void getFragmentsInReverseOrder() throws Exception {
		this.object.add(this.id1);
		this.object.add(this.id2);
		this.object.add(this.id3);
		
		Page<Fragment> fragments = this.object.getFragments(this.repository, 10, 0, false);
		
		assertEquals(3, fragments.size());
		assertEquals(this.id3, fragments.get(0).getId());
		assertEquals(this.id2, fragments.get(1).getId());
		assertEquals(this.id1, fragments.get(2).getId());
	}
}
