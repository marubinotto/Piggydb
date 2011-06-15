package marubinotto.piggydb.impl.jdbc.h2;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

import java.io.File;

import marubinotto.piggydb.impl.jdbc.h2.H2JdbcUrl;

import org.junit.Test;
import org.springframework.mock.web.MockServletContext;

/**
 * JDBC URL = "jdbc:h2:" + databasePath + ";DB_CLOSE_DELAY=-1"
 * databasePath = databasePrefix + databaseName
 * (cf. jdbc:h2:[file:][<path>]<databaseName>)
 * (ex. jdbc:h2:file:/data/sample)
 */
public class H2JdbcUrlTest {

	private H2JdbcUrl object = new H2JdbcUrl();
	
	@Test
	public void databasePath_inMemory() throws Exception {
		this.object.setDatabasePrefix("mem:");
		this.object.setDatabaseName("database-name");		
		assertEquals("mem:database-name", this.object.getDatabasePath());
	}
	
	@Test
	public void databasePath_file() throws Exception {
		this.object.setDatabasePrefix("file:/dir/");
		this.object.setDatabaseName("database-name");
		assertEquals("file:/dir/database-name", this.object.getDatabasePath());
	}
	
	@Test
	public void databasePath_file_supplementPathSeparator() throws Exception {
		this.object.setDatabasePrefix("file:/dir");
		this.object.setDatabaseName("database-name");
		assertEquals("file:/dir/database-name", this.object.getDatabasePath());
	}
	
	/**
	 * It does not automatically escape characters that are illegal in URLs
	 * because H2 JDBC does not recognize escaped characters
	 */
	@Test
	public void databasePath_file_notUrlEscaping() throws Exception {
		this.object.setDatabasePrefix("file:/my docs");
		this.object.setDatabaseName("database-name");
		assertEquals("file:/my docs/database-name", this.object.getDatabasePath());
	}

	@Test
	public void databasePath_file_userHome() throws Exception {
		this.object.setDatabasePrefix("~/dir");
		this.object.setDatabaseName("database-name");
		String path1 = this.object.getDatabasePath();
		assertTrue(path1.matches("^file:/(.*)/dir/database-name$"));
		
		this.object.setDatabasePrefix("file:~/dir");
		this.object.setDatabaseName("database-name");
		String path2 = this.object.getDatabasePath();
		assertEquals(path1, path2);
	}

	@Test
	public void databaseName_default() throws Exception {
		assertEquals(H2JdbcUrl.DEFAULT_DATABASE_NAME, this.object.getDatabaseName());
	}
	
	@Test
	public void databaseName() throws Exception {
		this.object.setDatabaseName("database-name");	
		assertEquals("database-name", this.object.getDatabaseName());
	}
	
	@Test
	public void databaseName_fromDeployName() throws Exception {
		MockServletContext context = new MockServletContext();
		context.setContextPath("/context-name");
		this.object.setServletContext(context);
		assertEquals("context-name", this.object.getDatabaseName());
	}
	
	@Test
	public void databaseName_fromDeployName_root() throws Exception {
		MockServletContext context = new MockServletContext();
		context.setContextPath("");
		this.object.setServletContext(context);
		assertEquals(H2JdbcUrl.DEFAULT_DATABASE_NAME, this.object.getDatabaseName());
	}
	
	@Test
	public void databaseName_fromDeployName_onlySlash() throws Exception {
		MockServletContext context = new MockServletContext();
		context.setContextPath("/");
		this.object.setServletContext(context);		
		assertEquals(H2JdbcUrl.DEFAULT_DATABASE_NAME, this.object.getDatabaseName());
	}
	
	@Test
	public void toUrlWithoutEscape() throws Exception {
		String result = H2JdbcUrl.toUrlWithoutEscape(new File("/my docs"));
		System.out.println("toUrlWithoutEscape: " + result);
		assertTrue("URL should not be escaped", result.indexOf("my docs") != -1);
	}
}
