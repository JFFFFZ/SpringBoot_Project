package com.atguigu.spzx.cart.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.spzx.cart.service.CartService;
import com.atguigu.spzx.feign.product.ProductFeignClient;
import com.atguigu.spzx.model.entity.h5.CartInfo;
import com.atguigu.spzx.model.entity.product.Product;
import com.atguigu.spzx.model.entity.product.ProductSku;
import com.atguigu.spzx.model.entity.user.UserInfo;
import com.atguigu.spzx.utils.AuthContextUtil;
import lombok.val;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CartServiceImpl implements CartService {
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    @Autowired
    private ProductFeignClient productFeignClient;

    @Override
    public void addToCart(Long skuId, Integer skuNum) {
        // 1 首先是登录的状态，并获取用户id 作为redis中hash类型的key值
        // 可以从ThreadLocal中获取userInfo
        UserInfo userInfo = AuthContextUtil.getUserInfo();
        Long userId = userInfo.getId();
        String cartKey = this.getCartKey(userId);// 构建hash类型的key值
        // 2 因为购物车放在redis中
        // hash类型  key：userId field：skuId value：skuInfo 商品信息
        // 从redis中获取购物车数据 根据用户id+skuid获取（hash类型 通过key field 才能得到对应的value）
        Object cartInfoObject = redisTemplate.opsForHash().get(cartKey, String.valueOf(skuId));
        // 3 cartInfoObject非空 表示购物车中有同类商品  则购物车该商品的数量增加
        CartInfo cartInfo = null;
        if(cartInfoObject!=null){
            // 类型转换
            cartInfo = JSON.parseObject(cartInfoObject.toString(),CartInfo.class);
            // 修改数量  数量相加
            cartInfo.setSkuNum(cartInfo.getSkuNum() + skuNum);
            // 设置属性isChecked为1  表示购物车中的商品是选中状态  用于前端显示用的参数
            cartInfo.setIsChecked(1);
            cartInfo.setUpdateTime(new Date());
        }else{// 4 不同类商品（购物车中没有）则增加商品sku至购物车redis
            // 远程调用实现根据skuid获取商品sku信息  通过nacos+Openfeign实现
            cartInfo = new CartInfo();
            // TODO:远程调用实现根据skuId获取商品sku信息
            ProductSku productSku = productFeignClient.getBySkuId(skuId);
            // 设置相关的数据到cartInfo对象中
            cartInfo.setCartPrice(productSku.getSalePrice());cartInfo.setSkuNum(skuNum);cartInfo.setSkuId(skuId);
            cartInfo.setUserId(userId);cartInfo.setImgUrl(productSku.getThumbImg());cartInfo.setSkuName(productSku.getSkuName());
            cartInfo.setIsChecked(1);cartInfo.setCreateTime(new Date());cartInfo.setUpdateTime(new Date());
        }
        // 将sku信息（商品信息cartInfo）添加到redis中
        redisTemplate.opsForHash().put(cartKey,
                String.valueOf(skuId),
                JSON.toJSONString(cartInfo));
    }

    // 查询购物车
    @Override
    public List<CartInfo> getCartList() {
        // 1 构建 用于查询redis的 key的值  使用getCartkey(userId即可得到)
        UserInfo userInfo = AuthContextUtil.getUserInfo();
        Long userId = userInfo.getId();
        String cartKey = this.getCartKey(userId);
        // 2 根据key从redis里面hash类型获取所有value值 即cartInfo对象集合
        List<Object> valueList = redisTemplate.opsForHash().values(cartKey);
        // 上面是Object类型 要类型转换  List<Object> => List<CartInfo>
        // 遍历集合，逐个转换得到cartInfo对象集合
        if(!CollectionUtils.isEmpty(valueList)){
            List<CartInfo> cartInfoList = valueList
                    .stream()
                    .map(cartInfoObject -> JSON.parseObject(cartInfoObject.toString(), CartInfo.class))
                    .sorted((o1, o2) -> o2.getCreateTime().compareTo(o1.getCreateTime()))
                    .collect(Collectors.toList());
            return cartInfoList;
        }
        // 空的集合不是null  而是[]
        return new ArrayList<>();
    }

    // 删除购物车商品
    @Override
    public void deleteCart(Long skuId) {
        UserInfo userInfo = AuthContextUtil.getUserInfo();
        Long userId = userInfo.getId();
        String cartKey = this.getCartKey(userId);
        redisTemplate.opsForHash().delete(cartKey,String.valueOf(skuId));
    }

    // 更新购物车中商品是否选中 选/不选
    @Override
    public void checkCart(Long skuId, Integer isChecked) {

        // 1 获取当前登录的用户数据 得到 用于查询redis中购物车hash数据 的key值
        Long userId = AuthContextUtil.getUserInfo().getId();
        String cartKey = this.getCartKey(userId);
        // 2 判断购物车中是否有商品  即key是否包含了field 若没有即购物车中没有东西 直接结束
        Boolean hasKey = redisTemplate.opsForHash().hasKey(cartKey, String.valueOf(skuId));
        if(hasKey==false){
            return;
        }
        // 3 若有 根据key和field获取value值 即cartInfo对象
        String cartInfoString = redisTemplate.opsForHash().get(cartKey, String.valueOf(skuId))
                .toString();
        // 4 修改3中得到的cartInfo对象的选中状态
        CartInfo cartInfo = JSON.parseObject(cartInfoString, CartInfo.class);
        cartInfo.setIsChecked(isChecked);
        // 5 将修改后的cartInfo对象重新添加到redis中
        redisTemplate.opsForHash().put(cartKey,
                String.valueOf(skuId),
                JSON.toJSONString(cartInfo));
    }

    // 更新购物车中商品是否全选  全选/ 全不选
    @Override
    public void allCheckCart(Integer isChecked) {
        // 1 获取当前登录的用户数据 得到 用于查询redis中购物车hash数据 的key值
        Long userId = AuthContextUtil.getUserInfo().getId();
        String cartKey = this.getCartKey(userId);
        // 2 根据key 获取购物车中的商品（是个List集合）
        List<Object> cartInfoObjList = redisTemplate.opsForHash().values(cartKey);
        // 购物车中不含有商品
        if(CollectionUtils.isEmpty(cartInfoObjList)){
            return;
        }// 不为空
        // 3 遍历集合中的所有商品 cartInfo 修改其选中状态isCheded
        // 类型转换 转换的过程中 修改选中状态 并放入到redis中  可以减少一次遍历
        cartInfoObjList.stream()
                .map(cartInfoObj -> {
                    CartInfo cartInfo = JSON.parseObject(cartInfoObj.toString(), CartInfo.class);
                    // 4 修改选中状态 并将修改后的cartInfo对象重新添加到redis中
                    cartInfo.setIsChecked(isChecked);
                    redisTemplate.opsForHash().put(cartKey,
                            String.valueOf(cartInfo.getSkuId()),
                            JSON.toJSONString(cartInfo));
                    return cartInfo;
                }).forEach(cartInfo -> {
                    // 也可以将上面的 修改和 放入redis的操作 放在forEach里面
                });
        // 注意使用终结操作  例如 .collect(Collectors.toList()); .forEach .count()等 否则Stream流会不执行
    }

    // 清空购物车中商品
    @Override
    public void clearCart() {
        Long userId = AuthContextUtil.getUserInfo().getId();
        String cartKey = this.getCartKey(userId);
        redisTemplate.delete(cartKey);
    }

    //查询获取 购物车中选中的所有商品
    @Override
    public List<CartInfo> getAllCkecked() {
        // 1 获取当前登录的用户数据 得到 用于查询redis中购物车hash数据 的key值
        Long userId = AuthContextUtil.getUserInfo().getId();
        String cartKey = this.getCartKey(userId);
        // 2 根据key 获取购物车中的商品（是个List集合）
        List<Object> cartInfoObjList = redisTemplate.opsForHash().values(cartKey);
        if(CollectionUtils.isEmpty(cartInfoObjList)){
            return new ArrayList<>();
        }
        // 3 类型转换得到购物车信息  注意：要使用.filter过滤掉未选中的
        List<CartInfo> cartInfoListChoosed = cartInfoObjList.stream()
                .map(cartInfoObj -> {
                    CartInfo cartInfo = JSON.parseObject(cartInfoObj.toString(), CartInfo.class);
                    return cartInfo;
                })
                .filter(cartInfo -> cartInfo.getIsChecked()==1) // 过滤掉未选中的
                .collect(Collectors.toList());
        return cartInfoListChoosed;
    }

    @Override
    public void deleteChecked() {
        // 1 获取当前登录的用户数据 得到 用于查询redis中购物车hash数据 的key值
        Long userId = AuthContextUtil.getUserInfo().getId();
        String cartKey = this.getCartKey(userId);
        // 2 根据key 获取购物车中的商品（是个List集合）
        List<Object> cartInfoObjList = redisTemplate.opsForHash().values(cartKey);
        if(CollectionUtils.isEmpty(cartInfoObjList)){
            return;
        }
        // 3 删除选中的商品
        cartInfoObjList.stream()
                .map(cartInfoObj -> {
            CartInfo cartInfo = JSON.parseObject(cartInfoObj.toString(), CartInfo.class);
            return cartInfo;})
                .filter(cartInfo -> cartInfo.getIsChecked()==1)
                .forEach(cartInfo -> {
                    redisTemplate.opsForHash().delete(cartKey,String.valueOf(cartInfo.getSkuId()));
                });
    }

    private String getCartKey(Long userId) {
        //定义key user:cart:userId
        return "user:cart:" + userId;
    }
}
