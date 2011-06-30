package marubinotto.util.time;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class StopWatch extends Interval {

	private static Log logger = LogFactory.getLog(StopWatch.class);

	private String name;

	public StopWatch() {
		logger.debug("started");
	}

	public StopWatch(String name) {
		this();
		this.name = name;
	}

	public String getName() {
		return this.name;
	}

	public String stop() {
		setEndInstant(DateTime.getCurrentTime());
		logger.debug("stopped");
		return getAsSecondFormat();
	}
}
