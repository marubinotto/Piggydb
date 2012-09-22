package marubinotto.piggydb.model;

import java.io.Serializable;

import marubinotto.piggydb.model.enums.FragmentField;
import marubinotto.piggydb.model.query.FragmentsSortOption;

public class FragmentsOptions implements Serializable {
	
	public FragmentsSortOption sortOption = new FragmentsSortOption();

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
}
