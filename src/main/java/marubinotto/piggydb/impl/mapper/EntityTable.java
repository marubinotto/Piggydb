package marubinotto.piggydb.impl.mapper;

import static marubinotto.util.CollectionUtils.list;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import marubinotto.piggydb.model.entity.RawEntity;
import marubinotto.piggydb.model.exception.BaseDataObsoleteException;
import marubinotto.util.Assert;
import marubinotto.util.time.DateTime;

import org.apache.commons.lang.ArrayUtils;
import org.springframework.jdbc.core.JdbcTemplate;

public class EntityTable {
	
	// Common columns
	public static final String COLUMN_CREATION_DATETIME = "creation_datetime";
	public static final String COLUMN_CREATOR = "creator";
	public static final String COLUMN_UPDATE_DATETIME = "update_datetime";
	public static final String COLUMN_UPDATER = "updater";
	
	private String tableName;
	private String primaryKey;
	private List<String> allColumns = new ArrayList<String>();
	private List<String> properColumns = new ArrayList<String>();
	
	public EntityTable(String tableName, String primaryKey) {
		Assert.Arg.notNull(tableName, "tableName");
		Assert.Arg.notNull(primaryKey, "primaryKey");
		
		this.tableName = tableName;
		this.primaryKey = primaryKey;
		
		this.allColumns.addAll(list(
			this.primaryKey, 
			COLUMN_CREATION_DATETIME,
			COLUMN_CREATOR,
			COLUMN_UPDATE_DATETIME,
			COLUMN_UPDATER));
	}

	public EntityTable defColumn(String column) {
		Assert.Arg.notNull(column, "column");
		Assert.require(
			!this.allColumns.contains(column), 
			"!this.allColumns.contains(column)");
		
		this.properColumns.add(column);
		this.allColumns.add(column);
		return this;
	}
	
	public List<String> getAllColumns() {
		return this.allColumns;
	}
	
	public List<String> getProperColumns() {
		return properColumns;
	}

	public String getProperColumnName(int index) {
		return this.properColumns.get(index);
	}

	/**
	 * Responsible for:
	 *   - setting the creation time and the update time
	 */
	public void insert(RawEntity entity, Object[] values, JdbcTemplate jdbcTemplate) {
		Assert.Arg.notNull(entity, "entity");
		Assert.Arg.notNull(values, "values");
		Assert.Arg.notNull(jdbcTemplate, "jdbcTemplate");
		Assert.Property.requireNotNull(primaryKey, "primaryKey");
		
		StringBuilder sql1 = new StringBuilder("insert into " + this.tableName + " (");
		sql1.append(this.primaryKey);
		sql1.append(", " + COLUMN_CREATION_DATETIME);
		sql1.append(", " + COLUMN_CREATOR);
		sql1.append(", " + COLUMN_UPDATE_DATETIME);	
		StringBuilder sql2 = new StringBuilder(") values (?, ?, ?, ?");
		
		for (String column : this.properColumns) {
			sql1.append(", " + column);
			sql2.append(", ?");
		}	
		
		String sql = sql1.toString() + sql2.toString() + ")";
		
		DateTime now = DateTime.getCurrentTime();
		Object[] commonFields = new Object[] {
			entity.getId(),
			now.toDate(),
			entity.getCreator(),
			now.toDate()
		};
		
		jdbcTemplate.update(sql, ArrayUtils.addAll(commonFields, values));
		
		entity.setCreationDatetime(now);
		entity.setUpdateDatetime(now);
	}
	
	/**
	 * Responsible for:
	 *   - optimistic concurrency control with the last-updated time
	 *   - setting the update time
	 * 
	 * NOTE: "updateTimestamp = false" will disable optimistic lock
	 */
	public void update(
		RawEntity entity, 
		Object[] values, 
		boolean updateTimestamp, 
		JdbcTemplate jdbcTemplate) 
	throws BaseDataObsoleteException {
		Assert.Arg.notNull(entity, "entity");
		Assert.Arg.notNull(values, "values");
		Assert.Arg.notNull(jdbcTemplate, "jdbcTemplate");
		Assert.Property.requireNotNull(primaryKey, "primaryKey");
		
		StringBuilder sql = new StringBuilder();
		sql.append("update " + this.tableName + " set ");
		sql.append(COLUMN_UPDATE_DATETIME + " = ?");
		sql.append(", " + COLUMN_UPDATER + " = ?");

		for (String column : this.properColumns) {
			sql.append(", " + column + " = ?");
		}
		
		sql.append(" where " + this.primaryKey + " = ?");
		sql.append(" and " + COLUMN_UPDATE_DATETIME + " = ?");

		DateTime updateDateTime = 
			updateTimestamp ? DateTime.getCurrentTime() : entity.getUpdateDatetime();
		
		List<Object> params = new ArrayList<Object>();
		params.add(updateDateTime.toDate());
		params.add(entity.getUpdater());
		params.addAll(Arrays.asList(values));
		params.add(entity.getId());
		params.add(entity.getUpdateDatetime().toDate());

		int affected = jdbcTemplate.update(sql.toString(), params.toArray());
		if (affected == 0) throw new BaseDataObsoleteException();

		entity.setUpdateDatetime(updateDateTime);
	}
	
	public void mapCommonColumns(ResultSet rs, String prefix, RawEntity entity) 
	throws SQLException {
		Assert.Arg.notNull(rs, "rs");
		Assert.Arg.notNull(prefix, "prefix");
		Assert.Arg.notNull(entity, "entity");
		
		entity.setId(rs.getLong(prefix + this.primaryKey));
		entity.setCreationDatetime(new DateTime(
			rs.getTimestamp(prefix + COLUMN_CREATION_DATETIME)));
		entity.setCreator(rs.getString(prefix + COLUMN_CREATOR));
		entity.setUpdateDatetime(new DateTime(
			rs.getTimestamp(prefix + COLUMN_UPDATE_DATETIME)));
		entity.setUpdater(rs.getString(prefix + COLUMN_UPDATER));
	}
}
