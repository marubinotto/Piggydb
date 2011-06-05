package marubinotto.util.time;

import marubinotto.util.Assert;

/**
 * A definite length of time marked off by two instants.
 */
public class Interval extends Duration {

    private DateTime start;
    private DateTime end;

    public Interval() {
        super();
        this.start = DateTime.getCurrentTime();
        this.end = this.start;
    }

    public Interval(DateTime start, DateTime end) {
        super(end.getTime() - start.getTime());
        this.start = start;
        this.end = end;
    }

    public DateTime getStartInstant() {
        return this.start;
    }

    public DateTime getEndInstant() {
        return this.end;
    }

    protected void setStartInstant(DateTime start) {
        this.start = start;
        updateDuration();
    }

    protected void setEndInstant(DateTime end) {
        this.end = end;
        updateDuration();
    }

    private void updateDuration() {
        if (this.start != null && this.end != null) {
            long milliseconds = this.end.getTime() - this.start.getTime();
            setMilliseconds(Math.abs(milliseconds));
        }
    }

    public boolean containsInstant(DateTime instant) {
        Assert.Arg.notNull(instant, "instant");
        return instant.getTime() >= this.start.getTime() &&
            instant.getTime() <= this.end.getTime();
    }

    public boolean overlapsInterval(Interval interval) {
        Assert.Arg.notNull(interval, "interval");
        return interval.containsInstant(getStartInstant())
            || interval.containsInstant(getEndInstant())
            || containsInstant(interval.getStartInstant());
    }

    public Interval getOverlap(Interval interval) {
        if (!overlapsInterval(interval))	{
            return null;
        }

        DateTime start = null;
        if (getStartInstant().after(interval.getStartInstant())) {
            start = getStartInstant();
        }
        else {
            start = interval.getStartInstant();
        }

        DateTime end = null;
        if (getEndInstant().before(interval.getEndInstant())) {
            end = getEndInstant();
        }
        else {
            end = interval.getEndInstant();
        }

        return new Interval(start, end);
    }
}
