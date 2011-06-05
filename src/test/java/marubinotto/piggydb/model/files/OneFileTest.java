package marubinotto.piggydb.model.files;

import static marubinotto.util.CollectionUtils.set;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;

import marubinotto.piggydb.fixture.mock.FileItemMock;
import marubinotto.piggydb.model.FileRepository;
import marubinotto.piggydb.model.entity.RawFragment;

import org.junit.Before;
import org.junit.Test;

public class OneFileTest extends FileRepositoryTestBase {
	
	private byte[] file;
	private RawFragment fragment;
	
	public OneFileTest(RepositoryFactory<FileRepository> factory) {
		super(factory);
	}

	@Before
	public void given() throws Exception {
		super.given();
		
		this.file = "hello".getBytes();
		
		this.fragment = new RawFragment();
		this.fragment.setId(1L);
		this.fragment.setFileInput(new FileItemMock("file", "/path/to/file.txt", this.file));

		this.object.putFile(this.fragment);
	}
	
	@Test
	public void sizeShouldBeOne() throws Exception {
		assertEquals(1, this.object.size());
	}
	
	@Test
	public void getFileNames() throws Exception {
		assertEquals(set("1.txt"), this.object.getFileNames());
	}
	
	@Test
	public void getFile() throws Exception {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		this.object.getFile(output, this.fragment);
		
		assertArrayEquals(this.file, output.toByteArray());
	}
	
	@Test
	public void deleteFile() throws Exception {
		this.object.deleteFile(this.fragment);
		assertEquals(0, this.object.size());
	}
	
	@Test
	public void replaceWithDifferentExtension() throws Exception {
		// When
		byte[] content = "image".getBytes();
		this.fragment.setFileInput(new FileItemMock("file", "/path/to/file.png", content));
		this.object.putFile(fragment);
		
		// Then
		assertEquals(1, this.object.size());
		assertEquals(set("1.png"), this.object.getFileNames());
		
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		this.object.getFile(output, this.fragment);
		assertArrayEquals(content, output.toByteArray());
	}
}
