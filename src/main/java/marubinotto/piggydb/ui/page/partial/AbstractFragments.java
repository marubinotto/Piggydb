package marubinotto.piggydb.ui.page.partial;

import static marubinotto.util.CollectionUtils.list;
import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import marubinotto.piggydb.model.Classification;
import marubinotto.piggydb.model.Filter;
import marubinotto.piggydb.model.Fragment;
import marubinotto.piggydb.model.Tag;
import marubinotto.piggydb.model.query.FragmentsAllButTrash;
import marubinotto.piggydb.model.query.FragmentsQuery;
import marubinotto.piggydb.model.query.FragmentsSortOption;
import marubinotto.piggydb.ui.wiki.DefaultWikiParser;
import marubinotto.piggydb.util.PiggydbUtils;
import marubinotto.util.RegexUtils;
import marubinotto.util.paging.Page;
import marubinotto.util.paging.PageUtils;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;

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
	public String tagsToInclude;
	public String tagsToExclude;

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
		
		if (isNotBlank(this.query)) {
		  this.query = modifyIfGarbledByTomcat(this.query);
		}
	}

	//
	// Model
	//

	public FragmentsView view;
	
	public String label = "";
	public boolean hideHeader = false;

	public Page<Fragment> fragments;
	public Classification contextTags;

	public boolean firstSet = true;
	public boolean lastSet = false;
	
	public String keywordRegex;
	
	public Filter filter;

	@Override
	protected void setModels() throws Exception {
		super.setModels();

		this.view = new FragmentsView(this.viewId);
		this.view.setScale(this.scale);

		setSelectedFragments();
		
		checkFragmentRef();
		this.filter = createFilter();
		if (this.fragments == null) setFragmentsByFilter();
		if (this.fragments == null) setFragments();

		if (this.fragments != null) {
			this.firstSet = (this.pi == 0);
			this.lastSet = this.fragments.isLastPage();
		}

		saveStateToSession();
	}
	
	private void checkFragmentRef() throws Exception {
	  if (isBlank(this.query)) return;
	  
	  // query == "#<number>"
	  if (this.query.matches(DefaultWikiParser.PS_FRAGMENT_REF)) {
	    long id = Long.parseLong(this.query.substring(1));
	    Fragment fragment = getDomain().getFragmentRepository().get(id);
	    this.fragments = fragment != null ?
	      PageUtils.getPage(list(fragment), this.view.getPageSize(), this.pi) :
	      emptyFragments();
	    this.label = this.query;
	  }
	}
	
	private Page<Fragment> emptyFragments() {
	  return PageUtils.<Fragment>empty(this.view.getPageSize());
	}
  
  protected Filter createFilter() throws Exception {
    return null;
  }
	
	private void setFragmentsByFilter() throws Exception {
	  if (this.filter == null) return;
	  
	  // add tags to include
	  if (isNotBlank(this.tagsToInclude)) {
	    for (String tagName : StringUtils.split(this.tagsToInclude, ',')) {
	      Tag tag = getTagByName(tagName);
	      if (tag == null) {
	        this.fragments = emptyFragments();
	        continue;
	      }
	      this.filter.addIncludeByUser(tag, getUser());
	    }
	  }
	  this.contextTags = this.filter.getIncludes();
	  
	  // add tag to exclude
	  if (isNotBlank(this.tagsToExclude)) {
	    for (String tagName : StringUtils.split(this.tagsToExclude, ',')) {
	      Tag tag = getTagByName(tagName);
	      if (tag != null) {
	        this.filter.addExcludeByUser(tag, getUser());
	      }
	    }
	  }
	  
	  if (this.fragments != null) return;
	  
	  
	  // query
	  if (this.filter.isEmpty() && isBlank(this.query)) {
	    this.label = getMessage("all");
      FragmentsQuery query = getQuery(FragmentsAllButTrash.class);
      this.fragments = getPage(query);
      if (this.fragments.getTotalSize() == 0 && isBlank(this.query)) {
        this.hideHeader = true;
      }
	  }
	  else {
	    marubinotto.piggydb.model.query.FragmentsByFilter query = 
        (marubinotto.piggydb.model.query.FragmentsByFilter)getQuery(
          marubinotto.piggydb.model.query.FragmentsByFilter.class);
      query.setFilter(this.filter);
      if (isNotBlank(this.query)) {
        query.setKeywords(this.query);
        setKeywordRegex(this.query);
        appendKeywordSearchLabel();
      }
      this.fragments = getPage(query);
	  }
	}
	
	private Tag getTagByName(String name) throws Exception {
	  return isNotBlank(name) ? getDomain().getTagRepository().getByName(name.trim()) : null;
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

	protected void setFragments() throws Exception {
	}

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
	  if (isBlank(this.query)) return;
	  if (isNotBlank(this.label)) {
	    this.label += " + ";
	  }
	  this.label += makeKeywordSearchLabel(this.query);
	}
}
