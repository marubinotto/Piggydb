package marubinotto.piggydb.impl.jdbc.h2.util;

import marubinotto.piggydb.impl.jdbc.SequenceAdjuster;
import marubinotto.util.Assert;

public class H2SequenceAdjuster extends SequenceAdjuster {
	
	private String sequenceName;

	public void setSequenceName(String sequenceName) {
		this.sequenceName = sequenceName;
	}

	@Override
	public long adjust() throws Exception {
		Assert.Property.requireNotNull(jdbcTemplate, "jdbcTemplate");
		Assert.Property.requireNotNull(sequenceName, "sequenceName");
		
		long maxValue = getMaxValue();
		long nextValue = maxValue + 1;
		
		this.jdbcTemplate.update(
			"alter sequence " + this.sequenceName + " restart with ?", 
			new Object[]{new Long(nextValue)});
		
		return nextValue;
	}
}
