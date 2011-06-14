package marubinotto.piggydb.external;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import marubinotto.piggydb.fixture.mock.FileItemMock;
import marubinotto.piggydb.model.FileRepository;
import marubinotto.piggydb.model.entity.RawFragment;
import marubinotto.util.FileSystemUtils;
import marubinotto.util.RdbUtils;
import marubinotto.util.ZipUtils;
import marubinotto.util.fixture.FileSystemFixture;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;

public class PigDumpTest {

	private PigDump object = new PigDump();
	
	private DataSource dataSource = RdbUtils.getInMemoryDataSource(null);
	private FileRepository fileRepository = new FileRepository.InMemory();
	
	private File testDir;
	private File pigDumpFile;
	
	@Before
	public void given() throws Exception {	
		// Set up fixtures

		this.object.setDataSource(this.dataSource);
		this.object.setFileRepository(this.fileRepository);
		
		registerFile(1, "/path/to/file.txt", "hello");
		
		// Output a pig dump file
		
		this.testDir = FileSystemUtils.getEmptyDirectory();
		this.pigDumpFile = new File(this.testDir, "test.pig");

		OutputStream output = FileUtils.openOutputStream(this.pigDumpFile);
        try {
        	this.object.outputDump(output);
        }
        finally {
        	output.close();
        }
	}
	
	private void registerFile(long id, String fileName, String content) 
	throws Exception{
		RawFragment fragment = new RawFragment();
		fragment.setId(id);
		fragment.setFileInput(new FileItemMock("file", fileName, content.getBytes()));
		this.fileRepository.putFile(fragment);
	}

	@Test
	public void contentOfDumpFile() throws Exception {
        FileSystemFixture extractedZip = new FileSystemFixture(new File(this.testDir, "extractedZip"));
		extractedZip.getBaseDirectory().mkdir();
		
		ZipUtils.extract(
			this.pigDumpFile, 
			null, 
			PigDump.FILE_NAME_ENCODING,
			new ZipUtils.Directory(extractedZip.getBaseDirectory()));
		
		Map<String, String> files = new HashMap<String, String>();
		files.put("rdb-dump.xml", null);
		files.put("files/1.txt", "hello");
		extractedZip.shouldEqual(files, "UTF-8");
	}
	
	@Test
	public void checkDumpFile() throws Exception {
		assertTrue(this.object.checkDumpFile(this.pigDumpFile));
		
		File notPig = FileSystemUtils.createFile(this.testDir, "not-pig", "content");
		assertFalse(this.object.checkDumpFile(notPig));
	}
	
	@Test
	public void restore() throws Exception {
		// Given
		this.fileRepository.clear();
		
		// When
		this.object.restore(this.pigDumpFile);
		
		// Then
		assertEquals(1, this.fileRepository.size());
	}
}
