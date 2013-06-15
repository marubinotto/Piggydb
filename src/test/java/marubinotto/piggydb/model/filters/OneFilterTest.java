package marubinotto.piggydb.model.filters;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import marubinotto.piggydb.model.Filter;
import marubinotto.piggydb.model.FilterRepository;
import marubinotto.piggydb.model.Tag;
import marubinotto.piggydb.model.TagRepository;
import marubinotto.piggydb.model.auth.User;
import marubinotto.piggydb.model.entity.RawTag;
import marubinotto.piggydb.model.exception.BaseDataObsoleteException;
import marubinotto.piggydb.model.exception.DuplicateException;
import marubinotto.util.time.DateTime;

import org.junit.Before;
import org.junit.Test;

public class OneFilterTest extends FilterRepositoryTestBase {

	protected long id;
	private DateTime registerDateTime;
	
	public OneFilterTest(RepositoryFactory<FilterRepository> factory) {
		super(factory);
	}
	
	@Before
	public void given() throws Exception {
		super.given();
		
		TagRepository tagRepository = this.object.getTagRepository();
		tagRepository.register(new RawTag("tag"));
		tagRepository.register(new RawTag("trash"));

		this.registerDateTime = new DateTime(2008, 1, 1);
		DateTime.setCurrentTimeForTest(registerDateTime);
		
		User user = new User("daisuke");
		Filter filter = this.object.newInstance(user);
		filter.setNameByUser("filter-name", user);
		filter.addIncludeByUser(tagRepository.getByName("tag"), user);
		filter.addExcludeByUser(tagRepository.getByName("trash"), user);
		
		this.id = this.object.register(filter);
		
		assertEquals(1, this.object.size());
		
		DateTime.setCurrentTimeForTest(null);
	}
	
	@Test
	public void getFilterById() throws Exception {
		Filter filter = this.object.get(this.id);
		
		assertNotNull(filter);
		assertEquals(new Long(this.id), filter.getId());
		assertEquals("filter-name", filter.getName());
		assertEquals(this.registerDateTime, filter.getCreationDatetime());
		assertEquals(this.registerDateTime, filter.getUpdateDatetime());
		assertEquals("daisuke", filter.getCreator());
		assertNull(filter.getUpdater());
		
		assertEquals(1, filter.getIncludes().size());
		assertTrue(filter.getIncludes().containsTagName("tag"));
		
		assertEquals(1, filter.getExcludes().size());
		assertTrue(filter.getExcludes().containsTagName("trash"));
	}
	
	@Test
	public void getFilterByName() throws Exception {
		Filter filter = this.object.getByName("filter-name");
		
		assertNotNull(filter);
		assertEquals(new Long(this.id), filter.getId());
		assertEquals("filter-name", filter.getName());
		assertEquals(this.registerDateTime, filter.getCreationDatetime());
		assertEquals(this.registerDateTime, filter.getUpdateDatetime());
		
		assertEquals(1, filter.getIncludes().size());
		assertTrue(filter.getIncludes().containsTagName("tag"));
		
		assertEquals(1, filter.getExcludes().size());
		assertTrue(filter.getExcludes().containsTagName("trash"));
	}
	
	@Test
	public void shouldReturnNullWhenNoSuchName() throws Exception {
		assertNull(this.object.getByName("no-such-name"));
	}
	
	@Test
	public void getIdByName() throws Exception {
		assertEquals(new Long(this.id), this.object.getIdByName("filter-name"));
	}
	
	@Test
	public void getNamesLikeSpecifiedCriteria() throws Exception {
		List<String> results = this.object.getNamesLike("f");
		assertEquals(1, results.size());
		assertTrue(results.contains("filter-name"));
		
		assertEquals(0, this.object.getNamesLike("x").size());
	}
	
	@Test
	public void updateFilterName() throws Exception {
		// Given
		DateTime updateDateTime = new DateTime(2008, 1, 2);
		DateTime.setCurrentTimeForTest(updateDateTime);

		// When
		Filter baseData = this.object.get(this.id);
		baseData.setNameByUser("new-filter-name", new User("akane"));
		
		boolean result = this.object.update(baseData);

		// Then
		assertTrue(result);
		assertEquals(updateDateTime, baseData.getUpdateDatetime());
		
		Filter storedData = this.object.get(this.id);
		assertEquals(updateDateTime, storedData.getUpdateDatetime());
		assertEquals("new-filter-name", storedData.getName());
		assertEquals("daisuke", storedData.getCreator());
		assertEquals("akane", storedData.getUpdater());
	}
	
	@Test
	public void updateNonexistentFilter() throws Exception {
		Filter baseData = this.object.get(this.id);
		this.object.delete(this.id, getPlainUser());

		boolean result = this.object.update(baseData);
		
		assertFalse(result);		
	}
	
	@Test(expected=BaseDataObsoleteException.class)
	public void shouldThrowExceptionWhenBaseDataAlreadyUpdated()
	throws Exception {
		// Given
		Filter baseData1 = this.object.get(this.id);
		
		Filter baseData2 = this.object.get(this.id);
		baseData2.setNameByUser("updated-first", new User("akane"));
		this.object.update(baseData2);
		
		// When
		baseData1.setNameByUser("cannot-store-this", new User("daisuke"));
		this.object.update(baseData1);
	}
	
	@Test
	public void whenTagNameHasBeenChanged() throws Exception {
		// Given
		TagRepository tagRepository = this.object.getTagRepository();
		Tag tag = tagRepository.getByName("tag");
		tag.setNameByUser("updated-tag", new User("daisuke"));
		tagRepository.update(tag);
		
		// When
		Filter filter = this.object.get(this.id);
		
		// Then
		assertEquals(1, filter.getIncludes().size());
		assertTrue(filter.getIncludes().containsTagName("updated-tag"));
	}
	
	@Test(expected=DuplicateException.class)
	public void shouldThrowExceptionWhenRegisterDuplicateName() throws Exception {
		this.object.register(newFilter("filter-name"));
	}
	
	@Test(expected=DuplicateException.class)
	public void shouldThrowExceptionWhenUpdateToDuplicateName() throws Exception {
		// Given
		this.object.register(newFilter("hogehoge"));

		// When
		Filter baseData = this.object.get(this.id);
		baseData.setNameByUser("hogehoge", getPlainUser());
		this.object.update(baseData);
	}
	
	@Test
	public void delete() throws Exception {
		// When
		this.object.delete(this.id, getPlainUser());
		
		// Then
		assertEquals(0, this.object.size());
	}
}
