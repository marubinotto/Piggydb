package marubinotto.util.time;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import marubinotto.util.Assert;
import marubinotto.util.message.MessageSource;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.time.FastDateFormat;

/**
 * marubinotto.util.time.DateTime
 */
public final class DateTime implements Serializable, Comparable<DateTime> {

	private long time;
	private Calendar timeAsCalendar;

	private DateFormat formatter = DateFormat.getDateTimeInstance();

	public static final String CURRENT_TIME_FILE = "current-time";
	public static final String CURRENT_TIME_FILE_FORMAT = "yyyy-MM-dd HH:mm:ss.S";

	private static DateTime sf_currentTime;

	public static DateTime getCurrentTime() {
		if (sf_currentTime != null) {
			return sf_currentTime;
		}

		DateTime currentTime = getCurrentTimeFromFile();
		if (currentTime != null) {
			return currentTime;
		}
		else {
			return new DateTime(System.currentTimeMillis());
		}
	}

	public static void clearCurrentTimeFile() throws IOException {
		File currentTimeFile = getCurrentTimeFile();
		if (currentTimeFile != null) {
			FileUtils.writeStringToFile(currentTimeFile, "");
		}
	}

	public static File getCurrentTimeFile() {
		URL currentTimeResource = DateTime.class.getClassLoader().getResource(
			CURRENT_TIME_FILE);
		if (currentTimeResource == null) {
			return null;
		}

		File currentTimeFile = FileUtils.toFile(currentTimeResource);
		if (!currentTimeFile.isFile()) {
			return null;
		}

		return currentTimeFile;
	}

	private static DateTime getCurrentTimeFromFile() {
		File currentTimeFile = getCurrentTimeFile();
		if (currentTimeFile == null) {
			return null;
		}

		String currentTime;
		try {
			currentTime = FileUtils.readFileToString(currentTimeFile);
		}
		catch (IOException e) {
			return null;
		}
		if (currentTime == null || currentTime.trim().equals("")) {
			return null;
		}

		return new DateTime(currentTime, CURRENT_TIME_FILE_FORMAT);
	}

	public static void setCurrentTimeForTest(DateTime currentTime) {
		sf_currentTime = currentTime;
	}

	public static void setCurrentTimeForTest(int year, int month, int dayOfMonth) {
		setCurrentTimeForTest(new DateTime(year, month, dayOfMonth));
	}

	public static Date date(int year, int month, int dayOfMonth) {
		return new DateTime(year, month, dayOfMonth).toDate();
	}

	public static void setCurrentTimeForTest(int year, int month, int dayOfMonth,
		int hourOfDay, int minute, int second) {
		setCurrentTimeForTest(new DateTime(year, month, dayOfMonth, hourOfDay,
			minute, second));
	}

	public DateTime(long time) {
		initializeTime(time);
	}

	public DateTime(Date date) {
		Assert.Arg.notNull(date, "date");
		initializeTime(date.getTime());
	}

	public DateTime(Calendar calendar) {
		Assert.Arg.notNull(calendar, "calendar");
		initializeTime(calendar.getTime().getTime());
	}

	public DateTime(DateTime dateTime) {
		Assert.Arg.notNull(dateTime, "dateTime");
		initializeTime(dateTime.time);
	}

	public static Calendar createCalendar(int year, int month, int dayOfMonth) {
		Calendar calendar = Calendar.getInstance();
		calendar.setLenient(false);
		calendar.clear();
		calendar.set(year, month - 1, dayOfMonth);
		return calendar;
	}

	public DateTime(int year, int month, int dayOfMonth) {
		this.timeAsCalendar = createCalendar(year, month, dayOfMonth);
		this.time = this.timeAsCalendar.getTime().getTime();
	}

	public static Calendar createCalendar(int year, int month, int dayOfMonth,
		int hourOfDay, int minute, int second) {
		Calendar calendar = Calendar.getInstance();
		calendar.setLenient(false);
		calendar.clear();
		calendar.set(year, month - 1, dayOfMonth, hourOfDay, minute, second);
		return calendar;
	}

	public DateTime(int year, int month, int dayOfMonth, int hourOfDay,
		int minute, int second) {
		this.timeAsCalendar = createCalendar(year, month, dayOfMonth, hourOfDay,
			minute, second);
		this.time = this.timeAsCalendar.getTime().getTime();
	}

	public DateTime(String source, String pattern) {
		Assert.Arg.notNull(source, "source");
		Assert.Arg.notNull(pattern, "pattern");

		applyPattern(pattern);
		Date date = this.formatter.parse(source, new ParsePosition(0));
		if (date == null) {
			throw new DateFormatException("Illegal pattern \"" + pattern + "\" for: "
				+ source);
		}
		initializeTime(date.getTime());
	}

	public DateTime(String source, String[] patterns) {
		Assert.Arg.notNull(source, "source");
		Assert.Arg.notNull(patterns, "patterns");

		Date date = null;
		for (int i = 0; i < patterns.length; i++) {
			applyPattern(patterns[i]);
			date = this.formatter.parse(source, new ParsePosition(0));
			if (date != null) {
				break;
			}
		}
		if (date == null) {
			throw new IllegalArgumentException("Illegal patterns for: " + source);
		}
		initializeTime(date.getTime());
	}

	public long getTime() {
		return this.time;
	}

	public Date toDate() {
		return new Date(this.time);
	}

	public Calendar toCalendar() {
		return (Calendar) this.timeAsCalendar.clone();
	}

	public int compareTo(DateTime another) {
		long thisTime = getTime();
		long anotherTime = another.getTime();
		return (thisTime < anotherTime ? -1 : (thisTime == anotherTime ? 0 : 1));
	}

	public synchronized String toString() {
		return this.formatter.format(toDate());
	}

	public boolean equals(Object obj) {
		if (!(obj instanceof DateTime)) {
			return false;
		}
		long thisTime = getTime();
		long anotherTime = ((DateTime) obj).getTime();
		return thisTime == anotherTime;
	}

	public int hashCode() {
		long ht = getTime();
		return (int) ht ^ (int) (ht >> 32);
	}

	// Time fields

	public int getYear() {
		return this.timeAsCalendar.get(Calendar.YEAR);
	}

	public int getMonth() {
		return this.timeAsCalendar.get(Calendar.MONTH) + 1;
	}

	public int getDayOfMonth() {
		return this.timeAsCalendar.get(Calendar.DAY_OF_MONTH);
	}

	public int getDayOfWeek() {
		return this.timeAsCalendar.get(Calendar.DAY_OF_WEEK);
	}

	public int getHourOfDay() {
		return this.timeAsCalendar.get(Calendar.HOUR_OF_DAY);
	}

	public int getMinute() {
		return this.timeAsCalendar.get(Calendar.MINUTE);
	}

	public int getSecond() {
		return this.timeAsCalendar.get(Calendar.SECOND);
	}

	public int getMillisecond() {
		return this.timeAsCalendar.get(Calendar.MILLISECOND);
	}

	// Utilities

	/**
	 * <ul>
	 * <li>yyyy-MM-dd HH:mm:ss</li>
	 * <li>yyyy.MM.dd G 'at' hh:mm:ss a zzz</li>
	 * </ul>
	 */
	public synchronized String format(String pattern) {
		Assert.Arg.notNull(pattern, "pattern");
		applyPattern(pattern);
		return this.formatter.format(toDate());
	}

	public static FastDateFormat ISO8601 = FastDateFormat
		.getInstance("yyyy-MM-dd'T'HH:mm:ss.SZZ");

	public String formatAsISO8601() {
		return ISO8601.format(toDate());
	}

	public boolean after(DateTime when) {
		return this.time > when.getTime();
	}

	public boolean before(DateTime when) {
		return this.time < when.getTime();
	}

	public boolean isSameMonth(DateTime other) {
		if (getYear() != other.getYear()) return false;
		if (getMonth() != other.getMonth()) return false;
		return true;
	}

	public boolean isSameDay(DateTime other) {
		if (!isSameMonth(other)) return false;
		if (getDayOfMonth() != other.getDayOfMonth()) return false;
		return true;
	}

	public boolean isToday() {
		return isSameDay(DateTime.getCurrentTime());
	}

	public boolean isLastDayOfMonth() {
		return getDayOfMonth() == this.timeAsCalendar
			.getActualMaximum(Calendar.DAY_OF_MONTH);
	}

	public DateTime addMonths(int months) {
		Calendar calendar = toCalendar();
		calendar.add(Calendar.MONTH, months);
		return new DateTime(calendar);
	}

	public DateTime addDays(int days) {
		Calendar calendar = toCalendar();
		calendar.add(Calendar.DAY_OF_MONTH, days);
		return new DateTime(calendar);
	}

	public DateTime addHours(int hours) {
		Calendar calendar = toCalendar();
		calendar.add(Calendar.HOUR_OF_DAY, hours);
		return new DateTime(calendar);
	}

	public DateTime addMinutes(int minutes) {
		Calendar calendar = toCalendar();
		calendar.add(Calendar.MINUTE, minutes);
		return new DateTime(calendar);
	}

	public DateTime addMilliseconds(int milliseconds) {
		Calendar calendar = toCalendar();
		calendar.add(Calendar.MILLISECOND, milliseconds);
		return new DateTime(calendar);
	}

	public Month toMonth() {
		return new Month(this);
	}

	public DateTime getStartInstantOfDay() {
		return new DateTime(getYear(), getMonth(), getDayOfMonth());
	}

	public DateTime getEndInstantOfDay() {
		DateTime firstInstant = getStartInstantOfDay();
		Calendar calendar = firstInstant.toCalendar();
		calendar.add(Calendar.DAY_OF_MONTH, 1);
		calendar.add(Calendar.MILLISECOND, -1);
		return new DateTime(calendar);
	}

	public Interval toDayInterval() {
		return new Interval(getStartInstantOfDay(), getEndInstantOfDay());
	}

	public String getRelativeDescription(MessageSource messageSource) {
		Assert.Arg.notNull(messageSource, "messageSource");
		
		DateTime now = DateTime.getCurrentTime();
		if (after(now)) return "future time";

		Interval interval = new Interval(this, now);

		if (interval.getTime() < Duration.Unit.MINUTE.getValue())
			return messageSource.getMessage("ago-seconds");

		if (interval.getTime() < 2 * Duration.Unit.MINUTE.getValue())
			return messageSource.getMessage("ago-one-minute");

		if (interval.getTime() < Duration.Unit.HOUR.getValue()) {
			int minutes = new Double(interval.getTime(Duration.Unit.MINUTE))
				.intValue();
			return messageSource.getMessage("ago-minutes", minutes);
		}

		if (interval.getTime() < 2 * Duration.Unit.HOUR.getValue())
			return messageSource.getMessage("ago-one-hour");

		if (interval.getTime() < Duration.Unit.DAY.getValue()) {
			int hours = new Double(interval.getTime(Duration.Unit.HOUR)).intValue();
			return messageSource.getMessage("ago-hours", hours);
		}

		if (interval.getTime() < 2 * Duration.Unit.DAY.getValue())
			return messageSource.getMessage("ago-one-day");

		if (interval.getTime() < Duration.Unit.MONTH.getValue()) {
			int days = new Double(interval.getTime(Duration.Unit.DAY)).intValue();
			return messageSource.getMessage("ago-days", days);
		}

		if (interval.getTime() < 2 * Duration.Unit.MONTH.getValue())
			return messageSource.getMessage("ago-one-month");

		if (interval.getTime() < Duration.Unit.YEAR.getValue()) {
			int month = new Double(interval.getTime(Duration.Unit.MONTH)).intValue();
			return messageSource.getMessage("ago-months", month);
		}

		int years = new Double(interval.getTime(Duration.Unit.YEAR)).intValue();
		return years <= 1 ? messageSource.getMessage("ago-one-year")
			: messageSource.getMessage("ago-years", years);
	}

	
// Internals

	private void initializeTime(long time) {
		this.time = time;
		this.timeAsCalendar = Calendar.getInstance();
		this.timeAsCalendar.setTimeInMillis(this.time);
	}

	private void applyPattern(String pattern) {
		if (this.formatter instanceof SimpleDateFormat) {
			((SimpleDateFormat) this.formatter).applyPattern(pattern);
		}
	}
}
