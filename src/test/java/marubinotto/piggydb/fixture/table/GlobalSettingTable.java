package marubinotto.piggydb.fixture.table;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import javax.sql.DataSource;

import marubinotto.util.fixture.DatabaseTableFixture;

public class GlobalSettingTable extends DatabaseTableFixture {

    public GlobalSettingTable(DataSource dataSource) {
        super(dataSource);
    }

    public static final String NAME = "global_setting";
    public static final String KEY_DATABASE_VERSION = "database.version";

    @Override
    public String getTableName() {
        return NAME;
    }
    
    public void shouldBeEmpty() throws Exception {
    	shouldEqual(new Object[][]{
            {"setting_name"},
            {KEY_DATABASE_VERSION}
        });
    }
    
    public void insertDatabaseVersion(int version) throws Exception {
    	insert(new Object[][]{
            {"setting_name", "setting_value"},
            {KEY_DATABASE_VERSION, version}
        });
    }
    
    public void databaseVersionShouldBe(int version) throws Exception {
    	String actual = (String)
    		selectWhere("setting_name = '" + KEY_DATABASE_VERSION + "'")
    			.getValue(0, "setting_value");
    	assertNotNull("Version should not be null", actual);
    	assertEquals(version, Integer.parseInt(actual));
    }
}
