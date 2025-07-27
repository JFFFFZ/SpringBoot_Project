package com.atguigu.spzx.user.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.spzx.common.exception.GuiguException;
import com.atguigu.spzx.model.dto.h5.UserLoginDto;
import com.atguigu.spzx.model.dto.h5.UserRegisterDto;
import com.atguigu.spzx.model.entity.user.UserInfo;
import com.atguigu.spzx.model.vo.common.ResultCodeEnum;
import com.atguigu.spzx.model.vo.h5.UserInfoVo;
import com.atguigu.spzx.user.mapper.UserInfoMapper;
import com.atguigu.spzx.user.service.UserInfoService;
import com.atguigu.spzx.utils.AuthContextUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class UserInfoServiceImpl implements UserInfoService {
    @Autowired
    private UserInfoMapper userInfoMapper;
    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Override
    public void register(UserRegisterDto userRegisterDto) {
        // 1 从userRegisterDto中获取数据  前端传入的数据
        String username = userRegisterDto.getUsername();
        String password = userRegisterDto.getPassword();
        password = DigestUtils.md5DigestAsHex(password.getBytes()); // 密码加密
        String nickName = userRegisterDto.getNickName();
        String inputCode = userRegisterDto.getCode();
        // 2 验证码校验
        // 2.1 从redis中获取验证码
        String redisCode = redisTemplate.opsForValue().get(username);
        // 2.2 获取输入的验证码 并比较验证码
        if(redisCode==null || redisCode!=null &&!redisCode.equals(inputCode)){ //
            // 验证码校验失败
            throw new GuiguException(ResultCodeEnum.VALIDATECODE_ERROR);
        } // 验证码校验通过
        // 3 校验用户名 查询数据库 不能重复
        UserInfo userInfo = userInfoMapper.selectByUserName(username);
        if (userInfo != null){
            throw new GuiguException(ResultCodeEnum.USER_NAME_IS_EXISTS);
        } // 用户名校验通过
        // 4 密码加密
        // 5 封装数据 保存至数据库中 调用mapper
        UserInfo user = new UserInfo();
        user.setUsername(username);
        user.setPassword(password);
        user.setNickName(nickName);
        user.setPhone(username);
        user.setStatus(1);
        user.setSex(0);
        user.setAvatar("http://thirdwx.qlogo.cn/mmopen/vi_32/DYAIOgq83eoj0hHXhgJNOTSOFsS4uZs8x1ConecaVOB8eIl115xmJZcT4oCicvia7wMEufibKtTLqiaJeanU2Lpg3w/132");
        userInfoMapper.save(user);

        // 6 注册成功  删除redis中的验证码
        redisTemplate.delete(username);
    }

    @Override
    public String login(UserLoginDto userLoginDto) {
        // 1 从Dto中 获取用户名和密码
        String username = userLoginDto.getUsername();
        String inputPassword = userLoginDto.getPassword();
        // 2 根据用户名查询数据库 获得用户信息
        UserInfo userInfo = userInfoMapper.selectByUserName(username);
        if(userInfo==null){
            throw new GuiguException(ResultCodeEnum.LOGIN_ERROR);
        }
        // 3 校验密码 加密比较 若密码不一致 抛出异常
        String sqlPassword = userInfo.getPassword();
        String md5Pwd = DigestUtils.md5DigestAsHex(inputPassword.getBytes());
        if(sqlPassword==null || sqlPassword!=null &&!sqlPassword.equals(md5Pwd)){
            throw new GuiguException(ResultCodeEnum.LOGIN_ERROR);
        }
        // 4 密码一致 校验是否禁止登录  不能登录则抛出异常
        if(userInfo.getStatus() == 0){
            throw new GuiguException(ResultCodeEnum.ACCOUNT_STOP);
        }// 正常状态则
        // 登录成功 生成token
        String token = UUID.randomUUID().toString().replaceAll("-", "");
        // 5 将用户信息 保存在redis中并返回token
        redisTemplate.opsForValue().set("user:spzx" + token,
                JSON.toJSONString(userInfo),
                1, TimeUnit.HOURS);
        return token;
    }

    //获取当前登录用户信息
    @Override
    public UserInfoVo getCurrentUserInfo(String token) {
//        // 1 根据token 从redis中获取用户信息
//        String key = "user:spzx" + token;
//        String userInfoJson = redisTemplate.opsForValue().get(key);
//        // 2 将JSON字符串转换成UserInfo类的对象
////        if(StringUtils.isEmpty(userInfoJson)){
////            throw new GuiguException(ResultCodeEnum.LOGIN_AUTH);
////        }
//        UserInfo userInfo = JSON.parseObject(userInfoJson, UserInfo.class);

        // 1 根据token 从threadLocal中获取用户信息  可以和上面替换
        UserInfo userInfo = AuthContextUtil.getUserInfo();

        // 3 将UserInfo对象封装成 成UserInfoVo对象
        UserInfoVo userInfoVo = new UserInfoVo();
//        userInfoVo.setNickName(userInfo.getNickName());
//        userInfoVo.setAvatar(userInfo.getAvatar());
        BeanUtils.copyProperties(userInfo,userInfoVo);
        return userInfoVo;
    }
}
