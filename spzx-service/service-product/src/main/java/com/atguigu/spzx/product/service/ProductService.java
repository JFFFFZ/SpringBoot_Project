package com.atguigu.spzx.product.service;

import com.atguigu.spzx.model.dto.h5.ProductSkuDto;
import com.atguigu.spzx.model.entity.product.ProductSku;
import com.atguigu.spzx.model.vo.h5.ProductItemVo;
import com.github.pagehelper.PageInfo;

import java.util.List;

public interface ProductService {
    // 根据销量查询商品
    List<ProductSku> selectProductSkuBySaleCount();

    // 分页
    PageInfo<ProductSku> findByPage(Integer page, Integer limit, ProductSkuDto productSkuDto);

    // 商品详情接口
    ProductItemVo item(Long skuId);

    //根据skuId获取商品sku信息
    ProductSku getBySkuId(Long skuId);
}
