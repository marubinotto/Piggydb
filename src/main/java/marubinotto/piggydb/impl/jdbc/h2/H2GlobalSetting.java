package marubinotto.piggydb.impl.jdbc.h2;

import marubinotto.piggydb.impl.jdbc.JdbcDao;
import marubinotto.piggydb.model.GlobalSetting;
import marubinotto.util.Assert;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

public class H2GlobalSetting extends GlobalSetting implements JdbcDao {
	
	protected JdbcTemplate jdbcTemplate;

    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

	public void put(String name, String value) {
		Assert.Arg.notNull(name, "name");
		
		StringBuilder update  = new StringBuilder();
		update.append("update global_setting");
		update.append(" set setting_value = ?");
		update.append(" where setting_name = ?");
		
		int updated = this.jdbcTemplate.update(
			update.toString(), new Object[]{value, name});
		if (updated > 0) {
			return;
		}
		
		String insert = "insert into global_setting" +
			" (setting_name, setting_value) values(?, ?)";
		this.jdbcTemplate.update(insert, new Object[]{name, value});
	}
	
	public String get(String name) {
		Assert.Arg.notNull(name, "name");
		
		String select = "select setting_value from global_setting" +
			" where setting_name = ?";
		try {
			return (String)this.jdbcTemplate.queryForObject(
				select, new Object[]{name}, String.class);
		}
		catch (EmptyResultDataAccessException e) {
			return null;
		}
	}
}
