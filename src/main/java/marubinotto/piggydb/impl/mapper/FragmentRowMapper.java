package marubinotto.piggydb.impl.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;

import marubinotto.piggydb.model.entity.RawEntityFactory;
import marubinotto.piggydb.model.entity.RawFragment;
import marubinotto.piggydb.model.exception.BaseDataObsoleteException;
import marubinotto.util.Assert;
import marubinotto.util.Size;

import org.springframework.jdbc.core.JdbcTemplate;

public class FragmentRowMapper extends EntityRowMapper<RawFragment> {

	private static final EntityTable TABLE = 
		new EntityTable("fragment", "fragment_id")
			.defColumn("title")
			.defColumn("content")
			.defColumn("file_name")
			.defColumn("file_type")
			.defColumn("file_size")
			.defColumn("password")
			.defColumn("tag_id");
	
	private static Object[] toValues(RawFragment fragment) {
		return new Object[]{
			fragment.getTitle(),
			fragment.getContent(),
			fragment.getFileName(),
			fragment.getFileType(),
			fragment.getFileSize() != null ? fragment.getFileSize().getValue() : null,
			fragment.getPassword(),
			fragment.getTagId()
		};
	}

	public static void insert(RawFragment fragment, JdbcTemplate jdbcTemplate) {
		Assert.Arg.notNull(fragment, "fragment");
		Assert.Arg.notNull(jdbcTemplate, "jdbcTemplate");

		TABLE.insert(fragment, toValues(fragment), jdbcTemplate);
	}

	public static void update(RawFragment fragment, boolean updateTimestamp,
		JdbcTemplate jdbcTemplate) throws BaseDataObsoleteException {
		Assert.Arg.notNull(fragment, "fragment");
		Assert.Arg.notNull(jdbcTemplate, "jdbcTemplate");

		TABLE.update(fragment, toValues(fragment), updateTimestamp, jdbcTemplate);
	}

	public FragmentRowMapper(RawEntityFactory<RawFragment> factory, String prefix) {
		super(factory, prefix);
	}

	@Override
	protected EntityTable getEntityTable() {
		return TABLE;
	}

	public RawFragment mapRow(ResultSet rs, int rowNum) throws SQLException {
		RawFragment fragment = createEntityWithCommonColumns(rs);
		
		// ResultSet.getLong
		// 	if the value is SQL NULL, the value returned is 0

		Iterator<String> columns = properColumns();
		fragment.setTitle(rs.getString(columns.next()));
		fragment.setContent(rs.getString(columns.next()));
		fragment.setFileName(rs.getString(columns.next()));
		fragment.setFileType(rs.getString(columns.next()));
		
		Long fileSize = rs.getLong(columns.next());
		if (fileSize != 0) fragment.setFileSize(new Size(fileSize));
		
		fragment.setPassword(rs.getString(columns.next()));
		
		Long tagId = rs.getLong(columns.next());	
		if (tagId != 0) fragment.setTagId(tagId);

		return fragment;
	}
}