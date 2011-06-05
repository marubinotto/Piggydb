package marubinotto.util.paging;

import java.util.List;

/**
 * Page is a partial view for a list of objects.
 * This is mainly targeted at usage in web UIs.
 *
 * @author	MORITA Daisuke
 */
public interface Page<E> extends List<E> {
	
    /**
     * Return the size that represents capacity of this page.
     * {@link #size()} can be used to get the number of the elements 
     * that this page contains.
     */
    public int getPageSize();
    
    /**
     * Return the number of the elements in the whole of the list.
     */
    public long getTotalSize();
    
    /**
     * Return the number of the pages in the whole of the list.
     * The number is determined by the size of a page and
     * the size of the whole of the list.
     */
    public int getPageCount();
    
    /**
     * Return the index of this page in the whole of the list.
     * Page numbering starts with 0.
     */
    public int getPageIndex();
    
    /**
     * Return the index of the first element of this page 
     * in the whole of the list.
     * If no elements are contained, a negative value will be returned.
     */
    public int getIndexOfFirstElement();
    
    /**
     * Return the index of the end element of this page
     * in the whole of the list.
     * If no elements are contained, a negative value will be returned.
     */
    public int getIndexOfLastElement();
    
    /**
     * Return true if this page is the first of all.
     */
    public boolean isFirstPage();
    
    /**
     * Return true if this page is the last of all.
     */
    public boolean isLastPage();
}
