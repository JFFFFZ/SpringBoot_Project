package com.atguigu.spzx.manager.utils;

import com.atguigu.spzx.model.entity.system.SysMenu;

import java.util.ArrayList;
import java.util.List;

public class MenuHelper {
    public static List<SysMenu> buildTree(List<SysMenu> sysMenuList) {
        //TODO 完成封装的过程 sysMenuList 所有菜单的集合
        // 创建list集合 用于封装最终的数据
        List<SysMenu> trees = new ArrayList<>();
        // 遍历传入的集合（所有菜单的集合）
        for(SysMenu sysMenu : sysMenuList){
            // 递归入口 一级菜单 parent_id= 0
            if(sysMenu.getParentId() == 0){
                // 根据第一层找下层数据 使用递归完成
                // 写方法实现找下层过程， 方法里面传递两个参数：
                // 第一个参数 当前第一层菜单 第二个参数 所有的菜单的集合（用于往下挖）
                trees.add(findChildren(sysMenu,sysMenuList));
            }
        }
        return trees;
    }

    // 递归查找下层菜单
    private static SysMenu findChildren(SysMenu sysMenu, List<SysMenu> sysMenuList) {
        // SysMenu类中有属性 private List<SysMenu> children;(存放的是下层数据)
        sysMenu.setChildren(new ArrayList<SysMenu>());
        // 递归查找
        // 用sysMenu的id值 去sysMenuList集合中找parent_id == sysMenu.id 的数据
        for(SysMenu it : sysMenuList){
            if(sysMenu.getId().longValue() == it.getParentId().longValue()){ // 递归入口
                sysMenu.getChildren().add(findChildren(it,sysMenuList));
            }
        }
        return sysMenu;
    }
}
