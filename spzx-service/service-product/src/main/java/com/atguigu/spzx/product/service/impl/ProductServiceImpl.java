package com.atguigu.spzx.product.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.atguigu.spzx.model.dto.h5.ProductSkuDto;
import com.atguigu.spzx.model.entity.product.Product;
import com.atguigu.spzx.model.entity.product.ProductDetails;
import com.atguigu.spzx.model.entity.product.ProductSku;
import com.atguigu.spzx.model.vo.h5.ProductItemVo;
import com.atguigu.spzx.product.mapper.ProductDetailsMapper;
import com.atguigu.spzx.product.mapper.ProductMapper;
import com.atguigu.spzx.product.mapper.ProductSkuMapper;
import com.atguigu.spzx.product.service.ProductService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ProductServiceImpl implements ProductService {
    @Autowired
    private ProductSkuMapper productSkuMapper;
    @Autowired
    private ProductDetailsMapper productDetailsMapper;
    @Autowired
    private ProductMapper productMapper;
    // 根据销量查询商品
    @Override
    public List<ProductSku> selectProductSkuBySaleCount() {
        List<ProductSku> productSkuList = productSkuMapper.selectProductSkuBySaleCount();
        return productSkuList;
    }

    @Override
    public PageInfo<ProductSku> findByPage(Integer page,
                                           Integer limit,
                                           ProductSkuDto productSkuDto) {
        PageHelper.startPage(page, limit);
        List<ProductSku> productSkuList = productSkuMapper.findByPage(productSkuDto);
        return new PageInfo<>(productSkuList);
    }

    // 商品详情接口
    @Override
    public ProductItemVo item(Long skuId) {
        // 1 创建vo对象  用于封装最终数据
        ProductItemVo productItemVo = new ProductItemVo();

        // 2 根据skuId获取sku信息
        ProductSku productSku = productSkuMapper.getById(skuId);

        // 3 根据第二部获取sku  从sku中获取productId  获取商品信息
        Long productId = productSku.getProductId();
        Product product = productMapper.getById(productId);

        // 4 productId 获取商品详情信息
        ProductDetails productDetails = productDetailsMapper.getByProductId(productId);

        // 5 封装map集合 == 商品规格对应商品skuId信息
        Map<String, Object> skuSpecValueMap = new HashMap<>();
        // 根据 商品id获取商品的所有sku列表
        List<ProductSku> productSkuList = productSkuMapper.findByProductId(productId);
        productSkuList.forEach(item->{
            skuSpecValueMap.put(item.getSkuSpec(),item.getId());
        });
        // 6 把需要的数据封装到productItemVo对象中
        productItemVo.setProduct(product);
        productItemVo.setProductSku(productSku);
        productItemVo.setSkuSpecValueMap(skuSpecValueMap);
        // 封装详情的图片 Vo中是一个list集合  需要格式转换
        String imageUrls = productDetails.getImageUrls();
        String[] split = imageUrls.split(",");  // 字符串分割
        List<String> list = Arrays.asList(split);  // 将数组转换为list
        productItemVo.setDetailsImageUrlList(list);
        // 封装轮播图  list集合
        productItemVo.setSliderUrlList(Arrays.asList(product.getSliderUrls().split(",")));
        // 封装商品规格数据
        JSONArray jsonArray = JSON.parseArray(product.getSpecValue());
        productItemVo.setSpecValueList(jsonArray);
        return productItemVo;
    }

    //根据skuId获取商品sku信息
    @Override
    public ProductSku getBySkuId(Long skuId) {
        ProductSku productSku = productSkuMapper.getById(skuId);
        return productSku;
    }
}
