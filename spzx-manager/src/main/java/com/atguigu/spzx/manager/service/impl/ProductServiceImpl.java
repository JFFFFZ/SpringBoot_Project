package com.atguigu.spzx.manager.service.impl;

import com.atguigu.spzx.manager.mapper.ProductDetailsMapper;
import com.atguigu.spzx.manager.mapper.ProductMapper;
import com.atguigu.spzx.manager.mapper.ProductSkuMapper;
import com.atguigu.spzx.manager.service.CategoryBrandService;
import com.atguigu.spzx.manager.service.ProductService;
import com.atguigu.spzx.model.dto.product.ProductDto;
import com.atguigu.spzx.model.entity.product.Product;
import com.atguigu.spzx.model.entity.product.ProductDetails;
import com.atguigu.spzx.model.entity.product.ProductSku;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductMapper productMapper;
    @Autowired
    private ProductSkuMapper productSkuMapper;
    @Autowired
    private ProductDetailsMapper productDetailsMapper;

    @Override
    public PageInfo<Product> findByPage(Integer page, Integer limit, ProductDto productDto) {
        PageHelper.startPage(page, limit);
        List<Product> list = productMapper.findByPage(productDto);
        PageInfo<Product> productPageInfo = new PageInfo<>(list);
        return productPageInfo;
    }

    @Override
    public void save(Product product) {
        //1 保存商品的基本信息  product表
        product.setStatus(0); // 线上状态：0-初始值，1-上架，-1-自主下架
        product.setAuditStatus(0);// 审核状态：0-初始值，1-审核中，2-审核通过，3-审核不通过
        productMapper.save(product);
        //2 获得商品的SKU列表集合，保存sku信息到product_sku表中
        List<ProductSku> productSkuList = product.getProductSkuList();
        int i = 0;
        for (ProductSku sku : productSkuList) {
            // 商品编号
            sku.setSkuCode(product.getId() + "_" + i);
            ++i;
            // 商品id
            sku.setProductId(product.getId());
            // skuname
            sku.setSkuName(product.getName() + sku.getSkuSpec());
            sku.setSaleNum(0);
            sku.setStatus(0);
            productSkuMapper.save(sku);
        }
        //3 保存商品详情部分 到product_details表中
        ProductDetails productDetails = new ProductDetails();
        productDetails.setProductId(product.getId());
        productDetails.setImageUrls(product.getDetailsImageUrls());
        productDetailsMapper.save(productDetails);
    }

    @Override
    public Product getById(Long id) {
        // 1 根据商品id查询商品基本信息 product
        // 2 根据商品id查询商品sku信息 product_sku
        // 3 根据商品id查询商品详情信息 product_details

        // 1 根据商品id查询商品基本信息 product
        Product product = productMapper.findProductById(id);
        // 2 根据商品id查询商品sku信息 product_sku
        List<ProductSku> productSkuList = productSkuMapper.findProductSkuByProductId(id);
        product.setProductSkuList(productSkuList);
        // 3 根据商品id查询商品详情信息 product_details
        ProductDetails productDetails = productDetailsMapper.findProductDetailsByProductId(id);
        product.setDetailsImageUrls(productDetails.getImageUrls());

        return product;
    }

    @Override
    public void update(Product product) {
        // 修改product表
        productMapper.updateById(product);
        // 修改product_sku表
        List<ProductSku> productSkuList = product.getProductSkuList();
        productSkuList.forEach(productSku -> {
            productSkuMapper.updateById(productSku);
        });
        // 修改product_details表
        String detailsImageUrls = product.getDetailsImageUrls();
        ProductDetails productDetails = productDetailsMapper.findProductDetailsByProductId(product.getId());
        productDetails.setImageUrls(detailsImageUrls);
        productDetailsMapper.updateById(productDetails);
    }

    @Override
    public void deleteById(Long id) {
        // 根据商品id 删除product表中数据
        productMapper.deleteById(id);
        // 根据商品id 删除product_sku表中数据
        productSkuMapper.deleteByProductId(id);
        // 根据商品id 删除product_details表中数据
        productDetailsMapper.deleteByProductId(id);
    }

    // 审核 本质就是设置状态值
    @Override
    public void updateAuditStatus(Long id, Integer auditStatus) {
        Product product = new Product();
        product.setId(id);  // 设置商品id
        if(auditStatus == 1) {
            product.setAuditStatus(1);
            product.setAuditMessage("审批通过");
        } else {
            product.setAuditStatus(-1);
            product.setAuditMessage("审批不通过");
        }
        productMapper.updateById(product);
    }

    // 上下架 本质就是设置状态值
    @Override
    public void updateStatus(Long id, Integer status) {
        Product product = new Product();
        product.setId(id);
        if(status == 1) {
            product.setStatus(1);
        } else {
            product.setStatus(-1);
        }
        productMapper.updateById(product);
    }
}
