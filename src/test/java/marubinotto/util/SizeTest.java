package marubinotto.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class SizeTest {

	@Test
	public void getAsMegaBytesString() throws Exception {
		Size size = new Size(409990569);
		assertEquals("391.00 MByte", size.getAsMegaBytesString());
	}

	@Test
	public void getAsGigaBytesString() throws Exception {
		Size size = new Size(4799150417L);
		assertEquals("4.47 GByte", size.getAsGigaBytesString());
	}

	@Test
	public void toStringIfLowerThanGigaByte() throws Exception {
		Size size = new Size(409990569);
		assertEquals("391.00 MByte", size.toString());
	}

	@Test
	public void toStringIfBiggerThanGigaByte() throws Exception {
		Size size = new Size(4799150417L);
		assertEquals("4.47 GByte", size.toString());
	}
}
