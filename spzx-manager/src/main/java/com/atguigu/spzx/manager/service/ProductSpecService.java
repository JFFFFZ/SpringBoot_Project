package com.atguigu.spzx.manager.service;

import com.atguigu.spzx.model.entity.product.ProductSpec;
import com.github.pagehelper.PageInfo;

import java.util.List;

public interface ProductSpecService {
    // 分页查询
    PageInfo<ProductSpec> findByPage(Integer page, Integer limit);

    // 添加
    void save(ProductSpec productSpec);

    // 根据传入的id修改
    void updateById(ProductSpec productSpec);

    // 删除
    void deleteById(Integer id);

    List<ProductSpec> findAll();
}
