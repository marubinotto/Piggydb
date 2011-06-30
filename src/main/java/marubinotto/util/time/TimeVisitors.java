package marubinotto.util.time;

import java.util.Calendar;

/**
 * marubinotto.util.time.TimeVisitors
 */
public class TimeVisitors {

	public interface DayOfMonthVisitor {
		public void start(Month month);

		public void visit(int dayOfMonth, int dayOfWeek, DateTime dateTime);

		public void end();
	}

	public static void traverseDayOfMonth(Month month, DayOfMonthVisitor visitor) {
		visitor.start(month);
		Calendar calendar = month.getFirstDay().toCalendar();
		for (int i = 0; i < month.getDayCount(); i++) {
			visitor.visit(calendar.get(Calendar.DAY_OF_MONTH),
				calendar.get(Calendar.DAY_OF_WEEK), new DateTime(calendar));
			calendar.add(Calendar.DAY_OF_MONTH, 1);
		}
		visitor.end();
	}

	private TimeVisitors() {
	}
}
