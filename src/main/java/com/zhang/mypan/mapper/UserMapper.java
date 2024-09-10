package com.zhang.mypan.mapper;

import com.zhang.mypan.entity.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * @author 86180
 * @description 针对表【user_info】的数据库操作Mapper
 * @createDate 2024-03-16 20:39:37
 * @Entity com.zhang.mypan.entity.User
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {


    Integer setUserSpace(@Param("userId") String userId, @Param("useSpace") Long useSpace, @Param("totalSpace") Long totalSpace);
}




