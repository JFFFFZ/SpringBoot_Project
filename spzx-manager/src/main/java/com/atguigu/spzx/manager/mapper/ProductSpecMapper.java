package com.atguigu.spzx.manager.mapper;

import com.atguigu.spzx.model.entity.product.ProductSpec;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ProductSpecMapper {
    // 查询所有方法
    List<ProductSpec> findAll();

    // 添加保存
    void save(ProductSpec productSpec);

    // 修改操作
    void update(ProductSpec productSpec);

    // 根据id进行逻辑删除
    void delete(Integer id);
}
