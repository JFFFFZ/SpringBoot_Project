package com.atguigu.spzx.manager.mapper;

import com.atguigu.spzx.model.dto.product.ProductDto;
import com.atguigu.spzx.model.entity.product.Product;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ProductMapper {

    List<Product> findByPage(ProductDto productDto);

    //保存商品的基本信息
    void save(Product product);

    Product findProductById(Long id);

    void updateById(Product product);

    // 根据商品id 删除product表中数据
    void deleteById(Long id);
}
