package marubinotto.piggydb.model.query;

import java.io.Serializable;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import marubinotto.piggydb.model.Fragment;
import marubinotto.piggydb.model.enums.FragmentField;
import marubinotto.util.Assert;

import org.apache.commons.collections.comparators.NullComparator;

public class FragmentsSortOption implements Serializable {
	
	public FragmentField orderBy = FragmentField.UPDATE_DATETIME;
	public boolean ascending = false;
	public boolean shuffle = false;
	
	public FragmentsSortOption() {
	}
	
	public FragmentsSortOption(FragmentField orderBy, Boolean ascending) {
		setOptions(orderBy, ascending);
	}
	
	private void setOptions(FragmentField orderBy, Boolean ascending) {
		if (orderBy != null) this.orderBy = orderBy;
		if (ascending != null) this.ascending = ascending;
	}
	
	public FragmentsSortOption(Integer idOfOrderBy, Boolean ascending) {
		FragmentField orderBy = null;
		if (idOfOrderBy != null) orderBy = FragmentField.getEnum(idOfOrderBy);
		setOptions(orderBy, ascending);
	}
	
	public static FragmentsSortOption getDefault() {
		return new FragmentsSortOption();
	}
	
	public void sort(List<? extends Fragment> fragments) {
		Assert.Arg.notNull(fragments, "fragments");
		if (this.shuffle) 
			Collections.shuffle(fragments);
		else
			Collections.sort(fragments, getComparator());
	}
	
	@SuppressWarnings("rawtypes")
	private static final Comparator nullHighComparator = new NullComparator(true);
	
	public Comparator<Fragment> getComparator() {
		return new Comparator<Fragment>() {
			@SuppressWarnings({"rawtypes", "unchecked"})
			public int compare(Fragment o1, Fragment o2) {
				Comparable field1 = orderBy.getFieldValue(o1);
				Comparable field2 = orderBy.getFieldValue(o2);
				if (orderBy.isString()) {
					if (field1 != null) field1 = ((String)field1).toUpperCase();
					if (field2 != null) field2 = ((String)field2).toUpperCase();
				}
				if (ascending)
					return nullHighComparator.compare(field1, field2);
				else
					return nullHighComparator.compare(field2, field1);
			}
		};
	}
}