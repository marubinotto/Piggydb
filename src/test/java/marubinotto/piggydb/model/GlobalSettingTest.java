package marubinotto.piggydb.model;

import static junit.framework.Assert.assertEquals;
import static marubinotto.util.time.DateTime.setCurrentTimeForTest;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

import java.util.List;

import marubinotto.piggydb.impl.InMemoryDatabase;
import marubinotto.util.time.DateTime;

import org.junit.Test;
import org.junit.runners.Parameterized.Parameters;

public class GlobalSettingTest extends RepositoryTestBase<GlobalSetting> {
	
	public GlobalSettingTest(RepositoryFactory<GlobalSetting> factory) {
		super(factory);
	}

	@Parameters
	public static List<Object[]> factories() {
		return toParameters(
			// RI
			new RepositoryFactory<GlobalSetting>() {
				public GlobalSetting create() throws Exception {
					return new GlobalSetting.InMemory();
				}
			},
			// Database
			new RepositoryFactory<GlobalSetting>() {
				public GlobalSetting create() throws Exception {
					return new InMemoryDatabase().getGlobalSetting();
				}
			}
		);
	}
	
	@Test
	public void noSuchEntry() throws Exception {
		assertNull(this.object.get("no-such-entry"));
	}
	
	@Test
	public void putAndGet() throws Exception {
		this.object.put("message", "Hello world");		
		assertThat(this.object.get("message"), is("Hello world"));
	}
	
	
	// DatabaseTitle
	
	@Test
	public void defaultDatabaseTitle() throws Exception {
		assertNull(this.object.getDatabaseTitle());
	}
	
	@Test
	public void setDatabaseTitle() throws Exception {
		this.object.setDatabaseTitle("Table Tennis Videos");
		assertEquals("Table Tennis Videos", this.object.getDatabaseTitle());
	}
	
	
	// DatabaseId
	
	@Test
	public void getDatabaseIdInitially() throws Exception {
		setCurrentTimeForTest(new DateTime(2010, 1, 1));
		
		String id = this.object.getDatabaseId();
		setCurrentTimeForTest(null);
		
		assertEquals("tag:piggydb.net,2009:db-201001010000000", id);
	}
	
	@Test
	public void getDatabaseIdWithTimestamp() throws Exception {
		this.object.put(GlobalSetting.GSK_DATABASE_TIMESTAMP, "1234567890");
		
		String id = this.object.getDatabaseId();
		
		assertEquals("tag:piggydb.net,2009:db-1234567890", id);
	}
}
