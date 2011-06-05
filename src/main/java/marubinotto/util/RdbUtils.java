package marubinotto.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;

import javax.sql.DataSource;


import org.apache.commons.lang.UnhandledException;
import org.dbunit.Assertion;
import org.dbunit.DatabaseUnitException;
import org.dbunit.database.DatabaseConfig;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.Column;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.DefaultDataSet;
import org.dbunit.dataset.DefaultTable;
import org.dbunit.dataset.DefaultTableMetaData;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.ITableMetaData;
import org.dbunit.dataset.datatype.DataType;
import org.dbunit.dataset.filter.DefaultColumnFilter;
import org.dbunit.dataset.xml.XmlDataSet;
import org.dbunit.dataset.xml.XmlDataSetWriter;
import org.dbunit.ext.h2.H2DataTypeFactory;
import org.dbunit.operation.DatabaseOperation;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import org.springframework.util.ClassUtils;

public class RdbUtils {

    public static final String H2_JDBC_DRIVER= "org.h2.Driver";
    public static final String H2_JDBC_URL = "jdbc:h2:mem:";
    public static final String H2_JDBC_USERNAME = "sa";
    public static final String H2_JDBC_PASSWORD = "";

    public static DataSource getInMemoryDataSource(String name) {
    	try {
			Class.forName(H2_JDBC_DRIVER, true, ClassUtils.getDefaultClassLoader());
		}
		catch (ClassNotFoundException e) {
			throw new UnhandledException(e);
		}
    	
        String url = H2_JDBC_URL;
        if (name != null) {
            url = url + name;
        }
        String username = H2_JDBC_USERNAME;
        String password = H2_JDBC_PASSWORD;

        return new SingleConnectionDataSource(url, username, password, true);
    }
    
    // Create DataSet

    public static IDataSet createDataSet(String tableName, Object[][] data)
    throws DataSetException {
        return new DefaultDataSet(createTable(tableName, data));
    }

    public static ITable createTable(String tableName, Object[][] data)
    throws DataSetException {
        if (data.length == 0) {
            throw new DataSetException("data is empty.");
        }

        ITableMetaData metaData = getMetaDataFromHeader(tableName, data[0]);
        DefaultTable table = new DefaultTable(metaData);
        for (int i = 1; i < data.length; i++) {
            table.addRow(data[i]);
        }
        return table;
    }

    private static ITableMetaData getMetaDataFromHeader(String tableName, Object[] header)
    throws DataSetException {
        if (header.length == 0) {
            throw new DataSetException("header is empty.");
        }
        Column[] columns = new Column[header.length];
        for (int i = 0; i < header.length; i++) {
            columns[i] = new Column(header[i].toString(), DataType.UNKNOWN);
        }
        return new DefaultTableMetaData(tableName, columns);
    }
    
    
    // Database connection
    
    public static Connection getSpringTransactionalConnection(DataSource dataSource) {
    	return DataSourceUtils.getConnection(dataSource);
    }
    
    private static IDatabaseConnection setUpConnection(Connection jdbcConnection) 
    throws SQLException {
    	IDatabaseConnection connection = new DatabaseConnection(jdbcConnection);
    	connection.getConfig().setProperty(
        	DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new H2DataTypeFactory());
    	return connection;
    }
    
    private static final String EXPORT_ENCODING = "UTF-8";
    
    public static void exportAllAsXml(Connection jdbcConnection, OutputStream output) 
    throws SQLException, DataSetException, IOException {
    	exportAsXml(jdbcConnection, null, output);
    }
    
    public static void exportAsXml(Connection jdbcConnection, String[] tableNames, OutputStream output) 
    throws SQLException, DataSetException, IOException {
    	Assert.Arg.notNull(jdbcConnection, "jdbcConnection");
    	Assert.Arg.notNull(output, "output");
    	
    	IDatabaseConnection connection = setUpConnection(jdbcConnection);
    	IDataSet dataSet = null;
    	if (tableNames != null) {
    		dataSet = connection.createDataSet(tableNames);
    	}
    	else {
    		dataSet = connection.createDataSet();
    	}
    	
    	Writer writer = new OutputStreamWriter(output, EXPORT_ENCODING);
		XmlDataSetWriter xmlWriter = new XmlDataSetWriter(writer, EXPORT_ENCODING);
		xmlWriter.write(dataSet);
    }
    
    public static void cleanImportXml(Connection jdbcConnection, InputStream input) 
    throws SQLException, IOException, DatabaseUnitException {
    	Assert.Arg.notNull(jdbcConnection, "jdbcConnection");
    	Assert.Arg.notNull(input, "input");
    	
    	XmlDataSet dataSet = new XmlDataSet(input);    	
    	IDatabaseConnection connection = setUpConnection(jdbcConnection);
    	DatabaseOperation.CLEAN_INSERT.execute(connection, dataSet);
    }

    public static void deleteAll(Connection jdbcConnection, String tableName)
    throws DatabaseUnitException, SQLException {
        IDatabaseConnection connection = setUpConnection(jdbcConnection);
        IDataSet dataSet = new DefaultDataSet(new DefaultTable(tableName));
        DatabaseOperation.DELETE_ALL.execute(connection, dataSet);
    }

    public static void cleanInsert(Connection jdbcConnection, String tableName, Object[][] data)
    throws DatabaseUnitException, SQLException {
        IDatabaseConnection connection = setUpConnection(jdbcConnection);
        IDataSet dataSet = createDataSet(tableName, data);
        DatabaseOperation.CLEAN_INSERT.execute(connection, dataSet);
    }

    public static void update(Connection jdbcConnection, String tableName, Object[][] data)
    throws DatabaseUnitException, SQLException {
        IDatabaseConnection connection = setUpConnection(jdbcConnection);
        IDataSet dataSet = createDataSet(tableName, data);
        DatabaseOperation.UPDATE.execute(connection, dataSet);
    }

    public static void cleanInsertMergedDataSet(
    	Connection jdbcConnection,
        String table,
        Object[][] base,
        Object[][] diff)
    throws Exception {
        if (diff != null) {
            RdbUtils.cleanInsert(jdbcConnection, table, RdbUtils.merge(base, diff));
        }
        else {
            RdbUtils.cleanInsert(jdbcConnection, table, base);
        }
    }

    public static void insert(Connection jdbcConnection, String tableName, Object[][] data)
    throws DatabaseUnitException, SQLException {
        IDatabaseConnection connection = setUpConnection(jdbcConnection);
        IDataSet dataSet = createDataSet(tableName, data);
        DatabaseOperation.INSERT.execute(connection, dataSet);
    }

    public static ITable getTableData(Connection jdbcConnection, String tableName)
    throws SQLException, DataSetException {
        IDatabaseConnection connection = setUpConnection(jdbcConnection);
        IDataSet databaseDataSet = connection.createDataSet();
        return databaseDataSet.getTable(tableName);
    }

    public static ITable query(Connection jdbcConnection, String resultName, String sql)
    throws SQLException, DataSetException {
        IDatabaseConnection connection = setUpConnection(jdbcConnection);
        return connection.createQueryTable(resultName, sql);
    }

    public static void assertTableEmpty(Connection jdbcConnection, String tableName)
    throws SQLException, DataSetException {
        ITable table = getTableData(jdbcConnection, tableName);
        junit.framework.Assert.assertEquals(
            "Table <" + tableName + "> should be empty.", 0, table.getRowCount());
    }

    public static void assertEquals(ITable expectedTable, ITable actualTable)
    throws Exception {
        Assertion.assertEquals(
            expectedTable,
            DefaultColumnFilter.includedColumnsTable(
                actualTable,
                expectedTable.getTableMetaData().getColumns()));
    }

    @SuppressWarnings("unchecked")
	public static Object[][] merge(Object[][] base, Object[][] diff) {
        Assert.Arg.notNull(base, "base");
        Assert.Arg.notNull(diff, "diff");

        if (base.length == 0) {
            return diff;
        }
        if (diff.length == 0) {
            return base;
        }

        List baseHeader = Arrays.asList(base[0]);
        Object[] diffHeader = diff[0];

        LinkedHashSet mergedHeader = new LinkedHashSet();
        mergedHeader.addAll(baseHeader);
        mergedHeader.addAll(Arrays.asList(diffHeader));
        int resultFieldCount = mergedHeader.size();

        int resultRowCount = Math.max(base.length, diff.length);
        Object[][] result = new String[resultRowCount][0];

        for (int rowIndex = 0; rowIndex < resultRowCount; rowIndex++) {
        	Object[] mergedRow = new String[resultFieldCount];
            if (rowIndex < diff.length) {
                if (rowIndex < base.length) {
                    System.arraycopy(base[rowIndex], 0, mergedRow, 0, base[rowIndex].length);
                }

                // Decide to add or replace for each diff field values
                int addedFeildCounter = 0;
                for (int fieldIndex = 0; fieldIndex < diff[rowIndex].length; fieldIndex++) {
                    Object fieldName = diffHeader[fieldIndex];
                    Object value = diff[rowIndex][fieldIndex];
                    if (baseHeader.contains(fieldName)) {
                        int fieldIndexToOverwrite = baseHeader.indexOf(fieldName);
                        mergedRow[fieldIndexToOverwrite] = value;
                    }
                    else {
                        mergedRow[baseHeader.size() + addedFeildCounter] = value;
                        addedFeildCounter++;
                    }
                }
            }
            else if (rowIndex < base.length) {
                System.arraycopy(base[rowIndex], 0, mergedRow, 0, base[rowIndex].length);
            }
            result[rowIndex] = mergedRow;
        }
        return result;
    }
}
