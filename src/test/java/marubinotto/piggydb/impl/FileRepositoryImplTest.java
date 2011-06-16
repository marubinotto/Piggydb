package marubinotto.piggydb.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.File;

import marubinotto.util.FileSystemUtils;

import org.junit.Before;
import org.junit.Test;

public class FileRepositoryImplTest {

	private FileRepositoryImpl object;
	
	@Before
	public void given() {
		this.object = new FileRepositoryImpl();
	}
	
	@Test
	public void databasePath_onMemory() throws Exception {
		this.object.setDatabasePath("mem:piggydb");
		assertNull(this.object.getBaseDirectory());
	}
	
	@Test
	public void databasePath_userHome() throws Exception {
		this.object.setDatabasePath("~/dir/dbname");
		assertEquals("dbname-files", this.object.getBaseDirectory().getName());
	}
	
	@Test
	public void databasePath_fileUrl() throws Exception {
		File dir = FileSystemUtils.getEmptyDirectory();
		this.object.setDatabasePath(dir.toURI() + "dbname");	
		assertEquals(new File(dir, "dbname-files"), this.object.getBaseDirectory());
	}
}
