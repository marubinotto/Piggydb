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
			.defColumn("password");

	public FragmentRowMapper(RawEntityFactory<RawFragment> factory, String prefix) {
		super(factory, prefix);
	}

	@Override
	protected EntityTable getEntityTable() {
		return TABLE;
	}

	public static void insert(RawFragment fragment, JdbcTemplate jdbcTemplate) {
		Assert.Arg.notNull(fragment, "fragment");
		Assert.Arg.notNull(jdbcTemplate, "jdbcTemplate");

		Object[] values = new Object[]{
			fragment.getTitle(),
			fragment.getContent(),
			fragment.getFileName(),
			fragment.getFileType(),
			fragment.getFileSize() != null ? fragment.getFileSize().getValue() : null,
			fragment.getPassword()};
		TABLE.insert(fragment, values, jdbcTemplate);
	}

	public static void update(RawFragment fragment, boolean updateTimestamp,
		JdbcTemplate jdbcTemplate) throws BaseDataObsoleteException {
		Assert.Arg.notNull(fragment, "fragment");
		Assert.Arg.notNull(jdbcTemplate, "jdbcTemplate");

		Object[] values = new Object[]{
			fragment.getTitle(),
			fragment.getContent(),
			fragment.getFileName(),
			fragment.getFileType(),
			fragment.getFileSize() != null ? fragment.getFileSize().getValue() : null,
			fragment.getPassword()};
		TABLE.update(fragment, values, updateTimestamp, jdbcTemplate);
	}

	public RawFragment mapRow(ResultSet rs, int rowNum) throws SQLException {
		RawFragment fragment = createEntityWithCommonColumns(rs);

		Iterator<String> columns = properColumns();
		fragment.setTitle(rs.getString(columns.next()));
		fragment.setContent(rs.getString(columns.next()));
		fragment.setFileName(rs.getString(columns.next()));
		fragment.setFileType(rs.getString(columns.next()));
		Long fileSize = rs.getLong(columns.next());
		if (fileSize != null) fragment.setFileSize(new Size(fileSize));
		fragment.setPassword(rs.getString(columns.next()));

		return fragment;
	}
}