package marubinotto.piggydb.model.fragment;

import static marubinotto.piggydb.model.Assert.assertClassificationEquals;
import static marubinotto.util.CollectionUtils.map;
import marubinotto.piggydb.model.auth.User;
import marubinotto.piggydb.model.entity.RawFragment;
import marubinotto.piggydb.model.entity.RawTag;

import org.junit.Before;
import org.junit.Test;

public class RemoveTagsClassifiedAsTest {

	private RawFragment object = new RawFragment();
	
	@Before
	public void given() throws Exception {
		RawTag bookmark = new RawTag("bookmark");
		RawTag important = new RawTag("important");
		important.getMutableClassification().addTag(bookmark);
		
		this.object.getMutableClassification().addTag(important);
		this.object.getMutableClassification().addTag(new RawTag("hogehoge"));
	}
	
	
	@Test
	public void removeDirectly() throws Exception {
		this.object.removeTagsByUserClassifiedAs("hogehoge", new User());
		
		assertClassificationEquals(
			map("important", map("bookmark", null)), 
			this.object.getClassification());
	}
	
	@Test
	public void removeIndirectly() throws Exception {
		this.object.removeTagsByUserClassifiedAs("bookmark", new User());
		
		assertClassificationEquals(
			map("hogehoge", null), 
			this.object.getClassification());
	}
}
