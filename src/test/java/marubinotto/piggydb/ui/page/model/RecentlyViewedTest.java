package marubinotto.piggydb.ui.page.model;

import static marubinotto.piggydb.fixture.EntityFixtures.newFragmentWithTitle;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import marubinotto.piggydb.external.jdbc.h2.InMemoryDatabase;
import marubinotto.piggydb.model.FilterRepository;
import marubinotto.piggydb.model.FragmentRepository;
import marubinotto.piggydb.model.TagRepository;
import marubinotto.piggydb.model.User;
import marubinotto.piggydb.ui.page.model.RecentlyViewed;
import marubinotto.piggydb.ui.page.model.RecentlyViewed.Entity;

import org.junit.Test;

public class RecentlyViewedTest {

	@Test
	public void entityEquity() throws Exception {
		assertTrue(new RecentlyViewed.Entity(1, 1).equals(new RecentlyViewed.Entity(1, 1)));
		assertFalse(new RecentlyViewed.Entity(1, 2).equals(new RecentlyViewed.Entity(1, 1)));
	}
	
	private RecentlyViewed object = new RecentlyViewed(3);
	
	private InMemoryDatabase database = new InMemoryDatabase();
	private FragmentRepository fragmentRepository = database.getFragmentRepository();
	protected TagRepository tagRepository = database.getTagRepository();
	private FilterRepository filterRepository = database.getFilterRepository();
	
	@Test
	public void oneFragment() throws Exception {
		// Given		
		long id = fragmentRepository.register(newFragmentWithTitle("hello"));
		this.object.add(new RecentlyViewed.Entity(RecentlyViewed.TYPE_FRAGMENT, id));
		
		// When
		LinkedHashMap<Entity, String> result = 
			this.object.getAllWithNames(
				this.fragmentRepository, 
				this.tagRepository, 
				this.filterRepository);
		
		// Then
		assertEquals(1, result.size());
		
		Iterator<Entry<Entity, String>> entries = result.entrySet().iterator();
		
		Entry<Entity, String> entry1 = entries.next();
		assertEquals(RecentlyViewed.TYPE_FRAGMENT, entry1.getKey().type);
		assertEquals(id, entry1.getKey().id);
		assertEquals("hello", entry1.getValue());
	}
	
	@Test
	public void oneTag() throws Exception {
		// Given
		long id = this.tagRepository.register(
			this.tagRepository.newInstance("piggydb", new User("daisuke")));
		this.object.add(new RecentlyViewed.Entity(RecentlyViewed.TYPE_TAG, id));
		
		// When
		LinkedHashMap<Entity, String> result = 
			this.object.getAllWithNames(
				this.fragmentRepository, 
				this.tagRepository, 
				this.filterRepository);
		
		// Then
		assertEquals(1, result.size());
		
		Iterator<Entry<Entity, String>> entries = result.entrySet().iterator();
		
		Entry<Entity, String> entry1 = entries.next();
		assertEquals(RecentlyViewed.TYPE_TAG, entry1.getKey().type);
		assertEquals(id, entry1.getKey().id);
		assertEquals("piggydb", entry1.getValue());
	}
	
	@Test
	public void oneFragmentAndOneTag() throws Exception {
		// Given
		long fragmentId = fragmentRepository.register(newFragmentWithTitle("hello"));
		long tagId = tagRepository.register(
			this.tagRepository.newInstance("piggydb", new User("daisuke")));
		
		this.object.add(new RecentlyViewed.Entity(RecentlyViewed.TYPE_FRAGMENT, fragmentId));
		this.object.add(new RecentlyViewed.Entity(RecentlyViewed.TYPE_TAG, tagId));
		
		// When
		LinkedHashMap<Entity, String> result = 
			this.object.getAllWithNames(
				this.fragmentRepository, 
				this.tagRepository, 
				this.filterRepository);
		
		// Then
		assertEquals(2, result.size());
		
		Iterator<Entry<Entity, String>> entries = result.entrySet().iterator();
		
		Entry<Entity, String> entry1 = entries.next();
		assertEquals(RecentlyViewed.TYPE_TAG, entry1.getKey().type);
		assertEquals(tagId, entry1.getKey().id);
		assertEquals("piggydb", entry1.getValue());
		
		Entry<Entity, String> entry2 = entries.next();
		assertEquals(RecentlyViewed.TYPE_FRAGMENT, entry2.getKey().type);
		assertEquals(fragmentId, entry2.getKey().id);
		assertEquals("hello", entry2.getValue());
	}
	
	@Test
	public void invalidId() throws Exception {
		// Given
		this.object.add(new RecentlyViewed.Entity(RecentlyViewed.TYPE_FRAGMENT, 123L));
		
		// When
		LinkedHashMap<Entity, String> result = 
			this.object.getAllWithNames(
				this.fragmentRepository, 
				this.tagRepository, 
				this.filterRepository);
		
		// Then
		assertEquals(0, result.size());
	}
}
