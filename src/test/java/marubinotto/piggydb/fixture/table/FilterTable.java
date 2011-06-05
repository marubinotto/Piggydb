package marubinotto.piggydb.fixture.table;

import javax.sql.DataSource;

import marubinotto.util.fixture.DatabaseTableFixture;

public class FilterTable extends DatabaseTableFixture {

	public FilterTable(DataSource dataSource) {
        super(dataSource);
    }

    public static final String NAME = "filter";

    @Override
    public String getTableName() {
        return NAME;
    }
}
