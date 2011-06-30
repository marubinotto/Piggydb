package marubinotto.util.time;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * @see Duration
 */
public class DurationTest {

	@Test
	public void shouldConvertValueAsDefaultFormatString() throws Exception {
		assertEquals("0:00:00.010", new Duration(10).getAsDefaultFormat());
		assertEquals("1:23:45.678", new Duration(5025678).getAsDefaultFormat());
	}

	@Test
	public void shouldConvertValueAsSecondFormatString() throws Exception {
		assertEquals("0.010", new Duration(10).getAsSecondFormat());
		assertEquals("1.234", new Duration(1234).getAsSecondFormat());
	}

	@Test
	public void shouldParseSecondFormatString() throws Exception {
		Duration duration = Duration.parseSecond("5025.678");
		assertEquals("1:23:45.678", duration.getAsDefaultFormat());
	}
}
