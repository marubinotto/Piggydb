package marubinotto.piggydb.impl;

import javax.sql.DataSource;

import marubinotto.piggydb.impl.db.DatabaseSchema;
import marubinotto.piggydb.impl.query.H2FragmentsAllButTrash;
import marubinotto.piggydb.model.FileRepository;
import marubinotto.util.RdbUtils;

import org.apache.commons.lang.UnhandledException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.incrementer.H2SequenceMaxValueIncrementer;

public class InMemoryDatabase {

	private DataSource dataSource;
	private JdbcTemplate jdbcTemplate;
	private FileRepository fileRepository;
	
	public InMemoryDatabase() {
		try {
			setUp();
		}
		catch (Exception e) {
			throw new UnhandledException(e);
		}
	}

	private void setUp() throws Exception {
		this.dataSource = RdbUtils.getInMemoryDataSource(null);
		this.jdbcTemplate = new JdbcTemplate(this.dataSource);
		this.fileRepository = new FileRepository.InMemory();

		DatabaseSchema schema = new DatabaseSchema();
		schema.setJdbcTemplate(this.jdbcTemplate);
		schema.update();
	}

	public DataSource getDataSource() {
		return this.dataSource;
	}

	public JdbcTemplate getJdbcTemplate() {
		return this.jdbcTemplate;
	}
	
	public H2GlobalSetting getGlobalSetting() {
		H2GlobalSetting globalSetting = new H2GlobalSetting();
		globalSetting.setJdbcTemplate(this.jdbcTemplate);
		return globalSetting;
	}
	
	public H2TagRepository getTagRepository() {
		H2TagRepository repository = new H2TagRepository();
		repository.setJdbcTemplate(this.jdbcTemplate);
		repository.setTagIdIncrementer(new H2SequenceMaxValueIncrementer(
			this.dataSource, "seq_tag_id"));
		return repository;
	}
	
	public H2FragmentRepository getFragmentRepository() {
		H2FragmentRepository repository = new H2FragmentRepository();
		
		repository.setJdbcTemplate(this.jdbcTemplate);
		repository.setFragmentIdIncrementer(new H2SequenceMaxValueIncrementer(
			this.dataSource, "seq_fragment_id"));
		repository.setRelationIdIncrementer(new H2SequenceMaxValueIncrementer(
			this.dataSource, "seq_fragment_relation_id"));
		repository.setTagRepository(getTagRepository());
		repository.setFileRepository(this.fileRepository);
	
		repository.registerQuery(H2FragmentsAllButTrash.class);
		
		return repository;
	}
	
	public H2FilterRepository getFilterRepository() {
		H2FilterRepository repository = new H2FilterRepository();
		repository.setJdbcTemplate(this.jdbcTemplate);
		repository.setFilterIdIncrementer(new H2SequenceMaxValueIncrementer(
			this.dataSource, "seq_filter_id"));
		repository.setTagRepository(getTagRepository());
		return repository;
	}
}
