package marubinotto.util.time;

import static junit.framework.Assert.assertEquals;

import org.junit.Test;

public class StopWatchTest {

	@Test
    public void stopAfterOneSecond() throws Exception {
		DateTime.setCurrentTimeForTest(new DateTime(2010, 1, 1, 0, 0, 0));
		StopWatch stopWatch = new StopWatch();
		
		DateTime.setCurrentTimeForTest(new DateTime(2010, 1, 1, 0, 0, 1));
		System.out.println(stopWatch.stop());
		
		assertEquals(1000, stopWatch.getTime());
	}
}
