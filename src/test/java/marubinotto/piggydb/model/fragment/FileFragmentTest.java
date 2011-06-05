package marubinotto.piggydb.model.fragment;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import marubinotto.piggydb.fixture.mock.FileItemMock;
import marubinotto.piggydb.model.entity.RawFragment;

import org.apache.commons.fileupload.FileItem;
import org.junit.Test;

public class FileFragmentTest {

	private RawFragment object = new RawFragment();
	
	@Test
	public void setFileItem() throws Exception {
		// When
		FileItem fileItem = new FileItemMock("file", "/path/to/file.txt", "hello".getBytes());
		this.object.setFileInput(fileItem);
		
		// Then
		assertSame(fileItem, this.object.fileInput);
		
		assertEquals("file.txt", this.object.getFileName());
		assertEquals("txt", this.object.getFileType());
		assertEquals(5, this.object.getFileSize().getValue());
		
		assertTrue(this.object.isFile());
		assertEquals("text/plain", this.object.getMimeType());
		assertFalse(this.object.isImageFile());
	}
	
	@Test
	public void imageFile() throws Exception {
		this.object.setFileInput(new FileItemMock("file", "/path/to/file.png", "hello".getBytes()));
		
		assertEquals("image/png", this.object.getMimeType());
		assertTrue(this.object.isImageFile());
	}
	
	@Test
	public void fileWithoutExtension() throws Exception {
		this.object.setFileInput(new FileItemMock("file", "/path/to/file", "hello".getBytes()));
		
		assertEquals("file", this.object.getFileName());
		assertNull(this.object.getFileType());
		
		assertTrue(this.object.isFile());
		assertNull(this.object.getMimeType());
		assertFalse(this.object.isImageFile());
	}
	
	@Test
	public void fileWithExceptionalExtension() throws Exception {
		this.object.setFileInput(new FileItemMock("file", "/path/to/file.hogehoge", "hello".getBytes()));
		
		assertTrue(this.object.isFile());
		assertNull(this.object.getMimeType());
		assertFalse(this.object.isImageFile());
	}
}
