package marubinotto.piggydb.external.jdbc.h2;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import marubinotto.piggydb.external.jdbc.h2.mapper.TagRowMapper;
import marubinotto.piggydb.model.Classifiable;
import marubinotto.piggydb.model.Classification;
import marubinotto.piggydb.model.MutableClassification;
import marubinotto.piggydb.model.Tag;
import marubinotto.piggydb.model.TagRepository;
import marubinotto.piggydb.model.entity.RawClassifiable;
import marubinotto.piggydb.model.entity.RawTag;
import marubinotto.piggydb.model.repository.RawEntityFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

public class QueryUtils {
	
	private static Log logger = LogFactory.getLog(QueryUtils.class);
	
	public static final Byte TAGGING_TARGET_TAG = 1;
	public static final Byte TAGGING_TARGET_FRAGMENT = 2;
	public static final Byte TAGGING_TARGET_FILTER_CLASSIFICATION = 3;
	public static final Byte TAGGING_TARGET_FILTER_EXCLUDES = 4;
	
	public static void appendLimit(StringBuilder sql, int pageSize, int pageIndex) {
		sql.append(" limit ");
        sql.append(pageSize * pageIndex);
        sql.append(", ");
        sql.append(pageSize);
	}
	
	public static void registerNewTagging(
		long tagId, 
		long targetId,
		Byte targetType, 
		JdbcTemplate jdbcTemplate) {
		
		StringBuffer sql = new StringBuffer();
        sql.append("insert into tagging (");
        sql.append("tag_id, target_id, target_type");
        sql.append(") values (?, ?, ?)");
        
        Object[] params = new Object[] {
            new Long(tagId),
            new Long(targetId),
            targetType
        };
        
        jdbcTemplate.update(sql.toString(), params);
	}
	
	public static void deleteTagging(
		long tagId, 
		long targetId,
		Byte targetType, 
		JdbcTemplate jdbcTemplate) {
		
		StringBuffer sql = new StringBuffer();
		sql.append("delete from tagging");
		sql.append(" where tag_id = ? and target_id = ? and target_type = ?");
        
        Object[] params = new Object[] {
            new Long(tagId),
            new Long(targetId),
            targetType
        };
        
        jdbcTemplate.update(sql.toString(), params);
	}
	
	public static void registerTaggings(
		Classifiable classifiable,
		Byte targetType,
		JdbcTemplate jdbcTemplate,
		TagRepository tagRepository) 
	throws Exception {
		registerTaggings(
			classifiable.getClassification(),
			classifiable.getId(),
			targetType,
			jdbcTemplate,
			tagRepository);
	}
	
	public static void registerTaggings(
		Classification classification,
		Long targetId,
		Byte targetType,
		JdbcTemplate jdbcTemplate,
		TagRepository tagRepository) 
	throws Exception {
		for (Tag tag : classification) {
			Long tagId = tag.getId();
			if (tagId == null) tagId = tagRepository.register(tag);
			registerNewTagging(tagId, targetId, targetType, jdbcTemplate);
		}
	}
	
	public static void updateTaggings(
		RawClassifiable classifiable, 
		Byte targetType,
		JdbcTemplate jdbcTemplate,
		TagRepository tagRepository) 
	throws Exception {
		updateTaggings(
			classifiable.getClassification(),
			classifiable.getId(),
			targetType,
			jdbcTemplate,
			tagRepository);
	}
	
	public static void updateTaggings(
		Classification classification,
		Long targetId,
		Byte targetType,
		JdbcTemplate jdbcTemplate,
		TagRepository tagRepository) 
	throws Exception {
		List<Long> currentTagIds = getParentTagIds(targetId, targetType, jdbcTemplate);
		for (Tag tag : classification) {
			Long tagId = tag.getId();
			if (tagId == null) tagId = tagRepository.register(tag);
			if (!currentTagIds.remove(tagId))
				registerNewTagging(tagId, targetId, targetType, jdbcTemplate);
		}
		for (Long tagToRemove : currentTagIds) {	// the remains should be deleted
			deleteTagging(tagToRemove, targetId, targetType, jdbcTemplate);
		}
	}
	
	@SuppressWarnings("unchecked")
	public static List<Long> getParentTagIds(
		Long targetId, 
		Byte targetType, 
		JdbcTemplate jdbcTemplate) {
		
		return (List<Long>)jdbcTemplate.queryForList(
            "select tag_id from tagging where target_id = ? and target_type = ?", 
            new Object[]{targetId, targetType}, 
            Long.class);
	}

	@SuppressWarnings("unchecked")
	public static List<RawTag> getParentTags(
		Long targetId, 
		Byte targetType,
		JdbcTemplate jdbcTemplate,
		RawEntityFactory<RawTag> entityFactory) {
		
		TagRowMapper tagRowMapper = new TagRowMapper(entityFactory, "tag.");	// TODO
		
		StringBuffer sql = new StringBuffer();
		sql.append("select ");
		sql.append(tagRowMapper.selectAll());
		sql.append(" from tagging, tag");
		sql.append(" where tagging.tag_id = tag.tag_id");
		sql.append(" and tagging.target_type = " + targetType);
		sql.append(" and tagging.target_id = ?");
		
		if (logger.isDebugEnabled()) 
			logger.debug("getParentTags for: " + targetId + " (type: " + targetType + ")");
		return jdbcTemplate.query(
            sql.toString(), 
            new Object[]{targetId}, 
            tagRowMapper);
	}
	
	public static Map<Long, RawTag> setOnlyParentTags(
		RawClassifiable classifiable,
		Byte targetType,
		JdbcTemplate jdbcTemplate,
		RawEntityFactory<RawTag> entityFactory) 
	throws Exception {
		return setOnlyParentTags(
			classifiable.getMutableClassification(),
			classifiable.getId(),
			targetType,
			jdbcTemplate,
			entityFactory);
	}
	
	public static Map<Long, RawTag> setOnlyParentTags(
		MutableClassification classification,
		Long targetId,
		Byte targetType,
		JdbcTemplate jdbcTemplate,
		RawEntityFactory<RawTag> entityFactory) 
	throws Exception {
		List<RawTag> parentTags = 
			getParentTags(targetId, targetType, jdbcTemplate, entityFactory);
		
		Map<Long, RawTag> id2parents = new HashMap<Long, RawTag>();
		for (RawTag parentTag : parentTags) {
			classification.addTag(parentTag);
			id2parents.put(parentTag.getId(), parentTag);
		}
		return id2parents;
	}
	
	/**
	 * The same tag object will be shared in the same level
	 */
	@SuppressWarnings("unchecked")
	public static void setTagsRecursively(
		Map<Long, ? extends RawClassifiable> classifiables, 
		Byte targetType,
		JdbcTemplate jdbcTemplate,
		RawEntityFactory<RawTag> entityFactory) 
	throws Exception {
		TagRowMapper tagRowMapper = new TagRowMapper(entityFactory, "tag.");
		
		StringBuilder sql = new StringBuilder();
		sql.append("select ");
		sql.append(tagRowMapper.selectAll());
		sql.append(", tagging.target_id");
		sql.append(" from tag, tagging");
		sql.append(" where tag.tag_id = tagging.tag_id");
		sql.append(" and tagging.target_type = " + targetType);
		sql.append(" and tagging.target_id in (");
		boolean first = true;
		for (Long id : classifiables.keySet()) {
			if (first) first = false; else sql.append(", ");
			sql.append(id);
		}
		sql.append(")");
		
		if (logger.isDebugEnabled()) logger.debug("setParentTags for: " + classifiables.values());		
		List tagMappings = jdbcTemplate.query(sql.toString(), new TagMappingRowMapper(tagRowMapper));
		if (tagMappings.size() == 0) return;
		
		Map<Long, RawTag> tags = new HashMap<Long, RawTag>();
		for (Object e : tagMappings) {
			TagMapping tagMapping = (TagMapping)e;
			
			// Restore a mapping
			RawClassifiable target = classifiables.get(tagMapping.targetId);
			target.getMutableClassification().addTag(tagMapping.tag);
			
			// Collect tags
			tags.put(tagMapping.tag.getId(), tagMapping.tag);
		}
		
		setTagsRecursively(tags, TAGGING_TARGET_TAG, jdbcTemplate, entityFactory);
	}
	
	private static class TagMapping {
		public Long targetId;
		public RawTag tag;
	}
	
	private static class TagMappingRowMapper implements RowMapper {
		private TagRowMapper tagRowMapper;
		private Map<Long, RawTag> tagCache = new HashMap<Long, RawTag>();
		
		public TagMappingRowMapper(TagRowMapper tagRowMapper) {
			this.tagRowMapper = tagRowMapper;
		}
		
        public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
        	RawTag tag = this.tagRowMapper.mapRow(rs, rowNum);
			if (this.tagCache.containsKey(tag.getId())) {
				tag = this.tagCache.get(tag.getId());
			}
			else {
				this.tagCache.put(tag.getId(), tag);
			}
			
			TagMapping tagMapping = new TagMapping();
			tagMapping.tag = tag;
			tagMapping.targetId = rs.getLong("tagging.target_id");
			return tagMapping;
		}
	};

	public static <T> Map<Long, T> getValuesForIds(
		String tableName, 
		final String columnName, 
		Set<Long> ids, 
		JdbcTemplate jdbcTemplate)
	throws Exception {
		final Map<Long, T> values = new HashMap<Long, T>();
		if (ids.isEmpty()) return values;
		
		final String idName = tableName + "_id";
		StringBuilder sql = new StringBuilder();
		sql.append("select " + idName + ", " + columnName);
		sql.append(" from " + tableName);
		sql.append(" where " + idName + " in (");
		boolean first = true;
		for (Long tagId : ids) {
			if (first) first = false; else sql.append(", ");
			sql.append(tagId);
		}
		sql.append(")");
		
		jdbcTemplate.query(
            sql.toString(), 
            new RowMapper() {
				@SuppressWarnings("unchecked")
				public Object mapRow(ResultSet rs, int rowNum) 
				throws SQLException {
					values.put(rs.getLong(idName), (T)rs.getObject(columnName));
					return null;
				}
            });
		
		return values;
	}
}
