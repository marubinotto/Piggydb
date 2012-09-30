package marubinotto.piggydb.impl.query;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import marubinotto.piggydb.model.Fragment;
import marubinotto.piggydb.model.Tag;
import marubinotto.piggydb.model.TagRepository;
import marubinotto.piggydb.model.query.FragmentsOfUser;
import marubinotto.util.Assert;
import marubinotto.util.paging.Page;

import org.springframework.jdbc.core.RowMapper;

public class H2FragmentsOfUser 
extends H2FragmentsQueryBase implements FragmentsOfUser {

	private String userName;

	public void setUserName(String userName) {
		this.userName = userName;
	}
	
	protected void appendFromWhere(StringBuilder sql, List<Object> args) 
	throws Exception {
		// Do nothing
	}
	
	@SuppressWarnings("unchecked")
	public List<Fragment> getAll() throws Exception {
		Assert.Property.requireNotNull(userName, "userName");
		
		TagRepository tagRepository = getRepository().getTagRepository();
		
		Tag userTag = tagRepository.getByName(Tag.NAME_USER);
		if (userTag == null) return new ArrayList<Fragment>();

		StringBuilder sql  = new StringBuilder();
		appendSqlToSelectFragmentIdsTaggedWithAnyOf(
			sql, userTag.expandToIdsOfSubtree(tagRepository), false);
		sql.append(" and f.title = ?");
		
		Tag trashTag = tagRepository.getTrashTag();
		if (trashTag != null) {
			sql.append(" minus ");
			appendSqlToSelectFragmentIdsTaggedWithAnyOf(
				sql, trashTag.expandToIdsOfSubtree(tagRepository), false);
		}

		List<Long> ids = (List<Long>)getJdbcTemplate().query(
			sql.toString(), new Object[]{userName}, new RowMapper() {
				public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
					return rs.getLong(1);
				}
			});
		if (ids.isEmpty()) return new ArrayList<Fragment>();
		
		return getByIds(ids);
	}
	
	public Page<Fragment> getPage(int pageSize, int pageIndex) throws Exception {
		throw new UnsupportedOperationException();
	}
	
	public Fragment getUserFragment() throws Exception {
		List<Fragment> fragments = getAll();
		return fragments.isEmpty() ? null : fragments.get(0);
	}
}
