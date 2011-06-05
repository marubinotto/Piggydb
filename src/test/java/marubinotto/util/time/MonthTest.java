package marubinotto.util.time;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * @see Month
 */
public class MonthTest {

	@Test
    public void test2005_10_04() throws Exception {
        Month month = new Month(new DateTime(2005, 10, 4));

        assertEquals(2005, month.getYear());
        assertEquals(10, month.getMonth());

        DateTime start = month.getStartInstant();
        assertEquals(
            "2005-10-01 00:00:00.0",
            start.format("yyyy-MM-dd HH:mm:ss.S"));

        DateTime end = month.getEndInstant();
        assertEquals(
            "2005-10-31 23:59:59.999",
            end.format("yyyy-MM-dd HH:mm:ss.S"));

        assertEquals(31, month.getDayCount());

        Month lastMonth = month.getLastMonth();
        assertEquals(2005, lastMonth.getYear());
        assertEquals(9, lastMonth.getMonth());

        Month nextMonth = month.getNextMonth();
        assertEquals(2005, nextMonth.getYear());
        assertEquals(11, nextMonth.getMonth());
    }

	@Test
    public void test2005_12_25() throws Exception {
        Month month = new Month(new DateTime(2005, 12, 25));

        assertEquals(2005, month.getYear());
        assertEquals(12, month.getMonth());

        DateTime start = month.getStartInstant();
        assertEquals(
            "2005-12-01 00:00:00.0",
            start.format("yyyy-MM-dd HH:mm:ss.S"));

        DateTime end = month.getEndInstant();
        assertEquals(
            "2005-12-31 23:59:59.999",
            end.format("yyyy-MM-dd HH:mm:ss.S"));

        assertEquals(31, month.getDayCount());

        Month lastMonth = month.getLastMonth();
        assertEquals(2005, lastMonth.getYear());
        assertEquals(11, lastMonth.getMonth());

        Month nextMonth = month.getNextMonth();
        assertEquals(2006, nextMonth.getYear());
        assertEquals(1, nextMonth.getMonth());
    }
}
