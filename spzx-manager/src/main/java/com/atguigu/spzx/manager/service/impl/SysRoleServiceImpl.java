package com.atguigu.spzx.manager.service.impl;

import com.atguigu.spzx.manager.mapper.SysRoleMapper;
import com.atguigu.spzx.manager.mapper.SysRoleUserMapper;
import com.atguigu.spzx.manager.service.SysRoleService;
import com.atguigu.spzx.model.dto.system.SysRoleDto;
import com.atguigu.spzx.model.entity.system.SysRole;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SysRoleServiceImpl implements SysRoleService {
    @Autowired
    private SysRoleMapper sysRoleMapper;
    @Autowired
    private SysRoleUserMapper sysRoleUserMapper;

    // 分页查询角色方法
    @Override
    public PageInfo<SysRole> findByPage(SysRoleDto sysRoleDto, Integer current, Integer limit) {
        // 设置分页参数
        PageHelper.startPage(current,limit);
        // 根据条件查询所有数据
        // 插件的底层  是先把所有满足条件的信息都查出来  再每次返回每页的数据  是个假分页
        List<SysRole> list = sysRoleMapper.findByPage(sysRoleDto);
        // 封装pageInfo对象  对上面查出的数据进行封装分页
        PageInfo<SysRole> pageInfo = new PageInfo<>(list);
        return pageInfo;
    }

    // 角色添加方法
    @Override
    public void saveSysRole(SysRole sysRole) {
        sysRoleMapper.save(sysRole);
    }

    // 角色修改方法
    @Override
    public void updateSysRole(SysRole sysRole) {
        sysRoleMapper.update(sysRole);
    }

    @Override
    public void deleteById(Integer roleId) {
        sysRoleMapper.delete(roleId);
    }

    @Override
    public Map<String, Object> findAll(Long userId) {
        // 1 查询所有角色
        List<SysRole> roleList= sysRoleMapper.findAll();
        // 2 根据用户id查询当前用户之前分配过的角色列表(即角色id)  用于回显 和修改该用户的角色
        List<Long> roleIds = sysRoleUserMapper.selectRoleIdsByUserId(userId);

        Map<String, Object> map = new HashMap<>();
        map.put("allRoleList", roleList);
        map.put("sysUserRoles",roleIds);
        return map;
    }
}
