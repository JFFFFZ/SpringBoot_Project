package com.atguigu.spzx.manager.controller;

import com.atguigu.spzx.manager.service.ProductService;
import com.atguigu.spzx.model.dto.product.ProductDto;
import com.atguigu.spzx.model.entity.product.Product;
import com.atguigu.spzx.model.vo.common.Result;
import com.atguigu.spzx.model.vo.common.ResultCodeEnum;
import com.github.pagehelper.PageInfo;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value="/admin/product/product")
public class ProductController {
    @Autowired
    private ProductService productService;

    @GetMapping(value = "/{page}/{limit}")
    public Result findByPage(@PathVariable("page") Integer page,
                             @PathVariable("limit") Integer limit,
                             ProductDto productDto) {
        //ProductDto类需要有对应的字段和 getter/setter 方法  才能实现Get方式传入对象的功能
        PageInfo<Product> pageInfo = productService.findByPage(page, limit,productDto);
        return Result.build(pageInfo, ResultCodeEnum.SUCCESS);
    }

    // 添加
    @PostMapping("/save")
    public Result save(@RequestBody Product product) {
        productService.save(product);
        return Result.build(null , ResultCodeEnum.SUCCESS) ;
    }

    // 根据商品id查询商品信息
    @GetMapping("/getById/{id}")
    public Result<Product> getById(@PathVariable Long id) {
        Product product = productService.getById(id);
        return Result.build(product , ResultCodeEnum.SUCCESS) ;
    }

    // 保存修改的数据
    @PutMapping("/updateById")
    public Result updateById(@RequestBody Product product) {
        productService.update(product);
        return Result.build(null , ResultCodeEnum.SUCCESS) ;
    }

    // 删除商品
    @DeleteMapping("/deleteById/{id}")
    public Result deleteById(@PathVariable Long id) {
        productService.deleteById(id);
        return Result.build(null , ResultCodeEnum.SUCCESS) ;
    }

    // 审核 修改 product中的审核状态
    @GetMapping("/updateAuditStatus/{id}/{auditStatus}")
    public Result updateAuditStatus(@PathVariable Long id, @PathVariable Integer auditStatus) {
        productService.updateAuditStatus(id, auditStatus);
        return Result.build(null , ResultCodeEnum.SUCCESS) ;
    }
    // 上架 下架
    @GetMapping("/updateStatus/{id}/{status}")
    public Result updateStatus(@PathVariable Long id, @PathVariable Integer status) {
        productService.updateStatus(id, status);
        return Result.build(null , ResultCodeEnum.SUCCESS) ;
    }


}
