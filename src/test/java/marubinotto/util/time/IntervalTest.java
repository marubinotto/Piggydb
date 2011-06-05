package marubinotto.util.time;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * @see Interval
 */
public class IntervalTest {

	@Test
    public void shouldSetStartAndEndInstantViaConstructor() throws Exception {
		// When
        Interval interval = new Interval(
            new DateTime("2006-02-10 12:00", "yyyy-MM-dd HH:mm"),
            new DateTime("2006-02-10 13:00", "yyyy-MM-dd HH:mm"));
        
        // Then
        assertEquals("2006-02-10 12:00", interval.getStartInstant().format("yyyy-MM-dd HH:mm"));
        assertEquals("2006-02-10 13:00", interval.getEndInstant().format("yyyy-MM-dd HH:mm"));
    }

	@Test
    public void shouldReturnDurationAsMilliseconds() throws Exception {
		// Given
        Interval interval = new Interval(
            new DateTime("2006-02-10 12:00", "yyyy-MM-dd HH:mm"),
            new DateTime("2006-02-10 13:00", "yyyy-MM-dd HH:mm"));
        
        // When & Then
        assertEquals(3600000, interval.getTime());
    }

	@Test
    public void shouldDecideWhetherToContainSpecifiedInstant() 
	throws Exception {
		// Given
        Interval interval = new Interval(
            new DateTime("2006-02-10 12:00", "yyyy-MM-dd HH:mm"),
            new DateTime("2006-02-10 13:00", "yyyy-MM-dd HH:mm"));
        
        // When & Then
        assertTrue(interval.containsInstant(
            new DateTime("2006-02-10 12:30", "yyyy-MM-dd HH:mm")));
    }

	@Test
    public void shouldDecideOnOverlappedIfOneIntervalContainsAnother() 
	throws Exception {
		// Given
        Interval interval = new Interval(
            new DateTime("2006-02-10 12:00", "yyyy-MM-dd HH:mm"),
            new DateTime("2006-02-10 13:00", "yyyy-MM-dd HH:mm"));
        
        // When & Then
        assertTrue(interval.overlapsInterval(
             new Interval(
                    new DateTime("2006-02-10 12:10", "yyyy-MM-dd HH:mm"),
                    new DateTime("2006-02-10 12:50", "yyyy-MM-dd HH:mm"))));
    }

	@Test
    public void shouldDecideOnNotOverlappedIfSo() 
	throws Exception {
		// Given
        Interval interval = new Interval(
            new DateTime("2006-02-10 12:00", "yyyy-MM-dd HH:mm"),
            new DateTime("2006-02-10 13:00", "yyyy-MM-dd HH:mm"));
        
        // When & Then
        assertFalse(interval.overlapsInterval(
             new Interval(
                    new DateTime("2006-02-10 09:00", "yyyy-MM-dd HH:mm"),
                    new DateTime("2006-02-10 11:00", "yyyy-MM-dd HH:mm"))));
    }

	@Test
    public void shouldDecideOnOverlappedIfTwoIntervalsTouchOnBorder() 
	throws Exception {
		// Given
        Interval interval = new Interval(
            new DateTime("2006-02-10 12:00", "yyyy-MM-dd HH:mm"),
            new DateTime("2006-02-10 13:00", "yyyy-MM-dd HH:mm"));
        
        // When & Then
        assertTrue(interval.overlapsInterval(
             new Interval(
                    new DateTime("2006-02-10 11:00", "yyyy-MM-dd HH:mm"),
                    new DateTime("2006-02-10 12:00", "yyyy-MM-dd HH:mm"))));
    }

	@Test
    public void shouldReturnNullIfNotOverlapped() throws Exception {
		// Given
        Interval interval = new Interval(
            new DateTime("2006-02-10 12:00", "yyyy-MM-dd HH:mm"),
            new DateTime("2006-02-10 13:00", "yyyy-MM-dd HH:mm"));
        
        // When
        Duration overlap = interval.getOverlap(
            new Interval(
                new DateTime("2006-02-10 09:30", "yyyy-MM-dd HH:mm"),
                new DateTime("2006-02-10 10:00", "yyyy-MM-dd HH:mm")));
        
        // Then
        assertNull(overlap);
    }

	@Test
    public void shouldReturnOverlapWithAnotherInterval() throws Exception {
		// Given
        Interval interval = new Interval(
            new DateTime("2006-02-10 12:00", "yyyy-MM-dd HH:mm"),
            new DateTime("2006-02-10 13:00", "yyyy-MM-dd HH:mm"));
        
        // When
        Interval overlap = interval.getOverlap(
            new Interval(
                new DateTime("2006-02-10 11:30", "yyyy-MM-dd HH:mm"),
                new DateTime("2006-02-10 12:10", "yyyy-MM-dd HH:mm")));

        // Then
        assertEquals(600000, overlap.getTime());
        assertEquals("2006-02-10 12:00", overlap.getStartInstant().format("yyyy-MM-dd HH:mm"));
        assertEquals("2006-02-10 12:10", overlap.getEndInstant().format("yyyy-MM-dd HH:mm"));
    }

	@Test
    public void shouldReturnZeroLengthOverlapWithOnlyTouchedAnotherInterval() throws Exception {
		// Given
        Interval interval = new Interval(
            new DateTime("2006-02-10 12:00", "yyyy-MM-dd HH:mm"),
            new DateTime("2006-02-10 13:00", "yyyy-MM-dd HH:mm"));
        
        // When
        Interval overlap = interval.getOverlap(
            new Interval(
                new DateTime("2006-02-10 11:30", "yyyy-MM-dd HH:mm"),
                new DateTime("2006-02-10 12:00", "yyyy-MM-dd HH:mm")));

        // Then
        assertEquals(0, overlap.getTime());
        assertEquals("2006-02-10 12:00", overlap.getStartInstant().format("yyyy-MM-dd HH:mm"));
        assertEquals("2006-02-10 12:00", overlap.getEndInstant().format("yyyy-MM-dd HH:mm"));
    }

	@Test
    public void shouldReturnOverlapWithAnotherSubInterval() throws Exception {
		// Given
        Interval interval = new Interval(
            new DateTime("2006-02-10 12:00", "yyyy-MM-dd HH:mm"),
            new DateTime("2006-02-10 13:00", "yyyy-MM-dd HH:mm"));
        
        // When
        Interval overlap = interval.getOverlap(
            new Interval(
                new DateTime("2006-02-10 12:20", "yyyy-MM-dd HH:mm"),
                new DateTime("2006-02-10 12:40", "yyyy-MM-dd HH:mm")));

        // Then
        assertEquals("2006-02-10 12:20", overlap.getStartInstant().format("yyyy-MM-dd HH:mm"));
        assertEquals("2006-02-10 12:40", overlap.getEndInstant().format("yyyy-MM-dd HH:mm"));
    }

	@Test
    public void shouldReturnOverlapWithAnotherSuperInterval() throws Exception {
        Interval interval = new Interval(
            new DateTime("2006-02-10 12:00", "yyyy-MM-dd HH:mm"),
            new DateTime("2006-02-10 13:00", "yyyy-MM-dd HH:mm"));
        Interval overlap = interval.getOverlap(
            new Interval(
                new DateTime("2006-02-10 11:00", "yyyy-MM-dd HH:mm"),
                new DateTime("2006-02-10 14:00", "yyyy-MM-dd HH:mm")));

        assertEquals("2006-02-10 12:00", overlap.getStartInstant().format("yyyy-MM-dd HH:mm"));
        assertEquals("2006-02-10 13:00", overlap.getEndInstant().format("yyyy-MM-dd HH:mm"));
    }

	@Test
    public void shouldReturnOverlapWithTheSameInterval() throws Exception {
        Interval interval = new Interval(
            new DateTime("2006-02-10 12:00", "yyyy-MM-dd HH:mm"),
            new DateTime("2006-02-10 13:00", "yyyy-MM-dd HH:mm"));
        Interval overlap = interval.getOverlap(interval);

        assertEquals("2006-02-10 12:00", overlap.getStartInstant().format("yyyy-MM-dd HH:mm"));
        assertEquals("2006-02-10 13:00", overlap.getEndInstant().format("yyyy-MM-dd HH:mm"));
    }
}
