package marubinotto.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileFilter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import marubinotto.util.fixture.FileSystemFixture;

import org.junit.Before;
import org.junit.Test;

public class ZipUtilsTest {
	
	private File testDir;
	
	private File zipFilePath;
	private String nameEncoding = "UTF-8";

	@Before
    public void given() throws Exception {
		this.testDir = FileSystemUtils.getEmptyDirectory();
		
		File baseDir = new File(this.testDir, "baseDir");
		FileSystemUtils.createFile(baseDir, "file-in-base", "Akane");
		FileSystemUtils.createFile(baseDir, "sub/file-in-sub", "Daisuke");
		FileSystemUtils.createFile(baseDir, "sub/should-be-excluded", "Piggydb");
		
		this.zipFilePath = new File(this.testDir, "test.zip");
		ZipUtils.zipDirectory(
	    	"root/",
	        baseDir,
	        new FileFilter() {
                public boolean accept(File pathname) {
                    return !pathname.getName().equals("should-be-excluded");
                }
            },
            this.zipFilePath ,
	       	this.nameEncoding);
	}
	
	@Test
	public void isZip() throws Exception {
		assertTrue(ZipUtils.isZip(this.zipFilePath));
		
		File notZip = FileSystemUtils.createFile(this.testDir, "not-zip", "content");
		assertFalse(ZipUtils.isZip(notZip));
	}
	
	@Test
	public void containsEntry() throws Exception {
		assertTrue(ZipUtils.containsEntry(this.zipFilePath, "root/file-in-base", this.nameEncoding));
		assertTrue(ZipUtils.containsEntry(this.zipFilePath, "root/sub/file-in-sub", this.nameEncoding));
		assertFalse(ZipUtils.containsEntry(this.zipFilePath, "root/sub/should-be-excluded", this.nameEncoding));
	}
	
	@Test
	public void getEntryNames() throws Exception {
		List<String> names = ZipUtils.getEntryNames(this.zipFilePath, this.nameEncoding);
		assertEquals(2, names.size());
		assertTrue(names.contains("root/file-in-base"));
		assertTrue(names.contains("root/sub/file-in-sub"));
	}
	
	@Test
	public void extractAll() throws Exception {
		FileSystemFixture toDir = new FileSystemFixture(new File(this.testDir, "toDir"));
		toDir.getBaseDirectory().mkdir();
		
		ZipUtils.extract(
			this.zipFilePath, 
			null, 
			this.nameEncoding, 
			new ZipUtils.Directory(toDir.getBaseDirectory()));
		
		Map<String, String> files = new HashMap<String, String>();
		files.put("root/file-in-base", "Akane");
		files.put("root/sub/file-in-sub", "Daisuke");
		toDir.shouldEqual(files, "UTF-8");
	}
	
	@Test
	public void extractPartly() throws Exception {
		FileSystemFixture toDir = new FileSystemFixture(new File(this.testDir, "toDir"));
		toDir.getBaseDirectory().mkdir();
		
		ZipUtils.extract(
			this.zipFilePath, 
			"root/sub/", 
			this.nameEncoding, 
			new ZipUtils.Directory(toDir.getBaseDirectory()));
		
		Map<String, String> files = new HashMap<String, String>();
		files.put("file-in-sub", "Daisuke");
		toDir.shouldEqual(files, "UTF-8");
	}
	
	@Test
	public void extractNothing() throws Exception {
		FileSystemFixture toDir = new FileSystemFixture(new File(this.testDir, "toDir"));
		toDir.getBaseDirectory().mkdir();
		
		ZipUtils.extract(
			this.zipFilePath, 
			"no/such/", 
			this.nameEncoding, 
			new ZipUtils.Directory(toDir.getBaseDirectory()));
		
		toDir.shouldBeEmpty();
	}
}
