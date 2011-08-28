package marubinotto.piggydb.ui.page.control;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Set;

import marubinotto.util.Assert;
import marubinotto.util.time.DateTime;
import marubinotto.util.time.Month;
import marubinotto.util.time.TimeVisitors.DayOfMonthVisitor;
import net.sf.click.control.PageLink;

public class CalendarIndex implements DayOfMonthVisitor {

	private StringWriter buffer = new StringWriter();
	private PrintWriter writer = new PrintWriter(buffer);

	private CalendarFocus focus;

	private DateTime today;
	private PageLink pageLink;
	private Set<Integer> linkDaysOfMonth;
	private Locale locale;

	private SimpleDateFormat dwf;

	public CalendarIndex(
		CalendarFocus focus, 
		DateTime today, 
		PageLink pageLink, 
		Set<Integer> linkDaysOfMonth, 
		Locale locale) {

		Assert.Arg.notNull(today, "today");
		Assert.Arg.notNull(pageLink, "pageLink");
		Assert.Arg.notNull(linkDaysOfMonth, "linkDaysOfMonth");
		Assert.Arg.notNull(locale, "locale");

		this.focus = focus;
		this.today = today;
		this.pageLink = pageLink;
		this.linkDaysOfMonth = linkDaysOfMonth;
		this.locale = locale;
		this.dwf = new SimpleDateFormat("EEE", this.locale);
	}

	public void start(Month month) {
		this.writer.println("<table id=\"calendar\" class=\"calendar\" summary=\"calendar\">");

		this.writer.println("<tr>");

		// Link to the previous month
		String linkToPrev = this.pageLink.getHref() + "?date=" + 
			month.getLastMonth().getFirstDay().format(CalendarFocus.FORMAT_MONTH);
		this.writer.print("<td class=\"calendar-prev-month\" colspan=\"2\">");
		this.writer.println(
			"<a id=\"link-to-prev-month\" class=\"mark_link\" href=\"" + linkToPrev + "\">&lt;&lt;</a></td>");

		// Current month
		this.writer.print("<td id=\"calendar-current-month\" class=\"calendar-current-month\" colspan=\"3\">");
		if (this.focus != null && this.focus.isSelected(month.getStartInstant(), CalendarFocus.Type.MONTH)) {
			this.writer.print("<span class=\"selected-month\">");
			this.writer.print(month.getFirstDay().format("yyyy/MM"));
			this.writer.print("</span>");
		}
		else {
			String linkToMonth = this.pageLink.getHref() + "?date=" + 
				month.getStartInstant().format(CalendarFocus.FORMAT_MONTH);
			this.writer.print("<a class=\"link-to-month\" href=\"" + linkToMonth + "\">");
			this.writer.print(month.getFirstDay().format("yyyy/MM"));
			this.writer.print("</a>");
		}
		this.writer.println("</td>");

		// Link to the next month
		String linkToNext = this.pageLink.getHref() + "?date=" + 
			month.getNextMonth().getFirstDay().format(CalendarFocus.FORMAT_MONTH);
		this.writer.print("<td class=\"calendar-next-month\" colspan=\"2\">");
		this.writer.println(
			"<a id=\"link-to-next-month\" class=\"mark_link\" href=\"" + linkToNext + "\">&gt;&gt;</a></td>");

		this.writer.println("</tr>");

		this.writer.println("<tr>");
		this.writer.println("<td class=\"calendar-sunday\">" + dwf.format(SUNDAY.toDate()) + "</td>");
		this.writer.println("<td class=\"calendar-weekday\">" + dwf.format(SUNDAY.addDays(1).toDate()) + "</td>");
		this.writer.println("<td class=\"calendar-weekday\">" + dwf.format(SUNDAY.addDays(2).toDate()) + "</td>");
		this.writer.println("<td class=\"calendar-weekday\">" + dwf.format(SUNDAY.addDays(3).toDate()) + "</td>");
		this.writer.println("<td class=\"calendar-weekday\">" + dwf.format(SUNDAY.addDays(4).toDate()) + "</td>");
		this.writer.println("<td class=\"calendar-weekday\">" + dwf.format(SUNDAY.addDays(5).toDate()) + "</td>");
		this.writer.println("<td class=\"calendar-saturday\">" + dwf.format(SUNDAY.addDays(6).toDate()) + "</td>");
		this.writer.println("</tr>");
	}

	private static final DateTime SUNDAY = new DateTime(2008, 7, 13);

	public void visit(int dayOfMonth, int dayOfWeek, DateTime dateTime) {
		if (dayOfWeek == 1 || dayOfMonth == 1) {
			this.writer.println("<tr>");
			outputBlankCell(dayOfWeek - 1);
		}

		if (this.focus != null && this.focus.isSelected(dateTime, CalendarFocus.Type.DAY)) {
			this.writer.print("<td id=\"selected-day-cell\" class=\"calendar-day selected-day");
			if (dateTime.isSameDay(this.today)) this.writer.print(" today");
			this.writer.print("\">");
			this.writer.print(dayOfMonth);
		}
		else {
			this.writer.print("<td class=\"calendar-day");
			if (dateTime.isSameDay(this.today)) this.writer.print(" today");
			this.writer.print("\">");
			if (this.linkDaysOfMonth.contains(new Integer(dayOfMonth))) {
				String linkToDay = this.pageLink.getHref() + "?date=" + dateTime.format(CalendarFocus.FORMAT_DAY);
				this.writer.print(
					"<a id=\"link-to-day-" + dayOfMonth + "\" class=\"link-to-day\" href=\"" + linkToDay + "\">");
				this.writer.print(dayOfMonth);
				this.writer.print("</a>");
			}
			else {
				this.writer.print(dayOfMonth);
			}
		}
		this.writer.println("</td>");

		if (dayOfWeek == 7 || dateTime.isLastDayOfMonth()) {
			outputBlankCell(7 - dayOfWeek);
			this.writer.println("</tr>");
		}
	}

	public void end() {
		this.writer.println("</table>");
	}

	public String toString() {
		return buffer.toString();
	}

	public void clearBuffer() {
		this.buffer = new StringWriter();
		this.writer = new PrintWriter(this.buffer);
	}

	private void outputBlankCell(int count) {
		for (int i = 0; i < count; i++) {
			this.writer.println("<td class=\"calendar-day\"></td>");
		}
	}
}
