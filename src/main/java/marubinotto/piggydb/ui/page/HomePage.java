package marubinotto.piggydb.ui.page;

import java.util.Set;

import marubinotto.piggydb.model.Fragment;
import marubinotto.piggydb.model.FragmentsOptions;
import marubinotto.piggydb.model.Tag;
import marubinotto.piggydb.model.entity.RawFilter;
import marubinotto.piggydb.model.enums.FragmentField;
import marubinotto.piggydb.ui.page.control.CalendarFocus;
import marubinotto.piggydb.ui.page.control.CalendarIndex;
import marubinotto.piggydb.ui.page.control.FragmentFormPanel;
import marubinotto.piggydb.ui.page.util.PageUrl;
import marubinotto.util.paging.Page;
import marubinotto.util.time.DateTime;
import marubinotto.util.time.Month;
import marubinotto.util.time.TimeVisitors;
import net.sf.click.control.PageLink;

public class HomePage extends AbstractFragmentsPage {

	@Override
	protected String getAtomUrl() {
    	return getDefaultAtomUrl();
    }

    @Override
    protected PageUrl createThisPageUrl() {
    	PageUrl pageUrl = super.createThisPageUrl();
    	pageUrl.parameters.put(PN_HOME_PAGE_INDEX, this.hpi);
    	if (this.date != null) {
    		pageUrl.parameters.put(PN_DATE, this.date);
    	}	
    	return pageUrl;
    }
    
    
	//
	// Input
	//
    
	public static final String PN_DATE = "date";
	public static final String PN_HOME_PAGE_INDEX = "hpi";

    public String date;
    
    public int hpi = 0;		// Page index for home fragments
    private int homePageSize = 10;
 
    
	//
	// Control
	//
    
    private FragmentFormPanel fragmentFormPanel;
    
	@Override
	public void onInit() {
        super.onInit();
        
        this.today = DateTime.getCurrentTime();
        initControls();
    }
	
	private void initControls() {
		this.fragmentFormPanel = createFragmentFormPanel();
		this.fragmentFormPanel.setRedirectPathAfterRegistration(
			getContext().getPagePath(HomePage.class));
	}

	
	//
	// Model
	//

    public DateTime today;
    public String calendarIndex;
    public Page<Fragment> homeFragments = EMPTY_FRAGMENTS;
    public Fragment userFragment;
    
    @Override 
	protected void setModels() throws Exception {
		super.setModels();
		
		importCssFile("style/piggydb-home.css", true, null);

		setHomeFragments();
		setUserFragment();
		
		setCommonSidebarModels();
		setCalendarIndex();
    }

    private void setCalendarIndex() throws Exception {
    	CalendarFocus calendarFocus = CalendarFocus.parseString(this.date);
    	Month currentMonth = new Month(
    		calendarFocus != null ? calendarFocus.toInterval().getStartInstant() : this.today);
        Set<Integer> linkDaysOfMonth = getFragmentRepository().getDaysOfMonth(
			FragmentField.UPDATE_DATETIME, currentMonth);
        CalendarIndex calendarIndex = new CalendarIndex(
        	calendarFocus,
        	this.today,
        	new PageLink(HomePage.class), 
        	linkDaysOfMonth, 
        	getContext().getRequest().getLocale());
        TimeVisitors.traverseDayOfMonth(currentMonth, calendarIndex);
        this.calendarIndex = calendarIndex.toString();
    }
    
    private void setHomeFragments() throws Exception {
		RawFilter filter = new RawFilter();
		
		Tag homeTag = getTagRepository().getByName(Tag.NAME_HOME);
		if (homeTag == null) return;
		filter.getClassification().addTag(homeTag);
		
		Tag trashTag = getTagRepository().getTrashTag();
		if (trashTag != null) filter.getExcludes().addTag(trashTag);
		
		this.homeFragments = getFragmentRepository().findByFilter(
			filter, new FragmentsOptions(this.homePageSize, this.hpi, true));
    }
	
	private void setUserFragment() throws Exception {
		if (getUser().homeFragmentId != null) {
			this.userFragment = getFragmentRepository().get(getUser().homeFragmentId);
		}
	}

	@Override
	public void onRender() {
		super.onRender();
		embedCurrentStateInParameters();
	}

    private void embedCurrentStateInParameters() {
    	addParameterToCommonForms(PN_HOME_PAGE_INDEX, this.hpi);
    	if (this.date != null) {
    		addParameterToCommonForms(PN_DATE, this.date);
    	}
    }
}
