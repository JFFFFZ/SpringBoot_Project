package com.atguigu.spzx.product.service.impl;

import com.alibaba.excel.util.StringUtils;
import com.alibaba.fastjson.JSON;
import com.atguigu.spzx.model.entity.product.Category;
import com.atguigu.spzx.product.mapper.CategoryMapper;
import com.atguigu.spzx.product.service.CategoryService;
import com.fasterxml.jackson.databind.type.CollectionLikeType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryMapper categoryMapper;
    @Autowired
    private RedisTemplate<String,String> redisTemplate;

    // 查询所有的一级分类
    @Override
    public List<Category> selectOneCategory() {
        // 1 查询redis 看是否有所有的一级分类
        String categoryOneJson = redisTemplate.opsForValue().get("category:one"); // 获取所有的一级分类JSON
        // 2 如果redis中有所有的一级分类 则获取该一级分类
        if(!StringUtils.isEmpty(categoryOneJson)) {
            // 1.1 转换格式
            List<Category> existCategoryList = JSON.parseArray(categoryOneJson, Category.class);
            return existCategoryList;
        }
        // 3 如果没有 则查询数据库 并将查询结果存储至redis中 并返回
        List<Category> categoryList = categoryMapper.selectOneCategory();
        redisTemplate.opsForValue().set("category:one",
                JSON.toJSONString(categoryList),
                7, TimeUnit.DAYS);
        return categoryList;
    }

    // 查询所有分类  并封装成树形结构  采用递归方式
    // 注意redis中key的实际值 为 value::key 即 category::all
    @Cacheable(value="category",key = " 'all' ")
    @Override
    public List<Category> findCategoryTree() {
        // 1 查询所有分类
        List<Category> allCategoryList = categoryMapper.findAll();
        // 2 根据树的级别一次向下递归查询  递归入口 一级分类 parent_id = 0
        // 根据一级分类的id 查询二级分类  递归向下查询 遍历结束即为递归出口
        // 获取一级分类
        List<Category> oneCategoryList = allCategoryList
                .stream()
                .filter(item -> item.getParentId().longValue() == 0)
                .collect(Collectors.toList());

        oneCategoryList.forEach(oneCategory -> {
            // 获取当前一级分类下的二级分类
            List<Category> twoCategoryList = allCategoryList
                    .stream()
                    .filter(item -> item.getParentId() == oneCategory.getId())
                    .collect(Collectors.toList());
            // 把二级分类封装到一级分类的children中
            oneCategory.setChildren(twoCategoryList);
            // 封装三级分类
            twoCategoryList.forEach(twoCategory->{
                List<Category> threeCategoryList = allCategoryList
                        .stream()
                        .filter(item -> item.getParentId() == twoCategory.getId())
                        .collect(Collectors.toList());
                // 把三级分类封装到二级分类children中
                twoCategory.setChildren(threeCategoryList);
            });
        });

        return oneCategoryList;
    }
}
