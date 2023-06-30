package com.lagou.user.dao;

import com.lagou.user.pojo.User;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
import tk.mybatis.mapper.common.Mapper;

public interface UserMapper extends Mapper<User> {

    /***
     * 增加用户积分
     * @param username
     * @param point
     * @return
     */
    @Update("UPDATE tb_user SET points=points+#{point} WHERE username=#{username}")
    int addUserPoints(@Param("username") String username, @Param("point") Integer point);
}
