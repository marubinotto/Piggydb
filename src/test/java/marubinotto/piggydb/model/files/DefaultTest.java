package marubinotto.piggydb.model.files;

import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import marubinotto.piggydb.model.FileRepository;
import marubinotto.piggydb.model.entity.RawFragment;

import org.junit.Test;

public class DefaultTest extends FileRepositoryTestBase {
	
	public DefaultTest(RepositoryFactory<FileRepository> factory) {
		super(factory);
	}
	
	@Test
	public void sizeShouldBeZero() throws Exception {
		assertEquals(0, this.object.size());
	}
	
	@Test
	public void getFileNames() throws Exception {
		assertTrue(this.object.getFileNames().isEmpty());
	}
	
	@Test
	public void getEntryReader() throws Exception {
		// When
		byte[] file = "hello".getBytes();
		ByteArrayInputStream input = new ByteArrayInputStream(file);
		
		this.object.getEntryReader().readEntry("1.txt", input);
		
		// Then
		assertEquals(1, this.object.size());
		
		RawFragment fragment = new RawFragment();
		fragment.setId(1L);
		fragment.setFileType("txt");
		
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		this.object.getFile(output, fragment);
		assertArrayEquals(file, output.toByteArray());
	}
}
