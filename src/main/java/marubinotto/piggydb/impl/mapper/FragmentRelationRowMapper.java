package marubinotto.piggydb.impl.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import marubinotto.piggydb.model.DuplicateException;
import marubinotto.piggydb.model.Fragment;
import marubinotto.piggydb.model.FragmentRelation;
import marubinotto.piggydb.model.FragmentRepository;
import marubinotto.piggydb.model.entity.RawEntityFactory;
import marubinotto.util.Assert;
import marubinotto.util.CollectionUtils;

import org.apache.commons.lang.UnhandledException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;

public class FragmentRelationRowMapper extends EntityRowMapper<FragmentRelation> {

	private static final EntityTable TABLE = 
		new EntityTable("fragment_relation", "fragment_relation_id")
			.defColumn("from_id")
			.defColumn("to_id")
			.defColumn("priority");
	
	private FragmentRepository fragmentResolver;
	
	private FragmentRowMapper fromMapper;
	private FragmentRowMapper toMapper;
	private Map<Long, List<FragmentRelation>> resultsById;
	
	public FragmentRelationRowMapper(
		RawEntityFactory<FragmentRelation> factory, 
		FragmentRepository fragmentResolver) {
		
		super(factory);
		this.fragmentResolver = fragmentResolver;			
	}
	
	public FragmentRelationRowMapper(
		RawEntityFactory<FragmentRelation> factory,
		String prefix, 
		FragmentRowMapper fromMapper, 
		FragmentRowMapper toMapper,
		Map<Long, List<FragmentRelation>> resultsById) {
		
		super(factory, prefix);
		this.fromMapper = fromMapper;
		this.toMapper = toMapper;
		this.resultsById = resultsById;
	}
	
	@Override
	protected EntityTable getEntityTable() {
		return TABLE;
	}
	
	public static void insert(FragmentRelation relation, long from, long to, JdbcTemplate jdbcTemplate) 
	throws DuplicateException {
		Assert.Arg.notNull(relation, "relation");
		Assert.Arg.notNull(jdbcTemplate, "jdbcTemplate");
		
		if (relation.priority == null) relation.priority = 0;
		
		Object[] values = new Object[] {from, to, relation.priority};
		try {
			TABLE.insert(relation, values, jdbcTemplate);
		} 
		catch (DataIntegrityViolationException e) {
			throw new DuplicateException(e.toString(), e);
		}
	}

    public FragmentRelation mapRow(ResultSet rs, int rowNum) throws SQLException {
		FragmentRelation relation = createEntityWithCommonColumns(rs);

		if (this.fragmentResolver != null) {
			fetchFromRepository(rs, relation);
		}
		else {
			fetchFromJoinedResultSet(rs, rowNum, relation);
		}
		
        return relation;
    }
	
	private void fetchFromRepository(ResultSet rs, FragmentRelation relation) 
	throws SQLException {
		try {
			relation.from = this.fragmentResolver.get(rs.getLong(properColumn(0)));
			relation.to = this.fragmentResolver.get(rs.getLong(properColumn(1)));
		}
		catch (Exception e) {
			throw new UnhandledException(e);
		}
		relation.priority = rs.getInt(properColumn(2));
	}
	
	private void fetchFromJoinedResultSet(
		ResultSet rs, 
		int rowNum, 
		FragmentRelation relation) 
	throws SQLException {		
		if (this.fromMapper != null) {
			relation.from = (Fragment)this.fromMapper.mapRow(rs, rowNum);
			if (this.resultsById != null) {
				Long toId = rs.getLong(properColumn(1));
				CollectionUtils.pileValue(this.resultsById, toId, relation);
			}
		}
		if (this.toMapper != null) {
			relation.to = (Fragment)this.toMapper.mapRow(rs, rowNum);
			if (this.resultsById != null) {
				Long fromId = rs.getLong(properColumn(0));
				CollectionUtils.pileValue(this.resultsById, fromId, relation);
			}
		}
		relation.priority = rs.getInt(properColumn(2));
	}
}