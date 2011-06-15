package marubinotto.piggydb.impl.jdbc;

import marubinotto.util.Assert;

import org.springframework.jdbc.core.JdbcTemplate;

public abstract class SequenceAdjuster {

	protected JdbcTemplate jdbcTemplate;

	protected String tableName;
	protected String columnName;

	public SequenceAdjuster() {
	}

	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public String getTableName() {
		return tableName;
	}

	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}

	protected long getMaxValue() {
		Assert.Property.requireNotNull(jdbcTemplate, "jdbcTemplate");
		Assert.Property.requireNotNull(tableName, "tableName");
		Assert.Property.requireNotNull(columnName, "columnName");

		Long value = (Long) this.jdbcTemplate.queryForObject("select max("
			+ this.columnName + ") from " + this.tableName, Long.class);
		return value != null ? value : 0;
	}

	public abstract long adjust() throws Exception;
}
