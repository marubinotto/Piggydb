package marubinotto.piggydb.ui.page.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import marubinotto.piggydb.model.FilterRepository;
import marubinotto.piggydb.model.FragmentRepository;
import marubinotto.piggydb.model.TagRepository;
import marubinotto.piggydb.model.base.Repository;
import marubinotto.util.Assert;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

public class RecentlyViewed extends RecentChanges<RecentlyViewed.Entity> {

  public static final int TYPE_FRAGMENT = 1;
  public static final int TYPE_TAG = 2;
  public static final int TYPE_FILTER = 3;

  public static class Entity implements Serializable {
    public int type;
    public long id;
    public Object additionalInfo = null;
    
    public Entity(int type, long id) {
      this(type, id, null);
    }

    public Entity(int type, long id, Object additionalInfo) {
      this.type = type;
      this.id = id;
      this.additionalInfo = additionalInfo;
    }

    @Override
    public boolean equals(Object obj) {
      if (obj == null) return false;
      if (obj == this) return true;
      if (obj.getClass() != getClass()) {
        return false;
      }
      Entity rhs = (Entity)obj;
      return new EqualsBuilder().append(type, rhs.type).append(id, rhs.id).isEquals();
    }

    @Override
    public int hashCode() {
      // you pick a hard-coded, randomly chosen, non-zero, odd number
      // ideally different for each class
      return new HashCodeBuilder(17, 37).append(type).append(id).toHashCode();
    }
  }

  public RecentlyViewed() {
  }

  public RecentlyViewed(int maxSize) {
    super(maxSize);
  }

  public synchronized LinkedHashMap<Entity, String> getAllWithNames(
    FragmentRepository fragmentRepository, 
    TagRepository tagRepository,
    FilterRepository filterRepository) 
  throws Exception {
    Assert.Arg.notNull(fragmentRepository, "fragmentRepository");
    Assert.Arg.notNull(tagRepository, "tagRepository");

    Map<Integer, Map<Long, String>> names = new HashMap<Integer, Map<Long, String>>();
    getNamesByType(TYPE_FRAGMENT, fragmentRepository, names);
    getNamesByType(TYPE_TAG, tagRepository, names);
    getNamesByType(TYPE_FILTER, filterRepository, names);

    LinkedHashMap<Entity, String> result = new LinkedHashMap<Entity, String>();
    for (Iterator<Entity> i = getRecentChanges().iterator(); i.hasNext();) {
      Entity entity = i.next();
      if (names.containsKey(entity.type) && names.get(entity.type).containsKey(entity.id)) {
        result.put(entity, names.get(entity.type).get(entity.id));
      }
      else {
        i.remove(); // Remove a missing entry
      }
    }
    return result;
  }

  private void getNamesByType(
    int type, 
    Repository<?> repository, 
    Map<Integer, Map<Long, String>> names) 
  throws Exception {
    Set<Long> ids = getIdsByType(type);
    names.put(type, repository.getNames(ids));
  }

  private Set<Long> getIdsByType(int type) {
    Set<Long> ids = new HashSet<Long>();
    for (Entity entity : getRecentChanges()) {
      if (entity.type == type) ids.add(entity.id);
    }
    return ids;
  }
}
