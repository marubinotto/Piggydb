package marubinotto.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ognl.Ognl;
import ognl.OgnlException;

public class CollectionUtils {

	public static <E> List<E> list(E... elements) {
		return new ArrayList<E>(Arrays.asList(elements));
	}

	public static <E> Set<E> set(E... elements) {
		return new HashSet<E>(Arrays.asList(elements));
	}

	public static <K, V> MapBuilder<K, V> map(K key, V value) {
		return new MapBuilder<K, V>().map(key, value);
	}

	public static class MapBuilder<K, V> extends HashMap<K, V> {
		public MapBuilder() {
		}

		public MapBuilder<K, V> map(K key, V value) {
			put(key, value);
			return this;
		}
	}

	public static <K, V> void pileValue(Map<K, List<V>> map, K key, V value) {
		Assert.Arg.notNull(map, "map");
		Assert.Arg.notNull(value, "value");

		List<V> values = map.get(key);
		if (values == null) {
			values = new ArrayList<V>();
			map.put(key, values);
		}
		values.add(value);
	}

	public static <T> List<T> inReverseOrder(Collection<T> src) {
		LinkedList<T> result = new LinkedList<T>();
		for (T element : src)
			result.addFirst(element);
		return result;
	}

	@SuppressWarnings("unchecked")
	public static <K, V> Map<K, V> makeMap(Collection<V> collection, String keyByOgnl) 
	throws OgnlException {
		Map<K, V> map = new LinkedHashMap<K, V>();
		for (V value : collection) {
			K key = (K)Ognl.getValue(keyByOgnl, value);
			if (map.containsKey(key)) {
				throw new IllegalArgumentException("Duplicate key [" + keyByOgnl + "]: " + key);
			}
			map.put(key, value);
		}
		return map;
	}

	@SuppressWarnings("unchecked")
	public static <E> List<E> covariantCast(List<? extends E> list) {
		return (List<E>)list;
	}
}
