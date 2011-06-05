package marubinotto.piggydb.model.fragments;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;

import marubinotto.piggydb.fixture.mock.FileItemMock;
import marubinotto.piggydb.model.Fragment;
import marubinotto.piggydb.model.FragmentRepository;

import org.junit.Before;
import org.junit.Test;

public class OneFileFragmentTest extends FragmentRepositoryTestBase {
	
	private byte[] file = "hello".getBytes();
	private long id;
	
	public OneFileFragmentTest(RepositoryFactory<FragmentRepository> factory) {
		super(factory);
	}
	
	@Before
	public void given() throws Exception {
		super.given();
		
		Fragment fragment = newFragment();
		fragment.setFileInput(new FileItemMock("file", "/path/to/file.txt", file));
		
		this.id = this.object.register(fragment);
		
		assertEquals(1, this.object.size());
		assertEquals(1, this.fileRepository.size());
	}
	
	@Test
	public void fileDataShouldBeStored() throws Exception {
		Fragment fragment = this.object.get(this.id);
		
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		this.fileRepository.getFile(output, fragment);
		
		assertArrayEquals(this.file, output.toByteArray());
	}

	@Test
	public void getById() throws Exception {
		Fragment fragment = this.object.get(this.id);
		assertTrue(fragment.isFile());
		assertEquals("file.txt", fragment.getFileName());
		assertEquals("txt", fragment.getFileType());
		assertEquals(5, fragment.getFileSize().getValue());
	}
	
	@Test
	public void update() throws Exception {
		// When
		Fragment baseData = this.object.get(this.id);
		byte[] newFile = "bye".getBytes();
		baseData.setFileInput(new FileItemMock("file", "/path/to/new-file.png", newFile));
		boolean result = this.object.update(baseData);
		
		// Then
		assertTrue(result);
		Fragment storedData = this.object.get(this.id);
		assertTrue(storedData.isFile());
		assertEquals("new-file.png", storedData.getFileName());
		assertEquals("png", storedData.getFileType());
		assertEquals(3, storedData.getFileSize().getValue());
		
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		this.fileRepository.getFile(output, storedData);
		assertArrayEquals(newFile, output.toByteArray());
	}
	
	@Test
	public void updateWithoutFileItem() throws Exception {
		// When
		Fragment baseData = this.object.get(this.id);
		boolean result = this.object.update(baseData);
		
		// Then
		assertTrue(result);
		Fragment storedData = this.object.get(this.id);		
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		this.fileRepository.getFile(output, storedData);
		assertArrayEquals(this.file, output.toByteArray());
	}
	
	@Test
	public void delete() throws Exception {
		// When
		this.object.delete(this.id, getPlainUser());
		
		// Then
		assertEquals(0, this.object.size());
		assertEquals(0, this.fileRepository.size());
	}
}
