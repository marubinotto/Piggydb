package marubinotto.util.time;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;

import junitx.framework.Assert;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Test;

/**
 * @see DateTime
 */
public class DateTimeTest {

	@After
    public void clearCurrentTimeForTest() throws Exception {
        DateTime.setCurrentTimeForTest(null);
    }

	@Test
    public void parseDateStringViaConstructor() throws Exception {
		// When
        DateTime dateTime = new DateTime(
            "1975-08-01 15:03:21", "yyyy-MM-dd HH:mm:ss");
        
        // Then
        assertEquals(1975, dateTime.getYear());
        assertEquals(8, dateTime.getMonth());
        assertEquals(1, dateTime.getDayOfMonth());
        assertEquals(15, dateTime.getHourOfDay());
        assertEquals(03, dateTime.getMinute());
        assertEquals(21, dateTime.getSecond());
    }
	
	@Test
    public void isSameMonth() throws Exception {
		DateTime x = new DateTime(2005, 1, 1);
        DateTime y = new DateTime(2005, 1, 2);
        assertTrue(x.isSameMonth(y));
        
		x = new DateTime(2005, 1, 1);
        y = new DateTime(2005, 2, 1);
        assertFalse(x.isSameMonth(y));
	}

	@Test
    public void isSameDay() throws Exception {
		// Case the same day
        DateTime x = new DateTime(2005, 10, 21, 21, 31, 4);
        DateTime y = new DateTime(2005, 10, 21, 11, 12, 24);
        assertTrue(x.isSameDay(y));

        // Case different days
        x = new DateTime(2005, 10, 21);
        y = new DateTime(2005, 10, 22);
        assertFalse(x.isSameDay(y));
    }

	@Test
    public void startAndEndInstanceOfTheDay() throws Exception {
		// Given
        DateTime dateTime = new DateTime(
            "1975-08-01 15:03:21", "yyyy-MM-dd HH:mm:ss");
        
        // When & Then
        assertEquals(
            "1975-08-01 00:00:00.0",
            dateTime.getStartInstantOfDay().format("yyyy-MM-dd HH:mm:ss.S"));
        assertEquals(
            "1975-08-01 23:59:59.999",
            dateTime.getEndInstantOfDay().format("yyyy-MM-dd HH:mm:ss.S"));
    }

	@Test
    public void addOneHour() throws Exception {
		// Given
        DateTime dateTime = new DateTime(
            "1975-08-01 15:03:21", "yyyy-MM-dd HH:mm:ss");
        
        // When & Then
        assertEquals(
            "1975-08-01 16:03:21",
            dateTime.addHours(1).format("yyyy-MM-dd HH:mm:ss"));
    }

	@Test
    public void addOneMinute() throws Exception {
		// Given
        DateTime dateTime = new DateTime(
            "1975-08-01 15:03:21", "yyyy-MM-dd HH:mm:ss");
        
        // When & Then
        assertEquals(
            "1975-08-01 15:04:21",
            dateTime.addMinutes(1).format("yyyy-MM-dd HH:mm:ss"));
    }

	@Test
    public void addOneMinuteTo59() throws Exception {
		// Given
        DateTime dateTime = new DateTime(
            "1975-08-01 15:59:21", "yyyy-MM-dd HH:mm:ss");
        
        // When & Then
        assertEquals(
            "1975-08-01 16:00:21",
            dateTime.addMinutes(1).format("yyyy-MM-dd HH:mm:ss"));
    }

	@Test
    public void addMinusOneMinute() throws Exception {
		// Given
        DateTime dateTime = new DateTime(
            "1975-08-01 15:03:21", "yyyy-MM-dd HH:mm:ss");
        
        // When & Then
        assertEquals(
            "1975-08-01 15:02:21",
            dateTime.addMinutes(-1).format("yyyy-MM-dd HH:mm:ss"));
    }

	@Test
    public void addZeroMinute() throws Exception {
        DateTime dateTime = new DateTime(
            "1975-08-01 15:03:21", "yyyy-MM-dd HH:mm:ss");
        assertEquals(
            "1975-08-01 15:03:21",
            dateTime.addMinutes(0).format("yyyy-MM-dd HH:mm:ss"));
    }

	@Test
    public void setCurrentTimeForTest() throws Exception {
        DateTime dateTime = new DateTime(
            "1975-08-01 15:03:21.0", "yyyy-MM-dd HH:mm:ss.S");
        DateTime.setCurrentTimeForTest(dateTime);
        assertEquals(
            "1975-08-01 15:03:21.0",
            DateTime.getCurrentTime().format("yyyy-MM-dd HH:mm:ss.S"));
    }

	@Test
    public void setCurrentTimeForTestWithFile() throws Exception {
        DateTime.setCurrentTimeForTest(null);

        File currentTimeFile = FileUtils.toFile(
            DateTime.class.getClassLoader().getResource("current-time"));
        FileUtils.writeStringToFile(currentTimeFile, "1971-06-14 12:20:23.6");

        assertEquals(
            "1971-06-14 12:20:23.6",
            DateTime.getCurrentTime().format("yyyy-MM-dd HH:mm:ss.S"));
    }

	@Test
    public void clearCurrentTimeFile() throws Exception {
        DateTime.setCurrentTimeForTest(null);

        File currentTimeFile = FileUtils.toFile(
            DateTime.class.getClassLoader().getResource("current-time"));
        FileUtils.writeStringToFile(currentTimeFile, "1971-06-14 12:20:23.6");

        DateTime.clearCurrentTimeFile();

        Assert.assertNotEquals(
            "1971-06-14 12:20:23.6",
            DateTime.getCurrentTime().format("yyyy-MM-dd HH:mm:ss.S"));
    }

	@Test
    public void getCurrentTime() throws Exception {
        DateTime.setCurrentTimeForTest(null);
        DateTime.clearCurrentTimeFile();

        Interval interval = new Interval(
            new DateTime(System.currentTimeMillis()).addHours(-1),
            new DateTime(System.currentTimeMillis()).addHours(1));
        assertTrue(interval.containsInstant(DateTime.getCurrentTime()));
    }
	
	@Test
	public void formatAsISO8601() throws Exception {
		System.out.println(DateTime.getCurrentTime().formatAsISO8601());
	}
}
