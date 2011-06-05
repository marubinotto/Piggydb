package marubinotto.piggydb.presentation.page.model;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

public class RecentChanges<T> implements Serializable {

	private int maxSize = 10;
	private LinkedList<T> objects = new LinkedList<T>();
	
	public RecentChanges() {
	}
	
	public RecentChanges(int maxSize) {
		this.maxSize = maxSize;
	}
	
	public synchronized void add(T id) {
		if (this.objects.contains(id)) {
			this.objects.remove(id);
		}
		this.objects.addFirst(id);
		while (this.objects.size() > this.maxSize) {
			this.objects.removeLast();
		}
	}
	
	public synchronized List<T> getRecentChanges() {
		return this.objects;
	}
}
