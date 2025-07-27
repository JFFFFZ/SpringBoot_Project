package com.atguigu.spzx.user.service.impl;

import com.atguigu.spzx.model.entity.user.UserAddress;
import com.atguigu.spzx.model.entity.user.UserInfo;
import com.atguigu.spzx.user.mapper.UserAddressMapper;
import com.atguigu.spzx.user.service.UserAddressService;
import com.atguigu.spzx.utils.AuthContextUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserAddressServiceImpl implements UserAddressService {
    @Autowired
    private UserAddressMapper userAddressMapper ;

    // 获取用户的地址列表
    @Override
    public List<UserAddress> findUserAddressList() {
        UserInfo userInfo = AuthContextUtil.getUserInfo();
        if(userInfo!=null){
            Long userId = userInfo.getId();
            return userAddressMapper.findUserAddressListByUserId(userId);
        }
        return new ArrayList<>();
    }

    @Override
    public UserAddress getUserAddressById(Long id) {
        return userAddressMapper.getUserAddressById(id);
    }
}
