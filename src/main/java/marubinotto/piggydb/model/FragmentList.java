package marubinotto.piggydb.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import marubinotto.util.Assert;

public class FragmentList<F extends Fragment> implements Iterable<F> {
	
	private List<F> fragments;
	
	private Map<Long, F> id2fragment;
	private List<F>	duplicates;
	
	public FragmentList() {
		setFragments(new ArrayList<F>());
	}

	public FragmentList(List<F> fragments) {
		setFragments(fragments);
	}
	
	private void setFragments(List<F> fragments) {
		Assert.Arg.notNull(fragments, "fragments");
		
		this.fragments = fragments;
		
		this.id2fragment = new HashMap<Long, F>();
		this.duplicates = new ArrayList<F>();
		
		for (F fragment : this.fragments) {
			if (this.id2fragment.containsKey(fragment.getId())) 
				this.duplicates.add(fragment);
			else 
				this.id2fragment.put(fragment.getId(), fragment);
		}
	}

	public Iterator<F> iterator() {
		return this.fragments.iterator();
	}
	
	public F get(Long id) {
		Assert.Arg.notNull(id, "id");
		return this.id2fragment.get(id);
	}
	
	public Set<Long> ids() {
		return this.id2fragment.keySet();
	}
	
	public List<F> getDuplicates() {
		return this.duplicates;
	}

	
	
	
	/*
	public FragmentList<F> getAllChildren() {
		
	}
	*/
}
