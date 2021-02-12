package fly.mybatis;

import org.apache.ibatis.cursor.Cursor;
import org.apache.ibatis.executor.resultset.ResultSetWrapper;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.type.TypeHandler;

import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ObjectListResultSetHandler implements org.apache.ibatis.executor.resultset.ResultSetHandler {
    private final Configuration configuration;

    /**
     * 应该通过 {@code #getTypeHandler} 获取
     */
    private List<TypeHandler<?>> typeHandlers;

    public ObjectListResultSetHandler(Configuration configuration) {
        this.configuration = configuration;
    }

    private List<TypeHandler<?>> getTypeHandlers(ResultSet rs) throws SQLException {
        if (typeHandlers == null) {
            synchronized (this) {
                if (typeHandlers == null) {
                    ResultSetWrapper rsw = new ResultSetWrapper(rs, configuration);
                    List<String> columnNames = rsw.getColumnNames();
                    typeHandlers = new ArrayList<>(columnNames.size());
                    for (String columnName : columnNames) {
                        typeHandlers.add(rsw.getTypeHandler(Object.class, columnName));
                    }
                }
            }
        }
        return typeHandlers;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<List<Object>> handleResultSets(Statement stmt) throws SQLException {
        StatementContainer statementContainer = new StatementContainer(stmt);
        List<List<Object>> result = new ArrayList<>(statementContainer.resultSet.getFetchSize());
        List<Object> rowObjects;
        while ((rowObjects = statementContainer.next()) != Collections.emptyList()) {
            result.add(rowObjects);
        }
        return result;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Cursor<List<Object>> handleCursorResultSets(Statement stmt) throws SQLException {
        return new ObjectListCursor(new StatementContainer(stmt));
    }

    @Override
    public void handleOutputParameters(CallableStatement cs) throws SQLException {

    }

    static ResultSet getNextResultSet(Statement stmt) throws SQLException {
        if (stmt.getConnection().getMetaData().supportsMultipleResultSets()) {
            if (stmt.getMoreResults()) {
                return stmt.getResultSet();
            }
        }
        return null;
    }

    static List<Object> handleRowValues(ResultSet resultSet, List<TypeHandler<?>> typeHandlers) throws SQLException {
        List<Object> rowObjects = new ArrayList<>(typeHandlers.size());
        for (int i = 0; i < typeHandlers.size(); i++) {
            Object col = typeHandlers.get(i).getResult(resultSet, i + 1);
            rowObjects.add(col);
        }
        return rowObjects;
    }

    public class StatementContainer {
        final Statement stmt;
        ResultSet resultSet;

        public StatementContainer(Statement stmt) throws SQLException {
            this.stmt = stmt;
            resultSet = stmt.getResultSet();
        }

        public List<Object> next() throws SQLException {
            if (resultSet == null) {
                return Collections.emptyList();
            }
            List<TypeHandler<?>> typeHandlers = getTypeHandlers(resultSet);
            if (!resultSet.next()) {
                resultSet = getNextResultSet(stmt);
                if (resultSet != null) {
                    resultSet.next();
                    return handleRowValues(resultSet, typeHandlers);
                }
            } else {
                return handleRowValues(resultSet, typeHandlers);
            }
            return Collections.emptyList();
        }
    }
}
