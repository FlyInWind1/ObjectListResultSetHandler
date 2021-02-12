package fly.mybatis;

import org.apache.ibatis.executor.resultset.DefaultResultSetHandler;
import org.apache.ibatis.executor.resultset.ResultSetHandler;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ResultMap;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.session.Configuration;

import java.lang.reflect.Field;
import java.sql.Statement;
import java.util.List;

/**
 * 结果集拦截
 *
 * @author fly
 * @date 2021/02/09
 */
@Intercepts({
        @Signature(type = ResultSetHandler.class, method = "handleResultSets", args = Statement.class),
        @Signature(type = ResultSetHandler.class, method = "handleCursorResultSets", args = Statement.class)
})
public class ResultSetInterceptor implements Interceptor {
    private static final Field MAPPED_STATEMENT_FIELD;

    private Configuration configuration;

    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }

    public ResultSetInterceptor() {
    }

    public ResultSetInterceptor(Configuration configuration) {
        this.configuration = configuration;
    }

    static {
        Field mappedStatementField = null;
        try {
            mappedStatementField = DefaultResultSetHandler.class.getDeclaredField("mappedStatement");
            mappedStatementField.setAccessible(true);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        MAPPED_STATEMENT_FIELD = mappedStatementField;
    }

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        Object target = invocation.getTarget();
        if (target instanceof DefaultResultSetHandler) {
            DefaultResultSetHandler handler = (DefaultResultSetHandler) target;
            MappedStatement mappedStatement = (MappedStatement) ResultSetInterceptor.MAPPED_STATEMENT_FIELD.get(handler);
            List<ResultMap> resultMaps = mappedStatement.getResultMaps();
            if (resultMaps.size() == 1 && resultMaps.get(0).getType() == List.class) {
                ObjectListResultSetHandler objectListResultSetHandler = new ObjectListResultSetHandler(configuration);
                return invocation.getMethod().invoke(objectListResultSetHandler, invocation.getArgs());
            }
        }
        return invocation.proceed();
    }
}
