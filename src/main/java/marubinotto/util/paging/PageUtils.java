package marubinotto.util.paging;

import java.util.ArrayList;
import java.util.List;

import marubinotto.util.Assert;

public class PageUtils {

	public static int calculatePageCount(long total, int pageSize) {
		if (total == 0) {
			return 0;
		}
		else if (total < pageSize) {
			return 1;
		}
		else {
			int count = (int)(total / pageSize);
			if ((total % pageSize) > 0) {
				count++;
			}
			return count;
		}
	}

	public static int roundPageIndex(int total, int pageSize, int pageIndex) {
		Assert.require(pageSize >= 0, "pageSize >= 0");

		if (total <= pageSize) return 0;
		if (pageIndex < 0) return 0;

		int lastIndex = calculatePageCount(total, pageSize) - 1;
		if (pageIndex > lastIndex) return lastIndex;

		return pageIndex;
	}

	public static <E> Page<E> empty(int pageSize) {
		return new PageImpl<E>(new ArrayList<E>(), pageSize, 0, 0);
	}

	public static <E> Page<E> getPage(List<E> wholeList, int pageSize, int pageIndex) {
		Assert.Arg.notNull(wholeList, "wholeList");
		Assert.require(pageSize > 0, "pageSize > 0");

		if (wholeList.size() <= pageSize) {
			return new PageImpl<E>(wholeList, pageSize, 0, wholeList.size());
		}

		pageIndex = PageUtils.roundPageIndex(wholeList.size(), pageSize, pageIndex);
		int fromIndex = pageSize * pageIndex;
		int toIndex = Math.min((pageSize * pageIndex) + pageSize, wholeList.size());
		List<E> sublist = wholeList.subList(fromIndex, toIndex);
		return new PageImpl<E>(sublist, pageSize, pageIndex, wholeList.size());
	}

	public static <E> List<Page<E>> splitToPages(List<E> wholeList, int pageSize) {
		Assert.Arg.notNull(wholeList, "wholeList");

		List<Page<E>> pages = new ArrayList<Page<E>>();
		if (wholeList.isEmpty()) return pages;

		int pageCount = PageUtils.calculatePageCount(wholeList.size(), pageSize);
		for (int i = 0; i < pageCount; i++) {
			pages.add(getPage(wholeList, pageSize, i));
		}
		return pages;
	}

	public static <E> Page<E> toPage(List<E> page, int pageSize, int pageIndex, TotalCounter counter) 
	throws Exception {
		Assert.require(page.size() <= pageSize, "page.size() <= pageSize");

		long totalSize = page.size();
		if (pageIndex > 0 || page.size() == pageSize) {
			totalSize = counter.getTotalSize();
		}
		return new PageImpl<E>(page, pageSize, pageIndex, totalSize);
	}

	public static interface TotalCounter {
		public long getTotalSize() throws Exception;
	}

	@SuppressWarnings("unchecked")
	public static <E> Page<E> covariantCast(Page<? extends E> page) {
		return (Page<E>)page;
	}
}
