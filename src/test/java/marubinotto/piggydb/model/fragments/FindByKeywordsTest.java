package marubinotto.piggydb.model.fragments;

import static marubinotto.util.time.DateTime.setCurrentTimeForTest;
import static org.junit.Assert.assertEquals;
import marubinotto.piggydb.external.jdbc.h2.H2FragmentRepository;
import marubinotto.piggydb.fixture.mock.FileItemMock;
import marubinotto.piggydb.model.Fragment;
import marubinotto.piggydb.model.FragmentRepository;
import marubinotto.piggydb.model.FragmentsOptions;
import marubinotto.util.paging.Page;

import org.junit.Before;
import org.junit.Test;

public class FindByKeywordsTest extends FragmentRepositoryTestBase {
	
	protected Long id1;
	protected Long id2;
	protected Long id3;
	
	public FindByKeywordsTest(RepositoryFactory<FragmentRepository> factory) {
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
		
		Fragment fileFragment = this.object.newInstance(getPlainUser());
		fileFragment.setFileInput(new FileItemMock("file", "/path/to/akane.png", new byte[0]));
		this.id3 = this.object.register(fileFragment);
	}
	
	@Test
	public void nullKeyword() throws Exception {
		Page<Fragment> page = this.object.findByKeywords(
			null, new FragmentsOptions(5, 0, false));
		assertEquals(0, page.size());
	}
	
	@Test
	public void blankKeyword() throws Exception {
		Page<Fragment> page = this.object.findByKeywords(
			"  ", new FragmentsOptions(5, 0, false));
		assertEquals(0, page.size());
	}
	
	@Test
	public void zeroHit() throws Exception {
		Page<Fragment> page = this.object.findByKeywords(
			"hogehoge", new FragmentsOptions(5, 0, false));
		assertEquals(0, page.size());
	}
	
	@Test
	public void title() throws Exception {
		if (!needsToBeTested()) return;
		
		Page<Fragment> page = this.object.findByKeywords(
			"creation", new FragmentsOptions(5, 0, false));
		
		assertEquals(1, page.size());
		assertEquals(this.id1, page.get(0).getId());
	}
	
	@Test
	public void content() throws Exception {
		if (!needsToBeTested()) return;
		
		Page<Fragment> page = this.object.findByKeywords(
			"application", new FragmentsOptions(5, 0, false));
		
		assertEquals(1, page.size());
		assertEquals(this.id2, page.get(0).getId());
	}
	
	@Test
	public void titleAndContent() throws Exception {
		if (!needsToBeTested()) return;
		
		Page<Fragment> page = this.object.findByKeywords(
			"knowledge", new FragmentsOptions(5, 0, false));
		
		assertEquals(2, page.size());
		assertEquals(this.id2, page.get(0).getId());
		assertEquals(this.id1, page.get(1).getId());
	}
	
	@Test
	public void fileName() throws Exception {
		if (!needsToBeTested()) return;
		
		Page<Fragment> page = this.object.findByKeywords(
			"akane", new FragmentsOptions(5, 0, false));
		
		assertEquals(1, page.size());
		assertEquals(this.id3, page.get(0).getId());
	}
	
	@Test
	public void fileExtension() throws Exception {
		if (!needsToBeTested()) return;
		
		Page<Fragment> page = this.object.findByKeywords(
			"png", new FragmentsOptions(5, 0, false));
		
		assertEquals(1, page.size());
		assertEquals(this.id3, page.get(0).getId());
	}

// Private
	
	private boolean needsToBeTested() {
		return this.object instanceof H2FragmentRepository;
	}
}
