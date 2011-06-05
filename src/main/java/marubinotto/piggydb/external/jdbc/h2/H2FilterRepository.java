package marubinotto.piggydb.external.jdbc.h2;

import java.util.List;
import java.util.Map;
import java.util.Set;

import marubinotto.piggydb.external.jdbc.JdbcDao;
import marubinotto.piggydb.external.jdbc.h2.mapper.FilterRowMapper;
import marubinotto.piggydb.model.BaseDataObsoleteException;
import marubinotto.piggydb.model.DuplicateException;
import marubinotto.piggydb.model.Filter;
import marubinotto.piggydb.model.User;
import marubinotto.piggydb.model.entity.RawFilter;
import marubinotto.piggydb.model.entity.RawTag;
import marubinotto.piggydb.model.repository.AbstractFilterRepository;
import marubinotto.piggydb.model.repository.RawEntityFactory;
import marubinotto.util.Assert;
import marubinotto.util.paging.Page;
import marubinotto.util.paging.PageUtils;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.incrementer.DataFieldMaxValueIncrementer;

public class H2FilterRepository extends AbstractFilterRepository 
implements JdbcDao, RawEntityFactory<RawFilter> {

	private static Log logger = LogFactory.getLog(H2FilterRepository.class);
	
	private H2TagRepository tagRepository;
	
	protected JdbcTemplate jdbcTemplate;
	private DataFieldMaxValueIncrementer filterIdIncrementer;
	
	private FilterRowMapper filterRowMapper = new FilterRowMapper(this, "filter.");
	
	public H2FilterRepository() {
	}
	
	public void setTagRepository(H2TagRepository tagRepository) {
		this.tagRepository = tagRepository;
	}
	
	public H2TagRepository getTagRepository() {
		return this.tagRepository;
	}

    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
	
	public void setFilterIdIncrementer(
		DataFieldMaxValueIncrementer filterIdIncrementer) {
		this.filterIdIncrementer = filterIdIncrementer;
	}

	public long register(Filter filter) throws Exception {
		Assert.Arg.notNull(filter, "filter");
		Assert.require(filter instanceof RawFilter, "filter instanceof RawFilter");
		Assert.require(filter.getId() == null, "filter.getId() == null");
		Assert.Property.requireNotNull(filterIdIncrementer, "filterIdIncrementer");
		
		if (containsName(filter.getName())) {
			throw new DuplicateException("Duplicate filter name: " + filter.getName());
		}
		
		((RawFilter)filter).setId(new Long(this.filterIdIncrementer.nextLongValue()));
		FilterRowMapper.insert((RawFilter)filter, this.jdbcTemplate);
		QueryUtils.registerTaggings(
			filter.getClassification(),
			filter.getId(),
			QueryUtils.TAGGING_TARGET_FILTER_CLASSIFICATION,
			this.jdbcTemplate,
			this.tagRepository);
		QueryUtils.registerTaggings(
			filter.getExcludes(),
			filter.getId(),
			QueryUtils.TAGGING_TARGET_FILTER_EXCLUDES,
			this.jdbcTemplate,
			this.tagRepository);
		
		return filter.getId();
	}
	
	private boolean containsName(String name) throws Exception {
		Assert.Arg.notNull(name, "name");
		
		return this.jdbcTemplate.queryForInt(
            "select count(*) from filter where filter_name = ?", 
            new Object[]{name}) > 0;
	}
	
	public Filter get(long id) throws Exception {
		RawFilter filter = queryForOneFilter(
			"select " + filterRowMapper.selectAll() + " from filter where filter_id = ?", 
			new Object[]{new Long(id)});
		if (filter == null) {
			return null;
		}
		setTags(filter);
		return filter;
	}
	
	private RawFilter queryForOneFilter(String sql, Object[] args) {
        try {
            return (RawFilter)this.jdbcTemplate.queryForObject(sql, args, filterRowMapper);
        }
        catch (EmptyResultDataAccessException e) {
            return null;
        }
    }
	
	private void setTags(RawFilter filter) throws Exception {
		// Classification
		Map<Long, RawTag> id2class = QueryUtils.setOnlyParentTags(
			filter.getClassification(), 
			filter.getId(),
			QueryUtils.TAGGING_TARGET_FILTER_CLASSIFICATION, 
			this.jdbcTemplate,
			getTagRepository());
		if (id2class.size() > 0) {
			QueryUtils.setTagsRecursively(
				id2class, 
				QueryUtils.TAGGING_TARGET_TAG, 
				this.jdbcTemplate, 
				getTagRepository());
		}
		
		// Excludes
		Map<Long, RawTag> id2excludes = QueryUtils.setOnlyParentTags(
			filter.getExcludes(), 
			filter.getId(),
			QueryUtils.TAGGING_TARGET_FILTER_EXCLUDES, 
			this.jdbcTemplate,
			getTagRepository());
		if (id2excludes.size() > 0) {
			QueryUtils.setTagsRecursively(
				id2excludes, 
				QueryUtils.TAGGING_TARGET_TAG, 
				this.jdbcTemplate, 
				getTagRepository());
		}
	}
	
	public Filter getByName(String name) throws Exception {
		Assert.Arg.notNull(name, "name");
		
		RawFilter filter = queryForOneFilter(
			"select " + filterRowMapper.selectAll() + 
				" from filter where filter_name = ?", 
			new Object[]{name});
		if (filter == null) {
			return null;
		}
		setTags(filter);
		return filter;
	}
	
	public Long getIdByName(String name) throws Exception {
		Assert.Arg.notNull(name, "name");
		try {
			return (Long)this.jdbcTemplate.queryForObject(
				"select filter_id from filter where filter_name = ?", 
				new Object[]{name}, 
				Long.class);
		}
		catch (EmptyResultDataAccessException e) {
            return null;
        }
	}

	@SuppressWarnings("unchecked")
	public List<String> getNamesLike(String criteria) throws Exception {
		Assert.Arg.notNull(criteria, "criteria");
			
		criteria = StringEscapeUtils.escapeSql(criteria);		
		return (List<String>)this.jdbcTemplate.queryForList(
            "select filter_name from filter where filter_name like '" + criteria + "%'", 
            new Object[]{}, 
            String.class);
	}

	public boolean update(Filter filter) 
	throws BaseDataObsoleteException, Exception {
		Assert.Arg.notNull(filter, "filter");
		Assert.require(filter instanceof RawFilter, "filter instanceof RawFilter");
		Assert.Arg.notNull(filter.getId(), "filter.getId()");
		
		// Check preconditions
		if (!containsId(filter.getId())) {
			logger.info("[update] No such filter ID: " + filter.getId()); 
			return false;
		}
		checkIfNameIsValidToUpdate(filter);
		if (filter.getUpdateDatetime() == null) {
			throw new BaseDataObsoleteException();
		}
		
		// Do update
		FilterRowMapper.update((RawFilter)filter, this.jdbcTemplate);
		QueryUtils.updateTaggings(
			filter.getClassification(),
			filter.getId(),
			QueryUtils.TAGGING_TARGET_FILTER_CLASSIFICATION,
			this.jdbcTemplate,
			this.tagRepository);		
		QueryUtils.updateTaggings(
			filter.getExcludes(),
			filter.getId(),
			QueryUtils.TAGGING_TARGET_FILTER_EXCLUDES,
			this.jdbcTemplate,
			this.tagRepository);		

		return true;
	}
	
	private boolean containsId(Long id) throws Exception {
		return this.jdbcTemplate.queryForInt(
            "select count(*) from filter where filter_id = ?", 
            new Object[]{id}) > 0;
	}
	
	private void checkIfNameIsValidToUpdate(Filter filter) throws DuplicateException {
		int duplicate = this.jdbcTemplate.queryForInt(
            "select count(*) from filter where filter_id <> ? and filter_name = ?", 
            new Object[]{filter.getId(), filter.getName()});
		if (duplicate > 0) {
			throw new DuplicateException("Duplicate filter name: " + filter.getName());
		}
	}
	
	@SuppressWarnings("unchecked")
	public Page<Filter> getRecentChanges(int pageSize, int pageIndex)
	throws Exception {
		StringBuilder sql = new StringBuilder();
		sql.append("select ");
		sql.append(filterRowMapper.selectAll());
		sql.append(" from filter order by update_datetime desc");
		QueryUtils.appendLimit(sql, pageSize, pageIndex);
		
		List<Filter> results = this.jdbcTemplate.query(sql.toString(), filterRowMapper);
		
		return PageUtils.toPage(results, pageSize, pageIndex, new PageUtils.TotalCounter() {
			public long getTotalSize() throws Exception {
				return size();
			}
		});
	}

	@Override
	protected void doDelete(Filter filter, User user) throws Exception {
		// Delete related taggings
		this.jdbcTemplate.update(
            "delete from tagging where target_id = ? and target_type in (?, ?)", 
            new Object[]{
            	filter.getId(),
            	QueryUtils.TAGGING_TARGET_FILTER_CLASSIFICATION,
            	QueryUtils.TAGGING_TARGET_FILTER_EXCLUDES
            });
		
		// Delete the fragment 
		this.jdbcTemplate.update(
            "delete from filter where filter_id = ?", 
            new Object[]{filter.getId()});
	}

	public long size() throws Exception {
		return (Long)this.jdbcTemplate.queryForObject(
			"select count(*) from filter", Long.class);
	}
	
	public Map<Long, String> getNames(Set<Long> ids) throws Exception {
		Assert.Arg.notNull(ids, "ids");
		return QueryUtils.getValuesForIds("filter", "filter_name", ids, this.jdbcTemplate);
	}
}
