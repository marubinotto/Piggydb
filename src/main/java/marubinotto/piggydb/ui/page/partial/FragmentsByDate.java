package marubinotto.piggydb.ui.page.partial;

import marubinotto.piggydb.model.enums.FragmentField;
import marubinotto.piggydb.model.query.FragmentsAllButTrash;
import marubinotto.piggydb.model.query.FragmentsByTime;
import marubinotto.piggydb.model.query.FragmentsQuery;
import marubinotto.piggydb.ui.page.control.CalendarFocus;

public class FragmentsByDate extends AbstractFragments {

	public String date;

	@Override 
	protected void setFragments() throws Exception {
		CalendarFocus calendarFocus = CalendarFocus.parseString(this.date);
		if (calendarFocus != null) {
			FragmentsByTime query = (FragmentsByTime)getQuery(FragmentsByTime.class);
			query.setCriteria(calendarFocus.toInterval(), FragmentField.UPDATE_DATETIME);
			this.fragments = getPage(query);
			this.label = calendarFocus.toString();
		}
		else {
			FragmentsQuery query = getQuery(FragmentsAllButTrash.class);
			this.fragments = getPage(query);
			this.label = getMessage("all");
		}
	}
}
