package marubinotto.util.rdb;

import static junit.framework.Assert.assertTrue;
import static org.custommonkey.xmlunit.XMLAssert.assertXMLEqual;
import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;

import javax.sql.DataSource;

import marubinotto.util.RdbUtils;

import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Before;
import org.junit.Test;
import org.springframework.jdbc.core.JdbcTemplate;

public class InMemoryDataSourceTest {

	private DataSource dataSource = RdbUtils.getInMemoryDataSource(null);
	private JdbcTemplate jdbcTemplate = new JdbcTemplate(this.dataSource);
	
	@Before
	public void given() throws Exception {
		this.jdbcTemplate.update(
			"create table test (" +
			"name varchar, " +
			"value varchar, " +
			"primary key (name));");
		this.jdbcTemplate.update("insert into test (name, value) values('version', '1');");
	}
	
	@Test
	public void query() throws Exception {
		assertEquals(
			"[{NAME=version, VALUE=1}]", 
			this.jdbcTemplate.queryForList("select * from test;").toString());
	}
	
	@Test
	public void exportAllAsXml() throws Exception {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		RdbUtils.exportAllAsXml(this.dataSource.getConnection(), out);

		XMLUnit.setIgnoreWhitespace(true);
		assertXMLEqual(
			"<dataset>" +
				"<table name=\"TEST\">" +
					"<column>NAME</column>" +
					"<column>VALUE</column>" +
					"<row>" +
						"<value>version</value>" +
						"<value>1</value>" +
					"</row>" +
				"</table>" +
			"</dataset>", 
			new String(out.toByteArray()));
	}
	
	@Test
	public void deleteAll() throws Exception {
		RdbUtils.deleteAll(this.dataSource.getConnection(), "test");
		assertTrue(this.jdbcTemplate.queryForList("select * from test;").isEmpty());
	}
	
	@Test
	public void cleanInsert() throws Exception {
		RdbUtils.cleanInsert(this.dataSource.getConnection(), "test", new String[][]{
            {"name", "value"},
            {"language", "Japanese"}
        });
		
		assertEquals(
			"[{NAME=language, VALUE=Japanese}]", 
			this.jdbcTemplate.queryForList("select * from test;").toString());
	}
}
