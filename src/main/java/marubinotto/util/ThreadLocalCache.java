package marubinotto.util;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.UnhandledException;

public class ThreadLocalCache extends ThreadLocal<Map<String, Object>> {
	
	public static ThreadLocalCache INSTANCE = new ThreadLocalCache();
	
	private ThreadLocalCache() {
	}

	protected synchronized Map<String, Object> initialValue() {
		return new HashMap<String, Object>();
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T get(Class<T> target) {
		Assert.Arg.notNull(target, "target");
		
		Map<String, Object> cache = INSTANCE.get();
		T object = (T)cache.get(target.getName());
		if (object == null) {
			try {
				object = target.newInstance();
			}
			catch (Exception e) {
				throw new UnhandledException(e);
			}
			cache.put(target.getName(), object);
		}
		return object;
	}
}
