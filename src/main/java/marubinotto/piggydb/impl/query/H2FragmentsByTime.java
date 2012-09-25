package marubinotto.piggydb.impl.query;

import java.util.List;

import marubinotto.piggydb.model.enums.FragmentField;
import marubinotto.piggydb.model.query.FragmentsByTime;
import marubinotto.util.Assert;
import marubinotto.util.time.Interval;

public class H2FragmentsByTime
extends H2FragmentsQueryBase implements FragmentsByTime {
	
	private Interval interval;
	private FragmentField field;
	
	public void setCriteria(Interval interval, FragmentField field) {
		this.interval = interval;
		this.field = field;
	}
	
	protected void buildSelectFromWhereSql(StringBuilder sql, List<Object> args) 
	throws Exception {
		Assert.Property.requireNotNull(interval, "interval");
		Assert.Property.requireNotNull(field, "field");
		
		appendSelectAll(sql);

		sql.append(" from fragment where");
		sql.append(" (" + this.field.getName() + " between ? and ?)");
		getRepository().appendConditionToExcludeTrash(sql, "fragment.fragment_id");
		
		args.add(this.interval.getStartInstant().toDate());
		args.add(this.interval.getEndInstant().toDate());
	}
}
