package zhh_fu.miaosha.DAO;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;
import zhh_fu.miaosha.pojo.User;

@Repository
@Mapper
public interface UserDAO {

    @Select("select * from user where id = #{id}")
    User getById(@Param("id") long id);
}
