package marubinotto.piggydb.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import marubinotto.util.Assert;

public class FragmentList<F extends Fragment> {
	
	private List<F> fragments;
	
	public FragmentList() {
		this.fragments = new ArrayList<F>();
	}

	public FragmentList(List<F> fragments) {
		Assert.Arg.notNull(fragments, "fragments");
		this.fragments = fragments;
	}
	
	
	private List<F>	duplicates;
	
	public Map<Long, F> toIdMap() {
		Map<Long, F> id2fragment = new HashMap<Long, F>();
		this.duplicates = new ArrayList<F>();
		
		for (F fragment : this.fragments) {
			if (id2fragment.containsKey(fragment.getId())) 
				this.duplicates.add(fragment);
			else 
				id2fragment.put(fragment.getId(), fragment);
		}
		return id2fragment;
	}
	
	public List<F> getDuplicates() {
		return this.duplicates;
	}
}
