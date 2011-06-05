package marubinotto.util.time;

import java.util.Calendar;

import marubinotto.util.Assert;

/**
 * marubinotto.util.time.Month
 */
public class Month extends Interval {

    public Month() {
        this(DateTime.getCurrentTime());
    }

    public Month(DateTime dateTime) {
        super(
            getFirstInstantOfMonth(dateTime),
            getEndInstantOfMonth(dateTime));
    }

    public Month(int year, int month) {
        this(new DateTime(year, month, 1));
    }

    public int getYear() {
        return getFirstDay().getYear();
    }

    public int getMonth() {
        return getFirstDay().getMonth();
    }

    public int getDayCount() {
        return getLastDay().getDayOfMonth();
    }

    public String toString() {
        return "Month: " + getFirstDay().format("yyyy-MM");
    }

    public DateTime getFirstDay() {
        return getStartInstant();
    }

    public DateTime getLastDay() {
        return getEndInstant();
    }

    public DateTime[] getDays() {
        int dayCount = getDayCount();
        DateTime[] days = new DateTime[dayCount];
        DateTime firstDay = getFirstDay();
        days[0] = firstDay;
        for (int i = 1; i < dayCount; i++) {
            days[i] = firstDay.addDays(i);
        }
        return days;
    }

    public Month getLastMonth() {
        return new Month(getFirstDay().addMonths(-1));
    }

    public Month getNextMonth() {
        return new Month(getLastDay().addMonths(1));
    }

// Utility methods

    public static DateTime getFirstInstantOfMonth(DateTime dateTime) {
        Assert.Arg.notNull(dateTime, "dateTime");
        return new DateTime(dateTime.getYear(), dateTime.getMonth(), 1);
    }

    public static DateTime getEndInstantOfMonth(DateTime dateTime) {
        Assert.Arg.notNull(dateTime, "dateTime");

        DateTime firstInstant = getFirstInstantOfMonth(dateTime);
        Calendar calendar = firstInstant.toCalendar();
        calendar.add(Calendar.MONTH, 1);
        calendar.add(Calendar.MILLISECOND, -1);
        return new DateTime(calendar);
    }
}
