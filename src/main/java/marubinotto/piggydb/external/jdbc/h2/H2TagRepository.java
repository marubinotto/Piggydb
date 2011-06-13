package marubinotto.piggydb.external.jdbc.h2;

import static marubinotto.util.CollectionUtils.map;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import marubinotto.piggydb.external.jdbc.JdbcDao;
import marubinotto.piggydb.external.jdbc.h2.mapper.TagRowMapper;
import marubinotto.piggydb.model.BaseDataObsoleteException;
import marubinotto.piggydb.model.DuplicateException;
import marubinotto.piggydb.model.Tag;
import marubinotto.piggydb.model.TagRepository;
import marubinotto.piggydb.model.User;
import marubinotto.piggydb.model.entity.RawTag;
import marubinotto.piggydb.util.PiggydbUtils;
import marubinotto.util.Assert;
import marubinotto.util.paging.Page;
import marubinotto.util.paging.PageUtils;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.incrementer.DataFieldMaxValueIncrementer;

public class H2TagRepository extends TagRepository.Base implements JdbcDao {
	
	private static Log logger = LogFactory.getLog(H2TagRepository.class);

	protected JdbcTemplate jdbcTemplate;
	private DataFieldMaxValueIncrementer tagIdIncrementer;
	
	private TagRowMapper tagRowMapper = new TagRowMapper(this, "tag.");

    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

	public JdbcTemplate getJdbcTemplate() {
		return jdbcTemplate;
	}

	public void setTagIdIncrementer(DataFieldMaxValueIncrementer incrementer) {
		this.tagIdIncrementer = incrementer;
	}

	public long register(Tag tag) throws Exception {
		Assert.Arg.notNull(tag, "tag");
		Assert.require(tag instanceof RawTag, "tag instanceof RawTag");
		Assert.Arg.notNull(tag.getName(), "tag.getName()");
		Assert.require(tag.getId() == null, "tag.getId() == null");
		Assert.Property.requireNotNull(tagIdIncrementer, "tagIdIncrementer");
		
		if (containsName(tag.getName())) {
			throw new DuplicateException("Duplicate tag name: " + tag.getName());
		}
		
		((RawTag)tag).setId(new Long(this.tagIdIncrementer.nextLongValue()));
		TagRowMapper.insert((RawTag)tag, this.jdbcTemplate);
		QueryUtils.registerTaggings(
			tag, 
			QueryUtils.TAGGING_TARGET_TAG, 
			this.jdbcTemplate, 
			this);
		
		return tag.getId().longValue();
	}

	public Tag get(long id) throws Exception {
		RawTag tag = queryForOneTag(
			"select " + tagRowMapper.selectAll() + 
				" from tag where tag_id = ?", 
			new Object[]{new Long(id)});
		if (tag == null) return null;
		setSuperordinateTags(tag);
		return tag;
	}

	public Tag getByName(String name) throws Exception {
		Assert.Arg.notNull(name, "name");
		
		RawTag tag = queryForOneTag(
			"select " + tagRowMapper.selectAll() + 
				" from tag where tag_name = ?", 
			new Object[]{name});
		if (tag == null) return null;
		setSuperordinateTags(tag);	
		return tag;
	}

	private RawTag queryForOneTag(String sql, Object[] args) {
		try {
			return (RawTag) this.jdbcTemplate.queryForObject(sql, args, tagRowMapper);
		}
		catch (EmptyResultDataAccessException e) {
			return null;
		}
	}
	
	private void setSuperordinateTags(RawTag tag) throws Exception {
		QueryUtils.setTagsRecursively(
			map(tag.getId(), tag), QueryUtils.TAGGING_TARGET_TAG, this.jdbcTemplate, this);
	}

	public boolean containsName(String name) throws Exception {
		Assert.Arg.notNull(name, "name");

		return this.jdbcTemplate.queryForInt(
			"select count(*) from tag where tag_name = ?", new Object[]{name}) > 0;
	}
	
	public Long getIdByName(String tagName) {
		Assert.Arg.notNull(tagName, "tagName");
		try {
			return (Long) getJdbcTemplate().queryForObject(
				"select tag_id from tag where tag_name = ?", new Object[]{tagName},
				Long.class);
		}
		catch (EmptyResultDataAccessException e) {
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	public List<String> getNamesLike(String criteria) throws Exception {
		Assert.Arg.notNull(criteria, "criteria");
		
		criteria = criteria.toLowerCase();
		criteria = StringEscapeUtils.escapeSql(criteria);		
		return (List<String>)this.jdbcTemplate.queryForList(
			"select tag_name from tag where LOWER(tag_name) like '" + criteria + "%'", String.class);
	}

	public boolean update(Tag tag) throws BaseDataObsoleteException, Exception {
		Assert.Arg.notNull(tag, "tag");
		Assert.require(tag instanceof RawTag, "tag instanceof RawTag");
		Assert.Arg.notNull(tag.getId(), "tag.getId()");
		Assert.Arg.notNull(tag.getName(), "tag.getName()");
				
		// Check preconditions
		if (!containsId(tag.getId())) {
			logger.info("[update] No such tag ID: " + tag.getId()); 
			return false;
		}
		checkIfNameIsValidToUpdate(tag);
		if (tag.getUpdateDatetime() == null) {
			throw new BaseDataObsoleteException();
		}
		
		// Do update
		TagRowMapper.update((RawTag)tag, this.jdbcTemplate);
		QueryUtils.updateTaggings(
			(RawTag)tag, QueryUtils.TAGGING_TARGET_TAG, this.jdbcTemplate, this);
		
		return true;
	}
	
	private boolean containsId(Long id) throws Exception {
		return this.jdbcTemplate.queryForInt(
			"select count(*) from tag where tag_id = ?", new Object[]{id}) > 0;
	}
	
	private void checkIfNameIsValidToUpdate(Tag tag) throws DuplicateException {
		int duplicate = this.jdbcTemplate.queryForInt(
			"select count(*) from tag where tag_id <> ? and tag_name = ?",
			new Object[]{tag.getId(), tag.getName()});
		if (duplicate > 0) {
			throw new DuplicateException("Duplicate tag name: " + tag.getName());
		}
	}

	@Override
	protected void doDelete(Tag tag, User user) throws Exception {
		this.jdbcTemplate.update("delete from tag where tag_id = ?",
			new Object[]{tag.getId()});

		this.jdbcTemplate
			.update(
				"delete from tagging where tag_id = ? or (target_id = ? and target_type = ?)",
				new Object[]{tag.getId(), tag.getId(), QueryUtils.TAGGING_TARGET_TAG});
	}
	
	public long size() throws Exception {
		return (Long)this.jdbcTemplate.queryForObject(
			"select count(*) from tag", Long.class);
	}
	
	@SuppressWarnings("unchecked")
	public Iterator<String> iterateAllTagNames() throws Exception {
		return this.jdbcTemplate.queryForList(
			"select tag_name from tag order by tag_name desc", 
			String.class).iterator();
	}

	@SuppressWarnings("unchecked")
	public Page<Tag> getRecentChanges(int pageSize, int pageIndex)
	throws Exception {
		StringBuilder sql = new StringBuilder();
		sql.append("select ");
		sql.append(tagRowMapper.selectAll());
		sql.append(" from tag order by update_datetime desc");
		QueryUtils.appendLimit(sql, pageSize, pageIndex);
        
		List<Tag> results = this.jdbcTemplate.query(sql.toString(), tagRowMapper);
		
		return PageUtils.toPage(results, pageSize, pageIndex, new PageUtils.TotalCounter() {
			public long getTotalSize() throws Exception {
				return size();
			}
		});
	}

	@SuppressWarnings("unchecked")
	public Page<Tag> getRootTags(int pageSize, int pageIndex) throws Exception {
		StringBuilder sql = new StringBuilder();
		sql.append("select ");
		sql.append(tagRowMapper.selectAll());
		
		StringBuilder condition = new StringBuilder();
		condition.append(" from tag left outer join tagging");
		condition.append(" on tag.tag_id = tagging.target_id");
		condition.append(" and tagging.target_type = " + QueryUtils.TAGGING_TARGET_TAG);
		condition.append(" where tagging.tag_id is null");

		sql.append(condition);
		sql.append(" order by LOWER(tag.tag_name)");
		QueryUtils.appendLimit(sql, pageSize, pageIndex);
		
		List<Tag> results = this.jdbcTemplate.query(sql.toString(), tagRowMapper);
		
		final String queryAll = "select count(*)" + condition;
		return PageUtils.toPage(results, pageSize, pageIndex, new PageUtils.TotalCounter() {
			public long getTotalSize() throws Exception {
				return (Long)getJdbcTemplate().queryForObject(queryAll, Long.class);
			}
		});
	}

	@SuppressWarnings("unchecked")
	public Page<Tag> findByParentTag(
		final long parentTagId, 
		int pageSize, 
		int pageIndex) 
	throws Exception {
		StringBuilder sql = new StringBuilder();
		sql.append("select ");
		sql.append(tagRowMapper.selectAll());
		
		StringBuilder condition = new StringBuilder();
		condition.append(" from tag, tagging");
		condition.append(" where tag.tag_id = tagging.target_id");
		condition.append(" and tagging.target_type = " + QueryUtils.TAGGING_TARGET_TAG);
		condition.append(" and tagging.tag_id = ?");
		
		sql.append(condition);
		sql.append(" order by LOWER(tag.tag_name)");
		QueryUtils.appendLimit(sql, pageSize, pageIndex);
		
		List<Tag> results = this.jdbcTemplate.query(
            sql.toString(), new Object[]{parentTagId}, tagRowMapper);
		
		final String queryAll = "select count(*)" + condition;
		return PageUtils.toPage(results, pageSize, pageIndex, new PageUtils.TotalCounter() {
			public long getTotalSize() throws Exception {
				return (Long)getJdbcTemplate().queryForObject(
					queryAll, new Object[]{parentTagId}, Long.class);
			}
		});
	}

	public Set<Long> getAllSubordinateTagIds(Set<Long> tagIds) throws Exception {
		Assert.Arg.notNull(tagIds, "tagIds");
		
		Set<Long> results = new HashSet<Long>();
		collectSubTagIds(tagIds, results);
		return results;
	}

	@SuppressWarnings("unchecked")
	private void collectSubTagIds(Set<Long> tagIds, Set<Long> results) 
	throws Exception {
		if (tagIds.isEmpty()) return;
		
		StringBuilder sql = new StringBuilder();
		sql.append("select target_id from tagging");
		sql.append(" where target_type = " + QueryUtils.TAGGING_TARGET_TAG);
		sql.append(" and tag_id in (");
		boolean first = true;
		for (Long tagId : tagIds) {
			if (first) first = false; else sql.append(", ");
			sql.append(tagId);
		}
		sql.append(")");
		logger.debug("collectSubTagIds: " + sql);
		
		Set<Long> children = new HashSet<Long>(
			this.jdbcTemplate.queryForList(sql.toString(), Long.class));
		if (children.isEmpty()) return;
		
		results.addAll(children);
		collectSubTagIds(children, results);
	}
	
	public Map<Long, String> getNames(Set<Long> ids) throws Exception {
		Assert.Arg.notNull(ids, "ids");
		return QueryUtils.getValuesForIds("tag", "tag_name", ids, this.jdbcTemplate);
	}
	
	@SuppressWarnings("unchecked")
	public Set<Long> selectAllThatHaveChildren(Set<Long> tagIds) throws Exception {
		Assert.Arg.notNull(tagIds, "tagIds");
		
		StringBuilder sql = new StringBuilder();
		sql.append("select tag_id from tagging");
		sql.append(" where target_type = " + QueryUtils.TAGGING_TARGET_TAG);
		sql.append(" and tag_id in (");
		boolean first = true;
		for (Long tagId : tagIds) {
			if (first) first = false; else sql.append(", ");
			sql.append(tagId);
		}
		sql.append(")");
		
		return new HashSet<Long>(
			(List<Long>)this.jdbcTemplate.queryForList(sql.toString(), Long.class));
	}
	
	@SuppressWarnings("unchecked")
	public Page<Tag> findByKeywords(String keywords, int pageSize, int pageIndex)
	throws Exception {
		if (StringUtils.isBlank(keywords)) return PageUtils.empty(pageSize);

		// TODO should be locale sensitive?
		// TODO a keyword may have to be escaped
		final String[] keywordList = PiggydbUtils.splitToKeywords(keywords.toLowerCase());
		if (keywordList.length == 0) return PageUtils.empty(pageSize);

		StringBuilder sql = new StringBuilder();
		sql.append("select ");
		sql.append(tagRowMapper.selectAll());
		
		StringBuilder condition = new StringBuilder();
		condition.append(" from tag");
		condition.append(" where");
		for (int i = 0; i < keywordList.length; i++) {
			if (i > 0) condition.append(" and");
			condition.append(" LOWER(tag_name) like '%' || ? || '%'");		
		}

		sql.append(condition);
		sql.append(" order by LOWER(tag_name)");
		QueryUtils.appendLimit(sql, pageSize, pageIndex);
		
		List<Tag> results = this.jdbcTemplate.query(sql.toString(), keywordList, tagRowMapper);
		
		final String queryAll = "select count(*)" + condition;
		return PageUtils.toPage(results, pageSize, pageIndex, new PageUtils.TotalCounter() {
			public long getTotalSize() throws Exception {
				return (Long)getJdbcTemplate().queryForObject(queryAll, keywordList, Long.class);
			}
		});
	}
	
	private TagRowMapper tagWithPopularityMapper = new TagRowMapper(this, "tag.") {
		@Override
		public RawTag mapRow(ResultSet rs, int rowNum) throws SQLException {
			RawTag tag = super.mapRow(rs, rowNum);
			tag.setPopularity(rs.getLong("popularity"));
			return tag;
		}
	};
	
	@SuppressWarnings("unchecked")
	public List<Tag> getPopularTags(int maxSize) throws Exception {
		StringBuilder sql = new StringBuilder();
		sql.append("select ");
		sql.append(tagWithPopularityMapper.selectAll());
		sql.append(", count(tag.tag_id) as popularity");
		sql.append(" from tagging, tag");
		sql.append(" where tagging.tag_id = tag.tag_id");
		sql.append(" and tagging.target_type in (");
		sql.append(QueryUtils.TAGGING_TARGET_TAG);
		sql.append(", " + QueryUtils.TAGGING_TARGET_FRAGMENT + ")");
		sql.append(" group by tag.tag_id");
		sql.append(" order by popularity desc");
		QueryUtils.appendLimit(sql, maxSize, 0);
		logger.debug("orderByPopularity: " + sql);
		
		return this.jdbcTemplate.query(sql.toString(), tagWithPopularityMapper);
	}
	
	@SuppressWarnings("unchecked")
	public Page<Tag> orderByName(int pageSize, int pageIndex) throws Exception {
		StringBuilder sql = new StringBuilder();
		sql.append("select ");
		sql.append(tagRowMapper.selectAll());
		sql.append(" from tag order by LOWER(tag_name)");
		QueryUtils.appendLimit(sql, pageSize, pageIndex);
        
		List<Tag> results = this.jdbcTemplate.query(sql.toString(), tagRowMapper);
		
		return PageUtils.toPage(results, pageSize, pageIndex, new PageUtils.TotalCounter() {
			public long getTotalSize() throws Exception {
				return size();
			}
		});
	}
	
	public Long countTaggings() throws Exception {
		StringBuilder sql = new StringBuilder();
		sql.append("select count(*) from tagging where target_type in (");
		sql.append(QueryUtils.TAGGING_TARGET_TAG);
		sql.append(", " + QueryUtils.TAGGING_TARGET_FRAGMENT + ")");
		
		return (Long)this.jdbcTemplate.queryForObject(sql.toString(), Long.class);
	}
}
