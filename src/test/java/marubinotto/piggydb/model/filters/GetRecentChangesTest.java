package marubinotto.piggydb.model.filters;

import static org.junit.Assert.assertEquals;
import marubinotto.piggydb.model.Filter;
import marubinotto.piggydb.model.FilterRepository;
import marubinotto.util.paging.Page;
import marubinotto.util.time.DateTime;

import org.junit.Before;
import org.junit.Test;

public class GetRecentChangesTest extends FilterRepositoryTestBase {

	protected long filter1Id;
	protected long filter2Id;
	
	public GetRecentChangesTest(RepositoryFactory<FilterRepository> factory) {
		super(factory);
	}
	
	@Before
	public void given() throws Exception {
		super.given();
		
		DateTime.setCurrentTimeForTest(new DateTime(2008, 1, 1));		
		this.filter1Id = this.object.register(newFilter("filter1"));
		
		DateTime.setCurrentTimeForTest(new DateTime(2008, 1, 2));		
		this.filter2Id = this.object.register(newFilter("filter2"));
		
		DateTime.setCurrentTimeForTest(null);
	}
	
	@Test
	public void fetching() throws Exception {
		// When
		Filter filter = this.object.getRecentChanges(3, 0).get(0);
		
		// Then
		assertEquals(this.filter2Id, filter.getId().longValue());
		assertEquals("filter2", filter.getName());
		assertEquals(new DateTime(2008, 1, 2), filter.getCreationDatetime());
		assertEquals(new DateTime(2008, 1, 2), filter.getUpdateDatetime());
	}
	
	@Test
	public void orderByUpdateDateDesc() throws Exception {
		// When
		Page<Filter> page = this.object.getRecentChanges(3, 0);
		
		// Then
		assertEquals(2, page.size());		
		assertEquals(this.filter2Id, page.get(0).getId().longValue());		
		assertEquals(this.filter1Id, page.get(1).getId().longValue());
	}
}
