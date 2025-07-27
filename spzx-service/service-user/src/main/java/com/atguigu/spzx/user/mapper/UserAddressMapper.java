package com.atguigu.spzx.user.mapper;

import com.atguigu.spzx.model.entity.user.UserAddress;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface UserAddressMapper {
    // 通过用户di 获取用户的地址列表
    List<UserAddress> findUserAddressListByUserId(Long userId);

    UserAddress getUserAddressById(Long id);
}
