package marubinotto.util.time;

import java.util.Calendar;

import marubinotto.util.Assert;

/**
 * marubinotto.util.time.Week
 */
public class Week extends Interval {

    public Week() {
        this(DateTime.getCurrentTime());
    }

    public Week(DateTime dateTime) {
        super(
            getFirstInstantOfWeek(dateTime),
            getEndInstantOfWeek(dateTime));
    }

    public String toString() {
        return "Week: " + getFirstDay() + " - " + getLastDay();
    }

    public DateTime getFirstDay() {
        return getStartInstant();
    }

    public DateTime getLastDay() {
        return getEndInstant();
    }

    public DateTime[] getDays() {
        DateTime[] days = new DateTime[7];
        DateTime firstDay = getFirstDay();
        days[0] = firstDay;
        for (int i = 1; i < 7; i++) {
            days[i] = firstDay.addDays(i);
        }
        return days;
    }

    public Week getLastWeek() {
        return new Week(getFirstDay().addDays(-1));
    }

    public Week getNextWeek() {
        return new Week(getLastDay().addDays(1));
    }

// Utility methods

    public static DateTime getFirstInstantOfWeek(DateTime dateTime) {
        Assert.Arg.notNull(dateTime, "dateTime");

        Calendar calendar = DateTime.createCalendar(
            dateTime.getYear(), dateTime.getMonth(), dateTime.getDayOfMonth());
        while (true) {
            if (calendar.get(Calendar.DAY_OF_WEEK) == calendar.getFirstDayOfWeek()) {
                break;
            }
            calendar.add(Calendar.DAY_OF_MONTH, -1);
        }
        return new DateTime(calendar);
    }

    public static DateTime getEndInstantOfWeek(DateTime dateTime) {
        Assert.Arg.notNull(dateTime, "dateTime");

        DateTime firstInstant = getFirstInstantOfWeek(dateTime);
        Calendar calendar = firstInstant.toCalendar();
        calendar.add(Calendar.DAY_OF_MONTH, 7);
        calendar.add(Calendar.MILLISECOND, -1);
        return new DateTime(calendar);
    }
}
