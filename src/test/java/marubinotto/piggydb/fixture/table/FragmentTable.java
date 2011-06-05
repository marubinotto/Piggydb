package marubinotto.piggydb.fixture.table;

import javax.sql.DataSource;

import marubinotto.util.fixture.DatabaseTableFixture;

public class FragmentTable extends DatabaseTableFixture {

	public FragmentTable(DataSource dataSource) {
        super(dataSource);
    }

    public static final String NAME = "fragment";

    @Override
    public String getTableName() {
        return NAME;
    }
}
