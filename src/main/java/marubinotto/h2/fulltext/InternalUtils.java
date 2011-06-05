package marubinotto.h2.fulltext;

import java.io.IOException;
import java.io.Reader;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.List;

import org.h2.command.Parser;
import org.h2.engine.Session;
import org.h2.expression.Comparison;
import org.h2.expression.ConditionAndOr;
import org.h2.expression.Expression;
import org.h2.expression.ExpressionColumn;
import org.h2.expression.ValueExpression;
import org.h2.jdbc.JdbcConnection;
import org.h2.message.DbException;
import org.h2.util.IOUtils;
import org.h2.util.New;
import org.h2.util.StringUtils;
import org.h2.value.DataType;

class InternalUtils {
	
	public static SQLException throwException(String message) throws SQLException {
        throw new SQLException(message, "FULLTEXT");
    }

	public static String getIndexPath(Connection conn) throws SQLException {
        Statement stat = conn.createStatement();
        ResultSet rs = stat.executeQuery("CALL IFNULL(DATABASE_PATH(), 'MEM:' || DATABASE())");
        rs.next();
        String path = rs.getString(1);
        rs.close();
        return path;
    }

	public static String toString(Object value, int type) throws SQLException {
        if (value == null) return "";
        switch (type) {
	        case Types.BIT:
	        case DataType.TYPE_BOOLEAN:
	        case Types.INTEGER:
	        case Types.BIGINT:
	        case Types.DECIMAL:
	        case Types.DOUBLE:
	        case Types.FLOAT:
	        case Types.NUMERIC:
	        case Types.REAL:
	        case Types.SMALLINT:
	        case Types.TINYINT:
	        case Types.DATE:
	        case Types.TIME:
	        case Types.TIMESTAMP:
	        case Types.LONGVARCHAR:
	        case Types.CHAR:
	        case Types.VARCHAR:
	            return value.toString();
	        case Types.CLOB:
	            try {
	                if (value instanceof Clob) {
	                    value = ((Clob) value).getCharacterStream();
	                }
	                return IOUtils.readStringAndClose((Reader)value, -1);
	            } 
	            catch (IOException e) {
	                throw DbException.toSQLException(e);
	            }
	        case Types.VARBINARY:
	        case Types.LONGVARBINARY:
	        case Types.BINARY:
	        case Types.JAVA_OBJECT:
	        case Types.OTHER:
	        case Types.BLOB:
	        case Types.STRUCT:
	        case Types.REF:
	        case Types.NULL:
	        case Types.ARRAY:
	        case DataType.TYPE_DATALINK:
	        case Types.DISTINCT:
	            throw throwException("Unsupported column data type: " + type);
	        default:
	            return "";
        }
    }
	
    public static String quoteSQL(Object value, int type) throws SQLException {
        if (value == null) {
            return "NULL";
        }
        switch (type) {
	        case Types.BIT:
	        case DataType.TYPE_BOOLEAN:
	        case Types.INTEGER:
	        case Types.BIGINT:
	        case Types.DECIMAL:
	        case Types.DOUBLE:
	        case Types.FLOAT:
	        case Types.NUMERIC:
	        case Types.REAL:
	        case Types.SMALLINT:
	        case Types.TINYINT:
	            return value.toString();
	        case Types.DATE:
	        case Types.TIME:
	        case Types.TIMESTAMP:
	        case Types.LONGVARCHAR:
	        case Types.CHAR:
	        case Types.VARCHAR:
	            return quoteString(value.toString());
	        case Types.VARBINARY:
	        case Types.LONGVARBINARY:
	        case Types.BINARY:
	            return "'" + StringUtils.convertBytesToString((byte[]) value) + "'";
	        case Types.CLOB:
	        case Types.JAVA_OBJECT:
	        case Types.OTHER:
	        case Types.BLOB:
	        case Types.STRUCT:
	        case Types.REF:
	        case Types.NULL:
	        case Types.ARRAY:
	        case DataType.TYPE_DATALINK:
	        case Types.DISTINCT:
	            throw throwException("Unsupported key data type: " + type);
	        default:
	            return "";
        }
    }
    
    public static String quoteString(String value) {
        if (value.indexOf('\'') < 0) {
            return "'" + value + "'";
        }
        int len = value.length();
        StringBuilder buff = new StringBuilder(len + 2);
        buff.append('\'');
        for (int i = 0; i < len; i++) {
            char ch = value.charAt(i);
            if (ch == '\'') {
                buff.append(ch);
            }
            buff.append(ch);
        }
        buff.append('\'');
        return buff.toString();
    }
    
	public static Object[][] parseConditionSqlToColumnsAndValues(String conditionSql, JdbcConnection conn) {
        List<String> columns = New.arrayList();
        List<String> values = New.arrayList();
        
        Expression expr = new Parser((Session)conn.getSession()).parseExpression(conditionSql);
        addColumnAndValue(expr, columns, values);

        return new Object[][] { 
        	columns.toArray(new Object[columns.size()]), 
        	values.toArray(new Object[columns.size()]) 
        };
    }
    
    private static void addColumnAndValue(Expression expr, List<String> columns, List<String> values) {
        if (expr instanceof ConditionAndOr) {
            ConditionAndOr and = (ConditionAndOr)expr;
            Expression left = and.getExpression(true);
            Expression right = and.getExpression(false);
            addColumnAndValue(left, columns, values);
            addColumnAndValue(right, columns, values);
        } 
        else {
            Comparison comp = (Comparison)expr;
            ExpressionColumn ec = (ExpressionColumn)comp.getExpression(true);
            ValueExpression ev = (ValueExpression)comp.getExpression(false);
            String columnName = ec.getColumnName();
            columns.add(columnName);
            if (ev == null) {
                values.add(null);
            } 
            else {
                values.add(ev.getValue(null).getString());
            }
        }
    }
}
