package marubinotto.piggydb.model.fragments;

import static marubinotto.util.time.DateTime.setCurrentTimeForTest;
import static org.junit.Assert.assertEquals;
import marubinotto.piggydb.fixture.mock.FileItemMock;
import marubinotto.piggydb.impl.H2FragmentRepository;
import marubinotto.piggydb.model.Fragment;
import marubinotto.piggydb.model.FragmentRepository;
import marubinotto.piggydb.model.query.FragmentsByKeywords;
import marubinotto.util.paging.Page;

import org.junit.Before;
import org.junit.Test;

public class FragmentsByKeywordsTest extends FragmentRepositoryTestBase {
	
	protected Long id1;
	protected Long id2;
	protected Long id3;
	
	public FragmentsByKeywordsTest(RepositoryFactory<FragmentRepository> factory) {
		super(factory);
	}

	@Before
	public void given() throws Exception {
		super.given();

		setCurrentTimeForTest(2009, 7, 1);
		this.id1 = this.object.register(newFragmentWithTitle("A knowledge creation system"));
		
		setCurrentTimeForTest(2009, 7, 2);
		Fragment fragment = this.object.newInstance(getPlainUser());
		fragment.setContentByUser(
			"Piggydb is an easy-to-use Web application for building a personal knowledge repository. ", 
			getPlainUser());
		this.id2 = this.object.register(fragment);
		
		setCurrentTimeForTest(2009, 7, 3);
		Fragment fileFragment = this.object.newInstance(getPlainUser());
		fileFragment.setFileInput(new FileItemMock("file", "/path/to/akane.png", new byte[0]));
		this.id3 = this.object.register(fileFragment);
	}
	
	private FragmentsByKeywords getQuery() throws Exception {
		return (FragmentsByKeywords)this.object.getQuery(FragmentsByKeywords.class);
	}
	
	private void assertItContainsAll(Page<Fragment> page) {
	  assertEquals(3, page.size());
    assertEquals(this.id3, page.get(0).getId());
    assertEquals(this.id2, page.get(1).getId());
    assertEquals(this.id1, page.get(2).getId());
	}
	
	@Test
	public void nullKeyword() throws Exception {
		FragmentsByKeywords query = getQuery();
		query.setKeywords(null);
		Page<Fragment> page = query.getPage(5, 0);
		
		assertItContainsAll(page);
	}
	
	@Test
	public void blankKeyword() throws Exception {
		FragmentsByKeywords query = getQuery();
		query.setKeywords("  ");
		Page<Fragment> page = query.getPage(5, 0);
		
		assertItContainsAll(page);
	}
	
	@Test
	public void zeroHit() throws Exception {
		FragmentsByKeywords query = getQuery();
		query.setKeywords("hogehoge");
		Page<Fragment> page = query.getPage(5, 0);
		
		assertEquals(0, page.size());
	}
	
	@Test
	public void title() throws Exception {
		if (!needsToBeTested()) return;
		
		FragmentsByKeywords query = getQuery();
		query.setKeywords("creation");
		Page<Fragment> page = query.getPage(5, 0);
		
		assertEquals(1, page.size());
		assertEquals(this.id1, page.get(0).getId());
	}
	
	@Test
	public void content() throws Exception {
		if (!needsToBeTested()) return;
		
		FragmentsByKeywords query = getQuery();
		query.setKeywords("application");
		Page<Fragment> page = query.getPage(5, 0);
		
		assertEquals(1, page.size());
		assertEquals(this.id2, page.get(0).getId());
	}
	
	@Test
	public void titleAndContent() throws Exception {
		if (!needsToBeTested()) return;
		
		FragmentsByKeywords query = getQuery();
		query.setKeywords("knowledge");
		Page<Fragment> page = query.getPage(5, 0);
		
		assertEquals(2, page.size());
		assertEquals(this.id2, page.get(0).getId());
		assertEquals(this.id1, page.get(1).getId());
	}
	
	@Test
	public void fileName() throws Exception {
		if (!needsToBeTested()) return;
				
		FragmentsByKeywords query = getQuery();
		query.setKeywords("akane");
		Page<Fragment> page = query.getPage(5, 0);
		
		assertEquals(1, page.size());
		assertEquals(this.id3, page.get(0).getId());
	}
	
	@Test
	public void fileExtension() throws Exception {
		if (!needsToBeTested()) return;
		
		FragmentsByKeywords query = getQuery();
		query.setKeywords("png");
		Page<Fragment> page = query.getPage(5, 0);
		
		assertEquals(1, page.size());
		assertEquals(this.id3, page.get(0).getId());
	}

// Private
	
	private boolean needsToBeTested() {
		return this.object instanceof H2FragmentRepository;
	}
}
