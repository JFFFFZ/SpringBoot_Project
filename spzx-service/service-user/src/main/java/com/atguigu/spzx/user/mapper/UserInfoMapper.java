package com.atguigu.spzx.user.mapper;

import com.atguigu.spzx.model.entity.user.UserInfo;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserInfoMapper {
    // 根据用户名查询用户信息
    UserInfo selectByUserName(String username);

    void save(UserInfo user);
}
