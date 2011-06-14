package marubinotto.piggydb.fixture.table;

import javax.sql.DataSource;

import marubinotto.util.fixture.DatabaseTableFixture;

public class FragmentRelationTable extends DatabaseTableFixture {

	public FragmentRelationTable(DataSource dataSource) {
		super(dataSource);
	}

	public static final String NAME = "fragment_relation";

	@Override
	public String getTableName() {
		return NAME;
	}
}
