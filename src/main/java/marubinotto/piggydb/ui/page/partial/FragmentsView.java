package marubinotto.piggydb.ui.page.partial;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class FragmentsView {
	
	private static Log logger = LogFactory.getLog(FragmentsView.class);
	
	public String viewId;
		
	private int scale;
	
	public String viewType;
	private static final String TYPE_MULTICOLUMN = "multicolumn";
	private static final String TYPE_TREE = "tree";
	private static final String TYPE_DETAIL = "detail";
	private static final String TYPE_FULL = "full";
	
	// Range of scale for each view
	// (0) - multicolumn - (400) - tree - (800) - detail - (950) - full - (1000)
	
	// Multi-column

	private static final int MAX_SCALE_FOR_MULTICOL = 400;
	
	private static final int MIN_WIDTH_COLUMN = 120;
	private static final int MAX_WIDTH_COLUMN = 600;
	private static final double PIXEL_PER_SCALE = 
		(double)(MAX_WIDTH_COLUMN - MIN_WIDTH_COLUMN) / MAX_SCALE_FOR_MULTICOL;
	private static final int MAX_WIDTH_COMPACT_COLUMN = 300;
	
	public int columnWidth;
	public boolean compactColumn = false;
	
	// Tree
	
	private static final int MAX_SCALE_FOR_LIGHT_NODE = 600;
	
	public boolean lightNode = false;
	
	// Detail

	private static final int MIN_SCALE_FOR_DETAIL = 800;
	
	// Full
	
	private static final int MIN_SCALE_FOR_FULL = 950;
	
	public FragmentsView(String viewId) {
		this.viewId = viewId;
	}

	public void setScale(int scale) {
		this.scale = scale;
		
		if (this.scale <= MAX_SCALE_FOR_MULTICOL) {
			this.viewType = TYPE_MULTICOLUMN;
			
			this.columnWidth = MIN_WIDTH_COLUMN + (int)(this.scale * PIXEL_PER_SCALE);
			logger.info("columnWidth: " + this.columnWidth);
			if (this.columnWidth < MAX_WIDTH_COMPACT_COLUMN) {
				this.compactColumn = true;
			}
		}
		else if (this.scale >= MIN_SCALE_FOR_DETAIL) {
			if (this.scale >= MIN_SCALE_FOR_FULL)
				this.viewType = TYPE_FULL;
			else 
				this.viewType = TYPE_DETAIL;
		}
		else {
			this.viewType = TYPE_TREE;
			
			if (this.scale <= MAX_SCALE_FOR_LIGHT_NODE) {
				this.lightNode = true;
			}
		}
	}

	public int getScale() {
		return this.scale;
	}
	
	public int getPageSize() {
		if (this.viewType.equals(TYPE_MULTICOLUMN))
			return this.compactColumn ? 200 : 100;
		else if (this.viewType.equals(TYPE_TREE))
			return 50;
		else 
			return 10;
	}
	
	public boolean needsEagerFetching() {
		return !this.viewType.equals(TYPE_MULTICOLUMN);
	}
	
	public boolean needsEagerFetchingMore() {
		return this.viewType.equals(TYPE_FULL);
	}
}