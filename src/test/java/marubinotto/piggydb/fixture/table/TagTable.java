package marubinotto.piggydb.fixture.table;

import javax.sql.DataSource;

import marubinotto.util.fixture.DatabaseTableFixture;

public class TagTable extends DatabaseTableFixture {

	public TagTable(DataSource dataSource) {
        super(dataSource);
    }

    public static final String NAME = "tag";

    @Override
    public String getTableName() {
        return NAME;
    }
}
