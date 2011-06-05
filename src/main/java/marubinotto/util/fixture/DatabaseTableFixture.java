package marubinotto.util.fixture;

import java.sql.Connection;

import javax.sql.DataSource;

import marubinotto.util.Assert;
import marubinotto.util.RdbUtils;

import org.dbunit.dataset.ITable;

/**
 * marubinotto.util.fixture.DatabaseTableFixture
 */
public abstract class DatabaseTableFixture {

    private DataSource dataSource;

    public DatabaseTableFixture() {
    }

    public DatabaseTableFixture(DataSource dataSource) {
        setDataSource(dataSource);
    }

    public DataSource getDataSource() {
        return this.dataSource;
    }

    public void setDataSource(DataSource dataSource) {
        Assert.Arg.notNull(dataSource, "dataSource");
        this.dataSource = dataSource;
    }
	
	private Connection getJdbcConnection() {
		return RdbUtils.getSpringTransactionalConnection(this.dataSource);
	}
	
    public abstract String getTableName();

    public ITable selectAll() throws Exception {
        return RdbUtils.getTableData(getJdbcConnection(), getTableName());
    }
    
    public ITable selectWhere(String where) throws Exception {
    	return RdbUtils.query(
    		getJdbcConnection(), 
    		getTableName(), 
    		"select * from " + getTableName() + " where " + where);
    }

    public void deleteAll() throws Exception {
        RdbUtils.deleteAll(getJdbcConnection(), getTableName());
    }

    public void cleanInsert(Object[][] table) throws Exception {
        RdbUtils.cleanInsert(getJdbcConnection(), getTableName(), table);
    }

    public void cleanInsertMergedTable(Object[][] base, Object[][] diff) throws Exception {
        RdbUtils.cleanInsertMergedDataSet(getJdbcConnection(), getTableName(), base, diff);
    }

    public void insert(Object[][] table) throws Exception {
        RdbUtils.insert(getJdbcConnection(), getTableName(), table);
    }

    public void update(Object[][] table) throws Exception {
        RdbUtils.update(getJdbcConnection(), getTableName(), table);
    }

    public void shouldEqual(Object[][] expectedTable) throws Exception {
        RdbUtils.assertEquals(
            RdbUtils.createTable(getTableName(), expectedTable),
            selectAll());
    }

    public void shouldBeEmpty() throws Exception {
        RdbUtils.assertTableEmpty(getJdbcConnection(), getTableName());
    }
}
