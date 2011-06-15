package marubinotto.piggydb.ui.page.control;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import marubinotto.piggydb.ui.page.control.CalendarFocus;
import marubinotto.util.time.DateTime;

import org.junit.Test;

public class CalendarFocusTest {
	
	public static final String TIME_FORMAT = "yyyy-MM-dd HH:mm:ss.S";
	
	@Test
	public void parseNull() throws Exception {
		assertNull(CalendarFocus.parseString(null));
	}
	
	// Focus on a month
	
	private CalendarFocus focusOnMonth = CalendarFocus.parseString("201008");

	@Test
	public void specifiedMonthIsSelected() throws Exception {
		assertTrue(this.focusOnMonth.isSelected(
			new DateTime(2010, 8, 1), CalendarFocus.Type.MONTH));
	}
	
	@Test
	public void anotherMonthIsNotSelected() throws Exception {
		assertFalse(this.focusOnMonth.isSelected(
			new DateTime(2010, 9, 1), CalendarFocus.Type.MONTH));
	}
	
	@Test
	public void dayIsNotSelected() throws Exception {
		assertFalse(this.focusOnMonth.isSelected(
			new DateTime(2010, 8, 1), CalendarFocus.Type.DAY));
	}

	@Test
	public void monthInterval() throws Exception {
		assertEquals(
			"2010-08-01 00:00:00.0", 
			this.focusOnMonth.toInterval().getStartInstant().format(TIME_FORMAT));
		assertEquals(
			"2010-08-31 23:59:59.999", 
			this.focusOnMonth.toInterval().getEndInstant().format(TIME_FORMAT));
	}
	
	
	// Focus on a day
	
	private CalendarFocus focusOnDay = CalendarFocus.parseString("20100830");
	
	@Test
	public void specifiedDayIsSelected() throws Exception {
		assertTrue(this.focusOnDay.isSelected(
			new DateTime(2010, 8, 30), CalendarFocus.Type.DAY));
	}
	
	@Test
	public void anotherDayIsNotSelected() throws Exception {
		assertFalse(this.focusOnDay.isSelected(
			new DateTime(2010, 8, 31), CalendarFocus.Type.DAY));
	}
	
	@Test
	public void monthIsNotSelected() throws Exception {
		assertFalse(this.focusOnDay.isSelected(
			new DateTime(2010, 8, 30), CalendarFocus.Type.MONTH));
	}
	
	@Test
	public void dayInterval() throws Exception {
		assertEquals(
			"2010-08-30 00:00:00.0", 
			this.focusOnDay.toInterval().getStartInstant().format(TIME_FORMAT));
		assertEquals(
			"2010-08-30 23:59:59.999", 
			this.focusOnDay.toInterval().getEndInstant().format(TIME_FORMAT));
	}
}
