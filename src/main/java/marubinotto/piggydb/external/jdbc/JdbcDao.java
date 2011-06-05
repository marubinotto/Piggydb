package marubinotto.piggydb.external.jdbc;

import org.springframework.jdbc.core.JdbcTemplate;

public interface JdbcDao {

	public void setJdbcTemplate(JdbcTemplate jdbcTemplate);
}
