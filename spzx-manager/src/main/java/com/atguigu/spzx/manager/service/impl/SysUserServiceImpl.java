package com.atguigu.spzx.manager.service.impl;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.atguigu.spzx.common.exception.GuiguException;
import com.atguigu.spzx.common.log.annotation.Log;
import com.atguigu.spzx.manager.mapper.SysRoleUserMapper;
import com.atguigu.spzx.manager.mapper.SysUserMapper;
import com.atguigu.spzx.manager.service.SysUserService;
import com.atguigu.spzx.model.dto.system.AssginRoleDto;
import com.atguigu.spzx.model.dto.system.LoginDto;
import com.atguigu.spzx.model.dto.system.SysUserDto;
import com.atguigu.spzx.model.entity.system.SysUser;
import com.atguigu.spzx.model.vo.common.ResultCodeEnum;
import com.atguigu.spzx.model.vo.system.LoginVo;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class SysUserServiceImpl implements SysUserService {

    @Autowired // 读取mysql数据库
    private SysUserMapper sysUserMapper;
    @Autowired // 与redis进行数据交互
    private RedisTemplate<String, String> redisTemplate;
    @Autowired
    private SysRoleUserMapper sysRoleUserMapper;

    @Override
    public LoginVo login(LoginDto loginDto) {
        // 验证码的校验过程
        // 1 获取用户提交的验证码和验证码的key 使用loginDto
        // 2 用验证码的key去redis中获取验证码的值
        // 3 比较用户提交的验证码值和redis中的验证码的值
        // 4 如果不相同 则 抛出验证码错误 提示用户 校验失败
        // 5 如果验证码正确，则删除redis中的验证码 再进行用户名密码校验过程

        // 账号密码校验过程
        // 1 获取提交过来的用户名，从LoginDto中获取到
        // 2 根据用户名查询数据库表 sys_user表 得到用户对象
        // 3 如果根据用户名查不到对应信息，则表明用户不存在  返回错误信息(登录失败)
        // 4 根据用户名查询到对应信息，则用户存在
        // 5 获取输入的密码，比较输入的密码和数据库中的密码是否一致
        // 6 如果密码不一致，则返回错误信息(登录失败)

        // 7 如果密码一致，则登录成功，生成用户唯一标识token字符串
        // 8 把登录成功用户信息放到redis中
        // 9 返回loginvo对象

        // 校验验证码阶段
        // 1 获取用户传入验证码和验证码的key
        String codeValue = loginDto.getCaptcha();
        String codeKey = loginDto.getCodeKey();
        // 2 根据验证码的key 去redis中查找出对应的验证码的值
        // 放入redis中的时候key有前缀user:validate  取得时候也需要加上前缀
        String redisCode = redisTemplate.opsForValue().get("user:validate" + codeKey);
        // 3 将验证码的真实值与用户输入的验证码进行比较
        // 4 不相同抛出异常
        if(StrUtil.isEmpty(redisCode) || !StrUtil.equalsIgnoreCase(redisCode,codeValue)){
            //throw new RuntimeException("验证码校验失败");
            throw new GuiguException(ResultCodeEnum.VALIDATECODE_ERROR);
        }
        // 5 验证码相同，删除redis中的验证码
        redisTemplate.delete("user:validate" + codeKey);


        // 校验用户名和密码阶段
        //1 获取提交过来的用户名，从LoginDto中获取到
        String userName = loginDto.getUserName();
        //2 根据用户名查询数据库表 sys_user表 得到用户对象
        SysUser sysUser = sysUserMapper.selectUserInfoByUserName(userName);
        //3如果根据用户名查不到对应信息，则表明用户不存在  返回错误信息(登录失败)
        if (sysUser == null) {
            // 抛出运行时异常
            // throw new RuntimeException("用户不存在");
            // 抛出自定义异常
            throw new GuiguException(ResultCodeEnum.LOGIN_ERROR);
        }
        // 4 根据用户名查询到对应信息，则用户存在
        //5 获取从数据库中查到的密码
        String databasePassword = sysUser.getPassword();
        //5 获取输入的密码 并加密
        String inputPassword = loginDto.getPassword();
        inputPassword = DigestUtils.md5DigestAsHex(inputPassword.getBytes());
        //5比较数据库中的密码
        if (!databasePassword.equals(inputPassword)) {
            //6如果密码不一致，则返回错误信息(登录失败)
            // throw new RuntimeException("密码错误,登录失败");
            // 抛出自定义异常
            throw new GuiguException(ResultCodeEnum.LOGIN_ERROR);
        }
        // 7 如果密码一致，则登录成功，生成用户唯一标识token字符串
        String token = UUID.randomUUID().toString().replaceAll("-", "");
        // 8 把登录成功用户信息放到redis中 key:token value:用户信息
        redisTemplate.opsForValue().set(
                "user:login" + token,
                JSON.toJSONString(sysUser),
                7,
                TimeUnit.DAYS);
        // 9 返回loginvo对象
        LoginVo loginVo = new LoginVo();
        loginVo.setToken(token);
        return loginVo;
    }

    @Override
    public SysUser getUserInfo(String token) {
        // 1 如果没有token 则不是登录状态 抛出登录异常
        // 2 如果有token 则从redis中去查询用户信息
        // 3 如果该token查询不到 则抛出登录异常
        // 4 如果查询得到用户信息 则返回用户信息

        String userJson = redisTemplate.opsForValue().get("user:login" + token);
        //1,2 可以合并成一个判断
        if(userJson.isEmpty()){
            // 3 如果该token查询不到 则抛出登录异常
            throw new GuiguException(ResultCodeEnum.LOGIN_AUTH);
        }
        // 4 如果查询得到用户信息 则返回用户信息
        SysUser sysUser = JSON.parseObject(userJson, SysUser.class);

        return sysUser;
    }

    // 用户退出登录
    @Override
    public void logout(String token) {
        // 1 删除redis中的token以及该token对应的用户信息
        // 2 跳转到首页

        // 1 删除redis中的token以及该token对应的用户信息
        redisTemplate.delete("user:login" + token);
    }

    @Override
    public PageInfo<SysUser> findByPage(Integer pageNum, Integer pageSize, SysUserDto sysUserDto) {
        PageHelper.startPage(pageNum,pageSize);
        List<SysUser> list = sysUserMapper.findByPage(sysUserDto);
        PageInfo<SysUser> sysUserPageInfo = new PageInfo<>(list);
        return sysUserPageInfo;
    }

    // 用户添加
    @Override
    public void saveSysUser(SysUser sysUser) {
        //1 判断用户名是否重复
        String userName = sysUser.getUserName(); // 获取从前端传来的用户名
        SysUser dbsysUser = sysUserMapper.selectUserInfoByUserName(userName); // 去数据库中查询是否存在该用户名的用户
        // 如果存在该用户 则抛出异常
        if(dbsysUser != null){
            // 抛出异常
            throw new GuiguException(ResultCodeEnum.USER_NAME_IS_EXISTS);
        }
        //2 对密码进行加密
        String password = sysUser.getPassword(); // 获取从前端传来的密码
        String md5Password = DigestUtils.md5DigestAsHex(password.getBytes());// 加密
        sysUser.setPassword(md5Password);// 放回用户对象中去
        // 设置数据库中status的值  数据库中定义  1表示可用 0不可用
        sysUser.setStatus(1);

        //3 保存进数据库
        sysUserMapper.save(sysUser);
    }

    @Override
    public void updateSysUser(SysUser sysUser) {
        sysUserMapper.update(sysUser);
    }

    @Override
    public void deleteById(Long userId) {
        sysUserMapper.delete(userId);
    }

    // 给用户分配角色
    @Log(title= "用户分配角色",businessType = 0)
    @Transactional  // 事务 当操作数据库时 都要考虑事务
    @Override
    public void doAssign(AssginRoleDto assginRoleDto) {
        // 1 根据userId删除用户之前分配的角色数据
        sysRoleUserMapper.deleteByUserId(assginRoleDto.getUserId());

        // todo 为了测试模拟异常
        // int a = 1/0;

        // 2 重新分配新数据
        List<Long> roleIdList = assginRoleDto.getRoleIdList();
        // 遍历得到前端传入的给当前用户分配的每个角色的id
        for(Long roleId:roleIdList){
            sysRoleUserMapper.doAssign(assginRoleDto.getUserId(), roleId);
        }
    }
}
