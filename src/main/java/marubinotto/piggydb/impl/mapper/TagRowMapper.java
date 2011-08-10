package marubinotto.piggydb.impl.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;

import marubinotto.piggydb.model.entity.RawEntityFactory;
import marubinotto.piggydb.model.entity.RawTag;
import marubinotto.piggydb.model.exception.BaseDataObsoleteException;
import marubinotto.util.Assert;

import org.springframework.jdbc.core.JdbcTemplate;

public class TagRowMapper extends EntityRowMapper<RawTag> {

	private static final EntityTable TABLE = 
		new EntityTable("tag", "tag_id")
			.defColumn("tag_name")
			.defColumn("fragment_id");
	
	private static Object[] toValues(RawTag tag) {
		return new Object[]{
			tag.getName(),
			tag.getFragmentId()
		};
	}

	public static void insert(RawTag tag, JdbcTemplate jdbcTemplate) {
		Assert.Arg.notNull(tag, "tag");
		Assert.Arg.notNull(jdbcTemplate, "jdbcTemplate");

		TABLE.insert(tag, toValues(tag), jdbcTemplate);
	}

	public static void update(RawTag tag, JdbcTemplate jdbcTemplate) 
	throws BaseDataObsoleteException {
		Assert.Arg.notNull(tag, "tag");
		Assert.Arg.notNull(jdbcTemplate, "jdbcTemplate");

		TABLE.update(tag, toValues(tag), true, jdbcTemplate);
	}

	public TagRowMapper(RawEntityFactory<RawTag> factory, String prefix) {
		super(factory, prefix);
	}

	@Override
	protected EntityTable getEntityTable() {
		return TABLE;
	}

	public RawTag mapRow(ResultSet rs, int rowNum) throws SQLException {
		RawTag tag = createEntityWithCommonColumns(rs);

		Iterator<String> columns = properColumns();
		tag.setName(rs.getString(columns.next()));
		tag.setFragmentId(rs.getLong(columns.next()));

		return tag;
	}
}