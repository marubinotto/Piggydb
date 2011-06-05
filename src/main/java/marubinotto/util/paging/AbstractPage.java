package marubinotto.util.paging;

import java.util.AbstractList;

public abstract class AbstractPage<E> 
extends AbstractList<E> implements Page<E> {

	public AbstractPage() {
	}
	
	public int getPageCount() {
        return PageUtils.calculatePageCount(getTotalSize(), getPageSize());
	}

	public int getIndexOfFirstElement() {
		if (this.size() == 0) {
            return -1;
        }
        return getPageSize() * getPageIndex();
	}

	public int getIndexOfLastElement() {
		if (this.size() == 0) {
            return -1;
        }
		return (getIndexOfFirstElement() + this.size()) - 1;
	}

	public boolean isFirstPage() {
		return getPageIndex() == 0;
	}

	public boolean isLastPage() {
		if (getPageCount() == 0) {
            return true;
        }
        return getPageIndex() == (getPageCount() - 1);
	}
}
