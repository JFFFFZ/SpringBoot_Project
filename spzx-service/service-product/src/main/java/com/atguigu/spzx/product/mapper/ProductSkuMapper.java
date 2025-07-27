package com.atguigu.spzx.product.mapper;

import com.atguigu.spzx.model.dto.h5.ProductSkuDto;
import com.atguigu.spzx.model.entity.product.ProductSku;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ProductSkuMapper {
    // 根据销量查询商品
    List<ProductSku> selectProductSkuBySaleCount();

    List<ProductSku> findByPage(ProductSkuDto productSkuDto);

    //根据skuId获取商品sku信息
    ProductSku getById(Long skuId);

    List<ProductSku> findByProductId(Long productId);
}
