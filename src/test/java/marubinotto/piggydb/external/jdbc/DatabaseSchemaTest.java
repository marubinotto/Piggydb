package marubinotto.piggydb.external.jdbc;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import javax.sql.DataSource;

import marubinotto.piggydb.fixture.table.AllTables;
import marubinotto.util.RdbUtils;
import marubinotto.util.fixture.DatabaseTableFixture;

import org.junit.Before;
import org.junit.Test;
import org.springframework.jdbc.core.JdbcTemplate;

public class DatabaseSchemaTest {

	private DatabaseSchema object = new DatabaseSchema();
	
	private DataSource dataSource = RdbUtils.getInMemoryDataSource(null);
	private JdbcTemplate jdbcTemplate = new JdbcTemplate(this.dataSource);
	
	@Before
	public void given() throws Exception {
		this.object.setJdbcTemplate(this.jdbcTemplate);
	}
	
	@Test
	public void shouldReturnVersionZeroByDefault() throws Exception {
		assertThat(this.object.getVersion(), is(0));
	}
	
	@Test
	public void shouldUpdateSchemaVersionFromZero() throws Exception {
		assertThat(this.object.getVersion(), is(0));
		this.object.update();
		assertTrue(this.object.getVersion() > 0);
	}
	
	@Test
	public void allTables() throws Exception {
		AllTables allTables = new AllTables(this.dataSource);
		for (DatabaseTableFixture table : allTables.getTables()) {
			System.out.println(table.getTableName());
		}
	}
}
