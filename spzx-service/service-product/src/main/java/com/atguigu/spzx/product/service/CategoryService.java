package com.atguigu.spzx.product.service;

import com.atguigu.spzx.model.entity.product.Category;

import java.util.List;

public interface CategoryService {

    //  查询所有的一级分类
    List<Category> selectOneCategory();

    // 查询所有分类  并封装成树形结构
    List<Category> findCategoryTree();
}
