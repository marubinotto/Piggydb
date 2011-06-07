package marubinotto.piggydb.model;

import java.io.Serializable;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.collections.comparators.NullComparator;

import marubinotto.piggydb.model.enums.FragmentField;
import marubinotto.util.Assert;

public class FragmentsOptions implements Serializable {
	
	public SortOption sortOption = new SortOption();

	public int pageSize;
	public int pageIndex;
	
	public boolean eagerFetching;
	
	public FragmentsOptions() {
	}
	
	public FragmentsOptions(int pageSize, int pageIndex, boolean eagerFetching) {
		setPagingOption(pageSize, pageIndex);
		this.eagerFetching = eagerFetching;
	}
	
	public void setSortOption(FragmentField orderBy, Boolean ascending) {
		if (orderBy != null) this.sortOption.orderBy = orderBy;
		if (ascending != null) this.sortOption.ascending = ascending;
	}
	
	public void setSortOption(Integer idOfOrderBy, Boolean ascending) {
		FragmentField orderBy = null;
		if (idOfOrderBy != null) orderBy = FragmentField.getEnum(idOfOrderBy);
		setSortOption(orderBy, ascending);
	}

	public void setPagingOption(int pageSize, int pageIndex) {
		this.pageSize = pageSize;
		this.pageIndex = pageIndex;
	}
	
	public static class SortOption implements Serializable {
		public FragmentField orderBy = FragmentField.UPDATE_DATETIME;
		public boolean ascending = false;
		
		public SortOption() {
		}
		
		public static SortOption getDefault() {
			return new SortOption();
		}
		
		public void sort(List<? extends Fragment> fragments) {
			Assert.Arg.notNull(fragments, "fragments");
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
}
