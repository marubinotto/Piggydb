package marubinotto.util.rdb;

import static org.junit.Assert.assertEquals;

import marubinotto.util.RdbUtils;

import org.dbunit.dataset.Column;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.ITableMetaData;
import org.dbunit.dataset.datatype.DataType;
import org.junit.Test;

/**
 * My original implementation to convert from a string array into IDataSet.
 * 
 * @see RdbUtils
 */
public class CreateDataSetTest {

	@Test(expected=DataSetException.class)
    public void createDataSetWithEmptyArray() throws Exception {
		RdbUtils.createDataSet("table", new String[][]{});
    }

	@Test(expected=DataSetException.class)
    public void createDataSetWithEmptyHeader() throws Exception {
		RdbUtils.createDataSet("table", new String[][]{{}});
     }

	@Test
    public void createDataSetOnlyWithHeader() throws Exception {
		// When
        IDataSet dataSet = RdbUtils.createDataSet("table", new String[][]{{"id", "name"}});

        // Then
        ITable table = dataSet.getTable("table");
        assertEquals(0, table.getRowCount());

        ITableMetaData metaData = table.getTableMetaData();
        assertEquals("table", metaData.getTableName());

        Column[] columns = metaData.getColumns();
        assertEquals(2, columns.length);
        
        assertEquals("id", columns[0].getColumnName());
        assertEquals(DataType.UNKNOWN, columns[0].getDataType());
        
        assertEquals("name", columns[1].getColumnName());
        assertEquals(DataType.UNKNOWN, columns[1].getDataType());
    }

	@Test
    public void createDataSetWithOneRow() throws Exception {
		// When
        IDataSet dataSet = RdbUtils.createDataSet(
            "table",
            new String[][]{
                {"id", "name"},
                {"1", "Daisuke"}
            });

        // Then
        ITable table = dataSet.getTable("table");
        assertEquals(1, table.getRowCount());
        
        assertEquals("1", (String)table.getValue(0, "id"));
        assertEquals("Daisuke", (String)table.getValue(0, "name"));
    }

	@Test
    public void createDataSetWithTwoRows() throws Exception {
		// When
        IDataSet dataSet = RdbUtils.createDataSet(
            "table",
            new String[][]{
                {"id", "name"},
                {"1", "Daisuke"},
                {"2", "Akane"}
            });

        // Then
        ITable table = dataSet.getTable("table");
        assertEquals(2, table.getRowCount());
        
        assertEquals("1", (String)table.getValue(0, "id"));
        assertEquals("Daisuke", (String)table.getValue(0, "name"));
        
        assertEquals("2", (String)table.getValue(1, "id"));
        assertEquals("Akane", (String)table.getValue(1, "name"));
    }
}
