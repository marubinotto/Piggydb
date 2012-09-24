package marubinotto.piggydb.impl.query;

import java.util.List;

import marubinotto.piggydb.model.enums.FragmentField;
import marubinotto.piggydb.model.query.FragmentsByTime;
import marubinotto.util.Assert;
import marubinotto.util.paging.PageUtils;
import marubinotto.util.time.Interval;

public class H2FragmentsByTime
extends H2FragmentsQueryBase implements FragmentsByTime {
	
	private Interval interval;
	private FragmentField field;
	
	private String conditionSql;
	private Object[] args;
	
	public void setCriteria(Interval interval, FragmentField field) {
		this.interval = interval;
		this.field = field;
	}

	public Object[] getArgs() {
		return this.args;
	}

	protected void buildSql(StringBuilder sql, List<Object> args) throws Exception {
		Assert.Property.requireNotNull(interval, "interval");
		Assert.Property.requireNotNull(field, "field");
		
		appendSelectAll(sql);

		StringBuilder condition = new StringBuilder();
		condition.append(" from fragment where");
		condition.append(" (" + this.field.getName() + " between ? and ?)");
		getRepository().appendConditionToExcludeTrash(condition, "fragment.fragment_id");
		this.conditionSql = condition.toString();
		sql.append(this.conditionSql);
		
		args.add(this.interval.getStartInstant().toDate());
		args.add(this.interval.getEndInstant().toDate());
		this.args = args.toArray();
	}
	
	protected PageUtils.TotalCounter getTotalCounter() {
		final String queryAll = "select count(*)" + this.conditionSql;
		return new PageUtils.TotalCounter() {
			public long getTotalSize() throws Exception {
				return (Long) getJdbcTemplate().queryForObject(queryAll, getArgs(), Long.class);
			}
		};
	}
}
