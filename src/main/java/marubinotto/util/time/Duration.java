package marubinotto.util.time;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;

import marubinotto.util.Assert;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;

/**
 * The period of time during which something continues.
 *
 * The class is named Duration, rather than Time,
 * to make explicit the distinction between a moment in time
 * (as represented by the Date class in Java) and an elapsed time,
 * which this class represents.
 */
public class Duration implements Serializable, Comparable<Duration> {

    private long interval = 0;

    public Duration() {
    }

    public Duration(long milliseconds) {
        setMilliseconds(milliseconds);
    }

    protected void setMilliseconds(long milliseconds) {
        Assert.require(milliseconds >= 0, "milliseconds >= 0");
        this.interval = milliseconds;
    }

    public static Duration parseSecond(String seconds) {
        double milliseconds = Double.parseDouble(seconds) * 1000;
        return new Duration((long)milliseconds);
    }

    public long getTime() {
        return this.interval;
    }

    public double getTime(Unit unit) {
        return this.interval / unit.getValue();
    }

    public String[] decompose(Unit[] units) {
        Arrays.sort(units, Collections.reverseOrder());
        long interval = this.interval;
        String[] components = new String[units.length];
        for (int i = 0; i < units.length; i++) {
            int component = (int)(interval / units[i].getValue());
            interval = interval % units[i].getValue();
            components[i] = String.valueOf(component);
            if (i > 0) {
                int digit = units[i - 1].calculateDigit(units[i]);
                components[i] = StringUtils.leftPad(components[i], digit, '0');
            }
        }
        return components;
    }

    public String getAsDefaultFormat() {
        String[] components = decompose(new Unit[]{
            Unit.HOUR, Unit.MINUTE, Unit.SECOND, Unit.MILLISECOND
        });
        return components[0] + ":" + components[1] + ":"
            + components[2] + "." + components[3];
    }

    public String getAsSecondFormat() {
        String[] components = decompose(new Unit[]{
            Unit.SECOND, Unit.MILLISECOND
        });
        return components[0] + "." + components[1];
    }

    public int compareTo(Duration another) {
        long anotherVal = another.interval;
        return (this.interval < anotherVal ? -1 :
            (this.interval == anotherVal ? 0 : 1));
    }

    public String toString() {
        return getAsDefaultFormat();
    }

// Nested classes

    public static class Unit implements Comparable<Unit> {
        public static final Unit MILLISECOND = new Unit(1, "millisecond");
        public static final Unit SECOND = new Unit(MILLISECOND.getValue() * 1000, "second");
        public static final Unit MINUTE = new Unit(SECOND.getValue() * 60, "minute");
        public static final Unit HOUR = new Unit(MINUTE.getValue() * 60, "hour");
        public static final Unit DAY = new Unit(HOUR.getValue() * 24, "day");
        public static final Unit WEEK = new Unit(DAY.getValue() * 7, "week");
        public static final Unit MONTH = new Unit(DAY.getValue() * 30, "month");
        public static final Unit YEAR = new Unit(DAY.getValue() * 365, "year");

        private long value;
        private String name;

        public Unit(long value, String name) {
            this.value = value;
            this.name = name;
        }

        public long getValue() {
            return this.value;
        }

        public String getName() {
            return this.name;
        }

        public int calculateDigit(Unit lowerUnit) {
            Validate.isTrue(lowerUnit.getValue() < getValue(),
                "lowerUnit.getValue() < getValue()");
            int times = (int)(getValue() / lowerUnit.getValue());
            return String.valueOf(times - 1).length();
        }

        public int compareTo(Unit another) {
        	long anotherVal = another.value;
            return (this.value < anotherVal ? -1 :
                (this.value == anotherVal ? 0 : 1));
        }
    }
}
