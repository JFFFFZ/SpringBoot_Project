package com.atguigu.spzx.manager.mapper;

import com.atguigu.spzx.model.entity.product.ProductSku;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ProductSkuMapper {
    // 添加保存sku信息
    void save(ProductSku sku);

    List<ProductSku> findProductSkuByProductId(Long id);

    void updateById(ProductSku productSku);

    void deleteByProductId(Long id);
}
