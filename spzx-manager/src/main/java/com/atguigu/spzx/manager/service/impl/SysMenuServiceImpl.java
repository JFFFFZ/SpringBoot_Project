package com.atguigu.spzx.manager.service.impl;

import com.atguigu.spzx.common.exception.GuiguException;
import com.atguigu.spzx.manager.mapper.SysMenuMapper;
import com.atguigu.spzx.manager.mapper.SysRoleMenuMapper;
import com.atguigu.spzx.manager.service.SysMenuService;
import com.atguigu.spzx.manager.utils.MenuHelper;
import com.atguigu.spzx.model.entity.system.SysMenu;
import com.atguigu.spzx.model.entity.system.SysUser;
import com.atguigu.spzx.model.vo.common.ResultCodeEnum;
import com.atguigu.spzx.model.vo.system.SysMenuVo;
import com.atguigu.spzx.utils.AuthContextUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.LinkedList;
import java.util.List;


@Service
public class SysMenuServiceImpl implements SysMenuService {

    @Autowired
    private SysMenuMapper sysMenuMapper;
    @Autowired
    private SysRoleMenuMapper sysRoleMenuMapper;

    @Override
    public List<SysMenu> findNodes() {
        // 1 查询所有的菜单 返回一个list集合 但是无法给前端直接使用
        List<SysMenu> sysMenuList = sysMenuMapper.findAll();
        if(CollectionUtils.isEmpty(sysMenuList)){
            return null;
        }
        // 2 调用工具类中的方法 把返回的list集合封装成要求的数据格式  例如树形数据格式
        List<SysMenu> treeList = MenuHelper.buildTree(sysMenuList);

        return treeList;
    }
// 添加菜单
    @Override
    public void save(SysMenu sysMenu) {
        sysMenuMapper.save(sysMenu);
        // 当新添加子菜单时，把父菜单的isHalf设置成半开状态（即 isHalf =1 ）
        updateSysRoleMenu(sysMenu);
    }
// 当新添加子菜单时，把父菜单的isHalf设置成半开状态（即 isHalf =1 ）
    private void updateSysRoleMenu(SysMenu sysMenu) {
        // 获取当前添加菜单的父菜单的id  （树形结构  可能有多层 需递归）
        SysMenu parentMenu = sysMenuMapper.selectParentMenu(sysMenu.getParentId());
        if(parentMenu!=null){
            // 设置父菜单的isHalf为1
            sysRoleMenuMapper.updateSysRoleMenuIsHalf(parentMenu.getId());
            // 递归调用
            updateSysRoleMenu(parentMenu);
        }
    }

    @Override
    public void update(SysMenu sysMenu) {
        sysMenuMapper.update(sysMenu);
    }

    @Override
    public void removeById(Long id) {
        // 根据当前菜单id，查询是否包含子菜单
        int count = sysMenuMapper.selectCountById(id);
        // 如果count=0  即不包含子菜单 则直接删除
        if(count == 0){
            sysMenuMapper.delete(id);
        }else{ // 包含子菜单 则不能删除 抛出异常（不推荐直接删除下面的结点  因为可能是误操作）
            throw new GuiguException(ResultCodeEnum.NODE_ERROR);
        }
    }

    // 查询用户可以操作菜单
    @Override
    public List<SysMenuVo> findMenusByUserId() {
        // 1 获取当前登录的用户id
        // 2 根据用户id查询用户可以操作的菜单列表
        // 3 将查询到的结果封装成要求的数据格式，并返回

        // 登录时就有用户id
        SysUser sysUser = AuthContextUtil.get();
        Long userId = sysUser.getId();

        // 根据id查询用户可以操作的菜单列表
        List<SysMenu> sysMenuList = sysMenuMapper.findSysMenuByUserId(userId);

        // 封装成要求的数据格式
        List<SysMenu> sysMenuTreeList = MenuHelper.buildTree(sysMenuList);
        List<SysMenuVo> sysMenuVos = this.buildMenus(sysMenuTreeList);
        return sysMenuVos;
    }
    // 将List<SysMenu>对象转换成List<SysMenuVo>对象
    private List<SysMenuVo> buildMenus(List<SysMenu> menus) {

        List<SysMenuVo> sysMenuVoList = new LinkedList<SysMenuVo>();
        for (SysMenu sysMenu : menus) {
            SysMenuVo sysMenuVo = new SysMenuVo();
            sysMenuVo.setTitle(sysMenu.getTitle());
            sysMenuVo.setName(sysMenu.getComponent());
            List<SysMenu> children = sysMenu.getChildren();
            // 递归
            if (!CollectionUtils.isEmpty(children)) {
                sysMenuVo.setChildren(buildMenus(children));
            }
            sysMenuVoList.add(sysMenuVo);
        }
        return sysMenuVoList;
    }
}
