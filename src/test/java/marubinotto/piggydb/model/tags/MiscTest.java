package marubinotto.piggydb.model.tags;

import static org.junit.Assert.assertEquals;
import marubinotto.piggydb.model.Tag;
import marubinotto.piggydb.model.TagRepository;

import org.junit.Test;

public class MiscTest extends TagRepositoryTestBase {
	
	public MiscTest(RepositoryFactory<TagRepository> factory) {
		super(factory);
	}
	
	@Test
	public void modifyOriginalObjectAfterRegistration() throws Exception {
		// Given
		Tag originalTag = newTag("tag");
		long tagId = this.object.register(originalTag);

		// When
		originalTag.setNameByUser("tag-modified", getPlainUser());
		
		// Then
		assertEquals("tag", this.object.get(tagId).getName());
	}
}
