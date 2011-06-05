package marubinotto.piggydb.model.files;

import static junit.framework.Assert.assertTrue;
import static marubinotto.util.CollectionUtils.set;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import marubinotto.piggydb.fixture.mock.FileItemMock;
import marubinotto.piggydb.model.FileRepository;
import marubinotto.piggydb.model.entity.RawFragment;
import marubinotto.util.FileSystemUtils;
import marubinotto.util.ZipUtils;
import marubinotto.util.fixture.FileSystemFixture;

import org.apache.commons.io.FileUtils;
import org.apache.tools.zip.ZipOutputStream;
import org.junit.Before;
import org.junit.Test;

public class MultipleFilesTest extends FileRepositoryTestBase {
	
	public static final String FILE_NAME_ENCODING = "UTF-8";
	
	protected File testDir;
	
	public MultipleFilesTest(RepositoryFactory<FileRepository> factory) {
		super(factory);
	}

	@Before
	public void given() throws Exception {
		super.given();
		
		this.testDir = FileSystemUtils.getEmptyDirectory();
		
		registerFile(1, "/path/to/file.txt", "hello");
		registerFile(2, "/path/to/no-extension", "bye");
	}
	
	@Test
	public void sizeShouldBeTwo() throws Exception {
		assertEquals(2, this.object.size());
	}
	
	@Test
	public void getFileNames() throws Exception {
		assertEquals(set("1.txt", "2"), this.object.getFileNames());
	}
	
	@Test
	public void outputAll() throws Exception {
		// When
		File zipFile = new File(this.testDir, "dump.zip");
		ZipOutputStream zipOut = new ZipOutputStream(FileUtils.openOutputStream(zipFile));
        try {
        	zipOut.setEncoding(FILE_NAME_ENCODING);
        	this.object.outputAll("files/", zipOut);
        }
        finally {
        	zipOut.close();
        }
        
        // Then
		FileSystemFixture extractedZip = new FileSystemFixture(new File(this.testDir, "extractedZip"));
		extractedZip.getBaseDirectory().mkdir();
		
		ZipUtils.extract(
			zipFile, 
			null, 
			FILE_NAME_ENCODING, 
			new ZipUtils.Directory(extractedZip.getBaseDirectory()));
		
		Map<String, String> files = new HashMap<String, String>();
		files.put("files/1.txt", "hello");
		files.put("files/2", "bye");
		extractedZip.shouldEqual(files, "UTF-8");
	}
	
	@Test
	public void clear() throws Exception {
		this.object.clear();
		assertEquals(0, this.object.size());
		assertTrue(this.object.getFileNames().isEmpty());
	}

	@Test
	public void replace() throws Exception {
		// When
		RawFragment fragment = new RawFragment();
		fragment.setId(1L);
		fragment.setFileInput(new FileItemMock("file", "/path/to/file.png", "image".getBytes()));
		this.object.putFile(fragment);
		
		// Then
		assertEquals(2, this.object.size());
		assertEquals(set("1.png", "2"), this.object.getFileNames());
	}}
