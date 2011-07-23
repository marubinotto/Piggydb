package marubinotto.util.paging;

import java.util.List;

import marubinotto.util.Assert;

public class PageImpl<E> extends AbstractPage<E> {

	private List<E> elements;

	private int pageSize;
	private int pageIndex;
	private long totalSize;

	public PageImpl(List<E> elements, int pageSize, int pageIndex, long totalSize) {
		Assert.Arg.notNull(elements, "elements");

		this.elements = elements;
		this.pageSize = pageSize;
		this.pageIndex = pageIndex;
		this.totalSize = totalSize;
	}

	@Override
	public E get(int index) {
		return this.elements.get(index);
	}

	@Override
	public int size() {
		return this.elements.size();
	}

	public int getPageSize() {
		return this.pageSize;
	}

	public int getPageIndex() {
		return this.pageIndex;
	}

	public long getTotalSize() {
		return this.totalSize;
	}

	public String toString() {
		return "Page: page(" + getPageIndex() + "/" + getPageCount() + 
			") size(" + size() + "/" + getTotalSize() + ") " + this.elements;
	}
}
