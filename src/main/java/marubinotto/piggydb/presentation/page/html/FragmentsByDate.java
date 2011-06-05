package marubinotto.piggydb.presentation.page.html;

import marubinotto.piggydb.model.enums.FragmentField;
import marubinotto.piggydb.presentation.page.control.CalendarFocus;

public class FragmentsByDate extends AbstractFragments {

	public String date;

	@Override 
	protected void setFragments() throws Exception {
		CalendarFocus calendarFocus = CalendarFocus.parseString(this.date);
		if (calendarFocus != null) {
			this.fragments = getFragmentRepository().findByTime(
				calendarFocus.toInterval(),
				FragmentField.UPDATE_DATETIME,
				this.options);
		}
		else {
			this.fragments = getFragmentRepository().getFragments(this.options);
		}
	}
}
