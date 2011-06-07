package marubinotto.piggydb.external.jdbc;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class SequenceAdjusterList extends SequenceAdjuster {
	
	private static Log logger = LogFactory.getLog(SequenceAdjusterList.class);

	@SuppressWarnings("rawtypes")
	private List sequenceAdjusters = new ArrayList();

	@SuppressWarnings("rawtypes")
	public void setSequenceAdjusters(List sequenceAdjusters) {
		this.sequenceAdjusters = sequenceAdjusters;
	}

	@Override
	public long adjust() throws Exception {
		logger.info("Adjusting sequences ...");
		for (Object e : this.sequenceAdjusters) {
			SequenceAdjuster adjuster = (SequenceAdjuster)e;
			long value = adjuster.adjust();
			logger.info("  " + adjuster.getTableName() + ": " + value);
		}
		return -1;
	}
}
