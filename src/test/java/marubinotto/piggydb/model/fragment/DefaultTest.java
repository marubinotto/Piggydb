package marubinotto.piggydb.model.fragment;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import marubinotto.piggydb.model.Tag;
import marubinotto.piggydb.model.entity.RawFragment;
import marubinotto.piggydb.model.entity.RawTag;
import marubinotto.util.time.DateTime;

import org.junit.Test;

public class DefaultTest {
	
	private RawFragment object = new RawFragment();

	@Test
	public void isTrash() throws Exception {
		assertFalse(this.object.isTrash());
		
		this.object.getMutableClassification().addTag(new RawTag(Tag.NAME_TRASH));
		assertTrue(this.object.isTrash());
	}
	
	@Test
	public void isPublic() throws Exception {
		assertFalse(this.object.isPublic());
		
		this.object.getMutableClassification().addTag(new RawTag(Tag.NAME_PUBLIC));
		assertTrue(this.object.isPublic());
	}

	@Test
	public void notUpdated() throws Exception {
		DateTime now = DateTime.getCurrentTime();
		this.object.setCreationDatetime(now);
		this.object.setUpdateDatetime(now);
		
		assertFalse(this.object.isUpdated());
	}
	
	@Test
	public void updated() throws Exception {
		this.object.setCreationDatetime(new DateTime(2009, 1, 1));
		this.object.setUpdateDatetime(new DateTime(2009, 1, 2));
		
		assertTrue(this.object.isUpdated());
	}
	
	@Test
	public void creator() throws Exception {
		this.object.setCreator("marubinotto");
		assertEquals("marubinotto", this.object.getCreator());
	}
	
	@Test
	public void creatorShouldBeOwnerIfNull() throws Exception {
		assertEquals("owner", this.object.getCreator());
	}
	
	@Test
	public void updater() throws Exception {
		this.object.setCreationDatetime(new DateTime(2009, 1, 1));
		this.object.setUpdateDatetime(new DateTime(2009, 1, 2));
		this.object.setUpdater("marubinotto");
		
		assertEquals("marubinotto", this.object.getUpdater());
	}
	
	@Test
	public void updaterShouldBeNullWhenNotYetUpdated() throws Exception {
		this.object.setCreationDatetime(new DateTime(2009, 1, 1));
		this.object.setUpdateDatetime(new DateTime(2009, 1, 1));
		
		assertNull(this.object.getUpdater());
	}
	
	@Test
	public void updaterShouldBeOwnerIfNullAndUpdated() throws Exception {
		this.object.setCreationDatetime(new DateTime(2009, 1, 1));
		this.object.setUpdateDatetime(new DateTime(2009, 1, 2));
		
		assertEquals("owner", this.object.getUpdater());
	}
	
	@Test
	public void lastUpdaterOrCreator_updater() throws Exception {
		this.object.setCreator("marubinotto");
		this.object.setUpdater("akane");
		
		assertEquals("akane", this.object.getLastUpdaterOrCreator());
	}
	
	@Test
	public void lastUpdaterOrCreator_creator() throws Exception {
		this.object.setCreator("marubinotto");
		this.object.setUpdater(null);
		
		assertEquals("marubinotto", this.object.getLastUpdaterOrCreator());
	}
}
