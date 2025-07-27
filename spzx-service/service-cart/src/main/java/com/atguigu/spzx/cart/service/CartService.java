package com.atguigu.spzx.cart.service;

import com.atguigu.spzx.model.entity.h5.CartInfo;

import java.util.List;

public interface CartService {
    // 添加购物车
    void addToCart(Long skuId, Integer skuNum);

    // 查询购物车
    List<CartInfo> getCartList();

    // 删除购物车商品
    void deleteCart(Long skuId);

    // 更新购物车中商品是否选中 选/不选
    void checkCart(Long skuId, Integer isChecked);
    // 更新购物车中商品是否全选  全选/ 全不选
    void allCheckCart(Integer isChecked);
    // 清空购物车中商品
    void clearCart();

    //查询获取 购物车中选中的所有商品
    List<CartInfo> getAllCkecked();

    // 删除生成订单的购物车商品
    void deleteChecked();
}
