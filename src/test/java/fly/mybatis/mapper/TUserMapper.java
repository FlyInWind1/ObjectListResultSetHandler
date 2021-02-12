package fly.mybatis.mapper;

import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.cursor.Cursor;

import java.util.List;

public interface TUserMapper {
    @Select("select * from t_user")
    List<List<Object>> selectAll();


    @Select("select * from t_user")
    Cursor<List<Object>> selectAllCursor();
}
