package marubinotto.piggydb.impl.jdbc;

import org.springframework.jdbc.core.JdbcTemplate;

public interface JdbcDao {

	public void setJdbcTemplate(JdbcTemplate jdbcTemplate);
}
