package com.atguigu.spzx.manager.controller;

import com.atguigu.spzx.common.log.annotation.Log;
import com.atguigu.spzx.manager.service.SysRoleService;
import com.atguigu.spzx.manager.service.SysUserService;
import com.atguigu.spzx.model.dto.system.SysRoleDto;
import com.atguigu.spzx.model.entity.system.SysRole;
import com.atguigu.spzx.model.vo.common.Result;
import com.atguigu.spzx.model.vo.common.ResultCodeEnum;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/system/sysRole")
public class SysRoleController {

    @Autowired
    private SysRoleService sysRoleService;

    // 1 角色列表的方法
    // current 表示当前页 limit表示每页记录的最大数目
    @PostMapping("/findByPage/{current}/{limit}")
    public Result findByPage(@PathVariable("current") Integer current,
                             @PathVariable("limit") Integer limit,
                             @RequestBody SysRoleDto sysRoleDto){
        // pageHelper插件 实现分页 返回的对象是PageInfo
        PageInfo<SysRole> pageInfo = sysRoleService.findByPage(sysRoleDto,current,limit);

        return Result.build(pageInfo, ResultCodeEnum.SUCCESS);
    }

    // 2 角色添加的方法
    @Log(title = "角色管理:添加",businessType = 1)
    @PostMapping(value = "/saveSysRole")
    public Result saveSysRole(@RequestBody SysRole sysRole){
        sysRoleService.saveSysRole(sysRole);
        return Result.build(null, ResultCodeEnum.SUCCESS);
    }

    // 3 角色修改的方法
    @PutMapping("/updateSysRole")
    public Result updateSysRole(@RequestBody SysRole sysRole){
        sysRoleService.updateSysRole(sysRole);
        return Result.build(null,ResultCodeEnum.SUCCESS);
    }

    // 4 角色删除的方法 （逻辑删除 本质是对有效位的修改）
    @DeleteMapping("/deleteById/{roleId}")
    public Result dedeleteById(@PathVariable("roleId") Integer roleId){
        sysRoleService.deleteById(roleId);
        return Result.build(null,ResultCodeEnum.SUCCESS);
    }

    // 查询所有角色
    @GetMapping("/findAllRoles/{userId}")
    public Result findAllRoles(@PathVariable("userId") Long userId){
        Map<String,Object> map = sysRoleService.findAll(userId);
        return Result.build(map,ResultCodeEnum.SUCCESS);
    }

}
