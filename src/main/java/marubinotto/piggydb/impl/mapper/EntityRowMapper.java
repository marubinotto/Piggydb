package marubinotto.piggydb.impl.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;

import marubinotto.piggydb.model.entity.RawEntity;
import marubinotto.piggydb.model.entity.RawEntityFactory;
import marubinotto.util.Assert;

import org.springframework.jdbc.core.RowMapper;

public abstract class EntityRowMapper<T extends RawEntity> implements RowMapper {
	
	private String prefix = "";
	private RawEntityFactory<T> factory;

	public EntityRowMapper(RawEntityFactory<T> factory) {
		this.factory = factory;
	}

	public EntityRowMapper(RawEntityFactory<T> factory, String prefix) {
		Assert.Arg.notNull(prefix, "prefix");
		this.prefix = prefix;
		this.factory = factory;
	}

	public String getColumnPrefix() {
		return this.prefix;
	}

	protected abstract EntityTable getEntityTable();
	
	public Iterator<String> properColumns() {
		final Iterator<String> rawIterator = getEntityTable().getProperColumns().iterator();
		return new Iterator<String>() {
			public boolean hasNext() {
				return rawIterator.hasNext();
			}

			public String next() {
				return prefix + rawIterator.next();
			}

			public void remove() {
				rawIterator.remove();
			}
		};
	}
	
	protected String properColumn(int index) {
		return this.prefix + getEntityTable().getProperColumnName(index);
	}
	
	private String selectCache;
	
	public String selectAll() {
		if (this.selectCache != null) return this.selectCache;
		
		StringBuilder select = new StringBuilder();
		for (String column : getEntityTable().getAllColumns()) {
			if (select.length() > 0) select.append(", ");
			select.append(this.prefix + column);
		}
		this.selectCache = select.toString();
		return this.selectCache;
	}
	
	protected T createEntityWithCommonColumns(ResultSet rs) throws SQLException {
		T entity = this.factory.newRawEntity();
		getEntityTable().mapCommonColumns(rs, this.prefix, entity);
		return entity;
	}
}
