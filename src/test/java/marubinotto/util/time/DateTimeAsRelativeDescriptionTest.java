package marubinotto.util.time;

import static junit.framework.Assert.assertEquals;
import marubinotto.util.MessageSource;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class DateTimeAsRelativeDescriptionTest {

	private DateTime object;

	@Before
	public void given() throws Exception {
		this.object = new DateTime(2010, 1, 1);
	}

	@After
	public void clearCurrentTimeForTest() throws Exception {
		DateTime.setCurrentTimeForTest(null);
	}

	static MessageSource messageSource = new MessageSource() {
		public String getMessage(String code) {
			return code;
		}

		public String getMessage(String code, Object arg) {
			return code + " " + arg;
		}

		public String getMessage(String code, Object[] args) {
			throw new UnsupportedOperationException();
		}
	};

	@Test
	public void tenSecondsAgo() throws Exception {
		DateTime.setCurrentTimeForTest(2010, 1, 1, 0, 0, 10);
		assertEquals("ago-seconds",
			this.object.getRelativeDescription(messageSource));
	}

	@Test
	public void oneMinuteAgo() throws Exception {
		DateTime.setCurrentTimeForTest(2010, 1, 1, 0, 1, 0);
		assertEquals("ago-one-minute",
			this.object.getRelativeDescription(messageSource));
	}

	@Test
	public void almostTwoMinutesAgo() throws Exception {
		DateTime.setCurrentTimeForTest(2010, 1, 1, 0, 1, 59);
		assertEquals("ago-one-minute",
			this.object.getRelativeDescription(messageSource));
	}

	@Test
	public void twoMinutesAgo() throws Exception {
		DateTime.setCurrentTimeForTest(2010, 1, 1, 0, 2, 0);
		assertEquals("ago-minutes 2",
			this.object.getRelativeDescription(messageSource));
	}

	@Test
	public void almostOneHourAgo() throws Exception {
		DateTime.setCurrentTimeForTest(2010, 1, 1, 0, 59, 0);
		assertEquals("ago-minutes 59",
			this.object.getRelativeDescription(messageSource));
	}

	@Test
	public void oneHourAgo() throws Exception {
		DateTime.setCurrentTimeForTest(2010, 1, 1, 1, 0, 0);
		assertEquals("ago-one-hour",
			this.object.getRelativeDescription(messageSource));
	}

	@Test
	public void almostTwoHoursAgo() throws Exception {
		DateTime.setCurrentTimeForTest(2010, 1, 1, 1, 59, 0);
		assertEquals("ago-one-hour",
			this.object.getRelativeDescription(messageSource));
	}

	@Test
	public void twoHoursAgo() throws Exception {
		DateTime.setCurrentTimeForTest(2010, 1, 1, 2, 0, 0);
		assertEquals("ago-hours 2",
			this.object.getRelativeDescription(messageSource));
	}

	@Test
	public void almostOneDayAgo() throws Exception {
		DateTime.setCurrentTimeForTest(2010, 1, 1, 23, 0, 0);
		assertEquals("ago-hours 23",
			this.object.getRelativeDescription(messageSource));
	}

	@Test
	public void oneDayAgo() throws Exception {
		DateTime.setCurrentTimeForTest(2010, 1, 2, 0, 0, 0);
		assertEquals("ago-one-day",
			this.object.getRelativeDescription(messageSource));
	}

	@Test
	public void almostTwoDaysAgo() throws Exception {
		DateTime.setCurrentTimeForTest(2010, 1, 2, 23, 0, 0);
		assertEquals("ago-one-day",
			this.object.getRelativeDescription(messageSource));
	}

	@Test
	public void twoDaysAgo() throws Exception {
		DateTime.setCurrentTimeForTest(2010, 1, 3, 0, 0, 0);
		assertEquals("ago-days 2",
			this.object.getRelativeDescription(messageSource));
	}

	@Test
	public void almostOneMonthAgo() throws Exception {
		DateTime.setCurrentTimeForTest(this.object.addDays(29));
		assertEquals("ago-days 29",
			this.object.getRelativeDescription(messageSource));
	}

	@Test
	public void oneMonthAgo() throws Exception {
		DateTime.setCurrentTimeForTest(this.object.addDays(30));
		assertEquals("ago-one-month",
			this.object.getRelativeDescription(messageSource));
	}

	@Test
	public void almostTwoMonthsAgo() throws Exception {
		DateTime.setCurrentTimeForTest(this.object.addDays(59));
		assertEquals("ago-one-month",
			this.object.getRelativeDescription(messageSource));
	}

	@Test
	public void twoMonthsAgo() throws Exception {
		DateTime.setCurrentTimeForTest(this.object.addDays(60));
		assertEquals("ago-months 2",
			this.object.getRelativeDescription(messageSource));
	}

	@Test
	public void almostOneYearAgo() throws Exception {
		DateTime.setCurrentTimeForTest(this.object.addDays(364));
		assertEquals("ago-months 12",
			this.object.getRelativeDescription(messageSource));
	}

	@Test
	public void oneYearAgo() throws Exception {
		DateTime.setCurrentTimeForTest(this.object.addDays(365));
		assertEquals("ago-one-year",
			this.object.getRelativeDescription(messageSource));
	}

	@Test
	public void almostTwoYearsAgo() throws Exception {
		DateTime.setCurrentTimeForTest(this.object.addDays(365 + 364));
		assertEquals("ago-one-year",
			this.object.getRelativeDescription(messageSource));
	}

	@Test
	public void twoYearsAgo() throws Exception {
		DateTime.setCurrentTimeForTest(this.object.addDays(365 + 365));
		assertEquals("ago-years 2",
			this.object.getRelativeDescription(messageSource));
	}
}
