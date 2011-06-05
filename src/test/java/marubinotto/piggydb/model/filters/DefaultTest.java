package marubinotto.piggydb.model.filters;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import marubinotto.piggydb.model.Filter;
import marubinotto.piggydb.model.FilterRepository;
import marubinotto.piggydb.model.User;
import marubinotto.util.time.DateTime;

import org.junit.Test;

public class DefaultTest extends FilterRepositoryTestBase {
	
	public DefaultTest(RepositoryFactory<FilterRepository> factory) {
		super(factory);
	}
	
	@Test
	public void newInstance() throws Exception {
		User user = getPlainUser();
		Filter filter = this.object.newInstance(user);
		
		assertEquals(user.getName(), filter.getCreator());
		assertNull(filter.getUpdater());
	}

	@Test
	public void registerFilter() throws Exception {
		// Given
		DateTime registerDateTime = new DateTime(2008, 1, 1);
		DateTime.setCurrentTimeForTest(registerDateTime);

		// When
		Filter filter = this.object.newInstance(getPlainUser());
		filter.setNameByUser("new-filter", getPlainUser());
		long filterId = this.object.register(filter);
		
		// Then
		assertEquals(filterId, filter.getId().longValue());
		assertEquals(registerDateTime, filter.getCreationDatetime());
		assertEquals(registerDateTime, filter.getUpdateDatetime());

		// The post conditions for the repository is described by OneFilterTest
	}
}
