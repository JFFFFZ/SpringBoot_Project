package com.atguigu.spzx.manager.service.impl;

import com.alibaba.excel.EasyExcel;
import com.atguigu.spzx.common.exception.GuiguException;
import com.atguigu.spzx.manager.listener.ExcelListener;
import com.atguigu.spzx.manager.mapper.CategoryMapper;
import com.atguigu.spzx.manager.service.CategoryService;
import com.atguigu.spzx.model.entity.product.Category;
import com.atguigu.spzx.model.vo.common.ResultCodeEnum;
import com.atguigu.spzx.model.vo.product.CategoryExcelVo;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryMapper categoryMapper;

    @Override
    public List<Category> findCategoryList(Long id) {
        //1 根据id条件值进行查询
        // select * from category where parent_id = id
         List<Category> categoryList = categoryMapper.selectCategoryByParentId(id);
        //2 遍历返回list集合
        // 判断每个分类是否有下一层的分类，如果有则设置hasChildren=true
        if(!CollectionUtils.isEmpty(categoryList)){
            categoryList.forEach(category -> {
                // 判断每个分类是否有下一层的分类
                int count = categoryMapper.selectCountByParentId(category.getId());
                if(count>0){ // 表示有下一层的分类
                    category.setHasChildren(true);
                }else {
                    category.setHasChildren(false);
                }
            });
        }
        return categoryList;
    }

    // 导出数据
    @Override
    public void exportData(HttpServletResponse response) {
        //1 设置响应头信息 content-disposition 和其他信息
        //2 调用mapper方法查询所有分类 返回list集合
        //3 调用EasyExcel的write方法 完成写操作
        try {
            //1 设置响应头信息 content-disposition 和其他信息
            response.setContentType("application/vnd.ms-excel");
            response.setCharacterEncoding("utf-8");
            // 这里URLEncoder.encode可以防止中文乱码 当然和easyexcel没有关系
            String fileName = URLEncoder.encode("分类数据", "UTF-8");
            // 设置响应头信息 content-disposition
            response.setHeader("Content-disposition", "attachment;filename=" + fileName + ".xlsx");

            //2 调用mapper方法查询所有分类 返回list集合
            List<Category> categoryList = categoryMapper.findAll();
            // 类型转换  List<Category> => List<CategoryExcelVo>
            List<CategoryExcelVo> categoryExcelVoList = new ArrayList<>();
            for(Category category : categoryList){
                CategoryExcelVo categoryExcelVo = new CategoryExcelVo();
                // 使用工具类进行实现复制   只复制名字相同的属性 （复制相同名字的成员变量）
                BeanUtils.copyProperties(category, categoryExcelVo);
                categoryExcelVoList.add(categoryExcelVo);
            }

            //3 调用EasyExcel的write方法 完成写操作
            EasyExcel.write(response.getOutputStream(), CategoryExcelVo.class)
                    .sheet("分类数据")
                    .doWrite(categoryExcelVoList);
        } catch (Exception e) {
            e.printStackTrace();
            throw new GuiguException(ResultCodeEnum.DATA_ERROR);
        }
    }

    // 导入数据
    @Override
    public void importData(MultipartFile file) {
        try {
            // TODO 监听器
            ExcelListener<CategoryExcelVo> excelListener = new ExcelListener(categoryMapper);
            // 包含了读取前端上传的文件 以及存储数据至 数据库
            EasyExcel.read(file.getInputStream(), CategoryExcelVo.class, excelListener)
                    .sheet()
                    .doRead();
        } catch (Exception e) {
            e.printStackTrace();
            throw new GuiguException(ResultCodeEnum.DATA_ERROR);
        }
    }
}
