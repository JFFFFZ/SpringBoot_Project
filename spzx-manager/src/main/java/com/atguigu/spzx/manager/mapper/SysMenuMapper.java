package com.atguigu.spzx.manager.mapper;

import com.atguigu.spzx.model.entity.system.SysMenu;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SysMenuMapper {
    List<SysMenu> findAll();

    void save(SysMenu sysMenu);

    void update(SysMenu sysMenu);

    void delete(Long id);

    // 查询是否有子菜单
    int selectCountById(Long id);

    List<SysMenu> findSysMenuByUserId(Long userId);

    SysMenu selectParentMenu(Long parentId);
}
