package marubinotto.piggydb.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import marubinotto.piggydb.model.base.Entity;
import marubinotto.util.Assert;

public class ModelUtils {
	
	public static List<Long> toIds(Collection<? extends Entity> entities) {
		List<Long> ids = new ArrayList<Long>();
		for (Entity entity : entities) ids.add(entity.getId());
		return ids;
	}

	public static List<Fragment> collectChildrenOfEach(List<? extends Fragment> fragments) {
		Assert.Arg.notNull(fragments, "fragments");
		
		List<Fragment> children = new ArrayList<Fragment>();
		for (Fragment fragment : fragments) children.addAll(fragment.getChildren());
		return children;
	}
	
	public static <E extends Entity> Map<Long, E> toIdMap(Collection<? extends E> entities) {
		Assert.Arg.notNull(entities, "entities");
		
		Map<Long, E> map = new HashMap<Long, E>();
		for (E e : entities) {
			if (!map.containsKey(e.getId())) map.put(e.getId(), e);
		}
		return map;
	}
	
	public static <E extends Entity> List<E> getByIds(List<Long> ids, Collection<? extends E> entities) {
		Assert.Arg.notNull(ids, "ids");
		Assert.Arg.notNull(entities, "entities");
		
		Map<Long, E> idMap = toIdMap(entities);
		List<E> result = new ArrayList<E>();
		for (Long id : ids) {
			if (idMap.containsKey(id)) result.add(idMap.get(id));
		}
		return result;
	}
	
	public static Set<Tag> getCommonTags(List<? extends Classifiable> classifiables) {
		Assert.Arg.notNull(classifiables, "classifiables");
		
		Set<Tag> tags = new HashSet<Tag>();
		if (classifiables.isEmpty()) return tags;
		
		tags.addAll(classifiables.get(0).getClassification().getTags());
		if (classifiables.size() == 1) return tags;
		
		for (int i = 1; i < classifiables.size(); i++) {
			tags.retainAll(classifiables.get(i).getClassification().getTags());
		}
		return tags;
	}
	
	public static List<Fragment> getCommonParents(List<? extends Fragment> fragments) {
		Assert.Arg.notNull(fragments, "fragments");
		
		List<Fragment> commonParents = new ArrayList<Fragment>();
		if (fragments.isEmpty()) return commonParents;
		
		Map<Long, Fragment> allParents = toIdMap(fragments.get(0).getParents());
		Set<Long> commonIds = new TreeSet<Long>(allParents.keySet());
		
		for (int i = 1; i < fragments.size(); i++) {
			Map<Long, Fragment> parents = toIdMap(fragments.get(i).getParents());
			commonIds.retainAll(parents.keySet());
			allParents.putAll(parents);
		}
		for (Long id : commonIds) {
			commonParents.add(allParents.get(id));
		}
		return commonParents;
	}
}
