package marubinotto.piggydb.model.files;

import static marubinotto.util.CollectionUtils.set;
import static org.junit.Assert.assertEquals;
import marubinotto.piggydb.fixture.mock.FileItemMock;
import marubinotto.piggydb.model.FileRepository;
import marubinotto.piggydb.model.entity.RawFragment;

import org.junit.Before;
import org.junit.Test;

public class OneNoExtensionFile extends FileRepositoryTestBase {
	
	private RawFragment fragment;
	
	public OneNoExtensionFile(RepositoryFactory<FileRepository> factory) {
		super(factory);
	}

	@Before
	public void given() throws Exception {
		super.given();
		
		this.fragment = new RawFragment();
		this.fragment.setId(1L);
		this.fragment.setFileInput(new FileItemMock("file", "/path/to/file", "hello".getBytes()));

		this.object.putFile(this.fragment);
	}
	
	@Test
	public void getFileNames() throws Exception {
		assertEquals(set("1"), this.object.getFileNames());
	}

	@Test
	public void replace() throws Exception {
		// When
		this.fragment.setFileInput(
			new FileItemMock("file", "/path/to/file.png", "image".getBytes()));
		this.object.putFile(fragment);
		
		// Then
		assertEquals(1, this.object.size());
		assertEquals(set("1.png"), this.object.getFileNames());
	}
}
