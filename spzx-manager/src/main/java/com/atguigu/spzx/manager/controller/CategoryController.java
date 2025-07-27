package com.atguigu.spzx.manager.controller;

import com.atguigu.spzx.manager.service.CategoryService;
import com.atguigu.spzx.model.entity.product.Category;
import com.atguigu.spzx.model.vo.common.Result;
import com.atguigu.spzx.model.vo.common.ResultCodeEnum;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping(value="/admin/product/category")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    // 分类的列表方法  每次只查询一层数据  实现懒加载
    // select * from category where parent_id = 0
    @GetMapping("/findCategoryList/{id}")
    public Result findCategoryList(@PathVariable("id") Long id){
        List<Category> list = categoryService.findCategoryList(id);
        return Result.build(list, ResultCodeEnum.SUCCESS);
    }
    // 文件导出
    @GetMapping("/exportData")
    public void exportData(HttpServletResponse response){
        categoryService.exportData(response);
    }

    // 文件导入
    @PostMapping("/importData")
    public Result importData(MultipartFile file){ // @RequestParam("file")  如果没有显式表明 默认按名称匹配 推荐显式表明
        categoryService.importData(file);
        return Result.build(null,ResultCodeEnum.SUCCESS);
    }
}
