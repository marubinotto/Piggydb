package marubinotto.util.time;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * @see Week
 */
public class WeekTest {

	@Test
	public void shouldKnowFirstAndLastDay() throws Exception {
		// Given
		DateTime dateTime = new DateTime(2005, 10, 4);
		Week week = new Week(dateTime);

		// When
		DateTime firstDay = week.getFirstDay();

		// Then
		assertEquals(2005, firstDay.getYear());
		assertEquals(10, firstDay.getMonth());
		assertEquals(2, firstDay.getDayOfMonth());

		// When
		DateTime lastDay = week.getLastDay();

		// Then
		assertEquals(2005, lastDay.getYear());
		assertEquals(10, lastDay.getMonth());
		assertEquals(8, lastDay.getDayOfMonth());
	}
}
