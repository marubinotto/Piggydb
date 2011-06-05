package marubinotto.util.rdb;

import static org.junit.Assert.*;

import marubinotto.util.RdbUtils;

import org.apache.commons.lang.ArrayUtils;
import org.junit.Test;

/**
 * @see RdbUtils#merge(String[][], String[][])
 */
public class MergeDataSetTest {

	@Test
    public void mergeEmptyDataSet() throws Exception {
		ensureMergedDataSetIsAsExpected(
			RdbUtils.merge(
	            new Object[][]{
	                {"id", "name"},
	                {"1", "Daisuke"}
	            },
	            new String[0][0]),
            new Object[][]{
                {"id", "name"},
                {"1", "Daisuke"}
            }
        );
    }

	@Test
    public void addField() throws Exception {
		ensureMergedDataSetIsAsExpected(
			RdbUtils.merge(
	            new Object[][]{
	                {"id", "name"},
	                {"1", "Daisuke"}
	            },
	            new Object[][]{
	                {"message"},
	                {"Hello!"}
	            }),
            new Object[][]{
                {"id", "name", "message"},
                {"1", "Daisuke", "Hello!"}
            }
        );
    }

	@Test
    public void mergeToEmpty() throws Exception {
		ensureMergedDataSetIsAsExpected(
			RdbUtils.merge(
	            new Object[0][0],
	            new Object[][]{
	                {"id", "name"},
	                {"1", "Daisuke"}
	            }),
            new Object[][]{
                {"id", "name"},
                {"1", "Daisuke"}
            }
        );
    }

	@Test
    public void modifyField() throws Exception {
		ensureMergedDataSetIsAsExpected(
			RdbUtils.merge(
	            new Object[][]{
	                {"id", "name"},
	                {"1", "Daisuke"}
	            },
	            new Object[][]{
	                {"name"},
	                {"Akane"}
	            }),
            new Object[][]{
                {"id", "name"},
                {"1", "Akane"}
            }
        );
    }

	@Test
    public void addAndModifyField() throws Exception {
		ensureMergedDataSetIsAsExpected(
			RdbUtils.merge(
	            new Object[][]{
	                {"id", "name"},
	                {"1", "Daisuke"}
	            },
	            new Object[][]{
	                {"name", "message"},
	                {"Akane", "I'm not a pig!"}
	            }),
            new Object[][]{
                {"id", "name", "message"},
                {"1", "Akane", "I'm not a pig!"}
            }
        );
    }

	@Test
    public void addRow() throws Exception {
		ensureMergedDataSetIsAsExpected(
			RdbUtils.merge(
	            new Object[][]{
	                {"id", "name"},
	                {"1", "Daisuke"}
	            },
	            new Object[][]{
	                {"id", "name"},
	                {},	// placeholder for the first record
	                {"2", "Akane"}
	            }),
            new Object[][]{
                {"id", "name"},
                {"1", "Daisuke"},
                {"2", "Akane"}
            }
        );
    }

	@Test
    public void addRowMissingField() throws Exception {
		ensureMergedDataSetIsAsExpected(
			RdbUtils.merge(
	            new Object[][]{
	                {"id", "name", "message"},
	                {"1", "Daisuke", "Hello!"}
	            },
	            new Object[][]{
	                {"id", "name"},
	                {},	// placeholder for the first record
	                {"2", "Akane"}
	            }),
            new Object[][]{
                {"id", "name", "message"},
                {"1", "Daisuke", "Hello!"},
                {"2", "Akane", null}
            }
        );
    }

	@Test
    public void addRowWithAdditionalField() throws Exception {
		ensureMergedDataSetIsAsExpected(
			RdbUtils.merge(
	            new Object[][]{
	                {"id", "name"},
	                {"1", "Daisuke"}
	            },
	            new Object[][]{
	                {"id", "name", "message"},
	                {},	// placeholder for the first record
	                {"2", "Akane", "I'm not a pig!"}
	            }),
            new Object[][]{
                {"id", "name", "message"},
                {"1", "Daisuke", null},
                {"2", "Akane", "I'm not a pig!"}
            }
        );
    }

	@Test
    public void modifyFieldOfTwoRows() throws Exception {
		ensureMergedDataSetIsAsExpected(
			RdbUtils.merge(
	            new Object[][]{
	                {"id", "name"},
	                {"1", "Daisuke"},
	                {"2", "Akane"}
	            },
	            new Object[][]{
	                {"name"},
	                {"Amy"},
	                {"Doug"}
	            }),
            new Object[][]{
                {"id", "name"},
                {"1", "Amy"},
                {"2", "Doug"}
            }
        );
    }

// Private

    private void ensureMergedDataSetIsAsExpected(
    	Object[][] actual, 
    	Object[][] expected)
    throws Exception {
        assertEquals(
            ArrayUtils.toString(expected),
            ArrayUtils.toString(actual));
    }
}
