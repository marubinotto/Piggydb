package marubinotto.piggydb.model;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Set;

import marubinotto.piggydb.model.auth.User;

public interface Tag extends Classifiable {
	
	public final static String PREFIX_SYSTEM_TAG = "#";
	
	public final static String NAME_TRASH = PREFIX_SYSTEM_TAG + "trash";
	public final static String NAME_BOOKMARK = PREFIX_SYSTEM_TAG + "bookmark";
	public final static String NAME_PUBLIC = PREFIX_SYSTEM_TAG + "public";
	public final static String NAME_USER = PREFIX_SYSTEM_TAG + "user";

	public static class TagNameComparator implements Comparator<Tag>, Serializable {
		public int compare(Tag o1, Tag o2) {
			if (o1.getName() == null && o2.getName() == null) return 0;
			if (o1.getName() == null) return -1;
			if (o2.getName() == null) return 1;
			return o1.getName().toLowerCase().compareTo(o2.getName().toLowerCase());
		}
	};
	public static final Comparator<Tag> TAG_NAME_COMPARATOR = new TagNameComparator();
	
	public boolean authorizes(User user);

	public String getName();
	
	public void setNameByUser(String name, User user);
	
	public boolean canRename(User user);
	
	public boolean isClassifiedAs(String name);
	
	public Long getPopularity();
	
	public Set<Long> expandToIdsOfSubtree(TagRepository tagRepository) 
	throws Exception;
	
	public boolean isTrashTag();
	
	public Long getFragmentId();
}
