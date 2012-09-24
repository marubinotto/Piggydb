package marubinotto.piggydb.model.query;

import marubinotto.piggydb.model.enums.FragmentField;
import marubinotto.util.time.Interval;

public interface FragmentsByTime extends FragmentsQuery {

	public void setCriteria(Interval interval, FragmentField field);
}
