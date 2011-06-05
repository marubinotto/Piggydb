package marubinotto.util;

import static org.junit.Assert.*;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

public class FileSystemUtilsTest {

	@Test
	public void getEmptyDirectory() throws Exception {
		// Get an empty directory
		File path = FileSystemUtils.getEmptyDirectory();
		assertEmptyDirectory(path);
		
		// Put a file on the directory
		FileUtils.writeStringToFile(new File(path, "file"), "hello");
		assertEquals(1, path.list().length);
		
		// Re-get an empty directory
		path = FileSystemUtils.getEmptyDirectory();
		assertEmptyDirectory(path);
	}
	
	private void assertEmptyDirectory(File path) {
		assertTrue(path.isDirectory());
		assertEquals(0, path.list().length);
	}
	
	@Test
	public void getRelativePathOfFileInBaseDir() throws Exception {
		File baseDir = FileSystemUtils.getEmptyDirectory();
		File targetFile = new File(baseDir, "file");
        assertEquals("file", FileSystemUtils.getRelativePath(baseDir, targetFile));
	}
	
	@Test
	public void getRelativePathOfFileInSubDir() throws Exception {
		File baseDir = FileSystemUtils.getEmptyDirectory();
		File targetFile = new File(baseDir, "subdir" + File.separator + "file");
        assertEquals("subdir/file", FileSystemUtils.getRelativePath(baseDir, targetFile));
	}
	
	@Test
	public void getPackageDirectory() throws Exception {
		File path = FileSystemUtils.getPackageDirectory(FileSystemUtilsTest.class);
		assertTrue(path.isDirectory());
		assertTrue(new File(path, "FileSystemUtilsTest.class").isFile());
	}
	
	@Test
	public void getFile() throws Exception {
		File path = FileSystemUtils.getFile(
			FileSystemUtilsTest.class, "FileSystemUtilsTest.class");
		assertTrue(path.isFile());
	}
	
	@Test
	public void getUserHome() throws Exception {
		System.out.println("UserHome(true): " + FileSystemUtils.getUserHome(true));
		System.out.println("UserHome(false): " + FileSystemUtils.getUserHome(false));
	}
}
