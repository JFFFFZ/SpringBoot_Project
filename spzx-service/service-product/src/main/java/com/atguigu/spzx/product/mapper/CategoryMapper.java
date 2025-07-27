package com.atguigu.spzx.product.mapper;

import com.atguigu.spzx.model.entity.product.Category;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CategoryMapper {
    //  查询所有的一级分类
    List<Category> selectOneCategory();

    // 查询所有分类
    List<Category> findAll();
}
