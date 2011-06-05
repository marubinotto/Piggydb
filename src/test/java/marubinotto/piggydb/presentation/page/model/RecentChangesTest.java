package marubinotto.piggydb.presentation.page.model;

import static marubinotto.util.CollectionUtils.list;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class RecentChangesTest {

	private RecentChanges<Integer> object = new RecentChanges<Integer>(3);
		
	@Test
	public void add() throws Exception {
		// When
		this.object.add(1);
		this.object.add(2);
		
		// Then
		assertEquals(list(2, 1), object.getRecentChanges());
	}
	
	@Test
	public void addOverMaxSize() throws Exception {
		// When
		this.object.add(1);
		this.object.add(2);
		this.object.add(3);
		this.object.add(4);
	
		// Then
		assertEquals(list(4, 3, 2), object.getRecentChanges());
	}
	
	@Test
	public void addSameIdTwice() throws Exception {
		// When
		this.object.add(1);
		this.object.add(2);
		this.object.add(1);
		
		// Then
		assertEquals(list(1, 2), object.getRecentChanges());
	}
}
