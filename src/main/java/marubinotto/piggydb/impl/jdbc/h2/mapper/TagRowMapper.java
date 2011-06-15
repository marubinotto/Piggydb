package marubinotto.piggydb.impl.jdbc.h2.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;

import marubinotto.piggydb.model.BaseDataObsoleteException;
import marubinotto.piggydb.model.entity.RawEntityFactory;
import marubinotto.piggydb.model.entity.RawTag;
import marubinotto.util.Assert;

import org.springframework.jdbc.core.JdbcTemplate;

public class TagRowMapper extends EntityRowMapper<RawTag> {

	private static final EntityTable TABLE = 
		new EntityTable("tag", "tag_id")
			.defColumn("tag_name");
	
	public TagRowMapper(RawEntityFactory<RawTag> factory, String prefix) {
		super(factory, prefix);
	}
	
	@Override
	protected EntityTable getEntityTable() {
		return TABLE;
	}
	
	public static void insert(RawTag tag, JdbcTemplate jdbcTemplate) {
		Assert.Arg.notNull(tag, "tag");
		Assert.Arg.notNull(jdbcTemplate, "jdbcTemplate");
		
		Object[] values = new Object[] {
			tag.getName()	
		};
		TABLE.insert(tag, values, jdbcTemplate);
	}
	
	public static void update(RawTag tag, JdbcTemplate jdbcTemplate) 
	throws BaseDataObsoleteException {
		Assert.Arg.notNull(tag, "tag");
		Assert.Arg.notNull(jdbcTemplate, "jdbcTemplate");
		
		Object[] values = new Object[] {
			tag.getName()		
		};
		TABLE.update(tag, values, true, jdbcTemplate);
	}

    public RawTag mapRow(ResultSet rs, int rowNum) throws SQLException {
    	RawTag tag = createEntityWithCommonColumns(rs);
        
        Iterator<String> columns = properColumns();
        tag.setName(rs.getString(columns.next()));
        
        return tag;
    }
}