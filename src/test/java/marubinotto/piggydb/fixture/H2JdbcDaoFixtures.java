package marubinotto.piggydb.fixture;

import javax.sql.DataSource;

import marubinotto.piggydb.external.jdbc.DatabaseSchema;
import marubinotto.piggydb.external.jdbc.h2.H2FilterRepository;
import marubinotto.piggydb.external.jdbc.h2.H2FragmentRepository;
import marubinotto.piggydb.external.jdbc.h2.H2GlobalSetting;
import marubinotto.piggydb.external.jdbc.h2.H2TagRepository;
import marubinotto.piggydb.fixture.table.AllTables;
import marubinotto.util.RdbUtils;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.incrementer.H2SequenceMaxValueIncrementer;

public class H2JdbcDaoFixtures {

	private DataSource dataSource;
	private JdbcTemplate jdbcTemplate;
	public AllTables tables;

	public H2JdbcDaoFixtures() throws Exception {
		setUp();
	}

	private void setUp() throws Exception {
		this.dataSource = createDataSource();
		this.jdbcTemplate = new JdbcTemplate(this.dataSource);

		DatabaseSchema schema = new DatabaseSchema();
		schema.setJdbcTemplate(this.jdbcTemplate);
		schema.update();

		this.tables = new AllTables();
		this.tables.setUp(this.dataSource);
		this.tables.cleanAll();
	}

	protected DataSource createDataSource() {
		return RdbUtils.getInMemoryDataSource(null);
	}

	public DataSource getDataSource() {
		return dataSource;
	}

	public JdbcTemplate getJdbcTemplate() {
		return this.jdbcTemplate;
	}

	//
	// JdbcDao
	//

	public H2GlobalSetting createH2GlobalSetting() {
		H2GlobalSetting globalSetting = new H2GlobalSetting();
		globalSetting.setJdbcTemplate(this.jdbcTemplate);
		return globalSetting;
	}

	public H2TagRepository createH2TagRepository() {
		H2TagRepository repository = new H2TagRepository();
		repository.setJdbcTemplate(this.jdbcTemplate);
		repository.setTagIdIncrementer(new H2SequenceMaxValueIncrementer(
			this.dataSource, "seq_tag_id"));
		return repository;
	}

	public H2FragmentRepository createH2FragmentRepository() {
		H2FragmentRepository repository = new H2FragmentRepository();
		repository.setJdbcTemplate(this.jdbcTemplate);
		repository.setFragmentIdIncrementer(new H2SequenceMaxValueIncrementer(
			this.dataSource, "seq_fragment_id"));
		repository.setRelationIdIncrementer(new H2SequenceMaxValueIncrementer(
			this.dataSource, "seq_fragment_relation_id"));
		repository.setTagRepository(createH2TagRepository());
		return repository;
	}

	public H2FilterRepository createH2FilterRepository() {
		H2FilterRepository repository = new H2FilterRepository();
		repository.setJdbcTemplate(this.jdbcTemplate);
		repository.setFilterIdIncrementer(new H2SequenceMaxValueIncrementer(
			this.dataSource, "seq_filter_id"));
		repository.setTagRepository(createH2TagRepository());
		return repository;
	}
}
