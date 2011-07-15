package marubinotto.piggydb.model;

import java.util.Comparator;
import java.util.Map;

import marubinotto.piggydb.model.exception.AuthorizationException;
import marubinotto.util.time.DateTime;

public interface Entity {

	public Long getId();
	
	public Map<String, Object> getAttributes();
	
	public DateTime getCreationDatetime();
	
	public String getCreator();
	
	public DateTime getUpdateDatetime();
	
	public String getUpdater();
	
	public boolean isUpdated();
	
	public String getLastUpdaterOrCreator();
	
	public void touch(User user, boolean ignoreAuth);
	
	public boolean canChange(User user);
	
	public boolean canDelete(User user);
	
	public void ensureCanChange(User user) throws AuthorizationException;
	
	public void ensureCanDelete(User user) throws AuthorizationException;

	public static class RecentChangeComparator implements Comparator<Entity> {
		public int compare(Entity o1, Entity o2) {
			return o2.getUpdateDatetime().compareTo(o1.getUpdateDatetime());
		}
	}
}
