package marubinotto.piggydb.fixture.table;

import javax.sql.DataSource;

import marubinotto.util.fixture.DatabaseTableFixture;

public class TaggingTable extends DatabaseTableFixture {

	public TaggingTable(DataSource dataSource) {
        super(dataSource);
    }

    public static final String NAME = "tagging";
    
    public static final byte TARGET_TYPE_TAG = 1;
    public static final byte TARGET_TYPE_FRAGMENT = 2;
    public static final byte TARGET_TYPE_FILTER_CLASSIFICATION = 3;
    public static final byte TARGET_TYPE_FILTER_EXCLUDES = 4;

    @Override
    public String getTableName() {
        return NAME;
    }
}
