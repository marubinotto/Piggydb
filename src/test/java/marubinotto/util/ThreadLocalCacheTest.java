package marubinotto.util;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

public class ThreadLocalCacheTest {

	@Test
	public void cacheShouldBeSameInstanceInSameThread() throws Exception {
		String cached1 = ThreadLocalCache.get(String.class);
		String cached2 = ThreadLocalCache.get(String.class);
		assertSame(cached1, cached2);
	}
	
	@Test
	public void cacheShouldNotBeSameInstanceInDifferentThreads() throws Exception {
		final Map<String, String> sharedResult = new HashMap<String, String>();
		
		sharedResult.put("cached1", ThreadLocalCache.get(String.class));
		
		Thread thread = new Thread(new Runnable() {
			public void run() {
				sharedResult.put("cached2", ThreadLocalCache.get(String.class));
			}
		});
		thread.start();
		thread.join();
		
		assertNotSame(sharedResult.get("cached1"), sharedResult.get("cached2"));
	}
}
