package marubinotto.piggydb.external.jdbc.h2.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;

import marubinotto.piggydb.model.BaseDataObsoleteException;
import marubinotto.piggydb.model.entity.RawEntityFactory;
import marubinotto.piggydb.model.entity.RawFilter;
import marubinotto.util.Assert;

import org.springframework.jdbc.core.JdbcTemplate;

public class FilterRowMapper extends EntityRowMapper<RawFilter> {

	private static final EntityTable TABLE = 
		new EntityTable("filter", "filter_id")
			.defColumn("filter_name");
	
	public FilterRowMapper(RawEntityFactory<RawFilter> factory, String prefix) {
		super(factory, prefix);
	}
	
	@Override
	protected EntityTable getEntityTable() {
		return TABLE;
	}
	
	public static void insert(RawFilter filter, JdbcTemplate jdbcTemplate) {
		Assert.Arg.notNull(filter, "filter");
		Assert.Arg.notNull(jdbcTemplate, "jdbcTemplate");
		
		Object[] values = new Object[] {
			filter.getName()	
		};
		TABLE.insert(filter, values, jdbcTemplate);
	}
	
	public static void update(RawFilter filter, JdbcTemplate jdbcTemplate) 
	throws BaseDataObsoleteException {
		Assert.Arg.notNull(filter, "filter");
		Assert.Arg.notNull(jdbcTemplate, "jdbcTemplate");
		
		Object[] values = new Object[] {
			filter.getName()	
		};
		TABLE.update(filter, values, true, jdbcTemplate);
	}

    public RawFilter mapRow(ResultSet rs, int rowNum) throws SQLException {
    	RawFilter filter = createEntityWithCommonColumns(rs);

        Iterator<String> columns = properColumns();
		filter.setName(rs.getString(columns.next()));
        return filter;
    }
}