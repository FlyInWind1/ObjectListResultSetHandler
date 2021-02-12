package fly.mybatis;

import fly.mybatis.mapper.TUserMapper;
import org.apache.ibatis.cursor.Cursor;
import org.apache.ibatis.datasource.pooled.PooledDataSource;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.managed.ManagedTransactionFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.sql.SQLException;
import java.util.List;

class ObjectListResultSetHandlerTest {
    static TUserMapper userMapper;

    @BeforeAll
    static void beforeAll() throws SQLException, IOException {
        //datasource
        PooledDataSource dataSource = new PooledDataSource("org.h2.Driver", "jdbc:h2:mem:test", null, null);
        //init database
        InputStream inputStream = ObjectListResultSetHandlerTest.class.getClassLoader().getResourceAsStream("init.sql");
        assert inputStream != null;
        ScriptRunner scriptRunner = new ScriptRunner(dataSource.getConnection());
        scriptRunner.runScript(new StringReader(new String(inputStream.readAllBytes())));
        //config mybatis
        Configuration configuration = new Configuration();
        Environment environment = new Environment
                .Builder("test-env")
                .dataSource(dataSource)
                .transactionFactory(new ManagedTransactionFactory())
                .build();
        configuration.setEnvironment(environment);
        configuration.getMapperRegistry().addMappers("fly.mybatis.mapper");
        configuration.addInterceptor(new ResultSetInterceptor(configuration));
        SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder()
                .build(configuration);
        userMapper = configuration.getMapper(TUserMapper.class, sqlSessionFactory.openSession());
    }

    @Test
    void test1() {
        //selectAll
        List<List<Object>> lists = userMapper.selectAll();
        System.out.println(lists);
    }

    @Test
    void test2() {
        //selectAll
        Cursor<List<Object>> cursor = userMapper.selectAllCursor();
        cursor.forEach(System.out::println);
    }
}
