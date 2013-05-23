package marubinotto.piggydb.ui.page.partial;

import static org.apache.commons.lang.StringUtils.isNotBlank;
import marubinotto.piggydb.model.Classification;
import marubinotto.piggydb.model.Fragment;
import marubinotto.piggydb.model.query.FragmentsQuery;
import marubinotto.piggydb.model.query.FragmentsSortOption;
import marubinotto.piggydb.util.PiggydbUtils;
import marubinotto.util.RegexUtils;
import marubinotto.util.paging.Page;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringEscapeUtils;

public abstract class AbstractFragments extends AbstractPartial {

	//
	// Input
	//

	public String viewId;

	public Integer scale; // 0 - 1000

	public Integer orderBy;
	public Boolean ascending;
	public Boolean shuffle;
	private FragmentsSortOption sortOption;

	public int pi = 0;

	public static final String SK_SCALE = "fragmentsViewScale";
	public static final String SK_ORDERBY = "fragmentsViewOrderBy";
	public static final String SK_ASCENDING = "fragmentsViewAscending";
	
	public String query;

	@Override
	public void onInit() {
		super.onInit();

		// restore the session values
		if (this.scale == null) {
			this.scale = (Integer)ObjectUtils.defaultIfNull(
				getContext().getSessionAttribute(SK_SCALE), 
				getWarSetting().getDefaultFragmentsViewScale());
		}
		if (this.orderBy == null) {
			this.orderBy = (Integer)getContext().getSessionAttribute(SK_ORDERBY);
		}
		if (this.ascending == null) {
			this.ascending = (Boolean)getContext().getSessionAttribute(SK_ASCENDING);
		}
		
		// create a sortOption
		this.sortOption = new FragmentsSortOption(this.orderBy, this.ascending);
		if (this.shuffle != null) {
			this.sortOption.shuffle = this.shuffle;
		}
	}

	//
	// Model
	//

	public FragmentsView view;
	
	public String label;
	public boolean hideHeader = false;

	public Page<Fragment> fragments;
	public Classification contextTags;

	public boolean firstSet = true;
	public boolean lastSet = false;
	
	public String keywordRegex;

	@Override
	protected void setModels() throws Exception {
		super.setModels();

		this.view = new FragmentsView(this.viewId);
		this.view.setScale(this.scale);

		setSelectedFragments();
		setFragments();

		if (this.fragments != null) {
			this.firstSet = (this.pi == 0);
			this.lastSet = this.fragments.isLastPage();
		}

		saveStateToSession();
	}
	
	protected void setKeywordRegex(String keywords) {
	  StringBuilder keywordRegex = new StringBuilder();
    for (String word : PiggydbUtils.splitToKeywords(keywords)) {
      if (keywordRegex.length() > 0) keywordRegex.append("|");
      word = StringEscapeUtils.escapeJavaScript(word);
      word = RegexUtils.escapeRegex(word);
      keywordRegex.append(word);
    }
    this.keywordRegex = "(" + keywordRegex.toString() + ")";
	}
	
	protected FragmentsQuery getQuery(Class<? extends FragmentsQuery> queryClass) 
	throws Exception {
		FragmentsQuery query = (FragmentsQuery)
			getDomain().getFragmentRepository().getQuery(queryClass);
		query.setSortOption(this.sortOption);
		query.setEagerFetching(this.view.needsEagerFetching());
		query.setEagerFetchingMore(this.view.needsEagerFetchingMore());
		return query;
	}
	
	protected Page<Fragment> getPage(FragmentsQuery query) throws Exception {
		return query.getPage(this.view.getPageSize(), this.pi);
	}

	protected abstract void setFragments() throws Exception;

	private void saveStateToSession() {
		if (this.scale != null) getContext().setSessionAttribute(SK_SCALE, this.scale);
		if (this.orderBy != null) getContext().setSessionAttribute(SK_ORDERBY, this.orderBy);
		if (this.ascending != null) getContext().setSessionAttribute(SK_ASCENDING, this.ascending);
	}
	
	protected static String makeKeywordSearchLabel(String keywords) {
	  String label = "<span class=\"search-icon-mini\">&nbsp;</span> ";
    for (String keyword : PiggydbUtils.splitToKeywords(keywords)) {
      label += "\"" + keyword + "\" ";
    }
    return label.trim();
	}
	
	protected void appendKeywordSearchLabel() {
	  if (isNotBlank(this.query)) {
      this.label += " + ";
      this.label += makeKeywordSearchLabel(this.query);
    }
	}
}
