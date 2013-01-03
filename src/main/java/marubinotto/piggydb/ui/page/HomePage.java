package marubinotto.piggydb.ui.page;

import java.util.List;
import java.util.Set;

import marubinotto.piggydb.model.Fragment;
import marubinotto.piggydb.model.enums.FragmentField;
import marubinotto.piggydb.ui.page.common.AbstractFragmentsPage;
import marubinotto.piggydb.ui.page.common.PageUrl;
import marubinotto.piggydb.ui.page.control.CalendarFocus;
import marubinotto.piggydb.ui.page.control.CalendarIndex;
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
		if (this.date != null) {
			pageUrl.parameters.put(PN_DATE, this.date);
		}
		return pageUrl;
	}

	//
	// Input
	//

	public static final String PN_DATE = "date";
	public String date;

	//
	// Control
	//

	@Override
	public void onInit() {
		super.onInit();

		this.today = DateTime.getCurrentTime();
	}

	//
	// Model
	//

	public DateTime today;
	public String calendarIndex;
	public List<Fragment> homeFragments = EMPTY_FRAGMENTS;
	public Fragment userFragment;

	@Override
	protected void setModels() throws Exception {
		super.setModels();

		importCss("style/piggydb-home.css", true, null);

		setHomeFragments();
		setUserFragment();

		setCommonSidebarModels();
		setCalendarIndex();
	}

	private void setCalendarIndex() throws Exception {
		CalendarFocus calendarFocus = CalendarFocus.parseString(this.date);
		Month currentMonth = new Month(
			calendarFocus != null ? 
				calendarFocus.toInterval().getStartInstant() : 
				this.today);
		Set<Integer> linkDaysOfMonth = getDomain().getFragmentRepository().
			getDaysOfMonth(FragmentField.UPDATE_DATETIME, currentMonth);
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
		this.homeFragments = getDomain().getFragmentRepository().getFragmentsAtHome(getUser());
	}

	private void setUserFragment() throws Exception {
		if (getUser().homeFragmentId != null) {
			this.userFragment = getDomain().getFragmentRepository().get(getUser().homeFragmentId);
		}
	}

	@Override
	public void onRender() {
		super.onRender();
		embedCurrentStateInParameters();
	}

	private void embedCurrentStateInParameters() {
		if (this.date != null) {
			addParameterToCommonForms(PN_DATE, this.date);
		}
	}
}
